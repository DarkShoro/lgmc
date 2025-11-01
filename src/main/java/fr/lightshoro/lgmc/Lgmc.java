package fr.lightshoro.lgmc;

import fr.lightshoro.lgmc.commands.*;
import fr.lightshoro.lgmc.listeners.GameListener;
import fr.lightshoro.lgmc.listeners.VoteListener;
import fr.lightshoro.lgmc.listeners.ChatListener;
import fr.lightshoro.lgmc.managers.*;
import fr.lightshoro.lgmc.tasks.VisibilityTask;
import fr.lightshoro.lgmc.tasks.VoteCheckTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.io.InputStream;
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
    private MotdManager motdManager;
    private WebsocketManager websocketManager;
    private ChatManager chatManager;
    public CachedServerIcon serverIcon;

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
        this.motdManager = new MotdManager(this);
        this.chatManager = new ChatManager(this);

        // Enregistrement des listeners
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoteListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(resourcePackManager, this);

        // Enregistrement des commandes
        if (getCommand("lg") != null) {
            LGCommand lgCommand = new LGCommand(this);
            Objects.requireNonNull(getCommand("lg")).setExecutor(lgCommand);
            Objects.requireNonNull(getCommand("lg")).setTabCompleter(lgCommand);
        }
        if (getCommand("goodGuys") != null) {
            Objects.requireNonNull(getCommand("goodGuys")).setExecutor(new GoodGuysCommand(this));
        }
        if (getCommand("love") != null) {
            Objects.requireNonNull(getCommand("love")).setExecutor(new LoveCommand(this));
        }

        // Démarrer la tâche périodique de vérification des votes (toutes les 7 secondes)
        new VoteCheckTask(this).runTaskTimer(this, 140L, 140L);

        // Démarrer la tâche périodique de gestion de visibilité (toutes les secondes)
        new VisibilityTask(this).runTaskTimer(this, 20L, 20L);

        // Affiche notre magnifique ASCII art dans la console ; ligne par ligne pour éviter les problèmes d'encodage
        for (String line : ASCII_ART.split("\n")) {
            getLogger().info(line);
        }

        // A l'initialisation du plugin, on force le nombre de slot a 12.
        Bukkit.getServer().setMaxPlayers(12);
        // On force aussi le server-icon sur le plugin
        // On prend le server-icon depuis les ressources du plugin
        InputStream iconStream = this.getResource("server-icon.png");
        try {
            if (iconStream != null) {
                java.nio.file.Files.copy(iconStream, new File(this.getDataFolder(), "server-icon.png").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                File serverIconFile = new File(this.getDataFolder(), "server-icon.png");
                serverIcon = Bukkit.loadServerIcon(serverIconFile);
                getLogger().info("Server-icon chargé depuis les ressources du plugin.");
            }
        } catch (Exception e) {
            getLogger().warning("Impossible de charger le server-icon depuis les ressources du plugin.");
        }

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║   Lgmc Plugin - Loup-Garou Minecraft   ║");
        getLogger().info("║          Plugin activé avec succès     ║");
        getLogger().info("║   Langue: " + languageManager.getCurrentLanguage().toUpperCase() + " | Config v" + configManager.getConfig().getInt("config-version") + "               ║");
        getLogger().info("╚════════════════════════════════════════╝");

        // Initialize WebsocketManager before registering commands
        if (this.getConfigManager().isWebsocketEnabled()) {
            this.websocketManager = new WebsocketManager(this.configManager.getWebsocketUrl(), this.configManager.getWebsocketSecret(), this);
        } else {
            this.websocketManager = new WebsocketManager(false,this);
        }

        if (getCommand("wsaction") != null) {
            Objects.requireNonNull(getCommand("wsaction")).setExecutor(new WSActionCommand(this));
        }
        if (getCommand("linkDiscord") != null) {
            Objects.requireNonNull(getCommand("linkDiscord")).setExecutor(new DiscordLinkCommand(this));
        }



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

    /**
     * Récupère le gestionnaire de MOTD
     * @return MotdManager instance
     */
    public MotdManager getMotdManager() {
        return motdManager;
    }

    /**
     * Récupère le gestionnaire de WebSocket
     * @return WebsocketManager instance
     */
    public WebsocketManager getWebsocketManager() {
        return websocketManager;
    }

    /**
     * Récupère le gestionnaire de chat
     * @return ChatManager instance
     */
    public ChatManager getChatManager() {
        return chatManager;
    }
}
