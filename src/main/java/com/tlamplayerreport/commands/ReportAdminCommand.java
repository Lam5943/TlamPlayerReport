package com.tlamplayerreport.commands;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;
import com.tlamplayerreport.data.ReportStatus;
import com.tlamplayerreport.inventory.impl.AdminReportViewGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportAdminCommand implements CommandExecutor {
    
    private final ReportPlugin plugin;
    
    public ReportAdminCommand(ReportPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tlamplayerreport.admin")) {
            plugin.getMessageManager().sendMessage(sender, "commands.no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                handleList(sender, args);
                break;
            case "view":
                handleView(sender);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "clear":
                handleClear(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleList(CommandSender sender, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Report> reports;
            
            if (args.length > 1) {
                String filter = args[1].toUpperCase();
                try {
                    ReportStatus status = ReportStatus.valueOf(filter);
                    reports = plugin.getReportManager().getReportsByStatus(status);
                } catch (IllegalArgumentException e) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.getConfigManager().getGuiConfig().getString("prefix") + 
                                " §cInvalid status. Use: PENDING, REVIEWED, RESOLVED, DISMISSED");
                    });
                    return;
                }
            } else {
                reports = plugin.getReportManager().getAllReports();
            }
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (reports.isEmpty()) {
                    plugin.getMessageManager().sendMessage(sender, "admin.no-reports");
                    return;
                }
                
                sender.sendMessage("§6§l=== Reports ===");
                for (Report report : reports) {
                    sender.sendMessage("§e#" + report.getId() + " §7- §e" + report.getType() + 
                            " §7by §e" + report.getReporterName() + " §7- §e" + report.getStatus());
                }
                sender.sendMessage("§6Total: §e" + reports.size() + " §6reports");
            });
        });
    }
    
    private void handleView(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "commands.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("tlamplayerreport.admin.view")) {
            plugin.getMessageManager().sendMessage(player, "commands.no-permission");
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Report> reports = plugin.getReportManager().getAllReports();
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (reports.isEmpty()) {
                    plugin.getMessageManager().sendMessage(player, "admin.no-reports");
                    return;
                }
                
                AdminReportViewGUI adminGUI = new AdminReportViewGUI(plugin, reports, 0);
                plugin.getGuiManager().openGUI(adminGUI, player);
            });
        });
    }
    
    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tlamplayerreport.admin.delete")) {
            plugin.getMessageManager().sendMessage(sender, "commands.no-permission");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getGuiConfig().getString("prefix") + 
                    " §cUsage: /tlamplayerreportadmin delete <id>");
            return;
        }
        
        try {
            int reportId = Integer.parseInt(args[1]);
            plugin.getReportManager().deleteReport(reportId);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", String.valueOf(reportId));
            plugin.getMessageManager().sendMessage(sender, "admin.report-deleted", placeholders);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getGuiConfig().getString("prefix") + 
                    " §cInvalid report ID.");
        }
    }
    
    private void handleClear(CommandSender sender) {
        if (!sender.hasPermission("tlamplayerreport.admin.clear")) {
            plugin.getMessageManager().sendMessage(sender, "commands.no-permission");
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Report> reports = plugin.getReportManager().getAllReports();
            int count = reports.size();
            
            plugin.getReportManager().clearAllReports();
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("count", String.valueOf(count));
                plugin.getMessageManager().sendMessage(sender, "admin.reports-cleared", placeholders);
            });
        });
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== TLamPlayerReport Admin Commands ===");
        sender.sendMessage("§e/tlamplayerreportadmin list [status] §7- List all reports");
        sender.sendMessage("§e/tlamplayerreportadmin view §7- Open GUI to view reports");
        sender.sendMessage("§e/tlamplayerreportadmin delete <id> §7- Delete a specific report");
        sender.sendMessage("§e/tlamplayerreportadmin clear §7- Clear all reports");
        sender.sendMessage("§e/tlamplayerreportadmin help §7- Show this help message");
    }
}