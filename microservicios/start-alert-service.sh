#!/bin/bash

# Script to start alert-service manually
# Port: 8082

echo "🚀 Iniciando ALERT-SERVICE en puerto 8082..."
echo "======================================"
echo ""

cd "$(dirname "$0")/alert-service"

# Check that the JAR exists
if [ ! -f "target/app.jar" ]; then
    echo "❌ Error: No se encontró el JAR compilado."
    echo "Ejecuta primero: mvn clean package -DskipTests"
    exit 1
fi

# Required environment variables
export SERVER_PORT=8082
export SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/alerta_mujer}
export SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-postgres}
export SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-postgres}
export JWT_SECRET=${JWT_SECRET:-tu-secreto-jwt-muy-largo-y-seguro-para-desarrollo-local}
export INTERNAL_API_KEY=${INTERNAL_API_KEY:-dev-internal-key-12345}
export AUTH_SERVICE_URL=${AUTH_SERVICE_URL:-http://localhost:8081}
export EVIDENCE_SERVICE_URL=${EVIDENCE_SERVICE_URL:-http://localhost:8083}

echo "📊 Configuración:"
echo "   Puerto: $SERVER_PORT"
echo "   Base de datos: $SPRING_DATASOURCE_URL"
echo "   Auth Service: $AUTH_SERVICE_URL"
echo "   Evidence Service: $EVIDENCE_SERVICE_URL"
echo ""
echo "🔧 Para detener: Ctrl+C"
echo ""

# Start the service
java -jar target/app.jar
