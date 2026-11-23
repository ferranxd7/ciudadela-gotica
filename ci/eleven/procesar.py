#!/usr/bin/env python3
"""
Monta los audios de ElevenLabs (ci/eleven/master) para el juego:
- Voz del Rey Corvin (voz "David - British Storyteller"): eco suave de sala de piedra. En la fase 3 (frases 9 a
  13) la misma voz con efecto de monstruo: mas grave, una segunda voz una octava por debajo, un poco de
  saturacion, eco mas largo y fuego crepitando debajo.
- Efectos de sonido: sin el silencio del principio, recortados a lo que dura cada uno en el juego, en mono (para
  que se oigan desde donde suenan) y con el volumen igualado.
- Voz de Ser Gavran (voz "Final Boss - Evil All Powerful Villain"): eco de sala grande (la cupula del arbol) y un
  poco mas de graves. En la fase 2 (frases 11, 12, 14, 15 y 16) la misma voz mas grave, con una segunda voz por
  debajo y madera crujiendo (como el fuego del Rey).
Sale todo en src/main/resources/assets/darkcraft/sounds/ (rey/voz_N.ogg, gavran/voz_N.ogg, sfx/NOMBRE_K.ogg) y las
duraciones de las frases en assets/darkcraft/rey_voces.json ("duraciones" del Rey y "gavran"). Necesita ffmpeg.
"""
import json
import os
import subprocess
import sys

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
MASTER = os.path.join(AQUI, "master")
ASSETS = os.path.join(REPO, "src/main/resources/assets/darkcraft")

# toma elegida de cada frase del Rey (a o b) y las que van con voz de monstruo
TOMAS = {n: "a" for n in range(1, 14)}
MONSTRUO = {9, 10, 11, 12, 13}

ECO_SALA = "aecho=0.8:0.55:55|110|190:0.20|0.12|0.07"

# Ser Gavran: toma elegida de cada frase y las de la fase 2 (voz de arbol)
TOMAS_GAVRAN = {n: "a" for n in range(1, 19)}
ARBOL = {11, 12, 14, 15, 16}
ECO_CUPULA = "aecho=0.8:0.6:70|150|260:0.22|0.14|0.08"
VOZ_GAVRAN = ("highpass=f=55,equalizer=f=120:t=q:w=1.0:g=3," + ECO_CUPULA + ","
              "acompressor=threshold=-20dB:ratio=2.5:attack=5:release=140,loudnorm=I=-15:TP=-1.5:LRA=9")
VOZ = ("highpass=f=65," + ECO_SALA + ",acompressor=threshold=-20dB:ratio=2.5:attack=5:release=120,"
       "loudnorm=I=-15:TP=-1.5:LRA=9")

# efectos: nombre -> (segundos como mucho, fundido final en s, volumen integrado LUFS)
SFX = {
    "rey_tajo": (1.4, 0.25, -14), "rey_tajo_fuego": (2.8, 0.6, -15), "rey_bola_fuego": (2.4, 0.5, -15),
    "rey_explosion": (4.5, 1.2, -13), "rey_golpe_suelo": (2.8, 0.6, -13), "rey_golpe_final": (6.0, 1.5, -12),
    "rey_rugido": (4.2, 0.8, -13), "rey_geiser": (4.0, 1.0, -15), "rey_pilares": (3.5, 0.8, -15),
    "rey_carga": (3.2, 0.5, -16), "cuenta_atras": (1.9, 0.4, -13), "circulo_dorado": (3.5, 0.8, -15),
    "puerta_runas": (4.0, 0.8, -16), "puerta_abre": (7.0, 1.2, -14), "puerta_cierra": (3.0, 0.6, -13),
    "ascensor_mueve": (7.0, 2.0, -18), "ascensor_llega": (2.5, 0.6, -16),
    # Ser Gavran (una sola toma de cada uno)
    "gavran_tajo": (1.6, 0.35, -14), "gavran_choque": (1.8, 0.5, -14), "gavran_embestida": (3.2, 0.8, -14),
    "gavran_raices": (2.6, 0.7, -15), "gavran_clavar": (3.6, 1.0, -13), "gavran_invocar": (4.5, 1.2, -15),
    "gavran_paso": (0.9, 0.25, -19), "gavran_guardia_rota": (3.2, 0.9, -13), "gavran_salto": (2.8, 0.7, -14),
    "arbol_despierta": (15.0, 3.0, -14), "hojas_cortantes": (3.2, 0.8, -16), "raices_atrapan": (2.8, 0.7, -15),
    "espinas": (3.8, 0.9, -14), "corteza_muro": (2.8, 0.7, -15), "florecer": (7.0, 2.0, -16),
    "madera_cruje": (3.2, 0.8, -16), "corazon_latido": (3.0, 0.8, -15), "hojas_disolver": (5.0, 1.5, -16),
    # Espejo de la Sombra y libro de la M
    "espejo_despierta": (3.0, 0.7, -14), "pluma_escribe": (2.0, 0.3, -20), "libro_abre": (2.6, 0.8, -17),
}
# de estos se bajaron 4 tomas (NOMBRE_1..4.mp3): las elegidas salen como NOMBRE_1.ogg, NOMBRE_2.ogg...
TOMAS_SFX = {
    "espejo_despierta": (3,),   # empieza grave y se aclara (el zumbido que sube y la campanilla)
    "pluma_escribe": (2, 4),    # las dos de volumen mas parejo
    "libro_abre": (3,),         # la del golpe de la tapa al principio
}
# efectos con una sola toma (_1)
UNA_TOMA = {k for k in SFX if k.startswith("gavran_")} | {"arbol_despierta", "hojas_cortantes", "raices_atrapan", "espinas",
                                                          "corteza_muro", "florecer", "madera_cruje", "corazon_latido",
                                                          "hojas_disolver"}


def ff(args):
    r = subprocess.run(["ffmpeg", "-y", "-v", "error"] + args, capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError(r.stderr[-600:])


def duracion(f):
    p = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", f],
                       capture_output=True, text=True)
    return float(p.stdout.strip())


def voz_normal(src, dst):
    ff(["-i", src, "-af", VOZ, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])


def voz_monstruo(src, dst, fuego, desfase):
    d = duracion(src) * 1.12 + 0.9
    grafo = (
        "[0:a]asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.82,aresample=44100,atempo=1.10[baja];"
        "[v2]asetrate=44100*0.60,aresample=44100,atempo=1.5033,volume=0.55[sub];"
        "[baja][sub]amix=inputs=2:weights=1 0.7:normalize=0,"
        "asoftclip=type=atan:threshold=0.6,equalizer=f=95:t=q:w=1.1:g=5,equalizer=f=2600:t=q:w=1.4:g=-2,"
        "aecho=0.8:0.6:70|150|270:0.28|0.18|0.10[voz];"
        f"[1:a]atrim=start={desfase:.2f}:duration={d:.2f},asetpts=PTS-STARTPTS,highpass=f=180,volume=0.32,"
        f"afade=t=in:d=0.35,afade=t=out:st={max(0.1, d - 0.8):.2f}:d=0.8[fuego];"
        "[voz][fuego]amix=inputs=2:weights=1 1:normalize=0:duration=first,"
        "acompressor=threshold=-18dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-i", fuego, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1",
        "-c:a", "libvorbis", "-q:a", "6", dst])


def voz_gavran(src, dst):
    ff(["-i", src, "-af", VOZ_GAVRAN, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])


def voz_arbol(src, dst, madera, desfase):
    """La voz de la fase 2: mas grave, una segunda voz por debajo y madera crujiendo."""
    d = duracion(src) * 1.08 + 1.0
    grafo = (
        "[0:a]asplit=2[v1][v2];"
        "[v1]asetrate=44100*0.88,aresample=44100,atempo=1.0606[baja];"
        "[v2]asetrate=44100*0.66,aresample=44100,atempo=1.4141,volume=0.5[sub];"
        "[baja][sub]amix=inputs=2:weights=1 0.65:normalize=0,"
        "asoftclip=type=atan:threshold=0.7,equalizer=f=100:t=q:w=1.1:g=4,equalizer=f=2800:t=q:w=1.4:g=-2,"
        "aecho=0.8:0.6:80|170|290:0.26|0.16|0.09[voz];"
        f"[1:a]atrim=start={desfase:.2f}:duration={d:.2f},asetpts=PTS-STARTPTS,highpass=f=120,volume=0.35,"
        f"afade=t=in:d=0.3,afade=t=out:st={max(0.1, d - 0.8):.2f}:d=0.8[madera];"
        "[voz][madera]amix=inputs=2:weights=1 1:normalize=0:duration=first,"
        "acompressor=threshold=-18dB:ratio=3:attack=4:release=140,loudnorm=I=-14:TP=-1.5:LRA=9[out]"
    )
    ff(["-i", src, "-i", madera, "-filter_complex", grafo, "-map", "[out]", "-ar", "44100", "-ac", "1",
        "-c:a", "libvorbis", "-q:a", "6", dst])


def efecto(src, dst, maxs, fade, lufs):
    filtro = (f"silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.02,atrim=0:{maxs},"
              f"afade=t=out:st={max(0.05, maxs - fade):.2f}:d={fade},"
              f"loudnorm=I={lufs}:TP=-1.0:LRA=11")
    ff(["-i", src, "-af", filtro, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])


def main():
    if not os.path.isdir(MASTER):
        print("no hay ci/eleven/master: nada que montar")
        return 1
    rey = os.path.join(ASSETS, "sounds", "rey")
    sfx = os.path.join(ASSETS, "sounds", "sfx")
    os.makedirs(rey, exist_ok=True)
    os.makedirs(sfx, exist_ok=True)
    fuego = os.path.join(MASTER, "sfx", "fuego_crepitar_1.mp3")
    dur, fallos = {}, 0
    for n, toma in sorted(TOMAS.items()):
        src = os.path.join(MASTER, "rey", f"voz_{n}{toma}.mp3")
        dst = os.path.join(rey, f"voz_{n}.ogg")
        try:
            if n in MONSTRUO and os.path.exists(fuego):
                voz_monstruo(src, dst, fuego, 1.5 + (n * 2.3) % 20)
            else:
                voz_normal(src, dst)
            dur[n] = round(duracion(dst), 2)
            print(f"voz {n:2d}{' (monstruo)' if n in MONSTRUO else ''}: {dur[n]} s")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO voz {n}: {e}")
    # Ser Gavran
    gav = os.path.join(ASSETS, "sounds", "gavran")
    os.makedirs(gav, exist_ok=True)
    madera = os.path.join(MASTER, "sfx", "madera_cruje_1.mp3")
    dur_g = {}
    for n, toma in sorted(TOMAS_GAVRAN.items()):
        src = os.path.join(MASTER, "gavran", f"voz_{n}{toma}.mp3")
        dst = os.path.join(gav, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            if n in ARBOL and os.path.exists(madera):
                voz_arbol(src, dst, madera, (n * 0.7) % 2.5)
            else:
                voz_gavran(src, dst)
            dur_g[n] = round(duracion(dst), 2)
            print(f"gavran {n:2d}{' (arbol)' if n in ARBOL else ''}: {dur_g[n]} s")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO gavran {n}: {e}")
    with open(os.path.join(ASSETS, "rey_voces.json"), "w") as fh:
        json.dump({"duraciones": dur, "gavran": dur_g}, fh)
    for nombre, (maxs, fade, lufs) in SFX.items():
        tomas = TOMAS_SFX.get(nombre) or ((1,) if nombre in UNA_TOMA else (1, 2))
        for i, k in enumerate(tomas, 1):
            src = os.path.join(MASTER, "sfx", f"{nombre}_{k}.mp3")
            if not os.path.exists(src):
                print("falta", src)
                fallos += 1
                continue
            dst = os.path.join(sfx, f"{nombre}_{i}.ogg")
            try:
                efecto(src, dst, maxs, fade, lufs)
                print(f"efecto {nombre}_{i} (toma {k}): {duracion(dst):.2f} s")
            except Exception as e:  # noqa: BLE001
                fallos += 1
                print(f"FALLO efecto {nombre}_{k}: {e}")
    print("OK" if not fallos else f"FALLOS: {fallos}")
    return 1 if fallos else 0


if __name__ == "__main__":
    sys.exit(main())
