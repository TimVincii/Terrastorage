package me.timvinci.terrastorage.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * An ItemEntity that moves from one point to another without interacting with the world in any other way.
 */
public class GhostItemEntity extends ItemEntity {
    // Not using the ItemEntity's internal velocity since it causes issues.
    private final Vec3 velocity;
    private int animationTicksLeft;
    private int movementDelay;

    public GhostItemEntity(Level world, double x, double y, double z, ItemStack stack, Vec3 velocity, int animationLength, int movementDelay) {
        super(world, x, y, z, stack, 0 ,0 ,0);
        this.animationTicksLeft = animationLength;
        this.velocity = velocity;
        this.movementDelay = movementDelay;
        this.setNoGravity(true);
    }

    /**
     * Moves the item entity towards its target position every tick, and discards it once it arrives.
     */
    @Override
    public void tick() {
        if (movementDelay > 0) {
            movementDelay--;
            return;
        }

        if (animationTicksLeft > 0) {
            this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
            this.needsSync = true;
            animationTicksLeft--;
        }
        else {
            this.discard();
        }
    }

    /**
     * Stops any interaction logic with players.
     */
    @Override
    public void playerTouch(Player player) { }

    /**
     * Stops the item entity from appearing as on-fire.
     */
    @Override
    public boolean fireImmune() {
        return true;
    }

    /**
     * Stops the saving of the item entity, otherwise it would be dropped as an item on chunk-unloading.
     */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
