package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.infrastructure.persistence.mongo.document.CompanyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCompanyMongoRepository extends MongoRepository<CompanyDocument, String> {
}
