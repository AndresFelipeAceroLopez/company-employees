package com.companyemployees.application.common.dto;

/**
 * DTO generico para poblar menus desplegables (selects) en el front.
 * label = texto visible para el humano (ej. nombre); value = id que se envia al guardar.
 * Asi el usuario elige por nombre sin conocer ni escribir el id.
 */
public record OptionResponse(String label, String value) {}
