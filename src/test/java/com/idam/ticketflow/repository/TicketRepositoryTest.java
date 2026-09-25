package com.idam.ticketflow.repository;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.Ticket;
import com.idam.ticketflow.domain.enums.Role;
import com.idam.ticketflow.domain.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department department;
    private Employee reporter;

    @BeforeEach
    void setUp() {
        department = departmentRepository.save(new Department("Engineering"));
        reporter = employeeRepository.save(
                new Employee("Jane Reporter", "jane@ticketflow.dev", "hashed", Role.USER, department));
    }

    @Test
    void savesAndLoadsTicketWithDefaults() {
        Ticket ticket = new Ticket("Printer not working", "The 3rd floor printer is jammed.", department, reporter);

        Ticket saved = ticketRepository.save(ticket);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByStatus_returnsOnlyMatchingTickets() {
        Ticket open = new Ticket("Open one", "desc", department, reporter);
        Ticket closed = new Ticket("Closed one", "desc", department, reporter);
        closed.setStatus(TicketStatus.CLOSED);

        ticketRepository.save(open);
        ticketRepository.save(closed);

        var page = ticketRepository.findByStatus(TicketStatus.OPEN, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Open one");
    }

    @Test
    void findByDepartmentId_returnsTicketsForThatDepartmentOnly() {
        Department otherDept = departmentRepository.save(new Department("HR"));

        ticketRepository.save(new Ticket("Eng ticket", "desc", department, reporter));
        ticketRepository.save(new Ticket("HR ticket", "desc", otherDept, reporter));

        var page = ticketRepository.findByDepartmentId(department.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Eng ticket");
    }
}
