package ru.javaroot.pafk.sess;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import ru.javaroot.pafk.cfg.Cfg;

public final class RewardSvc {
    private static final long WEEKEND_RECHECK_MS = 60_000L;
    private static final String PLAYER_PLACEHOLDER = "%player%";

    private final Consumer<String> cmdSink;
    private final Clock clock;
    private volatile boolean weekend;
    private volatile long nextCheckMs = Long.MIN_VALUE;

    public RewardSvc(Consumer<String> cmdSink, Clock clock) {
        this.cmdSink = cmdSink;
        this.clock = clock;
    }

    public void giveReward(Player p, boolean premium, Cfg cfg) {
        String cmd = substitutePlayer(commandFor(premium, isWeekendNow(), cfg), p.getName());
        cmdSink.accept(cmd);
        if (cfg.soundEnabled()) {
            p.playSound(cfg.rewardSound());
        }
    }

    public boolean isWeekendNow() {
        long now = clock.millis();
        if (now < nextCheckMs) {
            return weekend;
        }
        synchronized (this) {
            if (now >= nextCheckMs) {
                weekend = isWeekend(LocalDate.ofInstant(clock.instant(), clock.getZone()).getDayOfWeek());
                nextCheckMs = now + WEEKEND_RECHECK_MS;
            }
            return weekend;
        }
    }

    public static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public static String commandFor(boolean premium, boolean weekend, Cfg cfg) {
        if (premium) {
            return weekend ? cfg.rewardPremWeekend() : cfg.rewardPremWeekday();
        }
        return weekend ? cfg.rewardWeekend() : cfg.rewardWeekday();
    }

    public static String substitutePlayer(String cmd, String name) {
        return cmd.replace(PLAYER_PLACEHOLDER, name);
    }
}
