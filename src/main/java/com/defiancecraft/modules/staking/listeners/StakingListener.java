package com.defiancecraft.modules.staking.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.states.StakingState;

public class StakingListener implements Listener {

	private StakeManager man;
	
	public StakingListener(StakeManager man) {
		this.man = man;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		
		// Ensure that they moved and they are actually staking
		if (e.getFrom().getBlock().equals(e.getTo().getBlock())
				|| !man.hasState(e.getPlayer())
				|| !(man.getState(e.getPlayer()) instanceof StakingState))
			return;
		
		StakingState state = (StakingState)man.getState(e.getPlayer());
		
		// Freeze players if stake still starting
		if (state.isCountingDown()) {
			e.setCancelled(true);
			e.setTo(e.getFrom());
			e.getPlayer().setVelocity(new Vector());
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(EntityDamageEvent e) {

		if (!(e.getEntity() instanceof Player)
				|| !man.hasState((Player)e.getEntity())
				|| !(man.getState((Player)e.getEntity()) instanceof StakingState))
				return;
		
		Player p = (Player) e.getEntity();
		
		if (((StakingState)man.getState(p)).isCountingDown()) {
			e.setCancelled(true);
			return;
		}
		
		if (p.getHealth() - e.getFinalDamage() <= 0) {
			
			e.setCancelled(true);
			((StakingState)man.getState(p)).onWin(p == man.getState(p).getAlpha() ? man.getState(p).getBeta() : man.getState(p).getAlpha());
			
		}
		
	}
	
}
