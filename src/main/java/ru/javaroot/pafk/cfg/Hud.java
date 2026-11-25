package ru.javaroot.pafk.cfg;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import ru.javaroot.pafk.plat.txt.Txt;

public final class Hud {
    public static final int PROGRESS_STEPS = 15;
    private static final int MAX_PRECOMPUTED_SEC = 3600;
    private static final Title.Times TITLE_TIMES = Title.Times.times(Duration.ZERO, Duration.ofMillis(2000), Duration.ZERO);
    private static final Component FILLED_BLOCK = Component.text('■', NamedTextColor.GREEN);
    private static final Component EMPTY_BLOCK = Component.text('□', NamedTextColor.GRAY);

    private final Cfg cfg;
    private final int intervalSec;
    private final Component[] normalBarCache;
    private final Component[] premiumBarCache;
    private final Title[] normalTitles = new Title[PROGRESS_STEPS + 1];
    private final Title[] premiumTitles = new Title[PROGRESS_STEPS + 1];

    public Hud(Cfg cfg) {
        this.cfg = cfg;
        this.intervalSec = cfg.rewardIntervalSec();

        if (intervalSec <= MAX_PRECOMPUTED_SEC) {
            this.normalBarCache = new Component[intervalSec + 1];
            this.premiumBarCache = new Component[intervalSec + 1];
            for (int sec = 0; sec <= intervalSec; sec++) {
                normalBarCache[sec] = formatBossBar(cfg.barTitleNormal(), cfg.timeFormat(), sec, intervalSec);
                premiumBarCache[sec] = formatBossBar(cfg.barTitlePremium(), cfg.timeFormat(), sec, intervalSec);
            }
        } else {
            this.normalBarCache = null;
            this.premiumBarCache = null;
        }

        Component titleNormal = Txt.parse(cfg.titleNormal());
        Component titlePremium = Txt.parse(cfg.titlePremium());
        for (int step = 0; step <= PROGRESS_STEPS; step++) {
            Component bar = Component.empty();
            for (int i = 0; i < PROGRESS_STEPS; i++) {
                bar = bar.append(i < step ? FILLED_BLOCK : EMPTY_BLOCK);
            }
            normalTitles[step] = Title.title(titleNormal, bar, TITLE_TIMES);
            premiumTitles[step] = Title.title(titlePremium, bar, TITLE_TIMES);
        }
    }

    public Component bossBarTitle(int remainingSec, boolean premium) {
        int clamped = Math.max(0, Math.min(intervalSec, remainingSec));
        if (normalBarCache != null) {
            return (premium ? premiumBarCache : normalBarCache)[clamped];
        }
        String tpl = premium ? cfg.barTitlePremium() : cfg.barTitleNormal();
        return formatBossBar(tpl, cfg.timeFormat(), clamped, intervalSec);
    }

    public Title zoneTitle(int stepIndex, boolean premium) {
        int clamped = Math.max(0, Math.min(PROGRESS_STEPS, stepIndex));
        return (premium ? premiumTitles : normalTitles)[clamped];
    }

    private static Component formatBossBar(String tpl, String timeFmt, int remainingSec, int totalSec) {
        int min = remainingSec / 60;
        int sec = remainingSec % 60;
        long pct = Math.round((1.0 - (double) remainingSec / totalSec) * 100.0);
        String expanded = expandTime(tpl, min, sec)
                .replace("%time%", expandTime(timeFmt, min, sec))
                .replace("%percent%", String.valueOf(pct));
        return Txt.parse(expanded);
    }

    private static String expandTime(String tpl, int min, int sec) {
        return tpl
                .replace("%mm%", pad(min))
                .replace("%minutes%", String.valueOf(min))
                .replace("%m%", String.valueOf(min))
                .replace("%ss%", pad(sec))
                .replace("%seconds%", String.valueOf(sec))
                .replace("%s%", String.valueOf(sec));
    }

    private static String pad(int val) {
        return val < 10 ? "0" + val : String.valueOf(val);
    }
}
