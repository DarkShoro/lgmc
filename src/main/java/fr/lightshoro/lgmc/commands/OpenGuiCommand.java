package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.gui.SorciereGUI;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class OpenGuiCommand implements CommandExecutor {
    private final Lgmc plugin;

    public OpenGuiCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    // valid guis arguments:
    // sorciere -> SorciereGUI

    public static final Map<String, String> VALID_GUIS = Map.of(
        "sorciere", "SorciereGUI"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        GameManager gm = plugin.getGameManager();

        if (args.length == 0 || !VALID_GUIS.containsKey(args[0].toLowerCase())) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.opengui.usage")
                .replace("{guis}", String.join(", ", VALID_GUIS.keySet())));
            return true;
        }

        String guiName = VALID_GUIS.get(args[0].toLowerCase());

        switch (guiName) {
            case "SorciereGUI" -> {
                SorciereGUI sorciereGUI = new SorciereGUI(plugin);
                sorciereGUI.open(player);
            }
            default -> player.sendMessage(plugin.getLanguageManager().getMessage("commands.opengui.usage")
                .replace("{guis}", String.join(", ", VALID_GUIS.keySet())));
        }





        return true;
    }
}

