package com.companyemployees.api.response;

import java.math.BigDecimal;

public record EmployeeApiResponse(
        String nombre,
        String apellido,
        String correo,
        String cargo,
        BigDecimal salario,
        String status
) {}
