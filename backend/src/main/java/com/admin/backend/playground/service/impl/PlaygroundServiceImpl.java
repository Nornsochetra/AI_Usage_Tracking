package com.admin.backend.playground.service.impl;

import com.admin.backend.common.domain.User;
import com.admin.backend.common.enumeration.AiProvider;
import com.admin.backend.common.repository.UserRepository;
import com.admin.backend.playground.dto.PlaygroundRequest;
import com.admin.backend.playground.dto.PlaygroundResponse;
import com.admin.backend.playground.service.PlaygroundService;
import com.admin.backend.usage.service.AIUsageService;
import com.admin.backend.usage.util.CostCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaygroundServiceImpl implements PlaygroundService {

    private static final Logger log = LoggerFactory.getLogger(PlaygroundServiceImpl.class);

    private final UserRepository userRepository;
    private final AIUsageService aiUsageService;
    private final CostCalculator costCalculator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${gateway.anthropic.upstream-base-url}")
    private String anthropicBaseUrl;

    @Value("${gateway.anthropic.upstream-api-key}")
    private String anthropicApiKey;

    @Value("${gateway.gemini.upstream-base-url}")
    private String geminiBaseUrl;

    @Value("${gateway.gemini.upstream-api-key}")
    private String geminiApiKey;

    @Override
    public PlaygroundResponse generate(PlaygroundRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + request.getUserId()));

        AiProvider provider = request.getProvider();
        String model = (request.getModel() != null && !request.getModel().isBlank())
                ? request.getModel().trim()
                : defaultModel(provider);

        try {
            return provider == AiProvider.GEMINI
                    ? callGemini(user, model, request.getPrompt())
                    : callAnthropic(user, model, request.getPrompt());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Playground call to {} failed", provider, e);
            return fail(model, "Failed to reach provider: " + e.getMessage());
        }
    }

    private PlaygroundResponse callGemini(User user, String model, String prompt)
            throws IOException, InterruptedException {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return fail(model, "GEMINI_API_KEY is not configured in the backend .env");
        }

        String body = objectMapper.writeValueAsString(Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(geminiBaseUrl + "/v1beta/models/" + model + ":generateContent?key=" + geminiApiKey))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(resp.body());
        if (resp.statusCode() >= 300) {
            return fail(model, root.path("error").path("message").asText("Provider error " + resp.statusCode()));
        }

        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
        JsonNode usage = root.path("usageMetadata");
        long promptTokens = usage.path("promptTokenCount").asLong(0);
        long totalTokens = usage.path("totalTokenCount").asLong(0);
        // completion = everything Google billed as output, including reasoning ("thoughts") tokens
        long completionTokens = Math.max(0, totalTokens - promptTokens);

        return record(user, model, text, promptTokens, completionTokens);
    }

    private PlaygroundResponse callAnthropic(User user, String model, String prompt)
            throws IOException, InterruptedException {
        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            return fail(model, "ANTHROPIC_API_KEY is not configured in the backend .env");
        }

        String body = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt))));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(anthropicBaseUrl + "/v1/messages"))
                .header("content-type", "application/json")
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(resp.body());
        if (resp.statusCode() >= 300) {
            return fail(model, root.path("error").path("message").asText("Provider error " + resp.statusCode()));
        }

        String text = root.path("content").path(0).path("text").asText("");
        JsonNode usage = root.path("usage");
        long promptTokens = usage.path("input_tokens").asLong(0);
        long completionTokens = usage.path("output_tokens").asLong(0);

        return record(user, model, text, promptTokens, completionTokens);
    }

    private PlaygroundResponse record(User user, String model, String text,
                                      long promptTokens, long completionTokens) {
        aiUsageService.recordUsage(user, model, promptTokens, completionTokens);
        double cost = costCalculator.calculateCost(model, promptTokens, completionTokens);
        return PlaygroundResponse.builder()
                .success(true)
                .model(model)
                .responseText(text)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .costUsd(cost)
                .build();
    }

    private PlaygroundResponse fail(String model, String error) {
        return PlaygroundResponse.builder().success(false).model(model).error(error).build();
    }

    private String defaultModel(AiProvider provider) {
        return provider == AiProvider.GEMINI ? "gemini-flash-latest" : "claude-3-5-haiku-latest";
    }
}
