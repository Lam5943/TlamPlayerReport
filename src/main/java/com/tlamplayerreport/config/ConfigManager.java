package com.tlamplayerreport.config;

import com.tlamplayerreport.ReportPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    
    private final ReportPlugin plugin;
    private FileConfiguration guiConfig;
    private File guiFile;
    
    public ConfigManager(ReportPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    private void loadConfigs() {
        plugin.saveDefaultConfig();
        
        // Load INVENTORY_GUI.yml as specified in requirements
        guiFile = new File(plugin.getDataFolder(), "INVENTORY_GUI.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("INVENTORY_GUI.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }
    
    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }
    
    public void reload() {
        reloadConfigs();
    }
    
    public void saveGuiConfig() {
        try {
            guiConfig.save(guiFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save INVENTORY_GUI.yml: " + e.getMessage());
        }
    }
}