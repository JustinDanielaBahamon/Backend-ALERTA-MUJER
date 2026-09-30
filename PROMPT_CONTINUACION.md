# Prompt de Continuación - AlertaMujer Backend

## Instrucción para el AI

Continúa el trabajo del backend de AlertaMujer. Tienes todo el contexto necesario a continuación. No preguntes cosas que ya están respondidas en este documento. Si algo es ambiguo, pregunta. Trabaja en fases y haz commits pequeños con mensajes convencionales.

---

## Contexto del Proyecto

**Proyecto:** AlertaMujer - Sistema de alerta de emergencia para mujeres
**Programa:** ADSO - Análisis y Desarrollo de Software, Ficha 3145556
**Fecha límite:** 30 de septiembre de 2026, 8:00 a.m.
**Integrantes:** Maicol Stiven Sanchez, Jose Sebastian Suarez, Justin Daniela Bahamon

**Situación:** El evaluador exige backend real con BD, eliminar `json-server` y `loadMockData()`, e implementar componentes vacíos (reset-password, zone-management, report-management, evidence-management, devices-management).

---

## Arquitectura

**Stack:** Java + Spring Boot + PostgreSQL 16 + JWT + bcrypt + Docker

**Microservicios:**
1. **am-identity-service** (3001) - Registro, login, recuperación, perfiles, dispositivos, sesiones, permisos, preferencias
2. **am-alert-service** (3002) - Botón de pánico, ciclo de vida de alertas, notificación multi-canal, rastreo de ubicación, evidencia
3. **am-resource-service** (3003) - Zonas seguras/riesgosas, reportes de zonas, directorio de recursos, registros de llamadas
4. **am-admin-service** (3004) - Gestión de usuarios, moderación, auditoría, reportes del sistema, configuración
5. **gateway** (8080) - API Gateway, único punto de entrada

**Rutas del Gateway:**
- `/api/identity/*` → 3001
- `/api/alerts/*` → 3002
- `/api/resources/*` → 3003
- `/api/admin/*` → 3004

**Principios:**
- P1: Propiedad lógica de tablas — nunca un servicio consulta tablas de otro servicio directamente
- P2: Dominio independiente del framework (arquitectura hexagonal)
- P3: Fail Fast, Recover Gracefully
- P4: Fiabilidad de la ruta de alerta sobre todo lo demás
- P5: Observabilidad

---

## Repositorios

| Repositorio | URL | Rama |
|---|---|---|
| **Backend** | https://github.com/JustinDanielaBahamon/Backend-ALERTA-MUJER.git | `develop` |
| **Móvil** | https://github.com/JustinDanielaBahamon/AlertaMujer.git | `feat/mobile-backend-integration` |
| **Web** | https://github.com/JustinDanielaBahamon/Frond-end-Web.git | `develop` |

---

## Lo que se analizó

### db.json (35 colecciones)
- **identity:** role, users, account, user_profile, admin_profile, recovery_request, device, device_permission, device_session, alert_activation_setting, user_preference
- **alert:** emergency_contact, alert, alert_contact, location_log, notification, evidence, frequent_location, alert_reminder
- **resource:** zone, zone_report, emergency_resource, resource_call
- **admin:** audit_log, user_report, moderation_action, system_configuration

### Endpoints que consume el móvil (Expo/React Native)
- `GET /usuarios` - Login
- `POST /usuarios` - Registro
- `POST /account` - Crear cuenta
- `POST /user_profile` - Crear perfil
- `GET /emergency_contact` - Contactos de emergencia
- `POST /emergency_contact` - Agregar contacto
- `PUT /emergency_contact/:id` - Actualizar contacto
- `DELETE /emergency_contact/:id` - Eliminar contacto
- `GET /alertas?usuarioId=:id` - Historial de alertas

### Endpoints que consume el web (Angular)
- `/auth/login`, `/auth/register`
- `/users/me` (GET, PUT)
- `/alerts` (GET, POST, PUT, PATCH)
- `/zones` (GET, POST, DELETE)
- `/reportes` (GET, DELETE)
- `/evidencias` (GET)
- `/lineas-ayuda`, `/centros-ayuda`, `/recursos-guardados`
- `/frequentPlaces`, `/nearbyZones`, `/ubicaciones`
- `/zonasManuales`, `/puntosMapa`, `/contactos`

### Componentes vacíos identificados
- `evidence-management.ts` - Usa `loadMockData()` con 6 evidencias hardcodeadas
- `devices-management.ts` - Usa `loadMockData()` con 8 dispositivos hardcodeados
- `forgot-password.ts` - Solo valida email y navega, no hace HTTP
- `zone-management.ts` - Ya tiene lógica HTTP (GET/DELETE)
- `report-management.ts` - Ya tiene lógica HTTP (GET/DELETE)

---

## Lo que se implementó

### Base de datos (`db/init.sql`)
- 4 schemas: `identity`, `alert`, `resource`, `admin`
- 25+ tablas con relaciones (FK)
- Orden correcto de creación para respetar FK
- Seed data desde `db.json` (9 usuarios, 10 alertas, 6 zonas, 8 recursos de emergencia)
- `document_number` NULL en lugar de '' (para respetar UNIQUE constraint)

### Identity Service (Spring Boot)
- **Modelos:** User, Account, Role
- **Repositorios:** UserRepository, AccountRepository, RoleRepository
- **Servicios:** AuthService (register, login, forgotPassword, resetPassword), UserService (getMe, updateMe)
- **Controladores:** AuthController, UserController
- **Configuración:** JwtAuthenticationFilter, SecurityConfig
- **JWT:** Generación y validación con clave de 256+ bits

### Gateway (Spring Boot)
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

## Commits realizados

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
| `71d4dca` | fix: use longer JWT_SECRET (256+ bits) in all files |
| `653465e` | chore: remove duplicate files and clean project structure |

---

## Pendiente

### Backend
- [ ] Implementar `alert-service` completo (CRUD de alertas, notificaciones, evidencias, location_log, alert_reminder)
- [ ] Implementar `resource-service` completo (CRUD de zonas, emergency_resource, resource_call, zone_report)
- [ ] Implementar `admin-service` completo (CRUD de reportes, evidencias, dispositivos, moderación, auditoría, system_configuration)
- [ ] Implementar gateway con enrutamiento a los servicios
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

## Comandos útiles

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

## Notas importantes

- El `JWT_SECRET` debe tener al menos 256 bits (32 caracteres)
- Las contraseñas se almacenan con bcrypt, nunca en texto plano
- Los servicios se comunican por HTTP, no por base de datos
- El gateway es el único punto de entrada para los clientes
- Cada servicio tiene sus propias tablas (propiedad lógica)
- No hay joins entre servicios
- Los commits deben ser pequeños con mensajes convencionales (`feat:`, `fix:`, `chore:`, `docs:`)
