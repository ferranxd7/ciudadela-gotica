"""
Los marcados del Crater de Sangre (parte 7u): los de Morvain a los que la Sombra saco de la muerte antes que a ti y les
puso su marca para que rompieran sus cadenas. No pudieron. Siguen al pie de las anclas, de rodillas ante la cadena que
sube al cielo, con la marca encendida en el pecho (un ojo en un aro roto con tres espinas: la de la cinematica del
principio). La piel gris y agrietada con venas rojas, los grilletes rotos en las munecas. Tres:

  0 rompedor: el que golpea la cadena. Grande (el juego lo dibuja a 1,15), desnudo de cintura arriba con el delantal de
    cuero del herrero, calvo, la marca a fuego en la frente y en el pecho, barba de ceniza, y el mazo de forja a dos
    manos. El mazazo (lo alza por encima de la cabeza y lo estrella: la onda), el barrido y, de rodillas, golpea el suelo.
  1 caballero: la armadura negra con la marca en la coraza, el yelmo cerrado con tres espinas de hierro (por la rendija,
    la luz roja), la capa y el faldon rotos, la espada y el escudo con el ojo pintado. El tajo, la estocada, el escudazo
    y se cubre (el bloqueo).
  2 cazador: el gaban largo con capucha, el embozo que le tapa la cara (los ojos le lucen), la aljaba a la espalda, el
    arco en la izquierda y una flecha en la derecha. El disparo, la flecha negra (tensa mucho rato), la punzada con la
    flecha y el desliz (se agacha y se deshace en sombra: el juego lo lleva a otro sitio).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "marcado"
VARIANTES = ("mazo", "espada", "arco")
ESCALA = 1.04

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r)
    # ---- lo comun: los grilletes rotos de las munecas, con un eslabon colgando
    for n in ("d", "i"):
        r.pieza(f"grillete_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[
            Box(-2.45, 3.4, -2.45, 4.9, 1.8, 4.9, "grillete"),
            Box(-0.5, 5.2, -2.9, 1.0, 2.4, 1.0, "eslabon"),
        ])
    # ---- 0, el rompedor: la barba, el delantal (el peto y el faldon), el cinto y el mazo
    r.pieza("barba", "cabeza", (0, 0, 0), boxes=[
        Box(-3.6, -3.0, -4.65, 7.2, 3.0, 1.0, "barba"),
        Box(-2.6, 0.0, -4.4, 5.2, 3.2, 1.2, "barba", uv="barba_bajo"),
    ])
    r.pieza("peto", "torso", (0, 0, 0), boxes=[Box(-3.6, -5.0, -2.55, 7.2, 5.0, 0.5, "peto")])
    r.pieza("delantal", "cadera", (0, 0, 0), rot=(-3, 0, 0), boxes=[Box(-4.0, -0.4, -2.75, 8.0, 10.5, 0.6, "delantal")])
    r.pieza("cinto", "torso", (0, 0, 0), boxes=[Box(-4.55, -1.6, -2.55, 9.1, 1.6, 5.1, "cinto")])
    r.pieza("mazo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.75, -0.75, -19.0, 1.5, 1.5, 23.0, "astil"),
        Box(-2.8, -4.4, -24.4, 5.6, 8.8, 5.6, "cabeza_mazo"),
        Box(-3.1, -4.7, -22.4, 6.2, 1.2, 1.6, "cabeza_mazo", uv="aro_mazo"),
    ])
    # ---- 1, el caballero: el yelmo con sus espinas, la coraza, las hombreras, la capa, el faldon, la espada y el escudo
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4.0, -8.0, -4.0, 8, 8, 8, "yelmo", grow=g(0.6)),
        Box(-0.6, -12.2, -0.6, 1.2, 3.6, 1.2, "espina"),
    ])
    r.pieza("espina_d", "yelmo", (-2.6, -8.4, 0.4), rot=(0, 0, 26), boxes=[Box(-0.5, -3.0, -0.5, 1.0, 3.0, 1.0, "espina")])
    r.pieza("espina_i", "yelmo", (2.6, -8.4, 0.4), rot=(0, 0, -26), boxes=[Box(-0.5, -3.0, -0.5, 1.0, 3.0, 1.0, "espina")])
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, "coraza", grow=g(0.5))])
    for n in ("d", "i"):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-2.8, -3.2, -2.7, 5.6, 3.6, 5.4, f"hombrera_{n}")])
    r.pieza("capa", "torso", (0, -11.6, 2.7), rot=(5, 0, 0), boxes=[Box(-4.6, 0, 0, 9.2, 19.0, 0.6, "capa")])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2, 8, 7, 4, "faldon", grow=g(0.42))])
    r.pieza("espada", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2.5, 1, 1, 5, "puno"),
        Box(-1, -1, 2.5, 2, 2, 1.5, "pomo"),
        Box(-3.2, -0.6, -3.6, 6.4, 1.2, 1.1, "guarda"),
        Box(-0.5, -1.1, -21.5, 1, 2.2, 18, "hoja"),
        Box(-0.5, -0.6, -23.5, 1, 1.2, 2, "hoja", uv="hoja_punta"),
    ])
    r.pieza("escudo", "antebrazo_i", (0, 0, 0), boxes=[
        Box(2.3, -4.0, -5.0, 1, 12, 10, "escudo"),
        Box(2.3, 8.0, -3.0, 1, 2.5, 6, "escudo", uv="escudo_punta"),
    ])
    # ---- 2, el cazador: la capucha, el embozo, el gaban y sus faldones, la aljaba, el arco y la flecha
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 8, 8, "capucha", grow=g(0.55)),
        Box(-2.0, -7.2, 4.5, 4.0, 5.0, 1.4, "capucha", uv="capucha_pico"),
    ])
    r.pieza("embozo", "cabeza", (0, 0, 0), boxes=[Box(-4.3, -4.0, -4.6, 8.6, 4.4, 1.0, "embozo")])
    r.pieza("gaban", "torso", (0, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, "gaban", grow=g(0.42))])
    r.pieza("faldon_tras", "cadera", (0, 0, 0), boxes=[Box(-4.4, -0.5, 1.7, 8.8, 11.0, 0.8, "faldon_tras")])
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[
            Box(-2.75 if s < 0 else 1.85, -0.5, -2.4, 0.9, 10.4, 4.4, f"faldon_lado_{n}"),
        ])
    r.pieza("aljaba", "torso", (0, -10.5, 2.6), rot=(0, 0, 24), boxes=[
        Box(-1.3, 0, 0, 2.6, 10.0, 2.4, "aljaba"),
        Box(-1.1, -2.6, 0.2, 2.2, 2.6, 2.0, "plumas"),
    ])
    # el arco: la empunadura en la mano, las dos palas echadas hacia la cuerda y las puntas vueltas hacia fuera
    r.pieza("arco", "mano_i", (0, 0, 0), boxes=[Box(-0.8, -0.8, -2.2, 1.6, 1.6, 4.4, "empunadura")])
    r.pieza("pala_a", "arco", (0, 0, -2.0), rot=(11, 0, 0), boxes=[Box(-0.65, -0.65, -9.0, 1.3, 1.3, 9.2, "pala")])
    r.pieza("pala_b", "arco", (0, 0, 2.0), rot=(-11, 0, 0), boxes=[Box(-0.65, -0.65, -0.2, 1.3, 1.3, 9.2, "pala", uv="pala_b")])
    r.pieza("pala_curva_a", "pala_a", (0, 0, -9.0), rot=(-30, 0, 0), boxes=[Box(-0.55, -0.55, -3.2, 1.1, 1.1, 3.4, "pala", uv="punta_a")])
    r.pieza("pala_curva_b", "pala_b", (0, 0, 9.0), rot=(30, 0, 0), boxes=[Box(-0.55, -0.55, -0.2, 1.1, 1.1, 3.4, "pala", uv="punta_b")])
    r.pieza("cuerda", "arco", (0, 0, 0), boxes=[Box(-0.15, 1.45, -12.4, 0.3, 0.3, 24.8, "cuerda_arco")])
    r.pieza("flecha", "mano_d", (0, 0, 0), boxes=[
        Box(-0.3, -0.3, -12.5, 0.6, 0.6, 16.0, "astil_flecha"),
        Box(-0.6, -0.6, -14.0, 1.2, 1.2, 1.6, "punta_flecha"),
        Box(-0.1, -1.1, 1.6, 0.2, 2.2, 2.0, "pluma_flecha"),
    ])
    r.empaquetar()
    return r


DEL_ROMPEDOR = ["barba", "peto", "delantal", "cinto", "mazo"]
DEL_CABALLERO = ["yelmo", "espina_d", "espina_i", "coraza", "hombrera_d", "hombrera_i", "capa", "faldon", "espada", "escudo"]
DEL_CAZADOR = ["capucha", "embozo", "gaban", "faldon_tras", "faldon_d", "faldon_i", "aljaba", "arco", "pala_a", "pala_b",
               "pala_curva_a", "pala_curva_b", "cuerda", "flecha"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: DEL_CABALLERO + DEL_CAZADOR,
    1: DEL_ROMPEDOR + DEL_CAZADOR,
    2: DEL_ROMPEDOR + DEL_CABALLERO,
}

# ---------------------------------------------------------------- las texturas
# La piel gris azulada y agrietada (las grietas con un rojo sordo, como brasas bajo la ceniza), el hierro negro, el cuero
# viejo, la tela negra desgastada; la marca roja (en el brillo, el ojo luce).

PIEL = ["#2c2b31", "#3b3a41", "#4b4a52", "#5d5c64", "#706e76", "#838189"]
GRIETA = ["#3a0706", "#5c0d0b", "#82150f", "#a82214"]
NEGRO = ["#0b0b0d", "#131316", "#1c1c20", "#26262b", "#313137", "#3e3e45"]
HIERRO = ["#1a1a1e", "#26262c", "#34343b", "#45454e", "#585862", "#6e6e7a"]
CUERO = ["#140e0a", "#1f1510", "#2c1e16", "#3b291e", "#4b3527"]
CENIZA = ["#55524f", "#6c6863", "#85807a", "#9e9891"]
MADERA = ["#1d150e", "#2a1e14", "#38291b", "#473424"]
GRILLETE = ["#17171a", "#232327", "#323238", "#43434b"]
MARCA = "#6e0c0a"
MARCA_C = "#a5170f"
OJO = "#ff4a24"
OJO_C = "#ffb27a"
CUENCA = "#060506"

#: la marca (un ojo en un aro roto con tres espinas), 7 x 7: E espina, A aro, O ojo, P pupila
MARCA_7 = [
    "E..E..E",
    ".E.E.E.",
    ".AAAAA.",
    "A.OOO.A",
    "A.OPO.A",
    "..OOO.A",
    "..AAA..",
]


def _marca(x0, y0, w, x, y):
    """Lo que toca de la marca en (x, y) si empieza en (x0, y0): None, 'E', 'A', 'O' o 'P'."""
    i, j = y - y0, x - x0
    if 0 <= i < 7 and 0 <= j < 7:
        ch = MARCA_7[i][j]
        return None if ch == "." else ch
    return None


def materiales(variante):
    piel = ruido(PIEL[1:5], 2101, grano=0.4, base=0.55)

    def agrietada(c, cara, x, y, semilla):
        """Las grietas rojas de la piel: lineas finas que bajan torcidas."""
        k = (x * 5 + y * 3 + semilla * 7 + {"frente": 0, "espalda": 2, "der": 1, "izq": 3, "arriba": 4, "abajo": 5}.get(cara, 0)) % 37
        if k == 0:
            return mezcla(c, GRIETA[2], 0.7)
        if k == 19 and y % 2 == 0:
            return mezcla(c, GRIETA[1], 0.45)
        return c

    def pielg(cara, x, y, w, h, semilla=1):
        return agrietada(piel(cara, x, y, w, h), cara, x, y, semilla)

    negra = tela(NEGRO[1:], 2111 + variante, pliegue=3, grano=0.16)
    hierro = metal(HIERRO, 2121)
    cuero = ruido(CUERO[1:], 2131, grano=0.3, base=0.5)

    def rota(c, cara, x, y, h):
        """El borde de abajo de la tela, deshilachado."""
        if y == h - 1 and (x * 7 + 3) % 4 == 0:
            return None
        if y == h - 2 and (x * 5 + 1) % 7 == 0:
            return None
        return c

    def cabeza(cara, x, y, w, h):
        c = pielg(cara, x, y, w, h, 3)
        if variante == 0:
            if cara == "frente":
                # la marca a fuego en la frente (pequena: tres espinas y el ojo) y los ojos hundidos
                if y == 1 and x in (2, 4, 5):
                    return MARCA
                if y == 2 and 3 <= x <= 4:
                    return MARCA_C
                if y == 4 and x in (1, 2, 5, 6):
                    return CUENCA
                if y == 3 and x in (1, 2, 5, 6):
                    return oscuro(c, 0.35)
                if y >= 6 and 2 <= x <= 5:
                    return oscuro(c, 0.3) if y == 6 else c
            if cara == "arriba":
                return claro(c, 0.06)
            return c
        if cara == "frente":
            # los ojos (el cazador: por encima del embozo; el caballero no la ensena: el yelmo)
            if y == 3 and x in (1, 2, 5, 6):
                return CUENCA
            if y == 2 and x in (1, 2, 5, 6):
                return oscuro(c, 0.3)
        return c

    def barba(cara, x, y, w, h):
        c = CENIZA[(x * 3 + y * 5) % 4]
        if cara == "frente" and (x + y) % 5 == 0:
            return oscuro(c, 0.25)
        return c

    def torso(cara, x, y, w, h):
        if variante == 0:
            c = pielg(cara, x, y, w, h, 5)
            if cara == "frente":
                m = _marca(1, 0, w, x, y)
                if m:
                    return MARCA_C if m in ("O", "P") else MARCA
                if y in (8, 9) and x in (2, 5):
                    return oscuro(c, 0.18)
            return c
        if variante == 1:
            return oscuro(negra(cara, x, y, w, h), 0.1)
        return cuero(cara, x, y, w, h)

    def peto(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara == "frente":
            if y == 0 or x in (0, w - 1):
                return oscuro(c, 0.25)
            if (x * 3 + y * 7) % 19 == 0:
                return CUERO[0]                                # las quemaduras de la fragua
        return c

    def delantal(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        c = cuero(cara, x, y, w, h)
        if cara == "frente":
            if (x * 3 + y * 7) % 23 == 0:
                return CUERO[0]
            if x in (0, w - 1):
                return oscuro(c, 0.2)
        return rota(c, cara, x, y, h)

    def cinto(cara, x, y, w, h):
        if cara == "frente" and 3 <= x <= 5:
            return GRILLETE[3] if y != 1 else GRILLETE[2]  # la hebilla
        return CUERO[(x + y) % 2 + 1]

    def astil(cara, x, y, w, h):
        return madera(MADERA, 2141, "z", grano=0.3)(cara, x, y, w, h)

    def cabeza_mazo(cara, x, y, w, h):
        c = metal(HIERRO, 2151)(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            return claro(c, 0.12)                              # las bocas, gastadas de golpear
        if (x * 5 + y * 3) % 17 == 0:
            return mezcla(c, GRIETA[1], 0.5)                   # la sangre seca
        return c

    def yelmo(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "frente":
            if y == 3 and 1 <= x <= 6:
                return CUENCA if x not in (1, 6) else oscuro(c, 0.6)   # la rendija
            if y == 4 and 2 <= x <= 5:
                return oscuro(c, 0.35)
            if y >= 5 and x in (3, 4) and y % 2 == 1:
                return CUENCA                                  # los respiraderos
            if x in (3, 4) and y <= 2:
                return claro(c, 0.12)                          # la arista
        if cara == "arriba":
            return oscuro(c, 0.1)
        return c

    def espina(cara, x, y, w, h):
        c = HIERRO[2 + (y % 2)]
        return claro(c, 0.15) if y == 0 else c

    def coraza(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "frente":
            m = _marca(1, 2, w, x, y)
            if m:
                return MARCA_C if m in ("O", "P") else MARCA
            if y == 0:
                return claro(c, 0.1)
            if y == h - 1:
                return oscuro(c, 0.25)
        return c

    def hombrera(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "arriba" and (x + y) % 3 == 0:
            return claro(c, 0.12)
        if y == h - 1:
            return oscuro(c, 0.3)
        return c

    def capa(cara, x, y, w, h):
        c = negra(cara, x, y, w, h)
        if cara == "abajo":
            return None
        # rota por abajo a jirones (de mas largos a mas cortos)
        k = (x * 7 + 5) % 5
        if y >= h - 1 - k:
            return None
        return c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = negra(cara, x, y, w, h)
        if cara == "frente" and 3 <= x <= 4 and y <= 3:
            return MARCA                                       # el ribete rojo del tabardo
        return rota(c, cara, x, y, h)

    def puno(cara, x, y, w, h):
        return CUERO[1 + (y % 2)]

    def pomo(cara, x, y, w, h):
        return MARCA_C if cara in ("arriba", "abajo") else HIERRO[3]

    def guarda(cara, x, y, w, h):
        return HIERRO[3 + (x % 2)]

    def hoja(cara, x, y, w, h):
        c = metal(["#2a2a30", "#3c3c44", "#50505a", "#686872"], 2161)(cara, x, y, w, h)
        if cara in ("der", "izq") and x == 0:
            return claro(c, 0.18)                              # el filo
        if (x * 3 + y * 11) % 29 == 0:
            return mezcla(c, GRIETA[0], 0.6)
        return c

    def escudo(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "izq":
            # el ojo pintado en la cara de fuera del escudo (la de su izquierda: la de fuera del brazo)
            cx, cy = (w - 1) / 2.0, (h - 1) / 2.0 - 1
            d = ((x - cx) / 3.2) ** 2 + ((y - cy) / 2.2) ** 2
            if d <= 0.35:
                return "#120202"
            if d <= 1.0:
                return MARCA_C
            if 1.4 <= d <= 2.2:
                return MARCA
            if x in (0, w - 1) or y == 0:
                return claro(c, 0.08)
        return c

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = negra(cara, x, y, w, h)
        if cara == "frente" and 1 <= x <= w - 2 and 2 <= y:
            return None                                       # el hueco de la cara (se ve la cabeza y el embozo)
        return c

    def embozo(cara, x, y, w, h):
        c = oscuro(negra(cara, x, y, w, h), 0.15)
        if cara == "frente" and y == 0:
            return oscuro(c, 0.2)
        return c

    def gaban(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara == "frente":
            if x in (3, 4):
                return oscuro(c, 0.3)                          # la abertura
            if y in (2, 5, 8) and x in (2, 5):
                return GRILLETE[2]                             # los broches
            if y == 3 and x == 6:
                return MARCA                                   # la marca cosida al pecho (pequena)
            if y == 4 and x in (5, 6):
                return MARCA_C
        if cara == "espalda" and (x * 3 + y) % 11 == 0:
            return oscuro(c, 0.3)
        return c

    def faldon_cuero(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = cuero(cara, x, y, w, h)
        return rota(oscuro(c, 0.06 * y / max(1, h - 1)), cara, x, y, h)

    def aljaba(cara, x, y, w, h):
        c = NEGRO[1 + (x + y) % 3]
        return GRILLETE[3] if y in (1, h - 2) else c

    def plumas(cara, x, y, w, h):
        return ["#6a1a14", "#8c2418", "#d8cfc0"][(x + y) % 3]

    def empunadura(cara, x, y, w, h):
        return CUERO[1 + (y % 2)]

    def pala(cara, x, y, w, h):
        return madera(["#3a2616", "#4c331e", "#5e4027", "#704d30"], 2171, "z", grano=0.25)(cara, x, y, w, h)

    def cuerda_arco(cara, x, y, w, h):
        return "#a39886"

    def astil_flecha(cara, x, y, w, h):
        return "#3b2c1e" if (y + x) % 2 else "#2e2217"

    def punta_flecha(cara, x, y, w, h):
        return HIERRO[4]

    def pluma_flecha(cara, x, y, w, h):
        return "#4a1a18" if y % 2 else "#2a1010"

    def brazo(cara, x, y, w, h):
        if variante == 0:
            return pielg(cara, x, y, w, h, 7)
        if variante == 1:
            return oscuro(negra(cara, x, y, w, h), 0.15)
        return cuero(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if variante == 0:
            return pielg(cara, x, y, w, h, 9)
        if y >= h - 2:
            return pielg(cara, x, y, w, h, 9)                  # las manos grises
        if variante == 1:
            return hierro(cara, x, y, w, h)                    # los brazales
        return cuero(cara, x, y, w, h)

    def grillete(cara, x, y, w, h):
        c = GRILLETE[1 + (x + y) % 3]
        return claro(c, 0.1) if cara == "arriba" else c

    def eslabon(cara, x, y, w, h):
        return GRILLETE[2 + (y % 2)]

    def muslo(cara, x, y, w, h):
        if variante == 1:
            return hierro(cara, x, y, w, h)
        return oscuro(negra(cara, x, y, w, h), 0.25) if variante == 0 else oscuro(cuero(cara, x, y, w, h), 0.2)

    def pierna(cara, x, y, w, h):
        if cara == "abajo":
            return "#16110d"
        if y >= h - 3:
            return CUERO[0 + (x % 2)] if variante != 1 else HIERRO[1 + (x % 2)]   # las botas
        if variante == 1:
            return hierro(cara, x, y, w, h)
        return oscuro(negra(cara, x, y, w, h), 0.3)

    return {
        "cabeza": sombra(cabeza, abajo=0.16), "barba": barba,
        "torso": sombra(torso), "peto": peto, "delantal": delantal, "cinto": cinto,
        "astil": astil, "cabeza_mazo": sombra(cabeza_mazo, abajo=0.1),
        "yelmo": sombra(yelmo, abajo=0.1), "espina": espina, "coraza": sombra(coraza),
        "hombrera_d": sombra(hombrera), "hombrera_i": sombra(hombrera), "capa": capa, "faldon": faldon,
        "puno": puno, "pomo": pomo, "guarda": guarda, "hoja": hoja, "escudo": sombra(escudo, abajo=0.1),
        "capucha": sombra(capucha, abajo=0.08), "embozo": embozo, "gaban": sombra(gaban),
        "faldon_tras": faldon_cuero, "faldon_lado_d": faldon_cuero, "faldon_lado_i": faldon_cuero,
        "aljaba": aljaba, "plumas": plumas, "empunadura": empunadura, "pala": pala, "cuerda_arco": cuerda_arco,
        "astil_flecha": astil_flecha, "punta_flecha": punta_flecha, "pluma_flecha": pluma_flecha,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.16), "antebrazo_i": sombra(antebrazo, abajo=0.16),
        "grillete": grillete, "eslabon": eslabon,
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
    }


def brillo(variante):
    """Lo que luce: la marca (el ojo, mas; el aro, menos), los ojos y la rendija del yelmo."""

    def marca(x0, y0):
        def f(cara, x, y, w, h):
            if cara != "frente":
                return None
            m = _marca(x0, y0, w, x, y)
            if m == "P":
                return OJO_C
            if m == "O":
                return OJO
            if m in ("A", "E"):
                return "#8f160c"
            return None
        return f

    def ojos(cara, x, y, w, h):
        if cara == "frente" and y == (4 if variante == 0 else 3) and x in (1, 2, 5, 6):
            return OJO if x in (2, 5) else "#b31c0e"
        if variante == 0 and cara == "frente" and (y == 1 and x in (2, 4, 5) or y == 2 and 3 <= x <= 4):
            return "#c8240f"                                   # la marca de la frente
        return None

    b = {"cabeza": ojos}
    if variante == 0:
        b["torso"] = marca(1, 0)
    if variante == 1:
        b["coraza"] = marca(1, 2)
        b["yelmo"] = lambda cara, x, y, w, h: (OJO if 2 <= x <= 5 else "#b31c0e") if cara == "frente" and y == 3 and 1 <= x <= 6 else None
        b["escudo"] = lambda cara, x, y, w, h: (OJO if ((x - (w - 1) / 2.0) / 3.2) ** 2 + ((y - (h - 1) / 2.0 + 1) / 2.2) ** 2 <= 0.35
                                                else None) if cara == "izq" else None
    if variante == 2:
        b["gaban"] = lambda cara, x, y, w, h: (OJO if (y == 3 and x == 6) or (y == 4 and x in (5, 6)) else None) if cara == "frente" else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 12) y
# _arma = hacia donde apunta lo que lleva (el mazo, la espada y la flecha, a -Z de la mano; el arco tambien: se pone de pie
# apuntandolo arriba). _agarra: la izquierda en el astil del mazo, tantos pixeles por detras (+) o por delante (-).

BASE = dict(raiz=raiz(-0.2), torso=(6, 0, 0), cabeza=(8, 0, 0), muslo_d=(-3, 0, 2), pierna_d=(5, 0, 0), muslo_i=(2, 0, -2),
            pierna_i=(4, 0, 0))
PORTE = {
    # el mazo al hombro derecho (la cabeza a la espalda); la izquierda suelta
    "mazo": dict(_mano_d=("h", -0.6, -3.2, 4.4), _arma=(0.12, 0.55, -0.83), brazo_i=(4, 0, -6), antebrazo_i=(-12, 0, 0)),
    # la espada colgando por delante; el escudo un poco adelantado
    "espada": dict(_mano_d=("h", -0.6, -9.6, 2.0), _arma=(0.05, -0.5, 0.86), brazo_i=(-18, 0, -8), antebrazo_i=(-30, 0, 0)),
    # el arco de pie en la izquierda; la flecha abajo en la derecha
    "arco": dict(_mano_i=("h", 0.6, -9.4, 2.4), _arma_i=(0.0, 1.0, 0.12), _mano_d=("h", -0.4, -9.8, 1.4), _arma=(0.05, -0.6, 0.8)),
}
GUARDIA = dict(raiz=raiz(-1.0), torso=(14, 10, 0), cabeza=(-4, -10, 0), muslo_d=(8, 0, 5), pierna_d=(14, 0, 0), muslo_i=(-20, 0, -5),
               pierna_i=(22, 0, 0))
LISTO = {
    # el mazo cruzado por delante, a dos manos, la cabeza arriba a su izquierda
    "mazo": con(GUARDIA, torso=(14, 22, 0), cabeza=(-4, -22, 0), _mano_d=("h", 2.0, -7.0, 4.6), _arma=(0.55, 0.72, 0.42), _agarra=-8),
    # la espada delante apuntando a su presa, el escudo alzado
    "espada": con(GUARDIA, _mano_d=("h", 0.8, -5.4, 7.2), _arma=(0.08, 0.32, 0.94), brazo_i=(-64, 0, -12), antebrazo_i=(-46, 0, 0)),
    # el arco delante y bajo, la flecha ya en la cuerda
    "arco": con(GUARDIA, torso=(10, 30, 0), cabeza=(-4, -30, 0), _mano_i=("h", 1.0, -4.6, 8.0), _arma_i=(0.0, 1.0, 0.25),
                _mano_d=("h", 4.6, -4.4, 4.4), _arma=(0.15, 0.1, 1)),
}
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# de rodillas
RODILLAS = dict(raiz=raiz(-9.2, 0.8), torso=(12, 0, 0), cabeza=(20, 0, 0), muslo_d=(-8, 0, 5), pierna_d=(98, 0, 0),
                muslo_i=(-8, 0, -5), pierna_i=(98, 0, 0))
# caido de bruces (el juego lo baja un poco)
BRUCES = dict(raiz=raiz(2.2, 14.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-4, 22, 0),
              muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
              brazo_d=(-150, 0, 20), antebrazo_d=(-10, 0, 0), brazo_i=(-30, 0, -40), antebrazo_i=(-70, 0, 0))


def animaciones(r=None):
    """(bucles, acciones): los bucles por nombre y las acciones en el orden de sus numeros."""
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses, **kw):
        a = Anim(nombre, largo, bucle=True).poses(poses, r, **kw)
        B.append(a)
        return a

    def accion(nombre, largo, poses, golpes=()):
        a = Anim(nombre, largo, golpes=golpes).poses(poses, r)
        A.append(a)
        return a

    def fija(pose):
        return resolver(r, pose)

    # ---- quieto: respira hondo, los hombros suben y bajan; la cabeza ladeada
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.5, con(BASE, torso=(3, 2, 1), cabeza=(4, 4, -3), raiz=raiz(-0.1))),
        (2.8, con(BASE, torso=(7, -2, -1), cabeza=(10, -4, 2), raiz=raiz(-0.35))),
        (4.0, BASE),
    ])
    # ---- andar: el paso pesado, arrastrando un poco los pies
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-24, 18, 6, 18, 5, 0.0), (-4, 4, 4, 32, 0, 0.6), (18, -24, 18, 6, -5, 0.0), (4, -4, 32, 4, 0, 0.6), (-24, 18, 6, 18, 5, 0.0))):
        pasos.append((1.4 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, -ty * 0.4), "cabeza": (8, -ty * 0.6, ty * 0.3), "raiz": raiz(up),
                                    "faldon_tras": (abs(md) * 0.3, 0, 0), "delantal": (-abs(md) * 0.25, 0, 0)}))
    bucle("andar", 1.4, pasos)
    # ---- el brazo de lo que lleva (se suma al quieto) y en guardia
    for arma, p in PORTE.items():
        q = fija(con(BASE, p))
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    for arma, p in LISTO.items():
        l0 = fija(p)
        l1 = fija(con(p, raiz=raiz(-1.6), torso=(p["torso"][0] + 3, p["torso"][1] - 3, 0)))
        bucle(f"listo_{arma}", 1.6, [(0.0, l0), (0.8, l1), (1.6, l0)])
    # ---- de rodillas, cada uno lo suyo: el rompedor golpea el suelo con el mazo (a una mano, despacio); el caballero, la
    # espada clavada delante y las dos manos en el pomo; el cazador, el arco de pie delante y la cabeza gacha
    R0 = con(RODILLAS, brazo_i=(-20, 0, -10), antebrazo_i=(-30, 0, 0))
    M_ALZA = con(R0, torso=(2, 10, 0), cabeza=(6, 4, 0), _mano_d=("h", -0.8, 5.0, 4.0), _arma=(0.1, 0.85, -0.5))
    M_DA = con(R0, torso=(30, -4, 0), cabeza=(30, -2, 0), _mano_d=("h", 1.0, -5.0, 9.0), _arma=(0.1, -0.55, 0.83))
    bucle("rezando_mazo", 2.4, [(0.0, M_ALZA), (1.2, M_ALZA), (1.6, M_DA), (1.9, con(M_DA, torso=(32, -4, 0))), (2.4, M_ALZA)])
    E_RZ = con(RODILLAS, torso=(10, 0, 0), cabeza=(26, 0, 0), _mano_d=("h", 3.0, -2.6, 6.8), _arma=(0.0, -1, 0.08), _agarra=2.6)
    bucle("rezando_espada", 4.0, [(0.0, E_RZ), (2.0, con(E_RZ, torso=(13, 0, 0), cabeza=(32, 0, 0))), (4.0, E_RZ)])
    A_RZ = con(RODILLAS, torso=(14, 0, 0), cabeza=(30, 0, 0), _mano_i=("h", 0.4, -3.6, 6.6), _arma_i=(0.0, 1.0, 0.05),
               _mano_d=("h", 3.4, -6.0, 3.4), _arma=(0.1, -0.8, 0.5))
    bucle("rezando_arco", 4.4, [(0.0, A_RZ), (2.2, con(A_RZ, torso=(17, 0, 0), cabeza=(36, 0, 0))), (4.4, A_RZ)])
    # ---- caido de bruces (como un muerto mas)
    bucle("tumbado", 6.0, [(0.0, BRUCES), (3.0, con(BRUCES, cabeza=(-2, 22, 0))), (6.0, BRUCES)])

    # ================================================================ acciones
    Mz, Es, Ar = LISTO["mazo"], LISTO["espada"], LISTO["arco"]

    # ---- 1 mazazo (el rompedor): alza el mazo a dos manos por encima de la cabeza, lo sostiene y lo estrella delante
    MZ_ALTO = con(Mz, raiz=raiz(0.0, 1.4), torso=(-18, 6, 0), cabeza=(-20, -4, 0), muslo_d=(8, 0, 4), pierna_d=(8, 0, 0),
                  muslo_i=(-14, 0, -4), pierna_i=(10, 0, 0), _mano_d=("h", 4.0, 7.0, 0.5), _arma=(0.05, 0.55, -0.84), _agarra=-5)
    MZ_BAJO = con(Mz, raiz=raiz(-3.8, -2.4), torso=(48, -4, 0), cabeza=(-28, 2, 0), muslo_d=(16, 0, 4), pierna_d=(32, 0, 0),
                  muslo_i=(-54, 0, -4), pierna_i=(56, 0, 0), _mano_d=("h", 4.4, -3.4, 7.0), _arma=(0.05, -0.62, 0.78), _agarra=-5)
    accion("mazazo", 1.9, [
        (0.00, Mz),
        (0.55, MZ_ALTO),
        (0.85, con(MZ_ALTO, raiz=raiz(0.2, 1.6), torso=(-22, 6, 0))),
        (1.00, MZ_BAJO),
        (1.35, con(MZ_BAJO, torso=(46, -4, 0))),
        (1.90, Mz),
    ], golpes=(1.00,))
    # ---- 2 barrido (el rompedor): el mazo de derecha a izquierda a la altura de la cintura (a una mano: la izquierda suelta,
    # abierta para no irse detras del golpe)
    M1 = con(sin(Mz, "_agarra"), brazo_i=(-30, 0, -40), antebrazo_i=(-20, 0, 0))
    BM_D = con(M1, raiz=raiz(-1.4, 0.8), torso=(10, 56, 0), cabeza=(-2, -50, 0), _mano_d=("h", 0.6, -4.6, 4.6), _arma=(-1, 0.1, -0.2))
    BM_F = con(M1, raiz=raiz(-2.0, -0.6), torso=(16, 12, 0), cabeza=(-6, -12, 0), _mano_d=("h", 4.0, -5.6, 6.0), _arma=(0.2, 0.0, 1))
    BM_I = con(M1, raiz=raiz(-2.4, -1.8), torso=(20, -36, 0), cabeza=(-8, 30, 0), muslo_i=(-34, 0, -6), pierna_i=(30, 0, 0),
               _mano_d=("h", 6.0, -5.0, 4.4), _arma=(1, 0.05, 0.3), brazo_i=(-20, 0, -60))
    accion("barrido", 1.6, [
        (0.00, Mz),
        (0.50, BM_D),
        (0.74, con(BM_D, torso=(10, 62, 0), cabeza=(-2, -54, 0))),
        (0.84, BM_F),
        (0.94, BM_I),
        (1.22, con(BM_I, torso=(20, -40, 0))),
        (1.60, Mz),
    ], golpes=(0.84,))
    # ---- 3 tajo (el caballero): la espada atras por encima del hombro y la descarga en diagonal
    TJ_ATRAS = con(Es, raiz=raiz(-0.8, 1.0), torso=(2, 36, 0), cabeza=(-6, -30, 0), _mano_d=("h", -3.6, 5.0, -1.0), _arma=(-0.3, 0.7, -0.65))
    TJ_DA = con(Es, raiz=raiz(-2.6, -2.2), torso=(28, -30, 0), cabeza=(-16, 26, 0), muslo_i=(-36, 0, -4), pierna_i=(34, 0, 0),
                _mano_d=("h", 5.0, -3.6, 7.0), _arma=(0.6, -0.55, 0.58))
    accion("tajo", 1.1, [
        (0.00, Es),
        (0.32, TJ_ATRAS),
        (0.48, con(TJ_ATRAS, torso=(0, 40, 0))),
        (0.56, TJ_DA),
        (0.82, con(TJ_DA, torso=(26, -34, 0))),
        (1.10, Es),
    ], golpes=(0.56,))
    # ---- 4 estocada (el caballero): recoge la espada junto a la cadera y se tira a fondo
    ES_RECOGE = con(Es, raiz=raiz(-1.6, 1.4), torso=(8, 30, 0), cabeza=(-4, -30, 0), _mano_d=("h", -2.0, -3.0, -1.0), _arma=(0.1, 0.12, 1))
    ES_FONDO = con(Es, raiz=raiz(-3.6, -3.6), torso=(26, -14, 0), cabeza=(-14, 14, 0), muslo_d=(26, 0, 4), pierna_d=(12, 0, 0),
                   muslo_i=(-62, 0, -4), pierna_i=(46, 0, 0), _mano_d=("h", 2.0, -1.6, 10.6), _arma=(0.05, 0.05, 1))
    accion("estocada", 1.3, [
        (0.00, Es),
        (0.40, ES_RECOGE),
        (0.56, con(ES_RECOGE, raiz=raiz(-1.8, 1.6))),
        (0.66, ES_FONDO),
        (0.96, con(ES_FONDO, torso=(24, -12, 0))),
        (1.30, Es),
    ], golpes=(0.66,))
    # ---- 5 escudazo (el caballero): echa el escudo atras y lo estampa delante con el hombro
    EZ_ATRAS = con(Es, raiz=raiz(-1.4, 1.2), torso=(8, -30, 0), cabeza=(-4, 26, 0), brazo_i=(-20, 0, -30), antebrazo_i=(-70, 0, 0))
    EZ_DA = con(Es, raiz=raiz(-2.4, -2.6), torso=(20, 24, 0), cabeza=(-10, -22, 0), muslo_i=(-40, 0, -4), pierna_i=(34, 0, 0),
                brazo_i=(-84, 0, -2), antebrazo_i=(-16, 0, 0))
    accion("escudazo", 0.95, [
        (0.00, Es),
        (0.28, EZ_ATRAS),
        (0.42, EZ_DA),
        (0.66, con(EZ_DA, torso=(18, 22, 0))),
        (0.95, Es),
    ], golpes=(0.42,))
    # ---- 6 bloqueo (el caballero): el escudo arriba, recibe el golpe y retrocede un poco
    BQ = con(Es, raiz=raiz(-1.6, 0.6), torso=(18, -8, 0), cabeza=(6, 8, 0), brazo_i=(-92, 0, -8), antebrazo_i=(-30, 0, 0),
             _mano_d=("h", -1.0, -6.0, 4.0), _arma=(0.05, 0.4, 0.9))
    accion("bloqueo", 0.6, [
        (0.00, BQ),
        (0.10, con(BQ, raiz=raiz(-1.2, 2.2), torso=(6, -10, 0))),
        (0.30, BQ),
        (0.60, Es),
    ])
    # ---- 7 disparo (el cazador): alza el arco, tensa hasta la oreja y suelta
    AR_ALZA = con(Ar, torso=(4, 78, 0), cabeza=(-6, -76, 0), _mano_i=("h", 0.0, 1.4, 10.8), _arma_i=(0.0, 1.0, -0.05),
                  _mano_d=("h", 2.0, 1.8, 6.4), _arma=(0.0, 0.0, 1))
    AR_TENSA = con(AR_ALZA, _mano_d=("h", 2.0, 2.2, 1.0), _arma=(0.0, 0.0, 1))
    AR_SUELTA = con(AR_ALZA, raiz=raiz(-0.8, -0.4), _mano_d=("h", -2.6, 2.0, -0.8), _arma=(-0.3, 0.6, -0.7))
    accion("disparo", 1.3, [
        (0.00, Ar),
        (0.30, AR_ALZA),
        (0.62, AR_TENSA),
        (0.84, con(AR_TENSA, raiz=raiz(-0.2, 0.2))),
        (0.90, AR_SUELTA),
        (1.30, Ar),
    ], golpes=(0.86,))
    # ---- 8 flecha negra (el cazador): apunta mucho rato con todo tenso (temblando) y suelta
    accion("flecha_negra", 2.0, [
        (0.00, Ar),
        (0.30, AR_ALZA),
        (0.70, AR_TENSA),
        (1.00, con(AR_TENSA, cabeza=(-10, -76, 0), raiz=raiz(-0.4, 0.4))),
        (1.20, con(AR_TENSA, torso=(5, 79, 1))),
        (1.40, con(AR_TENSA, torso=(3, 77, -1), cabeza=(-10, -76, 0))),
        (1.46, AR_SUELTA),
        (2.00, Ar),
    ], golpes=(1.44,))
    # ---- 9 punzada (el cazador): clava la flecha de la mano, rapido, de abajo arriba
    PZ_RECOGE = con(Ar, raiz=raiz(-1.6, 1.0), torso=(10, 24, 0), cabeza=(-4, -24, 0), _mano_d=("h", -1.8, -6.0, 0.0), _arma=(0.1, 0.4, 0.9))
    PZ_DA = con(Ar, raiz=raiz(-3.0, -2.6), torso=(26, -16, 0), cabeza=(-14, 16, 0), muslo_i=(-46, 0, -4), pierna_i=(38, 0, 0),
                _mano_d=("h", 2.4, -1.0, 10.0), _arma=(0.1, 0.35, 0.93))
    accion("punzada", 0.85, [
        (0.00, Ar),
        (0.22, PZ_RECOGE),
        (0.38, PZ_DA),
        (0.60, con(PZ_DA, torso=(24, -14, 0))),
        (0.85, Ar),
    ], golpes=(0.38,))
    # ---- 10 desliz (el cazador): se agacha de golpe y salta de lado (el juego lo deshace en sombra y lo lleva a otro sitio)
    DZ_AGACHA = con(Ar, raiz=raiz(-5.0, 0.0), torso=(32, 20, 0), cabeza=(-24, -20, 0), muslo_d=(-56, 0, 10), pierna_d=(80, 0, 0),
                    muslo_i=(-30, 0, -10), pierna_i=(70, 0, 0))
    DZ_SALTA = con(Ar, raiz=raiz(1.6, -0.6), torso=(-6, 20, -18), cabeza=(-6, -20, 10), muslo_d=(-30, 0, 20), pierna_d=(20, 0, 0),
                   muslo_i=(10, 0, -26), pierna_i=(30, 0, 0))
    accion("desliz", 0.75, [
        (0.00, Ar),
        (0.22, DZ_AGACHA),
        (0.36, DZ_SALTA),
        (0.56, con(DZ_AGACHA, raiz=raiz(-3.0, 0.0))),
        (0.75, Ar),
    ], golpes=(0.36,))
    # ---- 11 aturdido: se tambalea hacia atras, los brazos caidos
    AT = dict(raiz=raiz(-1.6, 2.0), torso=(-14, 0, 8), cabeza=(-20, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(26, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(20, 0, 0), brazo_d=(18, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(20, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(6, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-0.8, 0.0))),
        (0.18, AT),
        (0.60, con(AT, torso=(-8, 6, -10), cabeza=(-14, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.00, con(AT, torso=(-12, -6, 8), cabeza=(-18, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(8, 0, 0), cabeza=(8, 0, 0), raiz=raiz(-1.0, 0.6))),
    ])
    # ---- 12 morir: se lleva la mano a la marca, cae de rodillas y se derrumba de bruces (el juego lo deshace en sombra)
    CAE = dict(raiz=raiz(-9.6, 0.6), muslo_d=(-10, 0, 6), pierna_d=(102, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0),
               torso=(8, 0, 4), cabeza=(-30, 0, 8), brazo_d=(-50, 0, -20), antebrazo_d=(-70, 0, 0), mano_d=(10, 0, 0),
               brazo_i=(8, 0, -24), antebrazo_i=(-12, 0, 0))
    accion("morir", 1.8, [
        (0.00, con(BASE, torso=(-12, 0, 6), cabeza=(-24, 0, 10), brazo_d=(-40, 0, -24), antebrazo_d=(-80, 0, 0), mano_d=(10, 0, 0),
                   brazo_i=(10, 0, -30), antebrazo_i=(-10, 0, 0))),
        (0.50, CAE),
        (0.90, con(CAE, raiz=raiz(-9.8, 0.2), torso=(26, 0, 2), cabeza=(16, 0, 6), brazo_d=(-10, 0, 20), antebrazo_d=(-20, 0, 0),
                   brazo_i=(-8, 0, -20))),
        (1.30, BRUCES),
        (1.80, BRUCES),
    ])
    # ---- 13 levantarse (de rodillas) y 14 alzarse (de bruces)
    DE_PIE = con(GUARDIA, brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0), brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    MEDIO = dict(raiz=raiz(-5.0, 0.4), muslo_d=(-60, 0, 6), pierna_d=(84, 0, 0), muslo_i=(0, 0, -6), pierna_i=(70, 0, 0),
                 torso=(30, 10, 0), cabeza=(-10, 10, 0), brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0),
                 brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    RZ = dict(raiz=raiz(-9.2, 0.8), torso=(12, 0, 0), cabeza=(20, 0, 0), muslo_d=(-8, 0, 5), pierna_d=(98, 0, 0), muslo_i=(-8, 0, -5),
              pierna_i=(98, 0, 0), brazo_d=(-40, 0, -14), antebrazo_d=(-60, 0, 0), mano_d=(40, 0, 0), brazo_i=(-40, 0, 14),
              antebrazo_i=(-60, 0, 0))
    accion("levantarse", 1.3, [
        (0.00, RZ),
        (0.35, con(RZ, torso=(6, 30, 0), cabeza=(-6, 36, 0), brazo_d=(-10, 0, 10), antebrazo_d=(-40, 0, 0), brazo_i=(-10, 0, -10),
                   antebrazo_i=(-30, 0, 0))),
        (0.80, MEDIO),
        (1.30, DE_PIE),
    ])
    accion("alzarse", 1.6, [
        (0.00, BRUCES),
        (0.40, con(BRUCES, raiz=raiz(1.0, 10.0, rot=(60, 0, 0)), cabeza=(-30, 0, 0), brazo_d=(-60, 0, 20), antebrazo_d=(-40, 0, 0),
                   brazo_i=(-60, 0, -20), antebrazo_i=(-40, 0, 0))),
        (0.85, dict(raiz=raiz(-6.0, 2.0, rot=(20, 0, 0)), torso=(30, 0, 0), cabeza=(-24, 0, 0),
                    muslo_d=(-80, 0, 6), pierna_d=(100, 0, 0), muslo_i=(-40, 0, -6), pierna_i=(100, 0, 0),
                    brazo_d=(-30, 0, 10), antebrazo_d=(-30, 0, 0), mano_d=(40, 0, 0), brazo_i=(-30, 0, -10), antebrazo_i=(-30, 0, 0))),
        (1.20, con(MEDIO, raiz=raiz(-4.6, 0.0), torso=(26, 0, 0), cabeza=(-8, 0, 0))),
        (1.60, DE_PIE),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if nombre in ("mazazo", "barrido") or nombre.endswith("mazo"):
        return 0
    if nombre in ("tajo", "estocada", "escudazo", "bloqueo") or nombre.endswith("espada"):
        return 1
    if nombre in ("disparo", "flecha_negra", "punzada", "desliz") or nombre.endswith("arco"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_mazo"]]
    if nombre == "quieto":
        return [anims["porte_mazo"]]
    return []


TABLA_EXTRA = ()
