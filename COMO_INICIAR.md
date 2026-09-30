# Cómo Iniciar el Proyecto AlertaMujer

## Estructura del Proyecto

El proyecto AlertaMujer consta de 3 componentes principales:

1. **Backend** (Java/Spring Boot) - `Backend-ALERTA-MUJER/`
2. **Frontend Web** (Angular) - `Frond-end-Web/alerta-mujer-web/`
3. **Frontend Móvil** (React Native/Expo) - `AlertaMujer/Frontend-movil/`

---

## 1. Iniciar el Backend (Spring Boot + PostgreSQL)

### Requisitos Previos
- Docker y Docker Compose instalados
- (Opcional) Java 17 y Maven 3.9+ para ejecución sin Docker

### Con Docker (Recomendado)

1. **Navegar al directorio del backend:**
   ```bash
   cd "/home/maicol/Documentos/proyecto /Backend-ALERTA-MUJER"
   ```

2. **Copiar archivo de entorno:**
   ```bash
   cp .env.example .env
   ```

3. **Levantar todos los servicios:**
   ```bash
   docker compose up --build
   ```

4. **Verificar que el backend esté funcionando:**
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

5. **El backend estará disponible en:** `http://localhost:8080`
6. **PostgreSQL estará disponible en:** `localhost:5433` (puerto 5433 para evitar conflicto con PostgreSQL local)

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

---

## 2. Iniciar el Frontend Web (Angular)

### Requisitos Previos
- Node.js 18+ y npm instalados
- Angular CLI instalado globalmente (opcional, viene con el proyecto)

### Pasos

1. **Navegar al directorio del frontend web:**
   ```bash
   cd "/home/maicol/Documentos/proyecto /Frond-end-Web/alerta-mujer-web"
   ```

2. **Instalar dependencias (primera vez):**
   ```bash
   npm install
   ```

3. **Configurar la URL del backend (opcional):**
   
   Por defecto, el frontend usa `http://localhost:8080`. Si necesitas cambiarlo:
   
   - Opción A: Crear archivo `.env`:
     ```bash
     cp .env.example .env
     ```
     Editar `.env` y descomentar la línea correspondiente:
     ```
     NG_APP_API_URL=http://localhost:8080
     ```
   
   - Opción B: El archivo `src/environments/environment.ts` ya está configurado para usar la variable de entorno

4. **Iniciar el servidor de desarrollo:**
   ```bash
   ng serve
   ```

5. ## Usuarios de Prueba

El backend tiene usuarios de prueba preconfigurados en la base de datos:

### Usuarias (role_id: 1)
- **Email:** `mujer@gmail.com`
- **Contraseña:** `Alerta@2024`
- **Estado:** active

- **Email:** `alerta@gmail.com`
- **Contraseña:** `Alerta@2024`
- **Estado:** active

- **Email:** `ana.gomez@test.com`
- **Contraseña:** `Alerta@2024`
- **Estado:** inactive

- **Email:** `laura.torres@test.com`
- **Contraseña:** `Alerta@2024`
- **Estado:** blocked

### Administrador (role_id: 2)
- **Email:** `admin@test.com`
- **Contraseña:** `Alerta@2024`
- **Estado:** active

**Nota:** Las contraseñas están hasheadas con BCrypt en la base de datos.

---

## 3. Iniciar el Frontend Móvil (React Native/Expo)

### Requisitos Previos
- Node.js 18+ y npm instalados
- Expo CLI instalado: `npm install -g expo-cli`
- (Opcional) Android Studio para emulador Android
- (Opcional) Xcode para simulador iOS (solo macOS)
- Expo Go app instalado en dispositivo físico (Android/iOS)

### Pasos

1. **Navegar al directorio del frontend móvil:**
   ```bash
   cd "/home/maicol/Documentos/proyecto /AlertaMujer/Frontend-movil"
   ```

2. **Instalar dependencias (primera vez):**
   ```bash
   npm install
   ```

3. **Configurar la URL del backend:**
   
   Crear archivo `.env`:
   ```bash
   cp .env.example .env
   ```
   
   Editar `.env` según tu entorno:
   
   - **Para desarrollo local (backend en localhost):**
     ```
     EXPO_PUBLIC_API_URL=http://localhost:8080
     ```
   
   - **Para emulador Android:**
     ```
     EXPO_PUBLIC_API_URL=http://10.0.2.2:8080
     ```
   
   - **Para dispositivo físico (reemplaza con tu IP):**
     ```
     EXPO_PUBLIC_API_URL=http://192.168.1.X:8080
     ```

4. **Iniciar Expo:**
   ```bash
   npx expo start
   ```

5. **Abrir la app:**
   
   El comando mostrará opciones:
   - **a** - Abrir en Android emulator
   - **i** - Abrir en iOS simulator (solo macOS)
   - **w** - Abrir en navegador web
   - Escanear código QR con app Expo Go en dispositivo físico

---

## Orden de Inicio Recomendado

1. **Primero:** Iniciar el Backend (Spring Boot + PostgreSQL)
2. **Segundo:** Iniciar el Frontend Web (Angular)
3. **Tercero:** Iniciar el Frontend Móvil (React Native/Expo)

---

## Verificación de Conexión

### Backend Health Check
```bash
curl http://localhost:8080/health
```

### Backend - Login Test
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.perez@test.com","password":"password"}'
```

### Backend - Listar Zonas (público)
```bash
curl http://localhost:8080/api/zones
```

### Backend - Listar Recursos (público)
```bash
curl http://localhost:8080/api/resources
```

---

## Puertos Utilizados

| Servicio | Puerto | URL |
|----------|--------|-----|
| Backend (Spring Boot) | 8080 | http://localhost:8080 |
| Frontend Web (Angular) | 4200 | http://localhost:4200 |
| PostgreSQL | 5432 | localhost:5432 |
| Expo (móvil) | 19000-19002 | http://localhost:19000 |

---

## Solución de Problemas Comunes

### Backend no inicia
- Verificar que Docker esté corriendo: `docker ps`
- Verificar que el puerto 8080 no esté en uso: `lsof -i :8080`
- Revisar logs de Docker: `docker compose logs`

### Frontend Web no conecta al backend
- Verificar que el backend esté corriendo en http://localhost:8080
- Verificar la configuración en `src/environments/environment.ts`
- Verificar CORS en el backend (configurado para `*`)

### Frontend Móvil no conecta al backend
- Para emulador Android: usar `http://10.0.2.2:8080`
- Para dispositivo físico: usar la IP de tu PC (no localhost)
- Verificar que el dispositivo y PC estén en la misma red
- Verificar firewall del PC

---

## Variables de Entorno del Backend

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `POSTGRES_USER` | `alerta_mujer` | Usuario PostgreSQL |
| `POSTGRES_PASSWORD` | `alerta_mujer_2026` | Contraseña PostgreSQL |
| `POSTGRES_DB` | `alerta_mujer` | Nombre base de datos |
| `JWT_SECRET` | (larga cadena) | Secreto para tokens JWT |
| `JWT_EXPIRES_IN` | `24h` | Expiración del token |
| `CORS_ORIGIN` | `*` | Orígenes permitidos CORS |

---

## Endpoints Principales del Backend

### Autenticación (Públicos)
- `POST /api/auth/register` - Registro
- `POST /api/auth/login` - Login

### Usuarios (Requieren JWT)
- `GET /api/users/me` - Obtener usuario actual
- `PUT /api/users/me` - Actualizar usuario

### Alertas (Requieren JWT)
- `GET /api/alerts` - Listar alertas
- `POST /api/alerts` - Crear alerta

### Zonas (Públicos)
- `GET /api/zones` - Listar zonas
- `GET /api/zones/city/{city}` - Filtrar por ciudad

### Recursos (Públicos)
- `GET /api/resources` - Listar recursos
- `GET /api/resources/city/{city}` - Filtrar por ciudad

---

## Documentación Adicional

- **Backend README:** `Backend-ALERTA-MUJER/README.md`
- **Frontend Web README:** `Frond-end-Web/alerta-mujer-web/README.md`
- **Frontend Móvil README:** `AlertaMujer/Frontend-movil/README.md`

---

## Notas Importantes

1. **Datos quemados eliminados:** Todos los servicios del frontend web ahora están conectados al backend Spring Boot usando variables de entorno. No hay datos mock hardcoded.

2. **Base de datos:** PostgreSQL se ejecuta en Docker con persistencia de datos. Los datos no se pierden al reiniciar contenedores.

3. **Arquitectura temporal:** El backend actualmente es un monolito modular. Posteriormente se separará en microservicios.

4. **CORS:** Configurado para permitir todos los orígenes (`*`) en desarrollo. En producción, restringir a dominios específicos.

---

## Integrantes del Proyecto

- Maicol Stiven Sanchez
- Jose Sebastian Suarez
- Justin Daniela Bahamon
