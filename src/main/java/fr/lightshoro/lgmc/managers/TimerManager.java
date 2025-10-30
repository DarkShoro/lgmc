package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerManager {
    private final Lgmc plugin;
    private BossBar timerBar;
    private int timerTime;
    private int timerMax;
    private BukkitRunnable timerTask;

    public TimerManager(Lgmc plugin) {
        this.plugin = plugin;
    }

    public void defineTimer(String timerName, int timerTime) {
        clearTimer();

        new BukkitRunnable() {
            @Override
            public void run() {
                timerBar = Bukkit.createBossBar(timerName, BarColor.RED, BarStyle.SOLID);
                timerBar.setVisible(true);
                timerBar.setProgress(1.0);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    timerBar.addPlayer(player);
                }

                TimerManager.this.timerMax = timerTime;
                TimerManager.this.timerTime = timerTime;

                startTimerTask();
            }
        }.runTaskLater(plugin, 20L);
    }

    private void startTimerTask() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timerTime <= 0) {
                    clearTimer();
                    plugin.getGameManager().nextStep();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.closeInventory();
                    }
                    cancel();
                } else {
                    timerTime--;
                    if (timerBar != null && timerMax > 0) {
                        double progress = (double) timerTime / timerMax;
                        timerBar.setProgress(Math.max(0, Math.min(1, progress)));
                    }
                }
            }
        };
        timerTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void clearTimer() {
        if (timerBar != null) {
            timerBar.removeAll();
            timerBar.setVisible(false);
            timerBar = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timerTime = 0;
        timerMax = 0;
    }

    public void advanceTimer() {
        if (timerTime == 0) {
            clearTimer();
            return;
        }
        if (timerTime > 5) {
            timerTime = 5;
        }
    }

    public int getTimerTime() {
        return timerTime;
    }

    public int getTimerMax() {
        return timerMax;
    }
}

