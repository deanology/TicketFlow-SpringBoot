package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.dto.request.LoginRequest;
import com.idam.ticketflow.dto.response.AuthResponse;
import com.idam.ticketflow.dto.response.EmployeeResponse;
import com.idam.ticketflow.security.EmployeeUserDetails;
import com.idam.ticketflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployeeService employeeService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, EmployeeService employeeService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.employeeService = employeeService;
    }

    // Transactional so the Employee stays managed for the EmployeeResponse
    // mapping below - without this, its lazy `department` association would
    // throw LazyInitializationException once getEntityByEmail's own
    // (shorter-lived) transaction has already closed.
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Employee employee = employeeService.getEntityByEmail(request.email());
        String token = jwtService.generateToken(new EmployeeUserDetails(employee));

        return new AuthResponse(token, EmployeeResponse.fromEntity(employee));
    }
}
