package org.inventivetalent.lasers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Lasers extends JavaPlugin implements Listener {

	public static String prefix = "§c[Lasers]§r ";

	public Items          items;
	public Recipes        recipes;
	public BannerHelper   bannerHelper;
	public ColorHelper    colorHelper;
	public LasersListener lasersListener;
	public LaserRunnable  laserRunnable;

	public File laserFile;

	public Map<Location, Object[]> unloadedLasers = new HashMap<>();

	public int     laserInterval           = 2;
	public int     laserLength             = 16;
	public double  laserFrequency          = 0.25;
	public boolean laserDamageEnabled      = true;
	public double  laserDamageAmount       = 0.25;
	public boolean laserDamageFire         = true;
	public String  laserDamageMessage      = "%player% tried to stand in front of a laser";
	public boolean colorMirror             = true;
	public boolean colorGlassBlock         = true;
	public boolean colorGlassPane          = true;
	public boolean colorMix                = true;
	public boolean mirrorsDouble           = false;
	public String  mirrorRotatorMode       = "side";
	public boolean blockedBlocks           = true;
	public boolean blockedEntities         = true;
	public String  receiverSignalMode      = "distance";
	public int     receiverSignalTolerance = 25;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		laserInterval = getConfig().getInt("laser.interval",laserInterval);
		laserLength = getConfig().getInt("laser.length",laserLength);
		laserFrequency = getConfig().getDouble("laser.frequency",laserFrequency);
		laserDamageEnabled = getConfig().getBoolean("laser.damage.enabled",laserDamageEnabled);
		laserDamageAmount = getConfig().getDouble("laser.damage.amount",laserDamageAmount);
		laserDamageFire = getConfig().getBoolean("laser.damage.fire", laserDamageFire);
		laserDamageMessage = getConfig().getString("laser.damage.message", laserDamageMessage);
		colorMirror = getConfig().getBoolean("color.mirror", colorMirror);
		colorGlassBlock = getConfig().getBoolean("color.glass.block", colorGlassBlock);
		colorGlassPane = getConfig().getBoolean("color.glass.pane", colorGlassPane);
		colorMirror = getConfig().getBoolean("color.mix",colorMix);
		mirrorsDouble = getConfig().getBoolean("mirrors.double", mirrorsDouble);
		mirrorRotatorMode = getConfig().getString("mirrors.rotator.mode", mirrorRotatorMode);
		blockedBlocks = getConfig().getBoolean("blocked.blocks", blockedBlocks);
		blockedEntities = getConfig().getBoolean("blocked.entities", blockedEntities);
		receiverSignalMode = getConfig().getString("receiver.signal.mode", receiverSignalMode);
		receiverSignalTolerance = getConfig().getInt("receiver.signal.tolerance", receiverSignalTolerance);

		this.items = new Items(this);
		this.items.load();

		this.recipes = new Recipes(this);
		this.recipes.load();

		this.bannerHelper = new BannerHelper(this);
		this.colorHelper = new ColorHelper(this);

		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(this.lasersListener = new LasersListener(this), this);

		laserFile = new File(this.getDataFolder(), "lasers.json");
		if (!laserFile.exists()) {
			try {
				laserFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Bukkit.getScheduler().runTaskLater(this, () -> {
			getLogger().info("Loading lasers...");
			loadLasers();
			getLogger().info("Loaded " + LaserRunnable.lasers.size() + " laser blocks (" + unloadedLasers.size() + " will be loaded later)");
		}, 40);

		this.laserRunnable = new LaserRunnable(this);
		Bukkit.getScheduler().runTaskLater(this, () -> laserRunnable.runTaskTimer(Lasers.this, laserInterval, laserInterval), 20 * 5);
	}

	@Override
	public void onDisable() {
		if (laserFile != null) {
			this.saveLasers();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lasers")) {
			if (args.length == 0) {
				sender.sendMessage(prefix + "§b/lasers item <CRYSTAL|EMITTER|RECEIVER>");
				return true;
			}
			if ("item".equalsIgnoreCase(args[0])) {
				if (!sender.hasPermission("lasers.command.item")) {
					sender.sendMessage(prefix + "§cNo permission");
					return false;
				}
				if (!(sender instanceof Player)) {
					sender.sendMessage(prefix + "§cYou must be a player");
					return false;
				}
				if (args.length == 1) {
					sender.sendMessage(prefix + "§c/lasers item <CRYSTAL|EMITTER|RECEIVER>");
					return false;
				}
				if (args.length == 2) {
					if ("CRYSTAL".equalsIgnoreCase(args[1]) || "LASER_CRYSTAL".equalsIgnoreCase(args[1])) {
						((Player) sender).getInventory().addItem(this.items.LASER_CRYSTAL);
					}
					if ("EMITTER".equalsIgnoreCase(args[1]) || "LASER_EMITTER".equalsIgnoreCase(args[1])) {
						((Player) sender).getInventory().addItem(this.items.LASER_EMITTER);
					}
					if ("RECEIVER".equalsIgnoreCase(args[1]) || "LASER_RECEIVER".equalsIgnoreCase(args[1])) {
						((Player) sender).getInventory().addItem(this.items.LASER_RECEIVER);
					}
					if ("ROTATOR".equalsIgnoreCase(args[1]) || "MIRROR_ROTATOR".equalsIgnoreCase(args[1])) {
						((Player) sender).getInventory().addItem(this.items.MIRROR_ROTATOR);
					}
					return true;
				}

			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			list.add("item");
		}
		if (args.length > 1) {
			if ("item".equalsIgnoreCase(args[0])) {
				list.addAll(Arrays.asList("CRYSTAL", "EMITTER", "RECEIVER", "ROTATOR"));
			}
		}
		return TabCompletionHelper.getPossibleCompletionsForGivenArgs(args, list.toArray(new String[list.size()]));
	}

	void loadLasers() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(laserFile));
			String json = "";
			String line = null;
			while ((line = in.readLine()) != null) {
				json += line;
			}
			in.close();
			if (json.isEmpty()) { return; }
			JSONArray array = new JSONArray(json);
			for (int i = 0; i < array.length(); i++) {
				JSONObject current = array.getJSONObject(i);
				JSONObject locObj = current.getJSONObject("location");
				Location location = new Location(Bukkit.getWorld(locObj.getString("world")), locObj.getInt("x"), locObj.getInt("y"), locObj.getInt("z"));
				boolean active = current.getBoolean("active");
				String type = current.getString("type");

				if (location.getWorld() == null) {
					continue;
				}

				if (location.getChunk().isLoaded()) {
					this.loadLaser(location, active, type);
				} else {
					this.unloadedLasers.put(location, new Object[] {
							Boolean.valueOf(active),
							type
					});
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void saveLasers() {
		JSONArray array = new JSONArray();
		for (Block b : LaserRunnable.lasers) {
			JSONObject current = new JSONObject();
			final Location loc = b.getLocation();

			ItemStack metaItem = null;
			for (MetadataValue meta : b.getMetadata("Lasers")) {
				if (meta.value() instanceof ItemStack) {
					metaItem = (ItemStack) meta.value();
					break;
				}
			}

			String type = null;
			if (this.items.LASER_EMITTER.getType() == b.getType() && this.items.LASER_EMITTER.isSimilar(metaItem)) {
				type = "EMITTER";
			}
			if (this.items.LASER_RECEIVER.getType() == b.getType() && this.items.LASER_RECEIVER.isSimilar(metaItem)) {
				type = "RECEIVER";
			}
			if (this.items.MIRROR_ROTATOR.getType() == b.getType() && this.items.MIRROR_ROTATOR.isSimilar(metaItem)) {
				type = "ROTATOR";
			}
			if (type == null) {
				continue;
			}
			current.put("type", type);
			current.put("location", new JSONObject() {
				{
					this.put("world", loc.getWorld().getName());
					this.put("x", loc.getBlockX());
					this.put("y", loc.getBlockY());
					this.put("z", loc.getBlockZ());
				}
			});
			current.put("active", LaserRunnable.activeLasers.contains(b));

			array.put(current);
		}
		for (Map.Entry<Location, Object[]> entry : this.unloadedLasers.entrySet()) {
			final Location loc = entry.getKey();
			final Boolean bool = (Boolean) entry.getValue()[0];
			String type = (String) entry.getValue()[1];

			JSONObject current = new JSONObject();
			if (type == null) {
				continue;
			}
			current.put("type", type);
			current.put("location", new JSONObject() {
				{
					this.put("world", loc.getWorld());
					this.put("x", loc.getBlockX());
					this.put("y", loc.getBlockY());
					this.put("z", loc.getBlockZ());
				}
			});
			current.put("active", bool);

			array.put(current);
		}

		String jsonString = array.toString(2);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(laserFile));
			out.write(jsonString);
			out.flush();
			out.close();
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Exception while saving Lasers to file", e);
		}
	}

	boolean loadLaser(Location loc, boolean active, String type) {
		if (loc.getChunk().isLoaded()) {
			loc.getBlock().setMetadata("Lasers", new FixedMetadataValue(this, "EMITTER".equals(type) ? this.items.LASER_EMITTER : "RECEIVER".equals(type) ? this.items.LASER_RECEIVER : "ROTATOR".equals(type) ? this.items.MIRROR_ROTATOR : null));
			LaserRunnable.lasers.add(loc.getBlock());
			if (active && "EMITTER".equals(type)) {
				LaserRunnable.activeLasers.add(loc.getBlock());
			}
			return true;
		}
		return false;
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		Iterator<Map.Entry<Location, Object[]>> iterator = this.unloadedLasers.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Location, Object[]> entry = iterator.next();
			Location loc = entry.getKey();
			boolean active = (Boolean) entry.getValue()[0];
			String type = (String) entry.getValue()[1];
			if (loc.getChunk().isLoaded()) {
				if (this.loadLaser(loc, active, type)) {
					iterator.remove();
				}
			}
		}
	}

}
