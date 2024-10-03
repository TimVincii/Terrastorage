package me.timvinci.inventory;

import me.timvinci.config.ConfigManager;
import me.timvinci.item.GhostItemEntity;
import me.timvinci.util.ComparatorTypes;
import me.timvinci.util.SortType;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

import compasses.expandedstorage.api.ExpandedStorageAccessors;

/**
 * A utility class for inventory/item related operations.
 */
public class InventoryUtils {
    public static boolean expandedStorageLoaded = false;

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
            if (!Objects.equals(existingStack.getNbt(), stackToTransfer.getNbt())) {
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
        Set<BlockPos> processedChests = new HashSet<>();

        // Getting the range, and whether the los check is enabled.
        int range = ConfigManager.getInstance().getConfig().getQuickStackRange();
        boolean performLosCheck = ConfigManager.getInstance().getConfig().getLineOfSightCheck();
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
                Vec3d losPoint;
                if (performLosCheck) {
                    losPoint = hasLineOfSight(player, world, pos);
                    // Return if the player doesn't have line of sight to the block entity.
                    if (losPoint == Vec3d.ZERO) {
                        return;
                    }
                }
                else {
                    losPoint = pos.toCenterPos();
                }

                if (blockEntity instanceof ChestBlockEntity) {
                    ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
                    if (chestType == ChestType.SINGLE) {
                        nearbyStorages.add(Pair.of(inventory, losPoint));
                        return;
                    }

                    BlockPos neighboringChestPos = getNeighboringChestPos(pos, chestType, state.get(ChestBlock.FACING));
                    Vec3d doubleChestLosPoint = getDoubleChestCenter(losPoint, neighboringChestPos.toCenterPos());
                    Inventory neighboringChestInventory = (Inventory) world.getBlockEntity(neighboringChestPos);

                    nearbyStorages.add(Pair.of(new DoubleInventory(inventory, neighboringChestInventory), doubleChestLosPoint));
                    processedChests.add(neighboringChestPos);
                }
                else if (expandedStorageLoaded) {
                    Optional<Direction> attachedChestDirection = ExpandedStorageAccessors.getAttachedChestDirection(state);
                    if (attachedChestDirection.isEmpty()) {
                        nearbyStorages.add(Pair.of(inventory, losPoint));
                        return;
                    }

                    BlockPos neighboringChestPos = pos.offset(attachedChestDirection.get());
                    Vec3d doubleChestLosPoint = getDoubleChestCenter(losPoint, neighboringChestPos.toCenterPos());
                    Inventory neighboringChestInventory = (Inventory) world.getBlockEntity(neighboringChestPos);

                    nearbyStorages.add(Pair.of(new DoubleInventory(inventory, neighboringChestInventory), doubleChestLosPoint));
                    processedChests.add(neighboringChestPos);
                }
                else {
                    nearbyStorages.add(Pair.of(inventory, losPoint));
                }
            }
        });

        Box searchBox = new Box(playerPos).expand(range);
        world.getEntitiesByType(TypeFilter.instanceOf(Entity.class), searchBox, entity ->
        entity instanceof Inventory inventory && inventory.size() >= 27)
            .forEach(entity -> {
                Vec3d losPoint;
                if (performLosCheck) {
                    losPoint = hasLineOfSightToEntity(player, world, entity);
                    if (losPoint == Vec3d.ZERO) {
                        return;
                    }
                }
                else {
                    losPoint = entity.getBoundingBox().getCenter();
                }

                nearbyStorages.add(Pair.of((Inventory) entity, losPoint));
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
        return chestBlockPos.offset(chestType == ChestType.LEFT ?
                facing.rotateYClockwise() :
                facing.rotateYCounterclockwise());
    }

    /**
     * Calculates the center point of a double chest, averaging the positions of both chest parts.
     * @param losPoint The line of sight point from the player to one part of the double chest.
     * @param secondChestCenter The center position of the second chest block in the double chest.
     * @return The center point of the entire double chest.
     */
    private static Vec3d getDoubleChestCenter(Vec3d losPoint, Vec3d secondChestCenter) {
        return (losPoint.add(secondChestCenter.x, losPoint.y, secondChestCenter.z)).multiply(0.5);
    }

    /**
     * Checks if the player has line of sight to either the chest's center, its top face's center, or its bottom face's
     * center.
     * @param player The player.
     * @param world The world in which the player and the block entity are in.
     * @param pos The block position of the block entity
     * @return The point that the player has line of sight to, or Vec3d.ZERO if the player doesn't have line of sight.
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
     * Checks if the player has line of sight to the entity's center.
     * @param player The player.
     * @param world The world in which the player and the entity are in.
     * @param entity The entity.
     * @return The center of the entity, or Vec3d.ZERO if the player doesn't have line of sight.
     */
    private static Vec3d hasLineOfSightToEntity(ServerPlayerEntity player, World world, Entity entity) {
        Vec3d playerEyes = player.getEyePos();
        Vec3d end = entity.getBoundingBox().getCenter();

        BlockHitResult result = world.raycast(new RaycastContext(
                playerEyes, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        return result.getType() == HitResult.Type.MISS ? end : Vec3d.ZERO;
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
