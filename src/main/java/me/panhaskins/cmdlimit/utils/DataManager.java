package me.panhaskins.cmdlimit.utils;

import me.panhaskins.cmdlimit.CMDLimiter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final Map<String, Integer> globalData = new HashMap<>();
    private final Map<String, Map<String, Integer>> playerData = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void load() {
        ConfigManager data = new ConfigManager(JavaPlugin.getPlugin(CMDLimiter.class), "data.yml");
        playerData.clear();

        for (String command : CMDLimiter.commandList) {
            Map<String, Integer> playerData = new HashMap<>();
            ConfigurationSection playerList = data.getConfig("data.yml").getConfigurationSection("Data." + command);
            if (playerList != null) {
                for (String player : playerList.getKeys(false)) {
                    playerData.put(player, playerList.getInt(player));
                }
            }
            this.playerData.put(command, playerData);
        }
    }

    public void save() {
        ConfigManager data = new ConfigManager(JavaPlugin.getPlugin(CMDLimiter.class), "data.yml");
        for (Map.Entry<String, Map<String, Integer>> entry : playerData.entrySet()) {
            String command = entry.getKey();
            for (Map.Entry<String, Integer> playerEntry : entry.getValue().entrySet()) {
                data.getConfig("data.yml").set("Data." + command + "." + playerEntry.getKey(), playerEntry.getValue());
            }
        }
        data.save("data.yml");
    }

    public List<String> getPlayerNames(String cmdName) {
        return new ArrayList<>(playerData.getOrDefault(cmdName, Collections.emptyMap()).keySet());
    }

    public int getPlayer(String player, String cmdName) {
        return playerData.getOrDefault(cmdName, Collections.emptyMap()).getOrDefault(player, 0);
    }

    public Map<String, Integer> getPlayer(Player player) {
        Map<String, Integer> playerData = new HashMap<>();
        for (String command : CMDLimiter.commandList) {
            playerData.put(command, this.playerData.getOrDefault(command, Collections.emptyMap()).getOrDefault(player.getName(), 0));
        }
        return playerData;
    }

    public void setPlayer(String playerName, String cmdName, int value) {
        playerData.computeIfAbsent(cmdName, k -> new HashMap<>()).put(playerName, value);
    }

    public void removePlayer(String playerName, String cmdName) {
        Map<String, Integer> commandData = playerData.get(cmdName);
        if (commandData != null) {
            commandData.remove(playerName);
        }
    }

    public void setGlobal(String cmdName, int value) {
        globalData.put(cmdName, value);
    }

    public int getGlobal(String cmdName) {
        return globalData.getOrDefault(cmdName, 0);
    }

    public void setCooldown(Player player, String command, int cooldownInSeconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(command, System.currentTimeMillis() + cooldownInSeconds * 1000L);
    }

    public boolean isOnCooldown(Player player, String command) {
        return cooldowns.containsKey(player.getUniqueId()) &&
                cooldowns.get(player.getUniqueId()).getOrDefault(command, 0L) > System.currentTimeMillis();
    }

    public int getRemainingCooldown(Player player, String command) {
        if (isOnCooldown(player, command)) {
            long remainingTime = cooldowns.get(player.getUniqueId()).get(command) - System.currentTimeMillis();
            return (int) (remainingTime / 1000);
        }
        return 0;
    }

    public void startDataSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskTimerAsynchronously(JavaPlugin.getPlugin(CMDLimiter.class), 0L, 20 * 60 * 5); // Every 5 minutes
    }

    public void startCooldownCleanTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID player : cooldowns.keySet()) {
                    Map<String, Long> playerCooldowns = cooldowns.get(player);
                    if (playerCooldowns != null) {
                        playerCooldowns.values().removeIf(cooldown -> cooldown < System.currentTimeMillis());
                        if (playerCooldowns.isEmpty()) {
                            cooldowns.remove(player);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(JavaPlugin.getPlugin(CMDLimiter.class), 0L, 20L * 60L); // Every minute
    }
}