package fr.lightshoro.lgmc.listeners;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;


public class GameListener implements Listener {
    private final Lgmc plugin;

    public GameListener(Lgmc plugin) {
        this.plugin = plugin;
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
}
