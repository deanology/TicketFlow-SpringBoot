package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Comment;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.Ticket;
import com.idam.ticketflow.dto.request.CommentCreateRequest;
import com.idam.ticketflow.dto.response.CommentResponse;
import com.idam.ticketflow.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final TicketService ticketService;
    private final EmployeeService employeeService;
    private final CommentRepository commentRepository;

    public CommentService(TicketService ticketService, EmployeeService employeeService, CommentRepository commentRepository) {
        this.ticketService = ticketService;
        this.employeeService = employeeService;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CommentCreateRequest request, Employee author) {
        Ticket ticket = ticketService.getEntityById(ticketId);

        // See TicketService.create for why we re-fetch a managed instance
        // rather than using the security-principal Employee directly.
        Employee managedAuthor = employeeService.getEntityById(author.getId());

        Comment comment = new Comment(managedAuthor, request.content());
        ticket.addComment(comment);

        // Explicit save (rather than relying on cascade-on-flush) so the
        // IDENTITY-generated id is assigned synchronously, before we build
        // the response DTO below - cascade alone wouldn't populate it until
        // the transaction commits, which happens after this method returns.
        Comment saved = commentRepository.save(comment);

        return CommentResponse.fromEntity(saved);
    }
}
