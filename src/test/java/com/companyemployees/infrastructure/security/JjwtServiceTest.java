package com.companyemployees.infrastructure.security;

import com.companyemployees.application.ports.security.JwtService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Verifica que la emision/verificacion de JWT es ASIMETRICA (RS256):
 * sin claves configuradas, el servicio genera un par RSA efimero, firma con la
 * privada y verifica con la publica.
 */
class JjwtServiceTest {

    private JwtService nuevoServicio() {
        // private/public vacios -> genera par RSA efimero internamente.
        return new JjwtService("", "", 60);
    }

    @Test
    void elTokenUsaElAlgoritmoRs256() {
        JwtService service = nuevoServicio();
        String token = service.generateToken("u1", "ana@x.com", "c1",
                Set.of("ADMIN"), Set.of("empleado:leer"));

        // La cabecera (primer segmento) debe declarar alg = RS256 (asimetrico), no HS256.
        String header = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);
        assertTrue(header.contains("\"alg\":\"RS256\""), "El header debe ser RS256: " + header);
    }

    @Test
    void firmaYVerificaElMismoToken() {
        JwtService service = nuevoServicio();
        String token = service.generateToken("u1", "ana@x.com", "c1",
                Set.of("ADMIN"), Set.of("empleado:leer", "empleado:crear"));

        JwtService.JwtClaims claims = service.parse(token);

        assertEquals("u1", claims.userId());
        assertEquals("ana@x.com", claims.correo());
        assertEquals("c1", claims.companiaId());
        assertTrue(claims.roles().contains("ADMIN"));
        assertTrue(claims.scopes().contains("empleado:crear"));
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        // Un token emitido por OTRA instancia (otro par de claves) no debe validar:
        // demuestra que la verificacion depende de la clave publica correcta.
        String tokenAjeno = nuevoServicio().generateToken("u1", "ana@x.com", "c1",
                Set.of("ADMIN"), Set.of("empleado:leer"));
        JwtService otroServicio = nuevoServicio();

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> otroServicio.parse(tokenAjeno));
    }

    @Test
    void cargaUnParDeClavesConfigurado() {
        // Mismas claves de ejemplo que application-local.properties: valida el camino
        // de carga PKCS#8 (privada) + X.509 (publica) y que el par emite/verifica.
        String priv = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDFviuf/nn64JvRejRp5lo4aRnBu/2oMkXhg31RakB0+2MtBgJr7i7wATOeBWE3TuuGgoUBIxrvcssitirhJZFckuDv4zpzNVKiOZY49mqxFIGIGSa+C95uDLLucTaFgq/MKSolA5/VE2r9od1UcMnJ1TYYRIJ+ICfcCO7BFthKF/RHQHLJA9DbDts947U4ERvkp5jXSqK1IzEdrqw+/E24+pXntrjv0G0RJYwNWWvRDi1r2IrjIB1G6v6EQWoDMOsVpgpE48aMprYjRLNe80kCZz62AJj6aA8v+kZPI5kkxSIdvZheJq0NamTQIiyFgURD6PzJryuR5neOERyqihvbAgMBAAECggEASf543ZRAec/NjsvPTCdd8Ejn0fZyGxvAyXvGC46aUwv0lxbuFgwZNUcF1SZHNAaMnJc/hObpf9txRupqzjgcFkuB71IUf6Le6oRpAaS31M6LD+31cN0JHR4UYKQtljUKanem5RXfm8B54zHMG/pej++AIAGW4PJQaAZRrSBKzCJU0Xb982osJoprY0TJvEMVM2fjwJdQ24aNPUW/t/1y9vScEV29VbIFAJXCyjf7dRglyCsS+qUYMVrMqKxx075izQv1PH8qZ/nimT5RkNsmUexMoiPWW0kkFDLV+NCw5rbrp/+QXJzEkde+o0db14N11axshtE1gJxWap3mCHTMeQKBgQDwFI2cW/HFAkPlh40671skiti/1G1DIzBrbsiRRfl/GfYn4yurzlHujCt4OeEsvkgNDW2xwREO37GLx95U8iJGUj4dYWXWPlWHzQQnFdr2tq6niz7QNYhowZceqqUYY6TGxrtqfQSi65KLcmaotFZUvcTtofBJI6EJlwVddle9AwKBgQDS2uys8qLC0EjJYZep6/ZlWf61bh/ZZP5uBMJs0SHiZ2x+3dJjm6BXKy2Zq2Lu8sBVh/tLvCXLRVBB1w1jiMzkr+COGnGVzx2B4O3w865ejOU/cEaMDKxBWjRYQVfYXqBMUa1X256tzw8SSMwTN+5UHiEnszEuwH18jHn1vmkSSQKBgQCDc/2F26IpqO/cQbhGyMAy5gOAJaQd2qqsaGBRiurteR2bAXvr+nBKGV3D2o08noN9AhlJ9tiMOFdSngFSqbGyp11zYN0NzduvG8ZmUEUwSR6ghlHCV011bmp8VBtJm2WhoTcFO6bKPgpjaCRlwzhx9eZZ5w1WPrfyNEqvvhiUWQKBgAz4GDyfA/HVm9R/IzP5ZoNfYLJKHr1hX+DmfArzZhEl6V94R1uHE/qoljpwdzpZRgk8vvEulmiFT+Vs087+eBPM2ZQD/l6HiboWWqxKYLxCEEuxnTU6BlJSuQuAoWWWlGd9Upsnz//Cz5uBTMQV2Zvk6ocll23XuyAxB8Ncvdu5AoGBAL4wdtCKwetwSSxmUe4EXIhHHd07KxwoByNDC8QtexMk3w+LWCltgREkoHBmnlHAFgguQm1x4ylJ8jfrkhWgiBWBiD1RXxaXuymcmSJmDyzNIOVQVR4KFtvj9ZPXefznv+WUQz2bDHX7M7qrNqvDhnwMmPGv7v9NBO9ntDiWdd+3";
        String pub = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxb4rn/55+uCb0Xo0aeZaOGkZwbv9qDJF4YN9UWpAdPtjLQYCa+4u8AEzngVhN07rhoKFASMa73LLIrYq4SWRXJLg7+M6czVSojmWOPZqsRSBiBkmvgvebgyy7nE2hYKvzCkqJQOf1RNq/aHdVHDJydU2GESCfiAn3AjuwRbYShf0R0ByyQPQ2w7bPeO1OBEb5KeY10qitSMxHa6sPvxNuPqV57a479BtESWMDVlr0Q4ta9iK4yAdRur+hEFqAzDrFaYKROPGjKa2I0SzXvNJAmc+tgCY+mgPL/pGTyOZJMUiHb2YXiatDWpk0CIshYFEQ+j8ya8rkeZ3jhEcqoob2wIDAQAB";

        JwtService service = new JjwtService(priv, pub, 60);
        String token = service.generateToken("u1", "ana@x.com", "c1",
                Set.of("ADMIN"), Set.of("compania:actualizar"));

        JwtService.JwtClaims claims = service.parse(token);
        assertEquals("u1", claims.userId());
        assertTrue(claims.scopes().contains("compania:actualizar"));
    }

    @Test
    void dosInstanciasGeneranClavesDistintas() {
        // Cada instancia efimera produce firmas distintas para el mismo payload.
        String a = nuevoServicio().generateToken("u1", "ana@x.com", "c1", Set.of(), Set.of());
        String b = nuevoServicio().generateToken("u1", "ana@x.com", "c1", Set.of(), Set.of());
        // La firma (tercer segmento) debe diferir entre pares de claves distintos.
        assertNotEquals(a.split("\\.")[2], b.split("\\.")[2]);
    }
}
