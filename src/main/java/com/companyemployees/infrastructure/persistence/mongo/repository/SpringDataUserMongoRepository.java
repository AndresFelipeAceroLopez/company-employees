package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.infrastructure.persistence.mongo.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
