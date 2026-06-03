package com.companyemployees.domain.user;

/** Value Object: identificador de un Role. El dominio no sabe que Mongo usa String como _id. */
public record RoleId(String value) {
    public RoleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RoleId no puede ser nulo o vacio");
        }
    }
}
