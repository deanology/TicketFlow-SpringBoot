package com.idam.ticketflow.controller;

import com.idam.ticketflow.domain.enums.TicketStatus;
import com.idam.ticketflow.dto.request.TicketCreateRequest;
import com.idam.ticketflow.dto.request.TicketUpdateRequest;
import com.idam.ticketflow.dto.response.TicketResponse;
import com.idam.ticketflow.security.EmployeeUserDetails;
import com.idam.ticketflow.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketCreateRequest request, @AuthenticationPrincipal EmployeeUserDetails principal) {
        return ticketService.create(request, principal.getEmployee());
    }

    @GetMapping("/{id}")
    public TicketResponse findById(@PathVariable Long id) {
        return ticketService.findById(id);
    }

    // NOTE (BACKLOG.md #5): no explicit @PageableDefault sort here means the
    // ordering is whatever the DB happens to return, which in practice drifts
    // depending on how Postgres decides to walk the index. List views like
    // this should always pin a default sort (e.g. createdAt desc).
    @GetMapping
    public Page<TicketResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return ticketService.findAll(pageable);
    }

    @GetMapping(params = "status")
    public Page<TicketResponse> findByStatus(
            @RequestParam TicketStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ticketService.findByStatus(status, pageable);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable Long id, @RequestBody TicketUpdateRequest request) {
        return ticketService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public void delete(@PathVariable Long id) {
        ticketService.delete(id);
    }
}
