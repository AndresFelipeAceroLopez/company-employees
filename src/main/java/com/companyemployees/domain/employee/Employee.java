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
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del empleado es obligatorio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido del empleado es obligatorio");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo del empleado es obligatorio");
        }
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("El cargo del empleado es obligatorio");
        }
        if (salario == null || salario.signum() <= 0) {
            throw new IllegalArgumentException("El salario debe ser mayor que 0");
        }
        if (companiaId == null) {
            throw new IllegalArgumentException("La companiaId es obligatoria");
        }
        if (status == null) {
            throw new IllegalArgumentException("El status es obligatorio");
        }
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
        if (salario == null || salario.signum() <= 0) {
            throw new IllegalArgumentException("El salario debe ser mayor que 0");
        }
        this.nombre = nombre;
        this.apellido = apellido;
        changeEmail(correo);
        this.cargo = cargo;
        this.salario = salario;
        this.status = status;
    }

    /**
     * Aplica un cambio parcial. Solo se modifican los campos con valor no nulo.
     * Las reglas (correo no vacio, salario positivo) se aplican aqui o en el use case.
     */
    public void applyPatch(String nombre, String apellido, String correo,
                           String cargo, BigDecimal salario, EmployeeStatus status) {
        if (nombre != null) this.nombre = nombre;
        if (apellido != null) this.apellido = apellido;
        if (correo != null) changeEmail(correo);
        if (cargo != null) this.cargo = cargo;
        if (salario != null) {
            if (salario.signum() <= 0) {
                throw new IllegalArgumentException("El salario debe ser mayor que 0");
            }
            this.salario = salario;
        }
        if (status != null) this.status = status;
    }

    public EmployeeId getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getCorreo() { return correo; }
    public String getCargo() { return cargo; }
    public BigDecimal getSalario() { return salario; }
    public CompanyId getCompaniaId() { return companiaId; }
    public EmployeeStatus getStatus() { return status; }
}
