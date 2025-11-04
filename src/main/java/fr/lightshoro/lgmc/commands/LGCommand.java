package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.gui.*;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Commande principale /lg avec sous-commandes
 * Utilise le système de TabCompleter de Mojang pour l'autocomplétion
 */
public class LGCommand implements CommandExecutor, TabCompleter {
    private final Lgmc plugin;

    // Valid GUI types for /lg gui command
    private static final Map<String, String> VALID_GUIS = Map.of(
            "sorciere", "SorciereGUI",
            "capitaine", "CapitaineVoteGUI",
            "loupgarou", "LoupGarouGUI",
            "vote", "VoteGUI",
            "voyante", "VoyanteGUI",
            "sorcierepoison", "SorcierePoisonGUI",
            "cupidon", "CupidonGUI",
            "testament", "TestamentGUI"
    );

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
            case "reset" -> {
                plugin.getGameManager().gameReset(true);
                return true;
            }
            case "reload" -> {
                return handleReload(sender, args);
            }
            case "setup" -> {
                return handleSetup(sender, args);
            }
            case "gui" -> {
                return handleGui(sender, args);
            }
            case "neighbors", "voisins" -> {
                return handleNeighbors(sender, args);
            }
            case "testskin" -> {
                return handleTestSkin(sender, args);
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

        int maxPlayers = plugin.getLocationManager().getMaxPlayers();
        if (playerCount > maxPlayers) {
            String message = plugin.getLanguageManager().getMessage("general.too-many-players")
                    .replace("{max}", String.valueOf(maxPlayers));
            sender.sendMessage(message);
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

        // Store old values for comparison
        int oldVersion = plugin.getConfigManager().getConfig().getInt("config-version");
        boolean wasWebsocketEnabled = plugin.getConfigManager().isWebsocketEnabled();
        
        // Reload configuration
        plugin.getConfigManager().reloadConfig();
        
        // Check what changed
        int newVersion = plugin.getConfigManager().getConfig().getInt("config-version");
        boolean isWebsocketEnabled = plugin.getConfigManager().isWebsocketEnabled();

        if (oldVersion != newVersion) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.config-updated")
                    .replace("{old}", String.valueOf(oldVersion))
                    .replace("{new}", String.valueOf(newVersion)));
        }

        // Reload language manager
        plugin.getLanguageManager().reload();
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.reloaded"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.language-info")
                .replace("{language}", plugin.getLanguageManager().getCurrentLanguage()));

        // Reload location manager (in case spawn locations or other locations changed)
        plugin.getLocationManager().reload();
        
        // Update max players based on spawn count
        int maxPlayers = plugin.getLocationManager().getMaxPlayers();
        org.bukkit.Bukkit.getServer().setMaxPlayers(maxPlayers);
        sender.sendMessage("§aMax players updated to: §e" + maxPlayers);

        // Handle WebSocket reconnection
        if (plugin.getWebsocketManager() != null) {
            // If websocket settings changed, reconnect
            if (wasWebsocketEnabled != isWebsocketEnabled) {
                sender.sendMessage("§eWebSocket setting changed, reconnecting...");
                plugin.getWebsocketManager().reconnect();
                
                if (isWebsocketEnabled) {
                    sender.sendMessage("§aWebSocket enabled and connected");
                } else {
                    sender.sendMessage("§cWebSocket disabled");
                }
            } else if (isWebsocketEnabled) {
                // If still enabled, reconnect to pick up URL/secret changes
                sender.sendMessage("§eReconnecting WebSocket with new configuration...");
                plugin.getWebsocketManager().reconnect();
                sender.sendMessage("§aWebSocket reconnected");
            }
        }

        // Reload skin manager settings
        if (plugin.getSkinManager() != null) {
            sender.sendMessage("§aSkin settings reloaded");
        }

        sender.sendMessage("§a§l✓ All settings reloaded successfully!");

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
                    if (spawnNumber < 1) {
                        sender.sendMessage("§cLe numéro de spawn doit être supérieur ou égal à 1.");
                        return true;
                    }

                    plugin.getLocationManager().setSpawnLocation(spawnNumber, playerLoc);
                    sender.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.spawn-set")
                            .replace("{number}", String.valueOf(spawnNumber)));
                    
                    // Notify about max players update
                    int maxPlayers = plugin.getLocationManager().getMaxPlayers();
                    sender.sendMessage("§aMax players set to: §e" + maxPlayers);
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

    private boolean handleGui(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.debug")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        if (args.length < 2 || !VALID_GUIS.containsKey(args[1].toLowerCase())) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.opengui.usage")
                    .replace("{guis}", String.join(", ", VALID_GUIS.keySet())));
            return true;
        }

        String guiName = VALID_GUIS.get(args[1].toLowerCase());

        switch (guiName) {
            case "SorciereGUI" -> {
                new SorciereGUI(plugin).open(player);
            }
            case "CapitaineVoteGUI" -> {
                new CapitaineVoteGUI(plugin).open(player);
            }
            case "LoupGarouGUI" -> {
                new LoupGarouGUI(plugin).open(player);
            }
            case "VoteGUI" -> {
                new VoteGUI(plugin).open(player);
            }
            case "VoyanteGUI" -> {
                new VoyanteGUI(plugin).open(player);
            }
            case "SorcierePoisonGUI" -> {
                new SorcierePoisonGUI(plugin).open(player);
            }
            case "CupidonGUI" -> {
                new CupidonGUI(plugin).open(player);
            }
            case "TestamentGUI" -> {
                new TestamentGUI(plugin).open(player);
            }
            default -> player.sendMessage(plugin.getLanguageManager().getMessage("commands.opengui.usage")
                    .replace("{guis}", String.join(", ", VALID_GUIS.keySet())));
        }

        return true;
    }

    private boolean handleNeighbors(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.debug")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /lg neighbors <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[1]);
            return true;
        }

        List<Player> playingPlayers = plugin.getGameManager().getPlayingPlayers();
        if (!playingPlayers.contains(target)) {
            sender.sendMessage("§c" + target.getName() + " ne participe pas à la partie.");
            return true;
        }

        int playerIndex = playingPlayers.indexOf(target);
        int totalPlayers = playingPlayers.size();

        if (totalPlayers < 3) {
            sender.sendMessage("§cIl n'y a pas assez de joueurs pour déterminer les voisins.");
            return true;
        }

        // Calcul des voisins (joueur précédent et suivant dans la liste)
        int leftIndex = (playerIndex - 1 + totalPlayers) % totalPlayers;
        int rightIndex = (playerIndex + 1) % totalPlayers;

        Player leftNeighbor = playingPlayers.get(leftIndex);
        Player rightNeighbor = playingPlayers.get(rightIndex);

        sender.sendMessage("§6=== Voisins de " + target.getName() + " ===");
        sender.sendMessage("§eVoisin gauche: §f" + leftNeighbor.getName());
        sender.sendMessage("§eVoisin droit: §f" + rightNeighbor.getName());
        sender.sendMessage("§7(Position: " + (playerIndex + 1) + "/" + totalPlayers + ")");

        return true;
    }

    private boolean handleTestSkin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lgmc.debug")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        if (!plugin.getSkinManager().isEnabled()) {
            sender.sendMessage("§cSkinsRestorer integration is disabled in config!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§6=== Test Skin Commands ===");
            sender.sendMessage("§e/lg testskin apply §f- Apply werewolf skin to yourself");
            sender.sendMessage("§e/lg testskin restore §f- Restore your original skin");
            sender.sendMessage("§e/lg testskin apply <player> §f- Apply werewolf skin to a player");
            sender.sendMessage("§e/lg testskin restore <player> §f- Restore a player's original skin");
            return true;
        }

        String action = args[1].toLowerCase();
        Player target = player;

        // Check if a target player was specified
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[2]);
                return true;
            }
        }

        switch (action) {
            case "apply" -> {
                plugin.getSkinManager().setWerewolfSkin(target);
                sender.sendMessage("§aApplying werewolf skin to " + target.getName() + "...");
                sender.sendMessage("§7(This may take a moment)");
                return true;
            }
            case "restore" -> {
                plugin.getSkinManager().restoreOriginalSkin(target);
                sender.sendMessage("§aRestoring original skin for " + target.getName() + "...");
                return true;
            }
            default -> {
                sender.sendMessage("§cInvalid action. Use 'apply' or 'restore'.");
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Commandes Loup-Garou ===");
        sender.sendMessage("§e/lg start §f- Démarrer une partie");
        sender.sendMessage("§e/lg stop §f- Arrêter la partie en cours");
        sender.sendMessage("§e/lg reset §f- Réinitialiser la partie en cours");
        sender.sendMessage("§e/lg reload §f- Recharger la configuration");
        sender.sendMessage("§e/lg setup <type> [args] §f- Configuration du jeu");
        sender.sendMessage("§e/lg gui <type> §f- Ouvrir un GUI (debug)");
        sender.sendMessage("§e/lg neighbors <player> §f- Afficher les voisins d'un joueur (debug)");
        sender.sendMessage("§e/lg testskin <apply|restore> [player] §f- Tester les skins (debug)");
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
            List<String> subCommands = Arrays.asList("start", "stop", "reset", "reload", "setup", "gui", "neighbors", "voisins", "testskin", "help");
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

        if (args.length == 2 && args[0].equalsIgnoreCase("gui")) {
            // Arguments pour /lg gui
            return VALID_GUIS.keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("spawn")) {
            // Numéros de spawn (1 et plus, suggérer jusqu'à 20)
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                numbers.add(String.valueOf(i));
            }
            return numbers.stream()
                    .filter(s -> s.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("neighbors") || args[0].equalsIgnoreCase("voisins"))) {
            // Liste des joueurs en ligne pour la commande neighbors
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("testskin")) {
            // Arguments pour /lg testskin
            List<String> skinActions = Arrays.asList("apply", "restore");
            return skinActions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("testskin")) {
            // Liste des joueurs en ligne pour la commande testskin
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}

