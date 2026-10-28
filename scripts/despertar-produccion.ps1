# Despierta los 3 microservicios de producción (Render, plan gratis) y espera a que respondan.
# En el plan gratis se duermen tras ~15 min sin uso; la primera petición tarda 1 a 3 minutos.
# Uso (PowerShell):  powershell -ExecutionPolicy Bypass -File scripts\despertar-produccion.ps1
$servicios = @("auth", "alert", "evidence")
$limite = (Get-Date).AddMinutes(7)
$listos = @{}

Write-Host "Despertando AlertaMujer en producción (puede tardar hasta 3 minutos)..."
$inicio = Get-Date
while ($listos.Count -lt $servicios.Count -and (Get-Date) -lt $limite) {
    foreach ($s in $servicios) {
        if ($listos.ContainsKey($s)) { continue }
        try {
            $r = Invoke-WebRequest -Uri "https://alerta-mujer-$s.onrender.com/actuator/health" -UseBasicParsing -TimeoutSec 20
            if ($r.StatusCode -eq 200) {
                $listos[$s] = $true
                Write-Host ("  OK {0} listo ({1} s)" -f $s, [int]((Get-Date) - $inicio).TotalSeconds)
            }
        } catch { }
    }
    if ($listos.Count -lt $servicios.Count) { Start-Sleep -Seconds 5 }
}
foreach ($s in $servicios) {
    if (-not $listos.ContainsKey($s)) { Write-Host "  X $s no respondió en 7 min: revisa sus logs en el dashboard de Render" }
}
Write-Host "Web: https://alerta-mujer.vercel.app"
Write-Host "Se vuelven a dormir tras ~15 min sin uso: si pasa, ejecuta este script otra vez."
