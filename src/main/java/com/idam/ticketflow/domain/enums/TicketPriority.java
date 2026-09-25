package com.idam.ticketflow.domain.enums;

/**
 * NOTE: enum declaration order is relied on by ordinal-based sorting
 * elsewhere in the codebase (see TicketService#sortByUrgencyDescending).
 * Keep this ordered least-to-most urgent, or fix that sort to not use
 * ordinal() at all. See BACKLOG.md item #4.
 */
public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
