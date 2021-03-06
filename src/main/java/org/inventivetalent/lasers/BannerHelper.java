package org.inventivetalent.lasers;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

import static org.inventivetalent.lasers.Util.isWallBanner;

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

	public Vector getBannerAngleForData(Material type, BlockData data) {
		Vector bannerAngle = new Vector();

		// this took way too long to figure out.
		boolean isWallBanner = isWallBanner(type);

		if (!isWallBanner) {
			switch (((Rotatable)data).getRotation()) {
				case SOUTH:
					bannerAngle.setZ(1);
					break;
				case SOUTH_SOUTH_WEST:
					bannerAngle.setZ(DEG_45);
					bannerAngle.setX(-DEG_22_5);
					break;
				case SOUTH_WEST:
					bannerAngle.setX(-DEG_90);
					bannerAngle.setZ(DEG_90);
					break;
				case WEST_SOUTH_WEST:
					bannerAngle.setX(-DEG_45);
					bannerAngle.setZ(DEG_22_5);
					break;

				case WEST:
					bannerAngle.setX(-1);
					break;
				case WEST_NORTH_WEST:
					bannerAngle.setX(-DEG_45);
					bannerAngle.setZ(-DEG_22_5);
					break;
				case NORTH_WEST:
					bannerAngle.setX(-DEG_90);
					bannerAngle.setZ(-DEG_90);
					break;
				case NORTH_NORTH_WEST:
					bannerAngle.setZ(-DEG_45);
					bannerAngle.setX(-DEG_22_5);
					break;

				case NORTH:
					bannerAngle.setZ(-1);
					break;
				case NORTH_NORTH_EAST:
					bannerAngle.setZ(-DEG_45);
					bannerAngle.setX(DEG_22_5);
					break;
				case NORTH_EAST:
					bannerAngle.setZ(-DEG_90);
					bannerAngle.setX(DEG_90);
					break;
				case EAST_NORTH_EAST:
					bannerAngle.setX(DEG_45);
					bannerAngle.setZ(-DEG_22_5);
					break;

				case EAST:
					bannerAngle.setX(1);
					break;
				case EAST_SOUTH_EAST:
					bannerAngle.setZ(DEG_22_5);
					bannerAngle.setX(DEG_45);
					break;
				case SOUTH_EAST:
					bannerAngle.setZ(DEG_90);
					bannerAngle.setX(DEG_90);
					break;
				case SOUTH_SOUTH_EAST:
					bannerAngle.setZ(DEG_45);
					bannerAngle.setX(DEG_22_5);
					break;

				default:
					break;
			}
		}
		if (isWallBanner) {
			switch (((Directional)data).getFacing()) {
				case SOUTH:
					bannerAngle.setZ(-1);
					break;
				case NORTH:
					bannerAngle.setZ(1);
					break;
				case EAST:
					bannerAngle.setX(-1);
					break;
				case WEST:
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


		Vector bannerAngle = this.getBannerAngleForData(banner.getType(), banner.getBlockData());// TODO: remove data - but the banner block doesn't seem to have a getter for rotation

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

	public BlockFace getBannerAngleForPower(Block banner) {
		BlockData blockData = banner.getBlockData();
		if (!(blockData instanceof  Rotatable)) { return BlockFace.SOUTH; }
		Block base = null;
		if ((base = banner.getRelative(BlockFace.DOWN)).isEmpty() || base.getType() == Material.AIR) { return BlockFace.SOUTH; }
		if (base.getBlockPower() <= 0) { return BlockFace.SOUTH; }

		BlockData s = base.getRelative(BlockFace.SOUTH).getBlockData();
		BlockData w = base.getRelative(BlockFace.WEST).getBlockData();
		BlockData n = base.getRelative(BlockFace.NORTH).getBlockData();
		BlockData e = base.getRelative(BlockFace.EAST).getBlockData();

		//TODO: get rid of data
		byte powerS = (byte) ((s instanceof AnaloguePowerable) ? ((AnaloguePowerable) s).getPower() : 0);
		byte powerW = (byte) ((w instanceof AnaloguePowerable) ?((AnaloguePowerable) w).getPower() : 0);
		byte powerN = (byte) ((n instanceof AnaloguePowerable) ? ((AnaloguePowerable) n).getPower() : 0);
		byte powerE = (byte) ((e instanceof AnaloguePowerable) ? ((AnaloguePowerable) e).getPower() : 0);

		byte powerA = 0;
		byte powerB = 0;

		byte data = 0;

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

		return dataToRotation(data);
	}

	public BlockFace dataToRotation(byte data) {
		switch (data) {
			case 0:
			default:
				return BlockFace.SOUTH;
			case 1:
				return BlockFace.SOUTH_SOUTH_WEST;
			case 2:
				return BlockFace.SOUTH_WEST;
			case 3:
				return BlockFace.WEST_SOUTH_WEST;
			case 4:
				return BlockFace.WEST;
			case 5:
				return BlockFace.WEST_NORTH_WEST;
			case 6:
				return BlockFace.NORTH_WEST;
			case 7:
				return BlockFace.NORTH_NORTH_WEST;
			case 8:
				return BlockFace.NORTH;
			case 9:
				return BlockFace.NORTH_NORTH_EAST;
			case 10:
				return BlockFace.NORTH_EAST;
			case 11:
				return BlockFace.EAST_NORTH_EAST;
			case 12:
				return BlockFace.EAST;
			case 13:
				return BlockFace.EAST_SOUTH_EAST;
			case 14:
				return BlockFace.SOUTH_EAST;
			case 15:
				return BlockFace.SOUTH_SOUTH_EAST;
		}
	}

}