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

        try {
            boolean needsExtract = !messagesFile.exists() || messagesFile.length() == 0;
            String resourcePath = "languages/" + language + "/messages.yml";
            if (needsExtract) {
                langFolder.mkdirs();
                plugin.getLogger().info("Trying to extract language resource: " + resourcePath);
                try (InputStream inputStream = plugin.getResource(resourcePath)) {
                    if (inputStream != null) {
                        java.nio.file.Files.copy(
                            inputStream,
                            messagesFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                        plugin.getLogger().info("Extracted: " + resourcePath);
                    } else {
                        plugin.getLogger().warning("Not found in JAR: " + resourcePath + ". Falling back to English.");
                        // Fallback to English
                        File enFolder = new File(plugin.getDataFolder(), "languages/en");
                        File enFile = new File(enFolder, "messages.yml");
                        enFolder.mkdirs();
                        try (InputStream enStream = plugin.getResource("languages/en/messages.yml")) {
                            if (enStream != null) {
                                java.nio.file.Files.copy(
                                    enStream,
                                    enFile.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                );
                                plugin.getLogger().info("Fallback: Extracted languages/en/messages.yml.");
                            } else {
                                plugin.getLogger().severe("ENGLISH language file missing from jar! Your plugin may not function as expected.");
                            }
                        }
                        // Load fallback English messages
                        messagesFile = enFile;
                    }
                }
            }
            messagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(messagesFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load or extract messages file: " + e.getMessage());
        }
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
    
    public void reload() {
        loadMessages();
    }
}
