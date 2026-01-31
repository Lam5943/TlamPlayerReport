package com.tlamplayerreport.inventory.impl;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;
import com.tlamplayerreport.inventory.InventoryButton;
import com.tlamplayerreport.inventory.InventoryGUI;
import com.tlamplayerreport.util.ItemBuilder;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminReportViewGUI extends InventoryGUI {
    
    private final ReportPlugin plugin;
    private final List<Report> reports;
    private final int page;
    private static final int REPORTS_PER_PAGE = 45;
    
    public AdminReportViewGUI(ReportPlugin plugin, List<Report> reports, int page) {
        super(plugin.getConfigManager().getGuiConfig().getString("admin-view.title", "&4&lReports").replace("{page}", String.valueOf(page + 1)), 54);
        this.plugin = plugin;
        this.reports = reports;
        this.page = page;
        
        refresh();
    }
    
    @Override
    public void refresh() {
        buttons.clear();
        inventory.clear();
        
        int startIndex = page * REPORTS_PER_PAGE;
        int endIndex = Math.min(startIndex + REPORTS_PER_PAGE, reports.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Report report = reports.get(i);
            int slot = i - startIndex;
            
            String material = report.getType().name().equals("PLAYER") ? "PLAYER_HEAD" : "ANVIL";
            
            List<String> lore = new ArrayList<>();
            lore.add("&7ID: &e#" + report.getId());
            lore.add("&7Reporter: &e" + report.getReporterName());
            lore.add("&7Type: &e" + report.getType());
            lore.add("&7Category: &e" + report.getCategory());
            
            if (report.getTargetName() != null) {
                lore.add("&7Target: &e" + report.getTargetName());
            }
            
            if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                lore.add("&7Description: &f" + report.getDescription());
            }
            
            lore.add("&7Status: " + getStatusColor(report.getStatus()) + report.getStatus());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            lore.add("&7Date: &e" + dateFormat.format(new Date(report.getTimestamp())));
            
            lore.add("");
            lore.add("&eClick for more options");
            
            ItemBuilder itemBuilder = new ItemBuilder(material);
            itemBuilder.name("&6Report #" + report.getId());
            itemBuilder.lore(lore);
            
            if (material.equals("PLAYER_HEAD") && report.getTargetName() != null) {
                itemBuilder.skull(report.getTargetName());
            }
            
            InventoryButton button = new InventoryButton(slot, itemBuilder.build(), (player, event) -> {
                openReportDetailsMenu(player, report);
            });
            
            addButton(button);
        }
        
        if (page > 0) {
            String prevMaterial = plugin.getConfigManager().getGuiConfig().getString("admin-view.items.previous-page.material", "ARROW");
            String prevName = plugin.getConfigManager().getGuiConfig().getString("admin-view.items.previous-page.name", "&ePrevious Page");
            int prevSlot = plugin.getConfigManager().getGuiConfig().getInt("admin-view.items.previous-page.slot", 45);
            
            InventoryButton prevButton = new InventoryButton(prevSlot, 
                    new ItemBuilder(prevMaterial).name(prevName).build(),
                    (player, event) -> {
                        AdminReportViewGUI prevGUI = new AdminReportViewGUI(plugin, reports, page - 1);
                        plugin.getGuiManager().openGUI(prevGUI, player);
                    }
            );
            addButton(prevButton);
        }
        
        if (endIndex < reports.size()) {
            String nextMaterial = plugin.getConfigManager().getGuiConfig().getString("admin-view.items.next-page.material", "ARROW");
            String nextName = plugin.getConfigManager().getGuiConfig().getString("admin-view.items.next-page.name", "&eNext Page");
            int nextSlot = plugin.getConfigManager().getGuiConfig().getInt("admin-view.items.next-page.slot", 53);
            
            InventoryButton nextButton = new InventoryButton(nextSlot,
                    new ItemBuilder(nextMaterial).name(nextName).build(),
                    (player, event) -> {
                        AdminReportViewGUI nextGUI = new AdminReportViewGUI(plugin, reports, page + 1);
                        plugin.getGuiManager().openGUI(nextGUI, player);
                    }
            );
            addButton(nextButton);
        }
    }
    
    private void openReportDetailsMenu(Player player, Report report) {
        player.closeInventory();
        plugin.getGuiManager().openGUI(new AdminReportStatusGUI(plugin, report), player);
    }
        
        if (report.getTargetName() != null) {
            player.sendMessage("§7Target: §e" + report.getTargetName());
        }
        
        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            player.sendMessage("§7Description: §f" + report.getDescription());
        }
        
        player.sendMessage("§7Status: " + getStatusColor(report.getStatus()) + report.getStatus());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        player.sendMessage("§7Date: §e" + dateFormat.format(new Date(report.getTimestamp())));
        
        if (report.getReviewerName() != null) {
            player.sendMessage("§7Reviewed by: §e" + report.getReviewerName());
        }
        
        if (report.getNotes() != null && !report.getNotes().isEmpty()) {
            player.sendMessage("§7Notes: §f" + report.getNotes());
        }
    }
    
    private String getStatusColor(com.tlamplayerreport.data.ReportStatus status) {
        switch (status) {
            case PENDING:
                return "&e";
            case REVIEWED:
                return "&b";
            case RESOLVED:
                return "&a";
            case DISMISSED:
                return "&c";
            default:
                return "&7";
        }
    }
}
