package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.config.ConfigManager;
import me.timvinci.terrastorage.item.GhostItemEntity;
import me.timvinci.terrastorage.item.StackIdentifier;
import me.timvinci.terrastorage.item.StackProcessor;
import me.timvinci.terrastorage.util.ComparatorTypes;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.SortType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * A utility class for inventory/item related operations.
 */
public class InventoryUtils {

    /**
     * Collects the storage side slots of a menu, meaning every slot that isn't backed by the player's inventory.
     * The player's inventory instance is used as the discriminator rather than an instanceof check, so modded
     * storages that happen to be backed by a vanilla Inventory are still treated as storage.
     * @param menu The player's open menu.
     * @param player The player.
     * @return The storage side slots, in menu order.
     */
    public static List<Slot> getStorageSlots(AbstractContainerMenu menu, Player player) {
        Inventory playerInventory = player.getInventory();
        List<Slot> storageSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container != playerInventory) {
                storageSlots.add(slot);
            }
        }

        return storageSlots;
    }

    /**
     * Checks whether a storage has any slot that could take part in a Terrastorage operation, meaning a slot that can
     * be taken from, or an empty slot that could be deposited into.
     * Emptiness is used rather than mayPlace since the latter depends on the incoming stack, and rather than
     * mayPickup since item handler backed slots commonly report empty slots as un-pickable, which would wrongly mark
     * an empty storage as restricted.
     * @param storageSlots The storage side slots of the menu.
     * @param player The player.
     * @return True if any slot is usable, false if the storage is entirely restricted.
     */
    public static boolean hasUsableSlot(List<Slot> storageSlots, Player player) {
        for (Slot slot : storageSlots) {
            if (slot.getItem().isEmpty() || slot.mayPickup(player)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Transfers a stack into the player's inventory, first attempting to top off existing stacks of the same item,
     * then filling empty slots. Each placement is capped at the target slot's max stack size, so an oversized stack
     * from a stack size upgraded storage is split across slots rather than dumped into one.
     * The provided stack is shrunk by the amount transferred.
     * @param access The rule aware view of the player's inventory.
     * @param receiverState An InventoryState object of the player's inventory.
     * @param stack The stack to transfer.
     */
    public static void transferStack(PlayerSlotAccess access, InventoryState receiverState, ItemStack stack) {
        Inventory to = access.inventory();
        StackIdentifier stackIdentifier = new StackIdentifier(stack);

        // Attempt to transfer the stack to an existing item stack of the same item.
        if (receiverState.getNonFullItemSlots().containsKey(stackIdentifier) && transferToExistingStack(access, receiverState, stack)) {
            return;
        }

        // Iterate the empty slots without consuming ones that reject this item, so that a single rejected item
        // doesn't starve every later item in the same operation.
        Iterator<Integer> emptyIterator = receiverState.getEmptySlots().iterator();
        while (!stack.isEmpty() && emptyIterator.hasNext()) {
            int emptySlot = emptyIterator.next();
            if (!to.getItem(emptySlot).isEmpty() || !access.canPlace(emptySlot, stack)) {
                continue;
            }

            int maxStackSize = access.maxStackSize(emptySlot, stack);
            if (maxStackSize <= 0) {
                continue;
            }

            ItemStack placed = stack.split(Math.min(stack.getCount(), maxStackSize));
            to.setItem(emptySlot, placed);
            emptyIterator.remove();
            receiverState.setModified();
            // Track the slot as non-full if it didn't receive a full stack, so later transfers can top it off.
            if (placed.getCount() < maxStackSize) {
                receiverState.getNonFullItemSlots().computeIfAbsent(stackIdentifier, k -> new ArrayList<>()).add(emptySlot);
            }
        }
    }

    /**
     * Transfers a stack into one or more existing non-full stacks of the same item in the player's inventory.
     * @param access The rule aware view of the player's inventory.
     * @param receiverState An InventoryState object of the player's inventory.
     * @param stackToTransfer The stack to transfer.
     * @return True if the entire stack was transferred, false otherwise.
     */
    public static boolean transferToExistingStack(PlayerSlotAccess access, InventoryState receiverState, ItemStack stackToTransfer) {
        Inventory to = access.inventory();
        StackIdentifier stackIdentifier = new StackIdentifier(stackToTransfer);
        ArrayList<Integer> slotsWithItem = receiverState.getNonFullItemSlots().get(stackIdentifier);
        Iterator<Integer> slotsIterator = slotsWithItem.iterator();

        while (slotsIterator.hasNext() && !stackToTransfer.isEmpty()) {
            int slotWithItem = slotsIterator.next();
            ItemStack existingStack = to.getItem(slotWithItem);
            if (!access.canPlace(slotWithItem, stackToTransfer)) {
                slotsIterator.remove();
                continue;
            }

            int spaceLeft = access.maxStackSize(slotWithItem, existingStack) - existingStack.getCount();
            if (spaceLeft <= 0) {
                // The slot is already at (or over) its capacity; it isn't a merge target after all.
                slotsIterator.remove();
                continue;
            }

            int transferAmount;
            // Check if the about to be combined stack will be a full stack.
            if (spaceLeft <= stackToTransfer.getCount()) {
                transferAmount = spaceLeft;
                // Remove this about to be full stack slot from the receiver's itemSlots.
                slotsIterator.remove();
            }
            else {
                transferAmount = stackToTransfer.getCount();
            }

            existingStack.grow(transferAmount);
            stackToTransfer.shrink(transferAmount);
            receiverState.setModified();
        }

        if (slotsWithItem.isEmpty()) {
            receiverState.getNonFullItemSlots().remove(stackIdentifier);
        }
        return stackToTransfer.isEmpty();
    }

    /**
     * Combines a list of gathered item stacks, splitting them at the item's max stack size, then sorts them.
     * The given stacks are mutated and reused, so they must already be copies detached from any inventory.
     * @param stacks The gathered stacks to combine.
     * @param type The sorting type to use.
     * @return A sorted list of combined stacks.
     */
    public static List<ItemStack> combineAndSort(List<ItemStack> stacks, SortType type) {
        List<ItemStack> combinedStacks = new ArrayList<>();
        // Rough estimate for an efficient initial capacity for the lastStackMap.
        // Use a map in which the key is an item and the value is the last stack of that item.
        Map<StackIdentifier, ItemStack> lastStackMap = new HashMap<>(Math.max(16, stacks.size() / 3));

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getMaxStackSize() <= 1 || stack.getCount() == stack.getMaxStackSize()) {
                combinedStacks.add(stack);
                continue;
            }

            StackIdentifier identifier = new StackIdentifier(stack);
            ItemStack lastStack = lastStackMap.get(identifier);

            if (lastStack == null || lastStack.getCount() == stack.getMaxStackSize()) {
                combinedStacks.add(stack);
                lastStackMap.put(identifier, stack);
            }
            else {
                int spaceLeft = lastStack.getMaxStackSize() - lastStack.getCount();
                if (spaceLeft < stack.getCount()) {
                    lastStack.grow(spaceLeft);
                    stack.shrink(spaceLeft);
                    combinedStacks.add(stack);
                    lastStackMap.put(identifier, stack);
                }
                else {
                    lastStack.grow(stack.getCount());
                }
            }
        }

        combinedStacks.sort(getComparator(type));
        return combinedStacks;
    }

    /**
     * Combines a list of gathered item stacks into one merged stack per stack identifier, then sorts them.
     * Unlike {@link #combineAndSort} the merged stacks aren't split by the item's max stack size, since the storage
     * insertion splits them by each slot's real capacity, which lets stack size upgraded storages consolidate
     * properly instead of being capped at the item's default max stack size.
     * @param stacks The gathered stacks (copies) to combine.
     * @param type The sorting type to use.
     * @return A sorted list of merged stacks.
     */
    public static List<ItemStack> combineForStorageSort(List<ItemStack> stacks, SortType type) {
        Map<StackIdentifier, ItemStack> merged = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            StackIdentifier identifier = new StackIdentifier(stack);
            ItemStack existing = merged.get(identifier);
            if (existing == null) {
                merged.put(identifier, stack.copy());
            }
            else {
                existing.grow(stack.getCount());
            }
        }

        List<ItemStack> result = new ArrayList<>(merged.values());
        result.sort(getComparator(type));
        return result;
    }

    /**
     * @param type The sorting type.
     * @return The comparator corresponding to the given sorting type.
     */
    public static Comparator<ItemStack> getComparator(SortType type) {
        return switch (type) {
            case ITEM_GROUP -> ComparatorTypes.BY_GROUP;
            case ITEM_COUNT -> ComparatorTypes.BY_COUNT;
            case ITEM_RARITY -> ComparatorTypes.BY_RARITY;
            case ITEM_NAME -> ComparatorTypes.BY_NAME;
            case ITEM_ID -> ComparatorTypes.BY_ID;
            default -> throw new IllegalArgumentException("Unknown sort type: " + type);
        };
    }


    /**
     * Gets the storages that are nearby the player, as well as their position.
     * @param player The player.
     * @return A list consisting of pairs of inventories and their position.
     */
    public static List<Pair<Container, Vec3>> getNearbyStorages(ServerPlayer player) {
        Level world = player.level();
        List<Pair<Container, Vec3>> nearbyStorages = new ArrayList<>();
        Set<BlockPos> processedChests = new HashSet<>();

        // Getting the range, and whether the los check is enabled.
        int range = ConfigManager.getInstance().getConfig().getQuickStackRange();
        boolean performLosCheck = ConfigManager.getInstance().getConfig().getLineOfSightCheck();
        BlockPos playerPos = player.blockPosition();

        BlockPos.withinManhattan(playerPos, range, range, range).forEach(pos -> {
            if (processedChests.contains(pos)) {
                return;
            }

            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.hasBlockEntity()) {
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Container inventory && inventory.getContainerSize() >= 27) {
                if (blockEntity instanceof BaseContainerBlockEntity lockable && !lockable.canOpen(player)) {
                    return; // Skip locked containers.
                }

                Vec3 losPoint;
                if (performLosCheck) {
                    losPoint = hasLineOfSight(player, world, pos);
                    // Return if the player doesn't have line of sight to the block entity.
                    if (losPoint == Vec3.ZERO) {
                        return;
                    }
                }
                else {
                    losPoint = Vec3.atCenterOf(pos);
                }

                if (blockEntity instanceof ChestBlockEntity) {
                    ChestType chestType = state.getValue(ChestBlock.TYPE);
                    if (chestType == ChestType.SINGLE) {
                        nearbyStorages.add(Pair.of(inventory, losPoint));
                        return;
                    }

                    BlockPos neighboringChestPos = getNeighboringChestPos(pos, chestType, state.getValue(ChestBlock.FACING));
                    Vec3 doubleChestLosPoint = getDoubleChestCenter(losPoint, Vec3.atCenterOf(neighboringChestPos));
                    Container neighboringChestInventory = (Container) world.getBlockEntity(neighboringChestPos);

                    CompoundContainer doubleInventory = chestType == ChestType.RIGHT ?
                            new CompoundContainer(inventory, neighboringChestInventory) :
                            new CompoundContainer(neighboringChestInventory, inventory);
                    nearbyStorages.add(Pair.of(doubleInventory, doubleChestLosPoint));
                    processedChests.add(neighboringChestPos);
                }
                else {
                    nearbyStorages.add(Pair.of(inventory, losPoint));
                }
            }
        });

        AABB searchBox = new AABB(playerPos).inflate(range);
        world.getEntities(EntityTypeTest.forClass(VehicleEntity.class), searchBox, entity ->
        entity instanceof Container inventory && inventory.getContainerSize() >= 27)
            .forEach(entity -> {
                Vec3 losPoint;
                if (performLosCheck) {
                    losPoint = hasLineOfSightToEntity(player, world, entity);
                    if (losPoint == Vec3.ZERO) {
                        return;
                    }
                }
                else {
                    losPoint = entity.getBoundingBox().getCenter();
                }

                nearbyStorages.add(Pair.of((Container) entity, losPoint));
            }
        );

        return nearbyStorages;
    }

    /**
     * Calculates the block position of the second chest in a double chest setup based on its orientation.
     * @param chestBlockPos The position of one part of the double chest.
     * @param chestType The chest type (LEFT or RIGHT).
     * @param facing The direction the double chest is facing.
     * @return The block position of the neighboring chest.
     */
    private static BlockPos getNeighboringChestPos(BlockPos chestBlockPos, ChestType chestType, Direction facing) {
        return chestBlockPos.relative(chestType == ChestType.LEFT ?
                facing.getClockWise() :
                facing.getCounterClockWise());
    }

    /**
     * Calculates the center point of a double chest, averaging the positions of both chest parts.
     * @param losPoint The line of sight point from the player to one part of the double chest.
     * @param secondChestCenter The center position of the second chest block in the double chest.
     * @return The center point of the entire double chest.
     */
    private static Vec3 getDoubleChestCenter(Vec3 losPoint, Vec3 secondChestCenter) {
        return (losPoint.add(secondChestCenter.x, losPoint.y, secondChestCenter.z)).scale(0.5);
    }

    /**
     * Checks if the player has line of sight to either the chest's center, its top face's center, or its bottom face's
     * center.
     * @param player The player.
     * @param world The world in which the player and the block entity are in.
     * @param pos The block position of the block entity
     * @return The point that the player has line of sight to, or Vec3d.ZERO if the player doesn't have line of sight.
     */
    private static Vec3 hasLineOfSight(ServerPlayer player, Level world, BlockPos pos) {
        Vec3 playerEyes = player.getEyePosition();
        Vec3 centerPos = Vec3.atCenterOf(pos);

        // Define the points to check (center, top center, bottom center)
        Vec3[] pointsToCheck = new Vec3[] {
                centerPos,
                centerPos.add(0, 0.5, 0),
                centerPos.add(0, -0.5, 0)
        };

        for (Vec3 end : pointsToCheck) {
            ClipContext context = new ClipContext(playerEyes, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            BlockHitResult hitResult = world.clip(context);

            if (hitResult.getBlockPos().equals(pos) || hitResult.getType() == HitResult.Type.MISS) {
                return end;
            }
        }

        return Vec3.ZERO;
    }

    /**
     * Checks if the player has line of sight to the entity's center.
     * @param player The player.
     * @param world The world in which the player and the entity are in.
     * @param entity The entity.
     * @return The center of the entity, or Vec3d.ZERO if the player doesn't have line of sight.
     */
    private static Vec3 hasLineOfSightToEntity(ServerPlayer player, Level world, Entity entity) {
        Vec3 playerEyes = player.getEyePosition();
        Vec3 end = entity.getBoundingBox().getCenter();

        BlockHitResult result = world.clip(new ClipContext(
                playerEyes, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        return result.getType() == HitResult.Type.MISS ? end : Vec3.ZERO;
    }

    /**
     * Triggers the item animation that occurs after Quick Stack To Nearby Storages is used.
     * @param world The server world.
     * @param playerEyes The position of the player's eyes.
     * @param animationMap An animation map consisting of target positions and lists of items.
     */
    public static void triggerFlyOutAnimation(ServerLevel world, Vec3 playerEyes, int itemAnimationLength, Map<Vec3, ArrayList<Item>> animationMap) {
        int itemAnimationInterval = ConfigManager.getInstance().getConfig().getItemAnimationInterval();

        for (Map.Entry<Vec3, ArrayList<Item>> entry : animationMap.entrySet()) {
            Vec3 targetPos = entry.getKey();
            Vec3 itemVelocity = new Vec3(
                    (targetPos.x - playerEyes.x) / itemAnimationLength,
                    (targetPos.y - playerEyes.y) / itemAnimationLength,
                    (targetPos.z - playerEyes.z) / itemAnimationLength
            );
            ArrayList<Item> items = entry.getValue();

            for (int i = 0; i < items.size(); i++) {
                int movementDelay = i * itemAnimationInterval;
                GhostItemEntity ghostItem = new GhostItemEntity(
                        world,
                        playerEyes.x,
                        playerEyes.y,
                        playerEyes.z,
                        items.get(i).getDefaultInstance(),
                        itemVelocity,
                        itemAnimationLength,
                        movementDelay
                );

                world.addFreshEntity(ghostItem);
            }
        }
    }

    /**
     * Builds a stack identifier for a player stack with any item favoriting data stripped out.
     * Stack identifiers compare components exactly, so this is what lets Loot All and Restock top off a stack the
     * player has favorited.
     * @param stack The player's stack.
     * @return The stack's identifier, ignoring whether it is favorite.
     */
    public static StackIdentifier favoriteAgnosticIdentifier(ItemStack stack) {
        if (!ItemFavoritingUtils.isFavorite(stack)) {
            return new StackIdentifier(stack);
        }

        PatchedDataComponentMap components = new PatchedDataComponentMap(stack.getComponents());
        ItemFavoritingUtils.unFavorite(components);
        return new StackIdentifier(stack.getItem(), components);
    }

    /**
     * This method acts similarly to the original ItemStack.areItemsAndComponentsEqual, but it will also return true
     * for any two item stacks whose only component difference is one being favorite while the other isn't.
     */
    public static boolean areItemsAndComponentsEqual(ItemStack firstStack, ItemStack secondStack) {
        return ItemStack.isSameItem(firstStack, secondStack) && areComponentsEqual(firstStack, secondStack);
    }

    /**
     * As stated in the description of the areItemsAndComponentsEqual method above, this method will also return true
     * for any two item stacks whose only component difference is one being favorite while the other isn't.
     */
    private static boolean areComponentsEqual(ItemStack firstStack, ItemStack secondStack) {
        if (Objects.equals(firstStack.getComponents(), secondStack.getComponents())) {
            return true;
        }

        boolean firstStackIsFavorite = ItemFavoritingUtils.isFavorite(firstStack);
        boolean secondStackIsFavorite = ItemFavoritingUtils.isFavorite(secondStack);

        if (firstStackIsFavorite ^ secondStackIsFavorite) {
            PatchedDataComponentMap firstComponentMap = new PatchedDataComponentMap(firstStack.getComponents());
            PatchedDataComponentMap secondComponentMap = new PatchedDataComponentMap(secondStack.getComponents());
            if (firstStackIsFavorite) { // Case 1 - First stack is favorite and the second stack isn't.
                ItemFavoritingUtils.unFavorite(firstComponentMap);
            }
            else { // Case 2 - Second stack is favorite and the first stack isn't.
                ItemFavoritingUtils.unFavorite(secondComponentMap);
            }

            return areComponentMapsEqual(firstComponentMap, secondComponentMap);
        }
        else {
            return false;
        }
    }

    public static boolean areComponentMapsEqual(PatchedDataComponentMap firstMap, PatchedDataComponentMap secondMap) {
        if (firstMap.size() != secondMap.size()) {
            return false;
        }

        for (DataComponentType<?> type : firstMap.keySet()) {
            Object value1 = firstMap.get(type);
            Object value2 = secondMap.get(type);

            if (!Objects.equals(value1, value2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Inserts a stack into a storage, first topping off existing non-full stacks of the same item, then filling
     * empty slots. The provided stack is shrunk by the amount inserted.
     * @param access The storage access.
     * @param state The storage state of the storage.
     * @param stack The stack to insert.
     */
    public static void insertIntoStorage(StorageAccess access, StorageState state, ItemStack stack) {
        StackIdentifier identifier = new StackIdentifier(stack);
        insertIntoExistingStorageStacks(access, state, identifier, stack);
        if (stack.isEmpty()) {
            return;
        }

        // Iterate the empty slots without consuming ones that reject this item, so that a single rejected item
        // doesn't starve every later item in the same operation.
        Iterator<Integer> emptyIterator = state.getEmptySlots().iterator();
        while (!stack.isEmpty() && emptyIterator.hasNext()) {
            int index = emptyIterator.next();
            int before = stack.getCount();
            access.insert(index, stack);
            if (stack.getCount() == before) {
                // The slot rejected the item; leave it available for other items.
                continue;
            }

            emptyIterator.remove();
            state.setModified();
            ItemStack placed = access.get(index);
            if (placed.getCount() < access.maxStackSize(index, placed)) {
                state.getNonFullItemSlots().computeIfAbsent(identifier, k -> new ArrayList<>()).add(index);
            }
        }
    }

    /**
     * Inserts a stack into a storage, only into existing non-full stacks of the same item, without creating new
     * stacks. The provided stack is shrunk by the amount inserted.
     * @param access The storage access.
     * @param state The storage state of the storage.
     * @param stack The stack to insert.
     */
    public static void insertIntoExistingStorageStacks(StorageAccess access, StorageState state, ItemStack stack) {
        insertIntoExistingStorageStacks(access, state, new StackIdentifier(stack), stack);
    }

    private static void insertIntoExistingStorageStacks(StorageAccess access, StorageState state, StackIdentifier identifier, ItemStack stack) {
        ArrayList<Integer> slots = state.getNonFullItemSlots().get(identifier);
        if (slots == null) {
            return;
        }

        Iterator<Integer> iterator = slots.iterator();
        while (iterator.hasNext() && !stack.isEmpty()) {
            int index = iterator.next();
            int before = stack.getCount();
            access.insert(index, stack);
            if (stack.getCount() == before) {
                // The slot didn't accept the item; stop considering it for this identifier.
                iterator.remove();
                continue;
            }

            state.setModified();
            ItemStack existing = access.get(index);
            if (existing.getCount() >= access.maxStackSize(index, existing)) {
                iterator.remove();
            }
        }

        if (slots.isEmpty()) {
            state.getNonFullItemSlots().remove(identifier);
        }
    }

    /**
     * Creates a stack processor for inserting player items into a storage, based on the quick stacking mode.
     * @param access The storage access.
     * @param state The storage state of the storage.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     * @return A processor that inserts an eligible player stack into the storage according to the given mode.
     */
    public static StackProcessor createStorageStackProcessor(StorageAccess access, StorageState state, boolean smartDepositMode) {
        return smartDepositMode ?
            new StackProcessor(
                    stack -> {
                        if (stack.isEmpty()) {
                            return false;
                        }
                        StackIdentifier identifier = new StackIdentifier(stack);
                        return state.getNonFullItemSlots().containsKey(identifier) ||
                                (state.getStoredItems().contains(identifier) && !state.getEmptySlots().isEmpty());
                    },
                    stack -> insertIntoStorage(access, state, stack)
            ) :
            new StackProcessor(
                    stack -> !stack.isEmpty() &&
                            state.getNonFullItemSlots().containsKey(new StackIdentifier(stack)),
                    stack -> insertIntoExistingStorageStacks(access, state, stack)
            );
    }
}
