package com.admin.backend.team.service;

import com.admin.backend.common.domain.Team;
import com.admin.backend.team.dto.CreateTeamRequest;
import com.admin.backend.team.dto.TeamResponse;

import java.util.List;

public interface TeamService {

    List<TeamResponse> getAllTeams();

    Team createTeam(CreateTeamRequest request);
}
