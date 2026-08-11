package ru.eremin.projections.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.eremin.projections.dto.request.EmployeeCreateRequest;
import ru.eremin.projections.dto.request.EmployeeUpdateRequest;
import ru.eremin.projections.dto.response.EmployeeDetailsResponse;
import ru.eremin.projections.dto.response.EmployeeProjectionResponse;
import ru.eremin.projections.exception.NotFoundException;
import ru.eremin.projections.model.Department;
import ru.eremin.projections.model.Employee;
import ru.eremin.projections.repository.DepartmentRepository;
import ru.eremin.projections.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class EmployeeServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    private Department saveDepartment(String name) {
        return departmentRepository.saveAndFlush(
                Department.builder()
                        .name(name)
                        .build()
        );
    }

    private Employee saveEmployee(
            String firstName,
            String lastName,
            String position,
            BigDecimal salary,
            Department department
    ) {
        return employeeRepository.saveAndFlush(
                Employee.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .position(position)
                        .salary(salary)
                        .department(department)
                        .build()
        );
    }

    @Test
    @DisplayName("create() должен сохранять сотрудника")
    void createShouldSaveEmployee() {
        Department department = saveDepartment("IT");

        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                department.getId()
        );

        EmployeeDetailsResponse response = employeeService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.firstName()).isEqualTo("Иван");
        assertThat(response.lastName()).isEqualTo("Петров");
        assertThat(response.position()).isEqualTo("Java Developer");
        assertThat(response.salary()).isEqualByComparingTo(new BigDecimal("180000.00"));
        assertThat(response.departmentName()).isEqualTo("IT");

        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("create() должен бросать NotFoundException, если отдел не найден")
    void createShouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                999L
        );

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("update() должен обновлять данные сотрудника и его отдел")
    void updateShouldChangeEmployeeFieldsAndDepartment() {
        Department itDepartment = saveDepartment("IT");
        Department hrDepartment = saveDepartment("HR");

        Employee employee = saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                itDepartment
        );

        EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest(
                "Иван",
                "Петров",
                "Senior Java Developer",
                new BigDecimal("250000.00"),
                hrDepartment.getId()
        );

        EmployeeDetailsResponse updated = employeeService.update(employee.getId(), updateRequest);

        assertThat(updated.id()).isEqualTo(employee.getId());
        assertThat(updated.firstName()).isEqualTo("Иван");
        assertThat(updated.lastName()).isEqualTo("Петров");
        assertThat(updated.position()).isEqualTo("Senior Java Developer");
        assertThat(updated.salary()).isEqualByComparingTo(new BigDecimal("250000.00"));
        assertThat(updated.departmentName()).isEqualTo("HR");

        Employee employeeFromDb = employeeRepository
                .findWithDepartmentById(employee.getId())
                .orElseThrow();

        assertThat(employeeFromDb.getPosition()).isEqualTo("Senior Java Developer");
        assertThat(employeeFromDb.getSalary()).isEqualByComparingTo(new BigDecimal("250000.00"));
        assertThat(employeeFromDb.getDepartment().getName()).isEqualTo("HR");
    }

    @Test
    @DisplayName("update() должен бросать NotFoundException, если сотрудник не найден")
    void updateShouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        Department department = saveDepartment("IT");

        EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                department.getId()
        );

        assertThatThrownBy(() -> employeeService.update(999L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("update() должен бросать NotFoundException, если новый отдел не найден")
    void updateShouldThrowNotFoundExceptionWhenNewDepartmentDoesNotExist() {
        Department department = saveDepartment("IT");

        Employee employee = saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                department
        );

        EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest(
                "Иван",
                "Петров",
                "Senior Java Developer",
                new BigDecimal("250000.00"),
                999L
        );

        assertThatThrownBy(() -> employeeService.update(employee.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("delete() должен удалять сотрудника")
    void deleteShouldRemoveEmployee() {
        Department department = saveDepartment("IT");

        Employee employee = saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                department
        );

        employeeService.delete(employee.getId());

        assertThat(employeeRepository.count()).isZero();
    }

    @Test
    @DisplayName("delete() должен бросать NotFoundException, если сотрудник не найден")
    void deleteShouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        assertThatThrownBy(() -> employeeService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findAll() должен возвращать пустой список, если сотрудников нет")
    void findAllShouldReturnEmptyListWhenNoEmployees() {
        List<EmployeeDetailsResponse> employees = employeeService.findAll();

        assertThat(employees).isEmpty();
    }

    @Test
    @DisplayName("findAll() должен возвращать всех сотрудников с данными отдела")
    void findAllShouldReturnAllEmployeesWithDepartmentName() {
        Department itDepartment = saveDepartment("IT");
        Department hrDepartment = saveDepartment("HR");

        saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                itDepartment
        );

        saveEmployee(
                "Мария",
                "Сидорова",
                "HR Manager",
                new BigDecimal("150000.00"),
                hrDepartment
        );

        List<EmployeeDetailsResponse> employees = employeeService.findAll();

        assertThat(employees).hasSize(2);

        assertThat(employees)
                .extracting(EmployeeDetailsResponse::firstName)
                .containsExactlyInAnyOrder("Иван", "Мария");

        assertThat(employees)
                .extracting(EmployeeDetailsResponse::departmentName)
                .containsExactlyInAnyOrder("IT", "HR");
    }

    @Test
    @DisplayName("findById() должен возвращать сотрудника по id")
    void findByIdShouldReturnEmployee() {
        Department department = saveDepartment("IT");

        Employee employee = saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                department
        );

        EmployeeDetailsResponse response = employeeService.findById(employee.getId());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(employee.getId());
        assertThat(response.firstName()).isEqualTo("Иван");
        assertThat(response.lastName()).isEqualTo("Петров");
        assertThat(response.position()).isEqualTo("Java Developer");
        assertThat(response.salary()).isEqualByComparingTo(new BigDecimal("180000.00"));
        assertThat(response.departmentName()).isEqualTo("IT");
    }

    @Test
    @DisplayName("findById() должен бросать NotFoundException, если сотрудник не найден")
    void findByIdShouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        assertThatThrownBy(() -> employeeService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findAllProjection() должен возвращать пустой список, если сотрудников нет")
    void findAllProjectionShouldReturnEmptyListWhenNoEmployees() {
        List<EmployeeProjectionResponse> projections = employeeService.findAllProjection();

        assertThat(projections).isEmpty();
    }

    @Test
    @DisplayName("findAllProjection() должен возвращать проекции сотрудников")
    void findAllProjectionShouldReturnEmployeeProjections() {
        Department itDepartment = saveDepartment("IT");
        Department hrDepartment = saveDepartment("HR");

        saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                itDepartment
        );

        saveEmployee(
                "Мария",
                "Сидорова",
                "HR Manager",
                new BigDecimal("150000.00"),
                hrDepartment
        );

        List<EmployeeProjectionResponse> projections = employeeService.findAllProjection();

        assertThat(projections).hasSize(2);

        assertThat(projections)
                .extracting(EmployeeProjectionResponse::fullName)
                .containsExactlyInAnyOrder("Иван Петров", "Мария Сидорова");

        assertThat(projections)
                .extracting(EmployeeProjectionResponse::position)
                .containsExactlyInAnyOrder("Java Developer", "HR Manager");

        assertThat(projections)
                .extracting(EmployeeProjectionResponse::departmentName)
                .containsExactlyInAnyOrder("IT", "HR");
    }

    @Test
    @DisplayName("findProjectionById() должен возвращать проекцию сотрудника")
    void findProjectionByIdShouldReturnEmployeeProjection() {
        Department department = saveDepartment("IT");

        Employee employee = saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                department
        );

        EmployeeProjectionResponse projection = employeeService.findProjectionById(employee.getId());

        assertThat(projection).isNotNull();
        assertThat(projection.fullName()).isEqualTo("Иван Петров");
        assertThat(projection.position()).isEqualTo("Java Developer");
        assertThat(projection.departmentName()).isEqualTo("IT");
    }

    @Test
    @DisplayName("findProjectionById() должен бросать NotFoundException, если сотрудник не найден")
    void findProjectionByIdShouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        assertThatThrownBy(() -> employeeService.findProjectionById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findProjectionByDepartmentId() должен возвращать сотрудников только указанного отдела")
    void findProjectionByDepartmentIdShouldReturnEmployeesOfDepartment() {
        Department itDepartment = saveDepartment("IT");
        Department hrDepartment = saveDepartment("HR");

        saveEmployee(
                "Иван",
                "Петров",
                "Java Developer",
                new BigDecimal("180000.00"),
                itDepartment
        );

        saveEmployee(
                "Мария",
                "Сидорова",
                "DevOps Engineer",
                new BigDecimal("200000.00"),
                itDepartment
        );

        saveEmployee(
                "Алексей",
                "Иванов",
                "HR Manager",
                new BigDecimal("150000.00"),
                hrDepartment
        );

        List<EmployeeProjectionResponse> projections =
                employeeService.findProjectionByDepartmentId(itDepartment.getId());

        assertThat(projections).hasSize(2);

        assertThat(projections)
                .extracting(EmployeeProjectionResponse::departmentName)
                .containsOnly("IT");

        assertThat(projections)
                .extracting(EmployeeProjectionResponse::fullName)
                .containsExactlyInAnyOrder("Иван Петров", "Мария Сидорова");
    }

    @Test
    @DisplayName("findProjectionByDepartmentId() должен возвращать пустой список, если в отделе нет сотрудников")
    void findProjectionByDepartmentIdShouldReturnEmptyListWhenDepartmentHasNoEmployees() {
        Department emptyDepartment = saveDepartment("Finance");

        List<EmployeeProjectionResponse> projections =
                employeeService.findProjectionByDepartmentId(emptyDepartment.getId());

        assertThat(projections).isEmpty();
    }

    @Test
    @DisplayName("findProjectionByDepartmentId() должен бросать NotFoundException, если отдел не найден")
    void findProjectionByDepartmentIdShouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
        assertThatThrownBy(() -> employeeService.findProjectionByDepartmentId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }
}
