package com.companyemployees.application.company.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando para crear una compania junto con sus empleados en una sola transaccion.
 * El salario usa BigDecimal por precision (dinero nunca debe ser Double).
 */
public record CreateCompanyWithEmployeesCommand(
        String nombre,
        String direccion,
        String telefono,
        List<EmployeeData> empleados
) {
    public record EmployeeData(
            String nombre,
            String apellido,
            String correo,
            String cargo,
            BigDecimal salario
    ) {}
}
