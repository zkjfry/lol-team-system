package com.example.lolteambackend.dto;

public class PlayerInputDto {

    private String name;

    /**
     * RED / BLUE / null
     */
    private String fixedTeam;

    /**
     * TOP / JUNGLE / MID / ADC / SUPPORT / null
     */
    private String fixedLane;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFixedTeam() {
        return fixedTeam;
    }

    public void setFixedTeam(String fixedTeam) {
        this.fixedTeam = fixedTeam;
    }

    public String getFixedLane() {
        return fixedLane;
    }

    public void setFixedLane(String fixedLane) {
        this.fixedLane = fixedLane;
    }
}