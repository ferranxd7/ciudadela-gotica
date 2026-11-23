#!/usr/bin/env python3
"""Texturas de la Fortaleza de Hueso (Parte 3): la reja de hueso, el portal de la Sombra (animado), el Amuleto de Hueso
y el libro del acertijo. Pixel art procedural, siempre igual (semilla fija).

Uso: python3 tools/fortaleza/texturas.py   (escribe en src/main/resources/assets/darkcraft/textures/)
"""
import json
import math
import os

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.normpath(os.path.join(AQUI, "..", ".."))
TEX = os.path.join(RAIZ, "src", "main", "resources", "assets", "darkcraft", "textures")
R = np.random.default_rng(1977)


def guardar(img, ruta):
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    Image.fromarray(img.astype(np.uint8), "RGBA").save(ruta)
    print("  ", os.path.relpath(ruta, RAIZ))


def hueso(v):
    """Color de hueso para un valor 0..1 (sombra -> luz)."""
    osc = np.array([118, 104, 82])
    med = np.array([206, 196, 168])
    luz = np.array([240, 234, 214])
    if v < 0.5:
        return osc + (med - osc) * (v / 0.5)
    return med + (luz - med) * ((v - 0.5) / 0.5)


# ---------------------------------------------------------------- la reja de hueso

def reja():
    img = np.zeros((16, 16, 4))
    # tres barrotes de hueso (con nudos) y dos travesanos
    for bx in (1, 7, 13):
        for y in range(16):
            for dx in range(2 if bx != 7 else 2):
                x = bx + dx
                v = 0.72 if dx == 0 else 0.48
                if y in (0, 15):
                    v += 0.1
                v += R.uniform(-0.06, 0.06)
                img[y, x, :3] = hueso(min(1, max(0, v)))
                img[y, x, 3] = 255
            # nudos (como las epifisis de un hueso) arriba y abajo
        for y in (0, 1, 14, 15):
            for x in (bx - 1, bx + 2):
                if 0 <= x < 16:
                    img[y, x, :3] = hueso(0.6 + R.uniform(-0.05, 0.05))
                    img[y, x, 3] = 255
    for ty in (4, 11):
        for x in range(16):
            for dy in range(2):
                y = ty + dy
                v = 0.66 if dy == 0 else 0.42
                img[y, x, :3] = hueso(min(1, max(0, v + R.uniform(-0.06, 0.06))))
                img[y, x, 3] = 255
    # vetas oscuras
    for _ in range(10):
        x, y = R.integers(0, 16), R.integers(0, 16)
        if img[y, x, 3] > 0:
            img[y, x, :3] *= 0.78
    guardar(img, os.path.join(TEX, "block", "reja_hueso.png"))
    borde = np.zeros((16, 16, 4))
    for y in range(16):
        for x in range(16):
            v = 0.55 + 0.25 * math.sin(y * 0.9 + x * 0.2) * 0.5 + R.uniform(-0.07, 0.07)
            borde[y, x, :3] = hueso(min(1, max(0, v)))
            borde[y, x, 3] = 255
    guardar(borde, os.path.join(TEX, "block", "reja_hueso_borde.png"))


# ---------------------------------------------------------------- el portal de la Sombra (animado)

def portal(frames=32):
    out = np.zeros((16 * frames, 16, 4))
    for f in range(frames):
        t = f / frames
        for y in range(16):
            for x in range(16):
                dx, dy = x - 7.5, y - 7.5
                r = math.hypot(dx, dy) / 10.6
                a = math.atan2(dy, dx)
                # dos brazos de remolino que giran hacia dentro, y un latido en el centro
                brazo = 0.5 + 0.5 * math.sin(2 * a + r * 7.0 - t * math.tau * 2)
                brazo2 = 0.5 + 0.5 * math.sin(3 * a - r * 5.0 + t * math.tau + 1.3)
                v = 0.62 * brazo ** 2.2 + 0.25 * brazo2 ** 3 + 0.35 * max(0, 1 - r * 2.4) * (0.7 + 0.3 * math.sin(t * math.tau * 2))
                v = min(1.0, v)
                oscuro = np.array([10, 2, 8])
                carmesi = np.array([140, 12, 30])
                brillo = np.array([255, 96, 72])
                if v < 0.55:
                    c = oscuro + (carmesi - oscuro) * (v / 0.55)
                else:
                    c = carmesi + (brillo - carmesi) * ((v - 0.55) / 0.45)
                c = c * (1.0 - 0.25 * r)
                out[f * 16 + y, x, :3] = np.clip(c, 0, 255)
                out[f * 16 + y, x, 3] = 220 + 35 * v
    guardar(out, os.path.join(TEX, "block", "portal_sombra.png"))
    with open(os.path.join(TEX, "block", "portal_sombra.png.mcmeta"), "w") as fh:
        json.dump({"animation": {"frametime": 2, "interpolate": True}}, fh)
    print("   + portal_sombra.png.mcmeta")


# ---------------------------------------------------------------- el Amuleto de Hueso

AMULETO = [
    "......cc........",
    ".....c..c.......",
    "....c....c......",
    "...c......c.....",
    "...c......c.....",
    "....c....c......",
    ".....nNNn.......",
    "....nBBBBn......",
    "...nBbRRbBn.....",
    "...NBRrrRBN.....",
    "...NBRrwRBN.....",
    "...nBbRRbBn.....",
    "....nBBBBn......",
    ".....nBBn.......",
    "......nn........",
    "................",
]


def amuleto():
    img = np.zeros((16, 16, 4))
    pal = {
        "c": (126, 118, 132),   # cadena de netherita
        "n": (52, 46, 50),      # engaste de netherita
        "N": (88, 80, 84),
        "B": None,              # hueso
        "b": None,
        "R": (150, 18, 30),     # la gema roja
        "r": (214, 40, 46),
        "w": (255, 196, 180),
    }
    for y, fila in enumerate(AMULETO):
        for x, ch in enumerate(fila):
            if ch == ".":
                continue
            if ch == "B":
                col = hueso(0.62 + 0.18 * (1 - y / 16) + R.uniform(-0.05, 0.05))
            elif ch == "b":
                col = hueso(0.36)
            else:
                col = np.array(pal[ch])
            img[y, x, :3] = col
            img[y, x, 3] = 255
    # desplazar un poco a la derecha para centrarlo
    img = np.roll(img, 2, axis=1)
    guardar(img, os.path.join(TEX, "item", "amuleto_hueso.png"))


# ---------------------------------------------------------------- el libro del acertijo

LIBRO = [
    "................",
    "...LLLLLLLLLL...",
    "..LPPPPPPPPPPL..",
    "..LPhhhhhhhhPL..",
    "..LPhhhhhhhhPL..",
    "..LPhhhhhhhhPL..",
    "..LPhhhRRhhhPL..",
    "..LPhhRrrRhhPL..",
    "..LPhhRrrRhhPL..",
    "..LPhhhRRhhhPL..",
    "..LPhhhhhhhhPL..",
    "..LPhhhhhhhhPL..",
    "..LPPPPPPPPPPL..",
    "..LppppppppppL..",
    "...LLLLLLLLLL...",
    "................",
]


def libro():
    img = np.zeros((16, 16, 4))
    for y, fila in enumerate(LIBRO):
        for x, ch in enumerate(fila):
            if ch == ".":
                continue
            if ch == "L":
                col = np.array([40, 24, 20])
            elif ch == "P":
                col = hueso(0.5 + R.uniform(-0.05, 0.05))
            elif ch == "p":
                col = np.array([226, 214, 186])
            elif ch == "h":
                col = hueso(0.7 + 0.2 * math.sin(x * 1.3 + y * 0.7) * 0.5 + R.uniform(-0.04, 0.04))
            elif ch == "R":
                col = np.array([128, 14, 22])
            else:
                col = np.array([196, 34, 40])
            img[y, x, :3] = col
            img[y, x, 3] = 255
    guardar(img, os.path.join(TEX, "item", "libro_acertijo.png"))


if __name__ == "__main__":
    reja()
    portal()
    amuleto()
    libro()
