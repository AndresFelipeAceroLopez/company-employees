package com.companyemployees.application.ports.repository;

import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;

import java.util.Optional;

/** Puerto de repositorio para Usuario — vive en Application. */
public interface UserRepository {
    Optional<User> findById(UserId id);
    Optional<User> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    User save(User user);
}
