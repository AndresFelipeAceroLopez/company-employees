package com.companyemployees.infrastructure.config;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inicializador de datos. Solo se activa con app.seed.enabled=true.
 * Crea companias y empleados de prueba, mas un usuario ADMIN y un USUARIO.
 * Las contrasenas se almacenan con hash BCrypt — nunca en texto plano.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    private final String adminEmail;
    private final String adminPassword;
    private final String userEmail;
    private final String userPassword;

    public DataInitializer(CompanyRepository companyRepository,
                           EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           @Value("${app.seed.admin-email}") String adminEmail,
                           @Value("${app.seed.admin-password}") String adminPassword,
                           @Value("${app.seed.user-email}") String userEmail,
                           @Value("${app.seed.user-password}") String userPassword) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    @Override
    public void run(String... args) {
        log.info("=== Iniciando seed de datos ===");

        List<Company> existingCompanies = companyRepository.findAll();
        if (existingCompanies.isEmpty()) {
            insertCompaniesAndEmployees();
        } else {
            log.info("Companias existentes ({}). Se omite seed de companias/empleados.", existingCompanies.size());
        }

        seedUsers();

        log.info("=== Seed de datos finalizado ===");
    }

    private void insertCompaniesAndEmployees() {
        Company techSolutions = saveCompany(Company.create("Tech Solutions S.A.S", "Calle 45 # 10-20, Bogota", "3001234567"));
        Company innovaCorp = saveCompany(Company.create("Innova Corp Ltda", "Carrera 15 # 85-30, Medellin", "3009876543"));
        Company digitalWorks = saveCompany(Company.create("Digital Works S.A", "Avenida 68 # 25-10, Cali", "3005551234"));

        createEmployee("Ana", "Garcia", "ana.garcia@techsolutions.com", "Desarrolladora Senior",
                new BigDecimal("4500000"), techSolutions);
        createEmployee("Carlos", "Rodriguez", "carlos.rodriguez@techsolutions.com", "Arquitecto de Software",
                new BigDecimal("5500000"), techSolutions);
        createEmployee("Maria", "Lopez", "maria.lopez@techsolutions.com", "Tester QA",
                new BigDecimal("3200000"), techSolutions);
        createEmployee("Diego", "Martinez", "diego.martinez@techsolutions.com", "DevOps Engineer",
                new BigDecimal("4800000"), techSolutions);

        createEmployee("Laura", "Hernandez", "laura.hernandez@innovacorp.com", "Product Manager",
                new BigDecimal("5000000"), innovaCorp);
        createEmployee("Andres", "Gomez", "andres.gomez@innovacorp.com", "Desarrollador Full Stack",
                new BigDecimal("4200000"), innovaCorp);
        createEmployee("Sofia", "Ramirez", "sofia.ramirez@innovacorp.com", "UX/UI Designer",
                new BigDecimal("3800000"), innovaCorp);

        createEmployee("Javier", "Torres", "javier.torres@digitalworks.com", "Scrum Master",
                new BigDecimal("4600000"), digitalWorks);
        createEmployee("Camila", "Vargas", "camila.vargas@digitalworks.com", "Desarrolladora Frontend",
                new BigDecimal("3900000"), digitalWorks);
        createEmployee("Roberto", "Silva", "roberto.silva@digitalworks.com", "Analista de Datos",
                new BigDecimal("4100000"), digitalWorks);
    }

    private Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    private void createEmployee(String nombre, String apellido, String correo, String cargo,
                                BigDecimal salario, Company company) {
        Employee employee = Employee.create(nombre, apellido, correo, cargo, salario, company.getId());
        employeeRepository.save(employee);
        company.increaseEmployeeCount();
        companyRepository.save(company);
        log.info("Empleado creado: {} {} ({})", nombre, apellido, cargo);
    }

    private void seedUsers() {
        if (!userRepository.existsByCorreo(adminEmail)) {
            User admin = User.register("Administrador", adminEmail, passwordHasher.hash(adminPassword),
                    Role.ADMIN, null);
            userRepository.save(admin);
            log.info("Usuario ADMIN creado ({}).", adminEmail);
        }

        if (!userRepository.existsByCorreo(userEmail)) {
            CompanyId targetCompany = companyRepository.findAll().stream()
                    .findFirst()
                    .map(Company::getId)
                    .orElse(null);
            if (targetCompany != null) {
                User usuario = User.register("Usuario Demo", userEmail, passwordHasher.hash(userPassword),
                        Role.USUARIO, targetCompany);
                userRepository.save(usuario);
                log.info("Usuario USUARIO creado ({}) asociado a compania {}.", userEmail, targetCompany.value());
            } else {
                log.warn("No hay companias para asociar el usuario seed; se omite creacion de USUARIO.");
            }
        }
    }
}
