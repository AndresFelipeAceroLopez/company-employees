package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.company.dto.CreateCompanyCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.company.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso transaccional: creacion masiva de companias.
 * Regla: o se guardan todas, o no se guarda ninguna.
 * Se respeta el orden del lote recibido (insercion en secuencia).
 */
@Service
public class CreateCompaniesBatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCompaniesBatchUseCase.class);

    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public CreateCompaniesBatchUseCase(CompanyRepository companyRepository, UnitOfWork unitOfWork) {
        this.companyRepository = companyRepository;
        this.unitOfWork = unitOfWork;
    }

    public List<CompanyResponse> execute(List<CreateCompanyCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("El lote no puede estar vacio");
        }
        log.info("Iniciando creacion masiva de {} companias", commands.size());

        return unitOfWork.execute(() -> {
            // Se guarda en el mismo orden recibido para no alterar la secuencia del lote.
            List<CompanyResponse> saved = new ArrayList<>(commands.size());
            for (CreateCompanyCommand command : commands) {
                Company company = Company.create(command.nombre(), command.direccion(), command.telefono());
                saved.add(CompanyResponse.from(companyRepository.save(company)));
            }

            log.info("Creacion masiva confirmada: {} companias", saved.size());
            return saved;
        });
    }
}
