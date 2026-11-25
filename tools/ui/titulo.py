#!/usr/bin/env python3
"""
La pantalla de titulo (parte 2f-2): el fondo animado y el logo, en pixel art.

El fondo son capas de 480 x 270 (lo que mide la interfaz con la escala automatica en 1920 x 1080) que el juego pinta
estiradas a toda la pantalla y mueve por separado:
  cielo.png         el cielo de tormenta con la luna y las nubes altas (quieto)
  lejos.png         Valdrenn sobre su isla flotante (castillo, la Torre de Ascenso, la torre-arbol de Ser Gavran, los
                    rios que caen al vacio) y otras islas pequenas (quieto, transparente)
  nubes_lejos.png   el mar de nubes de atras (512 de ancho, se repite: va despacio)
  nubes_cerca.png   el mar de nubes de delante (512 de ancho, se repite: va mas deprisa)
  rayo.png          un rayo (sale de vez en cuando, con el destello)
  logo.png          "Dark Craft" en la letra gotica, en oro con filo rojo y tinta

    python3 tools/ui/titulo.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import GUI, Lienzo, ORO, ROJO, TINTA, rgba  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
SALIDA = os.path.join(GUI, "titulo")
W, H = 480, 270
BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16.0


def hexa(c):
    c = c.lstrip("#")
    return np.array([int(c[i:i + 2], 16) for i in (0, 2, 4)], dtype=float)


def ruido(w, h, escala_x, escala_y, semilla, octavas=4, envolver=False):
    """Ruido de valor suave (0..1), por octavas. Con envolver, se repite de lado a lado."""
    r = np.random.default_rng(semilla)
    total = np.zeros((h, w))
    amp, peso = 1.0, 0.0
    for o in range(octavas):
        sx, sy = escala_x / (2 ** o), escala_y / (2 ** o)
        gw = max(2, int(math.ceil(w / sx)) + 2)
        gh = max(2, int(math.ceil(h / sy)) + 2)
        if envolver:
            gw = max(2, int(round(w / sx)))
        g = r.random((gh + 1, gw + 1))
        if envolver:
            g[:, gw] = g[:, 0]
        ys = np.arange(h) / sy
        xs = np.arange(w) / sx
        if envolver:
            xs = np.arange(w) * gw / w
        y0 = np.floor(ys).astype(int)
        x0 = np.floor(xs).astype(int)
        fy = ys - y0
        fx = xs - x0
        fy = fy * fy * (3 - 2 * fy)
        fx = fx * fx * (3 - 2 * fx)
        a = g[y0][:, x0]
        b = g[y0][:, x0 + 1]
        c = g[y0 + 1][:, x0]
        d = g[y0 + 1][:, x0 + 1]
        v = (a * (1 - fx) + b * fx) * (1 - fy[:, None]) + (c * (1 - fx) + d * fx) * fy[:, None]
        total += v * amp
        peso += amp
        amp *= 0.5
    return total / peso


class Lienzo2(Lienzo):
    """Un lienzo con las herramientas de pintar escenas (degradados tramados, poligonos, lineas)."""

    def degradado(self, colores, y0, y1, x0=0, x1=None):
        x1 = self.w if x1 is None else x1
        n = len(colores) - 1
        for y in range(max(0, y0), min(self.h, y1)):
            t = (y - y0) / max(1, y1 - y0 - 1) * n
            i = min(n - 1, int(t))
            f = t - i
            for x in range(x0, x1):
                self.px(x, y, colores[i + 1] if f > BAYER[y % 4, x % 4] else colores[i])
        return self

    def poli(self, pts, c):
        ys = [p[1] for p in pts]
        for y in range(max(0, int(min(ys))), min(self.h, int(math.ceil(max(ys))) + 1)):
            cortes = []
            for i in range(len(pts)):
                (xa, ya), (xb, yb) = pts[i], pts[(i + 1) % len(pts)]
                if (ya <= y + 0.5 < yb) or (yb <= y + 0.5 < ya):
                    cortes.append(xa + (y + 0.5 - ya) * (xb - xa) / (yb - ya))
            cortes.sort()
            for a, b in zip(cortes[0::2], cortes[1::2]):
                for x in range(int(math.ceil(a - 0.5)), int(math.floor(b - 0.5)) + 1):
                    if 0 <= x < self.w:
                        self.px(x, y, c)
        return self

    def rect(self, x0, y0, w, h, c):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                if 0 <= x < self.w and 0 <= y < self.h:
                    self.px(x, y, c)
        return self


# ---------------------------------------------------------------- el cielo

CIELO = ["#040208", "#07040d", "#0a0612", "#0e0818", "#130a1c", "#1a0c20", "#240e22", "#321022", "#441420", "#5a1a1e",
         "#70241c", "#86301c"]
NUBE_ALTA = ["#0c0812", "#130d1a", "#1a1322", "#22192a", "#2c2234", "#3a2e42", "#4c3e54"]
LUNA = ["#8a7e6a", "#b4a88c", "#d2c6a6", "#e8dec0", "#f6eed6"]
LUNA_X, LUNA_Y, LUNA_R = 322, 70, 24


def cielo():
    L = Lienzo2(W, H, CIELO[0])
    L.degradado(CIELO, 0, 212)
    L.rect(0, 212, W, H - 212, CIELO[-1])
    # estrellas donde no hay nubes (se tapan luego)
    r = np.random.default_rng(11)
    for _ in range(170):
        x, y = int(r.integers(0, W)), int(r.integers(0, 130))
        b = r.random()
        L.px(x, y, "#f0e8ff" if b > 0.9 else "#a8a0c0" if b > 0.55 else "#5a5478")
    # el halo de la luna (tramado)
    for y in range(LUNA_Y - 60, LUNA_Y + 60):
        for x in range(LUNA_X - 60, LUNA_X + 60):
            d = math.hypot(x - LUNA_X, y - LUNA_Y)
            if LUNA_R < d < 58 and 0 <= x < W and 0 <= y < H:
                f = (58 - d) / (58 - LUNA_R)
                if f * f * 0.9 > BAYER[y % 4, x % 4]:
                    base = L.leer(x, y)
                    m = 0.18 + 0.22 * f
                    c = (np.array(base[:3], float) * (1 - m) + hexa("#6a5a6a") * m).astype(int)
                    L.px(x, y, (int(c[0]), int(c[1]), int(c[2]), 255))
    # la luna, con sus manchas
    manchas = ruido(2 * LUNA_R + 2, 2 * LUNA_R + 2, 9, 9, 5, 3)
    for y in range(LUNA_Y - LUNA_R, LUNA_Y + LUNA_R + 1):
        for x in range(LUNA_X - LUNA_R, LUNA_X + LUNA_R + 1):
            d = math.hypot(x - LUNA_X, y - LUNA_Y)
            if d <= LUNA_R:
                m = manchas[y - LUNA_Y + LUNA_R, x - LUNA_X + LUNA_R]
                borde = d / LUNA_R
                i = 3 if m < 0.52 else 2 if m < 0.62 else 1
                if borde > 0.86:
                    i = max(0, i - 1)
                # luz por arriba a la derecha
                if (x - LUNA_X) + (LUNA_Y - y) > LUNA_R * 0.9 and i < 4:
                    i += 1
                L.px(x, y, LUNA[i])
    # las nubes altas de la tormenta: bandas alargadas
    n = ruido(W, 200, 70, 22, 21, 5)
    banda = np.clip(1.0 - np.abs(np.arange(200) - 70) / 120.0, 0, 1)[:, None]
    n = n * (0.55 + 0.45 * banda)
    claro = ruido(W, 200, 30, 12, 22, 3)
    for y in range(200):
        for x in range(W):
            v = n[y, x]
            if v < 0.47:
                continue
            k = (v - 0.47) / 0.25
            i = min(len(NUBE_ALTA) - 3, int(k * 4 + BAYER[y % 4, x % 4]))
            # cerca de la luna, el filo de arriba se ilumina
            d = math.hypot(x - LUNA_X, y - LUNA_Y)
            if d < 95 and y > 0 and n[y - 1, x] < 0.5:
                i = min(len(NUBE_ALTA) - 1, i + 3 if d < 55 else i + 2)
            elif claro[y, x] > 0.64 and y > 0 and n[y - 1, x] < 0.5:
                i = min(len(NUBE_ALTA) - 1, i + 1)
            # hacia el horizonte, las nubes cogen el rojo del fondo
            c = hexa(NUBE_ALTA[i])
            if y > 120:
                f = min(1.0, (y - 120) / 80.0) * 0.45
                c = c * (1 - f) + hexa("#4a1820") * f
            L.px(x, y, (int(c[0]), int(c[1]), int(c[2]), 255))
    return L


# ---------------------------------------------------------------- Valdrenn y las islas

ROCA = ["#07050b", "#0c0912", "#120e19", "#1a1522", "#241d2e", "#30283c"]
SOMBRA = "#0a0710"
VENTANA = ["#9a4a0a", "#e08a1a", "#f8c040"]
ARBOL = ["#06090a", "#0a1110", "#0f1814", "#16221a", "#1e2e22"]
AGUA = ["#3a4a66", "#5a6a8a", "#8a9ab8", "#c0cce0"]


def isla(L, cx, ytop, ancho, alto, semilla, borde_luz=True):
    """Una isla flotante: arriba plana (con algo de relieve) y abajo en punta, con vetas y luz de luna a la derecha."""
    r = np.random.default_rng(semilla)
    n = ruido(ancho + 2, alto + 2, 6, 5, semilla, 3)
    mascara = np.zeros((alto + 2, ancho + 2), bool)
    for x in range(ancho):
        u = (x - ancho / 2) / (ancho / 2)
        fondo = alto * (1 - abs(u) ** 1.6) * (0.8 + 0.3 * n[0, x])
        top = int(round(1.5 * math.sin(x * 0.4 + semilla) + 2 * (n[1, x] - 0.5)))
        for y in range(max(0, top), min(alto + 1, int(fondo)) + 1):
            mascara[y, x] = True
    # rocas que cuelgan
    for _ in range(max(1, ancho // 18)):
        x = int(r.integers(ancho // 6, ancho - ancho // 6))
        largo = int(r.integers(alto // 5, alto // 2))
        col = [y for y in range(alto + 2) if mascara[y, x]]
        if not col:
            continue
        yb = col[-1]
        for k in range(largo):
            w2 = max(0, int((largo - k) / largo * 2.5))
            for dx in range(-w2, w2 + 1):
                if 0 <= x + dx < ancho and yb + k < alto + 2:
                    mascara[yb + k, x + dx] = True
    x0 = cx - ancho // 2
    for y in range(alto + 2):
        for x in range(ancho):
            if not mascara[y, x]:
                continue
            v = n[y, x]
            i = 1 + int(v * 3 + BAYER[y % 4, x % 4] * 0.6)
            if y % 5 == 0 and v > 0.45:
                i = max(0, i - 1)
            # la luz de la luna viene de arriba a la derecha
            if borde_luz and (x + 1 >= ancho or not mascara[y, x + 1]) and y < alto * 0.7:
                i = min(len(ROCA) - 1, i + 2)
            if y <= 1 or (y > 0 and not mascara[y - 1, x]):
                i = min(len(ROCA) - 1, i + 2)
            L.px(x0 + x, ytop + y, ROCA[min(len(ROCA) - 1, i)])
    return mascara


def torre(L, x, ybase, w, h, tejado, semilla, ventanas=2):
    r = np.random.default_rng(semilla)
    L.rect(x, ybase - h, w, h, ROCA[1])
    # filo con luz a la derecha
    for y in range(ybase - h, ybase):
        L.px(x + w - 1, y, ROCA[3])
        L.px(x, y, ROCA[0])
    top = ybase - h
    if tejado == "cono":
        alto = int(w * 1.6)
        L.poli([(x - 1, top), (x + w, top), (x + w / 2 - 0.5, top - alto)], ROCA[2])
        for k in range(alto):
            xx = int(x + w / 2 + (w / 2) * k / alto)
            L.px(xx, top - alto + k + 1, ROCA[4])
    elif tejado == "almenas":
        L.rect(x - 1, top - 2, w + 2, 2, ROCA[1])
        for k in range(0, w + 2, 3):
            L.rect(x - 1 + k, top - 4, 2, 2, ROCA[1])
    elif tejado == "aguja":
        alto = int(w * 3.2)
        L.poli([(x - 1, top), (x + w, top), (x + w / 2 - 0.5, top - alto)], ROCA[2])
        for k in range(alto):
            xx = int(x + w / 2 + (w / 2) * k / alto)
            L.px(xx, top - alto + k + 1, ROCA[4])
        L.px(int(x + w / 2), top - alto - 1, ROCA[3])
    # ventanas encendidas
    for _ in range(ventanas):
        vx = int(r.integers(x + 1, x + max(2, w - 1)))
        vy = int(r.integers(top + 3, ybase - 4))
        c = VENTANA[int(r.integers(0, 3))]
        L.px(vx, vy, c).px(vx, vy + 1, VENTANA[0])


def copa(L, cx, cy, rx, ry, semilla):
    """La copa del arbol de Ser Gavran sobre su torre: matas de hojas oscuras con el filo de arriba iluminado por la
    luna, ramas que asoman y lianas que cuelgan."""
    r = np.random.default_rng(semilla)
    n = ruido(2 * rx + 9, 2 * ry + 9, 4, 3, semilla, 3)
    # matas: elipses pequenas repartidas por la copa
    matas = [(0, 0, rx * 0.8, ry * 0.75)]
    for _ in range(16):
        a = r.uniform(0, 2 * math.pi)
        d = r.uniform(0.35, 0.85)
        matas.append((math.cos(a) * rx * d, math.sin(a) * ry * d * 0.9 - 2, r.uniform(4, 8), r.uniform(3, 6)))
    dentro = {}
    for (mx, my, mrx, mry) in matas:
        for y in range(int(my - mry) - 1, int(my + mry) + 2):
            for x in range(int(mx - mrx) - 1, int(mx + mrx) + 2):
                if ((x - mx) / mrx) ** 2 + ((y - my) / mry) ** 2 <= 1.0:
                    luz = max(0.0, -(y - my) / mry) * 0.7 + (x - mx) / mrx * 0.25
                    dentro[(x, y)] = max(dentro.get((x, y), -1.0), luz)
    for (x, y), luz in dentro.items():
        v = n[min(2 * ry + 8, max(0, y + ry + 4)), min(2 * rx + 8, max(0, x + rx + 4))]
        i = int(1 + luz * 2.6 + (v - 0.5) * 1.4 + BAYER[(y + 64) % 4, (x + 64) % 4] * 0.7)
        if (x, y - 1) not in dentro:
            i += 1
        L.px(cx + x, cy + y, ARBOL[max(0, min(len(ARBOL) - 1, i))])
    # lianas que cuelgan por debajo
    for _ in range(7):
        x = int(r.integers(-rx + 3, rx - 3))
        ys = [y for (xx, y) in dentro if xx == x]
        if not ys:
            continue
        y0 = max(ys)
        for k in range(int(r.integers(4, 11))):
            L.px(cx + x + (1 if k % 5 == 4 else 0), cy + y0 + 1 + k, ARBOL[1])


def cascada(L, x, y0, y1, ancho, semilla):
    r = np.random.default_rng(semilla)
    for y in range(y0, y1):
        f = (y - y0) / max(1, y1 - y0)
        for k in range(ancho):
            if f > BAYER[y % 4, (x + k) % 4] * 1.1 + 0.15 and r.random() < f:
                continue
            c = AGUA[min(3, int((1 - f) * 3 + (k == 0) * 1))]
            L.px(x + k + int(1.5 * math.sin(y * 0.15 + k)), y, c)


def lejos():
    L = Lienzo2(W, H)
    L.a[:] = 0
    # islas pequenas (a la izquierda, detras del menu, y alguna a la derecha)
    isla(L, 70, 176, 30, 20, 54)
    isla(L, 150, 140, 18, 13, 51)
    isla(L, 205, 190, 24, 17, 53)
    isla(L, 462, 150, 20, 14, 52)
    # en la isla de arriba, un arco roto (y una pizca de hueso: la fortaleza que espera)
    L.rect(143, 128, 2, 12, ROCA[2]).rect(155, 132, 2, 8, ROCA[2]).rect(143, 127, 9, 2, ROCA[2])
    L.px(149, 125, "#6e6650").px(150, 125, "#9c9378").px(149, 124, "#6e6650")
    # una ruina en la isla de la izquierda
    L.rect(62, 166, 3, 10, ROCA[2]).rect(68, 169, 2, 7, ROCA[2]).rect(62, 165, 9, 2, ROCA[2])
    # Valdrenn: la isla grande, a la derecha
    ytop = 162
    cx = 336
    isla(L, cx, ytop, 220, 78, 31)
    # la muralla
    x0, x1 = cx - 92, cx + 92
    L.rect(x0, ytop - 9, x1 - x0, 9, ROCA[1])
    for k in range(x0, x1, 4):
        L.rect(k, ytop - 11, 2, 2, ROCA[1])
    for x in range(x0, x1):
        L.px(x, ytop - 9, ROCA[3] if x % 7 else ROCA[4])
    # las torres (de izquierda a derecha)
    torre(L, cx - 88, ytop - 8, 9, 22, "cono", 1, 2)
    torre(L, cx - 68, ytop - 8, 11, 34, "almenas", 2, 3)
    torre(L, cx - 48, ytop - 8, 8, 26, "cono", 3, 2)
    # el torreon y la Torre de Ascenso: la aguja alta delante de la luna
    torre(L, cx - 26, ytop - 8, 20, 40, "almenas", 4, 4)
    torre(L, cx - 21, ytop - 48, 10, 40, "aguja", 5, 3)
    # la torre-arbol (la sala de Ser Gavran arriba, en la copa)
    torre(L, cx + 22, ytop - 8, 14, 52, "almenas", 6, 4)
    copa(L, cx + 29, ytop - 70, 24, 15, 61)
    for (dx, dy) in ((-22, 6), (21, 4), (-14, 12), (16, 11)):
        L.px(cx + 29 + dx, ytop - 70 + dy, ARBOL[1]).px(cx + 29 + dx + (1 if dx > 0 else -1), ytop - 70 + dy + 1, ARBOL[1])
    torre(L, cx + 50, ytop - 8, 9, 28, "cono", 7, 2)
    torre(L, cx + 66, ytop - 8, 12, 20, "almenas", 8, 2)
    torre(L, cx + 84, ytop - 8, 7, 16, "cono", 9, 1)
    # los rios que caen al vacio
    cascada(L, cx - 104, ytop + 4, 252, 2, 41)
    cascada(L, cx + 98, ytop + 6, 246, 3, 42)
    cascada(L, cx - 30, ytop + 50, 262, 1, 43)
    return L


# ---------------------------------------------------------------- el mar de nubes (se repite de lado a lado)

NUBE_LEJOS = ["#1c1424", "#251a2e", "#302438", "#3e3046", "#4e3e56", "#625068", "#7a6680"]
NUBE_CERCA = ["#0e0a14", "#150f1c", "#1e1626", "#281e30", "#34283c", "#42344a", "#54445c"]


def mar(ancho, alto, colores, semilla, rmin, rmax, ybase, cuantas):
    """Bollos de nube (medias elipses achatadas) a lo largo; los que se salen por un lado entran por el otro."""
    L = Lienzo2(ancho, alto)
    L.a[:] = 0
    r = np.random.default_rng(semilla)
    bollos = []
    for i in range(cuantas):
        x = (i + r.random() * 0.9) * ancho / cuantas
        rx = r.uniform(rmin, rmax)
        ry = rx * r.uniform(0.45, 0.7)
        y = ybase + r.uniform(-ry * 0.6, ry * 0.6)
        bollos.append((x, y, rx, ry))
    # alguno pequeno encima de otros (cumulos)
    for i in range(cuantas // 2):
        bx, by, brx, bry = bollos[int(r.integers(0, len(bollos)))]
        rx = brx * r.uniform(0.35, 0.6)
        bollos.append((bx + r.uniform(-brx * 0.5, brx * 0.5), by - bry * r.uniform(0.5, 0.9), rx, rx * 0.6))
    dentro = np.zeros((alto, ancho), bool)
    luz = np.full((alto, ancho), -1.0)
    for (bx, by, brx, bry) in bollos:
        for off in (-ancho, 0, ancho):
            cx = bx + off
            for y in range(max(0, int(by - bry) - 1), alto):
                for x in range(max(0, int(cx - brx) - 1), min(ancho, int(cx + brx) + 2)):
                    dx = (x - cx) / brx
                    dy = (y - by) / bry
                    if dy < 0 and dx * dx + dy * dy > 1.0:
                        continue
                    if dy >= 0 and abs(dx) > 1.0:
                        continue
                    dentro[y, x] = True
                    if dy < 0:
                        # lo de arriba de cada bollo recibe la luz (mas por la derecha, de la luna)
                        luz[y, x] = max(luz[y, x], -dy * 0.85 + dx * 0.3)
    n = ruido(ancho, alto, 14, 6, semilla + 7, 3, envolver=True)
    for y in range(alto):
        for x in range(ancho):
            if not dentro[y, x]:
                continue
            profundo = max(0.0, (y - ybase) / max(1, alto - ybase))
            v = 2.4 + max(0.0, luz[y, x]) * 3.2 - profundo * 3.0 + (n[y, x] - 0.5) * 1.6
            if y == 0 or not dentro[y - 1, x]:
                v += 1.4
            i = int(v + BAYER[y % 4, x % 4] * 0.9)
            L.px(x, y, colores[max(0, min(len(colores) - 1, i))])
    return L


# ---------------------------------------------------------------- el rayo

def rayo():
    w, h = 64, 170
    L = Lienzo2(w, h)
    L.a[:] = 0
    r = np.random.default_rng(77)
    nucleo = np.zeros((h, w), bool)

    def trazo(x, y, largo, deriva, grosor):
        for _ in range(largo):
            if not (0 <= y < h):
                break
            for k in range(grosor):
                if 0 <= x + k < w:
                    nucleo[y, x + k] = True
            y += 1
            if r.random() < 0.45:
                x += int(r.choice([-1, 1])) + (deriva if r.random() < 0.3 else 0)
                x = max(2, min(w - 3, x))
            if r.random() < 0.025 and grosor > 1:
                trazo(x, y, int(r.integers(12, 34)), int(r.choice([-1, 1])), 1)

    trazo(32, 0, h, 0, 2)
    for y in range(h):
        for x in range(w):
            if nucleo[y, x]:
                L.px(x, y, "#f6f2ff")
            else:
                cerca = False
                for dx in (-1, 1):
                    if 0 <= x + dx < w and nucleo[y, x + dx]:
                        cerca = True
                if cerca:
                    L.px(x, y, rgba("#a490ff", 200))
                elif (x > 1 and nucleo[y, x - 2]) or (x < w - 2 and nucleo[y, x + 2]):
                    L.px(x, y, rgba("#6a50c0", 90))
    return L


# ---------------------------------------------------------------- el logo

def logo(texto="Dark Craft", px=52):
    font = ImageFont.truetype(os.path.join(AQUI, "fuentes", "NewRocker-Regular.ttf"), px)
    caja = font.getbbox(texto)
    tw, th = caja[2] - caja[0], caja[3] - caja[1]
    pad = 6
    im = Image.new("L", (tw + 2 * pad, th + 2 * pad), 0)
    ImageDraw.Draw(im).text((pad - caja[0], pad - caja[1]), texto, font=font, fill=255)
    m = np.asarray(im) > 110
    hh, ww = m.shape
    # el oro: claro arriba, oscuro abajo (por filas de la letra), con un brillo en la mitad de arriba
    ys = np.where(m.any(axis=1))[0]
    y0, y1 = ys.min(), ys.max()
    oro = [ORO[6], ORO[5], ORO[5], ORO[4], ORO[4], ORO[3], ORO[3], ORO[2]]
    L = Lienzo(ww + 4, hh + 4)
    L.a[:] = 0
    for y in range(hh):
        for x in range(ww):
            if not m[y, x]:
                continue
            t = (y - y0) / max(1, y1 - y0)
            i = min(len(oro) - 1, int(t * (len(oro) - 1) + BAYER[y % 4, x % 4] * 0.8))
            # bisel: lo que tiene hueco arriba, mas claro; lo que tiene hueco abajo a la derecha, mas oscuro
            if y > 0 and not m[y - 1, x]:
                i = max(0, i - 2)
            elif y + 1 < hh and x + 1 < ww and not m[y + 1, x + 1]:
                i = min(len(oro) - 1, i + 2)
            L.px(x + 2, y + 2, oro[i])
    # filo rojo y luego tinta, por fuera
    a = L.a[..., 3] > 0
    for capa, color in ((1, ROJO[2]), (2, TINTA)):
        b = np.zeros_like(a)
        b[1:, :] |= a[:-1, :]
        b[:-1, :] |= a[1:, :]
        b[:, 1:] |= a[:, :-1]
        b[:, :-1] |= a[:, 1:]
        if capa == 2:
            b[1:, 1:] |= a[:-1, :-1]
            b[1:, :-1] |= a[:-1, 1:]
            b[:-1, 1:] |= a[1:, :-1]
            b[:-1, :-1] |= a[1:, 1:]
        b &= ~a
        L.a[b] = rgba(color)
        a = a | b
    # sombra dura abajo a la derecha
    sombra = np.zeros_like(a)
    sombra[2:, 2:] = a[:-2, :-2]
    sombra &= ~a
    L.a[sombra] = rgba("#000000", 150)
    return L


def montar():
    return {
        "cielo.png": cielo(),
        "lejos.png": lejos(),
        "nubes_lejos.png": mar(512, 90, NUBE_LEJOS, 61, 14, 30, 26, 22),
        "nubes_cerca.png": mar(512, 80, NUBE_CERCA, 62, 22, 46, 30, 13),
        "rayo.png": rayo(),
        "logo.png": logo(),
    }


# donde va cada mar de nubes (en pixeles del fondo de 480 x 270); TituloScreen usa lo mismo
NUBES_LEJOS_Y, NUBES_CERCA_Y = 204, 222


def vista(capas, carpeta):
    """La escena montada como la pinta el juego (con los mares a su altura, el logo y un menu de muestra)."""
    fondo = capas["cielo.png"].imagen().convert("RGBA")
    fondo.alpha_composite(capas["lejos.png"].imagen())
    nl = capas["nubes_lejos.png"].imagen()
    nc = capas["nubes_cerca.png"].imagen()
    for x in range(-40, W, 512):
        fondo.alpha_composite(nl, (x, NUBES_LEJOS_Y))
        fondo.alpha_composite(nc, (x - 130, NUBES_CERCA_Y))
    # el velo oscuro de la izquierda (lo pone el juego, para leer el menu)
    velo = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    vp = velo.load()
    for x in range(W):
        a = int(max(0.0, 1.0 - x / (W * 0.5)) ** 1.4 * 150)
        for y in range(H):
            vp[x, y] = (4, 2, 8, a)
    fondo.alpha_composite(velo)
    ry = capas["rayo.png"].imagen()
    lg = capas["logo.png"].imagen()
    sin_rayo = fondo.copy()
    fondo.alpha_composite(ry, (250, 4))
    for f in (fondo, sin_rayo):
        f.alpha_composite(lg, (18, 18))
    os.makedirs(carpeta, exist_ok=True)
    sin_rayo.resize((W * 3, H * 3), Image.NEAREST).save(os.path.join(carpeta, "titulo.png"))
    fondo.resize((W * 3, H * 3), Image.NEAREST).save(os.path.join(carpeta, "titulo_rayo.png"))
    lg.resize((lg.width * 4, lg.height * 4), Image.NEAREST).save(os.path.join(carpeta, "logo.png"))


if __name__ == "__main__":
    capas = montar()
    os.makedirs(SALIDA, exist_ok=True)
    for nombre, L in capas.items():
        L.guardar(os.path.join(SALIDA, nombre))
        print(nombre, L.w, "x", L.h)
    if "--vista" in sys.argv:
        vista(capas, sys.argv[sys.argv.index("--vista") + 1])
