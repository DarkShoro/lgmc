package fr.lightshoro.lgmc.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;

@SuppressWarnings("deprecation")
public class VoteCheckTask extends BukkitRunnable {
    private final Lgmc plugin;

    public VoteCheckTask(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        GameManager gm = plugin.getGameManager();

        if (!gm.isInGame()) {
            return;
        }

        // Check if everyone voted for capitaine
        if (gm.isCapitaineVoteInProg()) {
            boolean didEveryoneVote = true;

            for (Player player : gm.getPlayersAlive()) {
                GamePlayer gp = gm.getGamePlayer(player);
                if (!gp.isDidVoteForCapitaine()) {
                    didEveryoneVote = false;
                    break;
                }
            }

            if (didEveryoneVote) {
                plugin.getTimerManager().advanceTimer();
                Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("general.timer-advanced-capitaine"));
            }
        }

        // Check if everyone voted
        if (gm.isVoteInProg()) {
            boolean didEveryoneVote = true;

            for (Player player : gm.getPlayersAlive()) {
                GamePlayer gp = gm.getGamePlayer(player);
                if (!gp.isDidVote()) {
                    didEveryoneVote = false;
                    break;
                }
            }

            if (didEveryoneVote) {
                plugin.getTimerManager().advanceTimer();
                Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("general.timer-advanced-vote"));
            }
        }
    }
}

