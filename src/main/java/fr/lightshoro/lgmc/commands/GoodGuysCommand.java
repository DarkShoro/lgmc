package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GoodGuysCommand implements CommandExecutor {
    private final Lgmc plugin;

    public GoodGuysCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        GameManager gm = plugin.getGameManager();

        // This is a debug command - show alive players
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.goodguys.header"));
        for (Player p : gm.getPlayersAlive()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.goodguys.player")
                .replace("{player}", p.getName()));
        }

        return true;
    }
}

