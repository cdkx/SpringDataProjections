package ru.eremin.projections.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.eremin.projections.dto.request.DepartmentCreateRequest;
import ru.eremin.projections.dto.request.DepartmentUpdateRequest;
import ru.eremin.projections.dto.response.DepartmentResponse;
import ru.eremin.projections.exception.ConflictException;
import ru.eremin.projections.exception.NotFoundException;
import ru.eremin.projections.model.Department;
import ru.eremin.projections.model.Employee;
import ru.eremin.projections.repository.DepartmentRepository;
import ru.eremin.projections.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DepartmentServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @Test
    @DisplayName("create() должен сохранять новый отдел")
    void createShouldSaveDepartment() {
        DepartmentCreateRequest request = new DepartmentCreateRequest("IT");

        DepartmentResponse response = departmentService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("IT");

        assertThat(departmentRepository.count()).isEqualTo(1);
        assertThat(departmentRepository.existsByName("IT")).isTrue();
    }

    @Test
    @DisplayName("findAll() должен возвращать пустой список, если отделов нет")
    void findAllShouldReturnEmptyListWhenNoDepartments() {
        List<DepartmentResponse> departments = departmentService.findAll();

        assertThat(departments).isEmpty();
    }

    @Test
    @DisplayName("findAll() должен возвращать все отделы")
    void findAllShouldReturnAllDepartments() {
        departmentService.create(new DepartmentCreateRequest("IT"));
        departmentService.create(new DepartmentCreateRequest("HR"));

        List<DepartmentResponse> departments = departmentService.findAll();

        assertThat(departments).hasSize(2);
        assertThat(departments)
                .extracting(DepartmentResponse::name)
                .containsExactlyInAnyOrder("IT", "HR");
    }

    @Test
    @DisplayName("findById() должен возвращать отдел по id")
    void findByIdShouldReturnDepartment() {
        DepartmentResponse created = departmentService.create(
                new DepartmentCreateRequest("IT")
        );

        DepartmentResponse found = departmentService.findById(created.id());

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.name()).isEqualTo("IT");
    }

    @Test
    @DisplayName("findById() должен бросать NotFoundException, если отдел не найден")
    void findByIdShouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
        assertThatThrownBy(() -> departmentService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("update() должен обновлять название отдела")
    void updateShouldChangeDepartmentName() {
        DepartmentResponse created = departmentService.create(
                new DepartmentCreateRequest("IT")
        );

        DepartmentUpdateRequest updateRequest = new DepartmentUpdateRequest("Finance");

        DepartmentResponse updated = departmentService.update(created.id(), updateRequest);

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("Finance");

        Department departmentFromDb = departmentRepository.findById(created.id())
                .orElseThrow();

        assertThat(departmentFromDb.getName()).isEqualTo("Finance");
    }

    @Test
    @DisplayName("update() должен бросать NotFoundException, если отдел не найден")
    void updateShouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
        DepartmentUpdateRequest updateRequest = new DepartmentUpdateRequest("Finance");

        assertThatThrownBy(() -> departmentService.update(999L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("delete() должен удалять отдел без сотрудников")
    void deleteShouldRemoveDepartmentWhenNoEmployeesAssigned() {
        DepartmentResponse created = departmentService.create(
                new DepartmentCreateRequest("IT")
        );

        departmentService.delete(created.id());

        assertThat(departmentRepository.count()).isZero();
        assertThat(departmentRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("delete() должен бросать NotFoundException, если отдел не найден")
    void deleteShouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
        assertThatThrownBy(() -> departmentService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("delete() должен бросать ConflictException, если в отделе есть сотрудники")
    void deleteShouldThrowConflictExceptionWhenDepartmentHasEmployees() {
        Department department = departmentRepository.save(
                Department.builder()
                        .name("IT")
                        .build()
        );

        employeeRepository.save(
                Employee.builder()
                        .firstName("Иван")
                        .lastName("Петров")
                        .position("Java Developer")
                        .salary(new BigDecimal("180000"))
                        .department(department)
                        .build()
        );

        assertThatThrownBy(() -> departmentService.delete(department.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(String.valueOf(department.getId()));

        assertThat(departmentRepository.count()).isEqualTo(1);
        assertThat(employeeRepository.count()).isEqualTo(1);
    }
}
