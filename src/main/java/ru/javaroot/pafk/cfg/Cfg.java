package ru.javaroot.pafk.cfg;

import java.util.Locale;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import ru.javaroot.pafk.plat.txt.Txt;

public record Cfg(
        String worldName,
        String regionNormal,
        String regionPremium,
        String premiumPerm,
        int rewardIntervalSec,
        String rewardWeekday,
        String rewardWeekend,
        String rewardPremWeekday,
        String rewardPremWeekend,
        BossBar.Color barColorNormal,
        BossBar.Color barColorPremium,
        BossBar.Overlay barOverlay,
        String barTitleNormal,
        String barTitlePremium,
        String timeFormat,
        String titleNormal,
        String titlePremium,
        String reloadSuccessRaw,
        Component noPermMsg,
        Component noPremMsg,
        boolean soundEnabled,
        Sound rewardSound
) {
    public static final int DEFAULT_INTERVAL_SEC = 900;
    private static final int MIN_INTERVAL_SEC = 10;
    private static final int MAX_INTERVAL_SEC = 86_400;
    private static final String DEFAULT_SOUND = "entity.player.levelup";
    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;
    private static final double VOLUME_MAX = 4.0;
    private static final double PITCH_MIN = 0.25;
    private static final double PITCH_MAX = 2.0;

    public static Cfg from(FileConfiguration raw) {
        String worldName = str(raw, "settings.world-name", "world");
        String regionNormal = str(raw, "settings.region-name", "afk").toLowerCase(Locale.ROOT);
        String regionPremium = str(raw, "settings.premium-region-name", "premiumafk").toLowerCase(Locale.ROOT);
        String premiumPerm = str(raw, "settings.premium-permission", "afkzone.premium");
        int interval = clampInt(raw.getInt("settings.reward-interval-sec", DEFAULT_INTERVAL_SEC), MIN_INTERVAL_SEC, MAX_INTERVAL_SEC);

        String rewardWeekday = str(raw, "settings.reward-command-weekday", "p give %player% 30");
        String rewardWeekend = str(raw, "settings.reward-command-weekend", "p give %player% 60");
        String rewardPremWeekday = str(raw, "settings.reward-premium-weekday", "p give %player% 100");
        String rewardPremWeekend = str(raw, "settings.reward-premium-weekend", "p give %player% 200");

        BossBar.Color barColorNormal = enumOrDefault(raw.getString("bossbar.color-normal"), BossBar.Color.class, BossBar.Color.GREEN);
        BossBar.Color barColorPremium = enumOrDefault(raw.getString("bossbar.color-premium"), BossBar.Color.class, BossBar.Color.PURPLE);
        BossBar.Overlay barOverlay = enumOrDefault(raw.getString("bossbar.overlay"), BossBar.Overlay.class, BossBar.Overlay.NOTCHED_10);

        String fallbackBar = str(raw, "messages.bossbar-title", "&a&lАФК-зона &7» &fНаграда через: &e%time%");
        String barTitleNormal = str(raw, "bossbar.title-normal", fallbackBar);
        String barTitlePremium = str(raw, "bossbar.title-premium", barTitleNormal);
        String timeFormat = str(raw, "bossbar.time-format", "%m% мин. %s% сек.");

        String titleNormal = str(raw, "messages.title-normal", "&6АФК-зона");
        String titlePremium = str(raw, "messages.title-premium", "&6Премиум АФК-зона");

        String reloadSuccessRaw = str(raw, "messages.reload-success", "&aКонфиг перезагружен &7(%ms% мс).");
        Component noPermMsg = Txt.parse(str(raw, "messages.no-permission", "&cНет прав."));
        Component noPremMsg = Txt.parse(str(raw, "messages.no-premium-access", "&cНет доступа в премиум АФК-зону."));

        boolean soundEnabled = raw.getBoolean("sound.enabled", true);
        float vol = boundedFloat(raw.getDouble("sound.volume", DEFAULT_VOLUME), 0.0, VOLUME_MAX, DEFAULT_VOLUME);
        float pitch = boundedFloat(raw.getDouble("sound.pitch", DEFAULT_PITCH), PITCH_MIN, PITCH_MAX, DEFAULT_PITCH);
        Sound sound = parseSound(str(raw, "sound.name", DEFAULT_SOUND), vol, pitch);

        return new Cfg(
                worldName,
                regionNormal,
                regionPremium,
                premiumPerm,
                interval,
                rewardWeekday,
                rewardWeekend,
                rewardPremWeekday,
                rewardPremWeekend,
                barColorNormal,
                barColorPremium,
                barOverlay,
                barTitleNormal,
                barTitlePremium,
                timeFormat,
                titleNormal,
                titlePremium,
                reloadSuccessRaw,
                noPermMsg,
                noPremMsg,
                soundEnabled,
                sound
        );
    }

    private static String str(FileConfiguration raw, String path, String def) {
        String val = raw.getString(path);
        return val != null ? val : def;
    }

    private static int clampInt(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static float boundedFloat(double val, double min, double max, float fallback) {
        if (Double.isNaN(val) || val < min || val > max) {
            return fallback;
        }
        return (float) val;
    }

    private static Sound parseSound(String name, float vol, float pitch) {
        try {
            return Sound.sound(Key.key(name), Sound.Source.PLAYER, vol, pitch);
        } catch (RuntimeException ex) {
            return Sound.sound(Key.key(DEFAULT_SOUND), Sound.Source.PLAYER, vol, pitch);
        }
    }

    private static <T extends Enum<T>> T enumOrDefault(String raw, Class<T> type, T fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
