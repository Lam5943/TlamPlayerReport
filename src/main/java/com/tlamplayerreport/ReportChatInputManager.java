package com.tlamplayerreport;

import com.tlamplayerreport.data.ReportType;
import com.tlamplayerreport.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class ReportChatInputManager implements Listener {

    private static final int MIN_REASON_LENGTH = 5; // You may want to make this configurable!
    private final Map<UUID, PendingInput> waitingPlayers = new HashMap<>();
    private final ReportPlugin plugin;

    public ReportChatInputManager(ReportPlugin plugin) {
        this.plugin = plugin;
        // Register yourself as listener (can also be done in plugin)
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Call this when you want to prompt the user for a description
    public void promptForDescription(Player player, ReportType type, String category, Runnable onCancel, java.util.function.Consumer<String> onSubmit) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("category", category);
        placeholders.put("min", String.valueOf(MIN_REASON_LENGTH));
        String prompt = plugin.getMessageManager().getMessage("report.enter-description", placeholders);
        player.sendMessage(prompt);
        waitingPlayers.put(player.getUniqueId(), new PendingInput(type, category, onCancel, onSubmit));

        // Optional: Timeout task to auto-cancel after 30 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (waitingPlayers.containsKey(player.getUniqueId())) {
                player.sendMessage(plugin.getMessageManager().getMessage("report.cancelled", Collections.emptyMap()));
                if (onCancel != null) onCancel.run();
                waitingPlayers.remove(player.getUniqueId());
            }
        }, 20 * 30); // 30 sec timeout
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (waitingPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            PendingInput pending = waitingPlayers.get(uuid);
            String reason = event.getMessage();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("category", pending.category);
            placeholders.put("min", String.valueOf(MIN_REASON_LENGTH));

            if (reason.length() < MIN_REASON_LENGTH) {
                String tooShortMsg = plugin.getMessageManager().getMessage("report.description-too-short", placeholders);
                player.sendMessage(tooShortMsg);
                return;
            }
            waitingPlayers.remove(uuid);
            pending.onSubmit.accept(reason);

            String successMsg = plugin.getMessageManager().getMessage("report.success", Collections.emptyMap());
            player.sendMessage(successMsg);
        }
    }

    private static class PendingInput {
        final ReportType type;
        final String category;
        final Runnable onCancel;
        final java.util.function.Consumer<String> onSubmit;

        PendingInput(ReportType type, String category, Runnable onCancel, java.util.function.Consumer<String> onSubmit) {
            this.type = type;
            this.category = category;
            this.onCancel = onCancel;
            this.onSubmit = onSubmit;
        }
    }
}
