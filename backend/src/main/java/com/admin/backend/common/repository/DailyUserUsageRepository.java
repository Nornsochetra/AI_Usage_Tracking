package com.admin.backend.common.repository;

import com.admin.backend.common.domain.DailyUserUsage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DailyUserUsageRepository extends JpaRepository<DailyUserUsage, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO daily_user_usage (usage_date, team_id, user_id, total_requests, prompt_tokens, completion_tokens, total_spend_usd, last_request_at)
        VALUES (:date, :teamId, :userId, 1, :promptTokens, :completionTokens, :cost, CURRENT_TIMESTAMP)
        ON CONFLICT (usage_date, team_id, user_id)
        DO UPDATE SET 
            total_requests = daily_user_usage.total_requests + 1,
            prompt_tokens = daily_user_usage.prompt_tokens + EXCLUDED.prompt_tokens,
            completion_tokens = daily_user_usage.completion_tokens + EXCLUDED.completion_tokens,
            total_spend_usd = daily_user_usage.total_spend_usd + EXCLUDED.total_spend_usd,
            last_request_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void incrementUsage(@Param("date") LocalDate date,
                        @Param("teamId") String teamId,
                        @Param("userId") String userId,
                        @Param("promptTokens") Integer promptTokens,
                        @Param("completionTokens") Integer completionTokens,
                        @Param("cost") Double cost);

    @Query("""
        SELECT COALESCE(SUM(u.totalSpendUsd), 0)
        FROM DailyUserUsage u
        WHERE u.teamId = :teamId
        AND u.usageDate BETWEEN :from AND :to
        """)
    Double sumSpendByTeamAndDateRange(@Param("teamId") String teamId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    @Query("SELECT SUM(u.totalSpendUsd) FROM DailyUserUsage u")
    Double sumAllSpend();

    @Query("SELECT SUM(u.promptTokens) FROM DailyUserUsage u")
    Long sumAllPromptTokens();

    @Query("SELECT SUM(u.completionTokens) FROM DailyUserUsage u")
    Long sumAllCompletionTokens();

}
