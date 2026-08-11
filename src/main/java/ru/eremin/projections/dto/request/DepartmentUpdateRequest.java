package ru.eremin.projections.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentUpdateRequest(

        @NotBlank
        @Size(max = 255)
        String name

) {
}
