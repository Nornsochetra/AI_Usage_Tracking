package com.admin.backend.usage.controller;

import com.admin.backend.usage.payload.AIUsageTelemetryRequest;
import com.admin.backend.usage.payload.BudgetStatusResponse;
import com.admin.backend.usage.service.AIUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class AIUsageIngestionController {

    private final AIUsageService aiUsageService;

    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@RequestBody AIUsageTelemetryRequest request) {
        aiUsageService.ingestUsage(request);
    }

    @GetMapping("/budget/{teamId}")
    public BudgetStatusResponse getBudgetStatus(@PathVariable String teamId) {
        return aiUsageService.getBudgetStatus(teamId);
    }
}
