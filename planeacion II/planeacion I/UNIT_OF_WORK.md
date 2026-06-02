# Unit of Work - Implementación y Análisis

## ✅ Implementación Realizada

### Ubicación
`src/main/java/com/companyemployees/infrastructure/persistence/mongo/MongoUnitOfWork.java`

### Interfaz del Dominio
`src/main/java/com/companyemployees/application/ports/transaction/UnitOfWork.java`

## ✅ ¿Qué hace Unit of Work?

- ✅ **Coordina varios repositorios**: Permite usar CompanyRepository y EmployeeRepository en la misma transacción
- ✅ **Controla una única sesión**: Usa TransactionTemplate para manejar la sesión de MongoDB
- ✅ **Agrupa operaciones**: Múltiples operaciones se ejecutan como una unidad atómica
- ✅ **Confirma cambios juntos**: Commit automático al final de la transacción
- ✅ **Revierte cambios**: Rollback automático si ocurre una excepción
- ✅ **Evita transacciones independientes**: Los repositorios no manejan sus propias transacciones

## ✅ Implementación Conceptual

```java
MongoUnitOfWork
 - TransactionTemplate (Spring)
 - execute(Supplier<T> action)
 - execute(Runnable action)
```

## ✅ Flujo Implementado

```
Service Layer
     ↓
MongoUnitOfWork
     ↓
CompanyRepository + EmployeeRepository
     ↓
MongoTemplate (Spring Data)
     ↓
MongoDB Database
```

## ✅ Ejemplo de Uso

```java
// En el servicio
unitOfWork.execute(() -> {
    Company company = companyRepository.save(newCompany);
    employee.setCompaniaId(company.getId());
    employeeRepository.save(employee);
    return company;
});
```

## 📋 Respuestas a Preguntas Obligatorias

### 1. ¿Qué es Unit of Work?
Es un patrón que **mantiene una lista de objetos afectados por una transacción de negocio y coordina la escritura de cambios y la resolución de problemas de concurrencia**.

### 2. ¿Qué problema resuelve?
- **Atomicidad**: Garantiza que todas las operaciones se completen o ninguna
- **Consistencia**: Mantiene la integridad de los datos
- **Control de transacciones**: Evita que cada repositorio maneje su propia transacción
- **Performance**: Reduce el número de round-trips a la base de datos

### 3. ¿Qué relación tiene con Repository Pattern?
- Los **repositorios** se encargan de las operaciones CRUD individuales
- **Unit of Work** coordina múltiples repositorios en una sola transacción
- Los repositorios **NO** confirman cambios directamente cuando se usa Unit of Work

### 4. ¿El ORM seleccionado ya implementa Unit of Work internamente?
**Sí**, Spring Data MongoDB implementa Unit of Work a través de:
- **TransactionTemplate**: Maneja las transacciones
- **MongoTemplate**: Actúa como la sesión/contexto
- **@Transactional**: Anotación declarativa para transacciones

### 5. ¿Qué objeto representa la unidad de trabajo?
**TransactionTemplate** en Spring - equivalente a:
- DbContext en Entity Framework
- Session en SQLAlchemy  
- EntityManager en JPA/Hibernate

### 6. ¿Dónde se ubica Unit of Work dentro de Onion Architecture?
- **Interfaz**: En `application/ports/transaction/` (capa de aplicación)
- **Implementación**: En `infrastructure/persistence/mongo/` (capa de infraestructura)

### 7. ¿Los repositorios llaman directamente a Save, Commit o Flush?
**No**, los repositorios solo realizan operaciones. El **Unit of Work** (TransactionTemplate) maneja automáticamente el commit/rollback.

### 8. ¿Cómo se revierte una operación cuando ocurre un error?
```java
unitOfWork.execute(() -> {
    // Si cualquier operación lanza excepción,
    // TransactionTemplate hace rollback automáticamente
    companyRepository.save(company);
    employeeRepository.save(employee); // Si falla aquí, se revierte todo
});
```

### 9. ¿Cómo se garantiza que varias operaciones se guarden como una sola unidad?
A través de **TransactionTemplate** que:
- Inicia una transacción MongoDB
- Ejecuta todas las operaciones dentro de esa transacción
- Hace commit si todo es exitoso
- Hace rollback si hay algún error

### 10. ¿Qué ventajas tiene usar Unit of Work en una API empresarial?
- **Integridad de datos**: Operaciones complejas son atómicas
- **Mejor performance**: Menos round-trips a la base de datos
- **Manejo de errores**: Rollback automático en caso de fallas
- **Separación de responsabilidades**: Los servicios se enfocan en lógica de negocio
- **Testabilidad**: Fácil de mockear para pruebas unitarias