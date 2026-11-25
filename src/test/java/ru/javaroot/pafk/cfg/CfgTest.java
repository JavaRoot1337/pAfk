package ru.javaroot.pafk.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class CfgTest {
    private static final Key DEFAULT_SOUND_KEY = Key.key("entity.player.levelup");

    private static YamlConfiguration parse(String yaml) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return config;
    }

    private static String soundConfig(String name, String volume, String pitch) {
        return "sound:\n"
                + "  name: \"" + name + "\"\n"
                + "  volume: " + volume + "\n"
                + "  pitch: " + pitch + "\n";
    }

    @Test
    void malformedSoundNameFallsBackToDefault() {
        Cfg cfg = Cfg.from(parse(soundConfig("бракованный звук!!", "1.0", "1.0")));
        assertEquals(DEFAULT_SOUND_KEY, cfg.rewardSound().name());
        assertEquals(1.0f, cfg.rewardSound().volume());
        assertEquals(1.0f, cfg.rewardSound().pitch());
    }

    @Test
    void outOfRangeVolumeFallsBackToDefault() {
        Cfg cfg = Cfg.from(parse(soundConfig("entity.player.levelup", "999", "1.0")));
        assertEquals(1.0f, cfg.rewardSound().volume());
    }

    @Test
    void negativePitchFallsBackToDefault() {
        Cfg cfg = Cfg.from(parse(soundConfig("entity.player.levelup", "1.0", "-3")));
        assertEquals(1.0f, cfg.rewardSound().pitch());
    }

    @Test
    void soundBoundsPassThrough() {
        Cfg atLimits = Cfg.from(parse(soundConfig("entity.player.levelup", "4.0", "0.25")));
        assertEquals(4.0f, atLimits.rewardSound().volume());
        assertEquals(0.25f, atLimits.rewardSound().pitch());

        Cfg inRange = Cfg.from(parse(soundConfig("entity.player.levelup", "2.5", "1.75")));
        assertEquals(2.5f, inRange.rewardSound().volume());
        assertEquals(1.75f, inRange.rewardSound().pitch());
    }

    @Test
    void rewardIntervalIsClamped() {
        Cfg tooLow = Cfg.from(parse("settings:\n  reward-interval-sec: 2\n"));
        Cfg tooHigh = Cfg.from(parse("settings:\n  reward-interval-sec: 999999\n"));
        Cfg valid = Cfg.from(parse("settings:\n  reward-interval-sec: 600\n"));

        assertEquals(10, tooLow.rewardIntervalSec());
        assertEquals(86_400, tooHigh.rewardIntervalSec());
        assertEquals(600, valid.rewardIntervalSec());
    }
}
