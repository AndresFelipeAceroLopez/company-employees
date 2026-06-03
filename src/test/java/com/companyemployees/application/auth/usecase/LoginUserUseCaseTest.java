package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.UserAuthorizationResolver;
import com.companyemployees.application.auth.UserAuthorizationResolver.ResolvedAuthorization;
import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.auth.dto.LoginUserCommand;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.domain.auth.InvalidCredentialsException;
import com.companyemployees.domain.user.RoleId;
import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    JwtService jwtService;
    @Mock
    UserAuthorizationResolver authorizationResolver;

    LoginUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUserUseCase(userRepository, passwordHasher, jwtService, authorizationResolver);
    }

    private User user() {
        return new User(new UserId("u1"), "Ana", "ana@x.com", "hash",
                Set.of(new RoleId("r-admin")), Set.of(), null, LocalDateTime.now());
    }

    @Test
    void loginDevuelveTokenConCredencialesCorrectas() {
        when(userRepository.findByCorreo("ana@x.com")).thenReturn(Optional.of(user()));
        when(passwordHasher.matches("secret123", "hash")).thenReturn(true);
        when(authorizationResolver.resolve(any(User.class))).thenReturn(
                new ResolvedAuthorization(new LinkedHashSet<>(Set.of("ADMIN")),
                        new LinkedHashSet<>(Set.of("empleado:eliminar"))));
        when(jwtService.generateToken(anyString(), anyString(), any(), anySet(), anySet())).thenReturn("jwt-token");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        AuthResult result = useCase.execute(new LoginUserCommand("ana@x.com", "secret123"));

        assertEquals("jwt-token", result.token());
        assertEquals(3600L, result.expiraEnSegundos());
        assertEquals("ana@x.com", result.usuario().correo());
        assertTrue(result.usuario().roles().contains("ADMIN"));
    }

    @Test
    void loginFallaConPasswordIncorrecto() {
        when(userRepository.findByCorreo("ana@x.com")).thenReturn(Optional.of(user()));
        when(passwordHasher.matches("malo", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginUserCommand("ana@x.com", "malo")));
        verify(jwtService, never()).generateToken(any(), any(), any(), any(), any());
    }

    @Test
    void loginFallaCuandoElUsuarioNoExiste() {
        when(userRepository.findByCorreo("nadie@x.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginUserCommand("nadie@x.com", "secret123")));
        verify(jwtService, never()).generateToken(any(), any(), any(), any(), any());
    }
}
