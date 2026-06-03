package com.example.lolteambackend.dao;

import com.example.lolteambackend.dto.AssignedPlayerDto;
import com.example.lolteambackend.dto.HistoryChampionDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AssignmentHistoryDao {

    Long selectNextGameNo(@Param("assignmentGroupId") String assignmentGroupId);

    List<Long> selectRecentChampionIds(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("recentGameCount") int recentGameCount
    );

    void insertHistoryBatch(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("gameNo") Long gameNo,
            @Param("champions") List<HistoryChampionDto> champions
    );

    void deleteOldHistory(
            @Param("assignmentGroupId") String assignmentGroupId,
            @Param("keepGameCount") int keepGameCount
    );

    void deleteExpiredHistory();
}