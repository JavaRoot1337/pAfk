package ru.javaroot.pafk.zone;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import ru.javaroot.pafk.cfg.Cfg;
import ru.javaroot.pafk.cfg.CfgLoad;
import ru.javaroot.pafk.hook.WgHook;

public final class Zones {
    private final CfgLoad cfgLoad;
    private final WgHook wg;
    private final Logger log;
    private volatile ZoneSet active = ZoneSet.EMPTY;

    public Zones(CfgLoad cfgLoad, WgHook wg, Logger log) {
        this.cfgLoad = cfgLoad;
        this.wg = wg;
        this.log = log;
    }

    public void reload() {
        Cfg cfg = cfgLoad.cfg();
        String worldName = cfg.worldName();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            log.log(Level.WARNING, "Configured AFK world not found: " + worldName);
            active = new ZoneSet(worldName, null, null, null, false, 0, 0, 0, 0);
            return;
        }

        ZoneBox normal = wg.findRegion(world, cfg.regionNormal());
        ZoneBox premium = wg.findRegion(world, cfg.regionPremium());
        boolean hasRegions = normal != null || premium != null;

        double broadMinX = 0;
        double broadMaxX = 0;
        double broadMinZ = 0;
        double broadMaxZ = 0;
        if (hasRegions) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (ZoneBox box : new ZoneBox[] { normal, premium }) {
                if (box == null) {
                    continue;
                }
                minX = Math.min(minX, box.minX());
                minZ = Math.min(minZ, box.minZ());
                maxX = Math.max(maxX, box.maxX());
                maxZ = Math.max(maxZ, box.maxZ());
            }
            broadMinX = minX - 1;
            broadMaxX = maxX + 2;
            broadMinZ = minZ - 1;
            broadMaxZ = maxZ + 2;
        }

        if (normal == null) {
            log.log(Level.WARNING, "Normal AFK region not found: " + cfg.regionNormal() + " in world: " + worldName);
        }
        if (premium == null) {
            log.log(Level.WARNING, "Premium AFK region not found: " + cfg.regionPremium() + " in world: " + worldName);
        }

        active = new ZoneSet(worldName, world.getUID(), normal, premium, hasRegions, broadMinX, broadMaxX, broadMinZ, broadMaxZ);
    }

    public boolean isOutsideBroadphase(World world, double x, double z) {
        ZoneSet s = active;
        if (!s.hasRegions() || world == null || !s.worldId().equals(world.getUID())) {
            return true;
        }
        return x < s.broadMinX() || x > s.broadMaxX() || z < s.broadMinZ() || z > s.broadMaxZ();
    }

    public ZoneType zoneAt(World world, int x, int y, int z) {
        ZoneSet s = active;
        if (!s.hasRegions() || world == null) {
            return ZoneType.NONE;
        }
        UUID wid = world.getUID();
        if (!s.worldId().equals(wid)) {
            return ZoneType.NONE;
        }
        if (s.premium() != null && s.premium().contains(wid, x, y, z)) {
            return ZoneType.PREMIUM;
        }
        if (s.normal() != null && s.normal().contains(wid, x, y, z)) {
            return ZoneType.NORMAL;
        }
        return ZoneType.NONE;
    }

    public ZoneStatus status() {
        ZoneSet s = active;
        return new ZoneStatus(s.worldName(), s.worldId() != null, s.normal() != null, s.premium() != null);
    }

    public record ZoneStatus(String worldName, boolean worldLoaded, boolean normalLoaded, boolean premiumLoaded) {
    }

    private record ZoneSet(
            String worldName,
            UUID worldId,
            ZoneBox normal,
            ZoneBox premium,
            boolean hasRegions,
            double broadMinX,
            double broadMaxX,
            double broadMinZ,
            double broadMaxZ
    ) {
        static final ZoneSet EMPTY = new ZoneSet("", null, null, null, false, 0, 0, 0, 0);
    }
}
