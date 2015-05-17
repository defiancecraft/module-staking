package com.defiancecraft.modules.staking.stakes;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.staking.config.components.ArenaConfig;

public class ArenaManager {

	private static LinkedList<Entry<PlayerPair, Consumer<ArenaConfig>>> queue = new LinkedList<>();
	private static Map<ArenaConfig, PlayerPair> arenas = new HashMap<ArenaConfig, PlayerPair>();
	private static Plugin plugin;
	
	/**
	 * Initializes the ArenaManager with a list of arenas to setup.
	 * @param arenas Arenas that may be used; usually defined in a configuration.
	 */
	public static void init(List<ArenaConfig> arenas, Plugin plugin) {
		
		ArenaManager.plugin = plugin;
		
		for (ArenaConfig arena : arenas)
			ArenaManager.arenas.put(arena, null);
		
	}
	
	/**
	 * Adds an arena to the list of arenas
	 * @param arena Arena to add
	 */
	public static void addArena(ArenaConfig arena) {
		
		if (arenas.containsKey(arena))
			throw new IllegalArgumentException("Arena already exists!");
		
		arenas.put(arena, null);
		
	}

	/**
	 * Gets the ArenaConfig (the key) for a free arena, if one exists.
	 * If there are none, null is returned.
	 * @return Free ArenaConfig, or null 
	 */
	public static ArenaConfig getFreeArena() {
		
		for (Entry<ArenaConfig, PlayerPair> entry : arenas.entrySet())
			if (entry.getValue() == null)
				return entry.getKey();
		
		return null;
		
	}
	
	/**
	 * Gets the ArenaConfig for a PlayerPair
	 * @param pair Pair of players in the arena
	 * @return ArenaConfig, or null if the arena was not found.
	 */
	public static ArenaConfig getArena(PlayerPair pair) {
		
		for (Entry<ArenaConfig, PlayerPair> entry : arenas.entrySet())
			if (entry.getValue() != null && entry.getValue().equals(pair))
				return entry.getKey();
		
		return null;
		
	}
	
	/**
	 * Attempts to allocate an arena to a pair of players. If there are no free
	 * arenas, the players are placed into the queue. When a pair of players is
	 * deallocated, the last added pair is popped off the queue and allocated the
	 * newly free arena. When this happens, `callback` will be called with their
	 * arena.
	 * <br><br>
	 * If there are free arenas, they are allocated to an arena instantaneously,
	 * and thus the callback will be called straight away.
	 * <br><br>
	 * Note that the callback will be executed on the next game tick (i.e. as a
	 * BukkitRunnable) to avoid conflicts from using this method, and so should
	 * account for this.
	 * 
	 * @param pair Pair of players to allocate an arena
	 * @param callback A callback to be executed when the pair are allocated an arena.
	 * @return Whether they were allocated an arena. If false, they were queued.
	 */
	public static boolean allocate(PlayerPair pair, Consumer<ArenaConfig> callback) {
		
		if (arenas.size() == 0)
			throw new NotInitializedException();
		
		ArenaConfig free = getFreeArena();
		
		if (free != null) {
			arenas.put(free, pair);
			new BukkitRunnable() {
				public void run() {
					callback.accept(free);
				}
			}.runTask(plugin);
			return true;
		} else {
			queue.add(new AbstractMap.SimpleEntry<>(pair, callback));
		}
		
		return false;
		
	}
	
	/**
	 * Removes a pair of players from their given arena, freeing up an arena.
	 * Note that the queue will be updated as this method is called, so it may be
	 * a good idea to perform any necessary processes such as teleporting players
	 * out of an arena before this method is called.
	 * 
	 * @param pair Pair of players to deallocate an arena.
	 */
	public static void deallocate(PlayerPair pair) {
		
		if (arenas.containsValue(pair))
			arenas.put(getArena(pair), null);
		
		updateQueue();
		
	}
	
	/**
	 * Updates the queue, if possible, by popping off the next pair
	 */
	public static void updateQueue() {
		
		ArenaConfig free;
		
		if (queue.size() == 0 || (free = getFreeArena()) == null)
			return;
		
		// Give the next pair an arena!
		Entry<PlayerPair, Consumer<ArenaConfig>> nextPair = queue.pop();
		arenas.put(free, nextPair.getKey());
		
		final Consumer<ArenaConfig> consumer = nextPair.getValue();
		new BukkitRunnable() {
			public void run() {
				consumer.accept(free);
			}
		}.runTask(plugin);
		
	}
	
	/**
	 * Dequeues a pair of players that are waiting in the queue.
	 * @param pair Pair of players to dequeue
	 */
	public static void dequeue(PlayerPair pair) {

		for (Entry<PlayerPair, Consumer<ArenaConfig>> entry : queue)
			if (entry.getKey().equals(pair))
				queue.remove(entry);
		
	}
	
	public static class NotInitializedException extends RuntimeException {
		
		private static final long serialVersionUID = -1713131128648858313L;

		public String getMessage() { 
			return "The ArenaManager has not been initialized or no arenas are defined. Call ArenaManager#init() before attempting to allocate arenas.";
		}
		
	}
	
}
