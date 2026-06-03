package com.example.lolteambackend.dto;

public class ChampionPoolDto {

    private Long championId;
    private String championName;
    private String damageType;
    private String primaryLane;
    private String secondaryLane;

    public ChampionPoolDto() {
    }

    public ChampionPoolDto(Long championId, String championName, String damageType,
                           String primaryLane, String secondaryLane) {
        this.championId = championId;
        this.championName = championName;
        this.damageType = damageType;
        this.primaryLane = primaryLane;
        this.secondaryLane = secondaryLane;
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

    public String getPrimaryLane() {
        return primaryLane;
    }

    public void setPrimaryLane(String primaryLane) {
        this.primaryLane = primaryLane;
    }

    public String getSecondaryLane() {
        return secondaryLane;
    }

    public void setSecondaryLane(String secondaryLane) {
        this.secondaryLane = secondaryLane;
    }
}