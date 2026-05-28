package com.companyemployees.application.auth.dto;

import com.companyemployees.domain.user.Role;

public record RegisterUserCommand(
        String nombre,
        String correo,
        String password,
        Role role,
        String companiaId
) {}
