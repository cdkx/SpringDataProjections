package ru.eremin.projections.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eremin.projections.dto.request.DepartmentCreateRequest;
import ru.eremin.projections.dto.request.DepartmentUpdateRequest;
import ru.eremin.projections.dto.response.DepartmentResponse;
import ru.eremin.projections.exception.ConflictException;
import ru.eremin.projections.exception.NotFoundException;
import ru.eremin.projections.mapper.DepartmentMapper;
import ru.eremin.projections.model.Department;
import ru.eremin.projections.repository.DepartmentRepository;
import ru.eremin.projections.repository.EmployeeRepository;
import ru.eremin.projections.service.DepartmentService;

import java.util.List;


@Service
@AllArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);

        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentMapper.toResponseList(departmentRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        Department department = getDepartmentEntity(id);
        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {
        Department department = getDepartmentEntity(id);

        departmentMapper.update(department, request);

        Department saved = departmentRepository.save(department);

        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = getDepartmentEntity(id);

        long employeeCount = employeeRepository.countByDepartmentId(id);

        if (employeeCount > 0) {
            throw new ConflictException("Невозможно удалить отдел с id = " + id + ", так как в нём есть сотрудники");
        }

        departmentRepository.delete(department);
    }

    private Department getDepartmentEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }
}
