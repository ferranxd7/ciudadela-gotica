#!/usr/bin/env python3
"""
Las flechas de fuego, de sangre y de escarcha (parte 7aj):

- textures/item/flecha_<clase>.png (16 x 16, como la flecha del juego: en diagonal, las plumas abajo a la izquierda y la punta
  arriba a la derecha). Cada pixel se mira en las coordenadas de la flecha (u a lo largo, desde las plumas; v de lado) y se le da
  el material de lo que cae ahi; luego el contorno de tinta donde hace falta.
- textures/entity/flecha_<clase>.png (32 x 32, con el reparto de la flecha del juego que usa su dibujante: arriba a la izquierda la
  flecha de lado en 16 x 5 —las plumas a la izquierda, la punta a la derecha, el asta en la fila 2— y debajo, en 5 x 5, la cola
  vista de frente: el asta en el centro y las plumas en cruz).

    python3 tools/rol/flechas.py [--vista carpeta]
"""
import math
import os
import sys

from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
ITEM = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/item")
ENT = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/entity")

TINTA = (24, 18, 22, 255)

# cada clase: el asta (claro, oscuro), la punta (claro, medio, oscuro), las plumas (claro, oscuro) y lo de mas
CLASES = {
    "fuego": {
        "asta": [(96, 62, 36), (58, 36, 22)],
        "punta": [(250, 214, 120), (226, 124, 40), (120, 46, 20)],
        "plumas": [(232, 120, 46), (156, 52, 26)],
        "trapo": [(70, 48, 40), (40, 26, 22), (250, 150, 60)],
    },
    "sangre": {
        "asta": [(112, 40, 40), (62, 20, 24)],
        "punta": [(226, 220, 206), (176, 160, 150), (110, 24, 30)],
        "plumas": [(60, 52, 58), (150, 22, 32)],
        "trapo": None,
    },
    "escarcha": {
        "asta": [(178, 196, 208), (104, 124, 142)],
        "punta": [(240, 252, 255), (150, 214, 240), (74, 140, 186)],
        "plumas": [(236, 244, 250), (142, 184, 214)],
        "trapo": None,
    },
}


def rgba(c):
    return tuple(c) + (255,)


# ------------------------------------------------------------------ el objeto (16 x 16, en diagonal)

def objeto(clase):
    m = CLASES[clase]
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    a = math.radians(45)
    du = (math.cos(a), -math.sin(a))
    dv = (math.sin(a), math.cos(a))
    ox, oy = 1.0, 15.0              # el culo de la flecha (abajo a la izquierda)
    punta0, punta1 = 13.9, 18.5     # donde empieza la punta y donde acaba (en el pixel 14, 2)
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5 - ox, y + 0.5 - oy
            u = px * du[0] + py * du[1]
            v = px * dv[0] + py * dv[1]
            c = None
            # las aletas de la punta junto a su base y luego se va cerrando
            ancho = 1.5 if u < punta0 + 1.0 else 0.8 if u < punta1 - 0.5 else 0.2
            if punta0 <= u <= punta1 + 0.2 and abs(v) <= ancho:
                # la punta (la luz de arriba a la izquierda: v negativa)
                if v < -0.3:
                    c = m["punta"][0]
                elif v < 0.3:
                    c = m["punta"][1] if u < punta1 - 0.6 else m["punta"][0]
                else:
                    c = m["punta"][2] if clase != "fuego" else m["punta"][1]
                if clase == "fuego" and u > punta1 - 1.2:
                    c = m["punta"][0]
            elif m["trapo"] and 12.2 <= u < punta0 and abs(v) <= 0.8:
                # el trapo empapado en pez de la de fuego, con sus brasas
                c = m["trapo"][2] if abs(v) < 0.3 and int(u * 2) % 2 == 0 else m["trapo"][0] if v < 0 else m["trapo"][1]
            elif 2.2 <= u < punta0 and abs(v) <= 0.4:
                c = m["asta"][0]
            elif 2.2 <= u < punta0 and 0.4 < v <= 0.8:
                c = m["asta"][1]
            elif 0.7 <= u <= 4.6 and abs(v) <= 1.6 - max(0.0, u - 3.0) * 1.0:
                # las plumas, a los dos lados del culo del asta
                c = m["plumas"][0] if v < -0.3 else m["asta"][1] if abs(v) <= 0.3 else m["plumas"][1]
            if c is not None:
                im.putpixel((x, y), rgba(c))
    if clase == "sangre":
        im.putpixel((12, 5), rgba((176, 20, 30)))
    if clase == "escarcha":
        im.putpixel((14, 2), rgba((255, 255, 255)))
    return contorno(im)


def contorno(im):
    w, h = im.size
    op = [[im.getpixel((x, y))[3] > 0 for x in range(w)] for y in range(h)]
    for y in range(h):
        for x in range(w):
            if op[y][x]:
                continue
            # solo abajo y a la derecha (la sombra de la tinta, como las flechas del juego)
            if (x > 0 and op[y][x - 1]) or (y > 0 and op[y - 1][x]):
                im.putpixel((x, y), (TINTA[0], TINTA[1], TINTA[2], 150))
    return im


# ------------------------------------------------------------------ la entidad (32 x 32, el reparto de la flecha del juego)

def entidad(clase):
    m = CLASES[clase]
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    p = lambda x, y, c: im.putpixel((x, y), rgba(c))
    # de lado (16 x 5): el asta en la fila 2
    for x in range(0, 13):
        p(x, 2, m["asta"][0] if x % 5 != 2 else m["asta"][1])
    # las plumas: arriba y abajo de las cuatro primeras columnas, escalonadas
    for x in range(0, 4):
        p(x, 1, m["plumas"][0])
        p(x, 3, m["plumas"][1])
    p(0, 0, m["plumas"][0])
    p(1, 0, m["plumas"][0])
    p(0, 4, m["plumas"][1])
    p(1, 4, m["plumas"][1])
    # el trapo de la de fuego
    if m["trapo"]:
        for x in (10, 11, 12):
            p(x, 1, m["trapo"][0])
            p(x, 2, m["trapo"][2] if x == 11 else m["trapo"][1])
            p(x, 3, m["trapo"][1])
    # la punta: tres de alto y la punta en la fila 2
    for y in (1, 2, 3):
        p(13, y, m["punta"][1] if y != 2 else m["punta"][0])
        p(14, y, m["punta"][2] if y == 3 else m["punta"][1])
    p(15, 2, m["punta"][0])
    if clase == "sangre":
        p(13, 4, m["punta"][2])
        p(14, 0, m["punta"][0])
    if clase == "escarcha":
        p(14, 0, m["punta"][0])
        p(14, 4, m["punta"][2])
    # la cola de frente (5 x 5 en 0, 5): el asta en el centro y las plumas en cruz
    for d in (-2, -1, 1, 2):
        p(2 + d, 7, m["plumas"][0] if d < 0 else m["plumas"][1])
        p(2, 7 + d, m["plumas"][0] if d < 0 else m["plumas"][1])
    p(2, 7, m["asta"][1])
    return im


def main():
    vista = sys.argv[sys.argv.index("--vista") + 1] if "--vista" in sys.argv else None
    os.makedirs(ENT, exist_ok=True)
    hoja = Image.new("RGBA", (len(CLASES) * 2 * 136, 136), (44, 40, 48, 255))
    for i, clase in enumerate(CLASES):
        it = objeto(clase)
        it.save(os.path.join(ITEM, "flecha_" + clase + ".png"))
        en = entidad(clase)
        en.save(os.path.join(ENT, "flecha_" + clase + ".png"))
        g = it.resize((128, 128), Image.NEAREST)
        hoja.paste(g, (4 + i * 272, 4), g)
        e = en.crop((0, 0, 16, 10)).resize((128, 80), Image.NEAREST)
        hoja.paste(e, (140 + i * 272, 28), e)
    if vista:
        os.makedirs(vista, exist_ok=True)
        hoja.save(os.path.join(vista, "flechas.png"))
    print("flechas:", ", ".join(CLASES))


if __name__ == "__main__":
    main()
