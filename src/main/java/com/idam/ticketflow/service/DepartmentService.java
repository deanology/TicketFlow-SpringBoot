package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.dto.request.DepartmentCreateRequest;
import com.idam.ticketflow.dto.response.DepartmentResponse;
import com.idam.ticketflow.exception.BusinessRuleException;
import com.idam.ticketflow.exception.DuplicateResourceException;
import com.idam.ticketflow.exception.ResourceNotFoundException;
import com.idam.ticketflow.repository.DepartmentRepository;
import com.idam.ticketflow.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        departmentRepository.findByNameIgnoreCase(request.name()).ifPresent(d -> {
            throw new DuplicateResourceException("A department named '" + request.name() + "' already exists");
        });

        Department saved = departmentRepository.save(new Department(request.name()));
        return DepartmentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Department getEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Department", id));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return DepartmentResponse.fromEntity(getEntityById(id));
    }

    @Transactional
    public void delete(Long id) {
        // no null check needed. If the department does not exist, the exception is thrown immediately
        Department department = getEntityById(id);

        //check for existing employee before deleting
        if (employeeRepository.existsByDepartmentId(department.getId())) {
            throw new BusinessRuleException(
                    "Cannot delete department '%s' - it still has employees assigned"
                            .formatted(department.getName()));
        }

        departmentRepository.delete(department);
    }
}
