package com.companyemployees.application.company.dto;

import com.companyemployees.domain.company.Company;

import java.time.LocalDateTime;

/** DTO de salida para Compañía — lo que devuelve el caso de uso al controlador */
public record CompanyResponse(
        String id,
        String nombre,
        String direccion,
        String telefono,
        LocalDateTime fechaCreacion,
        int employeeCount
) {
    /** Convierte una entidad de dominio a DTO de respuesta */
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId().value(),
                company.getNombre(),
                company.getDireccion(),
                company.getTelefono(),
                company.getFechaCreacion(),
                company.getEmployeeCount()
        );
    }
}
