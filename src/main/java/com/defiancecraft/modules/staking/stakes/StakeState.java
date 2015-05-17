package com.defiancecraft.modules.staking.stakes;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface StakeState extends Runnable {

	/**
	 * Gets the alpha player, i.e. the player who initiated the stake.
	 * This can be null if the player is offline, and is often stored using
	 * WeakReferences for this reason.
	 * @return Player, or null
	 */
	public Player getAlpha();

	/**
	 * Gets the beta player, i.e. the player who accepted the stake.
	 * This can be null if the player is offline, and is often stored using
	 * WeakReferences for this reason.
	 * @return Player, or null
	 */
	public Player getBeta();
			
	/**
	 * Runs this state as a BukkitRunnable on the next game tick
	 * after a player's state is set to an invocation of this state.
	 */
	public void run();
	
	/**
	 * Cancels the current state; will be called if any conditions
	 * are suddenly void, e.g. the player exits the staking area
	 */
	public void cancel(CancellationReason reason);

	/**
	 * Performs any necessary operations before this state is destroyed;
	 * this occurs when switching to another state, so players don't remain
	 * in static Maps, for example. This should NOT occur when the state is
	 * cancelled - they are two separate events.
	 */
	public void destroy();
	
	/**
	 * Checks if a location is within the designated area for this stage. If
	 * or when this method returns false, the stake will be cancelled.
	 * 
	 * @param loc Location to check
	 * @return Whether the location is in the staking area
	 */
	public boolean isInArea(Location loc);
	
}
