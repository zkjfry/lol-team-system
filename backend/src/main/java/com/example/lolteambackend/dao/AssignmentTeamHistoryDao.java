package com.example.lolteambackend.dao;

import com.example.lolteambackend.dto.AssignedPlayerDto;
import com.example.lolteambackend.entity.AssignmentTeamHistoryEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AssignmentTeamHistoryDao {

    List<AssignmentTeamHistoryEntity> selectRecentTeamHistory(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("recentGameCount") int recentGameCount
    );

    void insertTeamHistoryBatch(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("gameNo") Long gameNo,
            @Param("players") List<AssignedPlayerDto> players
    );

    void deleteOldTeamHistory(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("keepGameCount") int keepGameCount
    );

    void deleteExpiredTeamHistory();
}