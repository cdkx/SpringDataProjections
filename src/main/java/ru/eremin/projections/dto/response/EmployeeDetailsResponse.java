package ru.eremin.projections.dto.response;

import java.math.BigDecimal;

public record EmployeeDetailsResponse(
        Long id,
        String firstName,
        String lastName,
        String position,
        BigDecimal salary,
        String departmentName
) {
}
