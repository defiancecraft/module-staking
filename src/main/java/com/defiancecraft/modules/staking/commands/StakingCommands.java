package com.defiancecraft.modules.staking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.config.MainConfig;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.states.ArenaQueueState;
import com.defiancecraft.modules.staking.stakes.states.AwaitingAcceptState;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class StakingCommands {

	private StakeManager man;
	
	public StakingCommands(StakeManager man) {
		this.man = man;
	}
	
	@SuppressWarnings("deprecation")
	public boolean stake(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		Player player = (Player) sender;
		Player target;
		
		// Ensure they specified a target, and that the target is online
		if (args.length < 1) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_BAD_USAGE, "/stake <player>"));
			return true;
		} else if ((target = Bukkit.getPlayerExact(args[0])) == null) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_PLAYER_NOT_FOUND, args[0]));
			return true;
		} else if (target == player) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_STAKE_YOURSELF));
			return true;
		}
		
		MainConfig config = Staking.getConfiguration();
		Selection stakingArea;
		
		// If the staking area is not defined or can't be converted to selection
		if (config.stakingArea == null || (stakingArea = config.stakingArea.toSelection()) == null || config.arenas == null || config.arenas.size() == 0) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NOT_CONFIGURED));
			return true;
		}
		
		// Prevent if player or target are not in staking area & world
		if (!stakingArea.contains(player.getLocation()) || !player.getLocation().getWorld().getName().equalsIgnoreCase(config.stakingArea.world)) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NOT_IN_AREA, "You are"));
			return true;
		} else if (!stakingArea.contains(target.getLocation()) || !target.getWorld().getName().equalsIgnoreCase(config.stakingArea.world)) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NOT_IN_AREA, target.getName() + " is"));
			return true;
		}
		
		// Prevent stake if already staking
		if (man.hasState(player) && !(man.getState(player) instanceof AwaitingAcceptState)) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_ALREADY_STAKING));
		} else {
			// Otherwise, set their state!
			man.setState(player, new AwaitingAcceptState(player, target));
			man.runState(player);
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_SENT_REQUEST, target.getName()));
		}
	
		return true;		
		
	}
	
	@SuppressWarnings("deprecation")
	public boolean accept(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		Player player = (Player) sender;
		Player target;
		
		// If no player specified
		if (args.length < 1) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_BAD_USAGE, "/accept <player>"));
			
		// If target not online
		} else if ((target = Bukkit.getPlayerExact(args[0])) == null) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_PLAYER_NOT_FOUND, args[0]));
			
		// If no request to this player from them
		} else if (!man.hasState(target) || !(man.getState(target) instanceof AwaitingAcceptState) || man.getState(target).getBeta() != player) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NO_REQUEST, args[0]));
			
		// If they are already in a damn stake
		} else if (man.hasState(player) && !(man.getState(player) instanceof AwaitingAcceptState)) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_ALREADY_STAKING));
			
		} else {
			
			ArenaQueueState aq = new ArenaQueueState(man.getState(target).getAlpha(), man.getState(target).getBeta(), man);
			man.setState(target, aq);
			man.setState(player, aq);
			man.runState(aq);
			
		}
		
		return true;
		
	}

}
