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
    private Player votedCapitaine; // Stocke pour qui le joueur a voté pour capitaine
    private Player votedPlayer; // Stocke pour qui le joueur a voté lors de la désignation
    private String deathReason;
    private Player firstLover;
    private Player secondLover;
    private boolean hasGuiOpen;

    public GamePlayer(Player player) {
        this.player = player;
        this.role = Role.NOT_IN_GAME;
        this.didDesignation = false;
        this.didVote = false;
        this.didVoteForCapitaine = false;
        this.hasGuiOpen = false;
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

    public Player getFirstLover() {
        return firstLover;
    }

    public void setFirstLover(Player firstLover) {
        this.firstLover = firstLover;
    }

    public Player getSecondLover() {
        return secondLover;
    }

    public void setSecondLover(Player secondLover) {
        this.secondLover = secondLover;
    }

    public Player getVotedCapitaine() {
        return votedCapitaine;
    }

    public void setVotedCapitaine(Player votedCapitaine) {
        this.votedCapitaine = votedCapitaine;
    }

    public Player getVotedPlayer() {
        return votedPlayer;
    }

    public void setVotedPlayer(Player votedPlayer) {
        this.votedPlayer = votedPlayer;
    }

    public boolean hasGuiOpen() {
        return hasGuiOpen;
    }

    public void setGuiOpen(boolean hasGuiOpen) {
        this.hasGuiOpen = hasGuiOpen;
    }

    public void reset() {
        this.didDesignation = false;
        this.designated = null;
        this.vote = null;
        this.didVote = false;
        this.didVoteForCapitaine = false;
        this.votedCapitaine = null;
        this.votedPlayer = null;
        this.deathReason = null;
        this.firstLover = null;
        this.secondLover = null;
        this.hasGuiOpen = false;
    }
}

