package com.companyemployees.application.company.usecase;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DomainException;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteCompanyUseCase.class);
    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public DeleteCompanyUseCase(CompanyRepository companyRepository, UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.unitOfWork = unitOfWork;
    }

    public void execute(String id) {
        log.info("Eliminando compania con id: {}", id);
        unitOfWork.execute(() -> {
            CompanyId companyId = new CompanyId(id);
            if (!companyRepository.existsById(companyId)) {
                throw new EntityNotFoundException("Compania no encontrada con id: " + id);
            }
            // Regla de negocio: no se puede eliminar si tiene empleados
            long employeeCount = companyRepository.countEmployees(companyId);
            if (employeeCount > 0) {
                throw new DomainException("No se puede eliminar la compania porque tiene " + employeeCount + " empleado(s) asociado(s)");
            }
            companyRepository.deleteById(companyId);
            log.info("Compania eliminada: {}", id);
        });
    }
}
