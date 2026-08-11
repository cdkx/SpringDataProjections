package ru.eremin.projections.service;

import ru.eremin.projections.dto.request.DepartmentCreateRequest;
import ru.eremin.projections.dto.request.DepartmentUpdateRequest;
import ru.eremin.projections.dto.response.DepartmentResponse;

import java.util.List;


public interface DepartmentService {

    DepartmentResponse create(DepartmentCreateRequest request);

    List<DepartmentResponse> findAll();

    DepartmentResponse findById(Long id);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);

    void delete(Long id);
}
