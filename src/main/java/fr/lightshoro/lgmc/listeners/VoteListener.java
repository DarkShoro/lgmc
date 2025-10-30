package fr.lightshoro.lgmc.listeners;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.gui.CapitaineVoteGUI;
import fr.lightshoro.lgmc.gui.VoteGUI;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VoteListener implements Listener {
    private final Lgmc plugin;

    public VoteListener(Lgmc plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickWithPaper(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PAPER) {
            return;
        }

        GameManager gm = plugin.getGameManager();
        String gameStep = gm.getGameStep();

        if ("doCapitaine".equals(gameStep)) {
            GamePlayer gp = gm.getGamePlayer(player);
            if (gp.isDidVoteForCapitaine()) {
                player.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.already-voted"));
            } else {
                new CapitaineVoteGUI(plugin).open(player);
            }
        } else if ("doVote".equals(gameStep)) {
            GamePlayer gp = gm.getGamePlayer(player);
            if (gp.isDidVote()) {
                player.sendMessage(plugin.getLanguageManager().getMessage("vote.day.already-voted"));
            } else {
                new VoteGUI(plugin).open(player);
            }
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.cannot-vote-now"));
        }
    }

    public void shootHoe(PlayerInteractEvent event) {
        // Quand le jeu n'est pas en cours, faire clic-droit avec une houe permet de tirer à volonté.

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.WOODEN_HOE) {
            return;
        }

        // Trace from player eye to 50 blocks ahead, find first hit block or entity
        traceShot(player, 50.0);

    }

    @EventHandler
    public void onRightClickWithHoe(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.WOODEN_HOE) {
            return;
        }

        GameManager gm = plugin.getGameManager();

        if (!gm.isInGame()) {
            // Le jeu n'est pas en cours, permettre de tirer librement
            shootHoe(event);
            return;
        }

        if (player.equals(gm.getChasseur())) {
            // Get the player the chasseur is looking at
            Player target = getTargetedPlayer(player, 50.0);

            if (target == null) {
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.must-aim"));
                return;
            }

            if (!gm.getPlayersAlive().contains(target)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            if (target.equals(player)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.cant-shoot-self"));
                return;
            }

            // Set target and execute instant kill
            gm.setChasseurTarget(target);

            player.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.aim")
                .replace("{player}", target.getName()));

            // Execute the kill immediately with visual effects
            gm.getFinishers().finishChasseur();
        }
    }

    @EventHandler
    public void onRightClickWithPlayerHead(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return;
        }

        GameManager gm = plugin.getGameManager();

        // Vérifier si c'est le capitaine en train de départager
        if (!gm.isCapitaineTieBreakerInProg() || !player.equals(gm.getCapitaine())) {
            return;
        }

        // Obtenir le joueur ciblé à partir de la tête
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) {
            return;
        }

        String targetName = meta.getOwningPlayer().getName();
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            return;
        }

        // Vérifier que c'est bien un des deux joueurs en égalité
        if (!target.equals(gm.getMostVoted()) && !target.equals(gm.getDoubleMostVoted())) {
            return;
        }

        // Tuer le joueur sélectionné
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("vote.tiebreaker.decided")
                              .replace("{capitaine}", player.getName())
                              .replace("{player}", target.getName()));

        // Retirer les têtes du capitaine
        player.getInventory().setItem(3, new ItemStack(Material.AIR));
        player.getInventory().setItem(5, new ItemStack(Material.AIR));

        // Tuer le joueur
        gm.killPlayer(target, "vote");

        // Finir le départage
        gm.getFinishers().finishTieBreaker();
    }

    /**
     * Get the player that the shooter is looking at
     */
    private Player getTargetedPlayer(Player shooter, double range) {
        org.bukkit.util.Vector direction = shooter.getEyeLocation().getDirection();
        org.bukkit.Location start = shooter.getEyeLocation();

        Player closestPlayer = null;
        double closestDistance = range;

        for (Player target : shooter.getWorld().getPlayers()) {
            if (target.equals(shooter)) continue;

            org.bukkit.util.Vector toTarget = target.getEyeLocation().toVector().subtract(start.toVector());
            double distance = toTarget.length();

            if (distance > range) continue;

            // Normalize vectors for angle calculation
            double angle = direction.angle(toTarget);

            // If angle is less than 0.3 radians (~17 degrees), consider it a hit
            if (angle < 0.3 && distance < closestDistance) {
                closestPlayer = target;
                closestDistance = distance;
            }
        }

        return closestPlayer;
    }

    // Trace function to find either block or entity in length
    private void traceShot(@NotNull Player player, double maxDistance) {
        // Get the starting location (player's eye position)
        Location start = player.getEyeLocation();

        // Get the direction the player is looking at
        Vector direction = start.getDirection();

        // Perform the ray trace in the world
        RayTraceResult result = player.getWorld().rayTrace(
                start,
                direction,
                maxDistance,
                FluidCollisionMode.NEVER, // ignore fluids
                true,                     // ignore passable blocks like tall grass
                0.1,                      // entity border size (how close is considered a hit)
                entity -> entity != player // ignore self
        );

        // Check if something was hit
        if (result == null) {
            // If no hit, return false
            return;
        }

        // Check for entity hit
        if (result.getHitEntity() != null) {
            Entity entity = result.getHitEntity();
            // if hit, kill entity
            // Example: spawn particles at the hit position
            player.getWorld().spawnParticle(
                    Particle.CRIT,
                    result.getHitPosition().toLocation(player.getWorld()),
                    10,
                    0, 0, 0,
                    0.1
            );
            if (entity instanceof LivingEntity entity1) {
                entity1.damage(1000.0, player); // deal lethal damage
            }
            return;
        }

        // Check for block hit
        if (result.getHitBlock() != null) {
            Block block = result.getHitBlock();
            BlockFace face = result.getHitBlockFace();
            // Example: spawn particles at the hit position
            player.getWorld().spawnParticle(
                    Particle.CRIT,
                    result.getHitPosition().toLocation(player.getWorld()),
                    10,
                    0, 0, 0,
                    0.1
            );
        }

        // make particle just like the gunshot from player's location to the hit location
        for (double d = 0; d < result.getHitPosition().toLocation(player.getWorld()).distance(start); d += 0.5) {
            Location particleLocation = start.clone().add(direction.clone().multiply(d));
            player.getWorld().spawnParticle(
                    Particle.SMOKE,
                    particleLocation,
                    1,
                    0, 0, 0,
                    0.0
            );
        }

    }

}

