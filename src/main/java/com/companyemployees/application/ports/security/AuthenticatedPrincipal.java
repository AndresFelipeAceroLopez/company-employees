package com.companyemployees.application.ports.security;

/**
 * Principal autenticado expuesto a la capa web y a politicas de autorizacion.
 * Vive en Application: es el contrato que describe "que sabemos del usuario tras autenticar".
 * La capa de Infraestructura lo construye a partir del mecanismo concreto (JWT hoy).
 */
public record AuthenticatedPrincipal(String userId, String correo, String role, String companiaId) {

    @Override
    public String toString() {
        return correo == null ? userId : correo;
    }
}
