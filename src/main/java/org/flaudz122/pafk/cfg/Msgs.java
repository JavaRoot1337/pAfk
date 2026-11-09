package org.flaudz122.pafk.cfg;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Msgs {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private Msgs() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        if (input.indexOf('<') != -1 && input.indexOf('>') != -1) {
            Component parsed = tryMiniMessage(input);
            if (parsed != null) {
                return parsed;
            }
        }
        return LEGACY.deserialize(input);
    }

    private static Component tryMiniMessage(String input) {
        try {
            return MiniMessage.miniMessage().deserialize(input);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
