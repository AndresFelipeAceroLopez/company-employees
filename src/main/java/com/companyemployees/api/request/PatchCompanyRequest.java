package com.companyemployees.api.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request para actualizacion parcial de compania.
 * Todos los campos son opcionales; solo se validan si vienen presentes.
 */
public record PatchCompanyRequest(
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,
        @Size(min = 1, message = "La direccion no puede estar vacia")
        String direccion,
        @Pattern(regexp = "^[+0-9\\-\\s]{7,20}$", message = "El telefono debe contener entre 7 y 20 digitos validos")
        String telefono
) {}
