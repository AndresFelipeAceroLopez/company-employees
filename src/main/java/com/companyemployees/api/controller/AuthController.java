package com.companyemployees.api.controller;

import com.companyemployees.api.request.LoginRequest;
import com.companyemployees.api.request.RegisterRequest;
import com.companyemployees.api.response.AuthResponse;
import com.companyemployees.api.response.AuthenticatedUserResponse;
import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.auth.dto.LoginUserCommand;
import com.companyemployees.application.auth.dto.RegisterUserCommand;
import com.companyemployees.application.auth.usecase.GetAuthenticatedProfileUseCase;
import com.companyemployees.application.auth.usecase.LoginUserUseCase;
import com.companyemployees.application.auth.usecase.RegisterUserUseCase;
import com.companyemployees.application.ports.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetAuthenticatedProfileUseCase getAuthenticatedProfileUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          GetAuthenticatedProfileUseCase getAuthenticatedProfileUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getAuthenticatedProfileUseCase = getAuthenticatedProfileUseCase;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.nombre(), request.correo(), request.password(),
                request.role(), request.companiaId()
        );
        AuthResult result = registerUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUserUseCase.execute(new LoginUserCommand(request.correo(), request.password()));
        return ResponseEntity.ok(AuthResponse.from(result));
    }

    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthenticatedUserResponse> profile(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        AuthResult.AuthenticatedUser usuario = getAuthenticatedProfileUseCase.execute(principal.userId());
        return ResponseEntity.ok(AuthenticatedUserResponse.from(usuario));
    }
}
