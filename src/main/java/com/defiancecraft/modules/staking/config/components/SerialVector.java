package com.defiancecraft.modules.staking.config.components;

import com.sk89q.worldedit.Vector;

/**
 * A serialized equivalent of WorldEdit's Vecotr
 * @see com.sk89q.worldedit.Vector
 */
public class SerialVector {
	
	public double x, y, z;
	
	public SerialVector(Vector v) {
		this(v.getX(), v.getY(), v.getZ());
	}
	
	public SerialVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Converts the SerialVector to a WorldEdit vector
	 * @return Vector
	 */
	public Vector toVector() {
		return new Vector(x, y, z);
	}
	
}