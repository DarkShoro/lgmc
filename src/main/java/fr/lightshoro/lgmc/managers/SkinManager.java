package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Gestionnaire d'intégration avec SkinsRestorer
 * Change les skins des loups-garous pendant leur tour uniquement
 */
public class SkinManager {
    private final Lgmc plugin;
    private SkinsRestorer skinsRestorer;
    private boolean enabled;
    private String werewolfSkinUrl;
    
    // Suivi des joueurs qui ont eu leur skin modifié
    private final Set<Player> modifiedPlayers = new HashSet<>();

    public SkinManager(Lgmc plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigManager().isSkinsRestorerEnabled();
        this.werewolfSkinUrl = plugin.getConfigManager().getWerewolfSkinUrl();
        
        if (enabled) {
            initializeSkinsRestorer();
        }
    }

    /**
     * Initialise l'API SkinsRestorer si le plugin est présent
     */
    private void initializeSkinsRestorer() {
        if (Bukkit.getPluginManager().getPlugin("SkinsRestorer") == null) {
            plugin.getLogger().warning("SkinsRestorer is enabled in config but the plugin is not installed!");
            enabled = false;
            return;
        }

        try {
            skinsRestorer = SkinsRestorerProvider.get();
            plugin.getLogger().info("SkinsRestorer integration enabled for werewolf skins!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SkinsRestorer API: " + e.getMessage());
            enabled = false;
        }
    }

    /**
     * Initialise le skin d'un joueur dans la base de données SkinsRestorer
     * Doit être appelé quand un joueur rejoint le serveur pour s'assurer que son skin est bien enregistré
     */
    public void initializePlayerSkin(Player player) {
        if (!enabled || skinsRestorer == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SkinStorage skinStorage = skinsRestorer.getSkinStorage();
                
                // Vérifier si le joueur a déjà un skin enregistré
                skinStorage.findSkinData(player.getName()).ifPresentOrElse(
                    inputData -> {
                        // Le skin existe déjà, tout va bien
                        plugin.getLogger().fine("Player " + player.getName() + " already has a skin in the database");
                    },
                    () -> {
                        // Le skin n'existe pas, essayer de le récupérer depuis Mojang
                        try {
                            plugin.getLogger().info("Initializing skin for player " + player.getName() + " in SkinsRestorer database...");
                            
                            // Utiliser l'API SkinsRestorer pour récupérer le skin depuis Mojang
                            var skinDataResult = skinStorage.getPlayerSkin(player.getName(), false);
                            
                            skinDataResult.ifPresent(mojangSkinData -> {
                                plugin.getLogger().info("Successfully initialized skin for " + player.getName());
                            });
                            
                            if (!skinDataResult.isPresent()) {
                                plugin.getLogger().warning("Could not retrieve skin from Mojang for " + player.getName());
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to initialize skin for " + player.getName() + ": " + e.getMessage());
                        }
                    }
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking/initializing skin for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Change le skin d'un loup-garou pour le skin personnalisé
     */
    public void setWerewolfSkin(Player player) {
        if (!enabled || skinsRestorer == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Marquer le joueur comme ayant son skin modifié
                modifiedPlayers.add(player);
                
                // Appliquer le skin de loup-garou depuis l'URL ou nom de joueur
                SkinStorage skinStorage = skinsRestorer.getSkinStorage();
                skinStorage.findSkinData(werewolfSkinUrl).ifPresent(inputData -> {
                    SkinProperty werewolfSkin = inputData.getProperty();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        skinsRestorer.getSkinApplier(Player.class).applySkin(player, werewolfSkin);
                        plugin.getLogger().info("Applied werewolf skin to " + player.getName());
                    });
                });
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set werewolf skin for " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Restaure le skin original d'un joueur en appliquant son skin par son nom d'utilisateur
     */
    public void restoreOriginalSkin(Player player) {
        if (!enabled || skinsRestorer == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Utiliser le nom du joueur pour récupérer son skin original
                SkinStorage skinStorage = skinsRestorer.getSkinStorage();
                skinStorage.findSkinData(player.getName()).ifPresent(inputData -> {
                    SkinProperty originalSkin = inputData.getProperty();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        skinsRestorer.getSkinApplier(Player.class).applySkin(player, originalSkin);
                        plugin.getLogger().info("Restored original skin for " + player.getName() + " using their username");
                    });
                });
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore skin for " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Restaure tous les skins originaux (appelé en fin de partie)
     */
    public void restoreAllSkins() {
        if (!enabled || skinsRestorer == null) return;

        plugin.getLogger().info("Restoring skins for " + modifiedPlayers.size() + " player(s)...");
        
        // Restaurer les skins de tous les joueurs qui ont eu leur skin modifié
        for (Player player : new java.util.HashSet<>(modifiedPlayers)) {
            if (player != null && player.isOnline()) {
                restoreOriginalSkin(player);
            }
        }
        
        // Clear the tracking set immediately (no delay to avoid issues when plugin is being disabled)
        modifiedPlayers.clear();
        plugin.getLogger().info("All skins restored and tracking cleared");
    }

    /**
     * Nettoie les données d'un joueur (quand il quitte)
     */
    public void clearPlayerData(Player player) {
        modifiedPlayers.remove(player);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void reload() {
        this.enabled = plugin.getConfigManager().isSkinsRestorerEnabled();
        this.werewolfSkinUrl = plugin.getConfigManager().getWerewolfSkinUrl();
        
        if (enabled && skinsRestorer == null) {
            initializeSkinsRestorer();
        }
    }
}
