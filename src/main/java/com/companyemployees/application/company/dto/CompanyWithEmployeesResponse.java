package com.companyemployees.application.company.dto;

import com.companyemployees.application.employee.dto.EmployeeResponse;

import java.util.List;

/** DTO de salida para la creacion de una compania con sus empleados. */
public record CompanyWithEmployeesResponse(
        CompanyResponse compania,
        List<EmployeeResponse> empleados
) {}
