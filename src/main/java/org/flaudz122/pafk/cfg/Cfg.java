package org.flaudz122.pafk.cfg;

import java.util.Locale;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

public final class Cfg {
    private static final String DEFAULT_SOUND = "entity.player.levelup";
    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;
    private static final double VOLUME_MAX = 4.0;
    private static final double PITCH_MIN = 0.25;
    private static final double PITCH_MAX = 2.0;

    private final String worldName;
    private final String regionNormal;
    private final String regionPremium;
    private final String premiumPermission;
    private final String rewardCommandWeekday;
    private final String rewardCommandWeekend;
    private final String rewardPremiumWeekday;
    private final String rewardPremiumWeekend;
    private final BossBar.Color barColorNormal;
    private final BossBar.Color barColorPremium;
    private final BossBar.Overlay barOverlay;
    private final String bossBarTemplateNormal;
    private final String bossBarTemplatePremium;
    private final String timeFormat;
    private final String titleTextNormal;
    private final String titleTextPremium;
    private final Component reloadSuccessMessage;
    private final Component noPermissionMessage;
    private final Component noPremiumAccessMessage;
    private final boolean soundEnabled;
    private final Sound rewardSound;

    public Cfg(FileConfiguration config) {
        this.worldName = config.getString("settings.world-name", "world");
        this.regionNormal = config.getString("settings.region-name", "afk").toLowerCase(Locale.ROOT);
        this.regionPremium = config.getString("settings.premium-region-name", "premiumafk").toLowerCase(Locale.ROOT);
        this.rewardCommandWeekday = config.getString("settings.reward-command-weekday", "p give %player% 30");
        this.rewardCommandWeekend = config.getString("settings.reward-command-weekend", "p give %player% 60");
        this.rewardPremiumWeekday = config.getString("settings.reward-premium-weekday", "p give %player% 100");
        this.rewardPremiumWeekend = config.getString("settings.reward-premium-weekend", "p give %player% 200");
        this.premiumPermission = config.getString("settings.premium-permission", "afkzone.premium");

        this.barColorNormal = enumOrDefault(config.getString("bossbar.color-normal"), BossBar.Color.class, BossBar.Color.GREEN);
        this.barColorPremium = enumOrDefault(config.getString("bossbar.color-premium"), BossBar.Color.class, BossBar.Color.PURPLE);
        this.barOverlay = enumOrDefault(config.getString("bossbar.overlay"), BossBar.Overlay.class, BossBar.Overlay.NOTCHED_10);

        String defaultBossBarTitle = config.getString("messages.bossbar-title", "&lАфк-Зона &7» &fНаграда через: &e%time%");
        this.bossBarTemplateNormal = config.getString("bossbar.title-normal", defaultBossBarTitle);
        this.bossBarTemplatePremium = config.getString("bossbar.title-premium", this.bossBarTemplateNormal);
        this.timeFormat = config.getString("bossbar.time-format", "%m% мин. %s% сек.");

        this.titleTextNormal = config.getString("messages.title-normal", "&6Афк Зона");
        this.titleTextPremium = config.getString("messages.title-premium", "&6Премиум Афк Зона");

        this.reloadSuccessMessage = Msgs.parse(config.getString("messages.reload-success", "&6Афк-Зона &7» &aНастройки обновлены!"));
        this.noPermissionMessage = Msgs.parse(config.getString("messages.no-permission", "&6Афк-Зона &7» &cУ вас недостаточно прав!"));
        this.noPremiumAccessMessage = Msgs.parse(config.getString("messages.no-premium-access", "&6Афк-Зона &7» &cУ вас нет доступа к Премиум АФК зоне!"));

        this.soundEnabled = config.getBoolean("sound.enabled", true);
        this.rewardSound = rewardSound(
                bounded(config.getDouble("sound.volume", DEFAULT_VOLUME), 0.0, VOLUME_MAX, DEFAULT_VOLUME),
                bounded(config.getDouble("sound.pitch", DEFAULT_PITCH), PITCH_MIN, PITCH_MAX, DEFAULT_PITCH),
                config.getString("sound.name", DEFAULT_SOUND));
    }

    private static float bounded(double value, double min, double max, float fallback) {
        if (Double.isNaN(value) || value < min || value > max) {
            return fallback;
        }
        return (float) value;
    }

    private static Sound rewardSound(float volume, float pitch, String name) {
        try {
            return Sound.sound(Key.key(name), Sound.Source.PLAYER, volume, pitch);
        } catch (RuntimeException e) {
            return Sound.sound(Key.key(DEFAULT_SOUND), Sound.Source.PLAYER, volume, pitch);
        }
    }

    private static <T extends Enum<T>> T enumOrDefault(String raw, Class<T> type, T fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public String getWorldName() {
        return this.worldName;
    }

    public String getRegionNormal() {
        return this.regionNormal;
    }

    public String getRegionPremium() {
        return this.regionPremium;
    }

    public String getPremiumPermission() {
        return this.premiumPermission;
    }

    public String getRewardCommandWeekday() {
        return this.rewardCommandWeekday;
    }

    public String getRewardCommandWeekend() {
        return this.rewardCommandWeekend;
    }

    public String getRewardPremiumWeekday() {
        return this.rewardPremiumWeekday;
    }

    public String getRewardPremiumWeekend() {
        return this.rewardPremiumWeekend;
    }

    public BossBar.Color getBarColorNormal() {
        return this.barColorNormal;
    }

    public BossBar.Color getBarColorPremium() {
        return this.barColorPremium;
    }

    public BossBar.Overlay getBarOverlay() {
        return this.barOverlay;
    }

    public String getBossBarTemplateNormal() {
        return this.bossBarTemplateNormal;
    }

    public String getBossBarTemplatePremium() {
        return this.bossBarTemplatePremium;
    }

    public String getTimeFormat() {
        return this.timeFormat;
    }

    public String getTitleTextNormal() {
        return this.titleTextNormal;
    }

    public String getTitleTextPremium() {
        return this.titleTextPremium;
    }

    public Component getReloadSuccessMessage() {
        return this.reloadSuccessMessage;
    }

    public Component getNoPermissionMessage() {
        return this.noPermissionMessage;
    }

    public Component getNoPremiumAccessMessage() {
        return this.noPremiumAccessMessage;
    }

    public boolean isSoundEnabled() {
        return this.soundEnabled;
    }

    public Sound getRewardSound() {
        return this.rewardSound;
    }
}
