package fr.lightshoro.lgmc.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class TestamentGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public TestamentGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player capitaine) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());
        alivePlayers.remove(capitaine); // Le capitaine ne peut pas se choisir lui-même

        int rows = alivePlayers.size() > 9 ? 2 : 1;
        ChestGui gui = new ChestGui(rows, plugin.getLanguageManager().getMessage("gui.testament"));

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

                // Nommer le successeur
                gm.setCapitaine(target);

                // Donne le casque au successeur
                target.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("capitaine"));
                // Retire le casque au capitaine mourant
                capitaine.getInventory().setHelmet(null);
                // Retire le livre Testament
                capitaine.getInventory().setItem(4, new ItemStack(Material.AIR));

                Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("succession.testament-chosen")
                        .replace("{dying}", capitaine.getName())
                        .replace("{new}", target.getName()));

                gm.setCapitaineSuccession(false);
                plugin.getTimerManager().advanceTimer();

                capitaine.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        gui.addPane(pane);
        gui.show(capitaine);

        // Skip timer on gui close, randomly choose successor if none chosen
        gui.setOnClose(event -> {
            if (gm.isCapitaineSuccession()) {
                // Aucun successeur n'a été choisi, choisir au hasard
                List<Player> availablePlayers = new ArrayList<>(gm.getPlayersAlive());
                availablePlayers.remove(capitaine);

                if (!availablePlayers.isEmpty()) {
                    Player newCapitaine = availablePlayers.get((int)(Math.random() * availablePlayers.size()));
                    gm.setCapitaine(newCapitaine);

                    // Donner un casque bleu au nouveau capitaine
                    newCapitaine.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("capitaine"));
                    // Retirer le casque du capitaine mourant
                    capitaine.getInventory().setHelmet(null);
                    // Retirer le livre Testament
                    capitaine.getInventory().setItem(4, new ItemStack(Material.AIR));

                    Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("succession.testament-random")
                            .replace("{dying}", capitaine.getName())
                            .replace("{new}", newCapitaine.getName()));
                }

                gm.setCapitaineSuccession(false);
                plugin.getTimerManager().advanceTimer();
            }
        });
    }


}

