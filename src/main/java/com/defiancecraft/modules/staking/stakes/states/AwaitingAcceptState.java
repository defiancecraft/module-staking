package com.defiancecraft.modules.staking.stakes.states;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.stakes.CancellationReason;
import com.defiancecraft.modules.staking.stakes.StakeState;
import com.defiancecraft.modules.staking.util.RawMessaging;

public class AwaitingAcceptState implements StakeState {

	private static List<UUID> betas = new ArrayList<UUID>();
	
	// Note: alpha is the initiator of the duel; beta is the acceptant
	private WeakReference<Player> alpha;
	private WeakReference<Player> beta;
	private String betaName;
	private UUID betaUUID;
	
	public AwaitingAcceptState(Player alpha, Player beta) {
		this.alpha = new WeakReference<Player>(alpha);
		this.beta = new WeakReference<Player>(beta);
		this.betaName = beta.getName();
		this.betaUUID = beta.getUniqueId();
		betas.add(betaUUID);
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

		if (alpha.get() == null || beta.get() == null) {
			cancel(CancellationReason.BETA_OFFLINE);
			return;
		}
			
		RawMessaging.sendRawMessage(beta.get(), Lang.get(Staking.NAME, Staking.LANG_ACCEPT_STAKE, alpha.get().getName()), "/accept " + alpha.get().getName());
		
	}
	
	@Override
	public void cancel(CancellationReason reason) {
		
		// Send a message to alpha if beta leaves, but not vice versa;
		// they have the option to accept, they don't need to be notified when
		// it's cancelled as they haven't accepted
		if (alpha.get() != null && alpha.get().isOnline()) {
			alpha.get().sendMessage(Lang.get(Staking.NAME, reason.getMessageField(), reason.isAlpha() ? alpha.get().getName() : betaName));
		}
		
		if (betas.contains(betaUUID))
			betas.remove(betaUUID);
		
	}
	
	@Override
	public void destroy() {
		
		if (betas.contains(betaUUID))
			betas.remove(betaUUID);
		
	}
	
	@Override
	public boolean isInArea(Location loc) {
		return Staking.getConfiguration().stakingArea.toSelection().contains(loc);
	}

	/**
	 * Checks if a player is a 'beta'; this is necessary
	 * because in this state, betas do not have their own state
	 * assigned to them as they have not accepted. This should not
	 * be necessary for other states.
	 * 
	 * @param uuid UUID of beta to check
	 * @return Whether they are a beta in a stake
	 */
	public static boolean isBeta(UUID uuid) {
		return betas.contains(uuid);
	}

}
