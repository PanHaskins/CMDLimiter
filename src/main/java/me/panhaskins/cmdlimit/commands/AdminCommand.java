package me.panhaskins.cmdlimit.commands;

import me.panhaskins.cmdlimit.CMDLimiter;
import me.panhaskins.cmdlimit.utils.command.CommandManager;
import me.panhaskins.cmdlimit.utils.command.Commander;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

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
            sendMessage(sender, CMDLimiter.config.get().getString("noPermission").replace("%command%", args[0]));
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, CMDLimiter.config.get().getStringList("help"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                sendMessage(sender, CMDLimiter.config.get().getString("reloading"));
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

                sendMessage(sender, CMDLimiter.config.get().getString("reloadComplete"));
                break;

            case "set":
                if (args.length < 3) {
                    sendMessage(sender, CMDLimiter.config.get().getString("commandUsage"));
                    return true;
                }
                String target = args[1];
                String commandName = args[2];
                if (!CMDLimiter.commandList.contains(commandName)){
                    sendMessage(sender, CMDLimiter.config.get().getString("commandNotFound").replaceAll("%command%", "/" + commandName));
                    return true;
                }
                int uses = args.length > 3 ? Integer.parseInt(args[3]) : 0;
                CMDLimiter.dataManager.setPlayer(target, commandName, uses);
                sendMessage(sender, CMDLimiter.config.get().getString("setPlayerUses")
                        .replace("%player%", target)
                        .replace("%uses%", String.valueOf(uses)));
                break;

            case "save":
                CMDLimiter.dataManager.save();
                sendMessage(sender, CMDLimiter.config.get().getString("dataSaved"));
                break;

            default:
                sendMessage(sender, CMDLimiter.config.get().getStringList("help"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "set", "save"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            Set<String> uniquePlayers = new HashSet<>();

            for (String cmd : CMDLimiter.commandList) {
                uniquePlayers.addAll(CMDLimiter.dataManager.getPlayerNames(cmd));
            }

            completions.addAll(uniquePlayers);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(CMDLimiter.commandList);
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
