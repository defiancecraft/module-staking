package com.defiancecraft.modules.staking.stakes;

/**
 * An implementation of DestroyableState
 */
public abstract class DestroyableStakeState implements DestroyableState {

	private boolean destroyed = false;
	
	@Override
	public boolean isDestroyed() {
		return destroyed;
	}
	
	@Override
	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
	
}
