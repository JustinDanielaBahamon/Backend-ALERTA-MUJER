# Eliminación de Datos Quemados (Mock Data)

## Fecha
2026-09-30

## Objetivo
Eliminar todos los datos quemados (mock data) de los frontends y JSON Server, manteniendo únicamente los datos necesarios para iniciar sesión.

---

## Cambios Realizados

### 1. Frontend Web (Angular)

#### Servicios Modificados

**1.1 moderator.service.ts**
- ❌ **Eliminado:** Datos mock de reportes (7 reportes con datos falsos)
- ✅ **Resultado:** Array `reportsSubject` inicializado vacío
- ✅ **Estado:** Servicio listo para conectarse al backend (`/api/reports`)

**1.2 device.service.ts**
- ❌ **Eliminado:** Datos mock de dispositivos (3 dispositivos con datos falsos)
- ✅ **Resultado:** Array `devicesMock` eliminado, `devicesSubject` inicializado vacío
- ✅ **Estado:** Servicio listo para conectarse al backend (`/api/dispositivos`)

**1.3 evidence.service.ts**
- ❌ **Eliminado:** Datos mock de evidencias (6 evidencias con datos falsos)
- ✅ **Resultado:** Array `evidenciasMock` eliminado, `evidenciasSubject` inicializado vacío
- ✅ **Estado:** Servicio listo para conectarse al backend (`/api/evidencias`)

**1.4 reports.service.ts**
- ❌ **Eliminado:** Datos mock de reportes (8 reportes con datos falsos)
- ✅ **Resultado:** Array `reportesMock` eliminado, `reportesSubject` inicializado vacío
- ✅ **Estado:** Servicio listo para conectarse al backend (`/api/reportes`)

#### Verificación de Componentes Admin

**zone-management.ts**
- ✅ Ya usa `ZonesService` sin datos mock
- ✅ Lógica implementada correctamente

**report-management.ts**
- ✅ Ya usa `ReportsService` sin datos mock
- ✅ Lógica implementada correctamente

**reset-password.html**
- ℹ️ No encontrado en el proyecto actual

---

### 2. Frontend Móvil (React Native/Expo)

#### Archivos Modificados

**2.1 src/features/inicio/view/index.tsx**
- ❌ **Eliminado:** Array `mockAlerts` con 5 alertas falsas
- ✅ **Resultado:** Estado `alertas` inicializado vacío
- ✅ **Lógica:** Se carga desde el backend vía `getAlertasByUsuario()`
- ✅ **Fallback:** Muestra mensaje "sin alertas" si no hay datos

**2.2 src/features/historial/view/historial.tsx**
- ❌ **Eliminado:** Referencias a `mockAlerts` en el código
- ✅ **Resultado:** Usa únicamente datos de la API
- ✅ **Lógica:** Filtrado y búsqueda funcionan con datos reales del backend

---

### 3. JSON Server (db.json)

#### Datos Mantenidos (Solo para Login)

**Usuarios:**
- `maria.perez@test.com` / `password` (Usuaria)
- `ana.gomez@test.com` / `password` (Usuaria)
- `admin@test.com` / `password` (Admin)

**Tablas Mantenidas (con datos mínimos):**
- `usuarios`: 3 usuarios para login
- `role`: 2 roles (user, administrator)
- `account`: 3 accounts correspondientes
- `user_profile`: 3 perfiles correspondientes
- `system_configuration`: Configuración básica del sistema

#### Datos Eliminados (Arrays Vacíos)

- ❌ `alertas`: Array vacío (antes tenía 10 alertas)
- ❌ `emergency_contact`: Array vacío (antes tenía contactos)
- ❌ `alert_contact`: Array vacío
- ❌ `notification`: Array vacío
- ❌ `device`: Array vacío (antes tenía 8 dispositivos)
- ❌ `device_permission`: Array vacío
- ❌ `frequent_location`: Array vacío
- ❌ `alert_reminder`: Array vacío
- ❌ `alert_activation_setting`: Array vacío
- ❌ `zone_report`: Array vacío
- ❌ `user_preference`: Array vacío
- ❌ `audit_log`: Array vacío
- ❌ `user_report`: Array vacío
- ❌ `moderation_action`: Array vacío
- ❌ `device_session`: Array vacío
- ❌ `admin_perfil`: Array vacío
- ❌ `recovery_request`: Array vacío
- ❌ `resource_call`: Array vacío
- ❌ `frequentPlaces`: Array vacío (antes tenía 3 lugares)
- ❌ `nearbyZones`: Array vacío (antes tenía 3 zonas)
- ❌ `contactos`: Array vacío (antes tenía 4 contactos)
- ❌ `lineas-ayuda`: Array vacío (antes tenía 4 líneas)
- ❌ `centros-ayuda`: Array vacío (antes tenía 4 centros)
- ❌ `recursos-guardados`: Array vacío (antes tenía 2 recursos)

---

## Estado Actual

### Backend (Spring Boot)
- ✅ Corriendo en puerto 8080
- ✅ Base de datos PostgreSQL con datos reales
- ✅ Endpoint `/api/auth/login` funcionando
- ✅ Usuarios de prueba disponibles en base de datos

### Frontend Web
- ✅ Conectado al backend (puerto 8080)
- ✅ Sin datos mock en servicios administrativos
- ✅ Listo para recibir datos reales del backend

### Frontend Móvil
- ✅ Conectado al backend (puerto 8080)
- ✅ Sin datos mock en pantalla de inicio e historial
- ✅ Listo para recibir datos reales del backend

### JSON Server
- ✅ Solo datos mínimos para login
- ✅ Todos los demás arrays vacíos
- ⚠️ **Nota:** JSON Server ya NO se usa como fuente de datos principal. Los frontends están conectados al backend Spring Boot.

---

## Próximos Pasos

### Conexión Completa al Backend

Los servicios administrativos necesitan conectarse al backend Spring Boot:

1. **Moderator Service:**
   - Conectar a `/api/reports` (o endpoint correspondiente)
   - Implementar CRUD real

2. **Device Service:**
   - Conectar a `/api/devices` (o endpoint correspondiente)
   - Implementar CRUD real

3. **Evidence Service:**
   - Conectar a `/api/evidences` (o endpoint correspondiente)
   - Implementar CRUD real

4. **Reports Service:**
   - Conectar a `/api/reports` (o endpoint correspondiente)
   - Implementar CRUD real

5. **Alerts Service (móvil):**
   - Ya conectado a `/api/alerts`
   - Verificar que retorne datos reales

### Usuarios de Prueba

**Para login en ambos frontends:**
- `maria.perez@test.com` / `password`
- `ana.gomez@test.com` / `password`
- `admin@test.com` / `password`

**Nota:** La contraseña en la base de datos PostgreSQL es `password` (sin hash BCrypt en modo desarrollo).

---

## Resumen

✅ **Eliminados:** Todos los datos mock de servicios administrativos y pantallas de usuario
✅ **Mantenidos:** Solo 3 usuarios para pruebas de login
✅ **Conectados:** Frontends conectados al backend Spring Boot
✅ **Listos:** Servicios preparados para recibir datos reales del backend

---

## Archivos Modificados

### Frontend Web
- `src/app/core/services/moderator.service.ts`
- `src/app/core/services/device.service.ts`
- `src/app/core/services/evidence.service.ts`
- `src/app/core/services/reports.service.ts`

### Frontend Móvil
- `src/features/inicio/view/index.tsx`
- `src/features/historial/view/historial.tsx`

### JSON Server
- `db.json` (limpiado completamente)

---

## Verificación

Para verificar que no quedan datos mock:

```bash
# Buscar "mock" en servicios web
grep -r "mock" Frond-end-Web/alerta-mujer-web/src/app/core/services/

# Buscar "mockAlerts" en móvil
grep -r "mockAlerts" AlertaMujer/Frontend-movil/src/

# Verificar db.json
cat Frond-end-Web/alerta-mujer-web/db.json | grep -c "empty"
```

Resultado esperado: 0 coincidencias de datos mock.
