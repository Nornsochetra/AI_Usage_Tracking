package com.admin.backend.team.controller;

import com.admin.backend.common.domain.Team;
import com.admin.backend.team.dto.CreateTeamRequest;
import com.admin.backend.team.dto.TeamResponse;
import com.admin.backend.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamManagementController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamResponse> getAllTeams() {
        return teamService.getAllTeams();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request);
    }
}
