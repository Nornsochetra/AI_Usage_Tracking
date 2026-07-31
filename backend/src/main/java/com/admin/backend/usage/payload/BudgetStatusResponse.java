package com.admin.backend.usage.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class BudgetStatusResponse {

    private String teamId;

    private String teamName;

    private Double monthlyBudgetUsd;

    private Double currentSpendUsd;

    private Double remainingUsd;

    private boolean overBudget;
}
