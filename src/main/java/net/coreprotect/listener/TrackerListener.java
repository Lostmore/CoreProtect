package net.coreprotect.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrackerListener extends TelegramLongPollingBot implements Listener {
    private final String botToken;
    private String chatId;
    private final Set<String> trackedPlayers = new HashSet<>();
    private final String tracklistFilePath = "plugins/CoreProtect/tracklist.yml";

    public TrackerListener(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        loadTrackedPlayers();
    }

    private void loadTrackedPlayers() {
        Yaml yaml = new Yaml();
        File tracklistFile = new File(tracklistFilePath);
        if (!tracklistFile.exists()) {
            try {
                tracklistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Map<String, Set<String>> data = yaml.load(new FileReader(tracklistFile));
            if (data != null && data.containsKey("trackedPlayers")) {
                trackedPlayers.addAll(data.get("trackedPlayers"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void initializeTrackedPlayers() {
        loadTrackedPlayers();
    }

    private void saveTrackedPlayers() {
        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(tracklistFilePath)) {
            Map<String, Set<String>> data = new java.util.HashMap<>();
            data.put("trackedPlayers", trackedPlayers);
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return "CivCraft Dev";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (trackedPlayers.contains(playerName)) {
            sendNotification(playerName + " присоединился к игре.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (trackedPlayers.contains(playerName)) {
            sendNotification(playerName + " покинул игру.");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (trackedPlayers.contains(playerName)) {
            sendNotification(playerName + " написал в чат: " + event.getMessage());
        }
    }

    private void sendNotification(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}