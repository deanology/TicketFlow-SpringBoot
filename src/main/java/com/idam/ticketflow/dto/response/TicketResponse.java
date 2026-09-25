package com.idam.ticketflow.dto.response;

import com.idam.ticketflow.domain.Ticket;
import com.idam.ticketflow.domain.enums.TicketPriority;
import com.idam.ticketflow.domain.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        DepartmentResponse department,
        EmployeeResponse createdBy,
        EmployeeResponse assignedTo,
        String aiSummary,
        TicketPriority aiSuggestedPriority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> comments
) {

    /** Summary view - used in paginated list endpoints (no comments loaded, avoids N+1). */
    public static TicketResponse summary(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                DepartmentResponse.fromEntity(ticket.getDepartment()),
                EmployeeResponse.fromEntity(ticket.getCreatedBy()),
                ticket.getAssignedTo() != null ? EmployeeResponse.fromEntity(ticket.getAssignedTo()) : null,
                ticket.getAiSummary(),
                ticket.getAiSuggestedPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                null
        );
    }

    /** Full detail view - includes comment thread. */
    public static TicketResponse detail(Ticket ticket) {
        List<CommentResponse> comments = ticket.getComments().stream()
                .map(CommentResponse::fromEntity)
                .toList();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                DepartmentResponse.fromEntity(ticket.getDepartment()),
                EmployeeResponse.fromEntity(ticket.getCreatedBy()),
                ticket.getAssignedTo() != null ? EmployeeResponse.fromEntity(ticket.getAssignedTo()) : null,
                ticket.getAiSummary(),
                ticket.getAiSuggestedPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                comments
        );
    }
}
