#!/usr/bin/env python3
"""
Las miniaturas del codice (parte 2e-4): una escena pintada para cada capitulo de la cronica y para los libros
perdidos, el diario y el acertijo, como las de los manuscritos iluminados pero en la paleta del mod (noche azul, rojo
y oro). Cada una mide 128 x 80 y van juntas en textures/gui/miniaturas.png (4 x 4 huecos de 128 x 80: la fila de abajo, los
capitulos de Morvain, parte 7ae).

    python3 tools/ui/miniaturas.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import GUI, HUESO, Lienzo, ORO, PIEDRA, ROJO, AZUL, VERDE, TINTA, mezcla, rgba  # noqa: E402

W, H = 128, 80
NEGRO = "#07050a"
CIELO = ["#07061a", "#0b0a24", "#12123a", "#1a1c4e", "#252860", "#33306c"]
OCASO = ["#2a0c22", "#3e0f22", "#5a1420", "#7a2420", "#9a3a22"]
LUNA = ["#c8b888", "#e8dcb0", "#fff4d0"]
FUEGO = ["#5a1a06", "#9a4a0a", "#e08a1a", "#f8c040", "#fff0a0"]
AGUA = ["#1a3a7a", "#2f5cc6", "#5a8ae6", "#a0c0f8", "#e0f0ff"]
ROSA = ["#7a2848", "#b04878", "#e088b0", "#f8c8dc", "#fff0f6"]
MADERA = ["#1e120a", "#2e1c10", "#442a16", "#5e3c1e", "#7a5028"]
PIEDRA_C = ["#2a2632", "#3a3444", "#4c4558", "#625a70", "#7c7488", "#9a92a6"]
NUBE = ["#3a3048", "#5a4e66", "#80748a", "#a89cae", "#d0c6d2", "#efe8ee"]
VIOLETA = ["#14061e", "#2a0c3c", "#46146a", "#6a2a9a", "#9a5ad0", "#d0a0f4"]

BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16.0


class Mini(Lienzo):
    def __init__(self, fondo=NEGRO):
        super().__init__(W, H, fondo)

    def degradado(self, colores, y0, y1, x0=0, x1=W):
        """Degradado vertical entre colores de la lista, con tramado ordenado (sin colores nuevos)."""
        n = len(colores) - 1
        for y in range(y0, y1):
            t = (y - y0) / max(1, y1 - y0 - 1) * n
            i = min(n - 1, int(t))
            f = t - i
            for x in range(x0, x1):
                self.px(x, y, colores[i + 1] if f > BAYER[y % 4, x % 4] else colores[i])
        return self

    def poli(self, pts, c):
        """Rellena un poligono (puntos en pixeles, centros incluidos)."""
        ys = [p[1] for p in pts]
        for y in range(max(0, int(min(ys))), min(H, int(math.ceil(max(ys))) + 1)):
            cortes = []
            for i in range(len(pts)):
                (x0, y0), (x1, y1) = pts[i], pts[(i + 1) % len(pts)]
                if (y0 <= y + 0.5 < y1) or (y1 <= y + 0.5 < y0):
                    cortes.append(x0 + (y + 0.5 - y0) * (x1 - x0) / (y1 - y0))
            cortes.sort()
            for a, b in zip(cortes[0::2], cortes[1::2]):
                for x in range(int(math.ceil(a - 0.5)), int(math.floor(b - 0.5)) + 1):
                    self.px(x, y, c)
        return self

    def linea(self, x0, y0, x1, y1, c):
        x0, y0, x1, y1 = int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1))
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        e = dx + dy
        while True:
            self.px(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * e
            if e2 >= dy:
                e += dy
                x0 += sx
            if e2 <= dx:
                e += dx
                y0 += sy
        return self

    def elipse(self, cx, cy, rx, ry, c):
        for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
            for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
                if ((x - cx) / max(rx, 0.1)) ** 2 + ((y - cy) / max(ry, 0.1)) ** 2 <= 1.0:
                    self.px(x, y, c)
        return self

    def aro(self, cx, cy, rx, ry, c, grueso=1.0):
        for y in range(int(cy - ry) - 2, int(cy + ry) + 3):
            for x in range(int(cx - rx) - 2, int(cx + rx) + 3):
                d = math.hypot((x - cx) / max(rx, 0.1), (y - cy) / max(ry, 0.1))
                if abs(d - 1.0) * min(rx, ry) <= grueso * 0.5:
                    self.px(x, y, c)
        return self

    def estrellas(self, n, semilla, y1=40, x0=0, x1=W):
        r = np.random.default_rng(semilla)
        for _ in range(n):
            x, y = int(r.integers(x0, x1)), int(r.integers(0, y1))
            c = self.leer(x, y)
            if sum(c[:3]) > 200:
                continue
            brillo = r.random()
            self.px(x, y, LUNA[2] if brillo > 0.85 else LUNA[0] if brillo > 0.5 else "#6a6a90")
            if brillo > 0.95:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    self.px(x + dx, y + dy, "#8a86a8")
        return self

    def tinta(self, mascara, c=NEGRO):
        """Contorno de tinta alrededor de una mascara booleana (H x W)."""
        b = np.zeros_like(mascara)
        b[1:, :] |= mascara[:-1, :]
        b[:-1, :] |= mascara[1:, :]
        b[:, 1:] |= mascara[:, :-1]
        b[:, :-1] |= mascara[:, 1:]
        b &= ~mascara
        self.a[b] = rgba(c)
        return self


def capa():
    """Un lienzo transparente del tamano de la miniatura (para dibujar una figura y luego contornearla)."""
    L = Mini()
    L.a[:] = 0
    return L


def girar(L, x0, y0, w, h, grados):
    """Gira un trozo (x0, y0, w, h) del lienzo L sobre su centro (vecino mas cercano) y lo devuelve en otro lienzo."""
    a = math.radians(grados)
    n = int(math.ceil(math.hypot(w, h))) + 2
    out = capa()
    cx, cy = x0 + w / 2, y0 + h / 2
    for y in range(n):
        for x in range(n):
            dx, dy = x - n / 2, y - n / 2
            sx = cx + dx * math.cos(a) + dy * math.sin(a)
            sy = cy - dx * math.sin(a) + dy * math.cos(a)
            ix, iy = int(math.floor(sx)), int(math.floor(sy))
            if x0 <= ix < x0 + w and y0 <= iy < y0 + h:
                c = L.leer(ix, iy)
                if c[3] > 0:
                    out.px(x, y, c)
    return out


def medio_aro(M, cx, cy, rx, ry, c, grueso=1.0):
    for y in range(int(cy - ry) - 2, int(cy) + 1):
        for x in range(int(cx - rx) - 2, int(cx + rx) + 3):
            d = math.hypot((x - cx) / max(rx, 0.1), (y - cy) / max(ry, 0.1))
            if abs(d - 1.0) * min(rx, ry) <= grueso * 0.5:
                M.px(x, y, c)
    return M


def pegar_con_tinta(M, fig, c=NEGRO):
    mask = fig.a[..., 3] > 0
    M.tinta(mask, c)
    M.pegar(fig, 0, 0)
    return M


def luna(M, cx, cy, r, creciente=True):
    """La luna (creciente: solo la parte iluminada; el resto del cielo queda como esta)."""
    for y in range(cy - r - 1, cy + r + 2):
        for x in range(cx - r - 1, cx + r + 2):
            if math.hypot(x - cx, y - cy) > r:
                continue
            if creciente and math.hypot(x - (cx + r * 0.55), y - (cy - r * 0.15)) <= r * 0.9:
                continue
            borde = math.hypot(x - cx, y - cy) > r - 1.2
            M.px(x, y, LUNA[2] if not borde and x < cx else LUNA[1])
    return M


def almenas(M, y, x0, x1, c, alto=3, ancho=3, hueco=3):
    x = x0
    while x < x1:
        M.rect(x, y - alto, ancho, alto, c)
        x += ancho + hueco
    return M


def llama(M, x, y, alto=4):
    """Una llamita de vela o de brasero (con su halo)."""
    for r, c in ((4, mezcla(M.leer(x, y - 2), FUEGO[2], 0.25)), (2, mezcla(M.leer(x, y - 2), FUEGO[3], 0.35))):
        for yy in range(y - alto - r, y + r):
            for xx in range(x - r - 1, x + r + 2):
                if math.hypot(xx - x, (yy - (y - alto / 2)) * 0.8) <= r + alto / 2 and M.leer(xx, yy)[3] > 0:
                    base = M.leer(xx, yy)
                    if sum(base[:3]) < 300:
                        M.px(xx, yy, mezcla(base, c, 0.5))
    M.px(x, y - alto, FUEGO[2])
    for i in range(1, alto):
        M.px(x, y - i, FUEGO[3] if i > 1 else FUEGO[4])
    M.px(x, y, FUEGO[1])
    return M


# ================================================================= 1. La Corona Rota
def corona_rota():
    M = Mini()
    M.degradado(CIELO[:5] + [OCASO[1], OCASO[2]], 0, 60)
    M.estrellas(46, 3, 44)
    luna(M, 104, 13, 7)
    # el rayo de la tormenta de Valdrenn, a lo lejos
    pts = [(84, 0), (81, 9), (85, 10), (79, 22), (83, 23), (77, 36)]
    for (a, b) in zip(pts, pts[1:]):
        M.linea(a[0], a[1], b[0], b[1], ORO[5])
        M.linea(a[0] + 1, a[1], b[0] + 1, b[1], mezcla(ORO[3], CIELO[2], 0.4))
    # murallas y torres del reino en el horizonte
    sil = PIEDRA[1]
    M.rect(0, 50, W, 12, sil)
    almenas(M, 50, 0, W, sil)
    for (x, w, h) in ((40, 7, 12), (56, 9, 18), (70, 6, 10), (92, 8, 14), (110, 7, 9)):
        M.rect(x, 50 - h, w, h, sil)
        almenas(M, 50 - h, x, x + w, sil, 2, 2, 1)
        M.px(x + w // 2, 50 - h + 4, FUEGO[2])
    # la Torre de Ascenso, alta y delgada, con su tejado
    M.rect(14, 14, 11, 48, PIEDRA[2])
    M.rect(14, 14, 2, 48, PIEDRA[3])
    M.rect(23, 14, 2, 48, PIEDRA[1])
    M.poli([(12, 15), (19.5, 1), (27, 15)], PIEDRA[1])
    M.linea(19, 2, 13, 14, PIEDRA[3])
    for y in range(18, 60, 3):
        M.hline(14, y, 11, PIEDRA[1])
    M.rect(18, 24, 3, 5, FUEGO[2])
    M.px(19, 25, FUEGO[4])
    M.rect(18, 38, 3, 4, FUEGO[1])
    # suelo y escalones de piedra
    M.degradado([PIEDRA[1], PIEDRA[2]], 60, 66)
    for i, (y, x0, x1) in enumerate(((66, 22, 106), (71, 14, 114), (76, 6, 122))):
        M.rect(x0, y, x1 - x0, 5, PIEDRA_C[2 - i // 2])
        M.hline(x0, y, x1 - x0, PIEDRA_C[4])
        M.hline(x0, y + 4, x1 - x0, PIEDRA_C[0])
        for x in range(x0 + 6, x1, 13):
            M.vline(x, y + 1, 3, PIEDRA_C[1])
    # la corona, rota en dos: una mitad de pie y la otra tumbada
    fig = capa()
    # mitad izquierda (de pie, un poco inclinada)
    base = [(44, 64), (62, 64), (62, 58), (44, 58)]
    fig.poli(base, ORO[3])
    for (x, h) in ((44, 10), (50, 13), (56, 9)):
        fig.poli([(x, 58), (x + 3, 58 - h), (x + 6, 58)], ORO[4])
        fig.px(x + 3, 58 - h - 1, ROJO[5])
        fig.px(x + 3, 58 - h, ROJO[4])
    fig.hline(44, 59, 18, ORO[5])
    fig.hline(44, 63, 18, ORO[1])
    for x in (47, 53, 59):
        fig.px(x, 61, ROJO[4]).px(x + 1, 61, ROJO[3])
    # la grieta: zigzag por donde se partio
    for i, (x, y) in enumerate(((62, 58), (61, 60), (63, 61), (61, 63), (62, 64))):
        fig.px(x, y, NEGRO)
    # mitad derecha: la misma pieza, caida de lado sobre el escalon
    trozo = capa()
    trozo.poli([(0, 12), (16, 12), (16, 7), (0, 7)], ORO[2])
    for (x, h) in ((1, 7), (7, 10)):
        trozo.poli([(x, 7), (x + 3, 7 - h), (x + 6, 7)], ORO[3])
        trozo.px(x + 3, 7 - h - 1, ROJO[5])
    trozo.hline(0, 8, 16, ORO[4])
    trozo.px(4, 10, ROJO[4]).px(10, 10, ROJO[4])
    girada = girar(trozo, 0, 0, 17, 17, -62)
    fig.pegar(girada, 66, 47)
    # destellos
    fig.px(46, 59, ORO[6]).px(51, 47, ORO[6]).px(69, 61, ORO[5])
    pegar_con_tinta(M, fig)
    # unas gemas sueltas que saltaron
    M.px(88, 65, ROJO[5]).px(91, 69, ROJO[4]).px(36, 64, ROJO[5])
    return M


# ================================================================= 2. Donde el agua aun canta
def fuente():
    M = Mini()
    M.degradado(CIELO[:5], 0, 52)
    M.estrellas(40, 5, 36)
    luna(M, 20, 12, 6)
    # setos y arcos del jardin
    M.rect(0, 40, W, 22, VERDE[0])
    for x in range(0, W, 6):
        M.elipse(x + 3, 41, 4, 3, VERDE[0])
        M.px(x + 2, 39, VERDE[1])
    for cx in (14, 114):
        # cipreses
        M.poli([(cx - 5, 62), (cx, 18), (cx + 5, 62)], VERDE[0])
        M.linea(cx, 20, cx - 3, 60, VERDE[1])
    for cx in (40, 88):
        medio_aro(M, cx, 44, 12, 14, PIEDRA_C[1], 2.5)
        M.rect(cx - 13, 44, 3, 18, PIEDRA_C[1])
        M.rect(cx + 11, 44, 3, 18, PIEDRA_C[1])
    # el suelo del patio
    M.degradado([PIEDRA[2], PIEDRA[3], PIEDRA[2]], 62, 80)
    for y in (66, 71, 77):
        M.hline(0, y, W, PIEDRA[1])
    fig = capa()
    cx = 64
    # la taza grande de abajo
    fig.elipse(cx, 66, 30, 6, PIEDRA_C[3])
    fig.rect(cx - 30, 66, 61, 6, PIEDRA_C[2])
    fig.elipse(cx, 72, 30, 4, PIEDRA_C[1])
    fig.elipse(cx, 66, 27, 4, AGUA[1])
    for x in range(cx - 24, cx + 25, 6):
        fig.hline(x, 66 + (x // 6) % 2, 3, AGUA[3])
    # el pie y la taza de arriba
    fig.rect(cx - 3, 46, 7, 20, PIEDRA_C[3])
    fig.vline(cx - 3, 46, 20, PIEDRA_C[4])
    fig.vline(cx + 3, 46, 20, PIEDRA_C[1])
    fig.elipse(cx, 45, 14, 4, PIEDRA_C[3])
    fig.rect(cx - 14, 45, 29, 3, PIEDRA_C[2])
    fig.elipse(cx, 44, 12, 2, AGUA[2])
    fig.rect(cx - 1, 33, 3, 11, PIEDRA_C[3])
    fig.elipse(cx, 32, 3, 3, PIEDRA_C[4])
    pegar_con_tinta(M, fig)
    # los chorros: del remate arriba y de la taza de arriba a la de abajo
    for lado in (-1, 1):
        for i in range(14):
            t = i / 13
            x = cx + lado * (1 + t * 9)
            y = 31 - 6 * math.sin(t * math.pi * 0.9) + t * 12
            M.px(int(x), int(y), AGUA[4] if i % 3 else AGUA[3])
        for i in range(18):
            t = i / 17
            x = cx + lado * (13 + t * 10)
            y = 46 + t * t * 19
            M.px(int(x), int(y), AGUA[3] if i % 2 else AGUA[4])
            M.px(int(x) + lado, int(y), AGUA[2])
    # gotas y brillo de luna en el agua
    for (x, y) in ((50, 40), (78, 38), (44, 58), (85, 57), (60, 63), (70, 64)):
        M.px(x, y, AGUA[4])
    # las rosas de la reina, al pie
    for (x, y) in ((30, 72), (36, 75), (96, 73), (101, 76), (24, 76), (106, 77)):
        M.elipse(x, y + 1, 3, 2, VERDE[2])
        M.px(x - 2, y + 2, VERDE[3])
        M.elipse(x, y, 1.6, 1.6, ROJO[4])
        M.px(x, y - 1, ROJO[6])
    return M


# ================================================================= 3. El Rey que Espera
def trono():
    M = Mini(PIEDRA[0])
    M.degradado([PIEDRA[0], PIEDRA[1], PIEDRA[2]], 0, 56)
    # columnas
    for x in (6, 30, 92, 116):
        M.rect(x, 0, 7, 58, PIEDRA[2])
        M.vline(x, 0, 58, PIEDRA[3])
        M.vline(x + 6, 0, 58, PIEDRA[1])
    # estandartes rojos con el borde de oro y una corona
    for x in (14, 100):
        M.rect(x, 0, 13, 40, ROJO[2])
        M.poli([(x, 40), (x + 6.5, 46), (x + 13, 40)], ROJO[2])
        M.vline(x, 0, 40, ORO[2])
        M.vline(x + 12, 0, 40, ORO[2])
        M.rect(x + 4, 16, 5, 3, ORO[4])
        for dx in (0, 2, 4):
            M.px(x + 4 + dx, 15, ORO[4])
        M.vline(x + 3, 0, 40, ROJO[3])
    # el suelo en damero
    for y in range(56, 80):
        for x in range(W):
            k = ((x - 64) * 40 // max(1, y - 30)) // 7 + (y - 56) // 4
            M.px(x, y, PIEDRA_C[1] if k % 2 else PIEDRA_C[3])
    # escalones al trono
    M.rect(38, 54, 52, 4, PIEDRA_C[2])
    M.hline(38, 54, 52, PIEDRA_C[4])
    M.rect(44, 50, 40, 4, PIEDRA_C[3])
    M.hline(44, 50, 40, PIEDRA_C[5])
    fig = capa()
    cx = 64
    # el trono: respaldo alto con pinchos y brazos
    fig.rect(cx - 10, 14, 21, 32, MADERA[1])
    for (x, h) in ((cx - 10, 8), (cx - 4, 12), (cx + 3, 12), (cx + 9, 8)):
        fig.poli([(x, 14), (x + 1, 14 - h), (x + 2, 14)], MADERA[2])
    fig.poli([(cx - 1, 14), (cx, 2), (cx + 1, 14)], ORO[3])
    fig.rect(cx - 7, 18, 15, 22, ROJO[1])
    fig.rect(cx - 6, 19, 13, 20, ROJO[2])
    fig.rect(cx - 14, 34, 29, 5, MADERA[2])
    fig.rect(cx - 14, 39, 4, 11, MADERA[1])
    fig.rect(cx + 11, 39, 4, 11, MADERA[1])
    fig.rect(cx - 10, 40, 21, 4, MADERA[3])
    fig.hline(cx - 14, 34, 29, ORO[3])
    fig.px(cx - 14, 33, ORO[5]).px(cx + 14, 33, ORO[5])
    # la corona del Rey, dejada en el asiento
    fig.rect(cx - 4, 37, 9, 3, ORO[3])
    for x in (cx - 4, cx, cx + 4):
        fig.px(x, 36, ORO[4])
    fig.px(cx, 38, ROJO[5])
    pegar_con_tinta(M, fig)
    # las dos espadas del Rey, cruzadas y clavadas delante
    for (x0, y0, x1, y1) in ((44, 74, 76, 44), (84, 74, 52, 44)):
        M.linea(x0, y0, x1, y1, "#c8ccd8")
        M.linea(x0 + 1, y0, x1 + 1, y1, "#7c8090")
        M.px(x1, y1, "#ffffff")
    for (x, y, d) in ((74, 47, 1), (54, 47, -1)):
        M.linea(x - 4, y + 4 * d * 0, x + 4, y, ORO[3])
        M.rect(x - 1, y - 4, 2, 4, MADERA[2])
        M.px(x - 1, y - 5, ORO[4])
    # braseros a los lados
    for x in (40, 88):
        M.rect(x - 3, 46, 7, 2, PIEDRA_C[4])
        M.rect(x - 1, 48, 3, 6, PIEDRA_C[2])
        llama(M, x, 45, 4)
        llama(M, x - 2, 46, 2)
        llama(M, x + 2, 46, 3)
    return M


# ================================================================= 4. La Reina bajo el Altar
def catedral():
    M = Mini(NEGRO)
    M.degradado([NEGRO, PIEDRA[0], PIEDRA[1]], 0, 60)
    # el arco gotico del fondo
    M.poli([(26, 80), (26, 30), (40, 8), (64, 0), (88, 8), (102, 30), (102, 80)], PIEDRA[1])
    M.poli([(32, 80), (32, 32), (44, 13), (64, 6), (84, 13), (96, 32), (96, 80)], PIEDRA[0])
    # el roseton con sus vidrieras
    cx, cy, r = 64, 22, 13
    colores = [ROJO[4], AZUL[4], ORO[4], ROJO[3], AZUL[3], VERDE[3], ORO[5], AZUL[5]]
    for y in range(cy - r, cy + r + 1):
        for x in range(cx - r, cx + r + 1):
            d = math.hypot(x - cx, y - cy)
            if d > r:
                continue
            ang = (math.atan2(y - cy, x - cx) + math.pi) / (2 * math.pi)
            sector = int(ang * 8) % 8
            if d < 3.5:
                M.px(x, y, ORO[5])
            elif abs(d - 7.5) < 0.7 or (ang * 8) % 1 < 0.12:
                M.px(x, y, PIEDRA[0])
            else:
                M.px(x, y, colores[(sector + (1 if d > 7.5 else 0)) % 8])
    M.aro(cx, cy, r + 0.5, r + 0.5, PIEDRA[3], 1.6)
    # rayos de luz que bajan del roseton
    for i, x0 in enumerate((56, 64, 72)):
        for y in range(cy + r, 64):
            for dx in range(-2, 3):
                x = int(x0 + (y - cy) * (i - 1) * 0.35) + dx
                if (x + y) % 3 == 0 and 0 <= x < W:
                    M.px(x, y, mezcla(M.leer(x, y), ORO[4], 0.35))
    # los bancos a los lados
    for k in range(4):
        y = 50 + k * 7
        M.rect(0, y, 24 - k * 2, 3, MADERA[2])
        M.rect(104 + k * 2, y, 24, 3, MADERA[2])
        M.hline(0, y, 24 - k * 2, MADERA[4])
        M.hline(104 + k * 2, y, 24, MADERA[4])
    # el suelo
    M.degradado([PIEDRA[2], PIEDRA[3]], 64, 80, 26, 102)
    fig = capa()
    # el altar
    fig.rect(44, 46, 41, 4, PIEDRA_C[4])
    fig.rect(47, 50, 35, 12, PIEDRA_C[2])
    fig.hline(44, 46, 41, PIEDRA_C[5])
    fig.rect(61, 52, 7, 8, ROJO[2])
    fig.rect(63, 53, 3, 6, ORO[3])
    # el sepulcro de la reina delante, con su figura tumbada
    fig.rect(36, 64, 57, 10, PIEDRA_C[3])
    fig.hline(36, 64, 57, PIEDRA_C[5])
    fig.rect(36, 74, 57, 2, PIEDRA_C[1])
    fig.elipse(64, 62, 22, 2.5, HUESO[3])
    fig.elipse(44, 61, 3, 2.5, HUESO[4])
    fig.px(43, 58, ORO[4]).px(44, 58, ORO[5]).px(45, 58, ORO[4])
    fig.hline(50, 61, 26, HUESO[2])
    pegar_con_tinta(M, fig)
    # velas en el altar
    for x in (48, 53, 76, 81):
        M.rect(x, 41, 2, 5, HUESO[4])
        llama(M, x, 40, 3)
    return M


# ================================================================= 5. El Ultimo Juramento
def juramento():
    M = Mini()
    M.degradado([CIELO[0], CIELO[1], VERDE[0]], 0, 64)
    # el arbol enorme, en flor
    M.rect(78, 20, 18, 60, MADERA[1])
    M.vline(80, 20, 60, MADERA[3])
    M.vline(84, 26, 50, MADERA[2])
    for (x0, y0, x1, y1) in ((86, 30, 120, 14), (80, 28, 54, 10), (90, 24, 104, 4), (82, 40, 60, 30)):
        M.linea(x0, y0, x1, y1, MADERA[1])
        M.linea(x0, y0 + 1, x1, y1 + 1, MADERA[2])
    r = np.random.default_rng(9)
    for _ in range(170):
        cx, cy = r.normal(88, 22), r.normal(16, 9)
        if 0 <= cx < W and 0 <= cy < 44:
            M.elipse(cx, cy, 2.2, 1.6, VERDE[1] if r.random() < 0.35 else ROSA[int(r.integers(1, 4))])
    for _ in range(60):
        x, y = int(r.normal(88, 22)), int(r.normal(14, 8))
        M.px(x, y, ROSA[4])
    # raices en el suelo
    M.degradado([VERDE[0], MADERA[0]], 64, 80)
    for (x0, x1, y) in ((78, 50, 74), (96, 124, 72), (88, 108, 78)):
        M.linea(x0, 66, x1, y, MADERA[2])
        M.linea(x0, 67, x1, y + 1, MADERA[1])
    # petalos cayendo
    for _ in range(40):
        x, y = int(r.integers(0, W)), int(r.integers(10, 78))
        M.px(x, y, ROSA[3] if r.random() < 0.5 else ROSA[4])
    # el caballero de rodillas, con el mandoble clavado delante
    fig = capa()
    a = "#1a1a1e"
    b = "#2e2e36"
    c = "#46464f"
    # pierna doblada y rodilla en el suelo
    fig.poli([(30, 74), (44, 74), (44, 69), (34, 66), (30, 70)], a)
    fig.poli([(42, 74), (52, 74), (50, 62), (44, 60), (42, 66)], b)
    # cuerpo inclinado
    fig.poli([(34, 66), (46, 62), (48, 46), (40, 40), (33, 44), (31, 56)], b)
    fig.poli([(36, 46), (40, 41), (46, 45), (44, 52), (37, 54)], c)
    # capa
    fig.poli([(31, 44), (26, 72), (34, 72), (35, 50)], ROJO[1])
    # cabeza agachada (yelmo) con los ojos verdes
    fig.elipse(46, 38, 4.5, 4.5, b)
    fig.rect(45, 33, 3, 2, c)
    fig.px(49, 39, VERDE[5]).px(50, 39, VERDE[6])
    # brazos al pomo
    fig.poli([(44, 46), (56, 50), (57, 53), (45, 51)], a)
    # el mandoble clavado
    fig.rect(57, 44, 3, 32, "#9aa0b0")
    fig.vline(57, 44, 32, "#d0d4e0")
    fig.rect(52, 44, 13, 2, ORO[3])
    fig.rect(57, 38, 3, 6, MADERA[2])
    fig.rect(56, 36, 5, 2, ORO[4])
    fig.poli([(57, 76), (58.5, 79), (60, 76)], "#9aa0b0")
    pegar_con_tinta(M, fig)
    # un poco de luz verde en el pecho (el corazon del arbol)
    M.px(40, 49, VERDE[4]).px(41, 49, VERDE[5])
    return M


# ================================================================= 6. La puerta sobre las nubes
def portal():
    M = Mini()
    M.degradado([VIOLETA[0], CIELO[1], OCASO[1], OCASO[2], OCASO[3]], 0, 58)
    M.estrellas(38, 11, 30)
    # el mar de nubes
    r = np.random.default_rng(12)
    M.degradado([NUBE[1], NUBE[2], NUBE[1]], 58, 80)
    for capa_y, tono in ((54, 2), (60, 3), (67, 4), (74, 3)):
        for _ in range(16):
            x = int(r.integers(-6, W + 6))
            rx, ry = r.integers(6, 14), r.integers(3, 5)
            M.elipse(x, capa_y, rx, ry, NUBE[tono])
            M.elipse(x - 1, capa_y - 1, rx - 2, ry - 1, NUBE[min(5, tono + 1)])
    # la isla de hueso y el arco del portal encima
    fig = capa()
    fig.poli([(34, 60), (94, 60), (86, 68), (70, 72), (50, 70), (40, 66)], PIEDRA[2])
    fig.hline(34, 60, 61, HUESO[2])
    cx = 64
    for lado in (-1, 1):
        x = cx + lado * 17
        fig.rect(x - 4, 22, 8, 38, HUESO[3])
        fig.vline(x - 4, 22, 38, HUESO[4])
        fig.vline(x + 3, 22, 38, HUESO[1])
        for y in range(26, 60, 6):
            fig.hline(x - 4, y, 8, HUESO[1])
    for i in range(30):
        t = i / 29
        ang = math.pi * t
        x = cx - 17 * math.cos(ang)
        y = 22 - 14 * math.sin(ang)
        fig.elipse(x, y, 4, 4, HUESO[3] if i % 4 else HUESO[1])
    # la puerta: remolino violeta con el ojo rojo en medio
    for y in range(12, 60):
        for x in range(cx - 13, cx + 14):
            dx, dy = (x - cx) / 13.0, (y - 34) / 24.0
            if dx * dx + dy * dy > 1 or (y < 24 and math.hypot(x - cx, (y - 23) * 0.95) > 13):
                continue
            ang = math.atan2(dy, dx)
            d = math.hypot(dx, dy)
            v = (math.sin(ang * 3 + d * 9) + 1) / 2
            fig.px(x, y, VIOLETA[int(1 + v * 3 * (1 - d * 0.5))])
    fig.elipse(cx, 34, 7, 3.4, ROJO[3])
    fig.elipse(cx, 34, 5, 2.4, ROJO[5])
    fig.elipse(cx, 34, 1.4, 2.2, NEGRO)
    fig.px(cx - 2, 33, ROJO[6])
    pegar_con_tinta(M, fig)
    return M


# ================================================================= 7. Libros perdidos de Valdrenn
def perdidos_valdrenn():
    M = Mini(MADERA[0])
    M.degradado([PIEDRA[0], PIEDRA[1]], 0, 52)
    for y in range(4, 52, 8):
        for x in range((y // 8) % 2 * 9, W, 18):
            M.rect(x, y, 17, 7, PIEDRA[1])
            M.hline(x, y, 17, PIEDRA[2])
    # la ventana con la noche
    M.rect(84, 8, 24, 30, PIEDRA[0])
    M.degradado(CIELO[1:4], 10, 36, 86, 106)
    M.estrellas(8, 21, 36, 86, 106)
    M.vline(96, 10, 26, PIEDRA[2])
    M.hline(86, 22, 20, PIEDRA[2])
    # la mesa
    M.rect(0, 52, W, 28, MADERA[2])
    M.hline(0, 52, W, MADERA[4])
    for y in range(56, 80, 5):
        M.hline(0, y, W, MADERA[1])
    fig = capa()
    # el libro abierto
    fig.poli([(30, 66), (60, 62), (60, 48), (32, 52)], "#c8b890")
    fig.poli([(60, 62), (90, 66), (88, 52), (60, 48)], "#d8c8a0")
    fig.vline(60, 48, 15, "#8a7a5a")
    for k in range(5):
        y0 = 54 + k * 2
        fig.linea(35, y0 + 1, 56, y0 - 2, "#6a5a40")
        fig.linea(64, y0 - 2, 84, y0 + 1, "#6a5a40")
    # el tintero y la pluma
    fig.rect(98, 54, 8, 7, "#14121e")
    fig.rect(99, 52, 6, 2, "#2a283a")
    fig.linea(102, 52, 114, 30, "#e8e0d0")
    fig.linea(103, 52, 116, 31, "#b8b0a0")
    for i in range(8):
        fig.linea(108 + i * 0.6, 40 - i * 1.2, 111 + i * 0.6, 41 - i * 1.2, "#d8d0c0")
    # la vela
    fig.rect(14, 40, 5, 16, HUESO[4])
    fig.vline(14, 40, 16, HUESO[5])
    fig.rect(12, 56, 9, 3, ORO[2])
    pegar_con_tinta(M, fig)
    llama(M, 16, 38, 5)
    # cera derretida
    M.px(15, 41, HUESO[5]).px(18, 43, HUESO[4])
    return M


# ================================================================= 8. Libros perdidos de la Fortaleza
def perdidos_fortaleza():
    M = Mini()
    M.degradado([CIELO[0], OCASO[1], OCASO[2]], 0, 50)
    M.estrellas(20, 31, 30)
    M.degradado([NUBE[1], NUBE[2]], 62, 80)
    # la muralla de hueso y piedra negra
    M.rect(0, 30, W, 40, PIEDRA[1])
    almenas(M, 30, 0, W, PIEDRA[1], 4, 4, 4)
    for y in range(34, 70, 5):
        M.hline(0, y, W, PIEDRA[0])
    fig = capa()
    cx = 64
    # la calavera enorme de la puerta
    fig.elipse(cx, 36, 22, 18, HUESO[3])
    fig.rect(cx - 14, 44, 29, 14, HUESO[3])
    fig.elipse(cx - 9, 34, 6, 6, NEGRO)
    fig.elipse(cx + 9, 34, 6, 6, NEGRO)
    fig.px(cx - 9, 34, ROJO[5]).px(cx + 9, 34, ROJO[5])
    fig.poli([(cx - 3, 46), (cx, 40), (cx + 3, 46)], NEGRO)
    for x in range(cx - 12, cx + 13, 4):
        fig.rect(x, 52, 3, 6, HUESO[4])
    fig.hline(cx - 14, 51, 29, HUESO[1])
    for y in range(22, 50, 4):
        fig.px(cx - 18 + (y % 8), y, HUESO[1])
    # la reja de hueso abajo
    fig.rect(cx - 13, 58, 27, 14, NEGRO)
    for x in range(cx - 12, cx + 13, 4):
        fig.vline(x, 58, 14, HUESO[2])
    for y in (61, 66):
        fig.hline(cx - 13, y, 27, HUESO[2])
    pegar_con_tinta(M, fig)
    for _ in range(14):
        x = int(np.random.default_rng(_).integers(0, W))
        M.elipse(x, 74, 8, 3, NUBE[3])
    return M


# ================================================================= 9. El diario
def diario():
    M = Mini(NEGRO)
    M.degradado([NEGRO, ROJO[0], PIEDRA[0]], 0, 50)
    # humo de la Sombra alrededor
    r = np.random.default_rng(41)
    for _ in range(90):
        x, y = int(r.integers(0, W)), int(r.integers(0, 48))
        M.px(x, y, mezcla(M.leer(x, y), ROJO[2], 0.4))
    M.rect(0, 50, W, 30, MADERA[1])
    M.hline(0, 50, W, MADERA[3])
    fig = capa()
    # el espejo redondo de la Sombra, apoyado
    cx, cy = 34, 34
    fig.elipse(cx, cy, 17, 17, ORO[3])
    fig.elipse(cx, cy, 15, 15, ORO[1])
    fig.elipse(cx, cy, 13, 13, "#0c0a18")
    for y in range(cy - 13, cy + 14):
        for x in range(cx - 13, cx + 14):
            if math.hypot(x - cx, y - cy) < 13 and (x - y) % 7 == 0:
                fig.px(x, y, "#1a1630")
    fig.elipse(cx, cy, 5, 2.5, ROJO[3])
    fig.elipse(cx, cy, 1.2, 2, NEGRO)
    fig.px(cx - 2, cy - 1, ROJO[6])
    fig.px(cx, cy - 17, ROJO[5]).px(cx, cy - 16, ROJO[4])
    fig.rect(cx - 3, 50, 7, 4, ORO[2])
    # el diario abierto
    fig.poli([(56, 70), (84, 66), (84, 52), (58, 56)], "#c8b890")
    fig.poli([(84, 66), (112, 70), (110, 56), (84, 52)], "#d8c8a0")
    fig.vline(84, 52, 15, "#8a7a5a")
    for k in range(5):
        y0 = 58 + k * 2
        fig.linea(61, y0 + 1, 80, y0 - 2, "#6a5a40")
        fig.linea(88, y0 - 2, 106, y0 + 1, "#5e1010" if k == 0 else "#6a5a40")
    # la pluma
    fig.linea(94, 60, 118, 34, "#e8e0d0")
    for i in range(9):
        fig.linea(110 + i * 0.8, 44 - i * 1.0, 114 + i * 0.8, 45 - i * 1.0, "#d8d0c0")
    pegar_con_tinta(M, fig)
    return M


# ================================================================= 10. El acertijo (el yunque y la llave de hueso)
def acertijo():
    M = Mini(NEGRO)
    M.degradado([NEGRO, FUEGO[0], ROJO[1]], 0, 58)
    r = np.random.default_rng(51)
    M.rect(0, 58, W, 22, PIEDRA[1])
    M.hline(0, 58, W, PIEDRA[3])
    fig = capa()
    # el yunque
    fig.poli([(34, 40), (96, 40), (106, 36), (96, 46), (34, 46), (24, 42)], "#3a3a46")
    fig.hline(34, 40, 62, "#7a7a8a")
    fig.rect(52, 46, 26, 6, "#2a2a34")
    fig.poli([(44, 58), (86, 58), (78, 52), (52, 52)], "#2a2a34")
    # la llave de hueso encima
    fig.elipse(50, 36, 5, 4, HUESO[4])
    fig.elipse(50, 36, 2, 1.6, NEGRO)
    fig.rect(55, 35, 26, 3, HUESO[3])
    fig.hline(55, 35, 26, HUESO[5])
    for x in (72, 76, 80):
        fig.rect(x, 38, 2, 3, HUESO[3])
    pegar_con_tinta(M, fig)
    # chispas
    for _ in range(30):
        x, y = int(r.normal(66, 18)), int(r.normal(26, 10))
        M.px(x, y, FUEGO[3] if r.random() < 0.6 else FUEGO[4])
    return M


# ================================================================= 11. Las notas de Brumavela
def perdidos_brumavela():
    """El muelle de Brumavela al caer la tarde: el faro encendido en su penon, el espigon y La Garza Gris, rota, sobre el mar de nubes."""
    M = Mini()
    M.degradado(["#10121e", "#1c2234", "#30384c", "#525a6c", "#7c8494"], 0, 50)
    # el sol palido tras la bruma
    M.elipse(98, 20, 8, 8, "#9aa0ac")
    M.elipse(98, 20, 6, 6, "#c4c8cc")
    for x0, x1, y in ((84, 114, 17), (88, 110, 22), (80, 104, 26)):
        M.hline(x0, y, x1 - x0, "#525a6c")
    # el penon y el faro, lejos, a la izquierda
    fig = capa()
    fig.poli([(0, 50), (0, 30), (10, 26), (22, 27), (30, 33), (38, 44), (44, 50)], PIEDRA_C[0])
    fig.poli([(2, 34), (10, 28), (20, 29), (27, 35), (22, 38), (8, 37)], PIEDRA_C[1])
    fig.rect(13, 9, 5, 19, "#8a8a92")
    fig.vline(13, 9, 19, "#b0b0b6")
    fig.rect(13, 16, 5, 2, "#3a3a44")
    fig.rect(11, 7, 9, 2, MADERA[2])
    fig.rect(13, 3, 5, 4, AGUA[3])
    fig.poli([(11, 3), (20, 3), (15.5, -1)], "#2a2632")
    pegar_con_tinta(M, fig)
    M.px(15, 4, AGUA[4]).px(15, 5, AGUA[4])
    for k in range(1, 9):
        M.px(20 + k * 2, 5 - (k // 3), AGUA[2] if k % 2 else AGUA[1])
        M.px(10 - k, 5 + (k // 2), AGUA[1])
    # el mar de nubes
    M.degradado([NUBE[3], NUBE[2], NUBE[1], NUBE[0]], 50, H)
    r = np.random.default_rng(31)
    for _ in range(26):
        x, y, rx = int(r.integers(-6, W)), int(r.integers(50, 78)), int(r.integers(7, 18))
        M.elipse(x, y, rx, 2.5, NUBE[4] if y < 60 else NUBE[3] if y < 70 else NUBE[2])
        M.hline(x - rx + 2, y - 2, max(1, rx), NUBE[5] if y < 62 else NUBE[4])
    # el espigon
    fig = capa()
    fig.rect(28, 52, 44, 3, MADERA[3])
    fig.hline(28, 52, 44, MADERA[4])
    for x in range(31, 72, 8):
        fig.rect(x, 55, 2, 9 + (x % 3), MADERA[1])
    fig.rect(69, 45, 2, 7, MADERA[2])
    fig.rect(68, 43, 4, 2, AGUA[2])
    # La Garza Gris: el casco, el alcazar, el palo tronchado y el baupres
    fig.poli([(70, 50), (118, 48), (124, 44), (120, 58), (110, 63), (82, 63), (73, 58)], MADERA[2])
    fig.poli([(70, 50), (118, 48), (119, 51), (72, 53)], MADERA[4])
    fig.poli([(70, 50), (70, 43), (86, 42), (86, 50)], MADERA[3])
    fig.hline(70, 43, 16, MADERA[4])
    for x in (74, 79):
        fig.rect(x, 46, 2, 2, FUEGO[3])
    fig.rect(96, 36, 3, 13, MADERA[3])
    fig.poli([(95, 36), (97, 31), (98, 35), (100, 33), (99, 37)], MADERA[4])
    fig.linea(120, 46, 127, 40, MADERA[3])
    for x in range(76, 116, 7):
        fig.vline(x, 54, 8, MADERA[1])
    pegar_con_tinta(M, fig)
    # la bruma por delante
    for x0, x1, y in ((60, 128, 62), (0, 50, 66), (40, 110, 71)):
        for x in range(x0, x1):
            if (x + y) % 3 != 0:
                M.px(x, y, NUBE[4])
    return M


def perdidos_morvain():
    """Morvain al ponerse el sol: el Arbol de la Horca con un lazo vacio, arboles muertos y, lejos en el cielo, la ciudadela
    encadenada; la tierra marchita delante (los libros perdidos 18 y 19, los de Morvain)."""
    M = Mini()
    M.degradado(["#1a0a14", "#3a1018", "#6a2018", "#a04a1a", "#d08a2a", "#f0c050"], 0, 58)
    # el sol, bajo y grande, tras la bruma
    M.elipse(84, 50, 13, 13, "#f0b040")
    M.elipse(84, 50, 10, 10, "#f8d070")
    for x0, x1, y in ((64, 104, 44), (70, 100, 48), (60, 96, 53)):
        M.hline(x0, y, x1 - x0, "#c87a2a")
    # la ciudadela encadenada, lejos, arriba a la derecha (con dos cadenas que bajan)
    fig = capa()
    fig.poli([(102, 22), (124, 22), (119, 27), (113, 31), (107, 27)], "#2a1a22")
    fig.rect(108, 14, 3, 8, "#2a1a22")
    fig.rect(113, 10, 3, 12, "#2a1a22")
    fig.rect(118, 15, 3, 7, "#2a1a22")
    fig.poli([(112, 10), (114.5, 5), (117, 10)], "#2a1a22")
    for k in range(6):
        fig.px(104 - k * 2, 27 + k * 2, "#3a2430")
        fig.px(121 + k, 27 + k * 2, "#3a2430")
    pegar_con_tinta(M, fig, "#14080e")
    M.px(114, 14, FUEGO[3]).px(109, 17, FUEGO[2]).px(119, 18, FUEGO[2])
    # la tierra marchita
    M.degradado(["#2a1a12", "#1e120c", "#140c08", "#0c0806"], 58, H)
    r = np.random.default_rng(19)
    for _ in range(40):
        x, y = int(r.integers(0, W)), int(r.integers(60, H))
        M.px(x, y, "#3a2416" if y < 68 else "#24160e")
    # arboles muertos lejos (siluetas)
    fig = capa()
    for x, alto in ((30, 16), (44, 12), (58, 18), (100, 14), (118, 11)):
        fig.rect(x, 60 - alto, 2, alto, "#1a0e0c")
        fig.linea(x + 1, 60 - alto + 4, x + 5, 60 - alto, "#1a0e0c")
        fig.linea(x, 60 - alto + 6, x - 4, 60 - alto + 2, "#1a0e0c")
    pegar_con_tinta(M, fig, "#0c0606")
    # el Arbol de la Horca, delante a la izquierda: tronco grueso, ramas torcidas y el lazo vacio
    fig = capa()
    fig.poli([(6, 80), (8, 40), (6, 22), (10, 22), (13, 38), (16, 40), (18, 80)], MADERA[1])
    fig.vline(9, 24, 54, MADERA[2])
    fig.poli([(10, 24), (40, 14), (42, 16), (12, 28)], MADERA[1])
    fig.poli([(8, 30), (0, 22), (1, 20), (10, 27)], MADERA[1])
    fig.poli([(14, 36), (30, 30), (31, 32), (15, 39)], MADERA[1])
    fig.linea(40, 15, 46, 10, MADERA[1])
    fig.linea(26, 19, 28, 12, MADERA[1])
    # raices al aire
    fig.poli([(6, 76), (0, 80), (4, 80)], MADERA[1])
    fig.poli([(18, 76), (26, 80), (20, 80)], MADERA[1])
    pegar_con_tinta(M, fig)
    # las sogas: el lazo vacio que cuelga de la rama larga y un cabo cortado
    for y in range(17, 38):
        M.px(36, y, "#8a7046" if y % 3 else "#6a5434")
    M.aro(36, 42, 3.4, 4.6, "#8a7046", 1.2)
    for y in range(21, 31):
        M.px(24, y, "#8a7046" if y % 3 else "#6a5434")
    M.px(23, 31, "#6a5434").px(25, 32, "#6a5434")
    # unos cuervos en el cielo
    for x, y in ((56, 18), (62, 14), (70, 20)):
        M.px(x - 1, y - 1, NEGRO).px(x, y, NEGRO).px(x + 1, y - 1, NEGRO)
    return M


# ================================================================= 7-10. La cronica de Morvain (parte 7ae)
def _ciudadela(M, cx, cy, esc=1.0, c="#2a1a22", cadenas=True):
    """La ciudadela encadenada, pequena y lejos: la isla con sus torres y dos cadenas que bajan."""
    fig = capa()
    w = 11 * esc
    fig.poli([(cx - w, cy), (cx + w, cy), (cx + w * 0.55, cy + 4 * esc), (cx, cy + 8 * esc), (cx - w * 0.55, cy + 4 * esc)], c)
    for dx, h in ((-6, 6), (-2, 10), (3, 8), (7, 5)):
        fig.rect(int(cx + dx * esc), int(cy - h * esc), max(2, int(2 * esc)), int(h * esc), c)
    fig.poli([(cx - 2 * esc, cy - 10 * esc), (cx - 1 * esc + 0.5, cy - 14 * esc), (cx + 0.5, cy - 10 * esc)], c)
    if cadenas:
        for k in range(int(9 * esc)):
            fig.px(int(cx - w + 2 - k * 1.6), int(cy + 3 + k * 1.4), "#3a2430")
            fig.px(int(cx + w - 2 + k * 1.6), int(cy + 3 + k * 1.4), "#3a2430")
    pegar_con_tinta(M, fig, "#14080e")
    M.px(int(cx - 1 * esc), int(cy - 6 * esc), FUEGO[3]).px(int(cx + 3 * esc), int(cy - 4 * esc), FUEGO[2])
    return M


def morvain_tierras():
    """VII, Las Tierras Marchitas: el ocaso dorado y rojo, el molino de aspas rotas en su loma, el campanario con su campana,
    el campo seco y, lejos en el cielo, la ciudadela colgada de sus cadenas."""
    M = Mini()
    M.degradado(["#1a0a14", "#3a1018", "#6a2018", "#a04a1a", "#d08a2a", "#f0c050"], 0, 60)
    M.elipse(64, 56, 12, 12, "#f0b040")
    M.elipse(64, 56, 9, 9, "#f8d070")
    _ciudadela(M, 64, 18, 1.0)
    # la loma del molino (izquierda) y la del campanario (derecha)
    M.poli([(0, 80), (0, 52), (14, 48), (34, 52), (48, 60), (56, 80)], "#1e120c")
    M.poli([(70, 80), (80, 58), (100, 54), (128, 56), (128, 80)], "#1a100a")
    M.degradado(["#2a1a12", "#1a100a", "#0c0806"], 62, H)
    fig = capa()
    # el molino: torre que se estrecha, tejadillo y tres aspas (una rota)
    fig.poli([(14, 50), (26, 50), (24, 26), (16, 26)], MADERA[1])
    fig.poli([(14, 26), (26, 26), (20, 19)], MADERA[0])
    for ang, largo in ((-60, 15), (30, 14), (120, 15), (210, 6)):
        a = math.radians(ang)
        x1, y1 = 20 + math.cos(a) * largo, 24 + math.sin(a) * largo
        fig.linea(20, 24, x1, y1, MADERA[2])
        fig.linea(20 + 1, 24, x1 + 1, y1, MADERA[1])
        if largo > 10:
            # el entramado de la aspa (a un lado)
            for k in range(4, int(largo), 3):
                px, py = 20 + math.cos(a) * k, 24 + math.sin(a) * k
                fig.linea(px, py, px + math.cos(a + 1.57) * 3, py + math.sin(a + 1.57) * 3, MADERA[1])
    fig.rect(18, 42, 4, 8, NEGRO)
    # el campanario: torre cuadrada, el hueco con la campana, el chapitel
    fig.rect(96, 26, 14, 30, "#241a1e")
    fig.rect(99, 30, 8, 9, NEGRO)
    fig.poli([(94, 26), (112, 26), (103, 12)], "#1a1216")
    pegar_con_tinta(M, fig)
    M.elipse(103, 34, 2.6, 2.4, ORO[3])
    M.px(103, 37, ORO[4])
    # el campo seco: espigas muertas
    r = np.random.default_rng(71)
    for _ in range(70):
        x, y = int(r.integers(30, 98)), int(r.integers(62, 79))
        M.vline(x, y - 3, 3, "#5a3e22" if r.random() < 0.6 else "#3a2614")
        M.px(x + (1 if r.random() < 0.5 else -1), y - 3, "#7a5a30")
    # brasas que suben
    for _ in range(26):
        x, y = int(r.integers(0, W)), int(r.integers(20, 70))
        M.px(x, y, FUEGO[2] if r.random() < 0.6 else FUEGO[3])
    return M


def morvain_quedaron():
    """VIII, Los que se quedaron: la puerta de la Capital cerrada con su rastrillo, el fuego detras de la muralla, la ceniza
    que cae y, delante, la estatua del caballero de la mano alzada y dos que siguen en pie, ardiendo por dentro."""
    M = Mini()
    M.degradado(["#120608", "#2a0a0c", "#5a1410", "#9a3a14", "#d0701c"], 0, 58)
    # el resplandor del fuego tras la muralla
    r = np.random.default_rng(83)
    for _ in range(160):
        x, y = int(r.integers(0, W)), int(r.normal(46, 7))
        if 0 <= y < 58:
            M.px(x, y, FUEGO[2] if r.random() < 0.5 else FUEGO[1])
    # la muralla con almenas y la puerta en medio (dos torres y el rastrillo)
    M.rect(0, 40, W, 26, "#1e1418")
    almenas(M, 40, 0, W, "#1e1418", alto=3, ancho=4, hueco=3)
    M.rect(44, 26, 14, 40, "#241a1e")
    M.rect(70, 26, 14, 40, "#241a1e")
    almenas(M, 26, 44, 58, "#241a1e", alto=3, ancho=3, hueco=2)
    almenas(M, 26, 70, 84, "#241a1e", alto=3, ancho=3, hueco=2)
    M.poli([(58, 66), (58, 46), (64, 40), (70, 46), (70, 66)], "#0e0808")
    for x in range(59, 70, 2):
        M.vline(x, 44, 22, "#4a4048")
    for y in range(48, 66, 3):
        M.hline(58, y, 12, "#3a3038")
    M.degradado(["#1a1012", "#0c0808"], 66, H)
    # la estatua: el pedestal y el caballero con la mano alzada (la cabeza rota)
    fig = capa()
    fig.rect(14, 64, 18, 6, "#3a3440")
    fig.rect(16, 60, 14, 4, "#4c4558")
    fig.poli([(19, 60), (27, 60), (26, 46), (20, 46)], "#5a5468")
    fig.poli([(20, 46), (26, 46), (27, 36), (19, 36)], "#625a70")
    fig.poli([(26, 38), (29, 37), (31, 24), (29, 23), (27, 34)], "#625a70")
    fig.rect(28, 21, 3, 3, "#625a70")
    fig.rect(21, 32, 4, 4, "#5a5468")
    # dos calcinados de pie, con el fuego por dentro
    for x0 in (92, 106):
        fig.poli([(x0, 70), (x0 + 6, 70), (x0 + 5, 56), (x0 + 1, 56)], "#1a0e0a")
        fig.elipse(x0 + 3, 53, 2.6, 2.8, "#1a0e0a")
    pegar_con_tinta(M, fig)
    for x0 in (92, 106):
        M.px(x0 + 2, 53, FUEGO[3]).px(x0 + 4, 53, FUEGO[3])
        M.vline(x0 + 3, 59, 6, FUEGO[1])
        M.px(x0 + 3, 61, FUEGO[2])
    # la ceniza que cae
    for _ in range(55):
        x, y = int(r.integers(0, W)), int(r.integers(0, H))
        M.px(x, y, "#9a8a86" if r.random() < 0.5 else "#6a5c58")
    return M


def morvain_marcado():
    """IX, El primer marcado: la cadena enorme que sube del ancla escalonada al cielo rojo, Aldric de rodillas en lo alto del
    ancla y, en el cielo, la marca de la Sombra apenas dibujada."""
    M = Mini()
    M.degradado(["#16060a", "#2e0a10", "#4e1014", "#7a1a16", "#a42e18"], 0, 64)
    # la marca, muy tenue en el cielo (un ojo en un aro roto)
    M.aro(96, 18, 11, 11, "#6a1a1a", 1.2)
    for k in range(12):
        if k in (2, 3):
            continue
        a = math.radians(k * 30)
        M.px(int(96 + math.cos(a) * 11), int(18 + math.sin(a) * 11), "#3a0c10")
    M.elipse(96, 18, 5, 2.2, "#8a2420")
    M.elipse(96, 18, 1, 2, "#16060a")
    # el ancla: piramide escalonada
    M.degradado(["#1e1012", "#120a0a", "#0a0606"], 64, H)
    fig = capa()
    for k, (w, y) in enumerate(((44, 72), (36, 66), (28, 60), (20, 54), (12, 48))):
        fig.rect(22 - w // 2 + 10, y, w, 6, PIEDRA_C[k % 2])
        fig.hline(22 - w // 2 + 10, y, w, PIEDRA_C[2])
    # la cadena: eslabones grandes que suben en diagonal hacia arriba a la derecha
    for i in range(8):
        cx, cy = 34 + i * 12, 44 - i * 7
        if i % 2 == 0:
            fig.aro(cx, cy, 6.5, 3.6, "#2a2830", 2.0)
        else:
            fig.rect(cx - 1, cy - 4, 3, 8, "#2a2830")
    pegar_con_tinta(M, fig)
    for i in range(0, 8, 2):
        cx, cy = 34 + i * 12, 44 - i * 7
        M.px(cx - 4, cy - 2, "#6a6470").px(cx - 3, cy - 3, "#6a6470")
    # Aldric de rodillas en lo alto, de espaldas, con la marca ardiendole
    fig = capa()
    fig.poli([(27, 48), (35, 48), (34, 42), (28, 42)], "#14100e")
    fig.poli([(28, 42), (34, 42), (33, 34), (29, 34)], "#1e1814")
    fig.elipse(31, 32, 2.4, 2.6, "#1e1814")
    pegar_con_tinta(M, fig)
    M.px(31, 38, FUEGO[3]).px(32, 38, FUEGO[2])
    # chispas de los que golpeaban la cadena
    r = np.random.default_rng(97)
    for _ in range(24):
        x, y = int(r.integers(8, 60)), int(r.integers(50, 78))
        M.px(x, y, FUEGO[3] if r.random() < 0.5 else FUEGO[2])
    return M


def morvain_hermanas():
    """X, Las hermanas del fuego: ocho llamas en corro alrededor de la Sombra atada en el cielo, y abajo la ultima llama (el
    fuego de almas de la capilla) con Maelis sentada a su lado."""
    M = Mini()
    M.degradado(["#05040c", "#0c0a1e", "#16123a", "#221a4e", "#2e1e52"], 0, 62)
    M.estrellas(30, 101, y1=50)
    # la Sombra atada: una mancha negra con ojos rojos y las ocho cadenas de las llamas
    cx, cy = 64, 26
    for k in range(8):
        a = math.radians(k * 45 + 22.5)
        x1, y1 = cx + math.cos(a) * 30, cy + math.sin(a) * 17
        M.linea(cx, cy, x1, y1, "#3a3448")
    fig = capa()
    fig.elipse(cx, cy, 9, 7, "#05030a")
    fig.poli([(cx - 6, cy - 4), (cx - 2, cy - 12), (cx, cy - 5), (cx + 3, cy - 13), (cx + 6, cy - 4)], "#05030a")
    pegar_con_tinta(M, fig, "#1a1030")
    M.px(cx - 3, cy - 1, ROJO[4]).px(cx + 2, cy - 1, ROJO[4])
    # las ocho hermanas: llamitas en corro (una sigue encendida en azul)
    for k in range(8):
        a = math.radians(k * 45 + 22.5)
        x, y = int(cx + math.cos(a) * 30), int(cy + math.sin(a) * 17)
        azul = k == 2
        cs = ("#5aa0d8", "#a0e0f8", "#e0f8ff") if azul else (FUEGO[1], FUEGO[2], FUEGO[4])
        for dx, dy in ((-1, 0), (1, 0), (0, 1), (-1, -1), (1, -1)):
            M.px(x + dx, y + dy, mezcla(M.leer(x + dx, y + dy), cs[0], 0.55))
        M.px(x, y, cs[1]).px(x, y - 1, cs[2]).px(x, y - 2, cs[1]).px(x, y - 3, cs[0])
    # abajo: la capilla oscura, el brasero con el fuego de almas y Maelis sentada
    M.degradado(["#120e18", "#0a0810"], 62, H)
    fig = capa()
    fig.poli([(54, 78), (74, 78), (70, 70), (58, 70)], PIEDRA_C[1])
    fig.rect(56, 68, 16, 3, PIEDRA_C[2])
    fig.poli([(84, 78), (96, 78), (95, 70), (93, 64), (89, 62), (86, 64), (85, 70)], "#1c1626")
    fig.elipse(90, 60, 2.8, 3.0, "#241c30")
    pegar_con_tinta(M, fig)
    for i, (dx, alto) in enumerate(((-4, 6), (0, 10), (4, 7))):
        for h in range(alto):
            M.px(64 + dx + (1 if h % 3 == 1 else 0), 67 - h, "#e0f8ff" if h < 2 else "#a0e0f8" if h < 5 else "#5aa0d8")
    M.px(93, 66, "#a0e0f8").px(93, 65, "#5aa0d8")
    return M


def campana_peregrino():
    """La campana del peregrino (parte 7ag, la mision de Oswin): el ocaso tras el Arbol de la Horca, de cuya rama cuelga la
    campanita de oro de una soga; abajo, el camino del bosque muerto y, lejos, el peregrino ciego con su sombrero y su bordon."""
    M = Mini()
    M.degradado(["#0e0812", "#22101a", "#4a1a1c", "#7a3020", "#b05a22", "#d89434"], 0, 62)
    M.elipse(100, 58, 10, 10, "#e8a43a")
    M.elipse(100, 58, 7, 7, "#f6cc6a")
    r = np.random.default_rng(97)
    # el bosque muerto al fondo: troncos y ramas secas
    for k in range(9):
        x = 6 + k * 14 + int(r.integers(-3, 4))
        alto = int(r.integers(14, 26))
        M.poli([(x - 1, 64), (x + 2, 64), (x + 1, 64 - alto), (x, 64 - alto)], "#2a1a1a")
        M.linea(x, 64 - alto + 4, x + int(r.integers(-5, 6)), 64 - alto - 2, "#2a1a1a")
    M.degradado(["#241612", "#160e0c", "#0a0706"], 62, H)
    # el camino que se va al fondo, entre las raices
    M.poli([(52, 80), (84, 80), (74, 66), (66, 66)], "#3a2a1e")
    M.poli([(58, 80), (78, 80), (72, 68), (68, 68)], "#4a3626")
    fig = capa()
    # el Arbol de la Horca: tronco grueso a la izquierda, raices al aire y la rama larga que se inclina sobre el camino
    fig.poli([(4, 80), (26, 80), (24, 66), (21, 30), (18, 10), (12, 4), (8, 12), (10, 34), (8, 66)], "#1a100c")
    fig.poli([(2, 80), (10, 72), (6, 80)], "#1a100c")
    fig.poli([(24, 80), (34, 76), (30, 80)], "#1a100c")
    fig.poli([(18, 22), (40, 18), (62, 20), (76, 26), (78, 29), (62, 24), (40, 23), (20, 28)], "#1a100c")
    fig.poli([(14, 10), (4, 2), (6, 1), (16, 8)], "#1a100c")
    fig.poli([(40, 19), (46, 8), (48, 9), (43, 20)], "#1a100c")
    # la soga y otra soga con su lazo vacio
    fig.linea(60, 24, 60, 37, "#4a3a2a")
    fig.linea(40, 22, 40, 34, "#3a2e22")
    fig.elipse(40, 36, 2.0, 2.4, "#3a2e22")
    fig.elipse(40, 36, 1.0, 1.4, "#22140e")
    # el peregrino, lejos al final del camino: sombrero de ala ancha y plana, la capa y el bordon mas alto que el
    fig.poli([(70, 66), (73, 66), (72.5, 60), (70.5, 60)], "#120c10")
    fig.rect(68, 59, 7, 1, "#120c10")
    fig.rect(70, 58, 3, 1, "#120c10")
    fig.linea(75, 66, 76, 55, "#2a1e16")
    fig.elipse(76, 57, 0.8, 1.0, "#2a1e16")
    pegar_con_tinta(M, fig)
    # la campana de oro, colgando del cabo de la soga, con su badajo
    for y, (x0, x1) in enumerate(((59, 61), (58, 62), (58, 62), (57, 63), (57, 63), (56, 64))):
        for x in range(x0, x1 + 1):
            M.px(x, 38 + y, ORO[5] if x < (x0 + x1) // 2 - 1 else ORO[4] if x <= (x0 + x1) // 2 else ORO[2])
    M.px(59, 38, ORO[6]).px(58, 39, ORO[6]).px(58, 40, ORO[6])
    M.hline(55, 44, 11, NEGRO)
    for x in range(55, 66):
        M.px(x, 43, ORO[3] if x % 2 else ORO[2])
    M.px(60, 44, ORO[1]).px(60, 45, ORO[1])
    # el brillo de la campana y las motas de ceniza
    for dx, dy in ((-4, -2), (4, -1), (-5, 3), (5, 2), (0, -3)):
        M.px(60 + dx, 41 + dy, mezcla(M.leer(60 + dx, 41 + dy), ORO[5], 0.5))
    for _ in range(22):
        x, y = int(r.integers(0, W)), int(r.integers(4, 66))
        M.px(x, y, "#9a8a7a" if r.random() < 0.5 else "#6a5a52")
    return M


def bufanda_roja():
    """La bufanda roja (parte 7ah, la mision de Hedda): al ocaso, la Puerta Oeste de la capital quemada al fondo, con el rastrillo
    medio bajado y el humo; en el camino, una vieja sentada en su taburete junto a su farol, tejiendo, y la labor roja que le cae
    al suelo: lo unico con color."""
    M = Mini()
    M.degradado(["#0c0810", "#1e0e14", "#3e1618", "#6a2a1a", "#9a4a1e", "#c47428"], 0, 60)
    r = np.random.default_rng(131)
    # el humo que sube de la ciudad
    for k in range(5):
        x0 = 70 + k * 12 + int(r.integers(-3, 4))
        for y in range(2, 40):
            ancho = 2 + (40 - y) // 6
            for x in range(x0 - ancho + int(math.sin(y * 0.3 + k) * 3), x0 + ancho + int(math.sin(y * 0.3 + k) * 3)):
                if 0 <= x < W and r.random() < 0.5:
                    M.px(x, y, mezcla(M.leer(x, y), "#2a1c1e", 0.35))
    M.degradado(["#1e1210", "#140c0a", "#0a0606"], 60, H)
    # el camino de tierra hacia la puerta
    M.poli([(20, 80), (60, 80), (86, 58), (78, 58)], "#2e2016")
    fig = capa()
    # la muralla y la puerta, dos torres con almenas y el arco con el rastrillo
    fig.rect(56, 34, 72, 26, "#120a0c")
    for x in range(56, 128, 4):
        fig.rect(x, 31, 2, 3, "#120a0c")
    fig.rect(70, 20, 12, 40, "#120a0c")
    fig.rect(92, 20, 12, 40, "#120a0c")
    for x in (70, 74, 78, 92, 96, 100):
        fig.rect(x, 17, 2, 3, "#120a0c")
    # un carro quemado volcado en el camino
    fig.poli([(44, 64), (58, 62), (60, 66), (46, 68)], "#1a100c")
    fig.elipse(47, 68, 3, 3, "#1a100c")
    # la vieja en su taburete, de perfil, encorvada sobre la labor
    fig.poli([(18, 70), (20, 58), (24, 55), (28, 56), (30, 62), (31, 70)], "#14100e")  # el cuerpo con el chal
    fig.elipse(25, 53, 3.2, 3.4, "#14100e")  # la cabeza con el panuelo
    fig.poli([(17, 70), (33, 70), (33, 72), (17, 72)], "#1a120c")  # el taburete
    fig.rect(18, 72, 1, 6, "#1a120c")
    fig.rect(31, 72, 1, 6, "#1a120c")
    fig.poli([(28, 62), (34, 63), (35, 66), (29, 66)], "#14100e")  # los brazos tejiendo
    pegar_con_tinta(M, fig)
    # la puerta: el arco por dentro con el resplandor del fuego y los barrotes del rastrillo
    for y in range(36, 60):
        for x in range(82, 92):
            M.px(x, y, mezcla("#6a2a12", "#c86a20", (y - 36) / 24 * 0.6) if (x + y) % 5 else "#3a1a10")
    for x in range(83, 92, 2):
        for y in range(36, 50):
            M.px(x, y, "#1a1014")
    # el farol en el suelo, con su luz
    for y in range(66, 76):
        for x in range(4, 16):
            d = math.hypot(x - 10, y - 72)
            if d < 6:
                M.px(x, y, mezcla(M.leer(x, y), "#e8a440", (1 - d / 6) * 0.5))
    M.rect(9, 70, 3, 4, "#f6cc6a").rect(9, 69, 3, 1, "#3a2a1a").rect(10, 68, 1, 1, "#3a2a1a")
    # la labor roja: en sus manos y cayendo al suelo, y el ovillo
    for (x, y) in ((33, 64), (34, 65), (34, 66), (35, 67), (35, 68), (36, 69), (36, 70), (37, 71), (37, 72), (38, 73), (39, 74)):
        M.px(x, y, "#c43a34").px(x + 1, y, "#82191b")
    M.elipse(42, 75, 2.0, 1.8, "#a52626")
    M.px(41, 74, "#de5c4c")
    # las agujas, un destello
    M.px(32, 62, "#d8b070").px(33, 61, "#d8b070")
    for _ in range(18):
        x, y = int(r.integers(0, W)), int(r.integers(4, 58))
        M.px(x, y, "#c86a2a" if r.random() < 0.3 else "#6a5a52")
    return M


def luz_muelle():
    """La luz del muelle (parte 7an, la mision de Ylva): el ocaso sobre la bahia seca del Puerto Varado; el espigon de piedra que
    se va hasta el faro de hueso con su fuego frio; al final del espigon, Ylva sentada en su noray con el farol azul en el regazo;
    lejos, en el horizonte del norte, las costillas de la ballena y una lucecita ambar que le contesta."""
    M = Mini()
    M.degradado(["#080a18", "#10162c", "#1c2440", "#2e3048", "#4e3c44", "#7a5440", "#a87444"], 0, 46)
    M.estrellas(18, 211, y1=22)
    r = np.random.default_rng(139)
    # el horizonte: la bahia seca hasta donde alcanza la vista
    M.degradado(["#3a3026", "#2e261e", "#241e18", "#1a1612", "#110e0c"], 46, H)
    # la sal y las grietas del fondo del mar
    for _ in range(70):
        x, y = int(r.integers(0, W)), int(r.integers(47, H))
        M.px(x, y, mezcla(M.leer(x, y), "#d8dcd6", 0.25 + 0.25 * (y - 46) / 34))
    for k in range(6):
        x, y = int(r.integers(4, 120)), int(r.integers(56, 78))
        for i in range(int(r.integers(4, 9))):
            M.px(x + i, y + int(math.sin(i * 1.3 + k) * 1.2), "#0e0b09")
    # las costillas de la ballena en el horizonte (el Costillar) y su lucecita ambar: alguien que ve la luz
    for k in range(7):
        cx = 24 + k * 4
        alto = 7 - abs(k - 3) * 1.2
        medio_aro(M, cx, 46, 2.2, alto, "#1a1414", 1.0)
    M.hline(20, 46, 30, "#1a1414")
    for d, a in ((3, 0.18), (2, 0.35), (1, 0.6)):
        for y in range(43 - d, 43 + d + 1):
            for x in range(36 - d, 36 + d + 1):
                if math.hypot(x - 36, y - 43) <= d:
                    M.px(x, y, mezcla(M.leer(x, y), "#f0a030", a))
    M.px(36, 43, "#ffe0a0")
    fig = capa()
    # dos barcas varadas en la arena, de lado
    fig.poli([(10, 66), (26, 66), (23, 70), (13, 70)], "#16100c")
    fig.poli([(50, 58), (60, 57), (58, 60), (52, 60)], "#16100c")
    fig.linea(55, 57, 57, 50, "#16100c")
    # el espigon: la cara de piedra que cae a la bahia (mas alta cerca) y lo de arriba
    fig.poli([(0, 60), (94, 51), (94, 54), (0, 68)], "#1e1a1c")
    # el faro de hueso en su penon, con la galeria y la linterna
    fig.poli([(92, 56), (100, 50), (118, 50), (124, 56)], "#1a1618")
    fig.poli([(102, 50), (104, 18), (112, 18), (114, 50)], "#4a4440")
    fig.rect(101, 16, 15, 2, "#2a2624")
    fig.rect(104, 10, 9, 6, "#2a2624")
    fig.poli([(103, 10), (108.5, 5), (114, 10)], "#2a2624")
    # Ylva al final del espigon, sentada en su noray, de perfil mirando al norte (a la izquierda)
    fig.rect(87, 48, 3, 3, "#16131a")  # el noray
    fig.poli([(85, 48), (86, 42), (89, 41), (90, 48)], "#14101a")  # la espalda con la esclavina
    fig.elipse(87.5, 39.5, 1.8, 2.0, "#14101a")  # la cabeza
    fig.poli([(83, 46), (86, 45), (86, 48), (83, 48)], "#14101a")  # las rodillas
    fig.rect(83, 48, 2, 3, "#14101a")  # las piernas
    pegar_con_tinta(M, fig)
    # las costillas del faro y sus bandas
    for y in range(20, 50, 5):
        M.hline(103 + (50 - y) // 16, y, 10 - 2 * ((50 - y) // 16), "#6a625a")
    # el pretil y sus faroles de almas a lo largo del espigon
    for x in range(0, 94, 1):
        y = 60 - x * 9 / 94
        if x % 9 != 0:
            M.px(x, int(round(y)) - 1, "#2c2628")
    for x in (14, 38, 62):
        y = int(round(60 - x * 9 / 94)) - 2
        M.px(x, y, "#6ac8e6").px(x, y - 1, "#d6f6ff")
    # la luz fria del faro y la del farol de Ylva (con su halo)
    for (cx, cy, rr, fuerte) in ((108.5, 13, 11, 0.55), (84, 45, 8, 0.6)):
        for y in range(int(cy - rr), int(cy + rr) + 1):
            for x in range(int(cx - rr), int(cx + rr) + 1):
                d = math.hypot(x - cx, y - cy)
                if d < rr and 0 <= x < W and 0 <= y < H:
                    M.px(x, y, mezcla(M.leer(x, y), "#94d6f0", (1 - d / rr) ** 1.6 * fuerte))
    M.rect(105, 11, 7, 4, "#d6f6ff")
    M.rect(107, 12, 3, 2, "#ffffff")
    M.rect(83, 44, 2, 2, "#d6f6ff")
    M.px(83, 44, "#ffffff")
    # la trenza cobriza, lo unico calido junto a ella
    M.px(89, 40, "#a84e22").px(89, 41, "#843818").px(90, 42, "#a84e22")
    return M


def balizas_niebla():
    """Las balizas de la niebla (parte 7ao, la mision de Corwen): la laguna de los Pilotes de noche, con bancos de niebla; las chozas
    sobre sus pilotes y la torre de la capilla ahogada al fondo; dos balizas lejanas que arden en la niebla y, delante a la derecha, la
    tercera en su islote con el farol ambar; en el agua, dos barcas fantasma con su farolillo que vuelven a casa; abajo a la izquierda,
    Corwen en su caja al final de la pasarela, con la pertiga y su farol."""
    M = Mini()
    M.degradado(["#050807", "#0a100d", "#111a15", "#1a251e", "#26322a", "#33402f"], 0, 44)
    M.estrellas(10, 223, y1=16)
    r = np.random.default_rng(151)
    # el agua negra de la laguna
    M.degradado(["#1c2620", "#141c17", "#0e1410", "#090d0a", "#060806"], 44, H)
    # las chozas de la aldea sobre sus pilotes, lejos a la izquierda, y la torre de la capilla ahogada a la derecha
    lejos = capa()
    for (x0, ancho, alto) in ((6, 12, 6), (22, 9, 5), (35, 13, 7)):
        lejos.poli([(x0, 40), (x0 + ancho / 2, 40 - alto), (x0 + ancho, 40)], "#1a221c")
        lejos.rect(x0 + 1, 40, ancho - 2, 3, "#1a221c")
        for k in range(3):
            lejos.rect(x0 + 1 + k * (ancho - 3) // 2, 43, 1, 3, "#1a221c")
    lejos.rect(4, 44, 50, 1, "#1a221c")  # las pasarelas
    lejos.rect(96, 24, 6, 20, "#1c241e")  # la torre de la capilla
    lejos.poli([(95, 24), (99, 17), (103, 24)], "#1c241e")
    lejos.poli([(102, 30), (114, 30), (114, 44), (102, 44)], "#1c241e")
    M.pegar(lejos, 0, 0)
    # bancos de niebla por encima del agua (y un poco sobre las chozas)
    for y in range(26, 52):
        k = max(0.0, 1 - abs(y - 41) / 11) * 0.55
        for x in range(W):
            onda = 0.5 + 0.5 * math.sin(x / 7.0 + y * 0.6)
            M.px(x, y, mezcla(M.leer(x, y), "#5a6a5e", k * (0.55 + 0.45 * onda)))
    # el agua: reflejos y alguna onda clara
    for _ in range(40):
        x, y = int(r.integers(0, W)), int(r.integers(48, H))
        M.hline(x, y, int(r.integers(2, 6)), mezcla(M.leer(x, y), "#3a4a40", 0.5))
    # dos barcas fantasma en la niebla, con su farolillo palido
    for (bx, by) in ((58, 49), (76, 51)):
        M.poli([(bx - 5, by), (bx + 5, by), (bx + 3, by + 2), (bx - 3, by + 2)], "#2a362e")
        M.linea(bx, by, bx, by - 6, "#2a362e")
        M.px(bx + 1, by - 6, "#e8e0a0")
        for y in range(by - 9, by - 2):
            for x in range(bx - 3, bx + 5):
                d = math.hypot(x - bx - 1, y - by + 6)
                if d < 3.5:
                    M.px(x, y, mezcla(M.leer(x, y), "#c8c088", (1 - d / 3.5) * 0.35))
    # las balizas: dos lejos (el muelle y el islote del oeste) y la de delante, en su islote con el arbol muerto
    def halo(cx, cy, rr, fuerte):
        for y in range(int(cy - rr), int(cy + rr) + 1):
            for x in range(int(cx - rr), int(cx + rr) + 1):
                d = math.hypot(x - cx, y - cy)
                if d < rr and 0 <= x < W and 0 <= y < H:
                    M.px(x, y, mezcla(M.leer(x, y), "#f09030", (1 - d / rr) ** 1.5 * fuerte))
    for (bx, by, alto) in ((14, 41, 7), (44, 39, 6)):
        M.linea(bx, by, bx, by + alto, "#0c100d")
        M.hline(bx, by, 2, "#0c100d")
        halo(bx + 2, by + 1, 6, 0.5)
        M.px(bx + 2, by + 1, "#ffd070")
        # su reflejo en el agua
        for y in range(48, min(H, 48 + alto * 2)):
            if y % 2 == 0:
                M.px(bx + 2, y, mezcla(M.leer(bx + 2, y), "#f09030", 0.35))
    fig = capa()
    # el islote de delante a la derecha, con el arbol muerto
    fig.poli([(84, 62), (126, 60), (128, 66), (82, 67)], "#101410")
    fig.linea(112, 61, 113, 42, "#101410")
    fig.linea(113, 46, 120, 40, "#101410")
    fig.linea(113, 50, 106, 45, "#101410")
    fig.linea(120, 40, 123, 41, "#101410")
    # la baliza: el poste, el brazo y el farol colgado
    fig.rect(94, 34, 2, 28, "#141814")
    fig.rect(90, 34, 6, 2, "#141814")
    fig.rect(89, 36, 1, 2, "#141814")
    fig.rect(87, 38, 5, 1, "#141814")
    fig.rect(87, 44, 5, 1, "#141814")
    # Corwen en su caja al final de la pasarela, abajo a la izquierda: la caja, el chaqueton, el sombrero y la pertiga
    fig.rect(0, 70, 30, 3, "#121612")  # la pasarela
    fig.rect(13, 64, 8, 6, "#1a1612")  # la caja
    fig.poli([(14, 64), (15, 56), (20, 56), (21, 64)], "#121812")  # el cuerpo
    fig.elipse(17.5, 54, 2.0, 2.2, "#121812")  # la cabeza
    fig.poli([(13.5, 52.5), (21.5, 52.5), (20.5, 51), (14.5, 51)], "#2a2410")  # el ala del sombrero
    fig.poli([(19, 64), (25, 63), (25, 66), (19, 66)], "#121812")  # las piernas
    fig.linea(25, 70, 27, 40, "#1e1a12")  # la pertiga
    fig.linea(27, 40, 29, 40, "#3a2e16")
    fig.rect(28, 41, 3, 3, "#141814")  # su farol
    pegar_con_tinta(M, fig)
    # el farol de la baliza, encendido, y su reflejo
    M.rect(88, 39, 3, 5, "#f09030")
    M.rect(89, 40, 1, 3, "#fff0b0")
    halo(89.5, 41.5, 13, 0.55)
    for y in range(67, H):
        if y % 2:
            M.hline(87, y, 4, mezcla(M.leer(87, y), "#f09030", 0.3))
    # el farol de Corwen, encendido tambien
    M.px(29, 42, "#ffc860")
    halo(29.5, 42.5, 6, 0.4)
    # la barba blanca de Corwen
    M.px(18, 56, "#cecac2").px(17, 56, "#dedad2").px(17, 57, "#b0aca4")
    return M


ESCENAS = [corona_rota, fuente, trono, catedral, juramento, portal, perdidos_valdrenn, perdidos_fortaleza, diario, acertijo,
           perdidos_brumavela, perdidos_morvain, morvain_tierras, morvain_quedaron, morvain_marcado, morvain_hermanas,
           campana_peregrino, bufanda_roja, luz_muelle, balizas_niebla]


def montar():
    """Las miniaturas en un atlas de 512 x 400 (cuatro por fila; la quinta fila, las misiones: 7ag, 7ah, 7an, 7ao)."""
    A = Lienzo(512, 400)
    for i, f in enumerate(ESCENAS):
        m = f()
        A.pegar(m, (i % 4) * W, (i // 4) * H)
    return A


if __name__ == "__main__":
    A = montar()
    A.guardar(os.path.join(GUI, "miniaturas.png"))
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        from PIL import Image
        A.imagen().resize((512 * 3, 400 * 3), Image.NEAREST).save(os.path.join(d, "miniaturas.png"))
    print("miniaturas: hecho")
