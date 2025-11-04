package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import fr.lightshoro.lgmc.models.GamePlayer;
import fr.lightshoro.lgmc.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager {
    private final Lgmc plugin;
    private final Map<Player, Scoreboard> playerScoreboards;
    private LanguageManager lm;
    
    // Memory system to track role assignments even after death
    private final Map<String, Player> roleMemory; // role -> player
    private final Map<Player, Boolean> playerDeathStatus; // player -> isDead

    public ScoreboardManager(Lgmc plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        this.lm = plugin.getLanguageManager();
        this.roleMemory = new HashMap<>();
        this.playerDeathStatus = new HashMap<>();
    }

    /**
     * Crée ou met à jour le scoreboard pour tous les joueurs
     */
    public void updateScoreboards() {
        GameManager gm = plugin.getGameManager();

        if (!gm.isInGame()) {
            // Si pas en jeu, retirer tous les scoreboards
            clearScoreboards();
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player);
        }
    }

    /**
     * Met à jour le scoreboard pour un joueur spécifique
     */
    public void updatePlayerScoreboard(Player player) {
        GameManager gm = plugin.getGameManager();

        if (!gm.isInGame()) {
            removeScoreboard(player);
            return;
        }

        Scoreboard scoreboard = playerScoreboards.computeIfAbsent(player, p ->
            Bukkit.getScoreboardManager().getNewScoreboard()
        );

        Objective objective = scoreboard.getObjective("lgmc");
        if (objective != null) {
            objective.unregister();
        }

        String lgRoleName = lm.getRoleName("loup-garou", true, false);
        String villageoisRoleName = lm.getRoleName("villageois", false, false);
        String voyanteRoleName = lm.getRoleName("voyante", false, false);
        String sorciereRoleName = lm.getRoleName("sorciere", false, false);
        String chasseurRoleName = lm.getRoleName("chasseur", false, false);
        String petiteFilleRoleName = lm.getRoleName("petite-fille", false, false);
        String capitaineRoleName = lm.getRoleName("capitaine", false, false);

        String scoreboardTitle = lm.getMessage("general.scoreboard-title");



        objective = scoreboard.registerNewObjective("lgmc", "dummy",
            ChatColor.GOLD + "❖ " + ChatColor.YELLOW + ChatColor.BOLD + scoreboardTitle + ChatColor.GOLD + " ❖");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 15;

        // Afficher le compteur de nuit/jour
        if (gm.isNight()) {
            String nightText = lm.getMessage("scoreboard.nights").replace("{count}", String.valueOf(gm.getNightCount()));
            setScore(objective, nightText, line--);
        } else {
            String dayText = lm.getMessage("scoreboard.days").replace("{count}", String.valueOf(gm.getDayCount()));
            setScore(objective, dayText, line--);
        }

        // Ligne vide pour l'esthétique
        setScore(objective, ChatColor.RESET.toString(), line--);

        // Compter les loups-garous vivants
        List<Player> loupGarous = gm.getLoupGarous();
        List<Player> playersAlive = gm.getPlayersAlive();
        long lgAlive = loupGarous.stream().filter(playersAlive::contains).count();

        // Afficher les loups-garous avec leur nombre
        setScore(objective, ChatColor.RED + "" + ChatColor.BOLD + lgRoleName + ChatColor.DARK_RED + " ☽", line--);
        setScore(objective, ChatColor.WHITE + lm.getMessage("scoreboard.alive") + ": " + ChatColor.RED + lgAlive, line--);

        // Ligne vide
        setScore(objective, " ", line--);

        // Afficher les rôles spéciaux s'ils sont en vie
        setScore(objective, ChatColor.GREEN + "━━━ " + ChatColor.BOLD + villageoisRoleName +
                ChatColor.GREEN + " ━━━", line--);

        // Villageois
        List<Player> villageois = gm.getVillageois();
        long villageoisAlive = villageois.stream().filter(playersAlive::contains).count();
        // Stylise comme les autres rôles
        setScore(objective, ChatColor.WHITE + "  ◈ " + villageoisRoleName + ": " +
                ChatColor.YELLOW + villageoisAlive, line--);

        // Voyante
        Player voyante = gm.getVoyante();
        if (voyante == null && roleMemory.containsKey("voyante")) {
            voyante = roleMemory.get("voyante");
        }
        if (voyante != null) {
            boolean isAlive = playersAlive.contains(voyante);
            String status = isAlive ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            setScore(objective, ChatColor.GREEN + "  ◈ " + voyanteRoleName + ": " + status, line--);
        }

        // Sorcière
        Player sorciere = gm.getSorciere();
        if (sorciere == null && roleMemory.containsKey("sorciere")) {
            sorciere = roleMemory.get("sorciere");
        }
        if (sorciere != null) {
            boolean isAlive = playersAlive.contains(sorciere);
            String status = isAlive ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            setScore(objective, ChatColor.GREEN + "  ◈ " + sorciereRoleName + ": " + status, line--);
        }

        // Chasseur
        Player chasseur = gm.getChasseur();
        if (chasseur == null && roleMemory.containsKey("chasseur")) {
            chasseur = roleMemory.get("chasseur");
        }
        if (chasseur != null) {
            boolean isAlive = playersAlive.contains(chasseur);
            String status = isAlive ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            setScore(objective, ChatColor.GREEN + "  ◈ " + chasseurRoleName + ": " + status, line--);
        }

        // Petite Fille
        Player petiteFille = gm.getPetiteFille();
        if (petiteFille == null && roleMemory.containsKey("petiteFille")) {
            petiteFille = roleMemory.get("petiteFille");
        }
        if (petiteFille != null) {
            boolean isAlive = playersAlive.contains(petiteFille);
            String status = isAlive ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            setScore(objective, ChatColor.GREEN + "  ◈ " + petiteFilleRoleName + ": " + status, line--);
        }

        // Capitaine
        Player capitaine = gm.getCapitaine();
        if (capitaine == null && roleMemory.containsKey("capitaine")) {
            capitaine = roleMemory.get("capitaine");
        }
        if (capitaine != null) {
            boolean isAlive = playersAlive.contains(capitaine);
            if (isAlive) {
                setScore(objective, "  ", line--);
                setScore(objective, ChatColor.AQUA + "  ♔ " + capitaineRoleName + ": " +
                        ChatColor.WHITE + capitaine.getName(), line--);
            } else {
                // Show dead capitaine with strikethrough
                setScore(objective, "  ", line--);
                setScore(objective, ChatColor.AQUA + "  ♔ " + ChatColor.STRIKETHROUGH + capitaineRoleName + ": " +
                        ChatColor.GRAY + ChatColor.STRIKETHROUGH + capitaine.getName() + ChatColor.RED + " ✗", line--);
            }
        }

        // Ligne vide
        setScore(objective, "   ", line--);

        // Total de joueurs vivants
        setScore(objective, ChatColor.GRAY + "━━━━━━━━━━━━━━━━", line--);
        setScore(objective, ChatColor.WHITE + lm.getMessage("scoreboard.total") + ": " + ChatColor.YELLOW + playersAlive.size() +
                ChatColor.GRAY + " " + lm.getMessage("scoreboard.players"), line--);

        player.setScoreboard(scoreboard);
    }

    /**
     * Définit un score pour une ligne du scoreboard
     */
    private void setScore(Objective objective, String text, int score) {
        Score scoreEntry = objective.getScore(text);
        scoreEntry.setScore(score);
    }

    /**
     * Retire le scoreboard d'un joueur
     */
    public void removeScoreboard(Player player) {
        if (playerScoreboards.containsKey(player)) {
            Scoreboard scoreboard = playerScoreboards.get(player);
            Objective objective = scoreboard.getObjective("lgmc");
            if (objective != null) {
                objective.unregister();
            }
            playerScoreboards.remove(player);

            // Réinitialiser au scoreboard par défaut
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    /**
     * Retire tous les scoreboards
     */
    public void clearScoreboards() {
        // Create a copy of the keySet to avoid ConcurrentModificationException
        for (Player player : new java.util.ArrayList<>(playerScoreboards.keySet())) {
            removeScoreboard(player);
        }
        playerScoreboards.clear();
        roleMemory.clear();
        playerDeathStatus.clear();
    }
    
    /**
     * Register a player's role in memory so it can be displayed even after death
     */
    public void setRoleMemory(String role, Player player) {
        if (player != null) {
            roleMemory.put(role, player);
            playerDeathStatus.put(player, false); // Initially alive
        }
    }
    
    /**
     * Mark a player as dead in the scoreboard memory
     */
    public void markPlayerAsDead(Player player) {
        if (player != null) {
            playerDeathStatus.put(player, true);
        }
    }
}

