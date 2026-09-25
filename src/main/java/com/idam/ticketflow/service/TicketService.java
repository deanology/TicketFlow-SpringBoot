package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.Ticket;
import com.idam.ticketflow.domain.enums.TicketPriority;
import com.idam.ticketflow.domain.enums.TicketStatus;
import com.idam.ticketflow.dto.request.TicketCreateRequest;
import com.idam.ticketflow.dto.request.TicketUpdateRequest;
import com.idam.ticketflow.dto.response.TicketResponse;
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
        Ticket ticket = getEntityById(id);

        if (request.title() != null) {
            ticket.setTitle(request.title());
        }

        // KNOWN BUG (see BACKLOG.md item #3): updating the description does NOT
        // re-run AI analysis or clear the old aiSummary/aiSuggestedPriority, so
        // the AI fields silently go stale and no longer describe the current
        // ticket text. Either re-analyze here, or clear the fields and mark the
        // ticket as "needs re-analysis".
        if (request.description() != null) {
            ticket.setDescription(request.description());
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

    /**
     * KNOWN BUG (see BACKLOG.md item #2): this does not check that the
     * assignee actually belongs to the ticket's department, so an HR employee
     * can be assigned an Engineering ticket. Add that validation (throw
     * BusinessRuleException on mismatch) once the rule is confirmed with the
     * team - some orgs deliberately allow cross-department assignment for
     * generalist agents, so check before "fixing" this.
     */
    private void assignTicket(Ticket ticket, Long assigneeId) {
        Employee assignee = employeeService.getEntityById(assigneeId);
        ticket.setAssignedTo(assignee);
    }

    /**
     * KNOWN BUG (see BACKLOG.md item #4): TicketPriority.compareTo() sorts by
     * declaration order (LOW < MEDIUM < HIGH < CRITICAL), so a plain
     * Comparator.comparing(Ticket::getPriority) sorts *ascending* by urgency.
     * This method is meant to return most-urgent-first but currently doesn't
     * reverse the comparator, so CRITICAL tickets end up last, not first.
     */
    public List<Ticket> sortByUrgencyDescending(List<Ticket> tickets) {
        return tickets.stream()
                .sorted(Comparator.comparing(Ticket::getPriority))
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = getEntityById(id);
        ticketRepository.delete(ticket);
    }
}
