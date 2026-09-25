package com.idam.ticketflow.service.ai;

import com.idam.ticketflow.domain.enums.TicketPriority;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockTicketAiServiceTest {

    private final MockTicketAiService aiService = new MockTicketAiService();

    @Test
    void analyze_flagsProductionOutageAsCritical() {
        AiAnalysisResult result = aiService.analyze(
                "Production login endpoint returning 500",
                "Users are reporting intermittent 500 errors since this morning's deploy.");

        assertThat(result.suggestedPriority()).isEqualTo(TicketPriority.CRITICAL);
    }

    @Test
    void analyze_flagsAccessRequestAsLow() {
        AiAnalysisResult result = aiService.analyze(
                "Request access to shared drive",
                "Need read/write access to the shared design drive.");

        assertThat(result.suggestedPriority()).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void analyze_defaultsToMediumWhenNoKeywordsMatch() {
        AiAnalysisResult result = aiService.analyze(
                "New laptop setup",
                "Please set up a new laptop for the incoming hire starting next week.");

        assertThat(result.suggestedPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void analyze_summaryIncludesTitleAndFirstSentence() {
        AiAnalysisResult result = aiService.analyze(
                "VPN issue",
                "VPN keeps dropping every ten minutes. It started after the update.");

        assertThat(result.summary()).contains("VPN issue").contains("VPN keeps dropping every ten minutes.");
    }
}
