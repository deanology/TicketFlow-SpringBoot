package com.idam.ticketflow.dto.response;

import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.domain.enums.Role;

public record EmployeeResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        DepartmentResponse department
) {

    public static EmployeeResponse fromEntity(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getRole(),
                employee.getDepartment() != null ? DepartmentResponse.fromEntity(employee.getDepartment()) : null
        );
    }
}
