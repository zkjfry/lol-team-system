package com.example.lolteambackend.controller;

import com.example.lolteambackend.dto.ChampionPoolDto;
import com.example.lolteambackend.service.ChampionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChampionController {

    private final ChampionService championService;

    public ChampionController(ChampionService championService) {
        this.championService = championService;
    }

    @GetMapping("/api/champions/pool")
    public List<ChampionPoolDto> getChampionPool(
            @RequestParam(defaultValue = "Korea") String region,
            @RequestParam(defaultValue = "Emerald+") String tier,
            @RequestParam(defaultValue = "ranked") String queue
    ) {
        return championService.getChampionPool(region, tier, queue);
    }
}