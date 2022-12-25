package me.panhaskins.cmdlimit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

public class Commands implements CommandExecutor {

    private Plugin plugin;

    public Commands(Plugin plugin){
        this.plugin = plugin;

    }


    public void processCommand(Player player, String commandName){
        ConfigurationSection commandSection = CMDLimiter.config.get().getConfigurationSection("commands" + commandName);
        if (commandSection == null){
            return;
        }

        List<String> consoleCommandsList = CMDLimiter.config.get().getStringList("console");

        for (String consoleCommands : consoleCommandsList){
            consoleCommands = consoleCommands.replace("%player%", player.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommands);
        }

        player.sendMessage(APIColor.process(commandSection.getString("use")));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Set<String> commandsList = CMDLimiter.config.get().getConfigurationSection("commands").getKeys(false);
        Player player = (Player) sender;
        for (String cmdName : commandsList) {
            if (command.getName().equalsIgnoreCase(cmdName)) {

                if (sender instanceof Player) {
                    processCommand(player, cmdName);
                } else {
                    sender.sendMessage(APIColor.process(CMDLimiter.config.get().getString("onlyPlayer")));
                }
                return true;
            }
        }

        return false;
    }
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//
//            if (!main.data.get().contains("Data.free." + sender.getName()))
//              {
//
//                main.data.get().set("Data.free." + sender.getName() + ".picked", Boolean.TRUE);
//                main.data.save();
//                main.data.reload();

//                for (String commands : consoleCommandList){
//                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), APIColor.process(commands.replaceAll("%player%", sender.getName())));
//                }

//                sender.sendMessage(APIColor.process(main.config.get().getString("canUse")));
//                sender.sendMessage();
//              }
//            else
//              {
//                sender.sendMessage(APIColor.process(main.config.get().getString("cantUse")));
//              }

//            return false;
//    }


}
