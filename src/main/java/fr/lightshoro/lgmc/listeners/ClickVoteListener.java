package fr.lightshoro.lgmc.listeners;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.managers.GameManager;
import fr.lightshoro.lgmc.models.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Listener pour le système de vote par clic gauche
 * Alternative au système GUI traditionnel
 */
public class ClickVoteListener implements Listener {
    private final Lgmc plugin;

    public ClickVoteListener(Lgmc plugin) {
        this.plugin = plugin;
    }

    /**
     * Détecte le clic gauche (air ou bloc) et fait un raycast pour trouver le joueur visé
     */
    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        // Vérifier que c'est un clic gauche
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player damager = event.getPlayer();
        GameManager gm = plugin.getGameManager();

        // Vérifier que le mode clic est activé
        if (!plugin.getConfigManager().isClickVoteMode()) {
            return;
        }

        // Vérifier que le jeu est en cours
        if (!gm.isInGame()) {
            return;
        }

        // Vérifier que le joueur est vivant
        if (!gm.getPlayersAlive().contains(damager)) {
            return;
        }

        ItemStack itemInHand = damager.getInventory().getItemInMainHand();
        String gameStep = gm.getGameStep();

        // Vérifier si le joueur a un item pertinent en main
        boolean hasRelevantItem = false;
        
        if (itemInHand.getType() == Material.WRITTEN_BOOK && gm.isCapitaineSuccession()) {
            hasRelevantItem = true;
        } else if (itemInHand.getType() == Material.PAPER && 
                   ("doCapitaine".equals(gameStep) || "doVote".equals(gameStep))) {
            hasRelevantItem = true;
        } else if (itemInHand.getType() == Material.IRON_HOE && 
                   "doLoupGarou".equals(gameStep) && gm.getLoupGarous().contains(damager)) {
            hasRelevantItem = true;
        } else if (itemInHand.getType() == Material.GUNPOWDER && 
                   "doSorciere".equals(gameStep) && damager.equals(gm.getSorciere())) {
            hasRelevantItem = true;
        } else if (itemInHand.getType() == Material.WHEAT && 
                   "doVoleur".equals(gameStep) && damager.equals(gm.getVoleur())) {
            hasRelevantItem = true;
        } else if (itemInHand.getType() == Material.FLINT &&
                   "doVoyante".equals(gameStep) && damager.equals(gm.getVoyante())) {
            hasRelevantItem = true;
        } else if ("doCupidon".equals(gameStep) && damager.equals(gm.getGamePlayer(damager).getPlayer())) {
            hasRelevantItem = true;
        }

        if (!hasRelevantItem) {
            return;
        }

        // Si le joueur fait shift-clic, il se désigne lui-même (sauf voyante et chasseur)
        if (damager.isSneaking()) {
            // Exclure la voyante et le chasseur (qui tire avec la houe en bois, pas un item spécifique)
            if (!"doVoyante".equals(gameStep)) {
                handlePlayerClick(damager, damager, itemInHand, gameStep);
                return;
            }
        }

        // Faire un raycast pour trouver le joueur visé (ignorer les morts)
        Player target = getTargetedPlayer(damager, 50.0);

        if (target == null) {
            return;
        }

        // Traiter l'action
        handlePlayerClick(damager, target, itemInHand, gameStep);
    }

    /**
     * Backup: détection directe du clic sur entité (au cas où le raycast ne fonctionne pas)
     */
    @EventHandler
    public void onPlayerLeftClickPlayer(EntityDamageByEntityEvent event) {
        // Vérifier que c'est un joueur qui clique sur un joueur
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        GameManager gm = plugin.getGameManager();

        // Vérifier que le mode clic est activé
        if (!plugin.getConfigManager().isClickVoteMode()) {
            return;
        }

        // Vérifier que le jeu est en cours
        if (!gm.isInGame()) {
            return;
        }

        // Annuler les dégâts
        event.setCancelled(true);

        // Vérifier que le joueur est vivant
        if (!gm.getPlayersAlive().contains(damager)) {
            return;
        }

        ItemStack itemInHand = damager.getInventory().getItemInMainHand();
        String gameStep = gm.getGameStep();

        handlePlayerClick(damager, target, itemInHand, gameStep);
    }

    /**
     * Gère le clic d'un joueur sur un autre joueur
     */
    private void handlePlayerClick(Player damager, Player target, ItemStack itemInHand, String gameStep) {
        GameManager gm = plugin.getGameManager();
        GamePlayer gp = gm.getGamePlayer(damager);

        // TESTAMENT DU CAPITAINE (avec livre)
        if (itemInHand.getType() == Material.WRITTEN_BOOK && gm.isCapitaineSuccession()) {
            if (!damager.equals(gm.getDyingCapitaine())) {
                return;
            }

            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.player-dead"));
                return;
            }

            if (target.equals(damager)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("commands.lgreload.cant-self"));
                return;
            }

            // Nommer le successeur
            gm.setCapitaine(target);
            gm.setSuccessorChosen(true);

            // Retirer le livre du capitaine mourant
            damager.getInventory().setItem(4, new ItemStack(Material.AIR));

            // Donner le casque au nouveau capitaine
            target.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("capitaine"));

            // Annoncer le nouveau capitaine
            plugin.getServer().broadcastMessage(
                plugin.getLanguageManager().getMessage("succession.testament-chosen")
                    .replace("{dying}", damager.getName())
                    .replace("{new}", target.getName())
            );

            // Avancer le timer
            plugin.getTimerManager().advanceTimer();
            return;
        }

        // VOTE DU CAPITAINE (avec papier)
        if ("doCapitaine".equals(gameStep) && itemInHand.getType() == Material.PAPER) {
            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            // Permettre de voter pour soi-même (pas de vérification cant-shoot-self)

            // Ajouter l'effet glow sur le joueur voté pour capitaine
            plugin.getVoteDisplayManager().setGlowEffect(damager, target);

            gm.incrementCapitaineVote(target, damager);
            
            // Mettre à jour l'affichage des votes pour le capitaine
            plugin.getVoteDisplayManager().updateCapitaineVoteDisplays();

            damager.sendMessage(plugin.getLanguageManager().getMessage("gui.click-mode.vote-player")
                    .replace("{player}", target.getName()));

            return;
        }

        // VOTE DU JOUR (avec papier)
        if ("doVote".equals(gameStep) && itemInHand.getType() == Material.PAPER) {
            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            // Permettre de voter pour soi-même (pas de vérification cant-shoot-self)

            // Ajouter l'effet glow sur le joueur voté
            plugin.getVoteDisplayManager().setGlowEffect(damager, target);

            gm.incrementVoteCount(target, damager);
            gm.setVoteWasMade(true);

            damager.sendMessage(plugin.getLanguageManager().getMessage("gui.click-mode.vote-player")
                    .replace("{player}", target.getName()));

            // Mettre à jour l'affichage des votes
            plugin.getVoteDisplayManager().updateAllVoteDisplays();

            return;
        }

        // VOYANTE (pendant la nuit, avec silex)
        if ("doVoyante".equals(gameStep) && 
            itemInHand.getType() == Material.FLINT &&
            damager.equals(gm.getVoyante())) {
            if (gm.isVoyanteSondage()) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("vote.day.already-voted"));
                return;
            }

            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            GamePlayer targetGP = gm.getGamePlayer(target);
            String roleName = targetGP.getRole().getFormattedName();

            damager.sendMessage(plugin.getLanguageManager().getMessage("actions.voyante.probe")
                    .replace("{player}", target.getName())
                    .replace("{role}", roleName));

            gm.setVoyanteSondage(true);

            // Nettoyer les items
            damager.getInventory().setItem(4, new ItemStack(Material.AIR));
            damager.getInventory().setItem(8, new ItemStack(Material.AIR));

            // Finir automatiquement après un court délai
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (gm.isInGame() && "doVoyante".equals(gm.getGameStep())) {
                    plugin.getTimerManager().advanceTimer();
                }
            }, 40L);
            return;
        }

        // LOUPS-GAROUS (avec houe en fer)
        if ("doLoupGarou".equals(gameStep) && 
            itemInHand.getType() == Material.IRON_HOE && 
            gm.getLoupGarous().contains(damager)) {

            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            // Permettre de se désigner soi-même (stratégie de bluff)

            // Ajouter l'effet glow sur le joueur désigné
            plugin.getVoteDisplayManager().setGlowEffect(damager, target);

            // Utiliser la méthode qui gère le changement de désignation
            gm.designatePlayerAsWolf(target, damager);

            damager.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.designated")
                    .replace("{player}", target.getName()));

            // Vérifier si tous les loups ont voté
            if (checkAllWolvesVoted()) {
                plugin.getTimerManager().advanceTimer();
            }
            return;
        }

        // SORCIERE - Potion de mort (avec gunpowder)
        if ("doSorciere".equals(gameStep) && 
            itemInHand.getType() == Material.GUNPOWDER && 
            damager.equals(gm.getSorciere())) {
            
            if (gm.isPotionMort()) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.already-used-death"));
                return;
            }

            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            // Permettre de se suicider avec la potion de mort

            // Empoisonner le joueur
            damager.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.poisoned")
                    .replace("{player}", target.getName()));
            
            gm.waitListAdd(target, "sorciere");
            gm.setPotionMort(true);
            gm.setSorciereAction(true);

            // Nettoyer les items
            damager.getInventory().setItem(4, new ItemStack(Material.AIR));
            damager.getInventory().setItem(8, new ItemStack(Material.AIR));

            // Avancer le timer
            plugin.getTimerManager().advanceTimer();
            return;
        }

        // VOLEUR - Vol de rôle (pendant la première nuit, avec wheat)
        if ("doVoleur".equals(gameStep) && 
            itemInHand.getType() == Material.WHEAT && 
            damager.equals(gm.getVoleur())) {
            
            if (gm.isVoleurAction()) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("vote.day.already-voted"));
                return;
            }

            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            if (target.equals(damager)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.cant-shoot-self"));
                return;
            }

            // Voler le rôle
            GamePlayer targetGp = gm.getGamePlayer(target);
            fr.lightshoro.lgmc.models.Role targetRole = targetGp.getRole();

            // Get voleur's game player
            GamePlayer voleurGp = gm.getGamePlayer(damager);

            // Steal the role
            voleurGp.setRole(targetRole);
            targetGp.setRole(fr.lightshoro.lgmc.models.Role.VILLAGEOIS);

            // Update role-specific player references in GameManager
            gm.removePlayerFromRole(target);
            gm.addPlayerToRole(damager, targetRole);

            // Update item in hand for both players
            damager.getInventory().setItemInOffHand(
                plugin.getConfigManager().getRoleHelmetItemStack(targetRole.getName().toLowerCase().replace(" ", "-"))
            );
            target.getInventory().setItemInOffHand(
                plugin.getConfigManager().getRoleHelmetItemStack("villageois")
            );

            // Send messages
            damager.sendMessage(plugin.getLanguageManager().getMessage("roles.voleur.stole-role")
                .replace("{player}", target.getName())
                .replace("{role}", targetRole.getFormattedName()));
            
            target.sendMessage(plugin.getLanguageManager().getMessage("roles.voleur.victim-message"));

            gm.setVoleurAction(true);

            // Nettoyer les items
            damager.getInventory().setItem(4, new ItemStack(Material.AIR));
            damager.getInventory().setItem(8, new ItemStack(Material.AIR));

            // Avancer le timer
            plugin.getTimerManager().advanceTimer();
            return;
        }

        // CUPIDON - Premier amoureux (pendant la nuit, n'importe quel item)
        if ("doCupidon".equals(gameStep) && damager.equals(plugin.getGameManager().getGamePlayer(damager).getPlayer())) {
            if (!gm.getPlayersAlive().contains(target)) {
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.already-dead"));
                return;
            }

            GamePlayer cupidonGP = gm.getGamePlayer(damager);
            
            // Premier amoureux
            if (cupidonGP.getFirstLover() == null) {
                cupidonGP.setFirstLover(target);
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.first-lover")
                        .replace("{player}", target.getName()));
                return;
            }
            // Second amoureux
            else if (cupidonGP.getSecondLover() == null) {
                if (target.equals(cupidonGP.getFirstLover())) {
                    damager.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.same-player"));
                    return;
                }
                
                cupidonGP.setSecondLover(target);
                damager.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.second-lover")
                        .replace("{player}", target.getName()));
                
                // Créer le couple
                Player lover1 = cupidonGP.getFirstLover();
                Player lover2 = cupidonGP.getSecondLover();
                
                gm.addLovers(lover1, lover2);
                gm.setCupidonAction(true);
                
                // Informer les amoureux
                lover1.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.in-love")
                        .replace("{player}", lover2.getName()));
                lover2.sendMessage(plugin.getLanguageManager().getMessage("actions.cupidon.in-love")
                        .replace("{player}", lover1.getName()));
                
                // Nettoyer les items
                damager.getInventory().setItem(8, new ItemStack(Material.AIR));
                
                // Avancer le timer
                plugin.getTimerManager().advanceTimer();
                return;
            }
        }
    }

    /**
     * Vérifie si tous les joueurs vivants ont voté pour le capitaine
     */
    private boolean checkAllVotedCapitaine() {
        GameManager gm = plugin.getGameManager();
        for (Player player : gm.getPlayersAlive()) {
            GamePlayer gp = gm.getGamePlayer(player);
            if (!gp.isDidVoteForCapitaine()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si tous les joueurs vivants ont voté pour le vote du jour
     */
    private boolean checkAllVotedDay() {
        GameManager gm = plugin.getGameManager();
        for (Player player : gm.getPlayersAlive()) {
            GamePlayer gp = gm.getGamePlayer(player);
            if (!gp.isDidVote()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si tous les loups-garous ont voté
     */
    private boolean checkAllWolvesVoted() {
        GameManager gm = plugin.getGameManager();
        for (Player lg : gm.getLoupGarous()) {
            if (!gm.getPlayersAlive().contains(lg)) continue;
            GamePlayer gp = gm.getGamePlayer(lg);
            if (!gp.isDidVote()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Trouve le joueur visé par le raycast (ignore les joueurs morts)
     */
    private Player getTargetedPlayer(Player shooter, double range) {
        org.bukkit.util.Vector direction = shooter.getEyeLocation().getDirection();
        org.bukkit.Location start = shooter.getEyeLocation();
        GameManager gm = plugin.getGameManager();

        Player closestPlayer = null;
        double closestDistance = range;

        for (Player target : shooter.getWorld().getPlayers()) {
            // Ignorer soi-même
            if (target.equals(shooter)) continue;
            
            // Ignorer les joueurs morts
            if (!gm.getPlayersAlive().contains(target)) continue;

            org.bukkit.util.Vector toTarget = target.getEyeLocation().toVector().subtract(start.toVector());
            double distance = toTarget.length();

            if (distance > range) continue;

            // Normaliser les vecteurs pour le calcul de l'angle
            double angle = direction.angle(toTarget);

            // Si l'angle est inférieur à 0.3 attoradian (~17 degrés), considérer comme un hit
            if (angle < 0.3 && distance < closestDistance) {
                closestPlayer = target;
                closestDistance = distance;
            }
        }

        return closestPlayer;
    }
}
