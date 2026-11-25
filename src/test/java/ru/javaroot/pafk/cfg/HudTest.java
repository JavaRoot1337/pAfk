package ru.javaroot.pafk.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class HudTest {
    private static final String CONFIG = """
            settings:
              reward-interval-sec: 900
            bossbar:
              color-normal: "GREEN"
              color-premium: "PURPLE"
              overlay: "NOTCHED_10"
              title-normal: "&a&lАФК-зона &7» &fНаграда через: &e%time%"
              title-premium: "&d&lПремиум АФК-зона &7» &fНаграда через: &e%time%"
              time-format: "%m% мин. %s% сек."
            messages:
              title-normal: "&6АФК-зона"
              title-premium: "&6Премиум АФК-зона"
            """;

    private static YamlConfiguration config(String yaml) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return cfg;
    }

    private static Hud hud() {
        return new Hud(Cfg.from(config(CONFIG)));
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static int count(String text, char ch) {
        return (int) text.chars().filter(current -> current == ch).count();
    }

    @Test
    void bossBarTitleFormatsMinutesAndSeconds() {
        Hud hud = hud();
        assertTrue(plain(hud.bossBarTitle(900, false)).contains("15 мин. 0 сек."));
        assertTrue(plain(hud.bossBarTitle(65, false)).contains("1 мин. 5 сек."));
        assertTrue(plain(hud.bossBarTitle(0, false)).contains("0 мин. 0 сек."));
    }

    @Test
    void bossBarTitleClampsOutOfRangeSeconds() {
        Hud hud = hud();
        assertEquals(plain(hud.bossBarTitle(0, false)), plain(hud.bossBarTitle(-10, false)));
        assertEquals(plain(hud.bossBarTitle(900, false)), plain(hud.bossBarTitle(1500, false)));
    }

    @Test
    void premiumAndNormalTitlesDiffer() {
        Hud hud = hud();
        assertNotEquals(hud.bossBarTitle(300, false), hud.bossBarTitle(300, true));
        assertNotEquals(hud.zoneTitle(2, false), hud.zoneTitle(2, true));
    }

    @Test
    void titleProgressCountsFilledBlocksByStep() {
        Hud hud = hud();
        for (int step = 0; step <= Hud.PROGRESS_STEPS; step++) {
            String subtitle = plain(hud.zoneTitle(step, false).subtitle());
            assertEquals(step, count(subtitle, '■'));
            assertEquals(Hud.PROGRESS_STEPS - step, count(subtitle, '□'));
        }
    }

    @Test
    void largeIntervalFormatsWithoutPrecomputedCache() {
        String largeYaml = CONFIG.replace("reward-interval-sec: 900", "reward-interval-sec: 4000");
        Hud hud = new Hud(Cfg.from(config(largeYaml)));
        assertTrue(plain(hud.bossBarTitle(3665, false)).contains("61 мин. 5 сек."));
    }
}
