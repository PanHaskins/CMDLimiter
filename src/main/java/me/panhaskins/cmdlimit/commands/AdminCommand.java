package me.panhaskins.cmdlimit.commands;

import me.panhaskins.cmdlimit.CMDLimiter;
import me.panhaskins.cmdlimit.utils.Messager;
import me.panhaskins.cmdlimit.utils.command.CommandManager;
import me.panhaskins.cmdlimit.utils.command.Commander;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AdminCommand extends Commander {

    private CMDLimiter plugin;

    public AdminCommand(CMDLimiter plugin) {
        super("cmdlimiter");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cmdlimiter.admin")) {
            CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("noPermission").replace("%command%", label));
            return true;
        }

        if (args.length == 0) {
            CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getStringList("help"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("reloading"));
                CMDLimiter.config.reload();
                CMDLimiter.dataManager.save();
                ConfigurationSection commandSection = CMDLimiter.config.get().getConfigurationSection("commands");
                CMDLimiter.commandList.retainAll(commandSection.getKeys(false));
                if (!CMDLimiter.commandList.isEmpty()) {
                    for (String commandName : CMDLimiter.commandList) {
                        CommandManager.unregisterCommand(commandName);
                        CommandManager.registerCommand(plugin, new FreeCommands(commandName, commandSection.getConfigurationSection(commandName)));
                    }
                }

                CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("reloadComplete"));
                break;
            case "add":
            case "remove":
            case "set":
                if (args.length < 3) {
                    CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("commandUsage")
                            .replaceAll("%command%", "cmdlimiter " + args[0] + " <player> [command] <uses>"));
                    return true;
                }
                // target = args[1];
                // commandName = args[2];
                if (!CMDLimiter.commandList.contains(args[2].replace("_", " "))) {
                    CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("commandNotFound").replaceAll("%command%", args[2]));
                    return true;
                }
                int uses = args.length > 3 ? Integer.parseInt(args[3]) : (args[0].equalsIgnoreCase("set") ? 0 : 1);

                if (args[0].equalsIgnoreCase("add")) {
                    uses += CMDLimiter.dataManager.getPlayer(args[1], args[2].replace("_", " "));
                } else if (args[0].equalsIgnoreCase("remove")) {
                    uses -= CMDLimiter.dataManager.getPlayer(args[1], args[2].replace("_", " "));
                }

                CMDLimiter.dataManager.setPlayer(args[1], args[2], uses);
                CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("setPlayerUses")
                        .replace("%player%", args[1])
                        .replace("%uses%", String.valueOf(uses))
                        .replace("%command%", args[2]));
                break;
            case "save":
                CMDLimiter.dataManager.save();
                CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("dataSaved"));
                break;

            default:
                CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getStringList("help"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("cmdlimiter.admin")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("reload", "set", "add", "remove", "save"));
            } else if (args.length == 2 && Arrays.asList("set", "add", "remove").contains(args[0].toLowerCase())) {
                Set<String> uniquePlayers = new HashSet<>();
                CMDLimiter.commandList.forEach(cmd -> uniquePlayers.addAll(CMDLimiter.dataManager.getPlayerNames(cmd)));
                if (args[0].contains("add")) Bukkit.getServer().getOnlinePlayers().forEach(player -> uniquePlayers.add(player.getName()));
                completions.addAll(uniquePlayers);
            } else if (args.length == 3 && Arrays.asList("set", "add", "remove").contains(args[0].toLowerCase())) {
                completions.addAll(CMDLimiter.commandList);
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
