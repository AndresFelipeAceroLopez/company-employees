package com.companyemployees.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    private String nombre;

    @Indexed(unique = true)
    private String correo;

    private String passwordHash;

    /** Ids de los roles del usuario (referencia a la coleccion roles). */
    private List<String> roles;

    /** Ids de permisos asignados directamente al usuario (referencia a la coleccion permissions). */
    private List<String> permisos;

    private String companiaId;

    private LocalDateTime fechaCreacion;

    public UserDocument() {}

    public UserDocument(String id, String nombre, String correo, String passwordHash,
                        List<String> roles, List<String> permisos,
                        String companiaId, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.permisos = permisos;
        this.companiaId = companiaId;
        this.fechaCreacion = fechaCreacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermisos() { return permisos; }
    public void setPermisos(List<String> permisos) { this.permisos = permisos; }
    public String getCompaniaId() { return companiaId; }
    public void setCompaniaId(String companiaId) { this.companiaId = companiaId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
