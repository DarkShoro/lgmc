package fr.lightshoro.lgmc.tasks;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Tâche périodique pour gérer la visibilité des joueurs NOT_IN_GAME et DEAD
 * Ces joueurs doivent être visibles entre eux mais invisibles pour les joueurs vivants
 */
public class VisibilityTask extends BukkitRunnable {
    private final Lgmc plugin;

    public VisibilityTask(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        GameManager gm = plugin.getGameManager();

        // Ne rien faire si aucune partie n'est en cours
        if (!gm.isInGame()) {
            return;
        }

        // Pour chaque joueur en ligne
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            GamePlayer gamePlayer = gm.getGamePlayer(player);
            if (gamePlayer == null) continue;

            Role playerRole = gamePlayer.getRole();
            boolean isSpectator = (playerRole == Role.NOT_IN_GAME || playerRole == Role.DEAD);

            // Pour chaque autre joueur
            for (Player other : plugin.getServer().getOnlinePlayers()) {
                if (player.equals(other)) continue;

                GamePlayer otherGamePlayer = gm.getGamePlayer(other);
                if (otherGamePlayer == null) continue;

                Role otherRole = otherGamePlayer.getRole();
                boolean isOtherSpectator = (otherRole == Role.NOT_IN_GAME || otherRole == Role.DEAD);

                if (isSpectator) {
                    // Les spectateurs (NOT_IN_GAME et DEAD) voient tous les autres spectateurs
                    if (isOtherSpectator) {
                        player.showPlayer(plugin, other);
                    } else {
                        // Les spectateurs voient aussi les joueurs vivants
                        player.showPlayer(plugin, other);
                    }
                } else {
                    // Les joueurs vivants ne voient PAS les spectateurs
                    if (isOtherSpectator) {
                        player.hidePlayer(plugin, other);
                    }
                    // La visibilité entre joueurs vivants est gérée par le code existant
                }
            }
        }
    }
}

