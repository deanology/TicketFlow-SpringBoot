package com.idam.ticketflow.dto.request;

import com.idam.ticketflow.domain.enums.TicketPriority;
import com.idam.ticketflow.domain.enums.TicketStatus;

/**
 * All fields optional/nullable - only non-null fields are applied (partial update).
 */
public record TicketUpdateRequest(
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Long assignedToId
) {
}
