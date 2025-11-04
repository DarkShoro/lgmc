package fr.lightshoro.lgmc.gui;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.BrewingStandGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;

@SuppressWarnings("deprecation")
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

        BrewingStandGui gui = new BrewingStandGui(title);
        StaticPane pane1 = new StaticPane(0, 0, 1, 1);
        StaticPane pane2 = new StaticPane(0, 0, 1, 1);
        StaticPane pane3 = new StaticPane(0, 0, 1, 1);


        gm.setSorciereAction(false);

        //int slot = 3; // Start at position 3 (centered)

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

            pane1.addItem(vieItem, 0, 0);
            //gui.getFirstBottleComponent().setItem(vieItem, 0, 0);
            gui.getFirstBottleComponent().addPane(pane1);
        }

        // Ne rien faire
        ItemStack barrier = new ItemStack(Material.FEATHER);
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

        pane2.addItem(skipItem, 0, 0);
        //gui.getSecondBottleComponent().setItem(skipItem, 0, 0);
        gui.getSecondBottleComponent().addPane(pane2);

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
                
                // En mode clic, donner une gunpowder au lieu d'ouvrir le GUI
                if (plugin.getConfigManager().isClickVoteMode()) {
                    // Donner la poudre à canon
                    ItemStack gunpowder = new ItemStack(Material.GUNPOWDER);
                    org.bukkit.inventory.meta.ItemMeta gunMeta = gunpowder.getItemMeta();
                    if (gunMeta != null) {
                        gunMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.death-potion"));
                        gunpowder.setItemMeta(gunMeta);
                    }
                    sorciere.getInventory().setItem(4, gunpowder);
                    
                    // Donner aussi la plume de skip
                    ItemStack feather = new ItemStack(Material.FEATHER);
                    org.bukkit.inventory.meta.ItemMeta featherMeta = feather.getItemMeta();
                    if (featherMeta != null) {
                        featherMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.feather-skip"));
                        feather.setItemMeta(featherMeta);
                    }
                    sorciere.getInventory().setItem(8, feather);
                    
                    // Fermer l'inventaire après avoir donné les items
                    sorciere.closeInventory();
                } else {
                    sorciere.closeInventory();
                    openPoisonGUI(sorciere);
                }
            });

            pane3.addItem(mortItem, 0, 0);
            //gui.getThirdBottleComponent().setItem(mortItem, 0, 0);
            gui.getThirdBottleComponent().addPane(pane3);
        }

        // Marquer que le joueur a un GUI ouvert
        GamePlayer gamePlayer = gm.getGamePlayer(sorciere);
        if (gamePlayer != null) {
            gamePlayer.setGuiOpen(true);
        }

        gui.setOnClose(event -> {
            // Réinitialiser le flag GUI
            GamePlayer gp = gm.getGamePlayer(sorciere);
            if (gp != null) {
                gp.setGuiOpen(false);
            }
            
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

