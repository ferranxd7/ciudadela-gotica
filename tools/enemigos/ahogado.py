"""
El marinero ahogado de Brumavela (parte 3c): la tripulacion de La Garza Gris, que el mar de nubes devolvio. Hinchado, la
piel verdosa, algas colgando y los ojos como dos luces palidas; lento y pesado, pero pega fuerte. Dos:
  0  el sable   (panuelo a la cabeza; tajos anchos y lentos, y una tanda de tres)
  1  el arpon   (el pelo mojado a la cara; lo lanza de lejos y pincha de cerca)
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (Textura, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,
                   sombra, tela)

NOMBRE = "ahogado"
VARIANTES = ("sable", "arpon")

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), brazo=(4, 12, 4), pierna=(4, 12, 4))
    # la barriga hinchada y los jirones de la camisa
    r.caja("torso", -3.5, -7.5, -4.0, 7, 6, 2, "barriga")
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2.5, 8, 4, 5, "faldon", grow=g(0.35))])
    # el panuelo (variante 0) y el pelo mojado (variante 1)
    r.pieza("panuelo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 4, 8, "panuelo", grow=g(0.4)),
        Box(2.5, -5, 4, 2, 5, 1, "panuelo_nudo"),
    ])
    r.pieza("pelo", "cabeza", (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 9, 8, "pelo", grow=g(0.45))])
    # algas: del hombro izquierdo y del brazo derecho
    r.pieza("alga_hombro", "torso", (3.5, -12.2, 0), rot=(0, 0, -6), boxes=[Box(-1.5, 0, -3, 3, 9, 0, "alga")])
    r.pieza("alga_brazo", "antebrazo_d", (-2.1, 1, 0), boxes=[Box(0, 0, -1.5, 0, 6, 3, "alga_corta")])
    # ---- el sable: la hoja es un plano de canto (se pinta su silueta)
    r.pieza("sable", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2, 1, 1, 5, "puno"),
        Box(-1.5, -1.0, -3, 3, 2, 1, "guarda"),
        Box(0, -3.0, -19, 0, 5, 16, "hoja_sable"),
    ])
    # ---- el arpon: el asta en dos trozos y la punta con sus dientes (otro plano)
    r.pieza("arpon", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -16, 1, 1, 14, "asta"),
        Box(-0.5, -0.5, -2, 1, 1, 14, "asta"),
        Box(0, -2.5, -23, 0, 5, 8, "punta_arpon"),
    ])
    r.empaquetar()
    return r


OCULTAS = {
    0: ["pelo", "arpon"],
    1: ["panuelo", "sable"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#27332f", "#34443e", "#43574e", "#546c60", "#688273", "#7e9886"]
MORADO = "#3c3144"
OJO, OJO_B = "#d8fff0", "#9fe8d8"
BOCA = "#0e1413"
CAMISA = {
    0: (["#3b4046", "#50565c", "#676d72", "#7f8588", "#989d9d"], ["#1c2a3c", "#25364c", "#30455f"]),   # rayas azul marino
    1: (["#3a3327", "#4c4333", "#5f5440", "#73664e", "#87795d"], ["#33261b", "#423224", "#523f2e"]),   # jersey pardo
}
CALZON = ["#15191d", "#1d2328", "#262e34", "#303a41"]
PANUELO = ["#3d1418", "#521a1f", "#6a2228", "#832c33"]
PELO = ["#0c0e0d", "#151918", "#1f2523", "#2a322f"]
ALGA = ["#16301c", "#1f4426", "#2b5a32", "#3a7340"]
PALO = ["#2a2118", "#3a2e22", "#4b3c2d", "#5d4b39"]
HIERRO = ["#1f2224", "#33383b", "#4b5256", "#687176", "#8a9499"]
OXIDO = ["#4a2a18", "#6a3c20", "#8a5028"]
PERCEBE = "#a9a895"


# la punta del arpon, con sus dientes (vista desde el costado izquierdo: la punta a la izquierda)
PUNTA = [
    "...h....",
    "..hH.h..",
    "hHHHHHHH",
    "..hH.h..",
    "...h....",
]
# el arpon en vuelo (ArponRenderer): lo que mide el dibujo en la textura, de la cola a la punta
ARPON_LARGO, ARPON_ALTO = 35, 5


def extras(carpeta):
    """Lo que no es del modelo: el arpon en vuelo, que se dibuja con dos planos cruzados (la cola a la izquierda)."""
    import os

    from PIL import Image

    from pinta import hexa
    px = lambda c: tuple(int(round(v * 255)) for v in hexa(c)) + (255,)   # noqa: E731
    im = Image.new("RGBA", (64, 8), (0, 0, 0, 0))
    asta = ARPON_LARGO - len(PUNTA[0])
    for x in range(asta):
        im.putpixel((x, 2), px(PALO[1 + (x * 7) % 3]))
    colores = {"H": HIERRO[4], "h": HIERRO[3]}
    for y, fila in enumerate(PUNTA):
        for x, ch in enumerate(reversed(fila)):
            if ch != ".":
                im.putpixel((asta + x, y), px(colores[ch]))
    im.save(os.path.join(carpeta, "arpon.png"))


def _cara():
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "E": OJO, "e": OJO_B, "M": BOCA, "m": MORADO, "n": PIEL[1], "p": PERCEBE,
         "a": ALGA[2]}
    return dibujo([
        "OossssoO",
        "osSSSSso",
        "osSssSpo",
        "OmmoommO",
        "oEesseEo",
        "Oosnnsoa",
        "OoMMMMoa",
        "OOoMMoOO",
    ], C)


def materiales(variante):
    tonos, rayas = CAMISA[variante]
    piel = ruido(PIEL[1:5], 111, grano=0.55, base=0.5)
    base_camisa = tela(tonos, 121 + variante, pliegue=4, grano=0.12)
    calzon = tela(CALZON, 131, pliegue=4, grano=0.1)

    def camisa(cara, x, y, w, h):
        c = base_camisa(cara, x, y, w, h)
        # las rayas (variante 0) o el punto grueso (variante 1)
        if cara not in ("arriba", "abajo"):
            if variante == 0 and y % 3 == 1:
                return rayas[1 if (x + y) % 5 else 0]
            if variante == 1 and (x + y) % 4 == 0:
                return rayas[1]
        return c

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara()(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 3 <= y <= 4 and d in (3, 4):
                return PIEL[1]
            if (x * 5 + y * 3) % 17 == 0:
                return PERCEBE
        if cara == "arriba" and (x * 3 + y) % 7 == 0:
            return PIEL[1]
        return c

    def torso(cara, x, y, w, h):
        c = camisa(cara, x, y, w, h)
        if cara == "frente":
            if y <= 2 and abs(x - 3.5) <= 1.5 - y * 0.5:
                return PIEL[2]
            if y == 9:
                return "#1a1410" if x != 4 else HIERRO[3]      # el cinto y su hebilla
        if cara == "espalda" and y == 9:
            return "#1a1410"
        # manchas de verdin
        if (x * 7 + y * 11 + {"frente": 0, "espalda": 5, "der": 9, "izq": 3}.get(cara, 1)) % 23 == 0:
            return ALGA[1]
        return c

    def barriga(cara, x, y, w, h):
        if cara in ("espalda",):
            return None
        c = camisa(cara, x, y + 4, w, h + 6)
        if cara == "frente" and y >= h - 2 and (x in (2, 3, 4)):
            return PIEL[2] if y == h - 2 else PIEL[1]             # la camisa no le cierra: se le ve la tripa
        return claro(c, 0.06) if cara == "frente" else c

    def faldon(cara, x, y, w, h):
        corte = (2, 4, 3, 1, 4, 2, 3, 4, 1, 3)[(x * 3 + {"frente": 2, "espalda": 6, "der": 0, "izq": 5}.get(cara, 1)) % 10]
        if cara in ("arriba", "abajo") or y > corte:
            return None
        return oscuro(camisa(cara, x, y + 2, w, h + 2), 0.15 + 0.05 * y)

    def brazo(cara, x, y, w, h):
        return camisa(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y == 0 and x % 2 == 0:
            return camisa(cara, x, y, w, h)
        if y >= h - 1:
            return PIEL[1]
        c = piel(cara, x, y, w, h)
        return PERCEBE if (x * 5 + y * 7) % 19 == 0 else c

    def pierna(cara, x, y, w, h):
        if y <= 1:
            return calzon(cara, x, y, w, h)
        if y >= h - 1:
            return PIEL[1]
        return piel(cara, x, y, w, h)

    def panuelo(cara, x, y, w, h):
        t = tela(PANUELO, 141, pliegue=3)(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if cara == "frente" and y >= 3 and 1 <= x <= w - 2:
            return None
        return t

    def pelo(cara, x, y, w, h):
        # mechones mojados: caen mas largos por detras y a los lados, y uno le cruza la cara
        k = {"frente": (2, 3, 2, 5, 2, 2, 3, 2), "espalda": (8, 9, 8, 9, 9, 8, 9, 8), "der": (7, 8, 6, 5, 4, 3, 3, 2),
             "izq": (2, 3, 3, 4, 5, 6, 8, 7)}.get(cara)
        if cara == "arriba":
            return PELO[1 + (x + y) % 2]
        if cara == "abajo" or k is None:
            return None
        if y >= k[x % 8]:
            return None
        return PELO[(x + (y // 2)) % 3]

    def alga(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return None
        centro = 1 + (1 if (y // 2) % 2 else 0)
        if abs(x - centro) > (1 if y < h - 2 else 0):
            return None
        return ALGA[(x + y) % 3 + (1 if x == centro else 0)]

    def alga_corta(cara, x, y, w, h):
        if cara not in ("der", "izq"):
            return None
        if abs(x - 1) > (0 if y % 3 == 2 else 1) or y >= h - (x % 2):
            return None
        return ALGA[(x + y) % 3]

    S = {"H": HIERRO[4], "h": HIERRO[3], "d": HIERRO[2], "o": OXIDO[1], "x": OXIDO[0]}
    hoja = dibujo([
        # (desde su costado izquierdo: la punta a la izquierda) hoja curva, ancha en la punta
        ".hHHHh..........",
        "hHHdddHHhhhh....",
        "hHdddodddddddhhh",
        ".hhHddoodddxdddh",
        "...hhhHHHhhhhhh.",
    ], S)
    punta = dibujo(PUNTA, S)

    return {
        "cabeza": sombra(cabeza, abajo=0.14),
        "torso": sombra(torso),
        "barriga": barriga,
        "faldon": faldon,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.18), "antebrazo_i": sombra(antebrazo, abajo=0.18),
        "muslo_d": sombra(calzon, abajo=0.1), "muslo_i": sombra(calzon, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.25), "pierna_i": sombra(pierna, abajo=0.25),
        "panuelo": panuelo, "panuelo_nudo": sombra(tela(PANUELO, 142, pliegue=2), abajo=0.3),
        "pelo": pelo,
        "alga": alga, "alga_corta": alga_corta,
        "puno": madera(PALO, 151, "z"), "guarda": metal(HIERRO[1:5], 152),
        "hoja_sable": por_cara(liso(None), izq=hoja, der=espejado(hoja)),
        "asta": madera(PALO, 153, "z"),
        "punta_arpon": por_cara(liso(None), izq=punta, der=espejado(punta)),
    }


def brillo(variante):
    """Lo que brilla: los ojos."""
    C = {"E": OJO, "e": "#7fd8c8"}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "........",
        ".Ee..eE.",
    ], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones

# de pie: echado hacia atras, la cabeza caida a un lado, los brazos colgando pesados
BASE = dict(raiz=raiz(-0.3), torso=(-5, 0, -3), cabeza=(6, 0, 12),
            muslo_d=(3, 0, 4), pierna_d=(4, 0, 0), muslo_i=(-3, 0, -4), pierna_i=(6, 0, 0),
            brazo_i=(2, 0, -8), antebrazo_i=(-8, 0, 0))
PORTE = {
    "sable": dict(_mano_d=("h", -1.0, -8.7, -0.5), _arma=(-0.15, -0.5, -1)),       # la punta arrastrando por detras
    "arpon": dict(_mano_d=("h", -1.5, -5.5, 4.0), _arma=(0.0, 1, -0.12)),          # de pie, como un baston
}
GUARDIA = dict(raiz=raiz(-1.0), torso=(10, 6, 0), cabeza=(-4, -6, 6),
               muslo_d=(10, 0, 6), pierna_d=(14, 0, 0), muslo_i=(-20, 0, -6), pierna_i=(22, 0, 0))
LISTO = {
    "sable": con(GUARDIA, _mano_d=("h", -1.0, -4.0, 5.5), _arma=(0.25, 0.9, 0.5), brazo_i=(-18, 0, -20), antebrazo_i=(-40, 0, 0)),
    # el arpon a dos manos, cruzado por delante: el hombro izquierdo adelantado y la punta buscando el centro
    "arpon": con(GUARDIA, torso=(10, 28, 0), cabeza=(-4, -26, 6), _mano_d=("h", 2.0, -6.8, 1.5), _arma=(0.4, 0.1, 1), _agarra=-8),
}
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")

# el arponero, tras lanzar, saca otro arpon de la espalda: a partir de aqui (segundos de la accion) vuelve a llevarlo
ARPON_OTRO = 1.9
TABLA_EXTRA = (
    "",
    "    /** El tick de arpon_lanzar desde el que el arponero vuelve a llevar arpon (saca otro de la espalda). */",
    f"    public static final int ARPON_OTRO = {int(round(ARPON_OTRO * 20))};",
)


def animaciones(r=None):
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

    bucle("quieto", 5.0, [
        (0.0, BASE),
        (1.7, con(BASE, torso=(-7, 2, -5), cabeza=(8, 3, 16), raiz=raiz(-0.6))),
        (3.4, con(BASE, torso=(-4, -2, -1), cabeza=(4, -4, 8), raiz=raiz(-0.2))),
        (5.0, BASE),
    ])
    # andar: pesado, balanceandose de lado a lado
    pasos = []
    for k, (md, mi, pd, pi_, tz, up) in enumerate((
            (-20, 16, 6, 16, 5, 0.0), (-2, 2, 4, 30, 0, 0.6), (16, -20, 16, 6, -5, 0.0), (2, -2, 30, 4, 0, 0.6), (-20, 16, 6, 16, 5, 0.0))):
        pasos.append((1.5 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (2, tz * 0.6, tz), "cabeza": (0, 0, -tz * 0.8), "raiz": raiz(up),
                                    "brazo_i": (md * 0.35, 0, 0)}))
    bucle("andar", 1.5, pasos)
    for arma, p in PORTE.items():
        q = con(BASE, p)
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    for arma, p in LISTO.items():
        bucle(f"listo_{arma}", 2.2, [
            (0.0, p),
            (1.1, con(p, raiz=raiz(-1.5), torso=(p["torso"][0] + 3, p["torso"][1] - 2, 2), cabeza=(p["cabeza"][0] + 2, p["cabeza"][1] + 2, 10))),
            (2.2, p),
        ])

    S, P = LISTO["sable"], LISTO["arpon"]

    # ---- el sable: un tajo ancho de arriba abajo (lento: se ve venir de lejos)
    S_ALTO = con(S, raiz=raiz(-0.4, 1.6), torso=(-12, 34, 0), cabeza=(-10, -28, 0), brazo_i=(-50, 0, -30), antebrazo_i=(-30, 0, 0),
                 muslo_d=(14, 0, 6), pierna_d=(10, 0, 0), _mano_d=("h", -2.5, 8.2, -2.5), _arma=(-0.2, 0.7, -0.7))
    S_BAJO = con(S, raiz=raiz(-2.8, -3.0), torso=(36, -36, 0), cabeza=(-22, 28, 0), muslo_i=(-42, 0, -4), pierna_i=(40, 0, 0),
                 brazo_i=(20, 0, -30), antebrazo_i=(-30, 0, 0), _mano_d=("h", 6.0, -6.0, 5.0), _arma=(0.6, -0.9, 0.5))
    S_MEDIO = con(S, raiz=raiz(-1.8, -1.6), torso=(18, -4, 0), cabeza=(-14, 2, 0), brazo_i=(-10, 0, -30), antebrazo_i=(-30, 0, 0),
                  _mano_d=("h", 2.0, 1.0, 8.5), _arma=(0.15, 0.2, 1))
    accion("sable_tajo", 1.6, [
        (0.00, S),
        (0.50, S_ALTO),
        (0.74, con(S_ALTO, raiz=raiz(-0.2, 1.9), torso=(-16, 38, 0), cabeza=(-12, -32, 0))),
        (0.82, S_MEDIO),
        (0.90, S_BAJO),
        (1.15, con(S_BAJO, torso=(34, -40, 0))),
        (1.60, S),
    ], golpes=(0.82,))
    # ---- el sable: tres tajos seguidos avanzando (de fuera adentro, de vuelta, y de arriba abajo)
    S_FUERA = con(S, raiz=raiz(-1.0, 0.8), torso=(8, 44, 0), cabeza=(-6, -36, 0), brazo_i=(-20, 0, -26), antebrazo_i=(-40, 0, 0),
                  _mano_d=("h", -6.5, 0.5, 2.5), _arma=(-1, 0.2, 0.1))
    S_DENTRO = con(S, raiz=raiz(-2.0, -2.2), torso=(18, -46, 0), cabeza=(-10, 38, 0), muslo_i=(-34, 0, -4), pierna_i=(30, 0, 0),
                   brazo_i=(10, 0, -34), antebrazo_i=(-30, 0, 0), _mano_d=("h", 7.0, -1.0, 4.0), _arma=(1, 0.1, 0.25))
    accion("sable_combo", 2.7, [
        (0.00, S),
        (0.48, S_FUERA),
        (0.66, con(S_FUERA, torso=(8, 48, 0), cabeza=(-6, -40, 0))),
        (0.74, S_MEDIO),
        (0.84, S_DENTRO),
        (1.20, con(S_DENTRO, torso=(18, -52, 0), cabeza=(-10, 42, 0))),
        (1.30, con(S_MEDIO, raiz=raiz(-2.0, -3.4), muslo_i=(-40, 0, -4), pierna_i=(36, 0, 0))),
        (1.40, con(S_FUERA, raiz=raiz(-2.0, -4.4), torso=(16, 42, 0), muslo_i=(-40, 0, -4), pierna_i=(36, 0, 0))),
        (1.62, con(S_ALTO, raiz=raiz(-0.6, -3.6))),
        (1.88, con(S_ALTO, raiz=raiz(-0.4, -3.4), torso=(-16, 38, 0), cabeza=(-12, -32, 0))),
        (1.96, con(S_MEDIO, raiz=raiz(-2.0, -5.6))),
        (2.04, con(S_BAJO, raiz=raiz(-3.0, -7.2))),
        (2.30, con(S_BAJO, raiz=raiz(-2.8, -7.0), torso=(34, -40, 0))),
        (2.70, S),
    ], golpes=(0.74, 1.30, 1.96))
    # ---- el arpon: lo levanta sobre el hombro, se echa atras y lo lanza (lo suelta en el golpe)
    P_HOMBRO = con(sin(P, "_agarra"), raiz=raiz(-0.6, 1.8), torso=(-14, 40, 0), cabeza=(-6, -34, 0),
                   muslo_d=(16, 0, 6), pierna_d=(12, 0, 0), muslo_i=(-26, 0, -4), pierna_i=(12, 0, 0),
                   brazo_i=(-80, 0, -10), antebrazo_i=(-10, 0, 0), _mano_d=("h", -1.0, 5.0, -5.5), _arma=(0.05, 0.1, 1))
    P_SUELTA = con(sin(P, "_agarra"), raiz=raiz(-2.4, -3.0), torso=(30, -38, 0), cabeza=(-20, 30, 0),
                   muslo_d=(28, 0, 4), pierna_d=(10, 0, 0), muslo_i=(-44, 0, -4), pierna_i=(38, 0, 0),
                   brazo_i=(30, 0, -26), antebrazo_i=(-30, 0, 0), _mano_d=("h", 3.0, 2.0, 8.3), _arma=(0, -0.1, 1))
    # ...y saca otro de la espalda (el modelo no dibuja el arpon desde que lo suelta hasta ARPON_OTRO: ahi tiene la
    # mano detras del hombro)
    P_ESPALDA = con(sin(P, "_agarra"), raiz=raiz(-0.8, 0.4), torso=(2, 18, 0), cabeza=(2, -14, 6),
                    brazo_i=(-10, 0, -14), antebrazo_i=(-20, 0, 0), _mano_d=("h", -1.5, 3.4, -3.6), _arma=(0.1, 1, -0.45))
    accion("arpon_lanzar", 2.5, [
        (0.00, P),
        (0.55, P_HOMBRO),
        (0.95, con(P_HOMBRO, raiz=raiz(-0.4, 2.2), torso=(-18, 44, 0), cabeza=(-6, -38, 0), _mano_d=("h", -1.0, 5.2, -6.0))),
        (1.05, P_SUELTA),
        (1.40, con(P_SUELTA, torso=(28, -40, 0), _mano_d=("h", 4.0, -2.0, 7.5), _arma=(0, -0.5, 1))),
        (ARPON_OTRO, P_ESPALDA),
        (ARPON_OTRO + 0.12, P_ESPALDA),
        (2.50, P),
    ], golpes=(1.05,))
    # ---- el arpon: de cerca, pincha (como una lanza corta)
    P_ATRAS = con(P, raiz=raiz(-1.4, 2.0), torso=(6, 40, 0), cabeza=(-2, -34, 0), muslo_d=(18, 0, 6), pierna_d=(18, 0, 0),
                  _mano_d=("h", 1.0, -6.0, -5.0), _arma=(0.05, 0.05, 1), _agarra=-9)
    P_FONDO = con(sin(P, "_agarra"), raiz=raiz(-2.4, -3.8), torso=(26, -30, 0), cabeza=(-20, 26, 0), muslo_d=(28, 0, 4), pierna_d=(10, 0, 0),
                  muslo_i=(-44, 0, -4), pierna_i=(38, 0, 0), brazo_i=(36, 0, -24), antebrazo_i=(-30, 0, 0),
                  _mano_d=("h", 3.5, -3.0, 7.6), _arma=(0, 0.02, 1))
    accion("arpon_pinchar", 1.7, [
        (0.00, P),
        (0.55, P_ATRAS),
        (0.80, con(P_ATRAS, raiz=raiz(-1.4, 2.3), torso=(4, 44, 0), _mano_d=("h", 1.0, -6.0, -5.8))),
        (0.92, P_FONDO),
        (1.20, con(P_FONDO, torso=(24, -28, 0))),
        (1.70, P),
    ], golpes=(0.92,))
    # ---- aturdido: se tambalea, casi se cae de espaldas
    AT = dict(raiz=raiz(-1.4, 2.4), torso=(-22, 0, 10), cabeza=(-20, 0, 18), muslo_d=(-10, 0, 10), pierna_d=(24, 0, 0), muslo_i=(14, 0, -10),
              pierna_i=(18, 0, 0), brazo_d=(24, 0, 34), antebrazo_d=(-14, 0, 0), mano_d=(60, 0, 0), brazo_i=(26, 0, -36), antebrazo_i=(-12, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(4, 0, 0), cabeza=(0, 0, 6), raiz=raiz(-0.6, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-14, 8, -12), cabeza=(-12, -8, -14), raiz=raiz(-1.8, 3.0))),
        (1.05, con(AT, torso=(-18, -6, 8), cabeza=(-16, 8, 14), raiz=raiz(-1.6, 2.6))),
        (1.50, con(AT, torso=(6, 0, 0), cabeza=(2, 0, 8), raiz=raiz(-0.8, 0.8))),
    ])
    # ---- morir: se dobla y cae de espaldas
    CAIDO = dict(raiz=raiz(2.6, -14.0, rot=(-88, 0, 0)), torso=(0, 0, 0), cabeza=(10, 30, 0),
                 muslo_d=(-6, 0, 10), pierna_d=(10, 0, 0), muslo_i=(4, 0, -14), pierna_i=(30, 0, 0),
                 _mano_d=("h", -7.0, -0.8, 1.0), _arma=(-0.9, 0.0, 0.5), brazo_i=(-10, 0, -60), antebrazo_i=(-40, 0, 0))
    accion("morir", 1.8, [
        (0.00, con(BASE, torso=(14, 0, 4), cabeza=(20, 0, 8), brazo_d=(10, 0, 20), antebrazo_d=(-30, 0, 0), mano_d=(60, 0, 0))),
        (0.40, dict(raiz=raiz(-3.0, 1.0), torso=(-24, 0, 6), cabeza=(-30, 0, 10), muslo_d=(-20, 0, 8), pierna_d=(50, 0, 0),
                    muslo_i=(-10, 0, -8), pierna_i=(40, 0, 0), brazo_d=(-30, 0, 40), antebrazo_d=(-20, 0, 0), mano_d=(70, 0, 0),
                    brazo_i=(-30, 0, -44), antebrazo_i=(-20, 0, 0))),
        (0.85, dict(raiz=raiz(-4.0, -6.0, rot=(-50, 0, 0)), torso=(-10, 0, 0), cabeza=(-10, 10, 0), muslo_d=(-30, 0, 10), pierna_d=(60, 0, 0),
                    muslo_i=(-20, 0, -12), pierna_i=(50, 0, 0), brazo_d=(-30, 0, 50), antebrazo_d=(-20, 0, 0), mano_d=(80, 0, 0),
                    brazo_i=(-20, 0, -56), antebrazo_i=(-30, 0, 0))),
        (1.25, CAIDO),
        (1.80, CAIDO),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    for v, arma in enumerate(VARIANTES):
        if arma in nombre:
            return v
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_sable"]]
    if nombre == "quieto":
        return [anims["porte_sable"]]
    return []
