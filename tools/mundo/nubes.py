#!/usr/bin/env python3
"""
Los bloques del mar de nubes (parte 3a), 16 x 16:
  nube.png        la nube de verdad: blanca y lila, translucida (no se pisa: se cae a traves, al vacio)
  nube_firme.png  la nube apretada de los caminos: mas densa y con un brillo de oro gastado (se pisa)
Con ruido suave que casa de lado a lado (no se ven baldosas).

    python3 tools/mundo/nubes.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
SALIDA = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/block")


def ruido(semilla, escala):
    """Ruido de valor periodico de 16 x 16 (se repite sin costura)."""
    r = np.random.default_rng(semilla)
    n = 16 // escala
    g = r.random((n, n))
    out = np.zeros((16, 16))
    for y in range(16):
        for x in range(16):
            fx, fy = x / escala, y / escala
            x0, y0 = int(fx) % n, int(fy) % n
            x1, y1 = (x0 + 1) % n, (y0 + 1) % n
            tx, ty = fx - int(fx), fy - int(fy)
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = g[y0, x0] * (1 - tx) + g[y0, x1] * tx
            b = g[y1, x0] * (1 - tx) + g[y1, x1] * tx
            out[y, x] = a * (1 - ty) + b * ty
    return out


def nube(firme):
    n = 0.5 * ruido(3 if not firme else 7, 8) + 0.3 * ruido(4 if not firme else 8, 4) + 0.2 * ruido(5 if not firme else 9, 2)
    im = Image.new("RGBA", (16, 16))
    destellos = {(3, 4), (11, 9), (6, 13)}
    for y in range(16):
        for x in range(16):
            v = n[y, x]
            if firme:
                c = np.array([200, 192, 210]) * (0.92 + 0.08 * v)
                if (x, y) in destellos:
                    c = np.array([226, 191, 98])
                a = 238
            else:
                # blanco algo lila, con poco contraste (de lejos no se ven baldosas)
                c = np.array([218, 210, 230]) * (0.93 + 0.07 * v)
                a = int(176 + 36 * v)
            c = np.clip(c, 0, 255).astype(int)
            im.putpixel((x, y), (int(c[0]), int(c[1]), int(c[2]), a))
    return im


if __name__ == "__main__":
    os.makedirs(SALIDA, exist_ok=True)
    for nombre, firme in (("nube", False), ("nube_firme", True)):
        im = nube(firme)
        im.save(os.path.join(SALIDA, nombre + ".png"))
        if "--vista" in sys.argv:
            d = sys.argv[sys.argv.index("--vista") + 1]
            os.makedirs(d, exist_ok=True)
            mosaico = Image.new("RGBA", (64, 64), (20, 12, 30, 255))
            for i in range(4):
                for j in range(4):
                    mosaico.alpha_composite(im, (i * 16, j * 16))
            mosaico.resize((256, 256), Image.NEAREST).save(os.path.join(d, nombre + ".png"))
    print("nubes: hecho")
