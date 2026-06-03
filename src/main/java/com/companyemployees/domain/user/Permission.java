package com.companyemployees.domain.user;

/**
 * Entidad de dominio: Permiso fino (scope). Vive en su propia coleccion ("permissions").
 * Representa una accion concreta sobre un recurso y es la unidad que viaja en el claim
 * "scopes" del JWT (authority "SCOPE_&lt;scope&gt;" en Spring Security).
 * <p>
 * Convencion del scope: "&lt;recurso&gt;:&lt;accion&gt;", p.ej. "empleado:eliminar".
 */
public class Permission {

    private final PermissionId id;
    private final String scope;

    public Permission(PermissionId id, String scope) {
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("El scope del permiso es obligatorio");
        }
        this.id = id;
        this.scope = scope;
    }

    /** Crea un permiso nuevo (sin id; lo asigna la persistencia). */
    public static Permission create(String scope) {
        return new Permission(null, scope);
    }

    public PermissionId getId() { return id; }
    public String getScope() { return scope; }
}
