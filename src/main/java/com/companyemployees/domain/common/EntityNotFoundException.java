package com.companyemployees.domain.common;

/** Lanzada cuando un recurso no existe — produce HTTP 404 */
public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
