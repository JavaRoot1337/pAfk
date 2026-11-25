package ru.javaroot.pafk.sess;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.javaroot.pafk.cfg.Cfg;
import ru.javaroot.pafk.cfg.CfgLoad;
import ru.javaroot.pafk.cfg.Hud;
import ru.javaroot.pafk.plat.sched.Exec;
import ru.javaroot.pafk.zone.ZoneType;
import ru.javaroot.pafk.zone.Zones;

public final class SessMgr {
    private final CfgLoad cfgLoad;
    private final Zones zones;
    private final Exec exec;
    private final RewardSvc rewards;
    private final Map<UUID, Sess> sessions = new ConcurrentHashMap<>();

    public SessMgr(CfgLoad cfgLoad, Zones zones, Exec exec, RewardSvc rewards) {
        this.cfgLoad = cfgLoad;
        this.zones = zones;
        this.exec = exec;
        this.rewards = rewards;
    }

    public void addPlayer(Player p, boolean premium) {
        UUID uuid = p.getUniqueId();
        Sess current = sessions.get(uuid);
        if (current != null) {
            if (current.premium() == premium) {
                return;
            }
            p.hideBossBar(current.bar());
        }

        CfgLoad.State state = cfgLoad.state();
        Cfg cfg = state.cfg();
        BossBar bar = BossBar.bossBar(
                state.hud().bossBarTitle(cfg.rewardIntervalSec(), premium),
                1.0f,
                premium ? cfg.barColorPremium() : cfg.barColorNormal(),
                cfg.barOverlay()
        );
        Sess sess = new Sess(uuid, bar, premium, System.currentTimeMillis());
        sessions.put(uuid, sess);

        p.showBossBar(bar);
        p.setCollidable(false);
        updateDisplay(p, sess, 0L, state);
    }

    public void removePlayer(Player p) {
        Sess removed = sessions.remove(p.getUniqueId());
        if (removed == null) {
            return;
        }
        p.hideBossBar(removed.bar());
        if (exec.isOwned(p)) {
            p.setCollidable(true);
        } else {
            exec.runAtEntity(p, () -> p.setCollidable(true));
        }
    }

    public void tick() {
        if (sessions.isEmpty()) {
            return;
        }
        for (Sess s : sessions.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p == null) {
                sessions.remove(s.uuid(), s);
                continue;
            }
            exec.runAtEntity(p, () -> tickPlayer(p, s), () -> sessions.remove(s.uuid(), s));
        }
    }

    private void tickPlayer(Player p, Sess s) {
        if (!p.isOnline() || sessions.get(p.getUniqueId()) != s) {
            return;
        }

        Location loc = p.getLocation();
        ZoneType zone = zones.zoneAt(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        CfgLoad.State state = cfgLoad.state();
        Cfg cfg = state.cfg();

        if (zone == ZoneType.NONE) {
            removePlayer(p);
            return;
        }
        if (zone == ZoneType.PREMIUM && !p.hasPermission(cfg.premiumPerm())) {
            removePlayer(p);
            p.teleportAsync(p.getWorld().getSpawnLocation());
            p.sendMessage(cfg.noPremMsg());
            return;
        }
        if ((zone == ZoneType.PREMIUM) != s.premium()) {
            addPlayer(p, zone == ZoneType.PREMIUM);
            return;
        }

        long now = System.currentTimeMillis();
        long intervalMs = cfg.rewardIntervalSec() * 1000L;
        long elapsed = now - s.startMs();
        if (elapsed >= intervalMs) {
            rewards.giveReward(p, s.premium(), cfg);
            s.markRewarded(now);
            elapsed = 0L;
        }
        updateDisplay(p, s, elapsed, state);
    }

    private void updateDisplay(Player p, Sess s, long elapsedMs, CfgLoad.State state) {
        Cfg cfg = state.cfg();
        long intervalMs = cfg.rewardIntervalSec() * 1000L;
        long remaining = Math.max(0L, intervalMs - elapsedMs);
        float progress = Math.max(0.0f, Math.min(1.0f, remaining / (float) intervalMs));
        int remainingSec = (int) Math.min(cfg.rewardIntervalSec(), remaining / 1000L);
        int step = (int) Math.min(Hud.PROGRESS_STEPS, (elapsedMs * Hud.PROGRESS_STEPS) / intervalMs);

        BossBar bar = s.bar();
        bar.color(s.premium() ? cfg.barColorPremium() : cfg.barColorNormal());
        bar.overlay(cfg.barOverlay());
        bar.progress(progress);
        bar.name(state.hud().bossBarTitle(remainingSec, s.premium()));

        Title title = state.hud().zoneTitle(step, s.premium());
        p.showTitle(title);
    }

    public void cleanup() {
        for (Sess s : sessions.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p != null && p.isOnline()) {
                p.hideBossBar(s.bar());
                if (exec.isOwned(p)) {
                    p.setCollidable(true);
                }
            }
        }
        sessions.clear();
    }

    public boolean isAfk(UUID uuid) {
        return sessions.containsKey(uuid);
    }

    public Sess getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public int count() {
        return sessions.size();
    }
}
