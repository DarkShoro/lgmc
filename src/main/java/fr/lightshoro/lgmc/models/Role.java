package fr.lightshoro.lgmc.models;

import org.bukkit.ChatColor;

public enum Role {
    LOUP_GAROU("Loup Garou", "&c&l", Team.BAD_GUYS),
    VILLAGEOIS("Villageois", "&a&l", Team.GOOD_GUYS),
    PETITE_FILLE("Petite Fille", "&a&l", Team.GOOD_GUYS),
    VOYANTE("Voyante", "&a&l", Team.GOOD_GUYS),
    SORCIERE("Sorcière", "&a&l", Team.GOOD_GUYS),
    CUPIDON("Cupidon", "&a&l", Team.GOOD_GUYS),
    CHASSEUR("Chasseur", "&a&l", Team.GOOD_GUYS),
    VOLEUR("Voleur", "&e&l", Team.GOOD_GUYS),
    ANGE("Ange", "&b&l", Team.ANGE),
    DEAD("Décédé(e)", "&7&l", Team.NONE),
    NOT_IN_GAME("Pas dans la partie", "&7&l", Team.NONE);

    private final String name;
    private final String colorCode;
    private final Team team;

    Role(String name, String colorCode, Team team) {
        this.name = name;
        this.colorCode = colorCode;
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Team getTeam() {
        return team;
    }

    @SuppressWarnings("deprecation")
    public String getFormattedName() {
        return ChatColor.translateAlternateColorCodes('&', colorCode + name);
    }

    public enum Team {
        GOOD_GUYS,
        BAD_GUYS,
        ANGE,
        NONE
    }
}

