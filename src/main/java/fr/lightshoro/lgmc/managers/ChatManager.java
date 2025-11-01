package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class ChatManager {
    private final Lgmc plugin;
    private boolean loupGarouChatActive;

    public ChatManager(Lgmc plugin) {
        this.plugin = plugin;
    }

    public boolean isLoupGarouChatActive() {
        return loupGarouChatActive;
    }

    public void setLoupGarouChatActive(boolean active) {
        this.loupGarouChatActive = active;
    }

    public boolean canPlayerSeeLGChat(Player player) {
        return plugin.getGameManager().getLoupGarous().contains(player) ||
               player.equals(plugin.getGameManager().getPetiteFille());
    }

    public void sendLGChatMessage(Player sender, String message) {
        Component formattedMessage;
        Style boldName = Style.style(NamedTextColor.RED, TextDecoration.BOLD);

        if (plugin.getGameManager().getLoupGarous().contains(sender)) {
            formattedMessage = Component.text()
                    .append(Component.text(sender.getName(), boldName))
                    .append(Component.text(" >> ", NamedTextColor.YELLOW))
                    .append(Component.text(message, NamedTextColor.WHITE))
                    .build();
        } else {
            formattedMessage = Component.text()
                    .append(Component.text(plugin.getLanguageManager().getMessage("roles.loup-garou.name"), boldName))
                    .append(Component.text(" >> ", NamedTextColor.YELLOW))
                    .append(Component.text(message, NamedTextColor.WHITE))
                    .build();
        }

        for (Player player : plugin.getGameManager().getPlayersAlive()) {
            if (canPlayerSeeLGChat(player)) {
                player.sendMessage(formattedMessage);
            }
        }
    }
}
