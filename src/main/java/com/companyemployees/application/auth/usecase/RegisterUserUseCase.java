package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.UserAuthorizationResolver;
import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.auth.dto.RegisterUserCommand;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.PermissionRepository;
import com.companyemployees.application.ports.repository.RoleRepository;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.common.DuplicateResourceException;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.user.Permission;
import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.RoleId;
import com.companyemployees.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    /** Rol que exige companiaId asociada (pertenencia). */
    private static final String ROL_USUARIO = "USUARIO";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CompanyRepository companyRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final UserAuthorizationResolver authorizationResolver;
    private final UnitOfWork unitOfWork;

    public RegisterUserUseCase(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PermissionRepository permissionRepository,
                               CompanyRepository companyRepository,
                               PasswordHasher passwordHasher,
                               JwtService jwtService,
                               UserAuthorizationResolver authorizationResolver,
                               UnitOfWork unitOfWork) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.companyRepository = companyRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.authorizationResolver = authorizationResolver;
        this.unitOfWork = unitOfWork;
    }

    public AuthResult execute(RegisterUserCommand command) {
        if (command.password() == null || command.password().length() < 8) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 8 caracteres");
        }

        // Roles en MAYUSCULAS (catalogo ADMIN/USUARIO); scopes tal cual (p.ej. "empleado:leer").
        Set<String> nombresRol = new LinkedHashSet<>();
        for (String r : normalizar(command.roles())) {
            nombresRol.add(r.toUpperCase());
        }
        Set<String> scopes = normalizar(command.scopes());
        // Sin roles ni scopes explicitos -> USUARIO por defecto.
        if (nombresRol.isEmpty() && scopes.isEmpty()) {
            nombresRol.add(ROL_USUARIO);
        }

        CompanyId companiaId = null;
        if (command.companiaId() != null && !command.companiaId().isBlank()) {
            companiaId = new CompanyId(command.companiaId());
        }
        if (nombresRol.contains(ROL_USUARIO) && companiaId == null) {
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

            // Resuelve nombres de rol a ids (relacion users -> roles).
            Set<RoleId> roleIds = new LinkedHashSet<>();
            for (String nombre : nombresRol) {
                Role role = roleRepository.findByNombre(nombre)
                        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + nombre));
                roleIds.add(role.getId());
            }
            // Resuelve scopes a ids de permiso (relacion users -> permissions).
            Set<PermissionId> permisoIds = new LinkedHashSet<>();
            for (String scope : scopes) {
                Permission permiso = permissionRepository.findByScope(scope)
                        .orElseThrow(() -> new EntityNotFoundException("Scope no encontrado: " + scope));
                permisoIds.add(permiso.getId());
            }

            String hash = passwordHasher.hash(command.password());
            User user = User.register(command.nombre(), command.correo(), hash, roleIds, permisoIds, companiaFinal);
            User saved = userRepository.save(user);

            UserAuthorizationResolver.ResolvedAuthorization authz = authorizationResolver.resolve(saved);
            String token = jwtService.generateToken(
                    saved.getId().value(), saved.getCorreo(),
                    saved.getCompaniaId() != null ? saved.getCompaniaId().value() : null,
                    authz.roleNames(), authz.scopes());

            log.info("Usuario registrado: {} roles={} scopes={} companiaId={}",
                    saved.getCorreo(), authz.roleNames(), authz.scopes(),
                    saved.getCompaniaId() != null ? saved.getCompaniaId().value() : "-");

            return new AuthResult(token, jwtService.expirationSeconds(),
                    AuthResult.AuthenticatedUser.of(saved, authz.roleNames(), authz.scopes()));
        });
    }

    /** Normaliza un conjunto de cadenas: descarta nulos/vacios y recorta espacios. */
    private static Set<String> normalizar(Set<String> valores) {
        Set<String> result = new LinkedHashSet<>();
        if (valores != null) {
            for (String v : valores) {
                if (v != null && !v.isBlank()) result.add(v.trim());
            }
        }
        return result;
    }
}
