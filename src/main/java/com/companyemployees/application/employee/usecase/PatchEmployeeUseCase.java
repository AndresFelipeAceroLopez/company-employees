package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.dto.PatchEmployeeCommand;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PatchEmployeeUseCase {

    private static final Logger log = LoggerFactory.getLogger(PatchEmployeeUseCase.class);
    private final EmployeeRepository employeeRepository;
    private final UnitOfWork unitOfWork;

    public PatchEmployeeUseCase(EmployeeRepository employeeRepository, UnitOfWork unitOfWork) {
        this.employeeRepository = employeeRepository;
        this.unitOfWork = unitOfWork;
    }

    public EmployeeResponse execute(String id, PatchEmployeeCommand command) {
        log.info("PATCH empleado {}", id);
        return unitOfWork.execute(() -> {
            Employee employee = employeeRepository.findById(new EmployeeId(id))
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));

            if (command.correo() != null && !command.correo().equals(employee.getCorreo())) {
                employeeRepository.findByCorreo(command.correo()).ifPresent(e -> {
                    throw new DuplicateResourceException(
                            "Ya existe un empleado con el correo: " + command.correo());
                });
            }

            employee.applyPatch(
                    command.nombre(),
                    command.apellido(),
                    command.correo(),
                    command.cargo(),
                    command.salario(),
                    command.status()
            );
            Employee saved = employeeRepository.save(employee);
            return EmployeeResponse.from(saved);
        });
    }
}
