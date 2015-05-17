package com.defiancecraft.modules.staking.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtils {

	/**
	 * Sets the display name of an ItemStack.
	 * <br><br>
	 * This is purely a convenience function, and does the same as
	 * retrieving the ItemMeta object, setting the display name, and then
	 * setting the new ItemMeta to the ItemStack (see {@link org.bukkit.inventory.ItemStack#getItemMeta()}).
	 * 
	 * @param item Item to set display name of
	 * @param name New display name of item
	 * @return The ItemStack (can be used for building ItemStacks)
	 */
	public static ItemStack setDisplayName(ItemStack item, String name) {
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		
		return item; 
		
	}
	
	public static boolean isHelmet(ItemStack item) {
		switch (item.getType()) {
		case LEATHER_HELMET:
		case CHAINMAIL_HELMET:
		case IRON_HELMET:
		case GOLD_HELMET:
		case DIAMOND_HELMET:
		case PUMPKIN:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isChestplate(ItemStack item) {
		switch (item.getType()) {
		case LEATHER_CHESTPLATE:
		case CHAINMAIL_CHESTPLATE:
		case IRON_CHESTPLATE:
		case GOLD_CHESTPLATE:
		case DIAMOND_CHESTPLATE:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isLeggings(ItemStack item) {
		switch (item.getType()) {
		case LEATHER_LEGGINGS:
		case CHAINMAIL_LEGGINGS:
		case IRON_LEGGINGS:
		case GOLD_LEGGINGS:
		case DIAMOND_LEGGINGS:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isBoots(ItemStack item) {
		switch (item.getType()) {
		case LEATHER_BOOTS:
		case CHAINMAIL_BOOTS:
		case IRON_BOOTS:
		case GOLD_BOOTS:
		case DIAMOND_BOOTS:
			return true;
		default:
			return false;
		}
	}
	
}
