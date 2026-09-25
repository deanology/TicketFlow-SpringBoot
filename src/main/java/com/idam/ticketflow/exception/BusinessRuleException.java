package com.idam.ticketflow.exception;

/**
 * Thrown when a request is well-formed but violates a domain/business rule
 * (e.g. assigning a ticket to someone outside the department).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
