package com.defiancecraft.modules.staking.menus;

import org.bukkit.entity.Player;

import com.defiancecraft.modules.staking.menus.events.impl.BasicContainer;
import com.defiancecraft.modules.staking.stakes.PlayerPair;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.states.StakingState;

public class KitSelectionMenuContainer extends BasicContainer {

	private KitSelectionMenu menuAlpha;
	private KitSelectionMenu menuBeta;
	private PlayerPair pair;
	private StakeManager man;
	
	public KitSelectionMenuContainer(PlayerPair pair, StakeManager man) {
		this.menuAlpha = new KitSelectionMenu(this);
		this.menuBeta  = new KitSelectionMenu(this);
		this.man 	   = man;
		this.pair      = pair;
	}
	
	public void showMenu() {
		this.menuAlpha.openMenu(pair.getAlpha());
		this.menuBeta.openMenu(pair.getBeta());
	}
	
	public void closeMenu() {
		this.menuAlpha.closeMenu(pair.getAlpha());
		this.menuBeta.closeMenu(pair.getBeta());
	}
	
	void changeState() {
		
		pair.setKitSelection(menuAlpha.getKit());
		
		Player alpha = pair.getAlpha();
		Player beta  = pair.getBeta();
		StakingState state = new StakingState(pair, man);
		man.setState(alpha, state);
		man.setState(beta, state);
		man.runState(state);
		
	}

}
