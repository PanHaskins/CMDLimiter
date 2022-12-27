package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.api.APIColor;
import me.panhaskins.cmdlimit.api.APIConfig;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;


public final class CMDLimiter extends JavaPlugin implements Listener {

    public static APIConfig config, data;

    @Override
    public void onEnable() {

        // Plugin startup logic
        config = new APIConfig(this, getDataFolder() + File.separator + "config.yml" , "config.yml");
        data = new APIConfig(this, getDataFolder() + File.separator + "data.yml" , "data.yml");


        registerCommands();

        this.getServer().getPluginManager().registerEvents(this, this);

        }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("clreload")) {
            if (sender.hasPermission("cl.reload")) {
                sender.sendMessage(APIColor.process(config.get().getString("reloading")));
                config.reload();
                data.reload();
                sender.sendMessage(APIColor.process(config.get().getString("reloadComplete")));
            }
            else
            {
                sender.sendMessage(APIColor.process(config.get().getString("noPermission").replaceAll("%command%", "clreload")));
            }
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Set<String> commandsList = config.get().getConfigurationSection("commands").getKeys(false);
        Player player = e.getPlayer();
        for (String cmdName : commandsList) {

            if (config.get().contains("commands." + cmdName + ".join.enabled")){

                if (!searchPlayer(player, cmdName)) {
                    for (String joinMessage : APIColor.process(config.get().getStringList("commands." + cmdName + ".join.message"))) {

                        player.sendMessage(joinMessage);

                    }
                }

            }

        }
    }

    public static boolean searchPlayer(Player player, String cmdName){
        for (String playerList : CMDLimiter.data.get().getStringList("Data." + cmdName)) {

            if (playerList.contains(player.getName())) {
                return true;

            }
        }
        return false;
    }

    private void registerCommands() {
        Set<String> commandsList = config.get().getConfigurationSection("commands").getKeys(false);
        for (String cmdName : commandsList) {

            registerCommand(new Commands(), config.get().getString(cmdName + ".description"), cmdName);

        }

    }

    private void registerCommand(CommandExecutor commandExecutor, String description, String name, String... aliases) { //to co znamenaj tie tri bodky. To som este nevidel uvidis
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(name, this);
            pluginCommand.setExecutor(commandExecutor);

            if (description != null) pluginCommand.setDescription(description);
            pluginCommand.setAliases(Arrays.asList(aliases));
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(this.getServer().getPluginManager());
            commandMap.register(this.getDescription().getName(), pluginCommand);
        }catch (Exception e) { e.printStackTrace(); }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}