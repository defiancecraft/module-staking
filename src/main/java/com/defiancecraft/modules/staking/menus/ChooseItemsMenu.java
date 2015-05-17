package com.defiancecraft.modules.staking.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.core.menu.Menu;
import com.defiancecraft.core.menu.MenuOption;
import com.defiancecraft.core.menu.impl.AbsoluteMenuLayout;
import com.defiancecraft.core.menu.impl.SimpleMenuOption;
import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.config.ChooseItemsConfig;
import com.defiancecraft.modules.staking.config.components.SerialItemStack;
import com.defiancecraft.modules.staking.menus.events.impl.AcceptanceChangedEvent;
import com.defiancecraft.modules.staking.menus.events.impl.StakedItemsUpdateEvent;
import com.defiancecraft.modules.staking.menus.events.impl.TokenStakeUpdateEvent;
import com.defiancecraft.modules.staking.util.ItemStackUtils;

public class ChooseItemsMenu extends Menu {

	private ChooseItemsMenuContainer container;
	private TokenQuantityOption tokenQuantityOption;
	
	// Pieces of wool on both sides of screen
	private AcceptOption acceptOption;
	private AcceptOption remoteAcceptOption;
	
	// Owner of menu's tokens staked
	private int tokenStake = 0;
	
	// Peer's tokens staked
	private int tokenStakeRemote = 0;
	
	private int rows = 0;
	private Plugin plugin;
	
	public ChooseItemsMenu(ChooseItemsMenuContainer container, Plugin plugin) {
		super(
			ChatColor.translateAlternateColorCodes('&', Staking.getConfiguration().chooseItemsMenu.title),
			Staking.getConfiguration().chooseItemsMenu.rows,
			new AbsoluteMenuLayout(Staking.getConfiguration().chooseItemsMenu.rows)
		);
		this.container = container;
		this.rows = Staking.getConfiguration().chooseItemsMenu.rows;
		this.plugin = plugin;
		
		// Register events
		this.container.register(this, TokenStakeUpdateEvent.class, this::onTokenStakeUpdate);
		this.container.register(this, StakedItemsUpdateEvent.class, this::onStakedItemsUpdate);
		this.container.register(this, AcceptanceChangedEvent.class, this::onAcceptanceChanged);
		
		super.init();
	}

	/* ------------------------------
	 * 
	 * 		Overridden Methods
	 * 
	   ------------------------------ */
	
	@Override
	protected void addMenuOptions() {
		
		ChooseItemsConfig config = Staking.getConfiguration().chooseItemsMenu;
		this.rows = config.rows;
		
		MenuOption placeholder = new SimpleMenuOption(config.placeholder.toItemStack(), (p) -> true);
		
		// Add placeholder items down the middle
		for (int i = 4; i < rows * 9; i += 9)
			this.addMenuOption(placeholder, i);
		
		// Add token options to increment token amount
		this.addMenuOption(new TokenMenuOption(this, config.tokenValue1), rows * 9 - 9);
		this.addMenuOption(new TokenMenuOption(this, config.tokenValue2), rows * 9 - 8);
		this.addMenuOption(new TokenMenuOption(this, config.tokenValue3), rows * 9 - 7);
		
		// Make other slots placeable in user's half
		for (int i = 0; i < rows * 9; i++)
			if (isPlaceableSlot(i))
				this.addMenuOption(new PlaceableSlotOption(this), i);
		
		// Token quantity item
		this.tokenQuantityOption = new TokenQuantityOption(this);
		this.addMenuOption(tokenQuantityOption, rows * 9 - 6);
		
		// Accept option
		this.acceptOption = new AcceptOption(this, true);
		this.addMenuOption(acceptOption, rows * 9 - 15);
		
		// Remote accept option (their acceptance status)
		this.remoteAcceptOption = new AcceptOption(this, false);
		this.addMenuOption(remoteAcceptOption, rows * 9 - 10);
		
		
		// TODO add command to make arena
		
	}
	
	/* ------------------------------
	 * 
	 *		  Getters & Setters
	 * 
	   ------------------------------ */
	
	/**
	 * Gets a list of the player's items which they placed into
	 * the inventory. This should be used to restore them if the state
	 * is cancelled.
	 * 
	 * @return List of player's items
	 */
	public List<ItemStack> getPlayerItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		
		// Add to items if not air and in placeable slot
		for (int i = 0; i < rows * 9; i++)
			if (isPlaceableSlot(i) && this.getInventory().getContents()[i] != null)
				items.add(this.getInventory().getContents()[i]);
		
		return items;
	}
	
	/**
	 * Gets the plugin associated with this menu.
	 * @return Plugin
	 */
	Plugin getPlugin() {
		return this.plugin;
	}
	
	/**
	 * Gets the number of tokens staked by the remote player.
	 * @return Token stake by the other player
	 */
	int getRemoteTokens() {
		return this.tokenStakeRemote;
	}
	
	/**
	 * Gets the number of tokens staked by the viewer of this menu.
	 * @return Number of tokens
	 */
	int getTokens() {
		return this.tokenStake;
	}
	
	/**
	 * Sets the number of tokens staked by this player, and emits an
	 * event accordingly.
	 * 
	 * @param p Player who set the token amount
	 * @param amount New amount of tokens
	 */
	@SuppressWarnings("deprecation")
	void setTokens(Player p, int amount) {
		
		this.tokenStake = amount;
		// Cancel accept status
		this.cancelAccept(); // Re-rendered by updateTokenQuantity()
		this.updateTokenQuantity();
		
		// Emit event & update inventory
		this.container.emit(new TokenStakeUpdateEvent(p, this, this.tokenStake));
		p.updateInventory();
		
	}
	
	/**
	 * Sets the items staked by this player by adding a MenuOption (in the form
	 * of a PlaceableSlotOption) for each of the player's added items. This method
	 * then proceeds to emit an event to update the staked items for the partner,
	 * and updates the player's inventory.
	 * 
	 * @param p Player who changed the staked items
	 * @param items The new staked items (in inventory format, i.e. array of rows * 9 ItemStacks containing nulls for air)
	 */
	@SuppressWarnings("deprecation")
	void setStakedItems(Player p, ItemStack[] items) {
		
		// Add menu option if it is a placeable slot on their side
		// and is different from what was previously there.
		// MenuOptions must be used as rerender() ignores non-options.
		for (int i = 0; i < rows * 9; i++)
			if (i % 9 > 3) continue;
			else if (i >= (rows - 1) * 9) continue;
			else if (i == rows * 9 - 15) continue;
			else if (items[i] == this.getInventory().getContents()[i]) continue;
			else
				addMenuOption(new PlaceableSlotOption(this, items[i]), i);
		
		this.cancelAccept();
		this.rerender();
		
		// Emit event and update inventory.
		this.container.emit(new StakedItemsUpdateEvent(p, this, items));
		p.updateInventory();
		
	}
	
	/* ------------------------------
	 * 
	 * 		  Internal Methods
	 * 
	   ------------------------------ */
	
	/**
	 * Updates the token quantity item with the new amounts, which should have
	 * been modified using the setters or variables themselves. This does not
	 * modify these variables.
	 * <br><br>
	 * This method updates the name and lore of the token quantity option, re-adds
	 * it to the menu, and re-renders the menu (see {@link Menu#rerender()}).
	 * <br><br>
	 * <b>Players inventories should be updated after calling this method as they
	 * may not update</b>
	 */
	private void updateTokenQuantity() {
		
		this.tokenQuantityOption.updateAmounts();
		this.addMenuOption(tokenQuantityOption, rows * 9 - 6);
		this.rerender();
		
	}
	
	/**
	 * Updates the inventories of viewers
	 */
	@SuppressWarnings("deprecation")
	private void updateInventory() {
		
		for (HumanEntity e : this.getInventory().getViewers())
			if (e instanceof Player) ((Player)e).updateInventory();
		
	}
	
	/**
	 * Checks if a slot is a "placeable" slot (user may place items in it)
	 * @param i Raw slot ID
	 * @return Whether the slot is placeable
	 */
	private boolean isPlaceableSlot(int i) {
		
		return i % 9 <= 3
				&& i < rows * 9 - 15;
		
	}

	/**
	 * Resets the acceptance status of BOTH players
	 * without emitting an event, as this should be called
	 * upon receiving an event and when something (i.e. tokens
	 * or staked items changes). This does <b>not</b> re-render
	 * the menu.
	 */
	private void cancelAccept() {
		
		if (this.acceptOption.getAccepted())
			this.acceptOption.setAccepted(false);
		if (this.remoteAcceptOption.getAccepted())
			this.remoteAcceptOption.setAccepted(false);
		
	}
	
	/* ------------------------------
	 * 
	 * 			  Events
	 * 
	   ------------------------------ */
	
	private void onTokenStakeUpdate(TokenStakeUpdateEvent e) {
		 
		// Change the menu option for number of tokens
		this.tokenStakeRemote = e.getNewTokenAmount();
		this.cancelAccept(); // Re-rendered by updateTokenQuantity()
		this.updateTokenQuantity();
		this.updateInventory();
		
	}
	
	private void onStakedItemsUpdate(StakedItemsUpdateEvent e) {
		
		// Put items in their stake area in the inventory, if the item
		// is before placeholder, and before the potato item
		for (int i = 0; i < e.getNewItems().length; i++)
			if (isPlaceableSlot(i))
				addMenuOption(new SimpleMenuOption(e.getNewItems()[i], p -> true), i + 5);

		this.cancelAccept();
		this.rerender();
		this.updateInventory();
		
	}
	
	private void onAcceptanceChanged(AcceptanceChangedEvent e) {

		// Set the other block to green (accepted state)
		this.remoteAcceptOption.setAccepted(e.getAccepted());
		this.rerender();
		this.updateInventory();
		
		// If both have accepted, change the state
		if (this.remoteAcceptOption.getAccepted() && this.acceptOption.getAccepted())
			this.container.changeState();
		
	}
	
	/* ------------------------------
	 * 
	 *		   Intenal Classes
	 * 
	   ------------------------------ */
	
	/**
	 * Internal class representing a menu option which adds
	 * tokens to a user's stake. 
	 */
	static class TokenMenuOption implements MenuOption {

		private ItemStack stack;
		private int amount;
		private ChooseItemsMenu menu;
		
		public TokenMenuOption(ChooseItemsMenu menu, int amount) {
			
			SerialItemStack stackSerial = Staking.getConfiguration().chooseItemsMenu.tokenItem.clone();
			stackSerial.name = String.format(stackSerial.name, amount);
			
			this.stack = stackSerial.toItemStack();
			this.amount = amount;
			this.menu = menu;
			
		}
		
		@Override
		public ItemStack getItemStack() {
			return this.stack;
		}

		@Override
		public boolean onClick(Player p, InventoryClickEvent event) {
			menu.setTokens(p, menu.getTokens() + amount);
			return true;
		}
		
	}
	
	/**
	 * Internal class representing a slot in which the user may
	 * place an item. When clicked, the updateStake method is called
	 * on the parent Menu.
	 */
	static class PlaceableSlotOption implements MenuOption {
		
		private static final ItemStack AIR = new ItemStack(Material.AIR, 1);
		private ChooseItemsMenu menu;
		private ItemStack stack;
		
		public PlaceableSlotOption(ChooseItemsMenu menu) {
			this(menu, AIR);
		}
		
		public PlaceableSlotOption(ChooseItemsMenu menu, ItemStack stack) {
			this.menu = menu;
			this.stack = stack;
		}
		
		@Override
		public ItemStack getItemStack() {
			return stack;
		}
		
		@Override
		public boolean onClick(Player p, InventoryClickEvent event) {
			
			if (!event.getClick().equals(ClickType.LEFT)) { // TODO use lang
				p.sendMessage(ChatColor.RED + "Please left click!"); // TODO send this?
				return true;
			}
			
			this.stack = event.getCursor() == null ? null :
						 event.getCursor().getAmount() == 0 ? null :
					     event.getCursor();
			
			// Run on next tick because inventory will not be updated.
			new BukkitRunnable() {
				public void run() {
					menu.setStakedItems(p, event.getInventory().getContents());
				}
			}.runTask(this.menu.getPlugin());
			return false;
			
		}
		
	}
	
	/**
	 * Internal class representing a MenuOption with the staked quantity
	 * of tokens.
	 */
	static class TokenQuantityOption implements MenuOption {

		private ChooseItemsMenu menu;
		private ItemStack stack;
		
		public TokenQuantityOption(ChooseItemsMenu menu) {
			this.menu = menu;
			this.updateAmounts();
		}
		
		@Override
		public ItemStack getItemStack() {
			return stack;
		}

		@Override
		public boolean onClick(Player p, InventoryClickEvent event) {
			menu.setTokens(p, 0);
			return true;
		}
		
		public void updateAmounts() {
			
			SerialItemStack stack = Staking.getConfiguration().chooseItemsMenu.stakedTokensItem.clone();
			stack.name = String.format(stack.name, menu.getTokens());
		
			List<String> theSecondLore = new ArrayList<String>();
			for (String lore : stack.lore)
				theSecondLore.add(String.format(lore, menu.getRemoteTokens()));
			
			stack.lore = theSecondLore;
			
			this.stack = stack.toItemStack();
			
		}
		
	}
	
	static class AcceptOption implements MenuOption {

		private static final ItemStack WOOL_RED_PLAYER = new ItemStack(Material.WOOL, 1, (short)0x0E);
		private static final ItemStack WOOL_RED_REMOTE = new ItemStack(Material.WOOL, 1, (short)0x0E);
		private static final ItemStack WOOL_GREEN_PLAYER = new ItemStack(Material.WOOL, 1, (short)0x05);
		private static final ItemStack WOOL_GREEN_REMOTE = new ItemStack(Material.WOOL, 1, (short)0x05);
		
		static {
			ItemStackUtils.setDisplayName(WOOL_RED_PLAYER, Lang.get(Staking.NAME, Staking.LANG_CLICK_TO_ACCEPT));
			ItemStackUtils.setDisplayName(WOOL_RED_REMOTE, Lang.get(Staking.NAME, Staking.LANG_REMOTE_NOT_ACCEPTED));
			ItemStackUtils.setDisplayName(WOOL_GREEN_PLAYER, Lang.get(Staking.NAME, Staking.LANG_CLICK_TO_UNACCEPT));
			ItemStackUtils.setDisplayName(WOOL_GREEN_REMOTE, Lang.get(Staking.NAME, Staking.LANG_REMOTE_ACCEPTED));
		}
		
		private ChooseItemsMenu menu;
		private boolean accepted = false;
		private boolean clickable;
		
		public AcceptOption(ChooseItemsMenu menu, boolean clickable) {
			this.menu = menu;
			this.clickable = clickable;
		}
		
		@Override
		public ItemStack getItemStack() {
			return accepted ? (clickable ? WOOL_GREEN_PLAYER : WOOL_GREEN_REMOTE)
						    : (clickable ? WOOL_RED_PLAYER : WOOL_RED_REMOTE);
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean onClick(Player p, InventoryClickEvent event) {
			
			if (!clickable)
				return true;
			
			// Check that the player has the required amount of tokens
			try {
				if (Economy.getBalance(p.getUniqueId()) < menu.getTokens()) {
					p.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NOT_ENOUGH_MONEY));
					return true;
				}
			} catch (Exception e) {
				p.sendMessage(ChatColor.RED + "An internal error occurred.");
				e.printStackTrace();
				return true;
			}
			
			// Check that the other player has inventory space
			if (!menu.container.remoteHasInventorySpace(p, menu.getPlayerItems())) {
				p.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NOT_ENOUGH_SPACE));
				return true;
			}
			
			setAccepted(!accepted);
			
			menu.rerender();
			p.updateInventory();
			menu.container.emit(new AcceptanceChangedEvent(p, menu, accepted));
			
			return true;
		}
		
		public boolean getAccepted() {
			return accepted;
		}
		
		public void setAccepted(boolean accepted) {
			this.accepted = accepted;
		}
		
	}
	
}
