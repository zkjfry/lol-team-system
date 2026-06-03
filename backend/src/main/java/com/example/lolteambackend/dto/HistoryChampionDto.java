package com.example.lolteambackend.dto;

public class HistoryChampionDto {

    private Long championId;
    private String championName;

    public HistoryChampionDto() {
    }

    public HistoryChampionDto(Long championId, String championName) {
        this.championId = championId;
        this.championName = championName;
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
}