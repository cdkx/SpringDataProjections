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
import ru.eremin.projections.dto.request.DepartmentCreateRequest;
import ru.eremin.projections.dto.request.DepartmentUpdateRequest;
import ru.eremin.projections.dto.response.DepartmentResponse;
import ru.eremin.projections.exception.GlobalExceptionHandler;
import ru.eremin.projections.exception.NotFoundException;
import ru.eremin.projections.service.DepartmentService;

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


@WebMvcTest(DepartmentController.class)
@Import(GlobalExceptionHandler.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("POST /api/v1/departments должен создавать отдел и возвращать 201")
    void createShouldReturnCreatedStatus() throws Exception {
        DepartmentCreateRequest request = new DepartmentCreateRequest("IT");

        DepartmentResponse response = new DepartmentResponse(1L, "IT");

        when(departmentService.create(any(DepartmentCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"));

        verify(departmentService).create(any(DepartmentCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/departments должен возвращать 400, если name пустое")
    void createShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        DepartmentCreateRequest request = new DepartmentCreateRequest(" ");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentService);
    }

    @Test
    @DisplayName("GET /api/v1/departments должен возвращать список отделов")
    void findAllShouldReturnDepartmentsList() throws Exception {
        List<DepartmentResponse> departments = List.of(
                new DepartmentResponse(1L, "IT"),
                new DepartmentResponse(2L, "HR")
        );

        when(departmentService.findAll()).thenReturn(departments);

        mockMvc.perform(get("/api/v1/departments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("IT"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("HR"));

        verify(departmentService).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/departments/{id} должен возвращать отдел по id")
    void findByIdShouldReturnDepartment() throws Exception {
        DepartmentResponse response = new DepartmentResponse(1L, "IT");

        when(departmentService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/departments/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"));

        verify(departmentService).findById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/departments/{id} должен возвращать 404, если отдел не найден")
    void findByIdShouldReturnNotFoundWhenDepartmentDoesNotExist() throws Exception {
        when(departmentService.findById(99L))
                .thenThrow(new NotFoundException("Department not found: 99"));

        mockMvc.perform(get("/api/v1/departments/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(departmentService).findById(99L);
    }

    @Test
    @DisplayName("PUT /api/v1/departments/{id} должен обновлять отдел")
    void updateShouldReturnUpdatedDepartment() throws Exception {
        DepartmentUpdateRequest request = new DepartmentUpdateRequest("IT updated");

        DepartmentResponse response = new DepartmentResponse(1L, "IT updated");

        when(departmentService.update(eq(1L), any(DepartmentUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/departments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT updated"));

        verify(departmentService).update(eq(1L), any(DepartmentUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/departments/{id} должен возвращать 400, если name пустое")
    void updateShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        DepartmentUpdateRequest request = new DepartmentUpdateRequest(" ");

        mockMvc.perform(put("/api/v1/departments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(departmentService);
    }

    @Test
    @DisplayName("DELETE /api/v1/departments/{id} должен возвращать 204")
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(departmentService).delete(1L);

        mockMvc.perform(delete("/api/v1/departments/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(departmentService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/departments/{id} должен возвращать 404, если отдел не найден")
    void deleteShouldReturnNotFoundWhenDepartmentDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Department not found: 99"))
                .when(departmentService).delete(99L);

        mockMvc.perform(delete("/api/v1/departments/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(departmentService).delete(99L);
    }
}
