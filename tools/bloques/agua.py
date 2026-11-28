"""El agua propia (parte 8j): las texturas del agua del juego cambiadas por las del mod (el agua sigue siendo la del juego —asi
los bloques se pueden anegar y todo lo de nadar, ahogarse y el terreno sigue igual—, pero se ve nuestra): mas oscura, con
ondas que se cruzan, brillos de espuma y, en el agua que cae, chorros de espuma. En gris: el bioma la tine (cada region de
Morvain tiene su color de agua). Tambien los dibujos de las burbujas, salpicaduras y gotas.

- quieta(): 32 fotogramas de 64 x 64 que se repiten sin salto (las ondas avanzan un numero entero de vueltas).
- fluye(): 32 fotogramas de 128 x 128 (el juego coge la mitad: como su textura de 32 para la de 16) con vetas que bajan
  y espuma.
- encima(): la textura de agua vista a traves de otras cosas (water_overlay) y la de debajo del agua (misc/underwater).
- burbuja(), salpicadura(k), gota(tipo), burbuja_rota(k): los dibujos de las particulas del agua (16 x 16).
"""
import math

import numpy as np

import pintor as P
from madera import ruido_estirado
from pintor import ruido

N = 64


def _ondas(n, t, fotogramas, ondas):
    """Suma de ondas que se repiten sobre el toro (k enteros) y avanzan un numero entero de vueltas en la animacion."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    total = np.zeros((n, n))
    for (kx, ky, vueltas, amp, fase) in ondas:
        total += amp * np.sin(2 * math.pi * ((kx * xs + ky * ys) / n - vueltas * t / fotogramas) + fase)
    return total


def quieta(semilla, fotogramas=32, n=N):
    rng = np.random.default_rng(semilla)
    ondas = [(1, 2, 1, 1.0, rng.uniform(0, 6.28)), (-2, 1, 1, 0.8, rng.uniform(0, 6.28)), (3, -1, 2, 0.5, rng.uniform(0, 6.28)),
             (1, -3, 1, 0.45, rng.uniform(0, 6.28)), (4, 3, 2, 0.25, rng.uniform(0, 6.28))]
    ruido_fijo = ruido(semilla + 1, base=8, octavas=2)
    motas = [(rng.uniform(0, n), rng.uniform(0, n), int(rng.integers(1, 3)), rng.uniform(0, 6.28)) for _ in range(10)]
    tira = np.zeros((fotogramas * n, n, 4))
    for t in range(fotogramas):
        w = _ondas(n, t, fotogramas, ondas)
        # crestas: las curvas donde la suma de ondas vale lo mismo (lineas claras que se mueven y se cruzan, como
        # causticas), y la sombra de los valles
        cresta = np.clip(1 - np.abs(w - 1.1) / 0.32, 0, 1) + 0.6 * np.clip(1 - np.abs(w + 0.4) / 0.22, 0, 1)
        cresta = np.clip(cresta, 0, 1)
        valle = np.clip((-w - 1.3) / 1.0, 0, 1)
        v = 0.56 + 0.26 * cresta - 0.12 * valle + 0.06 * (ruido_fijo - 0.5)
        g = (P.bayer((n, n)) * 0.06 + v) * 255
        img = np.zeros((n, n, 4))
        img[..., 0] = img[..., 1] = img[..., 2] = np.round(np.clip(g, 0, 255) / 12) * 12
        img[..., 3] = 168 + 40 * cresta
        # brillos de espuma que van y vienen
        for (mx, my, vel, fase) in motas:
            if math.sin(2 * math.pi * vel * t / fotogramas + fase) > 0.55:
                x, y = int(mx + 3 * math.sin(2 * math.pi * t / fotogramas + fase)) % n, int(my) % n
                img[y, x, :3] = 245
                img[y, x, 3] = 230
        tira[t * n:(t + 1) * n] = img
    return tira


def fluye(semilla, fotogramas=32, n=2 * N):
    """El agua que corre: vetas a lo largo (hacia abajo de la textura) que bajan 4 pixeles por fotograma, y espuma."""
    vetas = ruido_estirado(semilla, 16, 2, octavas=3, n=n)
    espuma_r = ruido_estirado(semilla + 1, 32, 2, octavas=2, n=n)
    fino = ruido(semilla + 2, base=16, octavas=2, n=n)
    tira = np.zeros((fotogramas * n, n, 4))
    for t in range(fotogramas):
        d = int(round(t * n / fotogramas)) % n
        V = np.roll(vetas, d, axis=0)
        E = np.roll(espuma_r, d, axis=0)
        v = 0.52 + 0.24 * (V - 0.5) + 0.06 * (fino - 0.5)
        espuma = E > 0.74
        g = np.where(espuma, 0.80 + 0.17 * (E - 0.74) / 0.26, v)
        g = (P.bayer((n, n)) * 0.06 + g) * 255
        img = np.zeros((n, n, 4))
        img[..., 0] = img[..., 1] = img[..., 2] = np.round(np.clip(g, 0, 255) / 12) * 12
        img[..., 3] = np.where(espuma, 225, 180)
        tira[t * n:(t + 1) * n] = img
    return tira


def encima(semilla, n=N):
    """water_overlay (la cara del agua vista a traves de otras cosas): gris con algo de onda."""
    v = 0.5 + 0.12 * (ruido(semilla, base=4, octavas=3) - 0.5)
    img = np.zeros((n, n, 4))
    img[..., 0] = img[..., 1] = img[..., 2] = np.round(v * 255 / 12) * 12
    img[..., 3] = 170
    return img


def debajo(semilla, n=N):
    """misc/underwater: lo que se pinta sobre la pantalla bajo el agua (el juego lo mueve al moverse): manchas de luz
    tenues, como causticas."""
    w = _ondas(n, 0, 1, [(1, 2, 0, 1.0, 0.3), (-2, 1, 0, 0.8, 1.7), (3, -1, 0, 0.5, 2.2)])
    v = 0.35 + 0.35 * np.clip((w - 0.6) / 1.4, 0, 1)
    img = np.zeros((n, n, 4))
    img[..., 0] = img[..., 1] = img[..., 2] = v * 255
    img[..., 3] = 60 + 40 * np.clip((w - 0.6) / 1.4, 0, 1)
    return img


def burbuja(n=16):
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    r = np.hypot(xs - n / 2, ys - n / 2)
    img = np.zeros((n, n, 4))
    aro = (r > 4.0) & (r < 6.0)
    img[aro] = (170, 210, 235, 230)
    dentro = r <= 4.0
    img[dentro] = (120, 170, 210, 70)
    brillo = np.hypot(xs - n / 2 + 2, ys - n / 2 + 2) < 1.5
    img[brillo] = (245, 250, 255, 255)
    return img


def burbuja_rota(k, n=16):
    """La burbuja que estalla (0-4): el aro se abre en gotitas que se alejan."""
    img = np.zeros((n, n, 4))
    r = 4.5 + 1.2 * k
    for i in range(8):
        a = i * math.pi / 4 + 0.3
        x, y = int(n / 2 + r * math.cos(a)), int(n / 2 + r * math.sin(a))
        if 0 <= x < n and 0 <= y < n:
            img[y, x] = (200, 225, 245, 255 - 35 * k)
            if k < 2 and 0 <= x + 1 < n:
                img[y, x + 1] = (170, 210, 235, 200)
    return img


def salpicadura(k, n=16):
    """Gotas que saltan (0-3, de juntas a separadas)."""
    rng = np.random.default_rng(40 + k)
    img = np.zeros((n, n, 4))
    for i in range(4 + k):
        a = rng.uniform(-math.pi, 0)
        d = 1.5 + 1.6 * k + rng.uniform(0, 1.5)
        x, y = int(n / 2 + d * math.cos(a)), int(n / 2 + 3 + d * math.sin(a))
        for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1)) if k < 2 else ((0, 0),):
            if 0 <= x + dx < n and 0 <= y + dy < n:
                img[y + dy, x + dx] = (215, 232, 245, 255)
    return img


def gota(tipo, n=16):
    """Una gota: `hang` colgando (alargada), `fall` cayendo (redonda) y `land` aplastada en el suelo."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    img = np.zeros((n, n, 4))
    if tipo == "hang":
        m = ((xs - n / 2) / 2.5) ** 2 + ((ys - n / 2 - 1) / 4.0) ** 2 < 1
    elif tipo == "fall":
        m = np.hypot(xs - n / 2, ys - n / 2) < 2.6
    else:
        m = ((xs - n / 2) / 5.0) ** 2 + ((ys - n / 2 - 3) / 1.5) ** 2 < 1
    img[m] = (205, 225, 240, 240)
    brillo = m & (np.hypot(xs - n / 2 + 1, ys - n / 2 + (1 if tipo != "land" else -2)) < 1.0)
    img[brillo] = (250, 252, 255, 255)
    return img
