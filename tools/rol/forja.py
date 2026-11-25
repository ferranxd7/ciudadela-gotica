#!/usr/bin/env python3
"""
Los objetos de la forja (parte 2e-2): las PIEDRAS DE FORJA (I, II y III: un trozo de roca con vetas que brillan, cada
vez mas oscuras y encendidas; las pide Dorvan para mejorar las armas) y el MARTILLO del herrero (solo para que lo lleve
Dorvan en la mano). Texturas de 16 x 16 y sus modelos de objeto.

    python3 tools/rol/forja.py [--vista carpeta]
"""
import json
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, TINTA, Lienzo, mezcla  # noqa: E402

# piedra (de oscuro a claro) y veta (de apagada a encendida) de cada grado
PIEDRAS = {
    1: (["#1e1c22", "#2e2b33", "#413d47", "#57525e", "#6e6878", "#888292"], ["#3a5a7a", "#6a9cc8", "#a8d4f4", "#e8f6ff"]),
    2: (["#171316", "#251e22", "#362b30", "#4a3b40", "#5e4b50", "#745e62"], ["#7a3410", "#c8601c", "#f49a3a", "#ffd890"]),
    3: (["#0c0a0c", "#161216", "#221a20", "#30242c", "#3e2e38", "#4e3a46"], ["#7a1010", "#c81c1c", "#f45a2a", "#ffc070", "#fff2c0"]),
}


def _forma(semilla):
    """Un canto rodado irregular: radio por angulo con un poco de ruido (siempre el mismo para cada grado)."""
    r = np.random.default_rng(semilla)
    k = r.random(7) * 0.22
    f = r.random(7) * math.tau

    def dentro(dx, dy):
        a = math.atan2(dy, dx)
        rad = 1.0 - sum(k[i] * (0.5 + 0.5 * math.cos((i + 2) * a + f[i])) for i in range(3)) * 0.9
        return math.hypot(dx, dy) <= rad
    return dentro


def piedra(n):
    roca, veta = PIEDRAS[n]
    L = Lienzo(16, 16)
    dentro = _forma(n * 11 + 3)
    cx, cy, rx, ry = 8.0, 8.6, 6.6, 5.9
    r = np.random.default_rng(n * 7 + 1)
    grano = r.random((16, 16))
    pts = set()
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
            if not dentro(dx, dy):
                continue
            pts.add((x, y))
            # facetas: luz de arriba a la izquierda, con cortes planos
            t = 2.6 - dx * 1.3 - dy * 1.6 + (grano[y, x] - 0.5) * 1.2
            if (x + 2 * y) % 7 == 0:
                t -= 0.8
            L.px(x, y, roca[int(np.clip(round(t), 0, len(roca) - 1))])
    # las vetas: grietas que se encienden hacia el centro
    vetas = {
        1: [(4, 6), (5, 7), (6, 7), (7, 8), (8, 8), (9, 9), (10, 9), (11, 10), (8, 9), (8, 10), (7, 11)],
        2: [(4, 9), (5, 8), (6, 8), (7, 7), (8, 7), (9, 6), (10, 6), (8, 8), (9, 9), (10, 10), (11, 10), (6, 9), (6, 10)],
        3: [(3, 8), (4, 8), (5, 7), (6, 7), (7, 8), (8, 8), (9, 7), (10, 7), (11, 6), (8, 9), (8, 10), (9, 11), (7, 9),
            (6, 5), (7, 6), (10, 8), (11, 9), (12, 9)],
    }[n]
    for i, (x, y) in enumerate(vetas):
        if (x, y) not in pts:
            continue
        d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
        k = len(veta) - 1 - int(min(len(veta) - 2, d / 2.2))
        L.px(x, y, veta[k])
    # un brillo suave alrededor de las vetas (solo en la piedra)
    for (x, y) in pts:
        if any((x + ox, y + oy) in vetas for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))) and (x, y) not in vetas:
            L.px(x, y, mezcla(L.leer(x, y), veta[1], 0.30 if n > 1 else 0.22))
    # el canto de arriba a la izquierda con un pixel de luz
    for (x, y) in pts:
        if (x - 1, y) not in pts and (x, y - 1) not in pts:
            L.px(x, y, roca[-1])
    L.contorno(TINTA)
    # el grado: una, dos o tres chispas encima (III con la chispa mas grande)
    for i in range(n):
        x, y = 12 - i * 2, 2 + (i % 2)
        L.px(x, y, veta[-1])
        if n == 3 and i == 0:
            L.px(x - 1, y, veta[-2]).px(x + 1, y, veta[-2]).px(x, y - 1, veta[-2]).px(x, y + 1, veta[-2])
    return L


def martillo():
    """El martillo de forja de Dorvan: mango de madera en diagonal y cabeza de hierro pesada (arriba a la derecha)."""
    L = Lienzo(16, 16)
    MADERA = ["#2e1d10", "#4a301a", "#6a4626", "#8a6036"]
    HIERRO = ["#1c1c22", "#33333c", "#4c4c58", "#6c6c7a", "#9a9aa8", "#c8c8d4"]
    # el mango: de (2, 14) a (9, 7)
    for i in range(8):
        x, y = 2 + i, 14 - i
        L.px(x, y, MADERA[2] if i % 3 else MADERA[3])
        L.px(x + 1, y, MADERA[1])
        if i < 3:
            L.px(x, y, "#3a2616" if i % 2 else "#54381f")  # la empunadura con tira de cuero
    # la cabeza: un bloque de hierro atravesado al mango (largo de arriba a la izquierda a abajo a la derecha)
    for y in range(0, 13):
        for x in range(4, 16):
            u = (x - 10.5) + (y - 5.5)   # a lo largo de la cabeza
            v = (x - 10.5) - (y - 5.5)   # a lo ancho (la direccion del mango)
            if abs(u) <= 5.6 and abs(v) <= 2.7:
                t = 3.2 + v * 0.45 - u * 0.12
                if abs(u) > 4.6:
                    t += 0.8  # las dos caras de golpear, mas claras
                if abs(v) > 2.0:
                    t -= 1
                L.px(x, y, HIERRO[int(np.clip(round(t), 0, 5))])
    # la cara de golpear (abajo a la derecha) con el rojo de la fragua
    L.px(13, 9, "#c8601c").px(14, 8, "#7a3410")
    L.contorno(TINTA)
    return L


def modelo(nombre, padre="item/generated"):
    ruta = os.path.join(ASSETS, "models", "item", nombre + ".json")
    with open(ruta, "w", encoding="utf-8") as f:
        json.dump({"parent": padre, "textures": {"layer0": "darkcraft:item/" + nombre}}, f)


def montar():
    for n in PIEDRAS:
        piedra(n).guardar(os.path.join(ASSETS, "textures", "item", "piedra_forja_%d.png" % n))
        modelo("piedra_forja_%d" % n)
    martillo().guardar(os.path.join(ASSETS, "textures", "item", "martillo_herrero.png"))
    modelo("martillo_herrero", "item/handheld")
    print("forja: piedra_forja_1..3, martillo_herrero")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        hoja = Lienzo(20 * 4, 20, "#2a2430")
        for i, s in enumerate([piedra(1), piedra(2), piedra(3), martillo()]):
            hoja.pegar(s, i * 20 + 2, 2)
        hoja.imagen().resize((hoja.w * 8, hoja.h * 8), 0).save(os.path.join(d, "forja_objetos.png"))
        print("vista en", d)
