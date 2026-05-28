package com.companyemployees.api.response;

import com.companyemployees.application.auth.dto.AuthResult;

public record AuthenticatedUserResponse(
        String id,
        String nombre,
        String correo,
        String role,
        String companiaId
) {
    public static AuthenticatedUserResponse from(AuthResult.AuthenticatedUser user) {
        return new AuthenticatedUserResponse(
                user.id(), user.nombre(), user.correo(), user.role(), user.companiaId());
    }
}
