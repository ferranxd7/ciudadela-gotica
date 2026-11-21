#!/usr/bin/env python3
"""Analiza el sonido que grabo el cliente de la CI (OpenAL -> WAV): volumen segundo a segundo y si
aparecen dentro la voz del narrador y las musicas del mod (por correlacion con los archivos originales).

Uso: audio.py <grabacion.wav> <carpeta sounds del mod> <carpeta de salida>
"""
import os
import struct
import subprocess
import sys

import numpy as np

SR = 7350  # 44100 / 6


def leer_wav(ruta):
    with open(ruta, "rb") as f:
        data = f.read()
    if len(data) < 44 or data[:4] != b"RIFF":
        raise RuntimeError("no es un WAV")
    pos, fmt, raw = 12, None, None
    while pos + 8 <= len(data):
        cid, size = data[pos:pos + 4], struct.unpack("<I", data[pos + 4:pos + 8])[0]
        body = pos + 8
        if cid == b"fmt ":
            fmt = struct.unpack("<HHIIHH", data[body:body + 16])
        elif cid == b"data":
            end = body + size if 0 < size <= len(data) - body else len(data)
            raw = data[body:end]
            break
        pos = body + size + (size & 1)
    if fmt is None or raw is None:
        raise RuntimeError("WAV sin formato o sin datos")
    _, ch, rate, _, _, bits = fmt
    if bits != 16:
        raise RuntimeError(f"WAV de {bits} bits")
    a = np.frombuffer(raw[: len(raw) // (2 * ch) * 2 * ch], dtype="<i2").astype(np.float32) / 32768.0
    a = a.reshape(-1, ch).mean(axis=1)
    return a, rate


def bajar(a, rate):
    k = max(1, int(round(rate / SR)))
    n = len(a) // k * k
    return a[:n].reshape(-1, k).mean(axis=1)


def decodificar(ruta):
    r = subprocess.run(["ffmpeg", "-v", "error", "-i", ruta, "-ac", "1", "-ar", str(SR), "-f", "s16le", "-"],
                       capture_output=True)
    if r.returncode != 0 or not r.stdout:
        return None
    return np.frombuffer(r.stdout, dtype="<i2").astype(np.float32) / 32768.0


def buscar(rec, ref, nombre, max_ref_s=40):
    """Mejor coincidencia normalizada de ref (sus primeros segundos) dentro de rec."""
    ref = ref[: int(max_ref_s * SR)]
    if len(ref) < SR or len(rec) < len(ref):
        return f"{nombre}: demasiado corto para comparar"
    ref = ref - ref.mean()
    nfft = 1 << int(np.ceil(np.log2(len(rec) + len(ref))))
    c = np.fft.irfft(np.fft.rfft(rec, nfft) * np.conj(np.fft.rfft(ref, nfft)), nfft)[: len(rec) - len(ref) + 1]
    e = np.concatenate([[0.0], np.cumsum(rec.astype(np.float64) ** 2)])
    win = np.sqrt(np.maximum(e[len(ref):] - e[: len(rec) - len(ref) + 1], 1e-12))
    norm = c / (win * np.sqrt((ref ** 2).sum()) + 1e-9)
    i = int(np.argmax(norm))
    # cuanto destaca el pico frente al resto
    fondo = float(np.percentile(np.abs(norm), 99.5))
    return (f"{nombre}: mejor coincidencia {norm[i]:.3f} a los {i / SR:.1f} s de la grabacion "
            f"(fondo {fondo:.3f}) -> {'SUENA' if norm[i] > 0.6 or (norm[i] > 0.25 and norm[i] > 2.5 * fondo) else 'no se encuentra'}")


def main():
    wav, sonidos, salida = sys.argv[1], sys.argv[2], sys.argv[3]
    if not os.path.exists(wav):
        print("No hay grabacion (el cliente no arranco el sonido)")
        return
    a, rate = leer_wav(wav)
    print(f"Grabacion: {len(a) / rate:.1f} s a {rate} Hz")
    rec = bajar(a, rate)
    # volumen segundo a segundo
    lin = []
    for s in range(0, len(rec) // SR):
        seg = rec[s * SR:(s + 1) * SR]
        rms = float(np.sqrt((seg ** 2).mean()) + 1e-9)
        db = 20 * np.log10(rms)
        lin.append(f"{s:4d} s {db:6.1f} dB {'#' * max(0, int((db + 60) / 2))}")
    print("\n".join(lin))
    print()
    for nombre, rel in [("voz del capitulo 1", "narracion/cap1.ogg"), ("musica de los cofres", "musica/cofre.ogg"),
                        ("campana", "campana.ogg")]:
        ruta = os.path.join(sonidos, rel)
        if not os.path.exists(ruta):
            print(f"{nombre}: no esta el archivo {rel}")
            continue
        ref = decodificar(ruta)
        if ref is None:
            print(f"{nombre}: no se pudo leer {rel}")
            continue
        print(buscar(rec, ref, nombre, 3 if nombre == "campana" else 40))
    # copia comprimida para escucharla
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", wav, "-ac", "2", "-c:a", "libvorbis", "-q:a", "1",
                    os.path.join(salida, "audio_cliente.ogg")])


if __name__ == "__main__":
    main()
