# Junta las frases de la narracion con sus pausas, aplica el efecto y escribe ogg + tiempos.
import json, os, subprocess, sys, wave
import array

modelo, nombre = sys.argv[1], sys.argv[2]
RATE = 0.92          # mas grave y mas lento (asetrate)
LEAD = 0.3           # silencio inicial
OGG = "src/main/resources/assets/ciudadela/sounds/narracion/cap1.ogg"
JSN = "src/main/resources/assets/ciudadela/cinematica/cap1.json"
lineas = []
with open("ci/narracion/cap1.txt", encoding="utf-8") as f:
    for l in f:
        l = l.strip()
        if not l:
            continue
        txt, pausa = l.rsplit("|", 1)
        lineas.append((txt, float(pausa)))

def estimado():
    t, out = LEAD, []
    for txt, p in lineas:
        d = len(txt.split()) / 2.4
        out.append([round(t, 3), round(t + d, 3)])
        t += d + p
    return out, t + 1.0

ok = os.path.exists(modelo) and os.path.getsize(modelo) > 1000
muestras, sr, tiempos = array.array("h"), None, []
if ok:
    for i, (txt, pausa) in enumerate(lineas):
        wav = f"narr_tmp/l{i}.wav"
        r = subprocess.run(["./piper/piper", "--model", modelo, "--length_scale", "1.08", "--sentence_silence", "0",
                            "--output_file", wav], input=txt.encode("utf-8"), capture_output=True)
        if r.returncode != 0 or not os.path.exists(wav):
            print("FALLO piper en la linea", i, r.stderr.decode()[-300:])
            ok = False
            break
        with wave.open(wav) as w:
            if sr is None:
                sr = w.getframerate()
                muestras.extend([0] * int(LEAD * sr))
            data = array.array("h", w.readframes(w.getnframes()))
        start = len(muestras) / sr
        muestras.extend(data)
        end = len(muestras) / sr
        tiempos.append([round(start / RATE, 3), round(end / RATE, 3)])
        muestras.extend([0] * int(pausa * sr))
if ok:
    with wave.open("narr_tmp/todo.wav", "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(sr); w.writeframes(muestras.tobytes())
    fx = (f"asetrate={sr}*{RATE},aresample=44100,equalizer=f=110:t=q:w=1:g=5,"
          "aecho=0.8:0.5:90|180|320:0.3|0.18|0.1,lowpass=f=7500,loudnorm=I=-15:TP=-1.5")
    os.makedirs(os.path.dirname(OGG), exist_ok=True)
    r = subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", "narr_tmp/todo.wav", "-af", fx, "-ac", "1",
                        "-c:a", "libvorbis", "-q:a", "5", OGG], capture_output=True)
    if r.returncode != 0:
        print("FALLO ffmpeg", r.stderr.decode()[-300:])
        ok = False
if ok:
    d = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", OGG],
                       capture_output=True, text=True).stdout.strip()
    dur = float(d) if d else tiempos[-1][1] + 1.0
    info = {"voz": nombre, "audio": True, "duracion": round(dur, 3), "lineas": tiempos}
else:
    t, dur = estimado()
    info = {"voz": nombre, "audio": False, "duracion": round(dur, 3), "lineas": t}
    if os.path.exists(OGG):
        os.remove(OGG)
os.makedirs(os.path.dirname(JSN), exist_ok=True)
with open(JSN, "w") as f:
    json.dump(info, f)
print(("OK" if ok else "SIN AUDIO (tiempos estimados)"), json.dumps(info))
