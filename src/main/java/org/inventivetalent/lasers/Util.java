package org.inventivetalent.lasers;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.inventivetalent.boundingbox.BoundingBox;
import org.inventivetalent.boundingbox.BoundingBoxAPI;

public class Util {

	public static boolean entityBoundingBoxContains(Entity entity, Vector point) {
		BoundingBox box = BoundingBoxAPI.getBoundingBox(entity);
		return box.contains(point.getX(), point.getY(), point.getZ());
	}

	public static  boolean isGlass(Material type) {
		return type.name().contains("GLASS");
	}

	public static  boolean isStainedGlass(Material type) {
		return type.name().contains("STAINED_GLASS");
	}

	public static boolean isBanner(Material type) {
		return type.name().contains("BANNER");
	}

	public static boolean isWallBanner(Material type) {
		return type.name().contains("WALL_BANNER");
	}

	public static boolean isStandingBanner(Material type){
		return isBanner(type) && !isWallBanner(type);
	}

	public static DyeColor getColorableBlockColor(Material type) {
		return getColorableBlockColor(type, DyeColor.BLACK);
	}

	public static  DyeColor getColorableBlockColor(Material type, DyeColor def) {
		String[]split = type.name().split("_");
		String name = split[0];
		if ("LIGHT".equals(name)) {// LIGHT_GRAY & LIGHT_BLUE
			name = name + "_" + split[1];
		}
		try {
			return DyeColor.valueOf(name);
		} catch (Exception e) {
			System.err.println("[Lasers] Failed to find color for " + type);
		}
		return def;
	}

}