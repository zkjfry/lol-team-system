package com.example.lolteambackend.entity;

public class ChampionPoolEntity {

    private Long championId;
    private String championNameCn;
    private String damageType;
    private Boolean isEnabled;

    private String primaryLane;
    private String secondaryLane;

    private String source;
    private String region;
    private String tier;
    private String queueType;

    public Long getChampionId() {
        return championId;
    }

    public void setChampionId(Long championId) {
        this.championId = championId;
    }

    public String getChampionNameCn() {
        return championNameCn;
    }

    public void setChampionNameCn(String championNameCn) {
        this.championNameCn = championNameCn;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }
}