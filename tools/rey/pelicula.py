#!/usr/bin/env python3
"""Hoja de fotogramas de las animaciones del rey (para revisarlas sin abrir el juego).

Uso: pelicula.py salida.png anim1 [anim2 ...] [--n 8] [--yaw 30] [--fase 1] [--base quieto]
"""
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import partes  # noqa: E402
import pintar  # noqa: E402
import vista  # noqa: E402
import animaciones as A  # noqa: E402


def arg(name, default):
    if name in sys.argv:
        return sys.argv[sys.argv.index(name) + 1]
    return default


def main():
    out = sys.argv[1]
    names = [a for a in sys.argv[2:] if not a.startswith("--") and sys.argv[sys.argv.index(a) - 1] not in
             ("--n", "--yaw", "--fase", "--base", "--pitch")]
    n = int(arg("--n", 8))
    yaw = float(arg("--yaw", 30))
    pitch = float(arg("--pitch", -6))
    fase = int(arg("--fase", 1))
    base_name = arg("--base", "")
    used = partes.pack()
    tex, glow = pintar.build(fase, used)
    rows = []
    for name in names:
        an = A.ANIMS[name]
        tiles = []
        for k in range(n):
            t = an.length * k / (n - 1 if not an.loop else n)
            off = A.sample(an, t)
            if base_name:
                off = A.combine(A.sample(A.ANIMS[base_name], t), off)
            im = vista.render(tex, glow if fase > 1 else None, offsets=off, yaw=yaw, pitch=pitch, size=(220, 300),
                              zoom=300 / 4.4, center_y=1.5)
            pil = Image.fromarray(im)
            d = ImageDraw.Draw(pil)
            d.text((4, 4), f"{name} {t:.2f}s", fill=(255, 255, 255))
            tiles.append(np.array(pil))
        rows.append(np.concatenate(tiles, axis=1))
    Image.fromarray(np.concatenate(rows, axis=0)).save(out)


if __name__ == "__main__":
    main()
