package com.companyemployees.application.auth;

import com.companyemployees.application.ports.repository.PermissionRepository;
import com.companyemployees.application.ports.repository.RoleRepository;
import com.companyemployees.domain.user.Permission;
import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resuelve la autorizacion efectiva de un usuario y produce:
 *   - roleNames: nombres de los roles del usuario (claim "roles" del JWT).
 *   - scopes: UNION de los scopes de sus roles (relacion roles -&gt; permissions) y de los
 *     permisos asignados directamente al usuario (claim "scopes" del JWT).
 * La autorizacion es por scope y global; el companiaId no interviene aqui.
 * Depende solo de puertos (RoleRepository, PermissionRepository).
 */
@Service
public class UserAuthorizationResolver {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public UserAuthorizationResolver(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public ResolvedAuthorization resolve(User user) {
        Set<String> roleNames = new LinkedHashSet<>();
        Set<PermissionId> permisoIds = new LinkedHashSet<>();

        // Scopes via roles.
        List<Role> roles = roleRepository.findAllByIds(user.getRoles());
        for (Role role : roles) {
            roleNames.add(role.getNombre());
            permisoIds.addAll(role.getPermisos());
        }
        // Scopes asignados directamente al usuario.
        permisoIds.addAll(user.getPermisos());

        Set<String> scopes = new LinkedHashSet<>();
        if (!permisoIds.isEmpty()) {
            for (Permission permiso : permissionRepository.findAllByIds(permisoIds)) {
                scopes.add(permiso.getScope());
            }
        }
        return new ResolvedAuthorization(roleNames, scopes);
    }

    /** Roles (por nombre) y scopes efectivos de un usuario. */
    public record ResolvedAuthorization(Set<String> roleNames, Set<String> scopes) {}
}
