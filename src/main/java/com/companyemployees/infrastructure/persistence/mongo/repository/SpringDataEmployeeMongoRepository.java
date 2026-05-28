package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.infrastructure.persistence.mongo.document.EmployeeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataEmployeeMongoRepository extends MongoRepository<EmployeeDocument, String> {
    Optional<EmployeeDocument> findByCorreo(String correo);
    List<EmployeeDocument> findByCompaniaId(String companiaId);
    long countByCompaniaId(String companiaId);
}
