package com.example.lolteambackend.service.impl;

import com.example.lolteambackend.dao.ChampionDao;
import com.example.lolteambackend.dto.ChampionPoolDto;
import com.example.lolteambackend.entity.ChampionPoolEntity;
import com.example.lolteambackend.service.ChampionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChampionServiceImpl implements ChampionService {

    private final ChampionDao championDao;

    public ChampionServiceImpl(ChampionDao championDao) {
        this.championDao = championDao;
    }

    @Override
    public List<ChampionPoolDto> getChampionPool(String region, String tier, String queueType) {
        List<ChampionPoolEntity> entities = championDao.selectChampionPool(region, tier, queueType);

        return entities.stream()
                .map(x -> new ChampionPoolDto(
                        x.getChampionId(),
                        x.getChampionNameCn(),
                        x.getDamageType(),
                        x.getPrimaryLane(),
                        x.getSecondaryLane()
                ))
                .collect(Collectors.toList());
    }
}