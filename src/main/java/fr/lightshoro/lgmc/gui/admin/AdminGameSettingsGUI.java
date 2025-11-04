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
 * Admin GUI for Game Settings Configuration
 * Configure minimum players, wolf thresholds, and other game parameters
 */
@SuppressWarnings("deprecation")
public class AdminGameSettingsGUI {
    private final Lgmc plugin;

    public AdminGameSettingsGUI(Lgmc plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        ChestGui gui = new ChestGui(3, plugin.getLanguageManager().getMessage("admin.game.title"));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        int minPlayers = plugin.getConfigManager().getMinPlayers();
        int twoWolvesThreshold = plugin.getConfigManager().getTwoWolvesThreshold();
        int countdownDuration = plugin.getConfigManager().getCountdownDuration();
        int maxPlayers = plugin.getLocationManager().getMaxPlayers();

        // Min Players controls
        createSettingControls(pane, plugin.getLanguageManager().getMessage("admin.game.min-players"), 
            minPlayers, "game.min-players", Material.PLAYER_HEAD, 0, admin, 1, maxPlayers);

        // Two Wolves Threshold controls
        createSettingControls(pane, plugin.getLanguageManager().getMessage("admin.game.two-wolves"), 
            twoWolvesThreshold, "game.two-wolves-threshold", Material.BONE, 3, admin, 1, 30);

        // Countdown Duration controls
        createSettingControls(pane, plugin.getLanguageManager().getMessage("admin.game.countdown"), 
            countdownDuration, "game.countdown-duration", Material.CLOCK, 6, admin, 1, 60);

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

        pane.addItem(backItem, 0, 2);

        // Save button
        ItemStack save = new ItemStack(Material.FEATHER);
        ItemMeta saveMeta = save.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.roles.save"));
            save.setItemMeta(saveMeta);
        }

        GuiItem saveItem = new GuiItem(save, event -> {
            event.setCancelled(true);
            plugin.saveConfig();
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.game.saved"));
        });

        pane.addItem(saveItem, 8, 2);

        gui.addPane(pane);
        gui.show(admin);
    }

    private void createSettingControls(StaticPane pane, String label, int currentValue, String configPath, 
                                       Material icon, int startX, Player admin, int min, int max) {
        // Setting label
        ItemStack settingLabel = new ItemStack(icon);
        ItemMeta settingLabelMeta = settingLabel.getItemMeta();
        if (settingLabelMeta != null) {
            settingLabelMeta.setDisplayName(label);
            settingLabelMeta.setLore(java.util.Arrays.asList(
                plugin.getLanguageManager().getMessage("admin.game.current")
                    .replace("{value}", String.valueOf(currentValue)),
                plugin.getLanguageManager().getMessage("admin.game.range")
                    .replace("{min}", String.valueOf(min))
                    .replace("{max}", String.valueOf(max))
            ));
            settingLabel.setItemMeta(settingLabelMeta);
        }
        pane.addItem(new GuiItem(settingLabel, event -> event.setCancelled(true)), startX + 1, 0);

        // Decrease button
        ItemStack decrease = new ItemStack(Material.ARMADILLO_SCUTE);
        ItemMeta decreaseMeta = decrease.getItemMeta();
        if (decreaseMeta != null) {
            decreaseMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.decrease"));
            java.util.List<String> decreaseLore = plugin.getLanguageManager().getMessageList("admin.role-detail.decrease-lore");
            // Replace placeholders
            java.util.List<String> processedLore = new java.util.ArrayList<>();
            for (String line : decreaseLore) {
                processedLore.add(line.replace("{small}", "-1").replace("{large}", "-5"));
            }
            decreaseMeta.setLore(processedLore);
            decrease.setItemMeta(decreaseMeta);
        }

        GuiItem decreaseItem = new GuiItem(decrease, event -> {
            event.setCancelled(true);
            int change = event.isLeftClick() ? -1 : -5;
            int newValue = Math.max(min, currentValue + change);
            plugin.getConfig().set(configPath, newValue);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.game.set")
                .replace("{setting}", ChatColor.stripColor(label))
                .replace("{value}", String.valueOf(newValue)));
            open(admin);
        });

        pane.addItem(decreaseItem, startX, 0);

        // Increase button
        ItemStack increase = new ItemStack(Material.TURTLE_SCUTE);
        ItemMeta increaseMeta = increase.getItemMeta();
        if (increaseMeta != null) {
            increaseMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.increase"));
            java.util.List<String> increaseLore = plugin.getLanguageManager().getMessageList("admin.role-detail.increase-lore");
            // Replace placeholders
            java.util.List<String> processedLore = new java.util.ArrayList<>();
            for (String line : increaseLore) {
                processedLore.add(line.replace("{small}", "+1").replace("{large}", "+5"));
            }
            increaseMeta.setLore(processedLore);
            increase.setItemMeta(increaseMeta);
        }

        GuiItem increaseItem = new GuiItem(increase, event -> {
            event.setCancelled(true);
            int change = event.isLeftClick() ? 1 : 5;
            int newValue = Math.min(max, currentValue + change);
            plugin.getConfig().set(configPath, newValue);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.game.set")
                .replace("{setting}", ChatColor.stripColor(label))
                .replace("{value}", String.valueOf(newValue)));
            open(admin);
        });

        pane.addItem(increaseItem, startX + 2, 0);
    }
}
