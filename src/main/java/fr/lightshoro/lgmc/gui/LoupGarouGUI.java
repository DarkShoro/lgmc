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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoupGarouGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public LoupGarouGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player loupGarou) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());
        // Retirer tous les loups-garous de la liste
        // En fait, si, les loups-garous peuvent se désigner entre eux
        // Permet le bluff et les stratégies


        // alivePlayers.removeAll(gm.getLoupGarous());

        int rows = alivePlayers.size() > 9 ? 2 : 1;
        ChestGui gui = new ChestGui(rows, plugin.getLanguageManager().getMessage("gui.loup-garou"));

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

                loupGarou.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.designated")
                                    .replace("{player}", target.getName()));

                // Ajouter la désignation
                gm.addDesignatedPlayer(target);
                gm.incrementDesignationCount();

                GamePlayer lgGp = gm.getGamePlayer(loupGarou);
                lgGp.setDidDesignation(true);

                // Incrémenter le compteur de désignation pour ce joueur
                gm.incrementPlayerDesignation(target);

                // Vérifier si tous les loups ont désigné
                int numLG = gm.getLoupGarous().size();
                if (gm.getDesignationCount() == numLG) {
                    plugin.getTimerManager().advanceTimer();
                }

                loupGarou.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        // Option "Ne rien faire"
        ItemStack barrier = new ItemStack(Material.BARRIER);
        barrier.getItemMeta().setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);

            GamePlayer lgGp = gm.getGamePlayer(loupGarou);
            lgGp.setDidDesignation(false);

            // Vérifier si tous les loups ont fait leur choix (ou passé)
            int numLG = gm.getLoupGarous().size();
            if (gm.getDesignationCount() + 1 == numLG) {
                plugin.getTimerManager().advanceTimer();
            }

            loupGarou.closeInventory();
        });

        pane.addItem(skipItem, slot % 9, slot / 9);

        gui.addPane(pane);

        // Deprecated! : Deux loup-garous peuvent fermer le menu pour discuté dans le chat
        /*gui.setOnClose(event -> {
            GamePlayer lgGp = gm.getGamePlayer(loupGarou);
            if (!lgGp.isDidDesignation()) {
                loupGarou.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.no-target"));
                lgGp.setDidDesignation(false);

                int numLG = gm.getLoupGarous().size();
                if (gm.getDesignationCount() + 1 == numLG) {
                    plugin.getTimerManager().advanceTimer();
                }
            }
        });*/

        gui.show(loupGarou);
    }
}

