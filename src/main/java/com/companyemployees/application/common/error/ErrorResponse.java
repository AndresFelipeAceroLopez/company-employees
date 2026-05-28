package com.companyemployees.application.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Formato uniforme de error para toda la API.
 * <pre>
 * {
 *   "mensaje": "...",
 *   "errores": [ { "campo": "...", "detalle": "..." } ]
 * }
 * </pre>
 * Vive en Application/common para que tanto api (GlobalExceptionHandler)
 * como infrastructure (entry points de seguridad) compartan el mismo shape
 * sin que infrastructure tenga que importar de api.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String mensaje, List<FieldError> errores) {

    public static ErrorResponse of(String mensaje) {
        return new ErrorResponse(mensaje, null);
    }

    public static ErrorResponse of(String mensaje, List<FieldError> errores) {
        return new ErrorResponse(mensaje, errores);
    }

    public record FieldError(String campo, String detalle) {}
}
