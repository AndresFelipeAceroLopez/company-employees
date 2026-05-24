package com.companyemployees.domain.company;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Compañía.
 * NO depende de Spring, MongoDB ni ningún framework.
 * Contiene las reglas de negocio del dominio.
 */
public class Company {

    private CompanyId id;
    private String nombre;
    private String direccion;
    private String telefono;
    private LocalDateTime fechaCreacion;
    private int employeeCount;

    public Company(CompanyId id, String nombre, String direccion,
                   String telefono, LocalDateTime fechaCreacion, int employeeCount) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fechaCreacion = fechaCreacion;
        this.employeeCount = employeeCount;
    }

    /** Factory method — crea una compañía nueva sin id (MongoDB lo asigna) */
    public static Company create(String nombre, String direccion, String telefono) {
        return new Company(null, nombre, direccion, telefono, LocalDateTime.now(), 0);
    }

    /** Regla de negocio: incrementa el contador al agregar un empleado */
    public void increaseEmployeeCount() {
        this.employeeCount++;
    }

    /** Regla de negocio: decrementa el contador al eliminar un empleado */
    public void decreaseEmployeeCount() {
        if (this.employeeCount > 0) this.employeeCount--;
    }

    public void update(String nombre, String direccion, String telefono) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public CompanyId getId() { return id; }
    public void setId(CompanyId id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public int getEmployeeCount() { return employeeCount; }
}
