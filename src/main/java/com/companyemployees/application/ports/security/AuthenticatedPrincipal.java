package com.companyemployees.application.ports.security;

import java.util.Set;

/**
 * Principal autenticado expuesto a la capa web y a politicas de autorizacion.
 * Vive en Application: es el contrato que describe "que sabemos del usuario tras autenticar".
 * La capa de Infraestructura lo construye a partir del mecanismo concreto (JWT hoy).
 * <p>
 * Separa la identidad del usuario (userId, correo, companiaId) de su autorizacion
 * (roles y scopes), tal como viajan en claims distintos del token.
 */
public record AuthenticatedPrincipal(String userId, String correo, String companiaId,
                                     Set<String> roles, Set<String> scopes) {

    public AuthenticatedPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }

    @Override
    public String toString() {
        return correo == null ? userId : correo;
    }
}
