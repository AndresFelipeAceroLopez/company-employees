package com.companyemployees.application.ports.security;

import com.companyemployees.domain.user.User;

/** Puerto de seguridad: emision y verificacion de JWTs. */
public interface JwtService {

    /** Genera un token firmado con los claims (sub, correo, role, companiaId, exp). */
    String generateToken(User user);

    /** Parsea y valida un token. Lanza si es invalido o expirado. */
    JwtClaims parse(String token);

    /** Tiempo de expiracion configurado, en segundos. */
    long expirationSeconds();

    record JwtClaims(String userId, String correo, String role, String companiaId) {}
}
