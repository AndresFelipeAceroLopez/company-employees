package com.companyemployees.domain.common;

/** Excepción base del dominio */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
