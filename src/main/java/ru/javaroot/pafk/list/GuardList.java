package ru.javaroot.pafk.list;

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import ru.javaroot.pafk.sess.SessMgr;

public final class GuardList implements Listener {
    private final SessMgr sessions;

    public GuardList(SessMgr sessions) {
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent e) {
        if (sessions.isAfk(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof FishHook && e.getHitEntity() instanceof Player hit
                && sessions.isAfk(hit.getUniqueId())) {
            e.getEntity().remove();
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && e.getCaught() instanceof Player target
                && sessions.isAfk(target.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleCollide(VehicleEntityCollisionEvent e) {
        if (e.getEntity() instanceof Player p && sessions.isAfk(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
