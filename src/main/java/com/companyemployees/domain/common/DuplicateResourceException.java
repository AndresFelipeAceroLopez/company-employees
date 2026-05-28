package com.companyemployees.domain.common;

/** Lanzada cuando una operacion viola una restriccion de unicidad — produce HTTP 409. */
public class DuplicateResourceException extends DomainException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
