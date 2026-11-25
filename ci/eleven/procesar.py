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
duraciones de las frases en assets/darkcraft/rey_voces.json ("duraciones" del Rey y "gavran"; "habla" y
"habla_gavran" = el segundo en que acaba de hablar de verdad, sin el silencio ni la cola del eco: con eso cuenta el
juego para que la siguiente frase o la pelea no tarden en llegar). A las voces se les quita el silencio del principio.
Necesita ffmpeg.
"""
import json
import os
import re
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

# Maelis, la guia de los santuarios (voz "Autumn Veil"): serena y cerca, con un poco de aire de piedra
TOMAS_GUIA = {n: "a" for n in range(1, 11)}
VOZ_GUIA = ("highpass=f=70,equalizer=f=3200:t=q:w=1.2:g=1.5,aecho=0.8:0.35:38|85:0.10|0.05,"
            "acompressor=threshold=-22dB:ratio=2.2:attack=6:release=160,loudnorm=I=-16:TP=-1.5:LRA=9")
# Dorvan, el herrero (voz "Fjalar - Gruff Scottish Dwarf Warrior"): ronco, con algo de graves y el eco corto de la
# herreria (piedra y metal). La 5b es mas corta y va al grano (la 5a se toma un respiro al empezar).
TOMAS_HERRERO = {n: "a" for n in range(1, 9)} | {5: "b"}
VOZ_HERRERO = ("highpass=f=60,equalizer=f=140:t=q:w=1.0:g=2,equalizer=f=2600:t=q:w=1.3:g=1,"
               "aecho=0.8:0.4:32|71:0.14|0.07,acompressor=threshold=-21dB:ratio=2.5:attack=5:release=140,"
               "loudnorm=I=-15:TP=-1.5:LRA=9")
# Kerrick, el mercader (voz "Cornelius - Wise Sage"): al aire libre, casi seco y cercano
TOMAS_MERCADER = {n: "a" for n in range(1, 7)}
VOZ_MERCADER = ("highpass=f=75,equalizer=f=3000:t=q:w=1.2:g=1.5,aecho=0.8:0.25:24:0.06,"
                "acompressor=threshold=-22dB:ratio=2.2:attack=6:release=150,loudnorm=I=-16:TP=-1.5:LRA=9")
# los personajes con dialogo: id -> (tomas, filtro)
PERSONAJES = {"guia": (TOMAS_GUIA, VOZ_GUIA), "herrero": (TOMAS_HERRERO, VOZ_HERRERO), "mercader": (TOMAS_MERCADER, VOZ_MERCADER)}

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
    # caballeros zombi (el paso: solo el primero de los dos pasos de cada toma)
    "zombi_caballero_grunido": (2.0, 0.4, -18), "zombi_caballero_dolor": (0.9, 0.2, -17), "zombi_caballero_muerte": (2.6, 0.5, -16),
    "armadura_paso": (0.75, 0.2, -23), "escudo_golpe": (1.0, 0.3, -14), "alabarda_tajo": (1.1, 0.3, -15),
    # la Fortaleza de Hueso
    "llave_rastro": (2.0, 0.5, -19), "puente_hueso": (5.5, 1.2, -15), "puerta_hueso": (8.0, 1.8, -13),
    "amuleto_forja": (5.0, 1.5, -14), "portal_abre": (7.0, 2.0, -13),
    # el HUD nuevo y el rol: carteles, ecos, espada de luz y guardia rota
    "has_muerto": (5.0, 2.2, -13), "jefe_abatido": (5.0, 2.0, -13), "zona_descubierta": (4.0, 1.6, -16),
    "ecos_recuperados": (2.6, 0.8, -15), "ecos_ganados": (0.7, 0.3, -22), "espada_luz": (3.8, 0.9, -21),
    "guardia_rota": (1.3, 0.3, -13),
    # santuarios, frascos, subir de nivel y los menus (parte 2b)
    "santuario_enciende": (4.0, 1.4, -14), "santuario_fuego": (6.0, 1.0, -24), "santuario_descanso": (3.0, 0.9, -16),
    "frasco_beber": (1.3, 0.25, -17), "nivel_sube": (2.6, 0.9, -14),
    "menu_abre": (0.66, 0.12, -19), "menu_mueve": (0.3, 0.1, -26), "menu_acepta": (0.45, 0.15, -20), "menu_cancela": (0.4, 0.12, -20),
    # el combate propio (parte 2c)
    "golpe_ligero": (0.55, 0.12, -17), "golpe_pesado": (0.9, 0.2, -15), "carga_golpe": (1.3, 0.1, -18),
    "impacto_carne": (0.45, 0.12, -15), "impacto_armadura": (0.6, 0.2, -16), "rodar": (0.6, 0.15, -19), "fijar": (0.45, 0.12, -20),
    "estocada": (0.45, 0.12, -16), "golpe_salto": (1.1, 0.4, -13), "aturdido": (1.3, 0.35, -15), "esfuerzo": (0.45, 0.1, -19),
    "comer_fruto": (1.0, 0.25, -18), "talisman": (0.9, 0.25, -19),
    "hechizo_lanzar": (0.7, 0.15, -17), "hechizo_impacto": (0.6, 0.2, -16), "hechizo_rayo": (1.0, 0.3, -15),
    "hechizo_llama": (1.1, 0.35, -16), "hechizo_curar": (1.6, 0.4, -18), "hechizo_bendicion": (1.5, 0.4, -18),
    # el herrero y el mercader (parte 2e-2): el martillo en el yunque, mejorar un arma y comprar
    "herrero_golpe": (0.9, 0.35, -17), "forja_mejora": (2.0, 0.7, -14), "tienda_compra": (0.8, 0.25, -18),
    # las habilidades de las armas (cenizas de guerra, parte 2e-3) y ponerle una ceniza a un arma
    "habilidad_torbellino": (1.0, 0.3, -15), "habilidad_estocada": (0.9, 0.25, -15), "habilidad_grito": (1.6, 0.4, -14),
    "habilidad_sismo": (1.8, 0.6, -13), "habilidad_paso": (0.6, 0.15, -18), "habilidad_llamas": (1.2, 0.4, -15),
    "habilidad_escarcha": (1.2, 0.4, -15), "habilidad_relampago": (1.2, 0.4, -14), "habilidad_sangre": (1.0, 0.3, -15),
    "habilidad_parada": (0.8, 0.25, -14), "ceniza_aplicar": (2.0, 0.6, -15),
    # el codice de la historia (parte 2e-4): abrirlo, pasar una pagina y cerrarlo
    "codice_abre": (1.0, 0.3, -18), "codice_pagina": (0.6, 0.15, -19), "codice_cierra": (0.8, 0.3, -16),
}
# los que suenan en bucle (se relanzan antes de acabar y se solapan): entran poco a poco (segundos de fundido de entrada)
ENTRADA = {"santuario_fuego": 0.8, "espada_luz": 0.5}
# de estos se bajaron 4 tomas (NOMBRE_1..4.mp3): las elegidas salen como NOMBRE_1.ogg, NOMBRE_2.ogg...
TOMAS_SFX = {
    "espejo_despierta": (3,),   # empieza grave y se aclara (el zumbido que sube y la campanilla)
    "pluma_escribe": (2, 4),    # las dos de volumen mas parejo
    "libro_abre": (3,),         # la del golpe de la tapa al principio
    "zombi_caballero_grunido": (1, 2, 3, 4), "zombi_caballero_dolor": (1, 2, 3, 4), "zombi_caballero_muerte": (1, 2),
    "armadura_paso": (1, 2, 3, 4), "escudo_golpe": (1, 3), "alabarda_tajo": (2, 4),
    "llave_rastro": (2, 3),      # las dos que suenan enseguida (la 4 casi no se oye)
    "puente_hueso": (1, 2),      # las que crecen y se mantienen
    "puerta_hueso": (1,),        # la mas larga: el cuerpo pesado dura lo que sube la reja
    "amuleto_forja": (1,),       # golpe y brillo que se sostiene antes de apagarse
    "portal_abre": (1,),         # crece y se mantiene cinco segundos
    "has_muerto": (3,),          # el golpe mas fuerte y con mas graves, que se apaga despacio
    "jefe_abatido": (1,),        # el mas largo (3,5 s de cola) y con mas cuerpo
    "zona_descubierta": (3,),    # una campanada clara al principio que se va apagando
    "ecos_recuperados": (4,),    # soplo y campanilla, la que mas se oye
    "ecos_ganados": (2, 4),      # dos soplos cortos (las otras dos casi no suenan)
    "espada_luz": (2,),          # el brillo mas parejo de principio a fin
    "guardia_rota": (1, 3),      # golpe y quejido / golpe seco con graves
    "santuario_enciende": (1,),  # el soplo mas largo y con mas graves
    "santuario_fuego": (3,),     # el crepitar mas parejo
    "santuario_descanso": (3,),  # la que mas cuerpo tiene
    "frasco_beber": (2, 1),      # corcho y dos tragos
    "nivel_sube": (2,),          # la mas llena y larga
    "menu_abre": (3,),           # la losa mas grave
    "menu_mueve": (1,), "menu_acepta": (4,), "menu_cancela": (3,),
    "golpe_ligero": (1, 2, 3, 4),  # cuatro para que una cadena de golpes no suene siempre igual
    "golpe_pesado": (2, 4),      # la mas fuerte y la mas grave
    "carga_golpe": (1, 3),       # las que crecen con graves
    "impacto_carne": (1, 2, 3),  # la 4 casi no suena
    "impacto_armadura": (1, 3, 4),
    "rodar": (2, 4),             # las que se oyen enteras
    "fijar": (3,),               # clic con cuerpo, sin tanto grave
    "estocada": (1, 3, 4),       # la 2 casi no suena
    "golpe_salto": (2, 4),       # las mas largas y con mas cuerpo
    "aturdido": (1, 4),          # las mas cortas (las otras dos llevan otro golpe detras)
    "esfuerzo": (1, 2, 3, 4),
    "comer_fruto": (1, 2),
    "talisman": (2, 3),
    "hechizo_lanzar": (4, 1),
    "hechizo_impacto": (2, 1),
    "hechizo_rayo": (1, 4),
    "hechizo_llama": (4, 1),
    "hechizo_curar": (3, 2),
    "hechizo_bendicion": (4, 3),
    "herrero_golpe": (4, 1, 3),  # tres martillazos distintos (la 2 suena mas fina)
    "forja_mejora": (1, 4),      # el golpe y el brillo que se sostiene (la 1, la mas llena)
    "tienda_compra": (2, 4),     # monedas: las dos que mas se oyen
    "habilidad_torbellino": (1, 4),  # los dos silbidos mas fuertes
    "habilidad_estocada": (1, 3),    # las mas llenas
    "habilidad_grito": (4, 2),       # el rugido mas fuerte y largo
    "habilidad_sismo": (1, 4),       # las de mas graves
    "habilidad_paso": (3, 4),        # las que se oyen (la 1 y la 2 casi no suenan)
    "habilidad_llamas": (4, 2),
    "habilidad_escarcha": (1, 2),
    "habilidad_relampago": (3, 1),
    "habilidad_sangre": (1, 4),
    "habilidad_parada": (2, 3),      # el choque mas claro
    "ceniza_aplicar": (1, 4),        # la mas llena (la 2 casi no suena)
    "codice_abre": (2, 1),           # las dos que se oyen (la 3 y la 4 casi no suenan)
    "codice_pagina": (1, 3),         # la hoja que cae clara
    "codice_cierra": (4, 1),         # el golpe de la tapa con mas graves
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


def habla(f):
    """(silencio del principio, segundo en que acaba de hablar, duracion) de un audio original."""
    r = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", f, "-af", "silencedetect=noise=-38dB:d=0.10",
                        "-f", "null", "-"], capture_output=True, text=True)
    dur = duracion(f)
    ini = [float(x) for x in re.findall(r"silence_start: (-?[0-9.]+)", r.stderr)]
    fin = [float(x) for x in re.findall(r"silence_end: ([0-9.]+)", r.stderr)]
    principio = fin[0] if ini and ini[0] <= 0.01 and fin else 0.0
    final = dur
    if ini and (len(fin) < len(ini) or fin[-1] >= dur - 0.06):
        final = max(principio + 0.2, ini[-1])
    return principio, final, dur


def recorte(src):
    """El filtro que quita el silencio del principio, lo que quita (s) y lo que queda hablado (s)."""
    principio, final, _ = habla(src)
    r = max(0.0, principio - 0.03)
    return (f"atrim=start={r:.3f},asetpts=PTS-STARTPTS," if r > 0.01 else ""), r, final - r


# lo que estira la voz cada efecto (asetrate + atempo) y la cola de eco que aun se oye al acabar de hablar
ESTIRA_MONSTRUO = 1 / (0.82 * 1.10)
ESTIRA_ARBOL = 1 / (0.88 * 1.0606)
COLA = 0.10


def voz_normal(src, dst):
    pre, _, hablado = recorte(src)
    ff(["-i", src, "-af", pre + VOZ, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_monstruo(src, dst, fuego, desfase):
    pre, quita, hablado = recorte(src)
    d = (duracion(src) - quita) * 1.12 + 0.9
    grafo = (
        "[0:a]" + pre + "asplit=2[v1][v2];"
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
    return hablado * ESTIRA_MONSTRUO + COLA


def voz_gavran(src, dst):
    pre, _, hablado = recorte(src)
    ff(["-i", src, "-af", pre + VOZ_GAVRAN, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
    return hablado + COLA


def voz_arbol(src, dst, madera, desfase):
    """La voz de la fase 2: mas grave, una segunda voz por debajo y madera crujiendo."""
    pre, quita, hablado = recorte(src)
    d = (duracion(src) - quita) * 1.08 + 1.0
    grafo = (
        "[0:a]" + pre + "asplit=2[v1][v2];"
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
    return hablado * ESTIRA_ARBOL + COLA


def efecto(src, dst, maxs, fade, lufs, entrada=0.0):
    filtro = (f"silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.02,atrim=0:{maxs},"
              + (f"afade=t=in:st=0:d={entrada}," if entrada > 0 else "")
              + f"afade=t=out:st={max(0.05, maxs - fade):.2f}:d={fade},"
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
    dur, hab, fallos = {}, {}, 0
    for n, toma in sorted(TOMAS.items()):
        src = os.path.join(MASTER, "rey", f"voz_{n}{toma}.mp3")
        dst = os.path.join(rey, f"voz_{n}.ogg")
        try:
            if n in MONSTRUO and os.path.exists(fuego):
                h = voz_monstruo(src, dst, fuego, 1.5 + (n * 2.3) % 20)
            else:
                h = voz_normal(src, dst)
            dur[n] = round(duracion(dst), 2)
            hab[n] = round(min(h, dur[n]), 2)
            print(f"voz {n:2d}{' (monstruo)' if n in MONSTRUO else ''}: {dur[n]} s (habla hasta {hab[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO voz {n}: {e}")
    # Ser Gavran
    gav = os.path.join(ASSETS, "sounds", "gavran")
    os.makedirs(gav, exist_ok=True)
    madera = os.path.join(MASTER, "sfx", "madera_cruje_1.mp3")
    dur_g, hab_g = {}, {}
    for n, toma in sorted(TOMAS_GAVRAN.items()):
        src = os.path.join(MASTER, "gavran", f"voz_{n}{toma}.mp3")
        dst = os.path.join(gav, f"voz_{n}.ogg")
        if not os.path.exists(src):
            print("falta", src)
            fallos += 1
            continue
        try:
            if n in ARBOL and os.path.exists(madera):
                h = voz_arbol(src, dst, madera, (n * 0.7) % 2.5)
            else:
                h = voz_gavran(src, dst)
            dur_g[n] = round(duracion(dst), 2)
            hab_g[n] = round(min(h, dur_g[n]), 2)
            print(f"gavran {n:2d}{' (arbol)' if n in ARBOL else ''}: {dur_g[n]} s (habla hasta {hab_g[n]})")
        except Exception as e:  # noqa: BLE001
            fallos += 1
            print(f"FALLO gavran {n}: {e}")
    with open(os.path.join(ASSETS, "rey_voces.json"), "w") as fh:
        json.dump({"duraciones": dur, "gavran": dur_g, "habla": hab, "habla_gavran": hab_g}, fh)
    # los personajes con dialogo: voz_N.ogg y lo que tardan en decir cada frase (voces.json, para que el texto del
    # dialogo se escriba a su ritmo)
    voces = {}
    for pj, (tomas, filtro) in PERSONAJES.items():
        voces[pj] = {}
        carpeta = os.path.join(ASSETS, "sounds", pj)
        os.makedirs(carpeta, exist_ok=True)
        for n, toma in sorted(tomas.items()):
            src = os.path.join(MASTER, pj, f"voz_{n}{toma}.mp3")
            dst = os.path.join(carpeta, f"voz_{n}.ogg")
            if not os.path.exists(src):
                print("falta", src)
                fallos += 1
                continue
            try:
                pre, _, hablado = recorte(src)
                ff(["-i", src, "-af", pre + filtro, "-ar", "44100", "-ac", "1", "-c:a", "libvorbis", "-q:a", "6", dst])
                voces[pj][str(n)] = round(min(hablado + COLA, duracion(dst)), 2)
                print(f"{pj} {n:2d}: {duracion(dst):.2f} s (habla hasta {voces[pj][str(n)]})")
            except Exception as e:  # noqa: BLE001
                fallos += 1
                print(f"FALLO {pj} {n}: {e}")
    with open(os.path.join(ASSETS, "voces.json"), "w") as fh:
        json.dump(voces, fh)
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
                efecto(src, dst, maxs, fade, lufs, ENTRADA.get(nombre, 0.0))
                print(f"efecto {nombre}_{i} (toma {k}): {duracion(dst):.2f} s")
            except Exception as e:  # noqa: BLE001
                fallos += 1
                print(f"FALLO efecto {nombre}_{k}: {e}")
    print("OK" if not fallos else f"FALLOS: {fallos}")
    return 1 if fallos else 0


if __name__ == "__main__":
    sys.exit(main())
