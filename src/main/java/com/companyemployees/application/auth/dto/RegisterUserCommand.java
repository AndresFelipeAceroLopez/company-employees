package com.companyemployees.application.auth.dto;

import java.util.Set;

/**
 * roles: nombres de rol (p.ej. "ADMIN", "USUARIO"), se resuelven a ids contra la coleccion roles.
 * scopes: permisos asignados directamente al usuario (p.ej. "empleado:leer"), se resuelven
 * contra la coleccion permissions. Los scopes efectivos del token son la union de ambos.
 */
public record RegisterUserCommand(
        String nombre,
        String correo,
        String password,
        Set<String> roles,
        Set<String> scopes,
        String companiaId
) {}
