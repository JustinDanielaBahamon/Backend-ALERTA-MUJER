# Resumen del Trabajo - AlertaMujer Backend

**Fecha:** 30 de septiembre de 2026
**Proyecto:** AlertaMujer - Sistema de alerta de emergencia para mujeres
**Programa:** ADSO - Ficha 3145556

---

## 1. Lo que hicimos hoy

### Análisis del proyecto (Paso 0)
- Se analizó el `db.json` del frontend web (35 colecciones)
- Se identificaron los endpoints que consume el móvil (Expo/React Native) y el web (Angular)
- Se encontraron los componentes vacíos: `evidence-management`, `devices-management`, `forgot-password`
- Se identificó que el proyecto usaba `json-server` y `loadMockData()` como simulación

### Arquitectura definida
- **Microservicios** con Spring Boot + Java + PostgreSQL 16
- **4 servicios** + API Gateway
- **Arquitectura hexagonal** (puertos y adaptadores)
- **JWT** para autenticación + **bcrypt** para contraseñas

### Implementación
- Se creó el esquema de base de datos con 4 schemas: `identity`, `alert`, `resource`, `admin`
- Se implementó el `identity-service` completo (register, login, forgot-password, reset-password, users/me)
- Se creó el `gateway` con Spring Boot
- Se crearon los servicios base: `alert-service`, `resource-service`, `admin-service`
- Se conectó el móvil al gateway con interceptor de JWT

---

## 2. Cómo estamos trabajando

### Repositorios
| Repositorio | URL | Rama |
|---|---|---|
| **Backend** | https://github.com/JustinDanielaBahamon/Backend-ALERTA-MUJER.git | `develop` |
| **Móvil** | https://github.com/JustinDanielaBahamon/AlertaMujer.git | `feat/mobile-backend-integration` |
| **Web** | https://github.com/JustinDanielaBahamon/Frond-end-Web.git | `develop` |

### Flujo de trabajo
1. Se trabaja en local
2. Se verifican los errores
3. Se hacen commits con mensajes convencionales (`feat:`, `fix:`, `chore:`, `docs:`)
4. Se sube a GitHub
5. Se prueba con `docker compose up --build`

### Commits realizados
| Commit | Descripción |
|---|---|
| `90c3eee` | feat: add identity service base structure with Spring Boot |
| `94429a0` | feat: add identity service domain models |
| `15b39a3` | feat: add identity service repositories |
| `e05da07` | feat: add identity service REST controllers |
| `809d773` | feat: add identity service application services |
| `ed36d71` | feat: add API gateway with Spring Boot |
| `e6ce46a` | feat: update docker-compose for Spring Boot services |
| `14c36eb` | feat: update database schema with SRS table mapping |
| `46c9055` | feat: align database schema with team design |
| `fe1e047` | feat: add Dockerfiles for all services |
| `b9b443e` | fix: reorder table creation to respect foreign key dependencies |
| `76ca445` | fix: remove duplicate default value from system_configuration id column |
| `d0fb93a` | fix: correct document_number NULL values and JWT_SECRET length |
| `697b3dc` | fix: update Maven image to 3.9.9 for better TLS support |
| `1815177` | fix: correct AuthService and AuthController compilation errors |
| `b2cb34d` | fix: align User model and services with database schema |
| `9bfdf14` | feat: add JWT authentication filter and security config |
| `42b9521` | fix: remove @Valid annotation from Map parameters |
| `47548a0` | fix: use correct jjwt 0.12 API for token parsing |

---

## 3. Cómo funciona

### Arquitectura
```
AlertaMujer/
├── docker-compose.yml          # Orquestación de servicios
├── db/
│   └── init.sql                # Esquema + Seed (PostgreSQL 16)
├── gateway/                    # API Gateway (8080)
│   └── src/main/java/com/alertamujer/gateway/
│       └── GatewayApplication.java
└── services/
    ├── am-identity-service/    # 3001 - Autenticación y cuentas
    │   └── src/main/java/com/alertamujer/identity/
    │       ├── IdentityServiceApplication.java
    │       ├── domain/model/ (User, Account, Role)
    │       ├── application/service/ (AuthService, UserService)
    │       └── infrastructure/
    │           ├── controller/ (AuthController, UserController)
    │           ├── repository/ (UserRepository, AccountRepository, RoleRepository)
    │           └── config/ (JwtAuthenticationFilter, SecurityConfig)
    ├── am-alert-service/       # 3002 - Gestión de alertas
    ├── am-resource-service/    # 3003 - Zonas y recursos
    └── am-admin-service/       # 3004 - Administración y moderación
```

### Flujo de autenticación
1. El usuario hace login con email y contraseña
2. El `identity-service` valida las credenciales con bcrypt
3. Genera un token JWT con el userId y el rol
4. El cliente (móvil/web) guarda el token
5. En cada petición, el cliente envía `Authorization: Bearer <token>`
6. El `JwtAuthenticationFilter` valida el token y extrae el userId
7. El servicio procesa la petición con el userId

### Flujo de alertas
1. El usuario activa el botón de pánico
2. El `alert-service` crea una alerta con estado `active`
3. Se notifica a los contactos de emergencia
4. Se registra la ubicación en `location_log`
5. Se pueden agregar evidencias (foto, video, audio)
6. El admin puede ver y gestionar las alertas

---

## 4. Qué buscamos

### Objetivo principal
- **Eliminar** `json-server` y `loadMockData()`
- **Implementar** un backend real con base de datos
- **Conectar** el móvil y el web al backend
- **Implementar** los componentes vacíos (reset-password, zone-management, report-management, evidence-management, devices-management)

### Requisitos del evaluador
- Backend real con base de datos PostgreSQL
- Eliminar simuladores (json-server, loadMockData)
- Implementar componentes vacíos
- Arquitectura de microservicios
- JWT para autenticación
- bcrypt para contraseñas
- CORS configurable
- Errores consistentes: `{ "error": "mensaje", "code": "CODIGO" }`

---

## 5. Qué agregué

### Base de datos (`db/init.sql`)
- 4 schemas: `identity`, `alert`, `resource`, `admin`
- 25+ tablas con relaciones (FK)
- Seed data desde `db.json` (9 usuarios, 10 alertas, 6 zonas, 8 recursos de emergencia)
- Orden correcto de creación de tablas para respetar FK

### Identity Service
- **Modelos**: User, Account, Role
- **Repositorios**: UserRepository, AccountRepository, RoleRepository
- **Servicios**: AuthService (register, login, forgotPassword, resetPassword), UserService (getMe, updateMe)
- **Controladores**: AuthController, UserController
- **Configuración**: JwtAuthenticationFilter, SecurityConfig

### Gateway
- Spring Boot con WebFlux
- Endpoint `/health` para verificar estado

### Docker
- Dockerfiles para todos los servicios (Maven 3.9.9 + Java 17)
- docker-compose.yml con PostgreSQL 16 + 4 servicios + gateway

### Móvil (Frontend-movil)
- `api.ts` actualizado para apuntar al gateway (8080)
- `auth.service.ts` con endpoints reales
- `alerts.service.ts` y `contactos.service.ts` actualizados
- Interceptor de JWT con AsyncStorage

---

## 6. Pendiente

### Backend
- [ ] Implementar `alert-service` completo (CRUD de alertas, notificaciones, evidencias)
- [ ] Implementar `resource-service` completo (CRUD de zonas, recursos)
- [ ] Implementar `admin-service` completo (CRUD de reportes, evidencias, dispositivos, moderación)
- [ ] Implementar el gateway con enrutamiento a los servicios
- [ ] Agregar Swagger/OpenAPI
- [ ] Agregar tests

### Móvil
- [ ] Actualizar `environment.ts` para apuntar al gateway
- [ ] Implementar `forgot-password` con lógica real
- [ ] Implementar `evidence-management` y `devices-management` con HTTP

### Web
- [ ] Actualizar `environment.ts` para apuntar al gateway
- [ ] Reemplazar `loadMockData()` por servicios HTTP
- [ ] Implementar `forgot-password` con lógica real
- [ ] Implementar `evidence-management` y `devices-management` con HTTP

### Documentación
- [ ] README.md completo con instrucciones
- [ ] Colección de Postman
- [ ] Documentación de la API

---

## 7. Comandos útiles

### Levantar el backend
```bash
cd C:\Users\JUSTINDANIELA\Music\Musica\backend\AlertaMujer
docker compose up --build
```

### Detener el backend
```bash
docker compose down -v
```

### Ver logs
```bash
docker logs alerta-mujer-identity
docker logs alerta-mujer-db
```

### Probar el gateway
```bash
curl http://localhost:8080/health
```

### Probar login
```bash
curl -X POST http://localhost:8080/api/identity/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"maria.perez@test.com\",\"password\":\"Alerta@123\"}"
```

---

## 8. Integrantes

- Maicol Stiven Sanchez
- Jose Sebastian Suarez
- Justin Daniela Bahamon

---

## 9. Notas importantes

- El `JWT_SECRET` debe tener al menos 256 bits (32 caracteres)
- Las contraseñas se almacenan con bcrypt, nunca en texto plano
- Los servicios se comunican por HTTP, no por base de datos
- El gateway es el único punto de entrada para los clientes
- Cada servicio tiene sus propias tablas (propiedad lógica)
- No hay joins entre servicios
