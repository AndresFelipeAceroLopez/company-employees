package com.companyemployees.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

class EmployeeApiIntegrationTest extends AbstractMongoIntegrationTest {

    @Test
    void listadoPaginadoDevuelveEnvelopeConTotales() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");
        createEmployee(admin, companiaId, "Ana", "Garcia", "ana@x.com");
        createEmployee(admin, companiaId, "Beto", "Lopez", "beto@x.com");
        createEmployee(admin, companiaId, "Caro", "Mejia", "caro@x.com");

        getAsync("/api/empleados", admin, "pagina", "1", "tamano", "2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(2))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamano").value(2))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void listadoConFiltroBuscar() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");
        createEmployee(admin, companiaId, "Ana", "Garcia", "ana@x.com");
        createEmployee(admin, companiaId, "Beto", "Lopez", "beto@x.com");

        getAsync("/api/empleados", admin, "buscar", "garcia")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.datos[0].apellido").value("Garcia"));
    }

    @Test
    void listadoConOrdenamientoDescendente() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");
        createEmployee(admin, companiaId, "Ana", "Aaa", "ana@x.com");
        createEmployee(admin, companiaId, "Zoe", "Zzz", "zoe@x.com");

        getAsync("/api/empleados", admin, "orden", "apellido", "dir", "desc")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].apellido").value("Zzz"));
    }

    @Test
    void empleadosDeCompaniaPaginado() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");
        createEmployee(admin, companiaId, "Ana", "Garcia", "ana@x.com");

        getAsync("/api/companias/" + companiaId + "/empleados", admin, "pagina", "1", "tamano", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.datos[0].apellido").value("Garcia"))
                // Por seguridad el DTO publico no expone la companiaId del empleado.
                .andExpect(jsonPath("$.datos[0].companiaId").doesNotExist());
    }

    @Test
    void creacionMasivaDevuelve201() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");

        Map<String, Object> body = Map.of("empleados", List.of(
                Map.of("nombre", "Ana", "apellido", "Garcia", "correo", "ana@x.com",
                        "cargo", "Dev", "salario", 3000000, "companiaId", companiaId),
                Map.of("nombre", "Beto", "apellido", "Lopez", "correo", "beto@x.com",
                        "cargo", "Dev", "salario", 3500000, "companiaId", companiaId)));

        mockMvc.perform(post("/api/empleados/lote")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void patchModificaSoloElCampoEnviado() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");
        String empId = createEmployee(admin, companiaId, "Ana", "Garcia", "ana@x.com");

        mockMvc.perform(patch("/api/empleados/" + empId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("cargo", "Lead"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo").value("Lead"))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void eliminacionMasivaDevuelve204() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");
        String e1 = createEmployee(admin, companiaId, "Ana", "Garcia", "ana@x.com");
        String e2 = createEmployee(admin, companiaId, "Beto", "Lopez", "beto@x.com");

        mockMvc.perform(delete("/api/empleados/lote")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("ids", List.of(e1, e2)))))
                .andExpect(status().isNoContent());

        getAsync("/api/empleados", admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void cuerpoInvalidoDevuelve422() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaId = createCompany(admin, "Acme Corp");

        Map<String, Object> body = Map.of(
                "nombre", "",
                "apellido", "Garcia",
                "correo", "no-es-correo",
                "cargo", "Dev",
                "salario", 3000000,
                "companiaId", companiaId);

        mockMvc.perform(post("/api/empleados")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioNoPropietarioRecibe403() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaA = createCompany(admin, "Compania A");
        String companiaB = createCompany(admin, "Compania B");
        String empEnB = createEmployee(admin, companiaB, "Ana", "Garcia", "ana@x.com");

        // USUARIO pertenece a la compania A; intenta modificar un empleado de B.
        String usuario = tokenFor("user@x.com", "USUARIO", companiaA);

        mockMvc.perform(patch("/api/empleados/" + empEnB)
                        .header("Authorization", "Bearer " + usuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("cargo", "Hacker"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioPropietarioPuedeModificarYRecibe200() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaA = createCompany(admin, "Compania A");
        String empEnA = createEmployee(admin, companiaA, "Ana", "Garcia", "ana@x.com");

        String usuario = tokenFor("user@x.com", "USUARIO", companiaA);

        mockMvc.perform(patch("/api/empleados/" + empEnA)
                        .header("Authorization", "Bearer " + usuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("cargo", "Lead"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo").value("Lead"));
    }
}
