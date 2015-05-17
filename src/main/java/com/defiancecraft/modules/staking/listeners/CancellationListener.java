package com.defiancecraft.modules.staking.listeners;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.modules.staking.stakes.CancellationReason;
import com.defiancecraft.modules.staking.stakes.DestroyableStakeState;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.StakeState;
import com.defiancecraft.modules.staking.stakes.states.AwaitingAcceptState;

public class CancellationListener implements Listener {

	private StakeManager man;
	
	public CancellationListener(StakeManager man) {
		this.man = man;
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		
		if (!(e.getPlayer() instanceof Player))
			return;
		
		StakeState state = man.getState((Player)e.getPlayer());
		
		// Check for inventory close when choosing items
		if (state != null
				&& (state instanceof DestroyableStakeState)
				&& e.getInventory().getHolder() instanceof Menu
				&& !((DestroyableStakeState)state).isDestroyed()) {
			state.cancel(state.getAlpha() == e.getPlayer() ? CancellationReason.ALPHA_CANCELLED : CancellationReason.BETA_CANCELLED);
			if (state.getAlpha() != null && man.getState(state.getAlpha()) == state)
				man.removeState(state.getAlpha(), false);
			if (state.getBeta() != null && man.getState(state.getBeta()) == state)
				man.removeState(state.getBeta(), false);
		}
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
	
		StakeState state = man.getState(e.getPlayer());
		
		if (state != null) {
			state.cancel(state.getAlpha() == e.getPlayer() ? CancellationReason.ALPHA_OFFLINE : CancellationReason.BETA_OFFLINE);
			if (state.getAlpha() != null && man.getState(state.getAlpha()) == state)
				man.removeState(state.getAlpha(), false);
			if (state.getBeta() != null && man.getState(state.getBeta()) == state)
				man.removeState(state.getBeta(), false);
		}

		// Cancel any awaiting states, notifying alpha if they're online.
		// This is only necessary because betas do not have their own state in
		// the awaiting state.
		if (AwaitingAcceptState.isBeta(e.getPlayer().getUniqueId())) {
			for (StakeState ss : man.getStatesWithBeta(e.getPlayer())) {
				
				ss.cancel(CancellationReason.BETA_OFFLINE);
				if (ss.getAlpha() != null)
					man.removeState(ss.getAlpha(), false);
				
			}
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent e) {
		checkMovementEvent(e);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		checkMovementEvent(e);
	}
	
	private void checkMovementEvent(PlayerMoveEvent e) {
		
		// Cancel if no movement, not in a state and not a beta.
		if (e.isCancelled()
				|| e.getFrom().getBlock().equals(e.getTo().getBlock())
				|| (!man.hasState(e.getPlayer()) && !AwaitingAcceptState.isBeta(e.getPlayer().getUniqueId())))
			return;
		
		List<StakeState> states = man.hasState(e.getPlayer()) ? Arrays.asList(man.getState(e.getPlayer())) : man.getStatesWithBeta(e.getPlayer());
		
		// Do for all states, as there may be multiple if player is a beta, as he
		// may have received multiple requests.
		for (StakeState state : states) {
			if (!state.isInArea(e.getTo())) {
				state.cancel(state.getAlpha() == e.getPlayer() ? CancellationReason.ALPHA_LEFT_AREA : CancellationReason.BETA_LEFT_AREA);
				
				// Remove from both, regardless of whether player is alpha or beta (although both
				// must be accounted for)
				if (man.hasState(state.getAlpha()) && man.getState(state.getAlpha()) == state)
					man.removeState(state.getAlpha(), false);
				
				if (man.hasState(state.getBeta()) && man.getState(state.getBeta()) == state)
					man.removeState(state.getBeta(), false);
			}
		}
		
	}
	
}
