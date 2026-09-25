package com.idam.ticketflow.controller;

import com.idam.ticketflow.dto.request.DepartmentCreateRequest;
import com.idam.ticketflow.dto.response.DepartmentResponse;
import com.idam.ticketflow.config.SecurityConfig;
import com.idam.ticketflow.exception.DuplicateResourceException;
import com.idam.ticketflow.security.EmployeeUserDetailsService;
import com.idam.ticketflow.security.JwtService;
import com.idam.ticketflow.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    // Security beans are needed on the context even though this slice
    // doesn't exercise real auth - WithMockUser stands in for a logged-in user.
    @MockBean
    private JwtService jwtService;
    @MockBean
    private EmployeeUserDetailsService employeeUserDetailsService;

    @Test
    @WithMockUser
    void findAll_returnsDepartmentsFromService() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(
                new DepartmentResponse(1L, "IT Support"),
                new DepartmentResponse(2L, "Engineering")
        ));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("IT Support"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns201WhenValid() throws Exception {
        when(departmentService.create(any())).thenReturn(new DepartmentResponse(3L, "Finance"));

        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DepartmentCreateRequest("Finance")))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Finance"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DepartmentCreateRequest("Finance")))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns409WhenDuplicateName() throws Exception {
        when(departmentService.create(any())).thenThrow(new DuplicateResourceException("A department named 'IT Support' already exists"));

        mockMvc.perform(post("/api/departments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DepartmentCreateRequest("IT Support")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
