package me.panhaskins.cmdlimit.utils;

import me.panhaskins.cmdlimit.CMDLimiter;
import me.panhaskins.cmdlimit.api.APIConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class DataManager {

    private HashMap<String, HashMap<String, Integer>> dataMap = new HashMap<>();

    public void load(){

        APIConfig data = new APIConfig(JavaPlugin.getPlugin(CMDLimiter.class), "data.yml");
        dataMap.clear();

        for (String command : CMDLimiter.commandList) {

            dataMap.put(command, new HashMap<>());

            ConfigurationSection playerList = data.get().getConfigurationSection("Data." + command);
            if (playerList != null) playerList.getKeys(false).forEach(player ->
                    dataMap.get(command).put(player, playerList.getInt(player)));

        }

    }

    public void save(){

        APIConfig data = new APIConfig(JavaPlugin.getPlugin(CMDLimiter.class), "data.yml");
        for (String command : CMDLimiter.commandList) {
            for (String player : dataMap.get(command).keySet()) {
                data.get().set("Data." + command + "." + player, dataMap.get(command).get(player));
            }
        }
        data.save();
    }

    public ArrayList getPlayerNames(String cmdName) {
        ArrayList<String> players = new ArrayList<>();
        for (String player : dataMap.get(cmdName).keySet()) {
            players.add(player);
        }
        return players;

    }

    public int getPlayer(Player player, String cmdName) {
        return dataMap.get(cmdName).getOrDefault(player.getName(), 0);
    }

    public HashMap<String, Integer> getPlayer(Player player) {
        HashMap<String, Integer> playerData = new HashMap<>();
        CMDLimiter.commandList.forEach(command -> playerData.put(command, dataMap.get(command).get(player.getName())));

        return playerData;
    }

    public void setPlayer(String playerName, String cmdName, int value) {
        dataMap.computeIfAbsent(cmdName, k -> new HashMap<>());
        dataMap.get(cmdName).put(playerName, value);
    }

    public void removePlayer(String playerName, String cmdName) {
        dataMap.get(cmdName).remove(playerName);
    }
}
