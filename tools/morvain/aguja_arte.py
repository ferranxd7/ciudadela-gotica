#!/usr/bin/env python3
"""
El arte del Corazon de la Aguja (parte 7ac), pixel a pixel:

  textures/item/lanza_sombra.png      la lanza de sombra (lo que se ve de las lanzas que tira la Sombra: un sprite de cara a la
                                      camara): una astilla negra y larga con el filo violeta y el nucleo al rojo.
  textures/item/corazon_sombra.png    la textura del corazon de la Sombra (carne roja oscura, venas negras y brillos).
  models/item/corazon_sombra.json     el corazon en cubos: dos lobulos arriba, el cuerpo que se estrecha hacia la punta y la arteria.
  textures/gui/retratos/sombra.png    su retrato para el dialogo (64 x 64): la capucha sin cara (la oscuridad y los ojos rojos), la
                                      corona rota de Corvin flotando encima, las puas de hueso del manto y el rojo de su pecho.

    python3 tools/morvain/aguja_arte.py [carpeta_de_vistas]
"""
import json
import math
import os
import random
import sys

from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
RES = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))


def hexc(s, a=255):
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


OUT = hexc("050307")
NEGRO = [hexc(c) for c in ("030204", "08050c", "110a17", "1b1124", "271933", "352245")]
ROJO = [hexc(c) for c in ("2a0306", "4a060b", "7a0b12", "b0141b", "e3231c", "ff6a3a", "ffd0a0")]
ORO = [hexc(c) for c in ("3a2508", "5a3a0e", "7d5518", "a37526", "c99a3c", "f0d27a")]
HUESO = [hexc(c) for c in ("1c1814", "2b2520", "3d352c", "524638", "6a5b48")]


def lanza():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # de abajo a la izquierda a arriba a la derecha: el asta negra que se afila, el filo violeta y el nucleo rojo
    for t in range(0, 15):
        x, y = 1 + t, 14 - t
        ancho = 1 if t < 4 else 2 if t < 12 else 1
        for k in range(-ancho // 2, ancho - ancho // 2):
            img.putpixel((min(15, x + k), y), NEGRO[2] if t < 12 else NEGRO[4])
        if 4 <= t < 13:
            img.putpixel((min(15, x + 1), max(0, y - 1)) if t % 2 else (x, y), NEGRO[5])
        if 6 <= t <= 12:
            img.putpixel((x, y), ROJO[3] if t % 3 else ROJO[4])
    img.putpixel((15, 0), ROJO[5])
    img.putpixel((14, 1), ROJO[4])
    # el contorno
    antes = img.copy()
    for y in range(16):
        for x in range(16):
            if antes.getpixel((x, y))[3] == 0 and any(0 <= x + a < 16 and 0 <= y + b < 16 and antes.getpixel((x + a, y + b))[3] > 0
                                                       for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                img.putpixel((x, y), OUT)
    return img


def corazon_textura():
    rnd = random.Random(30)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = 0.5 + 0.5 * math.sin(x * 0.9 + y * 0.4) * math.cos(y * 0.7 - x * 0.2)
            c = mix(ROJO[1], ROJO[3], v * 0.8 + rnd.random() * 0.2)
            # las venas negras
            if (x * 3 + y * 5) % 11 == 0 or (x + 2 * y) % 13 == 0:
                c = mix(c, NEGRO[1], 0.75)
            # brillos humedos
            if rnd.random() < 0.05:
                c = ROJO[5]
            img.putpixel((x, y), c)
    return img


def corazon_modelo():
    """Un corazon de cubos (en unidades de bloque de 16): dos lobulos arriba, el cuerpo y la punta, y la arteria."""
    def caja(desde, hasta):
        caras = {}
        for cara in ("north", "south", "east", "west", "up", "down"):
            caras[cara] = {"uv": [0, 0, 16, 16], "texture": "#t"}
        return {"from": desde, "to": hasta, "faces": caras}
    elementos = [
        caja([3.5, 8, 6.5], [7.5, 12, 9.5]),       # lobulo izquierdo
        caja([8.5, 8, 6.5], [12.5, 12, 9.5]),      # lobulo derecho
        caja([4, 5, 6], [12, 10, 10]),             # el cuerpo
        caja([5.5, 3, 6.5], [10.5, 5, 9.5]),       # hacia la punta
        caja([7, 1.5, 7], [9, 3, 9]),              # la punta
        caja([7.5, 11.5, 7.5], [9.5, 14.5, 9.5]),  # la arteria
        caja([9.5, 12, 7.8], [11, 13.5, 9.2]),     # otra, mas corta
    ]
    return {
        "textures": {"t": "darkcraft:item/corazon_sombra", "particle": "darkcraft:item/corazon_sombra"},
        "elements": elementos,
        "display": {
            "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
            "gui": {"rotation": [20, 30, 0], "translation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
            "thirdperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 2, 1], "scale": [0.45, 0.45, 0.45]},
            "firstperson_righthand": {"rotation": [0, -20, 0], "translation": [1, 3, 1], "scale": [0.5, 0.5, 0.5]},
        },
    }


def retrato():
    rnd = random.Random(7)
    W = 64
    img = Image.new("RGBA", (W, W))
    # el fondo: la oscuridad, con un resplandor rojo abajo (su pecho) y motas
    for y in range(W):
        for x in range(W):
            d = math.hypot(x - 32, y - 70) / 50
            c = mix(hexc("16060a"), NEGRO[0], d)
            img.putpixel((x, y), c)
    for _ in range(18):
        x, y = rnd.randrange(W), rnd.randrange(W)
        img.putpixel((x, y), mix(img.getpixel((x, y)), ROJO[3], 0.5))
    # los hombros y el manto de placas de hueso negro, con sus puas
    for y in range(40, W):
        for x in range(W):
            ancho = 18 + (y - 40) * 0.95
            if abs(x + 0.5 - 32) < ancho:
                placa = ((x // 5) + (y // 4)) % 2
                c = mix(HUESO[1], HUESO[2], 0.4 if placa else 0.0)
                c = mix(c, NEGRO[1], 0.45)
                if y % 4 == 0:
                    c = mix(c, NEGRO[0], 0.5)
                img.putpixel((x, y), c)
    for cx, alto in ((10, 12), (16, 8), (54, 12), (48, 8)):
        for k in range(alto):
            y = 44 - k
            w = 1 if k > alto // 2 else 2
            for dx in range(-w // 2, w - w // 2 + 1):
                img.putpixel((cx + dx + (k // 3) * (1 if cx > 32 else -1), y), HUESO[3] if k < alto - 2 else HUESO[4])
    # el pecho abierto: el rojo que late en medio, entre las costillas
    for y in range(54, W):
        for x in range(24, 41):
            e = ((x + 0.5 - 32) / 7) ** 2 + ((y - 62) / 7) ** 2
            if e < 1:
                img.putpixel((x, y), mix(ROJO[4], ROJO[2], e))
            if e < 1 and y % 3 == 0 and abs(x - 32) > 1:
                img.putpixel((x, y), HUESO[3])
    # la capucha: tela negra violacea con pliegues
    for y in range(9, 56):
        for x in range(W):
            h = (y - 9) / 46.0
            medio = 3 + 20 * min(1.0, h * 1.9) ** 0.5
            if abs(x + 0.5 - 32) > medio:
                continue
            pl = 0.5 + 0.5 * math.sin((x - 32) * 0.55 + h * 3)
            c = mix(NEGRO[2], NEGRO[4], pl * 0.7)
            if abs(x + 0.5 - 32) > medio - 2:
                c = mix(c, NEGRO[5], 0.6)
            img.putpixel((x, y), c)
    # dentro, la oscuridad (sin cara) y los ojos
    for y in range(17, 46):
        for x in range(W):
            e = ((x + 0.5 - 32) / 11.5) ** 2 + ((y - 31) / 13.5) ** 2
            if e <= 1:
                img.putpixel((x, y), NEGRO[0] if e < 0.75 else mix(NEGRO[0], NEGRO[2], (e - 0.75) * 4))
    for ex in (26, 37):
        for (dx, dy, c) in ((0, 0, ROJO[5]), (1, 0, ROJO[4]), (-1, 0, ROJO[3]), (0, -1, ROJO[3]), (0, 1, ROJO[2]), (2, 0, ROJO[2]), (-2, 0, ROJO[1])):
            img.putpixel((ex + dx, 29 + dy), c)
    # la corona rota de Corvin, flotando ladeada sobre la capucha
    for x in range(20, 45):
        if x in (32, 33):
            continue
        y = 5 + int((x - 20) * 0.12)
        img.putpixel((x, y), ORO[4] if x % 3 else ORO[5])
        img.putpixel((x, y + 1), ORO[2])
        img.putpixel((x, y + 2), ORO[1])
    for px, alto in ((21, 4), (26, 6), (31, 7), (36, 5), (42, 3)):
        base = 5 + int((px - 20) * 0.12)
        for k in range(alto):
            img.putpixel((px, base - 1 - k), ORO[4] if k < alto - 1 else ORO[5])
            img.putpixel((px + 1, base - 1 - k), ORO[2])
    for (x, y) in ((31, 6), (30, 7)):
        img.putpixel((x, y), ROJO[4])
    # la grieta de la corona (el hueco del medio, al rojo apagado)
    img.putpixel((32, 6), ROJO[2])
    img.putpixel((33, 7), ROJO[1])
    return img


def main():
    vistas = sys.argv[1] if len(sys.argv) > 1 else None
    os.makedirs(os.path.join(RES, "textures/item"), exist_ok=True)
    os.makedirs(os.path.join(RES, "textures/gui/retratos"), exist_ok=True)
    os.makedirs(os.path.join(RES, "models/item"), exist_ok=True)
    salidas = {
        "textures/item/lanza_sombra.png": lanza(),
        "textures/item/corazon_sombra.png": corazon_textura(),
        "textures/gui/retratos/sombra.png": retrato(),
    }
    for ruta, im in salidas.items():
        im.save(os.path.join(RES, ruta))
        if vistas:
            os.makedirs(vistas, exist_ok=True)
            im.resize((im.width * 6, im.height * 6), Image.NEAREST).save(os.path.join(vistas, os.path.basename(ruta)))
    with open(os.path.join(RES, "models/item/lanza_sombra.json"), "w") as f:
        json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/lanza_sombra"}}, f)
    with open(os.path.join(RES, "models/item/corazon_sombra.json"), "w") as f:
        json.dump(corazon_modelo(), f, indent=1)
    print("ok")


if __name__ == "__main__":
    main()
