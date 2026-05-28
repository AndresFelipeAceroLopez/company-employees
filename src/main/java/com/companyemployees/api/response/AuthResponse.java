package com.companyemployees.api.response;

import com.companyemployees.application.auth.dto.AuthResult;

public record AuthResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        AuthenticatedUserResponse usuario
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.token(),
                "Bearer",
                result.expiraEnSegundos(),
                AuthenticatedUserResponse.from(result.usuario())
        );
    }
}
