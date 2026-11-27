"""
La bola de fuego que escupe Vorthak, el Dragon de Ceniza (parte 7z), 16 x 16: un nucleo blanco y amarillo que pasa a naranja y a
rojo hacia fuera, con lenguas de fuego que se van hacia atras (arriba a la derecha) y alguna brasa suelta. Solo sirve para
dibujar la bola en el aire (el dibujante la pone grande y a plena luz).

Uso: python3 tools/rol/dragon_objetos.py   (escribe textures/item/bola_dragon.png)
"""
import math
import os

from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
SAL = os.path.join(AQUI, "..", "..", "src", "main", "resources", "assets", "darkcraft", "textures", "item")

FUEGO = [(255, 250, 220), (255, 232, 120), (255, 186, 52), (246, 120, 24), (204, 58, 14), (130, 26, 8)]


def bola():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    cx, cy = 6.8, 9.2
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            # la cola de fuego: hacia arriba y a la derecha, mas larga cuanto mas atras, con lenguas
            ang = math.atan2(-dy, dx)
            atras = max(0.0, math.cos(ang - math.radians(45)))
            lengua = 0.8 + 0.6 * math.sin(ang * 5 + 1.3) * atras
            r = 4.4 + 3.6 * atras ** 2.2 * lengua
            if d > r:
                continue
            k = d / r
            if atras > 0.5 and d > 4.6:
                # en la cola, de naranja a rojo oscuro
                t = min(1.0, (d - 4.6) / max(0.5, r - 4.6))
                i = 3 + int(t * 2.99)
            else:
                i = min(5, int(k * 5.4))
            c = FUEGO[i]
            a = 255 if k < 0.92 else 200
            px[x, y] = c + (a,)
    # el corazon blanco
    for (x, y) in ((6, 9), (5, 9), (6, 8), (6, 10), (7, 9)):
        px[x, y] = FUEGO[0] + (255,)
    # brasas sueltas detras
    for (x, y, i) in ((14, 1, 3), (13, 3, 4), (15, 4, 5), (11, 0, 4)):
        px[x, y] = FUEGO[i] + (255,)
    return im


def main():
    os.makedirs(SAL, exist_ok=True)
    bola().save(os.path.join(SAL, "bola_dragon.png"))
    print("bola_dragon.png")


if __name__ == "__main__":
    main()
