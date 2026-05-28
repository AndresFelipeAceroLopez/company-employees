package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.CreateEmployeeCommand;
import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEmployeesBatchUseCaseTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    UnitOfWork unitOfWork;

    CreateEmployeesBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateEmployeesBatchUseCase(companyRepository, employeeRepository, unitOfWork);
        UnitOfWorkStubs.runInline(unitOfWork);
    }

    private CreateEmployeeCommand cmd(String correo, String companiaId, String salario) {
        return new CreateEmployeeCommand("Ana", "Garcia", correo, "Dev", new BigDecimal(salario), companiaId);
    }

    private Company company(String id) {
        return new Company(new CompanyId(id), "Acme", "Dir 1", "3001234567", LocalDateTime.now(), 0);
    }

    private Employee employee(String id, String correo, String companiaId) {
        return new Employee(new EmployeeId(id), "Ana", "Garcia", correo, "Dev",
                new BigDecimal("1000"), new CompanyId(companiaId), EmployeeStatus.ACTIVE);
    }

    @Test
    void creacionMasivaExitosa() {
        var commands = List.of(cmd("a@x.com", "c1", "1000"), cmd("b@x.com", "c1", "2000"));
        when(companyRepository.findById(new CompanyId("c1"))).thenReturn(Optional.of(company("c1")));
        when(employeeRepository.findByCorreoIn(anyList())).thenReturn(List.of());
        when(employeeRepository.saveAll(anyList())).thenReturn(
                List.of(employee("e1", "a@x.com", "c1"), employee("e2", "b@x.com", "c1")));

        List<EmployeeResponse> result = useCase.execute(commands);

        assertEquals(2, result.size());
        verify(employeeRepository).saveAll(anyList());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void fallaPorCorreoYaExistenteEnBD() {
        var commands = List.of(cmd("a@x.com", "c1", "1000"));
        when(companyRepository.findById(new CompanyId("c1"))).thenReturn(Optional.of(company("c1")));
        when(employeeRepository.findByCorreoIn(anyList()))
                .thenReturn(List.of(employee("e9", "a@x.com", "c1")));

        assertThrows(DuplicateResourceException.class, () -> useCase.execute(commands));
        verify(employeeRepository, never()).saveAll(anyList());
    }

    @Test
    void fallaPorCorreoDuplicadoDentroDelLote() {
        var commands = List.of(cmd("dup@x.com", "c1", "1000"), cmd("dup@x.com", "c1", "2000"));

        assertThrows(DuplicateResourceException.class, () -> useCase.execute(commands));
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void fallaPorSalarioNoPositivo() {
        var commands = List.of(cmd("a@x.com", "c1", "-5"));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(commands));
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void fallaPorCompaniaInexistente() {
        var commands = List.of(cmd("a@x.com", "cX", "1000"));
        when(companyRepository.findById(new CompanyId("cX"))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(commands));
        verify(employeeRepository, never()).saveAll(anyList());
    }
}
