package com.defiancecraft.modules.staking.stakes.states;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.menus.KitSelectionMenuContainer;
import com.defiancecraft.modules.staking.stakes.ArenaManager;
import com.defiancecraft.modules.staking.stakes.CancellationReason;
import com.defiancecraft.modules.staking.stakes.DestroyableStakeState;
import com.defiancecraft.modules.staking.stakes.PlayerPair;
import com.defiancecraft.modules.staking.stakes.StakeManager;

public class KitSelectionState extends DestroyableStakeState {

	private PlayerPair pair;
	private StakeManager man;
	private KitSelectionMenuContainer container;
	
	public KitSelectionState(PlayerPair pair, StakeManager man) {
		this.pair = pair;
		this.man = man;
	}
	
	@Override
	public Player getAlpha() {
		return pair.getAlpha();
	}

	@Override
	public Player getBeta() {
		return pair.getBeta();
	}

	@Override
	public void run() {
		
		this.container = new KitSelectionMenuContainer(pair, man);
		this.container.showMenu();
		
	}

	@Override
	public void cancel(CancellationReason reason) {
	
		ArenaManager.deallocate(pair);
		pair.restoreItems();
		
		Plugin plugin = JavaPlugin.getPlugin(Staking.class);
		
		String reasonString = Lang.get(
				Staking.NAME,
				reason.getMessageField(),
				reason.isAlpha() ? (getAlpha() != null ? getAlpha().getName() : "1") :
				reason.isBeta() ? (getBeta() != null ? getBeta().getName() : "2") :
				""
		);
		
		// Send message to alpha & close inventory
		if (getAlpha() != null && getAlpha().isOnline()) {
			getAlpha().sendMessage(reasonString);
			final Player alpha = getAlpha();
			new BukkitRunnable() {
				public void run() {
					if (alpha != null)
						alpha.closeInventory();
				}
			}.runTask(plugin);
		}
		
		// Send message to beta & close inventory
		if (getBeta() != null && getBeta().isOnline()) {
			getBeta().sendMessage(reasonString);
			final Player beta = getBeta();
			new BukkitRunnable() {
				public void run() {
					if (beta != null)
						beta.closeInventory();
				}
			}.runTask(plugin);
		}
		
	}

	@Override
	public void destroy() {

		this.setDestroyed(true);
		this.container.closeMenu();
		
	}

	@Override
	public boolean isInArea(Location loc) {
		return Staking.getConfiguration().stakingArea.toSelection().contains(loc);
	}

}
