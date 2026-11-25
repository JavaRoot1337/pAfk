package ru.javaroot.pafk.zone;

import java.util.UUID;

public final class ZoneBox {
    @FunctionalInterface
    public interface ShapeCheck {
        boolean contains(int x, int y, int z);
    }

    private final UUID worldId;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final ShapeCheck extra;

    public ZoneBox(UUID worldId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, ShapeCheck extra) {
        this.worldId = worldId;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.extra = extra;
    }

    public boolean contains(UUID targetWorldId, int x, int y, int z) {
        if (!worldId.equals(targetWorldId)) {
            return false;
        }
        if (x < minX || x > maxX || z < minZ || z > maxZ || y < minY || y > maxY) {
            return false;
        }
        return extra == null || extra.contains(x, y, z);
    }

    public int minX() {
        return minX;
    }

    public int maxX() {
        return maxX;
    }

    public int minZ() {
        return minZ;
    }

    public int maxZ() {
        return maxZ;
    }
}
