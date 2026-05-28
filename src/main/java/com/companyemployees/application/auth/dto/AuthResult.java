package com.companyemployees.application.auth.dto;

import com.companyemployees.domain.user.User;

public record AuthResult(
        String token,
        long expiraEnSegundos,
        AuthenticatedUser usuario
) {
    public record AuthenticatedUser(
            String id,
            String nombre,
            String correo,
            String role,
            String companiaId
    ) {
        public static AuthenticatedUser from(User user) {
            return new AuthenticatedUser(
                    user.getId().value(),
                    user.getNombre(),
                    user.getCorreo(),
                    user.getRole().name(),
                    user.getCompaniaId() != null ? user.getCompaniaId().value() : null
            );
        }
    }
}
