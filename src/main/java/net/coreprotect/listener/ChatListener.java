package net.coreprotect.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ChatListener implements Listener {
    private Set<String> trackerPlayers;
    private FileConfiguration config;
    private final JavaPlugin plugin;

    public ChatListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = loadConfig();
        this.trackerPlayers = new HashSet<>(config.getStringList("TrackerPlayers"));
    }

    private FileConfiguration loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (trackerPlayers.contains(player.getName())) {
            sendToTelegram(player.getName() + ": " + message);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (trackerPlayers.contains(player.getName())) {
            sendToTelegram(player.getName() + " joined the game.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (trackerPlayers.contains(player.getName())) {
            sendToTelegram(player.getName() + " left the game.");
        }
    }

    private void sendToTelegram(String message) {
    }

    public void addTrackerPlayer(String playerName) {
        trackerPlayers.add(playerName);
        config.set("TrackerPlayers", new HashSet<>(trackerPlayers));
        saveConfig();
    }

    public void removeTrackerPlayer(String playerName) {
        trackerPlayers.remove(playerName);
        config.set("TrackerPlayers", new HashSet<>(trackerPlayers));
        saveConfig();
    }

    // Добавление и удаление игрока списка
    public void trackPlayer(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /track <Nickname>");
            return;
        }

        String playerName = args[0];
        addTrackerPlayer(playerName);
        sender.sendMessage("Player " + playerName + " is now being tracked.");
    }

    public void untrackPlayer(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /untrack <Nickname>");
            return;
        }

        String playerName = args[0];
        removeTrackerPlayer(playerName);
        sender.sendMessage("Player " + playerName + " is no longer being tracked.");
    }

    // TODO: Реализовать механику трекера на игрока
}
