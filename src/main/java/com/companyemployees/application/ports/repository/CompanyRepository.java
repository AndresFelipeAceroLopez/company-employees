package com.companyemployees.application.ports.repository;

import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para Compañía — vive en Application.
 * Infrastructure implementa este contrato usando MongoDB.
 * Onion Architecture: Application define, Infrastructure implementa.
 */
public interface CompanyRepository {
    Optional<Company> findById(CompanyId id);
    List<Company> findAll();
    Company save(Company company);
    void deleteById(CompanyId id);
    boolean existsById(CompanyId id);
    long countEmployees(CompanyId id);
}
