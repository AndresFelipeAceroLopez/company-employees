PLAN_PARTE_II.md - Evolucion API Company Employees
Resumen
Este plan evoluciona el repositorio AndresFelipeAceroLopez/company-employees de acuerdo con la guia SENA Parte II: CRUD de colecciones, programacion asincrona, validaciones, pruebas, JWT por roles y JWT por politicas.

El proyecto actual usa Java 17, Spring Boot 3.4.5, Maven, Spring Web MVC, Spring Data MongoDB, Onion Architecture, Repository Pattern y Unit of Work.

La implementacion debe conservar siempre este flujo:

Controller -> UseCase/Application -> UnitOfWork -> Repository Port -> Mongo Repository Adapter -> MongoDB
No se debe crear un proyecto nuevo, no se deben mover reglas de negocio a los controllers y no se debe acceder a MongoDB directamente desde la capa API.

Arquitectura Que Se Debe Respetar
api: controladores REST, request/response DTOs, filtros/middleware web y manejo global de excepciones.
application: casos de uso, DTOs/comandos de aplicacion, puertos de repositorio, puertos de seguridad, reglas de negocio orquestadoras.
domain: entidades puras, value objects, enums y excepciones de dominio. No debe depender de Spring, MongoDB, JWT ni frameworks externos.
infrastructure: implementaciones concretas de repositorios Mongo, mappers, configuracion, JWT, hashing, Unit of Work y persistencia.
Reglas obligatorias:

Los controllers solo reciben HTTP, validan estructura basica, transforman requests a comandos y llaman use cases.
Los use cases coordinan reglas, repositorios y UnitOfWork.
Los repositorios no hacen commit, rollback ni manejan transacciones.
Las validaciones de negocio deben vivir en Application/use cases.
Los secretos no deben quedar escritos en el codigo fuente.
1. CRUD De Colecciones
Objetivo
Agregar operaciones de coleccion para empleados sin romper el CRUD individual existente.

Cambios En Application
Crear modelos reutilizables:

PagedResponse<T> con:
datos
pagina
tamano
total
totalPaginas
PageCriteria o equivalente con:
pagina, default 1
tamano, default 10
orden, default apellido
dir, default asc
buscar, opcional
Validar en Application:

pagina >= 1
tamano >= 1
tamano <= 100
dir solo permite asc o desc
orden solo permite campos autorizados: nombre, apellido, correo, salario, cargo, status
Extender EmployeeRepository con metodos de coleccion:

PagedResult<Employee> findPaged(PageCriteria criteria);
PagedResult<Employee> findPagedByCompaniaId(CompanyId companiaId, PageCriteria criteria);
List<Employee> saveAll(List<Employee> employees);
void deleteAllById(List<EmployeeId> ids);
List<Employee> findAllByIds(List<EmployeeId> ids);
PagedResult puede vivir en Application como objeto interno de persistencia, separado del response HTTP.

Cambios En Infrastructure
Implementar los nuevos metodos en MongoEmployeeRepository.

Usar MongoTemplate para:

construir filtros dinamicos con Criteria;
filtrar buscar sobre nombre, apellido o correo;
ordenar con Sort;
paginar con skip y limit;
contar el total con una query separada sin skip/limit.
No usar queries hardcodeadas en controllers.

Nuevos Use Cases
Agregar casos de uso dedicados:

GetPagedEmployeesUseCase
GetPagedCompanyEmployeesUseCase
CreateEmployeesBatchUseCase
PatchEmployeeUseCase
DeleteEmployeesBatchUseCase
La creacion masiva y eliminacion multiple deben ejecutarse dentro de UnitOfWork.

Reglas de lote:

Si algun empleado del lote es invalido, no se guarda ninguno.
Si alguna compania no existe, no se guarda ninguno.
Si algun correo ya existe, no se guarda ninguno.
Si hay correos duplicados dentro del mismo lote, no se guarda ninguno.
La eliminacion multiple debe validar que todos los ids existan antes de borrar.
Al crear o eliminar empleados, actualizar correctamente employeeCount de cada compania afectada dentro de la misma transaccion.
Endpoints
Agregar o modificar:

GET /api/empleados?pagina=1&tamano=10&orden=apellido&dir=asc&buscar=gomez
GET /api/companias/{id}/empleados?pagina=1&tamano=10
POST /api/empleados/lote
PATCH /api/empleados/{id}
DELETE /api/empleados/lote
Respuesta paginada obligatoria:

{
  "datos": [],
  "pagina": 1,
  "tamano": 10,
  "total": 57,
  "totalPaginas": 6
}
Codigos esperados:

200 OK: listados y PATCH exitoso.
201 Created: creacion masiva exitosa.
204 No Content: eliminacion multiple exitosa.
400 Bad Request: parametros de paginacion/filtro invalidos.
404 Not Found: recurso inexistente.
409 Conflict: correo duplicado o regla de dominio conflictiva.
422 Unprocessable Entity: cuerpo invalido.
2. Validaciones
Objetivo
Mantener Jakarta Bean Validation para validacion estructural, pero reforzar reglas en Application para cumplir la guia.

Validacion En API
Agregar o completar anotaciones:

Compania:
nombre: @NotBlank, @Size(min = 3, max = 100)
telefono: @NotBlank, @Pattern para digitos y longitud valida
Empleado:
nombre: @NotBlank
apellido: @NotBlank
correo: @NotBlank, @Email
cargo: @NotBlank
salario: @NotNull, @Positive
companiaId: @NotBlank
Para PATCH, crear un request propio, por ejemplo PatchEmployeeRequest, donde los campos sean opcionales. Solo se validan los campos presentes.

Validacion En Application
En use cases validar:

correo unico;
compania existente;
salario positivo cuando venga desde PATCH o lote;
duplicados dentro de operaciones masivas;
existencia de todos los empleados antes de eliminacion multiple;
ownership para politicas de usuario cuando aplique.
Manejo De Errores
Actualizar GlobalExceptionHandler para devolver formato uniforme:

{
  "mensaje": "Error de validacion",
  "errores": [
    { "campo": "correo", "detalle": "Formato de correo invalido" },
    { "campo": "salario", "detalle": "Debe ser mayor que 0" }
  ]
}
Se puede conservar ErrorResponse si se adapta, pero debe ser consistente.

Usar:

422 Unprocessable Entity para errores de validacion del cuerpo.
400 Bad Request para query params invalidos.
404 Not Found para recursos inexistentes.
409 Conflict para reglas de dominio como correo duplicado.
3. Programacion Asincrona
Decision Tecnica
No migrar a WebFlux ni a ReactiveMongoRepository, porque eso cambiaria demasiado el modelo actual y podria romper la estructura.

Spring MVC + MongoRepository es bloqueante. Por tanto, la solucion debe documentar honestamente que no hay asincronia real de I/O con el stack actual.

Implementacion Requerida
Agregar alternativa idiomatica:

Crear configuracion AsyncConfig con @EnableAsync.
Definir un TaskExecutor con nombre claro, por ejemplo applicationTaskExecutor.
Implementar asincronia con CompletableFuture en lecturas paginadas o listados.
Ejemplo de aplicacion:

GetPagedEmployeesUseCase#getPagedAsync(...)
GetPagedCompanyEmployeesUseCase#getPagedAsync(...)
Regla importante:

No ejecutar partes de una misma transaccion en hilos paralelos.
No compartir una sesion/transaccion Mongo entre tareas concurrentes.
Si una operacion transaccional se vuelve async, debe envolver toda la operacion completa dentro de un solo UnitOfWork.
Documentacion
En README explicar:

Spring MVC atiende peticiones con un modelo bloqueante por hilo.
Spring Data MongoDB usado actualmente tambien es bloqueante.
@Async + CompletableFuture no convierte MongoDB en reactivo; solo delega trabajo a un executor.
La alternativa reactiva real seria WebFlux + ReactiveMongoRepository, pero no se usa para conservar la arquitectura y alcance.
4. Seguridad JWT Por Roles
Dependencias
Agregar en pom.xml:

spring-boot-starter-security
jjwt-api
jjwt-impl
jjwt-jackson
Usar version 0.12.x.

Domain
Crear:

domain/user/User.java
domain/user/UserId.java
domain/user/Role.java
Campos minimos de User:

id
nombre
correo
passwordHash
role: ADMIN o USUARIO
companiaId: opcional para ADMIN, obligatorio para USUARIO
fechaCreacion
La contrasena nunca debe guardarse ni devolverse en texto plano.

Application
Crear puertos:

UserRepository
JwtService
PasswordHasher
Crear use cases:

RegisterUserUseCase
LoginUserUseCase
GetAuthenticatedProfileUseCase
Reglas:

correo de usuario unico.
Password se guarda con hash.
Login compara hash, no texto plano.
JWT debe incluir claims:
sub: id del usuario
correo
role
companiaId
exp
Infrastructure
Crear:

UserDocument
UserDocumentMapper
SpringDataUserMongoRepository
MongoUserRepository
JjwtService
BCryptPasswordHasher
API
Crear AuthController con:

POST /api/auth/registro
POST /api/auth/login
GET /api/auth/perfil
Crear request/response DTOs:

RegisterRequest
LoginRequest
AuthResponse
AuthenticatedUserResponse
Security Config
Crear configuracion:

SecurityConfig
JwtAuthenticationFilter
CustomUserDetailsService o equivalente
Reglas de acceso:

POST /api/auth/registro: publico.
POST /api/auth/login: publico.
Swagger/OpenAPI: publico para pruebas academicas.
GET de companias y empleados: cualquier usuario autenticado.
POST/PUT/PATCH de empleados: ADMIN o USUARIO.
DELETE de empleados: ADMIN o USUARIO, pero con policy de ownership.
DELETE de companias: solo ADMIN.
POST /api/companias/con-empleados: solo ADMIN.
Nota importante:

La guia propone DELETE como solo ADMIN, pero tambien exige una politica donde un USUARIO pueda eliminar empleados de su propia compania. Para no contradecir la policy, DELETE /api/empleados/{id} y DELETE /api/empleados/lote deben aceptar ADMIN o USUARIO y luego aplicar EsPropietarioDeCompania.

5. Seguridad JWT Por Politicas
Objetivo
Implementar autorizacion fina basada en claims y reglas de recurso.

Politica Obligatoria
EsPropietarioDeCompania:

ADMIN puede actualizar o eliminar cualquier empleado.
USUARIO solo puede actualizar o eliminar empleados cuyo companiaId coincida con el companiaId incluido en su JWT.
Si el empleado no existe, responder 404.
Si existe pero no pertenece a la compania del usuario, responder 403.
Implementacion
Activar:

@EnableMethodSecurity
Crear un componente de seguridad, por ejemplo:

EmployeeAuthorizationService
Este componente debe depender del puerto EmployeeRepository, no de MongoEmployeeRepository.

Metodos sugeridos:

boolean canModifyEmployee(Authentication authentication, String employeeId);
boolean canModifyEmployees(Authentication authentication, List<String> employeeIds);
Aplicar en endpoints o use cases con @PreAuthorize.

Ejemplos:

@PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployee(authentication, #id)")
Para eliminacion multiple:

@PreAuthorize("hasRole('ADMIN') or @employeeAuthorizationService.canModifyEmployees(authentication, #request.ids())")
6. Seguridad Y Configuracion
Variables De Entorno
Modificar application.properties:

spring.data.mongodb.uri=${MONGODB_URI}
spring.data.mongodb.database=${MONGODB_DATABASE:company_employees_db}
jwt.secret=${JWT_SECRET}
jwt.expiration-minutes=${JWT_EXPIRATION_MINUTES:60}
No dejar credenciales reales en el repo.

Accion externa obligatoria:

Rotar la credencial de MongoDB Atlas que ya aparecio en application.properties.
Esta rotacion debe hacerla el dueno de la cuenta en MongoDB Atlas; el agente solo debe eliminar el secreto del codigo y documentar las variables.
Seed Data
Actualmente existen indicios de dos inicializadores: MongoConfig y DataInitializer.

Consolidar a uno solo:

Mantener preferiblemente DataInitializer.
Eliminar seed duplicado de MongoConfig.
Controlar seeds con perfil o propiedad, por ejemplo app.seed.enabled=true.
Seed obligatorio:

Un usuario ADMIN con password hasheado.
Un usuario USUARIO asociado a una compania existente.
Companias y empleados de prueba.
No imprimir contrasenas en logs.

7. Pruebas
Dependencias
Mantener spring-boot-starter-test.

Agregar:

Testcontainers MongoDB.
Mockito ya viene cubierto por Spring Boot Test.
Configuracion De Tests
Crear:

src/test/resources/application-test.properties
Usar perfil test.

Desactivar seed automatico durante pruebas o controlarlo explicitamente para que no contamine escenarios.

Para pruebas transaccionales con MongoDB, usar MongoDB con replica set. Testcontainers debe configurarse de forma compatible con transacciones.

Pruebas Unitarias
Cubrir:

CreateEmployeesBatchUseCase
PatchEmployeeUseCase
DeleteEmployeesBatchUseCase
LoginUserUseCase
RegisterUserUseCase
EmployeeAuthorizationService
Escenarios minimos:

Creacion masiva exitosa.
Creacion masiva falla por correo duplicado existente.
Creacion masiva falla por duplicado dentro del lote.
PATCH modifica solo campos enviados.
Eliminacion multiple falla si falta un id.
Login devuelve token cuando las credenciales son correctas.
Login falla con password incorrecto.
Usuario propietario puede modificar empleado de su compania.
Usuario no propietario recibe denegacion.
ADMIN puede modificar cualquier empleado.
Pruebas De Integracion
Usar MockMvc.

Cubrir:

GET /api/empleados con paginacion.
GET /api/empleados con filtro buscar.
GET /api/empleados con ordenamiento.
GET /api/companias/{id}/empleados paginado.
POST /api/empleados/lote.
PATCH /api/empleados/{id}.
DELETE /api/empleados/lote.
Validacion con respuesta 422.
401 Unauthorized sin token.
403 Forbidden con rol insuficiente o policy fallida.
200/204 cuando la policy ownership se cumple.
Prueba Obligatoria De Rollback
Escenario:

Enviar POST /api/companias/con-empleados.
Incluir varios empleados.
Hacer que uno falle, por ejemplo por correo duplicado o salario negativo.
Verificar que no quedo guardada la compania.
Verificar que no quedo guardado ningun empleado del lote.
Esta prueba demuestra que UnitOfWork controla la transaccion.

8. Documentacion
Actualizar README.md agregando:

CRUD de colecciones
Paginacion, filtrado y ordenamiento
Programacion asincrona
Mi tecnologia soporta async
Validaciones
Pruebas
Prueba del rollback transaccional
Seguridad
Autenticacion con JWT
Autorizacion por roles
Autorizacion por politicas
Variables de entorno
Comparacion ampliada con ASP.NET Core
Conclusiones Parte II
Documentar comandos:

./mvnw spring-boot:run
./mvnw test
Documentar variables:

MONGODB_URI
MONGODB_DATABASE
JWT_SECRET
JWT_EXPIRATION_MINUTES
Agregar tabla comparativa:

Concepto ASP.NET Core	Equivalente en este proyecto
Endpoints de coleccion	Controllers + UseCases + PagedResponse
Paginacion Skip/Take	MongoTemplate skip/limit
async/await + Task<T>	@Async + CompletableFuture, con aclaracion de I/O bloqueante
DataAnnotations / FluentValidation	Jakarta Bean Validation + reglas en Application
xUnit / NUnit + Moq	JUnit 5 + Mockito + MockMvc
AddAuthentication().AddJwtBearer()	Spring Security + JwtAuthenticationFilter
[Authorize(Roles="...")]	@PreAuthorize / reglas SecurityFilterChain
[Authorize(Policy="...")]	@PreAuthorize + EmployeeAuthorizationService
ClaimsPrincipal / Claims	Authentication + claims del JWT
9. Criterios De Aceptacion
La implementacion se considera completa cuando:

CRUD individual existente sigue funcionando.
Listados paginados devuelven envelope con datos, pagina, tamano, total, totalPaginas.
Bulk create y delete multiple son atomicos con UnitOfWork.
PATCH modifica solo campos enviados.
Validaciones invalidas devuelven formato uniforme.
No hay secretos en application.properties.
Login genera JWT valido.
Endpoints protegidos devuelven 401, 403 o exito segun corresponda.
Politica ownership funciona para USUARIO y deja pasar a ADMIN.
Prueba de rollback demuestra que no quedan datos parciales.
README explica async real vs alternativa usada.
./mvnw test pasa.
10. Orden Recomendado De Implementacion
Limpiar configuracion de secretos y consolidar seeders.
Agregar modelos de paginacion y criterios en Application.
Extender puertos de repositorio.
Implementar consultas con MongoTemplate.
Agregar use cases de coleccion.
Agregar endpoints de coleccion.
Reforzar validaciones y errores.
Agregar configuracion async y documentacion.
Agregar dominio/puertos/use cases de usuario y auth.
Agregar infraestructura JWT, BCrypt y Mongo user repository.
Configurar Spring Security.
Implementar policy ownership.
Agregar pruebas unitarias.
Agregar pruebas de integracion.
Actualizar README.
Ejecutar ./mvnw test.
11. Supuestos
Se trabaja sobre el repo AndresFelipeAceroLopez/company-employees.
La rama base esperada es main.
Se conserva Spring MVC y Spring Data MongoDB bloqueante.
No se migra a WebFlux.
MongoDB debe estar en replica set para transacciones reales.
Las capas api, application, domain e infrastructure se mantienen.
La rotacion de credenciales de MongoDB Atlas la hace el dueno de la cuenta.
El agente implementador puede agregar archivos, clases y dependencias, pero no debe cambiar el enfoque arquitectonico.