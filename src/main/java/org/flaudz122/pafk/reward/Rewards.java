package org.flaudz122.pafk.reward;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.bukkit.entity.Player;
import org.flaudz122.pafk.cfg.Cfg;

public final class Rewards {
    private static final long WEEKEND_RECHECK_MILLIS = 60_000L;
    private static final String PLAYER_PLACEHOLDER = "%player%";

    private final Sink sink;
    private final Clock clock;
    private boolean weekend;
    private long nextWeekendCheckMillis;

    public Rewards(Sink sink, Clock clock) {
        this.sink = sink;
        this.clock = clock;
    }

    public void giveReward(Player player, boolean premium, Cfg cfg) {
        this.refreshWeekend();
        String command = substitutePlayer(commandFor(premium, this.weekend, cfg), player.getName());
        this.sink.dispatch(command);

        if (cfg.isSoundEnabled()) {
            player.playSound(cfg.getRewardSound());
        }
    }

    void refreshWeekend() {
        long now = this.clock.millis();
        if (now < this.nextWeekendCheckMillis) {
            return;
        }
        this.weekend = isWeekend(LocalDate.ofInstant(this.clock.instant(), this.clock.getZone()).getDayOfWeek());
        this.nextWeekendCheckMillis = now + WEEKEND_RECHECK_MILLIS;
    }

    boolean isWeekendNow() {
        this.refreshWeekend();
        return this.weekend;
    }

    static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    static String commandFor(boolean premium, boolean weekend, Cfg cfg) {
        return premium
                ? weekend ? cfg.getRewardPremiumWeekend() : cfg.getRewardPremiumWeekday()
                : weekend ? cfg.getRewardCommandWeekend() : cfg.getRewardCommandWeekday();
    }

    static String substitutePlayer(String command, String name) {
        return command.replace(PLAYER_PLACEHOLDER, name);
    }
}
