package com.companyemployees.domain.user;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidad de dominio: Rol. Vive en su propia coleccion ("roles") y referencia, por id,
 * los permisos que concede (relacion roles -&gt; permissions). Un usuario puede tener varios roles.
 * <p>
 * ADMIN y USUARIO son roles semilla; el modelo permite crear mas roles en BD.
 */
public class Role {

    private final RoleId id;
    private final String nombre;
    private final Set<PermissionId> permisos;

    public Role(RoleId id, String nombre, Set<PermissionId> permisos) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        this.id = id;
        this.nombre = nombre;
        this.permisos = permisos == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(permisos));
    }

    /** Crea un rol nuevo (sin id; lo asigna la persistencia). */
    public static Role create(String nombre, Set<PermissionId> permisos) {
        return new Role(null, nombre, permisos);
    }

    public RoleId getId() { return id; }
    public String getNombre() { return nombre; }
    public Set<PermissionId> getPermisos() { return permisos; }
}
