#!/usr/bin/env python3
"""
La espada de luz: donde mueres se queda clavada en el suelo guardando tus Ecos de la Sombra. Es un objeto
(darkcraft:espada_luz, no se puede conseguir) que el servidor ensena con una entidad item_display. Aqui salen su modelo
en 3D (cajas: hoja con nervio, guarda con gema, empunadura y pomo) y su textura:

  assets/darkcraft/models/item/espada_luz.json
  assets/darkcraft/textures/item/espada_luz.png   (16x16: muestras de color para cada cara)

El modelo va de punta: la punta abajo (y = 0) y el pomo arriba (y = 16), centrado en x y z.

    python3 tools/ui/espada_luz.py [--vista carpeta]
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import ASSETS, ORO, PIEDRA, ROJO, Lienzo  # noqa: E402

BLANCO = "#fffdf0"
LUZ = ["#f2c860", "#ffe9a8", "#fff6d2", BLANCO]   # del filo (dorado) al centro (blanco)


def textura():
    L = Lienzo(16, 16)
    # (0,0)-(2,10) la cara de la hoja: una columna calida y otra casi blanca, con algun destello
    for y in range(10):
        L.px(0, y, LUZ[1] if y % 4 else LUZ[2])
        L.px(1, y, LUZ[2] if y % 3 else LUZ[3])
    # (2,0)-(3,10) el filo (los cantos de la hoja)
    for y in range(10):
        L.px(2, y, LUZ[0] if y % 5 else LUZ[1])
    # (3,0)-(4,10) el nervio del centro
    for y in range(10):
        L.px(3, y, BLANCO)
    # (4,0)-(11,2) la guarda: oro claro arriba, mas oscuro abajo
    for x in range(4, 11):
        L.px(x, 0, ORO[6] if x in (5, 9) else ORO[5])
        L.px(x, 1, ORO[4] if x % 3 else ORO[3])
    # (11,0)-(13,2) los remates de la guarda
    L.px(11, 0, ORO[6]).px(12, 0, ORO[5]).px(11, 1, ORO[4]).px(12, 1, ORO[3])
    # (4,2)-(6,4) la gema roja
    L.px(4, 2, ROJO[5]).px(5, 2, "#ffd8c8").px(4, 3, ROJO[4]).px(5, 3, ROJO[5])
    # (6,2)-(7,5) la empunadura: cuero oscuro con vueltas
    L.px(6, 2, PIEDRA[5]).px(6, 3, PIEDRA[3]).px(6, 4, PIEDRA[5])
    # (7,2)-(9,4) el pomo
    L.px(7, 2, ORO[6]).px(8, 2, ORO[5]).px(7, 3, ORO[4]).px(8, 3, ORO[3])
    return L


def caja(desde, hasta, uv_frente, uv_lado=None, uv_tapa=None):
    """Una caja del modelo. uv_frente para norte y sur; uv_lado para este y oeste; uv_tapa para arriba y abajo."""
    uv_lado = uv_lado or uv_frente
    uv_tapa = uv_tapa or uv_lado
    caras = {}
    for nombre, uv in (("north", uv_frente), ("south", uv_frente), ("east", uv_lado), ("west", uv_lado),
                       ("up", uv_tapa), ("down", uv_tapa)):
        caras[nombre] = {"uv": list(uv), "texture": "#0"}
    return {"from": list(desde), "to": list(hasta), "shade": False, "faces": caras}


def modelo():
    hoja, filo, nervio = (0, 0, 2, 10), (2, 0, 3, 10), (3, 0, 4, 10)
    guarda, remate, gema = (4, 0, 11, 2), (11, 0, 13, 2), (4, 2, 6, 4)
    puno, pomo = (6, 2, 7, 5), (7, 2, 9, 4)
    e = [
        caja((7.5, 0, 7.8), (8.5, 1.5, 8.2), (0, 8, 2, 10), filo),                 # punta
        caja((7, 1.5, 7.75), (9, 10, 8.25), hoja, filo),                           # hoja
        caja((7.75, 2, 7.6), (8.25, 9.6, 8.4), nervio),                            # nervio
        caja((4.5, 10, 7.5), (11.5, 11, 8.5), guarda, (4, 1, 5, 2)),               # guarda
        caja((4, 9.6, 7.4), (5, 11.4, 8.6), remate),                               # remates de la guarda
        caja((11, 9.6, 7.4), (12, 11.4, 8.6), remate),
        caja((7.25, 9.75, 7.2), (8.75, 11.25, 8.8), gema),                         # gema
        caja((7.5, 11, 7.5), (8.5, 14, 8.5), puno),                                # empunadura
        caja((7, 14, 7), (9, 15.75, 9), pomo),                                     # pomo
    ]
    return {
        "credit": "Dark Craft: la espada de luz (tools/ui/espada_luz.py)",
        "gui_light": "front",
        "textures": {"0": "darkcraft:item/espada_luz", "particle": "darkcraft:item/espada_luz"},
        "elements": e,
        "display": {
            "gui": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [1.1, 1.1, 1.1]},
            "ground": {"rotation": [0, 0, 180], "translation": [0, 4, 0], "scale": [0.8, 0.8, 0.8]},
        },
    }


def generar():
    textura().guardar(os.path.join(ASSETS, "textures", "item", "espada_luz.png"))
    ruta = os.path.join(ASSETS, "models", "item", "espada_luz.json")
    with open(ruta, "w", encoding="utf-8") as fh:
        json.dump(modelo(), fh, indent=1)
    print("espada_luz: modelo con", len(modelo()["elements"]), "cajas y textura 16x16")


def vista(carpeta):
    """La espada vista de frente y de lado (proyeccion sin perspectiva, x8)."""
    from PIL import Image
    T = textura()
    m = modelo()
    S = 16
    hoja = Image.new("RGBA", (16 * S * 2 + 24, 16 * S + 16), (58, 74, 92, 255))
    for k, (eje, cara) in enumerate((("x", "south"), ("z", "east"))):
        # de atras adelante
        orden = sorted(m["elements"], key=lambda c: c["to"][2] if eje == "x" else c["to"][0])
        for c in orden:
            a0, a1 = (c["from"][0], c["to"][0]) if eje == "x" else (c["from"][2], c["to"][2])
            y0, y1 = c["from"][1], c["to"][1]
            u0, v0, u1, v1 = c["faces"][cara]["uv"]
            w, h = max(1, int(round((a1 - a0) * S))), max(1, int(round((y1 - y0) * S)))
            trozo = T.imagen().crop((int(u0), int(v0), max(int(u0) + 1, int(u1)), max(int(v0) + 1, int(v1)))).resize((w, h), 0)
            hoja.paste(trozo, (8 + k * (16 * S + 8) + int(round(a0 * S)), 8 + int(round((16 - y1) * S))))
    os.makedirs(carpeta, exist_ok=True)
    hoja.save(os.path.join(carpeta, "espada_luz.png"))
    print("vista en", carpeta)


if __name__ == "__main__":
    generar()
    if "--vista" in sys.argv:
        vista(sys.argv[sys.argv.index("--vista") + 1])
