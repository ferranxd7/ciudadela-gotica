#!/usr/bin/env bash
# Genera muestras de voz (Piper TTS) con efecto "narrador epico": mas grave, mas lento y con eco.
set -u
OUT=${1:-voces_out}
mkdir -p "$OUT" piper_models
if [ ! -x piper/piper ]; then
  curl -sL -o piper.tgz https://github.com/rhasspy/piper/releases/download/2023.11.14-2/piper_linux_x86_64.tar.gz
  tar xzf piper.tgz
fi
TEXT=$(cat ci/voces/frase.txt)
while IFS='|' read -r name path; do
  [ -z "$name" ] && continue
  base=https://huggingface.co/rhasspy/piper-voices/resolve/main/$path
  curl -sfL -o "piper_models/$name.onnx" "$base.onnx" || { echo "FALLO descarga $name"; continue; }
  curl -sfL -o "piper_models/$name.onnx.json" "$base.onnx.json" || { echo "FALLO json $name"; continue; }
  echo "$TEXT" | ./piper/piper --model "piper_models/$name.onnx" --length_scale 1.12 --output_file "$OUT/$name.wav" 2>/dev/null || { echo "FALLO voz $name"; continue; }
  sr=$(ffprobe -v error -select_streams a:0 -show_entries stream=sample_rate -of csv=p=0 "$OUT/$name.wav")
  # grave (-8%), eco de sala grande, refuerzo de graves y volumen normalizado
  ffmpeg -y -loglevel error -i "$OUT/$name.wav" -af "asetrate=${sr}*0.92,aresample=44100,equalizer=f=110:t=q:w=1:g=5,aecho=0.8:0.55:90|180|320:0.32|0.2|0.12,lowpass=f=7500,loudnorm=I=-16:TP=-1.5" -ac 1 -c:a libmp3lame -q:a 3 "$OUT/voz_$name.mp3"
  dur=$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$OUT/voz_$name.mp3")
  echo "OK $name ${dur}s"
done < ci/voces/voces.txt
rm -f "$OUT"/*.wav
