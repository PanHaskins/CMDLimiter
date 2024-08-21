package me.panhaskins.cmdlimit.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.panhaskins.cmdlimit.CMDLimiter;
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









        /*
        System.out.println(condition);
        if (condition.startsWith("permission:")) {
            return player.hasPermission(condition.replace("permission:", ""));
        } else if (condition.startsWith("!permission")) {
            return !player.hasPermission(condition.replace("!permission:", ""));

        } else if (condition.startsWith("world:")) {
            return player.getWorld().getName().equalsIgnoreCase(condition.replace("world:", ""));
        } else if (condition.startsWith("!world:")) {
            return !player.getWorld().getName().equalsIgnoreCase(condition.replace("!world:", ""));

        } else if (condition.startsWith("dateExpiration:")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CMDLimiter.config.get().getString("dateFormat"));
            System.out.println(dateTimeFormatter);
            System.out.println(condition.replace("dateExpiration:", ""));
            LocalDateTime removedDate = LocalDateTime.parse(condition.replace("dateExpiration:", ""), dateTimeFormatter);
            LocalDateTime currentDate = LocalDateTime.now();

            return currentDate.isBefore(removedDate);

        } else {

            Pattern pattern = Pattern.compile("^(.*)(==Aa|==|>=|<=|>|<|!=Aa|!=)(.*)$");
            Matcher matcher = pattern.matcher(PlaceholderAPI.setPlaceholders(player, condition));

            System.out.println(condition);

            if (matcher.matches()) {
                String input = matcher.group(1);
                String type = matcher.group(2);
                String output = matcher.group(3);
                System.out.println(input + " " + type + " " + output);

                switch (type) {
                    case "==Aa":
                        return input.equalsIgnoreCase(output);
                    case "==":
                        return input.equals(output);
                    case ">=":
                        return Double.parseDouble(input) >= Double.parseDouble(output);
                    case "<=":
                        return Double.parseDouble(input) <= Double.parseDouble(output);
                    case ">":
                        return Double.parseDouble(input) > Double.parseDouble(output);
                    case "<":
                        return Double.parseDouble(input) < Double.parseDouble(output);
                    case "!=":
                        return !input.equals(output);
                    case "!=Aa":
                        return !input.equalsIgnoreCase(output);
                }
            }
        }
        return false;
    }
     */
    }
}
