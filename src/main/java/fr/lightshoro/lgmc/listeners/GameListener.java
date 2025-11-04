package fr.lightshoro.lgmc.listeners;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;


public class GameListener implements Listener {
    private final Lgmc plugin;

    public GameListener(Lgmc plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setServerIcon(plugin.serverIcon);
        event.setMaxPlayers(plugin.getLocationManager().getMaxPlayers());
        event.motd(plugin.getMotdManager().getCurrentMotd());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Si une partie est en cours et que le joueur n'a pas de GamePlayer, le créer avec le rôle NOT_IN_GAME
        if (plugin.getGameManager().isInGame()) {
            GamePlayer gamePlayer = plugin.getGameManager().getGamePlayer(player);
            if (gamePlayer == null || gamePlayer.getRole() == Role.NOT_IN_GAME) {
                plugin.getGameManager().createGamePlayer(player, Role.NOT_IN_GAME);
                player.sendMessage(plugin.getLanguageManager().getMessage("general.game-in-progress"));

                // Configurer immédiatement la visibilité pour ce nouveau joueur NOT_IN_GAME
                // La tâche périodique s'en occupera également, mais on le fait tout de suite pour éviter les délais
                for (Player other : plugin.getServer().getOnlinePlayers()) {
                    if (player.equals(other)) continue;

                    GamePlayer otherGamePlayer = plugin.getGameManager().getGamePlayer(other);
                    if (otherGamePlayer == null) continue;

                    Role otherRole = otherGamePlayer.getRole();
                    boolean isOtherSpectator = (otherRole == Role.NOT_IN_GAME || otherRole == Role.DEAD);

                    // Le nouveau joueur NOT_IN_GAME voit les autres spectateurs et les joueurs vivants
                    player.showPlayer(plugin, other);

                    // Les joueurs vivants ne voient pas le nouveau spectateur
                    if (!isOtherSpectator) {
                        other.hidePlayer(plugin, player);
                    } else {
                        // Les autres spectateurs voient le nouveau spectateur
                        other.showPlayer(plugin, player);
                    }
                }
            }
        }

        // Configuration du pack de ressources pour le joueur
        plugin.getResourcePackManager().applyResourcePack(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Vérifier si le joueur doit être gelé
        if (plugin.getGameManager().isFreezeAll()) {
            GamePlayer gamePlayer = plugin.getGameManager().getGamePlayer(player);

            // Les joueurs NOT_IN_GAME et DEAD peuvent se déplacer librement
            if (gamePlayer != null &&
                (gamePlayer.getRole() == Role.NOT_IN_GAME || gamePlayer.getRole() == Role.DEAD)) {
                return; // Autoriser le mouvement
            }

            // Ne bloquer que les mouvements de position, pas la rotation de la caméra
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (plugin.getGameManager().isFreezeAll()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Only block commands during an active game
        if (!plugin.getGameManager().isInGame()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        // Extract command without the leading slash
        String[] parts = message.substring(1).split(" ");
        String command = parts[0];

        // List of blocked vanilla messaging commands
        String[] blockedCommands = {
            "tell", "msg", "w", "whisper",           // Private messages
            "me",                                     // Action messages
            "teammsg", "tm",                          // Team messages
            "minecraft:tell", "minecraft:msg",        // Namespaced versions
            "minecraft:w", "minecraft:me",
            "minecraft:teammsg", "minecraft:tm"
        };

        // Check if command is in blocked list
        for (String blocked : blockedCommands) {
            if (command.equals(blocked)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.blocked-during-game"));
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Si une partie n'est pas en cours, ne rien faire
        if (!plugin.getGameManager().isInGame()) {
            return;
        }

        Player quittingPlayer = event.getPlayer();
        GamePlayer gamePlayer = plugin.getGameManager().getGamePlayer(quittingPlayer);

        // Si le joueur n'a pas de GamePlayer ou est déjà mort/spectateur, ne rien faire
        if (gamePlayer == null || gamePlayer.getRole() == Role.DEAD || gamePlayer.getRole() == Role.NOT_IN_GAME) {
            return;
        }

        // Le joueur est vivant et quitte la partie - le traiter comme une mort

        // Annoncer la déconnexion
        plugin.getServer().broadcastMessage(
            plugin.getLanguageManager().getMessage("general.player-disconnect")
                .replace("{player}", quittingPlayer.getName())
        );

        // Utiliser la méthode killPlayer existante qui gère tout :
        // - Retrait des rôles spéciaux
        // - Gestion des amoureux
        // - Décrémentation des compteurs
        // - Vérification des conditions de victoire
        // - Attribution aléatoire du capitaine si le joueur déconnecté était capitaine
        plugin.getGameManager().killPlayer(quittingPlayer, "disconnect");
    }

    /**
     * Empêche les joueurs de déplacer les casques de rôle dans leur inventaire
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Ne bloquer que pendant une partie en cours
        if (!plugin.getGameManager().isInGame()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Vérifier si c'est l'inventaire du joueur
        if (event.getClickedInventory() == null) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Empêcher le déplacement du casque (slot helmet)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 39) {
            // Slot 39 = helmet
            event.setCancelled(true);
            return;
        }

        // Empêcher le déplacement d'items pertinents (papier, houes, livre, plume, etc.)
        if (clickedItem != null && isRelevantItem(clickedItem)) {
            event.setCancelled(true);
            return;
        }

        // Empêcher de mettre un item pertinent sur le curseur vers un autre slot
        if (cursorItem != null && isRelevantItem(cursorItem)) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Empêche les joueurs de jeter les casques et items pertinents
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // Ne bloquer que pendant une partie en cours
        if (!plugin.getGameManager().isInGame()) {
            return;
        }

        ItemStack droppedItem = event.getItemDrop().getItemStack();

        // Empêcher de jeter un item pertinent
        if (isRelevantItem(droppedItem)) {
            event.setCancelled(true);
        }
    }

    /**
     * Vérifie si un item est un item "pertinent" du jeu (casque de rôle ou item d'action)
     */
    private boolean isRelevantItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        Material type = item.getType();

        // Carved pumpkin (casque des morts) - toujours protégé
        if (type == Material.CARVED_PUMPKIN) {
            return true;
        }

        // Casques de rôle (lire depuis la config)
        String[] roles = {
            "loup-garou", "villageois", "petite-fille", "voyante", 
            "sorciere", "cupidon", "chasseur", "voleur", "ange", "capitaine"
        };
        
        for (String role : roles) {
            String helmetMaterial = plugin.getConfigManager().getRoleHelmet(role);
            try {
                Material configMaterial = Material.valueOf(helmetMaterial.toUpperCase());
                if (type == configMaterial) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                // Ignorer les matériaux invalides
            }
        }

        // Items d'action du jeu
        if (type == Material.PAPER ||           // Vote
            type == Material.WOODEN_HOE ||      // Chasseur
            type == Material.IRON_HOE ||        // Loup-garou
            type == Material.BOOK ||            // Sorcière
            type == Material.WRITTEN_BOOK ||    // Testament
            type == Material.FEATHER ||         // Skip
            type == Material.PLAYER_HEAD ||     // Têtes de joueurs dans GUI
            type == Material.GUNPOWDER ||       // Voleur / Sorcière poison
            type == Material.FLINT) {           // Voyante
            return true;
        }

        return false;
    }
}
