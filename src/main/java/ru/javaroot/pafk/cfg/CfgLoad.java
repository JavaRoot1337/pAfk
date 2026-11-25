package ru.javaroot.pafk.cfg;

import org.bukkit.plugin.Plugin;

public final class CfgLoad {
    private final Plugin plug;
    private volatile State state;

    public CfgLoad(Plugin plug) {
        this.plug = plug;
    }

    public long load() {
        long started = System.nanoTime();
        plug.saveDefaultConfig();
        plug.reloadConfig();
        Cfg cfg = Cfg.from(plug.getConfig());
        Hud hud = new Hud(cfg);
        this.state = new State(cfg, hud);
        return Math.max(0L, (System.nanoTime() - started) / 1_000_000L);
    }

    public State state() {
        return state;
    }

    public Cfg cfg() {
        return state.cfg();
    }

    public Hud hud() {
        return state.hud();
    }

    public record State(Cfg cfg, Hud hud) {
    }
}
