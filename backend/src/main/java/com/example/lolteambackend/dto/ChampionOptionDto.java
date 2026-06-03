package com.example.lolteambackend.dto;

public class ChampionOptionDto {

    private Long championId;
    private String championName;
    private String damageType;

    public ChampionOptionDto() {
    }

    public ChampionOptionDto(Long championId, String championName, String damageType) {
        this.championId = championId;
        this.championName = championName;
        this.damageType = damageType;
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
}