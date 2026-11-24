#!/usr/bin/env python3
"""
El Espejo de la Sombra redondo, en pixel art (el minimapa de arriba a la derecha). Una textura por tamano, dibujada al
tamano real para que cada pixel caiga en su sitio (nada de escalar):

  assets/darkcraft/textures/gui/espejo_rN.png   (N = radio del cristal: 28, 36 y 46)

A la izquierda el marco (aro de piedra con filo de oro, cuatro gemas rojas y arriba la cresta con el ojo de la Sombra)
y a la derecha el cristal que va encima del mapa (penumbra tramada hacia el borde y un reflejo). Las medidas las lee
Java de aqui: lado de cada mitad = 2 * (N + BORDE) + 1, y la cresta sube CRESTA pixeles por encima del aro.

    python3 tools/ui/espejo.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import GUI, ORO, PIEDRA, ROJO, TINTA, Lienzo, rgba  # noqa: E402

RADIOS = (28, 36, 46)
BORDE = 8     # grosor del marco por fuera del cristal
CRESTA = 7    # lo que sobresale la cresta por arriba

LUZ = (-0.62, -0.78)

CRESTA_SPR = [
    "....k.............k....",
    "...kok...........kok...",
    "...koOk.........kOok...",
    "..kkoOOk..kkk..kOOokk..",
    ".kooOOOkkkrRrkkkOOOook.",
    "kooOOkkrrRROORRrrkkOOok",
    "koOOkrRROOYkkYOORRrkOOk",
    "koOOkrRROOYkkYOORRrkOOk",
    ".kOOOkkrrRROORRrrkkOOk.",
    "..kkOOOkkkrRrkkkOOOkk..",
    "....kkOOOkkkkkOOOkk....",
    "......kkkOOOOOkkk......",
    ".........kkkkk.........",
]
CRESTA_PAL = {"k": TINTA, "o": ORO[5], "O": ORO[3], "r": ROJO[2], "R": ROJO[4], "Y": ROJO[6]}

GEMA = [
    ".kkk.",
    "kRYRk",
    "kYWRk",
    "kRRrk",
    ".kkk.",
]
GEMA_PAL = {"k": TINTA, "R": ROJO[4], "Y": ROJO[5], "W": "#ffd8c8", "r": ROJO[2]}

# tramado ordenado 4x4 (para la penumbra del cristal)
BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16.0


def marco(R):
    lado = 2 * (R + BORDE) + 1
    L = Lienzo(lado, lado + CRESTA)
    c = R + BORDE
    cy = c + CRESTA
    rng = np.random.default_rng(100 + R)
    for y in range(L.h):
        for x in range(L.w):
            dx, dy = x - c, y - cy
            r = math.hypot(dx, dy)
            if r > R + BORDE - 0.4 or r <= R - 0.5:
                continue
            n = (dx * LUZ[0] + dy * LUZ[1]) / max(r, 1e-6)   # 1 = mirando a la luz
            d = r - R
            if d > BORDE - 1.4:
                col = TINTA
            elif d > 3.6:
                # aro de piedra: abombado, claro hacia la luz
                u = (d - 3.6) / (BORDE - 1.4 - 3.6)          # 0 dentro, 1 fuera
                i = 3 + n * 2.2 + (0.9 - abs(u - 0.5) * 2.4)
                i += (rng.random() - 0.5) * 0.9
                col = PIEDRA[int(max(1, min(7, round(i))))]
            elif d > 2.6:
                col = PIEDRA[0]
            elif d > 0.6:
                # filo de oro por dentro (la luz le da por abajo a la derecha, como a un bisel hacia dentro)
                i = 3 - n * 2.4 + (0.6 if d > 1.6 else -0.6)
                col = ORO[int(max(0, min(6, round(i))))]
            else:
                col = TINTA
            L.px(x, y, col)
    # tachuelas de oro en los puntos cardinales de abajo y los lados; gemas rojas en las diagonales
    rm = R + 5.4
    for ang in (0, 90, 180):
        x, y = int(round(c + math.cos(math.radians(ang)) * rm)), int(round(cy + math.sin(math.radians(ang)) * rm))
        L.px(x, y, ORO[5]).px(x + 1, y, ORO[3]).px(x, y + 1, ORO[2]).px(x - 1, y, ORO[4]).px(x, y - 1, ORO[6])
    for ang in (45, 135, 225, 315):
        x, y = int(round(c + math.cos(math.radians(ang)) * rm)), int(round(cy + math.sin(math.radians(ang)) * rm))
        L.sprite(GEMA, GEMA_PAL, x - 2, y - 2)
    # la cresta, a caballo del aro por arriba
    cw = len(CRESTA_SPR[0])
    L.sprite(CRESTA_SPR, CRESTA_PAL, c - cw // 2, 0)
    return L


def cristal(R):
    lado = 2 * (R + BORDE) + 1
    L = Lienzo(lado, lado + CRESTA)
    c = R + BORDE
    cy = c + CRESTA
    for y in range(L.h):
        for x in range(L.w):
            dx, dy = x - c, y - cy
            r = math.hypot(dx, dy)
            if r > R + 0.5:
                continue
            # penumbra hacia el borde, tramada (rojo muy oscuro)
            k = max(0.0, (r / R - 0.55) / 0.45)
            k = k * k
            umbral = BAYER[y % 4, x % 4]
            a = 0
            if k > umbral * 0.95 + 0.05:
                a = 150 if k > 0.75 else 105
            elif k > 0.03:
                a = int(60 * k / (umbral + 0.3))
            if a:
                L.px(x, y, rgba("#0a0206", min(190, a)))
    # el reflejo: un arco fino arriba a la izquierda
    for ang in np.arange(200, 262, 0.6):
        for rr, al in ((R - 4.0, 54), (R - 5.0, 30)):
            x, y = int(round(c + math.cos(math.radians(ang)) * rr)), int(round(cy + math.sin(math.radians(ang)) * rr))
            k = 1 - abs((ang - 231) / 31)
            L.px(x, y, (255, 240, 236, int(al * max(0.25, k))))
    return L


def generar():
    for R in RADIOS:
        m, cr = marco(R), cristal(R)
        hoja = Lienzo(m.w * 2, m.h)
        hoja.pegar(m, 0, 0).pegar(cr, m.w, 0)
        hoja.guardar(os.path.join(GUI, f"espejo_r{R}.png"))
        print(f"espejo_r{R}.png {hoja.w}x{hoja.h}")
    return {R: (marco(R), cristal(R)) for R in RADIOS}


if __name__ == "__main__":
    s = generar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        ancho = sum(m.w + 6 for m, _ in s.values()) + 6
        alto = max(m.h for m, _ in s.values()) + 12
        fondo = Lienzo(ancho, alto, "#3a4a5c")
        x = 6
        for R, (m, cr) in s.items():
            # un mapa de mentira debajo
            for yy in range(m.h):
                for xx in range(m.w):
                    if math.hypot(xx - (R + BORDE), yy - (R + BORDE + CRESTA)) <= R:
                        v = (xx * 7 + yy * 13) % 23
                        fondo.px(x + xx, 6 + yy, (40 + v, 52 + v, 36 + v // 2, 255))
            fondo.pegar(cr, x, 6).pegar(m, x, 6)
            x += m.w + 6
        fondo.imagen().resize((fondo.w * 4, fondo.h * 4), 0).save(os.path.join(d, "espejo_redondo.png"))
        print("vista en", d)
