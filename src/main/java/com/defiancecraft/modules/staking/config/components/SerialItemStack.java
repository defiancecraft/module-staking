package com.defiancecraft.modules.staking.config.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SerialItemStack implements Cloneable {

	public String id;
	public int quantity;
	public short damage;
	public String name;
	public List<String> lore = new ArrayList<String>();
	public List<String> enchants = new ArrayList<String>();
	
	/**
	 * Only for config parsing - DO NOT USE
	 */
	public SerialItemStack() {}
	
	public SerialItemStack(ItemStack i) {
		this.id = i.getType().name();
		this.quantity = i.getAmount();
		this.damage = i.getDurability();
		this.name = i.getItemMeta().getDisplayName();
		this.lore = i.getItemMeta().getLore();
	
		// Add enchantments
		i.getEnchantments().forEach((e, l) -> {
			enchants.add(e.getName() + ":" + l);
		});
	}
	
	public SerialItemStack(ItemStack i, String name) {
		this(i);
		this.name = name;
	}
	
	public SerialItemStack(ItemStack i, String name, List<String> lore) {
		this(i, name);
		this.lore = lore;
	}
	
	public SerialItemStack(ItemStack i, List<String> enchants) {
		this(i);
		this.enchants = enchants;
	}
	
	/**
	 * Converts this SerialItemStack to its ItemStack equivalent.
	 * @return ItemStack
	 */
	public ItemStack toItemStack() {
		
		ItemStack is = new ItemStack(
				Material.getMaterial(this.id),
				this.quantity,
				this.damage);

		// Add enchantments
		if (this.enchants != null && this.enchants.size() > 0)
			for (String enchant : this.enchants)
				if (getEnchant(enchant) != null && getEnchantLevel(enchant) > 0)
					is.addUnsafeEnchantment(getEnchant(enchant), getEnchantLevel(enchant));
		
		ItemMeta meta = is.getItemMeta();
		
		// Set display name
		if (this.name != null && !this.name.isEmpty())
			meta.setDisplayName(translateCodes(this.name));
		
		// Set lore
		if (this.lore != null && this.lore.size() > 0)
			meta.setLore(
				this.lore
					.stream()
					.map(SerialItemStack::translateCodes)
					.collect(Collectors.toList())
			);
		
		is.setItemMeta(meta);
		
		
		return is;
	}
	
	/**
	 * Gets an enchantment from the encoded string (NAME:LEVEL)
	 * @param encoded Encoded string in format NAME:LEVEL
	 * @return Enchantment object (may be null)
	 */
	private Enchantment getEnchant(String encoded) {
		return Enchantment.getByName(encoded.split(":")[0]);
	}
	
	/**
	 * Gets the enchantment level from the encoded string (NAME:LEVEL)
	 * @param encoded Encoded string in format NAME:LEVEL
	 * @return Enchantment level, or 0
	 */
	private int getEnchantLevel(String encoded) {
		try {
			return Integer.parseInt(encoded.split(":").length > 1 ? encoded.split(":")[1] : "0");
		} catch (NumberFormatException e) { return 0; }
	}
	
	/**
	 * Translates the colour codes from ampersands in a string. Can be used as Consumer in streams
	 * @param input String to translate
	 * @return Translated string
	 */
	public static String translateCodes(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	
	public SerialItemStack clone() {
		return new SerialItemStack(this.toItemStack());
	}
	
}
