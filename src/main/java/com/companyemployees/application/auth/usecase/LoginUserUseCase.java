package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.auth.dto.LoginUserCommand;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.domain.auth.InvalidCredentialsException;
import com.companyemployees.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public LoginUserUseCase(UserRepository userRepository,
                            PasswordHasher passwordHasher,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public AuthResult execute(LoginUserCommand command) {
        User user = userRepository.findByCorreo(command.correo())
                .orElseThrow(() -> {
                    log.info("Login fallido para correo: {}", command.correo());
                    return new InvalidCredentialsException("Credenciales invalidas");
                });

        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            log.info("Login fallido (password) para correo: {}", command.correo());
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generateToken(user);
        log.info("Login exitoso: {}", user.getCorreo());
        return new AuthResult(token, jwtService.expirationSeconds(), AuthResult.AuthenticatedUser.from(user));
    }
}
