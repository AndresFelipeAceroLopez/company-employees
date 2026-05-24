package com.companyemployees.domain.employee;

import com.companyemployees.domain.company.CompanyId;

import java.math.BigDecimal;

/**
 * Entidad de dominio: Empleado.
 * NO depende de Spring, MongoDB ni ningún framework.
 * La referencia a la compañía es por ID (no hay join en MongoDB).
 */
public class Employee {

    private EmployeeId id;
    private String nombre;
    private String apellido;
    private String correo;
    private String cargo;
    private BigDecimal salario;
    private CompanyId companiaId;
    private EmployeeStatus status;

    public Employee(EmployeeId id, String nombre, String apellido, String correo,
                    String cargo, BigDecimal salario, CompanyId companiaId, EmployeeStatus status) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.cargo = cargo;
        this.salario = salario;
        this.companiaId = companiaId;
        this.status = status;
    }

    /** Factory method — crea un empleado nuevo con estado ACTIVE */
    public static Employee create(String nombre, String apellido, String correo,
                                   String cargo, BigDecimal salario, CompanyId companiaId) {
        return new Employee(null, nombre, apellido, correo, cargo, salario, companiaId, EmployeeStatus.ACTIVE);
    }

    /** Regla de negocio: cambiar correo con validación */
    public void changeEmail(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo del empleado es obligatorio");
        }
        this.correo = correo;
    }

    public void update(String nombre, String apellido, String correo,
                       String cargo, BigDecimal salario, EmployeeStatus status) {
        this.nombre = nombre;
        this.apellido = apellido;
        changeEmail(correo);
        this.cargo = cargo;
        this.salario = salario;
        this.status = status;
    }

    public EmployeeId getId() { return id; }
    public void setId(EmployeeId id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getCorreo() { return correo; }
    public String getCargo() { return cargo; }
    public BigDecimal getSalario() { return salario; }
    public CompanyId getCompaniaId() { return companiaId; }
    public EmployeeStatus getStatus() { return status; }
}
