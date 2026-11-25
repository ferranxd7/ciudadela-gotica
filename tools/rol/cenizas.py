#!/usr/bin/env python3
"""
Las cenizas de guerra (parte 2e-3): un montoncito de ceniza gris con una brasa encendida flotando encima, del color de su
habilidad, y un detalle que la distingue (el remolino del torbellino, la punta de la estocada, las grietas del sismo, la
estela del paso, la lengua de fuego, el copo de escarcha, el rayo, la gota de sangre, el anillo de la parada...).
Texturas de 16 x 16 y sus modelos de objeto; y el icono del efecto de la furia (el grito de guerra, 18 x 18).

    python3 tools/rol/cenizas.py [--vista carpeta]
"""
import json
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, TINTA, Lienzo, mezcla, rgba  # noqa: E402

CENIZA = ["#2a282c", "#3e3b40", "#55515a", "#6e6a72", "#8a858e", "#a6a1aa"]

# id: rampa de la brasa (de borde a centro)
COLORES = {
    "torbellino": ["#3a4256", "#6a7690", "#a8b4cc", "#dce4f2", "#ffffff"],
    "estocada": ["#203048", "#3e5e86", "#6e98c8", "#a8cdf0", "#eaf6ff"],
    "grito": ["#4a0e08", "#8a2410", "#d0481c", "#f48a3a", "#ffd890"],
    "sismo": ["#3e2a0c", "#6e4e1c", "#a87a2a", "#dcae4a", "#fff0b0"],
    "paso": ["#1a0e2e", "#3c2266", "#6e46b0", "#a682e6", "#e8dcff"],
    "llamas": ["#5e1806", "#a8320c", "#e86418", "#ffa83a", "#ffe08a"],
    "escarcha": ["#16324a", "#2e5a7c", "#4a8ab0", "#8fd0ee", "#f0fcff"],
    "relampago": ["#3a2a06", "#7a5a10", "#c8a41c", "#f2d640", "#fffbd0"],
    "sangre": ["#2a0206", "#5e0610", "#9a0e1a", "#d22a2a", "#ff8a7a"],
    "parada": ["#3a2a10", "#6e5222", "#a8863c", "#e2bf62", "#fff2c0"],
}


def _rampa(cs, t):
    return cs[int(np.clip(round(t), 0, len(cs) - 1))]


def ceniza(n):
    cs = COLORES[n]
    L = Lienzo(16, 16)
    r = np.random.default_rng(sum(ord(c) for c in n))
    grano = r.random((16, 16))
    # el monton de ceniza
    for y in range(9, 16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 6.4, (y + 0.5 - 12.6) / 2.8
            if dx * dx + dy * dy <= 1 and y <= 14:
                t = 2.6 - dx * 1.0 - dy * 1.4 + (grano[y, x] - 0.5) * 1.6
                L.px(x, y, _rampa(CENIZA, t))
    # la brasa: una bola con su luz
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 6.6)
            if d <= 3.3:
                t = (1 - d / 3.3) * 4.2 + 0.4 - ((x - 8) + (y - 6.6)) * 0.12
                L.px(x, y, _rampa(cs, t))
    L.px(6, 5, cs[4]).px(7, 4, mezcla(cs[4], "#ffffff", 0.5))
    # el reflejo de la brasa en la ceniza
    for x in range(5, 11):
        L.px(x, 10, mezcla(L.leer(x, 10), cs[3], 0.35))
    # el detalle de cada una
    det = {
        "torbellino": [(3, 4), (2, 6), (3, 8), (12, 4), (13, 6), (12, 8), (5, 2), (11, 2)],
        "estocada": [(8, 0), (8, 1), (7, 1), (9, 1), (8, 2)],
        "grito": [(2, 3), (3, 4), (13, 3), (12, 4), (8, 0), (2, 9), (13, 9)],
        "sismo": [(4, 12), (5, 13), (11, 12), (10, 13), (8, 14), (3, 11), (12, 11)],
        "paso": [(2, 7), (3, 7), (1, 6), (4, 8), (2, 5)],
        "llamas": [(8, 1), (8, 2), (9, 2), (7, 2), (9, 0)],
        "escarcha": [(3, 3), (12, 3), (2, 9), (13, 9), (8, 0)],
        "relampago": [(10, 0), (9, 1), (10, 2), (9, 3), (12, 9), (13, 10)],
        "sangre": [(8, 10), (8, 11), (7, 12), (8, 12)],
        "parada": [(4, 3), (11, 3), (3, 6), (12, 6), (4, 9), (11, 9)],
    }[n]
    for i, (x, y) in enumerate(det):
        if n == "sismo":
            L.px(x, y, CENIZA[0])
        elif n == "sangre":
            L.px(x, y, cs[2] if i < 2 else cs[1])
        else:
            L.px(x, y, cs[3] if i % 2 == 0 else cs[4])
    L.contorno(TINTA)
    # el halo de la brasa (muy suave, solo donde no hay nada: sin contorno)
    for y in range(16):
        for x in range(16):
            if L.leer(x, y)[3] > 0:
                continue
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 6.6)
            if d < 6.2:
                a = int(70 * (1 - max(0.0, d - 3.0) / 3.2))
                if a > 10:
                    L.px(x, y, rgba(cs[2], a))
    return L


def icono_furia():
    """El efecto del grito de guerra: una llama roja con el centro amarillo y un resplandor detras."""
    L = Lienzo(18, 18)
    for y in range(18):
        for x in range(18):
            d = math.hypot(x + 0.5 - 9, y + 0.5 - 10)
            if d <= 8.4:
                L.px(x, y, rgba("#c83a1e", int(110 * max(0.0, 1 - d / 8.4) + 25)))
    LLAMA = [
        "......r......",
        ".....rr......",
        ".....rRr.....",
        "....rRRr..r..",
        "..r.rROR.rr..",
        "..rrRROORrR..",
        ".rRRROYORRr..",
        ".rRROYYYORRr.",
        "rRROYYWYYORRr",
        "rRROYYWWYORRr",
        "rRRROYYYORRRr",
        ".rRRROOORRRr.",
        "..rrRRRRRrr..",
    ]
    PAL = {"r": "#5e0c13", "R": "#b81c26", "O": "#e86418", "Y": "#ffc85a", "W": "#fff2c0"}
    L.pegar(Lienzo(13, 13).sprite(LLAMA, PAL), 2, 3)
    return L


def modelo(nombre):
    ruta = os.path.join(ASSETS, "models", "item", nombre + ".json")
    with open(ruta, "w", encoding="utf-8") as f:
        json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/" + nombre}}, f)


def montar():
    for n in COLORES:
        ceniza(n).guardar(os.path.join(ASSETS, "textures", "item", "ceniza_" + n + ".png"))
        modelo("ceniza_" + n)
    icono_furia().guardar(os.path.join(ASSETS, "textures", "mob_effect", "furia.png"))
    print(len(COLORES), "cenizas y el icono de la furia")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        hoja = Lienzo(20 * 11, 22, "#2a2430")
        for i, n in enumerate(COLORES):
            hoja.pegar(ceniza(n), i * 20 + 2, 3)
        hoja.pegar(icono_furia(), 10 * 20 + 1, 2)
        hoja.imagen().resize((hoja.w * 6, hoja.h * 6), 0).save(os.path.join(d, "cenizas.png"))
        print("vista en", d)
