package com.defiancecraft.modules.staking.menus.events.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.modules.staking.menus.events.ContainerEvent;

public class StakedItemsUpdateEvent extends ContainerEvent {

	private ItemStack[] items;
	
	public StakedItemsUpdateEvent(Player player, Menu menu, ItemStack[] items) {
		super(player, menu);
		this.items = items;
	}
	
	public ItemStack[] getNewItems() {
		return items;
	}

}
