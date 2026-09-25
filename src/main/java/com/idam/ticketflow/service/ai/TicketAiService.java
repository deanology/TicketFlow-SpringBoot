package com.idam.ticketflow.service.ai;

/**
 * Abstraction over "whatever analyzes ticket text". Swap implementations
 * (mock, OpenAI, a local model, ...) without touching TicketService.
 */
public interface TicketAiService {
    AiAnalysisResult analyze(String title, String description);
}
