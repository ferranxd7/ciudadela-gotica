#!/usr/bin/env python3
"""
La luz del muelle (parte 7an, la mision de Ylva): el farol de Brann (`textures/entity/farol_brann.png` y lo que luce,
`farol_brann_brillo.png`, 64 x 32; las cajas de `client/mision/FarolBrannRenderer.crear`) y los dos objetos clave: el farol de
fuego frio de Ylva y el de aceite de su padre (16 x 16, `textures/item/farol_ylva.png` y `farol_ballenero.png`), con sus modelos.

- **El farol de Brann**: grande, de ballenero; base y tapa de laton verdoso con costras de sal, el cristal ahumado (por dentro, la
  mecha y el aceite) con sus barrotes, el tejadillo y el asa de hierro. Lo que luce: la llama ambar en el cristal.
- **El farol de Ylva**: pequeno, de hierro negro, con el fuego frio del faro (azul palido, casi blanco en el centro).

    python3 tools/mision/farol.py [--vista carpeta]
"""
import json
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "personajes"))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from piel import caras_de  # noqa: E402
from arte import ASSETS, TINTA, Lienzo, mezcla  # noqa: E402

LATON = ["#2e2612", "#4a3e1c", "#6a5a28", "#8c7a38", "#ae9a4e", "#d0bc6e"]
VERDIN = ["#3e5a48", "#5a8064", "#7aa688"]
SAL = "#dfe2da"
HIERRO = ["#121214", "#1e1e22", "#2c2c32", "#3c3c44", "#50505a"]
HUMO = ["#14110e", "#221c16", "#30281e", "#40362a"]
AMBAR = ["#7a3a08", "#c46a14", "#f0a030", "#ffd070", "#fff2c0"]
FRIO = ["#1e5a78", "#4c9cc4", "#94d6f0", "#d6f6ff", "#ffffff"]

# las cajas (u, v, ancho, alto, fondo), como en FarolBrannRenderer.crear
CAJAS = {"base": (0, 0, 6, 1, 6), "cristal": (0, 8, 5, 5, 5), "tapa": (24, 0, 6, 1, 6), "tejadillo": (24, 8, 4, 1, 4),
         "poste": (48, 0, 1, 2, 1), "travesano": (48, 4, 1, 1, 5)}


def textura(brillo=False):
    L = Lienzo(64, 32)
    r = np.random.default_rng(127)

    def laton(k):
        c = LATON[int(np.clip(round(k + (r.random() - 0.5) * 1.4), 0, 5))]
        p = r.random()
        if p < 0.16:
            c = VERDIN[int(r.integers(0, 3))]  # el cardenillo
        elif p < 0.24:
            c = mezcla(c, SAL, 0.7)  # costras de sal
        return c

    for nombre, caja in CAJAS.items():
        for cara, (x0, y0, w, h) in caras_de(*caja).items():
            for y in range(h):
                for x in range(w):
                    c = None
                    if nombre == "cristal":
                        lado = cara not in ("arriba", "abajo")
                        if not lado:
                            c = None if brillo else HUMO[0]
                        elif x in (0, w - 1):
                            c = None if brillo else HIERRO[2 if cara in ("frente", "der") else 1]  # los barrotes de las esquinas
                        elif brillo:
                            # la llama: mas clara abajo en el centro, donde esta la mecha
                            d = abs(x - (w - 1) / 2) + max(0, 2 - y) * 0.7
                            c = AMBAR[4] if d < 0.8 and y >= 2 else AMBAR[3] if d < 1.6 else AMBAR[2]
                        else:
                            # el cristal ahumado (apagado): lo de dentro apenas se ve
                            c = HUMO[1 + (x + y) % 3]
                            if y == h - 1:
                                c = HUMO[3]  # el aceite del fondo
                            if r.random() < 0.08:
                                c = mezcla(c, SAL, 0.5)
                    elif brillo:
                        c = None
                    elif nombre in ("base", "tapa"):
                        c = laton(3.4 if cara == "arriba" else 2.6 if cara in ("frente", "der") else 1.8)
                    elif nombre == "tejadillo":
                        c = laton(3.0 if cara == "arriba" else 2.2)
                    else:
                        c = HIERRO[3] if cara in ("frente", "der", "arriba") else HIERRO[2]
                    if c is not None:
                        L.px(x0 + x, y0 + y, c)
    return L


# ---------------------------------------------------------------- los objetos (16 x 16)

FAROL = [
    "................",
    "......kkkk......",
    ".....k....k.....",
    ".....k....k.....",
    "....kkkkkkkk....",
    "....kBBBBBBk....",
    "...kmGGGGGGmk...",
    "...kmGgLLgGmk...",
    "...kmGLWWLGmk...",
    "...kmGLWWLGmk...",
    "...kmGgLLgGmk...",
    "...kmGGGGGGmk...",
    "....kBBBBBBk....",
    "...kbbbbbbbbk...",
    "....kkkkkkkk....",
    "................",
]


def objeto(ylva):
    L = Lienzo(16, 16)
    if ylva:
        pal = {"k": TINTA, "B": HIERRO[3], "b": HIERRO[2], "m": HIERRO[4], "G": FRIO[0], "g": FRIO[1], "L": FRIO[2], "W": FRIO[4]}
    else:
        pal = {"k": TINTA, "B": LATON[4], "b": LATON[2], "m": LATON[3], "G": HUMO[1], "g": HUMO[2], "L": HUMO[3], "W": AMBAR[0]}
    L.sprite(FAROL, pal)
    if not ylva:
        # la sal y el cardenillo del laton
        for (x, y, c) in ((6, 5, SAL), (9, 12, SAL), (5, 13, VERDIN[1]), (10, 13, SAL), (4, 8, VERDIN[0])):
            L.px(x, y, c)
    else:
        # el asa de hierro y un brillo frio alrededor
        for (x, y) in ((6, 1), (7, 1), (8, 1), (9, 1)):
            L.px(x, y, HIERRO[4])
    return L


def modelos():
    item = os.path.join(ASSETS, "models", "item")
    for n in ("farol_ylva", "farol_ballenero"):
        with open(os.path.join(item, n + ".json"), "w", encoding="utf-8") as f:
            json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/" + n}}, f)


def montar():
    textura().guardar(os.path.join(ASSETS, "textures", "entity", "farol_brann.png"))
    textura(True).guardar(os.path.join(ASSETS, "textures", "entity", "farol_brann_brillo.png"))
    objeto(True).guardar(os.path.join(ASSETS, "textures", "item", "farol_ylva.png"))
    objeto(False).guardar(os.path.join(ASSETS, "textures", "item", "farol_ballenero.png"))
    modelos()
    print("farol: el de Brann (textura y brillo) y los dos objetos")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        textura().imagen().resize((64 * 8, 32 * 8), 0).save(os.path.join(d, "farol_brann.png"))
        hoja = Lienzo(40, 20, "#2a2430")
        hoja.pegar(objeto(True), 2, 2)
        hoja.pegar(objeto(False), 22, 2)
        hoja.imagen().resize((40 * 10, 20 * 10), 0).save(os.path.join(d, "farol_objetos.png"))
        print("vista en", d)
