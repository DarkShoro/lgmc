package fr.lightshoro.lgmc.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class SorcierePoisonGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public SorcierePoisonGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player sorciere) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());

        // Retirer la cible des loups-garous si elle existe
        Player lgTarget = gm.getNextLGTarget();
        if (lgTarget != null) {
            alivePlayers.remove(lgTarget);
        }

        int rows = alivePlayers.size() > 9 ? 2 : 1;
        ChestGui gui = new ChestGui(rows, plugin.getLanguageManager().getMessage("gui.sorciere-poison"));

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

                sorciere.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.poisoned")
                                   .replace("{player}", target.getName()));

                gm.waitListAdd(target, "sorciere");
                gm.setPotionMort(true);
                gm.setSorciereAction(true);
                plugin.getTimerManager().advanceTimer();
                sorciere.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        gui.addPane(pane);

        // Option "Ne rien faire"
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));
            barrier.setItemMeta(barrierMeta);
        }

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);
            gm.setSorciereAction(false);
            sorciere.closeInventory();
        });

        pane.addItem(skipItem, slot % 9, slot / 9);

        gui.addPane(pane);

        gui.setOnClose(event -> {
            if (!gm.isSorciereAction()) {
                sorciere.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.no-poison"));
            }
            plugin.getTimerManager().advanceTimer();
        });

        gm.setSorciereAction(false);
        gui.show(sorciere);
    }
}

