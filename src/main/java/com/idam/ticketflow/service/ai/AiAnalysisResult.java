package com.idam.ticketflow.service.ai;

import com.idam.ticketflow.domain.enums.TicketPriority;

public record AiAnalysisResult(
        String summary,
        TicketPriority suggestedPriority
) {
}
