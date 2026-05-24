# Repository Pattern - Implementación

## ✅ Repositorios Implementados

### CompanyRepository (Interfaz del Dominio)
**Ubicación**: `src/main/java/com/companyemployees/application/ports/repository/CompanyRepository.java`

### EmployeeRepository (Interfaz del Dominio)  
**Ubicación**: `src/main/java/com/companyemployees/application/ports/repository/EmployeeRepository.java`

## ✅ Implementaciones Concretas

### MongoCompanyRepository
**Ubicación**: `src/main/java/com/companyemployees/infrastructure/persistence/mongo/repository/MongoCompanyRepository.java`

### MongoEmployeeRepository
**Ubicación**: `src/main/java/com/companyemployees/infrastructure/persistence/mongo/repository/MongoEmployeeRepository.java`

## ✅ Spring Data Repositories

### SpringDataCompanyMongoRepository
```java
@Repository
public interface SpringDataCompanyMongoRepository extends MongoRepository<CompanyDocument, String> {
}
```

### SpringDataEmployeeMongoRepository
```java
@Repository  
public interface SpringDataEmployeeMongoRepository extends MongoRepository<EmployeeDocument, String> {
}
```

## ✅ Métodos Implementados

| Método | Propósito | Estado |
|--------|-----------|--------|
| GetAll | Obtener todos los registros | ✅ Implementado |
| GetById | Obtener un registro por id | ✅ Implementado |
| Create | Crear un nuevo registro | ✅ Implementado |
| Update | Actualizar un registro existente | ✅ Implementado |
| Delete | Eliminar un registro | ✅ Implementado |
| FindByCondition | Buscar por una condición | ✅ Implementado |

## ✅ Separación de Responsabilidades

### Dominio (Interfaces)
- Define contratos sin dependencias externas
- Ubicado en `application/ports/repository/`

### Infraestructura (Implementaciones)
- Implementa las interfaces del dominio
- Maneja la persistencia específica de MongoDB
- Ubicado en `infrastructure/persistence/mongo/`

## ✅ Unit of Work Integration
Los repositorios **NO** confirman cambios directamente. Esto se maneja a través del patrón Unit of Work implementado en `MongoUnitOfWork`.