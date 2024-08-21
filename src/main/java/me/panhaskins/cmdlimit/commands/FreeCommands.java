package me.panhaskins.cmdlimit.commands;

import me.panhaskins.cmdlimit.CMDLimiter;
import me.panhaskins.cmdlimit.api.APIColor;
import me.panhaskins.cmdlimit.utils.ConditionUtils;
import me.panhaskins.cmdlimit.utils.command.Commander;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class FreeCommands extends Commander {


    private final ConfigurationSection commandSection;

    public FreeCommands(String name, ConfigurationSection commandSection) {
        super(name);
        this.commandSection = commandSection;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sendMessage(sender, CMDLimiter.config.get().getString("playerOnly"));
            return true;
        }

        if (checkCommandConditions((Player) sender, commandSection)){
            executeCommandIfAllowed((Player) sender, getName(), commandSection);
                return true;
            }
        return false;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commandList = new ArrayList<>(CMDLimiter.commandList);
        return commandList;
    }

    public boolean checkCommandConditions (Player player, ConfigurationSection commandSection) {
        int minimumRequirements = commandSection.getInt("minimumRequirements", 0);
        ConfigurationSection requirementsSection = commandSection.getConfigurationSection("requirements");
        if (requirementsSection != null && minimumRequirements > 0) {
            int metRequirements = 0;

            for (String requirement : requirementsSection.getKeys(false)) {
                if (metRequirements >= minimumRequirements) {
                    System.out.println("command conditions presiel mojko");
                    return true;
                }

                if (ConditionUtils.checkCondition(player,
                        requirementsSection.getString(requirement + ".type"),
                        requirementsSection.getString(requirement + ".input"),
                        requirementsSection.getString(requirement + ".output")
                )) {
                    metRequirements++;
                    System.out.println("command condition prave presiel");
                } else {
                    String denyMessage = requirementsSection.getString(requirement + ".denyMessage");
                    if (denyMessage != null) {
                        player.sendMessage(APIColor.process(denyMessage));
                        return false;
                    }
                }
            }
        }
        return true;

    }

    private void executeCommandIfAllowed(Player player, String command, ConfigurationSection commandSection) {
        int maxUse = commandSection.getInt("maxUse");
        int playerUse = CMDLimiter.dataManager.getPlayer(player, command);
        System.out.println("use: " + playerUse + " max: " + maxUse);

        if (playerUse < maxUse) {
            CMDLimiter.dataManager.setPlayer(player.getName(), command, playerUse + 1);
            commandSection.getStringList("console").forEach(consoleCommand ->
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.replace("%player%", player.getName())));
            player.sendMessage(APIColor.process(commandSection.getString("use")));
        } else{
            player.sendMessage(APIColor.process(commandSection.getString("used")));
        }

    }
}
