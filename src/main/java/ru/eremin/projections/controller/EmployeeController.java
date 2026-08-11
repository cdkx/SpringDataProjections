package ru.eremin.projections.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.eremin.projections.dto.request.EmployeeCreateRequest;
import ru.eremin.projections.dto.request.EmployeeUpdateRequest;
import ru.eremin.projections.dto.response.EmployeeDetailsResponse;
import ru.eremin.projections.dto.response.EmployeeProjectionResponse;
import ru.eremin.projections.service.EmployeeService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/employees")
@AllArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDetailsResponse> create(
            @Valid @RequestBody EmployeeCreateRequest request
    ) {
        EmployeeDetailsResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<EmployeeDetailsResponse> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeDetailsResponse findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @GetMapping("/projections")
    public List<EmployeeProjectionResponse> findAllProjection() {
        return employeeService.findAllProjection();
    }

    @GetMapping("/projections/{id}")
    public EmployeeProjectionResponse findProjectionById(@PathVariable Long id) {
        return employeeService.findProjectionById(id);
    }

    @GetMapping("/projections/by-department/{departmentId}")
    public List<EmployeeProjectionResponse> findProjectionByDepartmentId(
            @PathVariable Long departmentId
    ) {
        return employeeService.findProjectionByDepartmentId(departmentId);
    }

    @PutMapping("/{id}")
    public EmployeeDetailsResponse update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}
