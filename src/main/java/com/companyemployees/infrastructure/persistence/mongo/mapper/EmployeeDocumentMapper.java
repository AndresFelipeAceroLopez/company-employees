package com.companyemployees.infrastructure.persistence.mongo.mapper;

import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import com.companyemployees.domain.employee.EmployeeStatus;
import com.companyemployees.infrastructure.persistence.mongo.document.EmployeeDocument;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDocumentMapper {

    public EmployeeDocument toDocument(Employee employee) {
        if (employee == null) return null;
        return new EmployeeDocument(
                employee.getId() != null ? employee.getId().value() : null,
                employee.getNombre(),
                employee.getApellido(),
                employee.getCorreo(),
                employee.getCargo(),
                employee.getSalario(),
                employee.getCompaniaId() != null ? employee.getCompaniaId().value() : null,
                employee.getStatus() != null ? employee.getStatus().name() : null
        );
    }

    public Employee toDomain(EmployeeDocument document) {
        if (document == null) return null;
        return new Employee(
                new EmployeeId(document.getId()),
                document.getNombre(),
                document.getApellido(),
                document.getCorreo(),
                document.getCargo(),
                document.getSalario(),
                document.getCompaniaId() != null ? new CompanyId(document.getCompaniaId()) : null,
                document.getStatus() != null ? EmployeeStatus.valueOf(document.getStatus()) : null
        );
    }
}
