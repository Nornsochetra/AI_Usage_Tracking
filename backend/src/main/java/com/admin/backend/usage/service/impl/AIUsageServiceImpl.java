package com.admin.backend.usage.service.impl;

import com.admin.backend.common.domain.Team;
import com.admin.backend.common.domain.User;
import com.admin.backend.common.repository.DailyUserUsageRepository;
import com.admin.backend.common.repository.TeamRepository;
import com.admin.backend.common.repository.UserRepository;
import com.admin.backend.usage.payload.AIUsageTelemetryRequest;
import com.admin.backend.usage.payload.BudgetStatusResponse;
import com.admin.backend.usage.service.AIUsageService;
import com.admin.backend.usage.util.CostCalculator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AIUsageServiceImpl implements AIUsageService {

    private static final Logger log = LoggerFactory.getLogger(AIUsageServiceImpl.class);
    private static final DateTimeFormatter MONTH_KEY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final DailyUserUsageRepository dailyUserUsageRepository;
    private final CostCalculator costCalculator;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void ingestUsage(AIUsageTelemetryRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + request.getUserEmail()));

        long promptTokens = request.getPromptTokens() != null ? request.getPromptTokens() : 0L;
        long completionTokens = request.getCompletionTokens() != null ? request.getCompletionTokens() : 0L;
        recordUsage(user, request.getModel(), promptTokens, completionTokens);
    }

    @Override
    public void recordUsage(User user, String model, long promptTokens, long completionTokens) {
        Team team = user.getTeam();
        double cost = costCalculator.calculateCost(model, promptTokens, completionTokens);

        LocalDate today = LocalDate.now();
        dailyUserUsageRepository.incrementUsage(today, team.getId(), user.getId(),
                (int) promptTokens, (int) completionTokens, cost);

        double monthSpend = incrementMonthlySpend(team.getId(), today, cost);

        if (team.getMonthlyBudgetUsd() != null && team.getMonthlyBudgetUsd() > 0
                && monthSpend > team.getMonthlyBudgetUsd()) {
            log.warn("Team {} exceeded its monthly budget: {} / {}",
                    team.getId(), monthSpend, team.getMonthlyBudgetUsd());
        }
    }

    @Override
    public BudgetStatusResponse getBudgetStatus(String teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown team: " + teamId));

        LocalDate today = LocalDate.now();
        Double cachedSpend = readMonthlySpendFromCache(teamId, today);
        double currentSpend = cachedSpend != null ? cachedSpend : loadMonthlySpendFromDb(teamId, today);

        double budget = team.getMonthlyBudgetUsd() != null ? team.getMonthlyBudgetUsd() : 0.0;
        double remaining = budget - currentSpend;

        return new BudgetStatusResponse(team.getId(), team.getTeamName(), budget,
                currentSpend, remaining, budget > 0 && currentSpend > budget);
    }

    private double incrementMonthlySpend(String teamId, LocalDate date, double cost) {
        String key = monthlySpendKey(teamId, date);
        Double updated = redisTemplate.opsForValue().increment(key, cost);
        redisTemplate.expire(key, Duration.ofDays(35));
        return updated != null ? updated : loadMonthlySpendFromDb(teamId, date);
    }

    private Double readMonthlySpendFromCache(String teamId, LocalDate date) {
        String value = redisTemplate.opsForValue().get(monthlySpendKey(teamId, date));
        return value != null ? Double.valueOf(value) : null;
    }

    private double loadMonthlySpendFromDb(String teamId, LocalDate date) {
        YearMonth month = YearMonth.from(date);
        Double sum = dailyUserUsageRepository.sumSpendByTeamAndDateRange(
                teamId, month.atDay(1), month.atEndOfMonth());
        return sum != null ? sum : 0.0;
    }

    private String monthlySpendKey(String teamId, LocalDate date) {
        return "usage:budget:%s:%s".formatted(teamId, MONTH_KEY_FORMAT.format(date));
    }
}
