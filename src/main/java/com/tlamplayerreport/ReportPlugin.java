package com.tlamplayerreport;

import com.tlamplayerreport.commands.ReportAdminCommand;
import com.tlamplayerreport.commands.ReportCommand;
import com.tlamplayerreport.config.ConfigManager;
import com.tlamplayerreport.config.MessageManager;
import com.tlamplayerreport.database.DatabaseManager;
import com.tlamplayerreport.integrations.DiscordWebhook;
import com.tlamplayerreport.integrations.GoogleSheetsHandler;
import com.tlamplayerreport.inventory.gui.GUIListener;
import com.tlamplayerreport.inventory.gui.GUIManager;
import com.tlamplayerreport.manager.ReportManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ReportPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private GoogleSheetsHandler googleSheetsHandler;
    private DiscordWebhook discordWebhook;
    private GUIManager guiManager;
    private ReportManager reportManager;
    private ReportChatInputManager reportChatInputManager; // <-- ADD THIS


    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        new MessageManager(this).forceExtractAllLanguages();
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.messageManager.forceExtractAllLanguages();
        this.reportChatInputManager = new ReportChatInputManager(this);
        
        this.databaseManager = new DatabaseManager(this);
        if (!this.databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        this.googleSheetsHandler = new GoogleSheetsHandler(this);
        if (getConfig().getBoolean("google-sheets.enabled")) {
            this.googleSheetsHandler.initialize();
        }
        
        this.discordWebhook = new DiscordWebhook(this);
        
        this.guiManager = new GUIManager();
        this.reportManager = new ReportManager(this);
        
        registerCommands();
        registerEvents();
        
        if (getConfig().getBoolean("database.auto-cleanup.enabled", true)) {
            startAutoCleanup();
        }
        
        getLogger().info("========================================");
        getLogger().info("TLamPlayerReport has been enabled!");
        getLogger().info("Author: TranLam (Midnight)");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Language: " + getConfig().getString("settings.language", "en"));
        getLogger().info("Cooldown: " + (getConfig().getInt("settings.cooldown-seconds", 7200) / 3600) + " hours");
        getLogger().info("========================================");
    }
    
    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.shutdown();
        }
        
        getLogger().info("TLamPlayerReport has been disabled!");
    }
    
    private void registerCommands() {
        String customCommand = getConfig().getString("settings.custom-command", "report");
        PluginCommand reportCmd = getCommand(customCommand);
        if (reportCmd != null) {
            reportCmd.setExecutor(new ReportCommand(this));
        } else {
            getCommand("report").setExecutor(new ReportCommand(this));
        }
        
        getCommand("tlamplayerreportadmin").setExecutor(new ReportAdminCommand(this));
    }
    
    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
    }
    
    private void startAutoCleanup() {
        int intervalSeconds = getConfig().getInt("database.auto-cleanup.run-interval", 86400);
        long intervalTicks = intervalSeconds * 20L;
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (!getConfig().getBoolean("database.auto-cleanup.enabled", true)) {
                return;
            }
            
            int days = getConfig().getInt("database.auto-cleanup.days", 30);
            long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
            
            databaseManager.cleanupOldReports(cutoffTime);
            
            getLogger().info("Auto-cleanup completed: removed reports older than " + days + " days");
        }, intervalTicks, intervalTicks);
    }
}
