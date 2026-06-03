package com.example.lolteambackend.dao;

import com.example.lolteambackend.entity.PlayerLaneWeightEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlayerLaneWeightDao {

    PlayerLaneWeightEntity selectByGroupAndPlayer(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("playerName") String playerName
    );

    void insertDefaultIfAbsent(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("playerName") String playerName
    );

    void updateAfterAssignedLane(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("playerName") String playerName,
            @Param("topWeight") int topWeight,
            @Param("jungleWeight") int jungleWeight,
            @Param("midWeight") int midWeight,
            @Param("adcWeight") int adcWeight,
            @Param("supportWeight") int supportWeight,
            @Param("lastLane1") String lastLane1,
            @Param("lastLane2") String lastLane2,
            @Param("lastLane3") String lastLane3
    );

    void deleteExpiredWeights();
}