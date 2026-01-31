package com.tlamplayerreport.inventory.impl;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;
import com.tlamplayerreport.data.ReportStatus;
import com.tlamplayerreport.inventory.InventoryButton;
import com.tlamplayerreport.inventory.InventoryGUI;
import com.tlamplayerreport.util.ItemBuilder;

public class AdminReportStatusGUI extends InventoryGUI {

    private final Report report;
    private final ReportPlugin plugin;

    public AdminReportStatusGUI(ReportPlugin plugin, Report report) {
        // Prefix menu name with plugin name
        super(plugin.getName() + " - Set Report Status", 27);
        this.plugin = plugin;
        this.report = report;
        refresh();
    }

    @Override
    public void refresh() {
        buttons.clear();
        inventory.clear();

        int slot = 10;
        for (ReportStatus status : ReportStatus.values()) {
            InventoryButton button = new InventoryButton(
                    slot,
                    new ItemBuilder("PAPER")
                            .name("&eMark as " + status.name())
                            .addLoreLine("&7Click to mark report as " + status.name())
                            .build(),
                    (player, event) -> {
                        report.setStatus(status);
                        player.sendMessage("&aReport marked as: " + status.name());
                        player.closeInventory();
                    });
            addButton(button);
            slot += 2;
        }
    }
}
