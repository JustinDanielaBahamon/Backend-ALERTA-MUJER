#!/bin/bash

# Script para iniciar todos los microservicios en terminales separadas
# Requiere: gnome-terminal, xterm, o konsole

echo "🚀 Iniciando todos los microservicios de AlertaMujer..."
echo "========================================================="
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Verificar que los JARs estén compilados
if [ ! -f "$SCRIPT_DIR/auth-service/target/app.jar" ] || \
   [ ! -f "$SCRIPT_DIR/alert-service/target/app.jar" ] || \
   [ ! -f "$SCRIPT_DIR/evidence-service/target/app.jar" ]; then
    echo "❌ Error: Los JARs no están compilados."
    echo ""
    echo "Compilando ahora..."
    cd "$SCRIPT_DIR"
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "❌ Error en la compilación. Abortando."
        exit 1
    fi
fi

# Función para abrir terminal según el sistema
open_terminal() {
    local title="$1"
    local script="$2"
    
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal --title="$title" -- bash -c "cd '$SCRIPT_DIR' && bash '$script'; exec bash"
    elif command -v xterm &> /dev/null; then
        xterm -T "$title" -e "cd '$SCRIPT_DIR' && bash '$script'; bash" &
    elif command -v konsole &> /dev/null; then
        konsole --new-tab -p tabtitle="$title" -e bash -c "cd '$SCRIPT_DIR' && bash '$script'; exec bash" &
    else
        echo "❌ No se encontró un emulador de terminal compatible."
        echo "Por favor, ejecuta manualmente cada script en terminales separadas:"
        echo "  - ./start-auth-service.sh"
        echo "  - ./start-alert-service.sh"
        echo "  - ./start-evidence-service.sh"
        exit 1
    fi
}

echo "📱 Abriendo terminales para cada servicio..."
echo ""

# Iniciar cada servicio en su propia terminal
sleep 1
open_terminal "Auth Service (8081)" "start-auth-service.sh"
echo "✅ Auth Service iniciándose en puerto 8081..."

sleep 2
open_terminal "Alert Service (8082)" "start-alert-service.sh"
echo "✅ Alert Service iniciándose en puerto 8082..."

sleep 2
open_terminal "Evidence Service (8083)" "start-evidence-service.sh"
echo "✅ Evidence Service iniciándose en puerto 8083..."

echo ""
echo "========================================================="
echo "✅ Todos los servicios están iniciándose"
echo ""
echo "📊 URLs de los servicios:"
echo "   🔐 Auth:     http://localhost:8081"
echo "   🚨 Alert:    http://localhost:8082"
echo "   📸 Evidence: http://localhost:8083"
echo ""
echo "📖 Documentación API (Swagger):"
echo "   http://localhost:8081/swagger-ui.html"
echo "   http://localhost:8082/swagger-ui.html"
echo "   http://localhost:8083/swagger-ui.html"
echo ""
echo "🔧 Para detener: Cierra cada terminal o presiona Ctrl+C"
echo "========================================================="
