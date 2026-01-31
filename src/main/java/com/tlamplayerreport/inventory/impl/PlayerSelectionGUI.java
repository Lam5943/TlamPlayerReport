package com.tlamplayerreport.inventory.impl;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.ReportType;
import com.tlamplayerreport.inventory.InventoryButton;
import com.tlamplayerreport.inventory.InventoryGUI;
import com.tlamplayerreport.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelectionGUI extends InventoryGUI {
    
    private final ReportPlugin plugin;
    
    public PlayerSelectionGUI(ReportPlugin plugin) {
        super(
            org.bukkit.ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("settings.menu-title-prefix", "&6&lTLamPlayerReport&r - ")
                + plugin.getConfigManager().getGuiConfig().getString("player-selection.title", "&6Select Player")
            ),
            plugin.getConfigManager().getGuiConfig().getInt("player-selection.size", 54)
        );
        this.plugin = plugin;
        refresh();
    }
    
    @Override
    public void refresh() {
        buttons.clear();
        inventory.clear();
        
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        if (onlinePlayers.isEmpty()) {
            ItemBuilder noPlayersItem = new ItemBuilder("BARRIER")
                    .name("&cNo online players")
                    .addLoreLine("&7There are no players online to report");
            inventory.setItem(22, noPlayersItem.build());
            return;
        }
        
        int slot = 0;
        for (Player target : onlinePlayers) {
            if (slot >= 54) break;
            
            ItemBuilder playerHead = new ItemBuilder("PLAYER_HEAD");
            playerHead.skull(target.getName());
            playerHead.name("&e" + target.getName());
            playerHead.addLoreLine("&7Click to report this player");
            
            InventoryButton button = new InventoryButton(slot, playerHead.build(), (player, event) -> {
                if (target.equals(player)) {
                    plugin.getMessageManager().sendMessage(player, "report.cannot-report-self");
                    player.closeInventory();
                    return;
                }
                
                CategorySelectionGUI categoryGUI = new CategorySelectionGUI(plugin, ReportType.PLAYER, target);
                plugin.getGuiManager().openGUI(categoryGUI, player);
            });
            
            addButton(button);
            slot++;
        }
    }
}
