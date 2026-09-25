package com.idam.ticketflow.repository;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void findByEmailIgnoreCase_matchesRegardlessOfCase() {
        Department dept = departmentRepository.save(new Department("Support"));
        employeeRepository.save(new Employee("Sam Support", "Sam@TicketFlow.dev", "hashed", Role.AGENT, dept));

        var found = employeeRepository.findByEmailIgnoreCase("sam@ticketflow.dev");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Sam Support");
    }

    @Test
    void existsByEmailIgnoreCase_isFalseForUnknownEmail() {
        assertThat(employeeRepository.existsByEmailIgnoreCase("nobody@ticketflow.dev")).isFalse();
    }
}
