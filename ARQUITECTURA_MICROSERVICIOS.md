# Arquitectura de microservicios — AlertaMujer

El backend de AlertaMujer está dividido en **3 microservicios** con **arquitectura hexagonal**,
desplegados en Render y compartiendo **una sola base de datos PostgreSQL**.

## Servicios desplegados

| Servicio | URL | Responsabilidad | Módulos |
|---|---|---|---|
| **Auth Service** | https://alerta-mujer-auth.onrender.com | Seguridad: registro, login (JWT y Google), sesiones, celulares, configuración y panel de administración | `identity`, `devices`, `settings`, `admin` |
| **Alert Service** | https://alerta-mujer-alert.onrender.com | Núcleo SOS: alertas, ubicación en tiempo real, chat, contactos, notificaciones (correo y push), zonas y recursos de ayuda | `alerts`, `notifications`, `zones`, `resources` |
| **Evidence Service** | https://alerta-mujer-evidence.onrender.com | Diferenciador: fotos, videos y audios en Backblaze B2, huella SHA-256, retención de 30 días con aviso 5 días antes | `evidence` |

Cada servicio tiene su documentación interactiva en `/swagger-ui/index.html` y su estado en
`/actuator/health`.

## Estructura del código

```
microservicios/
├── pom.xml              proyecto Maven padre
├── shared-kernel/       seguridad JWT, errores comunes, contratos (*Api) y cliente HTTP entre servicios
├── auth-service/        aplica las migraciones Liquibase de la base compartida
├── alert-service/
└── evidence-service/
```

### Arquitectura hexagonal

Los **9 módulos** de negocio siguen el mismo patrón:

| Servicio | Módulo | Dominio (reglas sin Spring ni JPA) |
|---|---|---|
| Auth | `identity` | registro, estados de la cuenta, contraseñas, código de recuperación |
| Auth | `devices` | celular principal, permisos de la app, token push, estados |
| Auth | `settings` | duración de la alerta, número de emergencia, límite de contactos, versión mínima |
| Auth | `admin` | moderación, eliminación con dos administradoras, informes, auditoría |
| Alert | `alerts` | una alerta activa por usuaria, estados, chat, contactos, ubicaciones, recorridos |
| Alert | `notifications` | enlace de seguimiento (vigencia y hash), límite del chat del contacto |
| Alert | `zones` | zonas del mapa y reportes de las usuarias |
| Alert | `resources` | recursos de ayuda y registro de llamadas |
| Evidence | `evidence` | tipos de archivo, nombres seguros, revisión |

Estructura de cada módulo (ejemplo: Evidence Service):

```
evidence/
├── domain/model/            Evidence: modelo puro (sin Spring ni JPA) con sus reglas
├── application/
│   ├── port/in/             casos de uso: SubirEvidencia, ConsultarEvidencias, ModificarEvidencia,
│   │                        AdministrarEvidencias, LimpiarEvidencias
│   ├── port/out/            EvidenciaRepositorio, AlmacenamientoEvidencias, AlertasPort, IdentidadPort
│   └── service/             implementación de los casos de uso (solo usa puertos)
└── adapter/
    ├── in/web/              controlador REST
    ├── in/scheduler/        limpieza nocturna
    ├── in/http/             endpoints internos para Alert Service
    ├── out/persistence/     JPA (entidad + repositorio + conversión)
    ├── out/storage/         disco o S3 (Backblaze B2)
    └── out/alerts, out/identity   clientes de los otros microservicios
```

`alerts` (el núcleo de Alert Service) sigue el mismo patrón:

```
alerts/
├── domain/model/            Alert, AlertContact, AlertMessage, EmergencyContact, LocationLog,
│                            SavedRoute, FrequentLocation: modelos puros con sus reglas (una alerta
│                            activa por usuaria, máximo 5 conservadas, 4 recorridos y 2 estrella...)
├── application/
│   ├── port/in/             GestionarAlertas, AdministrarAlertas, ChatAlerta, DestinatariosAlerta,
│   │                        ExpirarAlertas, ContactosEmergencia, Ubicaciones, PedirUbicacion,
│   │                        LimpiarUbicaciones, Recorridos, LugaresFrecuentes, ConsultarAlertas
│   ├── port/out/            repositorios, IdentidadPort, CelularesPort, AjustesPort, PushPort,
│   │                        EventosAlertaPort
│   └── service/             implementación de los casos de uso
└── adapter/
    ├── in/web/              controladores REST con DTOs (el JSON no cambió)
    ├── in/scheduler/        expiración de alertas y limpieza de ubicaciones
    ├── in/modulo/           AlertsApi para notificaciones, Auth y Evidence
    ├── out/persistence/     entidades JPA + repositorios + conversión
    ├── out/identity, out/devices, out/settings   Auth Service por HTTP
    ├── out/push/            Firebase
    └── out/eventos/         eventos de Spring hacia notificaciones
```

`notifications` (avisos de Alert Service):

```
notifications/
├── domain/model/            TrackingLink (código de 256 bits, solo el hash, vigencia de 2 h tras
│                            terminar) y LimiteMensajesContacto (1 mensaje cada 2 s, 30 cada 10 min)
├── application/
│   ├── port/in/             AvisarContactos, AvisarNuevoCelular, EnviarCodigoRecuperacion,
│   │                        SeguimientoPublico, AlertasRecibidas
│   ├── port/out/            EnlaceRepositorio, CorreosPort, PushPort, AlertasPort, IdentidadPort,
│   │                        DispositivosPort, AjustesPort, EvidenciasPort
│   └── service/             implementación de los casos de uso
└── adapter/
    ├── in/eventos/          escucha los eventos (en otro hilo, después de guardar)
    ├── in/web/              seguimiento público y alertas recibidas
    ├── out/email/           plantillas de la marca + Brevo, Resend o solo registro
    ├── out/persistence/     enlaces de seguimiento
    └── out/alerts, identity, devices, settings, evidence, push
```

`identity` (el núcleo de Auth Service) también:

```
identity/
├── domain/model/            User, Account, UserProfile, RecoveryRequest, DeviceSession: reglas de
│                            registro, estados de la cuenta, política de contraseñas, código de
│                            recuperación (15 minutos, 5 intentos, solo el hash)
├── application/
│   ├── port/in/             RegistrarUsuaria, IniciarSesion, MiCuenta, RecuperarContrasena,
│   │                        AdministrarCuentas, ConsultarIdentidad
│   ├── port/out/            repositorios, CifradoPort (BCrypt), TokensPort (JWT),
│   │                        VerificadorGooglePort, EventosIdentidadPort
│   └── service/             implementación de los casos de uso
└── adapter/
    ├── in/web/              AuthController, UserController y límite de intentos por IP
    ├── in/modulo/           IdentityApi e IdentityAdminApi
    ├── out/persistence/     entidades JPA + repositorios + conversión
    ├── out/seguridad/       BCrypt y JWT
    ├── out/google/          verificación del token de Google o Firebase
    └── out/eventos/         auditoría de sesiones y correo de recuperación
```

El dominio no depende de la tecnología. Cambiar la base de datos o el almacenamiento significa
cambiar un adaptador; la lógica de negocio no se toca.

## Comunicación

### Clientes (app móvil y web) → microservicios

Sin API Gateway. Cada cliente envía la petición al servicio dueño según el inicio de la ruta:

| Rutas | Servicio |
|---|---|
| `/api/evidences` | Evidence |
| `/api/alerts`, `/api/contacts`, `/api/location-logs`, `/api/zones`, `/api/resources`, `/api/saved-routes`, `/api/frequent-locations`, `/api/received-alerts`, `/api/zone-reports`, `/api/resource-calls`, `/api/public/tracking` | Alert |
| Todo lo demás (`/api/auth`, `/api/users`, `/api/devices`, `/api/admin`, `/api/settings`) | Auth |

- Web: `src/app/core/api/servicios.ts` y un interceptor HTTP.
- App: `src/services/servicios.ts` en el cliente axios.

### Entre microservicios

1. **Llamadas síncronas (HTTP + JSON).**
   - Cada módulo publica un contrato (`IdentityApi`, `AlertsApi`, `DevicesApi`, `SettingsApi`, `ZonesApi`,
     `EvidenceApi`). Si quien lo usa vive en otro servicio, recibe un cliente HTTP genérico (estilo
     Feign, `RpcClientFactory`) que implementa la misma interfaz y llama a
     `POST /internal/rpc/{Contrato}/{método}` del servicio dueño. El código que usa el contrato no
     cambió al separar los servicios.
   - Los errores viajan con su tipo (404, 403, 400, 409) y se vuelven a lanzar del otro lado.
2. **Eventos.**
   - Dentro de un servicio, los eventos de Spring siguen siendo asíncronos (`@Async` +
     `@TransactionalEventListener`). Ejemplo: Alert publica `AlertActivatedEvent` y notifications
     envía correos y push en otro hilo, solo después de guardar la alerta.
   - Entre servicios (recuperación de contraseña y celular nuevo, de Auth a Alert), el evento se
     reenvía por HTTP a `/internal/events/{Evento}`.
3. **Seguridad interna:** las rutas `/internal/**` exigen la cabecera `X-Internal-Key`
   (`INTERNAL_API_KEY`, la misma en los 3 servicios). Sin ella, responden 403.

### Seguridad de las usuarias

- Auth emite un **JWT** firmado. Alert y Evidence lo validan con la misma clave (`JWT_SECRET`), sin
  llamar a Auth en cada petición.
- Límite de 5 intentos por minuto en login, registro y recuperación de contraseña.
- Cada consulta verifica que el recurso pertenezca a quien lo pide.

## Base de datos compartida

- **PostgreSQL 16** en Render. Cada servicio es dueño de sus esquemas:

| Esquema | Dueño |
|---|---|
| `identity`, `admin` | Auth |
| `alert`, `notification`, `resource` | Alert |
| `alert.evidence` (tabla) | Evidence |

- Un servicio no consulta tablas de otro: le pide los datos por su contrato.
- **Solo Auth Service aplica las migraciones** (Liquibase). Alert y Evidence las tienen
  desactivadas, para que no compitan al desplegar.

## Despliegue (Render, plan gratis)

- `render.yaml` define los 3 servicios (Docker) y el grupo de variables
  `alerta-mujer-microservicios` (`INTERNAL_API_KEY` generada, CORS y memoria).
- Los secretos compartidos (`APP_DB_PASSWORD`, `JWT_SECRET`, `INTERNAL_API_KEY`) viven en el grupo de
  variables `alerta-mujer-microservicios`; los de correo (Alert) y de Backblaze B2 (Evidence), en su servicio.
- Firebase se sube como *Secret File* en Auth (inicio con Google) y Alert (push). Evidence no lo usa.
- Cada servicio se duerme tras unos 15 minutos sin uso y despierta en la primera petición
  (alrededor de un minuto).

## Pruebas

- **291 tests** en los microservicios (`mvn test` en `microservicios/`).
  - Incluyen pruebas del dominio sin Spring ni base de datos, y de los casos de uso con puertos
    simulados.
  - `BeanNamesTest` falla si dos adaptadores de módulos distintos tienen el mismo nombre de clase
    (Spring no arrancaría).
- Al pasar cada módulo a hexagonal se comparó la API antes y después con pruebas de humo en Docker
  (201 pasos en total): mismos códigos HTTP, mismas claves y mismo orden en el JSON.
- Local: `docker compose up -d --build` levanta los 3 servicios con PostgreSQL y SeaweedFS
  (S3), en los puertos 8081 (Auth), 8082 (Alert) y 8083 (Evidence).
- Probado de punta a punta en la nube: registro y login en Auth; contactos y alertas en Alert
  (consulta a Auth); evidencias en Evidence (consulta a Alert y a Auth); y protección de `/internal`.

## Trabajo futuro

- Separar la base de datos por servicio (los esquemas ya están separados).
- Broker de mensajería (RabbitMQ o Kafka) para los eventos entre servicios, y reintentos.
- SMS o RCS como canal de notificación (requiere un proveedor de pago).
