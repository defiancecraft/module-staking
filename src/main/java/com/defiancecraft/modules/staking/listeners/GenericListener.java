package com.defiancecraft.modules.staking.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class GenericListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent e) {

		if (e.isCancelled()
				|| e.getFrom().getBlock().equals(e.getTo().getBlock())
				|| !e.getTo().getWorld().getName().equalsIgnoreCase(Staking.getConfiguration().stakingArea.world))
			return;
		
		Selection sel = Staking.getConfiguration().stakingArea.toSelection();
		boolean fromInArena = sel.contains(e.getFrom());
		boolean toInArena   = sel.contains(e.getTo());
		
		if (fromInArena == toInArena)
			return;
		else if (fromInArena)
			e.getPlayer().sendMessage(Lang.get(Staking.NAME, Staking.LANG_LEFT_AREA));
		else
			e.getPlayer().sendMessage(Lang.get(Staking.NAME, Staking.LANG_ENTERED_AREA));

	}
	
}
