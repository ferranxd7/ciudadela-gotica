"""Texturas de lo que deja Ser Gavran (parte B3): el Mandoble del Juramento y la Llave de Hueso.

El mandoble va en diagonal (pomo abajo a la izquierda, punta arriba a la derecha) en 64x64: hoja negra con un
canal central y runas verdes, gavilan de raices retorcidas con espinas, empunadura de cuero y pomo con una gema
verde. Sale tambien la capa que brilla (runas, gema y venas del gavilan) para el modelo con capa emisiva.
La llave de hueso, en 32x32: anillo con forma de hoja tallada, cana y dientes, con una runa verde.

    python3 tools/gavran/objetos.py
"""
import math
import os
import random

from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), "..", "..", "src", "main", "resources", "assets", "darkcraft", "textures", "item")

NEGRO = (14, 14, 18)
ACERO = [(28, 30, 38), (38, 41, 52), (50, 54, 68), (66, 71, 88)]
FILO = (118, 126, 146)
CANAL = (10, 10, 14)
RAIZ = [(46, 30, 18), (68, 46, 28), (92, 64, 40), (118, 86, 56)]
CUERO = [(40, 24, 16), (58, 36, 22), (76, 50, 30)]
VERDE = (96, 255, 132)
VERDE_O = (40, 150, 70)


def lienzo(n):
    return Image.new("RGBA", (n, n), (0, 0, 0, 0))


def mandoble():
    N = 64
    base, brillo = lienzo(N), lienzo(N)
    pb, pg = base.load(), brillo.load()
    rnd = random.Random(11)
    x0, y0, x1, y1 = 5.5, 58.5, 59.0, 5.0
    L = math.hypot(x1 - x0, y1 - y0)
    dx, dy = (x1 - x0) / L, (y1 - y0) / L
    nx, ny = -dy, dx

    def mat(s, t):
        """Material en el punto (s a lo largo desde el pomo, t de lado): (color, brillo o None)."""
        a = abs(t)
        if s < 0:
            return None
        if s < 4.6:
            # pomo: anillo de metal con la gema
            r = math.hypot(s - 2.4, t)
            if r < 1.4:
                return VERDE, VERDE
            if r < 2.6:
                return ACERO[1] if t > 0 else ACERO[0], None
            return None
        if s < 15.0:
            # empunadura de cuero con vueltas
            if a < 1.55:
                k = int((s - 4.6) * 1.1) % 3
                c = CUERO[k] if t < 0.4 else CUERO[max(0, k - 1)]
                return c, None
            return None
        if s < 18.2:
            # gavilan de raices: largo, retorcido, con las puntas curvadas y alguna espina
            onda = math.sin(t * 0.9) * 0.6
            if a < 9.6 and abs(s - 16.6 - onda) < 1.5 + (0.4 if a < 2 else 0):
                k = (int(a * 1.3 + s) % 4)
                c = RAIZ[k] if (s - 16.6 - onda) < 0.6 else RAIZ[max(0, k - 2)]
                veta = abs(t) > 2.5 and int(a * 2) % 5 == 0
                return (VERDE_O if veta else c), (VERDE_O if veta else None)
            return None
        if s < 21.0:
            # recazo: acero liso un poco mas estrecho
            if a < 2.6:
                return ACERO[2] if t > 0.6 else ACERO[1], None
            return None
        # la hoja: se estrecha hacia la punta
        ancho = 3.2 if s < L - 11 else 3.4 * max(0.0, (L - 2.5 - s) / 8.5)
        if ancho < 0.5:
            return None
        if a > ancho:
            # espinas en el lomo, cerca del gavilan
            for s0 in (23.5, 28.0, 32.5):
                if t < 0 and 0 <= s - s0 < 1.6 and a < ancho + 1.8 - (s - s0):
                    return RAIZ[1], None
            return None
        if a > ancho - 0.9:
            return FILO if t > 0 else ACERO[3], None
        if a < 0.55 and s < L - 8:
            # canal central con runas que brillan cada pocos pixeles
            fase = (s - 22.0) % 4.4
            if 0.6 < fase < 2.4 and s < L - 10:
                return VERDE, VERDE
            return CANAL, None
        # acero negro con un poco de brillo hacia el filo
        k = 1 if t < 0 else 2
        if a > ancho - 1.6:
            k += 1
        return ACERO[min(3, k)], None

    for y in range(N):
        for x in range(N):
            px, py = x + 0.5 - x0, y + 0.5 - y0
            s = px * dx + py * dy
            t = px * nx + py * ny
            m = mat(s, t)
            if m is None:
                continue
            c, g = m
            if g is None and c not in (VERDE,):
                v = rnd.uniform(-6, 6)
                c = tuple(max(0, min(255, int(ch + v))) for ch in c)
            pb[x, y] = c + (255,)
            if g is not None:
                pg[x, y] = g + (255,)
    # contorno oscuro (para que se lea sobre cualquier fondo)
    contorno(base, NEGRO)
    return base, brillo


def contorno(im, c):
    w, h = im.size
    p = im.load()
    nuevos = []
    for y in range(h):
        for x in range(w):
            if p[x, y][3] != 0:
                continue
            for ddx, ddy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                xx, yy = x + ddx, y + ddy
                if 0 <= xx < w and 0 <= yy < h and p[xx, yy][3] != 0 and p[xx, yy][:3] != c:
                    nuevos.append((x, y))
                    break
    for x, y in nuevos:
        p[x, y] = c + (255,)


def llave():
    N = 32
    im = lienzo(N)
    p = im.load()
    HUESO = [(150, 138, 112), (196, 184, 156), (226, 216, 190), (244, 238, 220)]
    rnd = random.Random(5)
    # anillo (hoja tallada) arriba a la izquierda, cana en diagonal hasta abajo a la derecha
    cx, cy = 9.5, 9.5
    for y in range(N):
        for x in range(N):
            fx, fy = x + 0.5, y + 0.5
            r = math.hypot(fx - cx, fy - cy)
            ang = math.atan2(fy - cy, fx - cx)
            # contorno de hoja: radio que cambia con el angulo (cinco lobulos)
            R = 6.6 + 0.9 * math.cos(ang * 5)
            c = None
            if r < R and r > 2.6:
                k = 2 if (fx - cx) + (fy - cy) < 0 else 1
                if r > R - 1.0:
                    k = 0 if (fx - cx) + (fy - cy) > 0 else 1
                c = HUESO[k + (1 if rnd.random() < 0.2 else 0)]
            elif r <= 2.6 and r > 1.2:
                c = (60, 120, 70)
            elif r <= 1.2:
                c = VERDE
            # cana
            s = ((fx - cx) + (fy - cy)) / math.sqrt(2)
            t = ((fx - cx) - (fy - cy)) / math.sqrt(2)
            if 7.0 < s < 27.5 and abs(t) < 1.5:
                c = HUESO[2] if t < 0 else HUESO[1]
            # dientes (hacia un lado, cerca del final)
            for s0, largo in ((20.5, 3.4), (24.0, 4.4)):
                if s0 < s < s0 + 2.2 and 1.5 <= t < 1.5 + largo:
                    c = HUESO[1] if t > 1.5 + largo - 1 else HUESO[2]
            # nudos de la cana
            if abs(s - 11.5) < 0.8 and abs(t) < 2.2:
                c = HUESO[3]
            if c is not None:
                p[x, y] = c + (255,)
    contorno(im, (46, 38, 30))
    return im


def main():
    os.makedirs(OUT, exist_ok=True)
    b, g = mandoble()
    b.save(os.path.join(OUT, "mandoble_juramento.png"))
    g.save(os.path.join(OUT, "mandoble_juramento_brillo.png"))
    llave().save(os.path.join(OUT, "llave_hueso.png"))
    print("escrito: mandoble_juramento(.png, _brillo.png), llave_hueso.png")


if __name__ == "__main__":
    main()
