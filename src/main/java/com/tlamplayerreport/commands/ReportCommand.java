package com.tlamplayerreport.commands;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.inventory.impl.MainReportGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {
    
    private final ReportPlugin plugin;
    
    public ReportCommand(ReportPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "commands.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("tlamplayerreport.report")) {
            plugin.getMessageManager().sendMessage(player, "commands.no-permission");
            return true;
        }
        
        MainReportGUI mainGUI = new MainReportGUI(plugin);
        plugin.getGuiManager().openGUI(mainGUI, player);
        
        return true;
    }
}