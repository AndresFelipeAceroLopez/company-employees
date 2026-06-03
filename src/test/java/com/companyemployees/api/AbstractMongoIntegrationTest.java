package com.companyemployees.api;

import com.companyemployees.application.ports.security.JwtService;
import com.companyemployees.infrastructure.persistence.mongo.document.PermissionDocument;
import com.companyemployees.infrastructure.persistence.mongo.document.RoleDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base para pruebas de integracion. Levanta un MongoDB en un contenedor con
 * replica set (Testcontainers crea uno de un solo nodo), lo que habilita
 * transacciones reales — necesario para verificar el rollback del UnitOfWork.
 *
 * Requiere Docker disponible en la maquina que ejecuta las pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
abstract class AbstractMongoIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void limpiarBaseDeDatos() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
        seedRolesYPermisos();
    }

    /**
     * Siembra las colecciones permissions y roles que el registro de usuarios necesita.
     * Por simplicidad usa el scope como _id del permiso y el nombre como _id del rol.
     */
    private void seedRolesYPermisos() {
        List<String> all = List.of("empleado:leer", "empleado:crear", "empleado:actualizar",
                "empleado:eliminar", "compania:leer", "compania:crear", "compania:actualizar",
                "compania:eliminar");
        all.forEach(scope -> mongoTemplate.save(new PermissionDocument(scope, scope)));
        // ADMIN: CRUD completo (todos los scopes). USUARIO: solo lectura de empleados.
        mongoTemplate.save(new RoleDocument("ADMIN", "ADMIN", all));
        mongoTemplate.save(new RoleDocument("USUARIO", "USUARIO", List.of("empleado:leer")));
    }

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * Realiza un GET sobre un endpoint asincrono (que devuelve CompletableFuture) y
     * completa el ciclo servlet async con asyncDispatch. {@code params} se pasan en
     * pares clave/valor.
     */
    protected ResultActions getAsync(String url, String token, String... params) throws Exception {
        MockHttpServletRequestBuilder request = get(url).header("Authorization", "Bearer " + token);
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        MvcResult started = mockMvc.perform(request)
                .andExpect(request().asyncStarted())
                .andReturn();
        return mockMvc.perform(asyncDispatch(started));
    }

    /**
     * Acuna un token de prueba directamente con el {@link JwtService} (via confiable),
     * en vez de pasar por /api/auth/registro — que es publico y solo crea USUARIO.
     * Asi las pruebas obtienen un token con los scopes del rol indicado sin depender
     * de la politica del endpoint publico. Los scopes replican el seed de roles del test.
     */
    protected String tokenFor(String correo, String role, String companiaId) {
        String roleUpper = role.toUpperCase();
        Set<String> scopes = "ADMIN".equals(roleUpper)
                ? Set.of("empleado:leer", "empleado:crear", "empleado:actualizar", "empleado:eliminar",
                        "compania:leer", "compania:crear", "compania:actualizar", "compania:eliminar")
                : Set.of("empleado:leer");
        return jwtService.generateToken("test-" + correo, correo, companiaId,
                Set.of(roleUpper), scopes);
    }

    protected String createCompany(String token, String nombre) throws Exception {
        Map<String, Object> body = Map.of(
                "nombre", nombre,
                "direccion", "Calle 100 # 10-20",
                "telefono", "3001234567");
        MvcResult res = mockMvc.perform(post("/api/companias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return idFromLocation(res);
    }

    protected String createEmployee(String token, String companiaId,
                                    String nombre, String apellido, String correo) throws Exception {
        Map<String, Object> body = Map.of(
                "nombre", nombre,
                "apellido", apellido,
                "correo", correo,
                "cargo", "Desarrollador",
                "salario", 3000000,
                "companiaId", companiaId);
        MvcResult res = mockMvc.perform(post("/api/empleados")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return idFromLocation(res);
    }

    /** El id del recurso creado se expone via header Location (no en el body, por seguridad). */
    private String idFromLocation(MvcResult res) {
        String location = res.getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
