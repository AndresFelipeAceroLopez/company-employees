package com.companyemployees.infrastructure.persistence.mongo.mapper;

import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.infrastructure.persistence.mongo.document.CompanyDocument;
import org.springframework.stereotype.Component;

@Component
public class CompanyDocumentMapper {

    public CompanyDocument toDocument(Company company) {
        if (company == null) return null;
        return new CompanyDocument(
                company.getId() != null ? company.getId().value() : null,
                company.getNombre(),
                company.getDireccion(),
                company.getTelefono(),
                company.getFechaCreacion(),
                company.getEmployeeCount()
        );
    }

    public Company toDomain(CompanyDocument document) {
        if (document == null) return null;
        return new Company(
                new CompanyId(document.getId()),
                document.getNombre(),
                document.getDireccion(),
                document.getTelefono(),
                document.getFechaCreacion(),
                document.getEmployeeCount()
        );
    }
}
