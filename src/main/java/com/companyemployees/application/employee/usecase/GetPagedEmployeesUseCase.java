package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.common.pagination.PageCriteria;
import com.companyemployees.application.common.pagination.PagedResult;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class GetPagedEmployeesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPagedEmployeesUseCase.class);
    private final EmployeeRepository employeeRepository;

    public GetPagedEmployeesUseCase(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public PagedResult<EmployeeResponse> execute(PageCriteria criteria) {
        log.info("Listando empleados paginados: pagina={}, tamano={}, orden={}, dir={}, buscar={}",
                criteria.pagina(), criteria.tamano(), criteria.orden(), criteria.dir(), criteria.buscar());
        return employeeRepository.findPaged(criteria).map(EmployeeResponse::from);
    }

    /**
     * Variante asincrona del listado paginado. Se ejecuta en el pool
     * "applicationTaskExecutor". El acceso a MongoDB sigue siendo bloqueante;
     * esto solo delega la espera a otro hilo (no es I/O reactivo real).
     */
    @Async("applicationTaskExecutor")
    public CompletableFuture<PagedResult<EmployeeResponse>> getPagedAsync(PageCriteria criteria) {
        return CompletableFuture.completedFuture(execute(criteria));
    }
}
