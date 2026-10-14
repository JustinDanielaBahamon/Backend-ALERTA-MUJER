# Scripts SQL históricos (NO ejecutar)

Estos scripts se guardan solo como documentación del diseño original. **Nunca llegaron a aplicarse**
a la base de datos (el esquema lo crea Liquibase y no los incluía) y **no deben ejecutarse**:
se contradicen entre sí y algunos romperían la aplicación.

Su contenido, revisado y corregido, vive ahora en los changesets de Liquibase
`backend-monolith/src/main/resources/db/changelog/changesets/v1.1.0/`, que se aplican solos al
arrancar el backend.

| Script original | Qué pasó | Dónde quedó |
|---|---|---|
| `phase1_improvements.sql` — índices | Ampliado: se indexan las 25 llaves foráneas; índices compuestos y parciales | `01-indexes.sql` |
| `phase1_improvements.sql` — CHECK | Corregido: el regex de teléfono rechazaba datos reales (`+57 311 000 0001`) | `02-check-constraints.sql` |
| `phase1_improvements.sql` — `updated_at` | Corregido: columna con `DEFAULT now()` y trigger en todas las tablas | `03-updated-at.sql` |
| `phase2_improvements.sql` — ENUM | Sustituido por CHECK: las entidades JPA usan `String` y Hibernate falla con tipos ENUM | `02-check-constraints.sql` |
| `phase2_improvements.sql` — pgcrypto | Pendiente: cifrar `email` rompe el login; debe hacerse en la aplicación (AES-GCM + índice ciego) | — |
| `phase3_improvements.sql` — particiones | Pendiente: creaba tablas paralelas que nadie usaba y `create_monthly_partition()` tenía errores | — |
| `phase3_improvements.sql` — pg_cron | Pendiente: la imagen `postgres:16` no incluye pg_cron | — |
| `phase3_improvements.sql` — vistas materializadas | Sustituida por el índice parcial `idx_alert_active` (siempre al día, sin copiar datos personales) | `01-indexes.sql` |
| `defense-in-depth.sql` — roles | Corregido: contraseña por variable de entorno (`APP_DB_PASSWORD`), sin `DROP ROLE` | `05-app-role.sql` |
| `defense-in-depth.sql` — auditoría | Corregido: el trigger de `alert` hacía fallar todo UPDATE de alertas | `04-audit.sql` |
| `defense-in-depth.sql` — CHECK | Unificado con la fase 1 (`zone_type`, `alert_type` y `status` se contradecían) | `02-check-constraints.sql` |
| `defense-in-depth.sql` — RLS | Pendiente: la app no fija el usuario en la sesión, así que no vería sus propios datos | — |
