package com.idam.ticketflow.service.ai;

import com.idam.ticketflow.domain.enums.TicketPriority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Deterministic, offline stand-in for a real AI provider. Good enough to
 * demo the end-to-end flow and to unit test against without an API key.
 *
 * Keyword lists are intentionally simple - a real implementation would call
 * an LLM (see OpenAiTicketAiService) with the ticket text and parse a
 * structured response. This is exactly the "AI agent classifies severity"
 * pattern from the log-analyzer project, applied to tickets.
 */
@Service
public class MockTicketAiService implements TicketAiService {

    private static final List<String> CRITICAL_KEYWORDS =
            List.of("outage", "down", "production", "security breach", "data loss", "cannot log in", "500 error");

    private static final List<String> HIGH_KEYWORDS =
            List.of("error", "failing", "broken", "urgent", "blocked", "crash");

    private static final List<String> LOW_KEYWORDS =
            List.of("request access", "question", "how do i", "minor", "cosmetic");

    @Override
    public AiAnalysisResult analyze(String title, String description) {
        String text = (title + " " + description).toLowerCase(Locale.ROOT);

        TicketPriority suggested = TicketPriority.MEDIUM;
        if (containsAny(text, CRITICAL_KEYWORDS)) {
            suggested = TicketPriority.CRITICAL;
        } else if (containsAny(text, HIGH_KEYWORDS)) {
            suggested = TicketPriority.HIGH;
        } else if (containsAny(text, LOW_KEYWORDS)) {
            suggested = TicketPriority.LOW;
        }

        String summary = buildSummary(title, description);
        return new AiAnalysisResult(summary, suggested);
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private String buildSummary(String title, String description) {
        String trimmed = description.strip();
        String firstSentence = trimmed.split("(?<=[.!?])\\s+")[0];
        if (firstSentence.length() > 160) {
            firstSentence = firstSentence.substring(0, 157) + "...";
        }
        return "[" + title.strip() + "] " + firstSentence;
    }
}
