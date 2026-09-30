# AlertaMujer - Backend

Sistema backend para la aplicación AlertaMujer, compuesta por aplicación móvil (React Native/Expo), aplicación web (Angular), backend en Java/Spring Boot, y PostgreSQL como base de datos.

## 1. Descripción

AlertaMujer es un sistema de seguridad personal que permite a las usuarias activar alertas de emergencia, gestionar contactos de emergencia, registrar evidencias, administrar dispositivos, gestionar zonas de riesgo, acceder a recursos de emergencia, y generar reportes. El sistema incluye funcionalidades de autenticación, administración y moderación.

## 2. Arquitectura

### Arquitectura Original (Diseñada)

El proyecto originalmente contempla una arquitectura de microservicios con los siguientes servicios:

- **identity-service**: Autenticación, usuarios, cuentas, perfiles
- **alert-service**: Alertas, contactos de emergencia, evidencias, ubicaciones, notificaciones
- **resource-service**: Zonas, reportes de zonas, recursos de emergencia, llamadas a recursos
- **admin-service**: Administración, auditoría, reportes de usuarios, moderación
- **gateway**: API Gateway como punto de entrada único

### Arquitectura Actual (Implementación Temporal)

Para la presente revisión académica, se ha consolidado la implementación en un **backend monolítico modular**. Los dominios continúan separados conceptualmente dentro del código:

```
backend-monolith/
├── src/main/java/com/alertamujer/
│   ├── identity/          # Identidad, autenticación, usuarios
│   ├── alerts/            # Alertas, contactos de emergencia, evidencias
│   ├── devices/           # Dispositivos
│   ├── zones/             # Zonas y reportes de zonas
│   ├── resources/         # Recursos de emergencia
│   ├── admin/             # Administración, auditoría
│   └── infrastructure/    # Configuración compartida
├── src/main/resources/
│   └── application.properties
├── Dockerfile
└── pom.xml
```

## 3. Justificación de la Decisión Temporal

Debido al plazo establecido para la presente revisión académica, se decidió priorizar la disponibilidad de una implementación funcional y verificable sobre la separación física inmediata de todos los microservicios.

Esta decisión permite:

1. Tener un backend funcional rápidamente
2. Conectarlo con PostgreSQL
3. Exponer APIs reales
4. Permitir que Angular y React Native consuman el backend
5. Eliminar progresivamente los datos mock
6. Demostrar funcionalidad real durante la revisión
7. Mantener una estructura suficientemente organizada para posterior separación

**IMPORTANTE:** Esta decisión es temporal. No representa un abandono de la arquitectura de microservicios originalmente diseñada.

## 4. Evolución Futura

Una vez finalizada la revisión académica y superada la fecha límite, el desarrollo continuará normalmente y se realizará progresivamente la separación de los módulos en los microservicios definidos originalmente:

- identity-service
- alert-service
- resource-service
- admin-service
- gateway

La estructura modular actual del monolito facilitará esta migración futura.

## 5. Tecnologías

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: Persistencia con Hibernate
- **Spring Security**: Autenticación y autorización
- **PostgreSQL**: 16 (base de datos)
- **JWT**: JJWT 0.12.3 (tokens de autenticación)
- **BCrypt**: Hashing de contraseñas
- **Docker**: Contenerización
- **Docker Compose**: Orquestación
- **Maven**: Gestión de dependencias
- **Angular**: Frontend web
- **React Native/Expo**: Frontend móvil

## 6. Estructura del Proyecto

```
Backend-ALERTA-MUJER/
├── backend-monolith/          # Backend monolítico (puerto 8080)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/alertamujer/
│   │       │   ├── AlertaMujerBackendApplication.java
│   │       │   ├── identity/          # Módulo identidad
│   │       │   │   ├── domain/model/  # User, Account, Role, UserProfile
│   │       │   │   ├── application/service/  # AuthService, UserService
│   │       │   │   ├── infrastructure/controller/  # AuthController, UserController
│   │       │   │   ├── infrastructure/repository/  # UserRepository, AccountRepository
│   │       │   │   └── infrastructure/config/  # SecurityConfig, JwtAuthenticationFilter
│   │       │   ├── alerts/            # Módulo alertas
│   │       │   │   ├── domain/model/  # Alert, EmergencyContact, Evidence, LocationLog, Notification
│   │       │   │   ├── application/service/
│   │       │   │   ├── infrastructure/controller/
│   │       │   │   └── infrastructure/repository/
│   │       │   ├── devices/           # Módulo dispositivos
│   │       │   ├── zones/             # Módulo zonas
│   │       │   ├── resources/         # Módulo recursos
│   │       │   ├── admin/             # Módulo administración
│   │       │   └── infrastructure/    # HealthController
│   │       └── resources/
│   │           └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── db/
│   └── init.sql                    # Esquema completo + seed data
├── docker-compose.yml              # Orquestación Docker
├── .env.example                    # Plantilla variables entorno
└── README.md
```

## 7. Base de Datos

**Motor**: PostgreSQL 16

**Nombre de la base de datos**: alerta_mujer

**Conexión**:
- URL (Docker): `jdbc:postgresql://postgres:5432/alerta_mujer`
- URL (Local): `jdbc:postgresql://localhost:5432/alerta_mujer`

**Migraciones**: No se utiliza Liquibase ni Flyway. Las tablas se crean mediante el script SQL manual `db/init.sql`.

**Esquemas**:
- `identity`: 9 tablas (role, users, account, user_profile, admin_profile, recovery_request, device, device_permission, device_session)
- `alert`: 7 tablas (emergency_contact, alert, alert_contact, location_log, notification, evidence, frequent_location, alert_reminder)
- `resource`: 3 tablas (zone, zone_report, emergency_resource, resource_call)
- `admin`: 4 tablas (audit_log, user_report, moderation_action, system_configuration)

**Persistencia**: Los datos de PostgreSQL se persisten en un volumen Docker (`postgres_data`), por lo que no se pierden al reiniciar contenedores.

## 8. Endpoints Implementados

### Autenticación (Públicos)
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Login de usuario (retorna JWT)
- `POST /api/auth/forgot-password` - Solicitar recuperación contraseña (mock)
- `POST /api/auth/reset-password` - Restablecer contraseña (mock)

### Usuarios (Requieren JWT)
- `GET /api/users/me` - Obtener usuario actual
- `PUT /api/users/me` - Actualizar usuario actual

### Alertas (Requieren JWT)
- `GET /api/alerts` - Listar todas las alertas
- `GET /api/alerts/{id}` - Obtener alerta por ID
- `GET /api/alerts/user/{userProfileId}` - Listar alertas de un usuario
- `POST /api/alerts` - Crear alerta
- `PUT /api/alerts/{id}` - Actualizar alerta
- `DELETE /api/alerts/{id}` - Eliminar alerta

### Contactos de Emergencia (Requieren JWT)
- `GET /api/contacts` - Listar todos los contactos
- `GET /api/contacts/{id}` - Obtener contacto por ID
- `GET /api/contacts/user/{userProfileId}` - Listar contactos de un usuario
- `POST /api/contacts` - Crear contacto
- `PUT /api/contacts/{id}` - Actualizar contacto
- `DELETE /api/contacts/{id}` - Eliminar contacto

### Evidencias (Requieren JWT)
- `GET /api/evidences` - Listar todas las evidencias
- `GET /api/evidences/{id}` - Obtener evidencia por ID
- `POST /api/evidences` - Crear evidencia
- `PUT /api/evidences/{id}` - Actualizar evidencia
- `DELETE /api/evidences/{id}` - Eliminar evidencia

### Dispositivos (Requieren JWT)
- `GET /api/devices` - Listar todos los dispositivos
- `GET /api/devices/{id}` - Obtener dispositivo por ID
- `POST /api/devices` - Crear dispositivo
- `PUT /api/devices/{id}` - Actualizar dispositivo
- `DELETE /api/devices/{id}` - Eliminar dispositivo

### Zonas (Públicos)
- `GET /api/zones` - Listar todas las zonas
- `GET /api/zones/{id}` - Obtener zona por ID
- `GET /api/zones/city/{city}` - Filtrar zonas por ciudad
- `GET /api/zones/type/{type}` - Filtrar zonas por tipo
- `POST /api/zones` - Crear zona (admin)
- `PUT /api/zones/{id}` - Actualizar zona (admin)
- `DELETE /api/zones/{id}` - Eliminar zona (admin)

### Reportes de Zonas (Requieren JWT)
- `GET /api/zone-reports` - Listar todos los reportes
- `GET /api/zone-reports/{id}` - Obtener reporte por ID
- `GET /api/zone-reports/zone/{zoneId}` - Reportes por zona
- `GET /api/zone-reports/my/{userProfileId}` - Mis reportes
- `GET /api/zone-reports/status/{status}` - Filtrar por estado
- `POST /api/zone-reports` - Crear reporte
- `PUT /api/zone-reports/{id}/approve` - Aprobar reporte (admin)
- `PUT /api/zone-reports/{id}/reject` - Rechazar reporte (admin)
- `DELETE /api/zone-reports/{id}` - Eliminar reporte

### Recursos de Emergencia (Públicos)
- `GET /api/resources` - Listar todos los recursos
- `GET /api/resources/{id}` - Obtener recurso por ID
- `GET /api/resources/city/{city}` - Filtrar por ciudad
- `GET /api/resources/type/{type}` - Filtrar por tipo
- `POST /api/resources` - Crear recurso (admin)
- `PUT /api/resources/{id}` - Actualizar recurso (admin)
- `DELETE /api/resources/{id}` - Eliminar recurso (admin)

### Llamadas a Recursos (Requieren JWT)
- `GET /api/resource-calls` - Listar todas las llamadas
- `GET /api/resource-calls/{id}` - Obtener llamada por ID
- `GET /api/resource-calls/user/{userProfileId}` - Llamadas de un usuario
- `GET /api/resource-calls/resource/{emergencyResourceId}` - Llamadas a un recurso
- `GET /api/resource-calls/alert/{alertId}` - Llamadas de una alerta
- `POST /api/resource-calls` - Registrar llamada
- `PUT /api/resource-calls/{id}` - Actualizar llamada
- `DELETE /api/resource-calls/{id}` - Eliminar llamada

### Administración (Requieren JWT)
- `GET /api/admin/users` - Listar todos los usuarios
- `GET /api/admin/alerts` - Listar todas las alertas
- `GET /api/admin/contacts` - Listar todos los contactos
- `GET /api/admin/evidences` - Listar todas las evidencias
- `GET /api/admin/devices` - Listar todos los dispositivos
- `GET /api/admin/zones` - Listar todas las zonas
- `GET /api/admin/reports` - Listar todos los reportes de zonas
- `GET /api/admin/audit-logs` - Listar logs de auditoría
- `POST /api/admin/audit-logs` - Crear log de auditoría
- `GET /api/admin/user-reports` - Listar reportes de usuarios
- `GET /api/admin/user-reports/pending` - Reportes pendientes
- `PUT /api/admin/user-reports/{id}` - Actualizar estado de reporte

### Health Check (Público)
- `GET /health` - Verificar estado del backend

## 9. Ejecución Local

### Requisitos Previos
- Docker y Docker Compose instalados
- (Opcional) Java 17 y Maven 3.9+ para ejecución local sin Docker

### Con Docker (Recomendado)

1. Copiar archivo de entorno:
   ```bash
   cp .env.example .env
   ```

2. Levantar todos los servicios:
   ```bash
   docker compose up --build
   ```

3. El backend estará disponible en: `http://localhost:8080`
4. PostgreSQL estará disponible en: `localhost:5432`

### Sin Docker (Alternativa)

1. Instalar y configurar PostgreSQL 16 localmente
2. Crear base de datos `alerta_mujer`
3. Ejecutar script `db/init.sql` en la base de datos
4. Configurar variables de entorno o modificar `application.properties`
5. Compilar y ejecutar con Maven:
   ```bash
   cd backend-monolith
   mvn spring-boot:run
   ```

## 10. Pruebas

### Health Check
```bash
curl http://localhost:8080/health
```
Respuesta esperada:
```json
{
  "status": "ok",
  "service": "alerta-mujer-backend",
  "type": "monolithic"
}
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.perez@test.com","password":"password"}'
```
Nota: La contraseña en seed data está hasheada con BCrypt. Para pruebas, use el registro primero.

### Registro
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Usuario Test","email":"test@test.com","password":"Test123456","telefono":"3001234567"}'
```

### Obtener Usuario Actual (requiere JWT)
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

### Listar Zonas (público)
```bash
curl http://localhost:8080/api/zones
```

### Listar Recursos (público)
```bash
curl http://localhost:8080/api/resources
```

## 11. Estado del Proyecto

### Implementado y Funcional
- ✅ Backend monolítico modular con Spring Boot
- ✅ Autenticación con JWT (login, registro)
- ✅ Gestión de usuarios
- ✅ Configuración de seguridad con Spring Security
- ✅ CORS configurado
- ✅ Entidades JPA para todos los dominios
- ✅ Repositorios Spring Data JPA
- ✅ Servicios de negocio
- ✅ Controladores REST
- ✅ PostgreSQL con esquema completo
- ✅ Seed data con usuarios de prueba
- ✅ Docker Compose
- ✅ Health endpoint

### Endpoints CRUD Implementados
- ✅ Identity: auth, users
- ✅ Alerts: alertas, contactos de emergencia, evidencias
- ✅ Devices: dispositivos
- ✅ Zones: zonas, reportes de zonas
- ✅ Resources: recursos de emergencia, llamadas a recursos
- ✅ Admin: endpoints administrativos

### En Desarrollo / Pendiente
- ⏳ Integración completa con Angular (reemplazar mocks)
- ⏳ Integración completa con React Native (reemplazar mocks)
- ⏳ Implementación real de recuperación de contraseña
- ⏳ Implementación de refresh tokens
- ⏳ Implementación de logout
- ⏳ Almacenamiento real de archivos para evidencias (S3, Cloudinary, etc.)
- ⏳ Validaciones más robustas en controllers
- ⏳ Manejo global de excepciones
- ⏳ Tests unitarios y de integración
- ⏳ DTOs para separar entidades de respuestas API

### Evolución Posterior
Después de la revisión académica:
- Separar módulos en microservicios físicos
- Implementar API Gateway con routing real
- Implementar servicio de mensajería para alertas
- Implementar almacenamiento de archivos en la nube
- Agregar métricas y monitoreo
- Implementar rate limiting
- Agregar documentación con Swagger/OpenAPI

## 12. Variables de Entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DATABASE_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/alerta_mujer` |
| `POSTGRES_USER` | Usuario de PostgreSQL | `alerta_mujer` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | `alerta_mujer_2026` |
| `POSTGRES_DB` | Nombre de la base de datos | `alerta_mujer` |
| `JWT_SECRET` | Secreto para firmar JWT | `alerta_mujer_jwt_secret_key_2026_xK9mP2qR5sT8uV1wX4yZ7` |
| `JWT_EXPIRES_IN` | Expiración del token (ms) | `86400000` (24 horas) |
| `CORS_ORIGIN` | Orígenes permitidos para CORS | `*` |

## 13. Integrantes

- Maicol Stiven Sanchez
- Jose Sebastian Suarez
- Justin Daniela Bahamon
