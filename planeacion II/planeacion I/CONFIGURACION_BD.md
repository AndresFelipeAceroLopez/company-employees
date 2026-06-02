# Configuración de Base de Datos

## Información de Conexión

### Host
**MongoDB Atlas Cloud**: `java.nmycqbt.mongodb.net`

### Puerto
**27017** (puerto por defecto de MongoDB, implícito en la URI)

### Nombre de base de datos
**company_employees_db**

### Usuario
**andres**

### Contraseña
**ZWuqqMSVmQZeXZ5K** (configurada en application.properties)

### Cadena de conexión
```properties
spring.data.mongodb.uri=mongodb+srv://andres:ZWuqqMSVmQZeXZ5K@java.nmycqbt.mongodb.net/?appName=java
spring.data.mongodb.database=company_employees_db
```

### Archivo de configuración usado
**application.properties** ubicado en `src/main/resources/`

## Configuración adicional

### Logging de MongoDB
```properties
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```

### Configuración de aplicación
```properties
spring.application.name=company-employees
server.port=8080
```

## Tipo de Base de Datos
**MongoDB Atlas (Cloud)** - No se usa SQLite ya que estamos trabajando con una base de datos NoSQL en la nube.