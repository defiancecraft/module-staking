package com.defiancecraft.modules.staking.config;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.modules.staking.config.components.SerialItemStack;

public class KitSelectionConfig {

	public String title = "&b&lChoose Kits";
	
	public List<List<SerialItemStack>> kits = Arrays.asList(
		Arrays.asList(
			new SerialItemStack(new ItemStack(Material.DIAMOND_HELMET, 1)),
			new SerialItemStack(new ItemStack(Material.DIAMOND_CHESTPLATE, 1), Arrays.asList("PROTECTION_ENVIRONMENTAL:5")),
			new SerialItemStack(new ItemStack(Material.DIAMOND_LEGGINGS, 1)),
			new SerialItemStack(new ItemStack(Material.DIAMOND_BOOTS, 1)),
			new SerialItemStack(new ItemStack(Material.DIAMOND_SWORD, 1))
		)
	);
	
}
