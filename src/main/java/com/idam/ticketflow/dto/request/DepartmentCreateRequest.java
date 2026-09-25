package com.idam.ticketflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DepartmentCreateRequest(
        @NotBlank String name
) {
}
