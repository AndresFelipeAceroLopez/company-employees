package com.companyemployees.domain.employee;

public record EmployeeId(String value) {
    public EmployeeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EmployeeId no puede ser nulo o vacío");
        }
    }

    public String getValue() {
        return value;
    }
}
