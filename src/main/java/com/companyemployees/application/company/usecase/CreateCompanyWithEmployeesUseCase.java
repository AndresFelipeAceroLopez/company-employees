package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CompanyWithEmployeesResponse;
import com.companyemployees.application.company.dto.CreateCompanyWithEmployeesCommand;
import com.companyemployees.application.company.dto.CreateCompanyWithEmployeesCommand.EmployeeData;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Caso de uso transaccional: Crear una compania con varios empleados.
 * Si falla la creacion de un empleado, no se guarda nada (Unit of Work).
 */
@Service
public class CreateCompanyWithEmployeesUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCompanyWithEmployeesUseCase.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UnitOfWork unitOfWork;

    public CreateCompanyWithEmployeesUseCase(
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.unitOfWork = unitOfWork;
    }

    public CompanyWithEmployeesResponse execute(CreateCompanyWithEmployeesCommand command) {
        log.info("Iniciando transaccion: crear compania '{}' con {} empleados",
                command.nombre(), command.empleados().size());

        // Validacion previa: correos duplicados dentro del lote y salarios positivos.
        Set<String> seen = new HashSet<>();
        for (EmployeeData empData : command.empleados()) {
            if (empData.salario() == null || empData.salario().signum() <= 0) {
                throw new IllegalArgumentException(
                        "El salario debe ser mayor que 0 para " + empData.correo());
            }
            if (!seen.add(empData.correo())) {
                throw new DuplicateResourceException(
                        "Correo duplicado en el lote: " + empData.correo());
            }
        }

        return unitOfWork.execute(() -> {
            // Validacion contra BD: ningun correo existe ya.
            List<String> correos = command.empleados().stream().map(EmployeeData::correo).toList();
            List<Employee> existing = employeeRepository.findByCorreoIn(correos);
            if (!existing.isEmpty()) {
                throw new DuplicateResourceException(
                        "Ya existe un empleado con el correo: " + existing.get(0).getCorreo());
            }

            Company company = Company.create(command.nombre(), command.direccion(), command.telefono());
            Company savedCompany = companyRepository.save(company);

            List<Employee> savedEmployees = new ArrayList<>();
            for (EmployeeData empData : command.empleados()) {
                Employee employee = Employee.create(
                        empData.nombre(),
                        empData.apellido(),
                        empData.correo(),
                        empData.cargo(),
                        empData.salario(),
                        savedCompany.getId()
                );
                savedEmployees.add(employeeRepository.save(employee));
                savedCompany.increaseEmployeeCount();
            }
            savedCompany = companyRepository.save(savedCompany);

            log.info("Transaccion completada: compania '{}' con {} empleados",
                    savedCompany.getNombre(), savedEmployees.size());

            return new CompanyWithEmployeesResponse(
                    CompanyResponse.from(savedCompany),
                    savedEmployees.stream().map(EmployeeResponse::from).toList()
            );
        });
    }
}
