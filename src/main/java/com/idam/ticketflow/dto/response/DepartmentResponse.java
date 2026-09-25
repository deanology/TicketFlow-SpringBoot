package com.idam.ticketflow.dto.response;

import com.idam.ticketflow.domain.Department;

public record DepartmentResponse(Long id, String name) {

    public static DepartmentResponse fromEntity(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
