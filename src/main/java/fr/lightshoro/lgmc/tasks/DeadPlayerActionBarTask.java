package fr.lightshoro.lgmc.tasks;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Tâche périodique pour afficher un message dans l'action bar des joueurs morts
 * Le message reste affiché jusqu'à la fin de la partie
 */
public class DeadPlayerActionBarTask extends BukkitRunnable {
    private final Lgmc plugin;

    public DeadPlayerActionBarTask(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        GameManager gm = plugin.getGameManager();

        // Ne rien faire si aucune partie n'est en cours
        if (!gm.isInGame()) {
            return;
        }

        String deadMessage = plugin.getLanguageManager().getMessage("general.you-are-dead");

        // Pour chaque joueur en ligne
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            GamePlayer gamePlayer = gm.getGamePlayer(player);
            if (gamePlayer == null) continue;

            Role playerRole = gamePlayer.getRole();

            // Si le joueur est mort, afficher le message dans l'action bar
            if (playerRole == Role.DEAD) {
                player.sendActionBar(deadMessage);
            }
        }
    }
}

