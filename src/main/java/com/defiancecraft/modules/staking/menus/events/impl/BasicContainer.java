package com.defiancecraft.modules.staking.menus.events.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.modules.staking.menus.events.ContainerEvent;
import com.defiancecraft.modules.staking.menus.events.ContainerEventable;

public class BasicContainer implements ContainerEventable {

	private Map<Class<? extends ContainerEvent>, List<RegisteredContainer<?>>> listeners = new HashMap<>();
	
	@Override
	public void emit(ContainerEvent e) {
		
		if (!listeners.containsKey(e.getClass()))
			return;
		
		for (RegisteredContainer<?> listener : listeners.get(e.getClass()))
			if (listener.owner != e.getMenu())
				listener.callback(e);
		
	}

	@Override
	public <T extends ContainerEvent> void register(Menu owner, Class<T> type, Consumer<T> callback) {
		
		if (!listeners.containsKey(type))
			listeners.put(type, new ArrayList<RegisteredContainer<?>>());
		
		listeners.get(type).add(new RegisteredContainer<T>(type, owner, callback));
		
	}
	
	class RegisteredContainer <T extends ContainerEvent> {
		
		private Menu owner;
		private Consumer<T> callback;
		private Class<T> type;
		
		protected RegisteredContainer(Class<T> type, Menu owner, Consumer<T> callback) {
			this.owner = owner;
			this.callback = callback;
			this.type = type;
		}
		
		public void callback(ContainerEvent event) {
			try {
				callback.accept(type.cast(event));
			} catch (ClassCastException e) {}
		}
		
	}

}
