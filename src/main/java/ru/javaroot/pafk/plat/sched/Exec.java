package ru.javaroot.pafk.plat.sched;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class Exec {
    private final Plugin plug;
    private final Set<ScheduledTask> timers = ConcurrentHashMap.newKeySet();

    public Exec(Plugin plug) {
        this.plug = plug;
    }

    public void runAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plug, st -> task.run());
    }

    public void executeGlobal(Runnable task) {
        Bukkit.getGlobalRegionScheduler().execute(plug, task);
    }

    public void runAtEntity(Entity ent, Runnable task) {
        ent.getScheduler().run(plug, st -> task.run(), null);
    }

    public void runAtEntity(Entity ent, Runnable task, Runnable retired) {
        ent.getScheduler().run(plug, st -> task.run(), retired);
    }

    public ScheduledTask runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        long safeDelay = Math.max(1L, delayTicks);
        long safePeriod = Math.max(1L, periodTicks);
        ScheduledTask st = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plug,
                ignored -> task.run(),
                safeDelay,
                safePeriod
        );
        timers.add(st);
        return st;
    }

    public ScheduledTask runAsyncTimer(Runnable task, long delaySec, long periodSec) {
        long safeDelay = Math.max(1L, delaySec);
        long safePeriod = Math.max(1L, periodSec);
        ScheduledTask st = Bukkit.getAsyncScheduler().runAtFixedRate(
                plug,
                ignored -> task.run(),
                safeDelay,
                safePeriod,
                TimeUnit.SECONDS
        );
        timers.add(st);
        return st;
    }

    public boolean isOwned(Entity ent) {
        return Bukkit.isOwnedByCurrentRegion(ent);
    }

    public void cancel(ScheduledTask st) {
        if (st == null) {
            return;
        }
        timers.remove(st);
        st.cancel();
    }

    public void cancelAll() {
        for (ScheduledTask st : timers) {
            st.cancel();
        }
        timers.clear();
    }
}
