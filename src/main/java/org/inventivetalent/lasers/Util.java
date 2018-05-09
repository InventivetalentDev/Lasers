package org.inventivetalent.lasers;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.lang.reflect.Method;

public class Util {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> entity                  = nmsClassResolver.resolveSilent("Entity");

	static Method getBoundingBoxMethod = new MethodResolver(entity).resolveSilent("getBoundingBox");

	public static Object getEntityBoundingBox(Entity ent) {
		try {
			Object handle = Minecraft.getHandle(ent);
			return getBoundingBoxMethod.invoke(handle);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean entityBoundingBoxContains(Object boundingBox, Vector point) {
		try {
			double minX = boundingBox.getClass().getDeclaredField("a").getDouble(boundingBox);
			double minY = boundingBox.getClass().getDeclaredField("b").getDouble(boundingBox);
			double minZ = boundingBox.getClass().getDeclaredField("c").getDouble(boundingBox);
			double maxX = boundingBox.getClass().getDeclaredField("d").getDouble(boundingBox);
			double maxY = boundingBox.getClass().getDeclaredField("e").getDouble(boundingBox);
			double maxZ = boundingBox.getClass().getDeclaredField("f").getDouble(boundingBox);

			if (point.getX() >= minX && point.getX() <= maxX) {
				if (point.getZ() >= minZ && point.getZ() <= maxZ) {
					if (point.getY() >= minY && point.getY() <= maxY) { return true; }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}