package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.CreateEmployeeCommand;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DomainException;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Caso de uso transaccional: creacion masiva de empleados.
 * Regla: o se guardan todos, o no se guarda ninguno.
 */
@Service
public class CreateEmployeesBatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateEmployeesBatchUseCase.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UnitOfWork unitOfWork;

    public CreateEmployeesBatchUseCase(CompanyRepository companyRepository,
                                       EmployeeRepository employeeRepository,
                                       UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.unitOfWork = unitOfWork;
    }

    public List<EmployeeResponse> execute(List<CreateEmployeeCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("El lote no puede estar vacio");
        }
        log.info("Iniciando creacion masiva de {} empleados", commands.size());

        // ----- Validaciones previas (todo o nada) -----
        // 1. Duplicados dentro del lote por correo.
        Set<String> seen = new HashSet<>();
        for (CreateEmployeeCommand c : commands) {
            if (c.salario() == null || c.salario().signum() <= 0) {
                throw new IllegalArgumentException("El salario debe ser mayor que 0 para " + c.correo());
            }
            if (!seen.add(c.correo())) {
                throw new DuplicateResourceException("Correo duplicado en el lote: " + c.correo());
            }
        }

        return unitOfWork.execute(() -> {
            // 2. Companias requeridas existen.
            Map<String, Company> companies = new LinkedHashMap<>();
            for (CreateEmployeeCommand c : commands) {
                if (!companies.containsKey(c.companiaId())) {
                    Company company = companyRepository.findById(new CompanyId(c.companiaId()))
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Compania no encontrada con id: " + c.companiaId()));
                    companies.put(c.companiaId(), company);
                }
            }

            // 3. Ningun correo ya existe en BD.
            List<String> correos = commands.stream().map(CreateEmployeeCommand::correo).toList();
            List<Employee> existing = employeeRepository.findByCorreoIn(correos);
            if (!existing.isEmpty()) {
                String dup = existing.get(0).getCorreo();
                throw new DuplicateResourceException("Ya existe un empleado con el correo: " + dup);
            }

            // 4. Construir entidades y guardar.
            List<Employee> toSave = new ArrayList<>(commands.size());
            Map<String, Integer> deltas = new HashMap<>();
            for (CreateEmployeeCommand c : commands) {
                Company company = companies.get(c.companiaId());
                Employee employee = Employee.create(
                        c.nombre(), c.apellido(), c.correo(),
                        c.cargo(), c.salario(), company.getId());
                toSave.add(employee);
                deltas.merge(c.companiaId(), 1, Integer::sum);
            }

            List<Employee> saved = employeeRepository.saveAll(toSave);

            // 5. Actualizar contadores de companias.
            for (Map.Entry<String, Integer> entry : deltas.entrySet()) {
                Company company = companies.get(entry.getKey());
                for (int i = 0; i < entry.getValue(); i++) company.increaseEmployeeCount();
                companyRepository.save(company);
            }

            log.info("Creacion masiva confirmada: {} empleados", saved.size());
            return saved.stream().map(EmployeeResponse::from).toList();
        });
    }
}
