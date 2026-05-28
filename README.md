# API de Compañías y Empleados

## Tecnología usada
- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3.4.5
- **Gestor de dependencias**: Maven

## ORM usado
- **ORM / ODM**: Spring Data MongoDB
- **Justificación**: Spring Data proporciona una capa de abstracción muy similar a EF Core para bases de datos NoSQL, permitiendo manejar repositorios e integrando la configuración y mapeo a través de anotaciones en clases. Se seleccionó MongoDB debido a su alta flexibilidad para modelar documentos y su excelente integración con Spring Boot.

## Arquitectura aplicada
Se aplicó **Onion Architecture** (Arquitectura Cebolla / Clean Architecture) estructurando el proyecto en capas bien delimitadas con dependencias dirigidas hacia el interior:
- **Domain**: Núcleo de la aplicación. Contiene las entidades (`Company`, `Employee`), Value Objects (`CompanyId`, `EmployeeId`) e interfaces (contratos de Repositorios y Unit of Work).
- **Application**: Casos de uso (`UseCases`), DTOs de entrada y salida, y los puertos. Contiene la lógica orquestadora.
- **Infrastructure**: Implementaciones concretas de los repositorios (`MongoCompanyRepository`), el `MongoUnitOfWork`, configuración de base de datos y mapeos.
- **API (Presentation)**: Controladores REST, mapeo de rutas, y manejo global de excepciones.

## Estructura del proyecto
```
src/main/java/com/companyemployees/
├── domain/ (Capa Domain - Entidades e interfaces)
├── application/ (Capa Application - Casos de uso y DTOs)
├── infrastructure/ (Capa Infrastructure - Persistencia y configs)
└── api/ (Capa API - Controladores y DTOs REST)
```

## Entidades
Las entidades principales son:
1. **Company**: Contiene `id`, `nombre`, `direccion`, `telefono`, y `fechaCreacion`.
2. **Employee**: Contiene `id`, `nombre`, `apellido`, `correo`, `cargo`, `salario`, `companiaId` y `status`.

## Relación entre entidades
Existe una relación de **Uno a Muchos (1:N)**.
- Una Compañía puede tener varios Empleados.
- Un Empleado pertenece a una única Compañía (referenciada a través de `companiaId`).

## Repository Pattern
Se implementó el patrón Repositorio para abstraer el acceso a la base de datos.
- En la capa `Domain / Application` se definieron las interfaces `CompanyRepository` y `EmployeeRepository` con métodos base (`save`, `findById`, `findAll`, `delete`).
- En la capa `Infrastructure` se crearon las implementaciones concretas `MongoCompanyRepository` y `MongoEmployeeRepository` que por debajo llaman a Spring Data MongoDB.

## Unit of Work

### ¿Qué es Unit of Work?
El patrón Unit of Work mantiene una lista de objetos afectados por una transacción comercial y coordina la escritura de los cambios y la resolución de problemas de concurrencia. Es un objeto que encapsula una única sesión de la base de datos, agrupa varias operaciones en una sola transacción y confirma o revierte todo de forma atómica.

### ¿Cómo se implementó en esta tecnología?
En Spring Boot + MongoDB, se implementó usando `MongoTransactionManager` para manejar las transacciones. Se creó una interfaz `UnitOfWork` en la capa de aplicación y se implementó como `MongoUnitOfWork` en Infraestructura. Esta clase se encarga de inyectar las dependencias de los repositorios y provee métodos para ejecutar operaciones transaccionales.

### ¿Cómo se manejan las transacciones?
Las transacciones se manejan utilizando programación declarativa o programática mediante el objeto `MongoTemplate` y `MongoTransactionManager`. El `UnitOfWork` recibe un bloque de código y garantiza que todo lo ejecutado dentro de él ocurra en el contexto de una única transacción de base de datos.

### ¿Cómo se hace commit?
El commit ocurre de forma automática al finalizar con éxito el bloque de código pasado al método ejecutor del `UnitOfWork`. En MongoDB, el driver hace commit a la sesión transaccional si no se han lanzado excepciones.

### ¿Cómo se hace rollback?
El rollback ocurre automáticamente si ocurre una excepción (`RuntimeException` o cualquier tipo de `Exception` propagada) durante la ejecución del bloque transaccional. La sesión descarta cualquier cambio pendiente.

## Endpoints

**Compañías:**
- `GET /api/companias`: Listar todas las compañías.
- `GET /api/companias/{id}`: Consultar una compañía por ID.
- `POST /api/companias`: Crear una compañía.
- `PUT /api/companias/{id}`: Actualizar una compañía.
- `DELETE /api/companias/{id}`: Eliminar una compañía.
- `GET /api/companias/{id}/empleados`: Listar empleados de una compañía.

**Empleados:**
- `GET /api/empleados`: Listar todos los empleados.
- `GET /api/empleados/{id}`: Consultar un empleado por ID.
- `POST /api/empleados`: Crear un empleado.
- `PUT /api/empleados/{id}`: Actualizar un empleado.
- `DELETE /api/empleados/{id}`: Eliminar un empleado.

## Endpoint transaccional
Se implementó `POST /api/companias/con-empleados` que recibe en el JSON los datos de una nueva compañía y un listado de empleados para crearla. En caso de fallo con cualquier validación o inserción de la base de datos, la operación hace _rollback_ completo, no guardando la compañía ni los empleados insertados hasta el fallo.

## Instalación
1. Clonar el repositorio.
2. Contar con Java 17 o superior instalado y configurado correctamente en el PATH.
3. Actualizar la variable de configuración de la BD si es necesario en `src/main/resources/application.properties`.

## Configuración de base de datos
- **Motor**: MongoDB (MongoDB Atlas Cloud).
- **URI**: Configurada a través de `spring.data.mongodb.uri` en `application.properties`.
- Se requiere configuración de Replica Set en MongoDB para que soporten transacciones ACID, lo cual MongoDB Atlas provee por defecto.

## Migraciones
MongoDB es una base de datos _schema-less_ (sin esquemas estrictos) de tipo documental, por lo cual **no requiere un sistema tradicional de migraciones** (como EF Core o Flyway) para modificar columnas. Las colecciones se generan automáticamente y las validaciones se aplican en la capa de la aplicación de Java o a través de JsonSchema en el servidor de MongoDB.

## Ejecución del proyecto
Para compilar y ejecutar el proyecto:
```bash
./mvnw spring-boot:run
```

## Pruebas con Swagger/Postman
El proyecto cuenta con la dependencia **springdoc-openapi** integrada. Al ejecutar la aplicación, puedes probar y consultar todos los endpoints accediendo desde el navegador a:
`http://localhost:8080/swagger-ui.html`

*(Las capturas de pantalla de la ejecución y uso de Swagger se deben generar tras correr el proyecto y adjuntar)*

## Logging
El proyecto implementa un registro de logs a través de **SLF4J + Logback**.
- La salida está configurada por consola y archivos.
- Las consultas y eventos del Unit of Work/MongoDB se logean con nivel `DEBUG` mediante `logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG`.
- Se registra inicio, finalización y errores inesperados usando `GlobalExceptionHandler`.

## Uso de IA
Se utilizó la Inteligencia Artificial de Google Gemini para:
1. Traducir conceptos de ASP.NET Core y Entity Framework a Java Spring Boot y MongoDB.
2. Generar el patrón `UnitOfWork` adaptado para Spring Data.
3. Construir la estructura completa orientada a Arquitectura Onion.
4. Generar DTOs y validaciones de forma rápida.

---

# Parte II — Evolucion de la API

La Parte II conserva la arquitectura Onion y el flujo
`Controller -> UseCase -> UnitOfWork -> Repository Port -> Mongo Adapter -> MongoDB`.
Se agregan operaciones de coleccion, validaciones reforzadas, asincronia idiomatica,
seguridad con JWT por roles y politicas, y manejo uniforme de errores.

## Variables de entorno

| Variable                 | Obligatoria | Descripcion                                                        |
|--------------------------|-------------|--------------------------------------------------------------------|
| `MONGODB_URI`            | Si          | Cadena completa de conexion a Mongo (Atlas o replica set local).   |
| `MONGODB_DATABASE`       | No          | Nombre de la base. Default `company_employees_db`.                 |
| `JWT_SECRET`             | Si en prod  | Clave HMAC (>= 32 chars). En dev tiene default no apto para prod.  |
| `JWT_EXPIRATION_MINUTES` | No          | Vida del access token. Default `60`.                               |
| `APP_SEED_ENABLED`       | No          | Activa el seed inicial. Default `true`.                            |
| `APP_SEED_ADMIN_EMAIL`   | No          | Correo del usuario ADMIN inicial.                                  |
| `APP_SEED_ADMIN_PASSWORD`| No          | Contrasena del usuario ADMIN inicial (se almacena con BCrypt).     |
| `APP_SEED_USER_EMAIL`    | No          | Correo del usuario USUARIO inicial.                                |
| `APP_SEED_USER_PASSWORD` | No          | Contrasena del USUARIO inicial.                                    |

**Importante**: la credencial de MongoDB Atlas que aparecia previamente en
`application.properties` debe rotarse en la consola de Atlas. El repositorio ya no
contiene secretos en codigo fuente.

## CRUD de colecciones

Nuevos endpoints (manteniendo los existentes):

| Metodo | Ruta                                                                            | Notas                       |
|--------|---------------------------------------------------------------------------------|-----------------------------|
| GET    | `/api/empleados?pagina=1&tamano=10&orden=apellido&dir=asc&buscar=gomez`         | Lista paginada con filtros. |
| GET    | `/api/companias/{id}/empleados?pagina=1&tamano=10&orden=apellido&dir=asc`       | Por compania, paginado.     |
| POST   | `/api/empleados/lote`                                                           | Creacion masiva atomica.    |
| PATCH  | `/api/empleados/{id}`                                                           | Actualizacion parcial.      |
| DELETE | `/api/empleados/lote`                                                           | Eliminacion masiva atomica. |

### Paginacion, filtrado y ordenamiento

Los parametros se validan en Application (`PageCriteria`):

- `pagina` >= 1, default 1
- `tamano` entre 1 y 100, default 10
- `orden` en `{nombre, apellido, correo, salario, cargo, status}`, default `apellido`
- `dir` en `{asc, desc}`, default `asc`
- `buscar` opcional; filtra por `nombre`, `apellido` o `correo` con regex insensible
- Implementado en `MongoEmployeeRepository` con `MongoTemplate`: `Criteria` + `Sort` +
  `skip/limit`, y un `count` separado para el `total`.

**Respuesta paginada**:

```json
{
  "datos": [],
  "pagina": 1,
  "tamano": 10,
  "total": 57,
  "totalPaginas": 6
}
```

### Reglas de los lotes (todo-o-nada)

`CreateEmployeesBatchUseCase` y `DeleteEmployeesBatchUseCase` corren dentro de
`UnitOfWork`. Para crear empleados, se valida en orden:

1. Salarios positivos para cada elemento.
2. Sin correos duplicados dentro del lote.
3. Todas las companias referenciadas existen.
4. Ningun correo ya existe en la base.

Si alguna regla falla, no se guarda nada y se actualiza `employeeCount` de cada
compania afectada en la misma transaccion. La eliminacion masiva requiere que
todos los ids existan antes de borrar.

## Validaciones

- **Capa API**: `jakarta.validation` en cada `Request` (Create/Update/Patch/Login/
  Register). Para PATCH (`PatchEmployeeRequest`) los campos son opcionales y solo se
  validan si vienen presentes.
- **Capa Application**: los use cases verifican unicidad de correo, existencia de
  compania, salario positivo, duplicados en lote, presencia de todos los ids antes de
  eliminar y ownership cuando aplica.
- **Formato uniforme de error** (`ErrorResponse`):

  ```json
  {
    "mensaje": "Error de validacion",
    "errores": [
      { "campo": "correo", "detalle": "Formato de correo invalido" },
      { "campo": "salario", "detalle": "Debe ser mayor que 0" }
    ]
  }
  ```

  - `422 Unprocessable Entity`: validacion del cuerpo (`MethodArgumentNotValidException`).
  - `400 Bad Request`: query/path params invalidos.
  - `404 Not Found`: recursos inexistentes.
  - `409 Conflict`: correo duplicado u otras reglas de dominio.
  - `401 Unauthorized` / `403 Forbidden`: errores de autenticacion/autorizacion.

## Programacion asincrona

Decision: **no migrar a WebFlux ni `ReactiveMongoRepository`**. Spring MVC y
Spring Data MongoDB son bloqueantes; cambiar el stack romperia la arquitectura.

Alternativa implementada:

- `AsyncConfig` con `@EnableAsync` y un `applicationTaskExecutor`
  (`ThreadPoolTaskExecutor`: core 4, max 8, cola 50).
- Variantes `getPagedAsync(...)` anotadas con `@Async("applicationTaskExecutor")`
  que devuelven `CompletableFuture<PagedResult<EmployeeResponse>>` en
  `GetPagedEmployeesUseCase` y `GetPagedCompanyEmployeesUseCase`.
- Los endpoints `GET /api/empleados` y `GET /api/companias/{id}/empleados`
  devuelven `CompletableFuture<...>`: Spring MVC procesa la peticion de forma
  asincrona (el hilo del contenedor se libera mientras corre el executor). La
  validacion de parametros sigue siendo sincrona, de modo que un filtro invalido
  responde 400 antes de delegar al executor.

**Limitacion honesta**: `@Async` + `CompletableFuture` solo desplaza el trabajo a
otro hilo del pool. **No convierte** el I/O de Mongo en no-bloqueante. Para
asincronia real de I/O habria que migrar a WebFlux + `ReactiveMongoRepository`.

**Regla**: nunca se ejecutan partes de una misma transaccion en hilos paralelos.
Si una operacion transaccional se vuelve async, toda la operacion se envuelve en
un solo `UnitOfWork`.

## Seguridad

### Autenticacion con JWT

- `POST /api/auth/registro` — publico. Crea un usuario, hashea la contrasena con
  BCrypt y devuelve un JWT.
- `POST /api/auth/login` — publico. Verifica `correo` + `password` y devuelve JWT.
- `GET /api/auth/perfil` — requiere autenticacion. Devuelve los datos del token.

Claims del token: `sub` (userId), `correo`, `role`, `companiaId`, `exp`.

Implementacion:
- Puertos en Application: `JwtService`, `PasswordHasher`, `UserRepository`.
- Adapters en Infrastructure: `JjwtService` (jjwt 0.12.x), `BCryptPasswordHasher`,
  `MongoUserRepository` con `UserDocument` y `UserDocumentMapper`.

### Autorizacion por roles

`SecurityConfig` declara reglas globales con `SecurityFilterChain`:

| Ruta                                   | Acceso                          |
|----------------------------------------|---------------------------------|
| `POST /api/auth/registro`, `/login`    | Publico                         |
| `GET /api/companias/**`, `/empleados/**` | Autenticado                   |
| Swagger/OpenAPI                        | Publico                         |
| Resto                                  | Autenticado + @PreAuthorize     |

Reglas a nivel metodo con `@PreAuthorize` (gracias a `@EnableMethodSecurity`):

- `POST/PUT/PATCH /api/empleados/**`: `ADMIN` o `USUARIO`.
- `DELETE /api/empleados/{id}`: `ADMIN` o `USUARIO` con ownership.
- `DELETE /api/empleados/lote`: `ADMIN` o `USUARIO` con ownership.
- `DELETE /api/companias/{id}`: solo `ADMIN`.
- `POST /api/companias/con-empleados`: solo `ADMIN`.

### Autorizacion por politicas — `EsPropietarioDeCompania`

`EmployeeAuthorizationService` (bean `employeeAuthorizationService`) implementa la
politica de ownership. Depende de `EmployeeRepository` (puerto), nunca de la
implementacion Mongo. Lo invocan los controllers mediante `@PreAuthorize`:

```java
@PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployee(authentication, #id)")
```

Semantica:

- `ADMIN`: permite todo.
- `USUARIO`: permite solo cuando el `companiaId` del empleado coincide con el
  `companiaId` del JWT.
- Empleado inexistente: `404 Not Found`.
- Empleado existe pero no pertenece a la compania del usuario: `403 Forbidden`.

## Pruebas

Dependencias de prueba: `spring-boot-starter-test`, `spring-security-test` y
Testcontainers (`spring-boot-testcontainers`, `junit-jupiter`, `mongodb`). El
perfil `test` (`src/test/resources/application-test.properties`) desactiva el seed
y usa un `jwt.secret` propio. La conexion a Mongo la provee Testcontainers via
`@ServiceConnection` con un **replica set de un solo nodo**, necesario para probar
transacciones reales.

> **Requiere Docker.** Las pruebas de integracion estan anotadas con
> `@Testcontainers(disabledWithoutDocker = true)`: si no hay Docker se **omiten**
> (no fallan), de modo que `./mvnw test` pasa igual en maquinas sin Docker. Las
> pruebas unitarias no necesitan Docker.

**Pruebas unitarias** (JUnit 5 + Mockito, sin contexto Spring):

- `CreateEmployeesBatchUseCaseTest`: exito; fallo por correo ya existente en BD;
  duplicado dentro del lote; salario no positivo; compania inexistente.
- `PatchEmployeeUseCaseTest`: solo modifica campos enviados; 404; correo duplicado.
- `DeleteEmployeesBatchUseCaseTest`: exito; falla si falta un id (no borra nada).
- `LoginUserUseCaseTest`: token con credenciales correctas; password incorrecto;
  usuario inexistente.
- `RegisterUserUseCaseTest`: exito; correo duplicado; password corto; USUARIO sin
  companiaId.
- `EmployeeAuthorizationServiceTest`: ADMIN siempre permite; propietario permite;
  no propietario deniega; 404; reglas de lote.

**Pruebas de integracion** (MockMvc + Testcontainers):

- `EmployeeApiIntegrationTest`: paginacion (envelope `datos/pagina/tamano/total/
  totalPaginas`), filtro `buscar`, ordenamiento, empleados por compania, lote (201),
  PATCH (200), borrado masivo (204), cuerpo invalido (422), sin token (401),
  policy de ownership (403 no propietario / 200 propietario).
- `TransactionRollbackIntegrationTest` (**prueba obligatoria de rollback**):
  - Via `POST /api/companias/con-empleados` con un empleado de correo ya existente:
    responde 409 y **no** queda guardada la compania nueva ni ningun empleado del lote.
  - Via `UnitOfWork` directo: se guarda compania + empleado y luego se lanza una
    excepcion; se verifica que **ambas escrituras se revierten**.

## Tabla comparativa con ASP.NET Core (ampliada)

| Concepto ASP.NET Core                   | Equivalente en este proyecto                                  |
|-----------------------------------------|---------------------------------------------------------------|
| Endpoints de coleccion                  | Controllers + UseCases + `PagedResponse`                      |
| Paginacion Skip/Take                    | `MongoTemplate` con `skip`/`limit` y `Sort`                   |
| `async/await` + `Task<T>`               | `@Async` + `CompletableFuture` (I/O sigue siendo bloqueante)  |
| DataAnnotations / FluentValidation      | Jakarta Bean Validation + reglas en Application               |
| xUnit / NUnit + Moq                     | JUnit 5 + Mockito + MockMvc + Testcontainers                  |
| `AddAuthentication().AddJwtBearer()`    | Spring Security + `JwtAuthenticationFilter`                   |
| `[Authorize(Roles="...")]`              | `@PreAuthorize("hasRole('...')")` / reglas en `SecurityFilterChain` |
| `[Authorize(Policy="...")]`             | `@PreAuthorize("@employeeAuthorizationService...")`           |
| `ClaimsPrincipal` / Claims              | `Authentication` + `JwtPrincipal` con claims del JWT          |

## Comandos

```bash
# Definir variables en PowerShell
$env:MONGODB_URI = "mongodb+srv://USER:PASSWORD@cluster.mongodb.net/?retryWrites=true"
$env:JWT_SECRET = "una-clave-de-al-menos-32-caracteres-rotada-en-prod"

# Ejecutar
./mvnw spring-boot:run

# Tests
./mvnw test
```

## Conclusiones Parte II

- La arquitectura Onion absorbe la evolucion sin fricciones: todo lo nuevo
  (paginacion, lotes, auth, policies) se expreso como puertos en Application y
  adapters en Infrastructure; los controllers solo orquestan HTTP.
- Las transacciones se concentran en los use cases, nunca en repositorios o
  controllers, manteniendo la responsabilidad clara.
- La asincronia honesta requirio explicar el limite del stack bloqueante en vez de
  fingir una migracion a WebFlux que rompiera el alcance.
- La separacion puerto/adaptador permitio que `EmployeeAuthorizationService`
  consuma `EmployeeRepository` (puerto) y siga siendo testeable sin Mongo.
- El manejo uniforme de errores con `mensaje` + `errores` simplifica el contrato
  publico y elimina ramas duplicadas en los handlers.

## Conclusiones (Parte I)

Migrar una arquitectura conceptual basada en C# y .NET hacia Java y Spring Boot es totalmente viable. Al utilizar Onion Architecture, la lógica de negocio se mantuvo intacta y aislada, mientras que las tecnologías concretas (Spring Web, MongoDB, Spring Data) quedaron relegadas a las capas externas, demostrando que los patrones de arquitectura y persistencia son agnósticos al ecosistema tecnológico.
