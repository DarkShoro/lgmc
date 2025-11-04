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
 * Admin GUI for Role Configuration
 * Allows enabling/disabling roles and configuring their settings
 */
@SuppressWarnings("deprecation")
public class AdminRoleConfigGUI {
    private final Lgmc plugin;
    private final String[] roles = {"cupidon", "ange", "voleur", "petite-fille", "chasseur", "sorciere", "voyante"};

    public AdminRoleConfigGUI(Lgmc plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        ChestGui gui = new ChestGui(5, plugin.getLanguageManager().getMessage("admin.roles.title"));

        StaticPane pane = new StaticPane(0, 0, 9, 5);

        // Display each role with helmet and enable/disable toggle below
        for (int i = 0; i < roles.length; i++) {
            final String role = roles[i];
            final String displayName = plugin.getLanguageManager().getRoleName(role, false, false);
            final int index = i;

            boolean enabled = plugin.getConfigManager().isRoleEnabled(role);
            int threshold = plugin.getConfigManager().getRoleThreshold(role);
            double chance = plugin.getConfigManager().getRoleChance(role);

            // Role helmet display (top row)
            ItemStack roleHelmet = plugin.getConfigManager().getRoleHelmetItemStack(role);
            ItemMeta helmetMeta = roleHelmet.getItemMeta();
            if (helmetMeta != null) {
                helmetMeta.setDisplayName(plugin.getLanguageManager().getRoleName(role));
                helmetMeta.setLore(java.util.Arrays.asList(
                    plugin.getLanguageManager().getMessage("admin.roles.status")
                        .replace("{status}", enabled ? 
                            plugin.getLanguageManager().getMessage("admin.roles.enabled") : 
                            plugin.getLanguageManager().getMessage("admin.roles.disabled")),
                    plugin.getLanguageManager().getMessage("admin.roles.threshold")
                        .replace("{threshold}", String.valueOf(threshold)),
                    plugin.getLanguageManager().getMessage("admin.roles.chance")
                        .replace("{chance}", String.valueOf((int)(chance * 100))),
                    "",
                    plugin.getLanguageManager().getMessage("admin.roles.configure")
                ));
                roleHelmet.setItemMeta(helmetMeta);
            }

            GuiItem helmetGuiItem = new GuiItem(roleHelmet, event -> {
                event.setCancelled(true);
                // Open detail configuration
                new AdminRoleDetailGUI(plugin, role, displayName).open(admin);
            });

            // Enable/Disable toggle button (bottom row)
            ItemStack toggleItem = new ItemStack(enabled ? Material.CLAY_BALL : Material.FEATHER);
            ItemMeta toggleMeta = toggleItem.getItemMeta();
            if (toggleMeta != null) {
                toggleMeta.setDisplayName((enabled ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ") + 
                    (enabled ? 
                        plugin.getLanguageManager().getMessage("admin.roles.enabled") : 
                        plugin.getLanguageManager().getMessage("admin.roles.disabled")));
                toggleMeta.setLore(java.util.Arrays.asList(
                    plugin.getLanguageManager().getMessage("admin.roles.toggle")
                ));
                toggleItem.setItemMeta(toggleMeta);
            }

            GuiItem toggleGuiItem = new GuiItem(toggleItem, event -> {
                event.setCancelled(true);
                // Toggle enabled/disabled
                boolean currentlyEnabled = plugin.getConfigManager().isRoleEnabled(role);
                plugin.getConfig().set("game.roles." + role + ".enabled", !currentlyEnabled);
                admin.sendMessage((!currentlyEnabled ? 
                    plugin.getLanguageManager().getMessage("admin.roles.enabled-msg") : 
                    plugin.getLanguageManager().getMessage("admin.roles.disabled-msg"))
                        .replace("{role}", displayName));
                open(admin); // Refresh GUI
            });

            // Position in grid (helmet on top, toggle button below)
            int x = (index % 4) * 2 + 1; // 4 columns with spacing
            int yHelmet = (index / 4) * 2; // Helmet row
            int yToggle = yHelmet + 1; // Toggle button row below helmet
            
            pane.addItem(helmetGuiItem, x, yHelmet);
            pane.addItem(toggleGuiItem, x, yToggle);
        }

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

        pane.addItem(backItem, 0, 4);

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
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.roles.saved"));
        });

        pane.addItem(saveItem, 8, 4);

        gui.addPane(pane);
        gui.show(admin);
    }
}
