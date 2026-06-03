package com.companyemployees.infrastructure.security;

import com.companyemployees.application.ports.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Implementacion de JwtService con firma ASIMETRICA (RS256):
 *   - se firma con la clave PRIVADA (solo este servicio puede emitir tokens);
 *   - se verifica con la clave PUBLICA (cualquiera puede validar sin poder firmar).
 *
 * Las claves se leen (PEM/Base64 DER) de jwt.private-key y jwt.public-key. Si no estan
 * configuradas, se genera un par RSA efimero SOLO para desarrollo (ver advertencia en el log).
 */
@Component
public class JjwtService implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JjwtService.class);

    private static final String CLAIM_CORREO = "correo";
    private static final String CLAIM_COMPANIA = "companiaId";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_SCOPES = "scopes";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long expirationSeconds;

    public JjwtService(@Value("${jwt.private-key:}") String privatePem,
                       @Value("${jwt.public-key:}") String publicPem,
                       @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
        this.expirationSeconds = Duration.ofMinutes(expirationMinutes).toSeconds();

        if (hasText(privatePem) && hasText(publicPem)) {
            this.privateKey = loadPrivateKey(privatePem);
            this.publicKey = loadPublicKey(publicPem);
            log.info("JWT asimetrico (RS256): par de claves RSA cargado desde configuracion.");
        } else {
            KeyPair keyPair = generateRsaKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            log.warn("jwt.private-key/jwt.public-key no configurados: se genero un par RSA EFIMERO solo para "
                    + "desarrollo. Los tokens se invalidan al reiniciar y no sirven entre instancias. "
                    + "Configura las claves (RS256) en produccion.");
        }
    }

    @Override
    public String generateToken(String userId, String correo, String companiaId,
                                Set<String> roles, Set<String> scopes) {
        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date expiration = new Date(nowMillis + expirationSeconds * 1000L);
        return Jwts.builder()
                .subject(userId)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim(CLAIM_CORREO, correo)
                .claim(CLAIM_COMPANIA, companiaId)
                .claim(CLAIM_ROLES, roles != null ? List.copyOf(roles) : List.of())
                .claim(CLAIM_SCOPES, scopes != null ? List.copyOf(scopes) : List.of())
                .signWith(privateKey) // RS256: JJWT infiere el algoritmo de la clave RSA privada
                .compact();
    }

    @Override
    public JwtClaims parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey) // verifica la firma con la clave publica
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new JwtClaims(
                    claims.getSubject(),
                    claims.get(CLAIM_CORREO, String.class),
                    claims.get(CLAIM_COMPANIA, String.class),
                    stringList(claims.get(CLAIM_ROLES)),
                    stringList(claims.get(CLAIM_SCOPES))
            );
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Token JWT invalido o expirado");
        }
    }

    @Override
    public long expirationSeconds() {
        return expirationSeconds;
    }

    // ----- Carga / generacion de claves -----

    private static PrivateKey loadPrivateKey(String pem) {
        try {
            byte[] der = decodePem(pem);
            // Clave privada en formato PKCS#8.
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("jwt.private-key invalida (se espera RSA PKCS#8 en Base64/PEM)", ex);
        }
    }

    private static PublicKey loadPublicKey(String pem) {
        try {
            byte[] der = decodePem(pem);
            // Clave publica en formato X.509 (SubjectPublicKeyInfo).
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("jwt.public-key invalida (se espera RSA X.509 en Base64/PEM)", ex);
        }
    }

    /** Quita cabeceras PEM (-----BEGIN/END-----) y espacios, y decodifica el Base64 a DER. */
    private static byte[] decodePem(String pem) {
        String base64 = pem
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo generar el par de claves RSA para JWT", ex);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** Convierte el valor crudo de un claim (List<?> de JJWT) en List<String>. */
    private static List<String> stringList(Object claim) {
        if (claim instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return Collections.emptyList();
    }
}
