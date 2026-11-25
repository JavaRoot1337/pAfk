package ru.javaroot.pafk.plat.txt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

class TxtTest {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    void emptyAndNullProduceEmptyComponent() {
        assertEquals(Component.empty(), Txt.parse(null));
        assertEquals(Component.empty(), Txt.parse(""));
    }

    @Test
    void legacyAmpersandResolvesToColor() {
        Component parsed = Txt.parse("&cНет прав.");
        assertEquals(NamedTextColor.RED, parsed.color());
        assertEquals("Нет прав.", PLAIN.serialize(parsed));
    }

    @Test
    void hexFormatsResolveToTextColor() {
        Component hashHex = Txt.parse("&#FFB703Золото");
        Component plainHex = Txt.parse("&FFB703Золото");
        assertEquals(TextColor.color(0xFFB703), hashHex.color());
        assertEquals(TextColor.color(0xFFB703), plainHex.color());
        assertEquals("Золото", PLAIN.serialize(hashHex));
    }

    @Test
    void placeholdersSubstituteSafely() {
        Component parsed = Txt.parse(
                "&aПерезагружен за &f%ms% &aмс, статус: %st%",
                Txt.text("%ms%", 12),
                Txt.color("%st%", "&eок")
        );
        String plain = PLAIN.serialize(parsed);
        assertTrue(plain.contains("12"));
        assertTrue(plain.contains("ок"));
    }

    @Test
    void unknownTagsDoNotThrow() {
        Component parsed = Txt.parse("&c<неизвестный_тег>!");
        assertNotNull(parsed);
        assertTrue(PLAIN.serialize(parsed).contains("<неизвестный_тег>"));
    }
}
