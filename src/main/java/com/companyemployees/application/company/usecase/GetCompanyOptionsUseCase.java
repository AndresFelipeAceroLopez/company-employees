package com.companyemployees.application.company.usecase;

import com.companyemployees.application.common.dto.OptionResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Devuelve la lista de companias como opciones (nombre + id) para un select del front.
 * El usuario elige por nombre; el front envia el id al crear/asignar.
 */
@Service
public class GetCompanyOptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetCompanyOptionsUseCase.class);
    private final CompanyRepository companyRepository;

    public GetCompanyOptionsUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<OptionResponse> getOptions() {
        log.info("Obteniendo opciones de companias");
        return companyRepository.findAll().stream()
                .map(c -> new OptionResponse(c.getNombre(), c.getId().value()))
                .toList();
    }
}
