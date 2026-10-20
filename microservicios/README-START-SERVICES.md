# 🚀 Guía para Iniciar los Microservicios Localmente

## Pre-requisitos

1. ✅ Java 17 instalado
2. ✅ PostgreSQL corriendo en localhost:5432
3. ✅ Base de datos `alerta_mujer` creada
4. ✅ Maven instalado (o usar mvnw)

## 📋 Verificar Base de Datos

Asegúrate de que PostgreSQL esté corriendo:

```bash
sudo systemctl status postgresql
# o
pg_isready
```

Si necesitas iniciar PostgreSQL:
```bash
sudo systemctl start postgresql
```

## 🚀 OPCIÓN 1: Iniciar Todos los Servicios Automáticamente

El script `start-all-services.sh` abre 3 terminales automáticamente:

```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios
./start-all-services.sh
```

Esto abrirá 3 terminales:
- **Terminal 1**: Auth Service (puerto 8081)
- **Terminal 2**: Alert Service (puerto 8082)
- **Terminal 3**: Evidence Service (puerto 8083)

## 🚀 OPCIÓN 2: Iniciar Servicios Manualmente (Terminal por Terminal)

### Terminal 1 - Auth Service
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios
./start-auth-service.sh
```

### Terminal 2 - Alert Service
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios
./start-alert-service.sh
```

### Terminal 3 - Evidence Service
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios
./start-evidence-service.sh
```

## 🚀 OPCIÓN 3: Usar Maven directamente

### Terminal 1
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios/auth-service
mvn spring-boot:run
```

### Terminal 2
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios/alert-service
mvn spring-boot:run
```

### Terminal 3
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios/evidence-service
mvn spring-boot:run
```

## 🚀 OPCIÓN 4: Ejecutar JARs directamente

Si ya compilaste con `mvn clean package`:

### Terminal 1 - Auth Service
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios/auth-service
java -jar target/app.jar
```

### Terminal 2 - Alert Service
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios/alert-service
java -jar target/app.jar
```

### Terminal 3 - Evidence Service
```bash
cd /home/maicol/Documentos/proyecto\ /Backend-ALERTA-MUJER/microservicios/evidence-service
java -jar target/app.jar
```

## ⚙️ Variables de Entorno

Los scripts ya incluyen valores por defecto para desarrollo local. Si necesitas cambiarlos:

```bash
# Base de datos
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/alerta_mujer
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=tu_password

# Seguridad
export JWT_SECRET=tu-secreto-jwt-muy-largo-y-seguro-para-desarrollo-local
export INTERNAL_API_KEY=dev-internal-key-12345

# URLs de otros servicios (para comunicación entre microservicios)
export AUTH_SERVICE_URL=http://localhost:8081
export ALERT_SERVICE_URL=http://localhost:8082
export EVIDENCE_SERVICE_URL=http://localhost:8083
```

## 📊 Verificar que los Servicios Están Corriendo

### Health Check Endpoints
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

Deberías ver:
```json
{"status":"UP"}
```

### Swagger UI (Documentación de API)
Abre en tu navegador:
- Auth Service: http://localhost:8081/swagger-ui.html
- Alert Service: http://localhost:8082/swagger-ui.html
- Evidence Service: http://localhost:8083/swagger-ui.html

## 🛑 Detener los Servicios

- **Si usaste scripts**: Cierra cada terminal o presiona `Ctrl+C` en cada una
- **Si usaste Maven**: Presiona `Ctrl+C` en cada terminal
- **Si ejecutaste JARs**: Presiona `Ctrl+C` en cada terminal

## 🔍 Ver Logs

Los logs aparecen directamente en cada terminal. Para guardar logs en archivos:

```bash
# Iniciar con logs a archivo
java -jar target/app.jar > logs/auth-service.log 2>&1 &

# Ver logs en tiempo real
tail -f logs/auth-service.log
```

## 🐛 Troubleshooting

### Error: "Address already in use"
Un puerto ya está ocupado. Verifica qué proceso está usando el puerto:
```bash
sudo lsof -i :8081
sudo lsof -i :8082
sudo lsof -i :8083
```

Mata el proceso si es necesario:
```bash
kill -9 <PID>
```

### Error: "Connection refused to PostgreSQL"
PostgreSQL no está corriendo o no está en el puerto 5432:
```bash
sudo systemctl start postgresql
```

### Error: "Unable to connect to database"
Verifica las credenciales de la base de datos en `application.properties` o las variables de entorno.

### Error: Liquibase no puede aplicar migraciones
Asegúrate de que la base de datos `alerta_mujer` existe:
```bash
psql -U postgres -c "CREATE DATABASE alerta_mujer;"
```

## 📖 Documentación Adicional

- **Arquitectura**: Ver `.agents/tasks/VERIFICACION_ARQUITECTURA_HEXAGONAL.md`
- **API Contracts**: Ver `shared-kernel/src/main/java/com/alertamujer/*/api/`
- **Análisis del proyecto**: Ver archivos `ANALISIS_*.md` en la raíz

## 🎯 Orden de Inicio Recomendado

Para desarrollo local, este es el orden ideal:

1. **Primero**: Auth Service (8081) - Otros servicios pueden necesitar autenticación
2. **Segundo**: Evidence Service (8083) - Alert Service puede crear evidencias
3. **Tercero**: Alert Service (8082) - Depende de los otros dos

Aunque los servicios tienen retry y pueden iniciarse en cualquier orden, este orden minimiza errores en los logs durante el inicio.

## 🎓 Para la Demostración de Tesis

Usa **OPCIÓN 1** (start-all-services.sh) para iniciar todo de golpe y tener las 3 terminales visibles mostrando los logs en tiempo real. Esto demuestra:
- Arquitectura de microservicios
- Comunicación entre servicios
- Logs distribuidos
- Alta disponibilidad

---

**Creado:** 20 de octubre de 2026  
**Proyecto:** AlertaMujer - Sistema de Alertas de Emergencia  
**Arquitectura:** Microservicios con Arquitectura Hexagonal
