#!/bin/bash

# Script to start evidence-service manually
# Port: 8083

echo "🚀 Iniciando EVIDENCE-SERVICE en puerto 8083..."
echo "======================================"
echo ""

cd "$(dirname "$0")/evidence-service"

# Check that the JAR exists
if [ ! -f "target/app.jar" ]; then
    echo "❌ Error: No se encontró el JAR compilado."
    echo "Ejecuta primero: mvn clean package -DskipTests"
    exit 1
fi

# Required environment variables
export SERVER_PORT=8083
export SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/alerta_mujer}
export SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-postgres}
export SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-postgres}
export JWT_SECRET=${JWT_SECRET:-tu-secreto-jwt-muy-largo-y-seguro-para-desarrollo-local}
export INTERNAL_API_KEY=${INTERNAL_API_KEY:-dev-internal-key-12345}
export ALERT_SERVICE_URL=${ALERT_SERVICE_URL:-http://localhost:8082}
export B2_KEY_ID=${B2_KEY_ID:-tu-b2-key-id}
export B2_APP_KEY=${B2_APP_KEY:-tu-b2-app-key}
export B2_BUCKET_NAME=${B2_BUCKET_NAME:-alerta-mujer-evidencias}

echo "📊 Configuración:"
echo "   Puerto: $SERVER_PORT"
echo "   Base de datos: $SPRING_DATASOURCE_URL"
echo "   Alert Service: $ALERT_SERVICE_URL"
echo ""
echo "🔧 Para detener: Ctrl+C"
echo ""

# Start the service
java -jar target/app.jar
