package org.flaudz122.pafk.list;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.flaudz122.pafk.PAFK;
import org.flaudz122.pafk.cfg.Cfg;
import org.flaudz122.pafk.session.Session;
import org.flaudz122.pafk.zone.ZoneType;

public final class ZoneEntry implements Listener {
    private final PAFK plugin;
    private final Map<UUID, Long> messageCooldowns = new HashMap<>();

    public ZoneEntry(PAFK plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }
        Location to = event.getTo();
        Cfg cfg = this.plugin.getCfg();
        World commonWorld = this.plugin.getZones().getCommonWorld();

        if (commonWorld != null && commonWorld != to.getWorld()) {
            this.plugin.getSessions().removePlayer(event.getPlayer());
            return;
        }

        if (this.plugin.getZones().isOutsideHorizontalBroadphase(to.getX(), to.getZ())) {
            this.plugin.getSessions().removePlayer(event.getPlayer());
            return;
        }

        Player player = event.getPlayer();
        ZoneType toZone = this.plugin.getZones().getZone(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());

        if (toZone == ZoneType.PREMIUM && !player.hasPermission(cfg.getPremiumPermission())) {
            this.handleNoPremiumEntry(event, player, event.getFrom(), to, cfg);
            return;
        }

        this.process(player, toZone, cfg);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        Player player = event.getPlayer();
        Cfg cfg = this.plugin.getCfg();
        ZoneType toZone = this.plugin.getZones().getZone(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());

        if (toZone == ZoneType.PREMIUM && !player.hasPermission(cfg.getPremiumPermission())) {
            event.setCancelled(true);
            player.sendMessage(cfg.getNoPremiumAccessMessage());
            return;
        }

        this.process(player, toZone, cfg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.getSessions().removePlayer(event.getPlayer());
        this.messageCooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        this.plugin.getSessions().removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        this.plugin.getSessions().removePlayer(event.getPlayer());
    }

    private void process(Player player, ZoneType zone, Cfg cfg) {
        Session current = this.plugin.getSessions().getSession(player.getUniqueId());

        switch (zone) {
            case NONE -> this.plugin.getSessions().removePlayer(player);
            case NORMAL -> {
                if (current == null) {
                    this.plugin.getSessions().addPlayer(player, false);
                } else if (current.isPremium()) {
                    this.plugin.getSessions().removePlayer(player);
                    this.plugin.getSessions().addPlayer(player, false);
                }
            }
            case PREMIUM -> {
                if (player.hasPermission(cfg.getPremiumPermission())) {
                    if (current == null) {
                        this.plugin.getSessions().addPlayer(player, true);
                    } else if (!current.isPremium()) {
                        this.plugin.getSessions().removePlayer(player);
                        this.plugin.getSessions().addPlayer(player, true);
                    }
                } else if (current != null) {
                    this.plugin.getSessions().removePlayer(player);
                }
            }
        }
    }

    private void handleNoPremiumEntry(PlayerMoveEvent event, Player player, Location from, Location to, Cfg cfg) {
        ZoneType fromZone = this.plugin.getZones().getZone(from.getWorld(), from.getBlockX(), from.getBlockY(), from.getBlockZ());
        if (fromZone != ZoneType.PREMIUM) {
            double dx = from.getX() - to.getX();
            double dz = from.getZ() - to.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq > 0.0001) {
                double invDist = 1.0 / Math.sqrt(distSq);
                player.setVelocity(new Vector(dx * invDist * 0.8, 0.25, dz * invDist * 0.8));
            }
            event.setTo(from);
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        if (fromZone == ZoneType.NORMAL) {
            Session session = this.plugin.getSessions().getSession(player.getUniqueId());
            if (session == null || session.isPremium()) {
                this.plugin.getSessions().removePlayer(player);
                this.plugin.getSessions().addPlayer(player, false);
            }
        } else {
            this.plugin.getSessions().removePlayer(player);
        }

        long now = System.currentTimeMillis();
        Long lastTime = this.messageCooldowns.get(player.getUniqueId());
        if (lastTime == null || now - lastTime > 2000L) {
            this.messageCooldowns.put(player.getUniqueId(), now);
            player.sendMessage(cfg.getNoPremiumAccessMessage());
        }
    }
}
