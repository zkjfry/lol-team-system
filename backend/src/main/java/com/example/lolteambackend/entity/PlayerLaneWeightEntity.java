package com.example.lolteambackend.entity;

public class PlayerLaneWeightEntity {

    private Long id;
    private String assignmentGroupId;
    private String playerName;

    private Integer topWeight;
    private Integer jungleWeight;
    private Integer midWeight;
    private Integer adcWeight;
    private Integer supportWeight;

    private String lastLane1;
    private String lastLane2;
    private String lastLane3;

    public Long getId() {
        return id;
    }

    public String getAssignmentGroupId() {
        return assignmentGroupId;
    }

    public void setAssignmentGroupId(String assignmentGroupId) {
        this.assignmentGroupId = assignmentGroupId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getTopWeight() {
        return topWeight;
    }

    public void setTopWeight(Integer topWeight) {
        this.topWeight = topWeight;
    }

    public Integer getJungleWeight() {
        return jungleWeight;
    }

    public void setJungleWeight(Integer jungleWeight) {
        this.jungleWeight = jungleWeight;
    }

    public Integer getMidWeight() {
        return midWeight;
    }

    public void setMidWeight(Integer midWeight) {
        this.midWeight = midWeight;
    }

    public Integer getAdcWeight() {
        return adcWeight;
    }

    public void setAdcWeight(Integer adcWeight) {
        this.adcWeight = adcWeight;
    }

    public Integer getSupportWeight() {
        return supportWeight;
    }

    public void setSupportWeight(Integer supportWeight) {
        this.supportWeight = supportWeight;
    }

    public String getLastLane1() {
        return lastLane1;
    }

    public void setLastLane1(String lastLane1) {
        this.lastLane1 = lastLane1;
    }

    public String getLastLane2() {
        return lastLane2;
    }

    public void setLastLane2(String lastLane2) {
        this.lastLane2 = lastLane2;
    }

    public String getLastLane3() {
        return lastLane3;
    }

    public void setLastLane3(String lastLane3) {
        this.lastLane3 = lastLane3;
    }
}