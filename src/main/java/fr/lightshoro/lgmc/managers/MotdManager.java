package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/*
    * Gestionnaire centralisé pour le Message of the Day (MOTD)
    * Affiche des informations personnalisées lors du ping du serveur
 */
public class MotdManager {
    private final Lgmc plugin;
    private final GameManager gameManager;
    public MotdManager(Lgmc plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
    }

    public Component getCurrentMotd() {

        // Si le nombre de joueurs est supérieur ou égal à 12, on affiche "Serveur plein"
        int playerCount = plugin.getServer().getOnlinePlayers().size();

        if (playerCount >= 12) {
            return LegacyComponentSerializer.legacySection().deserialize(plugin.getLanguageManager().getMessage("motd.full"));
        }

        // Sinon, on affiche le statut actuel du jeu
        if (gameManager.isInGame()) {
            return LegacyComponentSerializer.legacySection().deserialize(plugin.getLanguageManager().getMessage("motd.in-progress"));
        }

        // Si le jeu est en cours de démarrage, on affiche "Démarrage en cours"
        if (gameManager.getIsStarting()) {
            return LegacyComponentSerializer.legacySection().deserialize(plugin.getLanguageManager().getMessage("motd.starting"));
        }

        // Par défaut, on affiche "En attente de joueurs"
        String waitingMessage = plugin.getLanguageManager().getMessage("motd.waiting");
        // replace {players} par le nombre actuel de joueurs
        waitingMessage = waitingMessage.replace("{players}", String.valueOf(playerCount));
        return LegacyComponentSerializer.legacySection().deserialize(waitingMessage);
    }
}
