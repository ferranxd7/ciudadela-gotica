#!/usr/bin/env python3
"""
Los bloques del mar de nubes (parte 3a), 16 x 16:
  nube.png, nube_1..3.png  la nube de verdad (no se pisa: se cae a traves, al vacio), en cuatro tonos segun lo honda que
                           esta bajo lo alto de su columna: arriba casi blanca, abajo gris lila (asi los bollos tienen
                           volumen sin sombreado por caras: el modelo va sin "shade")
  nube_firme.png           la nube apretada de los caminos: marfil calido con motas de oro gastado (se pisa)
Con ruido suave que casa de lado a lado (no se ven baldosas).

    python3 tools/mundo/nubes.py [--vista carpeta]
"""
import os
import sys

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
SALIDA = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/block")

# color y opacidad de cada tono de la nube (0 = lo alto)
TONOS = [((244, 242, 251), 226), ((230, 228, 244), 228), ((211, 209, 233), 232), ((186, 185, 216), 236)]
FIRME = (233, 222, 203)
ORO = (226, 186, 92)


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


def nube(tono):
    """La nube: su color con muy poco contraste (de lejos no se ven baldosas)."""
    n = 0.5 * ruido(3, 8) + 0.3 * ruido(4, 4) + 0.2 * ruido(5, 2)
    color, alfa = TONOS[tono]
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            c = np.clip(np.array(color) * (0.975 + 0.025 * n[y, x]), 0, 255).astype(int)
            im.putpixel((x, y), (int(c[0]), int(c[1]), int(c[2]), alfa))
    return im


def firme():
    n = 0.5 * ruido(7, 8) + 0.3 * ruido(8, 4) + 0.2 * ruido(9, 2)
    im = Image.new("RGBA", (16, 16))
    destellos = {(3, 4), (11, 9), (6, 13), (13, 2)}
    for y in range(16):
        for x in range(16):
            c = np.array(FIRME) * (0.93 + 0.07 * n[y, x])
            if (x, y) in destellos:
                c = np.array(ORO)
            c = np.clip(c, 0, 255).astype(int)
            im.putpixel((x, y), (int(c[0]), int(c[1]), int(c[2]), 244))
    return im


if __name__ == "__main__":
    os.makedirs(SALIDA, exist_ok=True)
    hechas = [("nube" if t == 0 else "nube_%d" % t, nube(t)) for t in range(4)] + [("nube_firme", firme())]
    for nombre, im in hechas:
        im.save(os.path.join(SALIDA, nombre + ".png"))
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        # sobre el azul oscuro de debajo de las nubes y sobre el cielo
        for fondo, sufijo in (((40, 60, 110, 255), "vacio"), ((112, 144, 208, 255), "cielo")):
            mosaico = Image.new("RGBA", (16 * 5 * 3, 48), fondo)
            for k, (nombre, im) in enumerate(hechas):
                for i in range(3):
                    for j in range(3):
                        mosaico.alpha_composite(im, (k * 48 + i * 16, j * 16))
            mosaico.resize((mosaico.width * 4, mosaico.height * 4), Image.NEAREST).save(os.path.join(d, "nubes_" + sufijo + ".png"))
    print("nubes: hecho")
