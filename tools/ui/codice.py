#!/usr/bin/env python3
"""
El codice de la historia (parte 2e-4): pergamino oscuro que se repite sin costuras, la tapa de cuero con hierros de
oro, la letra capital iluminada (sus marcos de colores), la cinta, los adornos y el marco de las miniaturas.

    python3 tools/ui/codice.py [--vista carpeta]

Escribe textures/gui/codice.png (atlas), textures/gui/codice_pergamino.png (256 x 256, se repite),
textures/gui/codice_cuero.png (64 x 64, se repite) y client/ui/CodiceTex.java (donde cae cada trozo).
"""
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import Atlas, GUI, Lienzo, ORO, PIEDRA, REPO, ROJO, AZUL, VERDE, TINTA, mezcla, rgba  # noqa: E402

# ---------------------------------------------------------------- colores del codice
# el pergamino oscuro (de oscuro a claro): manchas, sombra, base, luz y fibras
PERG = ["#4c3b27", "#5d4931", "#6f5a3d", "#816b4a", "#8f7955", "#9b855f", "#a8926b", "#b8a27a"]
# la tinta y la rubrica (lo que se escribe en rojo)
TINTA_C = "#1e140c"
RUBRICA = "#7a1010"
# el cuero negro de la tapa
CUERO = ["#0c0807", "#140e0b", "#1c1410", "#251a14", "#30221a", "#3c2b20"]


def ruido_periodico(n, celdas, semilla):
    """Ruido de valores que se repite cada n pixeles (celdas x celdas puntos interpolados en coseno)."""
    r = np.random.default_rng(semilla)
    g = r.random((celdas, celdas))
    xs = np.arange(n) * celdas / n
    i0 = np.floor(xs).astype(int)
    f = xs - i0
    f = (1 - np.cos(f * np.pi)) / 2
    i1 = (i0 + 1) % celdas
    a = g[i0][:, i0] * (1 - f)[None, :] + g[i0][:, i1] * f[None, :]
    b = g[i1][:, i0] * (1 - f)[None, :] + g[i1][:, i1] * f[None, :]
    return a * (1 - f)[:, None] + b * f[:, None]


def pergamino(n=256, semilla=7):
    """El pergamino que se repite: tonos de base con nubes de ruido, fibras y motas (sin manchas redondas: se notaria
    que se repite)."""
    v = (0.45 * ruido_periodico(n, 3, semilla) + 0.30 * ruido_periodico(n, 7, semilla + 1)
         + 0.25 * ruido_periodico(n, 16, semilla + 2))
    v = (v - v.min()) / (v.max() - v.min())
    r = np.random.default_rng(semilla + 3)
    grano = r.random((n, n))
    L = Lienzo(n, n)
    for y in range(n):
        for x in range(n):
            t = v[y, x]
            # base entre PERG[3] y PERG[6], con un poco de grano (dos tonos vecinos mezclados a pixeles)
            k = 3.3 + t * 2.5 + (grano[y, x] - 0.5) * 0.9
            L.px(x, y, PERG[int(np.clip(round(k), 2, 6))])
    # fibras: rayitas casi horizontales un tono mas claras u oscuras (las del borde siguen por el otro lado)
    idx_de = {rgba(c)[:3]: j for j, c in enumerate(PERG)}
    for _ in range(260):
        x, y = int(r.integers(0, n)), int(r.integers(0, n))
        largo = int(r.integers(4, 12))
        claro = r.random() < 0.6
        dy = 0
        for i in range(largo):
            xx, yy = (x + i) % n, (y + dy) % n
            idx = idx_de.get(L.leer(xx, yy)[:3], 4)
            L.px(xx, yy, PERG[min(7, idx + 1)] if claro else PERG[max(1, idx - 1)])
            if r.random() < 0.18:
                dy += 1 if r.random() < 0.5 else -1
    # motas oscuras sueltas
    for _ in range(200):
        x, y = int(r.integers(0, n)), int(r.integers(0, n))
        L.px(x, y, PERG[int(r.integers(1, 3))])
    return L


def cuero(n=64, semilla=11):
    """El cuero negro de la tapa: poros, arrugas y algo de brillo gastado (se repite)."""
    v = 0.6 * ruido_periodico(n, 4, semilla) + 0.4 * ruido_periodico(n, 8, semilla + 1)
    v = (v - v.min()) / (v.max() - v.min())
    r = np.random.default_rng(semilla + 2)
    L = Lienzo(n, n)
    for y in range(n):
        for x in range(n):
            k = 1.5 + v[y, x] * 2.4 + (r.random() - 0.5) * 1.0
            L.px(x, y, CUERO[int(np.clip(round(k), 0, 4))])
    for _ in range(40):
        x, y = int(r.integers(0, n)), int(r.integers(0, n))
        L.px(x, y, CUERO[0])
        if r.random() < 0.4:
            L.px((x + 1) % n, y, CUERO[0])
    for _ in range(18):
        x, y = int(r.integers(0, n)), int(r.integers(0, n))
        L.px(x, y, CUERO[5])
    return L


# ---------------------------------------------------------------- la tapa (nueve trozos de 20 + 24 + 20)
ESQ_TAPA = 20


def tapa():
    """El borde de la tapa del codice: cuero con un filete de oro a 5 px del canto y una esquinera de oro con gema."""
    n = ESQ_TAPA * 2 + 24
    L = cuero(n, 21)
    # canto oscuro y un brillo de cuero gastado
    for i in range(n):
        for d, c in ((0, TINTA), (1, CUERO[0]), (2, CUERO[4])):
            L.px(i, d, c).px(d, i, c)
            L.px(i, n - 1 - d, TINTA if d == 0 else CUERO[0]).px(n - 1 - d, i, TINTA if d == 0 else CUERO[0])
    # el filete de oro (doble) a 5 y 7 px del canto, con sombra
    for d, c in ((5, ORO[3]), (6, ORO[1]), (8, ORO[2])):
        for i in range(d, n - d):
            L.px(i, d, c).px(d, i, c)
            L.px(i, n - 1 - d, ORO[1] if c != ORO[1] else ORO[0]).px(n - 1 - d, i, ORO[1] if c != ORO[1] else ORO[0])
    for i in range(6, n - 6, 5):
        L.px(i, 5, ORO[5])
        L.px(5, i, ORO[4])
    # la esquinera: una hoja de oro en angulo con una gema roja (la misma en las cuatro, girada)
    esq = [
        "kkkkkkkkkkk.....",
        "kGGGGGGGGgk.....",
        "kGyyyyyyGgk.....",
        "kGyRRRyGgk......",
        "kGyRrRyGgk......",
        "kGyRRRyGk.......",
        "kGyyyyGgk.......",
        "kGGGGGgk........",
        "kGgggggk........",
        "kkkkkkk.........",
        "kk..............",
        "................",
    ]
    pal = {"k": TINTA, "G": ORO[4], "g": ORO[2], "y": ORO[5], "R": ROJO[4], "r": ROJO[6]}
    e = Lienzo(16, 12).sprite(esq, pal)
    for (x, y, h, v) in ((2, 2, False, False), (n - 18, 2, True, False), (2, n - 14, False, True), (n - 18, n - 14, True, True)):
        s = e
        if h:
            s = s.espejo_h()
        if v:
            s = s.espejo_v()
        L.pegar(s, x, y)
    return L


def nueve(L, esq):
    n = L.w
    m = n - 2 * esq
    cortes = {
        "ai": (0, 0, esq, esq), "a": (esq, 0, m, esq), "ad": (n - esq, 0, esq, esq),
        "i": (0, esq, esq, m), "c": (esq, esq, m, m), "d": (n - esq, esq, esq, m),
        "bi": (0, n - esq, esq, esq), "b": (esq, n - esq, m, esq), "bd": (n - esq, n - esq, esq, esq),
    }
    return [(k, L.recorte(*v)) for k, v in cortes.items()]


# ---------------------------------------------------------------- la letra capital (marco de 30 x 30)
LADO_CAP = 30


def capital(fondo, borde, sarmiento):
    """El cuadro de la letra capital: fondo de color con un rombeado, doble filete y sarmientos en las esquinas."""
    n = LADO_CAP
    L = Lienzo(n, n, fondo[2])
    # rombeado del fondo (como el de los manuscritos): un punto claro cada 4 en diagonal
    for y in range(3, n - 3):
        for x in range(3, n - 3):
            if (x + y) % 4 == 0 and (x - y) % 8 == 0:
                L.px(x, y, fondo[3])
            elif (x + y) % 4 == 2 and (x - y) % 8 == 4:
                L.px(x, y, fondo[1])
    # sombra por dentro (arriba e izquierda) y luz (abajo y derecha)
    for i in range(3, n - 3):
        L.px(i, 3, fondo[0]).px(3, i, fondo[0])
        L.px(i, n - 4, fondo[3]).px(n - 4, i, fondo[3])
    # filetes de oro
    for i in range(n):
        L.px(i, 0, TINTA).px(0, i, TINTA).px(i, n - 1, TINTA).px(n - 1, i, TINTA)
    for i in range(1, n - 1):
        L.px(i, 1, borde[4]).px(1, i, borde[4]).px(i, n - 2, borde[2]).px(n - 2, i, borde[2])
    for i in range(2, n - 2):
        L.px(i, 2, borde[1]).px(2, i, borde[1]).px(i, n - 3, borde[1]).px(n - 3, i, borde[1])
    # sarmientos: una espiral pequena en cada esquina de dentro
    esp = [".ss.", "s..s", "s.s.", ".s.."]
    for (x, y, h, v) in ((4, 4, False, False), (n - 8, 4, True, False), (4, n - 8, False, True), (n - 8, n - 8, True, True)):
        s = Lienzo(4, 4).sprite(esp, {"s": sarmiento})
        if h:
            s = s.espejo_h()
        if v:
            s = s.espejo_v()
        L.pegar(s, x, y)
    # destellos en el oro
    L.px(5, 1, ORO[6]).px(1, 5, ORO[6]).px(n - 6, 1, ORO[5])
    return L


# ---------------------------------------------------------------- adornos
def floron():
    """El adorno bajo el titulo de un capitulo: un filete que adelgaza a los lados con un rombo y dos hojas."""
    w, h = 77, 9
    L = Lienzo(w, h)
    c = w // 2
    for x in range(w):
        d = abs(x - c)
        if d < 4:
            continue
        fuerza = 1 - d / (c + 1)
        if fuerza < 0.08:
            continue
        L.px(x, 4, ORO[3] if fuerza > 0.45 else ORO[2])
        if fuerza > 0.55:
            L.px(x, 3, ORO[4] if d % 5 else ORO[5])
            L.px(x, 5, ORO[1])
    rombo = ["....k....", "...kGk...", "..kGyGk..", ".kGyRyGk.", "kGyRrRyGk", ".kGyRyGk.", "..kGyGk..", "...kGk...", "....k...."]
    L.sprite(rombo, {"k": TINTA, "G": ORO[3], "y": ORO[5], "R": ROJO[4], "r": ROJO[6]}, c - 4, 0)
    hoja = ["..gg", ".gGg", "gGg.", "gg.."]
    for (x, y, h, v) in ((c - 11, 0, False, False), (c + 8, 0, True, False), (c - 11, 5, False, True), (c + 8, 5, True, True)):
        s = Lienzo(4, 4).sprite(hoja, {"g": ORO[2], "G": ORO[4]})
        if h:
            s = s.espejo_h()
        if v:
            s = s.espejo_v()
        L.pegar(s, x, y)
    return L


def fin():
    """El adorno del final de un libro: una hoja de hiedra de tinta roja con su tallo (el signo de los manuscritos)."""
    filas = [
        "....kkk...kkk..",
        "...kRRRk.kRRRk.",
        "...kRrRRkRRRRk.",
        "...kRRrRRRRRRk.",
        "....kRRrRRRRk..",
        "k....kRRrRRk...",
        ".k....kRRRk....",
        "..kk...kRk.....",
        "....kkkkk......",
    ]
    return Lienzo(15, 9).sprite(filas, {"k": "#3a0808", "R": RUBRICA, "r": ROJO[5]})


def cinta():
    """La cinta de seda roja que cuelga sobre la pagina derecha (con la punta en cola de golondrina)."""
    w, h = 9, 64
    L = Lienzo(w, h)
    for y in range(h - 6):
        for x in range(w):
            if x == 0 or x == w - 1:
                c = ROJO[1]
            elif x == 1:
                c = ROJO[3]
            elif x in (2, 3):
                c = ROJO[4] if (y // 3) % 5 else ROJO[5]
            elif x == w - 2:
                c = ROJO[2]
            else:
                c = ROJO[3]
            L.px(x, y, c)
    # la cola de golondrina
    for i in range(6):
        y = h - 6 + i
        for x in range(w):
            if x < 4 - i or x > 4 + i:
                c = ROJO[3] if 0 < x < w - 1 else ROJO[1]
                L.px(x, y, c)
    for i in range(6):
        L.px(4 - i - 1 if 4 - i - 1 >= 0 else 0, h - 6 + i, ROJO[1])
    # sombra de la tapa de arriba
    for x in range(w):
        L.px(x, 0, ROJO[0]).px(x, 1, ROJO[1])
    return L


MARCO_MIN = 6


def marco_miniatura():
    """El marco de oro de las miniaturas (6 + 12 + 6): filete, cordon y clavos en las esquinas."""
    n = MARCO_MIN * 2 + 12
    L = Lienzo(n, n)
    for i in range(n):
        for d, ca, cb in ((0, TINTA, TINTA), (1, ORO[5], ORO[2]), (2, ORO[3], ORO[2]), (3, ORO[2], ORO[1]), (4, ORO[4], ORO[3]),
                          (5, TINTA, TINTA)):
            L.px(i, d, ca).px(d, i, ca)
            L.px(i, n - 1 - d, cb).px(n - 1 - d, i, cb)
    # el cordon: rayitas en diagonal sobre el filete de en medio
    for i in range(n):
        if i % 3 == 0:
            for d in (2, 3):
                L.px(i, d, ORO[5]).px(d, i, ORO[5])
                L.px(i, n - 1 - d, ORO[4]).px(n - 1 - d, i, ORO[4])
    # lo de dentro, transparente
    for y in range(MARCO_MIN, n - MARCO_MIN):
        for x in range(MARCO_MIN, n - MARCO_MIN):
            L.borrar(x, y)
    clavo = [".kk.", "kyGk", "kGgk", ".kk."]
    for (x, y) in ((1, 1), (n - 5, 1), (1, n - 5), (n - 5, n - 5)):
        L.sprite(clavo, {"k": TINTA, "y": ORO[6], "G": ORO[4], "g": ORO[2]}, x, y)
    return L


def esquina_pagina():
    """La filigrana de tinta roja y oro en las esquinas de fuera de cada pagina."""
    filas = [
        "RRRRRRRRRRRR",
        "Rgggggggg...",
        "Rg..........",
        "Rg.RRRR.....",
        "Rg.R..R.....",
        "Rg.R.R......",
        "Rg.RR.......",
        "Rg..........",
        "Rg..........",
        "R...........",
        "R...........",
        "R...........",
    ]
    return Lienzo(12, 12).sprite(filas, {"R": "#5a1010", "g": ORO[2]})


def sello_lacre():
    """Un sello de lacre rojo con el ojo de la Sombra (para el pie del ultimo folio)."""
    filas = [
        "....kkkkk....",
        "..kkRRRRRkk..",
        ".kRRrRRRrRRk.",
        ".kRrRRRRRrRk.",
        "kRRRkkkkkRRRk",
        "kRRkwwwwwkRRk",
        "kRRkwwkwwkRRk",
        "kRRkwwwwwkRRk",
        "kRRRkkkkkRRRk",
        ".kRrRRRRRrRk.",
        ".kRRrRRRrRRk.",
        "..kkRRRRRkk..",
        "....kkkkk....",
    ]
    return Lienzo(13, 13).sprite(filas, {"k": "#2a0406", "R": ROJO[3], "r": ROJO[5], "w": "#e8d8b0"})


# ---------------------------------------------------------------- montar
def montar():
    A = Atlas(256, 256)
    for k, t in nueve(tapa(), ESQ_TAPA):
        A.poner("tapa_" + k, t)
    A.poner("cap_rojo", capital(ROJO, ORO, ORO[4]))
    A.poner("cap_azul", capital(AZUL, ORO, ORO[4]))
    A.poner("cap_negro", capital(["#050307", "#0c0810", "#16101a", "#241a2a"], ROJO, ROJO[5]))
    A.poner("cap_verde", capital(VERDE, ORO, ORO[4]))
    A.poner("floron", floron())
    A.poner("fin", fin())
    A.poner("cinta", cinta())
    for k, t in nueve(marco_miniatura(), MARCO_MIN):
        A.poner("marco_" + k, t)
    A.poner("esquina", esquina_pagina())
    A.poner("lacre", sello_lacre())
    return A


def guardar():
    A = montar()
    A.L.guardar(os.path.join(GUI, "codice.png"))
    pergamino().guardar(os.path.join(GUI, "codice_pergamino.png"))
    cuero().guardar(os.path.join(GUI, "codice_cuero.png"))
    java = A.java("com.darkcraft.client.ui", "CodiceTex", "textures/gui/codice.png",
                  "El atlas del codice: tapa, letras capitales, adornos, cinta y marco de las miniaturas")
    with open(os.path.join(REPO, "src/main/java/com/darkcraft/client/ui/CodiceTex.java"), "w", encoding="utf-8") as f:
        f.write(java)
    return A


if __name__ == "__main__":
    A = guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        from PIL import Image
        A.L.imagen().resize((A.L.w * 4, A.L.h * 4), Image.NEAREST).save(os.path.join(d, "codice_atlas.png"))
        p = pergamino().imagen()
        big = Image.new("RGBA", (512, 512))
        for y in range(2):
            for x in range(2):
                big.paste(p, (x * 256, y * 256))
        big.resize((1024, 1024), Image.NEAREST).save(os.path.join(d, "pergamino_2x2.png"))
    print("codice: hecho")
