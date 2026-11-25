"""
El Capitan de Brumavela (parte 3c): Edrik Maroll, capitan de La Garza Gris. Desarmo su barco, subio el corazon al faro
para que sus ahogados supieran volver... y volvieron. Es el primer jefe con barra: mas alto que un hombre, la casaca de
marino hecha jirones, el tricornio, la barba de algas, un ojo que luce y el otro vacio. En la derecha, el sable; en la
izquierda, el farol de animas con el que llama a los suyos y prende el suelo.
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (Textura, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,
                   sombra, tela)

NOMBRE = "capitan"
VARIANTES = ("capitan",)
OCULTAS = {0: []}
#: se dibuja mas grande que un hombre (lo mismo que le da su dibujante en el juego)
ESCALA = 1.22
CENTRO_Y = 1.25

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), brazo=(4, 12, 4), pierna=(4, 12, 4))
    # la casaca: encima del torso, abierta por delante
    r.caja("torso", -4, -12, -2.5, 8, 12, 5, "casaca", grow=g(0.45))
    # sus faldones: el de atras cuelga de la cadera; los de los lados y los de delante van con cada muslo
    r.pieza("faldon_tras", "cadera", (0, 0, 0), boxes=[Box(-4.5, -0.5, 1.6, 9, 10, 1, "faldon_tras")])
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[
            Box(-3.0 if s < 0 else 2.0, -0.5, -2.5, 1, 8, 5, f"faldon_lado_{n}", uv=""),
            Box(-2.4 if s < 0 else -0.6, -0.5, -3.0, 3, 7, 1, f"faldon_frente_{n}"),
        ])
        # las charreteras
        r.pieza(f"charretera_{n}", f"brazo_{n}", (0, -2, 0), boxes=[Box(-2.5, -1, -2.5, 5, 2, 5, "charretera", grow=g(0.15), uv="charretera")])
    # el tricornio: la copa, el ala y sus tres vueltas (las de los lados se cierran hacia delante, en pico)
    r.pieza("sombrero", "cabeza", (0, -7.4, 0), rot=(-4, 0, 0), boxes=[
        Box(-6.5, -1, -6.5, 13, 1, 13, "ala"),
        Box(-4, -5, -4, 8, 4, 8, "copa", grow=g(0.3)),
        Box(-5, -4, 5.5, 10, 3, 1, "vuelta_tras"),
    ])
    r.pieza("vuelta_d", "sombrero", (-6.0, -1, 1.0), rot=(0, -24, 8), boxes=[Box(-0.5, -3, -8, 1, 3, 13, "vuelta", uv="vuelta")])
    r.pieza("vuelta_i", "sombrero", (6.0, -1, 1.0), rot=(0, 24, -8), boxes=[Box(-0.5, -3, -8, 1, 3, 13, "vuelta", uv="vuelta")])
    # la barba de algas
    r.pieza("barba", "cabeza", (0, 0, 0), boxes=[Box(-3, -2, -4.7, 6, 7, 1, "barba")])
    # ---- el sable (mano derecha): la hoja es un plano de canto
    r.pieza("sable", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2.5, 1, 1, 6, "puno"),
        Box(-2, -1.5, -4, 4, 3, 1, "guarda"),
        Box(0, -4, -27, 0, 7, 23, "hoja"),
    ])
    # ---- el farol de animas (mano izquierda): cuelga a lo largo de -Z de la mano (en las poses, hacia abajo)
    r.pieza("farol", "mano_i", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2, 1, 1, 3, "asa"),
        Box(-1.5, -1.5, -3, 3, 3, 1, "farol_tapa"),
        Box(-2, -2, -8, 4, 4, 5, "farol"),
        Box(-1, -1, -9, 2, 2, 1, "farol_pie"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas

PIEL = ["#1f2a2b", "#2b3a3a", "#394d4b", "#4a625e", "#5f7a73", "#789489"]
MORADO = "#33283c"
OJO, OJO_B = "#e8fffb", "#9af0e4"
BOCA = "#0b1011"
AZUL = ["#0b111c", "#111a2b", "#18243b", "#20304d", "#2a3e61"]
ORO = ["#3d3010", "#5c491b", "#7f6728", "#a58736", "#c9a94b"]
CAMISA = ["#4b4f48", "#61655c", "#797d71", "#929686"]
FAJA = ["#350f13", "#4a151a", "#611c22", "#7a252c"]
CALZON = ["#121416", "#1a1d20", "#23272b", "#2d3237"]
BOTA = ["#0f0d0b", "#181512", "#221d19", "#2e2722"]
NEGRO = ["#07090d", "#0d1118", "#141a24", "#1c2431"]
ALGA = ["#14261b", "#1d3826", "#294d32", "#386540", "#4b7d50"]
CANA = "#8d9b8a"
HIERRO = ["#181b1d", "#2a2f32", "#40474b", "#5b6468", "#7c878c", "#a3aeb2"]
OXIDO = ["#4a2a18", "#6a3c20"]
PALO = ["#241b13", "#33271b", "#443424", "#56422e"]
LLAMA = ["#2c8f8a", "#49c7bb", "#9af0e4", "#e8fffb"]
PERCEBE = "#a9a895"

#: la hoja del sable (vista desde su costado izquierdo: la punta a la izquierda), ancha y curva
HOJA = [
    "..hHHh.................",
    ".hHddHHHhh.............",
    "hHdddddddHHHHhhhh......",
    ".hHddodddddddddddHHHhhh",
    "..hHHddddoodddddxddddHh",
    "....hhHHHHdddddddddddHh",
    ".........hhhhhHHHHHHHh.",
]


def _cara():
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "E": OJO, "e": OJO_B, "M": BOCA, "m": MORADO, "n": PIEL[1],
         "K": "#06090a", "k": "#10181a", "c": "#5a2a2a", "b": ALGA[1], "B": ALGA[2], "p": PERCEBE}
    return dibujo([
        "OossssoO",
        "osSSSSso",
        "oSSssScp",
        "OmmoocmO",
        "oEesKcKo",
        "OosnnkoO",
        "bBMMMMBb",
        "bbBMMBbb",
    ], C)


def materiales(variante):
    piel = ruido(PIEL[1:5], 211, grano=0.5, base=0.5)
    azul = tela(AZUL, 221, pliegue=4, grano=0.12)
    camisa = tela(CAMISA, 222, pliegue=3, grano=0.16)
    calzon = tela(CALZON, 231, pliegue=4, grano=0.1)
    cuero = ruido(BOTA, 232, grano=0.4)
    negro = ruido(NEGRO, 241, grano=0.35)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara()(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 3 <= y <= 4 and d in (3, 4):
                return PIEL[1]
            if y >= 6 and d <= 3:
                return ALGA[1 + (x + y) % 2]                 # la barba le sube por la quijada
            if (x * 5 + y * 3) % 19 == 0:
                return PERCEBE
        if cara == "espalda" and y >= 5:
            return ALGA[(x + y) % 2]                          # greñas mojadas por la nuca
        return c

    def torso(cara, x, y, w, h):
        # debajo de la casaca: la camisa, la faja y el cinto
        c = camisa(cara, x, y, w, h)
        if cara in ("frente", "espalda", "der", "izq"):
            if y in (8, 9):
                return tela(FAJA, 223, pliegue=3)(cara, x, y, w, h)
            if y == 10:
                return "#16110d" if not (cara == "frente" and x in (3, 4)) else ORO[3]
        if cara == "frente":
            if y <= 2 and abs(x - 3.5) <= 1.5 - y * 0.5:
                return PIEL[2]
            if (x * 7 + y * 5) % 17 == 0:
                return ALGA[1]
        return c

    def casaca(cara, x, y, w, h):
        c = azul(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if cara == "frente":
            # abierta: se ve la camisa por el medio; solapas y botones de oro viejo
            ancho = 1 if y <= 1 else 2
            if abs(x - 3.5) < ancho:
                return None
            if abs(x - 3.5) < ancho + 1:
                return ORO[1] if y % 3 == 1 else AZUL[4]      # el canto de la solapa, con su galon
            if x in (1, 6) and y % 3 == 1 and y < 10:
                return ORO[3]
            if y >= h - 1 and (x * 3) % 4 == 0:
                return None
        if cara == "espalda":
            if x in (3, 4) and y >= 6:
                return AZUL[0]                                # la abertura de atras
            if y == 5 and x in (2, 5):
                return ORO[2]
        if cara in ("der", "izq") and y >= h - 1 and x % 2 == 0:
            return None
        # verdin y sal
        if (x * 11 + y * 7 + {"frente": 0, "espalda": 5, "der": 9, "izq": 3}.get(cara, 1)) % 29 == 0:
            return ALGA[1]
        return c

    def faldon(jiron, semilla):
        def f(cara, x, y, w, h):
            corte = jiron[(x + semilla) % len(jiron)]
            if cara in ("arriba", "abajo") or y > corte:
                return None
            c = azul(cara, x, y + 3, w, h + 3)
            if (x * 5 + y * 7 + semilla) % 23 == 0:
                return ALGA[1]
            return oscuro(c, 0.10 + 0.035 * y)
        return f

    def faldon_tras(cara, x, y, w, h):
        if cara not in ("espalda", "frente", "der", "izq"):
            return None
        if cara == "espalda" and x == 4 and y >= 2:
            return None                                       # la raja del faldon
        return faldon((9, 7, 9, 8, 6, 9, 8, 9, 7), 0)(cara, x, y, w, h)

    def brazo(cara, x, y, w, h):
        return azul(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 1:
            return PIEL[1] if cara != "abajo" else PIEL[2]    # la mano
        if y in (2, 3):
            # la bocamanga, vuelta y con su galon
            return ORO[2] if y == 2 else AZUL[4]
        if y == 4 and x % 2 == 0:
            return CAMISA[2]                                  # el puno de la camisa, roto
        if y >= 4:
            return piel(cara, x, y, w, h)
        return azul(cara, x, y, w, h)

    def charretera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            cx, cy = x - 2, y - 2
            return ORO[3] if abs(cx) + abs(cy) <= 1 else ORO[2 if (x + y) % 2 else 1]
        # los flecos
        return ORO[2 + (x % 2)] if y == 0 else (ORO[1] if x % 2 == 0 else None)

    def muslo(cara, x, y, w, h):
        return calzon(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        # la bota hasta la rodilla, con la vuelta arriba
        c = cuero(cara, x, y, w, h)
        if y <= 1:
            return claro(c, 0.10) if y == 0 else oscuro(c, 0.15)
        if y >= h - 1 and cara != "abajo":
            return oscuro(c, 0.25)
        return c

    def ala(cara, x, y, w, h):
        if cara not in ("arriba", "abajo"):
            return ORO[1] if (x % 3) else ORO[2]              # el canto, con el galon gastado
        cx, cy = x - (w - 1) / 2.0, y - (h - 1) / 2.0
        # tres picos: uno delante y dos detras (y = 0 es la parte de atras)
        r = 6.6 - 1.7 * max(0.0, 1.0 - abs(cx) / 4.5) * (1.0 if cy > 0 else 0.0)
        if cx * cx + cy * cy > r * r + 6 and not (abs(cx) < 2.2 and cy < 0):
            return None
        c = negro(cara, x, y, w, h)
        return oscuro(c, 0.3) if cara == "abajo" else c

    def copa(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = negro(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return ORO[2] if x % 4 else ORO[3]                # la cinta
        return c

    def vuelta(cara, x, y, w, h):
        c = negro(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            # mas baja hacia el pico de delante; el canto de arriba, con galon
            alto = 0 if d >= 4 else (1 if d >= 2 else 2)
            if y < alto:
                return None
            if y == alto:
                return ORO[2] if (x % 3) else ORO[1]
            return c
        if cara in ("arriba",):
            return ORO[1]
        return c

    def vuelta_tras(cara, x, y, w, h):
        c = negro(cara, x, y, w, h)
        if cara in ("frente", "espalda"):
            alto = 0 if 2 <= x <= w - 3 else 1
            if y < alto:
                return None
            if y == alto:
                return ORO[2] if (x % 3) else ORO[1]
        return c

    def barba(cara, x, y, w, h):
        if cara != "frente":
            return None
        # mechones de alga, mas largos en el medio; alguna cana y algun percebe
        largo = (3, 5, 6, 7, 5, 4)[x % 6]
        if y < 1 or y > largo:
            return None
        if (x * 3 + y * 5) % 11 == 0:
            return CANA
        # mechones a lo largo: cada columna, su tono (mas oscuro hacia la punta)
        return ALGA[max(0, (1, 3, 2, 3, 1, 2)[x % 6] - (1 if y >= largo - 1 else 0) - (1 if (y + x) % 4 == 0 else 0))]

    S = {"H": HIERRO[5], "h": HIERRO[4], "d": HIERRO[3], "o": OXIDO[1], "x": OXIDO[0]}
    hoja = dibujo(HOJA, S)

    def farol(cara, x, y, w, h):
        # la jaula de hierro y, dentro, la llama de animas (los cuatro costados; las tapas, de hierro)
        if cara in ("frente", "espalda"):
            return HIERRO[2] if (x in (0, w - 1) or y in (0, h - 1)) else HIERRO[1]
        if x in (0, w - 1) or y in (0, h - 1):
            return HIERRO[2 + ((x + y) % 2)]
        cx, cy = abs(x - (w - 1) / 2.0), abs(y - (h - 1) / 2.0)
        return LLAMA[3] if cx + cy < 1.2 else LLAMA[2] if cx + cy < 2.0 else LLAMA[1]

    return {
        "cabeza": sombra(cabeza, abajo=0.14),
        "torso": sombra(torso),
        "casaca": sombra(casaca, abajo=0.18),
        "faldon_tras": faldon_tras,
        "faldon_lado_d": faldon((7, 6, 7, 5, 7), 2), "faldon_lado_i": faldon((6, 7, 5, 7, 6), 4),
        "faldon_frente_d": faldon((6, 5, 6), 1), "faldon_frente_i": faldon((5, 6, 6), 3),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.16), "antebrazo_i": sombra(antebrazo, abajo=0.16),
        "charretera": charretera,
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
        "ala": ala, "copa": sombra(copa, abajo=0.1), "vuelta": vuelta, "vuelta_tras": vuelta_tras,
        "barba": barba,
        "puno": madera(PALO, 251, "z"), "guarda": metal(ORO[0:5], 252),
        "hoja": por_cara(liso(None), izq=hoja, der=espejado(hoja)),
        "asa": metal(HIERRO[1:5], 253), "farol_tapa": metal(HIERRO[0:4], 254), "farol": farol,
        "farol_pie": metal(HIERRO[0:4], 255),
    }


def brillo(variante):
    """Lo que brilla: el ojo que le queda y la llama del farol."""
    C = {"E": OJO, "e": OJO_B}
    ojo = dibujo([
        "........",
        "........",
        "........",
        "........",
        ".Ee.....",
    ], C)

    def llama(cara, x, y, w, h):
        if cara in ("frente", "espalda") or x in (0, w - 1) or y in (0, h - 1):
            return None
        cx, cy = abs(x - (w - 1) / 2.0), abs(y - (h - 1) / 2.0)
        return LLAMA[3] if cx + cy < 1.2 else LLAMA[2] if cx + cy < 2.0 else LLAMA[1]

    return {"cabeza": por_cara(liso(None), frente=ojo), "farol": llama}


# ---------------------------------------------------------------- las animaciones

ABAJO = (0.0, -1.0, 0.0)
# de pie, esperando ante la puerta del faro: el sable al hombro y el farol bajo, a un lado
BASE = dict(raiz=raiz(-0.2), torso=(-3, 0, 0), cabeza=(8, 0, 0),
            muslo_d=(2, 0, 5), pierna_d=(3, 0, 0), muslo_i=(-2, 0, -5), pierna_i=(4, 0, 0),
            _mano_d=("h", 0.6, -5.2, 4.2), _arma=(-0.12, 0.92, -0.42),
            _mano_i=("h", 1.6, -7.6, 2.4), _arma_i=ABAJO)
# en guardia: de perfil, el sable por delante y el farol en alto, detras
LISTO = dict(raiz=raiz(-1.0), torso=(6, -20, 0), cabeza=(-2, 18, 0),
             muslo_d=(-22, 0, 4), pierna_d=(20, 0, 0), muslo_i=(12, 0, -6), pierna_i=(12, 0, 0),
             _mano_d=("h", 0.0, -3.6, 7.4), _arma=(0.14, 0.14, 1),
             _mano_i=("h", 3.4, 2.4, 0.6), _arma_i=ABAJO)


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

    bucle("quieto", 6.0, [
        (0.0, BASE),
        (2.0, con(BASE, raiz=raiz(-0.5), torso=(-4, 3, 0), cabeza=(6, -8, 0), _mano_i=("h", 1.8, -7.3, 2.8))),
        (4.0, con(BASE, raiz=raiz(-0.1), torso=(-2, -3, 0), cabeza=(10, 9, 0), _mano_i=("h", 1.4, -7.8, 2.0))),
        (6.0, BASE),
    ])
    # andar: pasos largos y pesados; las botas se oyen
    pasos = []
    for k, (md, mi, pd, pi_, tz, up) in enumerate((
            (-24, 18, 6, 14, 3, 0.0), (-2, 2, 4, 34, 0, 0.7), (18, -24, 14, 6, -3, 0.0), (2, -2, 34, 4, 0, 0.7), (-24, 18, 6, 14, 3, 0.0))):
        pasos.append((1.3 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (3, tz * 1.2, tz * 0.5), "cabeza": (0, -tz * 1.0, -tz * 0.4), "raiz": raiz(up)}))
    bucle("andar", 1.3, pasos)
    bucle("listo", 2.6, [
        (0.0, LISTO),
        (1.3, con(LISTO, raiz=raiz(-1.6), torso=(9, -22, 0), cabeza=(-4, 20, 0), _mano_d=("h", 0.2, -3.9, 7.0), _mano_i=("h", 3.6, 2.0, 0.8))),
        (2.6, LISTO),
    ])

    # ---- el tajo: lo levanta por encima de la cabeza, lo sostiene un instante y lo descarga (pesado: se ve venir)
    ALTO = con(LISTO, raiz=raiz(-0.4, 1.6), torso=(-12, 22, 0), cabeza=(-8, -18, 0),
               muslo_d=(-10, 0, 4), pierna_d=(14, 0, 0), muslo_i=(16, 0, -6), pierna_i=(10, 0, 0),
               _mano_d=("h", -1.6, 8.0, -2.2), _arma=(-0.15, 0.75, -0.65), _mano_i=("h", 2.4, -5.0, 5.0))
    MEDIO = con(LISTO, raiz=raiz(-1.8, -1.6), torso=(16, -12, 0), cabeza=(-12, 10, 0),
                _mano_d=("h", 1.0, 2.0, 8.4), _arma=(0.1, 0.35, 1), _mano_i=("h", 3.6, -3.0, 0.0))
    BAJO = con(LISTO, raiz=raiz(-2.8, -3.2), torso=(34, -32, 0), cabeza=(-22, 26, 0),
               muslo_d=(-46, 0, 4), pierna_d=(44, 0, 0), muslo_i=(22, 0, -6), pierna_i=(10, 0, 0),
               _mano_d=("h", 4.4, -6.0, 5.6), _arma=(0.45, -0.85, 0.6), _mano_i=("h", 4.0, -4.0, -3.0))
    accion("tajo", 1.9, [
        (0.00, LISTO),
        (0.55, ALTO),
        (0.88, con(ALTO, raiz=raiz(-0.2, 2.0), torso=(-16, 26, 0), cabeza=(-10, -22, 0), _mano_d=("h", -1.6, 8.3, -2.8))),
        (0.95, MEDIO),
        (1.03, BAJO),
        (1.40, con(BAJO, torso=(32, -36, 0))),
        (1.90, LISTO),
    ], golpes=(0.95,))
    # ---- dos tajos anchos avanzando: de fuera adentro y de vuelta
    FUERA = con(LISTO, raiz=raiz(-1.0, 0.8), torso=(6, 40, 0), cabeza=(-4, -34, 0),
                muslo_d=(-14, 0, 4), pierna_d=(16, 0, 0), _mano_d=("h", -6.6, 0.2, 2.0), _arma=(-1, 0.15, 0.15),
                _mano_i=("h", 2.6, -4.0, 5.0))
    DENTRO = con(LISTO, raiz=raiz(-2.0, -2.4), torso=(14, -46, 0), cabeza=(-8, 40, 0),
                 muslo_d=(-40, 0, 4), pierna_d=(36, 0, 0), _mano_d=("h", 7.0, -1.0, 4.4), _arma=(1, 0.1, 0.3),
                 _mano_i=("h", 4.4, -2.0, -3.0))
    accion("doble", 2.4, [
        (0.00, LISTO),
        (0.50, FUERA),
        (0.72, con(FUERA, torso=(6, 44, 0), cabeza=(-4, -38, 0))),
        (0.80, con(MEDIO, raiz=raiz(-1.6, -1.2), _mano_d=("h", 0.5, -0.4, 8.6), _arma=(0.0, 0.1, 1))),
        (0.90, DENTRO),
        (1.26, con(DENTRO, torso=(14, -52, 0), cabeza=(-8, 44, 0))),
        (1.40, con(MEDIO, raiz=raiz(-2.0, -4.0), muslo_d=(-44, 0, 4), pierna_d=(40, 0, 0), _mano_d=("h", 0.5, -0.4, 8.6), _arma=(0.0, 0.1, 1))),
        (1.50, con(FUERA, raiz=raiz(-2.0, -5.0), torso=(12, 42, 0), muslo_d=(-44, 0, 4), pierna_d=(40, 0, 0))),
        (1.85, con(FUERA, raiz=raiz(-1.8, -4.8), torso=(10, 44, 0), muslo_d=(-40, 0, 4), pierna_d=(36, 0, 0))),
        (2.40, LISTO),
    ], golpes=(0.80, 1.40))
    # ---- la estocada: recoge el sable a la cadera, alumbra con el farol y se tira a fondo
    ATRAS = con(LISTO, raiz=raiz(-1.6, 2.2), torso=(4, 30, 0), cabeza=(-2, -26, 0),
                muslo_d=(-8, 0, 4), pierna_d=(22, 0, 0), muslo_i=(20, 0, -6), pierna_i=(20, 0, 0),
                _mano_d=("h", -1.0, -4.6, -4.6), _arma=(0.05, 0.05, 1), _mano_i=("h", 0.5, 1.2, 7.2))
    FONDO = con(LISTO, raiz=raiz(-3.0, -5.4), torso=(26, -42, 0), cabeza=(-20, 36, 0),
                muslo_d=(-58, 0, 4), pierna_d=(52, 0, 0), muslo_i=(32, 0, -4), pierna_i=(6, 0, 0),
                _mano_d=("h", 2.6, -1.4, 8.5), _arma=(0.0, 0.0, 1), _mano_i=("h", 5.0, -2.0, -5.0))
    accion("estocada", 1.8, [
        (0.00, LISTO),
        (0.50, ATRAS),
        (0.82, con(ATRAS, raiz=raiz(-1.6, 2.6), torso=(2, 34, 0), _mano_d=("h", -1.0, -4.6, -5.4))),
        (0.90, FONDO),
        (1.25, con(FONDO, torso=(24, -40, 0))),
        (1.80, LISTO),
    ], golpes=(0.90,))
    # ---- el farol: lo alza, y donde pisa cada uno el suelo se enciende (el golpe = sale la marca; la llama, despues)
    F_SUBE = con(LISTO, raiz=raiz(-0.6, 0.6), torso=(-4, 14, 0), cabeza=(-10, -10, 0),
                 muslo_d=(-6, 0, 5), pierna_d=(8, 0, 0), muslo_i=(4, 0, -6), pierna_i=(6, 0, 0),
                 _mano_d=("h", -2.0, -8.0, 1.0), _arma=(-0.3, -0.6, 0.75), _mano_i=("h", 2.0, 5.6, 4.6))
    F_ALTO = con(F_SUBE, raiz=raiz(0.5, 0.8), torso=(-10, 18, 0), cabeza=(-22, -14, 0), _mano_i=("h", 2.4, 8.3, 2.0))
    accion("farol", 2.3, [
        (0.00, LISTO),
        (0.50, F_SUBE),
        (0.90, con(F_SUBE, raiz=raiz(-1.2, 0.6), _mano_i=("h", 2.0, 4.8, 5.2))),
        (1.00, F_ALTO),
        (1.70, con(F_ALTO, raiz=raiz(0.2, 0.8), cabeza=(-18, -10, 0))),
        (2.30, LISTO),
    ], golpes=(1.00,))
    # ---- llamar a la tripulacion: se encoge, abre los brazos y los alza al cielo (el golpe = salen los ahogados)
    L_BAJO = con(LISTO, raiz=raiz(-2.6, 0.6), torso=(22, 0, 0), cabeza=(22, 0, 0),
                 muslo_d=(-30, 0, 8), pierna_d=(44, 0, 0), muslo_i=(-22, 0, -8), pierna_i=(40, 0, 0),
                 _mano_d=("h", -6.4, -4.4, 2.0), _arma=(-0.6, -0.75, 0.3), _mano_i=("h", 7.0, -3.4, 2.4))
    L_ALTO = con(LISTO, raiz=raiz(0.4, 1.0), torso=(-14, 0, 0), cabeza=(-26, 0, 0),
                 muslo_d=(-4, 0, 8), pierna_d=(4, 0, 0), muslo_i=(4, 0, -8), pierna_i=(4, 0, 0),
                 _mano_d=("h", -3.2, 8.0, 0.6), _arma=(-0.25, 1, -0.05), _mano_i=("h", 3.2, 8.2, 0.8))
    accion("llamar", 2.9, [
        (0.00, LISTO),
        (0.60, L_BAJO),
        (1.25, con(L_BAJO, raiz=raiz(-3.2, 0.6), torso=(26, 0, 0), cabeza=(26, 0, 0))),
        (1.40, L_ALTO),
        (2.20, con(L_ALTO, raiz=raiz(0.2, 1.0), torso=(-12, 0, 0), cabeza=(-22, 0, 0))),
        (2.90, LISTO),
    ], golpes=(1.40,))
    # ---- aturdido: le han parado el golpe o le han roto la guardia; el sable se le va y el farol se columpia
    AT = dict(raiz=raiz(-1.4, 2.6), torso=(-20, 10, 8), cabeza=(-16, -6, 14),
              muslo_d=(-12, 0, 8), pierna_d=(26, 0, 0), muslo_i=(16, 0, -8), pierna_i=(18, 0, 0),
              _mano_d=("h", -5.0, -6.6, -1.0), _arma=(-0.7, -0.6, 0.4), _mano_i=("h", 5.4, -4.0, 2.0), _arma_i=(0.25, -1, 0.1))
    accion("aturdido", 1.4, [
        (0.00, con(AT, raiz=raiz(-0.8, 0.6), torso=(4, 0, 0), cabeza=(0, 0, 4))),
        (0.22, AT),
        (0.65, con(AT, raiz=raiz(-1.8, 3.2), torso=(-14, -8, -8), cabeza=(-10, 8, -10), _arma_i=(-0.25, -1, -0.1))),
        (1.00, con(AT, raiz=raiz(-1.6, 2.8), torso=(-16, 6, 6), cabeza=(-14, -6, 10))),
        (1.40, con(LISTO, raiz=raiz(-1.2, 1.0))),
    ])
    # ---- morir: cae de rodillas, deja el farol en el suelo y se vence de bruces
    RODILLAS = dict(raiz=raiz(-9.6, 0.0), torso=(10, 0, 0), cabeza=(26, 0, 0),
                    muslo_d=(-12, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -6), pierna_i=(92, 0, 0),
                    _mano_d=("h", -3.6, -7.0, 4.0), _arma=(-0.3, -0.75, 0.6), _mano_i=("h", 2.6, -3.6, 6.0), _arma_i=ABAJO)
    CAIDO = dict(raiz=raiz(2.6, 15.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-14, 28, 0),
                 muslo_d=(4, 0, 10), pierna_d=(8, 0, 0), muslo_i=(-4, 0, -14), pierna_i=(26, 0, 0),
                 _mano_d=("h", -4.5, -1.2, 7.0), _arma=(-0.45, 0.0, 1), _mano_i=("h", 5.0, -1.2, 6.0), _arma_i=(0.5, 0.0, 0.85))
    accion("morir", 3.2, [
        (0.00, con(LISTO, raiz=raiz(-1.6, 1.4), torso=(-10, 0, 6), cabeza=(-14, 0, 8))),
        (0.50, con(LISTO, raiz=raiz(-3.0, 1.0), torso=(14, 0, 0), cabeza=(18, 0, 0), muslo_d=(-20, 0, 6), pierna_d=(44, 0, 0),
                   muslo_i=(-14, 0, -6), pierna_i=(40, 0, 0), _mano_d=("h", -3.0, -7.4, 3.0), _arma=(-0.3, -0.8, 0.5),
                   _mano_i=("h", 2.8, -6.0, 4.0))),
        (0.95, RODILLAS),
        (1.70, con(RODILLAS, torso=(16, 0, 0), cabeza=(32, 0, 0), _mano_i=("h", 2.6, -5.6, 6.6))),
        (2.45, CAIDO),
        (3.20, CAIDO),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    if nombre == "andar":
        return [anims["quieto"]]
    return []
