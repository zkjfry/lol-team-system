package com.example.lolteambackend.dao;

import com.example.lolteambackend.entity.ChampionPoolEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChampionDao {

    List<ChampionPoolEntity> selectChampionPool(
            @Param("region") String region,
            @Param("tier") String tier,
            @Param("queueType") String queueType
    );
}