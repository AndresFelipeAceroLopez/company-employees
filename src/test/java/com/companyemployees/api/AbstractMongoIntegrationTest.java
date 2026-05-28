package com.companyemployees.api;

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

import java.util.HashMap;
import java.util.Map;

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

    @BeforeEach
    void limpiarBaseDeDatos() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
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

    protected String tokenFor(String correo, String role, String companiaId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Test User");
        body.put("correo", correo);
        body.put("password", "secret123");
        body.put("role", role);
        if (companiaId != null) {
            body.put("companiaId", companiaId);
        }
        MvcResult res = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
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
