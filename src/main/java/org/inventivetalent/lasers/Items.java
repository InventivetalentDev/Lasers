package org.inventivetalent.lasers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Items {

	private final Lasers plugin;

	public ItemStack LASER_CRYSTAL;
	public ItemStack LASER_EMITTER;
	public ItemStack LASER_RECEIVER;
	public ItemStack MIRROR_ROTATOR;

	public Items(Lasers lasers) {
		this.plugin = lasers;
	}

	protected void load() {
		/* Laser Crystal */
		LASER_CRYSTAL = new ItemStack(Material.DIAMOND);
		ItemMeta meta = LASER_CRYSTAL.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Laser " + ChatColor.AQUA + "Crystal");
		meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
		LASER_CRYSTAL.setItemMeta(meta);

		/* Laser Emitter */
		LASER_EMITTER = new ItemStack(Material.DISPENSER);
		meta = LASER_EMITTER.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Laser Emitter");
		LASER_EMITTER.setItemMeta(meta);

		/* Laser Receiver */
		LASER_RECEIVER = new ItemStack(Material.DROPPER);
		meta = LASER_RECEIVER.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Laser Receiver");
		LASER_RECEIVER.setItemMeta(meta);

		/* Mirror Rotator */
		MIRROR_ROTATOR = new ItemStack(Material.DISPENSER);
		meta = MIRROR_ROTATOR.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Mirror Rotator");
		MIRROR_ROTATOR.setItemMeta(meta);
	}

}
