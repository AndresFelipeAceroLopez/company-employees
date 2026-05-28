package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.common.pagination.PagedResult;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class GetPagedCompanyEmployeesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPagedCompanyEmployeesUseCase.class);
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public GetPagedCompanyEmployeesUseCase(EmployeeRepository employeeRepository,
                                           CompanyRepository companyRepository) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    public PagedResult<EmployeeResponse> execute(String companiaId, PageCriteria criteria) {
        log.info("Listando empleados de compania {} paginados", companiaId);
        CompanyId id = new CompanyId(companiaId);
        if (!companyRepository.existsById(id)) {
            throw new EntityNotFoundException("Compania no encontrada con id: " + companiaId);
        }
        return employeeRepository.findPagedByCompaniaId(id, criteria).map(EmployeeResponse::from);
    }

    /**
     * Variante asincrona del listado paginado por compania. Corre en el pool
     * "applicationTaskExecutor"; el I/O a MongoDB sigue siendo bloqueante.
     */
    @Async("applicationTaskExecutor")
    public CompletableFuture<PagedResult<EmployeeResponse>> getPagedAsync(String companiaId, PageCriteria criteria) {
        return CompletableFuture.completedFuture(execute(companiaId, criteria));
    }
}
