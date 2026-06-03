package com.companyemployees.infrastructure.security;

import com.companyemployees.application.ports.security.AuthenticatedPrincipal;
import com.companyemployees.application.ports.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lee el header Authorization: Bearer <token>, valida el JWT y popula el SecurityContext.
 * Mapea los claims a authorities de Spring Security:
 *   - cada rol  -> "ROLE_<rol>"   (para hasRole / hasAnyRole)
 *   - cada scope -> "SCOPE_<scope>" (para hasAuthority / autorizacion fina)
 * Si el header no esta o el token es invalido, simplemente no autentica (los entrypoints
 * publicos siguen funcionando y los protegidos responden 401 por defecto).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            try {
                JwtService.JwtClaims claims = jwtService.parse(token);
                Set<String> roles = new LinkedHashSet<>(claims.roles());
                Set<String> scopes = new LinkedHashSet<>(claims.scopes());

                AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                        claims.userId(), claims.correo(), claims.companiaId(), roles, scopes);
                AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, buildAuthorities(roles, scopes));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                log.debug("Token JWT rechazado: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private static List<GrantedAuthority> buildAuthorities(Set<String> roles, Set<String> scopes) {
        List<GrantedAuthority> authorities = new ArrayList<>(roles.size() + scopes.size());
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        for (String scope : scopes) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
        }
        return authorities;
    }
}
