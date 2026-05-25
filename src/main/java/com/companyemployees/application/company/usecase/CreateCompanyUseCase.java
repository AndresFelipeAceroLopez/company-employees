package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CreateCompanyCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DomainException;
import com.companyemployees.domain.company.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCompanyUseCase.class);

    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public CreateCompanyUseCase(CompanyRepository companyRepository, UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.unitOfWork = unitOfWork;
    }

    public CompanyResponse execute(CreateCompanyCommand command) {
        log.info("Creando compania: {}", command.nombre());

        return unitOfWork.execute(() -> {
            Company company = Company.create(command.nombre(), command.direccion(), command.telefono());
            Company saved = companyRepository.save(company);
            log.info("Creación de una compañía: '{}' con ID: {}", saved.getNombre(), saved.getId().value());
            return CompanyResponse.from(saved);
        });
    }
}
