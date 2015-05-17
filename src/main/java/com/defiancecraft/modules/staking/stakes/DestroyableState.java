package com.defiancecraft.modules.staking.stakes;

/**
 * A state which needs to keep track of whether or not it has
 * been destroyed.
 * <br><br>
 * This is primarily used for synchronization between events and
 * states, which often involve things like menus. When the state is
 * destroyed, it will close the menu, and the listener must know that
 * it is destroyed before acting on an inventory close.
 */
public interface DestroyableState extends StakeState {

	/**
	 * Checks whether the state has been destroyed.
	 * @return
	 */
	public boolean isDestroyed();
	
	/**
	 * Sets whether the state has been destroyed.
	 * @param destroyed Whether the state has been destroyed.
	 */
	public void setDestroyed(boolean destroyed);
	
}
