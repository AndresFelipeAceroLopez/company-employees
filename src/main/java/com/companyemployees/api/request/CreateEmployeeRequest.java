package com.companyemployees.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateEmployeeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "El apellido es obligatorio")
        String apellido,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato valido")
        String correo,
        @NotBlank(message = "El cargo es obligatorio")
        String cargo,
        @NotNull(message = "El salario es obligatorio")
        @Positive(message = "El salario debe ser mayor que 0")
        BigDecimal salario,
        @NotBlank(message = "El id de la compania es obligatorio")
        String companiaId
) {}
