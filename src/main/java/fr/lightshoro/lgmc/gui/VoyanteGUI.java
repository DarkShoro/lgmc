package fr.lightshoro.lgmc.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class VoyanteGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public VoyanteGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player voyante) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());
        alivePlayers.remove(voyante); // La voyante ne peut pas se sonder elle-même

        int rows = alivePlayers.size() > 9 ? 2 : 1;
        ChestGui gui = new ChestGui(rows, plugin.getLanguageManager().getMessage("gui.voyante"));

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

                GamePlayer targetGp = gm.getGamePlayer(target);
                Role targetRole = targetGp.getRole();

                // Déterminer le nom du rôle exact
                String roleKey;
                if (targetRole != null) {
                    roleKey = "roles." + targetRole.toString().toLowerCase() + ".name";
                } else if (gm.getLoupGarous().contains(target)) {
                    roleKey = "roles.loup-garou.name";
                } else {
                    roleKey = "roles.villageois.name";
                }

                String roleName = plugin.getLanguageManager().getMessage(roleKey);
                String finalMessage = plugin.getLanguageManager().getMessage("actions.voyante.probe")
                        .replace("{player}", target.getName())
                        .replace("{role}", roleName);


                voyante.sendMessage(finalMessage);

                gm.setVoyanteSondage(true);
                plugin.getTimerManager().advanceTimer();
                voyante.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        // Option "Ne rien faire"
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));
            barrier.setItemMeta(barrierMeta);
        }

        GuiItem skipItem = new GuiItem(barrier, event -> {
            event.setCancelled(true);
            voyante.sendMessage(plugin.getLanguageManager().getMessage("actions.voyante.no-probe"));
            gm.setVoyanteSondage(true);
            plugin.getTimerManager().advanceTimer();
            voyante.closeInventory();
        });

        pane.addItem(skipItem, slot % 9, slot / 9);

        gui.addPane(pane);

        gui.setOnClose(event -> {
            if (!gm.isVoyanteSondage()) {
                voyante.sendMessage(plugin.getLanguageManager().getMessage("actions.voyante.no-probe"));
                gm.setVoyanteSondage(true);
                plugin.getTimerManager().advanceTimer();
            }
        });

        gui.show(voyante);
    }
}

