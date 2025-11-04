package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LocationManager {
    private final Lgmc plugin;
    private final Map<Integer, Location> spawnLocations;
    private Location campfireLocation;
    private Location chasseurTpLocation;

    public LocationManager(Lgmc plugin) {
        this.plugin = plugin;
        this.spawnLocations = new HashMap<>();
        loadLocations();
    }

    private void loadLocations() {
        FileConfiguration config = plugin.getConfig();

        // Load campfire location
        if (config.contains("locations.campfire")) {
            String worldName = config.getString("locations.campfire.world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = config.getDouble("locations.campfire.x");
                double y = config.getDouble("locations.campfire.y");
                double z = config.getDouble("locations.campfire.z");
                campfireLocation = new Location(world, x, y, z);
            }
        }

        // Load chasseur tp location
        if (config.contains("locations.chasseur-tp")) {
            String worldName = config.getString("locations.chasseur-tp.world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = config.getDouble("locations.chasseur-tp.x");
                double y = config.getDouble("locations.chasseur-tp.y");
                double z = config.getDouble("locations.chasseur-tp.z");
                float yaw = (float) config.getDouble("locations.chasseur-tp.yaw");
                float pitch = (float) config.getDouble("locations.chasseur-tp.pitch");
                chasseurTpLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }

        // Load spawn locations
        if (config.contains("locations.spawns")) {
            for (String key : config.getConfigurationSection("locations.spawns").getKeys(false)) {
                try {
                    int index = Integer.parseInt(key);
                    String worldName = config.getString("locations.spawns." + key + ".world", "world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = config.getDouble("locations.spawns." + key + ".x");
                        double y = config.getDouble("locations.spawns." + key + ".y");
                        double z = config.getDouble("locations.spawns." + key + ".z");
                        float yaw = (float) config.getDouble("locations.spawns." + key + ".yaw");
                        float pitch = (float) config.getDouble("locations.spawns." + key + ".pitch");
                        spawnLocations.put(index, new Location(world, x, y, z, yaw, pitch));
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid spawn location key: " + key);
                }
            }
        }

        // Set default locations if not configured
        if (campfireLocation == null) {
            World world = Bukkit.getWorld("world");
            if (world != null) {
                campfireLocation = new Location(world, 0, 64, 0);
                plugin.getLogger().warning("Campfire location not configured, using default (0, 64, 0)");
            }
        }

        if (chasseurTpLocation == null) {
            World world = Bukkit.getWorld("world");
            if (world != null) {
                chasseurTpLocation = new Location(world, 0, 100, 0);
                plugin.getLogger().warning("Chasseur TP location not configured, using default (0, 100, 0)");
            }
        }
    }

    public void saveLocations() {
        FileConfiguration config = plugin.getConfig();

        // Save campfire location
        if (campfireLocation != null) {
            config.set("locations.campfire.world", campfireLocation.getWorld().getName());
            config.set("locations.campfire.x", campfireLocation.getX());
            config.set("locations.campfire.y", campfireLocation.getY());
            config.set("locations.campfire.z", campfireLocation.getZ());
        }

        // Save chasseur tp location
        if (chasseurTpLocation != null) {
            config.set("locations.chasseur-tp.world", chasseurTpLocation.getWorld().getName());
            config.set("locations.chasseur-tp.x", chasseurTpLocation.getX());
            config.set("locations.chasseur-tp.y", chasseurTpLocation.getY());
            config.set("locations.chasseur-tp.z", chasseurTpLocation.getZ());
            config.set("locations.chasseur-tp.yaw", chasseurTpLocation.getYaw());
            config.set("locations.chasseur-tp.pitch", chasseurTpLocation.getPitch());
        }

        // Save spawn locations
        for (Map.Entry<Integer, Location> entry : spawnLocations.entrySet()) {
            Location loc = entry.getValue();
            String path = "locations.spawns." + entry.getKey();
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
        }

        plugin.saveConfig();
    }

    public Location getCampfireLocation() {
        return campfireLocation;
    }

    public void setCampfireLocation(Location location) {
        this.campfireLocation = location;
    }

    public Location getChasseurTpLocation() {
        return chasseurTpLocation;
    }

    public void setChasseurTpLocation(Location location) {
        this.chasseurTpLocation = location;
    }

    public Location getSpawnLocation(int index) {
        return spawnLocations.get(index);
    }

    public void setSpawnLocation(int index, Location location) {
        spawnLocations.put(index, location);
    }

    public Map<Integer, Location> getSpawnLocations() {
        return spawnLocations;
    }

    public int getSpawnCount() {
        return spawnLocations.size();
    }

    /**
     * Retourne le nombre maximum de joueurs basé sur le nombre de spawns
     * Minimum de 12 spawns requis pour une configuration valide
     */
    public int getMaxPlayers() {
        int spawnCount = getSpawnCount();
        return Math.max(12, spawnCount);
    }

    /**
     * Vérifie si le serveur est correctement configuré (au moins 12 spawns)
     */
    public boolean isProperlyConfigured() {
        return getSpawnCount() >= 12;
    }

    /**
     * Recharge tous les emplacements depuis la configuration
     */
    public void reload() {
        // Vider les emplacements actuels
        spawnLocations.clear();
        campfireLocation = null;
        chasseurTpLocation = null;

        // Recharger les emplacements depuis la config
        loadLocations();

        plugin.getLogger().info("Emplacements rechargés : " + spawnLocations.size() + " spawns configurés");
    }
}

