package fr.lightshoro.lgmc;

import fr.lightshoro.lgmc.commands.GoodGuysCommand;
import fr.lightshoro.lgmc.commands.LGReloadCommand;
import fr.lightshoro.lgmc.commands.LGSetupCommand;
import fr.lightshoro.lgmc.commands.LGStartCommand;
import fr.lightshoro.lgmc.commands.LGStopCommand;
import fr.lightshoro.lgmc.commands.TestamentCommand;
import fr.lightshoro.lgmc.listeners.GameListener;
import fr.lightshoro.lgmc.listeners.VoteListener;
import fr.lightshoro.lgmc.managers.ConfigManager;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.managers.LanguageManager;
import fr.lightshoro.lgmc.managers.LocationManager;
import fr.lightshoro.lgmc.managers.ResourcePackManager;
import fr.lightshoro.lgmc.managers.TimerManager;
import fr.lightshoro.lgmc.tasks.VisibilityTask;
import fr.lightshoro.lgmc.tasks.VoteCheckTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Lgmc - Loup-Garou Minecraft Plugin
 * Recréation du script lg.sk en Java
 * Ce plugin implémente un jeu de Loup-Garou pour Minecraft avec :
 * - Système de rôles (Loup-Garou, Villageois, Voyante, Sorcière, Chasseur, Cupidon, Petite Fille)
 * - Gestion des phases jour/nuit
 * - Système de vote
 * - Capitaine et succession
 * - Conditions de victoire
 */
public final class Lgmc extends JavaPlugin {

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private GameManager gameManager;
    private TimerManager timerManager;
    private LocationManager locationManager;
    private ResourcePackManager resourcePackManager;

    private static final String ASCII_ART =
            """
                       ⠀⠀⠀⠀⠀⣀⣠⣤⣤⣤⣤⣄⣀⠀⠀⠀⠀⠀
                       ⠀⠀⢀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⡀⠀⠀
                       ⠀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⢿⣿⣷⡀⠀  ██       ██████  ███    ███  ██████
                       ⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠁⠀⣴⢿⣿⣧⠀  ██      ██       ████  ████ ██    
                       ⣿⣿⣿⣿⣿⡿⠛⣩⠍⠀⠀⠀⠐⠉⢠⣿⣿⡇  ██      ██   ███ ██ ████ ██ ██    
                       ⣿⡿⠛⠋⠉⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⣿⣿⣿  ██      ██    ██ ██  ██  ██ ██     
                       ⢹⣿⣤⠄⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⣿⣿⣿⡏  ███████  ██████  ██      ██  ██████
                       ⠀⠻⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣿⣿⣿⠟⠀
                          ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⠟⠁⠀⠀
                    """;


    @Override
    public void onEnable() {
        // Initialisation des managers de configuration et langue
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this, configManager.getLanguage());

        // Initialisation des managers
        this.locationManager = new LocationManager(this);
        this.gameManager = new GameManager(this);
        this.timerManager = new TimerManager(this);
        this.resourcePackManager = new ResourcePackManager(this);

        // Enregistrement des listeners
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoteListener(this), this);
        Bukkit.getPluginManager().registerEvents(resourcePackManager, this);

        // Enregistrement des commandes
        if (getCommand("testament") != null) {
            Objects.requireNonNull(getCommand("testament")).setExecutor(new TestamentCommand(this));
        }
        if (getCommand("goodGuys") != null) {
            Objects.requireNonNull(getCommand("goodGuys")).setExecutor(new GoodGuysCommand(this));
        }
        if (getCommand("lgstart") != null) {
            Objects.requireNonNull(getCommand("lgstart")).setExecutor(new LGStartCommand(this));
        }
        if (getCommand("lgstop") != null) {
            Objects.requireNonNull(getCommand("lgstop")).setExecutor(new LGStopCommand(this));
        }
        if (getCommand("lgsetup") != null) {
            Objects.requireNonNull(getCommand("lgsetup")).setExecutor(new LGSetupCommand(this));
        }
        if (getCommand("lgreload") != null) {
            Objects.requireNonNull(getCommand("lgreload")).setExecutor(new LGReloadCommand(this));
        }

        // Démarrer la tâche périodique de vérification des votes (toutes les 7 secondes)
        new VoteCheckTask(this).runTaskTimer(this, 140L, 140L);

        // Démarrer la tâche périodique de gestion de visibilité (toutes les secondes)
        new VisibilityTask(this).runTaskTimer(this, 20L, 20L);

        // Affiche notre magnifique ASCII art dans la console ; ligne par ligne pour éviter les problèmes d'encodage
        for (String line : ASCII_ART.split("\n")) {
            getLogger().info(line);
        }

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║   Lgmc Plugin - Loup-Garou Minecraft   ║");
        getLogger().info("║          Plugin activé avec succès     ║");
        getLogger().info("║   Langue: " + languageManager.getCurrentLanguage().toUpperCase() + " | Config v" + configManager.getConfig().getInt("config-version") + "               ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        // Nettoyage du timer
        if (timerManager != null) {
            timerManager.clearTimer();
        }

        // Reset du jeu si en cours
        if (gameManager != null && gameManager.isInGame()) {
            gameManager.gameReset(true);
        }

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║   Lgmc Plugin - Loup-Garou Minecraft   ║");
        getLogger().info("║         Plugin désactivé proprement    ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    /**
     * Récupère le gestionnaire de configuration
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Récupère le gestionnaire de langue
     * @return LanguageManager instance
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Récupère le gestionnaire de jeu
     * @return GameManager instance
     */
    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Récupère le gestionnaire de timer
     * @return TimerManager instance
     */
    public TimerManager getTimerManager() {
        return timerManager;
    }

    /**
     * Récupère le gestionnaire d'emplacements
     * @return LocationManager instance
     */
    public LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * Récupère le gestionnaire de packs de ressources
     * @return ResourcePackManager instance
     */
    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }
}
