#!/usr/bin/env python3
"""
Las balizas de la niebla (parte 7ao, la mision de Corwen): la baliza (`textures/entity/baliza.png` y lo que luce,
`baliza_brillo.png`, 64 x 64; las cajas de `client/mision/BalizaRenderer.crear`), el yesquero de Corwen (objeto clave, 16 x 16) y
la Pertiga del farolero (el arma de quien le pide que se quede, 32 x 32, de esquina a esquina, con su modelo en la mano mas largo,
como la lanza de Tam), con sus modelos.

- **La baliza**: un poste de roble negro, podrido abajo y con musgo, clavado en una base de piedras; arriba, un brazo que sale
  hacia la laguna y de su punta, colgado de una cadena corta, un farol grande de hierro (tejadillo, jaula con cuatro cristales
  gordos y suelo) con la mecha dentro. Apagada, los cristales estan negros de hollin; encendida (lo que luce), la llama ambar y
  los cristales calientes.
- **El yesquero**: la cajita de lata con su tapa, el eslabon de acero curvo y una chispa.
- **La Pertiga del farolero**: la vara de fresno con la virola de laton, el gancho y la punta de hierro arriba (con brasas: prende).

    python3 tools/mision/baliza.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "personajes"))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from piel import caras_de  # noqa: E402
from arte import ASSETS, TINTA, Lienzo, mezcla  # noqa: E402

ROBLE = ["#120e0a", "#1c1610", "#282016", "#352a1e", "#443628", "#544434"]
MUSGO = ["#1e2a14", "#2e3e1c", "#405426"]
PIEDRA = ["#1a1a1c", "#26262a", "#343438", "#44444a", "#56565c"]
HIERRO = ["#100e10", "#1c191c", "#2a262a", "#3a3438", "#4e464a", "#645a5c"]
OXIDO = ["#3a1e10", "#5a2e16", "#7a4220"]
HOLLIN = ["#0c0a08", "#16120e", "#201a14"]
LATON = ["#3a2a12", "#5e4420", "#8a6830", "#b08c44", "#d0ae62"]
LUZ = ["#7a3008", "#c45a10", "#f09030", "#ffc860", "#fff0b0"]
MADERA = ["#2a1c10", "#3e2a18", "#543a22", "#6a4a2c", "#80603a", "#96764a"]

# las cajas (u, v, ancho, alto, fondo), como en BalizaRenderer.crear
CAJAS = {"poste": (0, 0, 3, 40, 3), "brazo": (12, 0, 2, 2, 8), "techo": (12, 10, 7, 1, 7), "fondo": (12, 18, 7, 1, 7),
         "jaula": (12, 26, 6, 7, 6), "llama": (36, 26, 2, 3, 2), "cadena": (40, 10, 1, 2, 1), "base": (12, 39, 6, 2, 6)}


def textura(brillo=False):
    L = Lienzo(64, 64)
    r = np.random.default_rng(149)
    for nombre, caja in CAJAS.items():
        for cara, (x0, y0, w, h) in caras_de(*caja).items():
            for y in range(h):
                for x in range(w):
                    c = None
                    lado = cara not in ("arriba", "abajo")
                    if nombre == "jaula":
                        if not lado:
                            c = None if brillo else HIERRO[1]
                        elif x in (0, w - 1):
                            c = None if brillo else HIERRO[3 if cara in ("frente", "der") else 2]  # los barrotes
                        elif y == 3:
                            c = None if brillo else HIERRO[2]  # el travesano de en medio
                        else:
                            # los cristales: apagados, de hollin (algo de reflejo arriba); encendidos, calientes y mas claros abajo
                            if brillo:
                                c = LUZ[3] if y >= h - 2 else LUZ[2] if y >= 2 else LUZ[1]
                            else:
                                c = HOLLIN[2] if (x + y) % 3 == 0 else HOLLIN[1]
                                if y == 1 and x == 1:
                                    c = HIERRO[4]  # un reflejo
                    elif nombre == "llama":
                        # la mecha (apagada) o la llama (en el brillo)
                        if brillo:
                            c = LUZ[4] if y == h - 1 else LUZ[3] if y == 1 else LUZ[2]
                        else:
                            c = HOLLIN[0] if y < h - 1 else HIERRO[2]
                    elif brillo:
                        c = None
                    elif nombre == "poste":
                        if not lado:
                            c = ROBLE[2]
                        else:
                            k = 3 + (0.6 if x == 1 else -0.4 if x == 2 else 0) + (r.random() - 0.5) * 1.6
                            c = ROBLE[int(np.clip(round(k), 0, 5))]
                            if (y + x * 3) % 7 == 0:
                                c = ROBLE[1]  # las grietas de la madera
                            # abajo, podrido y con musgo; arriba, el anillo de hierro donde sale el brazo
                            if y > h - 9 and r.random() < (y - (h - 9)) / 10:
                                c = MUSGO[int(r.integers(0, 3))]
                            if y in (2, 3):
                                c = HIERRO[3] if y == 2 else HIERRO[2]
                    elif nombre in ("brazo", "techo", "fondo", "cadena"):
                        if nombre == "brazo":
                            c = ROBLE[3] if (x + y) % 3 else ROBLE[2]
                            if cara == "arriba" and x % 4 == 0:
                                c = HIERRO[3]  # los herrajes
                        elif nombre == "cadena":
                            c = HIERRO[4] if y % 2 else HIERRO[3]
                        else:
                            borde = x in (0, w - 1) or (cara in ("arriba", "abajo") and y in (0, h - 1))
                            c = HIERRO[4] if borde else HIERRO[2] if (x + y) % 2 else HIERRO[3]
                            if r.random() < 0.15:
                                c = OXIDO[int(r.integers(0, 3))]
                            if nombre == "techo" and cara == "arriba" and 2 <= x <= 4 and 2 <= y <= 4:
                                c = HOLLIN[0]  # el humo del respiradero
                    elif nombre == "base":
                        c = PIEDRA[int(np.clip(round(2 + (r.random() - 0.5) * 2.4), 0, 4))]
                        if r.random() < 0.18:
                            c = MUSGO[int(r.integers(0, 3))]
                    if c is not None:
                        L.px(x0 + x, y0 + y, c)
    return L


# ---------------------------------------------------------------- los objetos

YESQUERO = [
    "................",
    "................",
    "..........k.....",
    ".........kEk....",
    "..........k.....",
    "....kkkkkk......",
    "...kSSSSSSk.....",
    "..kSssssssSk....",
    "..kTTTTTTTTk....",
    "..ktttttttttk...",
    "..ktbbbbbbbtk...",
    "..ktbbbbbbbtk...",
    "..kttttttttk....",
    "...kkkkkkkk.....",
    "................",
    "................",
]


def yesquero():
    """La cajita de lata del yesquero (la tapa entreabierta con la yesca), el eslabon de acero curvo encima y una chispa."""
    L = Lienzo(16, 16)
    pal = {"k": TINTA, "S": "#8a8070", "s": "#6a6050", "T": HIERRO[5], "t": HIERRO[3], "b": "#5a4430", "E": LUZ[3]}
    L.sprite(YESQUERO, pal)
    # el eslabon: una C de acero sobre la tapa
    for (x, y, c) in ((5, 4, HIERRO[5]), (6, 3, HIERRO[5]), (7, 3, HIERRO[4]), (8, 3, HIERRO[4]), (9, 4, HIERRO[5]),
                      (4, 5, TINTA), (10, 5, TINTA), (11, 2, LUZ[4]), (12, 1, LUZ[2]), (8, 2, LUZ[2])):
        L.px(x, y, c)
    # la yesca que asoma
    for (x, y) in ((4, 7), (5, 7), (9, 7)):
        L.px(x, y, "#a8884a")
    return L


def pertiga():
    """La Pertiga del farolero, de esquina a esquina (la punta arriba a la derecha): la vara de fresno, la virola de laton, el gancho
    curvo y la punta de hierro, con brasas en el gancho."""
    L = Lienzo(32, 32)
    # la vara: de (2, 29) a (23, 8)
    for i in range(0, 22):
        x, y = 2 + i, 29 - i
        L.px(x, y, MADERA[4] if i % 5 else MADERA[3])
        L.px(x + 1, y, MADERA[2])
        if i % 4 == 0:
            L.px(x, y - 1, MADERA[5])
    # el regaton de hierro abajo
    for (x, y) in ((1, 30), (2, 30), (1, 29), (2, 29)):
        L.px(x, y, HIERRO[4])
    # la virola de laton
    for (x, y, c) in ((22, 9, LATON[4]), (23, 9, LATON[3]), (22, 10, LATON[2]), (21, 10, LATON[3]), (23, 8, LATON[2])):
        L.px(x, y, c)
    # la punta de hierro, recta hacia arriba a la derecha
    for i, (x, y) in enumerate(((24, 7), (25, 6), (26, 5), (27, 4), (28, 3), (29, 2))):
        L.px(x, y, HIERRO[5] if i < 5 else HIERRO[4])
        if i < 4:
            L.px(x + 1, y, HIERRO[3])
    # el gancho de laton: sale de la virola hacia la derecha y se curva hacia abajo
    for (x, y, c) in ((24, 9, LATON[4]), (25, 9, LATON[4]), (26, 9, LATON[3]), (27, 10, LATON[3]), (27, 11, LATON[2]),
                      (26, 12, LATON[2]), (25, 12, LATON[1])):
        L.px(x, y, c)
    # brasas en el gancho
    for (x, y, c) in ((28, 9, LUZ[3]), (28, 12, LUZ[2]), (26, 13, LUZ[1]), (29, 11, LUZ[4])):
        L.px(x, y, c)
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
    with open(os.path.join(item, "yesquero.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/yesquero"}}, f)
    mano = {"rotation": [0, -90, 55], "translation": [0, 7, 1.5], "scale": [1.5, 1.5, 0.9]}
    izq = {"rotation": [0, 90, -55], "translation": [0, 7, 1.5], "scale": [1.5, 1.5, 0.9]}
    with open(os.path.join(item, "pertiga_farolero.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "minecraft:item/handheld", "textures": {"layer0": "darkcraft:item/pertiga_farolero"},
                   "display": {"thirdperson_righthand": mano, "thirdperson_lefthand": izq}}, f, indent=2)


def montar():
    textura().guardar(os.path.join(ASSETS, "textures", "entity", "baliza.png"))
    textura(True).guardar(os.path.join(ASSETS, "textures", "entity", "baliza_brillo.png"))
    yesquero().guardar(os.path.join(ASSETS, "textures", "item", "yesquero.png"))
    pertiga().guardar(os.path.join(ASSETS, "textures", "item", "pertiga_farolero.png"))
    modelos()
    print("baliza: la baliza (textura y brillo), el yesquero y la pertiga")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        textura().imagen().resize((64 * 8, 64 * 8), 0).save(os.path.join(d, "baliza.png"))
        textura(True).imagen().resize((64 * 8, 64 * 8), 0).save(os.path.join(d, "baliza_brillo.png"))
        hoja = Lienzo(56, 36, "#2a2430")
        hoja.pegar(yesquero(), 2, 10)
        hoja.pegar(pertiga(), 22, 2)
        hoja.imagen().resize((56 * 10, 36 * 10), 0).save(os.path.join(d, "baliza_objetos.png"))
        print("vista en", d)
