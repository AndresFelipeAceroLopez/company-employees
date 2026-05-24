package com.companyemployees.application.company.dto;

/** Comando para actualizar una compañía */
public record UpdateCompanyCommand(String nombre, String direccion, String telefono) {}
