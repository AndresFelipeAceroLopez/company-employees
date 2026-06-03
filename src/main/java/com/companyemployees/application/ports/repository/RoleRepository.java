package com.companyemployees.application.ports.repository;

import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.RoleId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de repositorio para Role — vive en Application. */
public interface RoleRepository {
    Optional<Role> findById(RoleId id);
    Optional<Role> findByNombre(String nombre);
    List<Role> findAllByIds(Collection<RoleId> ids);
    List<Role> findAll();
    boolean existsByNombre(String nombre);
    Role save(Role role);
    List<Role> saveAll(List<Role> roles);
}
