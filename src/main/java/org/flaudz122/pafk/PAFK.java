package org.flaudz122.pafk;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.time.Clock;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.flaudz122.pafk.cfg.Cfg;
import org.flaudz122.pafk.cfg.Hud;
import org.flaudz122.pafk.cmd.AfkReload;
import org.flaudz122.pafk.list.AfkGuard;
import org.flaudz122.pafk.list.ZoneEntry;
import org.flaudz122.pafk.reward.Rewards;
import org.flaudz122.pafk.reward.Sink;
import org.flaudz122.pafk.session.AfkSessions;
import org.flaudz122.pafk.zone.Zones;

public final class PAFK extends JavaPlugin {
    private static final long SCHEDULE_DELAY_TICKS = 20L;
    private static final long SCHEDULE_PERIOD_TICKS = 20L;

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    private Cfg cfg;
    private Hud hud;
    private Zones zones;
    private AfkSessions sessions;
    private BukkitTask bukkitTask;
    private ScheduledTask foliaTask;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.cfg = new Cfg(this.getConfig());
        this.hud = new Hud(this.cfg);
        this.zones = new Zones(this);
        this.zones.reload();

        Sink sink = this::dispatchConsoleCommand;
        this.sessions = new AfkSessions(this, new Rewards(sink, Clock.systemDefaultZone()));

        this.getServer().getPluginManager().registerEvents(new ZoneEntry(this), this);
        this.getServer().getPluginManager().registerEvents(new AfkGuard(this.sessions), this);

        PluginCommand command = this.getCommand("afkreload");
        if (command != null) {
            AfkReload executor = new AfkReload(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        if (FOLIA) {
            this.foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> this.sessions.tick(), SCHEDULE_DELAY_TICKS, SCHEDULE_PERIOD_TICKS);
        } else {
            this.bukkitTask = this.getServer().getScheduler().runTaskTimer(this, this.sessions::tick, SCHEDULE_DELAY_TICKS, SCHEDULE_PERIOD_TICKS);
        }
    }

    @Override
    public void onDisable() {
        if (this.bukkitTask != null) {
            this.bukkitTask.cancel();
            this.bukkitTask = null;
        }
        if (this.foliaTask != null) {
            this.foliaTask.cancel();
            this.foliaTask = null;
        }
        if (this.sessions != null) {
            this.sessions.cleanup();
        }
    }

    public void dispatchConsoleCommand(String command) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().execute(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public void reloadPlugin() {
        this.reloadConfig();
        this.cfg = new Cfg(this.getConfig());
        this.hud = new Hud(this.cfg);
        this.zones.reload();
    }

    public Cfg getCfg() {
        return this.cfg;
    }

    public Hud getHud() {
        return this.hud;
    }

    public AfkSessions getSessions() {
        return this.sessions;
    }

    public Zones getZones() {
        return this.zones;
    }
}
