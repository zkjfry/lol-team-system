package com.example.lolteambackend.dto;

import java.util.List;

public class AssignRequest {

    private String assignmentGroupId;
    private String region;
    private String tier;
    private String queue;
    private List<PlayerInputDto> players;

    public String getAssignmentGroupId() {
        return assignmentGroupId;
    }

    public void setAssignmentGroupId(String assignmentGroupId) {
        this.assignmentGroupId = assignmentGroupId;
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

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public List<PlayerInputDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInputDto> players) {
        this.players = players;
    }
}