package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
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
               player.equals(plugin.getGameManager().getPetiteFille()) ||
               isSpectator(player);
    }

    public void sendLGChatMessage(Player sender, String message) {
        Style boldName = Style.style(NamedTextColor.RED, TextDecoration.BOLD);

        // Message pour les loups-garous (avec pseudo réel)
        Component lgMessage = Component.text()
                .append(Component.text(sender.getName(), boldName))
                .append(Component.text(" >> ", NamedTextColor.YELLOW))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build();

        // Message pour la petite fille et les spectateurs (pseudo anonyme)
        Component anonymousMessage = Component.text()
                .append(Component.text(plugin.getLanguageManager().getMessage("roles.loup-garou.name"), boldName))
                .append(Component.text(" >> ", NamedTextColor.YELLOW))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build();

        for (Player player : plugin.getGameManager().getPlayersAlive()) {
            if (canPlayerSeeLGChat(player)) {
                // Les loups-garous voient les pseudos réels
                if (plugin.getGameManager().getLoupGarous().contains(player)) {
                    player.sendMessage(lgMessage);
                }
                // La petite fille et les spectateurs voient le message anonyme
                else {
                    player.sendMessage(anonymousMessage);
                }
            }
        }

        // Les spectateurs morts peuvent aussi voir le chat
        for (Player player : plugin.getGameManager().getPlayingPlayers()) {
            if (isSpectator(player) && !plugin.getGameManager().getPlayersAlive().contains(player)) {
                player.sendMessage(anonymousMessage);
            }
        }
    }

    public boolean isSpectator(Player player) {
        if (!plugin.getGameManager().isInGame()) {
            return false;
        }

        GamePlayer gamePlayer = plugin.getGameManager().getGamePlayer(player);
        if (gamePlayer == null) {
            return false;
        }

        Role role = gamePlayer.getRole();
        return role == Role.DEAD || role == Role.NOT_IN_GAME;
    }

    public void sendSpectatorChatMessage(Player sender, String message) {
        Style spectatorStyle = Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC, TextDecoration.BOLD);
        Component formattedMessage = Component.text()
                .append(Component.text("[" + plugin.getLanguageManager().getMessage("roles.spectator.name") + "] ", spectatorStyle))
                .append(Component.text(sender.getName(), Style.style(NamedTextColor.GRAY, TextDecoration.BOLD)))
                .append(Component.text(" >> ", NamedTextColor.DARK_GRAY))
                .append(Component.text(message, NamedTextColor.GRAY))
                .build();

        for (Player player : plugin.getGameManager().getPlayingPlayers()) {
            if (isSpectator(player)) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    public boolean isLover(Player player) {
        if (!plugin.getGameManager().isInGame()) {
            return false;
        }

        return plugin.getGameManager().getLesAmoureux().contains(player) &&
               plugin.getGameManager().getLesAmoureux().size() == 2;
    }

    public void sendLoverChatMessage(Player sender, String message) {
        Style loverStyle = Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);
        Component formattedMessage = Component.text()
                .append(Component.text("♥ ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(sender.getName(), loverStyle))
                .append(Component.text(" >> ", NamedTextColor.DARK_PURPLE))
                .append(Component.text(message, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" ♥", NamedTextColor.LIGHT_PURPLE))
                .build();

        for (Player lover : plugin.getGameManager().getLesAmoureux()) {
            lover.sendMessage(formattedMessage);
        }
    }
}
