#!/usr/bin/env python3
"""
La estela del mapa de Morvain (parte 7d, respuesta 92 del plan: "fragmentos de mapa"): un obelisco bajo de piedra negra
con el mapa de su region tallado en la cara y un pergamino clavado encima; quien la toca copia ese trozo del mapa. Hay una
por region, junto a uno de sus santuarios (la de las Llanuras, en la plaza del Arco del Umbral). Aqui salen sus texturas
en pixel art y sus modelos:

  assets/darkcraft/textures/block/estela_piedra.png     la piedra (zocalo, remate y lo de detras)
  assets/darkcraft/textures/block/estela_frente.png     la cara: el mapa tallado, con filete de oro
  assets/darkcraft/textures/block/estela_lado.png       los costados: una columna de runas talladas con un hilo azul
  assets/darkcraft/textures/block/estela_pergamino.png  el pergamino clavado (un trozo de mapa con dos clavos rojos)
  assets/darkcraft/models/block/estela_mapa.json, models/item/estela_mapa.json, blockstates/estela_mapa.json

El modelo mira al norte (el pergamino en la cara norte); el estado `facing` lo gira.

    python3 tools/morvain/estela.py [--vista carpeta]
"""
import json
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, HUESO, ORO, PIEDRA, ROJO, TINTA, Lienzo, mezcla  # noqa: E402

TEX = os.path.join(ASSETS, "textures", "block")
AZUL = ["#0c1c48", "#1e4a8c", "#3a7ccc", "#78b8ec", "#c4ecff"]
PAPEL = ["#5a4428", "#7c6038", "#a8885a", "#c8ac7c", "#e0cca0", "#f0e2bc"]


def grano(L, r, colores, n):
    for _ in range(n):
        L.px(int(r.integers(0, L.w)), int(r.integers(0, L.h)), colores[int(r.integers(0, len(colores)))])


# ---------------------------------------------------------------- texturas
def piedra():
    """Piedra negra azulada, sillares grandes y gastados, con algo de liquen gris."""
    r = np.random.default_rng(7101)
    L = Lienzo(16, 16, PIEDRA[3])
    for y in range(16):
        for x in range(16):
            v = r.random()
            L.px(x, y, PIEDRA[4] if v < 0.22 else PIEDRA[2] if v > 0.86 else PIEDRA[3])
    # juntas: dos hiladas, la de abajo desplazada
    L.hline(0, 7, 16, PIEDRA[1]).hline(0, 15, 16, PIEDRA[1])
    L.vline(5, 0, 7, PIEDRA[1]).vline(12, 8, 7, PIEDRA[1])
    # filo de luz encima de cada junta
    L.hline(0, 8, 16, PIEDRA[5]).hline(0, 0, 16, PIEDRA[5])
    L.px(6, 1, PIEDRA[5]).px(13, 9, PIEDRA[5])
    # muescas y liquen
    for (x, y) in ((2, 3), (9, 5), (14, 2), (3, 11), (8, 13)):
        L.px(x, y, PIEDRA[1])
    grano(L, r, ["#6a6a62", "#58584e"], 5)
    return L


def frente():
    """La cara de la estela (se usa la franja x 4..11: 8 de ancho y 16 de alto): arriba va el pergamino clavado (filas
    2..8) y abajo, tallado en la piedra, el mapa de la region (costa, rio, un camino a puntos y una cruz roja), todo
    dentro de un filete de oro gastado."""
    L = piedra()
    for y in range(1, 15):
        for x in range(5, 11):
            L.px(x, y, PIEDRA[2])
    L.marco(4, 0, 8, 16, ORO[3])
    for (x, y) in ((4, 0), (11, 0), (4, 15), (11, 15)):
        L.px(x, y, ORO[5])
    L.px(4, 7, ORO[2]).px(11, 11, ORO[2]).px(7, 0, ORO[4])
    # el mapa tallado (filas 9..14): la costa con el mar rayado a la izquierda
    costa = [(6, 9), (6, 10), (7, 11), (6, 12), (7, 13), (8, 14)]
    for (x, y) in costa:
        L.px(x, y, PIEDRA[5])
        for xx in range(5, x):
            if (xx + y) % 2 == 0:
                L.px(xx, y, PIEDRA[1])
    for (x, y) in ((10, 9), (9, 10), (9, 11), (8, 12)):
        L.px(x, y, AZUL[1])
    L.px(10, 12, PIEDRA[6]).px(10, 14, PIEDRA[6])
    L.px(9, 13, ROJO[3]).px(10, 13, ROJO[4])
    # una linea tallada bajo el pergamino (como un titulo)
    L.hline(6, 1, 4, PIEDRA[5])
    return L


def lado():
    """Los costados: una columna de runas talladas (x 5..10) con un hilo de luz azul por dentro."""
    L = piedra()
    for y in range(16):
        for x in range(5, 11):
            L.px(x, y, PIEDRA[2])
    L.vline(5, 0, 16, PIEDRA[1]).vline(10, 0, 16, PIEDRA[5])
    runas = [
        ["..x.", ".xx.", "..x.", ".xxx"],
        [".x..", "xxx.", ".x..", ".x.."],
        ["x..x", ".xx.", "x..x", "...."],
        [".xx.", "x...", ".xx.", "...x"],
    ]
    y = 0
    for k, ru in enumerate(runas):
        for j, fila in enumerate(ru):
            for i, ch in enumerate(fila):
                if ch == "x":
                    L.px(6 + i, y + j, AZUL[2] if (i + j + k) % 3 else AZUL[3])
        y += 4
    return L


def pergamino():
    """El pergamino clavado (se usa x 5..10, y 2..8: 6 de ancho y 7 de alto): papel claro con el borde quemado, un trozo
    de mapa a tinta (costa, sierra y el castillo del cielo en rojo) y dos clavos rojos arriba."""
    r = np.random.default_rng(7102)
    L = Lienzo(16, 16)
    x0, y0, w, h = 5, 2, 6, 7
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            borde = min(x - x0, x0 + w - 1 - x, y - y0, y0 + h - 1 - y)
            c = PAPEL[4] if r.random() < 0.7 else PAPEL[3]
            if borde == 0:
                c = PAPEL[1] if r.random() < 0.6 else PAPEL[2]
            L.px(x, y, c)
    L.borrar(x0 + w - 1, y0 + h - 1).px(x0 + w - 2, y0 + h - 1, PAPEL[0])
    for (x, y) in ((6, 4), (6, 5), (7, 6), (6, 7)):
        L.px(x, y, "#4a3420")
    L.px(9, 4, "#5a4430").px(8, 5, "#5a4430")
    L.px(9, 6, ROJO[4])
    L.px(x0 + 1, y0, ROJO[3]).px(x0 + w - 2, y0, ROJO[3])
    return L


# ---------------------------------------------------------------- modelo
def caja(desde, hasta, tex, uv=None, caras="nsewud", sombra=True):
    nombres = {"n": "north", "s": "south", "e": "east", "w": "west", "u": "up", "d": "down"}
    f = {}
    for c in caras:
        cara = {"texture": "#" + (tex[c] if isinstance(tex, dict) else tex)}
        if uv and c in uv:
            cara["uv"] = uv[c]
        if c == "d" and desde[1] == 0:
            cara["cullface"] = "down"
        f[nombres[c]] = cara
    e = {"from": list(desde), "to": list(hasta), "faces": f}
    if not sombra:
        e["shade"] = False
    return e


def elementos():
    fuste = {"n": "frente", "s": "piedra", "e": "lado", "w": "lado", "u": "piedra", "d": "piedra"}
    return [
        caja((2, 0, 2), (14, 2, 14), "piedra"),                                   # zocalo
        caja((3, 2, 3), (13, 3, 13), "piedra"),                                   # escalon
        caja((4, 3, 5), (12, 19, 11), fuste,
             uv={"n": [4, 0, 12, 16], "s": [4, 0, 12, 16], "e": [5, 0, 11, 16], "w": [5, 0, 11, 16]}),  # el fuste
        caja((3, 19, 4), (13, 21, 12), "piedra"),                                 # remate
        caja((5, 21, 6), (11, 23, 10), "piedra"),                                 # la punta
        caja((5, 10, 4.9), (11, 17, 4.9), "pergamino", caras="n", sombra=False,
             uv={"n": [5, 2, 11, 9]}),                                            # el pergamino clavado
    ]


def modelo():
    t = {"piedra": "darkcraft:block/estela_piedra", "frente": "darkcraft:block/estela_frente",
         "lado": "darkcraft:block/estela_lado", "pergamino": "darkcraft:block/estela_pergamino",
         "particle": "darkcraft:block/estela_piedra"}
    return {"credit": "Dark Craft: la estela del mapa (tools/morvain/estela.py)", "render_type": "minecraft:cutout",
            "textures": t, "elements": elementos(),
            "display": {
                "gui": {"rotation": [30, 225, 0], "translation": [0, -2.5, 0], "scale": [0.5, 0.5, 0.5]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.3, 0.3, 0.3]},
                "fixed": {"rotation": [0, 0, 0], "translation": [0, -2, 0], "scale": [0.5, 0.5, 0.5]},
                "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.3, 0.3, 0.3]},
                "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.35, 0.35, 0.35]},
            }}


def generar():
    os.makedirs(TEX, exist_ok=True)
    piedra().guardar(os.path.join(TEX, "estela_piedra.png"))
    frente().guardar(os.path.join(TEX, "estela_frente.png"))
    lado().guardar(os.path.join(TEX, "estela_lado.png"))
    pergamino().guardar(os.path.join(TEX, "estela_pergamino.png"))
    with open(os.path.join(ASSETS, "models", "block", "estela_mapa.json"), "w", encoding="utf-8") as fh:
        json.dump(modelo(), fh, indent=1)
    with open(os.path.join(ASSETS, "models", "item", "estela_mapa.json"), "w", encoding="utf-8") as fh:
        json.dump({"parent": "darkcraft:block/estela_mapa"}, fh)
    giros = {"north": 0, "east": 90, "south": 180, "west": 270}
    with open(os.path.join(ASSETS, "blockstates", "estela_mapa.json"), "w", encoding="utf-8") as fh:
        json.dump({"variants": {f"facing={k}": ({"model": "darkcraft:block/estela_mapa", "y": v} if v else {"model": "darkcraft:block/estela_mapa"})
                                for k, v in giros.items()}}, fh, indent=1)
    print("estela: texturas y modelo (", len(elementos()), "cajas )")


def vista(carpeta):
    """La estela de frente y de lado (sin perspectiva, x12) y las cuatro texturas."""
    from PIL import Image
    S = 12
    T = {"piedra": piedra(), "frente": frente(), "lado": lado(), "pergamino": pergamino()}
    hoja = Image.new("RGBA", (16 * S * 2 + 60 + 4 * 16 * 6 + 40, 24 * S + 20), (40, 46, 60, 255))
    for k, (cara, eje) in enumerate((("north", "x"), ("east", "z"))):
        ox = 20 + k * (16 * S + 20)
        els = elementos()
        orden = sorted(els, key=lambda e: -e["from"][2] if eje == "x" else e["to"][0])
        for e in orden:
            f = e["faces"].get(cara)
            if f is None:
                continue
            x0, y0, z0 = e["from"]
            x1, y1, z1 = e["to"]
            a0, a1 = (x0, x1) if eje == "x" else (z0, z1)
            if a1 - a0 < 0.01:
                continue
            t = T[f["texture"][1:]].imagen()
            uv = f.get("uv", [a0, 16 - min(16, y1), a1, 16 - y0])
            u0, v0, u1, v1 = [max(0, min(16, v)) for v in uv]
            w, h = max(1, int(round((a1 - a0) * S))), max(1, int(round((y1 - y0) * S)))
            trozo = t.crop((int(u0), int(v0), max(int(u0) + 1, int(u1)), max(int(v0) + 1, int(v1)))).resize((w, h), 0)
            hoja.paste(trozo, (ox + int(round((16 - a1 if eje == "x" else a0) * S)), 10 + int(round((24 - y1) * S))), trozo)
    ox = 16 * S * 2 + 60
    for i, (n, t) in enumerate(T.items()):
        im = t.imagen().resize((16 * 6, 16 * 6), 0)
        hoja.paste(im, (ox + i * (16 * 6 + 10), 20), im)
    os.makedirs(carpeta, exist_ok=True)
    hoja.save(os.path.join(carpeta, "estela.png"))
    print("vista en", carpeta)


if __name__ == "__main__":
    generar()
    if "--vista" in sys.argv:
        vista(sys.argv[sys.argv.index("--vista") + 1])
