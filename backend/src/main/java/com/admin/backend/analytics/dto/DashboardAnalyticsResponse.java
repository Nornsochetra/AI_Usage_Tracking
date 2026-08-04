package com.admin.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsResponse {
    private long totalTeams;
    private long totalUsers;
    private long totalApiKeys;
    private long totalPromptTokens;
    private long totalCompletionTokens;
    private double totalSpendUsd;
}
