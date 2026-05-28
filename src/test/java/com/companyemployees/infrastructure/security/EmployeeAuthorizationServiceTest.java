package com.companyemployees.infrastructure.security;

import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.security.AuthenticatedPrincipal;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import com.companyemployees.domain.employee.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAuthorizationServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    EmployeeAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeAuthorizationService(employeeRepository);
    }

    private Authentication admin() {
        var principal = new AuthenticatedPrincipal("admin", "admin@x.com", "ADMIN", null);
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication usuario(String companiaId) {
        var principal = new AuthenticatedPrincipal("u1", "user@x.com", "USUARIO", companiaId);
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
    }

    private Employee employee(String id, String companiaId) {
        return new Employee(new EmployeeId(id), "Ana", "Garcia", id + "@x.com", "Dev",
                new BigDecimal("1000"), new CompanyId(companiaId), EmployeeStatus.ACTIVE);
    }

    @Test
    void adminPuedeModificarCualquierEmpleado() {
        assertTrue(service.canModifyEmployee(admin(), "e1"));
    }

    @Test
    void usuarioPropietarioPuedeModificarEmpleadoDeSuCompania() {
        when(employeeRepository.findById(new EmployeeId("e1"))).thenReturn(Optional.of(employee("e1", "c1")));
        assertTrue(service.canModifyEmployee(usuario("c1"), "e1"));
    }

    @Test
    void usuarioNoPropietarioEsDenegado() {
        when(employeeRepository.findById(new EmployeeId("e1"))).thenReturn(Optional.of(employee("e1", "c2")));
        assertFalse(service.canModifyEmployee(usuario("c1"), "e1"));
    }

    @Test
    void empleadoInexistenteLanza404() {
        when(employeeRepository.findById(new EmployeeId("e9"))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.canModifyEmployee(usuario("c1"), "e9"));
    }

    @Test
    void adminPuedeModificarLote() {
        assertTrue(service.canModifyEmployees(admin(), List.of("e1", "e2")));
    }

    @Test
    void usuarioPropietarioPuedeModificarLoteDeSuCompania() {
        when(employeeRepository.findAllByIds(anyList()))
                .thenReturn(List.of(employee("e1", "c1"), employee("e2", "c1")));
        assertTrue(service.canModifyEmployees(usuario("c1"), List.of("e1", "e2")));
    }

    @Test
    void loteConEmpleadoDeOtraCompaniaEsDenegado() {
        when(employeeRepository.findAllByIds(anyList()))
                .thenReturn(List.of(employee("e1", "c1"), employee("e2", "c2")));
        assertFalse(service.canModifyEmployees(usuario("c1"), List.of("e1", "e2")));
    }

    @Test
    void loteConIdInexistenteLanza404() {
        when(employeeRepository.findAllByIds(anyList()))
                .thenReturn(List.of(employee("e1", "c1")));
        assertThrows(EntityNotFoundException.class,
                () -> service.canModifyEmployees(usuario("c1"), List.of("e1", "e2")));
    }
}
