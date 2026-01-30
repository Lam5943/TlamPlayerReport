package com.tlamplayerreport.manager;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;
import com.tlamplayerreport.data.ReportStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReportManager {
    
    private final ReportPlugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> dailyReportCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastResetTime = new ConcurrentHashMap<>();
    
    public ReportManager(ReportPlugin plugin) {
        this.plugin = plugin;
        startDailyReset();
    }
    
    public void submitReport(Report report, Player reporter) {
        if (hasCooldown(reporter)) {
            long remainingTime = getRemainingCooldown(reporter);
            Map<String, String> placeholders = new HashMap<>();
            
            long hours = remainingTime / 3600;
            long minutes = (remainingTime % 3600) / 60;
            long seconds = remainingTime % 60;
            
            String timeFormat;
            if (hours > 0) {
                timeFormat = hours + "h " + minutes + "m";
            } else if (minutes > 0) {
                timeFormat = minutes + "m " + seconds + "s";
            } else {
                timeFormat = seconds + "s";
            }
            
            placeholders.put("time", timeFormat);
            plugin.getMessageManager().sendMessage(reporter, "commands.cooldown", placeholders);
            return;
        }
        
        if (hasReachedMaxReports(reporter)) {
            plugin.getMessageManager().sendMessage(reporter, "commands.max-reports");
            return;
        }
        
        if (hasReachedDailyLimit(reporter, report.getType().name())) {
            reporter.sendMessage(plugin.getConfigManager().getGuiConfig().getString("prefix") + 
                    " §cYou have reached the daily limit for " + report.getType().name().toLowerCase() + " reports.");
            return;
        }
        
        if (plugin.getConfig().getBoolean("security.prevent-duplicate-submissions", true)) {
            if (isDuplicateReport(reporter, report)) {
                reporter.sendMessage(plugin.getConfigManager().getGuiConfig().getString("prefix") + 
                        " §cYou have already submitted a similar report recently.");
                return;
            }
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = plugin.getDatabaseManager().createReport(report);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    plugin.getMessageManager().sendMessage(reporter, "report.success");
                    setCooldown(reporter);
                    incrementDailyCount(reporter);
                    
                    if (plugin.getConfig().getBoolean("google-sheets.enabled") && 
                        plugin.getConfig().getBoolean("google-sheets.log-on-submit", true)) {
                        plugin.getGoogleSheetsHandler().logReport(report);
                    }
                    
                    if (plugin.getConfig().getBoolean("discord.enabled", false)) {
                        plugin.getDiscordWebhook().sendReportNotification(report);
                    }
                    
                    notifyAdmins(report);
                } else {
                    plugin.getMessageManager().sendMessage(reporter, "report.failed");
                }
            });
        });
    }
    
    public List<Report> getAllReports() {
        return plugin.getDatabaseManager().getAllReports();
    }
    
    public List<Report> getReportsByStatus(ReportStatus status) {
        return plugin.getDatabaseManager().getReportsByStatus(status);
    }
    
    public List<Report> getReportsByPlayer(UUID playerUuid) {
        return plugin.getDatabaseManager().getReportsByPlayer(playerUuid);
    }
    
    public void deleteReport(int reportId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().deleteReport(reportId);
        });
    }
    
    public void clearAllReports() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().clearReports();
        });
    }
    
    private boolean hasCooldown(Player player) {
        if (player.hasPermission("tlamplayerreport.bypass.cooldown")) {
            return false;
        }
        
        Long lastReport = cooldowns.get(player.getUniqueId());
        if (lastReport == null) return false;
        
        int cooldownSeconds = plugin.getConfig().getInt("settings.cooldown-seconds", 7200);
        long cooldownMillis = cooldownSeconds * 1000L;
        
        return System.currentTimeMillis() - lastReport < cooldownMillis;
    }
    
    private long getRemainingCooldown(Player player) {
        Long lastReport = cooldowns.get(player.getUniqueId());
        if (lastReport == null) return 0;
        
        int cooldownSeconds = plugin.getConfig().getInt("settings.cooldown-seconds", 7200);
        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsed = System.currentTimeMillis() - lastReport;
        
        return (cooldownMillis - elapsed) / 1000;
    }
    
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    private boolean hasReachedMaxReports(Player player) {
        if (player.hasPermission("tlamplayerreport.bypass.limit")) {
            return false;
        }
        
        int maxReports = plugin.getConfig().getInt("settings.max-reports-per-player", 10);
        List<Report> playerReports = plugin.getDatabaseManager().getPendingReportsByPlayer(player.getUniqueId());
        
        return playerReports.size() >= maxReports;
    }
    
    private boolean hasReachedDailyLimit(Player player, String reportType) {
        if (player.hasPermission("tlamplayerreport.bypass.limit")) {
            return false;
        }
        
        resetDailyCountIfNeeded(player);
        
        int count = dailyReportCounts.getOrDefault(player.getUniqueId(), 0);
        
        int limit;
        if (reportType.equals("PLAYER")) {
            limit = plugin.getConfig().getInt("settings.report-limits.player-reports-per-day", 5);
        } else {
            limit = plugin.getConfig().getInt("settings.report-limits.bug-reports-per-day", 3);
        }
        
        return count >= limit;
    }
    
    private void incrementDailyCount(Player player) {
        UUID uuid = player.getUniqueId();
        dailyReportCounts.put(uuid, dailyReportCounts.getOrDefault(uuid, 0) + 1);
        lastResetTime.put(uuid, System.currentTimeMillis());
    }
    
    private void resetDailyCountIfNeeded(Player player) {
        UUID uuid = player.getUniqueId();
        Long lastReset = lastResetTime.get(uuid);
        
        if (lastReset == null) {
            lastResetTime.put(uuid, System.currentTimeMillis());
            return;
        }
        
        long dayInMillis = 24 * 60 * 60 * 1000L;
        if (System.currentTimeMillis() - lastReset > dayInMillis) {
            dailyReportCounts.put(uuid, 0);
            lastResetTime.put(uuid, System.currentTimeMillis());
        }
    }
    
    private void startDailyReset() {
        long dayInTicks = 24 * 60 * 60 * 20L;
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            dailyReportCounts.clear();
            lastResetTime.clear();
        }, dayInTicks, dayInTicks);
    }
    
    private boolean isDuplicateReport(Player player, Report report) {
        int checkWindow = plugin.getConfig().getInt("security.duplicate-check-window", 300);
        long windowMillis = checkWindow * 1000L;
        long cutoffTime = System.currentTimeMillis() - windowMillis;
        
        List<Report> recentReports = plugin.getDatabaseManager().getReportsByPlayer(player.getUniqueId());
        
        for (Report existing : recentReports) {
            if (existing.getTimestamp() < cutoffTime) {
                continue;
            }
            
            if (existing.getType() == report.getType() && 
                existing.getCategory().equals(report.getCategory())) {
                
                if (report.getTargetUuid() != null && existing.getTargetUuid() != null) {
                    if (existing.getTargetUuid().equals(report.getTargetUuid())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private void notifyAdmins(Report report) {
        if (!plugin.getConfig().getBoolean("settings.notify-admins", true)) {
            return;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", report.getReporterName());
        placeholders.put("type", report.getType().toString().toLowerCase());
        
        String message = plugin.getMessageManager().getMessage("admin.report-notification", placeholders);
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("tlamplayerreport.notify")) {
                online.sendMessage(message);
            }
        }
    }
}