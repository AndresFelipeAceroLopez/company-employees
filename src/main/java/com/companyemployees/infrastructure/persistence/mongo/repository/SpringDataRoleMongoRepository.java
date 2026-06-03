package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.infrastructure.persistence.mongo.document.RoleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataRoleMongoRepository extends MongoRepository<RoleDocument, String> {
    Optional<RoleDocument> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
