package com.companyemployees.domain.user;

/** Value Object: identificador de un Permission. El dominio no sabe que Mongo usa String como _id. */
public record PermissionId(String value) {
    public PermissionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PermissionId no puede ser nulo o vacio");
        }
    }
}
