package com.companyemployees.api.response;

import java.util.List;

/** Wire format HTTP para la creacion de una compania con sus empleados. */
public record CompanyWithEmployeesApiResponse(
        CompanyApiResponse compania,
        List<EmployeeApiResponse> empleados
) {}
