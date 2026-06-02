# Migraciones - MongoDB

## ¿Por qué no aplican las migraciones tradicionales?

MongoDB es una base de datos **schema-less** (sin esquema fijo), lo que significa:

- ✅ **Creación automática**: Las colecciones se crean automáticamente al insertar el primer documento
- ✅ **Flexibilidad**: Los documentos pueden tener diferentes estructuras
- ✅ **Evolución natural**: Los cambios de esquema se manejan a nivel de aplicación

## Proceso de Creación Automática

### 1. Al ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

### 2. MongoDB creará automáticamente:
- **Base de datos**: `company_employees_db`
- **Colección**: `companies` (cuando se inserte la primera compañía)
- **Colección**: `employees` (cuando se inserte el primer empleado)

### 3. Índices automáticos:
```java
@Indexed(unique = true)
private String correo; // Crea índice único en el campo correo
```

## Evidencia de Creación

### Comando para verificar (MongoDB Shell):
```javascript
// Conectar a MongoDB Atlas
use company_employees_db

// Listar colecciones
show collections

// Ver estructura de companies
db.companies.findOne()

// Ver estructura de employees  
db.employees.findOne()
```

### Tablas/Colecciones generadas:
- ✅ **companies**: Para almacenar compañías
- ✅ **employees**: Para almacenar empleados

### Relación entre Company y Employee:
- Campo `companiaId` en employees hace referencia al `_id` de companies
- No hay foreign keys físicas (es NoSQL), pero se mantiene la integridad referencial a nivel de aplicación