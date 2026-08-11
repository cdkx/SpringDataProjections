package ru.eremin.projections.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.eremin.projections.dto.request.DepartmentCreateRequest;
import ru.eremin.projections.dto.request.DepartmentUpdateRequest;
import ru.eremin.projections.dto.response.DepartmentResponse;
import ru.eremin.projections.model.Department;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;


@Mapper(componentModel = SPRING)
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    Department toEntity(DepartmentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Department department, DepartmentUpdateRequest request);

    DepartmentResponse toResponse(Department department);

    List<DepartmentResponse> toResponseList(List<Department> departments);
}
