"""
El aldeano hueco de Brumavela (parte 3c): lo que queda de la gente del pueblo. Flaco, encorvado, la piel gris y las
cuencas de los ojos vacias; ropa de labrador hecha jirones. Tres, con la herramienta que les quedo en la mano:
  0  la horca    (capucha; estocada larga y barrido)
  1  la hoz      (sombrero de paja; tajos rapidos)
  2  la antorcha (la cabeza vendada; golpes que prenden)
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (Textura, brillo_de, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara,
                   ruido, sombra, tela)

NOMBRE = "hueco"
VARIANTES = ("horca", "hoz", "antorcha")

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, brazo=(3, 12, 4))
    # los jirones de la camisa, por debajo de la cintura
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2, 8, 5, 4, "faldon", grow=g(0.4))])
    # la capucha (variante 0): por encima de la cabeza, con su pico caido por detras
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 8, 8, "capucha", grow=g(0.55)),
        Box(-2, -6.5, 4, 4, 5, 2, "capucha_pico"),
    ])
    # el sombrero de paja (variante 1): el ala caida y la copa
    r.pieza("sombrero", "cabeza", (0, -7.6, 0), rot=(-5, 0, 4), boxes=[
        Box(-7, -1, -7, 14, 1, 14, "ala"),
        Box(-4, -4, -4, 8, 3, 8, "copa", grow=g(0.3)),
    ])
    # la venda (variante 2): una vuelta de trapo por la frente y un ojo
    r.pieza("venda", "cabeza", (0, 0, 0), boxes=[Box(-4, -7, -4, 8, 4, 8, "venda", grow=g(0.3))])
    # ---- lo que llevan en la mano derecha (el arma apunta hacia delante, -Z, con el brazo caido)
    r.pieza("horca", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -21, 1, 1, 17, "mango"),
        Box(-0.5, -0.5, -4, 1, 1, 16, "mango"),
        Box(-2.5, -0.5, -22, 5, 1, 1, "hierro"),
        Box(-2.5, -0.5, -27, 1, 1, 5, "hierro", uv="diente"),
        Box(1.5, -0.5, -27, 1, 1, 5, "hierro", uv="diente"),
        Box(-0.5, -0.5, -28, 1, 1, 6, "hierro"),
    ])
    r.pieza("hoz", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -4, 1, 1, 7, "mango"),
        # la hoja: un plano de canto (se pinta la silueta de la media luna)
        Box(0, -8, -13, 0, 9, 10, "hoja_hoz"),
    ])
    r.pieza("antorcha", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -8, 1, 1, 11, "mango"),
        Box(-1, -1, -11, 2, 2, 3, "estopa"),
    ])
    r.pieza("llama", "antorcha", (0, 0, -11), boxes=[
        Box(-1.5, -1.5, -4, 3, 3, 4, "llama"),
        Box(-0.5, -0.5, -6, 1, 1, 2, "llama_punta"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {
    0: ["sombrero", "venda", "hoz", "antorcha"],
    1: ["capucha", "venda", "horca", "antorcha"],
    2: ["capucha", "sombrero", "horca", "hoz"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#35322e", "#47423c", "#5a544b", "#6e665b", "#82796c", "#968c7d"]
HUESO = "#b3a88f"
CUENCA, BOCA = "#0b0909", "#151010"
ROPA = {
    0: dict(camisa=["#241c15", "#33281d", "#433526", "#544230", "#65503a"], calzon=["#191715", "#24211e", "#302c28", "#3c3732"]),
    1: dict(camisa=["#4a4636", "#5e5945", "#736d55", "#888166", "#9d9678"], calzon=["#2a2118", "#382c20", "#473829", "#574533"]),
    2: dict(camisa=["#20242a", "#2c323a", "#39414a", "#47505b", "#56616d"], calzon=["#1b1a1c", "#262527", "#323033", "#3e3c40"]),
}
CAPUCHA = ["#1c1f17", "#272b20", "#33382a", "#404634", "#4e553f"]
PAJA = ["#5e4c22", "#78622c", "#927a38", "#ab9146", "#c2a857"]
TRAPO = ["#4d4a40", "#636052", "#7a7665", "#918c79"]
PALO = ["#2e2014", "#3f2d1c", "#513b25", "#644a2f", "#785a3a"]
HIERRO = ["#232327", "#38383e", "#515158", "#6e6e78", "#8f8f9a", "#b0b0bb"]
SANGRE = "#3a1512"


def _cara(variante):
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": CUENCA, "k": "#1d1816", "M": BOCA, "T": HUESO,
         "n": PIEL[1], "r": SANGRE}
    filas = [
        "OossssoO",
        "osSSSSso",
        "oSSssSSo",
        "OKKosKKO",
        "oKkonkKo",
        "OosnnsoO",
        "OoMTTMoO",
        "OOoMMoOO",
    ]
    return dibujo(filas, C)


def materiales(variante):
    ropa = ROPA[variante]
    piel = ruido(PIEL[1:5], 11, grano=0.5, base=0.55)
    camisa = tela(ropa["camisa"], 21 + variante, pliegue=3)
    calzon = tela(ropa["calzon"], 31 + variante, pliegue=4, grano=0.1)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara(variante)(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            # la sien hundida y la oreja
            if 3 <= y <= 4 and d in (3, 4):
                return PIEL[1] if d == 3 else PIEL[2]
            if y >= 6 and d <= 2:
                return PIEL[1]
        if cara == "arriba" and (x + y) % 5 == 0:
            return PIEL[2]
        return c

    def torso(cara, x, y, w, h):
        c = camisa(cara, x, y, w, h)
        if cara == "frente":
            # el cuello abierto: se le ve el pecho hundido y las costillas
            if y <= 3 and abs(x - 3.5) <= 1.5 - y * 0.4:
                return PIEL[2] if (y % 2 == 0) else PIEL[1]
            # el cordon de la cintura
            if y == 8:
                return oscuro(ropa["calzon"][1], 0.1) if x not in (5,) else HUESO
            # un desgarron con sangre vieja
            if variante == 0 and (x, y) in ((6, 5), (6, 6), (5, 6)):
                return SANGRE
        if cara == "espalda" and y == 8:
            return oscuro(ropa["calzon"][1], 0.1)
        return c

    def faldon(cara, x, y, w, h):
        # jirones: el borde de abajo roto
        corte = (3, 5, 4, 2, 5, 3, 4, 5, 2, 4)[(x * 3 + {"frente": 0, "espalda": 4, "der": 2, "izq": 7}.get(cara, 1)) % 10]
        if cara in ("arriba", "abajo") or y > corte:
            return None
        c = camisa(cara, x, y + 3, w, h + 3)
        return oscuro(c, 0.12 + 0.05 * y)

    def brazo(cara, x, y, w, h):
        # la manga hasta el codo
        return camisa(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y <= 1 and (x + y) % 2 == 0:
            return camisa(cara, x, y, w, h)           # el puno roto de la manga
        if y >= h - 1:
            return PIEL[1] if cara != "abajo" else PIEL[2]   # los dedos
        return piel(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return calzon(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        if y <= 2 - (x % 2):
            return calzon(cara, x, y, w, h)           # el calzon acaba roto bajo la rodilla
        if y >= h - 1:
            return PIEL[1]
        return piel(cara, x, y, w, h)

    def capucha(cara, x, y, w, h):
        t = tela(CAPUCHA, 41, pliegue=4)(cara, x, y, w, h)
        if cara == "frente":
            # la abertura de la cara
            if 1 <= x <= w - 2 and 2 <= y <= h - 1:
                return None
            if y == 1 and 2 <= x <= w - 3:
                return None
        if cara == "abajo":
            return None
        if cara in ("der", "izq") and y >= h - 2 and delante(cara, x, w) <= 1:
            return None
        return t

    def ala(cara, x, y, w, h):
        v = madera(PAJA, 51, "x", grano=0.5)(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            cx, cy = x - (w - 1) / 2.0, y - (h - 1) / 2.0
            r2 = cx * cx + cy * cy
            if r2 > 7.4 ** 2:
                return None                            # el ala es redonda
            if r2 > 6.2 ** 2 and (x * 7 + y * 3) % 5 == 0:
                return None                            # y esta deshilachada
            if cara == "abajo":
                return oscuro(v, 0.3)
            return oscuro(v, 0.12) if int(r2 ** 0.5) % 2 == 0 else v
        return oscuro(v, 0.2)

    def venda(cara, x, y, w, h):
        t = ruido(TRAPO, 61, grano=0.5)(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente":
            # tapa la frente y el ojo de su izquierda (a la derecha de quien mira)
            if y <= 1 or (x >= 4 and y <= 3 - (0 if x >= 5 else 1)):
                return SANGRE if (x, y) == (6, 3) else t
            return None
        if y <= 1 or (cara == "izq" and y <= 2 and delante(cara, x, w) <= 3):
            return t
        if cara == "espalda" and y <= 2 and x in (3, 4):
            return oscuro(t, 0.15)                     # el nudo
        return None

    C = {"H": HIERRO[4], "h": HIERRO[3], "d": HIERRO[2], "o": HIERRO[1], "x": "#3b2418"}
    hoja = dibujo([
        # (como se ve desde el costado izquierdo: el frente a la izquierda) el filo por dentro de la curva
        "...hHHHh..",
        "..hHdddHh.",
        ".hHd...dHh",
        ".Hd.....dH",
        "hHd......d",
        "hHd.......",
        ".Hd.......",
        ".hHd......",
        "..hH.....x",
    ], C)

    mats = {
        "cabeza": sombra(cabeza, abajo=0.16),
        "torso": sombra(torso),
        "faldon": faldon,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.18), "antebrazo_i": sombra(antebrazo, abajo=0.18),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.25), "pierna_i": sombra(pierna, abajo=0.25),
        "capucha": capucha, "capucha_pico": sombra(tela(CAPUCHA, 42, pliegue=3), abajo=0.3),
        "ala": ala, "copa": sombra(madera(PAJA, 52, "x", grano=0.5), abajo=0.2),
        "venda": venda,
        "mango": madera(PALO, 71, "z"),
        "hierro": metal(HIERRO[1:5], 72),
        "hoja_hoz": por_cara(liso(None), izq=hoja, der=espejado(hoja)),
        "estopa": ruido(["#1a1210", "#2a1c14", "#3c2818", "#523620"], 73, grano=0.7),
        "llama": por_cara(ruido(["#c8501a", "#e8781e", "#f8a030", "#ffd060"], 74, grano=0.9, base=0.6),
                          arriba=liso("#ffe890")),
        "llama_punta": liso("#ffd060"),
    }
    return mats


def brillo(variante):
    """Lo que brilla (se dibuja encima sin sombrear): la llama de la antorcha."""
    return {
        "llama": por_cara(ruido(["#e8781e", "#f8a030", "#ffd060", "#fff0a0"], 74, grano=0.9, base=0.6), arriba=liso("#fff4b0")),
        "llama_punta": liso("#ffe080"),
    }


# ---------------------------------------------------------------- las animaciones
# Los brazos de casi todo van con objetivos (ik.py): ("h", x, y, z) = donde va el puno respecto a su hombro
# (x hacia su izquierda, y arriba, z delante; el brazo mide 9) y _arma = hacia donde apunta lo que lleva.

# de pie, encorvado: el peso en una pierna, la cabeza colgando
BASE = dict(raiz=raiz(-0.4), torso=(13, 0, 2), cabeza=(14, 0, -5),
            muslo_d=(-6, 0, 3), pierna_d=(10, 0, 0), muslo_i=(2, 0, -3), pierna_i=(6, 0, 0),
            brazo_i=(-4, 0, -5), antebrazo_i=(-14, 0, 0))
# como lleva cada cosa cuando no pasa nada (el brazo derecho)
PORTE = {
    "horca": dict(_mano_d=("h", -1.5, -8.6, 0.5), _arma=(-0.1, -0.44, -1)),        # arrastrando los dientes por detras
    "hoz": dict(_mano_d=("h", -0.5, -8.8, 1.0), _arma=(0, -1, 0.25)),             # colgando
    "antorcha": dict(_mano_d=("h", 1.0, -2.5, 6.5), _arma=(0.05, 1, 0.12)),       # en alto, delante
}
# en guardia (ha visto a alguien): mas agachado, un pie delante, el arma por delante
GUARDIA = dict(raiz=raiz(-1.2), torso=(20, 8, 0), cabeza=(-6, -8, 0),
               muslo_d=(8, 0, 5), pierna_d=(16, 0, 0), muslo_i=(-22, 0, -5), pierna_i=(24, 0, 0))
LISTO = {
    # la horca con las dos manos: la derecha atras, junto a la cadera; la izquierda delante, en el asta
    "horca": con(GUARDIA, _mano_d=("h", 0.5, -7.2, 0.5), _arma=(0.22, 0.06, 1), _agarra=-9),
    "hoz": con(GUARDIA, _mano_d=("h", 0.0, -3.5, 6.0), _arma=(0.1, 1, 0.45), brazo_i=(-30, 0, -14), antebrazo_i=(-50, 0, 0)),
    "antorcha": con(GUARDIA, _mano_d=("h", 1.5, -0.5, 7.5), _arma=(0, 1, 0.5), brazo_i=(-20, 0, -16), antebrazo_i=(-44, 0, 0)),
}
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")


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

    # ---- quieto: se mece, la cabeza cae y vuelve
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.3, con(BASE, torso=(15, 3, 3), cabeza=(18, 4, -7), raiz=raiz(-0.7), brazo_i=(-2, 0, -6))),
        (2.6, con(BASE, torso=(12, -3, 1), cabeza=(11, -5, -3), raiz=raiz(-0.3), brazo_i=(-6, 0, -4))),
        (4.0, BASE),
    ])
    # ---- andar: arrastra los pies (lo mueve el juego con lo que anda)
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-26, 20, 8, 22, 6, 0.0), (-4, 4, 6, 40, 0, 0.9), (20, -26, 22, 8, -6, 0.0), (4, -4, 40, 6, 0, 0.9), (-26, 20, 8, 22, 6, 0.0))):
        pasos.append((1.2 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (3, ty, -ty * 0.4), "cabeza": (0, -ty * 0.5, ty * 0.5), "raiz": raiz(up),
                                    "brazo_i": (md * 0.5, 0, 0)}))
    bucle("andar", 1.2, pasos)
    # ---- el brazo del arma, segun lo que lleve (se suma a lo demas; solo mueve ese brazo)
    for arma, p in PORTE.items():
        q = con(BASE, p)
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    # ---- en guardia, con cada arma
    for arma, p in LISTO.items():
        bucle(f"listo_{arma}", 1.6, [
            (0.0, p),
            (0.8, con(p, raiz=raiz(-1.8), torso=(p["torso"][0] + 3, p["torso"][1] - 3, 0), cabeza=(-4, -6, 2))),
            (1.6, p),
        ])
    # ---- de rodillas ante algo (la capilla, una tumba): se mece adelante y atras
    REZA = dict(raiz=raiz(-9.6, 1.0), muslo_d=(-8, 0, 4), pierna_d=(100, 0, 0), muslo_i=(-8, 0, -4), pierna_i=(100, 0, 0),
                torso=(26, 0, 0), cabeza=(30, 0, 0), faldon=(-6, 0, 0),
                brazo_d=(-46, 0, -16), antebrazo_d=(-74, 0, 0), mano_d=(60, 0, 0), brazo_i=(-46, 0, 16), antebrazo_i=(-74, 0, 0))
    bucle("rezar", 3.6, [
        (0.0, REZA),
        (1.8, con(REZA, torso=(38, 0, 0), cabeza=(36, 0, 0), raiz=raiz(-9.9, 0.4))),
        (3.6, REZA),
    ])
    # ---- tirado en el suelo, boca abajo (parece un muerto mas)
    TUMBADO = dict(raiz=raiz(2.2, 14.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 34, 0),
                   muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                   _mano_d=("h", -3.0, -0.6, 7.5), _arma=(-0.25, 0.0, 1), brazo_i=(-22, 0, -30), antebrazo_i=(-70, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (2.0, con(TUMBADO, cabeza=(-8, 38, 0))), (4.0, TUMBADO)])

    # ================================================================ acciones
    H, Z, T = LISTO["horca"], LISTO["hoz"], LISTO["antorcha"]

    # ---- la horca: estocada larga (se echa atras con el hombro, y a fondo con un paso)
    ATRAS = con(H, raiz=raiz(-1.6, 2.4), torso=(10, 42, 0), cabeza=(-4, -38, 0),
                muslo_d=(20, 0, 6), pierna_d=(22, 0, 0), muslo_i=(-30, 0, -4), pierna_i=(14, 0, 0),
                _mano_d=("h", 1.0, -6.0, -5.5), _arma=(0.06, 0.04, 1), _agarra=-10)
    FONDO = con(sin(H, "_agarra"), raiz=raiz(-2.6, -4.5), torso=(28, -32, 0), cabeza=(-22, 28, 0),
                muslo_d=(30, 0, 4), pierna_d=(10, 0, 0), muslo_i=(-48, 0, -4), pierna_i=(40, 0, 0),
                brazo_i=(38, 0, -24), antebrazo_i=(-34, 0, 0), _mano_d=("h", 3.5, -3.0, 7.6), _arma=(0, 0.03, 1))
    accion("horca_estocada", 1.6, [
        (0.00, H),
        (0.50, ATRAS),
        (0.70, con(ATRAS, raiz=raiz(-1.6, 2.7), torso=(8, 46, 0), cabeza=(-4, -42, 0), _mano_d=("h", 1.0, -6.0, -6.2))),
        (0.82, FONDO),
        (1.10, con(FONDO, raiz=raiz(-2.4, -4.2), torso=(26, -30, 0))),
        (1.60, H),
    ], golpes=(0.82,))
    # ---- la horca: barrido de lado a lado, a la altura de la cintura
    LADO_D = con(H, raiz=raiz(-1.4, 1.0), torso=(12, 52, 0), cabeza=(-2, -46, 0),
                 _mano_d=("h", -2.0, -6.5, -2.0), _arma=(-1, 0.0, 0.25), _agarra=-9)
    LADO_I = con(H, raiz=raiz(-2.2, -2.0), torso=(20, -50, 0), cabeza=(-8, 42, 0), muslo_i=(-34, 0, -6), pierna_i=(30, 0, 0),
                 _mano_d=("h", 4.5, -6.0, 5.0), _arma=(1, -0.05, 0.3), _agarra=-9)
    accion("horca_barrido", 1.8, [
        (0.00, H),
        (0.55, LADO_D),
        (0.80, con(LADO_D, torso=(12, 58, 0), cabeza=(-2, -50, 0))),
        (0.90, con(H, raiz=raiz(-2.0, -1.0), torso=(18, 0, 0), cabeza=(-6, 0, 0),
                   _mano_d=("h", 2.0, -6.5, 4.5), _arma=(0.05, -0.02, 1), _agarra=-9)),
        (1.00, LADO_I),
        (1.30, con(LADO_I, torso=(18, -54, 0))),
        (1.80, H),
    ], golpes=(0.90,))
    # ---- la hoz: un tajo de arriba abajo, cruzado
    Z_ALTO = con(Z, raiz=raiz(-0.8, 0.8), torso=(4, 30, 0), cabeza=(-8, -26, 0), brazo_i=(-36, 0, -22), antebrazo_i=(-40, 0, 0),
                 _mano_d=("h", -2.5, 7.0, -3.0), _arma=(-0.2, 0.9, -0.5))
    Z_BAJO = con(Z, raiz=raiz(-2.4, -2.2), torso=(32, -34, 0), cabeza=(-20, 26, 0), muslo_i=(-36, 0, -4), pierna_i=(34, 0, 0),
                 brazo_i=(10, 0, -26), antebrazo_i=(-30, 0, 0), _mano_d=("h", 6.5, -5.5, 4.5), _arma=(0.7, -0.9, 0.4))
    accion("hoz_tajo", 1.15, [
        (0.00, Z),
        (0.32, Z_ALTO),
        (0.44, con(Z_ALTO, torso=(2, 34, 0), cabeza=(-8, -30, 0))),
        (0.50, con(Z, raiz=raiz(-1.8, -1.4), torso=(22, -6, 0), cabeza=(-14, 4, 0), brazo_i=(-10, 0, -24), antebrazo_i=(-36, 0, 0),
                   _mano_d=("h", 2.5, 0.0, 8.4), _arma=(0.2, 0.1, 1))),
        (0.58, Z_BAJO),
        (0.78, con(Z_BAJO, torso=(30, -38, 0))),
        (1.15, Z),
    ], golpes=(0.50,))
    # ---- la hoz: dos tajos seguidos, de ida y de vuelta
    Z_FUERA = con(Z, raiz=raiz(-1.0, 0.6), torso=(10, 40, 0), cabeza=(-6, -34, 0),
                  _mano_d=("h", -6.0, 1.0, 3.0), _arma=(-1, 0.3, 0.1))
    Z_DENTRO = con(Z, raiz=raiz(-2.0, -2.0), torso=(20, -44, 0), cabeza=(-12, 36, 0), muslo_i=(-32, 0, -4), pierna_i=(30, 0, 0),
                   brazo_i=(0, 0, -30), antebrazo_i=(-40, 0, 0), _mano_d=("h", 7.0, -1.0, 4.0), _arma=(1, 0.1, 0.2))
    Z_MEDIO = con(Z, raiz=raiz(-1.6, -0.8), torso=(16, 0, 0), cabeza=(-10, 0, 0), _mano_d=("h", 1.5, -0.5, 8.4), _arma=(0, 0.2, 1))
    accion("hoz_doble", 1.7, [
        (0.00, Z),
        (0.30, Z_FUERA),
        (0.42, con(Z_FUERA, torso=(10, 44, 0), cabeza=(-6, -38, 0))),
        (0.48, Z_MEDIO),
        (0.56, Z_DENTRO),
        (0.80, con(Z_DENTRO, torso=(20, -50, 0), cabeza=(-12, 40, 0))),
        (0.88, con(Z_MEDIO, raiz=raiz(-2.0, -3.0), muslo_i=(-40, 0, -4), pierna_i=(36, 0, 0))),
        (0.96, con(Z_FUERA, raiz=raiz(-2.2, -3.8), torso=(18, 40, 0), muslo_i=(-40, 0, -4), pierna_i=(36, 0, 0))),
        (1.22, con(Z_FUERA, raiz=raiz(-2.0, -3.4), torso=(16, 44, 0))),
        (1.70, Z),
    ], golpes=(0.48, 0.88))
    # ---- la antorcha: la levanta por encima de la cabeza y la descarga
    T_ALTO = con(T, raiz=raiz(-0.2, 1.4), torso=(-14, 10, 0), cabeza=(-20, -8, 0),
                 muslo_d=(10, 0, 4), pierna_d=(8, 0, 0), muslo_i=(-14, 0, -4), pierna_i=(10, 0, 0),
                 brazo_i=(-150, 0, -16), antebrazo_i=(-50, 0, 0), _mano_d=("h", 1.5, 8.6, -1.0), _arma=(0, 0.75, -0.65))
    T_BAJO = con(T, raiz=raiz(-3.4, -2.6), torso=(44, -8, 0), cabeza=(-30, 6, 0),
                 muslo_d=(16, 0, 4), pierna_d=(30, 0, 0), muslo_i=(-50, 0, -4), pierna_i=(52, 0, 0),
                 brazo_i=(-40, 0, -20), antebrazo_i=(-30, 0, 0), _mano_d=("h", 3.0, -4.5, 7.2), _arma=(0, -0.5, 1))
    accion("antorcha_golpe", 1.5, [
        (0.00, T),
        (0.45, T_ALTO),
        (0.66, con(T_ALTO, raiz=raiz(0.0, 1.6), torso=(-18, 10, 0), cabeza=(-22, -8, 0))),
        (0.72, con(T, raiz=raiz(-1.6, -1.0), torso=(14, 0, 0), cabeza=(-16, 0, 0), brazo_i=(-90, 0, -18), antebrazo_i=(-40, 0, 0),
                   _mano_d=("h", 2.5, 3.5, 7.8), _arma=(0, 0.6, 1))),
        (0.78, T_BAJO),
        (1.05, con(T_BAJO, torso=(42, -8, 0))),
        (1.50, T),
    ], golpes=(0.74,))
    # ---- la antorcha: la pasa por delante de lado a lado (prende a quien pille)
    T_FUERA = con(T, raiz=raiz(-1.2, 0.8), torso=(8, 48, 0), cabeza=(-4, -40, 0), _mano_d=("h", -6.5, 0.0, 2.5), _arma=(-1, 0.25, 0.15))
    T_DENTRO = con(T, raiz=raiz(-2.0, -2.0), torso=(18, -50, 0), cabeza=(-8, 40, 0), muslo_i=(-34, 0, -4), pierna_i=(30, 0, 0),
                   brazo_i=(-10, 0, -36), antebrazo_i=(-40, 0, 0), _mano_d=("h", 7.0, -1.5, 4.0), _arma=(1, 0.2, 0.3))
    accion("antorcha_barrido", 1.6, [
        (0.00, T),
        (0.42, T_FUERA),
        (0.62, con(T_FUERA, torso=(8, 54, 0), cabeza=(-4, -44, 0))),
        (0.70, con(T, raiz=raiz(-1.6, -0.8), torso=(14, 0, 0), cabeza=(-8, 0, 0), _mano_d=("h", 1.5, -0.5, 8.4), _arma=(0, 0.3, 1))),
        (0.78, T_DENTRO),
        (1.05, con(T_DENTRO, torso=(16, -56, 0))),
        (1.60, T),
    ], golpes=(0.70,))
    # ---- aturdido: se tambalea hacia atras, los brazos caidos
    AT = dict(raiz=raiz(-1.6, 2.0), torso=(-14, 0, 8), cabeza=(-24, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(26, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(20, 0, 0), brazo_d=(18, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(20, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(6, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-0.8, 0.0))),
        (0.18, AT),
        (0.60, con(AT, torso=(-8, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.00, con(AT, torso=(-12, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(8, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.0, 0.6))),
    ])
    # ---- morir: cae de rodillas y luego de bruces
    RODILLAS = dict(raiz=raiz(-9.6, 0.6), muslo_d=(-10, 0, 6), pierna_d=(102, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0),
                    torso=(8, 0, 4), cabeza=(-30, 0, 8), brazo_d=(6, 0, 24), antebrazo_d=(-10, 0, 0), mano_d=(70, 0, 0),
                    brazo_i=(8, 0, -24), antebrazo_i=(-12, 0, 0))
    CAIDO = con(TUMBADO, cabeza=(-4, 20, 0), brazo_i=(-30, 0, -40), _mano_d=("h", -6.0, -0.6, 4.0), _arma=(-0.8, 0.0, 1))
    accion("morir", 1.7, [
        (0.00, con(BASE, torso=(-10, 0, 6), cabeza=(-26, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0),
                   brazo_i=(10, 0, -30))),
        (0.45, RODILLAS),
        (0.80, con(RODILLAS, raiz=raiz(-9.8, 0.2), torso=(24, 0, 2), cabeza=(10, 0, 6), brazo_d=(-10, 0, 20), brazo_i=(-8, 0, -20))),
        (1.20, CAIDO),
        (1.70, CAIDO),
    ])
    # ---- levantarse de rezar (se vuelve despacio) y del suelo
    DE_PIE = con(GUARDIA, brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0), brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    MEDIO = dict(raiz=raiz(-5.0, 0.4), muslo_d=(-60, 0, 6), pierna_d=(84, 0, 0), muslo_i=(0, 0, -6), pierna_i=(70, 0, 0),
                 torso=(30, 10, 0), cabeza=(-14, 10, 0), brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0),
                 brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    accion("levantarse", 1.3, [
        (0.00, REZA),
        (0.35, con(REZA, torso=(10, 30, 0), cabeza=(-10, 40, 0), brazo_d=(-10, 0, 10), antebrazo_d=(-40, 0, 0), mano_d=(40, 0, 0),
                   brazo_i=(-10, 0, -10), antebrazo_i=(-30, 0, 0))),
        (0.80, MEDIO),
        (1.30, DE_PIE),
    ])
    accion("alzarse", 1.9, [
        (0.00, TUMBADO),
        (0.40, con(TUMBADO, cabeza=(-30, 10, 0), brazo_i=(-70, 0, -30), antebrazo_i=(-90, 0, 0), _mano_d=("h", -4.0, -0.8, 4.0), _arma=(-0.4, 0.0, 1))),
        (0.90, dict(raiz=raiz(-5.5, 6.0, rot=(50, 0, 0)), torso=(-6, 0, 0), cabeza=(-40, 0, 0),
                    muslo_d=(-40, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(96, 0, 0),
                    brazo_d=(-96, 0, 14), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(-92, 0, -14), antebrazo_i=(-20, 0, 0))),
        (1.35, con(MEDIO, raiz=raiz(-5.6, 0.0), torso=(34, 0, 0), cabeza=(-10, 0, 0))),
        (1.90, DE_PIE),
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
    """Las animaciones que el juego pone debajo de esta (para verla como se vera)."""
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_horca"]]
    if nombre == "quieto":
        return [anims["porte_horca"]]
    if nombre in ("rezar", "tumbado"):
        return []
    return []
