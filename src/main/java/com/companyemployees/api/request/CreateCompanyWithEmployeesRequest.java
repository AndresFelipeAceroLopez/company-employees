package com.companyemployees.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Request para crear una compania con empleados en una sola transaccion. */
public record CreateCompanyWithEmployeesRequest(
        @NotBlank(message = "El nombre de la compania es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "La direccion es obligatoria")
        String direccion,

        @NotBlank(message = "El telefono es obligatorio")
        String telefono,

        @NotEmpty(message = "Debe incluir al menos un empleado")
        @Valid
        List<EmployeeData> empleados
) {
    public record EmployeeData(
            @NotBlank(message = "El nombre del empleado es obligatorio")
            String nombre,

            @NotBlank(message = "El apellido del empleado es obligatorio")
            String apellido,

            @NotBlank(message = "El correo del empleado es obligatorio")
            @Email(message = "El correo debe tener un formato valido")
            String correo,

            @NotBlank(message = "El cargo es obligatorio")
            String cargo,

            @NotNull(message = "El salario es obligatorio")
            @Positive(message = "El salario debe ser mayor que 0")
            BigDecimal salario
    ) {}
}
