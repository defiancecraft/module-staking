package com.defiancecraft.modules.staking.menus.events;

import org.bukkit.entity.Player;

import com.defiancecraft.core.menu.Menu;

/**
 * Represents an event which occurred within a menu, and
 * must be emitted to the other menu in a container.
 * <br><br>
 * ContainerEvent objects perform little function, other than
 * keeping properties which must be passed between menus in a
 * container.
 */
public abstract class ContainerEvent {

	protected Player player;
	protected Menu menu;
	
	protected ContainerEvent(Player player, Menu menu) {
		this.player = player;
		this.menu = menu;
	}
	
	/**
	 * Gets the player that triggered this event
	 * @return Player
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets the menu from which this event was fired
	 * @return Menu
	 */
	public Menu getMenu() {
		return menu;
	}
	
}
