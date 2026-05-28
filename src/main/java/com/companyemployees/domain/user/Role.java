package com.companyemployees.domain.user;

/**
 * Rol del usuario en el sistema.
 * ADMIN: acceso total. companiaId opcional.
 * USUARIO: acceso limitado por ownership a su companiaId.
 */
public enum Role {
    ADMIN,
    USUARIO
}
