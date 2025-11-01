package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class GameManager {
    private final Lgmc plugin;
    private final RoleFinishers finishers;
    private boolean inGame;
    private boolean freezeAll;
    private int nightCount;
    private int dayCount;
    private String gameStep;
    private String queueMode;

    private final Map<UUID, GamePlayer> gamePlayers;
    private final List<Player> loupGarous;
    private final List<Player> villageois;
    private final List<Player> playersAlive;
    private final List<Player> playersDead;
    private final List<Player> playingPlayers;
    private final List<Player> goodGuys;
    private final List<Player> badGuys;
    private final List<Player> waitList;
    private final LinkedList<String> actionQueue;
    private final List<Player> lesAmoureux;

    private Player cupidon;
    private Player petiteFille;
    private Player chasseur;
    private Player sorciere;
    private Player voyante;
    private Player capitaine;

    private Player nextLGTarget;
    private Player chasseurTarget;
    private Location chasseurOldPos;
    private boolean voyanteSondage;
    private boolean potionVie;
    private boolean potionMort;
    private boolean interceptChasseur;
    private boolean interceptCapitaine;
    private boolean capitaineVoteInProg;
    private boolean capitaineTieBreakerInProg;
    private boolean capitaineSuccession;
    private boolean voteInProg;
    private boolean voteWasMade;
    private boolean chasseurDidLastKill;
    private boolean cupidonAction;
    private boolean sorciereAction;

    private Player dyingCapitaine;
    private final Map<Player, Integer> voteCount;
    private final Map<Player, Integer> voteCapitaine;
    private final Map<Player, Integer> designatedPlayers;
    private int designationCount;
    private int goodGuysCount;
    private int badGuysCount;

    private Player mostVoted;
    private int mostVotedNum;
    private Player doubleMostVoted;
    private int doubleMostVotedNum;
    private boolean isStarting;

    public GameManager(Lgmc plugin) {
        this.plugin = plugin;
        this.finishers = new RoleFinishers(plugin, this);
        this.gamePlayers = new HashMap<>();
        this.loupGarous = new ArrayList<>();
        this.villageois = new ArrayList<>();
        this.playersAlive = new ArrayList<>();
        this.playersDead = new ArrayList<>();
        this.playingPlayers = new ArrayList<>();
        this.goodGuys = new ArrayList<>();
        this.badGuys = new ArrayList<>();
        this.waitList = new ArrayList<>();
        this.actionQueue = new LinkedList<>();
        this.lesAmoureux = new ArrayList<>();
        this.voteCount = new HashMap<>();
        this.voteCapitaine = new HashMap<>();
        this.designatedPlayers = new HashMap<>();
    }

    public void gameReset(boolean isAdmin) {
        inGame = false;
        nightCount = 0;
        dayCount = 0;
        gameStep = null;
        queueMode = null;

        gamePlayers.clear();
        loupGarous.clear();
        villageois.clear();
        playersAlive.clear();
        playersDead.clear();
        playingPlayers.clear();
        goodGuys.clear();
        badGuys.clear();
        waitList.clear();
        actionQueue.clear();
        lesAmoureux.clear();
        voteCount.clear();
        voteCapitaine.clear();
        designatedPlayers.clear();

        cupidon = null;
        petiteFille = null;
        chasseur = null;
        sorciere = null;
        voyante = null;
        capitaine = null;
        nextLGTarget = null;
        chasseurTarget = null;
        chasseurOldPos = null;
        dyingCapitaine = null;
        mostVoted = null;
        doubleMostVoted = null;

        voyanteSondage = false;
        potionVie = false;
        potionMort = false;
        interceptChasseur = false;
        interceptCapitaine = false;
        capitaineVoteInProg = false;
        capitaineTieBreakerInProg = false;
        capitaineSuccession = false;
        voteInProg = false;
        voteWasMade = false;
        chasseurDidLastKill = false;

        designationCount = 0;
        goodGuysCount = 0;
        badGuysCount = 0;
        mostVotedNum = 0;
        doubleMostVotedNum = 0;

        unSeparatePlayers();

        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(6000);
        }

        plugin.getTimerManager().clearTimer();
        plugin.getWebsocketManager().sendDemuteAll();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            player.getInventory().setHelmet(null);
            player.removePotionEffect(PotionEffectType.BLINDNESS);

            for (Player other : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, other);
            }

            ItemStack slot4 = player.getInventory().getItem(4);
            if (slot4 != null && (slot4.getType() == Material.PAPER || slot4.getType() == Material.WOODEN_HOE)) {
                player.getInventory().setItem(4, new ItemStack(Material.AIR));
            }

            // If the player have any kind of inventory GUI open, close it
            player.closeInventory();
        }

        freezeAll = false;
        plugin.getWebsocketManager().sendReset();

        if (isAdmin) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(plugin.getLanguageManager().getMessage("general.game-reset"));
            }
        }
    }

    public void separatePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(plugin, other);
            }
        }
    }

    public void unSeparatePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, other);
            }
        }
    }

    public void actionQueueAdd(String action) {
        actionQueue.add(action);
    }

    public void actionQueuePocketNext(String action) {
        actionQueue.addFirst(action);
    }

    public void actionQueueRemove(String action) {
        actionQueue.remove(action);
    }

    public void actionQueueClear() {
        actionQueue.clear();
    }

    public boolean isActionQueueEmpty() {
        return actionQueue.isEmpty();
    }

    public void waitListAdd(Player player, String reason) {
        if (!waitList.contains(player)) {
            waitList.add(player);
            GamePlayer gp = getGamePlayer(player);
            if (gp != null) {
                gp.setDeathReason(reason != null ? reason : "default");
            }
        }
    }

    public void waitListRemove(Player player) {
        waitList.remove(player);
        GamePlayer gp = getGamePlayer(player);
        if (gp != null) {
            gp.setDeathReason(null);
        }
    }

    public void waitListKill() {
        if (waitList.isEmpty()) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("death.no-deaths"));
            return;
        }

        List<Player> toKill = new ArrayList<>(waitList);
        waitList.clear();

        for (Player player : toKill) {
            GamePlayer gp = getGamePlayer(player);
            String deathType = gp != null ? gp.getDeathReason() : "default";
            killPlayer(player, deathType);
        }
    }

    public void killPlayer(Player player, String deathType) {
        if (player == null) return;

        if (player.equals(capitaine) && !interceptCapitaine) {
            if (goodGuysCount > 1) {
                interceptCapitaine = true;
                GamePlayer gp = getGamePlayer(player);
                if (gp != null) {
                    gp.setDeathReason(deathType);
                }
                actionQueuePocketNext("doCapitaineSuccessor");

                if (actionQueue.size() == 1) {
                    if (!player.equals(chasseur)) {
                        actionQueueAdd("endDay");
                    } else {
                        actionQueueAdd("skip");
                    }
                }
                return;
            }
        }

        if (player.equals(chasseur) && !interceptChasseur) {
            interceptChasseur = true;
            GamePlayer gp = getGamePlayer(player);
            if (gp != null) {
                gp.setDeathReason(deathType);
            }
            actionQueuePocketNext("doChasseur");

            if (actionQueue.size() == 1) {
                actionQueueAdd("endDay");
            }
            return;
        }

        GamePlayer gp = getGamePlayer(player);
        Role playerRole = gp != null ? gp.getRole() : Role.NOT_IN_GAME;

        if (playerRole == Role.LOUP_GAROU) {
            badGuysCount--;
        } else {
            goodGuysCount--;
        }

        playersAlive.remove(player);
        playersDead.add(player);

        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        player.getInventory().setHelmet(plugin.getConfigManager().getRoleHelmetItemStack("mort"));

        // Cacher le joueur mort de tous les joueurs vivants
        for (Player alive : playersAlive) {
            alive.hidePlayer(plugin, player);
        }

        // Le joueur mort peut voir tous les autres joueurs morts
        for (Player dead : playersDead) {
            player.showPlayer(plugin, dead);
            dead.showPlayer(plugin, player);
        }

        player.getWorld().strikeLightningEffect(player.getLocation());

        String message = getDeathMessage(player, deathType, playerRole);
        if (message != null) {
            Bukkit.broadcastMessage(message);
        }

        if (gp != null) {
            gp.setRole(Role.DEAD);
        }

        if (lesAmoureux.contains(player)) {
            lesAmoureux.remove(player);
            if (!lesAmoureux.isEmpty()) {
                Player lover = lesAmoureux.get(0);
                killPlayer(lover, "love");
                lesAmoureux.clear();
            }
        }

        removePlayerFromRole(player);
        checkForWinCondition();
        plugin.getWebsocketManager().sendPlayerDied(player.getName());
    }

    private String getDeathMessage(Player player, String deathType, Role role) {
        String roleName = role.getFormattedName();
        String playerName = "" + ChatColor.GOLD + ChatColor.BOLD + player.getName();

        if (deathType == null || deathType.equals("default")) {
            return plugin.getLanguageManager().getMessage("death.default")
                .replace("{player}", playerName)
                .replace("{role}", roleName);
        } else if (deathType.equals("love")) {
            return plugin.getLanguageManager().getMessage("death.love")
                .replace("{player}", playerName)
                .replace("{role}", roleName);
        } else if (deathType.equals("loupGarou")) {
            return plugin.getLanguageManager().getMessage("death.werewolf")
                .replace("{player}", playerName)
                .replace("{role}", roleName);
        } else if (deathType.equals("sorciere")) {
            return plugin.getLanguageManager().getMessage("death.witch")
                .replace("{player}", playerName)
                .replace("{role}", roleName);
        } else if (deathType.equals("chasseur")) {
            return plugin.getLanguageManager().getMessage("death.hunter")
                .replace("{player}", playerName)
                .replace("{role}", roleName);
        } else if (deathType.equals("vote")) {
            return plugin.getLanguageManager().getMessage("death.vote")
                .replace("{player}", playerName)
                .replace("{role}", roleName);
        } else if (deathType.equals("silent")) {
            return null;
        }
        return null;
    }

    public void removePlayerFromRole(Player player) {
        if (player.equals(cupidon)) cupidon = null;
        if (player.equals(voyante)) voyante = null;
        if (player.equals(sorciere)) sorciere = null;
        if (player.equals(chasseur) && goodGuysCount != 0) chasseur = null;
        if (player.equals(petiteFille)) petiteFille = null;
        loupGarous.remove(player);
        villageois.remove(player);
    }

    public void checkForWinCondition() {
        if (!inGame) return;

        // Special case: Chasseur killed everyone in a tie
        if (chasseurDidLastKill) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle(plugin.getLanguageManager().getMessage("victory.stalemate.title"),
                                plugin.getLanguageManager().getMessage("victory.stalemate.subtitle"), 10, 70, 20);
            }
            plugin.getWebsocketManager().sendGameOver();
            gameReset(false);
            return;
        }

        int numLG = loupGarous.size();
        int numVillage = goodGuysCount;

        // Check for lovers win condition (both lovers alive and everyone else dead)
        if (checkLoversWinCondition()) {
            return;
        }

        // Village wins: All werewolves are dead
        if (numLG == 0) {
            announceVillageVictory();
            gameReset(false);
            return;
        }

        // Werewolves win: Werewolves >= Village OR only werewolves left
        if (numLG >= numVillage || playersAlive.size() == numLG) {
            // Exception: If chasseur is still alive and hasn't been intercepted, wait for his shot
            if (chasseur != null && !interceptChasseur && playersAlive.contains(chasseur)) {
                return;
            }

            announceWerewolvesVictory();
            gameReset(false);
            return;
        }

        // Edge case: Only one player alive (shouldn't happen but handle it)
        if (playersAlive.size() == 1) {
            Player lastPlayer = playersAlive.get(0);
            GamePlayer gp = getGamePlayer(lastPlayer);
            if (gp.getRole().getTeam() == Role.Team.BAD_GUYS) {
                announceWerewolvesVictory();
            } else {
                announceVillageVictory();
            }
            gameReset(false);
        }
    }

    /**
     * Check if lovers should win (both alive and everyone else dead)
     * @return true if lovers won, false otherwise
     */
    private boolean checkLoversWinCondition() {
        if (lesAmoureux.size() != 2) {
            return false;
        }

        Player lover1 = lesAmoureux.get(0);
        Player lover2 = lesAmoureux.get(1);

        // Both lovers must be alive
        if (!playersAlive.contains(lover1) || !playersAlive.contains(lover2)) {
            return false;
        }

        // Only the two lovers should be alive
        if (playersAlive.size() == 2) {
            for (Player player : playingPlayers) {
                if (lesAmoureux.contains(player)) {
                    player.sendTitle(plugin.getLanguageManager().getMessage("victory.lovers.title"),
                                    plugin.getLanguageManager().getMessage("victory.lovers.subtitle"),
                                    10, 70, 20);
                } else {
                    player.sendTitle(plugin.getLanguageManager().getMessage("victory.defeat.title"),
                                    plugin.getLanguageManager().getMessage("victory.lovers.subtitle"),
                                    10, 70, 20);
                }
            }

            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.lovers.broadcast-header"));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.lovers.broadcast-title"));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.lovers.broadcast-players")
                                   .replace("{lover1}", lover1.getName())
                                   .replace("{lover2}", lover2.getName()));
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.lovers.broadcast-footer"));

            gameReset(false);
            return true;
        }

        return false;
    }

    /**
     * Announce village victory with detailed statistics
     */
    private void announceVillageVictory() {
        for (Player player : goodGuys) {
            if (playingPlayers.contains(player)) {
                player.sendTitle(plugin.getLanguageManager().getMessage("victory.village.title"),
                                plugin.getLanguageManager().getMessage("victory.village.subtitle"),
                                10, 70, 20);
            }
        }
        for (Player player : badGuys) {
            if (playingPlayers.contains(player)) {
                player.sendTitle(plugin.getLanguageManager().getMessage("victory.defeat.title"),
                                plugin.getLanguageManager().getMessage("victory.village.subtitle"),
                                10, 70, 20);
            }
        }

        // Broadcast detailed victory message
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.village.broadcast-header"));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.village.broadcast-title"));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.village.broadcast-all-dead"));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.village.broadcast-stats")
                               .replace("{alive}", String.valueOf(playersAlive.size()))
                               .replace("{total}", String.valueOf(playingPlayers.size())));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.village.broadcast-duration")
                               .replace("{nights}", String.valueOf(nightCount))
                               .replace("{days}", String.valueOf(dayCount)));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.village.broadcast-footer"));

        plugin.getWebsocketManager().sendGameOver();
    }

    /**
     * Announce werewolves victory with detailed statistics
     */
    private void announceWerewolvesVictory() {
        for (Player player : goodGuys) {
            if (playingPlayers.contains(player)) {
                player.sendTitle(plugin.getLanguageManager().getMessage("victory.defeat.title"),
                                plugin.getLanguageManager().getMessage("victory.werewolves.subtitle"),
                                10, 70, 20);
            }
        }
        for (Player player : badGuys) {
            if (playingPlayers.contains(player)) {
                player.sendTitle(plugin.getLanguageManager().getMessage("victory.werewolves.title"),
                                plugin.getLanguageManager().getMessage("victory.werewolves.subtitle"),
                                10, 70, 20);
            }
        }

        // Broadcast detailed victory message
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-header"));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-title"));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-domination"));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-wolves")
                               .replace("{wolves}", String.valueOf(loupGarous.size())));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-villagers")
                               .replace("{villagers}", String.valueOf(goodGuysCount)));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-duration")
                               .replace("{nights}", String.valueOf(nightCount))
                               .replace("{days}", String.valueOf(dayCount)));
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("victory.werewolves.broadcast-footer"));
        plugin.getWebsocketManager().sendGameOver();
    }

    public void startGame() {
        gameReset(false);
        isStarting = true;

        // Countdown using scheduler instead of Thread.sleep
        final int[] countdown = {10};

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (countdown[0] > 0) {
                final int count = countdown[0];
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(plugin.getLanguageManager().getMessage("general.game-starting")
                                       .replace("{time}", String.valueOf(count)));
                }
                countdown[0]--;
            } else {
                task.cancel();

                inGame = true;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(plugin.getLanguageManager().getMessage("general.game-started"));
                }

                roleDistribution();
                plugin.getWebsocketManager().sendGameStart();
                isStarting = false;
            }
        }, 0L, 20L); // 0 tick delay, 20 ticks (1 second) interval
    }

    public void roleDistribution() {
        inGame = true;

        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int playerNumber = allPlayers.size();

        if (playerNumber < 4) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("general.not-enough-players"));
            return;
        }

        if (playerNumber > 12) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("general.too-many-players"));
            return;
        }

        // Téléporter les joueurs aux spawns
        Map<Integer, Location> spawns = plugin.getLocationManager().getSpawnLocations();
        if (!spawns.isEmpty()) {
            int spawnIndex = 1;
            for (Player player : allPlayers) {
                Location spawnLoc = spawns.get(spawnIndex);
                if (spawnLoc != null) {
                    player.teleport(spawnLoc);
                    spawnIndex++;
                    // Réinitialiser l'index si on dépasse le nombre de spawns
                    if (spawnIndex > spawns.size()) {
                        spawnIndex = 1;
                    }
                } else {
                    // Si pas de spawn à cet index, utiliser le feu de camp
                    Location campfire = plugin.getLocationManager().getCampfireLocation();
                    if (campfire != null) {
                        player.teleport(campfire);
                    }
                }
            }
        } else {
            // Si aucun spawn n'est configuré, téléporter au feu de camp
            Location campfire = plugin.getLocationManager().getCampfireLocation();
            if (campfire != null) {
                for (Player player : allPlayers) {
                    player.teleport(campfire);
                }
            }
        }

        // Distribution du premier loup-garou (obligatoire)
        Player loupGarouOne = allPlayers.get((int)(Math.random() * allPlayers.size()));
        allPlayers.remove(loupGarouOne);
        loupGarous.add(loupGarouOne);

        // Distribution du deuxième loup-garou si 9+ joueurs
        if (playerNumber >= 9 && !allPlayers.isEmpty()) {
            Player loupGarouTwo = allPlayers.get((int)(Math.random() * allPlayers.size()));
            allPlayers.remove(loupGarouTwo);
            loupGarous.add(loupGarouTwo);
        }

        // Distribution de Cupidon si 9+ joueurs
        if (playerNumber >= 9 && !allPlayers.isEmpty()) {
            cupidon = allPlayers.get((int)(Math.random() * allPlayers.size()));
            allPlayers.remove(cupidon);
        }

        // Distribution de la Petite Fille
        if (!allPlayers.isEmpty()) {
            petiteFille = allPlayers.get((int)(Math.random() * allPlayers.size()));
            allPlayers.remove(petiteFille);
        }

        // Distribution du Chasseur
        if (!allPlayers.isEmpty()) {
            chasseur = allPlayers.get((int)(Math.random() * allPlayers.size()));
            allPlayers.remove(chasseur);
        }

        // Distribution de la Sorcière
        if (!allPlayers.isEmpty()) {
            sorciere = allPlayers.get((int)(Math.random() * allPlayers.size()));
            allPlayers.remove(sorciere);
        }

        // Distribution de la Voyante
        if (!allPlayers.isEmpty()) {
            voyante = allPlayers.get((int)(Math.random() * allPlayers.size()));
            allPlayers.remove(voyante);
        }

        // Les joueurs restants sont des villageois
        villageois.addAll(allPlayers);

        // Assignation des rôles et envoi des titres
        for (Player lg : loupGarous) {
            GamePlayer gp = getGamePlayer(lg);
            gp.setRole(Role.LOUP_GAROU);
            lg.sendTitle(plugin.getLanguageManager().getMessage("roles.loup-garou.title"),
                        plugin.getLanguageManager().getMessage("roles.loup-garou.subtitle"),
                        10, 70, 20);
            lg.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("loup-garou"));
        }

        for (Player vil : villageois) {
            GamePlayer gp = getGamePlayer(vil);
            gp.setRole(Role.VILLAGEOIS);
            vil.sendTitle(plugin.getLanguageManager().getMessage("roles.villageois.title"),
                         plugin.getLanguageManager().getMessage("roles.villageois.subtitle"),
                         10, 70, 20);
            vil.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("villageois"));
        }

        if (petiteFille != null) {
            GamePlayer gp = getGamePlayer(petiteFille);
            gp.setRole(Role.PETITE_FILLE);
            petiteFille.sendTitle(plugin.getLanguageManager().getMessage("roles.petite-fille.title"),
                                 plugin.getLanguageManager().getMessage("roles.petite-fille.subtitle"),
                                 10, 70, 20);
            petiteFille.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("petite-fille"));
        }

        if (cupidon != null) {
            GamePlayer gp = getGamePlayer(cupidon);
            gp.setRole(Role.CUPIDON);
            cupidon.sendTitle(plugin.getLanguageManager().getMessage("roles.cupidon.title"),
                             plugin.getLanguageManager().getMessage("roles.cupidon.subtitle"),
                             10, 70, 20);
            cupidon.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("cupidon"));
        }

        if (chasseur != null) {
            GamePlayer gp = getGamePlayer(chasseur);
            gp.setRole(Role.CHASSEUR);
            chasseur.sendTitle(plugin.getLanguageManager().getMessage("roles.chasseur.title"),
                              plugin.getLanguageManager().getMessage("roles.chasseur.subtitle"),
                              10, 70, 20);
            chasseur.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("chasseur"));
        }

        if (sorciere != null) {
            GamePlayer gp = getGamePlayer(sorciere);
            gp.setRole(Role.SORCIERE);
            sorciere.sendTitle(plugin.getLanguageManager().getMessage("roles.sorciere.title"),
                              plugin.getLanguageManager().getMessage("roles.sorciere.subtitle"),
                              10, 70, 20);
            sorciere.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("sorciere"));
        }

        if (voyante != null) {
            GamePlayer gp = getGamePlayer(voyante);
            gp.setRole(Role.VOYANTE);
            voyante.sendTitle(plugin.getLanguageManager().getMessage("roles.voyante.title"),
                             plugin.getLanguageManager().getMessage("roles.voyante.subtitle"),
                             10, 70, 20);
            voyante.getInventory().setItemInOffHand(plugin.getConfigManager().getRoleHelmetItemStack("voyante"));
        }

        // Initialisation des listes
        playersAlive.addAll(Bukkit.getOnlinePlayers());
        playingPlayers.addAll(Bukkit.getOnlinePlayers());
        goodGuys.addAll(Bukkit.getOnlinePlayers());

        // Calcul des équipes
        for (Player lg : loupGarous) {
            goodGuys.remove(lg);
            badGuys.add(lg);
        }

        goodGuysCount = goodGuys.size();
        badGuysCount = badGuys.size();

        // Clear chat
        for (int i = 0; i < 100; i++) {
            Bukkit.broadcastMessage(" ");
        }

        // Tous les joueurs en mode aventure
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }

        freezeAll = true;
        nightAction();
    }

    public void nightAction() {
        nextLGTarget = null;
        designationCount = 0;
        designatedPlayers.clear();
        voyanteSondage = false;

        nightCount++;
        queueMode = "night";

        // Muting all players during the night
        plugin.getWebsocketManager().sendMuteAll();

        // Hide all players from each other and apply blindness
        for (Player player : playersAlive) {
            for (Player other : playersAlive) {
                player.hidePlayer(plugin, other);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
        }

        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("phases.night")
                               .replace("{count}", String.valueOf(nightCount)));

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("phases.night-falls"));
        }

        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(18000);
        }

        // Éteindre le feu de camp la nuit
        unlightCampfire();

        // Build action queue for night
        actionQueueClear();
        if (cupidon != null && nightCount == 1) {
            actionQueueAdd("doCupidon");
        }
        if (voyante != null) {
            actionQueueAdd("doVoyante");
        }
        actionQueueAdd("doLoupGarou");
        if (sorciere != null) {
            actionQueueAdd("doSorciere");
        }
        actionQueueAdd("endNight");

        // Start the night sequence after a delay
        Bukkit.getScheduler().runTaskLater(plugin, this::nextStep, 60L);

        // Clear every player's slot 4 inventory
        for (Player player : playersAlive) {
            ItemStack slot4 = player.getInventory().getItem(4);
            if (slot4 != null && (
                    slot4.getType() == Material.PAPER ||
                    slot4.getType() == Material.WOODEN_HOE ||
                    slot4.getType() == Material.IRON_HOE)) {
                player.getInventory().setItem(4, new ItemStack(Material.AIR));
            }
        }
    }

    public void dayAction() {
        dayCount++;
        voteWasMade = false;
        queueMode = "day";

        // Unmuting all players during the day
        plugin.getWebsocketManager().sendDemuteAll();

        // Reveal all players and remove blindness
        for (Player player : playersAlive) {
            for (Player other : playersAlive) {
                player.showPlayer(plugin, other);
            }
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }

        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("phases.day")
                               .replace("{count}", String.valueOf(dayCount)));

        for (Player player : playingPlayers) {
            GamePlayer gp = getGamePlayer(player);
            gp.reset();
            player.sendMessage(plugin.getLanguageManager().getMessage("phases.day-breaks"));
        }

        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(6000);
        }

        // Allumer le feu de camp le jour
        lightCampfire();

        // Build action queue for day
        actionQueueClear();
        actionQueueAdd("revealDeath");
        if (capitaine == null) {
            actionQueueAdd("doCapitaine");
        }
        actionQueueAdd("doVote");
        actionQueueAdd("endDay");

        // Clear every player's slot 4 inventory
        for (Player player : playersAlive) {
            ItemStack slot4 = player.getInventory().getItem(4);
            if (slot4 != null && (
                    slot4.getType() == Material.PAPER ||
                            slot4.getType() == Material.WOODEN_HOE ||
                            slot4.getType() == Material.IRON_HOE)) {
                player.getInventory().setItem(4, new ItemStack(Material.AIR));
            }
        }

        // Start the day sequence after a delay
        Bukkit.getScheduler().runTaskLater(plugin, this::nextStep, 60L);
    }

    public void nextStep() {
        if (!inGame) return;

        // Finir l'action actuelle avant de passer à la suivante
        if (gameStep != null) {
            executeFinisher(gameStep);
        }

        if (isActionQueueEmpty()) {
            if ("night".equals(queueMode)) {
                dayAction();
            } else if ("day".equals(queueMode)) {
                nightAction();
            }
            return;
        }

        // Get next action
        String nextAction = actionQueue.poll();
        if (nextAction == null) return;

        gameStep = nextAction;

        // Execute action based on type
        switch (nextAction) {
            case "revealDeath":
                waitListKill();
                Bukkit.getScheduler().runTaskLater(plugin, this::nextStep, 40L);
                break;
            case "doCapitaine":
                doCapitaine();
                break;
            case "doVote":
                doVote();
                break;
            case "doVoyante":
                doVoyante();
                break;
            case "doLoupGarou":
                doLoupGarou();
                break;
            case "doSorciere":
                doSorciere();
                break;
            case "doChasseur":
                doChasseur();
                break;
            case "doCupidon":
                doCupidon();
                break;
            case "doCapitaineSuccessor":
                doCapitaineSuccessor();
                break;
            case "doCapitaineTieBreaker":
                // Le finisher sera appelé quand le capitaine choisit
                break;
            case "skip":
                Bukkit.getScheduler().runTaskLater(plugin, this::nextStep, 5L);
                break;
            case "endNight":
                for (Player player : playersAlive) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
                }
                Bukkit.getScheduler().runTaskLater(plugin, this::nextStep, 100L);
                break;
            case "endDay":
                Bukkit.getScheduler().runTaskLater(plugin, this::nextStep, 100L);
                break;
            default:
                nextStep();
                break;
        }
    }

    private void executeFinisher(String action) {
        switch (action) {
            case "doVoyante":
                finishers.finishVoyante();
                break;
            case "doLoupGarou":
                finishers.finishLoupGarou();
                break;
            case "doSorciere":
                finishers.finishSorciere();
                break;
            case "doCapitaine":
                finishers.finishCapitaine();
                break;
            case "doVote":
                finishers.finishVote();
                break;
            case "doChasseur":
                finishers.finishChasseur();
                break;
            case "doCapitaineSuccessor":
                finishers.finishCapitaineSuccessor();
                break;
            case "doCapitaineTieBreaker":
                finishers.finishTieBreaker();
                break;
        }
    }

    // Placeholder methods for role actions
    private void doCupidon() {
        if (cupidon == null || !playersAlive.contains(cupidon)) {
            nextStep();
            return;
        }

        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("actions.cupidon.announce"));
        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("actions.cupidon.timer"), 60);

        cupidonAction = false;
        new fr.lightshoro.lgmc.gui.CupidonGUI(plugin).open(cupidon);
    }

    private void doVoyante() {
        if (voyante == null || !playersAlive.contains(voyante)) {
            nextStep();
            return;
        }

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("actions.voyante.announce"));
        }

        voyante.removePotionEffect(PotionEffectType.BLINDNESS);
        for (Player player : playersAlive) {
            voyante.showPlayer(plugin, player);
        }

        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("actions.voyante.timer"), 30);

        voyanteSondage = false;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            new fr.lightshoro.lgmc.gui.VoyanteGUI(plugin).open(voyante);
        }, 20L);
    }

    private void doLoupGarou() {
        nextLGTarget = null;

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("actions.loups-garous.announce"));
        }

        int numLG = loupGarous.size();
        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("actions.loups-garous.timer"), 30 * numLG);

        for (Player lg : loupGarous) {
            lg.removePotionEffect(PotionEffectType.BLINDNESS);
            for (Player player : playersAlive) {
                lg.showPlayer(plugin, player);
            }
        }

        if (petiteFille != null && playersAlive.contains(petiteFille)) {
            petiteFille.removePotionEffect(PotionEffectType.BLINDNESS);
            for (Player player : playersAlive) {
                petiteFille.showPlayer(plugin, player);
            }
        }

        plugin.getChatManager().setLoupGarouChatActive(true);

        // Ouvrir le GUI pour chaque loup-garou après un délai
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player lg : loupGarous) {
                // new fr.lightshoro.lgmc.gui.LoupGarouGUI(plugin).open(lg);
                // Give loup-garou a iron hoe to open GUI
                lg.getInventory().setItem(4, new ItemStack(Material.IRON_HOE));
            }
        }, 20L);
    }

    private void doSorciere() {
        if (sorciere == null || !playersAlive.contains(sorciere)) {
            nextStep();
            return;
        }

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("actions.sorciere.announce"));
        }

        sorciere.removePotionEffect(PotionEffectType.BLINDNESS);
        for (Player player : playersAlive) {
            sorciere.showPlayer(plugin, player);
        }

        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("actions.sorciere.timer"), 30);

        sorciereAction = false;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            new fr.lightshoro.lgmc.gui.SorciereGUI(plugin).open(sorciere);
        }, 20L);
    }

    private void doCapitaine() {
        capitaineVoteInProg = true;

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("vote.capitaine.announce"));
        }

        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("vote.capitaine.timer"), 60);

        for (Player player : playersAlive) {
            player.getInventory().setItem(4, new ItemStack(Material.PAPER));
            ItemStack paper = player.getInventory().getItem(4);
            if (paper != null) {
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.vote-paper"));
                    paper.setItemMeta(meta);
                }
            }
        }
        // TODO: Implement GUI for Capitaine vote
    }

    private void doVote() {
        voteWasMade = false;
        voteInProg = true;

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("vote.day.announce"));
        }

        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("vote.day.timer"), 300);

        for (Player player : playersAlive) {
            player.getInventory().setItem(4, new ItemStack(Material.PAPER));
            ItemStack paper = player.getInventory().getItem(4);
            if (paper != null) {
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(plugin.getLanguageManager().getMessage("gui.items.vote-day"));
                    paper.setItemMeta(meta);
                }
            }
        }
        // TODO: Implement GUI for Vote
    }

    private void doChasseur() {
        interceptChasseur = true;

        for (Player player : playingPlayers) {
            player.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.announce")
                             .replace("{player}", chasseur.getName()));
        }

        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("actions.chasseur.timer"), 30);

        chasseur.sendMessage(plugin.getLanguageManager().getMessage("actions.chasseur.instruction"));
        chasseur.getInventory().setItem(4, new ItemStack(Material.WOODEN_HOE));
        chasseurOldPos = chasseur.getLocation();
        chasseurTarget = null;

        // Teleport chasseur to chasseur-tp location
        Location chasseurTpLoc = plugin.getLocationManager().getChasseurTpLocation();
        if (chasseurTpLoc != null) {
            chasseur.teleport(chasseurTpLoc);
        }
    }

    private void doCapitaineSuccessor() {
        if (capitaine == null) {
            nextStep();
            return;
        }

        capitaine.sendMessage(plugin.getLanguageManager().getMessage("succession.dying-message"));
        capitaine.sendMessage(plugin.getLanguageManager().getMessage("succession.instruction"));

        capitaineSuccession = true;
        plugin.getTimerManager().defineTimer(plugin.getLanguageManager().getMessage("succession.timer"), 30);
    }

    public GamePlayer getGamePlayer(Player player) {
        return gamePlayers.computeIfAbsent(player.getUniqueId(), k -> new GamePlayer(player));
    }

    public GamePlayer createGamePlayer(Player player, Role role) {
        GamePlayer gamePlayer = new GamePlayer(player);
        gamePlayer.setRole(role);
        gamePlayers.put(player.getUniqueId(), gamePlayer);
        return gamePlayer;
    }

    /**
     * Allume le feu de camp (pendant la journée)
     */
    private void lightCampfire() {
        Location campfire = plugin.getLocationManager().getCampfireLocation();
        if (campfire != null) {
            Block block = campfire.getBlock();
            if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
                if (block.getBlockData() instanceof Lightable) {
                    Lightable lightable = (Lightable) block.getBlockData();
                    lightable.setLit(true);
                    block.setBlockData(lightable);
                }
            }
        }
    }

    /**
     * Éteint le feu de camp (pendant la nuit)
     */
    private void unlightCampfire() {
        Location campfire = plugin.getLocationManager().getCampfireLocation();
        if (campfire != null) {
            Block block = campfire.getBlock();
            if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
                if (block.getBlockData() instanceof Lightable) {
                    Lightable lightable = (Lightable) block.getBlockData();
                    lightable.setLit(false);
                    block.setBlockData(lightable);
                }
            }
        }
    }

    // Getters and setters
    public boolean isInGame() { return inGame; }
    public void setInGame(boolean inGame) { this.inGame = inGame; }
    public boolean isFreezeAll() { return freezeAll; }
    public void setFreezeAll(boolean freezeAll) { this.freezeAll = freezeAll; }
    public String getGameStep() { return gameStep; }
    public void setGameStep(String gameStep) { this.gameStep = gameStep; }
    public Player getCapitaine() { return capitaine; }
    public void setCapitaine(Player capitaine) { this.capitaine = capitaine; }
    public void setDyingCapitaine(Player player) { this.dyingCapitaine = player; }
    public Player getDyingCapitaine() { return dyingCapitaine; }
    public Player getChasseur() { return chasseur; }
    public Player getVoyante() { return voyante; }
    public Player getSorciere() { return sorciere; }
    public List<Player> getLoupGarous() { return loupGarous; }
    public List<Player> getPlayersAlive() { return playersAlive; }
    public boolean isCapitaineVoteInProg() { return capitaineVoteInProg; }
    public void setCapitaineVoteInProg(boolean val) { this.capitaineVoteInProg = val; }
    public boolean isVoteInProg() { return voteInProg; }
    public void setVoteInProg(boolean val) { this.voteInProg = val; }
    public boolean isCapitaineSuccession() { return capitaineSuccession; }
    public void setCapitaineSuccession(boolean val) { this.capitaineSuccession = val; }
    public Map<Player, Integer> getVoteCount() { return voteCount; }
    public Map<Player, Integer> getVoteCapitaine() { return voteCapitaine; }
    public Lgmc getPlugin() { return plugin; }

    // Méthodes pour les GUIs
    public void setVoyanteSondage(boolean val) { this.voyanteSondage = val; }
    public boolean isVoyanteSondage() { return voyanteSondage; }

    public void addDesignatedPlayer(Player player) {
        if (!designatedPlayers.containsKey(player)) {
            designatedPlayers.put(player, 0);
        }
    }

    public void incrementDesignationCount() {
        this.designationCount++;
    }

    public int getDesignationCount() {
        return designationCount;
    }

    public void incrementPlayerDesignation(Player player) {
        designatedPlayers.put(player, designatedPlayers.getOrDefault(player, 0) + 1);
    }

    public Player getNextLGTarget() {
        return nextLGTarget;
    }

    public void setNextLGTarget(Player player) {
        this.nextLGTarget = player;
    }

    public boolean isPotionVie() {
        return potionVie;
    }

    public void setPotionVie(boolean val) {
        this.potionVie = val;
    }

    public boolean isPotionMort() {
        return potionMort;
    }

    public void setPotionMort(boolean val) {
        this.potionMort = val;
    }

    public boolean isSorciereAction() {
        return sorciereAction;
    }

    public void setSorciereAction(boolean val) {
        this.sorciereAction = val;
    }

    public void incrementCapitaineVote(Player player) {
        voteCapitaine.put(player, voteCapitaine.getOrDefault(player, 0) + 1);
    }

    public void incrementVoteCount(Player target, Player voter) {
        int voteValue = (voter.equals(capitaine)) ? 2 : 1;
        voteCount.put(target, voteCount.getOrDefault(target, 0) + voteValue);
    }

    public boolean isCupidonAction() {
        return cupidonAction;
    }

    public void setCupidonAction(boolean val) {
        this.cupidonAction = val;
    }

    public void addLovers(Player lover1, Player lover2) {
        lesAmoureux.clear();
        lesAmoureux.add(lover1);
        lesAmoureux.add(lover2);
    }

    // Getters supplémentaires pour RoleFinishers
    public RoleFinishers getFinishers() { return finishers; }
    public Player getPetiteFille() { return petiteFille; }
    public List<Player> getPlayingPlayers() { return playingPlayers; }
    public Map<Player, Integer> getDesignatedPlayers() { return designatedPlayers; }
    public Player getChasseurTarget() { return chasseurTarget; }
    public void setChasseurTarget(Player target) { this.chasseurTarget = target; }
    public Location getChasseurOldPos() { return chasseurOldPos; }
    public void setChasseurOldPos(Location loc) { this.chasseurOldPos = loc; }
    public void setInterceptChasseur(boolean val) { this.interceptChasseur = val; }
    public void decrementBadGuysCount() { this.badGuysCount--; }
    public void setVoteWasMade(boolean val) { this.voteWasMade = val; }
    public boolean isCapitaineTieBreakerInProg() { return capitaineTieBreakerInProg; }
    public void setCapitaineTieBreakerInProg(boolean val) { this.capitaineTieBreakerInProg = val; }
    public void setMostVoted(Player player) { this.mostVoted = player; }
    public void setDoubleMostVoted(Player player) { this.doubleMostVoted = player; }
    public Player getMostVoted() { return mostVoted; }
    public Player getDoubleMostVoted() { return doubleMostVoted; }
    public boolean getIsStarting() { return isStarting; }
}

