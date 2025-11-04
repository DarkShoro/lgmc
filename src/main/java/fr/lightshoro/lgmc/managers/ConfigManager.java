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
    private final int CURRENT_CONFIG_VERSION = 7;

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

                // Préserver la langue si elle était définie
                if (oldConfig.contains("language")) {
                    defConfig.set("language", oldConfig.get("language"));
                }

                // Locations (toujours préserver)
                if (oldConfig.contains("locations")) {
                    defConfig.set("locations", oldConfig.get("locations"));
                }

                // Game settings (préserver tout)
                if (oldConfig.contains("game.min-players")) {
                    defConfig.set("game.min-players", oldConfig.get("game.min-players"));
                }
                if (oldConfig.contains("game.two-wolves-threshold")) {
                    defConfig.set("game.two-wolves-threshold", oldConfig.get("game.two-wolves-threshold"));
                }
                if (oldConfig.contains("game.cupidon-threshold")) {
                    defConfig.set("game.cupidon-threshold", oldConfig.get("game.cupidon-threshold"));
                }
                if (oldConfig.contains("game.ange-threshold")) {
                    defConfig.set("game.ange-threshold", oldConfig.get("game.ange-threshold"));
                }
                if (oldConfig.contains("game.voleur-threshold")) {
                    defConfig.set("game.voleur-threshold", oldConfig.get("game.voleur-threshold"));
                }
                if (oldConfig.contains("game.cupidon-enabled")) {
                    defConfig.set("game.cupidon-enabled", oldConfig.get("game.cupidon-enabled"));
                }
                if (oldConfig.contains("game.ange-enabled")) {
                    defConfig.set("game.ange-enabled", oldConfig.get("game.ange-enabled"));
                }
                if (oldConfig.contains("game.voleur-enabled")) {
                    defConfig.set("game.voleur-enabled", oldConfig.get("game.voleur-enabled"));
                }
                if (oldConfig.contains("game.voleur-chance")) {
                    defConfig.set("game.voleur-chance", oldConfig.get("game.voleur-chance"));
                }
                if (oldConfig.contains("game.countdown-duration")) {
                    defConfig.set("game.countdown-duration", oldConfig.get("game.countdown-duration"));
                }

                // Timers (préserver tout)
                if (oldConfig.contains("game.timers")) {
                    if (oldConfig.contains("game.timers.voleur")) {
                        defConfig.set("game.timers.voleur", oldConfig.get("game.timers.voleur"));
                    }
                    if (oldConfig.contains("game.timers.cupidon")) {
                        defConfig.set("game.timers.cupidon", oldConfig.get("game.timers.cupidon"));
                    }
                    if (oldConfig.contains("game.timers.voyante")) {
                        defConfig.set("game.timers.voyante", oldConfig.get("game.timers.voyante"));
                    }
                    if (oldConfig.contains("game.timers.loups-garous")) {
                        defConfig.set("game.timers.loups-garous", oldConfig.get("game.timers.loups-garous"));
                    }
                    if (oldConfig.contains("game.timers.sorciere")) {
                        defConfig.set("game.timers.sorciere", oldConfig.get("game.timers.sorciere"));
                    }
                    if (oldConfig.contains("game.timers.vote-capitaine")) {
                        defConfig.set("game.timers.vote-capitaine", oldConfig.get("game.timers.vote-capitaine"));
                    }
                    if (oldConfig.contains("game.timers.debat")) {
                        defConfig.set("game.timers.debat", oldConfig.get("game.timers.debat"));
                    }
                    if (oldConfig.contains("game.timers.chasseur")) {
                        defConfig.set("game.timers.chasseur", oldConfig.get("game.timers.chasseur"));
                    }
                    if (oldConfig.contains("game.timers.capitaine-succession")) {
                        defConfig.set("game.timers.capitaine-succession", oldConfig.get("game.timers.capitaine-succession"));
                    }
                    if (oldConfig.contains("game.timers.capitaine-tiebreaker")) {
                        defConfig.set("game.timers.capitaine-tiebreaker", oldConfig.get("game.timers.capitaine-tiebreaker"));
                    }
                }

                // Capitaine settings
                if (oldConfig.contains("game.capitaine.vote-double")) {
                    defConfig.set("game.capitaine.vote-double", oldConfig.get("game.capitaine.vote-double"));
                }

                // Display settings (préserver tout)
                if (oldConfig.contains("display.death-particles")) {
                    defConfig.set("display.death-particles", oldConfig.get("display.death-particles"));
                }
                if (oldConfig.contains("display.death-lightning")) {
                    defConfig.set("display.death-lightning", oldConfig.get("display.death-lightning"));
                }
                if (oldConfig.contains("display.night-blindness")) {
                    defConfig.set("display.night-blindness", oldConfig.get("display.night-blindness"));
                }
                if (oldConfig.contains("display.chasseur-bullet-trace")) {
                    defConfig.set("display.chasseur-bullet-trace", oldConfig.get("display.chasseur-bullet-trace"));
                }
                if (oldConfig.contains("display.debug-messages")) {
                    defConfig.set("display.debug-messages", oldConfig.get("display.debug-messages"));
                }

                // Roles helmets (copier les valeurs individuellement pour permettre l'ajout de nouvelles clés)
                if (oldConfig.contains("roles.helmets")) {
                    for (String key : oldConfig.getConfigurationSection("roles.helmets").getKeys(false)) {
                        defConfig.set("roles.helmets." + key, oldConfig.get("roles.helmets." + key));
                    }
                }

                // GUI settings (préserver tout)
                if (oldConfig.contains("gui.titles")) {
                    defConfig.set("gui.titles", oldConfig.get("gui.titles"));
                }
                if (oldConfig.contains("gui.vote-mode")) {
                    defConfig.set("gui.vote-mode", oldConfig.get("gui.vote-mode"));
                }

                // Advanced settings (préserver tout)
                if (oldConfig.contains("advanced.save-stats")) {
                    defConfig.set("advanced.save-stats", oldConfig.get("advanced.save-stats"));
                }
                if (oldConfig.contains("advanced.spectators-see-dead")) {
                    defConfig.set("advanced.spectators-see-dead", oldConfig.get("advanced.spectators-see-dead"));
                }
                if (oldConfig.contains("advanced.auto-teleport-spawns")) {
                    defConfig.set("advanced.auto-teleport-spawns", oldConfig.get("advanced.auto-teleport-spawns"));
                }
                if (oldConfig.contains("advanced.freeze-during-night")) {
                    defConfig.set("advanced.freeze-during-night", oldConfig.get("advanced.freeze-during-night"));
                }

                // Messages (préserver si modifiés)
                if (oldConfig.contains("messages.game-start")) {
                    defConfig.set("messages.game-start", oldConfig.get("messages.game-start"));
                }
                if (oldConfig.contains("messages.game-stop")) {
                    defConfig.set("messages.game-stop", oldConfig.get("messages.game-stop"));
                }

                // WebSocket settings (préserver tout)
                if (oldConfig.contains("websocket.url")) {
                    defConfig.set("websocket.url", oldConfig.get("websocket.url"));
                }
                if (oldConfig.contains("websocket.enabled")) {
                    defConfig.set("websocket.enabled", oldConfig.get("websocket.enabled"));
                }
                if (oldConfig.contains("websocket.reconnect-interval")) {
                    defConfig.set("websocket.reconnect-interval", oldConfig.get("websocket.reconnect-interval"));
                }
                if (oldConfig.contains("websocket.secret")) {
                    defConfig.set("websocket.secret", oldConfig.get("websocket.secret"));
                }

                // SkinsRestorer settings (préserver si présent)
                if (oldConfig.contains("skinsrestorer.enabled")) {
                    defConfig.set("skinsrestorer.enabled", oldConfig.get("skinsrestorer.enabled"));
                }
                if (oldConfig.contains("skinsrestorer.werewolf-skin-url")) {
                    defConfig.set("skinsrestorer.werewolf-skin-url", oldConfig.get("skinsrestorer.werewolf-skin-url"));
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

    public int getCupidonThreshold() {
        return config.getInt("game.cupidon-threshold", 9);
    }

    public int getAngeThreshold() {
        return config.getInt("game.ange-threshold", 9);
    }

    public int getVoleurThreshold() {
        return config.getInt("game.voleur-threshold", 6);
    }

    public boolean isCupidonEnabled() {
        return config.getBoolean("game.cupidon-enabled", true);
    }

    public boolean isAngeEnabled() {
        return config.getBoolean("game.ange-enabled", true);
    }

    public boolean isVoleurEnabled() {
        return config.getBoolean("game.voleur-enabled", true);
    }

    public double getVoleurChance() {
        return config.getDouble("game.voleur-chance", 0.5);
    }

    public int getCountdownDuration() {
        return config.getInt("game.countdown-duration", 10);
    }

    public boolean isCapitaineVoteDouble() {
        return config.getBoolean("game.capitaine.vote-double", true);
    }

    // Getters pour les timers
    public int getTimerVoleur() {
        return config.getInt("game.timers.voleur", 60);
    }

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

    // Getter pour le mode de vote
    public String getVoteMode() {
        return config.getString("gui.vote-mode", "gui");
    }

    public boolean isClickVoteMode() {
        return "click".equalsIgnoreCase(getVoteMode());
    }

    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Recharge la configuration depuis le fichier
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
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

    public boolean isWebsocketEnabled() {
        return config.getBoolean("websocket.enabled", false);
    }

    // SkinsRestorer integration
    public boolean isSkinsRestorerEnabled() {
        return config.getBoolean("skinsrestorer.enabled", false);
    }

    public String getWerewolfSkinUrl() {
        return config.getString("skinsrestorer.werewolf-skin-url", "https://mineskin.eu/skin/werewolf");
    }
}

