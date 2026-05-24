package com.companyemployees.api.request;

import com.companyemployees.domain.employee.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateEmployeeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "El apellido es obligatorio")
        String apellido,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        String correo,
        @NotBlank(message = "El cargo es obligatorio")
        String cargo,
        @NotNull(message = "El salario es obligatorio")
        BigDecimal salario,
        @NotNull(message = "El estado es obligatorio")
        EmployeeStatus status
) {}
