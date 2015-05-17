package com.defiancecraft.modules.staking.stakes.states;

import java.lang.ref.WeakReference;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.config.components.ArenaConfig;
import com.defiancecraft.modules.staking.stakes.ArenaManager;
import com.defiancecraft.modules.staking.stakes.CancellationReason;
import com.defiancecraft.modules.staking.stakes.PlayerPair;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.StakeState;

public class ArenaQueueState implements StakeState {

	private WeakReference<Player> alpha;
	private WeakReference<Player> beta;
	private StakeManager man;
	private PlayerPair pair;
	private volatile ArenaConfig arena;
	
	public ArenaQueueState(Player alpha, Player beta, StakeManager man) {
		this.alpha = new WeakReference<Player>(alpha);
		this.beta = new WeakReference<Player>(beta);
		this.man = man;
	}
	
	@Override
	public Player getAlpha() {
		return alpha.get();
	}

	@Override
	public Player getBeta() {
		return beta.get();
	}

	@Override
	public void run() {
		
		// If one of them is offline when this runs for whatever reason, cancel and remove
		if (performOfflineCheck())
			return;
		
		// Notify them that the other player has accepted
		if (getAlpha() != null && getAlpha().isOnline())
			getAlpha().sendMessage(Lang.get(Staking.NAME, Staking.LANG_ACCEPTED_STAKE, getBeta().getName()));
		
		this.pair = new PlayerPair(alpha.get(), beta.get());
		if (!ArenaManager.allocate(pair, this::onAllocated)) {
			alpha.get().sendMessage(Lang.get(Staking.NAME, Staking.LANG_QUEUED));
			beta.get().sendMessage(Lang.get(Staking.NAME, Staking.LANG_QUEUED));
		}
		
	}

	private void onAllocated(ArenaConfig arena) {
		
		if (performOfflineCheck())
			return;
		
		ChooseItemsState state = new ChooseItemsState(pair, man);
		man.setState(alpha.get(), state);
		man.setState(beta.get(), state);
		man.runState(state);
		
	}
	
	private boolean performOfflineCheck() {
		
		// Check that neither are offline
		if (alpha.get() == null || beta.get() == null) {
			cancel(alpha.get() == null ? CancellationReason.ALPHA_OFFLINE : CancellationReason.BETA_OFFLINE);
			if (alpha.get() != null)
				man.removeState(alpha.get(), false);
			if (beta.get() != null)
				man.removeState(beta.get(), false);
			return true;
		}
		
		return false;
		
	}
	
	@Override
	public void cancel(CancellationReason reason) {
		
		if (this.pair != null)
			if (this.arena != null)
				ArenaManager.deallocate(pair);
			else 
				ArenaManager.dequeue(pair);
		
		String format = "";
		switch (reason) {
		case ALPHA_LEFT_AREA:
		case ALPHA_OFFLINE:
			format = this.alpha.get() == null ? "1" : alpha.get().getName();
		case BETA_LEFT_AREA:
		case BETA_OFFLINE:
			format = this.beta.get() == null ? "2" : beta.get().getName();
		default:
			format = "";
		}
		
		if (alpha.get() != null)
			alpha.get().sendMessage(Lang.get(Staking.NAME, reason.getMessageField(), format));
		
		if (beta.get() != null)
			beta.get().sendMessage(Lang.get(Staking.NAME, reason.getMessageField(), format));
		
	}

	@Override
	public void destroy() {
		
		// TODO?
		
	}

	@Override
	public boolean isInArea(Location loc) {
		return Staking.getConfiguration().stakingArea.toSelection().contains(loc);
	}

}
