package ru.javaroot.pafk.zone;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ZoneBoxTest {
    @Test
    void cuboidContainsChecksBoundsAndWorld() {
        UUID w1 = UUID.randomUUID();
        UUID w2 = UUID.randomUUID();
        ZoneBox box = new ZoneBox(w1, 0, 60, -10, 10, 80, 10, null);

        assertTrue(box.contains(w1, 0, 60, -10));
        assertTrue(box.contains(w1, 10, 80, 10));
        assertFalse(box.contains(w2, 5, 70, 0));
        assertFalse(box.contains(w1, 11, 70, 0));
        assertFalse(box.contains(w1, 5, 59, 0));
    }

    @Test
    void nonCuboidDelegatesToExtraShapeCheck() {
        UUID w1 = UUID.randomUUID();
        ZoneBox box = new ZoneBox(w1, 0, 0, 0, 10, 10, 10, (x, y, z) -> x + z <= 10);

        assertTrue(box.contains(w1, 4, 5, 6));
        assertFalse(box.contains(w1, 8, 5, 8));
    }
}
