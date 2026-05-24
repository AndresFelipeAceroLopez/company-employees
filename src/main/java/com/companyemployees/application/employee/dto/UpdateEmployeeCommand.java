package com.companyemployees.application.employee.dto;

import com.companyemployees.domain.employee.EmployeeStatus;

import java.math.BigDecimal;

/** Comando para actualizar un empleado */
public record UpdateEmployeeCommand(
        String nombre,
        String apellido,
        String correo,
        String cargo,
        BigDecimal salario,
        EmployeeStatus status
) {}
