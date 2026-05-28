package com.companyemployees.api.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DeleteEmployeesBatchRequest(
        @NotEmpty(message = "Debe enviar al menos un id")
        @Size(max = 100, message = "El lote no puede exceder 100 ids")
        List<String> ids
) {}
