#!/usr/bin/env python3
"""
Utiles para dibujar en pixel art la interfaz del mod (piedra oscura, oro gastado y rojo). Todo se dibuja al tamano
real (un pixel de la textura = un pixel de la interfaz), con paletas cortas y los detalles puestos a mano.
"""
import os

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
ASSETS = os.path.join(REPO, "src/main/resources/assets/darkcraft")
GUI = os.path.join(ASSETS, "textures", "gui")

# ---------------------------------------------------------------- paletas (de oscuro a claro)
PIEDRA = ["#08070b", "#0e0c13", "#15121b", "#1d1924", "#27222f", "#342e3d", "#453e4f", "#5a5266"]
ORO = ["#2a1c0a", "#4a3312", "#6e4e1c", "#96702a", "#c0963c", "#e2bf62", "#f6e3a0"]
ROJO = ["#1c0306", "#3a070c", "#5e0c13", "#8a121b", "#b81c26", "#de3a3a", "#f4786a"]
AZUL = ["#060a1e", "#0c1640", "#142866", "#1f3f96", "#2f5cc6", "#5a8ae6", "#a0c0f8"]
VERDE = ["#06180a", "#0e2e14", "#184a1e", "#27702a", "#3c9a3a", "#6cc45c", "#b0e89a"]
HUESO = ["#3c3628", "#6e6650", "#9c9378", "#c4bb9e", "#e2dcc4", "#f4f0e0"]
TINTA = "#050408"


def rgba(c, alfa=None):
    """'#rrggbb', '#rrggbbaa' o tupla -> (r, g, b, a)."""
    if isinstance(c, str):
        c = c.lstrip("#")
        v = [int(c[i:i + 2], 16) for i in range(0, len(c), 2)]
        if len(v) == 3:
            v.append(255)
    else:
        v = list(c)
        if len(v) == 3:
            v.append(255)
    if alfa is not None:
        v[3] = alfa
    return tuple(v)


def mezcla(a, b, t):
    a, b = rgba(a), rgba(b)
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(4))


class Lienzo:
    def __init__(self, w, h, fondo=None):
        self.w, self.h = w, h
        self.a = np.zeros((h, w, 4), np.uint8)
        if fondo is not None:
            self.a[:, :] = rgba(fondo)

    # ---- pixeles
    def px(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.a[y, x] = rgba(c)
        return self

    def leer(self, x, y):
        return tuple(int(v) for v in self.a[y, x]) if 0 <= x < self.w and 0 <= y < self.h else (0, 0, 0, 0)

    def rect(self, x, y, w, h, c):
        x0, y0, x1, y1 = max(0, x), max(0, y), min(self.w, x + w), min(self.h, y + h)
        if x1 > x0 and y1 > y0:
            self.a[y0:y1, x0:x1] = rgba(c)
        return self

    def hline(self, x, y, w, c):
        return self.rect(x, y, w, 1, c)

    def vline(self, x, y, h, c):
        return self.rect(x, y, 1, h, c)

    def marco(self, x, y, w, h, c):
        self.hline(x, y, w, c).hline(x, y + h - 1, w, c)
        return self.vline(x, y, h, c).vline(x + w - 1, y, h, c)

    def borrar(self, x, y, w=1, h=1):
        return self.rect(x, y, w, h, (0, 0, 0, 0))

    def sprite(self, filas, paleta, x=0, y=0):
        """Dibuja un sprite escrito como texto: cada letra es un color de la paleta ('.' o ' ' = nada)."""
        for j, fila in enumerate(filas):
            for i, ch in enumerate(fila):
                if ch in ". ":
                    continue
                self.px(x + i, y + j, paleta[ch])
        return self

    def pegar(self, otro, x, y):
        """Pega otro lienzo encima (mezclando por su alfa)."""
        src = otro.a if isinstance(otro, Lienzo) else np.asarray(otro)
        h, w = src.shape[:2]
        x0, y0, x1, y1 = max(0, x), max(0, y), min(self.w, x + w), min(self.h, y + h)
        if x1 <= x0 or y1 <= y0:
            return self
        s = src[y0 - y:y1 - y, x0 - x:x1 - x].astype(float)
        d = self.a[y0:y1, x0:x1].astype(float)
        sa = s[..., 3:4] / 255.0
        da = d[..., 3:4] / 255.0
        oa = sa + da * (1 - sa)
        rgb = np.where(oa > 0, (s[..., :3] * sa + d[..., :3] * da * (1 - sa)) / np.maximum(oa, 1e-6), 0)
        self.a[y0:y1, x0:x1, :3] = np.clip(np.round(rgb), 0, 255)
        self.a[y0:y1, x0:x1, 3:4] = np.clip(np.round(oa * 255), 0, 255)
        return self

    def espejo_h(self):
        o = Lienzo(self.w, self.h)
        o.a = self.a[:, ::-1].copy()
        return o

    def espejo_v(self):
        o = Lienzo(self.w, self.h)
        o.a = self.a[::-1].copy()
        return o

    def copia(self):
        o = Lienzo(self.w, self.h)
        o.a = self.a.copy()
        return o

    def recorte(self, x, y, w, h):
        o = Lienzo(w, h)
        o.a = self.a[y:y + h, x:x + w].copy()
        return o

    def contorno(self, c, diagonales=False):
        """Un pixel de contorno por fuera de todo lo dibujado."""
        op = self.a[..., 3] > 0
        borde = np.zeros_like(op)
        borde[1:, :] |= op[:-1, :]
        borde[:-1, :] |= op[1:, :]
        borde[:, 1:] |= op[:, :-1]
        borde[:, :-1] |= op[:, 1:]
        if diagonales:
            borde[1:, 1:] |= op[:-1, :-1]
            borde[:-1, :-1] |= op[1:, 1:]
            borde[1:, :-1] |= op[:-1, 1:]
            borde[:-1, 1:] |= op[1:, :-1]
        borde &= ~op
        self.a[borde] = rgba(c)
        return self

    def imagen(self):
        return Image.fromarray(self.a, "RGBA")

    def guardar(self, ruta):
        os.makedirs(os.path.dirname(ruta), exist_ok=True)
        self.imagen().save(ruta, optimize=True)
        return self


def piedra(w, h, semilla=1, base=2, grano=1):
    """Un trozo de piedra oscura: tono base de PIEDRA con grano de un tono arriba o abajo y alguna grieta."""
    r = np.random.default_rng(semilla)
    L = Lienzo(w, h, PIEDRA[base])
    n = r.random((h, w))
    for y in range(h):
        for x in range(w):
            if n[y, x] < 0.10 * grano:
                L.px(x, y, PIEDRA[min(len(PIEDRA) - 1, base + 1)])
            elif n[y, x] > 1 - 0.10 * grano:
                L.px(x, y, PIEDRA[max(0, base - 1)])
    return L


class Atlas:
    """Junta sprites en una textura y apunta donde cae cada uno (para escribir las constantes de Java)."""

    def __init__(self, w, h):
        self.L = Lienzo(w, h)
        self.x = self.y = self.fila = 0
        self.sitios = {}

    def poner(self, nombre, sprite, margen=1):
        w, h = sprite.w, sprite.h
        if self.x + w > self.L.w:
            self.x = 0
            self.y += self.fila + margen
            self.fila = 0
        if self.y + h > self.L.h:
            raise RuntimeError("no cabe " + nombre)
        self.L.a[self.y:self.y + h, self.x:self.x + w] = sprite.a
        self.sitios[nombre] = (self.x, self.y, w, h)
        self.x += w + margen
        self.fila = max(self.fila, h)
        return self.sitios[nombre]

    def java(self, paquete, clase, textura, comentario):
        lineas = [f"package {paquete};", "", "import net.minecraft.resources.ResourceLocation;", "",
                  f"/** {comentario} (lo escribe tools/ui: no tocar a mano). */", f"public final class {clase} {{",
                  f"    private {clase}() {{}}", "",
                  f"    public static final ResourceLocation TEX = new ResourceLocation(\"darkcraft\", \"{textura}\");",
                  f"    public static final int TW = {self.L.w}, TH = {self.L.h};", "",
                  "    /** Un trozo de la textura: u, v, ancho y alto. */",
                  "    public record T(int u, int v, int w, int h) {}", ""]
        for n, (x, y, w, h) in self.sitios.items():
            lineas.append(f"    public static final T {n.upper()} = new T({x}, {y}, {w}, {h});")
        lineas.append("}")
        return "\n".join(lineas) + "\n"
