package com.example.lolteambackend.dto;

import java.util.List;

public class AssignedPlayerDto {

    private String playerName;
    private String team;
    private String lane;

    private Long championId;
    private String championName;
    private String damageType;

    private List<ChampionOptionDto> championOptions;

    private Boolean fixedLane;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getChampionId() {
        return championId;
    }

    public void setChampionId(Long championId) {
        this.championId = championId;
    }

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public List<ChampionOptionDto> getChampionOptions() {
        return championOptions;
    }

    public void setChampionOptions(List<ChampionOptionDto> championOptions) {
        this.championOptions = championOptions;
    }

    public Boolean getFixedLane() {
        return fixedLane;
    }

    public void setFixedLane(Boolean fixedLane) {
        this.fixedLane = fixedLane;
    }
}