package ru.eremin.projections.service;

import ru.eremin.projections.dto.request.EmployeeCreateRequest;
import ru.eremin.projections.dto.request.EmployeeUpdateRequest;
import ru.eremin.projections.dto.response.EmployeeDetailsResponse;
import ru.eremin.projections.dto.response.EmployeeProjectionResponse;

import java.util.List;


public interface EmployeeService {

    EmployeeDetailsResponse create(EmployeeCreateRequest request);

    EmployeeDetailsResponse update(Long id, EmployeeUpdateRequest request);

    void delete(Long id);

    List<EmployeeDetailsResponse> findAll();

    EmployeeDetailsResponse findById(Long id);

    List<EmployeeProjectionResponse> findAllProjection();

    EmployeeProjectionResponse findProjectionById(Long id);

    List<EmployeeProjectionResponse> findProjectionByDepartmentId(Long departmentId);
}
