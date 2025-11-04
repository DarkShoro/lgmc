package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.models.GamePlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe contenant tous les "finishers" - méthodes qui finalisent les actions des rôles
 */
public class RoleFinishers {
    private final Lgmc plugin;
    private final GameManager gm;

    public RoleFinishers(Lgmc plugin, GameManager gm) {
        this.plugin = plugin;
        this.gm = gm;
    }

    public void finishVoleur() {
        if (gm.getVoleur() != null) {
            // Nettoyer l'inventaire du voleur
            gm.clearRelevantItems(gm.getVoleur());
        }

        if (!gm.isVoleurAction()) {
            if (gm.getVoleur() != null) {
                gm.getVoleur().sendMessage(plugin.getLanguageManager().getMessage("actions.voleur.no-steal"));
            }
        }

        plugin.getTimerManager().advanceTimer();
    }

    public void finishVoyante() {
        if (gm.getVoyante() != null) {
            gm.getVoyante().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
            for (Player player : gm.getPlayersAlive()) {
                gm.getVoyante().hidePlayer(plugin, player);
            }
            
            // Nettoyer l'inventaire de la voyante
            gm.clearRelevantItems(gm.getVoyante());
        }

        if (!gm.isVoyanteSondage()) {
            if (gm.getVoyante() != null) {
                gm.getVoyante().sendMessage(plugin.getLanguageManager().getMessage("actions.voyante.no-probe"));
            }
        }

        plugin.getTimerManager().advanceTimer();
    }

    public void finishLoupGarou() {
        // Cacher tout le monde des loups-garous
        for (Player lg : gm.getLoupGarous()) {
            lg.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
            for (Player player : gm.getPlayersAlive()) {
                lg.hidePlayer(plugin, player);
            }
            
            // Restaurer le skin original de chaque loup-garou
            plugin.getSkinManager().restoreOriginalSkin(lg);
        }

        // Cacher aussi pour la petite fille
        if (gm.getPetiteFille() != null && gm.getPlayersAlive().contains(gm.getPetiteFille())) {
            gm.getPetiteFille().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
            for (Player player : gm.getPlayersAlive()) {
                gm.getPetiteFille().hidePlayer(plugin, player);
            }
        }

        if (gm.getDesignationCount() == 0) {
            for (Player lg : gm.getLoupGarous()) {
                lg.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.no-target"));
            }
            plugin.getTimerManager().advanceTimer();
            // Retire leur ironhoe aux loups-garous
            for (Player lg : gm.getLoupGarous()) {
                gm.clearRelevantItems(lg);
            }
            return;
        }

        if (gm.getDesignationCount() == 1) {
            Player target = gm.getDesignatedPlayers().keySet().iterator().next();
            for (Player lg : gm.getLoupGarous()) {
                lg.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.designated")
                    .replace("{player}", target.getName()));
            }
            gm.setNextLGTarget(target);
            gm.waitListAdd(target, "loupGarou");
            plugin.getTimerManager().advanceTimer();
            // Retire leur ironhoe aux loups-garous
            for (Player lg : gm.getLoupGarous()) {
                gm.clearRelevantItems(lg);
            }
            return;
        }

        // Multiples désignations - vérifier l'unanimité
        int numLG = gm.getLoupGarous().size();
        Player unanimousChoice = null;

        for (Map.Entry<Player, Integer> entry : gm.getDesignatedPlayers().entrySet()) {
            if (entry.getValue() == numLG) {
                unanimousChoice = entry.getKey();
                break;
            }
        }

        if (unanimousChoice != null) {
            for (Player lg : gm.getLoupGarous()) {
                lg.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.designated")
                    .replace("{player}", unanimousChoice.getName()));
            }
            gm.setNextLGTarget(unanimousChoice);
            gm.waitListAdd(unanimousChoice, "loupGarou");
        } else {
            for (Player lg : gm.getLoupGarous()) {
                lg.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.no-agreement"));
            }
        }

        // Retire leur ironhoe aux loups-garous
        for (Player lg : gm.getLoupGarous()) {
            gm.clearRelevantItems(lg);
        }

        plugin.getChatManager().setLoupGarouChatActive(false);
        plugin.getTimerManager().advanceTimer();
    }

    public void finishSorciere() {
        if (gm.getSorciere() != null) {
            for (Player player : gm.getPlayersAlive()) {
                gm.getSorciere().hidePlayer(plugin, player);
            }
            
            // Nettoyer l'inventaire de la sorcière
            gm.clearRelevantItems(gm.getSorciere());
        }

        plugin.getTimerManager().advanceTimer();
    }

    public void finishCapitaine() {
        if (!gm.isCapitaineVoteInProg()) {
            return;
        }

        gm.setCapitaineVoteInProg(false);

        Player mostVoted = null;
        int mostVotedNum = 0;

        // Trouver le joueur le plus voté
        for (Map.Entry<Player, Integer> entry : gm.getVoteCapitaine().entrySet()) {
            if (entry.getValue() > mostVotedNum) {
                mostVoted = entry.getKey();
                mostVotedNum = entry.getValue();
            }
        }

        if (mostVoted == null) {
            // Personne n'a voté, choisir au hasard
            List<Player> alivePlayers = new ArrayList<>(gm.getPlayersAlive());
            if (!alivePlayers.isEmpty()) {
                mostVoted = alivePlayers.get((int)(Math.random() * alivePlayers.size()));
            }
        } else {
            // Vérifier s'il y a égalité
            List<Player> allPlayers = new ArrayList<>(gm.getPlayersAlive());
            allPlayers.remove(mostVoted);

            Player doubleMostVoted = null;
            int doubleMostVotedNum = 0;

            for (Player player : allPlayers) {
                int votes = gm.getVoteCapitaine().getOrDefault(player, 0);
                if (votes > doubleMostVotedNum) {
                    doubleMostVoted = player;
                    doubleMostVotedNum = votes;
                }
            }

            // Si égalité, choisir au hasard entre les deux
            if (doubleMostVotedNum == mostVotedNum) {
                List<Player> tied = new ArrayList<>();
                tied.add(mostVoted);
                tied.add(doubleMostVoted);
                mostVoted = tied.get((int)(Math.random() * tied.size()));
            }
        }

        gm.setCapitaine(mostVoted);

        for (Player player : gm.getPlayingPlayers()) {
            assert mostVoted != null;
            player.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.elected")
                             .replace("{player}", mostVoted.getName()));
        }

        // Donner un casque bleu au capitaine
        assert mostVoted != null;
        mostVoted.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("capitaine"));

        // Nettoyer les votes et réinitialiser les flags
        gm.getVoteCapitaine().clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            GamePlayer gp = gm.getGamePlayer(player);
            if (gp != null) {
                gp.setDidVoteForCapitaine(false);
            }
            // Nettoyer les items pertinents
            gm.clearRelevantItems(player);
        }

        plugin.getTimerManager().advanceTimer();
    }

    public void finishVote() {
        gm.setVoteWasMade(true);
        gm.setVoteInProg(false);

        Player mostVoted = null;
        int mostVotedNum = 0;

        // Compter le nombre de votes valides (pour des joueurs)
        int numberOfValidVotes = 0;
        for (Map.Entry<Player, Integer> entry : gm.getVoteCount().entrySet()) {
            numberOfValidVotes += entry.getValue();
        }

        // Compter le nombre de personnes qui ont voté
        int numberOfVoters = 0;
        for (Player player : gm.getPlayersAlive()) {
            GamePlayer gp = gm.getGamePlayer(player);
            if (gp != null && gp.isDidVote()) {
                numberOfVoters++;
            }
        }

        // Les votes nuls = personnes qui ont voté - votes valides
        int numberOfNullVotes = numberOfVoters - numberOfValidVotes;

        // Trouver le joueur le plus voté
        for (Map.Entry<Player, Integer> entry : gm.getVoteCount().entrySet()) {
            int numOfVote = entry.getValue();
            if (numOfVote > mostVotedNum) {
                mostVoted = entry.getKey();
                mostVotedNum = numOfVote;
            }
        }
        
        // Nettoyer les items de tous les joueurs
        for (Player player : Bukkit.getOnlinePlayers()) {
            gm.clearRelevantItems(player);
        }

        // Si plus de votes nuls que de votes valides, le village ne s'est pas mis d'accord
        if (numberOfNullVotes > numberOfValidVotes) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("vote.day.no-agreement"));

            // Nettoyer les votes
            gm.getVoteCount().clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                gm.getGamePlayer(player).setDidVote(false);
            }
            
            // Nettoyer l'affichage des votes et les effets glow
            plugin.getVoteDisplayManager().reset();

            plugin.getTimerManager().advanceTimer();
            return;
        }

        if (mostVoted == null || mostVotedNum == 0) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("vote.day.no-elimination"));

            // Nettoyer les votes
            gm.getVoteCount().clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                gm.getGamePlayer(player).setDidVote(false);
            }
            
            // Nettoyer l'affichage des votes et les effets glow
            plugin.getVoteDisplayManager().reset();

            plugin.getTimerManager().advanceTimer();
            return;
        }

        // Vérifier s'il y a égalité
        List<Player> allPlayers = new ArrayList<>(gm.getPlayersAlive());
        allPlayers.remove(mostVoted);

        Player doubleMostVoted = null;
        int doubleMostVotedNum = 0;

        for (Player player : allPlayers) {
            int votes = gm.getVoteCount().getOrDefault(player, 0);
            if (votes > doubleMostVotedNum) {
                doubleMostVoted = player;
                doubleMostVotedNum = votes;
            }
        }


        // Nettoyer les votes
        gm.getVoteCount().clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            gm.getGamePlayer(player).setDidVote(false);
        }
        
        // Nettoyer l'affichage des votes et les effets glow
        plugin.getVoteDisplayManager().reset();

        // Si égalité et qu'il y a un capitaine, il départage
        if (doubleMostVotedNum > 0 && mostVotedNum == doubleMostVotedNum && gm.getCapitaine() != null) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("vote.tiebreaker.announce"));
            gm.setCapitaineTieBreakerInProg(true);
            gm.setMostVoted(mostVoted);
            gm.setDoubleMostVoted(doubleMostVoted);
            gm.actionQueueAdd("doCapitaineTieBreaker");
            doCapitaineTieBreaker(mostVoted, doubleMostVoted);
        } else {
            // Pas d'égalité, éliminer le plus voté
            // Vérifier si c'est l'Ange qui est éliminé au premier jour
            if (gm.checkAngeVictory(mostVoted)) {
                return; // L'Ange a gagné, le jeu est terminé
            }
            gm.killPlayer(mostVoted, "vote");
            
            // Si c'est le premier jour et l'Ange n'a pas été éliminé, il devient villageois
            gm.checkAngeTransformation();
            
            plugin.getTimerManager().advanceTimer();
        }
    }

    private void doCapitaineTieBreaker(Player player1, Player player2) {
        gm.setCapitaineTieBreakerInProg(true);

        for (Player player : gm.getPlayingPlayers()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("vote.tiebreaker.capitaine-decides"));
        }

        gm.getCapitaine().sendMessage(plugin.getLanguageManager().getMessage("vote.tiebreaker.capitaine-message"));
        gm.getCapitaine().sendMessage(plugin.getLanguageManager().getMessage("vote.tiebreaker.choice")
                                      .replace("{player1}", player1.getName())
                                      .replace("{player2}", player2.getName()));

        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("vote.tiebreaker.timer"), 60);

        gm.getCapitaine().sendMessage(plugin.getLanguageManager().getMessage("vote.tiebreaker.instruction"));

        // Nettoyer les items de tous les joueurs
        for (Player player : gm.getPlayingPlayers()) {
            gm.clearRelevantItems(player);
        }

        // Donner les crânes au capitaine
        ItemStack skull1 = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta1 = (org.bukkit.inventory.meta.SkullMeta) skull1.getItemMeta();
        if (meta1 != null) {
            meta1.setOwningPlayer(player1);
            meta1.setDisplayName(ChatColor.GOLD + player1.getName());
            skull1.setItemMeta(meta1);
        }

        ItemStack skull2 = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta2 = (org.bukkit.inventory.meta.SkullMeta) skull2.getItemMeta();
        if (meta2 != null) {
            meta2.setOwningPlayer(player2);
            meta2.setDisplayName(ChatColor.GOLD + player2.getName());
            skull2.setItemMeta(meta2);
        }

        gm.getCapitaine().getInventory().setItem(3, skull1);
        gm.getCapitaine().getInventory().setItem(5, skull2);
    }

    public void finishTieBreaker() {
        gm.setCapitaineTieBreakerInProg(false);
        
        // Nettoyer les têtes du capitaine
        if (gm.getCapitaine() != null) {
            gm.clearRelevantItems(gm.getCapitaine());
        }
        
        // Si c'est le premier jour et l'Ange n'a pas été éliminé, il devient villageois
        gm.checkAngeTransformation();
        
        plugin.getTimerManager().advanceTimer();
    }

    public void finishChasseur() {
        // Si le chasseur est déjà mort, ne rien faire
        if (gm.getChasseur() == null || !gm.getPlayersAlive().contains(gm.getChasseur())) {
            plugin.getTimerManager().advanceTimer();
            return;
        }

        if (gm.getChasseurTarget() == null) {
            gm.getChasseur().teleport(gm.getChasseurOldPos());
            gm.killPlayer(gm.getChasseur(), null);
            gm.lightCampfire(); // Rallumer le feu même si le chasseur n'a pas tiré
            plugin.getTimerManager().advanceTimer();
            return;
        }

        Player chasseur = gm.getChasseur();
        Player target = gm.getChasseurTarget();

        // make a trace of smoke from chasseur to target
        Location start = chasseur.getEyeLocation();
        Location end = target.getEyeLocation();
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);
        for (double d = 0; d < distance; d += 0.5) {
            Location particleLocation = start.clone().add(direction.clone().multiply(d));
            chasseur.getWorld().spawnParticle(
                    Particle.SMOKE,
                    particleLocation,
                    1,
                    0, 0, 0,
                    0.0
            );
        }

        // Create particle trace from chasseur to target
        createBulletTrace(chasseur.getEyeLocation(), target.getEyeLocation());

        // Play gun sound for all players
        for (Player player : gm.getPlayingPlayers()) {
            player.playSound(player.getLocation(), "custom:gun", 1.0f, 1.0f);
        }

        // Visual effects
        chasseur.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER,
            target.getLocation().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);

        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("actions.chasseur.shot")
            .replace("{chasseur}", chasseur.getName())
            .replace("{target}", target.getName()));

        gm.getChasseur().teleport(gm.getChasseurOldPos());
        gm.setInterceptChasseur(true);
        gm.clearRelevantItems(gm.getChasseur());
        gm.lightCampfire();

        // Vérifier que le chasseur et la cible sont toujours vivants
        if (!gm.getPlayersAlive().contains(gm.getChasseur()) || !gm.getPlayersAlive().contains(gm.getChasseurTarget())) {
            plugin.getTimerManager().advanceTimer();
            return;
        }

        // Vérifier si c'est un stalemate (chasseur et loup-garou sont les deux derniers) AVANT de tuer qui que ce soit
        boolean isStalemate = false;
        if (gm.getPlayersAlive().size() == 2 &&
            gm.getPlayersAlive().contains(chasseur) &&
            gm.getPlayersAlive().contains(target)) {

            // Vérifier si l'un est loup-garou et l'autre chasseur
            boolean chasseurIsGoodGuy = !gm.getLoupGarous().contains(chasseur);
            boolean targetIsLoupGarou = gm.getLoupGarous().contains(target);

            if (chasseurIsGoodGuy && targetIsLoupGarou) {
                isStalemate = true;
            }
        }

        // Si c'est un stalemate, annoncer l'égalité et réinitialiser le jeu SANS tuer les joueurs
        if (isStalemate) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.stalemate.broadcast-header"));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.stalemate.broadcast-title"));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.stalemate.broadcast-message"));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.stalemate.broadcast-no-winner"));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.stalemate.broadcast-footer"));

            for (Player player : gm.getPlayingPlayers()) {
                player.sendTitle(plugin.getLanguageManager().getMessage("victory.stalemate.title"),
                               plugin.getLanguageManager().getMessage("victory.stalemate.subtitle"), 10, 70, 20);
            }

            gm.gameReset(false);
            return;
        }

        // Pas de stalemate, continuer normalement
        // Fix bug : si le joueur ciblé est loup-garou, le retirer
        if (gm.getLoupGarous().contains(gm.getChasseurTarget())) {
            gm.getLoupGarous().remove(gm.getChasseurTarget());
            gm.decrementBadGuysCount();
        }

        gm.killPlayer(gm.getChasseur(), gm.getGamePlayer(gm.getChasseur()).getDeathReason());
        gm.killPlayer(gm.getChasseurTarget(), "chasseur");

        plugin.getTimerManager().advanceTimer();
    }

    /**
     * Creates a particle trace from start to end location
     */
    private void createBulletTrace(org.bukkit.Location start, org.bukkit.Location end) {
        org.bukkit.util.Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        double step = 0.3; // Distance between each particle
        for (double i = 0; i < distance; i += step) {
            org.bukkit.Location point = start.clone().add(direction.clone().multiply(i));
            point.getWorld().spawnParticle(org.bukkit.Particle.CRIT, point, 1, 0, 0, 0, 0);
            point.getWorld().spawnParticle(org.bukkit.Particle.FLAME, point, 1, 0, 0, 0, 0);
        }
    }

    public void finishCapitaineSuccessor() {
        // Vérifier si un successeur a été choisi manuellement
        if (!gm.isSuccessorChosen() && gm.getDyingCapitaine() != null) {
            // Aucun successeur n'a été choisi, choisir au hasard
            List<Player> availablePlayers = new ArrayList<>(gm.getPlayersAlive());
            availablePlayers.remove(gm.getDyingCapitaine());

            if (!availablePlayers.isEmpty()) {
                Player newCapitaine = availablePlayers.get((int)(Math.random() * availablePlayers.size()));
                gm.setCapitaine(newCapitaine);

                // Donner un casque bleu au nouveau capitaine
                newCapitaine.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("capitaine"));
                // Retirer le casque du capitaine mourant
                gm.getDyingCapitaine().getInventory().setHelmet(null);
                // Nettoyer les items du capitaine mourant
                gm.clearRelevantItems(gm.getDyingCapitaine());

                Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("succession.testament-random")
                        .replace("{dying}", gm.getDyingCapitaine().getName())
                        .replace("{new}", newCapitaine.getName()));
            }
        }

        // On tue maintenant le capitaine mourant

        if (gm.getDyingCapitaine() != null) {
            if (gm.getDyingCapitaine().equals(gm.getChasseur())) {
                gm.actionQueuePocketNext("doChasseur");
            } else {
                gm.killPlayer(gm.getDyingCapitaine(), gm.getGamePlayer(gm.getDyingCapitaine()).getDeathReason());
            }
        }

        plugin.getTimerManager().advanceTimer();
    }
    

}

