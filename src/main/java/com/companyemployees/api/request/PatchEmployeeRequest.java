package com.companyemployees.api.request;

import com.companyemployees.domain.employee.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request para actualizacion parcial.
 * Todos los campos son opcionales; solo se validan si vienen presentes.
 */
public record PatchEmployeeRequest(
        @Size(min = 1, message = "El nombre no puede estar vacio")
        String nombre,
        @Size(min = 1, message = "El apellido no puede estar vacio")
        String apellido,
        @Email(message = "El correo debe tener un formato valido")
        String correo,
        @Size(min = 1, message = "El cargo no puede estar vacio")
        String cargo,
        @Positive(message = "El salario debe ser mayor que 0")
        BigDecimal salario,
        EmployeeStatus status
) {}
