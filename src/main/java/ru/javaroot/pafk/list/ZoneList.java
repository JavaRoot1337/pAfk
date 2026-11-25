package ru.javaroot.pafk.list;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import ru.javaroot.pafk.cfg.Cfg;
import ru.javaroot.pafk.cfg.CfgLoad;
import ru.javaroot.pafk.sess.Sess;
import ru.javaroot.pafk.sess.SessMgr;
import ru.javaroot.pafk.zone.ZoneType;
import ru.javaroot.pafk.zone.Zones;

public final class ZoneList implements Listener {
    private static final long MSG_COOLDOWN_MS = 2000L;

    private final CfgLoad cfgLoad;
    private final Zones zones;
    private final SessMgr sessions;
    private final Map<UUID, Long> msgCooldowns = new ConcurrentHashMap<>();

    public ZoneList(CfgLoad cfgLoad, Zones zones, SessMgr sessions) {
        this.cfgLoad = cfgLoad;
        this.zones = zones;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) {
            return;
        }
        Location to = e.getTo();
        Player p = e.getPlayer();
        if (zones.isOutsideBroadphase(to.getWorld(), to.getX(), to.getZ())) {
            sessions.removePlayer(p);
            return;
        }

        Cfg cfg = cfgLoad.cfg();
        ZoneType toZone = zones.zoneAt(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());
        if (toZone == ZoneType.PREMIUM && !p.hasPermission(cfg.premiumPerm())) {
            handleNoPremium(e, p, e.getFrom(), to, cfg);
            return;
        }
        applyZone(p, toZone, cfg);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Location to = e.getTo();
        Player p = e.getPlayer();
        Cfg cfg = cfgLoad.cfg();
        ZoneType toZone = zones.zoneAt(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());

        if (toZone == ZoneType.PREMIUM && !p.hasPermission(cfg.premiumPerm())) {
            e.setCancelled(true);
            p.sendMessage(cfg.noPremMsg());
            return;
        }
        applyZone(p, toZone, cfg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        Cfg cfg = cfgLoad.cfg();
        ZoneType zone = zones.zoneAt(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (zone == ZoneType.PREMIUM && !p.hasPermission(cfg.premiumPerm())) {
            p.teleportAsync(p.getWorld().getSpawnLocation());
            p.sendMessage(cfg.noPremMsg());
            return;
        }
        applyZone(p, zone, cfg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        sessions.removePlayer(p);
        msgCooldowns.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        sessions.removePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        sessions.removePlayer(e.getPlayer());
    }

    private void applyZone(Player p, ZoneType zone, Cfg cfg) {
        Sess cur = sessions.getSession(p.getUniqueId());
        switch (zone) {
            case NONE -> sessions.removePlayer(p);
            case NORMAL -> {
                if (cur == null || cur.premium()) {
                    sessions.addPlayer(p, false);
                }
            }
            case PREMIUM -> {
                if (p.hasPermission(cfg.premiumPerm())) {
                    if (cur == null || !cur.premium()) {
                        sessions.addPlayer(p, true);
                    }
                } else if (cur != null) {
                    sessions.removePlayer(p);
                }
            }
        }
    }

    private void handleNoPremium(PlayerMoveEvent e, Player p, Location from, Location to, Cfg cfg) {
        ZoneType fromZone = zones.zoneAt(from.getWorld(), from.getBlockX(), from.getBlockY(), from.getBlockZ());
        if (fromZone != ZoneType.PREMIUM) {
            double dx = from.getX() - to.getX();
            double dz = from.getZ() - to.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq > 0.0001) {
                double invDist = 1.0 / Math.sqrt(distSq);
                p.setVelocity(new Vector(dx * invDist * 0.8, 0.25, dz * invDist * 0.8));
            }
            e.setTo(from);
        } else {
            p.teleportAsync(p.getWorld().getSpawnLocation());
        }

        if (fromZone == ZoneType.NORMAL) {
            Sess cur = sessions.getSession(p.getUniqueId());
            if (cur == null || cur.premium()) {
                sessions.addPlayer(p, false);
            }
        } else {
            sessions.removePlayer(p);
        }

        long now = System.currentTimeMillis();
        Long last = msgCooldowns.get(p.getUniqueId());
        if (last == null || now - last > MSG_COOLDOWN_MS) {
            msgCooldowns.put(p.getUniqueId(), now);
            p.sendMessage(cfg.noPremMsg());
        }
    }
}
