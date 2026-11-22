#!/usr/bin/env python3
"""Texturas de la torre del arbol: la gran puerta (dos hojas de madera negra con herrajes, un arbol de oro tallado
y runas) y el disco de runas del ascensor. Cada una con su mapa de brillo (lo que se enciende)."""
import math
import os
import random

from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), "..", "..", "src", "main", "resources", "assets", "ciudadela", "textures", "entity")


def clamp(v, a=0, b=255):
    return max(a, min(b, int(round(v))))


def mix(c1, c2, t):
    n = min(len(c1), len(c2), 3)
    return tuple(clamp(c1[i] + (c2[i] - c1[i]) * t) for i in range(n))


class Lienzo:
    def __init__(self, w, h, fondo=(0, 0, 0, 0)):
        self.w, self.h = w, h
        self.im = Image.new("RGBA", (w, h), fondo)
        self.px = self.im.load()

    def put(self, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < self.w and 0 <= y < self.h:
            if len(c) == 3:
                c = c + (255,)
            self.px[x, y] = c

    def get(self, x, y):
        return self.px[int(x), int(y)]

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.put(x, y, c)


# ------------------------------------------------------------------ paleta
MADERA = [(28, 20, 17), (32, 23, 19), (25, 18, 15), (35, 25, 20), (30, 21, 18)]
VETA = (16, 11, 9)
JUNTA = (9, 6, 5)
HIERRO = (58, 58, 66)
HIERRO_L = (96, 96, 106)
HIERRO_S = (30, 30, 35)
REMACHE = (130, 128, 138)
ORO = (200, 149, 42)
ORO_L = (246, 214, 112)
ORO_S = (120, 82, 16)
ORO_O = (52, 34, 8)
BRILLO = (255, 206, 104)
BRILLO_2 = (255, 160, 60)


def tablas(c, x0, y0, w, h, rnd, ancho=8):
    """Tablones verticales de madera negra con vetas y juntas."""
    for i in range(0, w, ancho):
        base = MADERA[rnd.randrange(len(MADERA))]
        for x in range(x0 + i, min(x0 + i + ancho, x0 + w)):
            for y in range(y0, y0 + h):
                n = rnd.random()
                col = mix(base, (0, 0, 0), 0.12 * n)
                c.put(x, y, col)
        # vetas
        for _ in range(5):
            vx = x0 + i + 1 + rnd.randrange(max(1, ancho - 2))
            vy = y0 + rnd.randrange(h)
            ln = rnd.randrange(6, 26)
            for k in range(ln):
                yy = vy + k
                if yy >= y0 + h:
                    break
                xx = vx + (1 if (k // 7) % 2 and rnd.random() < 0.3 else 0)
                if xx < x0 + i + ancho - 1:
                    c.put(xx, yy, VETA)
        # junta
        if i > 0:
            for y in range(y0, y0 + h):
                c.put(x0 + i, y, JUNTA)


def banda_hierro(c, x0, x1, y0, alto, rnd):
    for y in range(y0, y0 + alto):
        for x in range(x0, x1):
            t = (y - y0) / max(1, alto - 1)
            col = HIERRO_L if y == y0 else (HIERRO_S if y == y0 + alto - 1 else mix(HIERRO, HIERRO_S, t * 0.5 + rnd.random() * 0.15))
            c.put(x, y, col)
    for x in range(x0 + 3, x1 - 2, 7):
        cy = y0 + alto // 2
        c.put(x, cy, REMACHE)
        c.put(x + 1, cy, HIERRO_L)
        c.put(x, cy + 1, HIERRO_S)
        c.put(x + 1, cy + 1, HIERRO_S)


def marco_hierro(c, x0, y0, w, h, grosor=3):
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            d = min(x - x0, y - y0, x0 + w - 1 - x, y0 + h - 1 - y)
            if d < grosor:
                col = HIERRO_L if d == 0 and (x == x0 or y == y0) else (HIERRO_S if d == 0 else HIERRO)
                c.put(x, y, col)
    for y in range(y0 + 6, y0 + h - 4, 12):
        for x in (x0 + 1, x0 + w - 2):
            c.put(x, y, REMACHE)


# ------------------------------------------------------------------ el arbol de oro
def bezier(p0, p1, p2, p3, n):
    out = []
    for i in range(n + 1):
        t = i / n
        a = (1 - t) ** 3
        b = 3 * (1 - t) ** 2 * t
        cc = 3 * (1 - t) * t * t
        d = t ** 3
        out.append((a * p0[0] + b * p1[0] + cc * p2[0] + d * p3[0], a * p0[1] + b * p1[1] + cc * p2[1] + d * p3[1]))
    return out


class Oro:
    """Acumula trazos (centro + radio) y luego pinta relieve: contorno oscuro, cuerpo, luz arriba-izquierda, vena."""

    def __init__(self, w, h):
        self.w, self.h = w, h
        self.r = [[-1.0] * w for _ in range(h)]   # 'altura' del relieve (radio relativo)
        self.vena = [[False] * w for _ in range(h)]

    def trazo(self, pts, r0, r1, vena=True):
        n = len(pts)
        for i, (x, y) in enumerate(pts):
            rr = r0 + (r1 - r0) * i / max(1, n - 1)
            ir = int(math.ceil(rr)) + 1
            for yy in range(int(y) - ir, int(y) + ir + 1):
                for xx in range(int(x) - ir, int(x) + ir + 1):
                    if not (0 <= xx < self.w and 0 <= yy < self.h):
                        continue
                    d = math.hypot(xx + 0.5 - x, yy + 0.5 - y)
                    if d <= rr:
                        h = 1 - d / max(rr, 0.01)
                        if h > self.r[yy][xx]:
                            self.r[yy][xx] = h
            if vena and rr >= 1.6:
                self.vena[int(y)][int(x)] = True

    def hoja(self, x, y, rr):
        self.trazo([(x, y)], rr, rr, vena=False)

    def pintar(self, c, g, ox=0, oy=0):
        for y in range(self.h):
            for x in range(self.w):
                h = self.r[y][x]
                if h < 0:
                    # contorno: tallado oscuro alrededor
                    vecino = any(0 <= x + dx < self.w and 0 <= y + dy < self.h and self.r[y + dy][x + dx] >= 0
                                 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                    if vecino:
                        c.put(ox + x, oy + y, ORO_O)
                    continue
                # luz desde arriba a la izquierda
                hl = self.r[y - 1][x - 1] if x > 0 and y > 0 else -1
                if hl < 0 or hl < h - 0.25:
                    col = ORO_L
                elif h < 0.25:
                    col = ORO_S
                else:
                    col = mix(ORO, ORO_L, h * 0.45)
                c.put(ox + x, oy + y, col)
                if self.vena[y][x]:
                    c.put(ox + x, oy + y, mix(ORO_L, (255, 255, 230), 0.4))
                    g.put(ox + x, oy + y, BRILLO)
                elif h > 0.75 and g is not None:
                    g.put(ox + x, oy + y, mix((0, 0, 0), BRILLO_2, 0.35))


def arbol(c, g, w, h, rnd):
    """El arbol tallado: tronco en el centro (la junta de las hojas), ramas abiertas, raices y hojas."""
    o = Oro(w, h)
    cx = w / 2
    # tronco
    o.trazo(bezier((cx, 152), (cx - 1, 128), (cx + 1, 100), (cx, 66), 80), 8.5, 4.0)
    # raices
    for s in (-1, 1):
        o.trazo(bezier((cx, 146), (cx + s * 10, 150), (cx + s * 22, 151), (cx + s * 38, 156), 50), 3.6, 1.2)
        o.trazo(bezier((cx + s * 4, 140), (cx + s * 12, 146), (cx + s * 16, 154), (cx + s * 20, 158), 30), 2.6, 1.0)
        o.trazo(bezier((cx + s * 20, 152), (cx + s * 26, 148), (cx + s * 30, 150), (cx + s * 32, 145), 20), 1.6, 0.8)
    # ramas principales
    for s in (-1, 1):
        rama = bezier((cx, 74), (cx + s * 6, 60), (cx + s * 22, 52), (cx + s * 36, 30), 70)
        o.trazo(rama, 4.2, 1.6)
        sub = bezier((cx + s * 14, 56), (cx + s * 22, 58), (cx + s * 32, 62), (cx + s * 44, 56), 40)
        o.trazo(sub, 2.6, 1.0)
        sub2 = bezier((cx + s * 28, 40), (cx + s * 30, 30), (cx + s * 26, 22), (cx + s * 20, 14), 30)
        o.trazo(sub2, 2.0, 0.8)
        sub3 = bezier((cx + s * 4, 84), (cx + s * 14, 82), (cx + s * 24, 88), (cx + s * 34, 84), 30)
        o.trazo(sub3, 2.2, 0.8)
        top = bezier((cx + s * 2, 66), (cx + s * 4, 46), (cx + s * 8, 30), (cx + s * 6, 12), 50)
        o.trazo(top, 2.8, 1.0)
        # ramitas con espiral al final
        for (bx, by, ang) in ((cx + s * 36, 30, -90 - s * 40), (cx + s * 44, 56, -s * 20), (cx + s * 34, 84, s * 10), (cx + s * 20, 14, -90)):
            pts = []
            for k in range(18):
                a = math.radians(ang) + s * k * 0.32
                rr = 6 - k * 0.3
                pts.append((bx + math.cos(a) * rr * 0.6 + s * k * 0.15, by + math.sin(a) * rr * 0.6))
            o.trazo(pts, 1.1, 0.6, vena=False)
    # hojas: gotas de oro en las puntas
    puntas = []
    for s in (-1, 1):
        puntas += [(cx + s * 36, 30), (cx + s * 44, 56), (cx + s * 20, 14), (cx + s * 34, 84), (cx + s * 6, 12), (cx + s * 26, 44), (cx + s * 40, 42)]
    for (px, py) in puntas:
        for _ in range(9):
            a = rnd.random() * math.pi * 2
            d = 3 + rnd.random() * 7
            o.hoja(px + math.cos(a) * d, py + math.sin(a) * d * 0.8, 1.3 + rnd.random() * 0.8)
    o.pintar(c, g)
    # bayas encendidas entre las hojas
    for (px, py) in puntas:
        for _ in range(3):
            a = rnd.random() * math.pi * 2
            d = 2 + rnd.random() * 8
            x, y = int(px + math.cos(a) * d), int(py + math.sin(a) * d * 0.8)
            c.put(x, y, (255, 236, 170))
            g.put(x, y, (255, 220, 130))


# ------------------------------------------------------------------ runas
RUNAS = [
    [(0, 0), (0, 1), (0, 2), (0, 3), (0, 4), (1, 1), (2, 2)],
    [(1, 0), (1, 1), (1, 2), (1, 3), (1, 4), (0, 1), (2, 1), (0, 3), (2, 3)],
    [(0, 0), (1, 1), (2, 2), (1, 3), (0, 4), (2, 0), (2, 4)],
    [(0, 0), (0, 1), (0, 2), (0, 3), (0, 4), (1, 2), (2, 1), (2, 3)],
    [(1, 0), (0, 1), (2, 1), (1, 2), (1, 3), (1, 4), (0, 4), (2, 4)],
    [(0, 0), (2, 0), (1, 1), (1, 2), (0, 3), (2, 3), (0, 4), (2, 4)],
    [(0, 0), (1, 0), (2, 0), (1, 1), (1, 2), (0, 3), (2, 3), (1, 4)],
    [(0, 1), (1, 0), (2, 1), (1, 2), (1, 3), (0, 4), (1, 4), (2, 4)],
]


def runa(c, g, x, y, k, tallada=True):
    for (dx, dy) in RUNAS[k % len(RUNAS)]:
        c.put(x + dx, y + dy, ORO if tallada else ORO_L)
        if g is not None:
            g.put(x + dx, y + dy, BRILLO)
    # sombra del tallado
    for (dx, dy) in RUNAS[k % len(RUNAS)]:
        if (dx + 1, dy + 1) not in RUNAS[k % len(RUNAS)]:
            q = c.get(min(c.w - 1, x + dx + 1), min(c.h - 1, y + dy + 1))
            if q[:3] not in (ORO, ORO_L):
                c.put(x + dx + 1, y + dy + 1, ORO_O)


# ------------------------------------------------------------------ la puerta
def puerta():
    rnd = random.Random(1717)
    W, H = 256, 256
    c = Lienzo(W, H)
    g = Lienzo(W, H, (0, 0, 0, 255))
    # ---- cara de la recepcion (112 x 160: las dos hojas juntas, la junta en x = 56)
    tablas(c, 0, 0, 112, 160, rnd)
    for hoja in (0, 56):
        marco_hierro(c, hoja, 0, 56, 160, 3)
    for yb in (16, 126):
        banda_hierro(c, 3, 53, yb, 5, rnd)
        banda_hierro(c, 59, 109, yb, 5, rnd)
    # bisagras grandes en los bordes de fuera
    for yb in (16, 70, 126):
        for x0, x1 in ((0, 14), (98, 112)):
            for y in range(yb - 1, yb + 7):
                for x in range(x0, x1):
                    t = abs(x - (x0 if x0 == 0 else x1 - 1)) / 14
                    c.put(x, y, mix(HIERRO_L, HIERRO_S, t + (0.2 if y in (yb - 1, yb + 6) else 0)))
            c.put(x0 + 3 if x0 == 0 else x1 - 4, yb + 2, REMACHE)
    # arco apuntado dorado que enmarca el arbol
    for y in range(8, 160):
        for x in range(4, 108):
            dxl = x - 56
            arco_r = 50
            if y < 60:
                d = math.hypot(abs(dxl) + 26, (y - 60) * 1.05)
                en = abs(d - arco_r) < 1.2
            else:
                en = abs(abs(dxl) - 49) < 1.2
            if en and 6 < x < 106:
                c.put(x, y, ORO_S if (x + y) % 2 else ORO)
    # runas en columnas por dentro del arco
    k = 0
    for y in range(30, 150, 11):
        runa(c, g, 9, y, k)
        runa(c, g, 100, y, k + 3)
        k += 1
    arbol(c, g, 112, 160, rnd)
    # bocallave: placa dorada lobulada en el corazon del arbol (y = 88)
    kx, ky = 56, 88
    for y in range(ky - 9, ky + 10):
        for x in range(kx - 7, kx + 8):
            d = math.hypot((x + 0.5 - kx) * 1.25, y + 0.5 - ky)
            lob = min(math.hypot(x + 0.5 - kx, y + 0.5 - (ky - 5)), math.hypot(x + 0.5 - kx, y + 0.5 - (ky + 5)),
                      math.hypot(x + 0.5 - (kx - 4), y + 0.5 - ky), math.hypot(x + 0.5 - (kx + 4), y + 0.5 - ky))
            if d < 8.2 or lob < 4.2:
                borde = d > 6.9 and lob > 3.0
                c.put(x, y, (ORO_L if (x < kx and y < ky) else ORO) if borde else mix(HIERRO_L if (x < kx and y < ky) else HIERRO, HIERRO_S, 0.25))
            if 6.9 < d < 8.2:
                g.put(x, y, BRILLO_2)
    for y in range(ky - 3, ky + 5):
        for x in range(kx - 1, kx + 1):
            c.put(x, y, (8, 6, 4))
    for y in range(ky - 4, ky - 1):
        for x in range(kx - 2, kx + 2):
            c.put(x, y, (8, 6, 4))
    # la junta entre las hojas
    for y in range(0, 160):
        q = c.get(56, y)
        c.put(56, y, mix(q, JUNTA, 0.55))
    # ---- cara de la sala (112 x 160 en x = 112): tablones, bandas, tornapuntas en Z y clavos
    tablas(c, 112, 0, 112, 160, rnd)
    for hoja in (112, 168):
        marco_hierro(c, hoja, 0, 56, 160, 3)
        for yb in (14, 76, 140):
            banda_hierro(c, hoja + 3, hoja + 53, yb, 5, rnd)
        # tornapunta diagonal
        for t in range(0, 100):
            x = hoja + 6 + int(t * 44 / 100)
            y = 136 - int(t * 56 / 100)
            for w in range(-2, 3):
                c.put(x, y + w, HIERRO if abs(w) < 2 else HIERRO_S)
        for t in range(0, 100):
            x = hoja + 6 + int(t * 44 / 100)
            y = 72 - int(t * 54 / 100)
            for w in range(-2, 3):
                c.put(x, y + w, HIERRO if abs(w) < 2 else HIERRO_S)
        for y in range(8, 156, 9):
            for x in range(hoja + 8, hoja + 50, 9):
                if c.get(x, y)[:3] in (HIERRO, HIERRO_S, HIERRO_L):
                    continue
                c.put(x, y, REMACHE)
                c.put(x + 1, y + 1, HIERRO_S)
    # ---- cantos (6 x 160 en x = 224) y arriba/abajo (56 x 6 en y = 160)
    for x in range(224, 230):
        for y in range(0, 160):
            c.put(x, y, mix(MADERA[(y // 9) % len(MADERA)], (0, 0, 0), 0.2 + (0.25 if x in (224, 229) else 0)))
    for y in range(160, 166):
        for x in range(0, 56):
            c.put(x, y, mix(MADERA[(x // 8) % len(MADERA)], (0, 0, 0), 0.25))
    # ---- cerrojo (24 x 6 en y = 176) y soportes (8 x 8 en x = 32, y = 176)
    for x in range(0, 24):
        for y in range(176, 182):
            t = (y - 176) / 5
            col = mix(HIERRO_L, HIERRO_S, t)
            if x in (0, 23):
                col = HIERRO_S
            c.put(x, y, col)
    for x in range(32, 40):
        for y in range(176, 184):
            c.put(x, y, mix(HIERRO, HIERRO_S, (y - 176) / 8))
    c.im.save(os.path.join(OUT, "puerta_arbol.png"))
    g.im.save(os.path.join(OUT, "puerta_arbol_brillo.png"))


# ------------------------------------------------------------------ el disco de runas del ascensor
def disco():
    rnd = random.Random(2323)
    W = 128
    c = Lienzo(W, W)
    g = Lienzo(W, W, (0, 0, 0, 255))
    R = 47.5
    cx = cy = 48
    for y in range(96):
        for x in range(96):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if d > R:
                continue
            n = rnd.random()
            base = mix((40, 36, 44), (28, 25, 31), n * 0.7)
            # baldosas en anillos
            a = math.atan2(y + 0.5 - cy, x + 0.5 - cx)
            if 21 < d < 35 and abs(math.sin(a * 6)) < 0.06 * 25 / d:
                base = (18, 16, 20)
            c.put(x, y, base)
            if d > R - 3.2:
                c.put(x, y, ORO_L if d < R - 2.4 else (ORO if d < R - 1 else ORO_S))
                g.put(x, y, mix((0, 0, 0), BRILLO, 0.45))
            elif 35 < d < 37.2:
                c.put(x, y, ORO_S if d > 36.4 else ORO)
                g.put(x, y, BRILLO_2)
            elif 19 < d < 21:
                c.put(x, y, ORO)
                g.put(x, y, BRILLO)
    # runas entre los anillos (12, mirando al centro)
    for k in range(12):
        a = k * math.pi / 6
        rx, ry = cx + math.cos(a) * 28 - 1, cy + math.sin(a) * 28 - 2
        runa(c, g, int(rx), int(ry), k, tallada=False)
    # arbol pequeno en el centro (emblema)
    o = Oro(96, 96)
    o.trazo(bezier((48, 60), (48, 54), (48, 48), (48, 38), 20), 2.6, 1.2)
    for s in (-1, 1):
        o.trazo(bezier((48, 44), (48 + s * 4, 40), (48 + s * 8, 38), (48 + s * 11, 33), 20), 1.6, 0.8)
        o.trazo(bezier((48, 58), (48 + s * 5, 60), (48 + s * 9, 61), (48 + s * 12, 63), 16), 1.4, 0.7)
        for _ in range(6):
            aa = rnd.random() * math.pi * 2
            o.hoja(48 + s * 10 + math.cos(aa) * 3, 34 + math.sin(aa) * 3, 1.1)
    o.pintar(c, g)
    # canto (128 x 8 en y = 96): piedra con filete de oro
    for x in range(128):
        for y in range(96, 104):
            col = ORO if y in (96, 103) else mix((36, 32, 40), (22, 20, 25), rnd.random() * 0.6)
            c.put(x, y, col)
            if y in (96, 103):
                g.put(x, y, mix((0, 0, 0), BRILLO, 0.5))
        if x % 16 == 8:
            for y in range(98, 102):
                c.put(x, y, ORO_S)
                g.put(x, y, BRILLO_2)
    c.im.save(os.path.join(OUT, "ascensor.png"))
    g.im.save(os.path.join(OUT, "ascensor_brillo.png"))


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    puerta()
    disco()
    print("texturas de la torre del arbol en", os.path.normpath(OUT))
