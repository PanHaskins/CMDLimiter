package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.api.APIColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class Commands implements CommandExecutor {

    public void researchCommand(Player player, String cmdName) {

        ConfigurationSection commandSection = CMDLimiter.config.get().getConfigurationSection("commands." + cmdName);
        ConfigurationSection dataSection = CMDLimiter.data.get().getConfigurationSection("Data." + cmdName);
        if (commandSection == null) {
            return;
        }

        if (dataSection == null) {
            dataSection = CMDLimiter.data.get().createSection("Data." + cmdName);
            CMDLimiter.data.save();
        }

        Map<String, Integer> playersWithValue = new HashMap<>();

        for (String playerList : CMDLimiter.data.get().getConfigurationSection("Data." + cmdName).getKeys(false)) {
            playersWithValue.put(playerList, CMDLimiter.data.get().getInt("Data." + cmdName + "." + playerList));
        }

        int playerValue = playersWithValue.getOrDefault(player.getName(), 0);
        int maxUse = CMDLimiter.config.get().getInt("commands." + cmdName + ".maxUse");


            if (!dataSection.getKeys(false).contains(player.getName())) {

                processCommand(playersWithValue, player, playerValue, cmdName);

            } else {

                if (playersWithValue.get(player.getName()) >= maxUse) {
                    player.sendMessage(APIColor.process(commandSection.getString("used")));
                } else {
                    processCommand(playersWithValue, player, playerValue, cmdName);
                }

            }
        }


    public void processCommand(Map<String, Integer> playersWithValue, Player player, int playerValue, String cmdName) {
        ConfigurationSection commandSection = CMDLimiter.config.get().getConfigurationSection("commands." + cmdName);

        List<String> consoleCommandsList = commandSection.getStringList("console");

        for (String consoleCommands : consoleCommandsList) {
            consoleCommands = consoleCommands.replace("%player%", player.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommands);
        }

        player.sendMessage(APIColor.process(commandSection.getString("use")));
        playersWithValue.put(player.getName(), playerValue + 1);
        CMDLimiter.data.get().set("Data." + cmdName, playersWithValue);
        CMDLimiter.data.save();
        CMDLimiter.data.reload();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Set<String> commandsList = CMDLimiter.config.get().getConfigurationSection("commands").getKeys(false);

        for (String cmdName : commandsList) {
            if (command.getName().equalsIgnoreCase(cmdName)) {
                if (sender instanceof Player){
                    Player player = (Player) sender;
                    if (CMDLimiter.config.get().getString("commands." + cmdName + ".permission").isEmpty()){

                        researchCommand(player, cmdName);

                        } else {
                            if (player.hasPermission(CMDLimiter.config.get().getString("commands." + cmdName + ".permission"))) {

                                researchCommand(player, cmdName);

                            } else {
                                sender.sendMessage(APIColor.process(CMDLimiter.config.get().getString("noPermission").replaceAll("%command%", cmdName)));
                            }
                    }
                    } else {
                        Bukkit.getConsoleSender().sendMessage(APIColor.process(CMDLimiter.config.get().getString("onlyForPlayer")));
                    }


                return true;
            }
        }

        return false;
    }
}