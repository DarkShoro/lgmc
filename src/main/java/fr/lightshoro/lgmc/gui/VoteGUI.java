package fr.lightshoro.lgmc.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

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

                // Incrémenter le vote pour ce joueur (double si voteur est capitaine)
                gm.incrementVoteCount(target, voter);

                GamePlayer gp = gm.getGamePlayer(voter);
                gp.setDidVote(true);

                voter.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        // Option "Ne rien faire" - toujours en dernière position
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));
            barrier.setItemMeta(barrierMeta);
        }

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);
            voter.sendMessage(plugin.getLanguageManager().getMessage("vote.day.no-vote"));

            GamePlayer gp = gm.getGamePlayer(voter);
            gp.setDidVote(true);

            voter.closeInventory();
        });

        // Placer la barrière au dernier slot (slot 8 pour 1 ligne, slot 17 pour 2 lignes)
        int barrierSlot = (rows * 9) - 1;
        pane.addItem(skipItem, barrierSlot % 9, barrierSlot / 9);

        gui.addPane(pane);


        gui.show(voter);
    }
}

