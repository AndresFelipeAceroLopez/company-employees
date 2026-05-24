package com.companyemployees.api.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "La dirección es obligatoria")
        String direccion,
        @NotBlank(message = "El teléfono es obligatorio")
        String telefono
) {}
