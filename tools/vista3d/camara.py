#!/usr/bin/env python3
"""
Lo que ve la camara de una cinematica en un volcado de Volcado.java, sin abrir el juego (parte 7ae): perspectiva con el
campo de vision de Minecraft (vertical), cada bloque con el color medio de su textura (los de vista.py), sombreado por
caras y algo de niebla; lo que no esta en el volcado es cielo (un ocaso, como el de Morvain).

    python3 tools/vista3d/camara.py volcado.bin planos.txt carpeta [--ancho 320] [--jar client-extra.jar]

planos.txt: lineas "plano N u x y z mx my mz fov" (las escribe Volcado con -Dcronica=N: tres momentos de cada plano) y,
si esta, "jugador x y z giro" (se pinta como una columna roja de 0,6 x 1,8).
"""
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from vista import Colores, leer, tabla  # noqa: E402

CIELO_ARRIBA = np.array([22, 12, 30], dtype=np.float32)
CIELO_HORIZONTE = np.array([214, 130, 70], dtype=np.float32)


def dibujar(origen, datos, tab, cam, mira, fov, ancho, alto, jugador=None, lejos=180.0):
    ox, oy, oz = origen
    nx, nz, ny = datos.shape
    cam = np.array(cam, dtype=np.float64)
    d = np.array(mira, dtype=np.float64) - cam
    d /= np.linalg.norm(d)
    arriba = np.array([0.0, 1.0, 0.0])
    der = np.cross(d, arriba)
    if np.linalg.norm(der) < 1e-6:
        der = np.array([1.0, 0.0, 0.0])
    der /= np.linalg.norm(der)
    up = np.cross(der, d)
    t = np.tan(np.radians(fov) / 2)
    asp = ancho / alto
    ii, jj = np.meshgrid(np.arange(ancho), np.arange(alto))
    u = (2 * (ii + 0.5) / ancho - 1) * t * asp
    v = (1 - 2 * (jj + 0.5) / alto) * t
    rayos = d[None, None, :] + u[..., None] * der[None, None, :] + v[..., None] * up[None, None, :]
    rayos /= np.linalg.norm(rayos, axis=2, keepdims=True)
    R = rayos.reshape(-1, 3)
    n = R.shape[0]
    # recorrido de celdas (Amanatides-Woo) para todos los rayos a la vez
    pos = np.floor(cam).astype(np.int64)
    celda = np.tile(pos, (n, 1))
    paso = np.where(R >= 0, 1, -1).astype(np.int64)
    with np.errstate(divide="ignore", invalid="ignore"):
        tdelta = np.where(R != 0, np.abs(1.0 / R), np.inf)
        siguiente = np.where(R >= 0, celda + 1 - cam, cam - celda)
        tmax = np.where(R != 0, siguiente * tdelta, np.inf)
    color = np.zeros((n, 3), dtype=np.float32)
    dist = np.full(n, np.inf)
    vivo = np.ones(n, dtype=bool)
    cara = np.zeros(n, dtype=np.int64)
    jx = jz = jy0 = None
    if jugador is not None:
        jx, jy0, jz = jugador
    for _ in range(int(lejos * 1.9)):
        idx = np.nonzero(vivo)[0]
        if len(idx) == 0:
            break
        tm = tmax[idx]
        eje = np.argmin(tm, axis=1)
        tt = tm[np.arange(len(idx)), eje]
        celda[idx, eje] += paso[idx, eje]
        tmax[idx, eje] += tdelta[idx, eje]
        cara[idx] = eje
        fuera = tt > lejos
        vivo[idx[fuera]] = False
        idx2 = idx[~fuera]
        if len(idx2) == 0:
            continue
        c = celda[idx2]
        x, y, z = c[:, 0] - ox, c[:, 1] - oy, c[:, 2] - oz
        dentro = (x >= 0) & (x < nx) & (z >= 0) & (z < nz) & (y >= 0) & (y < ny)
        golpe = np.zeros(len(idx2), dtype=bool)
        col = np.zeros((len(idx2), 3), dtype=np.float32)
        if dentro.any():
            k = np.nonzero(dentro)[0]
            est = datos[x[k], z[k], y[k]]
            a = tab[est, 4]
            g = a > 0.3
            golpe[k[g]] = True
            col[k[g]] = tab[est[g], :3]
        if jugador is not None:
            # el jugador: una columna de 0,6 x 1,8
            ce = c.astype(np.float64) + 0.5
            j = (np.abs(ce[:, 0] - jx) < 0.6) & (np.abs(ce[:, 2] - jz) < 0.6) & (ce[:, 1] > jy0) & (ce[:, 1] < jy0 + 1.9)
            golpe |= j
            col[j] = (190, 30, 30)
        if golpe.any():
            h = idx2[golpe]
            sombra = np.where(cara[h] == 1, 1.0, np.where(cara[h] == 0, 0.78, 0.88)).astype(np.float32)
            color[h] = col[golpe] * sombra[:, None]
            dist[h] = tt[~fuera][golpe]
            vivo[h] = False
    # cielo y niebla
    cielo = CIELO_HORIZONTE[None, :] + (CIELO_ARRIBA - CIELO_HORIZONTE)[None, :] * np.clip(R[:, 1] * 2.2, 0, 1)[:, None]
    sin = ~np.isfinite(dist)
    color[sin] = cielo[sin]
    niebla = np.clip((np.where(np.isfinite(dist), dist, 0) - 40) / (lejos - 40), 0, 0.85)[:, None]
    color = color * (1 - niebla) + cielo * niebla
    return Image.fromarray(np.clip(color, 0, 255).astype(np.uint8).reshape(alto, ancho, 3))


def main():
    args = sys.argv[1:]
    if len(args) < 3:
        print(__doc__)
        return 1
    ruta, planos, carpeta = args[:3]
    ancho = int(args[args.index("--ancho") + 1]) if "--ancho" in args else 320
    jar = args[args.index("--jar") + 1] if "--jar" in args else "/tmp/cp/cpjars/client-extra.jar"
    origen, datos, paleta, _ = leer(ruta)
    tab = tabla(paleta, Colores(jar))
    os.makedirs(carpeta, exist_ok=True)
    jugador = None
    lineas = [l.split() for l in open(planos) if l.strip()]
    for p in lineas:
        if p[0] == "jugador":
            jugador = (float(p[1]), float(p[2]), float(p[3]))
    hechas = []
    for p in lineas:
        if p[0] != "plano":
            continue
        k, u = int(p[1]), float(p[2])
        x, y, z, mx, my, mz, fov = map(float, p[3:10])
        im = dibujar(origen, datos, tab, (x, y, z), (mx, my, mz), fov, ancho, ancho * 9 // 16, jugador)
        nombre = os.path.join(carpeta, f"plano{k}_{int(round(u * 100)):02d}.png")
        im.save(nombre)
        hechas.append(nombre)
    print("\n".join(hechas))
    return 0


if __name__ == "__main__":
    sys.exit(main())
