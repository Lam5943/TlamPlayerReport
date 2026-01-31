package com.tlamplayerreport.inventory.impl;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.ReportType;
import com.tlamplayerreport.inventory.InventoryButton;
import com.tlamplayerreport.inventory.InventoryGUI;
import com.tlamplayerreport.util.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;

public class MainReportGUI extends InventoryGUI {
    
    private final ReportPlugin plugin;
    
    public MainReportGUI(ReportPlugin plugin) {
        super(
            org.bukkit.ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("settings.menu-title-prefix", "&6&lTLamPlayerReport&r - ")
            + plugin.getConfigManager().getGuiConfig().getString("main-menu.title", "&6&lReport Menu")
            ),
            plugin.getConfigManager().getGuiConfig().getInt("main-menu.size", 27)
        );
        this.plugin = plugin;
        refresh();
    }
    
    @Override
    public void refresh() {
        buttons.clear();
        inventory.clear();
        
        ConfigurationSection itemsSection = plugin.getConfigManager().getGuiConfig().getConfigurationSection("main-menu.items");
        
        if (itemsSection != null) {
            if (itemsSection.contains("player-report")) {
                int slot = itemsSection.getInt("player-report.slot");
                String material = itemsSection.getString("player-report.material");
                String name = itemsSection.getString("player-report.name");
                java.util.List<String> lore = itemsSection.getStringList("player-report.lore");
                
                InventoryButton playerReportButton = new InventoryButton(slot,
                        new ItemBuilder(material).name(name).lore(lore).build(),
                        (player, event) -> {
                            PlayerSelectionGUI playerSelectionGUI = new PlayerSelectionGUI(plugin);
                            plugin.getGuiManager().openGUI(playerSelectionGUI, player);
                        }
                );
                addButton(playerReportButton);
            }
            
            if (itemsSection.contains("bug-report")) {
                int slot = itemsSection.getInt("bug-report.slot");
                String material = itemsSection.getString("bug-report.material");
                String name = itemsSection.getString("bug-report.name");
                java.util.List<String> lore = itemsSection.getStringList("bug-report.lore");
                
                InventoryButton bugReportButton = new InventoryButton(slot,
                        new ItemBuilder(material).name(name).lore(lore).build(),
                        (player, event) -> {
                            CategorySelectionGUI categoryGUI = new CategorySelectionGUI(plugin, ReportType.BUG, null);
                            plugin.getGuiManager().openGUI(categoryGUI, player);
                        }
                );
                addButton(bugReportButton);
            }
        }
    }
}
