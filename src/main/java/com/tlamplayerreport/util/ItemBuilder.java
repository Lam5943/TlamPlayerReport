package com.tlamplayerreport.util;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    
    private ItemStack itemStack;
    
    public ItemBuilder(String materialName) {
        this.itemStack = XMaterial.matchXMaterial(materialName)
                .map(XMaterial::parseItem)
                .orElse(XMaterial.STONE.parseItem());
    }
    
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }
    
    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }
    
    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }
    
    public ItemBuilder addLoreLine(String line) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }
    
    public ItemBuilder skull(String identifier) {
        this.itemStack = XSkull.createItem()
                .profile(Profileable.detect(identifier))
                .apply();
        return this;
    }
    
    public ItemStack build() {
        return itemStack;
    }
}