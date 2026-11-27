#!/usr/bin/env python3
"""
Silas, el mercader ambulante de Morvain (parte 7af): la piel (128 x 64, las mismas piezas que Kerrick: sombrero de ala ancha,
mochila, manta enrollada, olla y farol; `MercaderRenderer`) y el retrato (64 x 64). Sale de `mercader.py` con sus colores
(sombrero negro con cinta roja y plumas de cuervo, abrigo morado oscuro remendado, panuelo rojo, la piel mas palida, el pelo
negro, lona y manta gastadas) y lo suyo pintado encima: el parche en el ojo, la perilla, la cicatriz y las plumas de cuervo.

    python3 tools/personajes/silas.py [--vista carpeta]
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mercader as M  # noqa: E402
from piel import mezcla  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

COLORES = dict(
    PIEL=["#4e3830", "#6a4c40", "#866252", "#a07a66", "#b8927c", "#ccaa94"],
    PELO=["#0c0a0c", "#181418", "#241e24", "#322a32"],
    SOMBRERO=["#0c0b0e", "#141218", "#1c1a22", "#26232e", "#302c3a", "#3c3848"],
    CINTA=["#4a0a0e", "#6e1016", "#961c22"],
    PLUMA=["#0a0a10", "#1a1a26", "#2c2c40"],
    ABRIGO=["#1a1020", "#24162c", "#301e3a", "#3c2648", "#4a3058", "#5a3c6a"],
    REMIENDO=["#2a2a24", "#3e3c32", "#545040"],
    PANUELO=["#4a0c0c", "#6a1212", "#8a1a18", "#aa2620", "#c43a2c"],
    LONA=["#2e2a26", "#3e3832", "#4e4840", "#5e5850", "#6e6860", "#7e786e"],
    MANTA=["#16201e", "#1e2c28", "#283a34", "#324a42", "#3e5a50"],
)
PARCHE = ["#0a0808", "#1a1412", "#2a201a"]


def _con_colores(f):
    viejo = {k: getattr(M, k) for k in COLORES}
    try:
        for k, v in COLORES.items():
            setattr(M, k, v)
        return f()
    finally:
        for k, v in viejo.items():
            setattr(M, k, v)


def piel():
    P = _con_colores(M.piel)
    pi, pe = COLORES["PIEL"], COLORES["PELO"]
    x0, y0, w, h = P.caras("cabeza")["frente"]
    # el parche en el ojo derecho (el de la izquierda de la cara vista de frente) y su cinta en diagonal
    for (x, y) in ((1, 3), (2, 3), (1, 4), (2, 4)):
        P.L.px(x0 + x, y0 + y, PARCHE[1] if (x, y) != (2, 3) else PARCHE[2])
    for (x, y) in ((3, 2), (4, 1), (5, 1), (0, 4)):
        P.L.px(x0 + x, y0 + y, PARCHE[0])
    # la perilla bajo la sonrisa y una cicatriz en la mejilla
    P.L.px(x0 + 3, y0 + 7, pe[2]).px(x0 + 4, y0 + 7, pe[1])
    P.L.px(x0 + 6, y0 + 4, mezcla(pi[3], "#c47a6a", 0.6))
    # la cinta del parche por los lados de la cabeza
    for cara in ("der", "izq"):
        cx0, cy0, cw, ch = P.caras("cabeza")[cara]
        for x in range(cw):
            P.L.px(cx0 + x, cy0 + 2 + (x // 4), PARCHE[0])
    # dos plumas de cuervo en la cinta del sombrero (en la copa, por el lado derecho y el de detras)
    cx0, cy0, cw, ch = P.caras("copa")["der"]
    for (x, y) in ((2, 0), (3, 0), (3, 1), (4, 0), (5, 1)):
        P.L.px(cx0 + x, cy0 + y, COLORES["PLUMA"][2] if (x + y) % 2 else COLORES["PLUMA"][1])
    return P


def retrato():
    L = _con_colores(M.retrato)
    pi, pe = COLORES["PIEL"], COLORES["PELO"]
    cx = 31
    # el parche sobre el ojo de la izquierda de la imagen y su cinta, que cruza la frente hacia el sombrero
    for y in range(23, 30):
        for x in range(cx - 8, cx - 1):
            d = ((x + 0.5 - (cx - 4.5)) / 3.6) ** 2 + ((y + 0.5 - 26.5) / 3.3) ** 2
            if d <= 1:
                L.px(x, y, PARCHE[2] if d < 0.35 and y < 26 else PARCHE[1])
    for k in range(14):
        x = cx - 2 + k
        y = 23 - int(k * 0.35)
        L.px(x, y, PARCHE[0])
    for k in range(5):
        L.px(cx - 8 - k, 26 + k // 2, PARCHE[0])
    # la perilla negra en punta bajo la sonrisa
    for y in range(39, 45):
        for x in range(cx - 1, cx + 3):
            if abs(x + 0.5 - (cx + 1)) <= 2.2 - (y - 39) * 0.35:
                L.px(x, y, pe[2] if (x + y) % 2 else pe[1])
    # una cicatriz en la mejilla de la derecha
    for (x, y) in ((cx + 8, 28), (cx + 8, 29), (cx + 9, 30), (cx + 9, 31)):
        L.px(x, y, mezcla(pi[3], "#c47a6a", 0.65))
    # unas motas de ceniza de Morvain en el aire
    for (x, y) in ((6, 10), (14, 4), (50, 8), (58, 20), (3, 22)):
        L.px(x, y, "#8a8078")
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "silas.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "silas.png"))
    print("silas: piel y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        p = piel()
        p.vista().save(os.path.join(d, "silas_piel.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "silas_retrato.png"))
        p.L.imagen().resize((128 * 4, 64 * 4), 0).save(os.path.join(d, "silas_textura.png"))
        print("vistas en", d)
