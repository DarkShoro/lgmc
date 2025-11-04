package fr.lightshoro.lgmc.gui.admin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import fr.lightshoro.lgmc.Lgmc;

/**
 * Admin GUI for Timer Configuration
 * Configure all phase durations in the game
 */
@SuppressWarnings("deprecation")
public class AdminTimerConfigGUI {
    private final Lgmc plugin;

    public AdminTimerConfigGUI(Lgmc plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        ChestGui gui = new ChestGui(6, plugin.getLanguageManager().getMessage("admin.timers.title"));

        StaticPane pane = new StaticPane(0, 0, 9, 6);

        // Define all timers
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.voleur"), 
            "game.timers.voleur", plugin.getConfigManager().getTimerVoleur(), Material.WHEAT, 0, 0, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.cupidon"), 
            "game.timers.cupidon", plugin.getConfigManager().getTimerCupidon(), Material.NETHERITE_SCRAP, 3, 0, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.voyante"), 
            "game.timers.voyante", plugin.getConfigManager().getTimerVoyante(), Material.FLINT, 6, 0, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.loups-garous"), 
            "game.timers.loups-garous", plugin.getConfigManager().getTimerLoupsGarous(), Material.IRON_HOE, 0, 1, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.sorciere"), 
            "game.timers.sorciere", plugin.getConfigManager().getTimerSorciere(), Material.GUNPOWDER, 3, 1, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.vote-capitaine"), 
            "game.timers.vote-capitaine", plugin.getConfigManager().getTimerVoteCapitaine(), Material.BLUE_DYE, 6, 1, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.debat"), 
            "game.timers.debat", plugin.getConfigManager().getTimerDebat(), Material.BOOK, 0, 2, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.chasseur"), 
            "game.timers.chasseur", plugin.getConfigManager().getTimerChasseur(), Material.WOODEN_HOE, 3, 2, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.succession"), 
            "game.timers.capitaine-succession", plugin.getConfigManager().getTimerCapitaineSuccession(), Material.WRITTEN_BOOK, 6, 2, admin);
        
        createTimerControl(pane, plugin.getLanguageManager().getMessage("admin.timers.tiebreaker"), 
            "game.timers.capitaine-tiebreaker", plugin.getConfigManager().getTimerCapitaineTiebreaker(), Material.PLAYER_HEAD, 0, 3, admin);

        // Back button
        ItemStack back = new ItemStack(Material.INK_SAC);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.roles.back"));
            back.setItemMeta(backMeta);
        }

        GuiItem backItem = new GuiItem(back, event -> {
            event.setCancelled(true);
            new AdminMainGUI(plugin).open(admin);
        });

        pane.addItem(backItem, 0, 5);

        // Save button
        ItemStack save = new ItemStack(Material.CLAY_BALL);
        ItemMeta saveMeta = save.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.roles.save"));
            save.setItemMeta(saveMeta);
        }

        GuiItem saveItem = new GuiItem(save, event -> {
            event.setCancelled(true);
            plugin.saveConfig();
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.timers.saved"));
        });

        pane.addItem(saveItem, 8, 5);

        gui.addPane(pane);
        gui.show(admin);
    }

    private void createTimerControl(StaticPane pane, String label, String configPath, int currentValue, 
                                    Material icon, int x, int y, Player admin) {
        ItemStack timerItem = new ItemStack(icon);
        ItemMeta timerMeta = timerItem.getItemMeta();
        if (timerMeta != null) {
            timerMeta.setDisplayName(label);
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(plugin.getLanguageManager().getMessage("admin.timers.current")
                .replace("{time}", String.valueOf(currentValue)));
            lore.add("");
            lore.addAll(plugin.getLanguageManager().getMessageList("admin.timers.controls"));
            timerMeta.setLore(lore);
            timerItem.setItemMeta(timerMeta);
        }

        GuiItem timerGuiItem = new GuiItem(timerItem, event -> {
            event.setCancelled(true);
            
            int change;
            if (event.isShiftClick()) {
                change = event.isLeftClick() ? 30 : -30;
            } else {
                change = event.isLeftClick() ? 10 : -10;
            }
            
            int newValue = Math.max(5, Math.min(600, currentValue + change));
            plugin.getConfig().set(configPath, newValue);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.timers.set")
                .replace("{timer}", ChatColor.stripColor(label))
                .replace("{time}", String.valueOf(newValue)));
            open(admin);
        });

        pane.addItem(timerGuiItem, x, y);
    }
}
