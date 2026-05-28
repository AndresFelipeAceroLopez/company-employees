package com.companyemployees.domain.user;

import com.companyemployees.domain.company.CompanyId;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Usuario. No depende de Spring/MongoDB/JWT.
 * La contrasena se almacena exclusivamente como hash.
 * Un USUARIO requiere companiaId; un ADMIN puede tenerla o no.
 */
public class User {

    private UserId id;
    private String nombre;
    private String correo;
    private String passwordHash;
    private Role role;
    private CompanyId companiaId;
    private LocalDateTime fechaCreacion;

    public User(UserId id, String nombre, String correo, String passwordHash,
                Role role, CompanyId companiaId, LocalDateTime fechaCreacion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("El passwordHash es obligatorio");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        if (role == Role.USUARIO && companiaId == null) {
            throw new IllegalArgumentException("Un USUARIO debe tener companiaId asociada");
        }
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.role = role;
        this.companiaId = companiaId;
        this.fechaCreacion = fechaCreacion;
    }

    public static User register(String nombre, String correo, String passwordHash,
                                Role role, CompanyId companiaId) {
        return new User(null, nombre, correo, passwordHash, role, companiaId, LocalDateTime.now());
    }

    public UserId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public CompanyId getCompaniaId() { return companiaId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
