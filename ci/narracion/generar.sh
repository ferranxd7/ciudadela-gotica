#!/usr/bin/env bash
# Narraciones de los capitulos (1 y 5): voz en ingles con Piper + efecto de narrador, y la tabla de tiempos de
# cada frase (para los subtitulos y los cortes de plano de la cinematica). Un capN.txt por capitulo narrado.
set -u
VOZ=$(cut -d'|' -f2 ci/narracion/voz.txt | tr -d '\r\n')
NOMBRE=$(cut -d'|' -f1 ci/narracion/voz.txt | tr -d '\r\n')
mkdir -p narr_tmp
if [ ! -x piper/piper ]; then
  curl -sL -o piper.tgz https://github.com/rhasspy/piper/releases/download/2023.11.14-2/piper_linux_x86_64.tar.gz && tar xzf piper.tgz
fi
base=https://huggingface.co/rhasspy/piper-voices/resolve/main/$VOZ
[ -s "narr_tmp/$NOMBRE.onnx" ] || curl -sfL -o "narr_tmp/$NOMBRE.onnx" "$base.onnx" || echo "FALLO descarga voz"
[ -s "narr_tmp/$NOMBRE.onnx.json" ] || curl -sfL -o "narr_tmp/$NOMBRE.onnx.json" "$base.onnx.json" || echo "FALLO descarga json"
for txt in ci/narracion/cap*.txt; do
  cap=$(basename "$txt" .txt); cap=${cap#cap}
  python3 ci/narracion/montar.py "narr_tmp/$NOMBRE.onnx" "$NOMBRE" "$cap"
done
