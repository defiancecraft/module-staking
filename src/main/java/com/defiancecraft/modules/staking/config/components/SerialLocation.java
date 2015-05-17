package com.defiancecraft.modules.staking.config.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * A serialized equivalent of Bukkit's Location
 * @see org.bukkit.Location
 */
public class SerialLocation {
	
	public int x, y, z;
	public String world = "world";
	
	public SerialLocation() {}
	
	public SerialLocation(Location loc) {
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
		world = loc.getWorld().getName();
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	
}