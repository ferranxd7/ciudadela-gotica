"""
Pintar la textura de un modelo de tools/enemigos: cada caja tiene un material y cada material un pintor
f(cara, x, y, w, h) -> color ("#rrggbb", (r, g, b) o None = transparente).

Cada cara se pinta como se ve desde fuera, con x hacia la derecha de quien la mira e y hacia abajo:
  frente   x = 0 es el lado DERECHO del personaje          espalda   x = 0 es su lado IZQUIERDO
  der      (su costado derecho)  x = 0 es la ESPALDA       izq       (su costado izquierdo)  x = 0 es el FRENTE
  arriba / abajo   x = 0 es su lado derecho; y = 0 es la ESPALDA (el frente queda abajo en el dibujo)
`delante(cara, x, w)` da, en los costados, a cuantos pixeles esta x del canto de delante.
"""
import math

import numpy as np
from PIL import Image

from modelo import CARAS, caras_de


def hexa(c):
    if isinstance(c, str):
        c = c.lstrip("#")
        return tuple(int(c[i:i + 2], 16) / 255.0 for i in (0, 2, 4))
    return tuple(c)


def mezcla(a, b, t):
    a, b = hexa(a), hexa(b)
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def oscuro(c, t):
    return mezcla(c, (0, 0, 0), t)


def claro(c, t):
    return mezcla(c, (1, 1, 1), t)


def delante(cara, x, w):
    """En un costado, a cuantos pixeles esta x del canto de delante (0 = pegado al frente)."""
    return (w - 1 - x) if cara == "der" else x


def _azar(semilla):
    return np.random.default_rng(semilla).random((64, 64))


def tono(tonos, v):
    """El tono de la lista (de oscuro a claro) que toca a v (0..1)."""
    n = len(tonos)
    return tonos[int(np.clip(round(v * (n - 1)), 0, n - 1))]


# ---------------------------------------------------------------- pintores

def liso(c):
    return lambda cara, x, y, w, h: c


def ruido(tonos, semilla, grano=0.35, base=0.5):
    """Un color con grano (piel, cuero, piedra)."""
    r = _azar(semilla)
    k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

    def f(cara, x, y, w, h):
        return tono(tonos, base + (r[(y + 7 * k[cara]) % 64, (x + 13 * k[cara]) % 64] - 0.5) * grano)

    return f


def tela(tonos, semilla, pliegue=3, grano=0.14, luz=0.22):
    """Una tela con pliegues a lo alto: mas clara arriba, una raya oscura cada `pliegue` pixeles."""
    r = _azar(semilla)
    k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

    def f(cara, x, y, w, h):
        v = 0.5
        if cara in ("arriba", "abajo"):
            v += 0.12 if cara == "arriba" else -0.2
        else:
            v += luz * (0.5 - y / max(1, h - 1))
            q = (x + (y // 5) % 2 + k[cara]) % pliegue
            v += -0.2 if q == 0 else (0.1 if q == 1 else 0)
        v += (r[(y + 5 * k[cara]) % 64, (x + 11 * k[cara]) % 64] - 0.5) * grano
        return tono(tonos, v)

    return f


def madera(tonos, semilla, alo_largo="z", grano=0.3):
    """Madera con la veta a lo largo de un eje de la caja ("y" = a lo alto; "z" = hacia delante; "x" = de lado a lado)."""
    r = _azar(semilla)

    def f(cara, x, y, w, h):
        if alo_largo == "y":
            a, b = (x, y) if cara not in ("arriba", "abajo") else (x, 0)
        elif alo_largo == "z":
            a, b = (y, x) if cara in ("der", "izq") else ((x, y) if cara in ("arriba", "abajo") else (x, 0))
        else:
            a, b = (y, x) if cara in ("frente", "espalda") else ((y, x) if cara in ("arriba", "abajo") else (y, 0))
        v = 0.5 + (r[a % 64, (b // 3) % 64] - 0.5) * grano + (0.12 if a % 2 == 0 else -0.05)
        return tono(tonos, v)

    return f


def metal(tonos, semilla, grano=0.18):
    """Hierro: mas claro arriba y en los cantos de la cara, con picaduras."""
    r = _azar(semilla)

    def f(cara, x, y, w, h):
        v = 0.5 + 0.2 * (0.5 - y / max(1, h - 1)) + (r[y % 64, x % 64] - 0.5) * grano
        if x == 0 or y == 0:
            v += 0.18
        if x == w - 1 or y == h - 1:
            v -= 0.18
        return tono(tonos, v)

    return f


def sombra(f, abajo=0.22, cantos=0.10, arriba_claro=0.06):
    """Envuelve un pintor: oscurece hacia abajo y en los cantos de cada cara (da volumen a las cajas)."""

    def g(cara, x, y, w, h):
        c = f(cara, x, y, w, h)
        if c is None:
            return None
        c = hexa(c)
        if cara == "abajo":
            return oscuro(c, abajo + 0.12)
        if cara == "arriba":
            return claro(c, arriba_claro)
        t = abajo * (y / max(1, h - 1)) ** 1.5
        if x == 0 or x == w - 1:
            t += cantos
        return oscuro(c, min(0.8, t))

    return g


def por_cara(base, **caras):
    """Un pintor que usa otro distinto en las caras nombradas (frente=..., espalda=..., lados=..., tapas=...)."""
    mapa = {}
    for k, v in caras.items():
        if k == "lados":
            mapa["der"] = mapa["izq"] = v
        elif k == "tapas":
            mapa["arriba"] = mapa["abajo"] = v
        else:
            mapa[k] = v

    def f(cara, x, y, w, h):
        return mapa.get(cara, base)(cara, x, y, w, h)

    return f


def dibujo(filas, paleta, resto=None):
    """Un pintor de una cara a partir de un dibujo escrito como texto ('.' = transparente, o lo que diga `resto`)."""

    def f(cara, x, y, w, h):
        ch = filas[y][x] if y < len(filas) and x < len(filas[y]) else "."
        if ch == ".":
            return resto(cara, x, y, w, h) if resto else None
        if ch == " ":
            return None
        return paleta[ch]

    return f


def espejado(f):
    """El mismo dibujo visto por el otro lado (para el costado contrario o el reves de un plano)."""
    return lambda cara, x, y, w, h: f(cara, w - 1 - x, y, w, h)


# ---------------------------------------------------------------- la textura

class Textura:
    def __init__(self, rig):
        self.rig = rig
        e = rig.escala
        self.a = np.zeros((rig.tex_h * e, rig.tex_w * e, 4))

    def pintar_caja(self, b, f, caras=CARAS):
        e = self.rig.escala
        for cara, (x0, y0, w, h) in caras_de(b).items():
            if cara not in caras or w == 0 or h == 0:
                continue
            for y in range(h * e):
                for x in range(w * e):
                    c = f(cara, x, y, w * e, h * e)
                    if c is None:
                        self.a[y0 * e + y, x0 * e + x] = 0
                    else:
                        c = hexa(c)
                        self.a[y0 * e + y, x0 * e + x] = (c[0], c[1], c[2], 1.0)

    def pintar(self, materiales, falta="#ff00ff"):
        """materiales: {material: pintor}. Lo que no tenga pintor sale en rosa chillon (para verlo)."""
        hechos = set()
        for _, b in self.rig.cajas():
            clave = b.uv or id(b)
            if clave in hechos:
                continue
            hechos.add(clave)
            self.pintar_caja(b, materiales.get(b.mat) or liso(falta))
        return self

    def copia(self):
        t = Textura(self.rig)
        t.a = self.a.copy()
        return t

    def imagen(self):
        return Image.fromarray((np.clip(self.a, 0, 1) * 255).astype(np.uint8), "RGBA")

    def guardar(self, ruta):
        self.imagen().save(ruta)


def brillo_de(tex, materiales_brillo):
    """La capa de lo que brilla (ojos, llamas): solo las cajas de esos materiales, lo demas transparente."""
    t = Textura(tex.rig)
    hechos = set()
    for _, b in tex.rig.cajas():
        clave = b.uv or id(b)
        if clave in hechos:
            continue
        hechos.add(clave)
        f = materiales_brillo.get(b.mat)
        if f:
            t.pintar_caja(b, f)
    return t
