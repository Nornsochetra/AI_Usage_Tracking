package com.admin.backend.team.dto;

import lombok.Data;

@Data
public class CreateTeamRequest {
    private String teamName;
    private Double monthlyBudgetUsd;
}
