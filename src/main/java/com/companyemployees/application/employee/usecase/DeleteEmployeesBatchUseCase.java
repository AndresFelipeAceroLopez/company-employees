package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Caso de uso transaccional: eliminacion masiva de empleados.
 * Regla: todos los ids deben existir antes de borrar. Si alguno falta, no se borra ninguno.
 * Actualiza employeeCount de cada compania afectada.
 */
@Service
public class DeleteEmployeesBatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteEmployeesBatchUseCase.class);

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public DeleteEmployeesBatchUseCase(EmployeeRepository employeeRepository,
                                       CompanyRepository companyRepository,
                                       UnitOfWork unitOfWork) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.unitOfWork = unitOfWork;
    }

    public void execute(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("El lote de ids no puede estar vacio");
        }

        // Deduplicar manteniendo orden.
        List<EmployeeId> employeeIds = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String raw : ids) {
            if (seen.add(raw)) employeeIds.add(new EmployeeId(raw));
        }

        log.info("Eliminacion masiva: {} ids unicos solicitados", employeeIds.size());

        unitOfWork.execute(() -> {
            List<Employee> found = employeeRepository.findAllByIds(employeeIds);
            if (found.size() != employeeIds.size()) {
                Set<String> foundIds = new HashSet<>();
                found.forEach(e -> foundIds.add(e.getId().value()));
                String missing = employeeIds.stream()
                        .map(EmployeeId::value)
                        .filter(id -> !foundIds.contains(id))
                        .findFirst()
                        .orElse("?");
                throw new EntityNotFoundException("Empleado no encontrado con id: " + missing);
            }

            employeeRepository.deleteAllById(employeeIds);

            // Decrementar contadores por compania afectada.
            Map<String, Integer> deltas = new HashMap<>();
            for (Employee e : found) {
                deltas.merge(e.getCompaniaId().value(), 1, Integer::sum);
            }
            for (Map.Entry<String, Integer> entry : deltas.entrySet()) {
                companyRepository.findById(new CompanyId(entry.getKey())).ifPresent(company -> {
                    for (int i = 0; i < entry.getValue(); i++) company.decreaseEmployeeCount();
                    companyRepository.save(company);
                });
            }
            log.info("Eliminacion masiva confirmada: {} empleados", found.size());
        });
    }
}
