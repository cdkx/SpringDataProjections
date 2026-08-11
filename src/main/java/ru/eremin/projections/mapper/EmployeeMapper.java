package ru.eremin.projections.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.eremin.projections.dto.request.EmployeeCreateRequest;
import ru.eremin.projections.dto.request.EmployeeUpdateRequest;
import ru.eremin.projections.dto.response.EmployeeDetailsResponse;
import ru.eremin.projections.dto.response.EmployeeProjectionResponse;
import ru.eremin.projections.model.Employee;
import ru.eremin.projections.projection.EmployeeProjection;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;


@Mapper(componentModel = SPRING)
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    Employee toEntity(EmployeeCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    void updateEntity(@MappingTarget Employee employee, EmployeeUpdateRequest request);

    @Mapping(target = "departmentName", source = "department.name")
    EmployeeDetailsResponse toDetailsResponse(Employee employee);

    List<EmployeeDetailsResponse> toDetailsResponseList(List<Employee> employees);

    EmployeeProjectionResponse toProjectionResponse(EmployeeProjection projection);

    List<EmployeeProjectionResponse> toProjectionResponseList(List<EmployeeProjection> projections);
}
