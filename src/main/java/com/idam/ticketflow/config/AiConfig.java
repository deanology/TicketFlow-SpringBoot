package com.idam.ticketflow.config;

import com.idam.ticketflow.service.ai.MockTicketAiService;
import com.idam.ticketflow.service.ai.OpenAiTicketAiService;
import com.idam.ticketflow.service.ai.TicketAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the active AI provider. Controlled by ticketflow.ai.provider (mock | openai).
 * Defaults to "mock" so the app runs fully offline out of the box - flip to "openai"
 * once OPENAI_API_KEY is set. See BACKLOG.md item #6 for wiring a real provider.
 */
@Configuration
public class AiConfig {

    @Bean
    public TicketAiService ticketAiService(
            @Value("${ticketflow.ai.provider:mock}") String provider,
            MockTicketAiService mockTicketAiService,
            OpenAiTicketAiService openAiTicketAiService
    ) {
        if ("openai".equalsIgnoreCase(provider)) {
            return openAiTicketAiService;
        }
        return mockTicketAiService;
    }
}
