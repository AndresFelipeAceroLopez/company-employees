package com.companyemployees.api.support;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Traduce los scopes del usuario autenticado a los metodos HTTP que puede usar
 * sobre un recurso ("empleado" o "compania"). Lo consume el handler OPTIONS para
 * construir la cabecera Allow, de modo que cada admin (ej. Medellin sin PUT/PATCH,
 * Bogota sin DELETE) reciba exactamente los verbos que sus scopes le permiten.
 */
public final class AllowedMethodsResolver {

    private AllowedMethodsResolver() {
    }

    public static List<HttpMethod> forResource(String recurso, Authentication authentication) {
        Set<String> scopes = authentication == null
                ? Set.of()
                : authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        List<HttpMethod> metodos = new ArrayList<>();
        metodos.add(HttpMethod.OPTIONS); // siempre disponible: es el sondeo en si
        if (scopes.contains("SCOPE_" + recurso + ":leer")) {
            metodos.add(HttpMethod.GET);
            metodos.add(HttpMethod.HEAD);
        }
        if (scopes.contains("SCOPE_" + recurso + ":crear")) {
            metodos.add(HttpMethod.POST);
        }
        if (scopes.contains("SCOPE_" + recurso + ":actualizar")) {
            metodos.add(HttpMethod.PUT);
            metodos.add(HttpMethod.PATCH);
        }
        if (scopes.contains("SCOPE_" + recurso + ":eliminar")) {
            metodos.add(HttpMethod.DELETE);
        }
        return metodos;
    }
}
