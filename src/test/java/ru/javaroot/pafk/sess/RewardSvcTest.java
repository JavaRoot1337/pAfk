package ru.javaroot.pafk.sess;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.javaroot.pafk.cfg.Cfg;

class RewardSvcTest {
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
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return cfg;
    }

    @BeforeEach
    void setUp() {
        this.cfg = Cfg.from(config(CONFIG));
        this.dispatched = new ArrayList<>();
    }

    private RewardSvc serviceAt(String isoInstant) {
        Clock clock = Clock.fixed(Instant.parse(isoInstant), ZoneId.of("UTC"));
        return new RewardSvc(dispatched::add, clock);
    }

    @Test
    void weekdayIsNotWeekend() {
        assertFalse(serviceAt("2026-11-09T12:00:00Z").isWeekendNow());
        assertFalse(serviceAt("2026-11-06T12:00:00Z").isWeekendNow());
    }

    @Test
    void weekendCoversSaturdayAndSunday() {
        assertTrue(serviceAt("2026-11-07T12:00:00Z").isWeekendNow());
        assertTrue(serviceAt("2026-11-08T23:30:00Z").isWeekendNow());
    }

    @Test
    void isWeekendMatchesDayOfWeek() {
        assertTrue(RewardSvc.isWeekend(DayOfWeek.SATURDAY));
        assertTrue(RewardSvc.isWeekend(DayOfWeek.SUNDAY));
        assertFalse(RewardSvc.isWeekend(DayOfWeek.MONDAY));
        assertFalse(RewardSvc.isWeekend(DayOfWeek.FRIDAY));
    }

    @Test
    void rewardCommandDependsOnZoneAndWeekend() {
        assertEquals("wp give %player% 30", RewardSvc.commandFor(false, false, cfg));
        assertEquals("wp give %player% 60", RewardSvc.commandFor(false, true, cfg));
        assertEquals("wp give %player% 100", RewardSvc.commandFor(true, false, cfg));
        assertEquals("wp give %player% 200", RewardSvc.commandFor(true, true, cfg));
    }

    @Test
    void playerNameReplacesPlaceholder() {
        assertEquals("p give Steve 30", RewardSvc.substitutePlayer("p give %player% 30", "Steve"));
    }
}
