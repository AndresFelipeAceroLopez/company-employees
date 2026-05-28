package com.companyemployees.domain.user;

public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId no puede ser nulo o vacio");
        }
    }
}
