package me.timvinci.inventory;

import me.timvinci.config.ConfigManager;
import me.timvinci.item.GhostItemEntity;
import me.timvinci.mixin.DoubleInventoryAccessor;
import me.timvinci.util.ComparatorTypes;
import me.timvinci.util.SortType;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A utility class for inventory/item related operations.
 */
public class InventoryUtils {

    /**
     * Transfers a stack from an inventory to another inventory, first attempts to transfer that stack to an existing
     * item stack of the same type in the receiver's inventory.
     * @param to The inventory that receives the transferred stack.
     * @param receiverState An InventoryState object of the receiver inventory.
     * @param stack The stack to transfer.
     */
    public static void transferStack(Inventory to, CompleteInventoryState receiverState, ItemStack stack) {
        Item stackItem = stack.getItem();

        // Attempt to transfer the stack to an existing item stack of the same item.
        if (receiverState.getNonFullItemSlots().containsKey(stackItem) && transferToExistingStack(to, receiverState, stack)) {
            return;
        }

        if (!receiverState.getEmptySlots().isEmpty()) {
            int emptySlot = receiverState.getEmptySlots().poll();
            to.setStack(emptySlot, stack.copyAndEmpty());
            receiverState.setModified();
            // Check if the stack that was transferred isn't full.
            if (stack.getCount() != stack.getMaxCount()) {
                // Add this slot to the item slots of the receiver state.
                receiverState.getNonFullItemSlots().computeIfAbsent(stackItem, k -> new ArrayList<>()).add(emptySlot);
            }
        }
    }

    /**
     * Transfers a stack from an inventory to one or more existing non-full stacks of the same item in another
     * inventory.
     * @param to The inventory that receives the transferred stack.
     * @param receiverState An InventoryState object of the receiver inventory.
     * @param stackToTransfer The stack to transfer.
     * @return True if the entire stack was transferred, false otherwise.
     */
    public static boolean transferToExistingStack(Inventory to, InventoryState receiverState, ItemStack stackToTransfer) {
        Item stackItem = stackToTransfer.getItem();
        ArrayList<Integer> slotsWithItem = receiverState.getNonFullItemSlots().get(stackItem);
        Iterator<Integer> slotsIterator = slotsWithItem.iterator();

        while (slotsIterator.hasNext() && !stackToTransfer.isEmpty()) {
            int slotWithItem = slotsIterator.next();
            ItemStack existingStack = to.getStack(slotWithItem);
            if (!Objects.equals(existingStack.getComponents(), stackToTransfer.getComponents())) {
                continue;  // Skip if NBT data is different.
            }

            int spaceLeft = existingStack.getMaxCount() - existingStack.getCount();
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

            existingStack.increment(transferAmount);
            stackToTransfer.decrement(transferAmount);
            receiverState.setModified();
        }

        if (slotsWithItem.isEmpty()) {
            receiverState.getNonFullItemSlots().remove(stackItem);
        }
        return stackToTransfer.isEmpty();
    }

    /**
     * Combines items from an inventory into a single list of items before sorting them.
     * @param inventory The inventory to sort.
     * @param type The sorting type to use.
     * @param startIndex The index at which item iteration starts.
     * @param endIndex The index at which item iteration ends.
     * @return A sorted list of items.
     */
    public static List<ItemStack> combineAndSortInventory(Inventory inventory, SortType type, int startIndex, int endIndex) {
        List<ItemStack> combinedStacks = new ArrayList<>();
        // Use a map in which the key is an item and the value is the last stack of that item.
        Map<Item, ItemStack> lastStackMap = new HashMap<>();

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            Item stackItem = stack.getItem();
            ItemStack lastStack = lastStackMap.get(stackItem);

            if (lastStack == null || lastStack.getCount() == stackItem.getMaxCount()) {
                ItemStack newStack = stack.copy();
                combinedStacks.add(newStack);
                lastStackMap.put(stackItem, newStack);
            }
            else {
                int spaceLeft = stackItem.getMaxCount() - lastStack.getCount();
                if (spaceLeft < stack.getCount()) {
                    lastStack.increment(spaceLeft);
                    ItemStack newStack = new ItemStack(stackItem, stack.getCount() - spaceLeft);
                    combinedStacks.add(newStack);
                    lastStackMap.put(stackItem, newStack);
                }
                else {
                    lastStack.increment(stack.getCount());
                }
            }

            inventory.setStack(i, ItemStack.EMPTY);
        }

        Comparator<ItemStack> comparator = switch (type) {
            case ITEM_GROUP -> ComparatorTypes.BY_GROUP;
            case ITEM_COUNT -> ComparatorTypes.BY_COUNT;
            case ITEM_RARITY -> ComparatorTypes.BY_RARITY;
            case ITEM_NAME -> ComparatorTypes.BY_NAME;
            case ITEM_ID -> ComparatorTypes.BY_ID;
            default -> throw new IllegalArgumentException("Unknown sort type: " + type);
        };

        combinedStacks.sort(comparator);
        return combinedStacks;
    }

    /**
     * Checks if an item stack is a shulker box.
     * @param itemStack The item stack to check.
     * @return True if the item stack is a shulker box, false otherwise.
     */
    public static boolean isShulkerBox(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem &&
                ((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock;
    }

    /**
     * Gets the storages that are nearby the player, as well as their position.
     * @param player The player.
     * @return A list consisting of pairs of inventories and their position.
     */
    public static List<Pair<Inventory, Vec3d>> getNearbyStorages(ServerPlayerEntity player) {
        World world = player.getWorld();
        List<Pair<Inventory, Vec3d>> nearbyStorages = new ArrayList<>();

        int range = ConfigManager.getInstance().getConfig().getQuickStackRange();
        boolean performLosCheck = ConfigManager.getInstance().getConfig().getLineOfSightCheck();

        if (performLosCheck) {
            getNearbyStoragesWithLos(player, world, range, nearbyStorages);
        }
        else {
            getNearbyStorages(player, world, range, nearbyStorages);
        }

        return nearbyStorages;
    }

    /**
     * Adds the nearby storages that the player has a line of sight to, including storages of block entities and
     * entities, to the nearby storages list.
     * @param player The player.
     * @param world The world.
     * @param range The range of the scan.
     * @param nearbyStorages The list to which inventories and positions are added.
     */
    private static void getNearbyStoragesWithLos(ServerPlayerEntity player, World world, int range, List<Pair<Inventory, Vec3d>> nearbyStorages) {
        Set<BlockPos> processedChests = new HashSet<>();
        BlockPos playerPos = player.getBlockPos();

        BlockPos.iterateOutwards(playerPos, range, range, range).forEach(pos -> {
            if (processedChests.contains(pos)) {
                return;
            }

            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.hasBlockEntity()) {
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory inventory && inventory.size() >= 27) {
                Vec3d losPoint = hasLineOfSight(player, world, pos);
                if (losPoint == Vec3d.ZERO) {
                    return;
                }

                if (blockEntity instanceof LidOpenable) {
                    processChestBlockEntity(world, (ChestBlockEntity) blockEntity, losPoint, pos, nearbyStorages, processedChests);
                }
                else {
                    nearbyStorages.add(Pair.of((Inventory) blockEntity, losPoint));
                }
            }
        });

        Box searchBox = new Box(playerPos).expand(range);
        world.getEntitiesByType(TypeFilter.instanceOf(VehicleEntity.class), searchBox, entity ->
        entity instanceof Inventory inventory && inventory.size() >= 27)
            .forEach(entity -> {
                if (hasLineOfSightToEntity(player, world, entity)) {
                    nearbyStorages.add(Pair.of((Inventory) entity, entity.getBoundingBox().getCenter()));
                }
            }
        );
    }

    /**
     * Adds the storages nearby the player, including storages of block entities and
     * entities, to the nearby storages list.
     * @param player The player.
     * @param world The world.
     * @param range The range of the scan.
     * @param nearbyStorages The list to which inventories and positions are added.
     */
    private static void getNearbyStorages(ServerPlayerEntity player, World world, int range, List<Pair<Inventory, Vec3d>> nearbyStorages) {
        Set<BlockPos> processedPositions = new HashSet<>();
        BlockPos playerPos = player.getBlockPos();

        BlockPos.iterateOutwards(playerPos, range, range, range).forEach(pos -> {
            if (processedPositions.contains(pos)) {
                return;
            }

            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.hasBlockEntity()) {
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory inventory && inventory.size() >= 27) {
                // TODO - Find a proper way to check if a block entity is a part of a double chest, one that will be
                //  compatible with block entities that don't implement LidOpenable.
                if (blockEntity instanceof LidOpenable) {
                    processChestBlockEntity(world, (ChestBlockEntity) blockEntity, pos.toCenterPos(), pos, nearbyStorages, processedPositions);
                }
                else {
                    nearbyStorages.add(Pair.of((Inventory) blockEntity, pos.toCenterPos()));
                }
            }
        });

        Box searchBox = new Box(playerPos).expand(range);
        world.getEntitiesByType(TypeFilter.instanceOf(VehicleEntity.class), searchBox, entity ->
        entity instanceof Inventory inventory && inventory.size() >= 27)
            .forEach(entity -> {
                nearbyStorages.add(Pair.of((Inventory) entity, entity.getBoundingBox().getCenter()));
            }
        );
    }

    /**
     * Adds the chest inventory (single or double) to the nearby storages list.
     * Handles both single and double chests, calculating the center position for double chests.
     * @param world The world in which the player and the chest block entity are in.
     * @param chestBlockEntity The chest block entity.
     * @param losPoint The position of the chest block entity to which the player has line of sight to.
     * @param chestBlockPos The block pos of the chest block entity.
     * @param nearbyStorages The list to which inventories and positions are added.
     * @param processedChests A set of block positions that were already processed.
     */
    private static void processChestBlockEntity(World world, ChestBlockEntity chestBlockEntity, Vec3d losPoint, BlockPos chestBlockPos, List<Pair<Inventory, Vec3d>> nearbyStorages, Set<BlockPos> processedChests) {
        BlockState chestBlockState = chestBlockEntity.getCachedState();
        if (chestBlockState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
            nearbyStorages.add(Pair.of(chestBlockEntity, losPoint));
        }
        else {
            ChestType chestType = chestBlockState.get(ChestBlock.CHEST_TYPE);
            Direction facingDirection = chestBlockState.get(ChestBlock.FACING);

            BlockPos neighboringChestPos = chestBlockPos.offset(chestType == ChestType.LEFT ?
                    facingDirection.rotateYClockwise() :
                    facingDirection.rotateYCounterclockwise());

            ChestBlockEntity neighboringChestEntity = (ChestBlockEntity) world.getBlockEntity(neighboringChestPos);
            // Calculate the center of the double chest.
            Vec3d doubleChestCenter = (losPoint.add(neighboringChestPos.toCenterPos().x, losPoint.y, neighboringChestPos.toCenterPos().z)).multiply(0.5);
            nearbyStorages.add(Pair.of(new DoubleInventory(chestBlockEntity, neighboringChestEntity), doubleChestCenter));

            processedChests.add(neighboringChestPos);
        }
    }

    /**
     * Checks if the player has line of sight to either the chest's center, its top face's center, or its bottom face's
     * center.
     * @param player The player.
     * @param world The world in which the player and the block entity are in.
     * @param pos The block position of the block entity
     * @return The point that the player has line of sight to.
     */
    private static Vec3d hasLineOfSight(ServerPlayerEntity player, World world, BlockPos pos) {
        Vec3d playerEyes = player.getEyePos();
        Vec3d centerPos = pos.toCenterPos();

        // Define the points to check (center, top center, bottom center)
        Vec3d[] pointsToCheck = new Vec3d[] {
                centerPos,
                centerPos.add(0, 0.5, 0),
                centerPos.add(0, -0.5, 0)
        };

        for (Vec3d end : pointsToCheck) {
            RaycastContext context = new RaycastContext(playerEyes, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
            BlockHitResult hitResult = world.raycast(context);

            if (hitResult.getBlockPos().equals(pos) || hitResult.getType() == HitResult.Type.MISS) {
                return end;
            }
        }

        return Vec3d.ZERO;
    }

    /**
     * Checks if the player's line of sight to the entity's center is unobstructed.
     * @param player The player.
     * @param world The world in which the player and the entity are in.
     * @param entity The entity.
     * @return True if the line of sight isn't interrupted by a block, false otherwise.
     */
    private static boolean hasLineOfSightToEntity(ServerPlayerEntity player, World world, Entity entity) {
        Vec3d playerEyes = player.getEyePos();
        Vec3d end = entity.getBoundingBox().getCenter();

        BlockHitResult result = world.raycast(new RaycastContext(
                playerEyes, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        return result.getType() == HitResult.Type.MISS;
    }

    /**
     * Triggers the item animation that occurs after Quick Stack To Nearby Storages is used.
     * @param world The server world.
     * @param playerEyes The position of the player's eyes.
     * @param animationMap An animation map consisting of target positions and lists of items.
     */
    public static void triggerFlyOutAnimation(ServerWorld world, Vec3d playerEyes, Map<Vec3d, ArrayList<Item>> animationMap) {
        int itemAnimationLength = ConfigManager.getInstance().getConfig().getItemAnimationLength();
        int itemAnimationInterval = ConfigManager.getInstance().getConfig().getItemAnimationInterval();

        for (Map.Entry<Vec3d, ArrayList<Item>> entry : animationMap.entrySet()) {
            Vec3d targetPos = entry.getKey();
            Vec3d itemVelocity = new Vec3d(
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
                        items.get(i).getDefaultStack(),
                        itemVelocity,
                        itemAnimationLength,
                        movementDelay
                );

                world.spawnEntity(ghostItem);
            }
        }
    }
}