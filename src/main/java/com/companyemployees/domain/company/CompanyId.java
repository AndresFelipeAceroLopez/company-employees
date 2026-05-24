package com.companyemployees.domain.company;

/**
 * Value Object que representa el identificador de una Compañía.
 * Usa record de Java — inmutable por diseño.
 * El dominio no sabe que MongoDB usa String como _id.
 */
public record CompanyId(String value) {
    public CompanyId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CompanyId no puede ser nulo o vacío");
        }
    }
}
