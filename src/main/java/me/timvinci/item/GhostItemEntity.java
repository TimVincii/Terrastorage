package me.timvinci.item;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * An ItemEntity that moves from one point to another without interacting with the world in any other way.
 */
public class GhostItemEntity extends ItemEntity {
    // Not using the ItemEntity's internal velocity since it causes issues.
    private final Vec3d velocity;
    private int animationTicksLeft;
    private int movementDelay;

    public GhostItemEntity(World world, double x, double y, double z, ItemStack stack, Vec3d velocity, int animationLength, int movementDelay) {
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
            this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
            this.velocityDirty = true;
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
    public void onPlayerCollision(PlayerEntity player) { }

    /**
     * Stops the item entity from appearing as on-fire.
     */
    @Override
    public boolean isFireImmune() {
        return true;
    }

    /**
     * Stops the saving of the item entity, otherwise it would be dropped as an item on chunk-unloading.
     */
    @Override
    public boolean shouldSave() {
        return false;
    }
}
