package ru.eremin.projections.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.eremin.projections.dto.request.EmployeeCreateRequest;
import ru.eremin.projections.dto.request.EmployeeUpdateRequest;
import ru.eremin.projections.dto.response.EmployeeDetailsResponse;
import ru.eremin.projections.dto.response.EmployeeProjectionResponse;
import ru.eremin.projections.exception.GlobalExceptionHandler;
import ru.eremin.projections.exception.NotFoundException;
import ru.eremin.projections.service.EmployeeService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST /api/v1/employees должен создавать сотрудника и возвращать 201")
    void createShouldReturnCreatedStatus() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                1L
        );

        EmployeeDetailsResponse response = new EmployeeDetailsResponse(
                1L,
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                "IT"
        );

        when(employeeService.create(any(EmployeeCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.position").value("Java Developer"))
                .andExpect(jsonPath("$.salary").value(180000.00))
                .andExpect(jsonPath("$.departmentName").value("IT"));

        verify(employeeService).create(any(EmployeeCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/employees должен возвращать 400, если firstName пустое")
    void createShouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                " ",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                1L
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("POST /api/v1/employees должен возвращать 400, если salary отрицательная")
    void createShouldReturnBadRequestWhenSalaryIsNegative() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("-1"),
                1L
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("POST /api/v1/employees должен возвращать 400, если departmentId null")
    void createShouldReturnBadRequestWhenDepartmentIdIsNull() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                null
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("GET /api/v1/employees должен возвращать список сотрудников")
    void findAllShouldReturnEmployeesList() throws Exception {
        List<EmployeeDetailsResponse> employees = List.of(
                new EmployeeDetailsResponse(
                        1L,
                        "Иван",
                        "Петров",
                        "Java Developer",
                        new BigDecimal("180000.00"),
                        "IT"
                ),
                new EmployeeDetailsResponse(
                        2L,
                        "Мария",
                        "Сидорова",
                        "DevOps Engineer",
                        new BigDecimal("200000.00"),
                        "IT"
                )
        );

        when(employeeService.findAll()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employees")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Иван"))
                .andExpect(jsonPath("$[0].lastName").value("Петров"))
                .andExpect(jsonPath("$[0].position").value("Java Developer"))
                .andExpect(jsonPath("$[0].departmentName").value("IT"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Мария"))
                .andExpect(jsonPath("$[1].lastName").value("Сидорова"))
                .andExpect(jsonPath("$[1].position").value("DevOps Engineer"))
                .andExpect(jsonPath("$[1].departmentName").value("IT"));

        verify(employeeService).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} должен возвращать сотрудника по id")
    void findByIdShouldReturnEmployee() throws Exception {
        EmployeeDetailsResponse response = new EmployeeDetailsResponse(
                1L,
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                "IT"
        );

        when(employeeService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/employees/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.position").value("Java Developer"))
                .andExpect(jsonPath("$.departmentName").value("IT"));

        verify(employeeService).findById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} должен возвращать 404, если сотрудник не найден")
    void findByIdShouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.findById(99L))
                .thenThrow(new NotFoundException("Employee not found: 99"));

        mockMvc.perform(get("/api/v1/employees/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService).findById(99L);
    }

    @Test
    @DisplayName("GET /api/v1/employees/projections должен возвращать список проекций сотрудников")
    void findAllProjectionShouldReturnProjectionList() throws Exception {
        List<EmployeeProjectionResponse> projections = List.of(
                new EmployeeProjectionResponse(
                        "Иван Петров",
                        "Java Developer",
                        "IT"
                ),
                new EmployeeProjectionResponse(
                        "Мария Сидорова",
                        "DevOps Engineer",
                        "IT"
                )
        );

        when(employeeService.findAllProjection()).thenReturn(projections);

        mockMvc.perform(get("/api/v1/employees/projections")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Иван Петров"))
                .andExpect(jsonPath("$[0].position").value("Java Developer"))
                .andExpect(jsonPath("$[0].departmentName").value("IT"))
                .andExpect(jsonPath("$[1].fullName").value("Мария Сидорова"))
                .andExpect(jsonPath("$[1].position").value("DevOps Engineer"))
                .andExpect(jsonPath("$[1].departmentName").value("IT"));

        verify(employeeService).findAllProjection();
    }

    @Test
    @DisplayName("GET /api/v1/employees/projections/{id} должен возвращать проекцию сотрудника")
    void findProjectionByIdShouldReturnProjection() throws Exception {
        EmployeeProjectionResponse response = new EmployeeProjectionResponse(
                "Иван Петров",
                "Java Developer",
                "IT"
        );

        when(employeeService.findProjectionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/employees/projections/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.position").value("Java Developer"))
                .andExpect(jsonPath("$.departmentName").value("IT"));

        verify(employeeService).findProjectionById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/employees/projections/{id} должен возвращать 404, если сотрудник не найден")
    void findProjectionByIdShouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.findProjectionById(99L))
                .thenThrow(new NotFoundException("Employee not found: 99"));

        mockMvc.perform(get("/api/v1/employees/projections/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService).findProjectionById(99L);
    }

    @Test
    @DisplayName("GET /api/v1/employees/projections/by-department/{departmentId} должен возвращать проекции сотрудников отдела")
    void findProjectionByDepartmentIdShouldReturnProjectionList() throws Exception {
        List<EmployeeProjectionResponse> projections = List.of(
                new EmployeeProjectionResponse(
                        "Иван Петров",
                        "Java Developer",
                        "IT"
                ),
                new EmployeeProjectionResponse(
                        "Мария Сидорова",
                        "DevOps Engineer",
                        "IT"
                )
        );

        when(employeeService.findProjectionByDepartmentId(1L)).thenReturn(projections);

        mockMvc.perform(get("/api/v1/employees/projections/by-department/{departmentId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Иван Петров"))
                .andExpect(jsonPath("$[0].position").value("Java Developer"))
                .andExpect(jsonPath("$[0].departmentName").value("IT"))
                .andExpect(jsonPath("$[1].fullName").value("Мария Сидорова"))
                .andExpect(jsonPath("$[1].position").value("DevOps Engineer"))
                .andExpect(jsonPath("$[1].departmentName").value("IT"));

        verify(employeeService).findProjectionByDepartmentId(1L);
    }

    @Test
    @DisplayName("PUT /api/v1/employees/{id} должен обновлять сотрудника")
    void updateShouldReturnUpdatedEmployee() throws Exception {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Иван",
                "Петров",
                "Senior Java Developer",
                new BigDecimal("220000.00"),
                1L
        );

        EmployeeDetailsResponse response = new EmployeeDetailsResponse(
                1L,
                "Иван",
                "Петров",
                "Senior Java Developer",
                new BigDecimal("220000.00"),
                "IT"
        );

        when(employeeService.update(eq(1L), any(EmployeeUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/employees/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.position").value("Senior Java Developer"))
                .andExpect(jsonPath("$.salary").value(220000.00))
                .andExpect(jsonPath("$.departmentName").value("IT"));

        verify(employeeService).update(eq(1L), any(EmployeeUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/employees/{id} должен возвращать 400, если position пустое")
    void updateShouldReturnBadRequestWhenPositionIsBlank() throws Exception {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Иван",
                "Петров",
                " ",
                new BigDecimal("220000.00"),
                1L
        );

        mockMvc.perform(put("/api/v1/employees/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("PUT /api/v1/employees/{id} должен возвращать 400, если salary отрицательная")
    void updateShouldReturnBadRequestWhenSalaryIsNegative() throws Exception {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Иван",
                "Петров",
                "Senior Java Developer",
                new BigDecimal("-5"),
                1L
        );

        mockMvc.perform(put("/api/v1/employees/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} должен возвращать 204")
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(employeeService).delete(1L);

        mockMvc.perform(delete("/api/v1/employees/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} должен возвращать 404, если сотрудник не найден")
    void deleteShouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Employee not found: 99"))
                .when(employeeService).delete(99L);

        mockMvc.perform(delete("/api/v1/employees/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(employeeService).delete(99L);
    }
}
