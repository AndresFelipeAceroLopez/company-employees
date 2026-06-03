package com.companyemployees.domain.company;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Compania.
 * NO depende de Spring, MongoDB ni ningun framework.
 * Las invariantes (campos obligatorios) se validan en el constructor y en update().
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
        validate(nombre, direccion, telefono);
        if (employeeCount < 0) {
            throw new IllegalArgumentException("employeeCount no puede ser negativo");
        }
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fechaCreacion = fechaCreacion;
        this.employeeCount = employeeCount;
    }

    /** Factory method — crea una compania nueva sin id (MongoDB lo asigna). */
    public static Company create(String nombre, String direccion, String telefono) {
        return new Company(null, nombre, direccion, telefono, LocalDateTime.now(), 0);
    }

    /** Regla de negocio: incrementa el contador al agregar un empleado. */
    public void increaseEmployeeCount() {
        this.employeeCount++;
    }

    /** Regla de negocio: decrementa el contador al eliminar un empleado. */
    public void decreaseEmployeeCount() {
        if (this.employeeCount > 0) this.employeeCount--;
    }

    public void update(String nombre, String direccion, String telefono) {
        validate(nombre, direccion, telefono);
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    /**
     * Aplica un cambio parcial: solo modifica los campos con valor no nulo.
     * Cada campo presente debe cumplir su invariante (no vacio).
     */
    public void applyPatch(String nombre, String direccion, String telefono) {
        if (nombre != null) {
            if (nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre de la compania es obligatorio");
            }
            this.nombre = nombre;
        }
        if (direccion != null) {
            if (direccion.isBlank()) {
                throw new IllegalArgumentException("La direccion de la compania es obligatoria");
            }
            this.direccion = direccion;
        }
        if (telefono != null) {
            if (telefono.isBlank()) {
                throw new IllegalArgumentException("El telefono de la compania es obligatorio");
            }
            this.telefono = telefono;
        }
    }

    private static void validate(String nombre, String direccion, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la compania es obligatorio");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("La direccion de la compania es obligatoria");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El telefono de la compania es obligatorio");
        }
    }

    public CompanyId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public int getEmployeeCount() { return employeeCount; }
}
