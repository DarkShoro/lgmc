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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class CapitaineVoteGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public CapitaineVoteGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player voter) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());

        int rows = alivePlayers.size() > 9 ? 2 : 1;
        ChestGui gui = new ChestGui(rows, plugin.getLanguageManager().getMessage("gui.vote-capitaine"));

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

                voter.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.voted-for")
                                .replace("{player}", target.getName()));

                // Incrémenter le vote pour ce joueur
                gm.incrementCapitaineVote(target);

                GamePlayer gp = gm.getGamePlayer(voter);
                gp.setDidVoteForCapitaine(true);

                voter.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        // Option "Ne rien faire"
        ItemStack barrier = new ItemStack(Material.BARRIER);
        barrier.getItemMeta().setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);
            voter.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.no-vote"));

            GamePlayer gp = gm.getGamePlayer(voter);
            gp.setDidVoteForCapitaine(true);

            voter.closeInventory();
        });

        pane.addItem(skipItem, slot % 9, slot / 9);

        gui.addPane(pane);


        gui.show(voter);
    }
}

