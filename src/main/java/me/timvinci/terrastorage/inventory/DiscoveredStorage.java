package me.timvinci.terrastorage.inventory;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A storage found during the nearby storage scan, along with every point the player can see it at.
 * A storage covering several blocks is found once per block, so its points are collected as the scan goes and averaged
 * only once it ends.
 */
public class DiscoveredStorage {
    private final StorageAccess access;
    private final List<Vec3> points = new ArrayList<>();

    public DiscoveredStorage(StorageAccess access, Vec3 point) {
        this.access = access;
        this.points.add(point);
    }

    public void addPoint(Vec3 point) {
        points.add(point);
    }

    public StorageAccess getAccess() {
        return access;
    }

    /**
     * @return The point the fly out animation travels to, horizontally centered between the storage's parts but
     * keeping the height of the first point, which is the face the player was found to have line of sight to.
     */
    public Vec3 getCenter() {
        if (points.size() == 1) {
            return points.getFirst();
        }

        double x = 0;
        double z = 0;
        for (Vec3 point : points) {
            x += point.x;
            z += point.z;
        }

        return new Vec3(x / points.size(), points.getFirst().y, z / points.size());
    }
}
