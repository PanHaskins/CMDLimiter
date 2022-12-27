package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.api.APIColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Commands implements CommandExecutor {

    public void processCommand(Player player, String cmdName) {

        ConfigurationSection commandSection = CMDLimiter.config.get().getConfigurationSection("commands." + cmdName);
        if (commandSection == null) {
            return;
        }


        if (!CMDLimiter.searchPlayer(player, cmdName)) {
            List<String> consoleCommandsList = commandSection.getStringList("console");
            ArrayList<String> playerName = (ArrayList<String>) CMDLimiter.data.get().getStringList("Data." + cmdName);
            for (String consoleCommands : consoleCommandsList) {
                consoleCommands = consoleCommands.replace("%player%", player.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommands);
            }

            player.sendMessage(APIColor.process(commandSection.getString("use")));
            playerName.add(player.getName());
            CMDLimiter.data.get().set("Data." + cmdName, playerName);
            CMDLimiter.data.save();
            CMDLimiter.data.reload();

        } else {
            player.sendMessage(APIColor.process(commandSection.getString("used")));
        }

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Set<String> commandsList = CMDLimiter.config.get().getConfigurationSection("commands").getKeys(false);
        Player player = (Player) sender;
        for (String cmdName : commandsList) {
            if (command.getName().equalsIgnoreCase(cmdName)) {

                if (CMDLimiter.config.get().getString("commands." + cmdName + ".permission").isEmpty()){

                    if (player != null) {
                        processCommand(player, cmdName);
                    }

                } else {
                    if (player.hasPermission(Objects.requireNonNull(CMDLimiter.config.get().getString("commands." + cmdName + ".permission")))) {
                        if (player != null) {
                            processCommand(player, cmdName);
                        }
                    } else {
                        sender.sendMessage(APIColor.process(CMDLimiter.config.get().getString("noPermission").replaceAll("%command%", cmdName)));
                    }
                }



                return true;
            }
        }

        return false;
    }
}