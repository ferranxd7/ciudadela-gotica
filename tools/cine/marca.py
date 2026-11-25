#!/usr/bin/env python3
"""La marca de la Sombra (la cinematica de inicio): el ojo de la Sombra dentro de un aro roto del que caen tres espinas.
Sale `textures/gui/marca_sombra.png`, de 128 x 64: a la izquierda la marca (64 x 64, pixel art: nucleo claro y filo rojo
oscuro) y a la derecha su resplandor (la misma forma desenfocada), que el juego pinta sumando luz y latiendo.

Uso: python3 tools/cine/marca.py [vista.png]
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

AQUI = os.path.dirname(os.path.abspath(__file__))
SALIDA = os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft/textures/gui/marca_sombra.png")
N = 64
C = 31.5  # centro


def forma():
    """La marca en blanco y negro a 8 veces (para que las curvas salgan limpias al reducir con umbral)."""
    k = 8
    im = Image.new("L", (N * k, N * k), 0)
    d = ImageDraw.Draw(im)

    def p(x, y):
        return (x * k, y * k)

    cx, cy = C + 0.5, C - 2.5
    # el aro, roto abajo (de 62 a 118 grados contando desde la derecha hacia abajo) y con dos muescas arriba
    r0, r1 = 21.0, 18.6
    for a0, a1 in ((122, 236), (246, 294), (304, 418)):
        d.pieslice([p(cx - r0, cy - r0), p(cx + r0, cy + r0)], a0, a1, fill=255)
    d.ellipse([p(cx - r1, cy - r1), p(cx + r1, cy + r1)], fill=0)
    # el ojo: dos arcos que se cortan (una almendra) con el hueco dentro
    def almendra(ancho, alto, relleno):
        pts = []
        for i in range(0, 41):
            u = i / 40.0
            pts.append(p(cx - ancho + 2 * ancho * u, cy - alto * math.sin(math.pi * u) ** 0.85))
        for i in range(40, -1, -1):
            u = i / 40.0
            pts.append(p(cx - ancho + 2 * ancho * u, cy + alto * math.sin(math.pi * u) ** 0.85))
        d.polygon(pts, fill=relleno)
    almendra(15.0, 8.2, 255)
    almendra(12.6, 6.0, 0)
    # la pupila: una rendija
    d.polygon([p(cx, cy - 6.4), p(cx + 1.7, cy), p(cx, cy + 6.4), p(cx - 1.7, cy)], fill=255)
    # las tres espinas que caen del hueco del aro
    for dx, largo, ancho in ((0, 15.5, 2.3), (-6.2, 9.0, 1.7), (6.2, 9.0, 1.7)):
        y0 = cy + 16.5 - abs(dx) * 0.28
        d.polygon([p(cx + dx - ancho, y0), p(cx + dx + ancho, y0), p(cx + dx, y0 + largo)], fill=255)
    # cuatro marcas cortas por fuera del aro, en las diagonales de arriba y a los lados
    for a in (200, 340, 232, 308):
        ca, sa = math.cos(math.radians(a)), math.sin(math.radians(a))
        x0, y0, x1, y1 = cx + ca * 23.0, cy + sa * 23.0, cx + ca * 27.0, cy + sa * 27.0
        d.line([p(x0, y0), p(x1, y1)], fill=255, width=int(1.7 * k))
    return im.resize((N, N), Image.LANCZOS).point(lambda v: 255 if v >= 120 else 0)


def main():
    f = forma()
    px = f.load()
    marca = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    m = marca.load()
    dentro = lambda x, y: 0 <= x < N and 0 <= y < N and px[x, y] > 0
    for y in range(N):
        for x in range(N):
            if dentro(x, y):
                # el nucleo, casi blanco de caliente; el borde de la forma, naranja y rojo
                borde = sum(1 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)) if not dentro(x + dx, y + dy))
                m[x, y] = (255, 236, 196, 255) if borde == 0 else ((255, 150, 70, 255) if borde == 1 else (232, 84, 40, 255))
            elif any(dentro(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                m[x, y] = (120, 16, 10, 255)   # el filo quemado
            elif any(dentro(x + dx, y + dy) for dx, dy in ((1, 1), (-1, 1), (1, -1), (-1, -1))):
                m[x, y] = (60, 6, 4, 150)
    # el resplandor: la forma engordada y desenfocada, en rojo
    g = f.filter(ImageFilter.MaxFilter(5)).filter(ImageFilter.GaussianBlur(4.2))
    gp = g.load()
    brillo = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    b = brillo.load()
    for y in range(N):
        for x in range(N):
            v = min(255, int(gp[x, y] * 1.5))
            if v > 3:
                b[x, y] = (255, 60 + v // 5, 30, v)
    hoja = Image.new("RGBA", (2 * N, N), (0, 0, 0, 0))
    hoja.paste(marca, (0, 0))
    hoja.paste(brillo, (N, 0))
    os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
    hoja.save(SALIDA)
    print("escrito", os.path.normpath(SALIDA))
    if len(sys.argv) > 1:
        z = 6
        vista = Image.new("RGBA", (2 * N * z, N * z), (4, 2, 3, 255))
        luz = brillo.resize((N * z, N * z), Image.NEAREST)
        vista.alpha_composite(luz, (0, 0))
        vista.alpha_composite(marca.resize((N * z, N * z), Image.NEAREST), (0, 0))
        vista.alpha_composite(marca.resize((N * z, N * z), Image.NEAREST), (N * z, 0))
        vista.save(sys.argv[1])
        print("vista", sys.argv[1])


if __name__ == "__main__":
    main()
