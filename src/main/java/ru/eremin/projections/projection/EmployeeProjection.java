package ru.eremin.projections.projection;

public interface EmployeeProjection {

    String getFirstName();

    String getLastName();

    default String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    String getPosition();

    String getDepartmentName();
}
