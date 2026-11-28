"""El pintor de los muebles (parte 8h), a 64 x 64: libros en estanterias, barriles, cajas, paja, lino, alacenas y libros.

- libros(): el lado de una estanteria: el marco de madera (la balda de en medio y las de arriba y abajo) y dos filas de
  lomos de libros (de colores apagados, con bandas doradas y algun titulo), alguno inclinado, huecos; `vacia`: pocos libros,
  polvo y una telarana; `polvo`: todo con polvo y telaranas.
- barril_lado() / barril_tapa(): duelas con la veta y dos aros de hierro con remaches; la tapa de tablas en un circulo.
- caja(): una caja de tablas con el marco, una riostra en diagonal y clavos.
- paja() y lino(): para los jergones.
- alacena(): el frente de una alacena: marco, dos puertas de cuarterones, bisagras y tiradores de hierro.
- libro_tapa() / libro_hojas(): las tapas de cuero y el canto de las hojas (las pilas de libros).
"""
import math

import numpy as np

import pintor as P
from madera import ruido_estirado
from pintor import N, ruido

LOMOS = ["#4a1414", "#5c1f17", "#2a3a22", "#1f3a32", "#1c2a44", "#2c2440", "#4a3418", "#3a2a1a", "#262422", "#5a4a26",
         "#3c1c2c", "#202c2c"]
ORO = np.array([176, 140, 60])


def _hex(c):
    c = c.lstrip("#")
    return np.array([int(c[i:i + 2], 16) for i in (0, 2, 4)], dtype=float)


def _madera(semilla, rampa, n=N, horizontal=True, base=0.55):
    veta = ruido_estirado(semilla, 2, 16, octavas=3) if horizontal else ruido_estirado(semilla, 16, 2, octavas=3)
    v = base + 0.14 * (veta - 0.5) + 0.05 * (ruido(semilla + 1, base=16, octavas=1) - 0.5)
    return rampa.pintar(np.clip(v, 0, 1), 0.6)


def libros(semilla, rampa_madera, vacia=False, polvo=False, n=N):
    rng = np.random.default_rng(semilla)
    img = _madera(semilla + 1, rampa_madera, n).astype(float)
    fondo = img * 0.22  # el fondo de la estanteria, en sombra
    # las baldas: arriba (0-3), en medio (30-33) y abajo (60-63); los costados (0-2 y 61-63)
    baldas = [(0, 4), (30, 34), (60, 64)]
    hueco = np.ones((n, n), dtype=bool)
    for a, b in baldas:
        hueco[a:b, :] = False
    hueco[:, 0:3] = False
    hueco[:, n - 3:] = False
    img = np.where(hueco[..., None], fondo, img)
    # canto de las baldas: claro arriba, sombra debajo
    for a, b in baldas:
        img[a, :] = img[a, :] * 1.25
        if b < n:
            img[b, 3:n - 3] = fondo[b, 3:n - 3] * 0.6
    for (y0, y1) in ((4, 30), (34, 60)):
        x = 3
        while x < n - 4:
            if vacia and rng.random() < 0.65:
                x += int(rng.integers(3, 8))
                continue
            ancho = int(rng.integers(3, 7))
            if x + ancho > n - 3:
                break
            alto = int(rng.integers(16, y1 - y0 - 1))
            color = _hex(LOMOS[int(rng.integers(0, len(LOMOS)))]) * rng.uniform(0.85, 1.15)
            inclinado = rng.random() < 0.08 and x + ancho + 3 < n - 3
            for xx in range(x, x + ancho):
                t = (xx - x + 0.5) / ancho
                luz = 0.8 + 0.35 * math.cos((t - 0.35) * math.pi)
                for yy in range(y1 - alto, y1):
                    desp = int((y1 - yy) * 0.25) if inclinado else 0
                    xi = xx + desp
                    if xi >= n - 3:
                        continue
                    c = color * luz
                    rel = (yy - (y1 - alto)) / alto
                    if abs(rel - 0.12) < 0.04 or abs(rel - 0.88) < 0.04:
                        c = ORO * (0.75 + 0.3 * luz)
                    elif 0.3 < rel < 0.6 and abs(t - 0.5) < 0.2 and (yy % 3 == 0) and ancho >= 4:
                        c = ORO * 0.8
                    img[yy, xi] = c
            x += ancho + (int(rng.integers(0, 2)) if not inclinado else 3)
    if vacia or polvo:
        # polvo: todo mas gris y una telarana en una esquina de cada hueco
        gris = img.mean(axis=2, keepdims=True)
        img = img * 0.6 + gris * 0.4 + (18 if polvo else 8)
        for (y0, esquina) in ((4, 3), (34, n - 4)):
            for r in range(1, 12, 3):
                for a in np.linspace(0, math.pi / 2, 12):
                    xx = int(esquina + (r * math.cos(a) if esquina < n / 2 else -r * math.cos(a)))
                    yy = int(y0 + r * math.sin(a))
                    if 0 <= xx < n and 0 <= yy < n:
                        img[yy, xx] = [190, 190, 185]
            for a in np.linspace(0, math.pi / 2, 4):
                for r in np.linspace(0, 11, 22):
                    xx = int(esquina + (r * math.cos(a) if esquina < n / 2 else -r * math.cos(a)))
                    yy = int(y0 + r * math.sin(a))
                    if 0 <= xx < n and 0 <= yy < n:
                        img[yy, xx] = [170, 170, 165]
    return np.clip(img, 0, 255)


def barril_lado(semilla, rampa, hierro, n=N):
    rng = np.random.default_rng(semilla)
    veta = ruido_estirado(semilla, 16, 2, octavas=3)
    xs = np.arange(n)
    duela = xs // 8
    tonos = 0.55 + rng.normal(0, 0.05, n // 8)
    v = tonos[duela][None, :] + 0.12 * (veta - 0.5)
    v[:, (xs % 8) == 0] -= 0.2
    v[:, (xs % 8) == 1] += 0.06
    img = rampa.pintar(np.clip(v, 0, 1), 0.6)
    for y0 in (9, 50):
        for y in range(y0, y0 + 5):
            l = 0.45 + 0.25 * math.cos((y - y0 - 1.5) / 5 * math.pi)
            img[y, :] = hierro.pintar(np.full((1, n), l), 0.3)[0]
        for x in range(4, n, 16):
            img[y0 + 2, x] = hierro.pintar(np.array([[0.95]]), 0.0)[0, 0]
    return img


def barril_tapa(semilla, rampa, hierro, n=N):
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    r = np.hypot(xs - n / 2, ys - n / 2)
    veta = ruido_estirado(semilla, 2, 16, octavas=3)
    tabla = (ys // 10).astype(int)
    v = 0.58 + 0.05 * np.sin(tabla * 2.1) + 0.12 * (veta - 0.5)
    v = np.where((ys % 10) < 1, v - 0.2, v)
    v = np.where(r > 27, 0.35 + 0.1 * np.sin(np.arctan2(ys - n / 2, xs - n / 2) * 8), v)
    img = rampa.pintar(np.clip(v, 0, 1), 0.6)
    aro = (r > 27) & (r < 30)
    img = np.where(aro[..., None], hierro.pintar(np.full((n, n), 0.55), 0.4), img)
    return img


def caja(semilla, rampa, hierro, n=N):
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    veta_h = ruido_estirado(semilla, 2, 16, octavas=3)
    veta_v = ruido_estirado(semilla + 1, 16, 2, octavas=3)
    tabla = (ys // 12).astype(int)
    v = 0.52 + rng.normal(0, 0.04, 8)[np.clip(tabla, 0, 7)] + 0.12 * (veta_h - 0.5)
    v = np.where((ys % 12) < 1, v - 0.22, v)
    marco = (xs < 8) | (xs > n - 8) | (ys < 8) | (ys > n - 8)
    v = np.where(marco, 0.6 + 0.12 * (np.where((xs < 8) | (xs > n - 8), veta_v, veta_h) - 0.5), v)
    v = np.where(marco & ((xs < 1) | (xs > n - 1) | (ys < 1) | (ys > n - 1)), 0.25, v)
    borde_marco = (((xs >= 7) & (xs < 8)) | ((xs > n - 8) & (xs <= n - 7)) | ((ys >= 7) & (ys < 8)) | ((ys > n - 8) & (ys <= n - 7)))
    v = np.where(borde_marco & ~marco, v - 0.2, v)
    diag = (np.abs((xs - 8) - (ys - 8)) < 4) & ~marco
    v = np.where(diag, 0.62 + 0.12 * (veta_h - 0.5), v)
    v = np.where((np.abs(np.abs((xs - 8) - (ys - 8)) - 4) < 0.6) & ~marco, v - 0.2, v)
    img = rampa.pintar(np.clip(v, 0, 1), 0.6)
    # esquinas de hierro con clavos
    for (cx, cy) in ((4, 4), (n - 4, 4), (4, n - 4), (n - 4, n - 4)):
        esq = (np.abs(xs - cx) < 5) & (np.abs(ys - cy) < 5)
        img = np.where(esq[..., None], hierro.pintar(np.full((n, n), 0.45), 0.4), img)
        clavo = np.hypot(xs - cx, ys - cy) < 1.5
        img = np.where(clavo[..., None], hierro.pintar(np.full((n, n), 0.85), 0.0), img)
    return img


def paja(semilla, n=N):
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 3))
    img[:] = (112, 92, 48)
    col = [np.array(c) for c in ((150, 124, 62), (176, 150, 82), (198, 172, 100), (128, 104, 52), (92, 74, 38))]
    for _ in range(700):
        x, y = rng.uniform(0, n), rng.uniform(0, n)
        a = rng.uniform(-0.6, 0.6) + (0 if rng.random() < 0.5 else math.pi / 2)
        largo = rng.uniform(4, 10)
        c = col[int(rng.integers(0, len(col)))]
        for t in np.linspace(0, largo, int(largo * 2)):
            img[int(y + math.sin(a) * t) % n, int(x + math.cos(a) * t) % n] = c
    return img


def lino(semilla, rampa, n=N):
    ys, xs = np.mgrid[0:n, 0:n]
    trama = ((xs + ys) % 4 < 2).astype(float)
    v = 0.55 + 0.08 * (trama - 0.5) + 0.06 * (ruido(semilla, base=8, octavas=2) - 0.5)
    # pespuntes cada 16
    v = np.where(((xs % 16) == 0) & ((ys % 4) < 2), v - 0.18, v)
    return rampa.pintar(np.clip(v, 0, 1), 0.5)


def alacena(semilla, rampa, hierro, n=N):
    """El frente de una alacena: marco, dos puertas con un cuarteron rehundido cada una, bisagras y tiradores."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    veta_v = ruido_estirado(semilla, 16, 2, octavas=3)
    veta_h = ruido_estirado(semilla + 1, 2, 16, octavas=3)
    v = 0.55 + 0.12 * (veta_v - 0.5)
    marco_h = (ys < 6) | (ys > n - 6)
    v = np.where(marco_h, 0.58 + 0.12 * (veta_h - 0.5), v)
    v = np.where((xs < 4) | (xs > n - 4) | (np.abs(xs - n / 2) < 1.5), 0.5 + 0.12 * (veta_v - 0.5), v)
    # juntas de las puertas
    for x0 in (4, n / 2 - 1.5, n / 2 + 1.5, n - 4):
        v = np.where(np.abs(xs - x0) < 0.6, 0.2, v)
    v = np.where((np.abs(ys - 6) < 0.6) | (np.abs(ys - (n - 6)) < 0.6), 0.2, v)
    # cuarterones: rehundidos con bisel
    for (x0, x1) in ((9, 27), (37, 55)):
        dentro = (xs > x0) & (xs < x1) & (ys > 11) & (ys < n - 11)
        v = np.where(dentro, v - 0.06, v)
        borde_luz = ((np.abs(xs - x1) < 1) | (np.abs(ys - (n - 11)) < 1)) & (xs > x0) & (xs <= x1) & (ys > 11) & (ys <= n - 11)
        borde_sombra = ((np.abs(xs - x0) < 1) | (np.abs(ys - 11) < 1)) & (xs >= x0) & (xs < x1) & (ys >= 11) & (ys < n - 11)
        v = np.where(borde_luz, v + 0.15, v)
        v = np.where(borde_sombra, v - 0.18, v)
    img = rampa.pintar(np.clip(v, 0, 1), 0.6)
    met = np.zeros((n, n), dtype=bool)
    for y0 in (12, n - 16):
        met |= (np.abs(xs - 5.5) < 2.5) & (ys > y0) & (ys < y0 + 4)
        met |= (np.abs(xs - (n - 5.5)) < 2.5) & (ys > y0) & (ys < y0 + 4)
    for x0 in (n / 2 - 5, n / 2 + 5):
        met |= np.hypot(xs - x0, ys - n / 2) < 2.2
    img = np.where(met[..., None], hierro.pintar(np.full((n, n), 0.55) + 0.25 * ((xs + ys) % 3 == 0), 0.0), img)
    return img


def libro_tapa(semilla, n=N):
    """Tapas de cuero de varios colores en franjas de 16 (cada libro de la pila coge la suya) con un filete dorado."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 3))
    ys, xs = np.mgrid[0:n, 0:n]
    for k in range(4):
        color = _hex(LOMOS[(k * 3 + 1) % len(LOMOS)])
        franja = (ys // 16) == k
        cuero = color * (0.85 + 0.25 * ruido(semilla + k, base=16, octavas=2))[..., None]
        img = np.where(franja[..., None], cuero, img)
        fil = franja & (((ys % 16) == 2) | ((ys % 16) == 13) | (np.abs(xs - n / 2) < 0.6) & ((ys % 16) > 2) & ((ys % 16) < 13))
        img = np.where(fil[..., None], ORO * 0.8, img)
    return img


def libro_hojas(semilla, n=N):
    """El canto de las hojas: rayas finas color pergamino."""
    ys, xs = np.mgrid[0:n, 0:n]
    v = 200 - 30 * ((ys % 2) == 0) - 12 * ruido(semilla, base=16, octavas=1)
    return np.dstack([v, v * 0.92, v * 0.78])
