#!/bin/bash
# Compila el mod (solo para esto), vuelca un plano y lo dibuja:  tools/vista3d/vista.sh pueblo /tmp/pueblo [opciones de vista.py]
set -e
AQUI="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$AQUI/../.." && pwd)"
QUE="$1"; SAL="$2"; shift 2
CP=$(ls /tmp/cp/cpjars/*.jar | tr '\n' ':')
mkdir -p /tmp/vista3d/clases /tmp/vista3d/out "$SAL"
find "$REPO/src/main/java" -name "*.java" > /tmp/vista3d/fuentes.txt
javac -nowarn -encoding UTF-8 --release 17 -proc:none -cp "$CP" -d /tmp/vista3d/clases @/tmp/vista3d/fuentes.txt 2>&1 | grep -v "^Picked up\|^Note:" || true
javac -nowarn -encoding UTF-8 --release 17 -proc:none -cp "/tmp/vista3d/clases:$CP" -d /tmp/vista3d/out "$AQUI/Volcado.java" 2>&1 | grep -v "^Picked up\|^Note:" || true
java -Xmx3g ${VOLCADO_OPTS:-} -cp "/tmp/vista3d/out:/tmp/vista3d/clases:$CP" Volcado "$QUE" "$SAL/$QUE.bin" 2>&1 | grep -v "^Picked up\|WARN Advanced"
python3 "$AQUI/vista.py" "$SAL/$QUE.bin" "$SAL" "$@"
