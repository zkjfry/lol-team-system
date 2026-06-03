package com.example.lolteambackend.service.impl;

import com.example.lolteambackend.dto.*;
import com.example.lolteambackend.service.AssignmentService;
import com.example.lolteambackend.service.ChampionService;
import org.springframework.stereotype.Service;
import com.example.lolteambackend.dao.AssignmentHistoryDao;
import com.example.lolteambackend.dao.PlayerLaneWeightDao;
import com.example.lolteambackend.entity.PlayerLaneWeightEntity;
import com.example.lolteambackend.dao.AssignmentTeamHistoryDao;
import com.example.lolteambackend.entity.AssignmentTeamHistoryEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final List<String> LANES = List.of("TOP", "JUNGLE", "MID", "ADC", "SUPPORT");

    private final ChampionService championService;
    private final AssignmentHistoryDao assignmentHistoryDao;
    private final AssignmentTeamHistoryDao assignmentTeamHistoryDao;
    private final PlayerLaneWeightDao playerLaneWeightDao;
    private final Random random = new Random();

    public AssignmentServiceImpl(
            ChampionService championService,
            AssignmentHistoryDao assignmentHistoryDao,
            AssignmentTeamHistoryDao assignmentTeamHistoryDao,
            PlayerLaneWeightDao playerLaneWeightDao
    ) {
        this.championService = championService;
        this.assignmentHistoryDao = assignmentHistoryDao;
        this.assignmentTeamHistoryDao = assignmentTeamHistoryDao;
        this.playerLaneWeightDao = playerLaneWeightDao;
    }

    @Override
    public AssignResponse assign(AssignRequest request) {
        validateRequest(request);

        String assignmentGroupId = defaultIfBlank(request.getAssignmentGroupId(), "default-room");
        String region = defaultIfBlank(request.getRegion(), "Korea");
        String tier = defaultIfBlank(request.getTier(), "Emerald+");
        String queue = defaultIfBlank(request.getQueue(), "ranked");

        List<ChampionPoolDto> championPool = championService.getChampionPool(region, tier, queue);

        if (championPool.size() < 10) {
            throw new IllegalArgumentException("英雄池数量不足，当前英雄数: " + championPool.size());
        }

        List<Long> recentChampionIds = assignmentHistoryDao.selectRecentChampionIds(
                assignmentGroupId,
                2
        );

        Set<Long> bannedChampionIds = new HashSet<>(recentChampionIds);

        for (int attempt = 0; attempt < 1000; attempt++) {
            try {
                AssignResponse response = tryAssignOnce(
                        assignmentGroupId,
                        request.getPlayers(),
                        championPool,
                        bannedChampionIds
                );

                if (isDamageCompositionValid(response.getRedTeam())
                        && isDamageCompositionValid(response.getBlueTeam())) {

                    Long gameNo = assignmentHistoryDao.selectNextGameNo(assignmentGroupId);

                    List<AssignedPlayerDto> allAssignedPlayers = new ArrayList<>();
                    allAssignedPlayers.addAll(response.getRedTeam());
                    allAssignedPlayers.addAll(response.getBlueTeam());

                    List<HistoryChampionDto> historyChampions = new ArrayList<>();

                    for (AssignedPlayerDto player : allAssignedPlayers) {
                        if (player.getChampionOptions() == null) {
                            continue;
                        }

                        for (ChampionOptionDto option : player.getChampionOptions()) {
                            historyChampions.add(new HistoryChampionDto(
                                    option.getChampionId(),
                                    option.getChampionName()
                            ));
                        }
                    }

                    assignmentHistoryDao.insertHistoryBatch(
                            assignmentGroupId,
                            gameNo,
                            historyChampions
                    );

                    assignmentTeamHistoryDao.insertTeamHistoryBatch(
                            assignmentGroupId,
                            gameNo,
                            allAssignedPlayers
                    );

                    assignmentHistoryDao.deleteOldHistory(assignmentGroupId, 3);
                    assignmentTeamHistoryDao.deleteOldTeamHistory(assignmentGroupId, 5);

                    updatePlayerLaneWeights(assignmentGroupId, allAssignedPlayers);

                    assignmentHistoryDao.deleteExpiredHistory();
                    assignmentTeamHistoryDao.deleteExpiredTeamHistory();
                    playerLaneWeightDao.deleteExpiredWeights();

                    return response;
                }
            } catch (RuntimeException ignored) {
                // 当前随机方案失败，继续尝试
            }
        }

        throw new IllegalArgumentException("无法找到满足队伍、分路、英雄唯一、最近两把英雄不重复、伤害类型约束的分配结果");
    }

    private AssignResponse tryAssignOnce(
            String assignmentGroupId,
            List<PlayerInputDto> players,
            List<ChampionPoolDto> championPool,
            Set<Long> bannedChampionIds
    ) {
        TeamSplit teamSplit = pickTeamSplitByHistoryRules(
                assignmentGroupId,
                players
        );

        List<PlayerInputDto> redPlayers = teamSplit.redPlayers;
        List<PlayerInputDto> bluePlayers = teamSplit.bluePlayers;

        Set<Long> usedChampionIds = new HashSet<>(bannedChampionIds);

        List<AssignedPlayerDto> redAssigned =
                assignLanesAndChampions(assignmentGroupId, "RED", redPlayers, championPool, usedChampionIds);

        List<AssignedPlayerDto> blueAssigned =
                assignLanesAndChampions(assignmentGroupId, "BLUE", bluePlayers, championPool, usedChampionIds);

        return new AssignResponse(redAssigned, blueAssigned);
    }

    private static class TeamSplit {
        private final List<PlayerInputDto> redPlayers;
        private final List<PlayerInputDto> bluePlayers;

        private TeamSplit(List<PlayerInputDto> redPlayers, List<PlayerInputDto> bluePlayers) {
            this.redPlayers = redPlayers;
            this.bluePlayers = bluePlayers;
        }
    }

    private TeamSplit pickTeamSplitByHistoryRules(
            String assignmentGroupId,
            List<PlayerInputDto> players
    ) {
        List<TeamSplit> candidates = generateAllTeamSplits(players);

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("无法生成满足固定红蓝队的 5v5 分队");
        }

        List<AssignmentTeamHistoryEntity> recentHistory =
                assignmentTeamHistoryDao.selectRecentTeamHistory(assignmentGroupId, 3);

        Map<Long, Map<String, String>> historyByGame = groupTeamHistoryByGame(recentHistory);
        List<Long> recentGameNos = new ArrayList<>(historyByGame.keySet());
        recentGameNos.sort(Collections.reverseOrder());

        List<TeamSplit> validCandidates = new ArrayList<>();

        for (TeamSplit candidate : candidates) {
            if (violatesSecondGameRule(candidate, historyByGame, recentGameNos)) {
                continue;
            }

            if (violatesTripleThreeGamesRule(candidate, players, historyByGame, recentGameNos)) {
                continue;
            }

            if (violatesPairFourGamesRule(candidate, players, historyByGame, recentGameNos)) {
                continue;
            }

            validCandidates.add(candidate);
        }

        if (validCandidates.isEmpty()) {
            throw new IllegalArgumentException("历史组队限制导致无可用分队方案，请重置本轮记录或调整固定队伍");
        }

        return validCandidates.get(random.nextInt(validCandidates.size()));
    }

    private List<TeamSplit> generateAllTeamSplits(List<PlayerInputDto> players) {
        List<PlayerInputDto> fixedRed = new ArrayList<>();
        List<PlayerInputDto> fixedBlue = new ArrayList<>();
        List<PlayerInputDto> freePlayers = new ArrayList<>();

        for (PlayerInputDto player : players) {
            String fixedTeam = normalizeTeam(player.getFixedTeam());

            if ("RED".equals(fixedTeam)) {
                fixedRed.add(player);
            } else if ("BLUE".equals(fixedTeam)) {
                fixedBlue.add(player);
            } else {
                freePlayers.add(player);
            }
        }

        if (fixedRed.size() > 5) {
            throw new IllegalArgumentException("固定红队人数超过 5");
        }

        if (fixedBlue.size() > 5) {
            throw new IllegalArgumentException("固定蓝队人数超过 5");
        }

        int redNeed = 5 - fixedRed.size();
        int blueNeed = 5 - fixedBlue.size();

        if (redNeed < 0 || blueNeed < 0 || redNeed + blueNeed != freePlayers.size()) {
            throw new IllegalArgumentException("固定队伍人数异常");
        }

        List<List<PlayerInputDto>> redFreeCombinations = new ArrayList<>();
        collectCombinations(freePlayers, redNeed, 0, new ArrayList<>(), redFreeCombinations);

        List<TeamSplit> result = new ArrayList<>();

        for (List<PlayerInputDto> redFree : redFreeCombinations) {
            Set<String> redFreeNames = new HashSet<>();
            for (PlayerInputDto p : redFree) {
                redFreeNames.add(p.getName());
            }

            List<PlayerInputDto> red = new ArrayList<>(fixedRed);
            red.addAll(redFree);

            List<PlayerInputDto> blue = new ArrayList<>(fixedBlue);
            for (PlayerInputDto p : freePlayers) {
                if (!redFreeNames.contains(p.getName())) {
                    blue.add(p);
                }
            }

            if (red.size() == 5 && blue.size() == 5) {
                result.add(new TeamSplit(red, blue));
            }
        }

        Collections.shuffle(result, random);
        return result;
    }

    private void collectCombinations(
            List<PlayerInputDto> source,
            int targetSize,
            int startIndex,
            List<PlayerInputDto> current,
            List<List<PlayerInputDto>> result
    ) {
        if (current.size() == targetSize) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = startIndex; i < source.size(); i++) {
            current.add(source.get(i));
            collectCombinations(source, targetSize, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private Map<Long, Map<String, String>> groupTeamHistoryByGame(
            List<AssignmentTeamHistoryEntity> history
    ) {
        Map<Long, Map<String, String>> result = new HashMap<>();

        for (AssignmentTeamHistoryEntity row : history) {
            result
                    .computeIfAbsent(row.getGameNo(), k -> new HashMap<>())
                    .put(row.getPlayerName(), row.getTeam());
        }

        return result;
    }

    private boolean violatesSecondGameRule(
            TeamSplit candidate,
            Map<Long, Map<String, String>> historyByGame,
            List<Long> recentGameNos
    ) {
        if (recentGameNos.isEmpty()) {
            return false;
        }

        Long lastGameNo = recentGameNos.get(0);
        Map<String, String> lastGame = historyByGame.get(lastGameNo);

        if (lastGame == null || lastGame.isEmpty()) {
            return false;
        }

        Set<String> candidateRed = getPlayerNameSet(candidate.redPlayers);
        Set<String> candidateBlue = getPlayerNameSet(candidate.bluePlayers);

        Set<String> lastRed = new HashSet<>();
        Set<String> lastBlue = new HashSet<>();

        for (Map.Entry<String, String> entry : lastGame.entrySet()) {
            if ("RED".equals(entry.getValue())) {
                lastRed.add(entry.getKey());
            } else if ("BLUE".equals(entry.getValue())) {
                lastBlue.add(entry.getKey());
            }
        }

        return candidateRed.equals(lastRed) && candidateBlue.equals(lastBlue);
    }

    private boolean violatesTripleThreeGamesRule(
            TeamSplit candidate,
            List<PlayerInputDto> players,
            Map<Long, Map<String, String>> historyByGame,
            List<Long> recentGameNos
    ) {
        if (recentGameNos.size() < 2) {
            return false;
        }

        Map<String, String> game1 = historyByGame.get(recentGameNos.get(0));
        Map<String, String> game2 = historyByGame.get(recentGameNos.get(1));

        if (game1 == null || game2 == null) {
            return false;
        }

        List<String> names = players.stream()
                .map(PlayerInputDto::getName)
                .collect(Collectors.toList());

        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                for (int k = j + 1; k < names.size(); k++) {
                    String a = names.get(i);
                    String b = names.get(j);
                    String c = names.get(k);

                    boolean sameInLastTwo =
                            sameTeamInGame(game1, a, b, c)
                                    && sameTeamInGame(game2, a, b, c);

                    if (!sameInLastTwo) {
                        continue;
                    }

                    boolean sameInCandidate = sameTeamInCandidate(candidate, a, b, c);

                    if (sameInCandidate) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean violatesPairFourGamesRule(
            TeamSplit candidate,
            List<PlayerInputDto> players,
            Map<Long, Map<String, String>> historyByGame,
            List<Long> recentGameNos
    ) {
        if (recentGameNos.size() < 3) {
            return false;
        }

        Map<String, String> game1 = historyByGame.get(recentGameNos.get(0));
        Map<String, String> game2 = historyByGame.get(recentGameNos.get(1));
        Map<String, String> game3 = historyByGame.get(recentGameNos.get(2));

        if (game1 == null || game2 == null || game3 == null) {
            return false;
        }

        List<String> names = players.stream()
                .map(PlayerInputDto::getName)
                .collect(Collectors.toList());

        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                String a = names.get(i);
                String b = names.get(j);

                boolean sameInLastThree =
                        sameTeamInGame(game1, a, b)
                                && sameTeamInGame(game2, a, b)
                                && sameTeamInGame(game3, a, b);

                if (!sameInLastThree) {
                    continue;
                }

                boolean sameInCandidate = sameTeamInCandidate(candidate, a, b);

                if (sameInCandidate) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> getPlayerNameSet(List<PlayerInputDto> players) {
        return players.stream()
                .map(PlayerInputDto::getName)
                .collect(Collectors.toSet());
    }

    private boolean sameTeamInGame(
            Map<String, String> game,
            String a,
            String b
    ) {
        String teamA = game.get(a);
        String teamB = game.get(b);

        return teamA != null && teamA.equals(teamB);
    }

    private boolean sameTeamInGame(
            Map<String, String> game,
            String a,
            String b,
            String c
    ) {
        String teamA = game.get(a);
        String teamB = game.get(b);
        String teamC = game.get(c);

        return teamA != null
                && teamA.equals(teamB)
                && teamA.equals(teamC);
    }

    private boolean sameTeamInCandidate(
            TeamSplit candidate,
            String a,
            String b
    ) {
        Set<String> red = getPlayerNameSet(candidate.redPlayers);
        Set<String> blue = getPlayerNameSet(candidate.bluePlayers);

        return (red.contains(a) && red.contains(b))
                || (blue.contains(a) && blue.contains(b));
    }

    private boolean sameTeamInCandidate(
            TeamSplit candidate,
            String a,
            String b,
            String c
    ) {
        Set<String> red = getPlayerNameSet(candidate.redPlayers);
        Set<String> blue = getPlayerNameSet(candidate.bluePlayers);

        return (red.contains(a) && red.contains(b) && red.contains(c))
                || (blue.contains(a) && blue.contains(b) && blue.contains(c));
    }

    private List<AssignedPlayerDto> assignLanesAndChampions(
            String assignmentGroupId,
            String team,
            List<PlayerInputDto> players,
            List<ChampionPoolDto> championPool,
            Set<Long> usedChampionIds
    ) {
        List<PlayerInputDto> remainingPlayers = new ArrayList<>(players);
        List<String> remainingLanes = new ArrayList<>(LANES);
        List<AssignedPlayerDto> result = new ArrayList<>();

        // 1. 先处理固定分路：固定分路不走权重
        for (PlayerInputDto player : new ArrayList<>(remainingPlayers)) {
            String fixedLane = normalizeLane(player.getFixedLane());

            if (fixedLane == null) {
                continue;
            }

            if (!LANES.contains(fixedLane)) {
                throw new IllegalArgumentException("非法分路: " + fixedLane);
            }

            if (!remainingLanes.contains(fixedLane)) {
                throw new IllegalArgumentException("同一队存在重复固定分路: " + fixedLane);
            }

            List<ChampionPoolDto> champions = pickTwoChampionsForLane(fixedLane, championPool, usedChampionIds);

            result.add(buildAssignedPlayer(team, player, fixedLane, champions, true));

            remainingPlayers.remove(player);
            remainingLanes.remove(fixedLane);
        }

        // 2. 非固定分路：按每个玩家自己的 lane weight 抽
        Collections.shuffle(remainingPlayers, random);

        for (PlayerInputDto player : remainingPlayers) {
            if (remainingLanes.isEmpty()) {
                throw new IllegalArgumentException("没有剩余分路可分配");
            }

            String lane = pickLaneByPlayerWeight(
                    assignmentGroupId,
                    player.getName(),
                    remainingLanes
            );

            List<ChampionPoolDto> champions = pickTwoChampionsForLane(lane, championPool, usedChampionIds);

            result.add(buildAssignedPlayer(team, player, lane, champions, false));

            remainingLanes.remove(lane);
        }

        result.sort((a, b) -> Integer.compare(
                LANES.indexOf(a.getLane()),
                LANES.indexOf(b.getLane())
        ));

        return result;
    }

    private List<ChampionPoolDto> pickTwoChampionsForLane(
            String lane,
            List<ChampionPoolDto> championPool,
            Set<Long> usedChampionIds
    ) {
        List<ChampionPoolDto> candidates = championPool.stream()
                .filter(x -> !usedChampionIds.contains(x.getChampionId()))
                .filter(x -> lane.equals(x.getPrimaryLane()) || lane.equals(x.getSecondaryLane()))
                .filter(x -> x.getDamageType() != null)
                .filter(x -> !"UNKNOWN".equalsIgnoreCase(x.getDamageType()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (candidates.size() < 2) {
            candidates = championPool.stream()
                    .filter(x -> !usedChampionIds.contains(x.getChampionId()))
                    .filter(x -> lane.equals(x.getPrimaryLane()) || lane.equals(x.getSecondaryLane()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (candidates.size() < 2) {
            throw new IllegalArgumentException("分路 " + lane + " 可用英雄不足 2 个");
        }

        Collections.shuffle(candidates, random);

        List<ChampionPoolDto> selected = candidates.subList(0, 2);

        usedChampionIds.add(selected.get(0).getChampionId());
        usedChampionIds.add(selected.get(1).getChampionId());

        return new ArrayList<>(selected);
    }

    private int safeWeight(Integer value) {
        return value == null ? 20 : Math.max(value, 0);
    }

    private String pickLaneByPlayerWeight(
            String assignmentGroupId,
            String playerName,
            List<String> availableLanes
    ) {
        playerLaneWeightDao.insertDefaultIfAbsent(assignmentGroupId, playerName);

        PlayerLaneWeightEntity weight = playerLaneWeightDao.selectByGroupAndPlayer(
                assignmentGroupId,
                playerName
        );

        Map<String, Integer> laneWeights = new HashMap<>();
        laneWeights.put("TOP", safeWeight(weight.getTopWeight()));
        laneWeights.put("JUNGLE", safeWeight(weight.getJungleWeight()));
        laneWeights.put("MID", safeWeight(weight.getMidWeight()));
        laneWeights.put("ADC", safeWeight(weight.getAdcWeight()));
        laneWeights.put("SUPPORT", safeWeight(weight.getSupportWeight()));

        int total = 0;
        for (String lane : availableLanes) {
            total += laneWeights.getOrDefault(lane, 0);
        }

        if (total <= 0) {
            return availableLanes.get(random.nextInt(availableLanes.size()));
        }

        int r = random.nextInt(total) + 1;
        int cumulative = 0;

        for (String lane : availableLanes) {
            cumulative += laneWeights.getOrDefault(lane, 0);

            if (r <= cumulative) {
                return lane;
            }
        }

        return availableLanes.get(availableLanes.size() - 1);
    }

    private void updatePlayerLaneWeights(
            String assignmentGroupId,
            List<AssignedPlayerDto> assignedPlayers
    ) {
        for (AssignedPlayerDto player : assignedPlayers) {
            if (Boolean.TRUE.equals(player.getFixedLane())) {
                continue;
            }

            String playerName = player.getPlayerName();
            String assignedLane = player.getLane();

            playerLaneWeightDao.insertDefaultIfAbsent(assignmentGroupId, playerName);

            PlayerLaneWeightEntity current = playerLaneWeightDao.selectByGroupAndPlayer(
                    assignmentGroupId,
                    playerName
            );

            Map<String, Integer> weights = new HashMap<>();
            weights.put("TOP", safeWeight(current.getTopWeight()));
            weights.put("JUNGLE", safeWeight(current.getJungleWeight()));
            weights.put("MID", safeWeight(current.getMidWeight()));
            weights.put("ADC", safeWeight(current.getAdcWeight()));
            weights.put("SUPPORT", safeWeight(current.getSupportWeight()));

            String newLastLane1 = assignedLane;
            String newLastLane2 = current.getLastLane1();
            String newLastLane3 = current.getLastLane2();

            boolean threeSameInARow =
                    assignedLane != null
                            && assignedLane.equals(current.getLastLane1())
                            && assignedLane.equals(current.getLastLane2());

            if (threeSameInARow) {
                // 第三次连续同一个位置：该位置归零，其他四个位置 +1
                weights.put(assignedLane, 0);

                for (String lane : LANES) {
                    if (!lane.equals(assignedLane)) {
                        weights.put(lane, weights.get(lane) + 1);
                    }
                }
            } else {
                // 正常规则：本次位置 -8，其他位置 +2
                for (String lane : LANES) {
                    if (lane.equals(assignedLane)) {
                        weights.put(lane, Math.max(0, weights.get(lane) - 8));
                    } else {
                        weights.put(lane, weights.get(lane) + 2);
                    }
                }
            }

            normalizeWeightsTo100(weights);

            playerLaneWeightDao.updateAfterAssignedLane(
                    assignmentGroupId,
                    playerName,
                    weights.get("TOP"),
                    weights.get("JUNGLE"),
                    weights.get("MID"),
                    weights.get("ADC"),
                    weights.get("SUPPORT"),
                    newLastLane1,
                    newLastLane2,
                    newLastLane3
            );
        }
    }

    private void normalizeWeightsTo100(Map<String, Integer> weights) {
        int total = 0;

        for (String lane : LANES) {
            total += weights.getOrDefault(lane, 0);
        }

        if (total == 100) {
            return;
        }

        int diff = 100 - total;

        // 优先把差值加到当前最高的非零位置
        String targetLane = LANES.get(0);

        for (String lane : LANES) {
            if (weights.getOrDefault(lane, 0) > weights.getOrDefault(targetLane, 0)) {
                targetLane = lane;
            }
        }

        weights.put(targetLane, Math.max(0, weights.getOrDefault(targetLane, 0) + diff));
    }

    private AssignedPlayerDto buildAssignedPlayer(
            String team,
            PlayerInputDto player,
            String lane,
            List<ChampionPoolDto> champions,
            boolean fixedLane
    ) {
        AssignedPlayerDto dto = new AssignedPlayerDto();
        dto.setPlayerName(player.getName());
        dto.setTeam(team);
        dto.setLane(lane);
        dto.setFixedLane(fixedLane);

        List<ChampionOptionDto> options = champions.stream()
                .map(x -> new ChampionOptionDto(
                        x.getChampionId(),
                        x.getChampionName(),
                        x.getDamageType()
                ))
                .collect(Collectors.toList());

        dto.setChampionOptions(options);

        // 兼容旧前端：默认把第一个 option 也放到旧字段里
        if (!champions.isEmpty()) {
            ChampionPoolDto first = champions.get(0);
            dto.setChampionId(first.getChampionId());
            dto.setChampionName(first.getChampionName());
            dto.setDamageType(first.getDamageType());
        }

        return dto;
    }

    private boolean isDamageCompositionValid(List<AssignedPlayerDto> team) {
        long adCount = 0;
        long apCount = 0;

        for (AssignedPlayerDto player : team) {
            if (player.getChampionOptions() == null) {
                continue;
            }

            for (ChampionOptionDto option : player.getChampionOptions()) {
                if ("AD".equalsIgnoreCase(option.getDamageType())) {
                    adCount++;
                }

                if ("AP".equalsIgnoreCase(option.getDamageType())) {
                    apCount++;
                }
            }
        }

        return adCount <= 6 && apCount <= 6;
    }

    private void validateRequest(AssignRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }

        if (request.getPlayers() == null || request.getPlayers().size() != 10) {
            throw new IllegalArgumentException("必须传入 10 个玩家");
        }

        Set<String> names = new HashSet<>();

        for (PlayerInputDto player : request.getPlayers()) {
            if (player.getName() == null || player.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("玩家名不能为空");
            }

            String name = player.getName().trim();

            if (!names.add(name)) {
                throw new IllegalArgumentException("玩家名重复: " + name);
            }
        }
    }

    private String normalizeTeam(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toUpperCase(Locale.ROOT);

        if ("RED".equals(v) || "红队".equals(value.trim())) {
            return "RED";
        }

        if ("BLUE".equals(v) || "蓝队".equals(value.trim())) {
            return "BLUE";
        }

        return null;
    }

    private String normalizeLane(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toUpperCase(Locale.ROOT);

        switch (v) {
            case "TOP":
            case "上路":
                return "TOP";
            case "JUNGLE":
            case "JUG":
            case "打野":
                return "JUNGLE";
            case "MID":
            case "中路":
                return "MID";
            case "ADC":
            case "AD":
            case "下路":
                return "ADC";
            case "SUPPORT":
            case "SUP":
            case "辅助":
                return "SUPPORT";
            default:
                return null;
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }
}