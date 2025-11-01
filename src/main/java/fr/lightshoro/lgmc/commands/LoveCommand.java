package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoveCommand implements CommandExecutor {
    private final Lgmc plugin;

    public LoveCommand(Lgmc plugin) {
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

        if (!gm.isInGame()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.game-not-started"));
            return true;
        }

        if (!plugin.getChatManager().isLover(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.love.not-lover"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.love.usage"));
            return true;
        }

        String message = String.join(" ", args);
        plugin.getChatManager().sendLoverChatMessage(player, message);
        return true;
    }
}

