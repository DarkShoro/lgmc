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

import java.util.Arrays;

public class SorciereGUI {
    private final Lgmc plugin;
    private final GameManager gm;
    private boolean openSecond = false;

    public SorciereGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player sorciere) {
        Player target = gm.getNextLGTarget();
        String title;

        if (target != null) {
            title = plugin.getLanguageManager().getMessage("actions.sorciere.target-info")
                   .replace("{player}", target.getName());
        } else {
            title = plugin.getLanguageManager().getMessage("gui.sorciere");
        }

        ChestGui gui = new ChestGui(1, title);
        StaticPane pane = new StaticPane(0, 0, 9, 1);

        gm.setSorciereAction(false);

        int slot = 3; // Start at position 3 (centered)

        // Potion de vie
        if (!gm.isPotionVie()) {
            ItemStack potionVie = new ItemStack(Material.SUGAR);
            ItemMeta meta = potionVie.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.life-potion"));
                if (target != null) {
                    meta.setLore(Arrays.asList(plugin.getLanguageManager().getMessage("gui.items.life-potion-lore")
                        .replace("{player}", target.getName())));
                }
                potionVie.setItemMeta(meta);
            }

            GuiItem vieItem = new GuiItem(potionVie, event -> {
                event.setCancelled(true);

                if (target != null) {
                    sorciere.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.saved")
                                       .replace("{player}", target.getName()));
                    gm.setNextLGTarget(null);
                    gm.waitListRemove(target);
                    gm.setPotionVie(true);
                } else {
                    sorciere.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.no-action"));
                }

                gm.setSorciereAction(true);
                plugin.getTimerManager().advanceTimer();
                sorciere.closeInventory();
            });

            pane.addItem(vieItem, slot, 0);
        }

        // Avance d'un slot pour le centre
        slot++;

        // Ne rien faire
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip-gray"));
            barrier.setItemMeta(barrierMeta);
        }

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);
            gm.setSorciereAction(false);
            sorciere.closeInventory();
        });

        pane.addItem(skipItem, slot, 0);

        // Avance d'un slot pour le côté droit
        slot++;

        // Potion de mort
        if (!gm.isPotionMort()) {
            ItemStack potionMort = new ItemStack(Material.GUNPOWDER);
            ItemMeta mortMeta = potionMort.getItemMeta();
            if (mortMeta != null) {
                mortMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.death-potion"));
                mortMeta.setLore(Arrays.asList(plugin.getLanguageManager().getMessage("gui.items.death-potion-lore")));
                potionMort.setItemMeta(mortMeta);
            }

            GuiItem mortItem = new GuiItem(potionMort, event -> {
                event.setCancelled(true);
                gm.setSorciereAction(true);
                openSecond = true;
                sorciere.closeInventory();
                openPoisonGUI(sorciere);
            });

            pane.addItem(mortItem, slot, 0);
        }

        gui.addPane(pane);

        gui.setOnClose(event -> {
            if (!gm.isSorciereAction()) {
                sorciere.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.no-action"));
            }
            if (!openSecond) {
                plugin.getTimerManager().advanceTimer();
            }
        });

        gui.show(sorciere);
    }

    private void openPoisonGUI(Player sorciere) {
        new SorcierePoisonGUI(plugin).open(sorciere);
    }
}

