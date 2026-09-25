package com.idam.ticketflow.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idam.ticketflow.domain.enums.TicketPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Real AI provider, backed by the OpenAI chat completions API. Only wired
 * in when ticketflow.ai.provider=openai (see AiConfig) and requires
 * OPENAI_API_KEY to be set.
 *
 * NOT YET COVERED BY TESTS - see BACKLOG.md item #6. Treat this as a
 * starting point: no retry/backoff, no timeout tuning, and errors currently
 * just propagate as a generic RuntimeException.
 */
@Service
public class OpenAiTicketAiService implements TicketAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiTicketAiService.class);
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiTicketAiService(
            @Value("${ticketflow.ai.openai.api-key:}") String apiKey,
            @Value("${ticketflow.ai.openai.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.create();
    }

    @Override
    public AiAnalysisResult analyze(String title, String description) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not set but ticketflow.ai.provider=openai. " +
                    "Set the env var or switch AI_PROVIDER back to 'mock'.");
        }

        String prompt = """
                You triage IT support tickets. Given the title and description below,
                respond with ONLY a JSON object (no markdown fences) of the shape:
                {"summary": "<one sentence summary>", "priority": "LOW|MEDIUM|HIGH|CRITICAL"}

                Title: %s
                Description: %s
                """.formatted(title, description);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.2
        );

        try {
            String response = restClient.post()
                    .uri(ENDPOINT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseResponse(response);
        } catch (Exception ex) {
            log.error("OpenAI ticket analysis failed", ex);
            throw new RuntimeException("AI analysis failed: " + ex.getMessage(), ex);
        }
    }

    private AiAnalysisResult parseResponse(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        JsonNode parsed = objectMapper.readTree(content);

        String summary = parsed.path("summary").asText("");
        String priorityText = parsed.path("priority").asText("MEDIUM");

        TicketPriority priority;
        try {
            priority = TicketPriority.valueOf(priorityText.toUpperCase());
        } catch (IllegalArgumentException ex) {
            priority = TicketPriority.MEDIUM;
        }

        return new AiAnalysisResult(summary, priority);
    }
}
