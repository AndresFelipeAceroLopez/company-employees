package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.auth.dto.RegisterUserCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;
import com.companyemployees.support.UnitOfWorkStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    CompanyRepository companyRepository;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    JwtService jwtService;
    @Mock
    UnitOfWork unitOfWork;

    RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, companyRepository, passwordHasher, jwtService, unitOfWork);
        UnitOfWorkStubs.runInline(unitOfWork);
    }

    @Test
    void registroExitosoDevuelveToken() {
        when(userRepository.existsByCorreo("ana@x.com")).thenReturn(false);
        when(passwordHasher.hash("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(new UserId("u1"), u.getNombre(), u.getCorreo(), u.getPasswordHash(),
                    u.getRole(), u.getCompaniaId(), LocalDateTime.now());
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        RegisterUserCommand command = new RegisterUserCommand("Ana", "ana@x.com", "secret123", Role.ADMIN, null);
        AuthResult result = useCase.execute(command);

        assertEquals("jwt-token", result.token());
        assertEquals("ana@x.com", result.usuario().correo());
        assertEquals("ADMIN", result.usuario().role());
    }

    @Test
    void registroFallaSiElCorreoYaExiste() {
        when(userRepository.existsByCorreo("ana@x.com")).thenReturn(true);

        RegisterUserCommand command = new RegisterUserCommand("Ana", "ana@x.com", "secret123", Role.ADMIN, null);
        assertThrows(DuplicateResourceException.class, () -> useCase.execute(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registroFallaConPasswordCorto() {
        RegisterUserCommand command = new RegisterUserCommand("Ana", "ana@x.com", "123", Role.ADMIN, null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registroDeUsuarioRequiereCompaniaId() {
        RegisterUserCommand command = new RegisterUserCommand("Ana", "ana@x.com", "secret123", Role.USUARIO, null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(userRepository, never()).save(any(User.class));
    }
}
