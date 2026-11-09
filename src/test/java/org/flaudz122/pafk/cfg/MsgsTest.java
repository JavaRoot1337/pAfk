package org.flaudz122.pafk.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

class MsgsTest {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    void emptyAndNullInputsProduceEmptyComponent() {
        assertEquals(Component.empty(), Msgs.parse(null));
        assertEquals(Component.empty(), Msgs.parse(""));
    }

    @Test
    void legacyAmpersandCodesResolveToColors() {
        Component parsed = Msgs.parse("&cБез права");
        assertEquals(NamedTextColor.RED, parsed.color());
        assertEquals("Без права", PLAIN.serialize(parsed));
    }

    @Test
    void miniMessageMarkupResolves() {
        Component parsed = Msgs.parse("<red>Красный</red>");
        assertEquals(NamedTextColor.RED, parsed.color());
        assertEquals("Красный", PLAIN.serialize(parsed));
    }

    @Test
    void brokenMarkupDoesNotThrow() {
        Component parsed = Msgs.parse("&c<неизвестный_тег>!");
        assertNotNull(parsed);
        assertTrue(PLAIN.serialize(parsed).contains("<неизвестный_тег>"));
    }
}
