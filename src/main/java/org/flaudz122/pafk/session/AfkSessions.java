package org.flaudz122.pafk.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.flaudz122.pafk.PAFK;
import org.flaudz122.pafk.cfg.Cfg;
import org.flaudz122.pafk.cfg.Hud;
import org.flaudz122.pafk.reward.Rewards;

public final class AfkSessions {
    private static final long REWARD_INTERVAL_MILLIS = Hud.REWARD_INTERVAL_SECONDS * 1000L;

    private final PAFK plugin;
    private final Rewards rewards;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public AfkSessions(PAFK plugin, Rewards rewards) {
        this.plugin = plugin;
        this.rewards = rewards;
    }

    public void addPlayer(Player player, boolean premium) {
        UUID uuid = player.getUniqueId();
        if (this.sessions.containsKey(uuid)) {
            return;
        }
        Cfg cfg = this.plugin.getCfg();
        BossBar bar = BossBar.bossBar(
                this.plugin.getHud().getBossBarTitle(Hud.REWARD_INTERVAL_SECONDS, premium),
                1.0f,
                premium ? cfg.getBarColorPremium() : cfg.getBarColorNormal(),
                cfg.getBarOverlay());
        player.showBossBar(bar);
        player.setCollidable(false);

        Session session = new Session(uuid, player, bar, premium);
        this.sessions.put(uuid, session);
        this.updateDisplay(player, session, 0L);
    }

    public void removePlayer(Player player) {
        Session session = this.sessions.remove(player.getUniqueId());
        if (session != null) {
            player.hideBossBar(session.getBar());
            player.setCollidable(true);
        }
    }

    public void cleanup() {
        for (Session session : this.sessions.values()) {
            Player player = session.getPlayer();
            if (player.isOnline()) {
                player.hideBossBar(session.getBar());
                player.setCollidable(true);
            }
        }
        this.sessions.clear();
    }

    public void tick() {
        if (this.sessions.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();

        Iterator<Session> iterator = this.sessions.values().iterator();
        while (iterator.hasNext()) {
            Session session = iterator.next();
            Player player = session.getPlayer();
            if (!player.isOnline()) {
                iterator.remove();
                continue;
            }

            long elapsed = now - session.getStartTime();
            if (elapsed >= REWARD_INTERVAL_MILLIS) {
                this.rewards.giveReward(player, session.isPremium(), this.plugin.getCfg());
                session.markRewarded(now);
                elapsed = 0L;
            }
            this.updateDisplay(player, session, elapsed);
        }
    }

    private void updateDisplay(Player player, Session session, long elapsed) {
        long remaining = Math.max(0L, REWARD_INTERVAL_MILLIS - elapsed);
        float progress = Math.max(0.0f, Math.min(1.0f, remaining / (float) REWARD_INTERVAL_MILLIS));
        int remainingSeconds = (int) Math.min(Hud.REWARD_INTERVAL_SECONDS, remaining / 1000L);
        int minuteIndex = (int) Math.min(Hud.PROGRESS_STEPS, elapsed / 60000L);

        Cfg cfg = this.plugin.getCfg();
        BossBar bar = session.getBar();
        bar.color(session.isPremium() ? cfg.getBarColorPremium() : cfg.getBarColorNormal());
        bar.overlay(cfg.getBarOverlay());
        bar.progress(progress);
        bar.name(this.plugin.getHud().getBossBarTitle(remainingSeconds, session.isPremium()));

        Title title = this.plugin.getHud().getZoneTitle(minuteIndex, session.isPremium());
        player.showTitle(title);
    }

    public boolean isAfk(UUID uuid) {
        return this.sessions.containsKey(uuid);
    }

    public Session getSession(UUID uuid) {
        return this.sessions.get(uuid);
    }
}
