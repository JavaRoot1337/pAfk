package org.flaudz122.pafk.zone;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public final class Region {
    private final World world;
    private final ProtectedRegion region;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final boolean cuboid;

    public Region(World world, ProtectedRegion region) {
        this.world = world;
        this.region = region;
        this.minX = region.getMinimumPoint().x();
        this.minY = region.getMinimumPoint().y();
        this.minZ = region.getMinimumPoint().z();
        this.maxX = region.getMaximumPoint().x();
        this.maxY = region.getMaximumPoint().y();
        this.maxZ = region.getMaximumPoint().z();
        this.cuboid = region instanceof ProtectedCuboidRegion;
    }

    public boolean contains(World targetWorld, int x, int y, int z) {
        if (this.world != targetWorld) {
            return false;
        }
        if (x < this.minX || x > this.maxX || z < this.minZ || z > this.maxZ || y < this.minY || y > this.maxY) {
            return false;
        }
        return this.cuboid || this.region.contains(x, y, z);
    }

    public int getMinX() {
        return this.minX;
    }

    public int getMaxX() {
        return this.maxX;
    }

    public int getMinZ() {
        return this.minZ;
    }

    public int getMaxZ() {
        return this.maxZ;
    }
}
