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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class CupidonGUI {
    private final Lgmc plugin;
    private final GameManager gm;
    private Player firstLover = null;

    public CupidonGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player cupidon) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());

        ChestGui gui = new ChestGui(2, plugin.getLanguageManager().getMessage("gui.cupidon-first"));

        StaticPane pane = new StaticPane(0, 0, 9, 2);

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

                if (firstLover == null) {
                    // Premier amoureux sélectionné
                    firstLover = target;
                    cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.first-lover")
                                      .replace("{player}", target.getName()));
                    cupidon.closeInventory();
                    // Ouvrir le GUI pour le second amoureux
                    openSecondLover(cupidon);
                } else {
                    // Deuxième amoureux sélectionné
                    if (target.equals(firstLover)) {
                        cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.same-player"));
                        return;
                    }

                    cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.second-lover")
                                      .replace("{player}", target.getName()));

                    // Créer le couple
                    gm.addLovers(firstLover, target);

                    // Notifier les amoureux
                    firstLover.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.in-love")
                                         .replace("{player}", target.getName()));
                    target.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.in-love")
                                     .replace("{player}", firstLover.getName()));

                    gm.setCupidonAction(true);
                    plugin.getTimerManager().advanceTimer();
                    cupidon.closeInventory();
                }
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        gui.addPane(pane);

        gui.setOnClose(event -> {
            if (firstLover == null) {
                cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.no-couple"));
                gm.setCupidonAction(true);
                plugin.getTimerManager().advanceTimer();
            }
        });

        gui.show(cupidon);
    }

    private void openSecondLover(Player cupidon) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());

        ChestGui gui = new ChestGui(2, plugin.getLanguageManager().getMessage("gui.cupidon-second"));

        StaticPane pane = new StaticPane(0, 0, 9, 2);

        int slot = 0;
        for (Player target : alivePlayers) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(ChatColor.YELLOW + target.getName());

                // Marquer le premier amoureux
                if (target.equals(firstLover)) {
                    List<String> lore = new ArrayList<>();
                    lore.add(plugin.getLanguageManager().getMessage("gui.items.first-lover"));
                    meta.setLore(lore);
                }

                skull.setItemMeta(meta);
            }

            GuiItem item = new GuiItem(skull, event -> {
                event.setCancelled(true);

                if (target.equals(firstLover)) {
                    cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.same-player"));
                    return;
                }

                cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.second-lover")
                                  .replace("{player}", target.getName()));

                // Créer le couple
                gm.addLovers(firstLover, target);

                // Notifier les amoureux
                firstLover.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.in-love")
                                     .replace("{player}", target.getName()));
                target.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.in-love")
                                 .replace("{player}", firstLover.getName()));

                gm.setCupidonAction(true);
                plugin.getTimerManager().advanceTimer();
                cupidon.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        gui.addPane(pane);

        gui.setOnClose(event -> {
            if (!gm.isCupidonAction()) {
                cupidon.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.no-couple"));
                gm.setCupidonAction(true);
                plugin.getTimerManager().advanceTimer();
            }
        });

        gui.show(cupidon);
    }
}

