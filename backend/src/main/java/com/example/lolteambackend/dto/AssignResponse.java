package com.example.lolteambackend.dto;

import java.util.List;

public class AssignResponse {

    private List<AssignedPlayerDto> redTeam;
    private List<AssignedPlayerDto> blueTeam;

    public AssignResponse() {
    }

    public AssignResponse(List<AssignedPlayerDto> redTeam, List<AssignedPlayerDto> blueTeam) {
        this.redTeam = redTeam;
        this.blueTeam = blueTeam;
    }

    public List<AssignedPlayerDto> getRedTeam() {
        return redTeam;
    }

    public void setRedTeam(List<AssignedPlayerDto> redTeam) {
        this.redTeam = redTeam;
    }

    public List<AssignedPlayerDto> getBlueTeam() {
        return blueTeam;
    }

    public void setBlueTeam(List<AssignedPlayerDto> blueTeam) {
        this.blueTeam = blueTeam;
    }
}