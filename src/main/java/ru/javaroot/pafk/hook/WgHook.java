package ru.javaroot.pafk.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import ru.javaroot.pafk.zone.ZoneBox;

public final class WgHook {
    private final Logger log;

    public WgHook(Logger log) {
        this.log = log;
    }

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
    }

    public ZoneBox findRegion(World world, String regionName) {
        if (!isAvailable() || world == null || regionName == null || regionName.isEmpty()) {
            return null;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager mgr = container.get(BukkitAdapter.adapt(world));
            if (mgr == null) {
                return null;
            }
            ProtectedRegion region = mgr.getRegion(regionName);
            if (region == null) {
                return null;
            }
            var min = region.getMinimumPoint();
            var max = region.getMaximumPoint();
            ZoneBox.ShapeCheck extra = region instanceof ProtectedCuboidRegion ? null : region::contains;
            return new ZoneBox(world.getUID(), min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), extra);
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Failed to resolve WorldGuard region: " + regionName, ex);
            return null;
        }
    }
}
