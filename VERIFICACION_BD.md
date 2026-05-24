# Cómo Verificar si se Creó la Base de Datos en MongoDB

## 🚀 **1. Ejecutar la Aplicación**

### Opción A: Con Maven Wrapper (Recomendado)
```bash
# En la raíz del proyecto
./mvnw spring-boot:run
```

### Opción B: Con Java directamente
```bash
# Compilar primero
./mvnw clean package -DskipTests

# Ejecutar el JAR
java -jar target/company-employees-1.0.0.jar
```

### Opción C: Desde tu IDE
- Ejecutar la clase `CompanyEmployeesApplication.java`

## 📊 **2. Verificar en MongoDB Atlas (Web)**

### Pasos:
1. Ve a [MongoDB Atlas](https://cloud.mongodb.com/)
2. Inicia sesión con tu cuenta
3. Selecciona tu cluster "java"
4. Haz clic en "Browse Collections"
5. Deberías ver:
   - **Base de datos**: `company_employees_db`
   - **Colecciones**: `companies` y `employees` (se crean al insertar datos)

## 🔍 **3. Verificar mediante Logs de la Aplicación**

### Busca estos mensajes en la consola:
```
=== Company Employees API iniciada ===
=== API: http://localhost:8080/api/companias ===
```

### Para ver conexión a MongoDB:
```
# Agregar esta línea a application.properties para más detalle
logging.level.org.springframework.data.mongodb=DEBUG
```

## 🧪 **4. Probar los Endpoints**

### Crear una compañía (POST):
```bash
curl -X POST http://localhost:8080/api/companias \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Tech Solutions",
    "direccion": "Calle 123",
    "telefono": "3001234567"
  }'
```

### Listar compañías (GET):
```bash
curl http://localhost:8080/api/companias
```

## 🛠 **5. Usar MongoDB Compass (Cliente Desktop)**

### Descargar e instalar:
1. Descarga [MongoDB Compass](https://www.mongodb.com/products/compass)
2. Conecta usando la URI:
   ```
   mongodb+srv://andres:ZWuqqMSVmQZeXZ5K@java.nmycqbt.mongodb.net/
   ```
3. Navega a la base de datos `company_employees_db`

## 📱 **6. Usar MongoDB Shell (mongosh)**

### Si tienes mongosh instalado:
```bash
# Conectar
mongosh "mongodb+srv://andres:ZWuqqMSVmQZeXZ5K@java.nmycqbt.mongodb.net/"

# Usar la base de datos
use company_employees_db

# Listar colecciones
show collections

# Ver documentos
db.companies.find()
db.employees.find()
```

## ✅ **Señales de que Todo Funciona**

### 1. Aplicación inicia sin errores
### 2. Logs muestran conexión exitosa a MongoDB
### 3. Endpoints responden correctamente
### 4. Se pueden crear y consultar datos
### 5. En MongoDB Atlas aparece la base de datos

## ❌ **Posibles Problemas**

### Error de conexión:
- Verificar que la IP esté en whitelist de MongoDB Atlas
- Verificar credenciales en application.properties
- Verificar conectividad a internet

### Error de Java:
- Verificar versión de Java (debe ser 21+)
- Verificar que el puerto 8080 esté libre