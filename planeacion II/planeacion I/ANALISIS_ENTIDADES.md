# Análisis de Entidades

## Entidad Company ✅
**Ubicación**: `src/main/java/com/companyemployees/domain/company/Company.java`

### Características implementadas:
- ✅ **Llave primaria**: CompanyId id
- ✅ **Campos obligatorios**: nombre, direccion, telefono
- ✅ **Fecha de creación**: LocalDateTime fechaCreacion
- ✅ **Contador de empleados**: int employeeCount
- ✅ **Reglas de negocio**: increaseEmployeeCount(), decreaseEmployeeCount()

## Entidad Employee ✅
**Ubicación**: `src/main/java/com/companyemployees/domain/employee/Employee.java`

### Características implementadas:
- ✅ **Llave primaria**: EmployeeId id
- ✅ **Campos obligatorios**: nombre, apellido, correo, cargo, salario
- ✅ **Relación**: CompanyId companiaId (referencia a Company)
- ✅ **Estado**: EmployeeStatus status
- ✅ **Reglas de negocio**: changeEmail() con validación

## Relación Uno a Muchos ✅
```
Company (1) ──── (N) Employee
```
- Una compañía puede tener muchos empleados
- Cada empleado pertenece a una sola compañía
- Implementado mediante CompanyId en Employee

## Documentos MongoDB ✅
**CompanyDocument**: `@Document(collection = "companies")`
**EmployeeDocument**: `@Document(collection = "employees")`

### Características:
- ✅ **Índices**: @Indexed(unique = true) en correo del empleado
- ✅ **Mapeo**: Separación clara entre dominio e infraestructura
- ✅ **Nombres apropiados**: Colecciones "companies" y "employees"