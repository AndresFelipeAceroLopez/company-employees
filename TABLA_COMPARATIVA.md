# Tabla Comparativa: ASP.NET Core vs Java Spring Boot

| Concepto en ASP.NET Core | Equivalente en Java Spring Boot |
|--------------------------|----------------------------------|
| Controller | @RestController |
| Entity | @Entity (JPA) |
| DbContext | EntityManager / MongoTemplate |
| DbSet | MongoRepository<T, ID> |
| Migration | No aplica (MongoDB es NoSQL) |
| Fluent API | @Document, @Field, @Indexed |
| Repository | Interface + Implementation |
| Unit of Work | @Transactional / MongoUnitOfWork |
| Service Layer | @Service |
| Dependency Injection | @Autowired / @Component |
| appsettings.json | application.properties |
| Program.cs / Startup.cs | @SpringBootApplication |
| Middleware | @Component / Filter |
| Logging | SLF4J + Logback |