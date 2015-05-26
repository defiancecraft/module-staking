package com.defiancecraft.modules.staking.stakes.states;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.core.api.Economy.InsufficientFundsException;
import com.defiancecraft.core.api.Economy.UserNotFoundException;
import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.config.components.ArenaConfig;
import com.defiancecraft.modules.staking.config.components.SerialPreciseLocation;
import com.defiancecraft.modules.staking.stakes.ArenaManager;
import com.defiancecraft.modules.staking.stakes.CancellationReason;
import com.defiancecraft.modules.staking.stakes.PlayerPair;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.defiancecraft.modules.staking.stakes.StakeState;
import com.defiancecraft.modules.staking.util.FailureLog;
import com.defiancecraft.modules.staking.util.ItemStackUtils;
import com.defiancecraft.modules.staking.util.Level;
import com.defiancecraft.modules.staking.util.RawMessaging;
import com.defiancecraft.modules.staking.util.StringBuilderOutputStream;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

public class StakingState implements StakeState {

	private PlayerPair pair;
	private StakeManager man;
	private boolean teleported = false;
	private boolean respawned = false;
	private BukkitTask countdown;
	private volatile boolean countingDown = false;
	
	// Store their inventories before giving them kits
	private ItemStack[] alphaInventoryArmor;
	private ItemStack[] alphaInventory;
	private ItemStack[] betaInventoryArmor;
	private ItemStack[] betaInventory;
	
	public StakingState(PlayerPair pair, StakeManager man) {
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

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		
		ArenaConfig arena = ArenaManager.getArena(pair);
		List<SerialPreciseLocation> spawns = new ArrayList<SerialPreciseLocation>(arena.spawns); // Clone to remove spawns from list
		Random spawnSelector = new Random();
		
		SerialPreciseLocation alphaSpawn = spawns.remove(spawnSelector.nextInt(spawns.size()));
		SerialPreciseLocation betaSpawn = spawns.remove(spawnSelector.nextInt(spawns.size()));
		
		// Teleport players
		teleported = true;
		getAlpha().teleport(alphaSpawn.toLocation());
		getBeta().teleport(betaSpawn.toLocation());
	
		// Give them health & food
		getAlpha().setHealth(getAlpha().getMaxHealth());
		getAlpha().setFoodLevel(20);
		getAlpha().setFireTicks(0);
		for (PotionEffect e : getAlpha().getActivePotionEffects())
			getAlpha().removePotionEffect(e.getType());
		getBeta().setHealth(getBeta().getMaxHealth());
		getBeta().setFoodLevel(20);
		getBeta().setFireTicks(0);
		for (PotionEffect e : getBeta().getActivePotionEffects())
			getBeta().removePotionEffect(e.getType());
		
		// Store inventories
		this.alphaInventoryArmor = getAlpha().getInventory().getArmorContents();
		this.alphaInventory = getAlpha().getInventory().getContents();
		this.betaInventoryArmor = getBeta().getInventory().getArmorContents();
		this.betaInventory = getBeta().getInventory().getContents();
		
		// Clear inventories
		getAlpha().getInventory().clear();
		getAlpha().getInventory().setArmorContents(null);
		getBeta().getInventory().clear();
		getBeta().getInventory().setArmorContents(null);
		
		// Give them their kits
		Staking.getConfiguration().kitSelectionMenu.kits.get(pair.getKitSelection())
			.stream()
			.map((item) -> item.toItemStack())
			.forEach((item) -> {
				
				if (ItemStackUtils.isHelmet(item)) {
					getAlpha().getInventory().setHelmet(item);
					getBeta().getInventory().setHelmet(item);
				} else if (ItemStackUtils.isChestplate(item)) {
					getAlpha().getInventory().setChestplate(item);
					getBeta().getInventory().setChestplate(item);
				} else if (ItemStackUtils.isLeggings(item)) {
					getAlpha().getInventory().setLeggings(item);
					getBeta().getInventory().setLeggings(item);
				} else if (ItemStackUtils.isBoots(item)) {
					getAlpha().getInventory().setBoots(item);
					getBeta().getInventory().setBoots(item);
				} else {
					getAlpha().getInventory().addItem(item);
					getBeta().getInventory().addItem(item);
				}
				
			});
		
		// Update inventories
		getAlpha().updateInventory();
		getBeta().updateInventory();
		
		// Begin countdown
		startCountdown();
		
	}

	@Override
	public void cancel(CancellationReason reason) {
		
		ArenaManager.deallocate(pair);
		
		if (reason.isAlpha())
			onWin(getBeta());
		else if (reason.isBeta())
			onWin(getAlpha());
		else
			onShutdown();
		
		if (this.countdown != null)
			this.countdown.cancel();
		
	}

	@Override
	public void destroy() {
		
		ArenaManager.deallocate(pair);
		
	}

	@Override
	public boolean isInArea(Location loc) {
		return teleported ?
					(respawned ?
						isAtSpawn(loc) :
						isInArena(loc)) :
					Staking.getConfiguration().stakingArea.toSelection().contains(loc);
	}
	
	// Checks if they're in the staking arena
	private boolean isInArena(Location loc) {
		
		ArenaConfig arena = ArenaManager.getArena(pair);
		if (arena == null)
			return false;
		
		return new CuboidSelection(
				Bukkit.getWorld(arena.pointA.world),
				arena.pointA.toLocation(),
				arena.pointB.toLocation())
			.contains(loc);
		
	}
	
	// Checks if they're at the spawn point
	private boolean isAtSpawn(Location loc) {
		
		return Staking.getConfiguration().respawnPoint.toLocation().distance(loc) < 1;
		
	}
	
	/**
	 * Checks whether the countdown is still taking place
	 * @return Whether the countdown is still taking place
	 */
	public boolean isCountingDown() {
		return this.countingDown;
	}
	
	/**
	 * Labels one player the winner of the stake, and rewards
	 * them with all of the staked goods. They are then teleported
	 * back to the defined spawn point in the config, and receive
	 * their inventories they had before starting the stake.
	 * 
	 * @param winner Whomever won the stake
	 */
	public void onWin(Player winner) {
		
		if (getAlpha() != winner && getBeta() != winner)
			throw new IllegalArgumentException("The winner must be a player within the stake!");
		
		Player alpha = getAlpha();
		Player beta  = getBeta();

		// Restore the non-staked items of alpha
		try {
			restoreItems(alpha, true);
		} catch (Exception e) {
			logFailure(e, true);
		}
		
		// Restore the non-staked items of beta
		try {
			restoreItems(beta, false);
		} catch (Exception e) {
			logFailure(e, false);
		}
		
		// Tell them how shit they were
		winner.sendMessage(Lang.get(Staking.NAME, Staking.LANG_WINRAR, (winner == alpha ? pair.getBetaTokens() : pair.getAlphaTokens()) + ""));
		if (winner == alpha && beta != null && beta.isOnline())
			beta.sendMessage(Lang.get(Staking.NAME, Staking.LANG_LOSER, winner.getName()));
		else if (winner == beta && alpha != null && alpha.isOnline())
			alpha.sendMessage(Lang.get(Staking.NAME, Staking.LANG_LOSER, winner.getName()));
		
		// Teleport them back to spawn
		respawned = true;
		Location spawn = Staking.getConfiguration().respawnPoint.toLocation();
		
		if (alpha != null && alpha.isOnline())
			alpha.teleport(spawn);
		if (beta != null && beta.isOnline())
			beta.teleport(spawn);
		
		// Give them the items that were staked by each other
		winner.getInventory().addItem(pair.getAlphaStake().toArray(new ItemStack[]{}))
			.values().stream()
			.forEach((item) -> spawn.getWorld().dropItem(spawn, item));
		winner.getInventory().addItem(pair.getBetaStake().toArray(new ItemStack[]{}))
			.values().stream()
			.forEach((item) -> spawn.getWorld().dropItem(spawn, item));
		
		// Deduct/give tokens
		int tokens = winner == alpha && pair.getBetaTokens() > 0 ? pair.getBetaTokens() :
					 winner == beta && pair.getAlphaTokens() > 0 ? pair.getAlphaTokens() :
						 												0;
		
		if (tokens > 0) {
			String loserName = (winner == alpha ? pair.getBetaName() : pair.getAlphaName());
			String winnerName = (winner == alpha ? pair.getAlphaName() : pair.getBetaName());
			try {
				Economy.withdraw(loserName, tokens);
				Economy.deposit(winnerName, tokens);
			} catch (UserNotFoundException | InsufficientFundsException e) {
				Bukkit.getLogger().warning("Error while trying to deduct " + tokens + " from " + loserName);
				e.printStackTrace();
			} catch (Exception e) {
				Bukkit.getLogger().warning("Error while trying to give/take tokens from " + loserName + ", likely DB related.");
				winner.sendMessage(Lang.get(Staking.NAME, Staking.LANG_RANDOM_FAILURE));
				e.printStackTrace();
			}
		}
		
		// Update inventories on next tick
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (alpha != null)
					alpha.updateInventory();
				if (beta != null)
					beta.updateInventory();
			}
		}.runTask(JavaPlugin.getPlugin(Staking.class));
		
		man.removeState(alpha, true);
		man.removeState(beta, true);
		
	}
	
	@SuppressWarnings("deprecation")
	public void onShutdown() {
		
		Player alpha = getAlpha();
		Player beta  = getBeta();

		// Restore the non-staked items of alpha
		try {
			restoreItems(alpha, true);
		} catch (Exception e) {
			logFailure(e, true);
		}
		
		// Restore the non-staked items of beta
		try {
			restoreItems(beta, false);
		} catch (Exception e) {
			logFailure(e, false);
		}
		
		respawned = true;
		Location spawn = Staking.getConfiguration().respawnPoint.toLocation();
		
		// Inform alpha and teleport, if online
		if (getAlpha() != null && getAlpha().isOnline()) {
			getAlpha().sendMessage(Lang.get(Staking.NAME, Staking.LANG_SHUTDOWN));
			getAlpha().teleport(spawn);
			getAlpha().getInventory().addItem(pair.getAlphaStake().toArray(new ItemStack[]{}))
				.values().stream()
				.forEach((item) -> spawn.getWorld().dropItem(spawn, item));
		}

		// Inform beta and teleport, if online
		if (getBeta() != null && getBeta().isOnline()) {
			getBeta().sendMessage(Lang.get(Staking.NAME, Staking.LANG_SHUTDOWN));
			getBeta().teleport(spawn);
			getBeta().getInventory().addItem(pair.getBetaStake().toArray(new ItemStack[]{}))
				.values().stream()
				.forEach((item) -> spawn.getWorld().dropItem(spawn, item));
		}
		
		// Note that as the tokens were not deducted due to this being a shutdown, no
		// tokens will need to be refunded in this method.
		
		// Update inventories (or try to..)
		if (alpha != null)
			alpha.updateInventory();
		if (beta != null)
			beta.updateInventory();
		
	}
	
	private void restoreItems(Player p, boolean alpha) {

		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.getInventory().clear();
		if ((alpha && alphaInventoryArmor != null) || (!alpha && betaInventoryArmor != null))
			p.getInventory().setArmorContents(alpha ? alphaInventoryArmor : betaInventoryArmor);
		if ((alpha && alphaInventory != null) || (!alpha && betaInventory != null))
			p.getInventory().setContents(alpha ? alphaInventory : betaInventory);
		
	}
	
	private void logFailure(Exception e, boolean alpha) {
		
		StringBuilder log = new StringBuilder();
		log.append("Failed to restore items after stake for ");
		log.append(alpha ? "alpha" : "beta");
		log.append(" (UUID ");
		log.append(alpha ? (getAlpha() != null ? getAlpha().getUniqueId().toString() : "unknown") : (getBeta() != null ? getBeta().getUniqueId().toString() : "unknown"));
		log.append(")\nItems to restore:");
		for (ItemStack item : (alpha ? alphaInventory : betaInventory))
			log.append("- " + (item.serialize().toString()) + "\n");
		for (ItemStack item : (alpha ? alphaInventoryArmor : betaInventoryArmor))
			log.append("- " + (item.serialize().toString()) + "\n");
		log.append("Exception: ");
		log.append(e.getMessage());
		log.append(" (");
		log.append(e.getClass().getName());
		log.append(")\nStack trace:\n");
		e.printStackTrace(new PrintStream(new StringBuilderOutputStream(log)));
		
		// Write to log
		FailureLog.write(Level.SEVERE, log.toString());
		
	}
	
	private void startCountdown() {
		
		this.countingDown = true;
		this.countdown = new CountdownTask(this).runTaskTimer(JavaPlugin.getPlugin(Staking.class), 0, 20); // Every second (it's a countdown...)
		
	}
	
	static class CountdownTask extends BukkitRunnable {
		
		private StakingState state;
		private int second = 5;
		
		public CountdownTask(StakingState state) {
			this.state = state;
		}
		
		public void run() {
			
			Player alpha = state.getAlpha();
			Player beta  = state.getBeta();
			String message = second > 0 ? Lang.get(Staking.NAME, Staking.LANG_TITLE_COUNTDOWN, second + "") : Lang.get(Staking.NAME, Staking.LANG_TITLE_GO);
			
			RawMessaging.sendTitle(alpha, message);
			RawMessaging.sendTitle(beta, message);
			
			second--;
			if (second < 0) {
				this.state.countingDown = false;
				this.cancel();
			}
			
		}
		
	}

}
