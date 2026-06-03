package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.EmployeeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetEmployeeUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetEmployeeUseCase.class);
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public GetEmployeeUseCase(EmployeeRepository employeeRepository,
                               CompanyRepository companyRepository) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    public List<EmployeeResponse> getAll() {
        log.info("Obteniendo todos los empleados");
        return employeeRepository.findAll().stream().map(EmployeeResponse::from).toList();
    }

    public long count() {
        return employeeRepository.count();
    }

    public EmployeeResponse getById(String id) {
        log.info("Buscando empleado con id: {}", id);
        return employeeRepository.findById(new EmployeeId(id))
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));
    }

    public List<EmployeeResponse> getByCompaniaId(String companiaId) {
        log.info("Obteniendo empleados de compania: {}", companiaId);
        if (!companyRepository.existsById(new CompanyId(companiaId))) {
            throw new EntityNotFoundException("Compania no encontrada con id: " + companiaId);
        }
        return employeeRepository.findByCompaniaId(new CompanyId(companiaId))
                .stream().map(EmployeeResponse::from).toList();
    }
}
