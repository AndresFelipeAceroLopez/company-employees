package com.companyemployees.infrastructure.config;

import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.employee.EmployeeId;
import com.companyemployees.infrastructure.persistence.mongo.repository.MongoCompanyRepository;
import com.companyemployees.infrastructure.persistence.mongo.repository.MongoEmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuración de MongoDB, transacciones y seed data.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.companyemployees.infrastructure.persistence.mongo.repository")
public class MongoConfig {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    /**
     * Seed data para poblar la base de datos al iniciar la aplicación.
     */
    @Bean
    public CommandLineRunner seedDatabase(
            MongoCompanyRepository companyRepository,
            MongoEmployeeRepository employeeRepository) {
        return args -> {
            // Limpiar datos existentes
            companyRepository.findAll().forEach(c -> companyRepository.deleteById(c.getId()));
            employeeRepository.findAll().forEach(e -> employeeRepository.deleteById(e.getId()));

            // Crear compañías
            Company company1 = new Company(
                new CompanyId(UUID.randomUUID().toString()),
                "Tech Solutions S.A.",
                "Av. Siempre Viva 123, CDMX",
                "+52-55-1234-5678",
                LocalDateTime.now().minusYears(5),
                0
            );

            Company company2 = new Company(
                new CompanyId(UUID.randomUUID().toString()),
                "Innovación Digital",
                "Calle Reforma 456, Guadalajara",
                "+52-33-8765-4321",
                LocalDateTime.now().minusYears(3),
                0
            );

            Company company3 = new Company(
                new CompanyId(UUID.randomUUID().toString()),
                "Desarrollo Web Pro",
                "Blvd. Tecnológico 789, Monterrey",
                "+52-81-5555-6666",
                LocalDateTime.now().minusYears(2),
                0
            );

            company1 = companyRepository.save(company1);
            company2 = companyRepository.save(company2);
            company3 = companyRepository.save(company3);

            // Crear empleados para company1
            employeeRepository.save(Employee.create("Juan", "Pérez", "juan.perez@techsolutions.com",
                    "Desarrollador Senior", new BigDecimal("3500000"), company1.getId()));
            employeeRepository.save(Employee.create("María", "González", "maria.gonzalez@techsolutions.com",
                    "Diseñadora UX", new BigDecimal("3000000"), company1.getId()));
            employeeRepository.save(Employee.create("Carlos", "Rodríguez", "carlos.rodriguez@techsolutions.com",
                    "Arquitecto de Software", new BigDecimal("4500000"), company1.getId()));
            employeeRepository.save(Employee.create("Ana", "Martínez", "ana.martinez@techsolutions.com",
                    "QA Tester", new BigDecimal("2800000"), company1.getId()));

            // Crear empleados para company2
            employeeRepository.save(Employee.create("Luis", "Hernández", "luis.hernandez@innovacion.com",
                    "DevOps Engineer", new BigDecimal("4000000"), company2.getId()));
            employeeRepository.save(Employee.create("Laura", "López", "laura.lopez@innovacion.com",
                    "Product Manager", new BigDecimal("3800000"), company2.getId()));
            employeeRepository.save(Employee.create("Pedro", "Sánchez", "pedro.sanchez@innovacion.com",
                    "Scrum Master", new BigDecimal("3200000"), company2.getId()));

            // Crear empleados para company3
            employeeRepository.save(Employee.create("Sofía", "Ramírez", "sofia.ramirez@webpro.com",
                    "Frontend Developer", new BigDecimal("3100000"), company3.getId()));
            employeeRepository.save(Employee.create("Diego", "Torres", "diego.torres@webpro.com",
                    "Backend Developer", new BigDecimal("3300000"), company3.getId()));
            employeeRepository.save(Employee.create("Valentina", "Flores", "valentina.flores@webpro.com",
                    "Data Analyst", new BigDecimal("2900000"), company3.getId()));

            // Actualizar contadores de empleados
            company1.increaseEmployeeCount();
            company1.increaseEmployeeCount();
            company1.increaseEmployeeCount();
            company1.increaseEmployeeCount();
            companyRepository.save(company1);

            company2.increaseEmployeeCount();
            company2.increaseEmployeeCount();
            company2.increaseEmployeeCount();
            companyRepository.save(company2);

            company3.increaseEmployeeCount();
            company3.increaseEmployeeCount();
            company3.increaseEmployeeCount();
            companyRepository.save(company3);

            System.out.println("✅ Base de datos MongoDB inicializada con datos de prueba");
            System.out.println("   - 3 compañías creadas");
            System.out.println("   - 10 empleados creados");
        };
    }
}
