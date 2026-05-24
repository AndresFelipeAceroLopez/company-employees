# Investigación del ORM - Spring Data MongoDB

## ¿Cuál ORM van a usar?
**Spring Data MongoDB** - Es el ODM (Object Document Mapper) oficial de Spring para MongoDB.

## ¿Por qué ese ORM es adecuado?
- **Integración nativa**: Se integra perfectamente con el ecosistema Spring Boot
- **Simplicidad**: Reduce el código boilerplate significativamente
- **Flexibilidad**: Permite tanto consultas automáticas como consultas personalizadas
- **Soporte completo**: Maneja automáticamente la serialización/deserialización de objetos Java a documentos BSON
- **Transacciones**: Soporta transacciones en MongoDB (desde la versión 4.0+)

## ¿Cómo se define una entidad?
```java
@Document(collection = "companies")
public class CompanyDocument {
    @Id
    private String id;
    private String nombre;
    // otros campos...
}
```

## ¿Cómo se configura una relación uno a muchos?
En MongoDB no hay JOINs como en SQL. Se maneja por referencia:
```java
// En Employee
private String companiaId; // Referencia al ID de la compañía
```

## ¿Cómo se hacen migraciones?
MongoDB es schema-less, no requiere migraciones tradicionales. Los cambios se aplican automáticamente.

## ¿Cómo se insertan datos iniciales?
```java
@Component
public class DataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Insertar datos iniciales
    }
}
```

## ¿Cómo se realizan consultas básicas?
```java
// Automáticas por nombre de método
List<CompanyDocument> findByNombre(String nombre);

// Personalizadas con @Query
@Query("{'nombre': ?0}")
List<CompanyDocument> findByCustomName(String nombre);
```

## ¿Cómo maneja el ORM las transacciones?
```java
@Transactional
public void operacionTransaccional() {
    // Múltiples operaciones en una transacción
}
```

## ¿El ORM implementa Unit of Work internamente?
**Sí**, Spring Data MongoDB implementa Unit of Work a través de:
- **MongoTemplate**: Maneja la sesión y el contexto
- **@Transactional**: Agrupa operaciones en una transacción
- **Session Management**: Controla automáticamente las sesiones de MongoDB