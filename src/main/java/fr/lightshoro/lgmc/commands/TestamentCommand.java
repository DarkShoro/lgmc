package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestamentCommand implements CommandExecutor {
    private final Lgmc plugin;

    public TestamentCommand(Lgmc plugin) {
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

        if (!gm.isCapitaineSuccession()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.testament.not-dying"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.testament.usage"));
            return true;
        }

        if (!player.equals(gm.getCapitaine())) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.testament.not-capitaine"));
            return true;
        }

        Player successor = Bukkit.getPlayer(args[0]);
        if (successor == null || !successor.isOnline()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.testament.player-not-found"));
            return true;
        }

        if (successor.equals(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.testament.not-dying"));
            return true;
        }

        if (!gm.getPlayersAlive().contains(successor)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.testament.player-dead"));
            return true;
        }

        // Nommer le successeur
        gm.setDyingCapitaine(player);
        gm.setCapitaine(successor);

        // Donne la palme au successeur
        successor.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("capitaine"));
        // Retire la palme au capitaine mourant
        player.getInventory().setHelmet(null);

        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("succession.testament-chosen")
                              .replace("{dying}", player.getName())
                              .replace("{new}", successor.getName()));

        gm.setCapitaineSuccession(false);
        plugin.getTimerManager().advanceTimer();

        return true;
    }
}

