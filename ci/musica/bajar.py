#!/usr/bin/env python3
"""Baja la musica (licencias libres CC0 / CC-BY, ver pistas.json) de OpenGameArt, la pasa a OGG
normalizada y escribe los creditos. Solo se usa en la CI; si algo falla el mod funciona sin esa pista.

Uso: bajar.py <carpeta de cache> <carpeta de sonidos del mod> <carpeta de salida (informes)>
"""
import html
import json
import os
import re
import subprocess
import sys
import urllib.parse
import urllib.request
import zipfile

AQUI = os.path.dirname(os.path.abspath(__file__))
UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
AUDIO = (".ogg", ".flac", ".wav", ".mp3")


def get(url, binary=False):
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "*/*"})
    with urllib.request.urlopen(req, timeout=120) as r:
        data = r.read()
    return data if binary else data.decode("utf-8", "replace")


def enlaces(pagina):
    txt = get(pagina)
    out = []
    for m in re.finditer(r'href="([^"]*?/sites/default/files/[^"]+)"', txt):
        u = html.unescape(m.group(1))
        if u.startswith("/"):
            u = "https://opengameart.org" + u
        nombre = urllib.parse.unquote(u.rsplit("/", 1)[-1])
        low = nombre.lower()
        if low.endswith(AUDIO) or low.endswith(".zip"):
            if u not in [o[0] for o in out]:
                out.append((u, nombre))
    return out


def elegir(lista, busca):
    cand = [x for x in lista if busca.lower() in x[1].lower()] or lista
    orden = {".ogg": 0, ".flac": 1, ".wav": 2, ".mp3": 3, ".zip": 4}
    cand.sort(key=lambda x: orden.get(os.path.splitext(x[1].lower())[1], 9))
    return cand[0] if cand else None


def de_zip(ruta, destino, log, prefiere=()):
    with zipfile.ZipFile(ruta) as z:
        miembros = [i for i in z.infolist() if i.filename.lower().endswith(AUDIO) and not i.filename.startswith("__MACOSX")]
        for i in miembros:
            log.write(f"    zip: {i.filename} ({i.file_size // 1024} KB)\n")
        if not miembros:
            return None
        # un bucle si lo hay; si no, el archivo mas grande (la version completa)
        bucles = [i for i in miembros if "loop" in i.filename.lower()]
        orden = {".ogg": 0, ".flac": 1, ".wav": 2, ".mp3": 3}
        pool = bucles or miembros
        # "zip_prefiere" de la pista: si alguno lleva en el nombre una de esas palabras (la version sin voces...), entre esos
        if prefiere:
            def casa(i):
                n = os.path.basename(i.filename).lower().replace("_", " ").replace("-", " ")
                return any(p.lower() in n for p in prefiere)
            elegidos = [i for i in pool if casa(i)] or [i for i in miembros if casa(i)]
            if elegidos:
                pool = elegidos
            else:
                log.write(f"    ninguno lleva en el nombre {list(prefiere)}: se elige como siempre\n")
        pool.sort(key=lambda i: (orden.get(os.path.splitext(i.filename.lower())[1], 9), -i.file_size))
        # entre formatos, quedarse con el mas grande del mejor formato disponible
        mejor = pool[0]
        salida = os.path.join(destino, "zip_" + os.path.basename(mejor.filename).replace(" ", "_"))
        with z.open(mejor) as src, open(salida, "wb") as dst:
            dst.write(src.read())
        log.write(f"    elegido del zip: {mejor.filename}\n")
        return salida


def duracion(ruta):
    r = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", ruta],
                       capture_output=True, text=True)
    try:
        return round(float(r.stdout.strip()), 2)
    except ValueError:
        return 0.0


def convertir(origen, destino, lufs):
    # quitar silencio al principio y al final, volumen igualado, 44,1 kHz estereo
    filtro = ("silenceremove=start_periods=1:start_threshold=-55dB,areverse,"
              "silenceremove=start_periods=1:start_threshold=-55dB,areverse,"
              f"loudnorm=I={lufs}:TP=-1.5:LRA=11")
    cmd = ["ffmpeg", "-y", "-v", "error", "-i", origen, "-vn", "-af", filtro, "-ar", "44100", "-ac", "2",
           "-c:a", "libvorbis", "-q:a", "3", destino]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.returncode == 0, r.stderr[-800:]


def main():
    cache, sonidos, salida = sys.argv[1], sys.argv[2], sys.argv[3]
    os.makedirs(cache, exist_ok=True)
    os.makedirs(os.path.join(sonidos, "musica"), exist_ok=True)
    os.makedirs(salida, exist_ok=True)
    pistas = json.load(open(os.path.join(AQUI, "pistas.json"), encoding="utf-8"))
    info = {}
    creditos = ["MÚSICA DE DARK CRAFT", "",
                "Todas las pistas tienen licencia libre. Se han pasado a OGG, se ha quitado el silencio",
                "del principio y del final y se ha igualado el volumen (sin otros cambios).", ""]
    with open(os.path.join(salida, "musica.log"), "w", encoding="utf-8") as log:
        for p in pistas:
            pid = p["id"]
            ogg = os.path.join(sonidos, "musica", pid + ".ogg")
            log.write(f"== {pid}: {p['titulo']} - {p['autor']} ({p['licencia']})\n")
            try:
                bruto = None
                for f in os.listdir(cache):
                    if f.startswith(pid + "__"):
                        bruto = os.path.join(cache, f)
                if bruto is None:
                    lista = []
                    for pag in [p["pagina"], p.get("pagina2")]:
                        if not pag:
                            continue
                        try:
                            lista = enlaces(pag)
                        except Exception as ex:  # noqa: BLE001
                            log.write(f"  pagina {pag}: {ex}\n")
                        if lista:
                            break
                    for u, n in lista:
                        log.write(f"  archivo en la pagina: {n}\n")
                    e = elegir(lista, p["busca"])
                    if e is None:
                        raise RuntimeError("no hay archivos de audio en la pagina")
                    u, n = e
                    log.write(f"  bajando {u}\n")
                    datos = get(u, binary=True)
                    bruto = os.path.join(cache, pid + "__" + re.sub(r"[^A-Za-z0-9._-]", "_", n))
                    with open(bruto, "wb") as fh:
                        fh.write(datos)
                log.write(f"  original: {os.path.basename(bruto)} ({os.path.getsize(bruto) // 1024} KB)\n")
                fuente = bruto
                if bruto.lower().endswith(".zip"):
                    fuente = de_zip(bruto, cache, log, p.get("zip_prefiere", ()))
                    if fuente is None:
                        raise RuntimeError("el zip no trae audio")
                tmp = ogg + ".tmp.ogg"
                ok, err = convertir(fuente, tmp, p.get("lufs", -18))
                if not ok:
                    raise RuntimeError("ffmpeg: " + err)
                os.replace(tmp, ogg)
                d = duracion(ogg)
                info[pid] = {"duracion": d, "titulo": p["titulo"], "autor": p["autor"]}
                log.write(f"  OK {ogg} {os.path.getsize(ogg) // 1024} KB, {d} s\n")
            except Exception as ex:  # noqa: BLE001
                log.write(f"  FALLO: {ex}\n")
                if os.path.exists(ogg):
                    # se queda la que ya habia (guardada en el repositorio)
                    info[pid] = {"duracion": duracion(ogg), "titulo": p["titulo"], "autor": p["autor"]}
                    log.write("  se usa la copia que ya habia\n")
            creditos += [f"- \"{p['titulo']}\" de {p['autor']}", f"  Licencia {p['licencia']} — {p['pagina']}",
                         f"  Aviso del autor: {p['aviso']}", f"  Se usa para: {p['uso']}", ""]
        log.write("RESUMEN: " + json.dumps(info, ensure_ascii=False) + "\n")
    creditos += ["Voz del narrador: Piper TTS, voz \"alan\" (en_GB), licencia libre del proyecto Piper.",
                 "Campana fúnebre, efectos y texturas: hechos para este mod.",
                 "Letra gótica de los títulos: mapas de bits derivados de la fuente \"New Rocker\" (Pablo Impallari, Brenda Gallo y",
                 "  Rodrigo Fuenzalida), SIL Open Font License 1.1 (el texto de la licencia va dentro del .jar, en licencias/)."]
    texto = "\n".join(creditos) + "\n"
    base = os.path.dirname(sonidos.rstrip("/"))
    with open(os.path.join(base, "creditos.txt"), "w", encoding="utf-8") as fh:
        fh.write(texto)
    with open(os.path.join(salida, "creditos.txt"), "w", encoding="utf-8") as fh:
        fh.write(texto)
    with open(os.path.join(base, "musica.json"), "w", encoding="utf-8") as fh:
        json.dump(info, fh, ensure_ascii=False)


if __name__ == "__main__":
    main()
