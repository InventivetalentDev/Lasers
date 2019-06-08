package org.inventivetalent.lasers;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.inventivetalent.boundingbox.BoundingBox;
import org.inventivetalent.boundingbox.BoundingBoxAPI;

public class Util {

	public static boolean entityBoundingBoxContains(Entity entity, Vector point) {
		BoundingBox box = BoundingBoxAPI.getBoundingBox(entity);
		return box.contains(point.getX(), point.getY(), point.getZ());
	}

}