package com.companyemployees.domain.user;

import com.companyemployees.domain.company.CompanyId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidad de dominio: Usuario. No depende de Spring/MongoDB/JWT.
 * La contrasena se almacena exclusivamente como hash.
 * Autorizacion por scopes: el usuario referencia roles (por id) y/o permisos directos
 * (por id). Los scopes efectivos del token son la union de ambos, resueltos en Application.
 * El companiaId indica la pertenencia del usuario (su compania/ciudad), no limita el acceso.
 */
public class User {

    private final UserId id;
    private final String nombre;
    private final String correo;
    private final String passwordHash;
    private final Set<RoleId> roles;
    private final Set<PermissionId> permisos;
    private final CompanyId companiaId;
    private final LocalDateTime fechaCreacion;

    public User(UserId id, String nombre, String correo, String passwordHash,
                Set<RoleId> roles, Set<PermissionId> permisos,
                CompanyId companiaId, LocalDateTime fechaCreacion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("El passwordHash es obligatorio");
        }
        boolean sinRoles = roles == null || roles.isEmpty();
        boolean sinPermisos = permisos == null || permisos.isEmpty();
        if (sinRoles && sinPermisos) {
            throw new IllegalArgumentException("El usuario debe tener al menos un rol o un permiso directo");
        }
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.roles = sinRoles ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(roles));
        this.permisos = sinPermisos ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(permisos));
        this.companiaId = companiaId;
        this.fechaCreacion = fechaCreacion;
    }

    public static User register(String nombre, String correo, String passwordHash,
                                Set<RoleId> roles, Set<PermissionId> permisos, CompanyId companiaId) {
        return new User(null, nombre, correo, passwordHash, roles, permisos, companiaId, LocalDateTime.now());
    }

    public UserId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public Set<RoleId> getRoles() { return roles; }
    public Set<PermissionId> getPermisos() { return permisos; }
    public CompanyId getCompaniaId() { return companiaId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
