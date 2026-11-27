#!/usr/bin/env python3
"""
Lo que queda de Tam (parte 7ah, la mision de Hedda): la textura del esqueleto sentado junto al torno de la Puerta Oeste
(`textures/entity/tam.png`, 128 x 64; las cajas de `client/mision/RestosRenderer.crear`) y los objetos: la bufanda roja (16 x 16,
objeto clave) y la Lanza de la Puerta Oeste (32 x 32, de esquina a esquina, con su modelo de objeto en la mano mas largo, como la
alabarda), con sus modelos.

    python3 tools/mision/tam.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "personajes"))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from piel import caras_de  # noqa: E402
from arte import ASSETS, TINTA, Lienzo, mezcla  # noqa: E402

HUESO = ["#4a4238", "#6a6052", "#8c8270", "#ada28c", "#c8bea6", "#e0d8c2"]
HOLLIN = ["#141010", "#221c1a", "#302826"]
HIERRO = ["#1a1a20", "#2a2a32", "#3c3c46", "#50505c", "#686874", "#84848e"]
OXIDO = ["#3a1e10", "#5a2e16", "#7a4220"]
SOBREVESTE = ["#1e0a0a", "#2e1010", "#421614", "#561c18", "#6a2620"]
ORO = ["#5a4418", "#8a6a24", "#b8923a"]
LANA = ["#3e0b0d", "#5e1214", "#82191b", "#a52626", "#c43a34", "#de5c4c"]
MADERA = ["#2a1c10", "#3e2a18", "#543a22", "#6a4a2c", "#80603a"]

# las cajas del modelo (u, v, ancho, alto, fondo), como en RestosRenderer.crear
CAJAS = {"craneo": (0, 0, 8, 8, 8), "capacete": (32, 0, 9, 4, 9), "ala": (72, 0, 12, 1, 12),
         "costillas": (16, 16, 8, 12, 4), "sobreveste": (16, 32, 8, 11, 4), "brazo": (40, 16, 2, 12, 2), "pierna": (0, 16, 2, 12, 2),
         "bufanda": (48, 16, 9, 2, 5), "cabo": (48, 24, 2, 7, 1), "asta": (0, 48, 28, 1, 1), "punta": (60, 48, 4, 2, 2)}


def textura():
    L = Lienzo(128, 64)
    r = np.random.default_rng(113)

    def hueso(x, y, k=3.0):
        c = HUESO[int(np.clip(round(k + (r.random() - 0.5) * 1.6), 0, 5))]
        if r.random() < 0.22:
            c = mezcla(c, HOLLIN[1], 0.55)  # el hollin del fuego
        return c

    for nombre, caja in CAJAS.items():
        for cara, (x0, y0, w, h) in caras_de(*caja).items():
            for y in range(h):
                for x in range(w):
                    c = None
                    if nombre == "craneo":
                        if cara == "frente":
                            # las cuencas, el hueco de la nariz y los dientes
                            if (y in (3, 4) and x in (1, 2, 5, 6)):
                                c = HOLLIN[0]
                            elif y == 5 and x in (3, 4):
                                c = HOLLIN[0]
                            elif y == 7 and x in range(1, 7):
                                c = HUESO[5] if x % 2 else HOLLIN[1]
                            else:
                                c = hueso(x, y, 3.4 - y * 0.12)
                        else:
                            c = hueso(x, y, 3.0 - (0.5 if cara in ("izq", "abajo") else 0))
                    elif nombre in ("capacete", "ala"):
                        # el capacete de hierro de la guardia, abollado y con oxido
                        k = 3.2 - (y * 0.3 if cara not in ("arriba", "abajo") else 0) + (0.6 if cara in ("frente", "der", "arriba") else -0.5)
                        c = HIERRO[int(np.clip(round(k + (r.random() - 0.5) * 1.2), 0, 5))]
                        if r.random() < 0.18:
                            c = OXIDO[int(r.integers(0, 3))]
                        if cara == "abajo":
                            c = HIERRO[0]
                    elif nombre == "costillas":
                        # la caja del pecho: costillas con huecos (transparentes) y el espinazo
                        if cara in ("frente", "espalda"):
                            espinazo = x in (3, 4)
                            costilla = y % 2 == 0 and y < 9
                            if espinazo and cara == "espalda" or costilla or (espinazo and y >= 9):
                                c = hueso(x, y, 3.0)
                            elif y >= 10:
                                c = hueso(x, y, 2.4) if x in (2, 5) else None  # la pelvis
                        elif cara in ("der", "izq"):
                            c = hueso(x, y, 2.6) if y % 2 == 0 and y < 9 else None
                        else:
                            c = hueso(x, y, 2.5)
                    elif nombre == "sobreveste":
                        # la sobreveste roja de la guardia, quemada y rota: agujeros, bordes negros y la puerta de oro bordada
                        if cara == "abajo" or cara == "arriba":
                            c = None if cara == "abajo" else SOBREVESTE[2]
                        else:
                            agujero = r.random() < 0.18 or (y >= h - 3 and r.random() < 0.45)
                            if agujero:
                                c = None
                            else:
                                c = SOBREVESTE[int(np.clip(round(2.6 - y * 0.12 + (r.random() - 0.5) * 1.6), 0, 4))]
                                if r.random() < 0.2:
                                    c = HOLLIN[int(r.integers(0, 3))]
                                # la puerta de la guardia, en oro gastado, en el pecho
                                if cara == "frente" and 2 <= x <= 5 and 1 <= y <= 4 and (x in (2, 5) or y == 1):
                                    c = ORO[int(r.integers(0, 3))]
                    elif nombre in ("brazo", "pierna"):
                        c = hueso(x, y, 3.0 - (0.6 if cara in ("izq", "espalda") else 0))
                        if y in (h - 1,) and cara != "abajo":
                            c = hueso(x, y, 2.2)
                    elif nombre in ("bufanda", "cabo"):
                        # la lana roja tejida a mano: puntos alternos y una franja mas clara
                        k = 3 if (x + y) % 2 == 0 else 2
                        if nombre == "bufanda" and y == 0 and cara not in ("arriba", "abajo"):
                            k = 4
                        if nombre == "cabo" and y == h - 1:
                            c = LANA[4] if x % 2 == 0 else None  # los flecos
                        else:
                            c = LANA[k] if r.random() > 0.12 else mezcla(LANA[k], HOLLIN[1], 0.4)
                    elif nombre == "asta":
                        c = MADERA[2 + (1 if (x + y) % 5 else 0) - (1 if cara in ("abajo", "izq") else 0)]
                        if x % 9 == 4:
                            c = MADERA[1]
                    elif nombre == "punta":
                        c = HIERRO[4 if cara in ("arriba", "frente", "der") else 2] if r.random() > 0.2 else OXIDO[1]
                    if c is None:
                        L.borrar(x0 + x, y0 + y)
                    else:
                        L.px(x0 + x, y0 + y, c)
    return L


BUFANDA = [
    "................",
    "..kkkkkkkkkkkk..",
    ".kRrRrRrRrRrRrk.",
    ".krRrRrRrRrRrRk.",
    "..kkkkRrRkkkkk..",
    ".....kRrRk......",
    "....kRrRrRk.....",
    "....krRkrRk.....",
    "....kRrkkRrk....",
    "...krRk.krRk....",
    "...kRrk..kRrk...",
    "...krRk..krRk...",
    "...kRrk...kRrk..",
    "...kfkfk..kfkfk.",
    "................",
    "................",
]


def bufanda():
    L = Lienzo(16, 16)
    L.sprite(BUFANDA, {"k": TINTA, "R": LANA[4], "r": LANA[2], "f": LANA[3]})
    return L


def lanza():
    """La Lanza de la Puerta Oeste, de esquina a esquina (la punta arriba a la derecha): asta de fresno con su refuerzo de hierro,
    la moharra de hoja ancha con oxido y una tira de lana roja atada bajo la punta."""
    L = Lienzo(32, 32)
    # el asta: de (3, 28) a (22, 9)
    for i in range(0, 21):
        x, y = 3 + i, 28 - i
        L.px(x, y, MADERA[3] if i % 6 else MADERA[2])
        L.px(x + 1, y, MADERA[2])
        L.px(x, y - 1, MADERA[1]) if i % 3 == 0 else None
    # el regaton de hierro abajo
    for (x, y) in ((2, 29), (3, 29), (2, 28), (3, 28)):
        L.px(x, y, HIERRO[3])
    # la virola bajo la punta y la tira de lana roja
    for (x, y, c) in ((21, 10, HIERRO[4]), (22, 10, HIERRO[3]), (21, 11, HIERRO[2]), (20, 11, LANA[4]), (19, 12, LANA[3]),
                      (19, 13, LANA[2]), (18, 13, LANA[3])):
        L.px(x, y, c)
    # la moharra: hoja ancha de (22, 9) a la punta (30, 1)
    hoja = [(23, 8), (24, 7), (25, 6), (26, 5), (27, 4), (28, 3), (29, 2), (30, 1)]
    for i, (x, y) in enumerate(hoja):
        ancho = 2 if i < 2 else 3 if i < 5 else 2 if i < 7 else 1
        for k in range(-ancho + 1, ancho):
            px, py = x + (k if k > 0 else 0), y - (k if k < 0 else 0)
            c = HIERRO[5] if k == 0 else HIERRO[4] if k > 0 else HIERRO[3]
            if (i + k) % 4 == 3:
                c = OXIDO[2]
            L.px(px, py, c)
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


def modelos():
    item = os.path.join(ASSETS, "models", "item")
    with open(os.path.join(item, "bufanda_roja.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/bufanda_roja"}}, f)
    mano = {"rotation": [0, -90, 55], "translation": [0, 7, 1.5], "scale": [1.5, 1.5, 0.9]}
    izq = {"rotation": [0, 90, -55], "translation": [0, 7, 1.5], "scale": [1.5, 1.5, 0.9]}
    with open(os.path.join(item, "lanza_tam.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "minecraft:item/handheld", "textures": {"layer0": "darkcraft:item/lanza_tam"},
                   "display": {"thirdperson_righthand": mano, "thirdperson_lefthand": izq}}, f, indent=2)


def montar():
    textura().guardar(os.path.join(ASSETS, "textures", "entity", "tam.png"))
    bufanda().guardar(os.path.join(ASSETS, "textures", "item", "bufanda_roja.png"))
    lanza().guardar(os.path.join(ASSETS, "textures", "item", "lanza_tam.png"))
    modelos()
    print("tam: textura del modelo, la bufanda y la lanza")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        hoja = Lienzo(128 + 2 + 16 + 2 + 32, 64, "#2a2430")
        hoja.pegar(textura(), 0, 0)
        hoja.pegar(bufanda(), 130, 0)
        hoja.pegar(lanza(), 148, 0)
        hoja.imagen().resize((hoja.w * 5, hoja.h * 5), 0).save(os.path.join(d, "tam.png"))
        print("vista en", d)
