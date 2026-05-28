package com.companyemployees.application.ports.security;

/** Puerto de seguridad: hashing y verificacion de contrasenas. */
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hash);
}
