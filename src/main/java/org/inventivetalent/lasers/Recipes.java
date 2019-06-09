package org.inventivetalent.lasers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

public class Recipes {

	private final Lasers plugin;

	public ShapedRecipe LASER_CRYSTAL;
	public ShapedRecipe LASER_EMITTER;
	public ShapedRecipe LASER_RECEIVER;
	public ShapedRecipe MIRROR_ROTATOR;

	public Recipes(Lasers lasers) {
		this.plugin = lasers;
	}

	protected void load() {
		/* Laser Crystal */
		LASER_CRYSTAL = new ShapedRecipe(new NamespacedKey(this.plugin, "laser_crystal"), this.plugin.items.LASER_CRYSTAL);
		LASER_CRYSTAL.shape("rrr", "rdr", "rrr");
		LASER_CRYSTAL.setIngredient('r', Material.REDSTONE);
		LASER_CRYSTAL.setIngredient('d', Material.DIAMOND);

		/* Laser Emitter */
		LASER_EMITTER = new ShapedRecipe(new NamespacedKey(this.plugin, "laser_emitter"), this.plugin.items.LASER_EMITTER);
		LASER_EMITTER.shape("sgs", "scs", "sts");
		LASER_EMITTER.setIngredient('s', Material.STONE);
		LASER_EMITTER.setIngredient('g', Material.GLASS);
		LASER_EMITTER.setIngredient('c', Material.DIAMOND);// Laser Crystal
		LASER_EMITTER.setIngredient('t', Material.REDSTONE_TORCH);

		/* Laser Receiver */
		LASER_RECEIVER = new ShapedRecipe(new NamespacedKey(this.plugin, "laser_receiver"), this.plugin.items.LASER_RECEIVER);
		LASER_RECEIVER.shape("sgs", "scs", "sts");
		LASER_RECEIVER.setIngredient('s', Material.STONE);
		LASER_RECEIVER.setIngredient('g', Material.GLASS);
		LASER_RECEIVER.setIngredient('c', Material.DIAMOND);// Laser Crystal
		LASER_RECEIVER.setIngredient('t', Material.COMPARATOR);

		/* Mirror Rotator */
		MIRROR_ROTATOR = new ShapedRecipe(new NamespacedKey(this.plugin, "mirror_rotator"), this.plugin.items.MIRROR_ROTATOR);
		MIRROR_ROTATOR.shape("rpr", "ptp", "cpc");
		MIRROR_ROTATOR.setIngredient('r', Material.REDSTONE);
		MIRROR_ROTATOR.setIngredient('p', Material.PISTON);
		MIRROR_ROTATOR.setIngredient('t', Material.REDSTONE_TORCH);
		MIRROR_ROTATOR.setIngredient('c', Material.COMPARATOR);

		inject();
	}

	private void inject() {
		Bukkit.addRecipe(LASER_CRYSTAL);
		Bukkit.addRecipe(LASER_EMITTER);
		Bukkit.addRecipe(LASER_RECEIVER);
		Bukkit.addRecipe(MIRROR_ROTATOR);
	}

}