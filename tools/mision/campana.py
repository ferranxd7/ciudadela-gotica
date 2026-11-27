#!/usr/bin/env python3
"""
La campana del peregrino (parte 7ag): la textura del modelo que cuelga del Arbol de la Horca y que Oswin toca
(`textures/entity/campana.png`, 32 x 32; las cajas de `client/mision/CampanaRenderer.crear`) y los objetos de 16 x 16: la campana
del peregrino (bronce con verdin y su cordon) y la de los difuntos (la misma, ennegrecida, con el fuego de almas en la boca), con
sus modelos de objeto.

    python3 tools/mision/campana.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "personajes"))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from piel import caras_de  # noqa: E402
from arte import ASSETS, TINTA, Lienzo, mezcla  # noqa: E402

BRONCE = ["#3a2410", "#5e3c16", "#86581e", "#b07a2a", "#d4a040", "#f0c868", "#fff0b0"]
VERDIN = ["#2e5a4a", "#3e7a62", "#5a9a7c"]
NEGRO = ["#0e0c0c", "#1a1614", "#26201c", "#342a24", "#44382e", "#564838"]
ALMAS = ["#1e4a6a", "#3a86b4", "#8cd0f0", "#e0f8ff"]

# las cajas del modelo (u, v, ancho, alto, fondo), como en CampanaRenderer.crear
CAJAS = {"asa": (0, 0, 2, 2, 2), "cuello": (8, 0, 3, 1, 3), "badajo": (20, 0, 1, 3, 1), "hombro": (0, 4, 5, 2, 5),
         "cuerpo": (0, 11, 6, 3, 6), "boca": (0, 20, 7, 2, 7)}


def textura():
    L = Lienzo(32, 32)
    r = np.random.default_rng(29)
    for nombre, caja in CAJAS.items():
        for cara, (x0, y0, w, h) in caras_de(*caja).items():
            for y in range(h):
                for x in range(w):
                    if nombre == "badajo":
                        c = "#2a2a30" if y < h - 1 else "#4a4a54"
                    elif cara == "abajo":
                        # por dentro de la boca, oscuro (el badajo se ve)
                        c = BRONCE[0] if nombre == "boca" else BRONCE[1]
                    elif cara == "arriba":
                        c = BRONCE[5] if (x + y) % 3 else BRONCE[4]
                    else:
                        # los lados: mas claro arriba, el filo de la boca brillante, y la cara de la luz (frente y der) mas clara
                        k = 4 - y * (1.6 / max(1, h)) + (0.6 if cara in ("frente", "der") else -0.4)
                        if nombre == "boca" and y == 0:
                            k += 1.2
                        c = BRONCE[int(np.clip(round(k), 1, 6))]
                        if r.random() < 0.16 and nombre in ("hombro", "cuerpo", "boca"):
                            c = VERDIN[int(r.integers(0, 3))]
                    L.px(x0 + x, y0 + y, c)
    return L


CAMPANA = [
    "......kk........",
    ".....kcck.......",
    "......kk........",
    "......kk........",
    ".....kYYk.......",
    "....kYyyYk......",
    "....kYyyyk......",
    "...kYyyyyyk.....",
    "...kYyvyyyk.....",
    "..kYyyyyvyyk....",
    "..kYyyyyyyyk....",
    ".kYyyvyyyyyyk...",
    ".kOOOOOOOOOOk...",
    ".kkkkkddkkkkk...",
    "......kdk.......",
    ".......k........",
]


def objeto(difuntos):
    L = Lienzo(16, 16)
    if difuntos:
        pal = {"k": TINTA, "Y": NEGRO[5], "y": NEGRO[3], "v": ALMAS[1], "O": NEGRO[4], "d": ALMAS[2], "c": "#3a3448"}
    else:
        pal = {"k": TINTA, "Y": BRONCE[5], "y": BRONCE[3], "v": VERDIN[1], "O": BRONCE[4], "d": BRONCE[1], "c": "#8a6a40"}
    L.sprite(CAMPANA, pal)
    if difuntos:
        # el fuego de almas que asoma por la boca
        for (x, y, k) in ((5, 13, 2), (8, 13, 3), (10, 13, 2), (6, 14, 1), (9, 14, 1)):
            L.px(x, y, ALMAS[k])
    return L


def modelo(nombre):
    ruta = os.path.join(ASSETS, "models", "item", nombre + ".json")
    with open(ruta, "w", encoding="utf-8") as f:
        json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/" + nombre}}, f)


def montar():
    textura().guardar(os.path.join(ASSETS, "textures", "entity", "campana.png"))
    objeto(False).guardar(os.path.join(ASSETS, "textures", "item", "campana_peregrino.png"))
    objeto(True).guardar(os.path.join(ASSETS, "textures", "item", "campana_difuntos.png"))
    modelo("campana_peregrino")
    modelo("campana_difuntos")
    print("campana: textura del modelo y los dos objetos")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        hoja = Lienzo(32 + 2 + 16 + 2 + 16, 32, "#2a2430")
        hoja.pegar(textura(), 0, 0)
        hoja.pegar(objeto(False), 34, 0)
        hoja.pegar(objeto(True), 52, 0)
        hoja.imagen().resize((hoja.w * 8, hoja.h * 8), 0).save(os.path.join(d, "campana.png"))
        print("vista en", d)
