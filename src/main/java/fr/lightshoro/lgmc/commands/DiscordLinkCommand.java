package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.WebsocketManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordLinkCommand implements CommandExecutor {

    private final Lgmc plugin;
    private final WebsocketManager websocketManager;

    public DiscordLinkCommand(Lgmc plugin) {
        this.plugin = plugin;
        this.websocketManager = plugin.getWebsocketManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Only players can run this command
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        if (plugin.getWebsocketManager().isDisabled()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.linkDiscord.websocket-disabled"));
            return true;
        }

        // Check if UUID argument is provided
        if (args.length != 1) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.linkDiscord.usage"));
            return true;
        }

        String uuid = args[0];

        // Check if WebSocket is connected
        if (!websocketManager.isConnected()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.linkDiscord.websocket-error"));
            return true;
        }

        // Send the link request to the Discord bot
        websocketManager.sendLink(uuid, sender.getName());
        return true;
    }


}

