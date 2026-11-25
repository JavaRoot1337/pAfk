package ru.javaroot.pafk;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.time.Clock;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.javaroot.pafk.cfg.CfgLoad;
import ru.javaroot.pafk.cmd.AfkCmd;
import ru.javaroot.pafk.hook.WgHook;
import ru.javaroot.pafk.list.GuardList;
import ru.javaroot.pafk.list.ZoneList;
import ru.javaroot.pafk.plat.sched.Exec;
import ru.javaroot.pafk.sess.RewardSvc;
import ru.javaroot.pafk.sess.SessMgr;
import ru.javaroot.pafk.zone.Zones;

public final class PAfk extends JavaPlugin {
    private static final long TICK_DELAY = 20L;
    private static final long TICK_PERIOD = 20L;

    private CfgLoad cfgLoad;
    private Exec exec;
    private Zones zones;
    private SessMgr sessions;

    @Override
    public void onEnable() {
        cfgLoad = new CfgLoad(this);
        cfgLoad.load();

        exec = new Exec(this);
        zones = new Zones(cfgLoad, new WgHook(getLogger()), getLogger());
        zones.reload();

        RewardSvc rewards = new RewardSvc(
                cmd -> exec.executeGlobal(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)),
                Clock.systemDefaultZone()
        );
        sessions = new SessMgr(cfgLoad, zones, exec, rewards);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ZoneList(cfgLoad, zones, sessions), this);
        pm.registerEvents(new GuardList(sessions), this);

        AfkCmd cmd = new AfkCmd(cfgLoad, zones, sessions, this::reloadPlugin);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> cmd.register(e.registrar()));

        exec.runGlobalTimer(sessions::tick, TICK_DELAY, TICK_PERIOD);
    }

    @Override
    public void onDisable() {
        if (exec != null) {
            exec.cancelAll();
        }
        if (sessions != null) {
            sessions.cleanup();
        }
    }

    private long reloadPlugin() {
        long ms = cfgLoad.load();
        zones.reload();
        return ms;
    }
}
