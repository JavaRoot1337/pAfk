package org.flaudz122.pafk.cfg;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public final class Hud {
    public static final int REWARD_INTERVAL_SECONDS = 900;
    public static final int PROGRESS_STEPS = 15;

    private static final Title.Times TITLE_TIMES = Title.Times.times(Duration.ZERO, Duration.ofMillis(2000), Duration.ZERO);
    private static final Component FILLED_BLOCK = Component.text('■', NamedTextColor.GREEN);
    private static final Component EMPTY_BLOCK = Component.text('□', NamedTextColor.GRAY);

    private final Component[] normalBossBarTitles = new Component[REWARD_INTERVAL_SECONDS + 1];
    private final Component[] premiumBossBarTitles = new Component[REWARD_INTERVAL_SECONDS + 1];
    private final Title[] normalTitles = new Title[PROGRESS_STEPS + 1];
    private final Title[] premiumTitles = new Title[PROGRESS_STEPS + 1];

    public Hud(Cfg cfg) {
        Component titleNormal = Msgs.parse(cfg.getTitleTextNormal());
        Component titlePremium = Msgs.parse(cfg.getTitleTextPremium());

        for (int seconds = 0; seconds <= REWARD_INTERVAL_SECONDS; seconds++) {
            this.normalBossBarTitles[seconds] = Msgs.parse(bossBarText(cfg.getBossBarTemplateNormal(), cfg.getTimeFormat(), seconds));
            this.premiumBossBarTitles[seconds] = Msgs.parse(bossBarText(cfg.getBossBarTemplatePremium(), cfg.getTimeFormat(), seconds));
        }

        for (int minute = 0; minute <= PROGRESS_STEPS; minute++) {
            Component sticks = Component.empty();
            for (int i = 0; i < PROGRESS_STEPS; i++) {
                sticks = sticks.append(i < minute ? FILLED_BLOCK : EMPTY_BLOCK);
            }
            this.normalTitles[minute] = Title.title(titleNormal, sticks, TITLE_TIMES);
            this.premiumTitles[minute] = Title.title(titlePremium, sticks, TITLE_TIMES);
        }
    }

    private static String bossBarText(String template, String timeFormat, int seconds) {
        return expandTime(template, seconds / 60, seconds % 60)
                .replace("%time%", expandTime(timeFormat, seconds / 60, seconds % 60))
                .replace("%percent%", String.valueOf(Math.round((1.0 - (double) seconds / REWARD_INTERVAL_SECONDS) * 100.0)));
    }

    private static String expandTime(String template, int minutes, int seconds) {
        return template
                .replace("%mm%", pad(minutes))
                .replace("%minutes%", String.valueOf(minutes))
                .replace("%m%", String.valueOf(minutes))
                .replace("%ss%", pad(seconds))
                .replace("%seconds%", String.valueOf(seconds))
                .replace("%s%", String.valueOf(seconds));
    }

    private static String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public Component getBossBarTitle(int remainingSeconds, boolean premium) {
        int clamped = Math.max(0, Math.min(REWARD_INTERVAL_SECONDS, remainingSeconds));
        return (premium ? this.premiumBossBarTitles : this.normalBossBarTitles)[clamped];
    }

    public Title getZoneTitle(int minuteIndex, boolean premium) {
        int clamped = Math.max(0, Math.min(PROGRESS_STEPS, minuteIndex));
        return (premium ? this.premiumTitles : this.normalTitles)[clamped];
    }
}
