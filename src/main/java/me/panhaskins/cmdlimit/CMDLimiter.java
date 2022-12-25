package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.api.APIColor;
import me.panhaskins.cmdlimit.api.APICommand;
import me.panhaskins.cmdlimit.api.APIConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;


public final class CMDLimiter extends JavaPlugin implements Listener {

    public static APIConfig config, data;
    public static Commands commands;
    private static CMDLimiter instance;

    @Override
    public void onEnable() {

        // Plugin startup logic
        config = new APIConfig(this, getDataFolder() + File.separator + "config.yml" , "config.yml");
        data = new APIConfig(this, getDataFolder() + File.separator + "data.yml" , "data.yml");

        commands = new Commands(this);

        instance = this;
        setupSimpleCommandMap();

        Set<String> commandsList = CMDLimiter.config.get().getConfigurationSection("commands").getKeys(false);

        for (String cmdName : commandsList) {
            // registerCommands(cmdName); - error
        }

        this.getServer().getPluginManager().registerEvents(this, this);


        }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("clreload")) {
            if (sender.hasPermission("cl.reload")) {
                sender.sendMessage(APIColor.process(config.get().getString("Reload.reloading")));
                config.reload();
                data.reload();
                sender.sendMessage(APIColor.process(config.get().getString("Reload.complete")));
            }
            else
            {
                sender.sendMessage(APIColor.process(config.get().getString("Reload.noPermission")));
            }
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!data.get().contains("Data.free." + player.getName())) {
            for (String joinMessage : APIColor.process(config.get().getStringList("joinMessage"))) {
                player.sendMessage(joinMessage);
            }
        }
    }



    private static SimpleCommandMap sCommandMap;
    private SimplePluginManager sPluginManager;
    private void registerCommands(APICommand commands) {
        // Arrays.stream(commands).forEach(command -> sCommandMap.register("CMDLimiter", command)); - error
    }

    private void setupSimpleCommandMap() {
        sPluginManager = (SimplePluginManager) this.getServer().getPluginManager();
        Field field = null;
        try {
            field = SimplePluginManager.class.getDeclaredField("commandMap");
        } catch (Exception e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            sCommandMap = (SimpleCommandMap) field.get(sPluginManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static SimpleCommandMap getCommandMap() {
        return sCommandMap;
    }

    public static CMDLimiter getInstance() {
        return instance;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}