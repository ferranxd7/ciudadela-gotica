#!/usr/bin/env bash
# Prueba del cliente (desde la parte 6): el juego DE VERDAD, como lo instala el usuario — Forge 1.20.1 y el jar de Dark
# Craft con Distant Horizons, Oculus y Embeddium dentro —, lanzado con portablemc. Si portablemc no instala Forge o el
# juego no llega a entrar en el mundo en 14 minutos, se corta y se repite en el entorno de desarrollo (gradle runClient,
# sin esos mods), para no quedarse sin capturas.
#
# Uso (desde la raiz del repositorio, con xvfb y el mundo de la prueba del servidor en srv2/world):
#   bash ci/cliente/lanzar.sh FORGE_VERSION JAR
set -u
FORGE="$1"
JAR="$2"
OUT="$PWD/out"
mkdir -p "$OUT"

preparar() {
  rm -rf run && mkdir -p run/saves run/config run/screenshots run/shaderpacks
  if [ -d srv2/world ]; then cp -r srv2/world run/saves/darkcraft; else echo "sin mundo de la prueba del servidor"; fi
  cp ci/cliente/options.txt run/options.txt
  printf 'earlyWindowControl = false\n' > run/config/fml.toml
}

dentro() { grep -q "Dentro del mundo" run/ci-cliente/informe_cliente.txt 2>/dev/null; }

preparar
sed "s#@WAV@#$OUT/audio_cliente.wav#" ci/cliente/alsoft.conf > "$RUNNER_TEMP/alsoft.conf"
export ALSOFT_CONF="$RUNNER_TEMP/alsoft.conf" LIBGL_ALWAYS_SOFTWARE=1 DARKCRAFT_CLIENTE=1
xvfb-run -a -s "-screen 0 1280x720x24" glxinfo -B > "$OUT/cliente_gl.txt" 2>&1 || true

MODO=desarrollo
PMCV="$RUNNER_TEMP/pmc"
if [ -f "$JAR" ] && { [ -x "$PMCV/bin/portablemc" ] || { python3 -m venv "$PMCV" && "$PMCV/bin/pip" install -q portablemc==5.0.6; }; } > "$OUT/pmc.log" 2>&1; then
  PMC=("$PMCV/bin/portablemc" --main-dir "$HOME/.pmc" start "forge::$FORGE" --mc-dir "$PWD/run" --jvm-policy system)
  if timeout 15m "${PMC[@]}" --dry >> "$OUT/pmc.log" 2>&1; then
    MODO=real
    mkdir -p run/mods && cp "$JAR" run/mods/
    cp "$OUT"/shaderpacks/*.zip run/shaderpacks/ 2>/dev/null || true
    echo "cliente: el juego de verdad (portablemc, Forge $FORGE, $(basename "$JAR"))" | tee "$OUT/cliente_modo.txt"
    ( timeout 134m xvfb-run -a -s "-screen 0 1280x720x24" "${PMC[@]}" --resolution 1280x720 -u Dev \
        --jvm-arg=-Xmx5G,-Ddarkcraft.ciClient=true > "$OUT/cliente.log" 2>&1 || echo "cliente terminado con error $?" >> "$OUT/cliente.log" ) &
    PID=$!
    for i in $(seq 1 84); do
      sleep 10
      dentro && break
      kill -0 "$PID" 2>/dev/null || break
    done
    if ! dentro; then
      echo "cliente: el juego de verdad no ha entrado en el mundo (ver cliente_real.log y crash-reports): se corta" | tee -a "$OUT/cliente_modo.txt"
      pkill -f "darkcraft.ciClient" 2>/dev/null || true
      sleep 5
      MODO=desarrollo
    fi
    wait "$PID" 2>/dev/null || true
    if [ "$MODO" = desarrollo ]; then
      cp "$OUT/cliente.log" "$OUT/cliente_real.log" 2>/dev/null || true
      cp run/logs/latest.log "$OUT/cliente_real_latest.log" 2>/dev/null || true
      for f in run/crash-reports/*.txt; do [ -f "$f" ] && cp "$f" "$OUT/crash_real_$(basename "$f")"; done
      cp run/ci-cliente/informe_cliente.txt "$OUT/informe_cliente_real.txt" 2>/dev/null || true
    fi
  else
    echo "cliente: portablemc no ha instalado Forge (ver pmc.log)" | tee "$OUT/cliente_modo.txt"
  fi
else
  echo "cliente: sin portablemc (ver pmc.log)" | tee "$OUT/cliente_modo.txt"
fi

if [ "$MODO" = desarrollo ]; then
  echo "cliente: prueba en el entorno de desarrollo (sin Distant Horizons, Oculus ni Embeddium)" | tee -a "$OUT/cliente_modo.txt"
  preparar
  timeout 132m xvfb-run -a -s "-screen 0 1280x720x24" gradle runClient --no-daemon > "$OUT/cliente.log" 2>&1 || echo "cliente terminado con error $?" >> "$OUT/cliente.log"
fi

cp run/logs/latest.log "$OUT/cliente_latest.log" 2>/dev/null || true
for f in run/crash-reports/*.txt; do [ -f "$f" ] && cp "$f" "$OUT/crash_$(basename "$f")"; done
for f in DistantHorizons.toml oculus.properties darkcraft-arranque.properties embeddium-options.json; do
  [ -f "run/config/$f" ] && cp "run/config/$f" "$OUT/cliente_config_$f.txt"
done
exit 0
