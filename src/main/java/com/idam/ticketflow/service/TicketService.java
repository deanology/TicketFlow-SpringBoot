package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.Ticket;
import com.idam.ticketflow.domain.enums.TicketPriority;
import com.idam.ticketflow.domain.enums.TicketStatus;
import com.idam.ticketflow.dto.request.TicketCreateRequest;
import com.idam.ticketflow.dto.request.TicketUpdateRequest;
import com.idam.ticketflow.dto.response.TicketResponse;
import com.idam.ticketflow.exception.BusinessRuleException;
import com.idam.ticketflow.exception.ResourceNotFoundException;
import com.idam.ticketflow.repository.TicketRepository;
import com.idam.ticketflow.service.ai.AiAnalysisResult;
import com.idam.ticketflow.service.ai.TicketAiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final TicketAiService ticketAiService;

    public TicketService(
            TicketRepository ticketRepository,
            DepartmentService departmentService,
            EmployeeService employeeService,
            TicketAiService ticketAiService
    ) {
        this.ticketRepository = ticketRepository;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.ticketAiService = ticketAiService;
    }

    @Transactional
    public TicketResponse create(TicketCreateRequest request, Employee createdBy) {
        Department department = departmentService.getEntityById(request.departmentId());

        // createdBy comes from the security principal, which was loaded in a
        // separate (already-closed) session during JWT authentication. Re-fetch
        // it here so it's a managed entity in *this* transaction - otherwise
        // mapping to a response DTO that touches its lazy department
        // association throws LazyInitializationException.
        Employee managedCreator = employeeService.getEntityById(createdBy.getId());

        Ticket ticket = new Ticket(request.title(), request.description(), department, managedCreator);

        // Run AI triage synchronously on creation. Fine for a mock/local model;
        // for a real remote provider this should move to an async job so ticket
        // creation doesn't block on a third-party API call. See BACKLOG.md #7.
        AiAnalysisResult analysis = ticketAiService.analyze(request.title(), request.description());
        ticket.applyAiAnalysis(analysis.summary(), analysis.suggestedPriority());
        ticket.setPriority(analysis.suggestedPriority());

        return TicketResponse.detail(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {
        return TicketResponse.detail(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public Ticket getEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Ticket", id));
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> findByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(TicketResponse::summary);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(TicketResponse::summary);
    }

    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request) {
        boolean needsReanalysis = false;

        Ticket ticket = getEntityById(id);

        if (request.title() != null && !request.title().equals(ticket.getTitle())) {
            ticket.setTitle(request.title());
            needsReanalysis = true;
        }

        if (request.description() != null && !request.description().equals(ticket.getDescription())) {
            ticket.setDescription(request.description());
            needsReanalysis = true;
        }

        if (needsReanalysis) {
            AiAnalysisResult analysis = ticketAiService.analyze(ticket.getTitle(), ticket.getDescription());
            ticket.applyAiAnalysis(analysis.summary(), analysis.suggestedPriority());
        }

        if (request.status() != null) {
            ticket.setStatus(request.status());
        }

        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }

        if (request.assignedToId() != null) {
            assignTicket(ticket, request.assignedToId());
        }

        return TicketResponse.detail(ticket);
    }

    private void assignTicket(Ticket ticket, Long assigneeId)  {
        Employee assignee = employeeService.getEntityById(assigneeId);
        Department assigneeDepartment = assignee.getDepartment();

        if (assigneeDepartment == null) {
            throw new BusinessRuleException(
                    "Cannot assign ticket to '%s' - they are not part of any department"
                            .formatted(assignee.getFullName()));
        }

        if (!assigneeDepartment.getId().equals(ticket.getDepartment().getId())) {
            throw new BusinessRuleException(
                    "Cannot assign ticket to '%s' - they belong to '%s' but the ticket belongs to '%s'"
                            .formatted(assignee.getFullName(),
                                    assigneeDepartment.getName(),
                                    ticket.getDepartment().getName()));
        }

        ticket.setAssignedTo(assignee);
    }

    /**
     * Returns tickets ordered most-urgent-first (CRITICAL, HIGH, MEDIUM, LOW).
     */
    public List<Ticket> sortByUrgencyDescending(List<Ticket> tickets) {
        return tickets.stream()
                .sorted(Comparator.comparing(Ticket::getPriority).reversed()
                .thenComparing(Ticket::getCreatedAt, Comparator.reverseOrder()))
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = getEntityById(id);
        ticketRepository.delete(ticket);
    }
}
