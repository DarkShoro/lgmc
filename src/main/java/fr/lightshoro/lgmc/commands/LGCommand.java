package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Commande principale /lg avec sous-commandes
 * Utilise le système de TabCompleter de Mojang pour l'autocomplétion
 */
public class LGCommand implements CommandExecutor, TabCompleter {
    private final Lgmc plugin;

    public LGCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start" -> {
                return handleStart(sender, args);
            }
            case "stop" -> {
                return handleStop(sender, args);
            }
            case "reload" -> {
                return handleReload(sender, args);
            }
            case "setup" -> {
                return handleSetup(sender, args);
            }
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            default -> {
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lg.unknown-subcommand")
                        .replace("{subcommand}", subCommand));
                return true;
            }
        }
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.start")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (plugin.getGameManager().isInGame()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.game-already-started"));
            return true;
        }

        int playerCount = Bukkit.getOnlinePlayers().size();
        if (playerCount < 4) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("general.not-enough-players"));
            return true;
        }

        if (playerCount > 12) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("general.too-many-players"));
            return true;
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgstart.started"));
        plugin.getGameManager().startGame();
        return true;
    }

    private boolean handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.stop")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (!plugin.getGameManager().isInGame()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("errors.game-not-started"));
            return true;
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgstop.stopped"));
        plugin.getGameManager().gameReset(true);
        return true;
    }

    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.reload")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.reloading"));

        int oldVersion = plugin.getConfigManager().getConfig().getInt("config-version");
        plugin.getConfigManager().reloadConfig();
        int newVersion = plugin.getConfigManager().getConfig().getInt("config-version");

        if (oldVersion != newVersion) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.config-updated")
                    .replace("{old}", String.valueOf(oldVersion))
                    .replace("{new}", String.valueOf(newVersion)));
        }

        plugin.getLanguageManager().reload();
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.reloaded"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.language-info")
                .replace("{language}", plugin.getLanguageManager().getCurrentLanguage()));

        return true;
    }

    private boolean handleSetup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.setup")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        if (args.length < 2) {
            sendSetupHelp(sender);
            return true;
        }

        String setupType = args[1].toLowerCase();
        Location playerLoc = player.getLocation();

        switch (setupType) {
            case "campfire" -> {
                plugin.getLocationManager().setCampfireLocation(playerLoc);
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.campfire-set"));
                return true;
            }
            case "chasseurtp", "chasseur" -> {
                plugin.getLocationManager().setChasseurTpLocation(playerLoc);
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.chasseur-set"));
                return true;
            }
            case "spawn" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.usage"));
                    return true;
                }

                try {
                    int spawnNumber = Integer.parseInt(args[2]);
                    if (spawnNumber < 1 || spawnNumber > 12) {
                        sender.sendMessage("§cLe numéro de spawn doit être entre 1 et 12.");
                        return true;
                    }

                    plugin.getLocationManager().setSpawnLocation(spawnNumber, playerLoc);
                    sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.spawn-set")
                            .replace("{number}", String.valueOf(spawnNumber)));
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cNuméro de spawn invalide.");
                    return true;
                }
            }
            case "info" -> {
                sendSetupInfo(sender);
                return true;
            }
            default -> {
                sendSetupHelp(sender);
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Commandes Loup-Garou ===");
        sender.sendMessage("§e/lg start §f- Démarrer une partie");
        sender.sendMessage("§e/lg stop §f- Arrêter la partie en cours");
        sender.sendMessage("§e/lg reload §f- Recharger la configuration");
        sender.sendMessage("§e/lg setup <type> [args] §f- Configuration du jeu");
        sender.sendMessage("§e/lg help §f- Afficher cette aide");
    }

    private void sendSetupHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-header"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-campfire"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-chasseurtp"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-spawn"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-info"));
    }

    private void sendSetupInfo(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-header"));

        Location campfire = plugin.getLocationManager().getCampfireLocation();
        String campfireStr = campfire != null ?
                String.format("%.1f, %.1f, %.1f", campfire.getX(), campfire.getY(), campfire.getZ()) :
                plugin.getLanguageManager().getMessage("commands.lgsetup.not-set");
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-campfire")
                .replace("{location}", campfireStr));

        Location chasseurTp = plugin.getLocationManager().getChasseurTpLocation();
        String chasseurStr = chasseurTp != null ?
                String.format("%.1f, %.1f, %.1f", chasseurTp.getX(), chasseurTp.getY(), chasseurTp.getZ()) :
                plugin.getLanguageManager().getMessage("commands.lgsetup.not-set");
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-chasseur")
                .replace("{location}", chasseurStr));

        int spawnCount = plugin.getLocationManager().getSpawnLocations().size();
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-spawns")
                .replace("{count}", String.valueOf(spawnCount)));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Sous-commandes principales
            List<String> subCommands = Arrays.asList("start", "stop", "reload", "setup", "help");
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setup")) {
            // Arguments pour /lg setup
            List<String> setupArgs = Arrays.asList("campfire", "chasseurtp", "spawn", "info");
            return setupArgs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("spawn")) {
            // Numéros de spawn (1-12)
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                numbers.add(String.valueOf(i));
            }
            return numbers.stream()
                    .filter(s -> s.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}

