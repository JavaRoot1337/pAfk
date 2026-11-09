package org.flaudz122.pafk.zone;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.flaudz122.pafk.PAFK;
import org.flaudz122.pafk.cfg.Cfg;

public final class Zones {
    private final PAFK plugin;
    private Region normal;
    private Region premium;
    private boolean hasRegions;
    private World commonWorld;
    private double broadMinX;
    private double broadMaxX;
    private double broadMinZ;
    private double broadMaxZ;

    public Zones(PAFK plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        Cfg cfg = this.plugin.getCfg();
        String worldName = cfg.getWorldName();
        this.commonWorld = Bukkit.getWorld(worldName);

        if (this.commonWorld == null) {
            this.plugin.getLogger().log(Level.WARNING, "Configured AFK world not found: " + worldName);
            this.normal = null;
            this.premium = null;
            this.hasRegions = false;
            return;
        }

        this.normal = this.findRegion(this.commonWorld, cfg.getRegionNormal());
        this.premium = this.findRegion(this.commonWorld, cfg.getRegionPremium());
        this.hasRegions = this.normal != null || this.premium != null;

        if (this.hasRegions) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Region region : new Region[] { this.normal, this.premium }) {
                if (region == null) {
                    continue;
                }
                minX = Math.min(minX, region.getMinX());
                minZ = Math.min(minZ, region.getMinZ());
                maxX = Math.max(maxX, region.getMaxX());
                maxZ = Math.max(maxZ, region.getMaxZ());
            }

            this.broadMinX = minX - 1;
            this.broadMaxX = maxX + 2;
            this.broadMinZ = minZ - 1;
            this.broadMaxZ = maxZ + 2;
        }

        if (this.normal == null) {
            this.plugin.getLogger().log(Level.WARNING, "Normal AFK region not found: " + cfg.getRegionNormal() + " in world: " + worldName);
        }
        if (this.premium == null) {
            this.plugin.getLogger().log(Level.WARNING, "Premium AFK region not found: " + cfg.getRegionPremium() + " in world: " + worldName);
        }
    }

    public boolean isOutsideHorizontalBroadphase(double x, double z) {
        if (!this.hasRegions) {
            return true;
        }
        return x < this.broadMinX || x > this.broadMaxX || z < this.broadMinZ || z > this.broadMaxZ;
    }

    public ZoneType getZone(World world, int x, int y, int z) {
        if (!this.hasRegions || this.commonWorld != world) {
            return ZoneType.NONE;
        }
        if (this.premium != null && this.premium.contains(world, x, y, z)) {
            return ZoneType.PREMIUM;
        }
        if (this.normal != null && this.normal.contains(world, x, y, z)) {
            return ZoneType.NORMAL;
        }
        return ZoneType.NONE;
    }

    private Region findRegion(World world, String regionName) {
        if (regionName == null || regionName.isEmpty()) {
            return null;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(world));
            if (manager != null) {
                ProtectedRegion region = manager.getRegion(regionName);
                if (region != null) {
                    return new Region(world, region);
                }
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to resolve WorldGuard region: " + regionName, ex);
        }
        return null;
    }

    public World getCommonWorld() {
        return this.commonWorld;
    }
}
