package com.companyemployees.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,
        @NotBlank(message = "La direccion es obligatoria")
        String direccion,
        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(regexp = "^[+0-9\\-\\s]{7,20}$", message = "El telefono debe contener entre 7 y 20 digitos validos")
        String telefono
) {}
