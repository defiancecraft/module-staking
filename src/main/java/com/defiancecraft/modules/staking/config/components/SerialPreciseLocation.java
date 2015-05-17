package com.defiancecraft.modules.staking.config.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerialPreciseLocation {

	public double x;
	public double y;
	public double z;
	public String world;
	public float pitch;
	public float yaw;
	
	public SerialPreciseLocation(Location loc) {
		x = loc.getX();
		y = loc.getY();
		z = loc.getZ();
		world = loc.getWorld().getName();
		pitch = loc.getPitch();
		yaw = loc.getYaw();
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
	}
	
}
