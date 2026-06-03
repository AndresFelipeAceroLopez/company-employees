package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.PatchCompanyCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.support.UnitOfWorkStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatchCompanyUseCaseTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    UnitOfWork unitOfWork;

    PatchCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PatchCompanyUseCase(companyRepository, unitOfWork);
        UnitOfWorkStubs.runInline(unitOfWork);
    }

    private Company company() {
        return new Company(new CompanyId("c1"), "Tech Solutions", "Calle 1, Bogota",
                "3001234567", LocalDateTime.now(), 4);
    }

    @Test
    void patchModificaSoloLosCamposEnviados() {
        when(companyRepository.findById(new CompanyId("c1"))).thenReturn(Optional.of(company()));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        // Solo se envia el telefono; el resto queda igual.
        PatchCompanyCommand command = new PatchCompanyCommand(null, null, "3009998877");
        CompanyResponse result = useCase.execute("c1", command);

        assertEquals("3009998877", result.telefono());
        assertEquals("Tech Solutions", result.nombre());
        assertEquals("Calle 1, Bogota", result.direccion());
    }

    @Test
    void fallaSiLaCompaniaNoExiste() {
        when(companyRepository.findById(new CompanyId("c1"))).thenReturn(Optional.empty());

        PatchCompanyCommand command = new PatchCompanyCommand("Nuevo", null, null);
        assertThrows(EntityNotFoundException.class, () -> useCase.execute("c1", command));
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void fallaSiUnCampoEnviadoEsInvalido() {
        when(companyRepository.findById(new CompanyId("c1"))).thenReturn(Optional.of(company()));

        // nombre en blanco: la invariante del dominio debe rechazarlo.
        PatchCompanyCommand command = new PatchCompanyCommand("   ", null, null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("c1", command));
        verify(companyRepository, never()).save(any(Company.class));
    }
}
