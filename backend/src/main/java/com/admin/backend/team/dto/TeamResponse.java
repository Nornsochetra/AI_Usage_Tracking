package com.admin.backend.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private String id;
    private String teamName;
    private Double monthlyBudgetUsd;
    private Double currentSpendUsd;
    private Long memberCount;
    private LocalDateTime createdAt;
}
