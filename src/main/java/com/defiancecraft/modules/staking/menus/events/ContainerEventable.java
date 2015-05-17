package com.defiancecraft.modules.staking.menus.events;

import java.util.function.Consumer;

import com.defiancecraft.core.menu.Menu;

public interface ContainerEventable {

	/**
	 * Emits an event to all of the relevant menus.
	 * @param e Event to emit
	 */
	public void emit(ContainerEvent e);
	
	/**
	 * Registers a callback for a certain type of event to a menu. The menu is
	 * required so that when emitted, the same menu does not receive its event.
	 * 
	 * @param owner Menu who will receive the event
	 * @param type Type of event to listen for
	 * @param callback Callback to execute when event is received.
	 */
	public <T extends ContainerEvent> void register(Menu owner, Class<T> type, Consumer<T> callback);
	
}
