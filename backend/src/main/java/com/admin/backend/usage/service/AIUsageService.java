package com.admin.backend.usage.service;

import com.admin.backend.common.domain.User;
import com.admin.backend.usage.payload.AIUsageTelemetryRequest;
import com.admin.backend.usage.payload.BudgetStatusResponse;

public interface AIUsageService {

    void ingestUsage(AIUsageTelemetryRequest request);

    void recordUsage(User user, String model, long promptTokens, long completionTokens);

    BudgetStatusResponse getBudgetStatus(String teamId);
}
