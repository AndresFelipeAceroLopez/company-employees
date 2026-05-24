package com.companyemployees.application.employee.dto;

import com.companyemployees.domain.employee.Employee;

import java.math.BigDecimal;

/** DTO de salida para Empleado */
public record EmployeeResponse(
        String id,
        String nombre,
        String apellido,
        String correo,
        String cargo,
        BigDecimal salario,
        String companiaId,
        String status
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId().value(),
                employee.getNombre(),
                employee.getApellido(),
                employee.getCorreo(),
                employee.getCargo(),
                employee.getSalario(),
                employee.getCompaniaId().value(),
                employee.getStatus().name()
        );
    }
}
