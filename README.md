# AlertaMujer - Backend

Sistema de microservicios para la aplicación AlertaMujer (Web y Móvil).

## Arquitectura

```
AlertaMujer/
├── docker-compose.yml      # Orquestación de servicios
├── .env.example            # Variables de entorno
├── db/
│   └── init.sql            # Esquema + Seed (PostgreSQL 16)
├── gateway/                # API Gateway (puerto 8080)
├── shared/                 # Middleware JWT, errores, DB, validaciones
└── services/
    ├── am-identity-service/   # 3001 - Autenticación y cuentas
    ├── am-alert-service/      # 3002 - Gestión de alertas
    ├── am-resource-service/   # 3003 - Zonas y recursos
    └── am-admin-service/      # 3004 - Administración y moderación
```

## Puertos

| Servicio | Puerto |
|---|---|
| API Gateway | 8080 |
| Identity Service | 3001 |
| Alert Service | 3002 |
| Resource Service | 3003 |
| Admin Service | 3004 |
| PostgreSQL | 5432 |

## Rutas del Gateway

| Prefijo | Servicio |
|---|---|
| `/api/identity/*` | Identity Service (3001) |
| `/api/alerts/*` | Alert Service (3002) |
| `/api/resources/*` | Resource Service (3003) |
| `/api/admin/*` | Admin Service (3004) |

## Cómo levantar todo

1. Copiar `.env.example` como `.env`:
   ```bash
   cp .env.example .env
   ```

2. Levantar todos los servicios:
   ```bash
   docker compose up --build
   ```

3. La base de datos se inicializa automáticamente con el seed desde `db/init.sql`.

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `POSTGRES_USER` | Usuario de PostgreSQL | `alerta_mujer` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | `alerta_mujer_2026` |
| `POSTGRES_DB` | Nombre de la base de datos | `alerta_mujer` |
| `JWT_SECRET` | Secreto para firmar JWT | `alerta_mujer_jwt_secret_2026` |
| `JWT_EXPIRES_IN` | Expiración del token | `24h` |
| `CORS_ORIGIN` | Orígenes permitidos para CORS | `*` |

## Integrantes

- Maicol Stiven Sanchez
- Jose Sebastian Suarez
- Justin Daniela Bahamon
