"""El pintor del metal y del vidrio (parte 8f), a 64 x 64.

- planchas(): planchas de metal (cuadricula fija, con la junta en la fila y la columna 0: todo borde de bloque es una junta,
  asi las variantes no tienen que casar), bisel, remaches, abolladuras de martillo, rayas y brillo.
- oxido() / verdin(): el oxido (manchas y chorretones que bajan de los remaches y las juntas) y el verdin del bronce.
- labrado(): una plancha de oro labrada (marco, cuadrifolio, botones, fondo punteado).
- barrotes(): los barrotes de una reja (los modelos son los de los barrotes del juego: barras en las columnas 2-3, 7-8 y
  12-13 de 16); `forjada`: con volutas; con `oxido`: comidos.
- cadena(): los eslabones de una cadena (los modelos son los de la cadena del juego: dos planos cruzados, las columnas 0-3
  y 3-6 de 16).
- vidrio(): vidrio viejo (ondas, burbujas), sucio (manchas y grietas) o emplomado (rombos de plomo); vidriera(): una
  vidriera de colores (plomo en un cuadrifolio que casa con los de al lado, piezas de colores).
Todo devuelve campos de luz (L.v, con L.piedra < 0 en las juntas) o rgba.
"""
import math

import numpy as np

import pintor as P
from pintor import Lienzo, N, ruido


def planchas(semilla, cols=2, filas=2, base=0.5, varia=0.05, remaches=True, abollado=1.0, rayas=1.0, n=N):
    """Planchas de cols x filas por bloque. L.piedra: el numero de plancha (-1 la junta); L.remache: los remaches;
    L.bisel_luz / L.bisel_sombra: los cantos."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    ys, xs = np.mgrid[0:n, 0:n]
    pw, ph = n // cols, n // filas
    ci, fi = xs // pw, ys // ph
    L.piedra = (fi * cols + ci).astype(int)
    junta = (xs % pw == 0) | (ys % ph == 0)
    L.piedra = np.where(junta, -1, L.piedra)
    tonos = base + rng.normal(0, varia, cols * filas)
    v = tonos[np.clip(L.piedra, 0, None)]
    # abolladuras de martillo y un brillo suave en diagonal dentro de cada plancha
    dent = ruido(semilla + 1, base=8, octavas=2)
    v = v + 0.10 * abollado * np.clip(P.luz_de(dent) * 6, -1, 1) + 0.04 * (ruido(semilla + 2, base=16, octavas=2) - 0.5)
    u = ((xs % pw) + (ys % ph)) / (pw + ph)
    v = v + 0.06 * np.cos((u - 0.35) * math.pi * 1.4)
    # bisel: canto de arriba y de la izquierda claro, de abajo y de la derecha oscuro
    L.bisel_luz = ((xs % pw == 1) | (ys % ph == 1)) & ~junta
    L.bisel_sombra = ((xs % pw == pw - 1) | (ys % ph == ph - 1)) & ~junta
    v = np.where(L.bisel_luz, v + 0.14, v)
    v = np.where(L.bisel_sombra, v - 0.12, v)
    v = np.where(junta, 0.06, v)
    # rayas finas
    for _ in range(int(7 * rayas)):
        x, y = rng.uniform(3, n - 3), rng.uniform(3, n - 3)
        a = rng.uniform(-0.5, 0.5) + (0 if rng.random() < 0.5 else math.pi / 2)
        largo = rng.uniform(4, 12)
        for t in np.linspace(0, largo, int(largo * 2)):
            xi, yi = int(x + math.cos(a) * t) % n, int(y + math.sin(a) * t) % n
            if not junta[yi, xi]:
                v[yi, xi] += 0.08
    L.remache = np.zeros((n, n), dtype=bool)
    if remaches:
        puntos = []
        for c in range(cols):
            for f in range(filas):
                x0, y0 = c * pw, f * ph
                for (dx, dy) in ((4, 4), (pw - 4, 4), (4, ph - 4), (pw - 4, ph - 4)):
                    puntos.append((x0 + dx, y0 + dy))
                # y por los lados si la plancha es grande
                if pw >= 32:
                    puntos += [(x0 + pw // 2, y0 + 4), (x0 + pw // 2, y0 + ph - 4)]
                if ph >= 32:
                    puntos += [(x0 + 4, y0 + ph // 2), (x0 + pw - 4, y0 + ph // 2)]
        for (px, py) in puntos:
            d = np.hypot(xs + 0.5 - px, ys + 0.5 - py)
            cab = d < 2.4
            L.remache |= cab
            luzr = (-(xs + 0.5 - px) - (ys + 0.5 - py)) / 2.4
            v = np.where(cab, 0.55 + 0.30 * luzr, v)
            # su sombra abajo a la derecha
            sombra = (np.hypot(xs + 0.5 - px - 1.2, ys + 0.5 - py - 1.2) < 2.6) & ~cab
            v = np.where(sombra, v - 0.12, v)
        L.puntos = puntos
    else:
        L.puntos = []
    L.v = v
    return L


def oxido(L, semilla, cuanto=0.5, n=N):
    """Mascara de oxido: manchas, las juntas y chorretones que bajan de cada remache."""
    rng = np.random.default_rng(semilla + 51)
    r = ruido(semilla + 52, base=4, octavas=4)
    m = np.clip((r - (1.0 - cuanto)) * 4, 0, 1)
    m = np.maximum(m, (L.piedra < 0) * 0.7)
    for (px, py) in getattr(L, "puntos", []):
        if rng.random() < 0.7:
            x = px + rng.uniform(-1, 1)
            largo = rng.uniform(6, 22)
            for j in range(int(largo)):
                y = int(py + 2 + j) % n
                w = 1.0 - j / largo
                for dx in (-1, 0, 1):
                    xi = int(x + dx) % n
                    m[y, xi] = max(m[y, xi], w * (0.9 if dx == 0 else 0.45))
                if rng.random() < 0.15:
                    x += rng.choice((-1, 1))
    return m


def labrado(semilla, base=0.55, n=N):
    """Una plancha labrada: marco, un cuadrifolio en el centro con su boton, hojitas en las esquinas y el fondo punteado.
    Devuelve el campo de luz (relieve con luz de arriba a la izquierda)."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    h = np.full((n, n), 0.35)
    # fondo punteado (punzon)
    pts = (((xs.astype(int) % 4) == 1) & ((ys.astype(int) % 4) == 1)) | (((xs.astype(int) % 4) == 3) & ((ys.astype(int) % 4) == 3))
    h = np.where(pts, 0.28, h)
    # marco
    d_borde = np.minimum(np.minimum(xs, n - xs), np.minimum(ys, n - ys))
    h = np.where(d_borde < 5, 0.85, h)
    h = np.where((d_borde >= 5) & (d_borde < 6), 0.25, h)
    h = np.where(d_borde < 1.2, 0.6, h)
    c = n / 2
    # cuadrifolio: cuatro circulos
    lob = np.zeros((n, n))
    for (ox, oy) in ((0, -9), (9, 0), (0, 9), (-9, 0)):
        rr = np.hypot(xs - c - ox, ys - c - oy)
        lob = np.maximum(lob, (rr < 11).astype(float))
        h = np.where((rr > 9.2) & (rr < 11.4), 0.9, h)
    interior = np.zeros((n, n), dtype=bool)
    for (ox, oy) in ((0, -9), (9, 0), (0, 9), (-9, 0)):
        interior |= np.hypot(xs - c - ox, ys - c - oy) < 9.2
    h = np.where(interior & (h < 0.9), 0.5 + 0.08 * ruido(semilla + 3, base=16, octavas=1), h)
    # boton central
    rr = np.hypot(xs - c, ys - c)
    h = np.where(rr < 5.5, 0.95 - 0.05 * rr / 5.5, h)
    h = np.where((rr >= 5.5) & (rr < 6.5), 0.3, h)
    # hojitas en las esquinas (entre el marco y el cuadrifolio)
    for (sx, sy) in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        hx, hy = c + sx * 20, c + sy * 20
        u = ((xs - hx) * sx + (ys - hy) * sy) / math.sqrt(2)
        w = ((xs - hx) * sy - (ys - hy) * sx) / math.sqrt(2)
        hoja = (u / 6.5) ** 2 + (w / 2.6) ** 2 < 1
        h = np.where(hoja, 0.8, h)
    rel = P.relieve(h)
    v = base + 0.55 * (h - 0.5) + 0.35 * rel + 0.04 * (ruido(semilla + 4, base=16, octavas=2) - 0.5)
    return v


# ------------------------------------------------------------------ rejas y cadenas

def _barra_v(img, x0, ancho, rampa, y0=0, y1=N, luz=0.0):
    for x in range(x0, x0 + ancho):
        t = (x - x0 + 0.5) / ancho
        l = 0.35 + 0.45 * math.cos((t - 0.3) * math.pi) + luz
        for y in range(y0, y1):
            img[y % N, x % N] = _c(rampa, l)


def _barra_h(img, y0, alto, rampa, x0=0, x1=N, luz=0.0):
    for y in range(y0, y0 + alto):
        t = (y - y0 + 0.5) / alto
        l = 0.35 + 0.45 * math.cos((t - 0.3) * math.pi) + luz
        for x in range(x0, x1):
            img[y % N, x % N] = _c(rampa, l)


def _c(rampa, l):
    return np.concatenate([rampa.pintar(np.array([[min(1.0, max(0.0, l))]]), 0.0)[0, 0], [255]])


def barrotes(semilla, rampa, forjada=False, oxido_rampa=None, n=N):
    """Los barrotes: tres barras redondas (columnas 8-15, 28-35 y 48-55 de 64, las de los barrotes del juego) y dos
    travesanos planos con remaches; `forjada`: volutas entre las barras; `oxido_rampa`: comidos de oxido."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    for y0 in (10, 46):
        _barra_h(img, y0, 6, rampa, luz=-0.08)
    for x0 in (8, 28, 48):
        _barra_v(img, x0, 8, rampa)
        for y0 in (10, 46):
            # el remache del cruce
            for dy in range(6):
                for dx in range(8):
                    d = math.hypot(dx - 3.5, dy - 2.5)
                    if d < 2.3:
                        img[y0 + dy, x0 + dx] = _c(rampa, 0.75 - 0.15 * (dx + dy - 6) / 3)
    if forjada:
        # volutas (una C y una C al reves) entre cada par de barras, entre los travesanos
        for xc in (2, 22, 42):
            for (cy, s) in ((24, 1), (36, -1)):
                for a in np.linspace(0, 2 * math.pi * 0.8, 70):
                    r = 2.0 + 2.6 * a / (2 * math.pi * 0.8)
                    x = xc + s * r * math.cos(a)
                    y = cy + r * math.sin(a) * s
                    for (ex, ey) in ((0, 0), (1, 0)):
                        xi, yi = int(x + ex + 2) % n, int(y + ey) % n
                        img[yi, xi] = _c(rampa, 0.55 + 0.2 * math.cos(a))
        # puntas de lanza bajo el travesano de arriba no (es una reja de ventana): botones en el centro
    if oxido_rampa is not None:
        r = ruido(semilla + 7, base=8, octavas=3)
        al = img[..., 3] > 0
        for y, x in zip(*np.nonzero(al)):
            if r[y, x] > 0.62:
                img[y, x] = _c(oxido_rampa, 0.3 + 0.6 * (r[y, x] - 0.62) / 0.38 + rng.uniform(-0.1, 0.1))
            if r[y, x] > 0.9 and rng.random() < 0.5:
                img[y, x, 3] = 0
    return img


def cadena(semilla, rampa, n=N):
    """La cadena: en el plano de la columna 0-11 los eslabones de frente (un anillo) y en el de la 12-23 los de canto (una
    barra), alternando; se repite de arriba abajo (dos eslabones de cada plano por bloque)."""
    img = np.zeros((n, n, 4))
    for k in range(2):
        # de frente, en el plano A (columnas 0-11): de y 32k a 32k + 24
        y0 = 32 * k
        for y in range(y0, y0 + 24):
            for x in range(0, 12):
                u = (x + 0.5 - 6) / 6
                w = (y + 0.5 - (y0 + 12)) / 12
                d = u * u + w * w
                ui = (x + 0.5 - 6) / 2.2
                wi = (y + 0.5 - (y0 + 12)) / 8.0
                if d <= 1 and ui * ui + wi * wi > 1:
                    l = 0.5 + 0.35 * (-u * 0.7 - w * 0.5) - 0.2 * (d > 0.75)
                    img[y % n, x] = _c(rampa, l)
        # de canto, en el plano B (columnas 12-23): de y 32k + 16 a 32k + 40
        y1 = 32 * k + 16
        for y in range(y1, y1 + 24):
            for x in range(16, 20):
                w = (y + 0.5 - (y1 + 12)) / 12
                if abs(w) <= 1:
                    t = (x - 16 + 0.5) / 4
                    l = 0.4 + 0.4 * math.cos((t - 0.3) * math.pi) - 0.15 * abs(w)
                    img[y % n, x] = _c(rampa, l)
    return img


def cadena_objeto(semilla, rampa, n=N):
    """El dibujo de la cadena en el inventario: tres eslabones en diagonal."""
    img = np.zeros((n, n, 4))
    for k in range(3):
        cx, cy = 21 + 11 * k, 21 + 11 * k
        for y in range(n):
            for x in range(n):
                u = ((x - cx) + (y - cy)) / math.sqrt(2) / 11
                w = ((x - cx) - (y - cy)) / math.sqrt(2) / (6.5 if k % 2 == 0 else 3.0)
                d = u * u + w * w
                ui = ((x - cx) + (y - cy)) / math.sqrt(2) / 7
                wi = ((x - cx) - (y - cy)) / math.sqrt(2) / (3.0 if k % 2 == 0 else 1.0)
                if d <= 1 and (k % 2 == 1 or ui * ui + wi * wi > 1):
                    img[y, x] = _c(rampa, 0.55 + 0.3 * (-(x - cx) - (y - cy)) / 16)
    return img


# ------------------------------------------------------------------ vidrio

def vidrio(semilla, color, alfa=0.42, tipo="viejo", n=N):
    """Vidrio: `viejo` (verdoso, ondas que bajan, burbujas, un brillo), `sucio` (manchas de mugre y grietas claras) o
    `emplomado` (rombos de plomo de 32 x 32 —casan con los de al lado—, cada pieza de un tono). `color`: (r, g, b).
    Devuelve rgba (0-255)."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    col = np.array(color, dtype=float)
    onda = ruido(semilla + 1, base=4, octavas=2)
    estria = 0.5 + 0.5 * np.sin(xs * 2 * math.pi / n * 3 + 2.5 * onda)
    brillo = 0.85 + 0.22 * estria + 0.10 * (ruido(semilla + 2, base=8, octavas=2) - 0.5)
    rgb = col[None, None, :] * brillo[..., None]
    a = np.full((n, n), alfa * 255) + 18 * (estria - 0.5)
    if tipo in ("viejo", "sucio"):
        for _ in range(26 if tipo == "viejo" else 14):
            x, y = int(rng.uniform(0, n)), int(rng.uniform(0, n))
            rgb[y, x] = np.minimum(255, col * 1.6 + 40)
            a[y, x] = min(255, a[y, x] + 60)
    if tipo == "sucio":
        mugre = ruido(semilla + 3, base=4, octavas=4)
        m = np.clip((mugre - 0.45) * 3, 0, 1)
        rgb = rgb * (1 - 0.55 * m[..., None]) + np.array([62, 52, 38])[None, None, :] * 0.55 * m[..., None]
        a = a + 120 * m
        for _ in range(2):
            x, y = rng.uniform(10, n - 10), rng.uniform(10, n - 10)
            for _r in range(3):
                ang = rng.uniform(0, 2 * math.pi)
                largo = rng.uniform(8, 20)
                for t in np.linspace(0, largo, int(largo * 2)):
                    xi, yi = int(x + math.cos(ang) * t) % n, int(y + math.sin(ang) * t) % n
                    rgb[yi, xi] = [210, 215, 205]
                    a[yi, xi] = 200
                    ang += rng.uniform(-0.15, 0.15)
    if tipo == "emplomado":
        # rombos: |x - cx| / 16 + |y - cy| / 16 en celdas de 32 x 32
        u = (xs % 32) - 16
        w = (ys % 32) - 16
        d = np.abs(u) / 16 + np.abs(w) / 16
        plomo = np.abs(d - 1.0) < 0.07
        # cada pieza (el rombo del centro de la celda o los cuatro trozos de las esquinas) con su tono
        celda = (xs // 32).astype(int) * 3 + (ys // 32).astype(int) * 7 + (d > 1) * (np.sign(u) + 2 * np.sign(w) + 5).astype(int)
        tono = 0.88 + 0.12 * np.sin(celda * 1.7 + semilla)
        rgb = rgb * tono[..., None]
        borde = np.abs(d - 1.0) < 0.16
        rgb = np.where(borde[..., None] & ~plomo[..., None], rgb * 0.75, rgb)
        rgb = np.where(plomo[..., None], np.array([40, 40, 44])[None, None, :] * (0.9 + 0.3 * (np.sign(w)[..., None] < 0)), rgb)
        a = np.where(plomo, 255, a)
    return np.dstack([np.clip(rgb, 0, 255), np.clip(a, 0, 255)])


def vidriera(semilla, principal, acento, claro, alfa=0.78, n=N):
    """Una vidriera: un cuadrifolio de plomo en el centro de cada bloque (con su boton de color de `acento`), las
    esquinas cortadas en cuartos de circulo (que con los bloques de al lado hacen otro circulo) y piezas de la rampa
    `principal` (cada una de un tono), con algunas de `claro`. Devuelve rgba."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    c = n / 2
    # las lineas de plomo
    lineas = np.zeros((n, n), dtype=bool)
    region = np.zeros((n, n), dtype=int)
    k = 1
    # cuadrifolio
    lobulos = []
    for (ox, oy) in ((0, -8.5), (8.5, 0), (0, 8.5), (-8.5, 0)):
        rr = np.hypot(xs - c - ox, ys - c - oy)
        lobulos.append(rr)
    dentro = np.zeros((n, n), dtype=bool)
    for rr in lobulos:
        dentro |= rr < 10
    for rr in lobulos:
        lineas |= (np.abs(rr - 10) < 0.8) & ~np.any([r2 < 9.2 for r2 in lobulos if r2 is not rr], axis=0)
    rc = np.hypot(xs - c, ys - c)
    boton = rc < 5.2
    lineas |= np.abs(rc - 5.2) < 0.75
    # cuartos de circulo en las esquinas (radio 14)
    esquinas = np.zeros((n, n), dtype=bool)
    for (ex, ey) in ((0, 0), (n, 0), (0, n), (n, n)):
        re = np.hypot(xs - ex, ys - ey)
        esquinas |= re < 13
        lineas |= np.abs(re - 13) < 0.8
    # el marco del bloque (media linea en cada borde: con el de al lado hace una entera)
    lineas |= (xs < 0.9) | (ys < 0.9) | (xs > n - 0.9) | (ys > n - 0.9)
    # lineas rectas que parten el fondo en piezas (diagonales del cuadrifolio a las esquinas)
    fondo = ~dentro & ~esquinas
    for (sx, sy) in ((1, 1), (1, -1)):
        diag = np.abs((xs - c) * sy - (ys - c) * sx) / math.sqrt(2) < 0.75
        lineas |= diag & fondo
    lineas |= (np.abs(xs - c) < 0.75) & fondo
    lineas |= (np.abs(ys - c) < 0.75) & fondo
    # las piezas: cada region conexa (aprox.: por cuadrante y tipo) con su tono
    cuad = ((xs > c).astype(int) + 2 * (ys > c).astype(int))
    oct_ = (np.abs(xs - c) > np.abs(ys - c)).astype(int)
    tipo = np.where(boton, 0, np.where(dentro, 1, np.where(esquinas, 2, 3)))
    region = tipo * 100 + cuad * 10 + oct_
    tonos = {}
    rgb = np.zeros((n, n, 3))
    for reg in np.unique(region):
        sel = region == reg
        t = reg // 100
        if t == 0:
            rp, l = acento, 0.75
        elif t == 1:
            rp, l = (claro if (reg % 10) % 2 == 0 and rng.random() < 0.5 else principal), rng.uniform(0.5, 0.8)
        elif t == 2:
            rp, l = acento, rng.uniform(0.4, 0.6)
        else:
            rp, l = principal, rng.uniform(0.25, 0.6)
        tonos[reg] = (rp, l)
        textura = l + 0.12 * (ruido(semilla + reg, base=8, octavas=2) - 0.5)
        pint = rp.pintar(np.clip(textura, 0, 1), 0.6)
        rgb = np.where(sel[..., None], pint, rgb)
    # junto al plomo, mas oscuro (el vidrio se ve mas grueso)
    cerca = np.zeros((n, n), dtype=bool)
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            cerca |= np.roll(np.roll(lineas, dy, 0), dx, 1)
    rgb = np.where((cerca & ~lineas)[..., None], rgb * 0.78, rgb)
    plomo = np.array([34, 33, 36]) + 12 * (ruido(semilla + 9, base=16, octavas=1)[..., None] - 0.5)
    rgb = np.where(lineas[..., None], plomo, rgb)
    a = np.where(lineas, 255, alfa * 255 + 20 * (ruido(semilla + 11, base=8, octavas=2) - 0.5))
    return np.dstack([np.clip(rgb, 0, 255), np.clip(a, 0, 255)])
