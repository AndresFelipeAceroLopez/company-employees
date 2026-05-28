package com.companyemployees.application.employee.dto;

import com.companyemployees.domain.employee.EmployeeStatus;

import java.math.BigDecimal;

/**
 * Comando para actualizacion parcial (PATCH).
 * Todos los campos son opcionales: solo se aplican los presentes (no nulos).
 */
public record PatchEmployeeCommand(
        String nombre,
        String apellido,
        String correo,
        String cargo,
        BigDecimal salario,
        EmployeeStatus status
) {}
