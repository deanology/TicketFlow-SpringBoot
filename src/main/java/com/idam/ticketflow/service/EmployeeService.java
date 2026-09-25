package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.domain.Employee;
import com.idam.ticketflow.dto.request.EmployeeCreateRequest;
import com.idam.ticketflow.dto.response.EmployeeResponse;
import com.idam.ticketflow.exception.DuplicateResourceException;
import com.idam.ticketflow.exception.ResourceNotFoundException;
import com.idam.ticketflow.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentService departmentService, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An employee with email '" + request.email() + "' already exists");
        }

        Department department = request.departmentId() != null
                ? departmentService.getEntityById(request.departmentId())
                : null;

        Employee employee = new Employee(
                request.fullName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.role(),
                department
        );

        return EmployeeResponse.fromEntity(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Employee getEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Employee", id));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return EmployeeResponse.fromEntity(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public Employee getEntityByEmail(String email) {
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("No employee found with email: " + email));
    }
}
