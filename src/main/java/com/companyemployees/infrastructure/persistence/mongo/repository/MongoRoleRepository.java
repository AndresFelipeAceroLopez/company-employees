package com.companyemployees.infrastructure.persistence.mongo.repository;

import com.companyemployees.application.ports.repository.RoleRepository;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.RoleId;
import com.companyemployees.infrastructure.persistence.mongo.document.RoleDocument;
import com.companyemployees.infrastructure.persistence.mongo.mapper.RoleDocumentMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoRoleRepository implements RoleRepository {

    private final SpringDataRoleMongoRepository mongoRepository;
    private final RoleDocumentMapper mapper;

    public MongoRoleRepository(SpringDataRoleMongoRepository mongoRepository, RoleDocumentMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        return mongoRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByNombre(String nombre) {
        return mongoRepository.findByNombre(nombre).map(mapper::toDomain);
    }

    @Override
    public List<Role> findAllByIds(Collection<RoleId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<String> raw = ids.stream().map(RoleId::value).toList();
        return mongoRepository.findAllById(raw).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Role> findAll() {
        return mongoRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return mongoRepository.existsByNombre(nombre);
    }

    @Override
    public Role save(Role role) {
        RoleDocument saved = mongoRepository.save(mapper.toDocument(role));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Role> saveAll(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return List.of();
        List<RoleDocument> docs = roles.stream().map(mapper::toDocument).toList();
        List<Role> result = new ArrayList<>();
        mongoRepository.saveAll(docs).forEach(d -> result.add(mapper.toDomain(d)));
        return result;
    }
}
