package fr.lightshoro.lgmc.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import fr.lightshoro.lgmc.Lgmc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Manages vote display features:
 * 1. Client-side glow effect for voted players (via ProtocolLib)
 * 2. Vote count display next to player names
 */
public class VoteDisplayManager {
    private final Lgmc plugin;
    private final ProtocolManager protocolManager;
    private final Map<Player, Player> voterToTarget; // voter -> voted player
    
    public VoteDisplayManager(Lgmc plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.voterToTarget = new HashMap<>();
    }
    
    /**
     * Set glow effect for a player (client-side only for the voter)
     * @param voter The player who voted
     * @param target The player who was voted for
     */
    public void setGlowEffect(Player voter, Player target) {
        // Remove previous glow if any
        if (voterToTarget.containsKey(voter)) {
            Player oldTarget = voterToTarget.get(voter);
            if (oldTarget != null && oldTarget.isOnline()) {
                removeGlowEffect(voter, oldTarget);
            }
        }
        
        voterToTarget.put(voter, target);
        
        try {
            // Create the packet
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, target.getEntityId());
            
            // Get current flags from the target
            WrappedDataWatcher targetWatcher = WrappedDataWatcher.getEntityWatcher(target);
            Byte currentFlags = targetWatcher.getByte(0);
            if (currentFlags == null) {
                currentFlags = 0;
            }
            
            // Add glowing flag (0x40)
            byte newFlags = (byte) (currentFlags | 0x40);
            
            // Create the data value list manually
            @SuppressWarnings("removal")
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
            List<WrappedDataValue> dataValues = new ArrayList<>();
            dataValues.add(new WrappedDataValue(0, serializer, newFlags));
            
            // Set the data values to the packet
            packet.getDataValueCollectionModifier().write(0, dataValues);
            
            // Send packet only to the voter (client-side effect)
            protocolManager.sendServerPacket(voter, packet);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set glow effect: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Remove glow effect for a player (client-side)
     * @param voter The player who voted
     * @param target The player to remove glow from
     */
    public void removeGlowEffect(Player voter, Player target) {
        if (target == null || !target.isOnline()) {
            return;
        }
        
        try {
            // Create the packet
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, target.getEntityId());
            
            // Get current flags from the target
            WrappedDataWatcher targetWatcher = WrappedDataWatcher.getEntityWatcher(target);
            Byte currentFlags = targetWatcher.getByte(0);
            if (currentFlags == null) {
                currentFlags = 0;
            }
            
            // Remove glowing flag (0x40)
            byte newFlags = (byte) (currentFlags & ~0x40);
            
            // Create the data value list manually
            @SuppressWarnings("removal")
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
            List<WrappedDataValue> dataValues = new ArrayList<>();
            dataValues.add(new WrappedDataValue(0, serializer, newFlags));
            
            // Set the data values to the packet
            packet.getDataValueCollectionModifier().write(0, dataValues);
            
            // Send packet only to the voter (client-side effect)
            protocolManager.sendServerPacket(voter, packet);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove glow effect: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Clear all glow effects for a voter
     * @param voter The player to clear effects for
     */
    public void clearGlowForVoter(Player voter) {
        if (voterToTarget.containsKey(voter)) {
            Player target = voterToTarget.get(voter);
            if (target != null && target.isOnline()) {
                removeGlowEffect(voter, target);
            }
            voterToTarget.remove(voter);
        }
    }
    
    /**
     * Clear all glow effects
     */
    public void clearAllGlowEffects() {
        for (Map.Entry<Player, Player> entry : voterToTarget.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isOnline()) {
                removeGlowEffect(entry.getKey(), entry.getValue());
            }
        }
        voterToTarget.clear();
    }
    
    /**
     * Update vote count display next to all players' names
     */
    public void updateAllVoteDisplays() {
        Map<Player, Integer> voteCount = plugin.getGameManager().getVoteCount();
        
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = viewer.getScoreboard();
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                viewer.setScoreboard(scoreboard);
            }
            
            // Update each player's team to show vote count
            for (Player alive : plugin.getGameManager().getPlayersAlive()) {
                int votes = voteCount.getOrDefault(alive, 0);
                
                String teamName = "vote_" + alive.getName();
                Team team = scoreboard.getTeam(teamName);
                
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                    team.addEntry(alive.getName());
                }
                
                // Set suffix with vote count
                team.suffix(Component.text(" [" + votes + "]", NamedTextColor.YELLOW));
            }
        }
    }
    
    /**
     * Update capitaine vote count display next to all players' names
     */
    public void updateCapitaineVoteDisplays() {
        Map<Player, Integer> voteCapitaine = plugin.getGameManager().getVoteCapitaine();
        
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = viewer.getScoreboard();
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                viewer.setScoreboard(scoreboard);
            }
            
            // Update each player's team to show vote count
            for (Player alive : plugin.getGameManager().getPlayersAlive()) {
                int votes = voteCapitaine.getOrDefault(alive, 0);
                
                String teamName = "vote_" + alive.getName();
                Team team = scoreboard.getTeam(teamName);
                
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                    team.addEntry(alive.getName());
                }
                
                // Set suffix with vote count (using GOLD for capitaine votes)
                team.suffix(Component.text(" [" + votes + "]", NamedTextColor.GOLD));
            }
        }
    }
    
    /**
     * Clear vote displays
     */
    public void clearVoteDisplay() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard != null) {
                // Remove all vote teams
                for (Player alive : plugin.getGameManager().getPlayersAlive()) {
                    String teamName = "vote_" + alive.getName();
                    Team team = scoreboard.getTeam(teamName);
                    if (team != null) {
                        team.unregister();
                    }
                }
            }
        }
    }
    
    /**
     * Full reset of all vote displays and glow effects
     */
    public void reset() {
        clearAllGlowEffects();
        clearVoteDisplay();
    }
}
