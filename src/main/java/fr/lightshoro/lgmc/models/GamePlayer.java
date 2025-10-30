package fr.lightshoro.lgmc.models;

import org.bukkit.entity.Player;

public class GamePlayer {
    private final Player player;
    private Role role;
    private boolean didDesignation;
    private Player designated;
    private Player vote;
    private boolean didVote;
    private boolean didVoteForCapitaine;
    private String deathReason;

    public GamePlayer(Player player) {
        this.player = player;
        this.role = Role.NOT_IN_GAME;
        this.didDesignation = false;
        this.didVote = false;
        this.didVoteForCapitaine = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isDidDesignation() {
        return didDesignation;
    }

    public void setDidDesignation(boolean didDesignation) {
        this.didDesignation = didDesignation;
    }

    public Player getDesignated() {
        return designated;
    }

    public void setDesignated(Player designated) {
        this.designated = designated;
    }

    public Player getVote() {
        return vote;
    }

    public void setVote(Player vote) {
        this.vote = vote;
    }

    public boolean isDidVote() {
        return didVote;
    }

    public void setDidVote(boolean didVote) {
        this.didVote = didVote;
    }

    public boolean isDidVoteForCapitaine() {
        return didVoteForCapitaine;
    }

    public void setDidVoteForCapitaine(boolean didVoteForCapitaine) {
        this.didVoteForCapitaine = didVoteForCapitaine;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public void setDeathReason(String deathReason) {
        this.deathReason = deathReason;
    }

    public void reset() {
        this.didDesignation = false;
        this.designated = null;
        this.vote = null;
        this.didVote = false;
        this.didVoteForCapitaine = false;
        this.deathReason = null;
    }
}

