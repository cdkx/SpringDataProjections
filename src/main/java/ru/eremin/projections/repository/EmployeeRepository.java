package ru.eremin.projections.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.eremin.projections.model.Employee;
import ru.eremin.projections.projection.EmployeeProjection;

import java.util.List;
import java.util.Optional;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            select e
            from Employee e
            join fetch e.department
            """)
    List<Employee> findAllWithDepartment();

    @Query("""
            select e
            from Employee e
            join fetch e.department
            where e.id = :id
            """)
    Optional<Employee> findWithDepartmentById(@Param("id") Long id);

    @Query("""
            select
                e.firstName as firstName,
                e.lastName as lastName,
                e.position as position,
                d.name as departmentName
            from Employee e
            join e.department d
            """)
    List<EmployeeProjection> findAllProjected();

    @Query("""
            select
                e.firstName as firstName,
                e.lastName as lastName,
                e.position as position,
                d.name as departmentName
            from Employee e
            join e.department d
            where e.id = :id
            """)
    Optional<EmployeeProjection> findProjectedById(@Param("id") Long id);

    @Query("""
            select
                e.firstName as firstName,
                e.lastName as lastName,
                e.position as position,
                d.name as departmentName
            from Employee e
            join e.department d
            where d.id = :departmentId
            """)
    List<EmployeeProjection> findAllProjectedByDepartmentId(
            @Param("departmentId") Long departmentId
    );

    long countByDepartmentId(Long departmentId);
}
