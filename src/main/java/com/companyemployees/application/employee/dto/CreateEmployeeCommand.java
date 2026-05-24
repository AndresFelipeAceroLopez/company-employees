package com.companyemployees.application.employee.dto;

import java.math.BigDecimal;

/** Comando para crear un empleado */
public record CreateEmployeeCommand(
        String nombre,
        String apellido,
        String correo,
        String cargo,
        BigDecimal salario,
        String companiaId
) {}
