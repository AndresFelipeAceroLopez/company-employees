package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.infrastructure.persistence.mongo.document.PermissionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPermissionMongoRepository extends MongoRepository<PermissionDocument, String> {
    Optional<PermissionDocument> findByScope(String scope);
}
