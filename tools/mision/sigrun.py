#!/usr/bin/env python3
"""
La Espada de la Guardiana (parte 7ap, el duelo del Paso del Colmillo): la espada larga de Sigrun, para quien acaba con ella (32 x 32,
de esquina a esquina: la punta arriba a la derecha), con su modelo de objeto (en la mano, algo mas larga que una espada del juego).

- La hoja de acero, larga y recta, con la canal oscura por el medio y la escarcha pegada al filo; la guarda recta de acero, el puno
  de cuero negro con su vuelta de alambre y el pomo redondo con una piedra azul (el color de sus ojos).

    python3 tools/mision/sigrun.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, TINTA, Lienzo  # noqa: E402

ACERO = ["#1e2328", "#343c44", "#4f5a64", "#6e7a86", "#96a2ae", "#c8d2da", "#eef3f7"]
ESCARCHA = "#e4f2fa"
CUERO = ["#140c08", "#24160e", "#3a2416", "#523420"]
ALAMBRE = "#8a96a2"
AZUL = ["#1c3a52", "#4c8ab4", "#8fd8ff"]


def espada():
    L = Lienzo(32, 32)
    r = np.random.default_rng(1717)
    # la guarda: centrada en (9, 22), atravesada (de abajo a la derecha a arriba a la izquierda)
    gx, gy = 9, 22
    for k in range(-4, 5):
        x, y = gx + k, gy + k
        L.px(x, y, ACERO[5] if k < 0 else ACERO[4] if k < 3 else ACERO[3])
        L.px(x + 1, y, ACERO[2])
    L.px(gx - 4, gy - 5, ACERO[4])
    L.px(gx + 5, gy + 4, ACERO[2])
    # la hoja: de (10, 21) a la punta (29, 2), tres pixeles de ancho que se estrechan al final
    n = 20
    for i in range(n):
        cx, cy = gx + 1 + i, gy - 1 - i
        resto = n - i
        # arriba a la izquierda, el filo con luz (y escarcha); en medio, la canal (oscura en sus dos tercios); abajo, la sombra
        if resto > 1:
            filo = ESCARCHA if r.random() < 0.22 else ACERO[6] if i % 5 else ACERO[5]
            L.px(cx, cy - 1, filo)
        centro = ACERO[2] if i < n * 0.66 and i > 1 else ACERO[4]
        L.px(cx, cy, centro)
        if resto > 2:
            L.px(cx + 1, cy, ACERO[3] if r.random() > 0.15 else ESCARCHA)
    L.px(gx + 1 + n, gy - 1 - n, ACERO[5])
    # el puno de cuero con su vuelta de alambre: de (8, 23) a (4, 27)
    for i in range(5):
        x, y = gx - 1 - i, gy + 1 + i
        L.px(x, y, ALAMBRE if i % 2 == 0 else CUERO[2])
        L.px(x + 1, y, CUERO[1])
        L.px(x, y + 1, CUERO[0]) if i == 4 else None
    # el pomo redondo con su piedra azul
    for (x, y, c) in ((2, 28, ACERO[4]), (3, 28, ACERO[3]), (2, 29, ACERO[3]), (3, 29, ACERO[2]), (1, 28, ACERO[5]), (2, 27, ACERO[5]),
                      (1, 29, ACERO[2]), (2, 30, ACERO[1]), (4, 29, ACERO[1]), (3, 30, ACERO[1])):
        L.px(x, y, c)
    L.px(2, 28, AZUL[2])
    L.px(3, 29, AZUL[1])
    # el contorno de tinta
    a = L.a[..., 3] > 0
    borde = []
    for y in range(32):
        for x in range(32):
            if a[y, x]:
                continue
            if any(0 <= x + dx < 32 and 0 <= y + dy < 32 and a[y + dy, x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                borde.append((x, y))
    for (x, y) in borde:
        L.px(x, y, TINTA)
    return L


def modelo():
    item = os.path.join(ASSETS, "models", "item")
    mano = {"rotation": [0, -90, 55], "translation": [0, 5.5, 1.0], "scale": [1.25, 1.25, 0.85]}
    izq = {"rotation": [0, 90, -55], "translation": [0, 5.5, 1.0], "scale": [1.25, 1.25, 0.85]}
    with open(os.path.join(item, "espada_guardiana.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "minecraft:item/handheld", "textures": {"layer0": "darkcraft:item/espada_guardiana"},
                   "display": {"thirdperson_righthand": mano, "thirdperson_lefthand": izq}}, f, indent=2)
        f.write("\n")


def main():
    L = espada()
    L.guardar(os.path.join(ASSETS, "textures", "item", "espada_guardiana.png"))
    modelo()
    print("sigrun: la espada de la guardiana (textura y modelo)")
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        L.imagen().resize((32 * 10, 32 * 10), 0).save(os.path.join(d, "espada_guardiana.png"))


if __name__ == "__main__":
    main()
