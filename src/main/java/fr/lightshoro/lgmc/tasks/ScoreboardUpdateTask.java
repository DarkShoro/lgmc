package fr.lightshoro.lgmc.tasks;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Tâche périodique pour mettre à jour les scoreboards de tous les joueurs
 */
public class ScoreboardUpdateTask extends BukkitRunnable {
    private final Lgmc plugin;

    public ScoreboardUpdateTask(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getScoreboardManager().updateScoreboards();
    }
}

