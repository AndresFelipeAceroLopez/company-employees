package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import com.companyemployees.domain.employee.EmployeeStatus;
import com.companyemployees.support.UnitOfWorkStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEmployeesBatchUseCaseTest {

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    CompanyRepository companyRepository;
    @Mock
    UnitOfWork unitOfWork;

    DeleteEmployeesBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteEmployeesBatchUseCase(employeeRepository, companyRepository, unitOfWork);
        UnitOfWorkStubs.runInline(unitOfWork);
    }

    private Employee employee(String id) {
        return new Employee(new EmployeeId(id), "Ana", "Garcia", id + "@x.com", "Dev",
                new BigDecimal("1000"), new CompanyId("c1"), EmployeeStatus.ACTIVE);
    }

    @Test
    void eliminacionMasivaExitosa() {
        var ids = List.of("e1", "e2");
        when(employeeRepository.findAllByIds(anyList()))
                .thenReturn(List.of(employee("e1"), employee("e2")));
        when(companyRepository.findById(new CompanyId("c1")))
                .thenReturn(Optional.of(new Company(new CompanyId("c1"), "Acme", "Dir", "3001234567",
                        LocalDateTime.now(), 2)));

        useCase.execute(ids);

        verify(employeeRepository).deleteAllById(anyList());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void fallaSiAlgunIdNoExiste() {
        var ids = List.of("e1", "e2");
        when(employeeRepository.findAllByIds(anyList())).thenReturn(List.of(employee("e1")));

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(ids));
        verify(employeeRepository, never()).deleteAllById(anyList());
    }
}
