#!/usr/bin/env bash
# Despierta los 3 microservicios de producción (Render, plan gratis) y espera a que respondan.
# En el plan gratis se duermen tras ~15 min sin uso; la primera petición tarda 1 a 3 minutos.
# Uso:  bash scripts/despertar-produccion.sh
SERVICIOS=(auth alert evidence)
LIMITE_S=420
inicio=$(date +%s)

echo "Despertando AlertaMujer en producción (puede tardar hasta 3 minutos)..."
for s in "${SERVICIOS[@]}"; do
  (
    url="https://alerta-mujer-$s.onrender.com/actuator/health"
    until [ "$(curl -s -o /dev/null -w '%{http_code}' --max-time 20 "$url")" = "200" ]; do
      if [ $(( $(date +%s) - inicio )) -gt $LIMITE_S ]; then
        echo "  ✗ $s no respondió en $((LIMITE_S / 60)) min: revisa sus logs en el dashboard de Render"
        exit 1
      fi
      sleep 5
    done
    echo "  ✓ $s listo ($(( $(date +%s) - inicio )) s)"
  ) &
done
wait
echo "Listo. Web: https://alerta-mujer.vercel.app"
echo "Se vuelven a dormir tras ~15 min sin uso: si pasa, ejecuta este script otra vez."
