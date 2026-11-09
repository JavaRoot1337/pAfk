package org.flaudz122.pafk.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class HudTest {
    private static final String CONFIG = """
            bossbar:
              color-normal: "GREEN"
              color-premium: "PURPLE"
              overlay: "NOTCHED_10"
              title-normal: "&a&lАфк-Зона &7» &fНаграда через: &e%time%"
              title-premium: "&d&lПремиум Афк-Зона &7» &fНаграда через: &e%time%"
              time-format: "%m% мин. %s% сек."
            messages:
              title-normal: "&6Афк Зона"
              title-premium: "&6Премиум Афк Зона"
              bossbar-title: "&lАфк-Зона &7» &fНаграда через: &e%time%"
            """;

    private static YamlConfiguration config(String yaml) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return config;
    }

    private static Hud hud() {
        return new Hud(new Cfg(config(CONFIG)));
    }

    private static String plain(net.kyori.adventure.text.Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static int count(String text, char ch) {
        return (int) text.chars().filter(current -> current == ch).count();
    }

    @Test
    void bossBarTitleFormatsMinutesAndSeconds() {
        Hud hud = hud();
        assertTrue(plain(hud.getBossBarTitle(900, false)).contains("15 мин. 0 сек."));
        assertTrue(plain(hud.getBossBarTitle(65, false)).contains("1 мин. 5 сек."));
        assertTrue(plain(hud.getBossBarTitle(0, false)).contains("0 мин. 0 сек."));
    }

    @Test
    void bossBarTitleClampsOutOfRangeSeconds() {
        Hud hud = hud();
        assertEquals(plain(hud.getBossBarTitle(0, false)), plain(hud.getBossBarTitle(-10, false)));
        assertEquals(plain(hud.getBossBarTitle(900, false)), plain(hud.getBossBarTitle(1500, false)));
    }

    @Test
    void premiumAndNormalTitlesDiffer() {
        Hud hud = hud();
        assertNotEquals(hud.getBossBarTitle(300, false), hud.getBossBarTitle(300, true));
        assertNotEquals(hud.getZoneTitle(2, false), hud.getZoneTitle(2, true));
    }

    @Test
    void titleProgressCountsFilledBlocksByMinute() {
        Hud hud = hud();
        for (int minute = 0; minute <= Hud.PROGRESS_STEPS; minute++) {
            String subtitle = plain(hud.getZoneTitle(minute, false).subtitle());
            assertEquals(minute, count(subtitle, '■'));
            assertEquals(Hud.PROGRESS_STEPS - minute, count(subtitle, '□'));
        }
    }
}
