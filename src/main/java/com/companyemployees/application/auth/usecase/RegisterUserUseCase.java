package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.auth.dto.RegisterUserCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final UnitOfWork unitOfWork;

    public RegisterUserUseCase(UserRepository userRepository,
                               CompanyRepository companyRepository,
                               PasswordHasher passwordHasher,
                               JwtService jwtService,
                               UnitOfWork unitOfWork) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.unitOfWork = unitOfWork;
    }

    public AuthResult execute(RegisterUserCommand command) {
        if (command.password() == null || command.password().length() < 8) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 8 caracteres");
        }
        Role role = command.role() == null ? Role.USUARIO : command.role();
        CompanyId companiaId = null;
        if (command.companiaId() != null && !command.companiaId().isBlank()) {
            companiaId = new CompanyId(command.companiaId());
        }
        if (role == Role.USUARIO && companiaId == null) {
            throw new IllegalArgumentException("El registro de USUARIO requiere companiaId");
        }
        final CompanyId companiaFinal = companiaId;

        return unitOfWork.execute(() -> {
            if (userRepository.existsByCorreo(command.correo())) {
                throw new DuplicateResourceException("Ya existe un usuario con el correo: " + command.correo());
            }
            if (companiaFinal != null && !companyRepository.existsById(companiaFinal)) {
                throw new EntityNotFoundException("Compania no encontrada con id: " + companiaFinal.value());
            }

            String hash = passwordHasher.hash(command.password());
            User user = User.register(command.nombre(), command.correo(), hash, role, companiaFinal);
            User saved = userRepository.save(user);

            String token = jwtService.generateToken(saved);
            log.info("Usuario registrado: {} ({}). companiaId={}",
                    saved.getCorreo(), saved.getRole(),
                    saved.getCompaniaId() != null ? saved.getCompaniaId().value() : "-");

            return new AuthResult(token, jwtService.expirationSeconds(), AuthResult.AuthenticatedUser.from(saved));
        });
    }
}
