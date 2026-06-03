package com.companyemployees.application.auth.dto;

import com.companyemployees.domain.user.User;

import java.util.Set;

public record AuthResult(
        String token,
        long expiraEnSegundos,
        AuthenticatedUser usuario
) {
    public record AuthenticatedUser(
            String id,
            String nombre,
            String correo,
            Set<String> roles,
            Set<String> scopes,
            String companiaId
    ) {
        /** Construye la vista del usuario con sus roles/scopes ya resueltos. */
        public static AuthenticatedUser of(User user, Set<String> roleNames, Set<String> scopes) {
            return new AuthenticatedUser(
                    user.getId().value(),
                    user.getNombre(),
                    user.getCorreo(),
                    roleNames,
                    scopes,
                    user.getCompaniaId() != null ? user.getCompaniaId().value() : null
            );
        }
    }
}
