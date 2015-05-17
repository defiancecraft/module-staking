package com.defiancecraft.modules.staking.menus.events.impl;

import org.bukkit.entity.Player;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.modules.staking.menus.events.ContainerEvent;

public class TokenStakeUpdateEvent extends ContainerEvent {

	private int newTokens = 0;
	
	public TokenStakeUpdateEvent(Player player, Menu menu, int newTokens) {
		super(player, menu);
		this.newTokens = newTokens;
	}
	
	public int getNewTokenAmount() {
		return newTokens;
	}

}
