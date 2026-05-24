package com.companyemployees.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request para crear una compañía con empleados en una sola transacción.
 */
public class CreateCompanyWithEmployeesRequest {

    @NotBlank(message = "El nombre de la compañía es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotEmpty(message = "Debe incluir al menos un empleado")
    @Valid
    private List<EmployeeData> empleados;

    public CreateCompanyWithEmployeesRequest() {}

    public CreateCompanyWithEmployeesRequest(String nombre, String direccion, String telefono, List<EmployeeData> empleados) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.empleados = empleados;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public List<EmployeeData> getEmpleados() { return empleados; }
    public void setEmpleados(List<EmployeeData> empleados) { this.empleados = empleados; }

    public static class EmployeeData {
        @NotBlank(message = "El nombre del empleado es obligatorio")
        private String nombre;

        @NotBlank(message = "El apellido del empleado es obligatorio")
        private String apellido;

        @NotBlank(message = "El correo del empleado es obligatorio")
        private String correo;

        @NotBlank(message = "El cargo es obligatorio")
        private String cargo;

        private Double salario;

        public EmployeeData() {}

        public EmployeeData(String nombre, String apellido, String correo, String cargo, Double salario) {
            this.nombre = nombre;
            this.apellido = apellido;
            this.correo = correo;
            this.cargo = cargo;
            this.salario = salario;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getCargo() { return cargo; }
        public void setCargo(String cargo) { this.cargo = cargo; }
        public Double getSalario() { return salario; }
        public void setSalario(Double salario) { this.salario = salario; }
    }
}
