package com.admin.backend.team.service.impl;

import com.admin.backend.common.domain.Team;
import com.admin.backend.common.repository.TeamRepository;
import com.admin.backend.common.repository.UserRepository;
import com.admin.backend.team.dto.CreateTeamRequest;
import com.admin.backend.team.dto.TeamResponse;
import com.admin.backend.team.service.TeamService;
import com.admin.backend.usage.service.AIUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AIUsageService aiUsageService;

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream().map(team -> {
            Double currentSpend = aiUsageService.getBudgetStatus(team.getId()).getCurrentSpendUsd();
            long memberCount = userRepository.countByTeamId(team.getId());
            return TeamResponse.builder()
                    .id(team.getId())
                    .teamName(team.getTeamName())
                    .monthlyBudgetUsd(team.getMonthlyBudgetUsd())
                    .currentSpendUsd(currentSpend)
                    .memberCount(memberCount)
                    .createdAt(team.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Team createTeam(CreateTeamRequest request) {
        Team team = new Team();
        team.setTeamName(request.getTeamName());
        team.setMonthlyBudgetUsd(request.getMonthlyBudgetUsd() != null ? request.getMonthlyBudgetUsd() : 0.0);
        return teamRepository.save(team);
    }
}
