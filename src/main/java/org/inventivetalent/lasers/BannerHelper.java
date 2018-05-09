package org.inventivetalent.lasers;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class BannerHelper {

	private static final double DEG_90   = Math.toRadians(90.0D);
	private static final double DEG_45   = Math.toRadians(45.0D);
	private static final double DEG_22_5 = Math.toRadians(45.0D / 2.0D);

	private final Lasers plugin;

	public BannerHelper(Lasers lasers) {
		this.plugin = lasers;
	}

	public DyeColor getColor(Block banner) {
		if (banner.getState() instanceof Banner) { return ((Banner) banner.getState()).getBaseColor(); }
		return DyeColor.RED;
	}

	public Vector getBannerAngleForData(Material type, byte data) {
		Vector bannerAngle = new Vector();

		if (type == Material.STANDING_BANNER) {
			switch (data) {
				case 0:
					bannerAngle.setZ(1);
					break;
				case 1:
					bannerAngle.setZ(DEG_45);
					bannerAngle.setX(-DEG_22_5);
					break;
				case 2:
					bannerAngle.setX(-DEG_90);
					bannerAngle.setZ(DEG_90);
					break;
				case 3:
					bannerAngle.setX(-DEG_45);
					bannerAngle.setZ(DEG_22_5);
					break;

				case 4:
					bannerAngle.setX(-1);
					break;
				case 5:
					bannerAngle.setX(-DEG_45);
					bannerAngle.setZ(-DEG_22_5);
					break;
				case 6:
					bannerAngle.setX(-DEG_90);
					bannerAngle.setZ(-DEG_90);
					break;
				case 7:
					bannerAngle.setZ(-DEG_45);
					bannerAngle.setX(-DEG_22_5);
					break;

				case 8:
					bannerAngle.setZ(-1);
					break;
				case 9:
					bannerAngle.setZ(-DEG_45);
					bannerAngle.setX(DEG_22_5);
					break;
				case 10:
					bannerAngle.setZ(-DEG_90);
					bannerAngle.setX(DEG_90);
					break;
				case 11:
					bannerAngle.setX(DEG_45);
					bannerAngle.setZ(-DEG_22_5);
					break;

				case 12:
					bannerAngle.setX(1);
					break;
				case 13:
					bannerAngle.setZ(DEG_22_5);
					bannerAngle.setX(DEG_45);
					break;
				case 14:
					bannerAngle.setZ(DEG_90);
					bannerAngle.setX(DEG_90);
					break;
				case 15:
					bannerAngle.setZ(DEG_45);
					bannerAngle.setX(DEG_22_5);
					break;

				default:
					break;
			}
		}
		if (type == Material.WALL_BANNER) {
			switch (data) {
				case 2:
					bannerAngle.setZ(-1);
					break;
				case 3:
					bannerAngle.setZ(1);
					break;
				case 4:
					bannerAngle.setX(-1);
					break;
				case 5:
					bannerAngle.setX(1);
					break;

				default:
					break;
			}
		}

		return bannerAngle;
	}

	public Vector getAngle(Block banner, Vector laserAngle) {
		Vector vector = new Vector();

		Vector bannerAngle = this.getBannerAngleForData(banner.getType(), banner.getData());// TODO: remove data - but the banner block doesn't seem to have a getter for rotation

		bannerAngle = bannerAngle.normalize();

		double dot = laserAngle.clone().dot(bannerAngle);

		if (dot == 0) { return vector; }
		if (!this.plugin.mirrorsDouble && dot > 0) { return vector; }

		// http://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector
		double perp = 2.0D * dot;
		vector = laserAngle.clone().subtract(bannerAngle.clone().multiply(perp));

		vector = vector.lengthSquared() == 0.0 ? vector : vector.normalize();
		return vector;
	}

	public byte getBannerAngleForPower(Block banner) {
		byte data = banner.getData();
		if (banner.getType() != Material.STANDING_BANNER) { return data; }
		Block base = null;
		if ((base = banner.getRelative(BlockFace.DOWN)) == null || base.getType() == Material.AIR) { return data; }
		if (base.getBlockPower() <= 0) { return 0; }

		byte powerS = base.getRelative(BlockFace.SOUTH).getData();
		byte powerW = base.getRelative(BlockFace.WEST).getData();
		byte powerN = base.getRelative(BlockFace.NORTH).getData();
		byte powerE = base.getRelative(BlockFace.EAST).getData();

		byte powerA = 0;
		byte powerB = 0;

		byte side = 0;
		if (powerS >= 1 && powerN == 0 && powerE == 0) {
			side = 0;
			powerA = powerS;
		} else if (powerW >= 1 && powerS == 0 && powerE == 0) {
			side = 4;
			powerA = powerW;
		} else if (powerN >= 1 && powerS == 0 && powerW == 0) {
			side = 8;
			powerA = powerN;
		} else if (powerE >= 1 && powerW == 0 && powerN == 0) {
			side = 12;
			powerA = powerE;
		}

		if ("side".equals(plugin.mirrorRotatorMode)) {
			byte nextSide = (byte) (side + 4);
			if (nextSide > 12) {
				nextSide = 0;
			}
			if (nextSide == 0) {
				powerB = powerS;
			}
			if (nextSide == 4) {
				powerB = powerW;
			}
			if (nextSide == 8) {
				powerB = powerN;
			}
			if (nextSide == 12) {
				powerB = powerE;
			}

			byte power = (byte) (powerA == powerB ? 2 : powerB > 0 && powerA > powerB ? 1 : powerA > 0 && powerB > powerA ? 3 : 0);

			data = (byte) (side + power);
			if (data > 15) {
				data = (byte) (data - 15);
			}
		}
		if ("strength".equals(plugin.mirrorRotatorMode)) {
			data = (byte) base.getBlockPower();
		}

		return data;
	}
}