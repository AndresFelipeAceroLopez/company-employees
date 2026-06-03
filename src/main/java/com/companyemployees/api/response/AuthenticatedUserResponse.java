package com.companyemployees.api.response;

import com.companyemployees.application.auth.dto.AuthResult;

import java.util.Set;

public record AuthenticatedUserResponse(
        String id,
        String nombre,
        String correo,
        Set<String> roles,
        Set<String> scopes,
        String companiaId
) {
    public static AuthenticatedUserResponse from(AuthResult.AuthenticatedUser user) {
        return new AuthenticatedUserResponse(
                user.id(), user.nombre(), user.correo(),
                user.roles(), user.scopes(), user.companiaId());
    }
}
