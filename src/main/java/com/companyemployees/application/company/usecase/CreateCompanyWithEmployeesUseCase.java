package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso transaccional: Crear una compañía con varios empleados.
 * Demuestra el uso de Unit of Work para garantizar atomicidad.
 * Si falla la creación de un empleado, no se guarda nada.
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
        log.info("Iniciando transacción: crear compañía '{}' con {} empleados",
                command.nombre(), command.empleados().size());

        return unitOfWork.execute(() -> {
            try {
                // 1. Crear y guardar la compañía
                log.info("Creando compañía: {}", command.nombre());
                Company company = Company.create(
                        command.nombre(),
                        command.direccion(),
                        command.telefono()
                );
                Company savedCompany = companyRepository.save(company);
                log.info("Creación de una compañía: '{}' con ID: {}", savedCompany.getNombre(), savedCompany.getId().value());

                // 2. Crear y guardar los empleados
                List<Employee> savedEmployees = new ArrayList<>();
                for (EmployeeData empData : command.empleados()) {
                    log.info("Creando empleado: {} {}", empData.nombre(), empData.apellido());
                    
                    Employee employee = Employee.create(
                            empData.nombre(),
                            empData.apellido(),
                            empData.correo(),
                            empData.cargo(),
                            BigDecimal.valueOf(empData.salario()),
                            savedCompany.getId()
                    );
                    
                    Employee savedEmployee = employeeRepository.save(employee);
                    savedEmployees.add(savedEmployee);
                    log.info("Creación de un empleado: {} {} con ID: {} en compañía: {}", 
                            savedEmployee.getNombre(), savedEmployee.getApellido(), savedEmployee.getId().value(), savedCompany.getId().value());
                }

                log.info("Transacción completada: compañía y {} empleados creados exitosamente",
                        savedEmployees.size());

                // 3. Construir respuesta
                return new CompanyWithEmployeesResponse(
                        toCompanyResponse(savedCompany),
                        savedEmployees.stream()
                                .map(this::toEmployeeResponse)
                                .toList()
                );

            } catch (Exception e) {
                log.error("Error en transacción: {}. Realizando rollback automático", e.getMessage());
                throw new RuntimeException("Error al crear compañía con empleados: " + e.getMessage(), e);
            }
        });
    }

    private CompanyResponse toCompanyResponse(Company company) {
        return new CompanyResponse(
                company.getId().value(),
                company.getNombre(),
                company.getDireccion(),
                company.getTelefono(),
                company.getFechaCreacion(),
                company.getEmployeeCount()
        );
    }

    private EmployeeResponse toEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId().value(),
                employee.getNombre(),
                employee.getApellido(),
                employee.getCorreo(),
                employee.getCargo(),
                employee.getSalario(),
                employee.getCompaniaId().value(),
                employee.getStatus().name()
        );
    }

    // DTOs internos
    public record CreateCompanyWithEmployeesCommand(
            String nombre,
            String direccion,
            String telefono,
            List<EmployeeData> empleados
    ) {}

    public record EmployeeData(
            String nombre,
            String apellido,
            String correo,
            String cargo,
            Double salario
    ) {}

    public record CompanyWithEmployeesResponse(
            CompanyResponse compania,
            List<EmployeeResponse> empleados
    ) {}
}
