package org.flaudz122.pafk.session;

import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;

public final class Session {
    private final UUID uuid;
    private final Player player;
    private final BossBar bar;
    private final boolean premium;
    private long startTime;

    public Session(UUID uuid, Player player, BossBar bar, boolean premium) {
        this.uuid = uuid;
        this.player = player;
        this.bar = bar;
        this.premium = premium;
        this.startTime = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public Player getPlayer() {
        return this.player;
    }

    public BossBar getBar() {
        return this.bar;
    }

    public boolean isPremium() {
        return this.premium;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void markRewarded(long now) {
        this.startTime = now;
    }
}
