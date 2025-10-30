package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LGStartCommand implements CommandExecutor {
    private final Lgmc plugin;

    public LGStartCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager gm = plugin.getGameManager();

        if (gm.isInGame()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.game-already-started"));
            return true;
        }

        int playerCount = Bukkit.getOnlinePlayers().size();
        if (playerCount < 4) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("general.not-enough-players"));
            return true;
        }

        if (playerCount > 12) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("general.too-many-players"));
            return true;
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgstart.started"));
        gm.startGame();

        return true;
    }
}

