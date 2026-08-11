package ru.eremin.projections.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eremin.projections.dto.request.EmployeeCreateRequest;
import ru.eremin.projections.dto.request.EmployeeUpdateRequest;
import ru.eremin.projections.dto.response.EmployeeDetailsResponse;
import ru.eremin.projections.dto.response.EmployeeProjectionResponse;
import ru.eremin.projections.exception.NotFoundException;
import ru.eremin.projections.mapper.EmployeeMapper;
import ru.eremin.projections.model.Department;
import ru.eremin.projections.model.Employee;
import ru.eremin.projections.projection.EmployeeProjection;
import ru.eremin.projections.repository.DepartmentRepository;
import ru.eremin.projections.repository.EmployeeRepository;
import ru.eremin.projections.service.EmployeeService;

import java.util.List;


@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeDetailsResponse create(EmployeeCreateRequest request) {
        Department department = getDepartmentEntity(request.departmentId());

        Employee employee = employeeMapper.toEntity(request);
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toDetailsResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeDetailsResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = getEmployeeEntityWithDepartment(id);

        employeeMapper.updateEntity(employee, request);

        Department department = getDepartmentEntity(request.departmentId());
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toDetailsResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new NotFoundException("Employee not found: " + id);
        }

        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDetailsResponse> findAll() {
        return employeeMapper.toDetailsResponseList(employeeRepository.findAllWithDepartment());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDetailsResponse findById(Long id) {
        Employee employee = getEmployeeEntityWithDepartment(id);
        return employeeMapper.toDetailsResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeProjectionResponse> findAllProjection() {
        return employeeMapper.toProjectionResponseList(employeeRepository.findAllProjected());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeProjectionResponse findProjectionById(Long id) {
        return employeeRepository.findProjectedById(id)
                .map(employeeMapper::toProjectionResponse)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeProjectionResponse> findProjectionByDepartmentId(Long departmentId) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new NotFoundException("Department not found: " + departmentId);
        }

        List<EmployeeProjection> projections = employeeRepository.findAllProjectedByDepartmentId(departmentId);
        return employeeMapper.toProjectionResponseList(projections);
    }

    private Employee getEmployeeEntityWithDepartment(Long id) {
        return employeeRepository.findWithDepartmentById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
    }

    private Department getDepartmentEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }
}
