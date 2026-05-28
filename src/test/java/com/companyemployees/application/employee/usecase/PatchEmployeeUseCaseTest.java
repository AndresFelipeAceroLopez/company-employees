package com.companyemployees.application.employee.usecase;

import com.companyemployees.application.employee.dto.EmployeeResponse;
import com.companyemployees.application.employee.dto.PatchEmployeeCommand;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.common.EntityNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatchEmployeeUseCaseTest {

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    UnitOfWork unitOfWork;

    PatchEmployeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PatchEmployeeUseCase(employeeRepository, unitOfWork);
        UnitOfWorkStubs.runInline(unitOfWork);
    }

    private Employee employee() {
        return new Employee(new EmployeeId("e1"), "Ana", "Garcia", "ana@x.com", "Dev",
                new BigDecimal("1000"), new com.companyemployees.domain.company.CompanyId("c1"),
                EmployeeStatus.ACTIVE);
    }

    @Test
    void patchModificaSoloLosCamposEnviados() {
        when(employeeRepository.findById(new EmployeeId("e1"))).thenReturn(Optional.of(employee()));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        // Solo se envia el cargo; el resto queda igual.
        PatchEmployeeCommand command = new PatchEmployeeCommand(null, null, null, "Lead", null, null);
        EmployeeResponse result = useCase.execute("e1", command);

        assertEquals("Lead", result.cargo());
        assertEquals("Ana", result.nombre());
        assertEquals("ana@x.com", result.correo());
        assertEquals(new BigDecimal("1000"), result.salario());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void fallaSiElEmpleadoNoExiste() {
        when(employeeRepository.findById(new EmployeeId("e1"))).thenReturn(Optional.empty());

        PatchEmployeeCommand command = new PatchEmployeeCommand(null, null, null, "Lead", null, null);
        assertThrows(EntityNotFoundException.class, () -> useCase.execute("e1", command));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void fallaSiElNuevoCorreoYaExiste() {
        when(employeeRepository.findById(new EmployeeId("e1"))).thenReturn(Optional.of(employee()));
        when(employeeRepository.findByCorreo("otro@x.com"))
                .thenReturn(Optional.of(employee()));

        PatchEmployeeCommand command = new PatchEmployeeCommand(null, null, "otro@x.com", null, null, null);
        assertThrows(DuplicateResourceException.class, () -> useCase.execute("e1", command));
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}
