package com.defiancecraft.modules.staking.config;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.modules.staking.config.components.SerialItemStack;

public class ChooseItemsConfig {

	public String title = "&b&lChoose Items";
	public int rows = 6;
	public SerialItemStack placeholder = new SerialItemStack(new ItemStack(Material.IRON_BARDING, 1));
	public SerialItemStack tokenItem = new SerialItemStack(new ItemStack(Material.DOUBLE_PLANT, 1), "&a+ %dT");
	public SerialItemStack stakedTokensItem = new SerialItemStack(new ItemStack(Material.POTATO_ITEM, 1), "&bYour stake: %d", Arrays.asList("&9Their stake: %d"));
	public int tokenValue1 = 1;
	public int tokenValue2 = 5;
	public int tokenValue3 = 10;
	
}
