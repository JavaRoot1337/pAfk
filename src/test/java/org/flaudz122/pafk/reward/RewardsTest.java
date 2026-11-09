package org.flaudz122.pafk.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.flaudz122.pafk.cfg.Cfg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RewardsTest {
    private static final String CONFIG = """
            settings:
              reward-command-weekday: "wp give %player% 30"
              reward-command-weekend: "wp give %player% 60"
              reward-premium-weekday: "wp give %player% 100"
              reward-premium-weekend: "wp give %player% 200"
            """;

    private Cfg cfg;
    private List<String> dispatched;

    private static YamlConfiguration config(String yaml) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return config;
    }

    @BeforeEach
    void setUp() {
        this.cfg = new Cfg(config(CONFIG));
        this.dispatched = new ArrayList<>();
    }

    private Rewards serviceAt(String isoInstant) {
        Clock clock = Clock.fixed(Instant.parse(isoInstant), ZoneId.of("UTC"));
        return new Rewards(command -> this.dispatched.add(command), clock);
    }

    @Test
    void weekdayIsNotWeekend() {
        assertFalse(this.serviceAt("2026-11-09T12:00:00Z").isWeekendNow());
        assertFalse(this.serviceAt("2026-11-06T12:00:00Z").isWeekendNow());
    }

    @Test
    void weekendCoversSaturdayAndSunday() {
        assertTrue(this.serviceAt("2026-11-07T12:00:00Z").isWeekendNow());
        assertTrue(this.serviceAt("2026-11-08T23:30:00Z").isWeekendNow());
    }

    @Test
    void weekendCheckIsStableForSameClock() {
        Rewards service = this.serviceAt("2026-11-07T12:00:00Z");
        assertTrue(service.isWeekendNow());
        assertTrue(service.isWeekendNow());
    }

    @Test
    void isWeekendMatchesDayOfWeek() {
        assertTrue(Rewards.isWeekend(DayOfWeek.SATURDAY));
        assertTrue(Rewards.isWeekend(DayOfWeek.SUNDAY));
        assertFalse(Rewards.isWeekend(DayOfWeek.MONDAY));
        assertFalse(Rewards.isWeekend(DayOfWeek.FRIDAY));
    }

    @Test
    void rewardCommandDependsOnZoneAndWeekend() {
        assertEquals("wp give %player% 30", Rewards.commandFor(false, false, this.cfg));
        assertEquals("wp give %player% 60", Rewards.commandFor(false, true, this.cfg));
        assertEquals("wp give %player% 100", Rewards.commandFor(true, false, this.cfg));
        assertEquals("wp give %player% 200", Rewards.commandFor(true, true, this.cfg));
    }

    @Test
    void playerNameReplacesPlaceholder() {
        assertEquals("p give Steve 30", Rewards.substitutePlayer("p give %player% 30", "Steve"));
    }
}
