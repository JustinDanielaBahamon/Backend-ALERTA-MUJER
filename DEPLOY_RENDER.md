# Despliegue del backend en Render (plan gratis)

El archivo `render.yaml` crea dos recursos:

- **alerta-mujer-db**: PostgreSQL 16.
- **alerta-mujer-backend**: el backend, construido con `backend-monolith/Dockerfile`.

## 1. Crear los servicios

1. Entra a <https://dashboard.render.com> con la cuenta de GitHub que tiene acceso al repositorio.
2. **New → Blueprint** y elige el repositorio `Backend-ALERTA-MUJER`.
3. En **Branch** elige una rama que tenga `render.yaml` (hoy `feature/audit-cleanup-google-signin`; después de unirla, `qa`).
4. Render lee `render.yaml` y muestra los dos recursos. Pulsa **Apply**.

Render crea la base, genera las contraseñas (`APP_DB_PASSWORD`, `JWT_SECRET`) y empieza el primer despliegue.

## 2. Subir el archivo de Firebase (obligatorio)

El backend no arranca sin la cuenta de servicio de Firebase.

1. Abre **alerta-mujer-backend → Environment → Secret Files → Add Secret File**.
2. **Filename**: `firebase-service-account.json`.
3. **Contents**: pega el contenido del archivo `firebase-service-account.json` que usas en local.
4. Guarda y pulsa **Manual Deploy → Deploy latest commit**.

Render lo deja en `/etc/secrets/firebase-service-account.json`, la ruta que ya apunta `FIREBASE_CONFIG_PATH`.
No subas ese archivo al repositorio.

## 3. Comprobar

Cuando el despliegue termine, Render muestra la URL del servicio, por ejemplo
`https://alerta-mujer-backend.onrender.com`. Abre:

```
https://<tu-url>.onrender.com/actuator/health
```

Debe responder `{"status":"UP"}`.

## 4. Apuntar las apps a la URL

- **Móvil**: en `Frontend-movil/eas.json`, perfil `preview`, cambia `EXPO_PUBLIC_API_URL` por la URL de Render y genera el APK con `eas build -p android --profile preview`.
- **Web**: cambia `apiUrl` en `src/environments/environment.prod.ts`. Cuando la web tenga su dominio (Vercel), pon ese dominio en `CORS_ORIGIN` en lugar de `*`.

## 5. Crear la primera cuenta de administrador

En Render la base arranca **sin cuentas de prueba** (`LIQUIBASE_CONTEXTS=prod`): solo con los roles y la configuración base. Para tener un administrador:

1. Regístrate normalmente desde la app o la web.
2. En **alerta-mujer-db → Connect** copia el comando `PSQL Command` y ejecútalo en tu computador.
3. Ejecuta (con tu correo):

```sql
UPDATE identity.users SET role_id = 2 WHERE email = 'tu-correo@ejemplo.com';
```

## Límites del plan gratis

| Límite | Efecto |
|---|---|
| El servicio se duerme tras 15 min sin peticiones | La primera petición después tarda ~1 minuto |
| La base gratis vence a los 30 días | Render avisa por correo; hay que crear otra o pasar a un plan de pago |
| Disco temporal | Las evidencias subidas se borran en cada despliegue o reinicio |
| SMTP bloqueado | No se puede enviar correo por SMTP; usar un proveedor con API HTTP (por ejemplo Brevo) |

## Variables de entorno

| Variable | Origen |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | La base de Render |
| `LIQUIBASE_USER`, `LIQUIBASE_PASSWORD` | Usuario dueño de la base (migraciones) |
| `POSTGRES_USER` = `alerta_app` | Usuario del backend, solo datos |
| `APP_DB_PASSWORD` | Generada por Render |
| `POSTGRES_PASSWORD` = `${APP_DB_PASSWORD}` | Spring la resuelve con la anterior |
| `JWT_SECRET` | Generada por Render |
| `PORT` | La pone Render; el backend la lee (`server.port=${PORT:8080}`) |
