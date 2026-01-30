package com.tlamplayerreport.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryButton {
    
    private final int slot;
    private final ItemStack item;
    private final ClickAction action;
    
    public InventoryButton(int slot, ItemStack item, ClickAction action) {
        this.slot = slot;
        this.item = item;
        this.action = action;
    }
    
    public int getSlot() {
        return slot;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public void onClick(InventoryClickEvent event) {
        if (action != null) {
            action.execute((Player) event.getWhoClicked(), event);
        }
    }
    
    @FunctionalInterface
    public interface ClickAction {
        void execute(Player player, InventoryClickEvent event);
    }
}