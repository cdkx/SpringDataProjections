package ru.eremin.projections.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eremin.projections.model.Department;


public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByName(String name);

}
