package com.tlamplayerreport;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.HashMap;
import java.util.Map;

public class ReportChatInputManager {
    // Map to track waiting players and their corresponding report types
    private Map<Player, ReportType> waitingPlayers = new HashMap<>();

    // Starts the prompt for a player to report
    public void startPromptForPlayer(Player player, ReportType type, String category) {
        // Logic for starting the prompt.
        // Example: Send a message to the player to start reporting.
        player.sendMessage("Please provide a description for your report on " + category);
        waitingPlayers.put(player, type);
    }

    // Handles the chat event when the player responds to the prompt
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if this player is currently waiting for a report input
        if (waitingPlayers.containsKey(player)) {
            ReportType type = waitingPlayers.get(player);
            String description = event.getMessage();

            // Logic to submit the report
            submitReport(player, type, description);

            // Remove player from waiting list
            waitingPlayers.remove(player);
            event.setCancelled(true); // Cancel the chat event to avoid flooding
        }
    }

    // Method to submit the report (placeholder for actual implementation)
    private void submitReport(Player player, ReportType type, String description) {
        // Implement report submission logic here
        player.sendMessage("Your report has been submitted successfully.");
        // Additional logic (timeout, cancellation etc.) can be added here.
    }
}

// Assume ReportType is an enum or class that you've defined elsewhere.