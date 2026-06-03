package com.companyemployees.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "roles")
public class RoleDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre;

    /** Ids de los permisos que concede el rol (referencia a la coleccion permissions). */
    private List<String> permisos;

    public RoleDocument() {}

    public RoleDocument(String id, String nombre, List<String> permisos) {
        this.id = id;
        this.nombre = nombre;
        this.permisos = permisos;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<String> getPermisos() { return permisos; }
    public void setPermisos(List<String> permisos) { this.permisos = permisos; }
}
