package com.companyemployees.domain.auth;

import com.companyemployees.domain.common.DomainException;

/** Credenciales invalidas: usuario inexistente o password incorrecta. Produce HTTP 401. */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
