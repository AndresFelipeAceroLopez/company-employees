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

## Conclusiones
Migrar una arquitectura conceptual basada en C# y .NET hacia Java y Spring Boot es totalmente viable. Al utilizar Onion Architecture, la lógica de negocio se mantuvo intacta y aislada, mientras que las tecnologías concretas (Spring Web, MongoDB, Spring Data) quedaron relegadas a las capas externas, demostrando que los patrones de arquitectura y persistencia son agnósticos al ecosistema tecnológico.
