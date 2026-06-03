package com.companyemployees.application.company.dto;

/** Comando para actualizacion parcial de una compania. Campos nulos = no se tocan. */
public record PatchCompanyCommand(String nombre, String direccion, String telefono) {}
