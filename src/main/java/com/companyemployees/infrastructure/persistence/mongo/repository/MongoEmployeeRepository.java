package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import com.companyemployees.infrastructure.persistence.mongo.document.EmployeeDocument;
import com.companyemployees.infrastructure.persistence.mongo.mapper.EmployeeDocumentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MongoEmployeeRepository implements EmployeeRepository {

    private final SpringDataEmployeeMongoRepository mongoRepository;
    private final EmployeeDocumentMapper mapper;

    public MongoEmployeeRepository(SpringDataEmployeeMongoRepository mongoRepository, EmployeeDocumentMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Employee> findAll() {
        return mongoRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Employee> findById(EmployeeId id) {
        return mongoRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByCorreo(String correo) {
        return mongoRepository.findByCorreo(correo)
                .map(mapper::toDomain);
    }

    @Override
    public List<Employee> findByCompaniaId(CompanyId companiaId) {
        return mongoRepository.findByCompaniaId(companiaId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeDocument document = mapper.toDocument(employee);
        EmployeeDocument saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(EmployeeId id) {
        mongoRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(EmployeeId id) {
        return mongoRepository.existsById(id.value());
    }
}
