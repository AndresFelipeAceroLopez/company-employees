package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CreateCompanyCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.support.UnitOfWorkStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCompaniesBatchUseCaseTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    UnitOfWork unitOfWork;

    CreateCompaniesBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCompaniesBatchUseCase(companyRepository, unitOfWork);
        UnitOfWorkStubs.runInline(unitOfWork);
    }

    private CreateCompanyCommand cmd(String nombre) {
        return new CreateCompanyCommand(nombre, "Calle 1", "3001234567");
    }

    /** Simula la asignacion de id que hace Mongo al guardar, conservando los datos. */
    private void stubSaveAssignsId() {
        AtomicInteger seq = new AtomicInteger();
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            return new Company(new CompanyId("id-" + seq.incrementAndGet()),
                    c.getNombre(), c.getDireccion(), c.getTelefono(),
                    LocalDateTime.now(), c.getEmployeeCount());
        });
    }

    @Test
    void creacionMasivaExitosa() {
        var commands = List.of(cmd("Acme"), cmd("Globex"), cmd("Initech"));
        stubSaveAssignsId();

        List<CompanyResponse> result = useCase.execute(commands);

        assertEquals(3, result.size());
        verify(companyRepository, times(3)).save(any(Company.class));
    }

    @Test
    void respetaElOrdenDelLote() {
        var commands = List.of(cmd("Primera"), cmd("Segunda"), cmd("Tercera"));
        stubSaveAssignsId();

        List<CompanyResponse> result = useCase.execute(commands);

        assertEquals(List.of("Primera", "Segunda", "Tercera"),
                result.stream().map(CompanyResponse::nombre).toList());
    }

    @Test
    void fallaPorLoteVacio() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(List.of()));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void fallaPorLoteNulo() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
        verifyNoInteractions(companyRepository);
    }
}
