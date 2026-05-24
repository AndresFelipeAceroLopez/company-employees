package com.companyemployees.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "employees")
public class EmployeeDocument {

    @Id
    private String id;
    private String nombre;
    private String apellido;

    @Indexed(unique = true)
    private String correo;

    private String cargo;
    private BigDecimal salario;
    private String companiaId;
    private String status;

    public EmployeeDocument() {}

    public EmployeeDocument(String id, String nombre, String apellido, String correo, String cargo, BigDecimal salario, String companiaId, String status) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.cargo = cargo;
        this.salario = salario;
        this.companiaId = companiaId;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public String getCompaniaId() { return companiaId; }
    public void setCompaniaId(String companiaId) { this.companiaId = companiaId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
