package com.companyemployees.infrastructure.security;

import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.security.AuthenticatedPrincipal;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Policy "EsPropietarioDeCompania" para empleados.
 * - ADMIN puede modificar cualquier empleado.
 * - USUARIO solo puede modificar empleados cuya companiaId coincida con la suya (claim del JWT).
 * - Si el empleado no existe, se lanza EntityNotFoundException (404).
 * - Si existe pero no pertenece a la compania, devuelve false (Spring Security responde 403).
 * <p>
 * Depende del puerto EmployeeRepository, no de la implementacion concreta.
 */
@Service("employeeAuthorizationService")
public class EmployeeAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeAuthorizationService.class);
    private final EmployeeRepository employeeRepository;

    public EmployeeAuthorizationService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public boolean canModifyEmployee(Authentication authentication, String employeeId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ADMIN")) return true;

        AuthenticatedPrincipal principal = principal(authentication);
        if (principal == null || principal.companiaId() == null) return false;

        Employee employee = employeeRepository.findById(new EmployeeId(employeeId))
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + employeeId));

        boolean allowed = principal.companiaId().equals(employee.getCompaniaId().value());
        if (!allowed) {
            log.info("Policy ownership denegada: user.companiaId={}, employee.companiaId={}",
                    principal.companiaId(), employee.getCompaniaId().value());
        }
        return allowed;
    }

    public boolean canModifyEmployees(Authentication authentication, List<String> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) return false;
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ADMIN")) return true;

        AuthenticatedPrincipal principal = principal(authentication);
        if (principal == null || principal.companiaId() == null) return false;

        List<EmployeeId> ids = employeeIds.stream().map(EmployeeId::new).toList();
        List<Employee> found = employeeRepository.findAllByIds(ids);
        if (found.size() != ids.size()) {
            Set<String> foundIds = found.stream().map(e -> e.getId().value()).collect(Collectors.toSet());
            List<String> missing = new ArrayList<>();
            for (EmployeeId id : ids) if (!foundIds.contains(id.value())) missing.add(id.value());
            throw new EntityNotFoundException("Empleado no encontrado con id: " + missing.get(0));
        }

        return found.stream()
                .allMatch(e -> principal.companiaId().equals(e.getCompaniaId().value()));
    }

    private static AuthenticatedPrincipal principal(Authentication authentication) {
        Object p = authentication.getPrincipal();
        return (p instanceof AuthenticatedPrincipal jp) ? jp : null;
    }

    private static boolean hasRole(Authentication authentication, String role) {
        String target = "ROLE_" + role;
        return authentication.getAuthorities().stream().anyMatch(a -> target.equals(a.getAuthority()));
    }
}
