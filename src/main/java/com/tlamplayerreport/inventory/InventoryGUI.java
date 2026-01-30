package com.tlamplayerreport.inventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public abstract class InventoryGUI implements InventoryHandler {
    
    protected final Inventory inventory;
    protected final Map<Integer, InventoryButton> buttons;
    
    public InventoryGUI(String title, int size) {
        this.inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
        this.buttons = new HashMap<>();
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void addButton(InventoryButton button) {
        buttons.put(button.getSlot(), button);
        inventory.setItem(button.getSlot(), button.getItem());
    }
    
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null) return;
        
        InventoryButton button = buttons.get(event.getSlot());
        if (button != null) {
            button.onClick(event);
        }
    }
    
    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
    
    @Override
    public void onClose(InventoryCloseEvent event) {
    }
    
    public abstract void refresh();
}