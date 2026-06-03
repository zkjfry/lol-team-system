package com.example.lolteambackend.service;

import com.example.lolteambackend.dto.ChampionPoolDto;

import java.util.List;

public interface ChampionService {

    List<ChampionPoolDto> getChampionPool(String region, String tier, String queueType);
}