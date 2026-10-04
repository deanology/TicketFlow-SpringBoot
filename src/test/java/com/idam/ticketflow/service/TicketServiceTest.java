package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.Ticket;
import com.idam.ticketflow.domain.enums.Role;
import com.idam.ticketflow.domain.enums.TicketPriority;
import com.idam.ticketflow.dto.request.TicketCreateRequest;
import com.idam.ticketflow.dto.response.TicketResponse;
import com.idam.ticketflow.repository.TicketRepository;
import com.idam.ticketflow.service.ai.AiAnalysisResult;
import com.idam.ticketflow.service.ai.TicketAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TicketAiService ticketAiService;

    @InjectMocks
    private TicketService ticketService;

    private Department department;
    private Employee reporter;

    @BeforeEach
    void setUp() {
        department = new Department("Engineering");
        reporter = new Employee("Jane Reporter", "jane@ticketflow.dev", "hashed", Role.USER, department);
    }

    @Test
    void create_appliesAiAnalysisAndSetsPriorityFromSuggestion() {
        TicketCreateRequest request = new TicketCreateRequest("App is down", "Production app returning 500s", 1L);
        AiAnalysisResult analysis = new AiAnalysisResult("[App is down] Production app returning 500s", TicketPriority.CRITICAL);

        when(departmentService.getEntityById(1L)).thenReturn(department);
        when(ticketAiService.analyze(request.title(), request.description())).thenReturn(analysis);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = ticketService.create(request, reporter);

        assertThat(response.priority()).isEqualTo(TicketPriority.CRITICAL);
        assertThat(response.aiSuggestedPriority()).isEqualTo(TicketPriority.CRITICAL);
        assertThat(response.aiSummary()).isEqualTo(analysis.summary());
        verify(ticketAiService).analyze(request.title(), request.description());
    }


    @Test
    void sortByUrgencyDescending_returnsMostUrgentFirst() {
        Ticket low = ticketWithPriority(TicketPriority.LOW);
        Ticket critical = ticketWithPriority(TicketPriority.CRITICAL);
        Ticket medium = ticketWithPriority(TicketPriority.MEDIUM);

        List<Ticket> sorted = ticketService.sortByUrgencyDescending(List.of(low, critical, medium));

        assertThat(sorted).extracting(Ticket::getPriority)
                .containsExactly(TicketPriority.CRITICAL, TicketPriority.MEDIUM, TicketPriority.LOW);
    }

    private Ticket ticketWithPriority(TicketPriority priority) {
        Ticket ticket = new Ticket("t", "d", department, reporter);
        ticket.setPriority(priority);
        return ticket;
    }
}
