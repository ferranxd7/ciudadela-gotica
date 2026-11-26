#!/usr/bin/env python3
"""
La soga de la Arboleda de los Ahorcados (parte 7g): una cuerda de canamo vieja que cuelga de las ramas. Tres partes (el
estado `parte` del bloque `darkcraft:soga`):

  cuerda  la cuerda que baja de arriba abajo por el centro del bloque (de una rama, o de la soga de encima)
  cabo    la cuerda que se acaba deshilachada a media altura (la que corto alguien... o se rompio al caer un ahorcado)
  lazo    la cuerda que acaba en el nudo del verdugo (las vueltas) y el lazo vacio debajo

Salen la textura y los modelos:

  assets/darkcraft/textures/block/soga.png
  assets/darkcraft/models/block/soga_{cuerda,cabo,lazo}.json, blockstates/soga.json

    python3 tools/morvain/soga.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, Lienzo  # noqa: E402

TEX = os.path.join(ASSETS, "textures", "block")
MOD = os.path.join(ASSETS, "models", "block")
EST = os.path.join(ASSETS, "blockstates")
#: los colores del canamo viejo (los mismos de la soga del ahorcado: tools/enemigos/ahorcado.py)
SOGA = ["#2c2216", "#3e3020", "#56442c", "#6e5838", "#876e46", "#a08656"]


def textura():
    """La cuerda torcida: cada tira de dos pixeles de ancho tiene las vueltas en diagonal (sirve cualquier columna)."""
    r = np.random.default_rng(7701)
    L = Lienzo(16, 16, SOGA[3])
    for y in range(16):
        for x in range(16):
            # las vueltas del torcido: una diagonal clara y otra oscura cada cuatro pixeles
            k = (x + y) % 4
            c = SOGA[4] if k == 0 else SOGA[3] if k == 1 else SOGA[2] if k == 2 else SOGA[1]
            if r.random() < 0.10:
                c = SOGA[5] if k < 2 else SOGA[0]
            L.px(x, y, c)
    return L


def cara(uv, cull=None, tex="#soga"):
    d = {"uv": uv, "texture": tex}
    if cull:
        d["cullface"] = cull
    return d


def caja(desde, hasta, cull_arriba=False, cull_abajo=False):
    """Un trozo de cuerda (todas sus caras con la textura: cada cara coge un trozo del tamano de la cara)."""
    x0, y0, z0 = desde
    x1, y1, z1 = hasta
    lado_x = [x0, 16 - y1, x1, 16 - y0]
    lado_z = [z0, 16 - y1, z1, 16 - y0]
    tapa = [x0, z0, x1, z1]
    return {
        "from": list(desde), "to": list(hasta),
        "faces": {
            "north": cara(lado_x), "south": cara(lado_x), "east": cara(lado_z), "west": cara(lado_z),
            "up": cara(tapa, "up" if cull_arriba else None), "down": cara(tapa, "down" if cull_abajo else None),
        },
    }


def modelos():
    base = {"parent": "block/block", "ambientocclusion": False, "textures": {"soga": "darkcraft:block/soga", "particle": "darkcraft:block/soga"}}
    cuerda = dict(base, elements=[caja((7, 0, 7), (9, 16, 9), True, True)])
    cabo = dict(base, elements=[
        caja((7, 5, 7), (9, 16, 9), True),
        # las hebras deshilachadas
        caja((6.5, 2.5, 7.5), (7.5, 5, 8.5)),
        caja((8.5, 2, 7), (9.5, 5, 8)),
        caja((7.5, 3, 8.5), (8.5, 5, 9.5)),
        caja((7.5, 3.5, 6.5), (8.5, 5, 7.5)),
    ])
    lazo = dict(base, elements=[
        caja((7, 10, 7), (9, 16, 9), True),
        # las vueltas del nudo
        caja((6.5, 6, 6.5), (9.5, 10, 9.5)),
        # el lazo vacio, de lado a lado (en el plano norte-sur del bloque)
        caja((5, 1.5, 7.25), (6.5, 6.5, 8.75)),
        caja((9.5, 1.5, 7.25), (11, 6.5, 8.75)),
        caja((5.5, 0.5, 7.25), (10.5, 2, 8.75)),
    ])
    return {"cuerda": cuerda, "cabo": cabo, "lazo": lazo}


def main():
    os.makedirs(TEX, exist_ok=True)
    os.makedirs(MOD, exist_ok=True)
    os.makedirs(EST, exist_ok=True)
    textura().guardar(os.path.join(TEX, "soga.png"))
    for nombre, m in modelos().items():
        with open(os.path.join(MOD, f"soga_{nombre}.json"), "w", encoding="utf-8") as f:
            json.dump(m, f, indent=1)
            f.write("\n")
    estados = {"variants": {f"parte={p}": {"model": f"darkcraft:block/soga_{p}"} for p in ("cuerda", "cabo", "lazo")}}
    with open(os.path.join(EST, "soga.json"), "w", encoding="utf-8") as f:
        json.dump(estados, f, indent=1)
        f.write("\n")
    if "--vista" in sys.argv:
        carpeta = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(carpeta, exist_ok=True)
        textura().imagen().resize((128, 128), 0).save(os.path.join(carpeta, "soga.png"))
    print("soga: textura y 3 modelos")


if __name__ == "__main__":
    main()
