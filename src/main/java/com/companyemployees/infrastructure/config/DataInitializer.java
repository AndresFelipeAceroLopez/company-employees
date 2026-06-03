package com.companyemployees.infrastructure.config;

import com.companyemployees.application.ports.repository.CompanyRepository;
import com.companyemployees.application.ports.repository.EmployeeRepository;
import com.companyemployees.application.ports.repository.PermissionRepository;
import com.companyemployees.application.ports.repository.RoleRepository;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.application.ports.security.PasswordHasher;
import com.companyemployees.domain.company.Company;
import com.companyemployees.domain.company.CompanyId;
import com.companyemployees.domain.employee.Employee;
import com.companyemployees.domain.user.Permission;
import com.companyemployees.domain.user.PermissionId;
import com.companyemployees.domain.user.Role;
import com.companyemployees.domain.user.RoleId;
import com.companyemployees.domain.user.User;
import com.companyemployees.infrastructure.persistence.mongo.document.CompanyDocument;
import com.companyemployees.infrastructure.persistence.mongo.document.EmployeeDocument;
import com.companyemployees.infrastructure.persistence.mongo.document.UserDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inicializador de datos. Solo se activa con app.seed.enabled=true.
 * Siembra el modelo de seguridad normalizado: colecciones permissions y roles,
 * mas companias/empleados de prueba. La coleccion users se vacia y se reconstruye
 * en cada arranque para garantizar consistencia con los ids de roles vigentes.
 * Las contrasenas se almacenan con hash BCrypt — nunca en texto plano.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_USUARIO = "USUARIO";

    // Catalogo de permisos (scopes) del sistema.
    private static final List<String> ALL_SCOPES = List.of(
            "empleado:leer", "empleado:crear", "empleado:actualizar", "empleado:eliminar",
            "compania:leer", "compania:crear", "compania:actualizar", "compania:eliminar");
    // Scopes que concede el rol USUARIO base.
    private static final List<String> USUARIO_SCOPES = List.of("empleado:leer");
    // Scopes del admin de Medellin: GET (all/by id), POST, POST por colecciones y DELETE.
    // NO puede actualizar (sin PUT/PATCH): leer + crear + eliminar.
    private static final List<String> MEDELLIN_SCOPES = List.of(
            "empleado:leer", "empleado:crear", "empleado:eliminar",
            "compania:leer", "compania:crear", "compania:eliminar");
    // Scopes del admin de Bogota: GET (all/by id), POST, POST por colecciones, PUT y PATCH.
    // NO puede eliminar (sin DELETE): leer + crear + actualizar.
    private static final List<String> BOGOTA_SCOPES = List.of(
            "empleado:leer", "empleado:crear", "empleado:actualizar",
            "compania:leer", "compania:crear", "compania:actualizar");

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordHasher passwordHasher;
    private final MongoTemplate mongoTemplate;
    private final boolean seedReset;

    private final String adminEmail;
    private final String adminPassword;
    private final String userEmail;
    private final String userPassword;
    private final String medellinEmail;
    private final String medellinPassword;

    public DataInitializer(CompanyRepository companyRepository,
                           EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           PasswordHasher passwordHasher,
                           @Value("${app.seed.admin-email}") String adminEmail,
                           @Value("${app.seed.admin-password}") String adminPassword,
                           @Value("${app.seed.user-email}") String userEmail,
                           @Value("${app.seed.user-password}") String userPassword,
                           @Value("${app.seed.medellin-email:admin.medellin@company.local}") String medellinEmail,
                           @Value("${app.seed.medellin-password:Medellin123!}") String medellinPassword,
                           MongoTemplate mongoTemplate,
                           @Value("${app.seed.reset:false}") boolean seedReset) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordHasher = passwordHasher;
        this.mongoTemplate = mongoTemplate;
        this.seedReset = seedReset;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.medellinEmail = medellinEmail;
        this.medellinPassword = medellinPassword;
    }

    @Override
    public void run(String... args) {
        log.info("=== Iniciando seed de datos ===");

        if (seedReset) {
            // Reset puntual (app.seed.reset=true): vacia datos de negocio para reconstruir
            // el seed desde cero. NO toca roles ni permissions.
            mongoTemplate.dropCollection(EmployeeDocument.class);
            mongoTemplate.dropCollection(CompanyDocument.class);
            mongoTemplate.dropCollection(UserDocument.class);
            log.warn("RESET de seed: colecciones companies, employees y users vaciadas.");
        }

        Map<String, PermissionId> permisosPorScope = seedPermissions();
        seedRoles(permisosPorScope);

        if (companyRepository.findAll().isEmpty()) {
            insertCompaniesAndEmployees();
        } else {
            log.info("Companias existentes. Se omite seed de companias/empleados.");
        }

        seedUsers(permisosPorScope);

        log.info("=== Seed de datos finalizado ===");
    }

    /**
     * Siembra (en lote) los permisos del catalogo que falten en la coleccion y devuelve
     * el mapa scope -> id. Idempotente: agrega solo los nuevos, conserva los existentes.
     */
    private Map<String, PermissionId> seedPermissions() {
        Map<String, PermissionId> porScope = new LinkedHashMap<>();
        for (Permission p : permissionRepository.findAll()) {
            porScope.put(p.getScope(), p.getId());
        }
        List<Permission> faltantes = ALL_SCOPES.stream()
                .filter(scope -> !porScope.containsKey(scope))
                .map(Permission::create)
                .toList();
        if (!faltantes.isEmpty()) {
            for (Permission guardado : permissionRepository.saveAll(faltantes)) {
                porScope.put(guardado.getScope(), guardado.getId());
            }
            log.info("Permisos nuevos sembrados: {}", faltantes.stream().map(Permission::getScope).toList());
        }
        return porScope;
    }

    /**
     * Sincroniza (en lote) los roles ADMIN y USUARIO con el catalogo de codigo: los crea si
     * faltan y actualiza sus permisos si cambiaron, conservando el id. Idempotente.
     */
    private void seedRoles(Map<String, PermissionId> permisosPorScope) {
        List<Role> aGuardar = new ArrayList<>();
        rolePendiente(ROL_ADMIN, permisoIds(ALL_SCOPES, permisosPorScope)).ifPresent(aGuardar::add);
        rolePendiente(ROL_USUARIO, permisoIds(USUARIO_SCOPES, permisosPorScope)).ifPresent(aGuardar::add);
        if (!aGuardar.isEmpty()) {
            roleRepository.saveAll(aGuardar);
            log.info("Roles sembrados/actualizados: {}", aGuardar.stream().map(Role::getNombre).toList());
        }
    }

    /** Devuelve el Role a guardar (crear o actualizar) o vacio si ya esta sincronizado. */
    private Optional<Role> rolePendiente(String nombre, Set<PermissionId> permisos) {
        Optional<Role> existente = roleRepository.findByNombre(nombre);
        if (existente.isEmpty()) {
            return Optional.of(Role.create(nombre, permisos));
        }
        Role actual = existente.get();
        if (actual.getPermisos().equals(permisos)) {
            return Optional.empty();
        }
        return Optional.of(new Role(actual.getId(), nombre, permisos));
    }

    private Set<PermissionId> permisoIds(List<String> scopes, Map<String, PermissionId> porScope) {
        return scopes.stream().map(porScope::get).collect(Collectors.toCollection(LinkedHashSet::new));
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

    /**
     * Crea los usuarios semilla solo si aun no existen (idempotente, sin borrar):
     *   - Admin Bogota: atado a la compania de Bogota, scopes directos = leer + crear + actualizar (sin eliminar).
     *   - Admin Medellin: atado a la compania de Medellin, scopes directos = leer + crear + eliminar (sin actualizar).
     *   - Usuario demo: rol USUARIO base.
     * La autorizacion es por scope y global; el companiaId indica la pertenencia (ciudad).
     */
    private void seedUsers(Map<String, PermissionId> permisosPorScope) {
        List<Company> companias = companyRepository.findAll();
        // companiaId es solo pertenencia; si no hay compania de esa ciudad, se usa un fallback.
        CompanyId primera = companias.stream().findFirst().map(Company::getId).orElse(null);
        CompanyId segunda = companias.stream().skip(1).findFirst().map(Company::getId).orElse(primera);
        CompanyId bogota = orElse(companiaPorCiudad(companias, "Bogota"), primera);
        CompanyId medellin = orElse(companiaPorCiudad(companias, "Medellin"), segunda);

        upsertAdmin("Admin Bogota", adminEmail, adminPassword, BOGOTA_SCOPES, bogota, permisosPorScope);
        upsertAdmin("Admin Medellin", medellinEmail, medellinPassword, MEDELLIN_SCOPES, medellin, permisosPorScope);

        if (!userRepository.existsByCorreo(userEmail)) {
            RoleId usuarioRoleId = roleRepository.findByNombre(ROL_USUARIO).orElseThrow().getId();
            CompanyId targetCompany = companias.stream().findFirst().map(Company::getId).orElse(null);
            if (targetCompany != null) {
                User usuario = User.register("Usuario Demo", userEmail, passwordHasher.hash(userPassword),
                        Set.of(usuarioRoleId), Set.of(), targetCompany);
                userRepository.save(usuario);
                log.info("Usuario USUARIO creado ({}) asociado a compania {}.", userEmail, targetCompany.value());
            }
        }
    }

    /**
     * Crea el admin si no existe o sincroniza sus scopes directos si cambiaron (idempotente,
     * sin borrar). Al actualizar conserva id, hash de contrasena, roles y fecha de creacion;
     * solo reemplaza los permisos. Asi los cambios de scope en codigo se aplican sin reset.
     */
    private void upsertAdmin(String nombre, String correo, String passwordPlano,
                             List<String> scopes, CompanyId companiaId,
                             Map<String, PermissionId> permisosPorScope) {
        if (companiaId == null) {
            return;
        }
        Set<PermissionId> permisos = permisoIds(scopes, permisosPorScope);
        Optional<User> existente = userRepository.findByCorreo(correo);
        if (existente.isEmpty()) {
            User nuevo = User.register(nombre, correo, passwordHasher.hash(passwordPlano),
                    Set.of(), permisos, companiaId);
            userRepository.save(nuevo);
            log.info("Admin creado ({}) con scopes {}.", correo, scopes);
            return;
        }
        User actual = existente.get();
        if (actual.getPermisos().equals(permisos)) {
            return; // ya sincronizado
        }
        User actualizado = new User(actual.getId(), actual.getNombre(), actual.getCorreo(),
                actual.getPasswordHash(), actual.getRoles(), permisos,
                actual.getCompaniaId(), actual.getFechaCreacion());
        userRepository.save(actualizado);
        log.info("Admin actualizado ({}) -> scopes {}.", correo, scopes);
    }

    private static CompanyId orElse(CompanyId valor, CompanyId fallback) {
        return valor != null ? valor : fallback;
    }

    /** Busca la compania cuya direccion menciona la ciudad dada (ej. "Bogota", "Medellin"). */
    private CompanyId companiaPorCiudad(List<Company> companias, String ciudad) {
        return companias.stream()
                .filter(c -> c.getDireccion() != null
                        && c.getDireccion().toLowerCase().contains(ciudad.toLowerCase()))
                .findFirst()
                .map(Company::getId)
                .orElse(null);
    }
}
