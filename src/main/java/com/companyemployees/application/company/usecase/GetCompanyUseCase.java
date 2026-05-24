package com.companyemployees.application.company.usecase;

import com.companyemployees.application.company.dto.CompanyResponse;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCompanyUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetCompanyUseCase.class);
    private final CompanyRepository companyRepository;

    public GetCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<CompanyResponse> getAll() {
        log.info("Obteniendo todas las companias");
        return companyRepository.findAll()
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    public CompanyResponse getById(String id) {
        log.info("Buscando compania con id: {}", id);
        return companyRepository.findById(new CompanyId(id))
                .map(CompanyResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Compania no encontrada con id: " + id));
    }
}
