"""El pintor de las texturas de los bloques nuevos (parte 8b): 64 x 64, que se repiten sin costuras.

Todo se pinta como un campo de luz (0 = lo mas oscuro de la rampa, 1 = lo mas claro) y al final se pasa a una rampa de
colores de cada material con un tramado ordenado (Bayer 4 x 4): asi queda pixel art nitido y no un degradado borroso.
Las capas de encima (musgo, escarcha, verdin, vetas) tienen su propia rampa y una mascara.

La luz viene siempre de arriba a la izquierda (como en las texturas de Minecraft): lo que sube hacia la derecha o hacia
abajo se ilumina (`luz_de`, `relieve`).

**Variantes** (para que un muro de 400 bloques no se vea a cuadros): cada receta recibe su semilla y la semilla `borde`,
comun a todas las variantes de un bloque: el ruido y la piedra que cruza el borde de cada hilada salen de `borde`, asi
todas casan entre si y el juego puede elegir una al azar en cada sitio.

Lo que hay:
- ruido(): ruido de valor por octavas que se repite (sobre un toro).
- sillares(): hiladas de sillares con su llaga (junta), bisel, desconchones, picado, grietas y manchas.
- ladrillos(): hiladas de ladrillos.
- cantos(): piedras redondeadas (adoquin, mamposteria) con un Voronoi sobre el toro.
- losas(): losas grandes irregulares (suelo).
- marmol(): placas pulidas con vetas.
- columna_lado() / columna_tapa(): fuste estriado y su seccion.
- tallado(): arqueria ciega gotica en relieve (arcos apuntados con un trebol) entre dos molduras.
- musgo(), escarcha(), verdin(): mascaras de lo que cubre la piedra; arriba_de() / abajo_de(): distancia a las juntas.
"""
import math

import numpy as np
from PIL import Image

N = 64
BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]], dtype=float) / 16.0 - 0.5


def hexa(c):
    c = c.lstrip("#")
    return np.array([int(c[i:i + 2], 16) for i in (0, 2, 4)], dtype=float)


def bayer(shape):
    return np.tile(BAYER, (shape[0] // 4 + 1, shape[1] // 4 + 1))[:shape[0], :shape[1]]


# ------------------------------------------------------------------ ruido que se repite

def _malla(g, n, f):
    """Muestrea la malla g (f x f, sobre un toro) en n x n pixeles con interpolacion suave."""
    xs = (np.arange(n) + 0.5) * f / n
    i0 = np.floor(xs).astype(int) % f
    i1 = (i0 + 1) % f
    t = xs - np.floor(xs)
    t = t * t * (3 - 2 * t)
    a = g[np.ix_(i0, i0)]
    b = g[np.ix_(i0, i1)]
    c = g[np.ix_(i1, i0)]
    d = g[np.ix_(i1, i1)]
    ty = t[:, None]
    tx = t[None, :]
    return (a * (1 - tx) + b * tx) * (1 - ty) + (c * (1 - tx) + d * tx) * ty


def ruido(semilla, base=4, octavas=4, persistencia=0.5, n=N):
    """Ruido de valor en [0, 1] que se repite en n x n."""
    rng = np.random.default_rng(semilla)
    total = np.zeros((n, n))
    amp, suma = 1.0, 0.0
    for o in range(octavas):
        f = base * (2 ** o)
        f = min(f, n)
        total += amp * _malla(rng.random((f, f)), n, f)
        suma += amp
        amp *= persistencia
    total /= suma
    lo, hi = total.min(), total.max()
    return (total - lo) / (hi - lo + 1e-9)


# ------------------------------------------------------------------ de campo de luz a colores

class Rampa:
    """Una rampa de colores de lo oscuro a lo claro (pixel art): un indice en [0, 1] -> color, con tramado."""

    def __init__(self, colores):
        self.c = np.array([hexa(x) for x in colores])

    def pintar(self, v, tramado=0.7):
        n = len(self.c)
        x = np.clip(v, 0, 1) * (n - 1) + bayer(v.shape) * tramado
        i = np.clip(np.round(x).astype(int), 0, n - 1)
        return self.c[i]


def imagen(rgb, alfa=None):
    a = np.clip(rgb, 0, 255).astype(np.uint8)
    if alfa is None:
        return Image.fromarray(a, "RGB").convert("RGBA")
    al = np.clip(alfa, 0, 255).astype(np.uint8)
    return Image.fromarray(np.dstack([a, al]), "RGBA")


def mezclar(fondo, encima, mascara):
    m = np.clip(mascara, 0, 1)[..., None]
    return fondo * (1 - m) + encima * m


def dura(mascara, tramado=0.8):
    """Una mascara suave a pixeles enteros (si o no), con el borde tramado."""
    return (np.clip(mascara, 0, 1) + bayer(mascara.shape) * tramado) > 0.5


# ------------------------------------------------------------------ luz

def _rol(a, dy, dx):
    return np.roll(np.roll(a, dy, axis=0), dx, axis=1)


def relieve(h):
    """Luz de un relieve a escalones (pixel art nitido): lo que sobresale tiene el filo de arriba y de la izquierda
    claro y el de abajo y de la derecha oscuro; lo hundido, la sombra del borde de arriba y de la izquierda."""
    arr, izq = _rol(h, 1, 0), _rol(h, 0, 1)
    aba, der = _rol(h, -1, 0), _rol(h, 0, -1)
    claro = np.maximum(0, h - arr) + np.maximum(0, h - izq)
    sombra = np.maximum(0, arr - h) + np.maximum(0, izq - h)
    bajo = np.maximum(0, h - aba) + np.maximum(0, h - der)
    return 0.9 * claro - 1.0 * sombra - 0.45 * bajo


def luz_de(h):
    """Luz de un relieve suave (gradiente; luz de arriba a la izquierda): >0 lo que mira a la luz."""
    gx = (_rol(h, 0, -1) - _rol(h, 0, 1)) / 2
    gy = (_rol(h, -1, 0) - _rol(h, 1, 0)) / 2
    return gx + gy


# ------------------------------------------------------------------ piezas comunes

class Lienzo:
    """El campo de luz y lo que hace falta para pintar piedras: cada pixel sabe a que piedra pertenece (o si es junta)."""

    def __init__(self, n=N):
        self.n = n
        self.v = np.zeros((n, n))
        self.piedra = -np.ones((n, n), dtype=int)  # -1 junta
        self.borde = np.zeros((n, n))  # distancia al borde de su piedra (para el bisel)


def _rect_toro(n, x0, y0, w, h):
    ys = [(y0 + j) % n for j in range(h)]
    xs = [(x0 + i) % n for i in range(w)]
    return ys, xs


def arriba_de(L):
    """Por cada pixel, cuantos hay hasta la junta de encima (0 = el primero bajo la junta); en la junta, -1."""
    n = L.n
    d = np.zeros((n, n), dtype=int)
    for x in range(n):
        c = 99
        for y in list(range(n)) * 2:
            if L.piedra[y, x] < 0:
                c = -1
            else:
                c = min(c + 1, 99)
            d[y, x] = c
    return d


def abajo_de(L):
    """Por cada pixel, cuantos hay hasta la junta de debajo (0 = el ultimo sobre la junta); en la junta, -1."""
    n = L.n
    d = np.zeros((n, n), dtype=int)
    for x in range(n):
        c = 99
        for y in list(range(n - 1, -1, -1)) * 2:
            if L.piedra[y, x] < 0:
                c = -1
            else:
                c = min(c + 1, 99)
            d[y, x] = c
    return d


def grieta(L, rng, x, y, pw, ph, largo=None, ramas=True, cruza=False, hondo=0.09):
    """Una grieta: un trazo oscuro de un pixel, casi recto, con el labio de abajo a la derecha que coge la luz y a
    veces una rama. `cruza`: sigue aunque salga de su piedra (las de los sillares agrietados)."""
    n = L.n
    cx, cy = rng.uniform(1.5, max(2, pw - 1.5)), rng.uniform(1.5, max(2, ph - 1.5))
    ang = rng.uniform(0, 2 * math.pi)
    largo = largo or int(rng.integers(max(6, int(min(pw, ph) * 0.6)), max(8, int(max(pw, ph) * 0.8)) + 1))
    puntos = []
    for _ in range(largo):
        if not cruza and not (0.5 <= cx < pw - 0.5 and 0.5 <= cy < ph - 0.5):
            break
        p = (int(math.floor(x + cx)) % n, int(math.floor(y + cy)) % n)
        if not puntos or puntos[-1] != p:
            puntos.append(p)
        ang += rng.normal(0, 0.28)
        cx += math.cos(ang) * 0.9
        cy += math.sin(ang) * 0.9
    hechos = set(puntos)
    for (xx, yy) in puntos:
        if L.piedra[yy, xx] < 0 and not cruza:
            continue
        L.v[yy, xx] = min(L.v[yy, xx], hondo + 0.04 * rng.random())
    for (xx, yy) in puntos:
        for (ax, ay) in ((1, 1), (1, 0)):
            q = ((xx + ax) % n, (yy + ay) % n)
            if q not in hechos and L.piedra[q[1], q[0]] >= 0:
                L.v[q[1], q[0]] += 0.05
                break
    if ramas and len(puntos) > 9 and rng.random() < 0.55:
        bx, by = puntos[int(rng.integers(len(puntos) // 3, len(puntos)))]
        rx, ry = (bx - x) % n, (by - y) % n
        sub = np.random.default_rng(int(rng.integers(0, 1 << 30)))
        grieta_desde(L, sub, x, y, pw, ph, rx, ry, max(4, len(puntos) // 2), cruza, hondo + 0.03)
    return puntos


def grieta_desde(L, rng, x, y, pw, ph, cx, cy, largo, cruza, hondo):
    n = L.n
    ang = rng.uniform(0, 2 * math.pi)
    for _ in range(largo):
        if not cruza and not (0.5 <= cx < pw - 0.5 and 0.5 <= cy < ph - 0.5):
            break
        xx, yy = int(math.floor(x + cx)) % n, int(math.floor(y + cy)) % n
        if L.piedra[yy, xx] >= 0 or cruza:
            L.v[yy, xx] = min(L.v[yy, xx], hondo)
        ang += rng.normal(0, 0.3)
        cx += math.cos(ang) * 0.9
        cy += math.sin(ang) * 0.9


def _piedra(L, r, k, x, y, pw, ph, tono, fino, grueso, bisel, mancha, granos, picado, desconchones, grietas):
    """Pinta una piedra (rectangulo sobre el toro) con todo lo suyo; r es el azar de ESA piedra."""
    n = L.n
    ys, xs = _rect_toro(n, x, y, pw, ph)
    for j, yy in enumerate(ys):
        for i, xx in enumerate(xs):
            L.piedra[yy, xx] = k
            L.borde[yy, xx] = min(i, j, pw - 1 - i, ph - 1 - j)
            v = tono + 0.10 * (grueso[yy, xx] - 0.5) + 0.12 * (fino[yy, xx] - 0.5)
            if mancha:
                v -= 0.07 * (j / max(1, ph - 1)) ** 1.5
            if j == 0 or i == 0:
                v += 0.16 * bisel
            elif j == 1 or i == 1:
                v += 0.05 * bisel
            if j == ph - 1 or i == pw - 1:
                v -= 0.20 * bisel
            elif j == ph - 2 or i == pw - 2:
                v -= 0.07 * bisel
            L.v[yy, xx] = v
    # los filos gastados: pixeles sueltos del borde que se han ido
    for _ in range(int(r.integers(0, 3 + (pw + ph) // 12))):
        lado = int(r.integers(0, 4))
        if lado < 2:
            i = int(r.integers(1, pw - 1))
            j = 0 if lado == 0 else ph - 1
        else:
            j = int(r.integers(1, ph - 1))
            i = 0 if lado == 2 else pw - 1
        xx, yy = (x + i) % n, (y + j) % n
        L.v[yy, xx] = 0.14 + 0.05 * r.random()
        L.piedra[yy, xx] = -1
    # desconchones en las esquinas
    for (cx, cy, sx, sy) in ((0, 0, 1, 1), (pw - 1, 0, -1, 1), (0, ph - 1, 1, -1), (pw - 1, ph - 1, -1, -1)):
        if r.random() < desconchones:
            rr = int(r.integers(1, 4))
            for a in range(rr):
                for b in range(rr - a):
                    xx, yy = (x + cx + sx * a) % n, (y + cy + sy * b) % n
                    L.v[yy, xx] = 0.12 + 0.05 * r.random()
                    L.piedra[yy, xx] = -1
    # picado: poros oscuros con su labio claro
    for _ in range(int(picado * pw * ph)):
        i, j = int(r.integers(1, pw - 2)), int(r.integers(1, ph - 2))
        xx, yy = (x + i) % n, (y + j) % n
        L.v[yy, xx] -= 0.13
        L.v[(yy + 1) % n, (xx + 1) % n] += 0.04
    if granos > 0:
        for _ in range(int(granos * pw * ph)):
            i, j = int(r.integers(1, pw - 1)), int(r.integers(1, ph - 1))
            xx, yy = (x + i) % n, (y + j) % n
            L.v[yy, xx] += 0.12 if r.random() < 0.5 else -0.12
    if r.random() < grietas:
        grieta(L, r, x, y, pw, ph)


def sillares(semilla, hiladas=(16, 16, 16, 16), anchos=(18, 40), junta=1, bisel=1.0, desconchones=0.5, grietas=0.25,
             manchas=0.6, granos=0.0, picado=0.006, varia=0.07, base=0.56, borde=None, n=N):
    """Hiladas de sillares (cada una desplazada). Con `borde`, la piedra que cruza el borde de cada hilada y el ruido
    salen de esa semilla (iguales en todas las variantes); lo de dentro, de `semilla`."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 13)
    L = Lienzo(n)
    fino = ruido(comun + 1, base=16, octavas=2)
    grueso = ruido(comun + 2, base=4, octavas=3)
    L.v[:] = 0.10 + 0.08 * fino  # la junta
    y = 0
    k = 0
    for h in hiladas:
        w0 = int(rb.integers(anchos[0], anchos[1] + 1))
        a0 = -int(rb.integers(1, w0))
        piedras = [(a0, w0, base + rb.normal(0, varia), np.random.default_rng(int(rb.integers(0, 1 << 30))))]
        x = a0 + w0
        fin = a0 + n
        while x < fin:
            w = int(rng.integers(anchos[0], anchos[1] + 1))
            if fin - x - w < anchos[0]:
                w = fin - x
            piedras.append((x, w, base + rng.normal(0, varia), np.random.default_rng(int(rng.integers(0, 1 << 30)))))
            x += w
        for (x, w, tono, r) in piedras:
            _piedra(L, r, k, x % n, y, w - junta, h - junta, tono, fino, grueso, bisel, r.random() < manchas, granos,
                    picado, desconchones, grietas)
            k += 1
        y += h
    return L


def ladrillos(semilla, alto=8, anchos=(15, 18), junta=1, varia=0.09, base=0.55, quemados=0.12, borde=None, n=N):
    """Hiladas de ladrillos, cada una medio desplazada; los quemados mas oscuros (el que cruza el borde, de `borde`)."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 17)
    L = Lienzo(n)
    fino = ruido(comun + 1, base=16, octavas=2)
    L.v[:] = 0.70 + 0.14 * (fino - 0.5)  # la junta de cal, clara
    y, k, fila = 0, 0, 0
    while y < n:
        w0 = int(rb.integers(anchos[0], anchos[1] + 1))
        a0 = -((fila % 2) * 8 + int(rb.integers(1, 5)))
        lista = [(a0, w0, rb)]
        x = a0 + w0
        fin = a0 + n
        while x < fin:
            w = int(rng.integers(anchos[0], anchos[1] + 1))
            if fin - x - w < anchos[0]:
                w = fin - x
            lista.append((x, w, rng))
            x += w
        for (x, w, r) in lista:
            pw, ph = w - junta, alto - junta
            ys, xs = _rect_toro(n, x % n, y, pw, ph)
            tono = base + r.normal(0, varia) - (0.18 if r.random() < quemados else 0)
            for j, yy in enumerate(ys):
                for i, xx in enumerate(xs):
                    L.piedra[yy, xx] = k
                    v = tono + 0.14 * (fino[yy, xx] - 0.5)
                    if j == 0:
                        v += 0.10
                    if j == ph - 1 or i == pw - 1:
                        v -= 0.14
                    L.v[yy, xx] = v
            if r.random() < 0.35:
                xx = (x + (pw - 1 if r.random() < 0.5 else 0)) % n
                yy = (y + (ph - 1 if r.random() < 0.5 else 0)) % n
                L.v[yy, xx] = 0.66
                L.piedra[yy, xx] = -1
            for _ in range(int(r.integers(0, 3))):
                xx, yy = (x + int(r.integers(1, pw - 1))) % n, (y + int(r.integers(1, ph - 1))) % n
                L.v[yy, xx] -= 0.12
            k += 1
        y += alto
        fila += 1
    return L


def _voronoi(semillas, n):
    """Distancias (sobre el toro) a la semilla mas cercana y a la segunda, y el indice de la mas cercana."""
    ys, xs = np.mgrid[0:n, 0:n]
    px, py = xs + 0.5, ys + 0.5
    d1 = np.full((n, n), 1e9)
    d2 = np.full((n, n), 1e9)
    idx = np.zeros((n, n), dtype=int)
    for k, (sx, sy) in enumerate(semillas):
        dx = np.abs(px - sx)
        dx = np.minimum(dx, n - dx)
        dy = np.abs(py - sy)
        dy = np.minimum(dy, n - dy)
        d = np.sqrt(dx * dx + dy * dy)
        menor = d < d1
        d2 = np.where(menor, d1, np.minimum(d2, d))
        idx = np.where(menor, k, idx)
        d1 = np.where(menor, d, d1)
    return d1, d2, idx


def cantos(semilla, cuantos=22, junta=1.0, redondez=4.0, varia=0.08, base=0.56, relajar=2, borde=None, margen=11, n=N):
    """Piedras redondeadas (Voronoi): la junta donde las dos semillas mas cercanas estan casi igual de lejos; cada piedra
    sombreada como un abultamiento con la luz arriba a la izquierda. Con `borde`, las semillas cerca de los bordes
    salen de esa semilla (las variantes casan)."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 19)
    todos = rb.uniform(0, n, (cuantos, 2))
    for _ in range(relajar):
        _, _, idx = _voronoi(todos, n)
        ys, xs = np.mgrid[0:n, 0:n]
        for k in range(cuantos):
            m = idx == k
            if m.sum() > 0:
                ax = np.angle(np.mean(np.exp(1j * 2 * np.pi * (xs[m] + 0.5) / n))) * n / (2 * np.pi) % n
                ay = np.angle(np.mean(np.exp(1j * 2 * np.pi * (ys[m] + 0.5) / n))) * n / (2 * np.pi) % n
                todos[k] = (ax, ay)
    pts = []
    for (sx, sy) in todos:
        cerca = min(sx, n - sx, sy, n - sy) < margen
        if cerca or borde is None:
            pts.append((sx, sy))
    # dentro: otras semillas para cada variante (las mismas que quitaria, movidas)
    dentro = [p for p in todos if min(p[0], n - p[0], p[1], n - p[1]) >= margen]
    for (sx, sy) in dentro if borde is not None else []:
        nx, ny = sx, sy
        for _ in range(8):
            cx, cy = sx + rng.normal(0, 4), sy + rng.normal(0, 4)
            if not (margen <= cx <= n - margen and margen <= cy <= n - margen):
                continue
            if all(min(abs(cx - ox), n - abs(cx - ox)) ** 2 + min(abs(cy - oy), n - abs(cy - oy)) ** 2 > 36 for (ox, oy) in pts):
                nx, ny = cx, cy
                break
        pts.append((float(nx), float(ny)))
    pts = np.array(pts)
    d1, d2, idx = _voronoi(pts, n)
    bord = (d2 - d1) / 2.0
    L = Lienzo(n)
    fino = ruido(comun + 1, base=16, octavas=2)
    h = np.clip(bord / redondez, 0, 1) ** 0.7
    luz = np.clip(luz_de(h) * 2.6, -1, 1)
    tonos = base + rng.normal(0, varia, len(pts))
    v = tonos[idx] + 0.18 * luz + 0.10 * (fino - 0.5) + 0.06 * (h - 0.5)
    junta_m = bord < junta
    L.v = np.where(junta_m, 0.10 + 0.10 * fino, v)
    L.piedra = np.where(junta_m, -1, idx)
    L.borde = bord
    return L


def losas(semilla, varia=0.06, base=0.55, borde=None, n=N):
    """Losas grandes: rectangulos irregulares con juntas finas, desgaste y alguna grieta (suelo). Las filas y la losa que
    cruza el borde de cada una salen de `borde`."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 23)
    L = Lienzo(n)
    fino = ruido(comun + 1, base=16, octavas=2)
    grueso = ruido(comun + 2, base=3, octavas=3)
    L.v[:] = 0.12 + 0.06 * fino
    k = 0
    y = 0
    for h in (24, 18, 22):
        w0 = int(rb.integers(16, 30))
        a0 = -int(rb.integers(2, w0 - 2))
        bordes = [a0, a0 + w0]
        x = a0 + w0
        fin = a0 + n
        while fin - x > 0:
            w = int(rng.integers(14, 30))
            if fin - x - w < 12:
                w = fin - x
            x += w
            bordes.append(x)
        for a, b in zip(bordes[:-1], bordes[1:]):
            r = rb if a == a0 else rng
            pw, ph = b - a - 1, h - 1
            ys, xs = _rect_toro(n, a % n, y, pw, ph)
            tono = base + r.normal(0, varia)
            for j, yy in enumerate(ys):
                for i, xx in enumerate(xs):
                    L.piedra[yy, xx] = k
                    v = tono + 0.12 * (grueso[yy, xx] - 0.5) + 0.08 * (fino[yy, xx] - 0.5)
                    if j == 0 or i == 0:
                        v += 0.07
                    if j == ph - 1 or i == pw - 1:
                        v -= 0.10
                    L.v[yy, xx] = v
            for _ in range(int(0.004 * pw * ph)):
                xx, yy = (a + int(r.integers(1, pw - 1))) % n, (y + int(r.integers(1, ph - 1))) % n
                L.v[yy, xx] -= 0.12
            if r.random() < 0.35:
                grieta(L, r, a % n, y, pw, ph)
            k += 1
        y += h
    return L


def _distancia_a_cero(s):
    """Lo lejos (en pixeles) de la linea donde s cruza el cero: para vetas de grueso parejo."""
    gx = (_rol(s, 0, -1) - _rol(s, 0, 1)) / 2
    gy = (_rol(s, -1, 0) - _rol(s, 1, 0)) / 2
    return np.abs(s) / (np.sqrt(gx * gx + gy * gy) + 1e-6)


def marmol(semilla, vetas=1, base=0.62, placas=True, n=N):
    """Placas pulidas con vetas largas y sinuosas (dominio desplazado por ruido); las juntas de las placas en los bordes y
    en el medio (asi cada variante es independiente)."""
    L = Lienzo(n)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    w1 = ruido(semilla + 1, base=2, octavas=4)
    w2 = ruido(semilla + 2, base=3, octavas=4)
    nube = ruido(semilla + 4, base=3, octavas=4)
    fino = ruido(semilla + 3, base=16, octavas=2)
    rng = np.random.default_rng(semilla)
    sx = 1 if rng.random() < 0.5 else -1
    fase = 2 * np.pi * (vetas * xs / n + sx * ys / n) + 4.5 * (w1 - 0.5) + 2.5 * (w2 - 0.5)
    d1 = _distancia_a_cero(np.sin(fase))
    fase2 = 2 * np.pi * (2 * xs / n - sx * ys / n) + 3.0 * (w2 - 0.5) + 1.5 * (nube - 0.5)
    d2 = _distancia_a_cero(np.sin(fase2))
    vena = (d1 < 0.52).astype(float)
    vena = np.maximum(vena, ((d1 < 1.1) & (w2 > 0.70)).astype(float))
    L.vena2 = ((d2 < 0.45) & (nube > 0.55) & (vena < 0.5)).astype(float)
    L.v = base + 0.10 * (nube - 0.5) + 0.04 * (fino - 0.5) - 0.05 * (d1 < 2.5) - 0.03 * (d2 < 1.5)
    L.vena = vena
    L.piedra[:] = 0
    if placas:
        for yy in (0, n // 2):
            L.v[yy, :] = 0.16
            L.piedra[yy, :] = -1
            L.v[(yy + 1) % n, :] += 0.05
        L.v[:, 0] = 0.16
        L.piedra[:, 0] = -1
        L.v[:, 1] += 0.05
        L.vena[L.piedra < 0] = 0
        L.vena2[L.piedra < 0] = 0
    return L


def columna_lado(semilla, estrias=4, base=0.58, n=N):
    """Fuste estriado: cada estria una media cana (la pared de la izquierda en sombra, la de la derecha a la luz; lo
    hondo mas oscuro), filetes lisos entre ellas, la junta del tambor arriba y chorretones de lluvia."""
    L = Lienzo(n)
    fino = ruido(semilla + 1, base=16, octavas=2)
    grueso = ruido(semilla + 2, base=4, octavas=3)
    ancho = n / estrias
    xs = np.arange(n) + 0.5
    f = (xs % ancho) / ancho
    filete = 2.5 / ancho
    t = np.clip((f - filete) / (1 - filete), 0, 1)
    perfil = np.where(f < filete, 1.0, 1.0 - np.sin(np.pi * t))
    d = (np.roll(perfil, -1) - np.roll(perfil, 1)) / 2
    fila = base + np.clip(d * 3.2, -1, 1) * 0.24 - 0.10 * (1 - perfil)
    # el filo de la estria: una linea oscura donde empieza y una clara donde acaba
    for x in range(n):
        if f[x] >= filete and f[x - 1] < filete if x > 0 else f[x] >= filete and f[n - 1] < filete:
            fila[x] -= 0.10
    L.v = np.tile(fila, (n, 1)) + 0.09 * (grueso - 0.5) + 0.07 * (fino - 0.5)
    # chorretones: bajan desde arriba, mas oscuros
    rng = np.random.default_rng(semilla + 3)
    for _ in range(5):
        x = int(rng.integers(0, n))
        largo = int(rng.integers(10, 40))
        for j in range(largo):
            L.v[j, x] -= 0.05 * (1 - j / largo)
    L.v[0, :] = base - 0.22
    L.v[1, :] += 0.07
    L.piedra[:] = 0
    L.piedra[0, :] = -1
    return L


def columna_tapa(semilla, base=0.58, n=N):
    """La seccion de la columna vista desde arriba: un disco con anillos y una flor tallada (el resto, la misma piedra)."""
    L = Lienzo(n)
    fino = ruido(semilla + 1, base=16, octavas=2)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    r = np.sqrt((xs - n / 2) ** 2 + (ys - n / 2) ** 2)
    a = np.arctan2(ys - n / 2, xs - n / 2)
    v = np.full((n, n), base) + 0.08 * (fino - 0.5)
    v = np.where(r > 29, v - 0.06, v)
    v = np.where((r > 27) & (r <= 29), base - 0.18, v)
    v = np.where((r > 22) & (r <= 23.5), base - 0.14, v)
    v = np.where((r > 23.5) & (r <= 25), base + 0.10, v)
    petalos = (np.cos(a * 8) + 1) / 2
    flor = (r < 14) & (r > 3 + 6 * (1 - petalos))
    v = np.where(flor, base + 0.08 + 0.10 * np.cos(a * 8 - 0.8), v)
    v = np.where(r <= 3.2, base - 0.10, v)
    v = np.where((r > 13.5) & (r <= 15), base - 0.16, v)
    v[0, :] += 0.12
    v[:, 0] += 0.12
    v[n - 1, :] -= 0.16
    v[:, n - 1] -= 0.16
    L.v = v
    L.piedra[:] = 0
    return L


def tallado(semilla, base=0.58, n=N):
    """Arqueria ciega gotica: dos arcos apuntados por bloque (hondos, con un trebol en lo alto), columnillas entre ellos
    y molduras arriba y abajo. Se pinta como un relieve (`relieve`) mas el ruido de la piedra."""
    fino = ruido(semilla + 1, base=16, octavas=2)
    grueso = ruido(semilla + 2, base=4, octavas=3)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    h = np.full((n, n), 0.5)
    arriba = [1.0, 1.0, 0.8, 0.62, 0.58, 0.66, 0.9, 0.9]
    abajo = [0.9, 0.9, 0.72, 0.78, 1.0, 1.0, 0.86, 0.86]
    for j in range(8):
        h[j, :] = arriba[j]
        h[n - 8 + j, :] = abajo[j]
    banda = (ys > 8) & (ys < n - 8)
    for c in (16, 48):
        dx = xs - c
        hw, e, ys0 = 10.5, 5.0, 30.0
        R = hw + e
        izq = np.sqrt((dx + e) ** 2 + (ys - ys0) ** 2)
        der = np.sqrt((dx - e) ** 2 + (ys - ys0) ** 2)
        cabeza = (ys < ys0) & (izq < R) & (der < R)
        cuerpo = (ys >= ys0) & (np.abs(dx) < hw) & (ys < n - 10)
        hueco = cabeza | cuerpo
        # el marco: dos pixeles alrededor del hueco
        izq2 = np.sqrt((dx + e) ** 2 + (ys - ys0) ** 2)
        marco = (((ys < ys0) & (izq2 < R + 2.2) & (np.sqrt((dx - e) ** 2 + (ys - ys0) ** 2) < R + 2.2))
                 | ((ys >= ys0) & (np.abs(dx) < hw + 2.2) & (ys < n - 9))) & ~hueco & banda
        h = np.where(marco, 0.82, h)
        h = np.where(hueco & banda, 0.16, h)
        # el trebol en lo alto del hueco
        for (ox, oy) in ((0, 19.5), (-3.4, 24.0), (3.4, 24.0)):
            rr = np.sqrt((dx - ox) ** 2 + (ys - oy) ** 2)
            h = np.where(hueco & (rr > 1.6) & (rr < 3.1), 0.62, h)
        # el alfeizar
        h = np.where((np.abs(dx) < hw + 2.2) & (ys >= n - 10) & (ys < n - 8), 0.92, h)
    # columnillas entre los arcos (en x 0 y 32): fuste redondo
    for c in (0, 32, 64):
        dx = xs - c
        col = (np.abs(dx) < 2.6) & banda
        h = np.where(col, 0.86 + 0.1 * np.cos(np.clip(dx / 2.6, -1, 1) * np.pi / 2), h)
        cap = (np.abs(dx) < 3.6) & (ys >= ys0 - 2) & (ys < ys0) & banda
        h = np.where(cap, 0.95, h)
    L = Lienzo(n)
    L.v = base + 0.22 * relieve(h) + 0.14 * (h - 0.5) + 0.08 * (grueso - 0.5) + 0.07 * (fino - 0.5)
    L.piedra[:] = 0
    # juntas: arriba del todo, sobre y bajo la banda; en la banda, en medio de cada arco (dentro del hueco no se ve)
    for yy in (0, 8, n - 8):
        L.v[yy, :] = np.minimum(L.v[yy, :], 0.14)
        L.piedra[yy, :] = -1
    for xx in (16, 48):
        L.v[9:n - 8, xx] -= 0.06
    rng = np.random.default_rng(semilla + 5)
    for _ in range(40):
        x, y = int(rng.integers(0, n)), int(rng.integers(0, n))
        L.v[y, x] -= 0.1
    return L


# ------------------------------------------------------------------ capas de encima

def musgo(L, semilla, cuanto=0.5, borde=None):
    """Mascara del musgo (0-1): manchas grandes (de `borde`, asi casan las variantes) que prefieren lo alto de cada
    piedra y las juntas."""
    comun = semilla if borde is None else borde
    gran = ruido(comun + 7, base=4, octavas=4)
    fino = ruido(semilla + 8, base=16, octavas=2)
    arr = arriba_de(L)
    junta = (L.piedra < 0).astype(float)
    cerca = np.clip(1.0 - np.where(arr < 0, 0, arr) / 5.0, 0, 1)
    m = 0.62 * gran + 0.18 * fino + 0.30 * cerca + 0.12 * junta
    umbral = 1.02 - 0.5 * cuanto
    return np.clip((m - umbral) * 7 + 0.5, 0, 1)


def escarcha(L, semilla, cuanto=0.5, borde=None):
    """La escarcha (0-1): en lo alto de cada piedra (de uno a cuatro pixeles, segun el ruido), en las juntas de debajo de
    cada hilada y cristalitos sueltos."""
    comun = semilla if borde is None else borde
    rng = np.random.default_rng(semilla + 11)
    arr = arriba_de(L)
    grosor = 1 + np.floor(ruido(comun + 9, base=8, octavas=2) * 3.6 * (0.6 + cuanto * 0.8))
    m = ((arr >= 0) & (arr < grosor)).astype(float)
    junta = L.piedra < 0
    debajo_piedra = np.roll(L.piedra, -1, axis=0) >= 0
    m = np.maximum(m, (junta & debajo_piedra).astype(float))
    for _ in range(int(cuanto * 70)):
        x, y = int(rng.integers(0, L.n)), int(rng.integers(0, L.n))
        m[y, x] = 1.0
    return m


def verdin(L, semilla, cuanto=0.5):
    """Verdin: chorretones que bajan de lo alto de cada piedra (agua que escurre) y manchas."""
    rng = np.random.default_rng(semilla + 13)
    m = np.zeros((L.n, L.n))
    for _ in range(int(14 * cuanto) + 4):
        x = int(rng.integers(0, L.n))
        y = int(rng.integers(0, L.n))
        largo = int(rng.integers(6, 26))
        ancho = 1 + int(rng.random() < 0.4)
        for j in range(largo):
            yy = (y + j) % L.n
            a = 1.0 - j / largo
            for i in range(ancho):
                m[yy, (x + i) % L.n] = max(m[yy, (x + i) % L.n], a)
            if rng.random() < 0.15:
                x += 1 if rng.random() < 0.5 else -1
    r = ruido(semilla + 14, base=5, octavas=3)
    m = np.maximum(m, np.clip((r - 0.72) * 4, 0, 1))
    return np.clip(m, 0, 1)
