#!/usr/bin/env python3
"""Voz del Rey Corvin: cada frase de frases.txt con Piper (voz inglesa grave) y efectos de salon
(mas grave, eco de sala de piedra). Sale sounds/rey/voz_N.ogg y la duracion de cada una."""
import json
import os
import subprocess
import sys
import urllib.request

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
OUT = os.path.join(REPO, "src/main/resources/assets/ciudadela/sounds/rey")
TMP = os.path.join(REPO, "narr_tmp")


def main():
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(TMP, exist_ok=True)
    nombre, ruta = open(os.path.join(AQUI, "voz.txt")).read().strip().split("|")
    onnx = os.path.join(TMP, nombre + ".onnx")
    base = "https://huggingface.co/rhasspy/piper-voices/resolve/main/" + ruta
    for ext in (".onnx", ".onnx.json"):
        dst = onnx if ext == ".onnx" else onnx + ".json"
        if not os.path.exists(dst) or os.path.getsize(dst) < 1000:
            print("bajando", base + ext)
            urllib.request.urlretrieve(base + ext, dst)
    piper = os.path.join(REPO, "piper", "piper")
    frases = [l.strip() for l in open(os.path.join(AQUI, "frases.txt"), encoding="utf-8") if l.strip()]
    dur = {}
    for i, f in enumerate(frases, start=1):
        wav = os.path.join(TMP, f"rey_{i}.wav")
        r = subprocess.run([piper, "--model", onnx, "--output_file", wav, "--length_scale", "1.12", "--sentence_silence", "0.3"],
                           input=f, text=True, capture_output=True)
        if r.returncode != 0 or not os.path.exists(wav):
            print("FALLO piper", i, r.stderr[-400:])
            continue
        ogg = os.path.join(OUT, f"voz_{i}.ogg")
        filtro = ("asetrate=22050*0.84,aresample=44100,atempo=1.06,"
                  "equalizer=f=110:t=q:w=1.2:g=5,equalizer=f=3200:t=q:w=1.5:g=-3,"
                  "aecho=0.8:0.78:60|130|210:0.30|0.18|0.10,"
                  "acompressor=threshold=-20dB:ratio=3:attack=5:release=120,"
                  "loudnorm=I=-15:TP=-1.5:LRA=9")
        r = subprocess.run(["ffmpeg", "-y", "-v", "error", "-i", wav, "-af", filtro, "-ar", "44100", "-ac", "1",
                            "-c:a", "libvorbis", "-q:a", "5", ogg], capture_output=True, text=True)
        if r.returncode != 0:
            print("FALLO ffmpeg", i, r.stderr[-400:])
            continue
        p = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", ogg],
                           capture_output=True, text=True)
        try:
            dur[i] = round(float(p.stdout.strip()), 2)
        except ValueError:
            dur[i] = 4.0
        print(f"voz {i}: {dur[i]} s  {f}")
    with open(os.path.join(REPO, "src/main/resources/assets/ciudadela/rey_voces.json"), "w") as fh:
        json.dump({"duraciones": dur}, fh)
    print("OK" if len(dur) == len(frases) else f"FALTAN {len(frases) - len(dur)}")


if __name__ == "__main__":
    sys.exit(main())
