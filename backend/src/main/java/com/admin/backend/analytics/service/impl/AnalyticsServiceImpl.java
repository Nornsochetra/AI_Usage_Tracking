package com.admin.backend.analytics.service.impl;

import com.admin.backend.analytics.dto.DashboardAnalyticsResponse;
import com.admin.backend.analytics.service.AnalyticsService;
import com.admin.backend.common.repository.ApiKeyRepository;
import com.admin.backend.common.repository.DailyUserUsageRepository;
import com.admin.backend.common.repository.TeamRepository;
import com.admin.backend.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final DailyUserUsageRepository dailyUserUsageRepository;

    @Override
    public DashboardAnalyticsResponse getDashboardStats() {
        long teams = teamRepository.count();
        long users = userRepository.count();
        long keys = apiKeyRepository.count();

        Double totalSpend = dailyUserUsageRepository.sumAllSpend();
        Long promptTokens = dailyUserUsageRepository.sumAllPromptTokens();
        Long completionTokens = dailyUserUsageRepository.sumAllCompletionTokens();

        return DashboardAnalyticsResponse.builder()
                .totalTeams(teams)
                .totalUsers(users)
                .totalApiKeys(keys)
                .totalPromptTokens(promptTokens != null ? promptTokens : 0L)
                .totalCompletionTokens(completionTokens != null ? completionTokens : 0L)
                .totalSpendUsd(totalSpend != null ? totalSpend : 0.0)
                .build();
    }
}
