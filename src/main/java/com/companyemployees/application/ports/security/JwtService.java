package com.companyemployees.application.ports.security;

import java.util.List;
import java.util.Set;

/** Puerto de seguridad: emision y verificacion de JWTs. */
public interface JwtService {

    /**
     * Genera un token firmado. Separa claims de usuario (sub, correo, companiaId)
     * de los claims de autorizacion (roles, scopes) y la expiracion (exp).
     * Los roles y scopes ya vienen resueltos desde las colecciones roles/permissions.
     */
    String generateToken(String userId, String correo, String companiaId,
                         Set<String> roles, Set<String> scopes);

    /** Parsea y valida un token. Lanza si es invalido o expirado. */
    JwtClaims parse(String token);

    /** Tiempo de expiracion configurado, en segundos. */
    long expirationSeconds();

    /**
     * Claims relevantes extraidos del token.
     * roles: nombres de rol (p.ej. "ADMIN"). scopes: permisos finos (p.ej. "empleado:eliminar").
     */
    record JwtClaims(String userId, String correo, String companiaId,
                     List<String> roles, List<String> scopes) {}
}
