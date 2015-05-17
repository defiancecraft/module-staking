package com.defiancecraft.modules.staking.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.defiancecraft.modules.staking.config.components.ArenaConfig;
import com.defiancecraft.modules.staking.config.components.SerialLocation;
import com.defiancecraft.modules.staking.config.components.SerialSelection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

public class MainConfig {

	public ChooseItemsConfig chooseItemsMenu = new ChooseItemsConfig();
	public KitSelectionConfig kitSelectionMenu = new KitSelectionConfig();
	
	public SerialSelection stakingArea = new SerialSelection(new CuboidSelection(Bukkit.getWorld("world"), new Vector(0, 0, 0), new Vector(0, 0, 0)));
	public List<ArenaConfig> arenas = new ArrayList<ArenaConfig>();
	
	public SerialLocation respawnPoint = new SerialLocation();
	
}
