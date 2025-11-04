package fr.lightshoro.lgmc.listeners;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
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

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.gui.CapitaineVoteGUI;
import fr.lightshoro.lgmc.gui.LoupGarouGUI;
import fr.lightshoro.lgmc.gui.TestamentGUI;
import fr.lightshoro.lgmc.gui.VoteGUI;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;

@SuppressWarnings("deprecation")
public class VoteListener implements Listener {
    private final Lgmc plugin;

    public VoteListener(Lgmc plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickWithPaper(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        GameManager gm = plugin.getGameManager();
        String gameStep = gm.getGameStep();

        // Gestion de la plume pour skip (mode clic)
        if (item.getType() == Material.FEATHER && plugin.getConfigManager().isClickVoteMode()) {
            if (!gm.isInGame()) {
                return;
            }

            GamePlayer gp = gm.getGamePlayer(player);
            
            // Skip pour les votes
            if ("doCapitaine".equals(gameStep)) {
                // If the player already voted, remove their previous vote (vote blank)
                if (gp.isDidVoteForCapitaine()) {
                    Player previousVote = gp.getVotedCapitaine();
                    if (previousVote != null) {
                        int currentVotes = gm.getVoteCapitaine().getOrDefault(previousVote, 0);
                        if (currentVotes > 0) {
                            gm.getVoteCapitaine().put(previousVote, currentVotes - 1);
                        }
                        // Remove glow effect
                        plugin.getVoteDisplayManager().removeGlowEffect(player, previousVote);
                        // Update vote displays
                        plugin.getVoteDisplayManager().updateCapitaineVoteDisplays();
                    }
                    // Mark as having voted but with no vote registered (blank vote)
                    gp.setVotedCapitaine(null);
                    player.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.no-vote"));
                } else {
                    // First time clicking skip
                    gp.setDidVoteForCapitaine(true);
                    player.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.no-vote"));
                }
                return;
            } else if ("doVote".equals(gameStep)) {
                // If the player already voted, remove their previous vote (vote blank)
                if (gp.isDidVote()) {
                    Player previousVote = gp.getVotedPlayer();
                    if (previousVote != null) {
                        int voteValue = (player.equals(gm.getCapitaine())) ? 2 : 1;
                        int currentVotes = gm.getVoteCount().getOrDefault(previousVote, 0);
                        if (currentVotes >= voteValue) {
                            gm.getVoteCount().put(previousVote, currentVotes - voteValue);
                        }
                        // Remove glow effect
                        plugin.getVoteDisplayManager().removeGlowEffect(player, previousVote);
                        // Update vote displays
                        plugin.getVoteDisplayManager().updateAllVoteDisplays();
                    }
                    // Mark as having voted but with no vote registered (blank vote)
                    gp.setVotedPlayer(null);
                    player.sendMessage(plugin.getLanguageManager().getMessage("vote.day.no-vote"));
                } else {
                    // First time clicking skip
                    gp.setDidVote(true);
                    player.sendMessage(plugin.getLanguageManager().getMessage("vote.day.no-vote"));
                }
                return;
            } else if ("doVoyante".equals(gameStep) && player.equals(gm.getVoyante()) && !gm.isVoyanteSondage()) {
                gm.setVoyanteSondage(true);
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.voyante.no-probe"));
                // Finir automatiquement
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (gm.isInGame() && "doVoyante".equals(gm.getGameStep())) {
                        plugin.getTimerManager().advanceTimer();
                    }
                }, 20L);
                return;
            } else if ("doLoupGarou".equals(gameStep) && gm.getLoupGarous().contains(player) && !gp.isDidVote()) {
                gp.setDidVote(true);
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.no-target"));
                
                // Vérifier si tous les loups ont voté pour avancer le timer
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (gm.isInGame() && "doLoupGarou".equals(gm.getGameStep())) {
                        boolean allVoted = true;
                        for (Player lg : gm.getLoupGarous()) {
                            if (gm.getPlayersAlive().contains(lg)) {
                                GamePlayer lgGp = gm.getGamePlayer(lg);
                                if (!lgGp.isDidVote()) {
                                    allVoted = false;
                                    break;
                                }
                            }
                        }
                        if (allVoted) {
                            plugin.getTimerManager().advanceTimer();
                        }
                    }
                }, 20L);
                return;
            } else if ("doSorciere".equals(gameStep) && player.equals(gm.getSorciere()) && !gm.isSorciereAction()) {
                gm.setSorciereAction(false);
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.no-action"));
                // Finir automatiquement
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (gm.isInGame() && "doSorciere".equals(gm.getGameStep())) {
                        plugin.getTimerManager().advanceTimer();
                    }
                }, 20L);
                return;
            } else if ("doCupidon".equals(gameStep) && player.equals(gm.getCupidon()) && !gm.isCupidonAction()) {
                gm.setCupidonAction(false);
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.no-action"));
                // Finir automatiquement
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (gm.isInGame() && "doCupidon".equals(gm.getGameStep())) {
                        plugin.getTimerManager().advanceTimer();
                    }
                }, 20L);
                return;
            } else if ("doVoleur".equals(gameStep) && player.equals(gm.getVoleur()) && !gm.isVoleurAction()) {
                gm.setVoleurAction(false);
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.voleur.no-steal"));
                // Finir automatiquement
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (gm.isInGame() && "doVoleur".equals(gm.getGameStep())) {
                        plugin.getTimerManager().advanceTimer();
                    }
                }, 20L);
                return;
            }
            return;
        }

        // Gestion du livre Testament pour la succession du capitaine
        if (item.getType() == Material.WRITTEN_BOOK) {
            if (!gm.isInGame() || !gm.isCapitaineSuccession()) {
                return;
            }

            // Vérifier que c'est bien le capitaine mourant
            if (!player.equals(gm.getDyingCapitaine())) {
                return;
            }

            // En mode clic, le testament se fait par clic gauche sur un joueur
            if (plugin.getConfigManager().isClickVoteMode()) {
                //player.sendMessage(plugin.getLanguageManager().getMessage("gui.click-mode.left-click-hint"));
                event.setCancelled(true);
                return;
            }

            // Ouvrir le GUI de testament (mode GUI uniquement)
            new TestamentGUI(plugin).open(player);
            event.setCancelled(true);
            return;
        }

        // Gestion du papier pour les votes (mode GUI uniquement)
        if (item.getType() != Material.PAPER) {
            return;
        }

        // En mode clic, ne pas ouvrir de GUI avec le papier
        if (plugin.getConfigManager().isClickVoteMode()) {
            //player.sendMessage(plugin.getLanguageManager().getMessage("gui.click-mode.left-click-hint"));
            return;
        }

        // If either dead or not in game, cannot vote
        if (!gm.isInGame() || !gm.getPlayersAlive().contains(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.cannot-vote-now"));
            return;
        }

        if ("doCapitaine".equals(gameStep)) {
            new CapitaineVoteGUI(plugin).open(player);
        } else if ("doVote".equals(gameStep)) {
            new VoteGUI(plugin).open(player);
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

    public void handleWoodenHoe(PlayerInteractEvent event) {
        GameManager gm = plugin.getGameManager();
        Player player = event.getPlayer();

        if (!gm.isInGame()) {
            // Le jeu n'est pas en cours, permettre de tirer librement
            shootHoe(event);
            return;
        }

        if (player.equals(gm.getChasseur())) {
            // Get the player the chasseur is looking at (ignores dead players)
            Player target = getTargetedPlayer(player, 50.0);

            if (target == null) {
                player.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.must-aim"));
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

    public void handleIronHoe(PlayerInteractEvent event) {
        GameManager gm = plugin.getGameManager();
        Player player = event.getPlayer();

        if (!gm.isInGame()) {
            // Le jeu n'est pas en cours, le vote n'a pas lieu d'être
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.cannot-vote-now"));
            return;
        }

        // Iron hoe is used as the vote item for werewolves
        if (!gm.getLoupGarous().contains(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("errors.cannot-vote-now"));
            return;
        }

        // En mode clic, ne pas ouvrir le GUI
        if (plugin.getConfigManager().isClickVoteMode()) {
            //player.sendMessage(plugin.getLanguageManager().getMessage("gui.click-mode.left-click-hint"));
            return;
        }

        GamePlayer gp = gm.getGamePlayer(player);
        if (gp.isDidVote()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("vote.day.already-voted"));
            return;
        }

        new LoupGarouGUI(plugin).open(player);
    }

    @EventHandler
    public void onRightClickWithHoe(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        switch (item.getType()) {
            case WOODEN_HOE -> handleWoodenHoe(event);
            case IRON_HOE -> handleIronHoe(event);
            default -> {
                // Do nothing
            }
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

        // Vérifier si c'est l'Ange qui est éliminé au premier jour
        if (gm.checkAngeVictory(target)) {
            return; // L'Ange a gagné, le jeu est terminé
        }

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
        GameManager gm = plugin.getGameManager();

        Player closestPlayer = null;
        double closestDistance = range;

        for (Player target : shooter.getWorld().getPlayers()) {
            if (target.equals(shooter)) continue;
            
            // Ignore dead players - they shouldn't be targeted
            if (!gm.getPlayersAlive().contains(target)) {
                continue;
            }

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

        // Play sound "custom:gun"
        player.getWorld().playSound(
                player.getLocation(),
                "custom:gun",
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        GameManager gm = plugin.getGameManager();

        // Perform the ray trace in the world
        RayTraceResult result = player.getWorld().rayTrace(
                start,
                direction,
                maxDistance,
                FluidCollisionMode.NEVER, // ignore fluids
                true,                     // ignore passable blocks like tall grass
                0.1,                      // entity border size (how close is considered a hit)
                entity -> {
                    // Ignore self
                    if (entity.equals(player)) {
                        return false;
                    }
                    // Ignore dead players (they shouldn't block shots)
                    if (entity instanceof Player targetPlayer) {
                        GamePlayer targetGp = gm.getGamePlayer(targetPlayer);
                        if (targetGp != null && !gm.getPlayersAlive().contains(targetPlayer)) {
                            return false;
                        }
                    }
                    return true;
                }
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

