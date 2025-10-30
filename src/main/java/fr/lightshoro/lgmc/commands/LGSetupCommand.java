package fr.lightshoro.lgmc.commands;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.LocationManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LGSetupCommand implements CommandExecutor {
    private final Lgmc plugin;

    public LGSetupCommand(Lgmc plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        LocationManager lm = plugin.getLocationManager();

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "campfire":
                lm.setCampfireLocation(player.getLocation());
                lm.saveLocations();
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.campfire-set"));
                break;

            case "chasseurtp":
                lm.setChasseurTpLocation(player.getLocation());
                lm.saveLocations();
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.chasseur-set"));
                break;

            case "spawn":
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.usage"));
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[1]);
                    lm.setSpawnLocation(index, player.getLocation());
                    lm.saveLocations();
                    player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.spawn-set")
                        .replace("{number}", String.valueOf(index)));
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.usage"));
                }
                break;

            case "info":
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-header"));
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-campfire")
                    .replace("{location}", lm.getCampfireLocation() != null ? formatLocation(lm.getCampfireLocation()) :
                        plugin.getLanguageManager().getMessage("commands.lgsetup.not-set")));
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-chasseur")
                    .replace("{location}", lm.getChasseurTpLocation() != null ? formatLocation(lm.getChasseurTpLocation()) :
                        plugin.getLanguageManager().getMessage("commands.lgsetup.not-set")));
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.info-spawns")
                    .replace("{count}", String.valueOf(lm.getSpawnCount())));
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-header"));
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-campfire"));
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-chasseurtp"));
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-spawn"));
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.lgsetup.help-info"));
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("%s (%.1f, %.1f, %.1f)",
            loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }
}

