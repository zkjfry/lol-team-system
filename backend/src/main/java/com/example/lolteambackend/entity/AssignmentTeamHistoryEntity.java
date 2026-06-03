package com.example.lolteambackend.entity;

public class AssignmentTeamHistoryEntity {

    private Long id;
    private String assignmentGroupId;
    private Long gameNo;
    private String playerName;
    private String team;

    public Long getId() {
        return id;
    }

    public String getAssignmentGroupId() {
        return assignmentGroupId;
    }

    public void setAssignmentGroupId(String assignmentGroupId) {
        this.assignmentGroupId = assignmentGroupId;
    }

    public Long getGameNo() {
        return gameNo;
    }

    public void setGameNo(Long gameNo) {
        this.gameNo = gameNo;
    }

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
}