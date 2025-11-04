package fr.lightshoro.lgmc.gui.admin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import fr.lightshoro.lgmc.Lgmc;

/**
 * Main Admin Configuration GUI
 * Central hub for all admin configuration panels
 */
@SuppressWarnings("deprecation")
public class AdminMainGUI {
    private final Lgmc plugin;

    public AdminMainGUI(Lgmc plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        ChestGui gui = new ChestGui(3, plugin.getLanguageManager().getMessage("admin.main.title"));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        // Role Configuration Button
        ItemStack roleConfig = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta roleConfigMeta = roleConfig.getItemMeta();
        if (roleConfigMeta != null) {
            roleConfigMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.main.role-config"));
            roleConfigMeta.setLore(plugin.getLanguageManager().getMessageList("admin.main.role-config-desc"));
            roleConfig.setItemMeta(roleConfigMeta);
        }

        GuiItem roleConfigItem = new GuiItem(roleConfig, event -> {
            event.setCancelled(true);
            new AdminRoleConfigGUI(plugin).open(admin);
        });

        // Game Settings Button
        ItemStack gameSettings = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta gameSettingsMeta = gameSettings.getItemMeta();
        if (gameSettingsMeta != null) {
            gameSettingsMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.main.game-settings"));
            gameSettingsMeta.setLore(plugin.getLanguageManager().getMessageList("admin.main.game-settings-desc"));
            gameSettings.setItemMeta(gameSettingsMeta);
        }

        GuiItem gameSettingsItem = new GuiItem(gameSettings, event -> {
            event.setCancelled(true);
            new AdminGameSettingsGUI(plugin).open(admin);
        });

        // Timer Configuration Button
        ItemStack timerConfig = new ItemStack(Material.CLOCK);
        ItemMeta timerConfigMeta = timerConfig.getItemMeta();
        if (timerConfigMeta != null) {
            timerConfigMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.main.timer-config"));
            timerConfigMeta.setLore(plugin.getLanguageManager().getMessageList("admin.main.timer-config-desc"));
            timerConfig.setItemMeta(timerConfigMeta);
        }

        GuiItem timerConfigItem = new GuiItem(timerConfig, event -> {
            event.setCancelled(true);
            new AdminTimerConfigGUI(plugin).open(admin);
        });

        // Save Configuration Button
        ItemStack saveConfig = new ItemStack(Material.CLAY_BALL);
        ItemMeta saveConfigMeta = saveConfig.getItemMeta();
        if (saveConfigMeta != null) {
            saveConfigMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.main.save"));
            saveConfigMeta.setLore(plugin.getLanguageManager().getMessageList("admin.main.save-desc"));
            saveConfig.setItemMeta(saveConfigMeta);
        }

        GuiItem saveConfigItem = new GuiItem(saveConfig, event -> {
            event.setCancelled(true);
            plugin.saveConfig();
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.main.saved"));
            admin.closeInventory();
        });

        // Reload Configuration Button
        ItemStack reloadConfig = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta reloadConfigMeta = reloadConfig.getItemMeta();
        if (reloadConfigMeta != null) {
            reloadConfigMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.main.reload"));
            reloadConfigMeta.setLore(plugin.getLanguageManager().getMessageList("admin.main.reload-desc"));
            reloadConfig.setItemMeta(reloadConfigMeta);
        }

        GuiItem reloadConfigItem = new GuiItem(reloadConfig, event -> {
            event.setCancelled(true);
            admin.closeInventory();
            admin.performCommand("lg reload");
        });

        // Close Button
        ItemStack close = new ItemStack(Material.FEATHER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.main.close"));
            close.setItemMeta(closeMeta);
        }

        GuiItem closeItem = new GuiItem(close, event -> {
            event.setCancelled(true);
            admin.closeInventory();
        });

        // Layout
        pane.addItem(roleConfigItem, 1, 1);
        pane.addItem(gameSettingsItem, 3, 1);
        pane.addItem(timerConfigItem, 5, 1);
        pane.addItem(saveConfigItem, 7, 1);
        pane.addItem(reloadConfigItem, 2, 2);
        pane.addItem(closeItem, 6, 2);

        gui.addPane(pane);
        gui.show(admin);
    }
}
