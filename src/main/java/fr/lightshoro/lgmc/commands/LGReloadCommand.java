package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Commande pour recharger la configuration du plugin
 */
public class LGReloadCommand implements CommandExecutor {
    private final Lgmc plugin;

    public LGReloadCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Vérifier si une partie est en cours
        if (plugin.getGameManager().isInGame()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.game-already-started"));
            return true;
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.reloading"));

        try {
            int oldVersion = plugin.getConfig().getInt("config-version", 1);
            String oldLanguage = plugin.getLanguageManager().getCurrentLanguage();

            // Recharger la configuration
            plugin.getConfigManager().reload();

            int newVersion = plugin.getConfig().getInt("config-version", 1);
            String newLanguage = plugin.getConfigManager().getLanguage();

            // Détecter et appliquer un changement de langue
            if (!oldLanguage.equals(newLanguage)) {
                plugin.getLogger().info("Changement de langue détecté: " + oldLanguage + " -> " + newLanguage);
                plugin.getLanguageManager().setLanguage(newLanguage);
                sender.sendMessage(ChatColor.GREEN + "Langue changée de " + oldLanguage.toUpperCase() + " à " + newLanguage.toUpperCase());
            } else {
                // Recharger la langue actuelle
                plugin.getLanguageManager().reload();
            }

            // Recharger les emplacements
            plugin.getLocationManager().reload();

            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.reloaded"));
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.language-info")
                .replace("{language}", plugin.getLanguageManager().getCurrentLanguage().toUpperCase()));

            if (oldVersion < newVersion) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.config-updated")
                    .replace("{old}", String.valueOf(oldVersion))
                    .replace("{new}", String.valueOf(newVersion)));
            }

            // Log dans la console
            plugin.getLogger().info("Configuration rechargée par " + sender.getName());

        } catch (Exception e) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.config-error"));
            plugin.getLogger().severe("Erreur lors du rechargement de la configuration : " + e.getMessage());
            e.printStackTrace();
            return true;
        }

        return true;
    }
}

