package com.companyemployees.infrastructure.security;

import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JjwtService implements JwtService {

    private static final String CLAIM_CORREO = "correo";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_COMPANIA = "companiaId";

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JjwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos 32 caracteres. Configura JWT_SECRET en el entorno.");
        }
        // Acepta base64 o texto plano UTF-8.
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException | DecodingException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = Duration.ofMinutes(expirationMinutes).toSeconds();
    }

    @Override
    public String generateToken(User user) {
        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date expiration = new Date(nowMillis + expirationSeconds * 1000L);
        return Jwts.builder()
                .subject(user.getId().value())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim(CLAIM_CORREO, user.getCorreo())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_COMPANIA, user.getCompaniaId() != null ? user.getCompaniaId().value() : null)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public JwtClaims parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new JwtClaims(
                    claims.getSubject(),
                    claims.get(CLAIM_CORREO, String.class),
                    claims.get(CLAIM_ROLE, String.class),
                    claims.get(CLAIM_COMPANIA, String.class)
            );
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Token JWT invalido o expirado");
        }
    }

    @Override
    public long expirationSeconds() {
        return expirationSeconds;
    }
}
