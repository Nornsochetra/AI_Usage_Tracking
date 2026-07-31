package com.admin.backend.common.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_user_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_date_team_user",
                columnNames = {"usage_date", "team_id", "user_id"}
        )
)
@Getter @Setter
public class DailyUserUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate usageDate;

    @Column(name = "team_id", nullable = false)
    private String teamId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private Integer totalRequests = 0;

    @Column(nullable = false)
    private Long promptTokens = 0L;

    @Column(nullable = false)
    private Long completionTokens = 0L;

    @Column(nullable = false)
    private Double totalSpendUsd = 0.0;

    private LocalDateTime lastRequestAt;
}