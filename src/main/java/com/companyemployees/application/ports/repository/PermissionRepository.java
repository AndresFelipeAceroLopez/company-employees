package com.companyemployees.application.ports.repository;

import com.companyemployees.domain.user.Permission;
import com.companyemployees.domain.user.PermissionId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de repositorio para Permission — vive en Application. */
public interface PermissionRepository {
    Optional<Permission> findByScope(String scope);
    List<Permission> findAllByIds(Collection<PermissionId> ids);
    List<Permission> findAll();
    Permission save(Permission permission);
    List<Permission> saveAll(List<Permission> permissions);
}
