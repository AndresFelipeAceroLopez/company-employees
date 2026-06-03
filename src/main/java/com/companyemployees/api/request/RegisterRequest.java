package com.companyemployees.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro publico de usuario. Siempre crea un USUARIO (rol base) ligado a su compania
 * (pertenencia). NO acepta roles ni scopes del cliente: la asignacion de privilegios de
 * admin no es self-service (se hace por seed o por un endpoint solo-admin), para evitar
 * escalada de privilegios desde un endpoint publico.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato valido")
        String correo,
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,
        /** Compania a la que pertenece el usuario (su ciudad/municipio). Obligatoria. */
        @NotBlank(message = "La compania (companiaId) es obligatoria")
        String companiaId
) {}
