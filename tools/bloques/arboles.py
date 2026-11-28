"""El pintor de los arboles (parte 8e): cortezas, testas, hojas, hojarasca, musgo que cuelga y lianas, a 64 x 64.

Las variantes de una corteza casan entre si por arriba, por abajo y por los lados: cada una se pinta dos veces, una con
lo comun (`borde`: los surcos de los bordes y su ruido) y otra con lo suyo (los surcos de dentro serpentean distinto, los
puentes entre surcos, las grietas, las lenticelas), y se mezclan con un peso que es 0 en los bordes y 1 desde 7 pixeles
hacia dentro (`_peso`). Lo que cae cerca del borde se pinta en las dos.

- corteza(): `surcos` (roble, sangre: surcos hondos que se juntan y se separan), `musgosa` (pantano: lo mismo, mas
  blanda), `placas` (pino: placas largas a capas), `seca` (muerto: madera desnuda, rajada y gris) y `lisa` (ceniza:
  lisa y palida con lenticelas).
- testa(): el corte del tronco: anillos, medula, una grieta y el aro de corteza.
- hojas(): una masa de hojas con huecos (transparencia): las de atras en sombra, las de delante con luz, y los racimos
  con luz de arriba a la izquierda. `agujas`: ramitas de pino; `nieve`: con nieve encima.
- hojarasca(): hojas caidas sueltas por el suelo (o agujas de pino).
- colgante(): mechones de musgo que cuelgan (`punta`: el de abajo, que acaba).
- liana(): tallos con hojitas que trepan.
"""
import math

import numpy as np

import pintor as P
from madera import ruido_estirado
from pintor import Lienzo, N, ruido


def _peso(n=N, m0=2.0, m1=7.0):
    """0 en los bordes (a menos de m0 pixeles), 1 desde m1 hacia dentro, suave entre medias."""
    d = np.minimum(np.arange(n) + 0.5, n - (np.arange(n) + 0.5))
    w = np.clip((d - m0) / (m1 - m0), 0, 1)
    w = w * w * (3 - 2 * w)
    return np.minimum.outer(w, w)


def _envolver(d, n=N):
    return (d + n / 2) % n - n / 2


def _segmento(X, Y, x0, y0, x1, y1, n=N):
    """Distancia de cada pixel al segmento (sobre el toro: el segmento no cruza los bordes)."""
    dx, dy = x1 - x0, y1 - y0
    t = np.clip(((X - x0) * dx + (Y - y0) * dy) / (dx * dx + dy * dy + 1e-9), 0, 1)
    return np.hypot(_envolver(X - (x0 + t * dx), n), _envolver(Y - (y0 + t * dy), n))


class _Red:
    """Los surcos de una corteza: curvas que bajan de arriba abajo. Las anclas (donde cruzan el borde de arriba y el de
    abajo) y su vaiven comun salen de la semilla comun; los de dentro (`dentro` pixeles lejos de los lados) serpentean
    ademas a su manera en cada variante (un vaiven que vale 0 arriba y abajo)."""

    def __init__(self, comun, semilla, cuantos, ancho=(0.9, 1.5), amp_comun=1.5, amp_var=3.0, dentro=15, n=N):
        rb = np.random.default_rng(comun + 11)
        rv = np.random.default_rng(semilla + 13)
        self.n = n
        ys = np.arange(n) + 0.5
        paso = n / cuantos
        off = rb.uniform(0, n)
        anclas = sorted(((k + rb.uniform(0.3, 0.7)) * paso + off) % n for k in range(cuantos))
        self.xs, self.xv, self.anchos, self.dentro = [], [], [], []
        for a in anclas:
            w = np.zeros(n)
            for j in (1, 2):
                w += rb.uniform(-1, 1) * amp_comun / 2 * np.sin(2 * math.pi * j * ys / n + rb.uniform(0, 2 * math.pi))
            xs = a + w
            es_dentro = dentro <= a <= n - dentro
            wv = np.zeros(n)
            if es_dentro:
                for j in (1, 2, 3):
                    wv += rv.uniform(-1, 1) * amp_var / j * np.sin(math.pi * j * ys / n)
                wv = np.clip(wv, -amp_var, amp_var)
            self.xs.append(xs)
            self.xv.append(xs + wv)
            self.anchos.append(rb.uniform(*ancho))
            self.dentro.append(es_dentro)
        Y, X = np.mgrid[0:n, 0:n] + 0.5
        self.X, self.Y = X, Y

    def distancia(self, variante):
        """Distancia (en anchos de surco) al surco mas cercano, en la version comun o en la de la variante."""
        curvas = self.xv if variante else self.xs
        D = np.full((self.n, self.n), 1e9)
        for xk, a in zip(curvas, self.anchos):
            D = np.minimum(D, np.abs(_envolver(self.X - xk[:, None], self.n)) / a)
        return D

    def puentes(self, D, rng, cuantos, largo=(6, 12)):
        """Surcos cortos en diagonal de un surco de dentro al de al lado (solo en la variante): la red de rombos."""
        n = self.n
        dentro = [k for k in range(len(self.xv)) if self.dentro[k]]
        for _ in range(cuantos):
            if len(dentro) < 2:
                break
            i = int(rng.integers(0, len(dentro) - 1))
            k1, k2 = dentro[i], dentro[i + 1]
            if rng.random() < 0.5:
                k1, k2 = k2, k1
            y1 = rng.uniform(10, n - 10 - largo[1])
            y2 = y1 + rng.uniform(*largo)
            x1, x2 = self.xv[k1][int(y1)], self.xv[k2][int(y2)]
            a = min(self.anchos[k1], self.anchos[k2]) * 0.85
            D = np.minimum(D, _segmento(self.X, self.Y, x1, y1, x2, y2, n) / a)
        return D


def _alto(D, plano=4.0):
    """De la distancia al surco a la altura: 0 en el surco, 1 (meseta) desde `plano` anchos."""
    t = np.clip(D / plano, 0, 1)
    return t * t * (3 - 2 * t)


def _cuerpo(D, vetas, fino, base, hondo=0.10, plano=4.0):
    """El valor de luz de una corteza de surcos: el fondo del surco oscuro, las laderas con luz de la izquierda y la
    meseta con sus vetas."""
    h = _alto(D, plano)
    luz = np.clip(P.luz_de(h) * 2.2, -1, 1)
    v = base + 0.12 * (vetas - 0.5) + 0.06 * (fino - 0.5)
    v = hondo + (v - hondo) * (0.30 + 0.70 * h) + 0.15 * luz
    return np.where(D < 0.55, hondo * 0.7, v)


def corteza(semilla, tipo="surcos", base=0.5, borde=None, n=N):
    comun = semilla if borde is None else borde
    rng = np.random.default_rng(semilla + 3)
    L = Lienzo(n)
    L.piedra[:] = 0
    w = _peso(n)
    vetas_c = ruido_estirado(comun + 1, 8, 1, octavas=3)
    vetas_v = ruido_estirado(semilla + 1, 8, 1, octavas=3)
    fino_c = ruido(comun + 2, base=16, octavas=2)
    fino_v = ruido(semilla + 2, base=16, octavas=2)
    # (ruido_estirado: fx celdas a lo ancho y fy a lo alto; 8 a lo ancho y 1 a lo alto = vetas verticales)
    if tipo in ("surcos", "musgosa", "placas", "seca"):
        if tipo == "placas":
            red = _Red(comun, semilla, 5, ancho=(1.0, 1.4), amp_comun=2.0, amp_var=2.0, dentro=14)
        elif tipo == "seca":
            red = _Red(comun, semilla, 3, ancho=(0.6, 0.9), amp_comun=1.0, amp_var=2.5, dentro=14)
        else:
            red = _Red(comun, semilla, 7, ancho=(0.9, 1.6))
        Dc = red.distancia(False)
        Dv = red.distancia(True)
        if tipo in ("surcos", "musgosa"):
            Dv = red.puentes(Dv, rng, 5 if tipo == "surcos" else 4, largo=(10, 18))
        if tipo == "placas":
            # placas irregulares, largas, cada una con su tono; el corte entre placas, hondo
            Pc, Pv, tc, tv = _voronoi_placas(comun, semilla, n)
            Dc, Dv = Pc / 1.15, Pv / 1.15
            vc = _cuerpo(Dc, vetas_c, fino_c, base + tc, hondo=0.12, plano=2.0)
            vv = _cuerpo(Dv, vetas_v, fino_v, base + tv, hondo=0.12, plano=2.0)
        elif tipo == "seca":
            grano_c = ruido_estirado(comun + 5, 16, 2, octavas=2)
            grano_v = ruido_estirado(semilla + 5, 16, 2, octavas=2)
            vc = _seca(Dc, grano_c, fino_c, base)
            vv = _seca(Dv, grano_v, fino_v, base)
            # nudos y rajas de la variante
            vv = _rajas(vv, rng, n)
        else:
            vc = _cuerpo(Dc, vetas_c, fino_c, base, hondo=0.08 if tipo == "surcos" else 0.12)
            vv = _cuerpo(Dv, vetas_v, fino_v, base, hondo=0.08 if tipo == "surcos" else 0.12)
            # grietas cortas de traves en las mesetas (solo dentro)
            for _ in range(10):
                x, y = rng.uniform(9, n - 9), int(rng.uniform(9, n - 9))
                for i in range(int(rng.integers(2, 5))):
                    xi = int(x + i) % n
                    if Dv[y, xi] > 2.5:
                        vv[y, xi] -= 0.09
                        vv[(y + 1) % n, xi] += 0.04
        L.v = vc * (1 - w) + vv * w
        L.surco = (Dc * (1 - w) + Dv * w) < 1.0
    elif tipo == "lisa":
        vc = base + 0.05 * (vetas_c - 0.5) + 0.04 * (fino_c - 0.5)
        vv = base + 0.05 * (vetas_v - 0.5) + 0.04 * (fino_v - 0.5)
        # lenticelas: las de los bordes (comunes) en las dos, las de dentro solo en la variante
        rb = np.random.default_rng(comun + 17)
        for _ in range(30):
            x, y = rb.uniform(0, n), rb.uniform(0, n)
            if min(x, n - x, y, n - y) < 9:
                _lenticela(vc, x, y, n)
                _lenticela(vv, x, y, n)
        for _ in range(20):
            x, y = rng.uniform(10, n - 10), rng.uniform(10, n - 10)
            _lenticela(vv, x, y, n)
        for _ in range(2):
            # cicatrices (ojos) oscuras donde hubo una rama
            cx, cy = rng.uniform(16, n - 16), rng.uniform(16, n - 16)
            for dy in range(-3, 4):
                for dx in range(-6, 7):
                    d = (dx / 5.0) ** 2 + (dy / 2.4) ** 2
                    if d < 1:
                        x, y = int(cx + dx) % n, int(cy + dy) % n
                        vv[y, x] = 0.16 + 0.04 * rng.random() if d < 0.45 else vv[y, x] - 0.08
        L.v = vc * (1 - w) + vv * w
        L.surco = np.zeros((n, n), dtype=bool)
    return L


def _lenticela(v, x, y, n):
    """Una lenticela: una raya corta y oscura de lado, con un brillo debajo."""
    largo = 2 + int((x * 7 + y * 3) % 5)
    for i in range(largo):
        xi, yi = int(x + i) % n, int(y) % n
        v[yi, xi] = 0.14 + 0.05 * (((xi * 5 + yi * 3) % 7) / 7)
        v[(yi + 1) % n, xi] += 0.05


def _placas(red, Dc, Dv, comun, rng):
    """Las placas del pino: cortes de traves (torcidos) entre cada par de surcos, a alturas distintas. Los cortes de las
    columnas de los bordes y los que caen cerca de arriba o de abajo salen de la semilla comun y van en las dos versiones;
    los demas, solo en la variante."""
    n = red.n
    X, Y = red.X, red.Y
    rb = np.random.default_rng(comun + 29)
    for k in range(len(red.xv)):
        k2 = (k + 1) % len(red.xv)
        borde = not (red.dentro[k] and red.dentro[k2])
        for c in range(int(rb.integers(2, 4))):
            y = rb.uniform(0, n)
            comun_ = borde or min(y, n - y) < 9
            if not comun_:
                # este corte es de la variante: su altura sale de la variante
                y = rng.uniform(10, n - 10)
            subida = (rb if comun_ else rng).uniform(-5, 5)
            for variante in ((False, True) if comun_ else (True,)):
                xk = (red.xv if variante else red.xs)
                x1, x2 = xk[k][int(y) % n], xk[k2][int(y + subida) % n]
                if x2 < x1:
                    x2 += n
                d = _segmento(X, Y, x1, y, x2, y + subida, n) / 0.9
                if variante:
                    Dv = np.minimum(Dv, d)
                else:
                    Dc = np.minimum(Dc, d)
    return Dc, Dv


def _voronoi_placas(comun, semilla, n=N, estira=0.42, banda=11, dentro=15):
    """Placas de pino irregulares (celdas de Voronoi estiradas a lo alto). Las semillas de la banda de los bordes salen de
    la semilla comun (y van en las dos versiones); las de dentro, de la variante. Devuelve la distancia al corte (en
    pixeles) en la version comun y en la de la variante, y el tono de cada placa."""
    rb = np.random.default_rng(comun + 41)
    rv = np.random.default_rng(semilla + 43)
    puntos_c = []
    while len(puntos_c) < 30:
        x, y = rb.uniform(0, n), rb.uniform(0, n)
        if min(x, n - x, y, n - y) < banda:
            puntos_c.append((x, y, rb.normal(0, 0.06)))
    puntos_v = []
    while len(puntos_v) < 7:
        x, y = rv.uniform(dentro, n - dentro), rv.uniform(dentro, n - dentro)
        puntos_v.append((x, y, rv.normal(0, 0.06)))
    Y, X = np.mgrid[0:n, 0:n] + 0.5

    def campo(puntos):
        d1 = np.full((n, n), 1e9)
        d2 = np.full((n, n), 1e9)
        tono = np.zeros((n, n))
        for (px, py, t) in puntos:
            dx = _envolver(X - px, n)
            dy = _envolver(Y - py, n) * estira
            d = np.sqrt(dx * dx + dy * dy)
            menor = d < d1
            d2 = np.where(menor, d1, np.minimum(d2, d))
            tono = np.where(menor, t, tono)
            d1 = np.where(menor, d, d1)
        return d2 - d1, tono
    Dc, tc = campo(puntos_c)
    Dv, tv = campo(puntos_c + puntos_v)
    return Dc, Dv, tc, tv


def _seca(D, grano, fino, base):
    """Madera muerta sin corteza: gris, con el grano en rayas finas y unas pocas rajas hondas."""
    h = _alto(D, plano=2.5)
    v = base + 0.16 * (grano - 0.5) + 0.04 * (fino - 0.5)
    v = v - 0.10 * (grano < 0.25)
    v = 0.10 + (v - 0.10) * (0.25 + 0.75 * h) + 0.06 * np.clip(P.luz_de(h) * 2, -1, 1)
    return np.where(D < 0.6, 0.06, v)


def _rajas(v, rng, n):
    """Rajas finas a lo largo y algun nudo en la madera muerta (solo dentro)."""
    v = v.copy()
    for _ in range(4):
        x = rng.uniform(10, n - 10)
        y0 = rng.uniform(9, n - 30)
        for j in range(int(rng.integers(10, 22))):
            y = int(y0 + j)
            v[y, int(x) % n] -= 0.14
            if rng.random() < 0.15:
                x += 1 if rng.random() < 0.5 else -1
    for _ in range(1):
        cx, cy = rng.uniform(16, n - 16), rng.uniform(16, n - 16)
        for dy in range(-5, 6):
            for dx in range(-3, 4):
                d = (dx / 2.6) ** 2 + (dy / 4.5) ** 2
                if d < 1:
                    x, y = int(cx + dx) % n, int(cy + dy) % n
                    v[y, x] = 0.12 if d < 0.4 else v[y, x] + 0.06
    return v


def testa(semilla, base=0.55, n=N):
    """El corte de un tronco: anillos alrededor de la medula, una grieta que sale del centro y el aro de corteza por
    fuera (`L.corteza`: la mascara de la corteza)."""
    rng = np.random.default_rng(semilla)
    L = Lienzo(n)
    L.piedra[:] = 0
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    cx, cy = n / 2 + rng.uniform(-3, 3), n / 2 + rng.uniform(-3, 3)
    w = ruido(semilla + 1, base=4, octavas=3)
    r = np.sqrt((xs - cx) ** 2 + (ys - cy) ** 2) + 2.2 * (w - 0.5)
    L.v = base + 0.07 * np.sin(r * 2 * np.pi / 3.4) + 0.05 * (ruido(semilla + 2, base=16, octavas=2) - 0.5)
    # mas claro hacia fuera (albura) y mas oscuro el corazon
    L.v += 0.06 * np.clip((r - 8) / 18, -1, 1)
    a = np.arctan2(ys - cy, xs - cx)
    ang = rng.uniform(-math.pi, math.pi)
    largo = rng.uniform(14, 22)
    grieta = (np.abs(np.angle(np.exp(1j * (a - ang)))) * np.maximum(r, 1) < 0.8) & (r > 2) & (r < largo)
    L.v = np.where(grieta, 0.14, L.v)
    L.v = np.where(r < 1.8, base - 0.18, L.v)
    # el aro de corteza: los pixeles de fuera, con surcos hacia dentro
    borde = np.minimum(np.minimum(xs, n - xs), np.minimum(ys, n - ys))
    dientes = 3.5 + 2.0 * ruido(semilla + 3, base=16, octavas=1)
    L.corteza = borde < dientes
    L.v = np.where(L.corteza, 0.30 + 0.35 * ruido(semilla + 4, base=16, octavas=2) - 0.2 * (borde < 1.2), L.v)
    L.v = np.where(~L.corteza & (borde < dientes + 1.2), L.v - 0.12, L.v)
    return L


# ------------------------------------------------------------------ hojas

def _color(rampa, l):
    return np.concatenate([rampa.pintar(np.array([[min(1.0, max(0.0, l))]]), 0.0)[0, 0], [255]])


def _hoja(img, cx, cy, ang, largo, ancho, rampa, luz, n=N, nervio=True, limite=0):
    """Una hoja (elipse girada) con la luz de arriba a la izquierda y el nervio; se repite sobre el toro. `limite`: no
    pinta a menos de esos pixeles de los bordes."""
    ca, sa = math.cos(ang), math.sin(ang)
    r = int(largo) + 1
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            u = dx * ca + dy * sa
            v = -dx * sa + dy * ca
            q = (u / largo) ** 2 + (v / ancho) ** 2
            if q <= 1.0:
                x, y = int(cx + dx) % n, int(cy + dy) % n
                if limite and min(x, n - 1 - x, y, n - 1 - y) < limite:
                    continue
                l = luz + 0.16 * (-(dx + dy) / (2 * r)) - 0.10 * (q > 0.7)
                if nervio and abs(v) < 0.5 and abs(u) < largo * 0.75:
                    l -= 0.08
                img[y, x] = _color(rampa, l)


def hojas(semilla, rampa, cuantas=520, tam=(3.2, 5.0), huecos=0.36, agujas=False, nieve=None, borde=None, n=N):
    """Una masa de hojas: el ruido dice donde hay hojas (por encima de `huecos`) y donde se ve el cielo; debajo de todo, la
    sombra honda entre hojas; encima, las hojas en tres capas (las de atras oscuras, las de delante claras) y cada racimo
    con la luz de su ladera. Las variantes casan entre si: el ruido de los bordes y las hojas que caen cerca de un borde
    salen de la semilla comun (`borde`); las de la variante, solo dentro (y nunca tocan los 2 pixeles de fuera).
    Devuelve rgba."""
    comun = semilla if borde is None else borde
    rb = np.random.default_rng(comun + 101)
    rv = np.random.default_rng(semilla + 103)
    img = np.zeros((n, n, 4))
    w = _peso(n)
    dens_c = 0.75 * ruido(comun + 1, base=4, octavas=3) + 0.25 * ruido(comun + 5, base=8, octavas=2)
    dens_v = 0.75 * ruido(semilla + 1, base=4, octavas=3) + 0.25 * ruido(semilla + 5, base=8, octavas=2)
    dens = dens_c * (1 - w) + dens_v * w
    luz_c = np.clip(P.luz_de(dens_c) * 9, -1, 1)
    luz_v = np.clip(P.luz_de(dens) * 9, -1, 1)
    masa = dens > huecos
    for y, x in zip(*np.nonzero(masa)):
        img[y, x] = _color(rampa, 0.05 + 0.08 * dens[y, x])
    margen = 15 if agujas else 9
    banda = 1 - ((n - 2 * margen) / n) ** 2
    capas = ((0.20, 0.42), (0.42, 0.36), (0.66, 0.22))
    for capa, (luz0, cuota) in enumerate(capas):
        for comunes in (True, False):
            r = rb if comunes else rv
            cuenta = int(cuantas * cuota * (banda if comunes else 1 - banda))
            puestas = intentos = 0
            while puestas < cuenta and intentos < cuenta * 8 + 8:
                intentos += 1
                if comunes:
                    x, y = r.uniform(0, n), r.uniform(0, n)
                    if min(x, n - x, y, n - y) >= margen:
                        continue
                    d, lz = dens_c[int(y), int(x)], luz_c[int(y), int(x)]
                else:
                    x, y = r.uniform(margen, n - margen), r.uniform(margen, n - margen)
                    d, lz = dens[int(y), int(x)], luz_v[int(y), int(x)]
                if d < huecos - 0.04 + 0.05 * capa:
                    continue
                puestas += 1
                luz = luz0 + r.uniform(-0.08, 0.08) + 0.16 * lz + 0.12 * (d - 0.5)
                lim = 0 if comunes else 2
                if agujas:
                    _ramita(img, x, y, r, rampa, luz, n, nieve if capa >= 1 else None, limite=lim)
                else:
                    _hoja(img, x, y, r.uniform(0, math.pi), r.uniform(*tam), r.uniform(1.5, 2.4), rampa, luz, n, limite=lim)
    if nieve is not None:
        # y en el pixel de hoja que no tiene nada encima (el azar, comun: asi casan los bordes)
        al = img[..., 3] > 0
        arriba = al & ~np.roll(al, 1, axis=0) & ~np.roll(al, 2, axis=0)
        azar = np.random.default_rng(comun + 107).random((n, n))
        azar2 = np.random.default_rng(comun + 109).random((n, n))
        for y, x in zip(*np.nonzero(arriba)):
            if azar[y, x] < 0.65:
                img[y, x] = _color(nieve, 0.8 + 0.2 * (azar2[y, x] - 0.5))
    return img


def _ramita(img, x, y, rng, rampa, luz, n, nieve=None, limite=0):
    """Una ramita de pino: un tallo que cae hacia fuera con agujas a los dos lados (espina de pez), las puntas mas claras.
    Con `nieve`, las agujas de encima del tallo llevan nieve."""
    def poner(px, py, c):
        xi, yi = int(px) % n, int(py) % n
        if limite and min(xi, n - 1 - xi, yi, n - 1 - yi) < limite:
            return
        img[yi, xi] = c
    sentido = 1 if rng.random() < 0.5 else -1
    ang = math.pi / 2 - sentido * rng.uniform(0.9, 1.35)
    largo = rng.uniform(7, 13)
    ca, sa = math.cos(ang), math.sin(ang)
    pasos = int(largo * 2)
    for i in range(pasos):
        t = i / pasos
        px, py = x + ca * largo * t, y + sa * largo * t
        poner(px, py, _color(rampa, luz - 0.18))
        if i % 2:
            continue
        for lado in (-1, 1):
            al = 3.2 * (1 - 0.55 * t)
            # las agujas salen hacia delante y un poco hacia abajo
            nx, ny = -sa * lado, ca * lado
            arriba = ny < 0
            for k in range(1, int(al) + 1):
                qx = px + (nx * 0.85 + ca * 0.55) * k
                qy = py + (ny * 0.85 + sa * 0.55) * k + 0.25 * k
                l = luz + 0.10 * (k / al) + (0.06 if arriba else -0.06)
                c = _color(rampa, l)
                if nieve is not None and arriba and t < 0.8 and rng.random() < 0.8:
                    c = _color(nieve, 0.62 + 0.3 * (k / al) + rng.uniform(-0.06, 0.06))
                poner(qx, qy, c)


def hojarasca(semilla, rampas, cuantas=150, agujas=False, n=N):
    """Hojas caidas sueltas (o agujas), cada una de un color de las rampas dadas; con huecos donde se ve el suelo."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    dens = ruido(semilla + 1, base=4, octavas=2)
    for _ in range(cuantas):
        x, y = rng.uniform(0, n), rng.uniform(0, n)
        if dens[int(y), int(x)] < 0.25 + 0.3 * rng.random():
            continue
        rp = rampas[int(rng.integers(0, len(rampas)))]
        luz = rng.uniform(0.3, 0.75)
        if agujas:
            a = rng.uniform(0, math.pi)
            largo = rng.uniform(3, 6)
            for t in np.linspace(0, 1, int(largo * 2)):
                img[int(y + math.sin(a) * largo * t) % n, int(x + math.cos(a) * largo * t) % n] = _color(rp, luz + 0.1 * t)
        else:
            _hoja(img, x, y, rng.uniform(0, math.pi), rng.uniform(2.6, 4.2), rng.uniform(1.4, 2.2), rp, luz, n)
    return img


def colgante(semilla, rampa, punta=False, n=N):
    """Mechones de musgo que cuelgan, ondulados, mas claros arriba. El de en medio se repite de arriba abajo (las hebras
    siguen); la punta acaba (hebras de distinto largo, alguna se queda arriba)."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    cuantas = 16
    for k in range(cuantas):
        x0 = (k + rng.uniform(0.2, 0.8)) * n / cuantas
        amp = rng.uniform(0.8, 2.2)
        fase = rng.uniform(0, 2 * math.pi)
        vueltas = int(rng.integers(1, 3))
        largo = n if not punta else rng.uniform(14, n - 4)
        grueso = 2 if rng.random() < 0.45 else 1
        tono = rng.uniform(-0.08, 0.08)
        for y in range(int(largo)):
            xx = x0 + amp * math.sin(2 * math.pi * vueltas * y / n + fase)
            l = 0.62 + tono - 0.25 * (y / n) + 0.10 * math.sin(y * 0.9 + fase)
            if punta:
                l -= 0.15 * (y / largo)
            g = grueso if (not punta or y < largo - 6) else 1
            for a in range(g):
                img[y, int(xx + a) % n] = _color(rampa, l - 0.08 * a)
        # hebras sueltas a los lados
        for _ in range(4):
            y0 = int(rng.uniform(0, largo * 0.85))
            lado = 1 if rng.random() < 0.5 else -1
            for j in range(int(rng.integers(3, 7))):
                yi = y0 + j
                if yi < (n if not punta else largo):
                    xi = int(x0 + amp * math.sin(2 * math.pi * vueltas * yi / n + fase) + lado * (1 + j * 0.5))
                    img[yi % n, xi % n] = _color(rampa, 0.38 + tono)
    return img


def liana(semilla, tallo, hoja, flor=None, n=N):
    """Tallos que suben serpenteando (se repiten arriba y abajo) con hojitas a los lados y alguna flor."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    tallos = 3
    for k in range(tallos):
        x = (k + rng.uniform(0.25, 0.75)) * n / tallos
        fase = rng.uniform(0, 2 * math.pi)
        amp = rng.uniform(3, 6)
        for y in range(n):
            xx = int(x + amp * math.sin(2 * math.pi * y / n + fase)) % n
            img[y, xx] = _color(tallo, 0.5)
            img[y, (xx + 1) % n] = _color(tallo, 0.3)
        for y in range(0, n, 3):
            if rng.random() < 0.75:
                xx = x + amp * math.sin(2 * math.pi * y / n + fase)
                lado = 1 if (y // 3) % 2 else -1
                _hoja(img, xx + lado * 3.2, y + rng.uniform(-1, 1), rng.uniform(-0.5, 0.5) + (0 if lado > 0 else math.pi),
                      rng.uniform(2.6, 3.8), rng.uniform(1.4, 2.0), hoja, rng.uniform(0.35, 0.8), n)
            if flor is not None and rng.random() < 0.06:
                xx = int(x + amp * math.sin(2 * math.pi * y / n + fase))
                for (dx, dy) in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                    img[(y + dy) % n, (xx + 2 + dx) % n] = _color(flor, 0.85 if dx == dy == 0 else 0.55)
    return img
