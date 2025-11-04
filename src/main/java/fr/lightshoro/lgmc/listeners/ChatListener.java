package fr.lightshoro.lgmc.listeners;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final Lgmc plugin;

    public ChatListener(Lgmc plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check for <3 prefix to send to love chat
        if (message.startsWith("<3")) {
            if (plugin.getChatManager().isLover(player)) {
                event.setCancelled(true);
                // Remove the <3 prefix and any spaces after it
                String loveMessage = message.substring(2).trim();
                if (!loveMessage.isEmpty()) {
                    plugin.getChatManager().sendLoverChatMessage(player, loveMessage);
                } else {
                    player.sendMessage("§c❤ Votre message d'amour ne peut pas être vide !");
                }
            } else {
                event.setCancelled(true);
                player.sendMessage(plugin.getLanguageManager().getMessage("chat.not-lover"));
            }
            return;
        }

        // Check if player is a spectator (dead or not in game)
        if (plugin.getChatManager().isSpectator(player)) {
            event.setCancelled(true);
            plugin.getChatManager().sendSpectatorChatMessage(player, event.getMessage());
            return;
        }

        if (plugin.getChatManager().isLoupGarouChatActive()) {
            if (plugin.getChatManager().canPlayerSeeLGChat(player)) {
                event.setCancelled(true);
                plugin.getChatManager().sendLGChatMessage(player, event.getMessage());
            } else {
                // Hide the message from everyone
                event.setCancelled(true);
                player.sendMessage(plugin.getLanguageManager().getMessage("chat.cannot-chat"));
            }
        } else if (plugin.getGameManager().isNight()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().getMessage("chat.cannot-chat"));
        }
    }
}
