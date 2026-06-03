package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.common.dto.OptionResponse;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Devuelve la lista de empleados como opciones (nombre + id) para un select del front.
 * El label incluye el cargo para desambiguar nombres repetidos; el value es el id.
 */
@Service
public class GetEmployeeOptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetEmployeeOptionsUseCase.class);
    private final EmployeeRepository employeeRepository;

    public GetEmployeeOptionsUseCase(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<OptionResponse> getOptions() {
        log.info("Obteniendo opciones de empleados");
        return employeeRepository.findAll().stream()
                .map(e -> new OptionResponse(
                        e.getNombre() + " " + e.getApellido() + " (" + e.getCargo() + ")",
                        e.getId().value()))
                .toList();
    }
}
