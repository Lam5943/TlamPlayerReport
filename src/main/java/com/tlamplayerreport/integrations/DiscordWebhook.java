package com.tlamplayerreport.integrations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscordWebhook {
    
    private final ReportPlugin plugin;
    
    public DiscordWebhook(ReportPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void sendReportNotification(Report report) {
        if (!plugin.getConfig().getBoolean("discord.enabled", false)) {
            return;
        }
        
        if (!plugin.getConfig().getBoolean("discord.notifications.new-report", true)) {
            return;
        }
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String webhookUrl = plugin.getConfig().getString("discord.webhook-url");
                
                if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK_URL")) {
                    return;
                }
                
                JsonObject embedObject = new JsonObject();
                
                int color = plugin.getConfig().getInt("discord.embed.color", 15844367);
                embedObject.addProperty("color", color);
                
                embedObject.addProperty("title", "New Report Submitted");
                
                StringBuilder description = new StringBuilder();
                description.append("**Report ID:** #").append(report.getId()).append("\n");
                description.append("**Type:** ").append(report.getType()).append("\n");
                description.append("**Category:** ").append(report.getCategory()).append("\n");
                description.append("**Reporter:** ").append(report.getReporterName()).append("\n");
                
                if (report.getTargetName() != null) {
                    description.append("**Target:** ").append(report.getTargetName()).append("\n");
                }
                
                if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                    description.append("**Description:** ").append(report.getDescription()).append("\n");
                }
                
                description.append("**Status:** ").append(report.getStatus()).append("\n");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date(report.getTimestamp()));
                description.append("**Time:** ").append(timestamp);
                
                embedObject.addProperty("description", description.toString());
                
                JsonObject footer = new JsonObject();
                String footerText = plugin.getConfig().getString("discord.embed.footer-text", "TLamPlayerReport");
                footer.addProperty("text", footerText);
                embedObject.add("footer", footer);
                
                embedObject.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));
                
                JsonArray embedsArray = new JsonArray();
                embedsArray.add(embedObject);
                
                JsonObject payload = new JsonObject();
                payload.add("embeds", embedsArray);
                
                if (plugin.getConfig().getBoolean("discord.mentions.enabled", false)) {
                    StringBuilder content = new StringBuilder();
                    
                    String roleId = plugin.getConfig().getString("discord.mentions.role-id");
                    if (roleId != null && !roleId.isEmpty() && !roleId.equals("YOUR_ROLE_ID")) {
                        content.append("<@&").append(roleId).append("> ");
                    }
                    
                    if (content.length() > 0) {
                        payload.addProperty("content", content.toString());
                    }
                }
                
                sendWebhook(webhookUrl, payload.toString());
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }
    
    private void sendWebhook(String webhookUrl, String payload) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "TLamPlayerReport/1.0");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 204 || responseCode == 200) {
                plugin.getLogger().info("Successfully sent Discord webhook notification");
            } else {
                plugin.getLogger().warning("Discord webhook returned code: " + responseCode);
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
        }
    }
}