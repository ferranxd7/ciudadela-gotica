"""
Los gigantes errantes de Morvain (parte 7w; respuesta 132 del plan: "gigantes errantes"): los ultimos de los que levantaron el
reino, que andan sin descanso por sus caminos de siempre (tools/morvain/Rutas.java: tres vueltas largas, una por gigante). El
juego los dibuja a 1,8 (unos ocho bloques de alto: el modelo esta hecho al doble de lo normal, para que la textura tenga el
detalle de los bloques).

Encorvados, enormes de hombros y de manos, los brazos hasta las rodillas, la cabeza pequena metida entre los hombros, la panza,
el taparrabos y los pies descalzos. Tres, cada uno con lo suyo:

  0  Ogmund, el de la Campana (las Llanuras): el campanero. Calvo con su corona de pelo cano, la barba larga trenzada con
     cuerda, el manto de arpillera sobre los hombros y la cuerda cruzada al pecho. Lleva la campana de su aldea (que ya no
     existe) atada al cabo de una viga: con ella pega. Dobla al andar.
  1  Ulfgar, el de las Costillas (la Costa): el ultimo de los de la costa. La melena negra y larga con algas, la barba con
     conchas, el omoplato de ballena por hombrera y el collar de dientes; la piel gris azulada, con lapas que lucen un poco.
     Pega con una costilla de los suyos ("estos huesos eran mis hermanos").
  2  Haldor, el de la Columna (la Capital): el cantero que levanto las murallas. El pelo corto y la cinta de cuero, la barba
     cuadrada, el mandil de cantero y los goznes de la puerta de la ciudad por brazales; el polvo de piedra en la piel, que se le
     va volviendo piedra. Pega con una columna de marmol con su capitel.

Sus golpes: el mazazo (lo alza a dos manos y lo estrella delante: la onda; se queda clavado un momento), el barrido (de su
derecha a su izquierda, bajo), el pisoton (alza la rodilla y pisa: la onda alrededor del pie), la patada (al que se le mete
entre los pies), la roca (se agacha, arranca una roca del suelo con la izquierda y se la tira a quien esta lejos), el rugido
(al empezar y a media vida), aturdido (cae sobre una rodilla: su cabeza queda a tiro) y morir (de rodillas, habla y cae de
bruces).

Las armas largas estan "tumbadas" (una pieza girada 90 grados: las cajas van a lo largo de su Y), para que su zona de la
textura no se coma media hoja.
"""
import math

import numpy as np

from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, madera, metal, mezcla, oscuro, sombra, tela)

NOMBRE = "gigante"
VARIANTES = ("campana", "costilla", "columna")
#: lo que se dibuja en el juego: 1,8 (el modelo, al doble de lo normal: unos ocho bloques)
ESCALA = 1.8
CENTRO_Y = 4.2
#: para las hojas de revision (generar.py): mas lejos que a los de tamano de persona
ZOOM_VARIANTES = 25
ZOOM_TIRA = 21
LADO_TIRA = 420

DE_OGMUND = ["manto", "capucha", "corona_pelo", "barba_o", "trenza", "cuerda_f", "cuerda_t", "viga", "campana", "badajo"]
DE_ULFGAR = ["melena", "grenas", "barba_u", "collar_u", "omoplato", "costilla_1", "costilla_2", "costilla_3", "costilla_4"]
DE_HALDOR = ["pelo_h", "cinta", "barba_h", "mandil", "mandil_b", "gozne_d", "gozne_i", "columna"]
#: la roca que arranca del suelo: solo se ve mientras la lleva (lo decide el modelo del juego)
ROCA = ["roca_mano"]
OCULTAS = {0: DE_ULFGAR + DE_HALDOR + ROCA, 1: DE_OGMUND + DE_HALDOR + ROCA, 2: DE_OGMUND + DE_ULFGAR + ROCA}

#: el puno: el centro de lo que agarra, en el sistema de la mano (y hacia abajo)
AGARRE = 3.5


# ---------------------------------------------------------------- el modelo

def rig():
    r = Rig(NOMBRE, 256)
    humanoide(r, torso=(24, 30, 14), cabeza=(13, 13, 13), brazo=(10, 36, 10), pierna=(12, 28, 12), hombro=-1.0, cuello=1.0)
    # la cabeza adelantada, metida entre los hombros
    r.por_nombre["cabeza"].pivot = (0.0, -30.0, -3.0)
    # ---- lo de todos: el cogote de toro, la panza, el cinto, el taparrabos, los pies y los punos
    r.pieza("trapecio", "torso", (0, 0, 0), boxes=[Box(-11, -35, -3, 22, 7, 11, "trapecio")])
    r.pieza("panza", "torso", (0, 0, 0), boxes=[Box(-10, -16, -9.4, 20, 14, 2.8, "panza")])
    r.pieza("cinto", "torso", (0, 0, 0), boxes=[Box(-12.4, -4.2, -9.8, 24.8, 5, 17.4, "cinto", grow=g(0.2))])
    r.pieza("faldon_f", "cadera", (0, 0.4, -9.6), rot=(-3, 0, 0), boxes=[Box(-8, 0, -1.2, 16, 17, 1.2, "faldon")])
    r.pieza("faldon_t", "cadera", (0, 0.4, 7.6), rot=(4, 0, 0), boxes=[Box(-10, 0, 0, 20, 16, 1.2, "faldon_t")])
    for n in ("d", "i"):
        r.pieza(f"pie_{n}", f"pierna_{n}", (0, 0, 0), boxes=[
            Box(-6.6, 9, -11, 13.2, 5, 17, "pie", uv="pie"),
            Box(-6.2, 11, -13.6, 12.4, 3, 2.6, "dedos", uv="dedos"),
        ])
        r.pieza(f"puno_{n}", f"mano_{n}", (0, 0, 0), boxes=[Box(-6, -1, -6, 12, 9, 12, "puno", uv="puno")])
    # ---- 0, Ogmund: la corona de pelo cano, la barba trenzada, el manto de arpillera con la capucha caida, la cuerda cruzada
    r.pieza("corona_pelo", "cabeza", (0, 0, 0), boxes=[Box(-6.5, -9.5, -6.5, 13, 6, 13, "corona_pelo", grow=g(0.45))])
    r.pieza("barba_o", "cabeza", (0, -4.6, -6.9), rot=(-6, 0, 0), boxes=[Box(-5.5, 0, -1.6, 11, 12, 1.8, "barba_o")])
    r.pieza("trenza", "barba_o", (0, 11.6, -0.6), rot=(-4, 0, 0), boxes=[Box(-1.6, 0, -1.0, 3.2, 10, 2.0, "trenza")])
    r.pieza("manto", "torso", (0, 0, 0), boxes=[Box(-13.2, -36, -8.2, 26.4, 10, 17.4, "manto", grow=g(0.3))])
    r.pieza("capucha", "manto", (0, -36.4, 6.6), rot=(16, 0, 0), boxes=[Box(-6.5, -4.0, -1.0, 13, 5, 6, "capucha")])
    r.pieza("cuerda_f", "torso", (0, -15, -9.6), rot=(0, 0, 38), boxes=[Box(-1.0, -19, -0.8, 2, 38, 1.2, "cuerda")])
    r.pieza("cuerda_t", "torso", (0, -15, 7.4), rot=(0, 0, 38), boxes=[Box(-1.0, -19, -0.4, 2, 38, 1.2, "cuerda", uv="cuerda")])
    #     la viga (tumbada: a lo largo de su -Y va hacia delante; su +Z es "arriba" de la mano) y la campana atada al cabo
    r.pieza("viga", "mano_d", (0, 0, 0), rot=(90, 0, 0), boxes=[
        Box(-3, -44, -AGARRE - 3, 6, 52, 6, "viga"),
        Box(-3.7, -42.5, -AGARRE - 3.7, 7.4, 1.6, 7.4, "atadura"),
        Box(-3.7, -37.5, -AGARRE - 3.7, 7.4, 1.6, 7.4, "atadura", uv="atadura"),
        Box(-3.7, 2.5, -AGARRE - 3.7, 7.4, 1.6, 7.4, "atadura", uv="atadura"),
    ])
    r.pieza("campana", "viga", (0, -40, -AGARRE - 3), rot=(-90, 0, 0), boxes=[
        Box(-2.5, -1, -2.5, 5, 3, 5, "asa"),
        Box(-6.5, 2, -6.5, 13, 5, 13, "hombro_c"),
        Box(-8.5, 7, -8.5, 17, 9, 17, "cuerpo_c"),
        Box(-10.5, 16, -10.5, 21, 5, 21, "labio_c"),
    ])
    r.pieza("badajo", "campana", (0, 7, 0), boxes=[
        Box(-1.5, 0, -1.5, 3, 12, 3, "badajo"),
        Box(-2.5, 11, -2.5, 5, 4.5, 5, "bola_badajo"),
    ])
    # ---- 1, Ulfgar: la melena con algas (y las grenas por la espalda), la barba con conchas, el collar de dientes, el
    # omoplato de ballena por hombrera y la costilla (cuatro tramos, curvada)
    r.pieza("melena", "cabeza", (0, 0, 0), boxes=[Box(-6.5, -13, -6.5, 13, 13, 13, "melena", grow=g(0.6))])
    r.pieza("grenas", "cabeza", (0, -7.0, 6.6), rot=(12, 0, 0), boxes=[Box(-7.4, 0, 0, 14.8, 17, 2.0, "grenas")])
    r.pieza("barba_u", "cabeza", (0, -4.4, -6.9), rot=(-4, 0, 0), boxes=[Box(-5.5, 0, -1.4, 11, 8, 1.6, "barba_u")])
    r.pieza("collar_u", "torso", (0, 0, 0), boxes=[
        Box(-9.5, -31, -9.4, 19, 3, 2.2, "collar_u"),
        Box(-7.6, -28.4, -9.2, 2, 4.2, 1.4, "colmillo", uv="colmillo"),
        Box(-3.8, -28.0, -9.6, 2, 4.2, 1.4, "colmillo", uv="colmillo"),
        Box(-1.0, -28.6, -9.8, 2, 5.4, 1.6, "colmillo_g"),
        Box(1.8, -28.0, -9.6, 2, 4.2, 1.4, "colmillo", uv="colmillo"),
        Box(5.6, -28.4, -9.2, 2, 4.2, 1.4, "colmillo", uv="colmillo"),
    ])
    r.pieza("omoplato", "brazo_i", (0, 0, 0), rot=(0, 0, -8), boxes=[Box(-4.2, -5.6, -7.2, 11.4, 6, 14.4, "omoplato")])
    r.pieza("costilla_1", "mano_d", (0, 0, 0), rot=(90, 0, 0), boxes=[Box(-2.5, -15, -AGARRE - 3, 5, 21, 6, "costilla_1")])
    r.pieza("costilla_2", "costilla_1", (0, -15, -AGARRE), rot=(8, 0, 0), boxes=[Box(-3, -14.4, -3.8, 6, 14.8, 7.6, "costilla_2")])
    r.pieza("costilla_3", "costilla_2", (0, -14, 0), rot=(9, 0, 0), boxes=[Box(-3.2, -13.4, -4.3, 6.4, 13.8, 8.6, "costilla_3")])
    r.pieza("costilla_4", "costilla_3", (0, -13, 0), rot=(11, 0, 0), boxes=[Box(-2.6, -10, -3.6, 5.2, 10.4, 7.2, "costilla_4")])
    # ---- 2, Haldor: el pelo corto, la cinta, la barba cuadrada, el mandil de cantero, los goznes y la columna con su capitel
    r.pieza("pelo_h", "cabeza", (0, 0, 0), boxes=[Box(-6.5, -13, -6.5, 13, 4, 13, "pelo_h", grow=g(0.45))])
    r.pieza("cinta", "cabeza", (0, 0, 0), boxes=[Box(-6.5, -9.8, -6.5, 13, 1.6, 13, "cinta", grow=g(0.75))])
    r.pieza("barba_h", "cabeza", (0, -4.4, -6.9), boxes=[Box(-5.6, 0, -1.4, 11.2, 7, 1.6, "barba_h")])
    r.pieza("mandil", "torso", (0, 0, 0), boxes=[Box(-9.6, -24, -10.2, 19.2, 20, 0.8, "mandil")])
    r.pieza("mandil_b", "cadera", (0, 0.6, -10.4), rot=(-3, 0, 0), boxes=[Box(-9.6, 0, -0.9, 19.2, 18, 0.9, "mandil_b")])
    for n in ("d", "i"):
        r.pieza(f"gozne_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-5, 5, -5, 10, 9, 10, f"gozne", grow=g(0.5), uv="gozne")])
    r.pieza("columna", "mano_d", (0, 0, 0), rot=(90, 0, 0), boxes=[
        Box(-5, -46, -AGARRE - 5, 10, 52, 10, "fuste"),
        Box(-7, -50, -AGARRE - 7, 14, 4, 14, "capitel"),
        Box(-8.5, -53, -AGARRE - 8.5, 17, 3, 17, "abaco"),
        Box(-6, -6.5, -AGARRE - 6, 12, 2, 12, "anillo"),
    ])
    # ---- la roca que arranca del suelo (en la izquierda)
    r.pieza("roca_mano", "mano_i", (0, 0, 0), boxes=[
        Box(-8, 1, -8, 16, 14, 16, "roca"),
        Box(-6, 13, -5, 11, 4, 10, "roca_b"),
    ])
    r.empaquetar()
    r.posproceso = _plomada(r)
    r.posproceso_clave = "_campana"
    r.posproceso_piezas = ("campana",)
    r.posproceso_mezcla = lambda a, b, d: bool(a or b)
    return r


def _plomada(r):
    """`_campana` en una pose: la campana cuelga a plomo de la viga (su +Y, hacia abajo), sea cual sea la pose del brazo.
    Las piezas giran Z-Y-X y lo que pone la animacion se SUMA a los angulos de reposo de la pieza."""
    import vista
    reposo = r.por_nombre["campana"].rot
    D = math.pi / 180

    def post(q, spec, prev):
        if not spec.get("_campana"):
            return q
        q = dict(q)
        q["campana"] = (0.0, 0.0, 0.0)
        Rp = vista.matrices(r, q)["viga"][:3, :3]
        d = Rp.T @ np.array([0.0, 1.0, 0.0])                    # abajo, visto desde la viga
        R0 = (vista.rz(reposo[2] * D) @ vista.ry(reposo[1] * D) @ vista.rx(reposo[0] * D))[:3, :3]
        y0 = R0 @ np.array([0.0, 1.0, 0.0])
        eje = np.cross(y0, d)
        sn, cs = float(np.linalg.norm(eje)), float(np.dot(y0, d))
        if sn < 1e-6:
            Q = np.eye(3)
        else:
            k = eje / sn
            K = np.array([[0, -k[2], k[1]], [k[2], 0, -k[0]], [-k[1], k[0], 0]])
            a = math.atan2(sn, cs)
            Q = np.eye(3) + math.sin(a) * K + (1 - math.cos(a)) * (K @ K)
        R = Q @ R0
        b = math.asin(max(-1.0, min(1.0, -float(R[2, 0]))))
        a = math.atan2(float(R[2, 1]), float(R[2, 2]))
        c = math.atan2(float(R[1, 0]), float(R[0, 0]))
        q["campana"] = (math.degrees(a) - reposo[0], math.degrees(b) - reposo[1], math.degrees(c) - reposo[2])
        return q

    return post


# ---------------------------------------------------------------- los colores

PIEL = (
    ("#33291f", "#4a3c30", "#615042", "#776453", "#8c7863", "#a08b74"),     # Ogmund: curtida, del sol de las llanuras
    ("#34403f", "#475553", "#5a6967", "#6e7d7a", "#83918d", "#98a5a0"),     # Ulfgar: gris azulada, del mar
    ("#3b352d", "#544c40", "#6c6253", "#837867", "#998e7b", "#aea591"),     # Haldor: con polvo de piedra
)
CANA = ("#5c5850", "#78736a", "#948e84", "#aba59a", "#c2bdb2", "#d6d2c8")
NEGRO_PELO = ("#141414", "#1d1c1b", "#262523", "#302e2b", "#3b3834", "#47433e")
CASTANO = ("#2a1d14", "#3a291c", "#4a3524", "#5a422d", "#6b5038", "#7c5f45")
ARPILLERA = ("#3a2f20", "#4d3f2b", "#615038", "#746045", "#887253", "#9b8462")
CUERDA = ("#4a3a24", "#5f4b30", "#76603e", "#8c744d", "#a1885d")
CUERO = ("#2a1a10", "#3a2516", "#4b311e", "#5c3d26", "#6e4a2f", "#80573a")
TAPARRABOS = (
    ("#2e2a22", "#3e382d", "#4f4739", "#605645", "#706553"),            # arpillera vieja
    ("#24302c", "#2f3e39", "#3b4c46", "#485b54", "#556962"),            # lona de vela podrida, verdosa
    ("#3a2c22", "#4c3a2d", "#5e4838", "#705644", "#826550"),            # cuero de cantero
)
MADERA = ("#2b1d12", "#3b2918", "#4c3520", "#5d4229", "#6f5033", "#815e3d")
BRONCE = ("#3a2a14", "#5a401c", "#7a5826", "#956d33", "#ad8443", "#c49a58")
VERDIN = ("#2f5a4a", "#3f7560", "#529078", "#68a88f")
HUESO = ("#6e6552", "#857b66", "#9c927b", "#b2a990", "#c7bfa6", "#dad3bc")
MARMOL = ("#8a8780", "#a19e96", "#b6b3ab", "#c9c6be", "#dad8d1", "#e9e7e1")
VETA = "#7c776c"
HIERRO = ("#1c1c1e", "#2b2b2e", "#3b3a3e", "#4c4b4f", "#5e5c60", "#716f72")
OXIDO = ("#4a2a16", "#653a1d", "#7e4a26")
PIEDRA = ("#3e3c38", "#504d48", "#625f58", "#76726a", "#89857c", "#9c978d")
ALGA = ("#1d3324", "#28442f", "#33553a", "#3f6746")
OJO = ("#e6b45a", "#8ae0d0", "#e8e2cc")
OJO_C = ("#fff0c0", "#d8fff6", "#ffffff")


def _r(sem):
    return np.random.default_rng(sem).random((96, 96))


def _suave(sem, paso=6):
    """Ruido de manchas grandes (una rejilla gruesa interpolada): la piel no es grava."""
    n = 96 // paso + 2
    g0 = np.random.default_rng(sem).random((n, n))
    out = np.zeros((96, 96))
    for y in range(96):
        for x in range(96):
            fy, fx = y / paso, x / paso
            iy, ix = int(fy), int(fx)
            dy, dx = fy - iy, fx - ix
            dy, dx = dy * dy * (3 - 2 * dy), dx * dx * (3 - 2 * dx)
            a = g0[iy, ix] * (1 - dx) + g0[iy, ix + 1] * dx
            b = g0[iy + 1, ix] * (1 - dx) + g0[iy + 1, ix + 1] * dx
            out[y, x] = a * (1 - dy) + b * dy
    return out


def materiales(v):
    P = PIEL[v]
    rr = _r(7100 + v)
    rs = _suave(7150 + v)

    def piel(cara, x, y, w, h, sem=0):
        k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}[cara]
        n = rr[(y + 11 * k + sem * 7) % 96, (x + 17 * k + sem * 13) % 96]
        sv = rs[(y + 13 * k + sem * 5) % 96, (x + 19 * k + sem * 11) % 96]
        t = 0.52 + (sv - 0.5) * 0.55 + (n - 0.5) * 0.16
        c = P[int(np.clip(round(t * 5), 0, 5))]
        # las manchas de la edad, las cicatrices y (Haldor) la piel que se le vuelve piedra
        m = rr[(y // 2 + 31 * k + sem) % 96, (x // 2 + 5 * k + sem * 3) % 96]
        if m > 0.93:
            c = oscuro(c, 0.18)
        if v == 2 and m < 0.10:
            c = mezcla(c, PIEDRA[3 + (x + y) % 2], 0.65)
        if v == 1 and m < 0.05:
            c = mezcla(c, "#c8c2b0", 0.6)                       # una lapa
        return c

    def pielg(cara, x, y, w, h, sem=0):
        return piel(cara, x, y, w, h, sem)

    def cabeza(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 1)
        if cara == "abajo":
            return oscuro(c, 0.25)
        if cara != "frente":
            if cara in ("der", "izq") and y in (6, 7) and delante(cara, x, w) in (4, 5):
                return oscuro(c, 0.35)                           # la oreja
            return c
        # la cara (13 x 13): x = 0 es su lado derecho
        cejas = {4}
        if y in cejas and 1 <= x <= 11 and x != 6:
            return (CANA if v == 0 else NEGRO_PELO if v == 1 else CASTANO)[1 + (x % 2)]
        if y == 5 and (1 <= x <= 4 or 8 <= x <= 11):
            return oscuro(c, 0.55)                               # las cuencas
        if y == 6 and (1 <= x <= 4 or 8 <= x <= 11):
            if x in (3, 9):
                return "#141010"
            if x in (2, 10):
                return OJO[v]
            return oscuro(c, 0.5)
        if y == 7 and (2 <= x <= 4 or 8 <= x <= 10):
            return oscuro(c, 0.32)                               # las ojeras
        if x == 6 and 5 <= y <= 9:
            return claro(c, 0.12) if y < 9 else oscuro(c, 0.2)  # la nariz
        if y == 9 and x in (5, 7):
            return oscuro(c, 0.4)
        if y in (8, 9) and x in (4, 8):
            return oscuro(c, 0.12)
        if y == 11 and 3 <= x <= 9:
            return "#1a0f0c" if 4 <= x <= 8 else oscuro(c, 0.4)  # la boca
        if y == 12 and 4 <= x <= 8:
            return oscuro(c, 0.15)
        if y in (1, 2) and x in (2, 3, 9, 10) and v != 1:
            return oscuro(c, 0.14)                               # las arrugas de la frente
        return c

    def torso(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 2)
        if cara == "frente":
            # los pectorales: dos bultos, mas claros arriba y con su sombra debajo; el esternon y la clavicula
            for cx in (w * 0.27, w * 0.73):
                dx, dy = (x + 0.5 - cx) / 6.0, (y + 0.5 - 7.5) / 4.8
                if dx * dx + dy * dy < 1.0:
                    if dy > 0.45:
                        c = oscuro(c, 0.3 * (dy - 0.25))
                    elif dy < -0.25:
                        c = claro(c, 0.08)
            if x in (w // 2 - 1, w // 2) and 3 <= y <= 13:
                c = oscuro(c, 0.14)
            if y == 2 and 3 <= x <= w - 4 and x not in (w // 2 - 1, w // 2):
                c = oscuro(c, 0.1)
            # los costados (las costillas bajo la piel)
            if y in (15, 18, 21) and (x <= 3 or x >= w - 4):
                c = oscuro(c, 0.14)
        if cara == "espalda":
            if x in (w // 2 - 1, w // 2):
                return oscuro(c, 0.16)                           # el espinazo
            if 4 <= y <= 12 and x in (4, w - 5):
                return oscuro(c, 0.1)
        return c

    def trapecio(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 3)
        if cara == "arriba":
            return claro(c, 0.04)
        return c

    def panza(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 4)
        if cara == "frente":
            if x in (w // 2 - 1, w // 2) and y == 8:
                return oscuro(c, 0.45)                           # el ombligo
            if y >= h - 2:
                return oscuro(c, 0.14)
        return c

    def cinto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if 1 < x < w - 2 and 1 < y < h - 2 else CUERO[1]
        if v == 0:
            # una soga gorda dada varias vueltas
            k = (x + y * 2) % 4
            return (CUERDA[3], CUERDA[2], CUERDA[1], CUERDA[2])[k]
        if v == 1:
            # una red de pesca retorcida, con algas
            if (x * 7 + y * 3) % 11 == 0:
                return ALGA[2]
            return (TAPARRABOS[1][1], TAPARRABOS[1][2], TAPARRABOS[1][0])[(x + y) % 3]
        c = CUERO[2 + (y == 0) - (y == h - 1)]
        if cara == "frente" and w // 2 - 2 <= x <= w // 2 + 1:
            return HIERRO[4] if y in (0, h - 1) or x in (w // 2 - 2, w // 2 + 1) else HIERRO[1]   # la hebilla
        return c

    tap = TAPARRABOS[v]
    tapr = _r(7300 + v)

    def faldon(cara, x, y, w, h, sem=0):
        if cara in ("arriba", "abajo"):
            return tap[1]
        n = tapr[(y + sem) % 96, (x * 3 + sem) % 96]
        c = tap[int(np.clip(round((0.55 - 0.3 * y / max(1, h - 1) + (n - 0.5) * 0.3) * 4), 0, 4))]
        if x % 4 == 0:
            c = oscuro(c, 0.12)                                  # los pliegues
        # roto por abajo, a jirones
        k = (x * 7 + 3 + sem) % 5
        if y >= h - 1 - k * 0.6 - (1 if x in (0, w - 1) else 0):
            return None
        if v == 2 and y == 1:
            return CUERO[0]
        return c

    def faldon_t(cara, x, y, w, h):
        return faldon(cara, x, y, w, h, 5)

    def muslo(cara, x, y, w, h):
        return piel(cara, x, y, w, h, 6)

    def pierna(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 7)
        if cara == "frente" and x in (w // 2 - 1, w // 2) and y <= 2:
            return claro(c, 0.08)                                # la rodilla
        if y >= h - 3:
            return oscuro(c, 0.12 * (y - h + 4))                 # el barro de los tobillos
        return c

    def pie(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 8)
        if cara == "abajo":
            return "#1e1813"
        return oscuro(mezcla(c, "#3b2e22", 0.35), 0.1 if y >= h - 2 else 0.0)

    def dedos(cara, x, y, w, h):
        c = mezcla(piel(cara, x, y, w, h, 9), "#3b2e22", 0.3)
        if cara == "frente" and x % 3 == 2:
            return oscuro(c, 0.45)                               # entre los dedos
        if cara == "arriba" and x % 3 != 2:
            return claro(c, 0.06) if y == h - 1 else c
        return c

    def brazo(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 10)
        if cara in ("der", "izq", "frente") and y in (h - 4, h - 3) and (x + y) % 3 == 0:
            return oscuro(c, 0.2)                                # las venas
        return c

    def antebrazo(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 11)
        if cara == "frente" and (x * 3 + y) % 7 == 0:
            return oscuro(c, 0.16)
        return c

    def puno(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 12)
        if cara == "frente":
            if y in (1, 2):
                return oscuro(c, 0.1) if x % 3 == 2 else claro(c, 0.06)   # los nudillos
            if y >= 3 and x % 3 == 2:
                return oscuro(c, 0.38)                           # entre los dedos
        if cara == "abajo":
            return oscuro(c, 0.2)
        if cara in ("der", "izq") and y >= 3 and x % 3 == 0:
            return oscuro(c, 0.2)
        return c

    # ---- Ogmund
    def corona_pelo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None                                          # la calva
        if cara == "frente":
            return CANA[2 + x % 2] if (x <= 1 or x >= w - 2) and y >= 2 else None   # las patillas
        if y < 2 and (x * 5 + y) % 3:
            return None                                          # el borde ralo
        return CANA[1 + (x * 3 + y) % 4]

    def barba_o(cara, x, y, w, h):
        if cara == "arriba":
            return None
        c = CANA[2 + (x * 3 + y // 2) % 3]
        if cara == "frente":
            if y <= 1 and 3 <= x <= 7:
                return None if y == 1 else CANA[1]              # la boca y el bigote
            largo = (h - 4, h - 2, h - 1, h, h, h, h, h, h - 1, h - 2, h - 4)[x % 11]
            if y >= largo:
                return None
            if (x + y) % 5 == 0:
                return CANA[1]
        return c

    def trenza(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        if y in (3, 7):
            return CUERDA[2]                                     # la cuerda que la ata
        return CANA[(1, 3, 4, 2)[(y + x) % 4]]

    arp = tela(ARPILLERA, 7401, pliegue=4, grano=0.2)

    def manto(cara, x, y, w, h):
        if cara == "abajo":
            return None if 2 < x < w - 3 and 2 < y < h - 3 else ARPILLERA[1]
        c = arp(cara, x, y, w, h)
        if (x + y) % 2 == 0:
            c = oscuro(c, 0.06)                                  # la trama
        if cara != "arriba":
            k = (x * 5 + 1) % 4
            if y >= h - 1 - k * 0.5:
                return None                                      # el borde deshilachado
            if (x * 13 + y * 7) % 53 == 0:
                return None                                      # algun agujero
        return c

    def capucha(cara, x, y, w, h):
        c = arp(cara, x, y, w, h)
        return oscuro(c, 0.15) if cara in ("abajo", "frente") else c

    def cuerda(cara, x, y, w, h):
        return CUERDA[(3, 2, 1, 2)[(y + x) % 4]]

    def viga(cara, x, y, w, h):
        # tumbada: las caras largas son frente / espalda / der / izq (y a lo largo); las tapas, los cabos
        if cara in ("arriba", "abajo"):
            c = MADERA[2]
            return MADERA[1] if (x in (1, 4) or y in (1, 4)) else c   # los anillos
        c = madera(MADERA, 7411, alo_largo="y")(cara, x, y, w, h)
        if (y * 7 + x * 3) % 37 == 0:
            return oscuro(c, 0.4)                                # un nudo
        if y > h - 9 and cara == "espalda":
            return oscuro(c, 0.2)                                # gastada donde la agarra
        return c

    def atadura(cara, x, y, w, h):
        return CUERDA[(2, 3, 1)[(x + y) % 3]]

    def bronce(cara, x, y, w, h, sem=0):
        n = _r(7420 + sem)[y % 96, x % 96]
        c = BRONCE[int(np.clip(round((0.55 + (n - 0.5) * 0.4) * 5), 0, 5))]
        m = _r(7430 + sem)[(y // 2) % 96, (x // 2) % 96]
        if m > 0.72:
            c = mezcla(c, VERDIN[int(m * 10) % 4], 0.55 + 0.4 * (m - 0.72))   # el verdin
        return c

    def asa(cara, x, y, w, h):
        return bronce(cara, x, y, w, h, 1)

    def hombro_c(cara, x, y, w, h):
        c = bronce(cara, x, y, w, h, 2)
        if cara == "arriba":
            return claro(c, 0.1)
        return c

    def cuerpo_c(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return bronce(cara, x, y, w, h, 3)
        c = bronce(cara, x, y, w, h, 3)
        # la franja de la inscripcion (letras gastadas) y la grieta de un lado
        if y in (2, 3):
            return BRONCE[1] if (x * 3 + y) % 4 == 0 else claro(c, 0.08)
        if y == 1 or y == 4:
            return oscuro(c, 0.25)
        if cara == "frente" and x in (11, 12) and y >= 4 and (x - 11) == (y % 2):
            return "#1a120a"
        return c

    def labio_c(cara, x, y, w, h):
        if cara == "abajo":
            # la boca de la campana: el borde de bronce y lo oscuro de dentro
            if x < 2 or y < 2 or x >= w - 2 or y >= h - 2:
                return BRONCE[3]
            return "#100c08" if (x + y) % 7 else "#1a140c"
        c = bronce(cara, x, y, w, h, 4)
        if y == h - 1:
            return claro(c, 0.18)                                # el canto, donde da
        if y == 0:
            return oscuro(c, 0.2)
        return c

    def badajo(cara, x, y, w, h):
        return HIERRO[2 + (y % 2)]

    def bola_badajo(cara, x, y, w, h):
        return HIERRO[3] if cara != "abajo" else HIERRO[5]

    # ---- Ulfgar
    def melena(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = NEGRO_PELO[1 + (x * 3 + y // 3) % 4]
        if (x * 7 + y * 3) % 13 == 0:
            c = ALGA[1 + (x % 3)]                                # algas enredadas
        if cara == "frente":
            # la cara al aire: solo el pelo de los lados y un flequillo a mechones
            if x <= 1 or x >= w - 2:
                return c
            if y <= 1 + (x * 5) % 3:
                return c
            return None
        return c

    def grenas(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        c = NEGRO_PELO[1 + (x * 5) % 4]
        if (x * 3 + y) % 9 == 0:
            c = ALGA[2]
        k = (x * 7 + 2) % 6
        if y >= h - 1 - k:
            return None                                          # las puntas
        return c

    def barba_u(cara, x, y, w, h):
        if cara == "arriba":
            return None
        c = CANA[(0, 1, 0, 2, 1)[(x + y // 2) % 5]]
        if cara == "frente":
            if y == 0 and 3 <= x <= 7:
                return None
            largo = (h - 3, h - 1, h, h, h, h - 1, h, h, h, h - 1, h - 3)[x % 11]
            if y >= largo:
                return None
            if (x, y) in ((3, 5), (7, 6), (5, 3)):
                return "#d8d0bc"                                 # las conchas
        return c

    def collar_u(cara, x, y, w, h):
        return CUERDA[1 + (x % 2)] if cara != "frente" else (HUESO[3] if x % 3 else CUERDA[1])

    def colmillo(cara, x, y, w, h):
        return HUESO[4] if y < h - 1 else HUESO[2]

    def colmillo_g(cara, x, y, w, h):
        return HUESO[5] if y < h - 2 else HUESO[3]

    hueso_r = _r(7450)

    def hueso(cara, x, y, w, h, sem=0):
        n = hueso_r[(y + sem * 5) % 96, (x + sem * 11) % 96]
        c = HUESO[int(np.clip(round((0.6 + (n - 0.5) * 0.45) * 5), 0, 5))]
        if n > 0.94:
            c = oscuro(c, 0.35)                                  # los poros
        m = hueso_r[(y // 3 + 40) % 96, (x // 2 + sem) % 96]
        if m > 0.86:
            c = mezcla(c, "#5d6a5c", 0.5)                        # lo verde del mar
        return c

    def omoplato(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h, 3)
        if cara == "arriba" and (x + y * 2) % 11 == 0:
            return oscuro(c, 0.3)
        return c

    def costilla(sem):
        def f(cara, x, y, w, h):
            if cara in ("arriba", "abajo"):
                return HUESO[2] if (x + y) % 3 else HUESO[1]
            c = hueso(cara, x, y, w, h, sem)
            if sem == 1 and y >= h - 12:
                return CUERDA[(2, 3, 1)[(y + x) % 3]] if y % 3 else CUERDA[0]   # el agarre, con cuerda
            if cara in ("der", "izq") and x in (0, w - 1):
                return oscuro(c, 0.12)
            if (x * 5 + y * 3) % 29 == 0:
                return mezcla(c, "#d9d4c4", 0.6)                 # una lapa
            return c
        return f

    # ---- Haldor
    def pelo_h(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = CASTANO[(2, 3, 1, 4, 2, 3, 1)[(x * 3 + (y // 4)) % 7]]
        if (x * 5 + y * 3) % 13 == 0:
            c = mezcla(c, "#b8b1a2", 0.45)                       # el polvo
        if cara == "frente":
            return c if y <= 1 or x <= 0 or x >= w - 1 else None
        return c

    def cinta(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return CUERO[2 + (x % 2)] if y else CUERO[4]

    def barba_h(cara, x, y, w, h):
        if cara == "arriba":
            return None
        c = CASTANO[(2, 3, 1, 3, 2, 4, 1)[(x * 2 + y // 3) % 7]]
        if cara == "frente":
            if y == 0 and 3 <= x <= 7:
                return None
            if y == h - 1 and x % 2:
                return None                                      # el borde, a mechones
            if (x * 5 + y * 2) % 11 == 0:
                return mezcla(c, "#b8b1a2", 0.35)
        return c

    mand = tela(CUERO, 7461, pliegue=5, grano=0.18, luz=0.15)

    def mandil(cara, x, y, w, h):
        c = mand(cara, x, y, w, h)
        if (x * 3 + y * 7) % 19 == 0:
            return mezcla(c, "#b8b1a2", 0.45)                    # el polvo de piedra
        if cara == "frente" and y == 2 and 3 <= x <= w - 4:
            return CUERO[0]                                      # la costura del bolsillo
        return c

    def mandil_b(cara, x, y, w, h):
        c = mand(cara, x, y, w, h)
        k = (x * 5 + 2) % 4
        if y >= h - 1 - k * 0.5:
            return None
        if (x * 3 + y * 5) % 17 == 0:
            return mezcla(c, "#b8b1a2", 0.45)
        return c

    def gozne(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if 1 < x < w - 2 and 1 < y < h - 2 else HIERRO[2]
        c = metal(HIERRO, 7471)(cara, x, y, w, h)
        if y in (1, h - 2) and x % 4 == 1:
            return OXIDO[2]                                      # los clavos
        if (x * 5 + y * 3) % 11 == 0:
            return OXIDO[1]
        return c

    mr = _r(7480)

    def marmol(cara, x, y, w, h, sem=0):
        n = mr[(y + sem * 3) % 96, (x + sem * 7) % 96]
        c = MARMOL[int(np.clip(round((0.62 + (n - 0.5) * 0.3) * 5), 0, 5))]
        # las vetas: unas lineas que bajan torcidas
        if (x * 2 + int(3 * math.sin(y * 0.35 + sem)) + sem * 5) % 17 == 0:
            c = mezcla(c, VETA, 0.5)
        return c

    def fuste(cara, x, y, w, h):
        if cara == "abajo":
            # el cabo roto (donde la agarra): la piedra partida
            n = mr[(y * 3) % 96, (x * 5) % 96]
            return mezcla(MARMOL[2], PIEDRA[2], 0.3 + 0.4 * n)
        if cara == "arriba":
            return marmol(cara, x, y, w, h, 2)
        c = marmol(cara, x, y, w, h, 1)
        if x % 3 == 2:
            c = oscuro(c, 0.16)                                  # las estrias
        if y > h - 8 and (x * 7 + y) % 5 == 0:
            return mezcla(c, PIEDRA[2], 0.6)                     # la rotura
        if y > h - 3:
            return oscuro(c, 0.1 * (y - h + 4))
        return c

    def capitel(cara, x, y, w, h):
        c = marmol(cara, x, y, w, h, 3)
        if cara not in ("arriba", "abajo") and y == h - 1:
            return oscuro(c, 0.2)
        if cara not in ("arriba", "abajo") and x in (1, w - 2) and y in (1, 2):
            return oscuro(c, 0.25)                               # las volutas
        return c

    def abaco(cara, x, y, w, h):
        c = marmol(cara, x, y, w, h, 4)
        if cara not in ("arriba", "abajo") and (x * 3) % 13 == 0:
            return oscuro(c, 0.3)                                # desconchado
        return c

    def anillo(cara, x, y, w, h):
        return claro(marmol(cara, x, y, w, h, 5), 0.06)

    pr = _r(7490)

    def roca(cara, x, y, w, h, sem=0):
        n = pr[(y + sem) % 96, (x + sem * 3) % 96]
        c = PIEDRA[int(np.clip(round((0.5 + (n - 0.5) * 0.6) * 5), 0, 5))]
        if cara == "arriba" and n > 0.7:
            return mezcla(c, "#3f5a2e", 0.6)                     # la hierba que se trae
        if n < 0.1:
            return oscuro(c, 0.35)
        if cara == "abajo":
            return mezcla(c, "#3a2a1c", 0.5)                     # la tierra de debajo
        return c

    def roca_b(cara, x, y, w, h):
        return mezcla(roca(cara, x, y, w, h, 9), "#3a2a1c", 0.45)

    M = {
        "cabeza": sombra(cabeza, abajo=0.14), "torso": sombra(torso, abajo=0.18), "trapecio": sombra(trapecio),
        "panza": sombra(panza, abajo=0.2), "cinto": cinto, "faldon": faldon, "faldon_t": faldon_t,
        "muslo_d": sombra(muslo, abajo=0.12), "muslo_i": sombra(muslo, abajo=0.12),
        "pierna_d": sombra(pierna, abajo=0.16), "pierna_i": sombra(pierna, abajo=0.16),
        "pie": sombra(pie, abajo=0.1), "dedos": dedos,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.14), "antebrazo_i": sombra(antebrazo, abajo=0.14),
        "puno": sombra(puno, abajo=0.12),
        "corona_pelo": corona_pelo, "barba_o": barba_o, "trenza": trenza, "manto": sombra(manto, abajo=0.14), "capucha": capucha,
        "cuerda": cuerda, "viga": viga, "atadura": atadura,
        "asa": asa, "hombro_c": sombra(hombro_c, abajo=0.1), "cuerpo_c": sombra(cuerpo_c, abajo=0.12), "labio_c": labio_c,
        "badajo": badajo, "bola_badajo": bola_badajo,
        "melena": melena, "grenas": grenas, "barba_u": barba_u, "collar_u": collar_u, "colmillo": colmillo,
        "colmillo_g": colmillo_g, "omoplato": sombra(omoplato, abajo=0.12),
        "costilla_1": costilla(1), "costilla_2": costilla(2), "costilla_3": costilla(3), "costilla_4": costilla(4),
        "pelo_h": pelo_h, "cinta": cinta, "barba_h": barba_h, "mandil": sombra(mandil, abajo=0.1),
        "mandil_b": sombra(mandil_b, abajo=0.14), "gozne": sombra(gozne, abajo=0.1),
        "fuste": fuste, "capitel": sombra(capitel, abajo=0.1), "abaco": sombra(abaco, abajo=0.1), "anillo": anillo,
        "roca": sombra(roca, abajo=0.2), "roca_b": roca_b,
    }
    return M


def brillo(v):
    """Lo que luce: los ojos, apagados como faroles viejos; y (Ulfgar) alguna lapa del omoplato y de la costilla, verdosa."""

    def ojos(cara, x, y, w, h):
        if cara == "frente" and y == 6 and x in (2, 10):
            return OJO[v]
        if cara == "frente" and y == 6 and x in (1, 11):
            return mezcla(OJO[v], "#000000", 0.6)
        return None

    out = {"cabeza": ojos}
    if v == 1:
        def lapas(cara, x, y, w, h):
            return "#2f8f80" if (x * 5 + y * 3) % 29 == 0 else None
        out["omoplato"] = lapas
        for k in (2, 3, 4):
            out[f"costilla_{k}"] = lapas
    return out


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos
# 33); _arma = hacia donde apunta lo que lleva la derecha (la viga, la costilla y la columna van a -Z de la mano).
# _agarra: la izquierda en el arma, tantos pixeles por detras (+) o por delante (-) de la derecha.

ARRIBA = (0.0, 1.0, 0.0)
ABAJO = (0.0, -1.0, 0.0)

# de pie: muy cargado de hombros, la cabeza adelantada y alzada para mirar, las piernas algo abiertas y flexionadas
BASE = dict(raiz=raiz(-1.2), torso=(22, 0, 0), cabeza=(-20, 0, 0), faldon_f=(-6, 0, 0), faldon_t=(8, 0, 0),
            muslo_d=(-6, 0, 5), pierna_d=(9, 0, 0), muslo_i=(2, 0, -5), pierna_i=(8, 0, 0),
            grenas=(-14, 0, 0), barba_o=(16, 0, 0), barba_u=(14, 0, 0), barba_h=(12, 0, 0), trenza=(6, 0, 0))
#: lo que lleva cada uno al andar y quieto (se suma al quieto): la izquierda colgando, la derecha con su arma
PORTE = {
    # la viga al hombro derecho, la campana colgando a la espalda
    "campana": dict(_mano_d=("h", 0.5, 7.0, 9.0), _arma=(-0.05, 0.15, -0.99), _campana=True,
                    _mano_i=("h", 2.0, -30.0, 6.0), _arma_i=(0.0, -1.0, 0.2)),
    # la costilla a rastras por detras
    "costilla": dict(_mano_d=("h", -3.0, -29.0, 2.0), _arma=(-0.12, -0.42, -0.9),
                     _mano_i=("h", 2.0, -30.0, 6.0), _arma_i=(0.0, -1.0, 0.2)),
    # la columna al hombro derecho, como un tronco
    "columna": dict(_mano_d=("h", 1.5, 4.5, 11.0), _arma=(-0.04, 0.22, -0.97),
                    _mano_i=("h", 2.0, -30.0, 6.0), _arma_i=(0.0, -1.0, 0.2)),
}
# en guardia: agachado y abierto de piernas, el arma baja y adelantada en la derecha, la izquierda abierta delante
GUARDIA = dict(raiz=raiz(-4.0), torso=(30, -8, 0), cabeza=(-28, 8, 0), faldon_f=(-16, 0, 0), faldon_t=(12, 0, 0),
               muslo_d=(-18, 0, 9), pierna_d=(24, 0, 0), muslo_i=(8, 0, -9), pierna_i=(18, 0, 0),
               grenas=(-20, 0, 0), barba_o=(24, 0, 0), barba_u=(22, 0, 0), barba_h=(18, 0, 0), trenza=(10, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", -3.0, -22.0, 14.0), _arma=(0.15, -0.12, 1.0), _campana=True,
            _mano_i=("h", 6.0, -18.0, 16.0), _arma_i=(0.1, 0.1, 1.0))
# de rodillas (al morir) y de bruces
RODILLAS = dict(raiz=raiz(-14.0, 1.0), torso=(8, 0, 0), cabeza=(-10, 0, 0), faldon_f=(-30, 0, 0), faldon_t=(30, 0, 0),
                muslo_d=(-6, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(96, 0, 0))


def animaciones(r=None):
    """(bucles, acciones): los bucles por nombre y las acciones en el orden de sus numeros."""
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses, **kw):
        a = Anim(nombre, largo, bucle=True).poses(poses, r, **kw)
        B.append(a)
        return a

    def accion(nombre, largo, poses, golpes=(), interp="c"):
        a = Anim(nombre, largo, golpes=golpes, interp=interp).poses(poses, r)
        A.append(a)
        return a

    # ---- quieto: respira hondo (sube y baja el pecho y la cabeza)
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (2.5, con(BASE, torso=(25, 0, 0), cabeza=(-18, 3, 0), raiz=raiz(-1.6))),
        (5.0, BASE),
    ])
    # ---- lo que lleva cada uno (los brazos), que se suma al quieto
    for k, v in PORTE.items():
        bucle(f"porte_{k}", 5.0, [(0.0, con(BASE, v)), (5.0, con(BASE, v))],
              solo=("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i", "campana"))
    # ---- andar: el paso de un gigante (el talon derecho pisa en 0 y el izquierdo en 1,6; se suma al quieto y al porte)
    pasos = []
    for k, (md, pd, mi, pi_, ty, up, bi) in enumerate(((-24, 8, 18, 16, 6, -1.4, -14), (0, 6, -6, 44, 0, 0.9, 0),
                                                      (18, 16, -24, 8, -6, -1.4, 12), (-6, 44, 0, 6, 0, 0.9, 0),
                                                      (-24, 8, 18, 16, 6, -1.4, -14))):
        pasos.append((3.2 * k / 4, {
            "muslo_d": (md, 0, 0), "pierna_d": (pd, 0, 0), "muslo_i": (mi, 0, 0), "pierna_i": (pi_, 0, 0),
            "torso": (0, ty, -ty * 0.5), "cabeza": (0, -ty * 0.7, ty * 0.4), "raiz": raiz(up),
            "faldon_f": (min(md, mi) * 0.8, 0, 0), "faldon_t": (max(md, mi) * 0.5, 0, 0),
            "brazo_i": (bi, 0, 0), "campana": (-bi * 0.6, 0, ty), "trenza": (abs(ty) * 1.5, 0, ty),
            "grenas": (-abs(ty), 0, 0)}))
    bucle("andar", 3.2, pasos)
    # ---- en guardia: se mece despacio
    l1 = con(LISTO, raiz=raiz(-4.6), torso=(33, -10, 2), cabeza=(-30, 10, 0), _mano_d=("h", -3.4, -23.0, 13.0))
    bucle("listo", 3.0, [(0.0, LISTO), (1.5, l1), (3.0, LISTO)])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 mazazo: lo alza a dos manos por encima de la cabeza, lo sostiene y lo estrella delante; se queda clavado
    MZ_ALTO = con(L, raiz=raiz(0.4, 2.0), torso=(-8, 4, 0), cabeza=(-30, -4, 0), faldon_f=(-4, 0, 0),
                  muslo_d=(-4, 0, 7), pierna_d=(8, 0, 0), muslo_i=(14, 0, -7), pierna_i=(12, 0, 0),
                  _mano_d=("h", 10.0, 24.0, 4.0), _arma=(0.0, 0.55, -0.84), _agarra=5, _campana=False)
    MZ_BAJO = con(L, raiz=raiz(-9.0, -3.0), torso=(52, 0, 0), cabeza=(-34, 0, 0), faldon_f=(-40, 0, 0), faldon_t=(20, 0, 0),
                  muslo_d=(-44, 0, 8), pierna_d=(58, 0, 0), muslo_i=(20, 0, -8), pierna_i=(34, 0, 0),
                  _mano_d=("h", 11.0, -18.0, 22.0), _arma=(0.0, -0.62, 0.78), _agarra=5)
    accion("mazazo", 3.2, [
        (0.00, L),
        (0.90, MZ_ALTO),
        (1.25, con(MZ_ALTO, raiz=raiz(0.6, 2.4), torso=(-12, 4, 0))),
        (1.50, MZ_BAJO),
        (2.40, con(MZ_BAJO, torso=(50, 0, 0), raiz=raiz(-8.6, -3.0))),
        (2.85, con(L, raiz=raiz(-5.0), torso=(36, -4, 0))),
        (3.20, L),
    ], golpes=(1.5,))
    # ---- 2 barrido: el arma de su derecha a su izquierda, bajo (a una mano; la izquierda abierta al otro lado)
    B_D = con(L, raiz=raiz(-3.0, 1.0), torso=(20, 54, 0), cabeza=(-24, -46, 0), muslo_d=(6, 0, 9), muslo_i=(-20, 0, -9),
              _mano_d=("h", -24.0, -10.0, -8.0), _arma=(-1.0, -0.1, -0.35), _mano_i=("h", 10.0, -12.0, 20.0), _arma_i=(0.3, 0.0, 1.0))
    B_F = con(L, raiz=raiz(-6.0, -1.0), torso=(32, 6, 0), cabeza=(-30, -6, 0),
              _mano_d=("h", -6.0, -24.0, 24.0), _arma=(0.35, -0.3, 1.0), _mano_i=("h", 20.0, -14.0, 10.0), _arma_i=(1.0, 0.0, 0.2))
    B_I = con(L, raiz=raiz(-6.4, -2.0), torso=(34, -46, 0), cabeza=(-30, 36, 0), muslo_i=(-36, 0, -9), pierna_i=(36, 0, 0),
              _mano_d=("h", 22.0, -20.0, 12.0), _arma=(1.0, -0.2, 0.3), _mano_i=("h", 24.0, -14.0, -6.0), _arma_i=(1.0, 0.0, -0.4))
    accion("barrido", 2.4, [
        (0.00, L),
        (0.80, B_D),
        (1.10, con(B_D, torso=(20, 60, 0), cabeza=(-24, -52, 0))),
        (1.30, B_F),
        (1.45, B_I),
        (1.90, con(B_I, torso=(34, -50, 0))),
        (2.40, L),
    ], golpes=(1.3,))
    # ---- 3 pisoton: alza la rodilla derecha muy alto y pisa (la onda alrededor del pie)
    P_ALTO = con(L, raiz=raiz(1.0, -1.0), torso=(12, 0, -4), cabeza=(-20, 0, 2), faldon_f=(-70, 0, 0),
                 muslo_d=(-84, 0, 10), pierna_d=(76, 0, 0), muslo_i=(4, 0, -6), pierna_i=(6, 0, 0),
                 _mano_d=("h", -18.0, -16.0, 8.0), _arma=(-0.6, -0.4, 0.7), _mano_i=("h", 18.0, -10.0, 8.0), _arma_i=(1.0, 0.0, 0.3))
    P_DA = con(L, raiz=raiz(-6.0, 1.5), torso=(38, 0, 2), cabeza=(-34, 0, 0), faldon_f=(-30, 0, 0),
               muslo_d=(-34, 0, 10), pierna_d=(26, 0, 0), muslo_i=(16, 0, -6), pierna_i=(30, 0, 0),
               _mano_d=("h", -12.0, -24.0, 12.0), _arma=(-0.2, -0.5, 0.8), _mano_i=("h", 12.0, -20.0, 14.0), _arma_i=(0.6, -0.3, 0.8))
    accion("pisoton", 2.0, [
        (0.00, L),
        (0.70, P_ALTO),
        (0.95, con(P_ALTO, muslo_d=(-88, 0, 10), raiz=raiz(1.4, -1.0))),
        (1.10, P_DA),
        (1.50, con(P_DA, torso=(36, 0, 2))),
        (2.00, L),
    ], golpes=(1.1,))
    # ---- 4 patada: al que se le mete entre los pies (la pierna derecha recogida y estirada hacia delante)
    accion("patada", 1.6, [
        (0.00, L),
        (0.45, con(L, raiz=raiz(-1.0, -1.0), torso=(10, 0, 0), cabeza=(-14, 0, 0), faldon_f=(-60, 0, 0),
                   muslo_d=(-66, 0, 6), pierna_d=(96, 0, 0), muslo_i=(6, 0, -6), pierna_i=(10, 0, 0))),
        (0.80, con(L, raiz=raiz(-0.4, -2.0), torso=(-2, 0, 0), cabeza=(-6, 0, 0), faldon_f=(-80, 0, 0),
                   muslo_d=(-82, 0, 4), pierna_d=(14, 0, 0), muslo_i=(10, 0, -6), pierna_i=(14, 0, 0))),
        (1.10, con(L, raiz=raiz(-2.0, -1.0), torso=(16, 0, 0), faldon_f=(-40, 0, 0), muslo_d=(-44, 0, 6), pierna_d=(70, 0, 0))),
        (1.60, L),
    ], golpes=(0.8,))
    # ---- 5 roca: se agacha, mete la izquierda en el suelo y arranca una roca (1,0 s), la alza por detras de la cabeza y
    # se la tira a quien esta lejos (2,1 s)
    R_BAJO = con(L, raiz=raiz(-14.0, 2.0), torso=(68, -24, 0), cabeza=(-50, 18, 0), faldon_f=(-50, 0, 0),
                 muslo_d=(-30, 0, 9), pierna_d=(70, 0, 0), muslo_i=(-56, 0, -12), pierna_i=(80, 0, 0),
                 _mano_d=("h", -8.0, -26.0, 4.0), _arma=(-0.2, -0.3, -1.0), _mano_i=("h", 2.0, -30.0, 12.0), _arma_i=(0.0, -1.0, 0.4))
    R_ATRAS = con(L, raiz=raiz(-1.0, -1.0), torso=(-6, -36, 0), cabeza=(-24, 30, 0), muslo_d=(-18, 0, 9), muslo_i=(14, 0, -9),
                  _mano_d=("h", -14.0, -24.0, 10.0), _arma=(-0.3, -0.3, 1.0), _mano_i=("h", 6.0, 20.0, -14.0), _arma_i=(0.0, 1.0, -0.3))
    R_TIRA = con(L, raiz=raiz(-5.0, 2.0), torso=(40, 22, 0), cabeza=(-30, -18, 0), muslo_d=(8, 0, 9), muslo_i=(-30, 0, -9),
                 pierna_i=(30, 0, 0), _mano_d=("h", -18.0, -22.0, 0.0), _arma=(-0.4, -0.3, -1.0),
                 _mano_i=("h", -4.0, 6.0, 28.0), _arma_i=(0.0, 0.3, 1.0))
    accion("roca", 3.2, [
        (0.00, L),
        (0.60, R_BAJO),
        (1.00, con(R_BAJO, _mano_i=("h", 2.0, -27.0, 11.0))),
        (1.55, con(R_ATRAS, torso=(10, -24, 0), _mano_i=("h", 4.0, 14.0, -4.0))),
        (1.90, R_ATRAS),
        (2.10, R_TIRA),
        (2.50, con(R_TIRA, torso=(44, 26, 0), _mano_i=("h", -8.0, -18.0, 20.0), _arma_i=(0.0, -0.6, 1.0))),
        (3.20, L),
    ], golpes=(1.0, 2.1))
    # ---- 6 rugido: toma aire encogido y ruge con el pecho fuera, la cabeza atras y los brazos abiertos
    accion("rugido", 2.8, [
        (0.00, L),
        (0.55, con(L, torso=(40, 0, 0), cabeza=(10, 0, 0), raiz=raiz(-5.0),
                   _mano_d=("h", -6.0, -28.0, 8.0), _mano_i=("h", 6.0, -28.0, 8.0))),
        (1.00, con(BASE, raiz=raiz(-0.4), torso=(-14, 0, 0), cabeza=(-44, 0, 0), faldon_f=(-2, 0, 0), _campana=True,
                   _mano_d=("h", -24.0, 8.0, 10.0), _arma=(-0.6, 0.7, 0.4), _mano_i=("h", 24.0, 8.0, 10.0), _arma_i=(0.6, 0.7, 0.4))),
        (2.00, con(BASE, raiz=raiz(-0.8), torso=(-10, 0, 0), cabeza=(-38, 0, 0), _campana=True,
                   _mano_d=("h", -22.0, 6.0, 12.0), _arma=(-0.6, 0.7, 0.4), _mano_i=("h", 22.0, 6.0, 12.0), _arma_i=(0.6, 0.7, 0.4))),
        (2.80, L),
    ], golpes=(1.0,))
    # ---- 7 aturdido: le tiembla todo y cae sobre la rodilla izquierda, apoyado en el arma; la cabeza queda baja
    AT_RODILLA = con(L, raiz=raiz(-12.0, 1.0), torso=(40, 0, 0), cabeza=(6, 0, 0), faldon_f=(-50, 0, 0), faldon_t=(20, 0, 0),
                     muslo_d=(-72, 0, 8), pierna_d=(78, 0, 0), muslo_i=(20, 0, -6), pierna_i=(100, 0, 0),
                     _mano_d=("h", -6.0, -22.0, 16.0), _arma=(0.05, -0.9, 0.4), _mano_i=("h", 4.0, -24.0, 14.0), _arma_i=(0.0, -1.0, 0.3))
    accion("aturdido", 3.0, [
        (0.00, L),
        (0.35, con(L, raiz=raiz(-2.0, -3.0), torso=(-14, 10, 0), cabeza=(-40, -12, 0), muslo_d=(10, 0, 9), muslo_i=(-20, 0, -9))),
        (0.90, AT_RODILLA),
        (2.30, con(AT_RODILLA, torso=(44, 0, 0), cabeza=(10, 0, 0))),
        (3.00, L),
    ])
    # ---- 8 morir: el golpe; cae de rodillas (el arma al suelo), habla mirando al cielo y cae de bruces
    VENCIDO = con(RODILLAS, torso=(14, 0, 0), cabeza=(-36, 0, 0), _mano_d=("h", -4.0, -26.0, 10.0), _arma=(0.05, -0.8, 0.6),
                  _mano_i=("h", 4.0, -26.0, 10.0), _arma_i=(0.0, -1.0, 0.3))
    BRUCES = dict(raiz=raiz(4.6, 34.0, rot=(86, 0, 0)), torso=(0, 0, 0), cabeza=(-6, 26, 0), faldon_f=(0, 0, 0), faldon_t=(-10, 0, 0),
                  muslo_d=(4, 0, 8), pierna_d=(8, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(24, 0, 0),
                  _mano_d=("h", -20.0, -4.0, 18.0), _arma=(-0.3, -0.05, 1.0), _mano_i=("h", 20.0, -4.0, 22.0), _arma_i=(0.3, -0.1, 1.0))
    accion("morir", 9.0, [
        (0.00, con(L, torso=(-16, 8, 0), cabeza=(-36, 0, 0))),
        (0.60, con(L, raiz=raiz(-3.0, -3.0), torso=(-12, 12, 4), cabeza=(-34, -6, 0), muslo_d=(14, 0, 9), muslo_i=(-14, 0, -9))),
        (1.60, con(RODILLAS, torso=(26, 0, 0), cabeza=(10, 0, 0), _mano_d=("h", -4.0, -26.0, 12.0), _arma=(0.05, -0.8, 0.6),
                   _mano_i=("h", 4.0, -26.0, 12.0), _arma_i=(0.0, -1.0, 0.3))),
        (2.60, VENCIDO),
        (6.00, con(VENCIDO, torso=(18, 0, 0), cabeza=(-30, 0, 0))),
        (6.60, con(RODILLAS, torso=(40, 0, 0), cabeza=(16, 0, 0), _mano_d=("h", -8.0, -22.0, 16.0), _arma=(0.0, -0.9, 0.4),
                   _mano_i=("h", 8.0, -22.0, 16.0), _arma_i=(0.0, -1.0, 0.3))),
        (7.40, BRUCES),
        (9.00, BRUCES),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if nombre in ("barrido", "pisoton", "porte_costilla") or nombre.endswith("costilla"):
        return 1
    if nombre in ("roca", "aturdido", "porte_columna") or nombre.endswith("columna"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_campana"]]
    if nombre == "quieto":
        return [anims["porte_campana"]]
    return []


#: los ticks de la roca: cuando la arranca y cuando la suelta (los dos golpes de la accion)
TABLA_EXTRA = ()
