package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.application.ports.repository.PermissionRepository;
import com.companyemployees.domain.user.Permission;
import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.infrastructure.persistence.mongo.document.PermissionDocument;
import com.companyemployees.infrastructure.persistence.mongo.mapper.PermissionDocumentMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoPermissionRepository implements PermissionRepository {

    private final SpringDataPermissionMongoRepository mongoRepository;
    private final PermissionDocumentMapper mapper;

    public MongoPermissionRepository(SpringDataPermissionMongoRepository mongoRepository,
                                     PermissionDocumentMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Permission> findByScope(String scope) {
        return mongoRepository.findByScope(scope).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAllByIds(Collection<PermissionId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<String> raw = ids.stream().map(PermissionId::value).toList();
        return mongoRepository.findAllById(raw).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Permission> findAll() {
        return mongoRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Permission save(Permission permission) {
        PermissionDocument saved = mongoRepository.save(mapper.toDocument(permission));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Permission> saveAll(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) return List.of();
        List<PermissionDocument> docs = permissions.stream().map(mapper::toDocument).toList();
        List<Permission> result = new ArrayList<>();
        mongoRepository.saveAll(docs).forEach(d -> result.add(mapper.toDomain(d)));
        return result;
    }
}
