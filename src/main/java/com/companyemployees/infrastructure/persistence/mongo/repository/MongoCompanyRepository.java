package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.infrastructure.persistence.mongo.mapper.CompanyDocumentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MongoCompanyRepository implements CompanyRepository {

    private final SpringDataCompanyMongoRepository mongoRepository;
    private final SpringDataEmployeeMongoRepository employeeMongoRepository;
    private final CompanyDocumentMapper mapper;

    public MongoCompanyRepository(
            SpringDataCompanyMongoRepository mongoRepository,
            SpringDataEmployeeMongoRepository employeeMongoRepository,
            CompanyDocumentMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.employeeMongoRepository = employeeMongoRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Company> findAll() {
        return mongoRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Company> findById(CompanyId id) {
        return mongoRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Company save(Company company) {
        var document = mapper.toDocument(company);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(CompanyId id) {
        mongoRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(CompanyId id) {
        return mongoRepository.existsById(id.value());
    }

    @Override
    public long countEmployees(CompanyId id) {
        return employeeMongoRepository.countByCompaniaId(id.value());
    }
}
