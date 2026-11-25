#!/usr/bin/env python3
"""
La daga de los bandidos (parte 2f-3): textures/item/daga.png (16 x 16). Hoja corta de acero en diagonal (como las
espadas del juego: la punta arriba a la derecha), guarda de bronce, puno de cuero y pomo.

    python3 tools/rol/daga.py [--vista carpeta]
"""
import os
import sys

from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
SALIDA = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/item/daga.png")

PAL = {
    "k": (26, 22, 30, 255),     # tinta
    "o": (74, 78, 88, 255),     # filo oscuro
    "s": (128, 132, 143, 255),  # acero
    "S": (184, 188, 200, 255),  # acero claro
    "W": (236, 236, 244, 255),  # brillo
    "b": (110, 78, 28, 255),    # bronce oscuro
    "B": (192, 150, 60, 255),   # bronce
    "Y": (226, 191, 98, 255),   # bronce claro
    "l": (58, 36, 22, 255),     # cuero oscuro
    "L": (96, 62, 36, 255),     # cuero
}

DIBUJO = [
    "................",
    "................",
    ".............kk.",
    "............kWk.",
    "...........kWSk.",
    "..........kWSok.",
    ".........kWSok..",
    "........kWSok...",
    "....kk.kWSok....",
    "....kYkkSok.....",
    ".....kBYkk......",
    "....kLkBb.k.....",
    "...kLlk.kYk.....",
    "..kLlk...k......",
    ".kBkk...........",
    ".kk.............",
]


def montar():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, fila in enumerate(DIBUJO):
        for x, ch in enumerate(fila):
            if ch in PAL:
                im.putpixel((x, y), PAL[ch])
    return im


if __name__ == "__main__":
    im = montar()
    os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
    im.save(SALIDA)
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        im.resize((256, 256), Image.NEAREST).save(os.path.join(d, "daga.png"))
    print("daga: hecho")
