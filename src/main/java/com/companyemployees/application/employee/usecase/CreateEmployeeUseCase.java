package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.CreateEmployeeCommand;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DomainException;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateEmployeeUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateEmployeeUseCase.class);
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UnitOfWork unitOfWork;

    public CreateEmployeeUseCase(CompanyRepository companyRepository,
                                  EmployeeRepository employeeRepository,
                                  UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.unitOfWork = unitOfWork;
    }

    /**
     * Caso transaccional: crea el empleado y actualiza el contador de la compañía.
     * Si falla cualquier operación, se hace rollback de todo (Unit of Work).
     */
    public EmployeeResponse execute(CreateEmployeeCommand command) {
        log.info("Iniciando transaccion: crear empleado {}", command.correo());

        return unitOfWork.execute(() -> {
            // 1. Verificar que la compañía existe (404 si no)
            Company company = companyRepository.findById(new CompanyId(command.companiaId()))
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Compania no encontrada con id: " + command.companiaId()));

            // 2. Verificar que el correo no esté duplicado (409 si existe)
            employeeRepository.findByCorreo(command.correo()).ifPresent(e -> {
                throw new DomainException("Ya existe un empleado con el correo: " + command.correo());
            });

            // 3. Crear el empleado
            Employee employee = Employee.create(
                    command.nombre(), command.apellido(), command.correo(),
                    command.cargo(), command.salario(), company.getId());

            Employee saved = employeeRepository.save(employee);

            // 4. Actualizar contador de la compañía (Unit of Work: ambas ops en la misma tx)
            company.increaseEmployeeCount();
            companyRepository.save(company);

            log.info("Transaccion confirmada: empleado {} creado en compania {}",
                    saved.getId().value(), command.companiaId());
            return EmployeeResponse.from(saved);
        });
    }
}
