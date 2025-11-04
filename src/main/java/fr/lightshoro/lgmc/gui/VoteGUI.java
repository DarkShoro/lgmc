package fr.lightshoro.lgmc.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;

@SuppressWarnings("deprecation")
public class VoteGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public VoteGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player voter) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());

        // Calculer le nombre de lignes nécessaires (incluant la barrière)
        int totalSlots = alivePlayers.size() + 1; // +1 pour la barrière
        int rows = totalSlots > 9 ? 2 : 1;
        ChestGui gui = new ChestGui(rows, plugin.getLanguageManager().getMessage("gui.vote"));

        StaticPane pane = new StaticPane(0, 0, 9, rows);

        int slot = 0;
        for (Player target : alivePlayers) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(ChatColor.YELLOW + target.getName());
                skull.setItemMeta(meta);
            }

            GuiItem item = new GuiItem(skull, event -> {
                event.setCancelled(true);

                voter.sendMessage(plugin.getLanguageManager().getMessage("vote.day.voted-for")
                                .replace("{player}", target.getName()));

                // Ajouter l'effet glow sur le joueur voté (visible uniquement par le voteur)
                plugin.getVoteDisplayManager().setGlowEffect(voter, target);

                // Incrémenter le vote pour ce joueur (gère automatiquement le changement de vote)
                gm.incrementVoteCount(target, voter);
                
                // Mettre à jour l'affichage des votes pour tous les joueurs
                plugin.getVoteDisplayManager().updateAllVoteDisplays();

                voter.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        // Option "Ne rien faire" - toujours en dernière position
        ItemStack barrier = new ItemStack(Material.FEATHER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));
            barrier.setItemMeta(barrierMeta);
        }

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);
            voter.sendMessage(plugin.getLanguageManager().getMessage("vote.day.no-vote"));

            GamePlayer gp = gm.getGamePlayer(voter);
            Player previousVote = gp.getVotedPlayer();
            
            // Si le joueur avait voté, décrémenter le vote précédent
            if (previousVote != null) {
                int voteValue = (voter.equals(gm.getCapitaine())) ? 2 : 1;
                int currentVotes = gm.getVoteCount().getOrDefault(previousVote, 0);
                if (currentVotes >= voteValue) {
                    gm.getVoteCount().put(previousVote, currentVotes - voteValue);
                }
                // Retirer l'effet glow
                plugin.getVoteDisplayManager().removeGlowEffect(voter, previousVote);
                // Mettre à jour l'affichage
                plugin.getVoteDisplayManager().updateAllVoteDisplays();
            }
            
            // Marquer comme ayant voté mais sans vote enregistré
            gp.setVotedPlayer(null);
            gp.setDidVote(true);

            voter.closeInventory();
        });

        // Placer la barrière au dernier slot (slot 8 pour 1 ligne, slot 17 pour 2 lignes)
        int barrierSlot = (rows * 9) - 1;
        pane.addItem(skipItem, barrierSlot % 9, barrierSlot / 9);

        gui.addPane(pane);

        // Marquer que le joueur a un GUI ouvert
        GamePlayer gamePlayer = gm.getGamePlayer(voter);
        if (gamePlayer != null) {
            gamePlayer.setGuiOpen(true);
        }

        // Ajouter un listener pour réinitialiser le flag quand le GUI se ferme
        gui.setOnClose(event -> {
            GamePlayer gp = gm.getGamePlayer(voter);
            if (gp != null) {
                gp.setGuiOpen(false);
            }
        });

        gui.show(voter);
    }
}

