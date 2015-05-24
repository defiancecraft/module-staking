package com.defiancecraft.modules.staking.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.Staking;
import com.defiancecraft.modules.staking.config.KitSelectionConfig;
import com.defiancecraft.modules.staking.config.MainConfig;
import com.defiancecraft.modules.staking.config.components.ArenaConfig;
import com.defiancecraft.modules.staking.config.components.SerialItemStack;
import com.defiancecraft.modules.staking.config.components.SerialLocation;
import com.defiancecraft.modules.staking.config.components.SerialPreciseLocation;
import com.defiancecraft.modules.staking.config.components.SerialSelection;
import com.defiancecraft.modules.staking.stakes.ArenaManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class AdminCommands {

	private static Map<UUID, ArenaConfig> arenaSetups = new HashMap<UUID, ArenaConfig>();

	private Staking plugin;
	
	public AdminCommands(Staking plugin) {
		this.plugin = plugin;
	}
	
	public boolean help(CommandSender sender, String[] args) {
		
		sender.sendMessage(ChatColor.BLUE + "Staking Help:\n" +
				ChatColor.AQUA + "/staking help\n" +
				ChatColor.AQUA + "/staking setarea - Sets staking area to WorldEdit selection\n" +
				ChatColor.AQUA + "/staking arena <a|ha|b|hb|spawn|hspawn|save>\n" + 
				ChatColor.AQUA + "/staking respawn - Sets the respawn point after stake is complete\n" +
				ChatColor.AQUA + "/staking addkit - Adds a kit using inventory contents");
		return true;
		
	}
	
	public boolean setArea(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
				|| !(Bukkit.getPluginManager().getPlugin("WorldEdit") instanceof WorldEditPlugin)) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NO_WORLDEDIT));
			return true;
		}
		
		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		Selection sel = worldEdit.getSelection((Player)sender);
		MainConfig config = Staking.getConfiguration();
		
		if (sel == null) {
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NO_SELECTION));
			return true;
		}
		
		config.stakingArea = new SerialSelection(sel);
		
		if (plugin.saveConfiguration(config))
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_SAVE_SUCCESS));
		else
			sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_SAVE_FAILURE));
		
		
		return true;
		
	}
	
	public boolean arena(CommandSender sender, String[] args) {
		
		if (args.length < 1 || !args[0].matches("^a|ha|b|hb|region|spawn|hspawn|save$") || !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.BLUE + "Usage:\n" +
				ChatColor.AQUA + "/staking arena a - Set point A to standing location\n" +
				ChatColor.AQUA + "/staking arena ha - Set point A to block on crosshair\n" +
				ChatColor.AQUA + "/staking arena b - Set point B to standing location\n" +
				ChatColor.AQUA + "/staking arena hb - Set point B to block on crosshair\n" +
				ChatColor.AQUA + "/staking arena region - Set boundaries to selected WorldEdit region\n" +
				ChatColor.AQUA + "/staking arena spawn - Add a spawn at standing location\n" +
				ChatColor.AQUA + "/staking arena hspawn - Add a spawn at crosshair block\n" + 
				ChatColor.AQUA + "/staking arena save - Save the arena to the configuration.");
				
			return true;
		}
		
		Player player = (Player)sender;
		UUID uuid = player.getUniqueId();
		boolean crosshair = args[0].toLowerCase().charAt(0) == 'h';
		
		// Point A command
		if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("ha")) {
			
			if (!arenaSetups.containsKey(uuid))
				arenaSetups.put(uuid, new ArenaConfig());
			
			arenaSetups.get(uuid).pointA = new SerialLocation(crosshair ? player.getTargetBlock((Set<Material>)null, 40).getLocation() : player.getLocation());
			player.sendMessage(ChatColor.GREEN + "Set point A to " + (crosshair ? "crosshair block" : "standing location") + "!");
				
		// Point B command
		} else if (args[0].equalsIgnoreCase("b") || args[0].equalsIgnoreCase("hb")) {
			
			if (!arenaSetups.containsKey(uuid))
				arenaSetups.put(uuid, new ArenaConfig());
			
			arenaSetups.get(uuid).pointB = new SerialLocation(crosshair ? player.getTargetBlock((Set<Material>)null, 40).getLocation() : player.getLocation());
			player.sendMessage(ChatColor.GREEN + "Set point B to " + (crosshair ? "crosshair block" : "standing location") + "!");

		// Region definition command
		} else if (args[0].equalsIgnoreCase("region")) {
			
			WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
			Selection sel = worldEdit.getSelection((Player)sender);
			
			if (sel == null) {
				sender.sendMessage(Lang.get(Staking.NAME, Staking.LANG_NO_SELECTION));
				return true;
			}
			
			if (!arenaSetups.containsKey(uuid))
				arenaSetups.put(uuid, new ArenaConfig());
			
			SerialLocation pointA = new SerialLocation(sel.getMinimumPoint());
			SerialLocation pointB = new SerialLocation(sel.getMaximumPoint());
			arenaSetups.get(uuid).pointA = pointA;
			arenaSetups.get(uuid).pointB = pointB;
			
			sender.sendMessage(ChatColor.GREEN + "Set points A and B to WorldEdit selection!");
			
		// Spawn definition command
		} else if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("hspawn")) {
			
			if (!arenaSetups.containsKey(uuid))
				arenaSetups.put(uuid, new ArenaConfig());
			if (arenaSetups.get(uuid).spawns == null)
				arenaSetups.get(uuid).spawns = new ArrayList<SerialPreciseLocation>();
			
			SerialPreciseLocation loc = new SerialPreciseLocation(crosshair ? player.getTargetBlock((Set<Material>)null, 40).getLocation() : player.getLocation());
			arenaSetups.get(uuid).spawns.add(loc);
			player.sendMessage(ChatColor.GREEN + "Defined spawn point at " + (crosshair ? "crosshair block" : "standing location") + "!");
			
		// Save command
		} else if (args[0].equalsIgnoreCase("save")) {
			
			if (!arenaSetups.containsKey(uuid))
				player.sendMessage(ChatColor.RED + "You haven't even started defining an arena.");
			else if (arenaSetups.get(uuid).pointA == null)
				player.sendMessage(ChatColor.RED + "Point A is not defined. Do /staking arena a, or /staking arena ha.");
			else if (arenaSetups.get(uuid).pointB == null)
				player.sendMessage(ChatColor.RED + "Point B is not defined. Do /staking arena b, or /staking arena hb.");
			else if (arenaSetups.get(uuid).spawns == null || arenaSetups.get(uuid).spawns.size() == 0)
				player.sendMessage(ChatColor.RED + "No spawns are defined. Do /staking arena spawn or /staking arena hspawn");
			else {

				MainConfig config = Staking.getConfiguration();
				config.arenas.add(arenaSetups.get(uuid));
				plugin.saveConfiguration(config);
				
				ArenaManager.addArena(arenaSetups.get(uuid));
				arenaSetups.remove(uuid);
				player.sendMessage(ChatColor.GREEN + "Saved arena successfully!");
				
			}
			
		}
	
		return true;
		
	}
	
	public boolean respawn(CommandSender sender, String[] args) {
		
		SerialLocation loc = new SerialLocation(((Player)sender).getLocation());
		MainConfig config = Staking.getConfiguration();
		config.respawnPoint = loc;
		plugin.saveConfiguration(config);
		
		sender.sendMessage(ChatColor.GREEN + "Saved respawn point!");
		return true;
		
	}
	
	public boolean addKit(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		List<SerialItemStack> kit = Arrays
			.stream(((Player)sender).getInventory().getContents())
			.filter((item) -> item != null)
			.map((item) -> new SerialItemStack(item))
			.collect(Collectors.toList());
		
		MainConfig config = Staking.getConfiguration();
		if (config.kitSelectionMenu == null)
			config.kitSelectionMenu = new KitSelectionConfig();
		
		// Wrap the list of kits in ArrayList, as by default it is immutable.
		config.kitSelectionMenu.kits = config.kitSelectionMenu.kits == null ? new ArrayList<>() : new ArrayList<>(config.kitSelectionMenu.kits);
		config.kitSelectionMenu.kits.add(kit);
		
		// Save config
		plugin.saveConfiguration(config);
		
		sender.sendMessage(ChatColor.GREEN + "Saved kit!");
		
		return true;
		
	}
	
}
