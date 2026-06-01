package com.companyemployees.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCompaniesBatchRequest(
        @NotEmpty(message = "El lote no puede estar vacio")
        @Size(max = 100, message = "El lote no puede exceder 100 companias")
        @Valid
        List<CreateCompanyRequest> companias
) {}
