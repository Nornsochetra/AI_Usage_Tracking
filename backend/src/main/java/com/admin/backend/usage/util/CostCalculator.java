package com.admin.backend.usage.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CostCalculator {

    private record Rate(double promptPer1k, double completionPer1k) {
    }

    private static final Rate DEFAULT_RATE = new Rate(0.003, 0.015);

    private static final Map<String, Rate> RATES_PER_1K_TOKENS = Map.ofEntries(
            Map.entry("claude-opus-4-8", new Rate(0.015, 0.075)),
            Map.entry("claude-sonnet-5", new Rate(0.003, 0.015)),
            Map.entry("claude-haiku-4-5-20251001", new Rate(0.0008, 0.004)),
            Map.entry("gpt-4o", new Rate(0.0025, 0.01)),
            Map.entry("gpt-4o-mini", new Rate(0.00015, 0.0006)),
            Map.entry("gemini-1.5-pro", new Rate(0.00125, 0.005)),
            Map.entry("gemini-1.5-flash", new Rate(0.000075, 0.0003))
    );

    public double calculateCost(String model, long promptTokens, long completionTokens) {
        Rate rate = RATES_PER_1K_TOKENS.getOrDefault(model, DEFAULT_RATE);
        return (promptTokens / 1000.0) * rate.promptPer1k()
                + (completionTokens / 1000.0) * rate.completionPer1k();
    }
}
