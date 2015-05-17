package com.defiancecraft.modules.staking.stakes;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.util.FailureLog;
import com.defiancecraft.modules.staking.util.FailureLog.Level;

/**
 * Represents a pair of players entering a stake.
 * <br><br>
 * Their respective stakes (i.e. items and tokens) are stored in this
 * class, and PlayerPair objects are generally passed between states.
 */
public class PlayerPair {

	private WeakReference<Player> alpha;
	private WeakReference<Player> beta;
	
	private UUID alphaUUID;
	private String alphaName;
	private UUID betaUUID;
	private String betaName;
	
	private List<ItemStack> alphaStake; // Alpha's staked items (starts off as null)
	private List<ItemStack> betaStake;  // Beta's staked items
	private int alphaTokens = 0;		// Alpha's staked token amount
	private int betaTokens = 0;			// Beta's staked token amounte
	private int kitChoice = 0;			// Their kit selection
	
	public PlayerPair(Player a, Player b) {
		this.alpha = new WeakReference<>(a);
		this.alphaName = a.getName();
		this.alphaUUID = a.getUniqueId();
		this.beta = new WeakReference<>(b);
		this.betaName = b.getName();
		this.betaUUID = b.getUniqueId();
	}
	
	public Player getAlpha() {
		return alpha.get();
	}

	public String getAlphaName() {
		return alphaName;
	}
	
	public List<ItemStack> getAlphaStake() {
		return this.alphaStake;
	}
	
	public int getAlphaTokens() {
		return this.alphaTokens;
	}
	
	public Player getBeta() {
		return beta.get();
	}
	
	public String getBetaName() {
		return betaName;
	}
	
	public List<ItemStack> getBetaStake() {
		return this.betaStake;
	}
	
	public int getBetaTokens() {
		return this.betaTokens;
	}
	
	public int getKitSelection() {
		return this.kitChoice;
	}
	
	public void setAlphaStake(List<ItemStack> alphaStake) {
		this.alphaStake = alphaStake;
	}
	
	public void setAlphaTokens(int tokens) {
		this.alphaTokens = tokens;
	}
	
	public void setBetaStake(List<ItemStack> betaStake) {
		this.betaStake = betaStake;
	}
	
	public void setBetaTokens(int tokens) {
		this.betaTokens = tokens;
	}
	
	public void setKitSelection(int kit) {
		this.kitChoice = kit;
	}
	
	/**
	 * Restores the items of both players in the pair.
	 */
	public void restoreItems() {
		
		// Check that alpha is online and not null; restore items if so
		if (getAlpha() == null || !getAlpha().isOnline())
			logFailure(alphaUUID, getAlphaStake());
		else if (getAlphaStake() != null && getAlphaStake().size() > 0)
			restoreItems(getAlpha(), getAlphaStake());
		
		// As above, for beta.
		if (getBeta() == null || !getBeta().isOnline())
			logFailure(betaUUID, getBetaStake());
		else if (getBetaStake() != null && getBetaStake().size() > 0)
			restoreItems(getBeta(), getBetaStake());
		
	}
	
	private void restoreItems(Player player, List<ItemStack> items) {
		
		// Drop items which failed
		Map<Integer, ItemStack> failed = player.getInventory().addItem(items.toArray(new ItemStack[]{}));
		
		for (ItemStack i : failed.values())
			player.getWorld().dropItem(player.getLocation(), i);
		
		// Notify player
		if (failed.size() > 0)
			player.sendMessage(Lang.get(Staking.NAME, Staking.LANG_INVENTORY_FULL));

		// Use Runnable because items may be restored during an inventory event (i.e. close)
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				player.updateInventory();
			}
		}.runTask(JavaPlugin.getPlugin(Staking.class));
		
	}
	
	private void logFailure(UUID uuid, List<ItemStack> items) {
		
		StringBuilder log = new StringBuilder();
		log.append("Failed to restore items for player!\n");
		log.append("UUID: ");
		log.append(uuid.toString());
		log.append("\nItems to restore:\n");
		for (ItemStack item : items) {
			log.append("- ");
			log.append(item.serialize().toString());
			log.append("\n");
		}
		
		FailureLog.write(Level.SEVERE, log.toString());
		
	}
	
	@Override
	public boolean equals(Object b) {
		
		if (!(b instanceof PlayerPair))
			return false;
		
		return alpha == ((PlayerPair)b).alpha && beta == ((PlayerPair)b).beta;
		
	}
	
}
