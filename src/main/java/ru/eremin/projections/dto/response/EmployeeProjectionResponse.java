package ru.eremin.projections.dto.response;

public record EmployeeProjectionResponse(
        String fullName,
        String position,
        String departmentName
) {
}
