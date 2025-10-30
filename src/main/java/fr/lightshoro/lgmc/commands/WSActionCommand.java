package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.WebsocketManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class WSActionCommand implements CommandExecutor {

    private final Lgmc plugin;
    private final WebsocketManager websocketManager;

    public WSActionCommand(Lgmc plugin) {
        this.plugin = plugin;
        this.websocketManager = plugin.getWebsocketManager();
    }

    // valid actions:
    // gamestart

    private ArrayList <String> validActions = new ArrayList<String>() {{
        add("gamestart");
        add("muteall");
        add("demuteall");
        add("player_died");
        add("gameover");
    }};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Only players can run this command
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        if (plugin.getWebsocketManager().isDisabled()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.websocket-disabled"));
            return true;
        }

        // Check if action argument is provided
        if (args.length < 1) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.usage"));
            return true;
        }
        String action = args[0];
        // Check if action is valid
        if (!validActions.contains(action)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.invalid-action"));
            return true;
        }

        // Check if WebSocket is connected
        if (!websocketManager.isConnected()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.websocket-error"));
            return true;
        }

        switch (action) {
            case "gamestart":
                websocketManager.sendGameStart();
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.gamestart.sent"));
                break;
            case "muteall":
                websocketManager.sendMuteAll();
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.muteall.sent"));
                break;
            case "demuteall":
                websocketManager.sendDemuteAll();
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.demuteall.sent"));
                break;
            case "player_died":
                if (args.length != 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.player-died.usage"));
                    return true;
                }
                String playerName = args[1];
                websocketManager.sendPlayerDied(playerName);
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.player-died.sent").replace("{player}", playerName));
                break;
            case "gameover":
                websocketManager.sendGameOver();
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.gameover.sent"));
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.wsaction.invalid-action"));
                break;
        }

        return true;

    }


}

