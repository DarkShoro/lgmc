package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LGStopCommand implements CommandExecutor {
    private final Lgmc plugin;

    public LGStopCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager gm = plugin.getGameManager();

        if (!gm.isInGame()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.game-not-started"));
            return true;
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgstop.stopped"));
        gm.gameReset(true);

        return true;
    }
}

