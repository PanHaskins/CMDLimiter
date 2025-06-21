package me.panhaskins.cmdlimit.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.panhaskins.cmdlimit.CMDLimiter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConditionUtils {

    public static boolean checkCondition(Player player, String type, String input, String output) {
        if(player == null || type == null || input == null || output == null) return false;

        input = PlaceholderAPI.setPlaceholders(player, input);
        output = PlaceholderAPI.setPlaceholders(player, output);

        switch (type) {
            case "<":
                if ("currentDate".equals(input)) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime outputDate = LocalDateTime.parse(output, dateTimeFormatter);
                    return currentDate.isBefore(outputDate);
                }
                return Double.parseDouble(input) < Double.parseDouble(output);
            case ">":
                if ("currentDate".equals(input)) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime outputDate = LocalDateTime.parse(output, dateTimeFormatter);
                    return currentDate.isAfter(outputDate);
                }
                return Double.parseDouble(input) > Double.parseDouble(output);
            case "<=":
            case "=<":
                if ("currentDate".equals(input)) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime outputDate = LocalDateTime.parse(output, dateTimeFormatter);
                    return currentDate.isBefore(outputDate) || currentDate.isEqual(outputDate);
                }
                return Double.parseDouble(input) <= Double.parseDouble(output);
            case ">=":
            case "=>":
                if ("currentDate".equals(input)) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime outputDate = LocalDateTime.parse(output, dateTimeFormatter);
                    return currentDate.isAfter(outputDate) || currentDate.isEqual(outputDate);
                }
                return Double.parseDouble(input) >= Double.parseDouble(output);
            case "=":
            case "==":
                if ("currentDate".equals(input)) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime outputDate = LocalDateTime.parse(output, dateTimeFormatter);
                    return currentDate.isEqual(outputDate);
                }
                return input.equalsIgnoreCase(output);
            case "=!":
            case "!=":
                if ("currentDate".equals(input)) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime outputDate = LocalDateTime.parse(output, dateTimeFormatter);
                    return !currentDate.isEqual(outputDate);
                }
                return !input.equalsIgnoreCase(output);

        }
        return false;

    }

    public static boolean checkRequirements(Player player, ConfigurationSection section) {
        int minimumRequirements = section.getInt("minimumRequirements", 0);
        ConfigurationSection requirementsSection = section.getConfigurationSection("requirements");
        if (requirementsSection != null && minimumRequirements > 0) {
            int metRequirements = 0;

            for (String requirement : requirementsSection.getKeys(false)) {
                if (metRequirements >= minimumRequirements) {
                    return true;
                }

                if (checkCondition(player,
                        requirementsSection.getString(requirement + ".type"),
                        requirementsSection.getString(requirement + ".input"),
                        requirementsSection.getString(requirement + ".output")
                )) {
                    metRequirements++;
                } else {
                    player.spigot().sendMessage(Messager.translateToBaseComponents(
                            requirementsSection.getString(requirement + ".denyMessage", "&cYou do not meet the requirements for this command!"),
                            player
                    ));
                    return false;

                }
            }
        }
        return true;

    }
}
