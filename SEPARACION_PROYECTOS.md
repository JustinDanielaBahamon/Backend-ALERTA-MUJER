# Instrucciones para separar los proyectos

Este documento explica cómo configurar cada proyecto cuando se separen en repositorios independientes.

## 📁 Estructura de carpetas final

Después de separar, tendrás 3 repositorios independientes:

```
alerta-mujer-backend/          # Backend Spring Boot
alerta-mujer-web/              # Frontend Web Angular
alerta-mujer-mobile/           # Frontend Móvil React Native/Expo
```

---

## 🔧 Configuración por proyecto

### 1. Backend (Spring Boot)

**Ubicación actual:** `Backend-ALERTA-MUJER/backend-monolith/`

**No requiere cambios adicionales** - el backend ya es independiente.

**Variables de entorno (opcional):**
- `DATABASE_URL`: URL de base de datos PostgreSQL
- `POSTGRES_USER`: Usuario de PostgreSQL
- `POSTGRES_PASSWORD`: Contraseña de PostgreSQL
- `JWT_SECRET`: Secret para firmar tokens JWT
- `JWT_EXPIRES_IN`: Expiración del token (ms)
- `CORS_ORIGIN`: Orígenes permitidos para CORS

**Puerto por defecto:** 8080

---

### 2. Frontend Web (Angular)

**Ubicación actual:** `Frond-end-Web/alerta-mujer-web/`

**Configuración de la API del backend:**

**Opción A: Usar variable de entorno (recomendado para producción)**
```bash
# Crear archivo .env en la raíz del proyecto
NG_APP_API_URL=http://localhost:8080
```

**Opción B: Modificar el archivo de entorno directamente**
```typescript
// src/environments/environment.ts
export const environment = {
    production: false,
    apiUrl: 'http://localhost:8080'  // Cambiar a la URL de tu backend
  };
```

**Para producción:**
```typescript
// src/environments/environment.prod.ts
export const environment = {
    production: true,
    apiUrl: 'https://tu-backend-api.com'  // URL de producción
  };
```

**Instalación y ejecución:**
```bash
npm install
ng serve
```

---

### 3. Frontend Móvil (React Native/Expo)

**Ubicación actual:** `AlertaMujer/Frontend-movil/`

**Configuración de la API del backend:**

**Opción A: Usar variable de entorno (recomendado)**
```bash
# Crear archivo .env en la raíz del proyecto
EXPO_PUBLIC_API_URL=http://localhost:8080
```

**Opción B: Modificar el archivo api.ts directamente**
```typescript
// src/services/api.ts
// La función getApiBaseUrl() detecta automáticamente:
// - Emulador Android: http://10.0.2.2:8080
// - Dispositivo físico: http://TU_IP:8080
// - Web: http://localhost:8080
```

**Para emulador Android:**
- No requiere cambios, usa `http://10.0.2.2:8080` automáticamente

**Para dispositivo físico:**
- Necesitas conocer la IP de tu PC donde corre el backend
- Usa la variable de entorno:
  ```bash
  EXPO_PUBLIC_API_URL=http://192.168.1.X:8080
  ```

**Instalación y ejecución:**
```bash
npm install
npx expo start
```

---

## 🌐 Escenarios de despliegue

### Desarrollo local (todo en la misma máquina)

**Backend:** `http://localhost:8080`
**Web:** `http://localhost:4200` (ng serve)
**Móvil:** `http://localhost:8080` (usa detección automática)

### Backend en servidor separado

1. **Backend:** Desplegado en `https://api.alertamujer.com`
2. **Web:** Configurar `NG_APP_API_URL=https://api.alertamujer.com`
3. **Móvil:** Configurar `EXPO_PUBLIC_API_URL=https://api.alertamujer.com`

### Backend en Docker

Si usas Docker Compose, los servicios pueden comunicarse por nombres de servicio:

```yaml
# docker-compose.yml
services:
  backend:
    ports:
      - "8080:8080"
  
  web:
    environment:
      - NG_APP_API_URL=http://backend:8080
  
  mobile:
    environment:
      - EXPO_PUBLIC_API_URL=http://backend:8080
```

---

## 🔐 Notas de seguridad

- **Nunca** commitees archivos `.env` con credenciales reales
- Usa `.env.example` como plantilla
- En producción, usa variables de entorno del servidor
- Los tokens JWT se manejan automáticamente en los interceptores

---

## 📞 Soporte

Si tienes problemas de conexión:
1. Verifica que el backend esté corriendo
2. Verifica que el puerto sea correcto (8080 por defecto)
3. Verifica CORS en el backend (configurado para permitir todos los orígenes en desarrollo)
4. Para móvil físico, verifica que el dispositivo y el backend estén en la misma red
