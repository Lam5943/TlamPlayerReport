package com.tlamplayerreport.inventory.impl;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;
import com.tlamplayerreport.data.ReportStatus;
import com.tlamplayerreport.data.ReportType;
import com.tlamplayerreport.inventory.InventoryButton;
import com.tlamplayerreport.inventory.InventoryGUI;
import com.tlamplayerreport.util.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class CategorySelectionGUI extends InventoryGUI {
    
    private final ReportPlugin plugin;
    private final ReportType reportType;
    private final Player targetPlayer;
    
    public CategorySelectionGUI(ReportPlugin plugin, ReportType reportType, Player targetPlayer) {
        super(getCategoryTitle(plugin, reportType), 27);
        this.plugin = plugin;
        this.reportType = reportType;
        this.targetPlayer = targetPlayer;
        
        refresh();
    }
    
    private static String getCategoryTitle(ReportPlugin plugin, ReportType type) {
        String path = type == ReportType.PLAYER ? "categories.player.title" : "categories.bug.title";
        return plugin.getConfigManager().getGuiConfig().getString(path, "&6Select Category");
    }
    
    @Override
    public void refresh() {
        buttons.clear();
        inventory.clear();
        
        String categoryPath = reportType == ReportType.PLAYER ? "categories.player.categories" : "categories.bug.categories";
        ConfigurationSection categorySection = plugin.getConfigManager().getGuiConfig().getConfigurationSection(categoryPath);
        
        if (categorySection != null) {
            for (String categoryKey : categorySection.getKeys(false)) {
                String basePath = categoryPath + "." + categoryKey;
                
                int slot = plugin.getConfigManager().getGuiConfig().getInt(basePath + ".slot");
                String material = plugin.getConfigManager().getGuiConfig().getString(basePath + ".material");
                String name = plugin.getConfigManager().getGuiConfig().getString(basePath + ".name");
                List<String> lore = plugin.getConfigManager().getGuiConfig().getStringList(basePath + ".lore");
                
                ItemBuilder itemBuilder = new ItemBuilder(material).name(name).lore(lore);
                
                InventoryButton button = new InventoryButton(slot, itemBuilder.build(), (player, event) -> {
                    Report report = Report.builder()
                            .reporterUuid(player.getUniqueId())
                            .reporterName(player.getName())
                            .type(reportType)
                            .category(categoryKey)
                            .targetUuid(targetPlayer != null ? targetPlayer.getUniqueId() : null)
                            .targetName(targetPlayer != null ? targetPlayer.getName() : null)
                            .status(ReportStatus.PENDING)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    
                    plugin.getReportManager().submitReport(report, player);
                    
                    if (plugin.getConfig().getBoolean("settings.close-gui-on-report", true)) {
                        player.closeInventory();
                    }
                });
                
                addButton(button);
            }
        }
    }
}