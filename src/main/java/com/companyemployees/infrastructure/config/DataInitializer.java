package com.companyemployees.infrastructure.config;

import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeStatus;
import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inicializador de datos para insertar información inicial en la base de datos.
 * Se ejecuta automáticamente al iniciar la aplicación.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public DataInitializer(CompanyRepository companyRepository, EmployeeRepository employeeRepository) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Iniciando inserción de datos iniciales ===");

        // Verificar si ya existen datos
        List<Company> existingCompanies = companyRepository.findAll();
        if (!existingCompanies.isEmpty()) {
            log.info("Ya existen {} compañías en la base de datos. Saltando inserción de datos iniciales.", existingCompanies.size());
            return;
        }

        insertInitialData();
        log.info("=== Datos iniciales insertados correctamente ===");
    }

    private void insertInitialData() {
        // Crear 3 compañías
        Company techSolutions = createCompany("Tech Solutions S.A.S", "Calle 45 # 10-20, Bogotá", "3001234567");
        Company innovaCorp = createCompany("Innova Corp Ltda", "Carrera 15 # 85-30, Medellín", "3009876543");
        Company digitalWorks = createCompany("Digital Works S.A", "Avenida 68 # 25-10, Cali", "3005551234");

        log.info("Insertando compañías...");
        Company savedTechSolutions = companyRepository.save(techSolutions);
        Company savedInnovaCorp = companyRepository.save(innovaCorp);
        Company savedDigitalWorks = companyRepository.save(digitalWorks);

        log.info("Compañías creadas: Tech Solutions ({}), Innova Corp ({}), Digital Works ({})",
                savedTechSolutions.getId().value(),
                savedInnovaCorp.getId().value(),
                savedDigitalWorks.getId().value());

        // Crear 10 empleados distribuidos entre las compañías
        log.info("Insertando empleados...");

        // Empleados para Tech Solutions (4 empleados)
        createAndSaveEmployee("Ana", "García", "ana.garcia@techsolutions.com", "Desarrolladora Senior", 
                new BigDecimal("4500000"), savedTechSolutions.getId());
        createAndSaveEmployee("Carlos", "Rodríguez", "carlos.rodriguez@techsolutions.com", "Arquitecto de Software", 
                new BigDecimal("5500000"), savedTechSolutions.getId());
        createAndSaveEmployee("María", "López", "maria.lopez@techsolutions.com", "Tester QA", 
                new BigDecimal("3200000"), savedTechSolutions.getId());
        createAndSaveEmployee("Diego", "Martínez", "diego.martinez@techsolutions.com", "DevOps Engineer", 
                new BigDecimal("4800000"), savedTechSolutions.getId());

        // Empleados para Innova Corp (3 empleados)
        createAndSaveEmployee("Laura", "Hernández", "laura.hernandez@innovacorp.com", "Product Manager", 
                new BigDecimal("5000000"), savedInnovaCorp.getId());
        createAndSaveEmployee("Andrés", "Gómez", "andres.gomez@innovacorp.com", "Desarrollador Full Stack", 
                new BigDecimal("4200000"), savedInnovaCorp.getId());
        createAndSaveEmployee("Sofía", "Ramírez", "sofia.ramirez@innovacorp.com", "UX/UI Designer", 
                new BigDecimal("3800000"), savedInnovaCorp.getId());

        // Empleados para Digital Works (3 empleados)
        createAndSaveEmployee("Javier", "Torres", "javier.torres@digitalworks.com", "Scrum Master", 
                new BigDecimal("4600000"), savedDigitalWorks.getId());
        createAndSaveEmployee("Camila", "Vargas", "camila.vargas@digitalworks.com", "Desarrolladora Frontend", 
                new BigDecimal("3900000"), savedDigitalWorks.getId());
        createAndSaveEmployee("Roberto", "Silva", "roberto.silva@digitalworks.com", "Analista de Datos", 
                new BigDecimal("4100000"), savedDigitalWorks.getId());

        log.info("10 empleados creados exitosamente");
    }

    private Company createCompany(String nombre, String direccion, String telefono) {
        return Company.create(nombre, direccion, telefono);
    }

    private void createAndSaveEmployee(String nombre, String apellido, String correo, String cargo, 
                                     BigDecimal salario, CompanyId companiaId) {
        Employee employee = Employee.create(nombre, apellido, correo, cargo, salario, companiaId);
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Empleado creado: {} {} - {} ({})", nombre, apellido, cargo, savedEmployee.getId().getValue());
    }
}