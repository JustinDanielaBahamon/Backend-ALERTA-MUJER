# AlertaMujer — Backend

Backend de AlertaMujer: **3 microservicios** en Java 17 y Spring Boot 3.2, con arquitectura
hexagonal y una base de datos PostgreSQL 16 compartida. Atiende a la app móvil (Expo) y a la web
(Angular). El diseño completo está en [ARQUITECTURA_MICROSERVICIOS.md](ARQUITECTURA_MICROSERVICIOS.md).

| Servicio | Puerto local | Responsabilidad |
|---|---|---|
| Auth Service | 8081 | Registro, login (JWT y Google), sesiones, celulares, configuración y panel de administración |
| Alert Service | 8082 | Alertas, ubicación, chat, contactos, avisos (correo y push), zonas y recursos de ayuda |
| Evidence Service | 8083 | Fotos, audio y video en S3 (Backblaze B2), huella SHA-256 y retención |

## Estructura

```
microservicios/        proyecto Maven: shared-kernel + auth-service, alert-service, evidence-service
docker/                configuración del S3 local (SeaweedFS)
docs/database/         modelo entidad-relación, diccionario de datos y esquema SQL
ci/jenkins/            Jenkins local para el pipeline (Jenkinsfile)
scripts/               despertar los servicios de producción antes de una demostración
docker-compose.yml     PostgreSQL, S3 local y los 3 servicios
render.yaml            despliegue en Render (3 servicios + base de datos)
```

Las migraciones de la base las aplica solo Auth Service con Liquibase
(`microservicios/auth-service/src/main/resources/db/changelog`).

## Ejecutar en local

1. Crear `.env` a partir de `.env.example` con al menos `APP_DB_PASSWORD` y `JWT_SECRET`
   (aleatorio, 32 caracteres o más).
2. Para el login con Google y las notificaciones push, poner la cuenta de servicio de Firebase en
   `src/main/resources/firebase-service-account.json` (nunca se sube al repositorio).
3. Levantar todo:

   ```bash
   docker compose up -d --build
   ```

   Cada servicio publica su documentación en `/swagger-ui/index.html` y su estado en
   `/actuator/health`.

## Pruebas

```bash
cd microservicios && mvn test
```

## Despliegue

- **Render** (plan gratis) con `render.yaml`. Antes de sincronizar el blueprint, el grupo de
  variables `alerta-mujer-microservicios` debe tener `APP_DB_PASSWORD` y `JWT_SECRET`; Alert
  necesita las claves de correo y Evidence las de Backblaze B2. Firebase se sube como *Secret File*
  en Auth y Alert.
- **Jenkins** (`Jenkinsfile`): pruebas, una imagen Docker por servicio y, en `main`, despliegue en
  Render con el *deploy hook* de cada servicio.
