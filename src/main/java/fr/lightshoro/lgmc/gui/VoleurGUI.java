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

public class VoleurGUI {
    private final Lgmc plugin;
    private final GameManager gm;

    public VoleurGUI(Lgmc plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
    }

    public void open(Player voleur) {
        List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());
        // Remove the thief from the list
        alivePlayers.remove(voleur);

        ChestGui gui = new ChestGui(2, plugin.getLanguageManager().getMessage("gui.voleur"));

        StaticPane pane = new StaticPane(0, 0, 9, 2);

        // Add skip button
        ItemStack skipItem = new ItemStack(Material.BARRIER);
        ItemMeta skipMeta = skipItem.getItemMeta();
        if (skipMeta != null) {
            skipMeta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.skip"));
            skipItem.setItemMeta(skipMeta);
        }

        GuiItem skipGuiItem = new GuiItem(skipItem, event -> {
            event.setCancelled(true);
            voleur.sendMessage(plugin.getLanguageManager().getMessage("actions.voleur.no-steal"));
            gm.setVoleurAction(true);
            plugin.getTimerManager().advanceTimer();
            voleur.closeInventory();
        });

        pane.addItem(skipGuiItem, 8, 1);

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

                // Get target's role
                GamePlayer targetGp = gm.getGamePlayer(target);
                Role targetRole = targetGp.getRole();

                // Get voleur's game player
                GamePlayer voleurGp = gm.getGamePlayer(voleur);

                // Steal the role
                voleurGp.setRole(targetRole);
                targetGp.setRole(Role.VILLAGEOIS);

                // Update role-specific player references in GameManager
                gm.removePlayerFromRole(target);
                gm.addPlayerToRole(voleur, targetRole);

                // Update item in hand for both players
                voleur.getInventory().setItemInOffHand(
                    plugin.getConfigManager().getRoleHelmetItemStack(targetRole.getName().toLowerCase().replace(" ", "-"))
                );
                target.getInventory().setItemInOffHand(
                    plugin.getConfigManager().getRoleHelmetItemStack("villageois")
                );

                // Send messages
                voleur.sendMessage(plugin.getLanguageManager().getMessage("roles.voleur.stole-role")
                    .replace("{player}", target.getName())
                    .replace("{role}", targetRole.getFormattedName()));
                
                target.sendMessage(plugin.getLanguageManager().getMessage("roles.voleur.victim-message"));

                gm.setVoleurAction(true);
                plugin.getTimerManager().advanceTimer();
                voleur.closeInventory();
            });

            pane.addItem(item, slot % 9, slot / 9);
            slot++;
        }

        gui.addPane(pane);

        // Marquer que le joueur a un GUI ouvert
        GamePlayer gamePlayer = gm.getGamePlayer(voleur);
        if (gamePlayer != null) {
            gamePlayer.setGuiOpen(true);
        }

        gui.setOnClose(event -> {
            // Réinitialiser le flag GUI
            GamePlayer gp = gm.getGamePlayer(voleur);
            if (gp != null) {
                gp.setGuiOpen(false);
            }
            
            if (!gm.isVoleurAction()) {
                voleur.sendMessage(plugin.getLanguageManager().getMessage("actions.voleur.no-steal"));
                gm.setVoleurAction(true);
                plugin.getTimerManager().advanceTimer();
            }
        });

        gui.show(voleur);
    }
}
