package com.defiancecraft.modules.staking.stakes;

import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class StakeManager {

	private WeakHashMap<Player, StakeState> states = new WeakHashMap<>();
	private Plugin plugin;
	
	public StakeManager(Plugin p) {
		this.plugin = p;
	}
	
	/**
	 * Checks if a player has a StakeState
	 * @param p Player to check
	 * @return Whether they have a state
	 */
	public boolean hasState(Player p) {
		return states.containsKey(p);
	}
	
	/**
	 * Gets the state of a player
	 * @param p Player to get state of
	 * @return StakeState
	 */
	public StakeState getState(Player p) {
		return states.get(p);
	}
	
	/**
	 * Sets the state of a player to the given state. This is generally
	 * used to advance from state to state.
	 * <br><br>
	 * Note that for the state to take effect, {@link #runState(Player)}
	 * or {@link #runState(StakeState)} must be called in order to run
	 * the state itself. This method simply sets their state in the Map
	 * so it can be retrieved and checked via {@link #hasState(Player)} and
	 * {@link #getState(Player)}.
	 * <br><br>
	 * If the player whose state is set already has a state, the state
	 * will be destroyed, thus indicating a successful change of state. If
	 * this is not the desired intention, use {@link #removeState(Player, boolean)}
	 * with destroy set to false and set their state. This way, the state will
	 * not be destroyed.
	 * 
	 * @param p Player to set state of
	 * @param state New state
	 */
	public void setState(Player p, StakeState state) {
		
		if (hasState(p))
			getState(p).destroy();
		
		states.put(p, state);
		
	}

	/**
	 * Runs a player's state on the Bukkit thread (i.e
	 * in a BukkitRunnable). This will likely affect both
	 * players if the state has progressed from a single
	 * player state, and thus it may be more preferable to
	 * use {@link #runState(StakeState)} in this case.
	 * <br><br>
	 * This method will have no effect if the player has no state.
	 * 
	 * @param p Player to run state of
	 */
	public void runState(Player p) {

		if (!hasState(p))
			return;
		
		StakeState state = getState(p);
		runState(state);
		
	}
	
	/**
	 * Runs a state on the Bukkit thread (i.e. in a
	 * BukkitRunnable). This will likely affect both players
	 * within the state, if it is a two player state.
	 * <br><br>
	 * It is necessary that this method is called in order
	 * for a state to actually take effect (see {@link #setState(Player, StakeState)})
	 * 
	 * @param state State to run
	 */
	public void runState(StakeState state) {
		
		new BukkitRunnable() {
			public void run() {
				try {
					state.run();
				} catch (Throwable t) {
					Bukkit.getLogger().severe("[Staking] Oh damn, an exception or something occurred during stake. Gonna have to cancel, I think.");
					Bukkit.getLogger().severe("[Staking] This could have unpredictable consequences; players involved: " + (state.getAlpha() == null ? "unknown" : state.getAlpha().getName()) + " (alpha) and " + (state.getBeta() == null ? "unknown" : state.getBeta().getName()));
					t.printStackTrace();
					state.cancel(CancellationReason.SHUTDOWN);
				}
			}
		}.runTask(plugin);
		
	}
	
	/**
	 * Removes a state from a player, with the option to
	 * 'destroy' it (call the destroy method, which indicates a
	 * successful change or execution of a state as opposed to
	 * the cancel method on a state).
	 * <br><br>
	 * Like the {@link #setState(Player, StakeState)} method, the
	 * destroy method is not run in the Bukkit thread, but rather the
	 * same thread that this method is executed in. Thus, it is necessary
	 * to run any code that must be in the Bukkit thread in a BukkitRunnable.
	 * 
	 * @param p Player to remove state from
	 * @param destroy Whether or not to call the destroy method of the state
	 */
	public void removeState(Player p, boolean destroy) {
		
		if (destroy && hasState(p))
			getState(p).destroy();
		
		states.remove(p);
		
	}
	
	/**
	 * Gets a list of states with the given player as 'beta' (i.e. the
	 * player who did not initiate the stake). There will generally be
	 * more than one of these if the player has multiple requests for
	 * a stake and does not have a stake of their own.
	 * 
	 * @param beta The player to find as 'beta' in states 
	 * @return A list of StakeStates with the given player as 'beta'. This may be empty
	 */
	public List<StakeState> getStatesWithBeta(Player beta) {
		return states.values().stream()
			.filter((p) -> p.getBeta() == beta)
			.collect(Collectors.toList());
	}
	
	/**
	 * Shuts down the StakeManager by cancelling all active states with the
	 * reason {@link CancellationReason#SHUTDOWN}.
	 */
	public void shutdown() {
		
		for (Entry<Player, StakeState> entry : states.entrySet())
			entry.getValue().cancel(CancellationReason.SHUTDOWN);
		
	}
	
}
