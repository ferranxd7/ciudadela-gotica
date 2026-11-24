#!/usr/bin/env python3
"""
El santuario: la hoguera de almas donde se descansa (un brasero de piedra y hueso con fuego azul palido). Es un bloque
con modelo propio (nada que ver con la fogata de Minecraft). Aqui salen sus texturas en pixel art y sus modelos:

  assets/darkcraft/textures/block/santuario_piedra.png    piedra oscura labrada (el pie y el cuenco)
  assets/darkcraft/textures/block/santuario_runas.png     la banda de runas del fuste, apagada
  assets/darkcraft/textures/block/santuario_runas_luz.png la misma con las runas encendidas (animada: laten)
  assets/darkcraft/textures/block/santuario_hueso.png     las cuatro garras de hueso
  assets/darkcraft/textures/block/santuario_ceniza.png    el fondo del cuenco, apagado
  assets/darkcraft/textures/block/santuario_brasa.png     el fondo del cuenco, encendido (animada)
  assets/darkcraft/textures/block/santuario_llama.png     la llama de almas (animada, 12 fotogramas; 16 x 20 dentro de 16 x 32)
  assets/darkcraft/textures/block/santuario_llama_dentro.png  la llama pequena de dentro (a otro compas)
  assets/darkcraft/models/block/santuario.json, santuario_encendido.json, models/item/santuario.json
  assets/darkcraft/blockstates/santuario.json

    python3 tools/santuario/modelo.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, HUESO, PIEDRA, TINTA, Lienzo, rgba  # noqa: E402

TEX = os.path.join(ASSETS, "textures", "block")
AZULES = ["#0c1c48", "#16388c", "#2a66cc", "#58a8ec", "#9adcf8", "#d8f6ff", "#ffffff"]


def meta(ruta, frametime, ancho=None, alto=None, interpolar=False):
    """El .mcmeta de una textura animada. Si los fotogramas no son cuadrados hay que decir su tamano (ancho, alto)."""
    d = {"animation": {"frametime": frametime}}
    if interpolar:
        d["animation"]["interpolate"] = True
    if ancho:
        d["animation"]["width"] = ancho
        d["animation"]["height"] = alto
    with open(ruta + ".mcmeta", "w", encoding="utf-8") as fh:
        json.dump(d, fh)


# ---------------------------------------------------------------- texturas
def piedra_labrada():
    """Sillares oscuros: dos hiladas con juntas, grano y alguna muesca."""
    r = np.random.default_rng(301)
    L = Lienzo(16, 16, PIEDRA[3])
    for y in range(16):
        for x in range(16):
            v = r.random()
            L.px(x, y, PIEDRA[4] if v < 0.16 else PIEDRA[2] if v > 0.86 else PIEDRA[3])
    # juntas: horizontales en 0 y 8; verticales alternadas
    for x in range(16):
        L.px(x, 0, PIEDRA[1]).px(x, 8, PIEDRA[1])
        L.px(x, 1, PIEDRA[5] if x % 5 else PIEDRA[4]).px(x, 9, PIEDRA[5] if x % 4 else PIEDRA[4])
        L.px(x, 7, PIEDRA[2]).px(x, 15, PIEDRA[2])
    for y in range(1, 8):
        L.px(5, y, PIEDRA[1]).px(13, y, PIEDRA[1])
    for y in range(9, 16):
        L.px(1, y, PIEDRA[1]).px(9, y, PIEDRA[1])
    for (x, y) in ((3, 4), (10, 3), (6, 12), (12, 13), (14, 5)):
        L.px(x, y, PIEDRA[1])
    return L


RUNAS = [
    "................",
    "................",
    ".#..#.##..#..#..",
    ".#..#.#.#.#.#...",
    ".####.##..##....",
    "....#.#.#.#.#...",
    "....#.##..#..#..",
    "................",
    "................",
    "..#..###.#...#..",
    ".#.#..#..##.##..",
    ".###..#..#.#.#..",
    ".#.#..#..#...#..",
    ".#.#..#..#...#..",
    "................",
    "................",
]


def runas(luz=0.0):
    """La banda de runas: apagadas (hundidas en la piedra) o encendidas con un brillo azul (luz 0..1)."""
    L = piedra_labrada()
    for y, fila in enumerate(RUNAS):
        for x, ch in enumerate(fila):
            if ch != "#":
                continue
            if luz <= 0:
                L.px(x, y, PIEDRA[0])
            else:
                i = 3 + int(round(luz * 2))
                L.px(x, y, AZULES[i])
    return L


def runas_animadas():
    """Cuatro fotogramas: las runas laten."""
    hoja = Lienzo(16, 16 * 4)
    for i, v in enumerate((0.35, 0.7, 1.0, 0.7)):
        hoja.pegar(runas(v), 0, 16 * i)
    return hoja


def hueso():
    r = np.random.default_rng(302)
    L = Lienzo(16, 16, HUESO[3])
    for y in range(16):
        for x in range(16):
            v = r.random()
            L.px(x, y, HUESO[4] if v < 0.22 else HUESO[2] if v > 0.84 else HUESO[3])
    # vetas a lo largo y nudos
    for y in range(16):
        L.px(0, y, HUESO[1]).px(15, y, HUESO[2])
        if y % 5 == 2:
            for x in range(16):
                L.px(x, y, HUESO[2] if x % 3 else HUESO[1])
    for (x, y) in ((4, 6), (11, 11), (7, 14), (9, 1)):
        L.px(x, y, HUESO[0])
    return L


def ceniza():
    r = np.random.default_rng(303)
    L = Lienzo(16, 16, "#131118")
    for y in range(16):
        for x in range(16):
            v = r.random()
            L.px(x, y, "#1c1922" if v < 0.3 else "#0b0a10" if v > 0.8 else "#131118")
    for (x, y) in ((4, 4), (9, 6), (6, 10), (11, 11), (7, 7)):
        L.px(x, y, "#2c2834")
    return L


def brasa():
    """Brasas de almas: cuatro fotogramas en los que los rescoldos azules se encienden y se apagan."""
    hoja = Lienzo(16, 16 * 4)
    r = np.random.default_rng(304)
    puntos = [(int(r.integers(1, 15)), int(r.integers(1, 15)), float(r.random())) for _ in range(46)]
    for f in range(4):
        L = ceniza()
        for (x, y, fase) in puntos:
            v = 0.5 + 0.5 * np.sin(2 * np.pi * (f / 4 + fase))
            d = np.hypot(x - 7.5, y - 7.5) / 8.0
            i = int(round((1 - d) * 3 + v * 2.5))
            if i >= 1:
                L.px(x, y, AZULES[min(6, i)])
        hoja.pegar(L, 0, 16 * f)
    return hoja


# La llama mide 16 x 20 pixeles, pero cada fotograma es de 16 x 32 (la llama, en las 20 filas de abajo): con una textura
# que no mida una potencia de dos, Minecraft baja el suavizado a distancia (mipmaps) de TODOS los bloques del juego.
LLAMA_W, LLAMA_H, LLAMA_N, LLAMA_CUADRO = 16, 20, 12, 32


def llama(alto=(18.6, 14.6, 10.4, 5.8), base=(6.6, 5.0, 3.3, 1.7), fase0=0.0, chispas=((-3.0, 0.00), (2.5, 0.37), (-1.0, 0.68), (4.0, 0.52)),
          n=LLAMA_N, w=LLAMA_W, h=LLAMA_H):
    """La llama de almas, dibujada por capas como las de los juegos de 16 bits: cuatro lenguas una dentro de otra (de
    azul oscuro por fuera a blanco en el corazon) que se mecen y laten, y unas chispas que se sueltan por arriba.
    Sale una tira de n fotogramas de w x h pixeles que empalma el ultimo con el primero. En el modelo un pixel de la
    llama mide lo mismo que uno de la piedra (el plano mide 16 x 20 de los 16 del bloque)."""
    color = [AZULES[1], AZULES[2], AZULES[4], AZULES[6]]
    hoja = Lienzo(w, h * n)
    cx = (w - 1) / 2.0
    for f in range(n):
        fase = (f / n + fase0) % 1.0
        for yy in range(h):                # yy = 0 abajo
            for x in range(w):
                capa = -1
                for k in range(4):
                    H = alto[k] * (1 + 0.07 * np.sin(2 * np.pi * (fase + k * 0.21)))
                    if yy >= H:
                        continue
                    t = yy / H
                    # se mece mas cuanto mas arriba; cada capa un poco menos que la de fuera
                    mece = np.sin(2 * np.pi * (fase + yy / 15.0)) * t * (2.3 - 0.45 * k) * (alto[0] / 22.0 + 0.15)
                    ancho = base[k] * (1 - t) ** 0.72 * (1 + 0.16 * np.sin(2 * np.pi * (2 * fase + yy / 6.5 + k * 0.3)))
                    if abs(x - cx - mece) <= ancho:
                        capa = k
                if capa >= 0:
                    hoja.px(x, f * h + (h - 1 - yy), color[capa])
        # el borde de la capa de fuera, un tono mas oscuro (da contorno sin linea negra)
        cuadro = hoja.a[f * h:(f + 1) * h]
        lleno = cuadro[..., 3] > 0
        for yy in range(h):
            for x in range(w):
                if not lleno[yy, x]:
                    continue
                vecinos = [(yy - 1, x), (yy, x - 1), (yy, x + 1)]
                if any(not (0 <= a < h and 0 <= b < w) or not lleno[a, b] for a, b in vecinos) and tuple(cuadro[yy, x][:3]) == rgba(AZULES[1])[:3]:
                    cuadro[yy, x] = rgba(AZULES[0], 240)
        # chispas que suben y se apagan
        for (dx, des) in chispas:
            v = (fase + des) % 1.0
            y = alto[0] * 0.55 + v * (h - 1 - alto[0] * 0.55)
            x = cx + dx + np.sin(2 * np.pi * (v + des)) * 1.5
            if v < 0.85 and 0 <= int(round(x)) < w:
                hoja.px(int(round(x)), f * h + (h - 1 - int(round(y))), AZULES[5] if v < 0.45 else AZULES[3])
    return hoja


def en_cuadros(tira):
    """La tira de fotogramas de 16 x 20 pasada a fotogramas de 16 x 32 (la llama abajo, lo de arriba vacio)."""
    hoja = Lienzo(LLAMA_W, LLAMA_CUADRO * LLAMA_N)
    for f in range(LLAMA_N):
        hoja.pegar(tira.recorte(0, f * LLAMA_H, LLAMA_W, LLAMA_H), 0, f * LLAMA_CUADRO + (LLAMA_CUADRO - LLAMA_H))
    return hoja


def llama_dentro():
    """La llama de dentro (los dos planos en cruz): mas baja y estrecha, y a otro compas, para que el fuego tenga cuerpo."""
    return llama(alto=(13.4, 10.4, 7.4, 4.2), base=(5.2, 3.9, 2.6, 1.3), fase0=0.37, chispas=((1.5, 0.2), (-2.5, 0.75)))


# ---------------------------------------------------------------- modelos
def caja(desde, hasta, tex, uv=None, sombra=True, caras="nsewud", luz=False):
    f = {}
    nombres = {"n": "north", "s": "south", "e": "east", "w": "west", "u": "up", "d": "down"}
    for c in caras:
        cara = {"texture": "#" + (tex[c] if isinstance(tex, dict) else tex)}
        if uv and c in uv:
            cara["uv"] = uv[c]
        f[nombres[c]] = cara
    e = {"from": list(desde), "to": list(hasta), "faces": f}
    if not sombra:
        e["shade"] = False
    if luz:
        e["forge_data"] = {"block_light": 15, "sky_light": 15}
    return e


def garra(sx, sz):
    """Una garra de hueso en la esquina (sx, sz = -1 o 1): tres tramos que suben y se cierran hacia dentro."""
    def tramo(a, b, y0, y1):
        # a, b: distancia al centro (8) por fuera y por dentro
        x0, x1 = sorted((8 + sx * a, 8 + sx * b))
        z0, z1 = sorted((8 + sz * a, 8 + sz * b))
        return caja((x0, y0, z0), (x1, y1, z1), "hueso")
    return [tramo(7.5, 5.5, 2, 8.5), tramo(7.0, 5.0, 8.5, 13.5), tramo(6.0, 4.5, 13.5, 16.5), tramo(5.0, 4.0, 16.5, 18.5)]


def elementos(encendido):
    banda = "runas_luz" if encendido else "runas"
    e = [
        caja((1, 0, 1), (15, 2, 15), "piedra"),                         # zocalo
        caja((2.5, 2, 2.5), (13.5, 4, 13.5), "piedra"),                 # segundo escalon
        caja((5, 4, 5), (11, 9, 11), {"n": banda, "s": banda, "e": banda, "w": banda, "u": "piedra", "d": "piedra"},
             uv={c: [2, 1, 14, 9] for c in "nsew"}, luz=encendido),    # el fuste con su banda de runas
        caja((3, 9, 3), (13, 11, 13), "piedra"),                        # el cuenco
        caja((2, 11, 2), (14, 12.5, 14), {"n": "hueso", "s": "hueso", "e": "hueso", "w": "hueso", "u": "piedra", "d": "piedra"}),
        caja((3.5, 12.5, 3.5), (12.5, 12.6, 12.5), "brasa" if encendido else "ceniza", caras="u", sombra=False,
             uv={"u": [0, 0, 16, 16]}, luz=encendido),                 # el fondo del cuenco
    ]
    for sx in (-1, 1):
        for sz in (-1, 1):
            e += garra(sx, sz)
    if encendido:
        # la llama: dos planos cruzados en diagonal y, por dentro, otros dos en cruz con la llama pequena
        v0 = 16.0 * (LLAMA_CUADRO - LLAMA_H) / LLAMA_CUADRO      # donde empieza la llama dentro de su fotograma
        for ang in (45, -45):
            p = caja((0, 12, 8), (16, 32, 8), "llama", caras="ns", sombra=False, uv={"n": [0, v0, 16, 16], "s": [0, v0, 16, 16]}, luz=True)
            p["rotation"] = {"angle": ang, "axis": "y", "origin": [8, 8, 8]}
            e.append(p)
        e.append(caja((0, 12, 8), (16, 32, 8), "llama2", caras="ns", sombra=False, uv={"n": [16, v0, 0, 16], "s": [16, v0, 0, 16]}, luz=True))
        e.append(caja((8, 12, 0), (8, 32, 16), "llama2", caras="ew", sombra=False, uv={"e": [16, v0, 0, 16], "w": [16, v0, 0, 16]}, luz=True))
    return e


def modelo(encendido):
    t = {"piedra": "darkcraft:block/santuario_piedra", "hueso": "darkcraft:block/santuario_hueso",
         "runas": "darkcraft:block/santuario_runas", "ceniza": "darkcraft:block/santuario_ceniza",
         "particle": "darkcraft:block/santuario_piedra"}
    if encendido:
        t.update({"runas_luz": "darkcraft:block/santuario_runas_luz", "brasa": "darkcraft:block/santuario_brasa",
                  "llama": "darkcraft:block/santuario_llama", "llama2": "darkcraft:block/santuario_llama_dentro"})
    m = {"credit": "Dark Craft: el santuario (tools/santuario/modelo.py)", "render_type": "minecraft:cutout", "ambientocclusion": False,
         "textures": t, "elements": elementos(encendido)}
    if not encendido:
        m["display"] = {
            "gui": {"rotation": [30, 225, 0], "translation": [0, -1.5, 0], "scale": [0.56, 0.56, 0.56]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.3, 0.3, 0.3]},
            "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
        }
    return m


def generar():
    os.makedirs(TEX, exist_ok=True)
    piedra_labrada().guardar(os.path.join(TEX, "santuario_piedra.png"))
    runas(0).guardar(os.path.join(TEX, "santuario_runas.png"))
    runas_animadas().guardar(os.path.join(TEX, "santuario_runas_luz.png"))
    meta(os.path.join(TEX, "santuario_runas_luz.png"), 12, interpolar=True)
    hueso().guardar(os.path.join(TEX, "santuario_hueso.png"))
    ceniza().guardar(os.path.join(TEX, "santuario_ceniza.png"))
    brasa().guardar(os.path.join(TEX, "santuario_brasa.png"))
    meta(os.path.join(TEX, "santuario_brasa.png"), 6, interpolar=True)
    ll = llama()
    for nombre, tira in (("santuario_llama", ll), ("santuario_llama_dentro", llama_dentro())):
        en_cuadros(tira).guardar(os.path.join(TEX, nombre + ".png"))
        meta(os.path.join(TEX, nombre + ".png"), 2, LLAMA_W, LLAMA_CUADRO)
    for nombre, enc in (("santuario", False), ("santuario_encendido", True)):
        with open(os.path.join(ASSETS, "models", "block", nombre + ".json"), "w", encoding="utf-8") as fh:
            json.dump(modelo(enc), fh, indent=1)
    with open(os.path.join(ASSETS, "models", "item", "santuario.json"), "w", encoding="utf-8") as fh:
        json.dump({"parent": "darkcraft:block/santuario"}, fh)
    with open(os.path.join(ASSETS, "blockstates", "santuario.json"), "w", encoding="utf-8") as fh:
        json.dump({"variants": {"encendido=false": {"model": "darkcraft:block/santuario"},
                                "encendido=true": {"model": "darkcraft:block/santuario_encendido"}}}, fh, indent=1)
    print("santuario: texturas y modelos (", len(elementos(True)), "cajas encendido )")
    return ll


def vista(carpeta, ll):
    """El santuario apagado y encendido de frente (sin perspectiva, x10) y las tiras de las dos llamas."""
    from PIL import Image
    S = 10
    H = LLAMA_H
    T = {"piedra": piedra_labrada(), "hueso": hueso(), "runas": runas(0), "runas_luz": runas(1.0), "ceniza": ceniza(),
         "brasa": brasa().recorte(0, 0, 16, 16)}
    dentro = llama_dentro()
    hoja = Image.new("RGBA", (16 * S * 2 + 60, 34 * S + 20), (40, 46, 60, 255))
    for k, enc in enumerate((False, True)):
        ox = 20 + k * (16 * S + 20)
        els = [e for e in elementos(enc) if "rotation" not in e and e["faces"][next(iter(e["faces"]))]["texture"][1:] in T]
        for e in sorted(els, key=lambda e: e["to"][2]):
            x0, y0, z0 = e["from"]; x1, y1, z1 = e["to"]
            cara = e["faces"].get("south") or e["faces"].get("up")
            if cara is None or x1 - x0 < 0.01:
                continue
            t = T[cara["texture"][1:]]
            w, h = max(1, int(round((x1 - x0) * S))), max(1, int(round((y1 - y0) * S)))
            uv = cara.get("uv", [x0, 16 - y1 if y1 <= 16 else 0, x1, 16 - y0 if y1 <= 16 else 16])
            u0, v0, u1, v1 = [max(0, min(16, v)) for v in uv]
            im = t.imagen()
            trozo = im.crop((int(min(u0, u1)), int(min(v0, v1)), max(int(min(u0, u1)) + 1, int(max(u0, u1))), max(int(min(v0, v1)) + 1, int(max(v0, v1))))).resize((w, h), 0)
            hoja.paste(trozo, (ox + int(round(x0 * S)), 10 + int(round((34 - y1) * S))), trozo)
        if enc:
            for tira, f in ((ll, 3), (dentro, 3)):
                fl = tira.recorte(0, H * f, 16, H).imagen().resize((16 * S, H * S), 0)
                hoja.paste(fl, (ox, 10 + int(round((34 - 32) * S))), fl)
    os.makedirs(carpeta, exist_ok=True)
    hoja.save(os.path.join(carpeta, "santuario.png"))
    cuadro = Image.new("RGBA", (16 * LLAMA_N * 6, H * 6 * 2 + 6), (20, 22, 30, 255))
    for j, tira in enumerate((ll, dentro)):
        im = tira.imagen()
        for i in range(LLAMA_N):
            f = im.crop((0, H * i, 16, H * (i + 1))).resize((16 * 6, H * 6), 0)
            cuadro.paste(f, (i * 16 * 6, j * (H * 6 + 6)), f)
    cuadro.save(os.path.join(carpeta, "santuario_llama.png"))
    print("vista en", carpeta)


if __name__ == "__main__":
    ll = generar()
    if "--vista" in sys.argv:
        vista(sys.argv[sys.argv.index("--vista") + 1], ll)
