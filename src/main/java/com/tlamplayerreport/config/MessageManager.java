package com.tlamplayerreport.config;

import com.tlamplayerreport.ReportPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final ReportPlugin plugin;
    private FileConfiguration messagesConfig;
    
    public MessageManager(ReportPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        String language = plugin.getConfig().getString("settings.language", "en");
        File langFolder = new File(plugin.getDataFolder(), "languages/" + language);
        File messagesFile = new File(langFolder, "messages.yml");
        
        if (!messagesFile.exists()) {
            langFolder.mkdirs();
            try {
                InputStream inputStream = plugin.getResource("languages/" + language + "/messages.yml");
                if (inputStream != null) {
                    Files.copy(inputStream, messagesFile.toPath());
                } else {
                    plugin.getLogger().warning("Language '" + language + "' not found, using English as default.");
                    langFolder = new File(plugin.getDataFolder(), "languages/en");
                    messagesFile = new File(langFolder, "messages.yml");
                    langFolder.mkdirs();
                    inputStream = plugin.getResource("languages/en/messages.yml");
                    if (inputStream != null) {
                        Files.copy(inputStream, messagesFile.toPath());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load messages file: " + e.getMessage());
            }
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public String getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messagesConfig.getString(path, "&cMessage not found: " + path);
        
        String prefix = messagesConfig.getString("prefix", "");
        message = message.replace("{prefix}", prefix);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }
    
    public void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(path, placeholders));
    }
}