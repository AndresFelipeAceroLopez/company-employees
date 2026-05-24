package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteEmployeeUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteEmployeeUseCase.class);
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public DeleteEmployeeUseCase(EmployeeRepository employeeRepository,
                                  CompanyRepository companyRepository,
                                  UnitOfWork unitOfWork) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.unitOfWork = unitOfWork;
    }

    public void execute(String id) {
        log.info("Eliminando empleado con id: {}", id);
        unitOfWork.execute(() -> {
            Employee employee = employeeRepository.findById(new EmployeeId(id))
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));

            employeeRepository.deleteById(new EmployeeId(id));

            // Actualizar contador de la compañía
            companyRepository.findById(employee.getCompaniaId()).ifPresent(company -> {
                company.decreaseEmployeeCount();
                companyRepository.save(company);
            });

            log.info("Empleado eliminado: {}", id);
        });
    }
}
