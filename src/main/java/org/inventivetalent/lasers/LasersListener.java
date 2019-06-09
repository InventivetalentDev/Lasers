package org.inventivetalent.lasers;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.data.Rotatable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import static org.inventivetalent.lasers.Util.isBanner;

public class LasersListener implements Listener {

	private final Lasers plugin;

	public LasersListener(Lasers lasers) {
		this.plugin = lasers;
	}

	@EventHandler
	public void on(CraftItemEvent event) {
		if (event.getInventory() != null) {
			final CraftingInventory inv = event.getInventory();
			if (inv.getSize() == 10) {
				if (event.getRecipe().getResult().equals(plugin.items.LASER_EMITTER) || event.getRecipe().getResult().equals(plugin.items.LASER_RECEIVER)) {
					ItemStack crystal = inv.getMatrix()[4];
					if (!plugin.items.LASER_CRYSTAL.isSimilar(crystal)) {
						inv.setResult(new ItemStack(Material.AIR));
						event.setCancelled(true);
						event.setResult(Event.Result.DENY);
					}
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerInteractEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getClickedBlock() == null) { return; }
		boolean isRotator=false;
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (plugin.items.LASER_EMITTER.getType() == event.getClickedBlock().getType()
					|| plugin.items.LASER_RECEIVER.getType() == event.getClickedBlock().getType()
					|| plugin.items.MIRROR_ROTATOR.getType() == event.getClickedBlock().getType()) {
				if (event.getClickedBlock().hasMetadata("Lasers")) {
					if (plugin.items.MIRROR_ROTATOR.getType() == event.getClickedBlock().getType() && event.getItem() != null && isBanner(event.getItem().getType()) && event.getPlayer().isSneaking()) {// sneaking, because you can't place blocks on dispensers otherwise
						event.setUseInteractedBlock(Event.Result.DEFAULT);
						event.setUseItemInHand(Event.Result.DEFAULT);
					}else {
						event.setUseInteractedBlock(Event.Result.DENY);
						event.setUseItemInHand(Event.Result.ALLOW);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockPlaceEvent event) {
		if (event.isCancelled() || !event.canBuild()) { return; }
		if (plugin.items.LASER_CRYSTAL.equals(event.getItemInHand())) {
			event.setBuild(false);
			event.setCancelled(true);
			return;
		}
		if (event.getBlock() == null || event.getBlock().getType() == Material.AIR) { return; }
		if (plugin.items.LASER_EMITTER.isSimilar(event.getItemInHand())) {
			event.getBlock().setMetadata("Lasers", new FixedMetadataValue(this.plugin, plugin.items.LASER_EMITTER));
			LaserRunnable.lasers.add(event.getBlock());
			if (event.getBlock().getState() instanceof InventoryHolder) {
				((InventoryHolder) event.getBlock().getState()).getInventory().addItem(new ItemStack(Material.COBWEB, 64));
			}
		}
		if (plugin.items.LASER_RECEIVER.isSimilar(event.getItemInHand())) {
			event.getBlock().setMetadata("Lasers", new FixedMetadataValue(this.plugin, plugin.items.LASER_RECEIVER));
			LaserRunnable.lasers.add(event.getBlock());
		}
		if (plugin.items.MIRROR_ROTATOR.isSimilar(event.getItemInHand())) {
			event.getBlock().setMetadata("Lasers", new FixedMetadataValue(this.plugin, plugin.items.MIRROR_ROTATOR));
			org.bukkit.block.data.type.Dispenser dispenser = (org.bukkit.block.data.type.Dispenser) event.getBlock().getBlockData();
			dispenser.setFacing(BlockFace.UP);
			event.getBlock().setBlockData(dispenser);
			LaserRunnable.lasers.add(event.getBlock());
			if (event.getBlock().getState() instanceof InventoryHolder) {
				((InventoryHolder) event.getBlock().getState()).getInventory().addItem(new ItemStack(Material.COBWEB, 64));
			}
		}
	}

	@EventHandler
	public void on(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getBlock() == null) { return; }
		if (event.getBlock().hasMetadata("Lasers")) {
			ItemStack toDrop = null;
			if (event.getBlock().getType() == plugin.items.LASER_EMITTER.getType() || event.getBlock().getType() == plugin.items.LASER_RECEIVER.getType() || event.getBlock().getType() == plugin.items.MIRROR_ROTATOR.getType()) {
				for (MetadataValue meta : event.getBlock().getMetadata("Lasers")) {
					if (meta.value() instanceof ItemStack) {
						toDrop = (ItemStack) meta.value();
						break;
					}
				}
			}
			if (toDrop != null) {
				if (event.getBlock().getState() instanceof InventoryHolder) {
					((InventoryHolder) event.getBlock().getState()).getInventory().clear();
				}
				LaserRunnable.activeLasers.remove(event.getBlock());
				LaserRunnable.lasers.remove(event.getBlock());
				event.getBlock().setType(Material.AIR);
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
				}
			}
		}
	}

	@EventHandler
	public void on(InventoryMoveItemEvent event) {
		InventoryHolder holder = event.getSource().getType() == InventoryType.DISPENSER || event.getSource().getType() == InventoryType.DROPPER ? event.getSource().getHolder() : event.getDestination().getHolder();
		if (holder == null) { return; }
		Block block = null;
		if (holder instanceof Dispenser) {
			block = ((Dispenser) holder).getBlock();
		}
		if (holder instanceof Dropper) {
			block = ((Dropper) holder).getBlock();
		}
		if (block == null) { return; }
		if (plugin.items.LASER_EMITTER.getType() == block.getType() || plugin.items.LASER_RECEIVER.getType() == block.getType()) {
			if (block.hasMetadata("Lasers")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void on(BlockDispenseEvent event) {
		if (event.getBlock() == null) { return; }
		if (event.getBlock().hasMetadata("Lasers")) {
			ItemStack metaItem = null;
			for (MetadataValue meta : event.getBlock().getMetadata("Lasers")) {
				if (meta.value() instanceof ItemStack) {
					metaItem = (ItemStack) meta.value();
					break;
				}
			}
			if (plugin.items.LASER_EMITTER.getType() == event.getBlock().getType() && plugin.items.LASER_EMITTER.isSimilar(metaItem)) {
				LaserRunnable.activeLasers.add(event.getBlock());
				event.setCancelled(true);
				event.setItem(new ItemStack(Material.AIR));
			}
			if (plugin.items.LASER_RECEIVER.getType() == event.getBlock().getType() && plugin.items.LASER_RECEIVER.isSimilar(metaItem)) {
				event.setCancelled(true);
				event.setItem(new ItemStack(Material.AIR));
			}
			if (plugin.items.MIRROR_ROTATOR.getType() == event.getBlock().getType() && plugin.items.MIRROR_ROTATOR.isSimilar(metaItem)) {
				event.setCancelled(true);
				event.setItem(new ItemStack(Material.AIR));
				Block up = event.getBlock().getRelative(BlockFace.UP);
//				if (event.getBlock().getData() != 1 && event.getBlock().getData() != 9) {// 1 = off, 9 = on  //TODO: replace data
//					this.plugin.getLogger().warning("A invalid MirrorRotator was triggered but has a data value of " + event.getBlock().getData() + "!");
//					return;
//				}
				if (up.getBlockData()  instanceof Rotatable/* standing */) {
					Rotatable rotatable = (Rotatable) up.getBlockData();
					//TODO: replace data
					BlockFace newFace = this.plugin.bannerHelper.getBannerAngleForPower(up);
					if (newFace != rotatable.getRotation()) {
						up.getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_PISTON_EXTEND, 1, 1);
						rotatable.setRotation(newFace);
						up.setBlockData(rotatable);
						org.bukkit.block.data.type.Dispenser dispenser = (org.bukkit.block.data.type.Dispenser)event.getBlock().getBlockData();
						dispenser.setTriggered(false);// Turn the dispenser off, so it can be triggered again
						event.getBlock().setBlockData(dispenser);
						up.getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1, 1);
					}
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerDeathEvent event) {
		if (event.getEntity().hasMetadata("Laser_Damage")) {
			for (MetadataValue meta : event.getEntity().getMetadata("Laser_Damage")) {
				if (meta.asDouble() == this.plugin.laserDamageAmount) {
					event.setDeathMessage(this.plugin.laserDamageMessage.replace("%player%", event.getEntity().getName()));
					event.getEntity().removeMetadata("Laser_Damage", this.plugin);
					break;
				}
			}
		}
	}
}
