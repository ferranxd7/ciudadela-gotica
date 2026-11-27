"""
Los objetos de lo que tira Pell, el Bufon del Rey (parte 7y), 16 x 16: el cuchillo de malabarista (hoja de acero, guarda negra,
mango de rombos rojos y negros y una anilla dorada) y la bola de fuego de los malabares (una bola de rombos rojos y negros
envuelta en llamas). Solo sirven para dibujar sus cuchillos y sus bolas en el aire.

Uso: python3 tools/rol/bufon_objetos.py   (escribe textures/item/cuchillo_bufon.png y bola_bufon.png)
"""
import os
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
SAL = os.path.join(AQUI, "..", "..", "src", "main", "resources", "assets", "darkcraft", "textures", "item")


def cuchillo():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    acero = [(236, 238, 242), (196, 200, 210), (150, 156, 170), (92, 96, 110)]
    # la hoja: de (6, 9) a (14, 1), dos de ancho, con el filo claro arriba y el lomo oscuro abajo
    for k in range(0, 9):
        x, y = 6 + k, 9 - k
        px[x, y] = acero[1] + (255,)
        if x + 1 < 16:
            px[x + 1, y] = acero[0] + (255,)
        if y + 1 < 16:
            px[x, y + 1] = acero[2] + (255,)
    px[14, 1] = acero[0] + (255,)
    px[15, 0] = acero[1] + (255,)
    # el reflejo
    for k in (2, 4, 6):
        px[6 + k + 1, 9 - k] = (255, 255, 255, 255)
    # la guarda: negra con remaches dorados, cruzada
    for (x, y) in ((4, 9), (5, 10), (6, 11), (7, 12), (5, 8), (6, 9)):
        px[x, y] = (26, 22, 28, 255)
    px[4, 9] = (212, 170, 60, 255)
    px[7, 12] = (212, 170, 60, 255)
    # el mango: rombos rojos y negros
    for k in range(0, 4):
        x, y = 4 - k, 11 + k
        c = (176, 24, 30) if k % 2 == 0 else (30, 22, 26)
        px[x, y] = c + (255,)
        if x + 1 < 16:
            px[x + 1, y] = ((120, 14, 20) if k % 2 == 0 else (60, 44, 50)) + (255,)
    # la anilla dorada del pomo
    for (x, y) in ((0, 14), (0, 15), (1, 15), (0, 13)):
        px[x, y] = (232, 190, 70, 255)
    px[1, 14] = (0, 0, 0, 0)
    return im


def bola():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    cx, cy, r = 7.5, 8.5, 4.6
    # las llamas alrededor (arriba mas largas)
    llamas = {(7, 1): 1, (8, 2): 1, (6, 2): 2, (9, 1): 2, (5, 3): 2, (10, 3): 1, (4, 4): 2, (11, 4): 2, (3, 6): 2, (12, 6): 2,
              (8, 0): 3, (6, 1): 3, (10, 2): 3, (3, 9): 3, (12, 9): 3, (4, 12): 3, (11, 12): 3}
    tonos = {1: (255, 228, 90), 2: (255, 150, 40), 3: (220, 70, 20)}
    for (x, y), t in llamas.items():
        px[x, y] = tonos[t] + (255,)
    for y in range(16):
        for x in range(16):
            d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
            if d > r + 0.9:
                continue
            if d > r:
                # el borde que arde
                px[x, y] = (255, 120, 30, 255)
                continue
            # rombos de arlequin rojos y negros, con la luz arriba a la izquierda
            k = ((x + y) // 3 + (x - y + 16) // 3) % 2
            base = (196, 28, 34) if k == 0 else (40, 26, 34)
            luz = max(0.0, 1.0 - (((x + 0.5 - cx + 1.6) ** 2 + (y + 0.5 - cy + 1.8) ** 2) ** 0.5) / 5.0)
            c = tuple(min(255, int(v + (255 - v) * 0.45 * luz)) for v in base)
            px[x, y] = c + (255,)
    # el brillo
    px[6, 6] = (255, 236, 200, 255)
    return im


if __name__ == "__main__":
    os.makedirs(SAL, exist_ok=True)
    cuchillo().save(os.path.join(SAL, "cuchillo_bufon.png"))
    bola().save(os.path.join(SAL, "bola_bufon.png"))
    print("cuchillo_bufon.png y bola_bufon.png")
