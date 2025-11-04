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
 * Admin GUI for detailed role configuration
 * Configure threshold and chance for a specific role
 */
@SuppressWarnings("deprecation")
public class AdminRoleDetailGUI {
    private final Lgmc plugin;
    private final String role;
    private final String displayName;

    public AdminRoleDetailGUI(Lgmc plugin, String role, String displayName) {
        this.plugin = plugin;
        this.role = role;
        this.displayName = displayName;
    }

    public void open(Player admin) {
        ChestGui gui = new ChestGui(3, plugin.getLanguageManager().getMessage("admin.role-detail.title")
            .replace("{role}", displayName));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        int threshold = plugin.getConfigManager().getRoleThreshold(role);
        double chance = plugin.getConfigManager().getRoleChance(role);

        // Threshold controls
        createThresholdControls(pane, threshold, admin);

        // Chance controls
        createChanceControls(pane, chance, admin);

        // Back button
        ItemStack back = new ItemStack(Material.INK_SAC);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.roles.back"));
            back.setItemMeta(backMeta);
        }

        GuiItem backItem = new GuiItem(back, event -> {
            event.setCancelled(true);
            new AdminRoleConfigGUI(plugin).open(admin);
        });

        pane.addItem(backItem, 0, 2);

        gui.addPane(pane);
        gui.show(admin);
    }

    private void createThresholdControls(StaticPane pane, int currentThreshold, Player admin) {
        // Threshold label
        ItemStack thresholdLabel = new ItemStack(Material.IRON_DOOR);
        ItemMeta thresholdLabelMeta = thresholdLabel.getItemMeta();
        if (thresholdLabelMeta != null) {
            thresholdLabelMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.player-threshold"));
            thresholdLabelMeta.setLore(java.util.Arrays.asList(
                plugin.getLanguageManager().getMessage("admin.role-detail.threshold-current")
                    .replace("{threshold}", String.valueOf(currentThreshold)),
                plugin.getLanguageManager().getMessage("admin.role-detail.threshold-desc")
            ));
            thresholdLabel.setItemMeta(thresholdLabelMeta);
        }
        pane.addItem(new GuiItem(thresholdLabel, event -> event.setCancelled(true)), 1, 0);

        // Decrease threshold
        ItemStack decreaseThreshold = new ItemStack(Material.ARMADILLO_SCUTE);
        ItemMeta decreaseThresholdMeta = decreaseThreshold.getItemMeta();
        if (decreaseThresholdMeta != null) {
            decreaseThresholdMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.decrease"));
            java.util.List<String> decreaseLore = plugin.getLanguageManager().getMessageList("admin.role-detail.decrease-lore");
            java.util.List<String> processedLore = new java.util.ArrayList<>();
            for (String line : decreaseLore) {
                processedLore.add(line.replace("{small}", "-1").replace("{large}", "-5"));
            }
            decreaseThresholdMeta.setLore(processedLore);
            decreaseThreshold.setItemMeta(decreaseThresholdMeta);
        }

        GuiItem decreaseThresholdItem = new GuiItem(decreaseThreshold, event -> {
            event.setCancelled(true);
            int change = event.isLeftClick() ? -1 : -5;
            int newThreshold = Math.max(0, currentThreshold + change);
            plugin.getConfig().set("game.roles." + role + ".threshold", newThreshold);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.role-detail.threshold-set")
                .replace("{role}", displayName)
                .replace("{value}", String.valueOf(newThreshold)));
            open(admin);
        });

        pane.addItem(decreaseThresholdItem, 0, 0);

        // Increase threshold
        ItemStack increaseThreshold = new ItemStack(Material.TURTLE_SCUTE);
        ItemMeta increaseThresholdMeta = increaseThreshold.getItemMeta();
        if (increaseThresholdMeta != null) {
            increaseThresholdMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.increase"));
            java.util.List<String> increaseLore = plugin.getLanguageManager().getMessageList("admin.role-detail.increase-lore");
            java.util.List<String> processedLore = new java.util.ArrayList<>();
            for (String line : increaseLore) {
                processedLore.add(line.replace("{small}", "+1").replace("{large}", "+5"));
            }
            increaseThresholdMeta.setLore(processedLore);
            increaseThreshold.setItemMeta(increaseThresholdMeta);
        }

        GuiItem increaseThresholdItem = new GuiItem(increaseThreshold, event -> {
            event.setCancelled(true);
            int change = event.isLeftClick() ? 1 : 5;
            int newThreshold = Math.min(100, currentThreshold + change);
            plugin.getConfig().set("game.roles." + role + ".threshold", newThreshold);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.role-detail.threshold-set")
                .replace("{role}", displayName)
                .replace("{value}", String.valueOf(newThreshold)));
            open(admin);
        });

        pane.addItem(increaseThresholdItem, 2, 0);
    }

    private void createChanceControls(StaticPane pane, double currentChance, Player admin) {
        // Chance label
        ItemStack chanceLabel = new ItemStack(Material.ENDER_PEARL);
        ItemMeta chanceLabelMeta = chanceLabel.getItemMeta();
        if (chanceLabelMeta != null) {
            chanceLabelMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.spawn-chance"));
            chanceLabelMeta.setLore(java.util.Arrays.asList(
                plugin.getLanguageManager().getMessage("admin.role-detail.chance-current")
                    .replace("{chance}", String.valueOf((int)(currentChance * 100))),
                plugin.getLanguageManager().getMessage("admin.role-detail.chance-desc")
            ));
            chanceLabel.setItemMeta(chanceLabelMeta);
        }
        pane.addItem(new GuiItem(chanceLabel, event -> event.setCancelled(true)), 5, 0);

        // Decrease chance
        ItemStack decreaseChance = new ItemStack(Material.ARMADILLO_SCUTE);
        ItemMeta decreaseChanceMeta = decreaseChance.getItemMeta();
        if (decreaseChanceMeta != null) {
            decreaseChanceMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.decrease"));
            java.util.List<String> decreaseLore = plugin.getLanguageManager().getMessageList("admin.role-detail.decrease-lore");
            java.util.List<String> processedLore = new java.util.ArrayList<>();
            for (String line : decreaseLore) {
                processedLore.add(line.replace("{small}", "-5%").replace("{large}", "-25%"));
            }
            decreaseChanceMeta.setLore(processedLore);
            decreaseChance.setItemMeta(decreaseChanceMeta);
        }

        GuiItem decreaseChanceItem = new GuiItem(decreaseChance, event -> {
            event.setCancelled(true);
            double change = event.isLeftClick() ? -0.05 : -0.25;
            double newChance = Math.max(0.0, Math.min(1.0, currentChance + change));
            plugin.getConfig().set("game.roles." + role + ".chance", newChance);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.role-detail.chance-set")
                .replace("{role}", displayName)
                .replace("{value}", String.valueOf((int)(newChance * 100))));
            open(admin);
        });

        pane.addItem(decreaseChanceItem, 4, 0);

        // Increase chance
        ItemStack increaseChance = new ItemStack(Material.TURTLE_SCUTE);
        ItemMeta increaseChanceMeta = increaseChance.getItemMeta();
        if (increaseChanceMeta != null) {
            increaseChanceMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.increase"));
            java.util.List<String> increaseLore = plugin.getLanguageManager().getMessageList("admin.role-detail.increase-lore");
            java.util.List<String> processedLore = new java.util.ArrayList<>();
            for (String line : increaseLore) {
                processedLore.add(line.replace("{small}", "+5%").replace("{large}", "+25%"));
            }
            increaseChanceMeta.setLore(processedLore);
            increaseChance.setItemMeta(increaseChanceMeta);
        }

        GuiItem increaseChanceItem = new GuiItem(increaseChance, event -> {
            event.setCancelled(true);
            double change = event.isLeftClick() ? 0.05 : 0.25;
            double newChance = Math.max(0.0, Math.min(1.0, currentChance + change));
            plugin.getConfig().set("game.roles." + role + ".chance", newChance);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.role-detail.chance-set")
                .replace("{role}", displayName)
                .replace("{value}", String.valueOf((int)(newChance * 100))));
            open(admin);
        });

        pane.addItem(increaseChanceItem, 6, 0);

        // Preset buttons
        createPresetButton(pane, "0%", 0.0, 7, 1, admin);
        createPresetButton(pane, "50%", 0.5, 8, 1, admin);
        createPresetButton(pane, "100%", 1.0, 7, 2, admin);
    }

    private void createPresetButton(StaticPane pane, String label, double value, int x, int y, Player admin) {
        ItemStack preset = new ItemStack(Material.PAPER);
        ItemMeta presetMeta = preset.getItemMeta();
        if (presetMeta != null) {
            presetMeta.setDisplayName(plugin.getLanguageManager().getMessage("admin.role-detail.set-to")
                .replace("{value}", label));
            preset.setItemMeta(presetMeta);
        }

        GuiItem presetItem = new GuiItem(preset, event -> {
            event.setCancelled(true);
            plugin.getConfig().set("game.roles." + role + ".chance", value);
            admin.sendMessage(plugin.getLanguageManager().getMessage("admin.role-detail.chance-set")
                .replace("{role}", displayName)
                .replace("{value}", label));
            open(admin);
        });

        pane.addItem(presetItem, x, y);
    }
}
