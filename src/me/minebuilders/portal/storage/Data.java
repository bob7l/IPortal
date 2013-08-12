package me.minebuilders.portal.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import me.minebuilders.portal.Bound;
import me.minebuilders.portal.IP;
import me.minebuilders.portal.Status;
import me.minebuilders.portal.Util;
import me.minebuilders.portal.portals.Portal;
import me.minebuilders.portal.portals.PortalType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Data {

	private FileConfiguration data = null;
	private File customConfigFile = null;
	private IP plugin;

	public Data(IP plugin) {
		this.plugin = plugin;
		reloadCustomConfig();
		load();
	}

	public void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(plugin.getDataFolder(), "portals.yml");
		}
		data = YamlConfiguration.loadConfiguration(customConfigFile);

		InputStream defConfigStream = plugin.getResource("portals.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			data.setDefaults(defConfig);
		}
	}

	public FileConfiguration getConfig() {
		if (data == null) {
			this.reloadCustomConfig();
		}
		return data;
	}

	public void saveCustomConfig() {
		if (data != null && customConfigFile != null) {
			try {
				getConfig().save(customConfigFile);
			} catch (IOException ex) {
				Util.warning("Could not save portals.yml! Error:" + ex.getMessage());
			}
		}
	}

	public void load() {
		if (new File(plugin.getDataFolder(), "portals.yml").exists()) {
			for (String s : data.getConfigurationSection("portals").getKeys(false)) {
				Status status = Status.RUNNING;
				String exit = null;
				Bound b = null;
				PortalType type = PortalType.DEFAULT;

				try {
					exit = data.getString("portals." + s + ".tpto");
					type = getType(data.getString("portals." + s + ".type"));
				} catch (Exception e) { 
					Util.warning("Unable to load teleport location for portal " + s + "!"); 
					status = Status.BROKEN;
				}

				try {
					b = new Bound(data.getString("portals." + s + "." + "world"), BC(s, "x"), BC(s, "y"), BC(s, "z"), BC(s, "x2"), BC(s, "y2"), BC(s, "z2"));
				} catch (Exception e) { 
					Util.warning("Unable to load region bounds for portal " + s + "!"); 
					status = Status.BROKEN;
				}
				try {
					plugin.portals.add((Portal) type.getPortal().newInstance(s, exit, b, status));
				} catch (Exception e) { 
					Util.warning("Unable to load portal " + s + "!"); 
				}
			}
		}
	}

	public int BC(String s, String st) {
		return data.getInt("portals." + s + "." + st);
	}

	public Location uncompressLoc(String s) {
		String[] h = s.split(":");
		return new Location(Bukkit.getServer().getWorld(h[0]), Integer.parseInt(h[1]) + 0.5, Integer.parseInt(h[2]), Integer.parseInt(h[3]) + 0.5, Float.parseFloat(h[4]), Float.parseFloat(h[5]));
	}

	public String compressLoc(Location l) {
		return (l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ() + ":" + l.getYaw() + ":" + l.getPitch());
	}

	public PortalType getType(String s) {
		for (PortalType t : PortalType.values()) {
			if (s.equalsIgnoreCase(t.name())) {
				return t;
			}
		}
		return PortalType.DEFAULT;
	}
}
