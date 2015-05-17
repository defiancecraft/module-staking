package com.defiancecraft.modules.staking.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.core.menu.MenuOption;
import com.defiancecraft.core.menu.impl.AbsoluteMenuLayout;
import com.defiancecraft.core.menu.impl.SimpleMenuOption;
import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.config.components.SerialItemStack;
import com.defiancecraft.modules.staking.menus.KitSelectionMenu.DualAcceptanceMenuOption.AcceptanceState;
import com.defiancecraft.modules.staking.menus.events.impl.KitSelectionChangedEvent;
import com.defiancecraft.modules.staking.util.ItemStackUtils;

public class KitSelectionMenu extends Menu {

	// Which kit either player has selected
	private int playerSelected = -1;
	private int remoteSelected = -1;
	
	// List of "acceptance options", so each can update upon changing
	protected List<DualAcceptanceMenuOption> acceptanceOptions = new ArrayList<DualAcceptanceMenuOption>();
	
	private KitSelectionMenuContainer container;
	
	protected KitSelectionMenu(KitSelectionMenuContainer container) {
		super(
			ChatColor.translateAlternateColorCodes('&', Staking.getConfiguration().kitSelectionMenu.title),
			KitSelectionMenu.getRows(Staking.getConfiguration().kitSelectionMenu.kits),
			new AbsoluteMenuLayout(KitSelectionMenu.getRows(Staking.getConfiguration().kitSelectionMenu.kits))
		);
		this.container = container;
		this.container.register(this, KitSelectionChangedEvent.class, this::onKitSelectionChanged);
		this.init();
	}
	
	/* ------------------------------
	 * 
	 * 		Overridden Methods
	 * 
	   ------------------------------ */
	
	@Override
	protected void addMenuOptions() {
		
		int i = 0;
		
		for (List<SerialItemStack> kit : Staking.getConfiguration().kitSelectionMenu.kits) {
		
			// Add acceptance option for this kit
			DualAcceptanceMenuOption option = new DualAcceptanceMenuOption(this, i);
			this.acceptanceOptions.add(option);
			this.addMenuOption(option, i);

			// Add options in the kit
			int j = 1;
			for (SerialItemStack kitItem : kit)
				this.addMenuOption(new SimpleMenuOption(kitItem.toItemStack(), (p) -> true), j++ * 9 + i);
			
			i++;
			
		}
		
	}
	
	/* ------------------------------
	 * 
	 * 		 Getters & Setters
	 * 
	   ------------------------------ */
	
	int getPlayerSelected() {
		return this.playerSelected;
	}
	
	void setPlayerSelected(int kit) {
		this.playerSelected = kit;
	}
	
	int getRemoteSelected() {
		return this.remoteSelected;
	}
	
	void setRemoteSelected(int kit) {
		this.remoteSelected = kit;
	}
	
	int getKit() {
		return this.playerSelected;
	}
	
	/* ------------------------------
	 * 
	 * 		 Internal Methods
	 * 
	   ------------------------------ */
	
	/**
	 * Updates the MenuOptions for each player's accept button,
	 * re-renders the inventory, and calls updateInventory() on
	 * all viewers. Should be used after playerSelected/remoteSelected
	 * are changed.
	 */
	private void updateAcceptanceOptions() {
		
		for (DualAcceptanceMenuOption option : acceptanceOptions)
			option.setAccepted(
				playerSelected == option.getIndex() && remoteSelected == option.getIndex() ? AcceptanceState.BOTH :
				playerSelected == option.getIndex() 									   ? AcceptanceState.PLAYER :
				remoteSelected == option.getIndex()										   ? AcceptanceState.REMOTE :
				AcceptanceState.NONE
			);
		
		this.rerender();
		this.updateInventory();
		
	}
	
	@SuppressWarnings("deprecation")
	private void updateInventory() {
		
		for (HumanEntity e : this.getInventory().getViewers())
			if (e instanceof Player) ((Player)e).updateInventory();
		
	}
	
	/* ------------------------------
	 * 
	 * 		       Events
	 * 
	   ------------------------------ */
	private void onKitSelectionChanged(KitSelectionChangedEvent e) {
		
		this.setRemoteSelected(e.getKit());
		this.updateAcceptanceOptions();
		
		if (e.getKit() == this.getPlayerSelected() && e.getKit() > -1)
			this.container.changeState();
		
	}
	
	/* ------------------------------
	 * 
	 * 		  Static Methods
	 * 
	   ------------------------------ */
	
	/**
	 * Gets the maximum number of rows needed, depending on the
	 * maximum size of a kit in the config (how many items need to
	 * be placed vertically)
	 * 
	 * @param kits
	 * @return
	 */
	private static int getRows(List<List<SerialItemStack>> kits) {
		
		if (kits.size() == 0)
			return 1;
		
		return kits.stream()
			.mapToInt((k) -> k.size())
			.max()
			.getAsInt() + 1; // Add 1 to account for acceptance blocks
	}
	
	/* ------------------------------
	 * 
	 * 		  Internal Classes
	 * 
	   ------------------------------ */
	
	static class DualAcceptanceMenuOption implements MenuOption {

		private static final ItemStack WOOL_RED = new ItemStack(Material.WOOL, 1, (short)0x0E);
		private static final ItemStack WOOL_YELLOW_PLAYER = new ItemStack(Material.WOOL, 1, (short)0x04);
		private static final ItemStack WOOL_YELLOW_REMOTE = new ItemStack(Material.WOOL, 1, (short)0x04);
		private static final ItemStack WOOL_GREEN = new ItemStack(Material.WOOL, 1, (short)0x05);
		
		static {
			ItemStackUtils.setDisplayName(WOOL_YELLOW_PLAYER, Lang.get(Staking.NAME, Staking.LANG_WAIT_FOR_REMOTE));
			ItemStackUtils.setDisplayName(WOOL_YELLOW_REMOTE, Lang.get(Staking.NAME, Staking.LANG_REMOTE_CHOSEN));
			ItemStackUtils.setDisplayName(WOOL_GREEN, Lang.get(Staking.NAME, Staking.LANG_ACCEPTED_KIT));
		}
		
		// How many players have accepted
		private AcceptanceState accepted = AcceptanceState.NONE;
		
		// Index of this option relative to other kits
		private int index = 0;
		
		// Menu of this option
		private final KitSelectionMenu menu;
		
		public DualAcceptanceMenuOption(KitSelectionMenu menu, int index) {
			this.index = index;
			this.menu = menu;
		}
		
		@Override
		public ItemStack getItemStack() {
			switch (accepted) {
			case NONE: // Format display name with the kit number (i.e. index + 1 so they become kit 1, kit 2, etc.)
				return ItemStackUtils.setDisplayName(WOOL_RED.clone(), Lang.get(Staking.NAME, Staking.LANG_CLICK_TO_CHOOSE, (index + 1) + ""));
			case PLAYER:
				return WOOL_YELLOW_PLAYER;
			case REMOTE:
				return WOOL_YELLOW_REMOTE;
			case BOTH:
				return WOOL_GREEN;
			default:
				return WOOL_RED;
			}
		}
		
		@Override
		public boolean onClick(Player p, InventoryClickEvent event) {
			
			boolean updated = false;
			
			switch (accepted) {
			case NONE:
				accepted = AcceptanceState.PLAYER;
				updated = true;
				break;
			case PLAYER:
				accepted = AcceptanceState.NONE;
				updated = true;
				break;
			case REMOTE:
				accepted = AcceptanceState.BOTH;
				updated = true;
			default:
				break;
			}
			
			if (updated) {
				
				if (accepted.equals(AcceptanceState.NONE))
					this.menu.setPlayerSelected(-1); // -1 = No selection
				else if (accepted.equals(AcceptanceState.PLAYER) || accepted.equals(AcceptanceState.BOTH))
					this.menu.setPlayerSelected(index);

				// Update visible options and emit an event so peer does the same
				this.menu.updateAcceptanceOptions();
				this.menu.container.emit(new KitSelectionChangedEvent(
						p,
						this.menu,
						accepted.equals(AcceptanceState.NONE) ? -1 : this.index, // Use -1 if none to show de-select. 
						this.accepted.equals(AcceptanceState.PLAYER)));
				
			}
			
			return true;
			
		}
		
		public int getIndex() {
			return index;
		}
		
		public void setAccepted(AcceptanceState state) {
			this.accepted = state;
		}
		
		static enum AcceptanceState {
			
			NONE,
			PLAYER,
			REMOTE,
			BOTH
			
		}
		
	}
	
}
