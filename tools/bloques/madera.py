"""El pintor de la madera, los entramados y los tejados (parte 8c): lo mismo que `pintor.py` (campos de luz de 64 x 64 que se
repiten, luz de arriba a la izquierda, variantes que casan por la semilla `borde`), con piezas de carpinteria.

- ruido_estirado(): ruido que se repite con distinto numero de celdas a lo ancho y a lo alto (vetas).
- tablones(): tablas horizontales con vetas, juntas, testas, nudos y clavos (podridas: agujeros; quemadas: cuarteado).
- viga_lado() / viga_tapa(): madera escuadrada (vetas a lo largo, fendas) y su testa (anillos y grietas).
- entramado(): enlucido con vigas oscuras (borde, cruz, diagonales, poste, travesano); enlucido() solo.
- pizarra(), escamas(), barro(), tejamanil(): tejados en hiladas.
- puerta() / trampilla(): tablas en vertical con herrajes (la puerta en dos mitades; la de arriba con ventanuco).
"""
import math

import numpy as np

import pintor as P
from pintor import Lienzo, N, ruido


def _eje(n, f):
    xs = (np.arange(n) + 0.5) * f / n
    i0 = np.floor(xs).astype(int) % f
    i1 = (i0 + 1) % f
    t = xs - np.floor(xs)
    return i0, i1, t * t * (3 - 2 * t)


def ruido_estirado(semilla, fx, fy, octavas=3, persistencia=0.5, n=N):
    """Ruido de valor en [0, 1] que se repite, con fx celdas a lo ancho y fy a lo alto (fx pequeno y fy grande: vetas
    horizontales; al reves, verticales). fx y fy tienen que dividir a n."""
    rng = np.random.default_rng(semilla)
    total = np.zeros((n, n))
    amp, suma = 1.0, 0.0
    for o in range(octavas):
        gx, gy = min(fx * 2 ** o, n), min(fy * 2 ** o, n)
        g = rng.random((gy, gx))
        y0, y1, ty = _eje(n, gy)
        x0, x1, tx = _eje(n, gx)
        a, b = g[np.ix_(y0, x0)], g[np.ix_(y0, x1)]
        c, d = g[np.ix_(y1, x0)], g[np.ix_(y1, x1)]
        tx, ty = tx[None, :], ty[:, None]
        total += amp * ((a * (1 - tx) + b * tx) * (1 - ty) + (c * (1 - tx) + d * tx) * ty)
        suma += amp
        amp *= persistencia
    total /= suma
    lo, hi = total.min(), total.max()
    return (total - lo) / (hi - lo + 1e-9)


# ------------------------------------------------------------------ tablas

def _nudo(L, cx, cy, r, rng):
    n = L.n
    for dy in range(-3, 4):
        for dx in range(-5, 6):
            d = math.hypot(dx / 1.7, dy)
            xx, yy = (cx + dx) % n, (cy + dy) % n
            if L.piedra[yy, xx] < 0:
                continue
            if d < r * 0.45:
                L.v[yy, xx] = 0.16 + 0.05 * rng.random()
            elif d < r * 0.75:
                L.v[yy, xx] += 0.06
            elif d < r:
                L.v[yy, xx] -= 0.08


def tablones(semilla, ancho=16, largos=(36, 64), varia=0.06, base=0.55, borde=None, nudos=0.45, clavos=True,
             agujeros=0.0, cuarteado=0.0, n=N):
    """Tablas horizontales de `ancho` pixeles: vetas a lo largo, la junta oscura entre tablas, testas (cortes) a lo largo de
    cada fila, nudos y clavos junto a las testas. Lo que cruza el borde de cada fila sale de `borde`. `agujeros`: madera
    podrida (huecos oscuros); `cuarteado`: madera quemada (escamas de carbon)."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 29)
    L = Lienzo(n)
    veta = ruido_estirado(comun + 1, 2, 32, octavas=2)
    veta2 = ruido_estirado(comun + 2, 1, 16, octavas=3)
    fino = ruido(comun + 3, base=16, octavas=2)
    L.v[:] = 0.06
    k = 0
    for fila in range(n // ancho):
        y = fila * ancho
        w0 = int(rb.integers(largos[0], largos[1] + 1))
        a0 = -int(rb.integers(1, w0))
        trozos = [(a0, w0, np.random.default_rng(int(rb.integers(0, 1 << 30))))]
        x = a0 + w0
        fin = a0 + n
        while x < fin:
            w = int(rng.integers(largos[0], largos[1] + 1))
            if fin - x - w < largos[0]:
                w = fin - x
            trozos.append((x, w, np.random.default_rng(int(rng.integers(0, 1 << 30)))))
            x += w
        for (x, w, r) in trozos:
            tono = base + r.normal(0, varia)
            fase = r.uniform(0, 6.3)
            for j in range(ancho - 1):
                yy = (y + j) % n
                for i in range(w - 1):
                    xx = (x + i) % n
                    L.piedra[yy, xx] = k
                    v = tono + 0.13 * (veta[yy, xx] - 0.5) + 0.07 * (veta2[yy, xx] - 0.5) + 0.04 * (fino[yy, xx] - 0.5)
                    v += 0.035 * math.sin((j + 2.5 * veta2[yy, xx]) * 1.7 + fase)
                    if j == 0:
                        v += 0.10
                    elif j == ancho - 2:
                        v -= 0.13
                    if i == 0:
                        v += 0.05
                    elif i == w - 2:
                        v -= 0.09
                    L.v[yy, xx] = v
            if clavos:
                for cx in (x + 2, x + w - 4):
                    for cy in (y + ancho // 4, y + ancho - 2 - ancho // 4):
                        xx, yy = cx % n, cy % n
                        L.v[yy, xx] = 0.12
                        L.v[(yy + 1) % n, (xx + 1) % n] += 0.06
            if r.random() < nudos and w > 14:
                _nudo(L, x + int(r.integers(6, w - 7)), y + ancho // 2 - 1, 3.2 + r.random() * 1.6, r)
            if agujeros > 0:
                for _ in range(int(agujeros * 4 * r.random()) + (1 if r.random() < agujeros else 0)):
                    cx, cy = x + int(r.integers(3, max(4, w - 4))), y + int(r.integers(2, ancho - 3))
                    rad = 1.0 + r.random() * 2.2
                    for dy in range(-3, 4):
                        for dx in range(-4, 5):
                            if (dx / 1.5) ** 2 + dy ** 2 < rad * rad:
                                xx, yy = (cx + dx) % n, (cy + dy) % n
                                if L.piedra[yy, xx] == k:
                                    L.v[yy, xx] = 0.07
                                    L.piedra[yy, xx] = -1
            k += 1
    if cuarteado > 0:
        # escamas de carbon: celdas pequenas con su filo claro arriba (Voronoi)
        pts = rb.uniform(0, n, (int(90 * cuarteado), 2))
        d1, d2, idx = P._voronoi(pts, n)
        filo = (d2 - d1) < 0.9
        dentro = L.piedra >= 0
        L.v = np.where(filo & dentro, L.v - 0.16, L.v)
        h = np.clip((d2 - d1) / 3.0, 0, 1)
        L.v = np.where(dentro & ~filo, L.v + 0.10 * np.clip(P.luz_de(h) * 2.5, -1, 1), L.v)
    return L


def viga_lado(semilla, base=0.55, n=N):
    """La cara de una viga escuadrada: vetas a lo largo (en vertical), fendas, marcas de azuela y las aristas."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    veta = ruido_estirado(semilla + 1, 32, 2, octavas=2)
    veta2 = ruido_estirado(semilla + 2, 16, 1, octavas=3)
    fino = ruido(semilla + 3, base=16, octavas=2)
    L.v = base + 0.14 * (veta - 0.5) + 0.07 * (veta2 - 0.5) + 0.04 * (fino - 0.5)
    L.piedra[:] = 0
    # marcas de azuela: franjas horizontales suaves
    for y in range(0, n, 7):
        L.v[y, :] += 0.04
        L.v[(y + 1) % n, :] -= 0.02
    # fendas (grietas a lo largo de la veta)
    for _ in range(2):
        x = int(rng.integers(8, n - 8))
        y = int(rng.integers(0, n))
        largo = int(rng.integers(20, 44))
        for j in range(largo):
            yy = (y + j) % n
            hondo = 1 - abs(j / largo * 2 - 1)
            L.v[yy, x] -= 0.10 + 0.14 * hondo
            L.v[yy, x + 1] += 0.04
            if rng.random() < 0.08:
                x += 1 if rng.random() < 0.5 else -1
    # las aristas: la de la izquierda a la luz, la de la derecha en sombra
    L.v[:, 0] += 0.14
    L.v[:, 1] += 0.05
    L.v[:, n - 1] -= 0.18
    L.v[:, n - 2] -= 0.06
    return L


def viga_tapa(semilla, base=0.55, n=N):
    """La testa de una viga: anillos alrededor de la medula (algo descentrada), grietas radiales y el bisel del borde."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    cx, cy = n / 2 + rng.uniform(-6, 6), n / 2 + rng.uniform(-6, 6)
    w = ruido(semilla + 1, base=4, octavas=3)
    r = np.sqrt((xs - cx) ** 2 + (ys - cy) ** 2) + 3.0 * (w - 0.5)
    anillos = np.sin(r * 2 * np.pi / 3.4)
    L.v = base + 0.07 * anillos + 0.05 * (ruido(semilla + 2, base=16, octavas=2) - 0.5) - 0.004 * r
    L.piedra[:] = 0
    a = np.arctan2(ys - cy, xs - cx)
    for _ in range(3):
        ang = rng.uniform(-math.pi, math.pi)
        largo = rng.uniform(10, 26)
        grieta = (np.abs(np.angle(np.exp(1j * (a - ang)))) < 0.9 / np.maximum(r, 1)) & (r < largo)
        L.v = np.where(grieta, 0.12, L.v)
    L.v = np.where(r < 1.5, base - 0.12, L.v)
    L.v[0, :] += 0.12
    L.v[:, 0] += 0.12
    L.v[n - 1, :] -= 0.16
    L.v[:, n - 1] -= 0.16
    return L


# ------------------------------------------------------------------ entramado y enlucido

def enlucido(semilla, base=0.62, grietas=0.6, ladrillo=0.3, n=N):
    """Enlucido de cal: manchas, desconchados que dejan ver el ladrillo, grietas finas y suciedad que baja."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    g = ruido(semilla + 1, base=4, octavas=4)
    f = ruido(semilla + 2, base=16, octavas=2)
    L.v = base + 0.10 * (g - 0.5) + 0.05 * (f - 0.5)
    L.piedra[:] = 0
    L.ladrillo = np.zeros((n, n), dtype=bool)
    if rng.random() < ladrillo:
        # un desconchado: se ve el ladrillo de debajo (hiladas de 4 px)
        cx, cy = int(rng.integers(20, 44)), int(rng.integers(20, 44))
        rx, ry = rng.uniform(8, 14), rng.uniform(5, 9)
        ys, xs = np.mgrid[0:n, 0:n]
        borde = ruido(semilla + 3, base=8, octavas=2)
        dentro = ((xs - cx) / rx) ** 2 + ((ys - cy) / ry) ** 2 < 0.7 + 0.6 * borde
        L.ladrillo = dentro
        filo = dentro & ~np.roll(dentro, 1, axis=0)
        L.v = np.where(np.roll(dentro, -1, axis=0) & ~dentro, L.v - 0.10, L.v)
        L.v = np.where(filo, L.v + 0.0, L.v)
    for _ in range(int(grietas * 3)):
        x, y = int(rng.integers(0, n)), int(rng.integers(0, n))
        ang = rng.uniform(0, 2 * math.pi)
        for _ in range(int(rng.integers(6, 18))):
            L.v[y % n, x % n] -= 0.12
            ang += rng.normal(0, 0.5)
            x += int(round(math.cos(ang)))
            y += int(round(math.sin(ang)))
    # suciedad: chorretones desde arriba
    for _ in range(6):
        x = int(rng.integers(0, n))
        largo = int(rng.integers(8, 30))
        for j in range(largo):
            L.v[j, x] -= 0.05 * (1 - j / largo)
    return L


# las figuras del entramado: que vigas lleva cada bloque, ademas del marco
FIGURAS = ("marco", "cruz", "diagonal", "contra", "poste", "travesano")


def entramado(semilla, figura="marco", base_viga=0.45, ladrillo=0.3, n=N):
    """Enlucido con vigas: el marco (4 px en cada borde: con el bloque de al lado hace una viga de 8) y la figura del
    medio. Devuelve (L del enlucido, L de las vigas, mascara de las vigas)."""
    E = enlucido(semilla, ladrillo=ladrillo)
    V = Lienzo(n)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    m = np.zeros((n, n), dtype=bool)
    # cada viga con su direccion (para las vetas): 0 horizontal, 1 vertical, 2 diagonal /, 3 diagonal \
    dirv = np.full((n, n), -1)

    def poner(mask, d):
        nonlocal m
        nuevo = mask & ~m
        dirv[nuevo] = d
        m = m | mask

    t = 4.0
    poner((ys < t) | (ys > n - t), 0)
    poner((xs < t) | (xs > n - t), 1)
    if figura in ("cruz", "diagonal"):
        poner(np.abs((xs - ys) * 0.7071) < 4.2, 3)
    if figura in ("cruz", "contra"):
        poner(np.abs((xs + ys - n) * 0.7071) < 4.2, 2)
    if figura == "poste":
        poner(np.abs(xs - n / 2) < 4, 1)
    if figura == "travesano":
        poner(np.abs(ys - n / 2) < 4, 0)
    vh = ruido_estirado(semilla + 11, 2, 32, octavas=2)
    vv = ruido_estirado(semilla + 12, 32, 2, octavas=2)
    # las diagonales: la veta a lo largo de la diagonal
    u = ((xs + ys) / (2 * n)) % 1.0
    w2 = ruido(semilla + 13, base=8, octavas=2)
    vd1 = 0.5 + 0.5 * np.sin((xs - ys) * 0.9 + 4 * w2)
    vd2 = 0.5 + 0.5 * np.sin((xs + ys) * 0.9 + 4 * w2)
    veta = np.where(dirv == 0, vh, np.where(dirv == 1, vv, np.where(dirv == 3, vd2, vd1)))
    V.v = base_viga + 0.14 * (veta - 0.5) + 0.05 * (ruido(semilla + 14, base=16, octavas=2) - 0.5)
    h = m.astype(float)
    V.v = V.v + 0.20 * np.clip(P.relieve(h), -1, 1)
    V.piedra = np.where(m, 0, -1)
    # el enlucido: la sombra que echan las vigas
    sombra = (~m) & (np.roll(m, 1, axis=0) | np.roll(m, 1, axis=1))
    E.v = np.where(sombra, E.v - 0.12, E.v)
    # clavijas en los cruces del marco con la figura
    for (px, py) in ((n // 2, 2), (n // 2, n - 3), (2, n // 2), (n - 3, n // 2)):
        if m[py, px]:
            V.v[py, px] = 0.12
            V.v[py, (px + 1) % n] = 0.2
    return E, V, m


# ------------------------------------------------------------------ tejados

def pizarra(semilla, alto=8, anchos=(11, 18), base=0.5, escamas=False, borde=None, n=N):
    """Hiladas de pizarras (rectangulares o de escama, con el canto de abajo redondo): cada una tapa la parte de arriba de
    la de debajo, asi se ve la mitad de abajo de cada pieza y su sombra sobre la hilada siguiente."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun)
    rng = np.random.default_rng(semilla * 7919 + 31)
    L = Lienzo(n)
    fino = ruido(comun + 1, base=16, octavas=2)
    grueso = ruido(comun + 2, base=4, octavas=3)
    L.v[:] = 0.08
    k = 0
    for fila in range(n // alto):
        y = fila * alto
        desp = (anchos[0] // 2) * (fila % 2)
        w0 = int(rb.integers(anchos[0], anchos[1] + 1))
        a0 = -int(rb.integers(1, w0)) - desp
        piezas = [(a0, w0, np.random.default_rng(int(rb.integers(0, 1 << 30))))]
        x = a0 + w0
        fin = a0 + n
        while x < fin:
            w = int(rng.integers(anchos[0], anchos[1] + 1))
            if fin - x - w < anchos[0]:
                w = fin - x
            piezas.append((x, w, np.random.default_rng(int(rng.integers(0, 1 << 30)))))
            x += w
        for (x, w, r) in piezas:
            tono = base + r.normal(0, 0.07)
            caida = int(r.integers(0, 2))
            for i in range(w - 1):
                xx = (x + i) % n
                # el canto de abajo: recto (con una muesca de vez en cuando) o redondo (escamas)
                if escamas:
                    u = (i + 0.5) / (w - 1) * 2 - 1
                    abajo = alto - 1 - int(round((1 - math.sqrt(max(0.0, 1 - u * u))) * (alto * 0.55)))
                else:
                    abajo = alto - 1 - caida
                for j in range(abajo):
                    yy = (y + j) % n
                    L.piedra[yy, xx] = k
                    v = tono + 0.08 * (grueso[yy, xx] - 0.5) + 0.08 * (fino[yy, xx] - 0.5) + 0.05 * (j / alto)
                    if i == 0:
                        v += 0.08
                    if i == w - 2:
                        v -= 0.10
                    if j == abajo - 1:
                        v += 0.06
                    L.v[yy, xx] = v
            if r.random() < 0.15 and w > 6:
                # una pizarra rota: le falta una esquina
                i0 = int(r.integers(1, w - 3))
                for i in range(i0, min(w - 1, i0 + 3)):
                    xx = (x + i) % n
                    for j in range(alto - 3, alto - 1):
                        L.v[(y + j) % n, xx] = 0.10
                        L.piedra[(y + j) % n, xx] = -1
            k += 1
    # la sombra de cada hilada sobre la de debajo: las dos primeras filas de cada hilada, mas oscuras
    for fila in range(n // alto):
        y = fila * alto
        L.v[y, :] -= 0.10
        L.v[(y + 1) % n, :] -= 0.05
    return L


def barro(semilla, alto=16, ancho=8, base=0.55, borde=None, n=N):
    """Teja arabe: canales y cobijas alternas (una banda de luz en lo alto de cada cobija y sombra en los canales), en
    hiladas de `alto` con la sombra de la de encima; alguna teja movida o rota."""
    comun = semilla if borde is None else borde
    rng = np.random.default_rng(semilla * 7919 + 37)
    L = Lienzo(n)
    fino = ruido(comun + 1, base=16, octavas=2)
    grueso = ruido(comun + 2, base=4, octavas=3)
    xs = np.arange(n) + 0.5
    col = (xs // ancho).astype(int)
    u = (xs % ancho) / ancho
    cobija = col % 2 == 0
    perfil = np.where(cobija, np.sin(u * np.pi), -np.sin(u * np.pi) * 0.6)
    luz = np.where(cobija, np.cos(u * np.pi), -np.cos(u * np.pi) * 0.6)
    k = 0
    L.piedra[:] = 0
    for fila in range(n // alto):
        y = fila * alto
        tonos = base + rng.normal(0, 0.06, n // ancho + 1)
        for j in range(alto):
            yy = (y + j) % n
            for x in range(n):
                v = tonos[col[x]] + 0.16 * luz[x] * 0.8 + 0.10 * perfil[x] + 0.07 * (grueso[yy, x] - 0.5) + 0.07 * (fino[yy, x] - 0.5)
                v += 0.05 * (j / alto)
                if j < 3:
                    v -= 0.14 - 0.04 * j
                L.v[yy, x] = v
        # el canto de abajo de cada cobija: una linea oscura
        L.v[(y + alto - 1) % n, :] -= 0.06
        for c in range(n // ancho):
            if rng.random() < 0.06:
                x0 = c * ancho
                for x in range(x0 + 2, x0 + ancho - 2):
                    for j in range(alto - 4, alto - 1):
                        L.v[(y + j) % n, x] = 0.22
        k += 1
    L.canal = np.tile((~cobija).astype(float), (n, 1))
    return L


def tejamanil(semilla, base=0.55, borde=None, n=N):
    """Tejas de madera (tejamanil): hiladas de 8 con piezas estrechas de veta vertical, gastadas."""
    L = pizarra(semilla, alto=8, anchos=(8, 14), base=base, borde=borde, n=n)
    veta = ruido_estirado((borde or semilla) + 41, 32, 2, octavas=2)
    L.v = np.where(L.piedra >= 0, L.v + 0.12 * (veta - 0.5), L.v)
    return L


# ------------------------------------------------------------------ puertas y trampillas

def puerta(semilla, base=0.55, ventana=True, n=N):
    """Una puerta de tablas en vertical (8 de ancho) con dos bandas de hierro remachadas por mitad, la aldaba y, arriba,
    un ventanuco con rejas. Devuelve (L de abajo, L de arriba, mascara de hierro de abajo y de arriba, hueco de arriba)."""
    mitades = []
    for mitad in (0, 1):
        rng = np.random.default_rng(semilla + 17 * mitad)
        L = Lienzo(n)
        veta = ruido_estirado(semilla + 1, 32, 2, octavas=2)
        fino = ruido(semilla + 2, base=16, octavas=2)
        xs = np.arange(n)
        tabla = xs // 8
        tonos = base + rng.normal(0, 0.05, n // 8)
        L.v = tonos[tabla][None, :] + 0.12 * (veta - 0.5) + 0.04 * (fino - 0.5)
        L.v[:, (xs % 8) == 7] -= 0.18
        L.v[:, (xs % 8) == 0] += 0.06
        L.piedra[:] = 0
        hierro = np.zeros((n, n), dtype=bool)
        bandas = (8, 48) if mitad == 0 else (12, 52)
        for b in bandas:
            hierro[b:b + 5, 2:n - 2] = True
        hueco = np.zeros((n, n), dtype=bool)
        if mitad == 1 and ventana:
            # el ventanuco: un arco apuntado pequeno con dos barrotes
            ys, xx = np.mgrid[0:n, 0:n] + 0.5
            c = n / 2
            izq = np.sqrt((xx - c + 4) ** 2 + (ys - 30) ** 2)
            der = np.sqrt((xx - c - 4) ** 2 + (ys - 30) ** 2)
            arco = ((ys < 30) & (izq < 12) & (der < 12)) | ((ys >= 30) & (np.abs(xx - c) < 8) & (ys < 44))
            marco = (((ys < 30) & (izq < 14) & (der < 14)) | ((ys >= 30) & (np.abs(xx - c) < 10) & (ys < 46))) & ~arco
            hierro |= marco
            hueco = arco.copy()
            for bx in (c - 3, c + 3):
                barra = (np.abs(xx - bx) < 1) & arco
                hierro |= barra
                hueco &= ~barra
            hierro |= (np.abs(ys - 34) < 1) & arco
            hueco &= ~((np.abs(ys - 34) < 1) & arco)
        if mitad == 0:
            # la aldaba: una argolla
            ys, xx = np.mgrid[0:n, 0:n] + 0.5
            rr = np.sqrt((xx - 46) ** 2 + (ys - 30) ** 2)
            hierro |= (rr > 3.2) & (rr < 5.0)
            hierro |= (np.abs(xx - 46) < 2.2) & (np.abs(ys - 25) < 2.2)
        mitades.append((L, hierro, hueco))
    return mitades


def trampilla(semilla, base=0.55, n=N):
    """Una trampilla: tablas en vertical, un marco de hierro y dos bandas cruzadas con remaches."""
    L = Lienzo(n)
    rng = np.random.default_rng(semilla)
    veta = ruido_estirado(semilla + 1, 32, 2, octavas=2)
    xs = np.arange(n)
    tabla = xs // 8
    tonos = base + rng.normal(0, 0.05, n // 8)
    L.v = tonos[tabla][None, :] + 0.12 * (veta - 0.5)
    L.v[:, (xs % 8) == 7] -= 0.18
    L.piedra[:] = 0
    hierro = np.zeros((n, n), dtype=bool)
    hierro[:4, :] = hierro[-4:, :] = True
    hierro[:, :4] = hierro[:, -4:] = True
    hierro[14:19, :] = hierro[45:50, :] = True
    return L, hierro


def remaches(hierro, n=N):
    """Donde van los remaches de unas bandas de hierro: cada 8 pixeles, en el medio de cada banda horizontal."""
    r = np.zeros((n, n), dtype=bool)
    for y in range(1, n - 1):
        fila = hierro[y]
        if fila.sum() > n * 0.6 and hierro[y - 1].sum() > n * 0.6 and hierro[y + 1].sum() > n * 0.6:
            for x in range(4, n - 4, 8):
                if fila[x]:
                    r[y, x] = True
    return r
