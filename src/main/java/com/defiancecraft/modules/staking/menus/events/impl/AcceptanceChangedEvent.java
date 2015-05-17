package com.defiancecraft.modules.staking.menus.events.impl;

import org.bukkit.entity.Player;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.modules.staking.menus.events.ContainerEvent;

public class AcceptanceChangedEvent extends ContainerEvent {

	private boolean value = false;
	
	public AcceptanceChangedEvent(Player player, Menu menu, boolean value) {
		super(player, menu);
		this.value = value;
	}
	
	public boolean getAccepted() {
		return value;
	}

}
