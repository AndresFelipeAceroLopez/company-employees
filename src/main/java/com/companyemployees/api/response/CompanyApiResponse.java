package com.companyemployees.api.response;

import java.time.LocalDateTime;

public record CompanyApiResponse(
        String id,
        String nombre,
        String direccion,
        String telefono,
        LocalDateTime fechaCreacion,
        int employeeCount
) {}
