package org.inventivetalent.lasers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LaserRunnable extends BukkitRunnable {

	private final Lasers plugin;

	public static final List<Block> lasers       = new ArrayList<>();
	public static final List<Block> activeLasers = new ArrayList<>();

	public LaserRunnable(Lasers lasers) {
		this.plugin = lasers;
	}

	@Override
	public void run() {
		Iterator<Block> iterator = activeLasers.iterator();
		while (iterator.hasNext()) {
			if (Bukkit.getOnlinePlayers().isEmpty()) {
				break;
			}
			Block block = iterator.next();
			if (!block.getLocation().getChunk().isLoaded()) {
				continue;
			}
			boolean remove = false;
			if (block.getType() == Material.AIR) {
				remove = true;
			}
			if (this.plugin.items.LASER_EMITTER.getType() != block.getType()) {
				remove = true;
			}
			if (block.getBlockPower() <= 0) {
				remove = true;
			}
			Block destination = this.generateBeam(block);
			if (this.plugin.items.LASER_RECEIVER.getType() == destination.getType() && destination.hasMetadata("Lasers")) {
				if (remove) {
					if (destination.getState() instanceof InventoryHolder) {
						((InventoryHolder) destination.getState()).getInventory().clear();
					}
					iterator.remove();
					continue;
				} else {
					this.handleLaserInput(destination, block);
				}

			} else {
				if (block.hasMetadata("Laser_Receiver")) {
					List<MetadataValue> meta = block.getMetadata("Laser_Receiver");
					for (MetadataValue val : meta) {
						if (val.value() instanceof Block) {
							Block receiver = (Block) val.value();
							if (this.plugin.items.LASER_RECEIVER.getType() == receiver.getType()) {
								if (receiver.hasMetadata("Lasers")) {
									if (receiver.getState() instanceof InventoryHolder) {
										((InventoryHolder) receiver.getState()).getInventory().clear();
									}
								}
							}
						}
					}
				}
				if (remove) {
					iterator.remove();
					continue;
				}
			}
		}
	}

	protected void handleLaserInput(Block receiver, Block sender) {
		if (!receiver.hasMetadata("Laser_Sender")) {
			receiver.setMetadata("Laser_Sender", new FixedMetadataValue(this.plugin, sender));
		}
		if (!sender.hasMetadata("Laser_Receiver")) {
			sender.setMetadata("Laser_Receiver", new FixedMetadataValue(this.plugin, receiver));
		} else {
			boolean replace = false;
			Iterator<MetadataValue> iterator = sender.getMetadata("Laser_Receiver").iterator();
			while (iterator.hasNext()) {
				MetadataValue meta = iterator.next();
				if (meta.value() instanceof Block) {
					if (!receiver.equals(meta.value())) {
						replace = true;
						if (((Block) meta.value()).getState() instanceof InventoryHolder) {
							((InventoryHolder) ((Block) meta.value()).getState()).getInventory().clear();
						}
					}
				}
			}
			if (replace) {
				sender.setMetadata("Laser_Receiver", new FixedMetadataValue(this.plugin, receiver));
			}
		}

		Color color = DyeColor.RED.getColor();

		if (sender.hasMetadata("Laser_color")) {
			for (MetadataValue meta : sender.getMetadata("Laser_color")) {
				if (meta.value() instanceof Color) {
					color = (Color) meta.value();
					break;
				}
			}
		}

		if (sender.hasMetadata("Laser_dest_vector")) {
			for (MetadataValue meta : sender.getMetadata("Laser_dest_vector")) {
				if (meta.value() instanceof Vector) {
					sender = ((Vector) meta.value()).toLocation(sender.getWorld()).getBlock();
					break;
				}
			}
		}

		int xDiff = receiver.getX() - sender.getX();
		int yDiff = receiver.getY() - sender.getY();
		int zDiff = receiver.getZ() - sender.getZ();

		boolean canReceive = false;
		int distance = 0;

		//TODO: get rid of data
		if (xDiff == 0 && zDiff == 0) {
			if (yDiff > 0) {
				if (receiver.getData() == 0) {
					canReceive = true;
				}
				distance = yDiff;
			} else {
				if (receiver.getData() == 1) {
					canReceive = true;
				}
				distance = -yDiff;
			}
		} else if (xDiff == 0) {
			if (zDiff > 0) {
				if (receiver.getData() == 2) {
					canReceive = true;
				}
				distance = zDiff;
			} else {
				if (receiver.getData() == 3) {
					canReceive = true;
				}
				distance = -zDiff;
			}
		} else if (zDiff == 0) {
			if (xDiff > 0) {
				if (receiver.getData() == 4) {
					canReceive = true;
				}
				distance = xDiff;
			} else {
				if (receiver.getData() == 5) {
					canReceive = true;
				}
				distance = -xDiff;
			}
		}

		if (!canReceive) { return; }

		if (receiver.getState() instanceof InventoryHolder) {
			InventoryHolder holder = (InventoryHolder) receiver.getState();
			Inventory inv = holder.getInventory();
			inv.setMaxStackSize(64);
			int maxSize = inv.getSize() * inv.getMaxStackSize();
			int amount = 0;
			if ("distance".equals(this.plugin.receiverSignalMode)) {
				amount = maxSize * (16 - (distance - 1)) / 15;
			} else if ("color".equals(this.plugin.receiverSignalMode)) {
				amount = maxSize * this.plugin.colorHelper.colorToSignal(color) / 15;
			}
			amount = Math.min(amount, maxSize);
			if (amount <= 0) {
				inv.clear();
			}
			int curr = amount;
			for (int slot = 0; slot < inv.getSize(); slot++) {
				int c = Math.max(0, Math.min(curr, inv.getMaxStackSize()));
				if (c > 0) {
					inv.setItem(slot, new ItemStack(Material.WEB, c, (short) 1));
				} else {
					inv.setItem(slot, null);
					break;
				}
				curr -= inv.getMaxStackSize();
			}
		}

	}

	protected Block generateBeam(Block origin) {
		Vector start = origin.getLocation().toVector();
		Vector direction = new Vector();

		//TODO: get rid of data
		switch (origin.getData()) {
			case 8:
				start.setX(start.getX() + .5);
				start.setZ(start.getZ() + .5);

				direction.setY(-1);
				break;
			case 9:
				start.setX(start.getX() + .5);
				start.setZ(start.getZ() + .5);
				start.setY(start.getY() + 1);

				direction.setY(1);
				break;
			case 10:
				start.setX(start.getX() + .5);
				start.setY(start.getY() + .35);

				direction.setZ(-1);
				break;
			case 11:
				start.setX(start.getX() + .5);
				start.setZ(start.getZ() + 1);
				start.setY(start.getY() + .35);

				direction.setZ(1);
				break;
			case 12:
				start.setZ(start.getZ() + .5);
				start.setY(start.getY() + .35);

				direction.setX(-1);
				break;

			case 13:
				start.setX(start.getX() + 1);
				start.setZ(start.getZ() + .5);
				start.setY(start.getY() + .35);

				direction.setX(1);
				break;
			default:
				return origin;
		}

		Block destination = origin;

		Vector lastRedirect = start;
		Color currentColor = Color.RED;

		Vector lastVector = start;

		boolean inGlass = false;

		int length = this.plugin.laserLength;
		for (double d = 0; d < length; d += this.plugin.laserFrequency) {
			if (Bukkit.getOnlinePlayers().isEmpty()) {
				break;
			}
			Vector vec = direction.clone().multiply(d).add(lastRedirect);
			Location loc = vec.toLocation(origin.getWorld());

			destination = loc.getBlock();
			if (d > .5) {
				if (!this.canLaserPass(destination, loc)) {
					Block mirror = loc.clone().subtract(.5, 0, .5).getBlock();
					if ((mirror.getType() == Material.STANDING_BANNER//
							|| (mirror = loc.clone().subtract(0, 1, 0).getBlock()).getType() == Material.STANDING_BANNER//
							|| (mirror = destination).getType() == Material.STANDING_BANNER//
							|| (mirror = destination).getType() == Material.WALL_BANNER)//
							) {
						Vector rotation = this.plugin.bannerHelper.getAngle(mirror, direction.clone());

						mirror = loc.clone().getBlock();

						double xCorr = 0.5;
						double zCorr = 0.5;

						//TODO: get rid of data
						if (mirror.getType()==Material.WALL_BANNER) {// Hanging banner
							switch (mirror.getData()) {
								case 2:
									zCorr = 0.5;
									break;
								case 3:
									zCorr = 0.4;
									break;
								case 4:
									xCorr = 0.7;
									break;
								case 5:
									xCorr = 0.3;
									break;
								default:
									break;
							}
						}

						Vector v = mirror.getLocation().toVector().add(new Vector(xCorr, 0.35, zCorr));
						if (!lastRedirect.equals(v)) {
							lastRedirect = v;
							d = 0;
						}
						if (Double.isNaN(rotation.getX()) || Double.isNaN(rotation.getY()) || Double.isNaN(rotation.getZ())) {
							break;
						}
						direction.copy(rotation);
						if (this.plugin.colorMirror) {
							DyeColor color = this.plugin.bannerHelper.getColor(mirror);
							if (this.plugin.colorMix) {
								currentColor = currentColor.mixColors(color.getColor());
							} else {
								currentColor = color.getColor();
							}
						}
						continue;
					} else {
						break;
					}
				}
				if (this.plugin.colorGlassBlock && destination.getType() == Material.STAINED_GLASS || this.plugin.colorGlassPane && destination.getType() == Material.STAINED_GLASS_PANE) {
					if (!inGlass) {
						DyeColor color = DyeColor.getByWoolData(destination.getData());
						if (this.plugin.colorMix) {
							currentColor = currentColor.mixColors(color.getColor());
						} else {
							currentColor = color.getColor();
						}
					}
					inGlass = true;
				} else {
					inGlass = false;
				}
			}
			lastVector = vec;
			try {
				loc.getWorld().spawnParticle(
						Particle.REDSTONE,
						loc,
						0,
						particleColor(currentColor.getRed()),
						particleColor(currentColor.getGreen()),
						particleColor(currentColor.getBlue()),
						1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		origin.setMetadata("Laser_dest_vector", new FixedMetadataValue(this.plugin, lastVector));
		origin.setMetadata("Laser_color", new FixedMetadataValue(this.plugin, currentColor));
		return destination;
	}

	double particleColor(double value) {
		if (value <= 0) {
			value = -1;
		}
		return value / 255;
	}

	boolean canLaserPass(Block block, Location loc) {
		boolean passMaterial = false;
		if (!this.plugin.blockedBlocks) {
			passMaterial = true;
		} else {
			if (block.getType().isTransparent()) {
				passMaterial = true;
			}
			if (block.getType() == Material.GLASS) {
				passMaterial = true;
			}
			if (block.getType() == Material.THIN_GLASS) {
				passMaterial = true;
			}
			if (block.getType() == Material.STAINED_GLASS) {
				passMaterial = true;
			}
			if (block.getType() == Material.STAINED_GLASS_PANE) {
				passMaterial = true;
			}
			if (block.getType()==Material.BARRIER) {// Barriers
				passMaterial = true;
			}
			if (block.getTypeId() == 107 || block.getTypeId() >= 183 && block.getTypeId() <= 187) {// Fence gates  //TODO: update to Material & add new fence types
				if (block.getData() >= 4) {// Gate is open
					passMaterial = true;
				}
			}
		}
		if (loc.clone().subtract(0, 1, 0).getBlock().getTypeId() == 176) {
			passMaterial = false;
		}
		if (!passMaterial) { return false; }
		for (Entity ent : block.getWorld().getEntities()) {
			if (Bukkit.getOnlinePlayers().isEmpty()) { return false; }
			Location entLoc = ent.getLocation();
			if (entLoc.distanceSquared(loc) > 8) {
				continue;
			}

			if (Util.entityBoundingBoxContains(Util.getEntityBoundingBox(ent), loc.toVector())) {
				if (this.plugin.laserDamageEnabled) {
					if (ent instanceof Damageable) {
						if (this.plugin.laserDamageFire) {
							if (ent.getFireTicks() <= 0) {
								ent.setFireTicks(100);
							}
						}
						((Damageable) ent).damage(this.plugin.laserDamageAmount);
						((Damageable) ent).setMetadata("Laser_Damage", new FixedMetadataValue(this.plugin,this.plugin.laserDamageAmount));
					}
				}
				if (this.plugin.blockedEntities) { return false; }
			} else {
				if (ent instanceof Metadatable) {
					((Metadatable) ent).removeMetadata("Laser_Damage", this.plugin);
				}
			}
		}
		return true;
	}
}