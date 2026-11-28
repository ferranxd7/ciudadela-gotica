"""El pintor de la decoracion (parte 8i), a 64 x 64: estandartes, tapices, alfombras, hueso y calaveras, telaranas y
lapidas.

- estandarte(): un pano de dos bloques de alto (devuelve la mitad de arriba y la de abajo): campo de color con pliegues, una
  cenefa dorada, un emblema en medio (corona, ojo, llama, calavera) y la punta de abajo en cola de golondrina; `roto`:
  desgarrado y con agujeros.
- tapiz(): un damasco que se repite (un motivo de flor de lis cada 32 pixeles, con su trama).
- alfombra(): rombos con ribetes que se repiten (para pasillos largos).
- hueso(), calavera_cara(): hueso amarillento con grietas; la cara de una calavera (cuencas, nariz, dientes).
- telarana(): hilos radiales y la espiral (con transparencia).
- lapida(): piedra gastada con una cruz grabada, lineas de letras y liquen.
"""
import math

import numpy as np

import pintor as P
from pintor import N, ruido


def _tela(semilla, n=N):
    """La trama de una tela (hilos que se cruzan) y unos pliegues verticales suaves."""
    ys, xs = np.mgrid[0:n, 0:n]
    trama = ((xs + ys) % 2 == 0) * 0.04 + ((xs % 4) == 0) * 0.02
    pliegue = 0.10 * np.sin(xs / n * 2 * math.pi * 2 + 0.6) + 0.04 * np.sin(xs / n * 2 * math.pi * 5)
    return trama + pliegue + 0.04 * (ruido(semilla, base=16, octavas=2) - 0.5)


def _emblema(tipo, xs, ys, cx, cy, s):
    """Una mascara del emblema (en coordenadas del pano entero) y su relleno interior (para sombrearlo)."""
    u, v = (xs - cx) / s, (ys - cy) / s
    if tipo == "corona":
        base = (np.abs(u) < 1.0) & (v > 0.2) & (v < 0.6)
        picos = (np.abs(u) < 1.0) & (v <= 0.2) & (v > -0.7) & (np.abs(((u + 1) * 2) % 1 - 0.5) * 2 < (v + 0.7) / 0.9 * 1.0)
        joyas = (np.hypot(u, v - 0.4) < 0.12) | (np.hypot(u - 0.6, v - 0.4) < 0.1) | (np.hypot(u + 0.6, v - 0.4) < 0.1)
        return base | picos, joyas
    if tipo == "ojo":
        ojo = (u / 1.1) ** 2 + (v / 0.55) ** 2 < 1
        pupila = np.hypot(u, v) < 0.32
        aro = (np.abs(np.hypot(u, v) - 1.25) < 0.08) & ~ojo
        rayos = (np.abs(np.hypot(u, v) - 1.5) < 0.25) & (np.abs(np.sin(np.arctan2(v, u) * 6)) > 0.85)
        return ojo | aro | rayos, pupila
    if tipo == "llama":
        def gota(esc):
            uu, vv = u / esc, (v - 0.45) / esc + 0.45
            w = np.where(vv < 0.45, 0.6 * np.clip((vv + 1.2) / 1.65, 0, 1) ** 0.9, np.sqrt(np.clip(0.36 - (vv - 0.45) ** 2, 0, None)))
            return np.abs(uu - 0.12 * np.sin((vv - 0.45) * 3) * (vv < 0.45)) < w
        return gota(1.0), gota(0.5)
    if tipo == "calavera":
        craneo = (u / 0.9) ** 2 + ((v + 0.15) / 0.8) ** 2 < 1
        mandibula = (np.abs(u) < 0.5) & (v > 0.5) & (v < 1.0)
        cuencas = (np.hypot(u - 0.38, v + 0.05) < 0.22) | (np.hypot(u + 0.38, v + 0.05) < 0.22)
        nariz = (np.abs(u) < 0.1 + 0.1 * (v - 0.25) / 0.2) & (v > 0.2) & (v < 0.45)
        dientes = mandibula & ((np.floor((u + 0.5) * 6) % 2) == 0) & (v > 0.7)
        return (craneo | mandibula) & ~cuencas & ~nariz, dientes
    return np.zeros_like(xs, dtype=bool), np.zeros_like(xs, dtype=bool)


def estandarte(semilla, campo, cenefa, emblema_rampa, tipo, roto=False, n=N):
    """Devuelve (arriba, abajo): las dos mitades rgba de un pano de 64 x 128 (cola de golondrina abajo)."""
    rng = np.random.default_rng(semilla)
    H = 2 * n
    ys, xs = np.mgrid[0:H, 0:n] + 0.5
    tela = np.concatenate([_tela(semilla, n), _tela(semilla + 1, n)], axis=0)
    v = 0.5 + tela
    # sombra arriba (bajo la barra) y abajo
    v -= 0.10 * np.clip(1 - ys / 10, 0, 1)
    alfa = np.ones((H, n))
    # cola de golondrina: el pico en medio sube 18 pixeles desde abajo
    corte = H - 18 * (1 - np.abs(xs - n / 2) / (n / 2))
    alfa = np.where(ys > corte, 0, alfa)
    # cenefa dorada a los lados y siguiendo la cola, y una banda arriba
    borde = (xs < 4) | (xs > n - 4) | ((ys > corte - 4) & (ys <= corte)) | ((ys > 6) & (ys < 10))
    mask_e, dentro = _emblema(tipo, xs, ys, n / 2, H * 0.45, 17)
    rgb = campo.pintar(np.clip(v, 0, 1), 0.6)
    rgb = np.where(borde[..., None], cenefa.pintar(np.clip(v + 0.1, 0, 1), 0.6), rgb)
    rgb = np.where(mask_e[..., None], emblema_rampa.pintar(np.clip(v + 0.2, 0, 1), 0.6), rgb)
    rgb = np.where((dentro & mask_e)[..., None] | dentro[..., None], campo.pintar(np.clip(v - 0.25, 0, 1), 0.6), rgb)
    if roto:
        # desgarros: agujeros y el borde de abajo comido
        r = ruido(semilla + 5, base=4, octavas=4)
        r = np.concatenate([r, ruido(semilla + 6, base=4, octavas=4)], axis=0)
        agujero = (r > 0.78) & (ys > 20)
        comido = ys > corte - 22 * np.clip(r * 1.6 - 0.4, 0, 1)
        alfa = np.where(agujero | comido, 0, alfa)
        rgb = rgb * 0.75 + 12
    a = (alfa * 255)[..., None]
    img = np.concatenate([rgb, a], axis=2)
    return img[:n], img[n:]


def tapiz(semilla, fondo, dibujo, n=N):
    """Un damasco: un motivo (flor de lis estilizada) cada 32 pixeles, al tresbolillo, sobre la trama."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    v = 0.42 + _tela(semilla, n) * 0.6
    m = np.zeros((n, n), dtype=bool)
    for (ox, oy) in ((16, 16), (48, 48), (48, 16), (16, 48)):
        alt = (ox == oy)
        for dx in (-n, 0, n):
            for dy in (-n, 0, n):
                u, w = (xs - ox - dx) / 7.0, (ys - oy - dy) / 7.0
                if alt:
                    petalo_c = (np.abs(u) < 0.35 * np.clip(1 - (w + 1.2) / 2.0, 0, 1) + 0.12) & (w > -1.3) & (w < 0.6)
                    petalo_l = (np.hypot(u - 0.65, w + 0.1) < 0.55) & ~(np.hypot(u - 0.95, w - 0.25) < 0.4)
                    petalo_r = (np.hypot(u + 0.65, w + 0.1) < 0.55) & ~(np.hypot(u + 0.95, w - 0.25) < 0.4)
                    banda = (np.abs(u) < 0.75) & (np.abs(w - 0.25) < 0.12)
                    pie = (np.abs(u) < 0.3) & (w > 0.3) & (w < 1.0)
                    m |= petalo_c | petalo_l | petalo_r | banda | pie
                else:
                    m |= (np.abs(u) + np.abs(w) < 0.55) & ~(np.abs(u) + np.abs(w) < 0.28)
    rgb = fondo.pintar(np.clip(v, 0, 1), 0.6)
    rgb = np.where(m[..., None], dibujo.pintar(np.clip(v + 0.15, 0, 1), 0.6), rgb)
    return rgb


def alfombra(semilla, fondo, dibujo, ribete, n=N):
    """Rombos con ribetes que se repiten (32 x 32), con su trama de nudos y algo de desgaste."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    nudos = ((xs.astype(int) % 2) ^ (ys.astype(int) % 2)) * 0.05
    v = 0.45 + nudos + 0.08 * (ruido(semilla, base=8, octavas=2) - 0.5)
    u = np.abs((xs % 32) - 16)
    w = np.abs((ys % 32) - 16)
    d = u + w
    rombo = d < 12
    filo = np.abs(d - 12) < 1.5
    centro = d < 4
    rgb = fondo.pintar(np.clip(v, 0, 1), 0.6)
    rgb = np.where(rombo[..., None], dibujo.pintar(np.clip(v, 0, 1), 0.6), rgb)
    rgb = np.where(filo[..., None], ribete.pintar(np.clip(v + 0.1, 0, 1), 0.6), rgb)
    rgb = np.where(centro[..., None], ribete.pintar(np.clip(v - 0.1, 0, 1), 0.6), rgb)
    desgaste = ruido(semilla + 3, base=4, octavas=3) > 0.72
    rgb = np.where(desgaste[..., None], rgb * 0.82 + 20, rgb)
    return rgb


def hueso(semilla, rampa, n=N):
    rng = np.random.default_rng(semilla)
    v = 0.6 + 0.10 * (ruido(semilla, base=8, octavas=3) - 0.5) + 0.05 * (ruido(semilla + 1, base=32, octavas=1) - 0.5)
    for _ in range(8):
        x, y = rng.uniform(0, n), rng.uniform(0, n)
        a = rng.uniform(0, 2 * math.pi)
        for t in range(int(rng.integers(4, 12))):
            v[int(y + math.sin(a) * t) % n, int(x + math.cos(a) * t) % n] -= 0.2
            a += rng.uniform(-0.4, 0.4)
    for _ in range(30):
        v[int(rng.uniform(0, n)), int(rng.uniform(0, n))] -= 0.12
    return rampa.pintar(np.clip(v, 0, 1), 0.6)


def calavera_cara(semilla, rampa, n=N):
    """La cara de una calavera (el frente del craneo del modelo): cuencas hondas, la nariz y la fila de dientes."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    v = 0.6 + 0.08 * (ruido(semilla, base=8, octavas=3) - 0.5)
    for cx in (20, 44):
        r = np.hypot((xs - cx) / 10, (ys - 26) / 11)
        v = np.where(r < 1, 0.05 + 0.1 * r, v)
        v = np.where((r >= 1) & (r < 1.2), v - 0.15, v)
    nariz = (np.abs(xs - 32) < 1 + 4 * (ys - 36) / 10) & (ys > 36) & (ys < 46)
    v = np.where(nariz, 0.08, v)
    dientes = (ys > 52) & (ys < 62) & (np.abs(xs - 32) < 18)
    v = np.where(dientes, 0.72 + 0.1 * np.cos((xs - 32) / 6 * math.pi), v)
    v = np.where(dientes & ((np.floor(xs) % 6) < 1), 0.15, v)
    v = np.where((ys > 50) & (ys < 52) & (np.abs(xs - 32) < 18), 0.2, v)
    return rampa.pintar(np.clip(v, 0, 1), 0.5)


def telarana(semilla, n=N):
    """Una telarana: radios desde una esquina y desde el centro, hilos en espiral, con transparencia."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    cx, cy = n / 2 + rng.uniform(-4, 4), n / 2 + rng.uniform(-4, 4)
    angs = np.sort(rng.uniform(0, 2 * math.pi, 9))
    for a in angs:
        for t in np.linspace(0, n, n * 2):
            x, y = int(cx + math.cos(a) * t), int(cy + math.sin(a) * t)
            if 0 <= x < n and 0 <= y < n:
                img[y, x] = (210, 210, 205, 200)
    for r in np.arange(5, 40, 6.0):
        for k in range(len(angs)):
            a0, a1 = angs[k], angs[(k + 1) % len(angs)] + (2 * math.pi if k == len(angs) - 1 else 0)
            rr0 = r * rng.uniform(0.85, 1.1)
            for t in np.linspace(0, 1, 30):
                a = a0 + (a1 - a0) * t
                rr = rr0 * (0.86 + 0.14 * math.cos((t - 0.5) * math.pi))
                x, y = int(cx + math.cos(a) * rr), int(cy + math.sin(a) * rr)
                if 0 <= x < n and 0 <= y < n and rng.random() < 0.92:
                    img[y, x] = (225, 225, 220, 150)
    return img


def lapida(semilla, rampa, liquen, n=N):
    """Piedra gastada con una cruz grabada arriba, tres lineas de letras y liquen."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    v = 0.55 + 0.10 * (ruido(semilla, base=8, octavas=3) - 0.5)
    h = np.zeros((n, n))
    h = np.where((np.abs(xs - 32) < 2.5) & (ys > 8) & (ys < 30), -1, h)
    h = np.where((np.abs(ys - 15) < 2.5) & (np.abs(xs - 32) < 9), -1, h)
    for k, y0 in enumerate((38, 45, 52)):
        x = 14
        while x < 50:
            w = int(rng.integers(2, 5))
            h[y0:y0 + 4, x:x + w] = np.where(rng.random((4, w)) < 0.6, -1, 0)
            x += w + int(rng.integers(1, 3))
    rel = P.relieve(h)
    v = v + 0.25 * rel + 0.12 * h
    rgb = rampa.pintar(np.clip(v, 0, 1), 0.6)
    m = ruido(semilla + 3, base=4, octavas=4) > 0.66
    return np.where(m[..., None], liquen.pintar(np.clip(v, 0, 1), 0.6), rgb)
