package com.defiancecraft.modules.staking.menus.events.impl;

import org.bukkit.entity.Player;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.modules.staking.menus.events.ContainerEvent;

public class KitSelectionChangedEvent extends ContainerEvent {

	private int index = 0;
	private boolean value = false;
	
	public KitSelectionChangedEvent(Player player, Menu menu, int index, boolean value) {
		super(player, menu);
		this.index = index;
		this.value = value;
	}

	public int getKit() {
		return index;
	}
	
	public boolean isAccepted() {
		return value;
	}
	
}
