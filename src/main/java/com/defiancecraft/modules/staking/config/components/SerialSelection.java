package com.defiancecraft.modules.staking.config.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * A serialized equivalent of WorldEdit's Selection class
 * @see com.sk89q.worldedit.bukkit.selections.Selection
 */
public class SerialSelection {
	
	public List<SerialVector> points;
	public String world;
	
	/**
	 * Constructs a SerialSelection from a WorldEdit selection
	 * @param sel WorldEdit selection (cuboid or polygonal)
	 */
	public SerialSelection(Selection sel) {
		
		if (sel instanceof CuboidSelection)
			points = Arrays.asList(
				new SerialVector(((CuboidSelection) sel).getNativeMinimumPoint()),
				new SerialVector(((CuboidSelection) sel).getNativeMaximumPoint())
			);
		else if (sel instanceof Polygonal2DSelection) {
			
			List<SerialVector> points = new ArrayList<SerialVector>();
			Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
			
			for (int i = 0; i < polySel.getNativePoints().size(); i++) {
				
				BlockVector2D point = polySel.getNativePoints().get(i);
				
				if (i == polySel.getNativePoints().size() - 1)
					points.add(new SerialVector(
						point.getBlockX(),
						Integer.max(polySel.getNativeMinimumPoint().getBlockY(), polySel.getNativeMaximumPoint().getBlockY()),
						point.getBlockZ()
					));
				else
					points.add(new SerialVector(
						point.getBlockX(),
						Integer.min(polySel.getNativeMinimumPoint().getBlockY(), polySel.getNativeMaximumPoint().getBlockY()),
						point.getBlockZ()
					));
				
			}
			
			this.points = points;
			
		} else {
			throw new IllegalArgumentException("Not a cuboid or polygonal selection.");
		}
		
		this.world = sel.getWorld().getName();
				
	}
	
	/**
	 * Converts the SerialSelection to a WorldEdit selection,
	 * the type of which is dependent upon the number of points
	 * (less than 2 = null, 2 = cuboid, more = polygonal)
	 * @return Selection
	 */
	public Selection toSelection() {
		
		if (points.size() < 2)
			return null;
		else if (points.size() == 2)
			return toCuboidSelection();
		else
			return toPolygonal2DSelection();
		
	}
	
	/**
	 * Converts the selection to a CuboidSelection, if possible
	 * @return CuboidSelection
	 */
	public CuboidSelection toCuboidSelection() {
		
		if (points.size() < 2)
			throw new IllegalStateException("Selection does not contain 2 or more points.");
		
		return new CuboidSelection(Bukkit.getWorld(world), points.get(0).toVector(), points.get(1).toVector());
		
	}
	
	/**
	 * Converts the selection to a Polygonal2DSelection, if possible
	 * @return Polygonal2DSelection
	 */
	public Polygonal2DSelection toPolygonal2DSelection() {
		
		if (points.size() < 2)
			throw new IllegalStateException("Selection does not contain 2 or more points.");
		
		return new Polygonal2DSelection(
			Bukkit.getWorld(world),
			points.stream()
				.map((v) -> new BlockVector2D(v.toVector().toVector2D()))
				.collect(Collectors.toList()),
			(int) points.stream()
				.max((a, b) -> Double.compare(a.y, b.y))
				.get().y,
			(int) points.stream()
				.min((a, b) -> Double.compare(a.y, b.y))
				.get().y
		);
		
	}
	
}