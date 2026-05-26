# Documentación Técnica — Arquitectura del Proyecto

## Tabla de contenidos
1. [Visión general](#1-visión-general)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Onion Architecture aplicada](#3-onion-architecture-aplicada)
4. [Estructura de carpetas](#4-estructura-de-carpetas)
5. [Capa Domain](#5-capa-domain)
6. [Capa Application](#6-capa-application)
7. [Capa Infrastructure](#7-capa-infrastructure)
8. [Capa API (Presentation)](#8-capa-api-presentation)
9. [Patrón Repository](#9-patrón-repository)
10. [Patrón Unit of Work](#10-patrón-unit-of-work)
11. [Flujo completo de una petición](#11-flujo-completo-de-una-petición)
12. [Manejo de errores](#12-manejo-de-errores)
13. [Configuración y arranque](#13-configuración-y-arranque)

---

## 1. Visión general

Este proyecto es una **API REST** que expone operaciones CRUD sobre dos entidades de negocio: **Compañía** y **Empleado**, con una relación de uno a muchos (1:N). La aplicación está construida siguiendo los principios de **Onion Architecture** (también conocida como Clean Architecture o Arquitectura Cebolla), garantizando que la lógica de negocio quede aislada de detalles técnicos como el framework web o el motor de base de datos.

**Objetivo arquitectónico:** que el dominio de negocio (Compañías y Empleados) no dependa de Spring, MongoDB, HTTP ni ningún otro detalle de infraestructura. Cualquiera de estos componentes podría ser reemplazado sin tocar la lógica de negocio.

---

## 2. Stack tecnológico

| Componente | Tecnología |
|------------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.4.5 |
| Gestor de dependencias | Maven |
| Base de datos | MongoDB (Atlas, Replica Set) |
| ODM | Spring Data MongoDB |
| Transacciones | `MongoTransactionManager` + `TransactionTemplate` |
| Validación | Jakarta Bean Validation (`@NotBlank`, `@Email`, etc.) |
| Documentación API | springdoc-openapi (Swagger UI) |
| Logging | SLF4J + Logback |

---

## 3. Onion Architecture aplicada

### Principio fundamental
> Las dependencias apuntan **siempre hacia adentro**. El núcleo (Domain) no sabe nada de las capas externas.

```
   ┌──────────────────────────────────────────────────┐
   │  API (Controladores REST, DTOs HTTP)             │  ← Capa más externa
   │   ┌───────────────────────────────────────────┐  │
   │   │  Infrastructure (MongoDB, mappers, config)│  │
   │   │   ┌───────────────────────────────────┐   │  │
   │   │   │  Application (Casos de uso, DTOs) │   │  │
   │   │   │   ┌───────────────────────────┐   │   │  │
   │   │   │   │  Domain (Entidades, VOs)  │   │   │  │  ← Núcleo
   │   │   │   └───────────────────────────┘   │   │  │
   │   │   └───────────────────────────────────┘   │  │
   │   └───────────────────────────────────────────┘  │
   └──────────────────────────────────────────────────┘
```

### Reglas de dependencia
| Capa | Puede depender de | NO puede depender de |
|------|-------------------|----------------------|
| **Domain** | Solo de sí mismo y de Java estándar | Nada externo |
| **Application** | Domain | Infrastructure, API |
| **Infrastructure** | Application, Domain | API |
| **API** | Application, Domain | Infrastructure (directamente) |

### Inversión de dependencias
Para que Application pueda usar MongoDB sin depender de él, se aplica el **principio de inversión de dependencias**:
- Application **define interfaces** (`CompanyRepository`, `UnitOfWork`) → estos son los **puertos**.
- Infrastructure **implementa esas interfaces** (`MongoCompanyRepository`, `MongoUnitOfWork`) → estos son los **adaptadores**.
- Spring inyecta la implementación concreta en tiempo de ejecución.

---

## 4. Estructura de carpetas

```
src/main/java/com/companyemployees/
│
├── CompanyEmployeesApplication.java       # Entry point de Spring Boot
│
├── domain/                                 # ════ CAPA DOMAIN ════
│   ├── common/
│   │   ├── DomainException.java
│   │   └── EntityNotFoundException.java
│   ├── company/
│   │   ├── Company.java                    # Entidad
│   │   └── CompanyId.java                  # Value Object
│   └── employee/
│       ├── Employee.java                   # Entidad
│       ├── EmployeeId.java                 # Value Object
│       └── EmployeeStatus.java             # Enum
│
├── application/                            # ════ CAPA APPLICATION ════
│   ├── ports/                              # Interfaces (contratos)
│   │   ├── repository/
│   │   │   ├── CompanyRepository.java
│   │   │   └── EmployeeRepository.java
│   │   └── transaction/
│   │       └── UnitOfWork.java
│   ├── company/
│   │   ├── dto/                            # Commands y Responses
│   │   │   ├── CreateCompanyCommand.java
│   │   │   ├── UpdateCompanyCommand.java
│   │   │   └── CompanyResponse.java
│   │   └── usecase/                        # Casos de uso
│   │       ├── CreateCompanyUseCase.java
│   │       ├── GetCompanyUseCase.java
│   │       ├── UpdateCompanyUseCase.java
│   │       ├── DeleteCompanyUseCase.java
│   │       └── CreateCompanyWithEmployeesUseCase.java
│   └── employee/
│       ├── dto/
│       └── usecase/
│
├── infrastructure/                         # ════ CAPA INFRASTRUCTURE ════
│   ├── config/
│   │   ├── MongoConfig.java                # Beans de Mongo + seed
│   │   └── DataInitializer.java
│   └── persistence/mongo/
│       ├── document/                       # Modelos persistentes
│       │   ├── CompanyDocument.java
│       │   └── EmployeeDocument.java
│       ├── mapper/                         # Documento ↔ Dominio
│       │   ├── CompanyDocumentMapper.java
│       │   └── EmployeeDocumentMapper.java
│       ├── repository/
│       │   ├── MongoCompanyRepository.java
│       │   ├── MongoEmployeeRepository.java
│       │   ├── SpringDataCompanyMongoRepository.java
│       │   └── SpringDataEmployeeMongoRepository.java
│       └── MongoUnitOfWork.java
│
└── api/                                    # ════ CAPA API ════
    ├── controller/
    │   ├── CompanyController.java
    │   └── EmployeeController.java
    ├── request/                            # DTOs de entrada (JSON)
    ├── response/                           # DTOs de salida (JSON)
    └── exception/
        ├── GlobalExceptionHandler.java
        └── ErrorResponse.java
```

---

## 5. Capa Domain

Es el **núcleo puro** de la aplicación. No depende de ningún framework: solo Java estándar.

### 5.1 Entidad `Company`

`domain/company/Company.java`

```java
public class Company {
    private CompanyId id;
    private String nombre;
    private String direccion;
    private String telefono;
    private LocalDateTime fechaCreacion;
    private int employeeCount;

    public static Company create(String nombre, String direccion, String telefono) {
        return new Company(null, nombre, direccion, telefono, LocalDateTime.now(), 0);
    }

    public void increaseEmployeeCount() { this.employeeCount++; }
    public void decreaseEmployeeCount() { if (this.employeeCount > 0) this.employeeCount--; }
    public void update(String nombre, String direccion, String telefono) { ... }
}
```

**Características clave:**
- **Sin anotaciones de Spring/Mongo**: es POJO puro.
- **Factory method `create()`**: produce instancias en un estado válido sin ID (lo asigna Mongo más tarde).
- **Reglas de negocio**: `increaseEmployeeCount()` y `decreaseEmployeeCount()` encapsulan invariantes (no decrementa por debajo de 0).

### 5.2 Entidad `Employee`

`domain/employee/Employee.java`

```java
public class Employee {
    private EmployeeId id;
    private String nombre, apellido, correo, cargo;
    private BigDecimal salario;
    private CompanyId companiaId;       // Referencia por ID (no hay joins en Mongo)
    private EmployeeStatus status;

    public void changeEmail(String correo) {
        if (correo == null || correo.isBlank())
            throw new IllegalArgumentException("El correo del empleado es obligatorio");
        this.correo = correo;
    }
}
```

La relación con `Company` se modela como **referencia por ID** (`CompanyId companiaId`), no como objeto anidado. Esto refleja la naturaleza documental de MongoDB.

### 5.3 Value Objects: `CompanyId` y `EmployeeId`

`domain/company/CompanyId.java`

```java
public record CompanyId(String value) {
    public CompanyId {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("CompanyId no puede ser nulo o vacío");
    }
}
```

**Beneficios de usar Value Objects** en lugar de `String`:
- **Type-safety**: no se puede pasar un `EmployeeId` donde se espera un `CompanyId`.
- **Validación centralizada**: el `record` valida en su compact constructor.
- **Inmutabilidad**: los records de Java son inmutables por diseño.

### 5.4 Excepciones de dominio

```java
public class DomainException extends RuntimeException { ... }
public class EntityNotFoundException extends DomainException { ... }
```

Estas excepciones expresan **violaciones del dominio**, no errores técnicos. El `GlobalExceptionHandler` las traduce a códigos HTTP.

---

## 6. Capa Application

Contiene **casos de uso** (orquestadores) y los **puertos** (interfaces que infrastructure debe implementar).

### 6.1 Puertos (interfaces)

`application/ports/repository/CompanyRepository.java`

```java
public interface CompanyRepository {
    Optional<Company> findById(CompanyId id);
    List<Company> findAll();
    Company save(Company company);
    void deleteById(CompanyId id);
    boolean existsById(CompanyId id);
    long countEmployees(CompanyId id);
}
```

> **Importante:** la interfaz vive en Application, pero la implementación vive en Infrastructure. Esta es la **inversión de dependencias** que hace funcionar Onion.

`application/ports/transaction/UnitOfWork.java`

```java
public interface UnitOfWork {
    <T> T execute(Supplier<T> action);
    void execute(Runnable action);
}
```

### 6.2 Casos de uso (Use Cases)

Cada operación de negocio es **una clase**. Ejemplo: `CreateCompanyUseCase`.

`application/company/usecase/CreateCompanyUseCase.java`

```java
@Service
public class CreateCompanyUseCase {
    private final CompanyRepository companyRepository;
    private final UnitOfWork unitOfWork;

    public CompanyResponse execute(CreateCompanyCommand command) {
        return unitOfWork.execute(() -> {
            Company company = Company.create(command.nombre(), command.direccion(), command.telefono());
            Company saved = companyRepository.save(company);
            return CompanyResponse.from(saved);
        });
    }
}
```

**Patrón común en todos los use cases:**
1. Reciben un **Command** (DTO de entrada).
2. Envuelven la operación en `unitOfWork.execute(...)` para transaccionalidad.
3. Llaman al dominio (`Company.create(...)`) y al repositorio.
4. Retornan un **Response** (DTO de salida).

### 6.3 DTOs de Application

Hay dos tipos:

**Commands** (entrada del use case):
```java
public record CreateCompanyCommand(String nombre, String direccion, String telefono) {}
```

**Responses** (salida del use case):
```java
public record CompanyResponse(String id, String nombre, ..., int employeeCount) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(company.getId().value(), company.getNombre(), ...);
    }
}
```

> **Nota:** estos DTOs **no llevan anotaciones de validación HTTP** (`@NotBlank`). Eso se queda en la capa API. Application no sabe que existe HTTP.

### 6.4 Reglas de negocio orquestadas

Ejemplo: no se puede borrar una compañía con empleados (`DeleteCompanyUseCase`):

```java
public void execute(String id) {
    unitOfWork.execute(() -> {
        CompanyId companyId = new CompanyId(id);
        if (!companyRepository.existsById(companyId))
            throw new EntityNotFoundException("Compania no encontrada con id: " + id);

        long employeeCount = companyRepository.countEmployees(companyId);
        if (employeeCount > 0)
            throw new DomainException("No se puede eliminar la compania porque tiene "
                + employeeCount + " empleado(s) asociado(s)");

        companyRepository.deleteById(companyId);
    });
}
```

---

## 7. Capa Infrastructure

Implementa los puertos definidos en Application usando tecnologías concretas (MongoDB, Spring Data).

### 7.1 Documentos (modelos persistentes)

`infrastructure/persistence/mongo/document/CompanyDocument.java`

```java
@Document(collection = "companies")
public class CompanyDocument {
    @Id private String id;
    private String nombre, direccion, telefono;
    private LocalDateTime fechaCreacion;
    private int employeeCount;
    // getters/setters
}
```

**¿Por qué hay un `CompanyDocument` distinto de `Company`?**
Para evitar contaminar el dominio con anotaciones de Mongo (`@Document`, `@Id`). Si mañana migras a PostgreSQL, solo cambias el documento y el mapper. La entidad `Company` no se toca.

`EmployeeDocument` añade un índice único en `correo`:
```java
@Indexed(unique = true)
private String correo;
```

### 7.2 Mappers (documento ↔ dominio)

`infrastructure/persistence/mongo/mapper/CompanyDocumentMapper.java`

```java
@Component
public class CompanyDocumentMapper {
    public CompanyDocument toDocument(Company company) { ... }
    public Company toDomain(CompanyDocument document) { ... }
}
```

Traduce en **ambas direcciones**: al guardar (dominio → documento) y al leer (documento → dominio).

### 7.3 Repositorios

Hay **dos niveles** de repositorios:

**Nivel 1 — Repositorio Spring Data (boilerplate):**
```java
public interface SpringDataCompanyMongoRepository extends MongoRepository<CompanyDocument, String> {}
```
Spring genera implementación automáticamente para `findAll()`, `findById()`, `save()`, etc.

**Nivel 2 — Implementación del puerto (adaptador):**
```java
@Repository
public class MongoCompanyRepository implements CompanyRepository {
    private final SpringDataCompanyMongoRepository mongoRepository;
    private final CompanyDocumentMapper mapper;

    @Override
    public Company save(Company company) {
        var document = mapper.toDocument(company);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }
}
```

Este adaptador:
- Implementa el puerto `CompanyRepository` (definido en Application).
- Delega operaciones a Spring Data.
- Aplica mappers para convertir entre dominio y documento.

### 7.4 Implementación del Unit of Work

`infrastructure/persistence/mongo/MongoUnitOfWork.java`

```java
@Component
public class MongoUnitOfWork implements UnitOfWork {
    private final TransactionTemplate transactionTemplate;

    public MongoUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        try {
            T result = transactionTemplate.execute(status -> action.get());
            log.info("Confirmación de una transacción");
            return result;
        } catch (Exception e) {
            log.error("Rollback de una transacción. Motivo: {}", e.getMessage());
            throw e;
        }
    }
}
```

### 7.5 Configuración de Mongo

`infrastructure/config/MongoConfig.java`

```java
@Configuration
@EnableMongoRepositories(basePackages = "com.companyemployees.infrastructure.persistence.mongo.repository")
public class MongoConfig {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @Bean
    public CommandLineRunner seedDatabase(...) { ... }  // Datos iniciales
}
```

- Habilita los repositorios Spring Data.
- Provee el `MongoTransactionManager` que usa el `UnitOfWork`.
- Carga datos semilla (3 compañías + 10 empleados) al arrancar.

---

## 8. Capa API (Presentation)

Es la capa más externa: expone HTTP y traduce JSON ↔ casos de uso.

### 8.1 Controladores REST

`api/controller/CompanyController.java`

```java
@RestController
@RequestMapping("/api/companias")
public class CompanyController {
    private final CreateCompanyUseCase createCompanyUseCase;
    // ... otros use cases

    @PostMapping
    public ResponseEntity<CompanyApiResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        CreateCompanyCommand command = new CreateCompanyCommand(
            request.nombre(), request.direccion(), request.telefono()
        );
        CompanyResponse created = createCompanyUseCase.execute(command);
        return ResponseEntity.created(URI.create("/api/companias/" + created.id()))
                             .body(mapToApiResponse(created));
    }
}
```

**Responsabilidades del controller:**
1. Recibir JSON y deserializar a un `Request` DTO.
2. Validar con `@Valid` (Jakarta Validation).
3. Mapear `Request` → `Command` y llamar al use case.
4. Mapear `Response` → `ApiResponse` y devolver JSON.

### 8.2 DTOs HTTP (Request / Response)

`api/request/CreateCompanyRequest.java`

```java
public record CreateCompanyRequest(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "La dirección es obligatoria") String direccion,
    @NotBlank(message = "El teléfono es obligatorio") String telefono
) {}
```

`api/response/CompanyApiResponse.java`

```java
public record CompanyApiResponse(
    String id, String nombre, String direccion, String telefono,
    LocalDateTime fechaCreacion, int employeeCount
) {}
```

### 8.3 Endpoints expuestos

**Compañías** (`/api/companias`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/companias` | Listar todas |
| GET | `/api/companias/{id}` | Obtener por ID |
| POST | `/api/companias` | Crear |
| PUT | `/api/companias/{id}` | Actualizar |
| DELETE | `/api/companias/{id}` | Eliminar |
| GET | `/api/companias/{id}/empleados` | Empleados de una compañía |
| **POST** | `/api/companias/con-empleados` | **Transaccional: crear compañía + empleados** |

**Empleados** (`/api/empleados`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/empleados` | Listar todos |
| GET | `/api/empleados/{id}` | Obtener por ID |
| POST | `/api/empleados` | Crear |
| PUT | `/api/empleados/{id}` | Actualizar |
| DELETE | `/api/empleados/{id}` | Eliminar |

---

## 9. Patrón Repository

### Definición
Abstrae el acceso a datos mediante interfaces, ocultando el motor de persistencia subyacente.

### Implementación en este proyecto

```
┌──────────────────────────────────────┐
│ application/ports/repository/        │
│   CompanyRepository (interface)      │   ← Contrato
└──────────────────────────────────────┘
                  ▲
                  │ implementa
                  │
┌──────────────────────────────────────┐
│ infrastructure/.../repository/       │
│   MongoCompanyRepository (clase)     │   ← Adaptador
│   ├─ usa SpringDataCompanyMongoRepo  │
│   └─ usa CompanyDocumentMapper       │
└──────────────────────────────────────┘
```

**Ventajas:**
- El use case no sabe que estamos usando Mongo.
- Se pueden crear implementaciones in-memory para tests.
- Se puede migrar a otro motor cambiando solo la capa Infrastructure.

---

## 10. Patrón Unit of Work

### Definición
Mantiene una lista de objetos afectados por una transacción de negocio y coordina su persistencia atómica (commit o rollback completo).

### Implementación

**Puerto** (`application/ports/transaction/UnitOfWork.java`):
```java
public interface UnitOfWork {
    <T> T execute(Supplier<T> action);
    void execute(Runnable action);
}
```

**Adaptador** (`infrastructure/.../MongoUnitOfWork.java`):
- Usa `TransactionTemplate` de Spring.
- Internamente usa `MongoTransactionManager`.
- **Requiere MongoDB con Replica Set** (Atlas lo provee por defecto).

### Caso de uso transaccional clave

`CreateCompanyWithEmployeesUseCase` crea una compañía + N empleados en una sola transacción:

```java
public CompanyWithEmployeesResponse execute(CreateCompanyWithEmployeesCommand command) {
    return unitOfWork.execute(() -> {
        Company savedCompany = companyRepository.save(Company.create(...));

        for (EmployeeData empData : command.empleados()) {
            Employee employee = Employee.create(..., savedCompany.getId());
            employeeRepository.save(employee);
        }

        return new CompanyWithEmployeesResponse(...);
    });
}
```

**Garantías:**
- Si **cualquier** `save()` falla → rollback completo. No queda ni la compañía ni los empleados parciales.
- Si todo sale bien → commit automático al terminar el bloque.

### Manejo de commit/rollback

| Evento | Comportamiento |
|--------|---------------|
| Bloque termina sin excepción | **Commit** automático |
| Bloque lanza `RuntimeException` | **Rollback** automático |
| Bloque lanza excepción chequeada | **Rollback** automático |

---

## 11. Flujo completo de una petición

Ejemplo: `POST /api/companias` con `{ "nombre": "Acme", "direccion": "...", "telefono": "..." }`

```
1.  HTTP POST → Spring DispatcherServlet → CompanyController.create()
                                                  │
2.  @Valid valida CreateCompanyRequest            │
    (si falla → 400 con MethodArgumentNotValidException)
                                                  │
3.  Controller convierte:                         │
    CreateCompanyRequest → CreateCompanyCommand   │
                                                  │
4.  CreateCompanyUseCase.execute(command)         │
                                                  ▼
5.  unitOfWork.execute(() -> {                   ┌─────────────────────┐
       Company c = Company.create(...);          │ Inicio transacción  │
       Company saved = companyRepository.save(c);└─────────────────────┘
       return CompanyResponse.from(saved);         │
    });                                            ▼
                                                  │
6.  MongoCompanyRepository.save(company)          │
    └─ mapper.toDocument(company) → CompanyDocument
    └─ springDataRepo.save(document)              │
    └─ mapper.toDomain(savedDocument) → Company   │
                                                  ▼
7.  Use case retorna CompanyResponse              ┌─────────────────────┐
                                                  │ Commit transacción  │
                                                  └─────────────────────┘
                                                  │
8.  Controller convierte:                         │
    CompanyResponse → CompanyApiResponse          │
                                                  │
9.  Spring serializa a JSON → HTTP 201 Created    │
                                                  ▼
```

---

## 12. Manejo de errores

### Excepciones del dominio
| Excepción | HTTP Status | Significado |
|-----------|-------------|-------------|
| `EntityNotFoundException` | 404 Not Found | El recurso no existe |
| `DomainException` | 409 Conflict | Violación de regla de negocio |
| `IllegalArgumentException` | 400 Bad Request | Argumento inválido |
| `MethodArgumentNotValidException` | 400 Bad Request | Validación `@Valid` falló |
| `DataAccessException`, `MongoException` | 500 Internal Server Error | Error de BD |
| `Exception` (resto) | 500 Internal Server Error | Error inesperado |

### `GlobalExceptionHandler`

`api/exception/GlobalExceptionHandler.java`

Centraliza el mapeo de excepciones → respuestas HTTP usando `@RestControllerAdvice`. Todas las respuestas usan el formato **RFC 7807 (Problem Details)**:

```json
{
  "type": "https://api.companyemployees.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Compania no encontrada con id: abc-123",
  "errors": null
}
```

---

## 13. Configuración y arranque

### `CompanyEmployeesApplication.java`

```java
@SpringBootApplication
public class CompanyEmployeesApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompanyEmployeesApplication.class, args);
    }
}
```

Spring Boot escanea el paquete `com.companyemployees` y registra automáticamente:
- `@Service` → Use Cases
- `@Repository` → Adaptadores de repositorio
- `@Component` → `MongoUnitOfWork`, mappers
- `@RestController` → Controladores
- `@Configuration` → `MongoConfig`

### `application.properties`
Define la URI de MongoDB Atlas y los niveles de logging.

### Comando de arranque
```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/companias` y Swagger UI en `http://localhost:8080/swagger-ui.html`.

---

## Resumen de beneficios arquitectónicos

| Beneficio | Cómo se logra |
|-----------|---------------|
| **Testabilidad** | El dominio se testea sin Spring ni Mongo. Los use cases se testean con mocks de los puertos. |
| **Independencia tecnológica** | Cambiar Mongo por PostgreSQL solo afecta Infrastructure. |
| **Independencia de transporte** | Los use cases podrían exponerse vía gRPC, CLI o jobs sin tocar Application/Domain. |
| **Lógica de negocio centralizada** | Las reglas viven en el dominio (`Company.increaseEmployeeCount()`) o en los use cases (`DeleteCompanyUseCase` valida que no haya empleados). |
| **Transaccionalidad explícita** | `UnitOfWork` hace visible dónde empieza y termina cada transacción. |
| **Errores consistentes** | `GlobalExceptionHandler` traduce excepciones de dominio a HTTP siguiendo RFC 7807. |
