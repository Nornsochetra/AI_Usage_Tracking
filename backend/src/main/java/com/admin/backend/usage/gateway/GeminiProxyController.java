package com.admin.backend.usage.gateway;

import com.admin.backend.common.domain.ApiKey;
import com.admin.backend.common.domain.User;
import com.admin.backend.common.enumeration.AiProvider;
import com.admin.backend.common.repository.ApiKeyRepository;
import com.admin.backend.usage.payload.BudgetStatusResponse;
import com.admin.backend.usage.service.AIUsageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Base URL / path pattern (generateContent, streamGenerateContent) matches the standard
 * Generative Language API contract. Confirm the exact override flag for the Gemini CLI
 * version in use points here before wiring real traffic through it.
 */
@RestController
@RequestMapping("/gateway/gemini")
@RequiredArgsConstructor
public class GeminiProxyController {

    private static final Logger log = LoggerFactory.getLogger(GeminiProxyController.class);

    private final ApiKeyRepository apiKeyRepository;
    private final AIUsageService aiUsageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${gateway.gemini.upstream-base-url}")
    private String upstreamBaseUrl;

    @Value("${gateway.gemini.upstream-api-key}")
    private String upstreamApiKey;

    @PostMapping("/v1beta/models/{modelAndAction}")
    public ResponseEntity<StreamingResponseBody> proxyGenerateContent(
            @PathVariable String modelAndAction,
            @RequestParam(value = "key", required = false) String virtualKeyParam,
            @RequestHeader(value = "x-goog-api-key", required = false) String virtualKeyHeader,
            @RequestBody byte[] body) {

        String virtualKey = virtualKeyHeader != null ? virtualKeyHeader : virtualKeyParam;
        Optional<ApiKey> apiKey = virtualKey == null
                ? Optional.empty()
                : apiKeyRepository.findByKeyValueAndProvider(virtualKey, AiProvider.GEMINI);
        if (apiKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = apiKey.get().getUser();

        // Pre-flight Budget Guard
        BudgetStatusResponse budgetStatus = aiUsageService.getBudgetStatus(user.getTeam().getId());
        if (budgetStatus.isOverBudget()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Budget-Exceeded", "true")
                    .build();
        }
        String model = modelAndAction.contains(":") ? modelAndAction.split(":")[0] : modelAndAction;

        HttpRequest upstreamRequest = HttpRequest.newBuilder()
                .uri(URI.create(upstreamBaseUrl + "/v1beta/models/" + modelAndAction + "?key=" + upstreamApiKey))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<InputStream> upstreamResponse;
        try {
            upstreamResponse = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to reach Gemini upstream", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        StreamingResponseBody streamingBody = outputStream -> {
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            try (InputStream upstreamStream = upstreamResponse.body()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = upstreamStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                    outputStream.flush();
                    captured.write(buffer, 0, read);
                }
            } finally {
                recordUsage(user, model, captured.toByteArray());
            }
        };

        return ResponseEntity.status(upstreamResponse.statusCode())
                .contentType(MediaType.parseMediaType(
                        upstreamResponse.headers().firstValue("content-type").orElse("application/json")))
                .body(streamingBody);
    }

    private void recordUsage(User user, String model, byte[] responseBytes) {
        long promptTokens = 0;
        long completionTokens = 0;
        String text = new String(responseBytes, StandardCharsets.UTF_8);

        try {
            if (text.startsWith("data:")) {
                for (String line : text.split("\n")) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    JsonNode usage = objectMapper.readTree(line.substring(5).trim()).path("usageMetadata");
                    promptTokens = Math.max(promptTokens, usage.path("promptTokenCount").asLong());
                    completionTokens = Math.max(completionTokens, usage.path("candidatesTokenCount").asLong());
                }
            } else {
                JsonNode usage = objectMapper.readTree(responseBytes).path("usageMetadata");
                promptTokens = usage.path("promptTokenCount").asLong(0);
                completionTokens = usage.path("candidatesTokenCount").asLong(0);
            }
        } catch (IOException e) {
            log.warn("Failed to parse Gemini usage from response for user {}", user.getId(), e);
            return;
        }

        if (promptTokens == 0 && completionTokens == 0) {
            return;
        }
        aiUsageService.recordUsage(user, model, promptTokens, completionTokens);
    }
}
