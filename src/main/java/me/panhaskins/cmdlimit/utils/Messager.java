package me.panhaskins.cmdlimit.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.OfflinePlayer;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class Messager {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    /**
     * Translates legacy colors, hex, gradients, and MiniMessage formatting.
     */
    public static Component translate(String text) {
        if (text == null) return Component.empty();
        text = translateLegacyColors(text);
        text = parseFancyGradients(text);
        text = translateHexColors(text);
        text = translateBukkitHex(text);
        text = translateAmpHex(text);
        return MINI.deserialize(text);
    }

    /**
     * Translates and applies PlaceholderAPI.
     */
    public static Component translate(String text, OfflinePlayer player) {
        if (player != null && isPapiPresent() && text != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return translate(text);
    }

    /**
     * Converts legacy colors (&a, §b) to MiniMessage tags.
     */
    private static String translateLegacyColors(String text) {
        Pattern p = Pattern.compile("(?i)[&§]([0-9A-FK-OR])");
        return p.matcher(text).replaceAll(m -> {
            switch (m.group(1).toLowerCase(Locale.ROOT)) {
                case "0": return "<black>";
                case "1": return "<dark_blue>";
                case "2": return "<dark_green>";
                case "3": return "<dark_aqua>";
                case "4": return "<dark_red>";
                case "5": return "<dark_purple>";
                case "6": return "<gold>";
                case "7": return "<gray>";
                case "8": return "<dark_gray>";
                case "9": return "<blue>";
                case "a": return "<green>";
                case "b": return "<aqua>";
                case "c": return "<red>";
                case "d": return "<light_purple>";
                case "e": return "<yellow>";
                case "f": return "<white>";
                case "k": return "<obfuscated>";
                case "l": return "<bold>";
                case "m": return "<strikethrough>";
                case "n": return "<underlined>";
                case "o": return "<italic>";
                case "r": return "<reset>";
                default: return "";
            }
        });
    }

    /**
     * Converts gradients like {#RRGGBB>}...{#RRGGBB<>}...{#RRGGBB<} to <gradient:#A:#B:#C>...</gradient>
     */
    private static String parseFancyGradients(String input) {
        Pattern p = Pattern.compile("\\{#([A-Fa-f0-9]{6})(>|<>|<)}");
        Matcher m = p.matcher(input);
        StringBuilder output = new StringBuilder();
        int lastEnd = 0;
        List<String> colors = new ArrayList<>();
        StringBuilder textBlock = new StringBuilder();
        int textStart = -1;
        int textEnd = -1;
        boolean collecting = false;

        while (m.find()) {
            String color = "#" + m.group(1);
            String type = m.group(2);
            switch (type) {
                case ">":
                    collecting = true;
                    colors.clear();
                    colors.add(color);
                    textBlock.setLength(0);
                    textStart = m.end();
                    textEnd = m.end();
                    break;
                case "<>":
                    if (collecting && textStart != -1) {
                        textBlock.append(input, textEnd, m.start());
                        colors.add(color);
                        textEnd = m.end();
                    }
                    break;
                case "<":
                    if (collecting && textStart != -1) {
                        textBlock.append(input, textEnd, m.start());
                        colors.add(color);
                        output.append(input, lastEnd, textStart - m.group().length());
                        output.append("<gradient:").append(String.join(":", colors)).append(">");
                        output.append(textBlock);
                        output.append("</gradient>");
                        lastEnd = m.end();
                        collecting = false;
                        textStart = -1;
                        textEnd = -1;
                    }
                    break;
            }
        }
        if (lastEnd < input.length()) output.append(input.substring(lastEnd));
        return output.toString();
    }

    /**
     * Converts &#RRGGBB and {#RRGGBB} to MiniMessage hex.
     */
    private static String translateHexColors(String text) {
        Pattern p = Pattern.compile("(?i)(?:&|§)#([A-F0-9]{6})|\\{#([A-F0-9]{6})\\}");
        return p.matcher(text).replaceAll(m -> "<#" + (m.group(1) != null ? m.group(1) : m.group(2)) + ">");
    }


    /**
     * Converts §x§R§R§G§G§B§B to MiniMessage hex.
     */
    private static String translateBukkitHex(String text) {
        Pattern p = Pattern.compile("§x((?:§[A-Fa-f0-9]){6})");
        return p.matcher(text).replaceAll(m -> {
            String hex = m.group(1).replaceAll("[§&]", "");
            return "<#" + hex + ">";
        });
    }

    /**
     * Converts &x&R&R&G&G&B&B to MiniMessage hex.
     */
    private static String translateAmpHex(String text) {
        Pattern p = Pattern.compile("&x((?:&[A-Fa-f0-9]){6})");
        return p.matcher(text).replaceAll(m -> {
            String hex = m.group(1).replaceAll("[§&]", "");
            return "<#" + hex + ">";
        });
    }

    /**
     * Translates to BaseComponent[] for Spigot/Bungee.
     */
    public static BaseComponent[] translateToBaseComponents(String text) {
        Component adventureComponent = translate(text);
        return BungeeComponentSerializer.get().serialize(adventureComponent);
    }

    public static BaseComponent[] translateToBaseComponents(String text, OfflinePlayer player) {
        Component adventureComponent = translate(text, player);
        return BungeeComponentSerializer.get().serialize(adventureComponent);
    }

    public static List<BaseComponent[]> translateToBaseComponents(List<String> lines) {
        return lines == null ? null : lines.stream().map(Messager::translateToBaseComponents).collect(Collectors.toList());
    }

    public static List<BaseComponent[]> translateToBaseComponents(List<String> lines, OfflinePlayer player) {
        return lines == null ? null : lines.stream().map(line -> translateToBaseComponents(line, player)).collect(Collectors.toList());
    }

    private static boolean isPapiPresent() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}