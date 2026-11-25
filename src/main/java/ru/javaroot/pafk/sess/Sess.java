package ru.javaroot.pafk.sess;

import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;

public final class Sess {
    private final UUID uuid;
    private final BossBar bar;
    private final boolean premium;
    private volatile long startMs;

    public Sess(UUID uuid, BossBar bar, boolean premium, long startMs) {
        this.uuid = uuid;
        this.bar = bar;
        this.premium = premium;
        this.startMs = startMs;
    }

    public UUID uuid() {
        return uuid;
    }

    public BossBar bar() {
        return bar;
    }

    public boolean premium() {
        return premium;
    }

    public long startMs() {
        return startMs;
    }

    public void markRewarded(long nowMs) {
        this.startMs = nowMs;
    }
}
