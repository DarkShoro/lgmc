package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Gestionnaire de configuration avec système de mise à jour automatique
 * Préserve les valeurs personnalisées lors des mises à jour
 */
public class ConfigManager {
    private final Lgmc plugin;
    private FileConfiguration config;
    private final int CURRENT_CONFIG_VERSION = 3;

    public ConfigManager(Lgmc plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Charge la configuration et la met à jour si nécessaire
     */
    public void loadConfig() {
        // Créer le fichier de config s'il n'existe pas
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Vérifier la version de la config
        int configVersion = config.getInt("config-version", 1);

        if (configVersion < CURRENT_CONFIG_VERSION) {
            plugin.getLogger().info("Mise à jour de la configuration de la version " + configVersion + " à " + CURRENT_CONFIG_VERSION);
            updateConfig(configVersion);
        }
    }

    /**
     * Met à jour la configuration en préservant les valeurs personnalisées
     */
    private void updateConfig(int oldVersion) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File backupFile = new File(plugin.getDataFolder(), "config_backup_v" + oldVersion + ".yml");

        try {
            // Sauvegarder l'ancienne config
            if (configFile.exists()) {
                YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(configFile);
                oldConfig.save(backupFile);
                plugin.getLogger().info("Ancienne configuration sauvegardée dans " + backupFile.getName());
            }

            // Charger la config par défaut
            InputStream defConfigStream = plugin.getResource("config.yml");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));

                // Copier les valeurs personnalisées
                FileConfiguration oldConfig = config;

                // Locations (toujours préserver)
                if (oldConfig.contains("locations")) {
                    defConfig.set("locations", oldConfig.get("locations"));
                }

                // Game settings (préserver si modifiés)
                if (oldConfig.contains("game.min-players")) {
                    defConfig.set("game.min-players", oldConfig.get("game.min-players"));
                }
                if (oldConfig.contains("game.two-wolves-threshold")) {
                    defConfig.set("game.two-wolves-threshold", oldConfig.get("game.two-wolves-threshold"));
                }
                if (oldConfig.contains("game.cupidon-enabled")) {
                    defConfig.set("game.cupidon-enabled", oldConfig.get("game.cupidon-enabled"));
                }

                // Préserver la langue si elle était définie
                if (oldConfig.contains("language")) {
                    defConfig.set("language", oldConfig.get("language"));
                }

                // Mettre à jour la version
                defConfig.set("config-version", CURRENT_CONFIG_VERSION);

                // Sauvegarder la nouvelle config
                defConfig.save(configFile);

                // Recharger
                plugin.reloadConfig();
                config = plugin.getConfig();

                plugin.getLogger().info("Configuration mise à jour avec succès !");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la mise à jour de la configuration", e);
        }
    }

    /**
     * Recharge la configuration
     */
    public void reload() {
        loadConfig();
    }

    // Getters pour les paramètres du jeu
    public int getMinPlayers() {
        return config.getInt("game.min-players", 4);
    }

    public int getTwoWolvesThreshold() {
        return config.getInt("game.two-wolves-threshold", 9);
    }

    public boolean isCupidonEnabled() {
        return config.getBoolean("game.cupidon-enabled", true);
    }

    public int getCountdownDuration() {
        return config.getInt("game.countdown-duration", 10);
    }

    public boolean isCapitaineVoteDouble() {
        return config.getBoolean("game.capitaine.vote-double", true);
    }

    // Getters pour les timers
    public int getTimerCupidon() {
        return config.getInt("game.timers.cupidon", 60);
    }

    public int getTimerVoyante() {
        return config.getInt("game.timers.voyante", 30);
    }

    public int getTimerLoupsGarous() {
        return config.getInt("game.timers.loups-garous", 30);
    }

    public int getTimerSorciere() {
        return config.getInt("game.timers.sorciere", 30);
    }

    public int getTimerVoteCapitaine() {
        return config.getInt("game.timers.vote-capitaine", 60);
    }

    public int getTimerDebat() {
        return config.getInt("game.timers.debat", 300);
    }

    public int getTimerChasseur() {
        return config.getInt("game.timers.chasseur", 30);
    }

    public int getTimerCapitaineSuccession() {
        return config.getInt("game.timers.capitaine-succession", 30);
    }

    public int getTimerCapitaineTiebreaker() {
        return config.getInt("game.timers.capitaine-tiebreaker", 60);
    }

    // Getters pour l'affichage
    public boolean isDeathParticlesEnabled() {
        return config.getBoolean("display.death-particles", true);
    }

    public boolean isDeathLightningEnabled() {
        return config.getBoolean("display.death-lightning", true);
    }

    public boolean isNightBlindnessEnabled() {
        return config.getBoolean("display.night-blindness", true);
    }

    public boolean isChasseurBulletTraceEnabled() {
        return config.getBoolean("display.chasseur-bullet-trace", true);
    }

    public boolean isDebugMessagesEnabled() {
        return config.getBoolean("display.debug-messages", false);
    }

    // Getters pour les paramètres avancés
    public boolean isSaveStatsEnabled() {
        return config.getBoolean("advanced.save-stats", true);
    }

    public boolean isSpectatorsSeeDeadEnabled() {
        return config.getBoolean("advanced.spectators-see-dead", true);
    }

    public boolean isAutoTeleportSpawnsEnabled() {
        return config.getBoolean("advanced.auto-teleport-spawns", true);
    }

    public boolean isFreezeDuringNightEnabled() {
        return config.getBoolean("advanced.freeze-during-night", true);
    }

    // Getter pour la langue
    public String getLanguage() {
        return config.getString("language", "fr");
    }

    // Getter pour les casques des rôles
    public String getRoleHelmet(String role) {
        return config.getString("roles.helmets." + role, "AIR");
    }

    public ItemStack getRoleHelmetItemStack(String role) {
        String helmetConfig = getRoleHelmet(role);
        try {
            Material material = Material.valueOf(helmetConfig.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            // Gérer les casques spéciaux comme les têtes de joueur
            if (helmetConfig.startsWith("PLAYER_HEAD:")) {
                String playerName = helmetConfig.split(":", 2)[1];
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(plugin.getServer().getPlayerExact(playerName));
                    skull.setItemMeta(meta);
                }
                return skull;
            }
            // Retourner un casque par défaut si le matériau est invalide
            return new ItemStack(Material.AIR);
        }
    }

    // Getter pour les titres de GUI personnalisés
    public String getCustomGuiTitle(String gui) {
        String title = config.getString("gui.titles." + gui, "");
        return title.isEmpty() ? null : title;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getWebsocketUrl() {
        return config.getString("websocket.url", "");
    }
    public String getWebsocketSecret() {
        return config.getString("websocket.secret", "");
    }

    public int getWebsocketReconnectInterval() {
        return config.getInt("websocket.reconnect-interval", 10);
    }
}

