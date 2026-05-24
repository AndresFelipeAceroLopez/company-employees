package com.companyemployees.application.company.dto;

/** Comando para crear una compañía (entrada al caso de uso) */
public record CreateCompanyCommand(String nombre, String direccion, String telefono) {}
