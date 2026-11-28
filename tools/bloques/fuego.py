"""El pintor del fuego (parte 8g): llamas animadas de 64 x 64 por fotograma que se repiten sin salto (el ruido se repite
de arriba abajo y sube un numero entero de vueltas en toda la animacion; cada lengua parpadea un numero entero de veces),
la llamita de las velas y los faroles, y los dibujos de las particulas (llama, chispa y humo).

- llamas(semilla, rampa, fotogramas=32): lenguas de fuego que suben desde abajo, ondean y parpadean, con chispas que
  suben. Devuelve rgba (fotogramas * 64, 64, 4): la tira de fotogramas (con su .mcmeta).
- llamita(semilla, rampa, fotogramas=16): una llama sola en medio (gota), para las velas.
- particulas: sprites pequenos (16 x 16) de llama (4), chispa (2) y humo (4).
"""
import math

import numpy as np

from pintor import ruido

N = 64


def _ruido_t(semilla, n=N, base=4, octavas=3):
    return ruido(semilla, base=base, octavas=octavas, n=n)


def llamas(semilla, rampa, fotogramas=32, n=N, alto=0.95, lenguas=3, chispas=10, vueltas=2):
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    y_rel = (n - ys) / n  # 0 abajo, 1 arriba
    turb = _ruido_t(semilla + 1, base=4, octavas=3)
    turb2 = _ruido_t(semilla + 2, base=8, octavas=2)
    cuerpo = []
    for k in range(lenguas):
        cuerpo.append(dict(
            cx=(k + rng.uniform(0.3, 0.7)) * n / lenguas,
            ancho=rng.uniform(0.85, 1.25) * n / lenguas * 0.75,
            alto=rng.uniform(0.75, 1.0) * alto,
            parpadeo=int(rng.integers(1, 4)),
            fase=rng.uniform(0, 2 * math.pi),
            ola=rng.uniform(2.0, 4.5),
            olas=int(rng.integers(1, 3)),
        ))
    # lenguas pequenas entre las grandes
    for k in range(lenguas):
        cuerpo.append(dict(
            cx=(k + 1.0) * n / lenguas + rng.uniform(-2, 2),
            ancho=rng.uniform(0.35, 0.55) * n / lenguas,
            alto=rng.uniform(0.45, 0.7) * alto,
            parpadeo=int(rng.integers(2, 5)),
            fase=rng.uniform(0, 2 * math.pi),
            ola=rng.uniform(1.0, 2.5),
            olas=int(rng.integers(1, 4)),
        ))
    motas = [dict(x=rng.uniform(4, n - 4), y0=rng.uniform(0, n), vel=int(rng.integers(1, 3)), fase=rng.uniform(0, 6.28))
             for _ in range(chispas)]
    tira = np.zeros((fotogramas * n, n, 4))
    for t in range(fotogramas):
        f = t / fotogramas
        # el ruido sube: se lee mas abajo cada fotograma (vueltas enteras en toda la animacion)
        off = int(round(f * n * vueltas)) % n
        T = np.roll(turb, -off, axis=0)
        T2 = np.roll(turb2, -(2 * off) % n, axis=0)
        intens = np.zeros((n, n))
        for L in cuerpo:
            h = L["alto"] * (0.80 + 0.20 * math.sin(2 * math.pi * L["parpadeo"] * f + L["fase"]))
            sway = L["ola"] * np.sin(2 * math.pi * (L["olas"] * f + y_rel * 1.1) + L["fase"]) * y_rel
            dx = np.abs(xs - L["cx"] - sway)
            dx = np.minimum(dx, n - dx)
            perfil = np.clip(1 - y_rel / h, 0, 1) ** 0.6
            e = np.clip(1 - dx / (L["ancho"] * perfil + 1e-6), 0, 1) ** 0.7 * (y_rel < h)
            intens = np.maximum(intens, e * (0.6 + 0.4 * np.clip(1 - y_rel / h, 0, 1)))
        # el cuerpo del fuego: abajo casi todo lleno, con su borde que sube y baja
        base = 0.30 + 0.06 * np.sin(2 * math.pi * (2 * f + xs / n * 2)) + 0.05 * np.sin(2 * math.pi * (3 * f - xs / n * 3))
        intens = np.maximum(intens, np.clip(1 - y_rel / base, 0, 1) ** 0.5 * 0.95)
        v = intens * (0.50 + 0.65 * T + 0.25 * (T2 - 0.5))
        v = v - 0.10 * y_rel
        m = v > 0.24
        luz = np.clip((v - 0.24) / 0.7 + 0.25 * (1 - y_rel) ** 3, 0, 1)
        img = np.zeros((n, n, 4))
        col = rampa.pintar(luz, 0.8)
        img[..., :3] = col
        img[..., 3] = np.where(m, 255, 0)
        # chispas que suben
        for c in motas:
            y = (c["y0"] - f * n * c["vel"]) % n
            if y > n * 0.6:
                continue
            x = c["x"] + 2.5 * math.sin(2 * math.pi * f * c["vel"] + c["fase"])
            xi, yi = int(x) % n, int(y) % n
            if img[yi, xi, 3] == 0:
                img[yi, xi, :3] = rampa.pintar(np.array([[0.95]]), 0.0)[0, 0]
                img[yi, xi, 3] = 255
        tira[t * n:(t + 1) * n] = img
    return tira


def llamita(semilla, rampa, fotogramas=16, n=N):
    """Una llama sola (gota) en medio del dibujo: abajo el pabilo azulado, el cuerpo y la punta que oscila."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    tira = np.zeros((fotogramas * n, n, 4))
    turb = _ruido_t(semilla + 1, base=4, octavas=2)
    for t in range(fotogramas):
        f = t / fotogramas
        alto = 0.78 + 0.08 * math.sin(2 * math.pi * f * 2) + 0.04 * math.sin(2 * math.pi * f * 3 + 1.0)
        sway = 2.5 * math.sin(2 * math.pi * f + 0.5)
        y_rel = (n - 4 - ys) / (n - 8)  # 0 en la base de la llama
        cy = 0.28
        # gota: ancha abajo (radio 0.22) y en punta arriba
        dx = xs - n / 2 - sway * np.clip(y_rel, 0, 1) ** 1.5
        ancho = np.where(y_rel < cy, np.sqrt(np.clip(1 - ((y_rel - cy) / cy) ** 2, 0, 1)) * 0.2 * n,
                         0.2 * n * np.clip(1 - (y_rel - cy) / (alto - cy), 0, 1) ** 0.9)
        e = np.clip(1 - np.abs(dx) / (ancho + 1e-6), 0, 1) * (y_rel > 0) * (y_rel < alto)
        T = np.roll(turb, -int(round(f * n)) % n, axis=0)
        v = e * (0.85 + 0.25 * (T - 0.5))
        core = np.clip(1 - np.hypot(dx / (0.08 * n), (y_rel - 0.3) / 0.22), 0, 1)
        luz = np.clip(0.25 + 0.6 * v + 0.5 * core, 0, 1)
        m = v > 0.12
        img = np.zeros((n, n, 4))
        img[..., :3] = rampa.pintar(luz, 0.6)
        img[..., 3] = np.where(m, 255, 0)
        tira[t * n:(t + 1) * n] = img
    return tira


def sprite_llama(semilla, rampa, k, n=16):
    """El dibujo de una particula de llama (k: 0-3, de grande a pequena)."""
    rng = np.random.default_rng(semilla + k)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    r = (6.5 - 1.3 * k)
    d = np.hypot((xs - n / 2) / r, (ys - n / 2 - 1) / (r * 1.25))
    tur = rng.random((n, n))
    v = np.clip(1 - d, 0, 1) * (0.8 + 0.3 * tur)
    img = np.zeros((n, n, 4))
    img[..., :3] = rampa.pintar(np.clip(0.2 + 0.9 * v, 0, 1), 0.5)
    img[..., 3] = np.where(v > 0.12, 255, 0)
    return img


def sprite_chispa(rampa, k, n=16):
    img = np.zeros((n, n, 4))
    c = n // 2
    tam = 2 - k
    for dy in range(-tam, tam + 1):
        for dx in range(-tam, tam + 1):
            if abs(dx) + abs(dy) <= tam:
                img[c + dy, c + dx, :3] = rampa.pintar(np.array([[1.0 - 0.15 * (abs(dx) + abs(dy))]]), 0.0)[0, 0]
                img[c + dy, c + dx, 3] = 255
    return img


def sprite_humo(semilla, k, n=16):
    rng = np.random.default_rng(semilla + 31 * k)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    img = np.zeros((n, n, 4))
    for _ in range(4):
        cx, cy, r = rng.uniform(5, 11), rng.uniform(5, 11), rng.uniform(2.5, 4.5) - 0.4 * k
        d = np.hypot(xs - cx, ys - cy) / r
        m = d < 1
        g = 40 + 50 * (1 - d) + 10 * k
        img[..., :3] = np.where(m[..., None], np.dstack([g, g, g * 1.05]), img[..., :3])
        img[..., 3] = np.where(m, np.maximum(img[..., 3], 150 * (1 - 0.5 * d)), img[..., 3])
    return img


def brasas(semilla, rampa, n=N):
    """El lecho de brasas de un brasero: carbones oscuros redondeados con grietas que brillan del color del fuego."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    pts = rng.uniform(0, n, (26, 2))
    d1 = np.full((n, n), 1e9)
    d2 = np.full((n, n), 1e9)
    for (px, py) in pts:
        dx = np.abs(xs - px)
        dx = np.minimum(dx, n - dx)
        dy = np.abs(ys - py)
        dy = np.minimum(dy, n - dy)
        d = np.hypot(dx, dy)
        menor = d < d1
        d2 = np.where(menor, d1, np.minimum(d2, d))
        d1 = np.where(menor, d, d1)
    borde = d2 - d1
    calor = ruido(semilla + 1, base=4, octavas=3)
    img = np.zeros((n, n, 4))
    img[..., 3] = 255
    # el carbon: gris muy oscuro con un brillo arriba a la izquierda en cada trozo
    g = 18 + 22 * np.clip(borde / 6, 0, 1) + 10 * (ruido(semilla + 2, base=16, octavas=1) - 0.5)
    img[..., 0] = g
    img[..., 1] = g * 0.95
    img[..., 2] = g * 0.92
    # las grietas entre trozos brillan (mas donde el ruido de calor es alto)
    brillo = np.clip((1.6 - borde) / 1.6, 0, 1) * np.clip(calor * 1.4 - 0.2, 0, 1)
    col = rampa.pintar(np.clip(0.35 + 0.6 * brillo, 0, 1), 0.6)
    m = brillo > 0.25
    img[..., :3] = np.where(m[..., None], col, img[..., :3])
    return img


def cera(semilla, rampa, n=N):
    """La cera de una vela: lisa, con chorretones que bajan (mas claros) y la parte de arriba algo mas clara."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    v = 0.55 + 0.06 * (ruido(semilla + 1, base=8, octavas=2) - 0.5)
    v = v + 0.10 * np.cos(xs / n * 2 * math.pi * 4)  # redondez repetida (cada vela usa 2 de 16)
    for _ in range(9):
        x = rng.uniform(0, n)
        largo = rng.uniform(10, 40)
        ancho = rng.uniform(1.5, 3.5)
        d = np.abs(xs - x)
        d = np.minimum(d, n - d)
        gota = (d < ancho) & (ys < largo) | (np.hypot(xs - x, ys - largo) < ancho * 1.3)
        v = np.where(gota, v + 0.12, v)
    v = np.where(ys < 4, v + 0.12, v)
    return rampa.pintar(np.clip(v, 0, 1), 0.6)


def palo(semilla, rampa, n=N):
    """El palo de una antorcha: madera con la veta de arriba abajo."""
    from madera import ruido_estirado
    v = 0.5 + 0.18 * (ruido_estirado(semilla, 16, 2, octavas=3) - 0.5) + 0.06 * (ruido(semilla + 1, base=16, octavas=1) - 0.5)
    return rampa.pintar(np.clip(v, 0, 1), 0.6)


def trapo(semilla, rampa, n=N):
    """La cabeza de una antorcha: trapo enrollado y empapado de brea (vueltas de lado, oscuras, con brillo)."""
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    vuelta = 0.5 + 0.5 * np.sin(ys / n * 2 * math.pi * 8 + 1.2 * np.sin(xs / n * 2 * math.pi * 2))
    v = 0.30 + 0.30 * vuelta + 0.10 * (ruido(semilla, base=16, octavas=2) - 0.5)
    return rampa.pintar(np.clip(v, 0, 1), 0.6)
