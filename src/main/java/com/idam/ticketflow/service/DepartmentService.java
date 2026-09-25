package com.idam.ticketflow.service;

import com.idam.ticketflow.domain.Department;
import com.idam.ticketflow.dto.request.DepartmentCreateRequest;
import com.idam.ticketflow.dto.response.DepartmentResponse;
import com.idam.ticketflow.exception.DuplicateResourceException;
import com.idam.ticketflow.exception.ResourceNotFoundException;
import com.idam.ticketflow.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
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

    /**
     * KNOWN BUG (see BACKLOG.md item #1): this blindly deletes the department
     * even if employees still reference it, which violates the FK constraint
     * in production and throws an unhelpful DataIntegrityViolationException
     * instead of a clean 409. Needs a check (or a reassignment step) before
     * deleting.
     */
    @Transactional
    public void delete(Long id) {
        Department department = getEntityById(id);
        departmentRepository.delete(department);
    }
}
