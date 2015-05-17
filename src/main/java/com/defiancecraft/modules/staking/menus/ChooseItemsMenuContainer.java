package com.defiancecraft.modules.staking.menus;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.defiancecraft.modules.staking.menus.events.impl.BasicContainer;
import com.defiancecraft.modules.staking.stakes.PlayerPair;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.states.KitSelectionState;

public class ChooseItemsMenuContainer extends BasicContainer {

	private static final int INVENTORY_SIZE = 36;
	
	private ChooseItemsMenu menuAlpha;
	private ChooseItemsMenu menuBeta;
	private PlayerPair pair;
	private StakeManager man;
	
	public ChooseItemsMenuContainer(PlayerPair pair, Plugin plugin, StakeManager man) {
		this.menuAlpha = new ChooseItemsMenu(this, plugin);
		this.menuBeta = new ChooseItemsMenu(this, plugin);
		this.pair = pair;
		this.man = man;
	}
	
	public void showMenu() {
		this.menuAlpha.openMenu(pair.getAlpha());
		this.menuBeta.openMenu(pair.getBeta());
	}

	public void closeMenu() {
		this.menuAlpha.closeMenu(pair.getAlpha());
		this.menuBeta.closeMenu(pair.getBeta());
	}
	
	/**
	 * Updates the stakes for each player as per the menus on the PlayerPair object.
	 */
	public void updatePairStakes() {
	
		// Set the new variables in the player pair
		pair.setAlphaStake(menuAlpha.getPlayerItems());
		pair.setAlphaTokens(menuAlpha.getTokens());
		pair.setBetaStake(menuBeta.getPlayerItems());
		pair.setBetaTokens(menuBeta.getTokens());
		
	}
	
	public void changeState() {
		
		this.updatePairStakes();
		
		Player alpha = pair.getAlpha();
		Player beta  = pair.getBeta();
		KitSelectionState state = new KitSelectionState(pair, man);
		
		man.setState(alpha, state);
		man.setState(beta, state);
		man.runState(state);
		
	}
	
	public boolean remoteHasInventorySpace(Player p, List<ItemStack> items) {
		
		Player remote = pair.getAlpha() == p ? pair.getBeta() : pair.getAlpha();
		ItemStack[] remoteInv = remote.getInventory().getContents();
		int remoteItemCount = 0;
		
		// Count where it isn't air
		for (int i = 0; i < remoteInv.length; i++)
			if (remoteInv[i] != null) remoteItemCount++;
		
		remoteItemCount += (remote == pair.getAlpha() ? menuAlpha.getPlayerItems() : menuBeta.getPlayerItems()).size();
		remoteItemCount += (remote.getItemOnCursor() == null ? 0 : remote.getItemOnCursor().getType().equals(Material.AIR) ? 0 : 1);
		
		// If their current items + staked items + winnings is less than or equal to inv size, return true.
		return (remoteItemCount + items.size() <= INVENTORY_SIZE);
		
	}
	
}
