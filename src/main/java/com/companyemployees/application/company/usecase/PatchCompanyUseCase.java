package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.PatchCompanyCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PatchCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(PatchCompanyUseCase.class);
    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public PatchCompanyUseCase(CompanyRepository companyRepository, UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.unitOfWork = unitOfWork;
    }

    public CompanyResponse execute(String id, PatchCompanyCommand command) {
        log.info("PATCH compania {}", id);
        return unitOfWork.execute(() -> {
            Company company = companyRepository.findById(new CompanyId(id))
                    .orElseThrow(() -> new EntityNotFoundException("Compania no encontrada con id: " + id));
            company.applyPatch(command.nombre(), command.direccion(), command.telefono());
            Company saved = companyRepository.save(company);
            return CompanyResponse.from(saved);
        });
    }
}
