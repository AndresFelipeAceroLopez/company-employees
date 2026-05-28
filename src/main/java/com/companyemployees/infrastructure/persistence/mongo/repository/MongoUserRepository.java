package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;
import com.companyemployees.infrastructure.persistence.mongo.document.UserDocument;
import com.companyemployees.infrastructure.persistence.mongo.mapper.UserDocumentMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoUserRepository implements UserRepository {

    private final SpringDataUserMongoRepository mongoRepository;
    private final UserDocumentMapper mapper;

    public MongoUserRepository(SpringDataUserMongoRepository mongoRepository, UserDocumentMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return mongoRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByCorreo(String correo) {
        return mongoRepository.findByCorreo(correo).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return mongoRepository.existsByCorreo(correo);
    }

    @Override
    public User save(User user) {
        UserDocument doc = mapper.toDocument(user);
        UserDocument saved = mongoRepository.save(doc);
        return mapper.toDomain(saved);
    }
}
