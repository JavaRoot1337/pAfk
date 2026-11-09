package org.flaudz122.pafk.list;

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.flaudz122.pafk.session.AfkSessions;

public final class AfkGuard implements Listener {
    private final AfkSessions sessions;

    public AfkGuard(AfkSessions sessions) {
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent event) {
        if (this.sessions.isAfk(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof FishHook && event.getHitEntity() instanceof Player hitPlayer
                && this.sessions.isAfk(hitPlayer.getUniqueId())) {
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && event.getCaught() instanceof Player target
                && this.sessions.isAfk(target.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleCollide(VehicleEntityCollisionEvent event) {
        if (event.getEntity() instanceof Player player && this.sessions.isAfk(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
