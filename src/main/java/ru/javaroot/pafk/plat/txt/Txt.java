package ru.javaroot.pafk.plat.txt;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public final class Txt {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Pattern HEX_HASH = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final Pattern HEX_PLAIN = Pattern.compile("&([0-9A-Fa-f]{6})");
    private static final Pattern CODE_AMP = Pattern.compile("&([0-9a-fA-Fk-oK-OrR])");
    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "<black>"),
            Map.entry('1', "<dark_blue>"),
            Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"),
            Map.entry('4', "<dark_red>"),
            Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"),
            Map.entry('7', "<gray>"),
            Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"),
            Map.entry('a', "<green>"),
            Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"),
            Map.entry('d', "<light_purple>"),
            Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"),
            Map.entry('k', "<obfuscated>"),
            Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),
            Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>")
    );

    private Txt() {
    }

    public static String toMini(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String hexReplaced = HEX_HASH.matcher(raw).replaceAll("<#$1>");
        hexReplaced = HEX_PLAIN.matcher(hexReplaced).replaceAll("<#$1>");
        if (hexReplaced.indexOf('&') < 0) {
            return hexReplaced;
        }
        Matcher m = CODE_AMP.matcher(hexReplaced);
        StringBuilder sb = new StringBuilder(hexReplaced.length());
        while (m.find()) {
            char ch = Character.toLowerCase(m.group(1).charAt(0));
            String tag = LEGACY_TAGS.get(ch);
            m.appendReplacement(sb, tag != null ? Matcher.quoteReplacement(tag) : m.group(0));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static Sub text(String key, Object val) {
        return new Sub(key, MM.escapeTags(String.valueOf(val)), false);
    }

    public static Sub color(String key, String val) {
        return new Sub(key, val == null ? "" : val, true);
    }

    public static Component parse(String raw, Sub... subs) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        String out = toMini(raw);
        if (subs != null) {
            for (Sub s : subs) {
                String rep = s.colored() ? toMini(s.val()) : s.val();
                out = out.replace(s.key(), rep);
            }
        }
        try {
            return MM.deserialize(out);
        } catch (RuntimeException ex) {
            return Component.text(raw);
        }
    }

    public static void send(CommandSender snd, String raw, Sub... subs) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        snd.sendMessage(parse(raw, subs));
    }

    public record Sub(String key, String val, boolean colored) {
    }
}
