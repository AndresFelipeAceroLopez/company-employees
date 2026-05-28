package com.companyemployees.api;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.transaction.UnitOfWork;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.employee.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba obligatoria de rollback transaccional (Parte II, seccion 7).
 * Demuestra que el UnitOfWork controla la transaccion: si algo falla a mitad
 * de la operacion, no queda ningun dato parcial.
 */
class TransactionRollbackIntegrationTest extends AbstractMongoIntegrationTest {

    @Autowired
    UnitOfWork unitOfWork;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    EmployeeRepository employeeRepository;

    @Test
    void crearCompaniaConEmpleadosNoDejaDatosParcialesSiUnEmpleadoEsInvalido() throws Exception {
        String admin = tokenFor("admin@x.com", "ADMIN", null);
        String companiaA = createCompany(admin, "Compania A");
        createEmployee(admin, companiaA, "Existente", "Perez", "existente@x.com");

        // La compania nueva incluye un empleado con un correo ya existente:
        // la operacion debe abortar por completo (409) y no persistir nada nuevo.
        Map<String, Object> body = Map.of(
                "nombre", "RollbackCo",
                "direccion", "Calle 50",
                "telefono", "3009999999",
                "empleados", List.of(
                        Map.of("nombre", "Nuevo", "apellido", "Empleado", "correo", "nuevo@x.com",
                                "cargo", "Dev", "salario", 3000000),
                        Map.of("nombre", "Choca", "apellido", "Correo", "correo", "existente@x.com",
                                "cargo", "Dev", "salario", 3000000)));

        mockMvc.perform(post("/api/companias/con-empleados")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isConflict());

        // La compania nueva NO quedo guardada: solo existe la original.
        mockMvc.perform(get("/api/companias").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Compania A"));

        // Ningun empleado del lote quedo guardado (solo el original).
        getAsync("/api/empleados", admin, "buscar", "nuevo@x.com")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void unitOfWorkRevierteEscriturasPreviasCuandoFallaLaTransaccion() {
        // Garantiza que las colecciones existen para no crear namespaces dentro de la transaccion.
        if (!mongoTemplate.collectionExists("companies")) {
            mongoTemplate.createCollection("companies");
        }
        if (!mongoTemplate.collectionExists("employees")) {
            mongoTemplate.createCollection("employees");
        }

        long companiasAntes = companyRepository.findAll().size();

        assertThrows(RuntimeException.class, () -> unitOfWork.execute((Runnable) () -> {
            Company saved = companyRepository.save(Company.create("TxCo", "Calle 1", "3001234567"));
            employeeRepository.save(Employee.create("Ana", "Garcia", "tx@x.com", "Dev",
                    new BigDecimal("3000000"), saved.getId()));
            // Falla DESPUES de haber escrito compania y empleado dentro de la misma transaccion.
            throw new RuntimeException("forzar rollback");
        }));

        // El rollback debe haber descartado ambas escrituras.
        assertEquals(companiasAntes, companyRepository.findAll().size());
        assertTrue(employeeRepository.findByCorreo("tx@x.com").isEmpty());
    }
}
