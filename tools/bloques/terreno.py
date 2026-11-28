"""El pintor del terreno (parte 8d): suelos, hierba, roca natural, arena, nieve, hielo y plantas, a 64 x 64.

En el terreno hay muchisimos bloques iguales seguidos: aqui no hay formas grandes dentro de un bloque (se notaria la
cuadricula); todo es detalle de 4-16 pixeles, y cada variante cambia entero. Lo que cruza el borde (el fleco de la
hierba del lado, las caras de la roca) sale de la semilla comun `borde` para que case con el bloque de al lado.

- tierra(), tierra_seca(), barro(), turba(), ceniza(), grava(), arena(), nieve(), hielo(), musgo_suelo()
- hierba_arriba() y hierba_lado(): la cara de arriba (briznas vistas desde arriba) y la del lado (tierra con el fleco).
- roca(): roca natural en caras (Voronoi con un plano de luz por cara), grietas, estratos, liquen, poros.
- pizarra_natural(): capas finas que se descascarillan.
- plantas (con transparencia): hierba_alta(), helecho(), matorral(), flores(), hongos(), juncos().
"""
import math

import numpy as np

import pintor as P
from pintor import Lienzo, N, ruido


def _lumpy(L, rng, cuantos, fuerza, n=N):
    """Terrones suaves (Voronoi) con su luz de arriba a la izquierda."""
    pts = rng.uniform(0, n, (cuantos, 2))
    d1, d2, idx = P._voronoi(pts, n)
    h = np.clip((d2 - d1) / 4.0, 0, 1)
    L.v = L.v + fuerza * np.clip(P.luz_de(h) * 2.2, -1, 1)


def _piedrecitas(L, rng, cuantas, tono=(0.55, 0.75), radio=(0.8, 2.2), n=N):
    """Piedrecitas sueltas (nunca cruzan el borde): su luz arriba a la izquierda y su sombra abajo a la derecha."""
    for _ in range(cuantas):
        r = rng.uniform(*radio)
        cx, cy = rng.uniform(3, n - 3), rng.uniform(3, n - 3)
        t = rng.uniform(*tono)
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                x, y = int(cx) + dx, int(cy) + dy
                if not (0 <= x < n and 0 <= y < n):
                    continue
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if d < r:
                    luz = -((x + 0.5 - cx) + (y + 0.5 - cy)) / (2 * r)
                    L.v[y, x] = t + 0.14 * luz
                elif d < r + 1.0 and (x + 0.5 - cx) + (y + 0.5 - cy) > 0:
                    L.v[y, x] -= 0.08


def _raices(L, rng, cuantas, n=N):
    for _ in range(cuantas):
        x, y = rng.uniform(4, n - 4), rng.uniform(4, n - 4)
        ang = rng.uniform(0, 2 * math.pi)
        for _ in range(int(rng.integers(6, 16))):
            xi, yi = int(x), int(y)
            if 1 <= xi < n - 1 and 1 <= yi < n - 1:
                L.v[yi, xi] = min(L.v[yi, xi], 0.22)
                L.v[yi + 1, xi] += 0.03
            ang += rng.normal(0, 0.45)
            x += math.cos(ang)
            y += math.sin(ang)


# ------------------------------------------------------------------ suelos

def tierra(semilla, base=0.5, piedras=34, raices=5, n=N):
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    L.v = base + 0.10 * (ruido(semilla + 1, base=16, octavas=2) - 0.5) + 0.08 * (ruido(semilla + 2, base=8, octavas=3) - 0.5)
    _lumpy(L, rng, 34, 0.07)
    _raices(L, rng, raices)
    _piedrecitas(L, rng, piedras)
    return L


def tierra_seca(semilla, base=0.58, borde=None, n=N):
    """Tierra cuarteada: placas con su borde levantado y las grietas oscuras entre ellas (las de cerca del borde, de la
    semilla comun)."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    rb = np.random.default_rng(borde if borde is not None else semilla)
    todos = rb.uniform(0, n, (26, 2))
    pts = np.array([p if min(p[0], n - p[0], p[1], n - p[1]) < 8 else
                    (np.clip(p[0] + rng.normal(0, 3), 8, n - 8), np.clip(p[1] + rng.normal(0, 3), 8, n - 8)) for p in todos])
    d1, d2, idx = P._voronoi(pts, n)
    borde = d2 - d1
    tonos = base + rng.normal(0, 0.04, len(pts))
    h = np.clip(borde / 5.0, 0, 1) ** 0.5
    L.v = tonos[idx] + 0.06 * (ruido(semilla + 1, base=16, octavas=2) - 0.5) + 0.08 * np.clip(P.luz_de(h) * 2, -1, 1)
    grieta = borde < 0.9
    L.v = np.where(grieta, 0.16, L.v)
    L.v = np.where((borde >= 0.9) & (borde < 1.8), L.v + 0.05, L.v)
    _piedrecitas(L, rng, 8, tono=(0.5, 0.7), radio=(0.6, 1.4))
    return L


def barro(semilla, base=0.42, n=N):
    """Barro: oscuro, con charquitos lisos que brillan y marcas."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    L.v = base + 0.10 * (ruido(semilla + 1, base=8, octavas=3) - 0.5) + 0.06 * (ruido(semilla + 2, base=16, octavas=2) - 0.5)
    _lumpy(L, rng, 22, 0.08)
    charco = ruido(semilla + 3, base=8, octavas=2) > 0.80
    L.v = np.where(charco, base - 0.12 + 0.03 * ruido(semilla + 4, base=16, octavas=1), L.v)
    # brillos de humedad en el borde de arriba de cada charco
    brillo = charco & ~np.roll(charco, 1, axis=0) & (np.random.default_rng(semilla + 5).random((n, n)) < 0.5)
    L.v = np.where(brillo, 0.62, L.v)
    for _ in range(40):
        x, y = int(rng.integers(1, n - 1)), int(rng.integers(1, n - 1))
        if not charco[y, x]:
            L.v[y, x] += 0.18
    return L


def turba(semilla, base=0.4, n=N):
    """Turba de pantano: fibras oscuras y raicillas."""
    from madera import ruido_estirado
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    fib = ruido_estirado(semilla + 1, 4, 32, octavas=2)
    L.v = base + 0.10 * (fib - 0.5) + 0.08 * (ruido(semilla + 2, base=16, octavas=2) - 0.5)
    _raices(L, rng, 14)
    _piedrecitas(L, rng, 6, tono=(0.45, 0.6), radio=(0.6, 1.2))
    return L


def ceniza(semilla, base=0.6, n=N):
    """Ceniza: polvo palido, carbonillas y alguna brasa apagada."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    L.v = base + 0.10 * (ruido(semilla + 1, base=16, octavas=2) - 0.5) + 0.06 * (ruido(semilla + 2, base=8, octavas=2) - 0.5)
    _lumpy(L, rng, 28, 0.06)
    for _ in range(70):
        x, y = int(rng.integers(1, n - 2)), int(rng.integers(1, n - 2))
        L.v[y, x] = 0.12
        if rng.random() < 0.4:
            L.v[y, x + 1] = 0.18
    L.brasas = np.zeros((n, n), dtype=bool)
    for _ in range(6):
        x, y = int(rng.integers(2, n - 2)), int(rng.integers(2, n - 2))
        L.brasas[y, x] = True
    return L


def grava(semilla, base=0.5, borde=None, n=N):
    L = P.cantos(semilla, cuantos=70, junta=0.6, redondez=2.2, varia=0.11, base=base, relajar=1, borde=borde, margen=7)
    L.v = np.where(L.piedra < 0, 0.14, L.v)
    return L


def arena(semilla, base=0.6, ondas=True, borde=None, n=N):
    """Arena: grano fino y ondas del viento (de la semilla comun: siguen de un bloque al otro)."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    L.v = base + 0.08 * (ruido(semilla + 1, base=16, octavas=2) - 0.5)
    if ondas:
        w = ruido((borde if borde is not None else semilla) + 2, base=4, octavas=3)
        fase = 2 * np.pi * (1 * xs / n + 4 * ys / n) + 3.0 * (w - 0.5)
        L.v += 0.06 * np.sin(fase) + 0.04 * np.clip(np.cos(fase) * 3, -1, 1) * (np.sin(fase) > 0)
    granos = rng.random((n, n))
    L.v = np.where(granos < 0.05, L.v + 0.12, L.v)
    L.v = np.where(granos > 0.96, L.v - 0.12, L.v)
    return L


def nieve(semilla, base=0.78, n=N):
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    L.v = base + 0.07 * (ruido(semilla + 1, base=8, octavas=3) - 0.5) + 0.04 * (ruido(semilla + 2, base=16, octavas=1) - 0.5)
    _lumpy(L, rng, 20, 0.07)
    for _ in range(36):
        x, y = int(rng.integers(0, n)), int(rng.integers(0, n))
        L.v[y, x] = 1.0
    return L


def hielo(semilla, base=0.55, n=N):
    """Hielo: grietas blancas, burbujas y vetas; devuelve el campo y la transparencia."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    L.v = base + 0.12 * (ruido(semilla + 1, base=4, octavas=3) - 0.5)
    for _ in range(3):
        x, y = rng.uniform(4, n - 4), rng.uniform(4, n - 4)
        ang = rng.uniform(0, 2 * math.pi)
        for _ in range(int(rng.integers(8, 22))):
            xi, yi = int(x), int(y)
            if 0 <= xi < n and 0 <= yi < n:
                L.v[yi, xi] = 0.95
            ang += rng.normal(0, 0.35)
            x += math.cos(ang)
            y += math.sin(ang)
    for _ in range(5):
        cx, cy, r = rng.uniform(3, n - 3), rng.uniform(3, n - 3), rng.uniform(0.7, 1.4)
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                x, y = int(cx) + dx, int(cy) + dy
                if 0 <= x < n and 0 <= y < n and abs(math.hypot(dx, dy) - r) < 0.6:
                    L.v[y, x] = 0.9
    L.alfa = np.clip(170 + 60 * (L.v - base), 150, 235)
    return L


def musgo_suelo(semilla, base=0.45, n=N):
    """Un manto de musgo: bultitos redondos con su luz y fibras."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    L.v = base + 0.08 * (ruido(semilla + 1, base=16, octavas=2) - 0.5)
    pts = rng.uniform(0, n, (90, 2))
    d1, d2, idx = P._voronoi(pts, n)
    h = np.clip(1 - d1 / 4.5, 0, 1)
    L.v = L.v + 0.14 * np.clip(P.luz_de(h) * 3, -1, 1) + 0.06 * h
    L.v = np.where((d2 - d1) < 0.7, L.v - 0.10, L.v)
    for _ in range(60):
        x, y = int(rng.integers(0, n)), int(rng.integers(0, n))
        L.v[y, x] += 0.15
    return L


# ------------------------------------------------------------------ hierba

def hierba_arriba(semilla, base=0.42, briznas=330, n=N):
    """La hierba vista desde arriba: fondo oscuro, matas (donde hay mas briznas y mas claras) y claros entre ellas; cada
    brizna un trazo corto con la punta clara."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    matas = ruido(semilla + 3, base=8, octavas=2)
    L.v = base - 0.06 + 0.12 * (matas - 0.5) + 0.06 * (ruido(semilla + 1, base=16, octavas=2) - 0.5)
    for _ in range(briznas):
        x, y = rng.uniform(1, n - 1), rng.uniform(1, n - 1)
        if rng.random() > 0.25 + 0.9 * matas[int(y), int(x)]:
            continue
        ang = rng.uniform(0, 2 * math.pi)
        largo = int(rng.integers(3, 7))
        tono = rng.uniform(0.45, 0.75) + 0.25 * matas[int(y), int(x)]
        for k in range(largo):
            xi, yi = int(x + math.cos(ang) * k), int(y + math.sin(ang) * k)
            if 0 <= xi < n and 0 <= yi < n:
                L.v[yi, xi] = tono - 0.10 * (largo - 1 - k) / max(1, largo - 1)
    return L


def fleco(borde, n=N):
    """Cuanto baja la hierba por el lado en cada columna (comun a todas las variantes: casa con el bloque de al lado)."""
    rng = np.random.default_rng(borde + 71)
    r = ruido(borde + 70, base=8, octavas=3)[0]
    largo = 6 + (r * 7).astype(int) + rng.integers(-2, 3, n)
    for _ in range(6):
        x = int(rng.integers(0, n))
        largo[x] += int(rng.integers(2, 5))
    return np.clip(largo, 3, 20)


def hierba_lado(semilla, borde, tierra_L, base=0.55, n=N):
    """El lado: la tierra de debajo y, arriba, el fleco de hierba que cuelga (mascara) con su sombra en la tierra."""
    largo = fleco(borde, n)
    rng = np.random.default_rng(semilla + 3)
    G = Lienzo(n)
    G.piedra[:] = 0
    mascara = np.zeros((n, n), dtype=bool)
    g = ruido(semilla + 5, base=16, octavas=2)
    for x in range(n):
        for y in range(largo[x]):
            mascara[y, x] = True
            G.v[y, x] = base + 0.25 - 0.28 * (y / largo[x]) + 0.12 * (g[y, x] - 0.5)
        # sombra de la hierba sobre la tierra
        if largo[x] < n:
            tierra_L.v[largo[x], x] -= 0.14
        if largo[x] + 1 < n:
            tierra_L.v[largo[x] + 1, x] -= 0.06
    # briznas claras sueltas en el fleco
    for _ in range(60):
        x = int(rng.integers(0, n))
        y = int(rng.integers(0, max(1, largo[x] - 1)))
        G.v[y, x] += 0.15
    return G, mascara


# ------------------------------------------------------------------ roca natural

def roca(semilla, borde=None, celdas=26, base=0.5, varia=0.05, grietas=0.35, estratos=0.0, poros=0.0, margen=9, n=N):
    """Roca natural: bultos suaves (Voronoi con la luz de arriba a la izquierda), costuras apenas marcadas y algunas
    grietas de verdad, manchas grandes de color, estratos y poros. Lo de cerca del borde sale de `borde` (las variantes
    casan entre si)."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 41)
    todos = rb.uniform(0, n, (celdas, 2))
    pts = [tuple(p) for p in todos if borde is None or min(p[0], n - p[0], p[1], n - p[1]) < margen]
    if borde is not None:
        for p in todos:
            if min(p[0], n - p[0], p[1], n - p[1]) >= margen:
                pts.append((float(np.clip(p[0] + rng.normal(0, 4), margen, n - margen)),
                            float(np.clip(p[1] + rng.normal(0, 4), margen, n - margen))))
    pts = np.array(pts)
    d1, d2, idx = P._voronoi(pts, n)
    rr = np.random.default_rng(comun + 9)
    tonos = np.array([(rr if min(px, n - px, py, n - py) < margen else rng).normal(0, varia) for (px, py) in pts]) + base
    h = np.clip((d2 - d1) / 7.0, 0, 1) ** 0.6
    L = Lienzo(n)
    L.v = tonos[idx] + 0.10 * np.clip(P.luz_de(h) * 3.0, -1, 1) + 0.04 * (h - 0.5)
    L.v += 0.10 * (ruido(comun + 1, base=4, octavas=3) - 0.5) + 0.07 * (ruido(comun + 2, base=16, octavas=2) - 0.5)
    costura = (d2 - d1) < 0.9
    L.v = np.where(costura, L.v - 0.05, L.v)
    hr = np.random.default_rng(comun + 11)
    cual = hr.random(len(pts)) < grietas
    grieta = costura & cual[idx]
    L.v = np.where(grieta, 0.05, L.v)
    L.v = np.where(np.roll(grieta, 1, axis=0) & ~grieta, L.v + 0.06, L.v)
    if estratos > 0:
        ys = np.mgrid[0:n, 0:n][0] + 0.5
        w = ruido(comun + 3, base=4, octavas=2)
        L.v += estratos * np.sin(2 * np.pi * (ys / n * 8) + 2.5 * (w - 0.5))
    if poros > 0:
        for _ in range(int(poros * 120)):
            x, y = int(rng.integers(1, n - 1)), int(rng.integers(1, n - 1))
            L.v[y, x] = 0.12
            L.v[y + 1, x + 1] += 0.06
    L.piedra = np.where(grieta, -1, idx)
    return L


def pizarra_natural(semilla, borde=None, base=0.48, n=N):
    """Pizarra en capas: bandas finas (3-7 px) onduladas, cada una con su canto de luz y su sombra; algunas sobresalen y
    otras se han descascarillado."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla + 5)
    L = Lienzo(n)
    L.piedra[:] = 0
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    w = ruido(comun + 1, base=2, octavas=3)
    yy = (ys + 5 * (w - 0.5)) % n
    cortes = [0]
    while cortes[-1] < n:
        cortes.append(cortes[-1] + int(rb.integers(3, 8)))
    cortes[-1] = n
    capa = np.searchsorted(np.array(cortes[1:]), yy, side="right")
    altos = np.diff(np.array(cortes))
    inicio = np.array(cortes[:-1])
    tonos = base + rb.normal(0, 0.06, len(altos))
    sale = rb.random(len(altos)) < 0.5
    pos = yy - inicio[capa]
    L.v = tonos[capa] + 0.07 * (ruido(comun + 2, base=16, octavas=2) - 0.5) + np.where(sale[capa], 0.05, -0.04)
    L.v = np.where(pos < 1, L.v + 0.12, L.v)
    L.v = np.where(pos > altos[capa] - 1, L.v - 0.14, L.v)
    # escamas sueltas
    for _ in range(26):
        x, y = int(rng.integers(2, n - 6)), int(rng.integers(2, n - 3))
        largo = int(rng.integers(2, 6))
        L.v[y, x:x + largo] += 0.1
        L.v[y + 1, x:x + largo] -= 0.08
    return L


# ------------------------------------------------------------------ plantas (con transparencia)

def _trazo(img, pts, color, ancho=1):
    """Una linea de pixeles por los puntos dados (ancho 1 o 2)."""
    n = img.shape[0]
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        pasos = int(max(abs(x1 - x0), abs(y1 - y0)) * 2) + 1
        for k in range(pasos + 1):
            t = k / pasos
            x, y = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            for a in range(ancho):
                xi, yi = int(x) + a, int(y)
                if 0 <= xi < n and 0 <= yi < n:
                    img[yi, xi] = color(t, yi)


def hierba_alta(semilla, rampa, briznas=34, alto=(20, 58), n=N):
    """Matas de hierba: briznas que salen de abajo y se curvan; la base oscura y la punta clara. Devuelve rgba."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    orden = sorted(range(briznas), key=lambda _: rng.random())
    for _ in orden:
        x0 = rng.uniform(8, n - 8)
        h = rng.uniform(*alto)
        lean = rng.normal(0, 9)
        curva = rng.normal(0, 6)
        tono = rng.uniform(0.35, 0.95)
        pts = []
        for k in range(9):
            t = k / 8
            pts.append((x0 + lean * t + curva * t * t, n - 1 - h * t))
        col = lambda t, y, tono=tono: np.concatenate([rampa.pintar(np.array([[min(1, 0.15 + tono * (0.4 + 0.6 * t))]]), 0)[0, 0], [255]])
        _trazo(img, pts, col, ancho=2 if rng.random() < 0.3 else 1)
    return img


def helecho(semilla, rampa, frondas=7, n=N):
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    for f in range(frondas):
        ang = math.radians(-90 + rng.uniform(-60, 60))
        largo = rng.uniform(34, 52)
        x0, y0 = n / 2 + rng.uniform(-6, 6), n - 1
        curva = rng.normal(0, 0.25)
        tono = rng.uniform(0.4, 0.85)
        pts = []
        for k in range(12):
            t = k / 11
            a = ang + curva * t
            pts.append((x0 + math.cos(a) * largo * t, y0 + math.sin(a) * largo * t + 10 * t * t))
        col = lambda t, y, tono=tono: np.concatenate([rampa.pintar(np.array([[0.35 + tono * 0.5 * (0.6 + 0.4 * t)]]), 0)[0, 0], [255]])
        _trazo(img, pts, col)
        # foliolos a los dos lados, mas cortos hacia la punta
        for k in range(2, 11):
            t = k / 11
            px, py = pts[k]
            a = ang + curva * t
            for s in (-1, 1):
                l = (1 - t) * 7 + 1.5
                qa = a + s * 1.2
                q = [(px, py), (px + math.cos(qa) * l, py + math.sin(qa) * l + 1.5)]
                c2 = lambda tt, y, tono=tono: np.concatenate([rampa.pintar(np.array([[0.3 + tono * 0.6]]), 0)[0, 0], [255]])
                _trazo(img, q, c2)
    return img


def matorral(semilla, rampa, n=N):
    """Un matorral seco: ramas que se abren desde abajo y se parten."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))

    def rama(x, y, ang, largo, grosor, nivel):
        if nivel > 4 or largo < 3:
            return
        x2, y2 = x + math.cos(ang) * largo, y + math.sin(ang) * largo
        tono = 0.35 + 0.12 * nivel + rng.uniform(-0.05, 0.05)
        col = lambda t, yy, tono=tono: np.concatenate([rampa.pintar(np.array([[tono]]), 0)[0, 0], [255]])
        _trazo(img, [(x, y), ((x + x2) / 2 + rng.normal(0, 1.5), (y + y2) / 2), (x2, y2)], col, ancho=grosor)
        for _ in range(int(rng.integers(2, 4))):
            rama(x2, y2, ang + rng.normal(0, 0.55), largo * rng.uniform(0.5, 0.75), 1, nivel + 1)

    for _ in range(4):
        rama(n / 2 + rng.uniform(-4, 4), n - 1, math.radians(-90 + rng.uniform(-35, 35)), rng.uniform(14, 22), 2, 0)
    return img


def flores(semilla, tallo, petalo, centro, cuantas=6, n=N):
    """Unas flores: tallos con hojas y una corola de cinco petalos de un color, con su centro."""
    rng = np.random.default_rng(semilla)
    img = hierba_alta(semilla + 1, tallo, briznas=12, alto=(10, 26))
    for _ in range(cuantas):
        x0 = rng.uniform(12, n - 12)
        h = rng.uniform(22, 46)
        lean = rng.normal(0, 5)
        pts = [(x0 + lean * t, n - 1 - h * t) for t in np.linspace(0, 1, 6)]
        col = lambda t, y: np.concatenate([tallo.pintar(np.array([[0.35 + 0.3 * t]]), 0)[0, 0], [255]])
        _trazo(img, pts, col)
        cx, cy = pts[-1]
        for k in range(5):
            a = k * 2 * math.pi / 5 + rng.uniform(0, 0.4)
            for r in (1.3, 2.4):
                x, y = int(cx + math.cos(a) * r), int(cy + math.sin(a) * r)
                if 0 <= x < n and 0 <= y < n:
                    v = 0.85 if r < 2 else 0.6
                    img[y, x] = np.concatenate([petalo.pintar(np.array([[v - (0.15 if math.sin(a) > 0 else 0)]]), 0)[0, 0], [255]])
        xi, yi = int(cx), int(cy)
        if 0 <= xi < n and 0 <= yi < n:
            img[yi, xi] = np.concatenate([centro.pintar(np.array([[0.7]]), 0)[0, 0], [255]])
    return img


def hongos(semilla, sombrero, pie, n=N):
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    for k in range(3):
        cx = n / 2 + (k - 1) * rng.uniform(10, 14)
        h = rng.uniform(10, 24) if k != 1 else rng.uniform(22, 32)
        r = rng.uniform(5, 9) if k != 1 else rng.uniform(9, 12)
        base = n - 1
        for y in range(int(base - h), base + 1):
            for x in range(int(cx - 1.5), int(cx + 2)):
                if 0 <= x < n:
                    v = 0.55 + 0.25 * (1 - (x - cx + 1.5) / 3.5)
                    img[y, x] = np.concatenate([pie.pintar(np.array([[v]]), 0)[0, 0], [255]])
        top = base - h
        for y in range(int(top - r * 0.75), int(top) + 2):
            for x in range(int(cx - r), int(cx + r) + 1):
                u = (x + 0.5 - cx) / r
                vy = (top + 1 - (y + 0.5)) / (r * 0.75)
                if 0 <= x < n and 0 <= y < n and u * u + vy * vy <= 1.0 and vy >= -0.15:
                    luz = 0.75 - 0.35 * u - 0.25 * (1 - vy)
                    img[y, x] = np.concatenate([sombrero.pintar(np.array([[np.clip(luz, 0, 1)]]), 0)[0, 0], [255]])
        # motas del sombrero
        for _ in range(4):
            x, y = int(cx + rng.uniform(-r * 0.6, r * 0.6)), int(top - rng.uniform(1, r * 0.5))
            if 0 <= x < n and 0 <= y < n and img[y, x, 3] > 0:
                img[y, x, :3] = np.minimum(img[y, x, :3] + 70, 255)
    return img


def juncos(semilla, tallo, espiga, n=N):
    """Juncos de pantano: tallos rectos casi verticales y algunas espigas pardas."""
    rng = np.random.default_rng(semilla)
    img = hierba_alta(semilla + 1, tallo, briznas=14, alto=(30, 62))
    for _ in range(4):
        x0 = rng.uniform(10, n - 10)
        h = rng.uniform(44, 62)
        lean = rng.normal(0, 3)
        pts = [(x0 + lean * t, n - 1 - h * t) for t in np.linspace(0, 1, 6)]
        col = lambda t, y: np.concatenate([tallo.pintar(np.array([[0.45 + 0.2 * t]]), 0)[0, 0], [255]])
        _trazo(img, pts, col)
        cx, cy = pts[-1]
        for y in range(int(cy) + 2, int(cy) + 10):
            for x in range(int(cx + lean * 0.1) - 1, int(cx + lean * 0.1) + 2):
                if 0 <= x < n and 0 <= y < n:
                    v = 0.5 + 0.25 * (1 if x == int(cx + lean * 0.1) - 1 else 0)
                    img[y, x] = np.concatenate([espiga.pintar(np.array([[v]]), 0)[0, 0], [255]])
    return img
