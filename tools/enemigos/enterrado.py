"""
Los Enterrados de las Minas de Hierro Negro (parte 7aq): los mineros del turno de noche, que estaban abajo cuando el cielo
se puso rojo y la montana tembló. Nadie subio. Siguen abajo, secos, grises del polvo de hierro que se les ha metido en la
piel; los ojos, blancos de polvo (estan ciegos: oyen). Tres:

  0 picador: gorro de cuero con su candil encendido en la frente (luce), camisa rota, chaleco de cuero, rodilleras y el
    pico a dos manos. Picotazo de arriba abajo (si lo paras con el escudo, el pico lo engancha y te rompe la guardia) y
    barrido. Espera picando la roca (REZA: de cara a la pared, el pico sube y baja: se le oye antes de verle).
  1 barrenero: gorra de lana, un panuelo sobre la boca, la bandolera de cartuchos y el morral; en la mano, un cartucho con
    la mecha encendida (luce). Lo lanza (revienta al poco de caer) y de cerca golpea con el. Espera agazapado (REZA).
  2 cargador: enorme (el dibujante lo agranda), el torso al aire con el arnes de cuero, el cuevano lleno de mineral a la
    espalda (con vetas rojas que lucen) y la almadena. Almadenazo (la onda tumba), barrido y embestida. Espera sentado
    (REZA) o enterrado en los escombros (TUMBADO, todos).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, humanoide
from pinta import (claro, dibujo, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "enterrado"
VARIANTES = ("picador", "barrenero", "cargador")
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), cabeza=(7, 8, 7), brazo=(3, 14, 3), pierna=(4, 12, 4), hombro=0.6)
    # ---- 0, el picador: el gorro con el candil, las rodilleras y el pico
    r.pieza("gorro", "cabeza", (0, 0, 0), boxes=[
        Box(-3.9, -8.7, -3.9, 7.8, 3.2, 7.8, "gorro"),
        Box(-3.9, -5.6, 3.2, 7.8, 0.6, 1.4, "ala"),
    ])
    r.pieza("candil", "cabeza", (0, 0, 0), boxes=[
        Box(-1.0, -8.2, -5.5, 2.0, 2.2, 1.6, "candil"),
        Box(-0.55, -9.6, -5.2, 1.1, 1.4, 1.0, "llama_candil"),
    ])
    for n in ("d", "i"):
        r.pieza(f"rodillera_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-2.3, -0.6, -2.7, 4.6, 3.2, 1.0, "rodillera")])
    r.pieza("pico", "mano_d", (0, 0, 0), boxes=[
        Box(-0.7, -0.7, -21.0, 1.4, 1.4, 24.0, "mango"),
        Box(-0.9, -8.6, -22.9, 1.8, 17.2, 2.4, "pico_hierro"),
        Box(-0.55, -10.8, -22.4, 1.1, 2.4, 1.4, "punta"),
        Box(-0.55, 8.4, -22.4, 1.1, 2.4, 1.4, "punta"),
    ])
    # ---- 1, el barrenero: la gorra, el panuelo, la bandolera, el morral y el cartucho con su mecha
    r.pieza("gorra", "cabeza", (0, 0, 0), boxes=[Box(-3.8, -8.8, -3.8, 7.6, 2.6, 7.6, "gorra")])
    r.pieza("panuelo", "cabeza", (0, 0, 0), boxes=[Box(-3.8, -4.4, -3.95, 7.6, 2.6, 7.6, "panuelo")])
    r.pieza("bandolera", "torso", (0, 0, 0), boxes=[
        Box(-4.35, -9.6, -2.85, 8.7, 1.8, 5.7, "bandolera"),
        Box(-3.2, -10.9, -3.3, 1.0, 3.0, 0.6, "cartucho_pecho"),
        Box(-1.6, -10.9, -3.3, 1.0, 3.0, 0.6, "cartucho_pecho"),
        Box(0.6, -10.9, -3.3, 1.0, 3.0, 0.6, "cartucho_pecho"),
        Box(2.2, -10.9, -3.3, 1.0, 3.0, 0.6, "cartucho_pecho"),
    ])
    r.pieza("morral", "torso", (0, 0, 0), boxes=[
        Box(4.1, -3.4, -1.9, 2.4, 3.8, 3.8, "morral"),
        Box(-4.3, -1.2, -2.7, 8.6, 1.2, 5.4, "cinto"),
    ])
    r.pieza("cartucho", "mano_d", (0, 1.0, 0), boxes=[
        Box(-0.8, -0.8, -5.4, 1.6, 1.6, 5.2, "cartucho"),
        Box(-0.25, -0.25, -7.0, 0.5, 0.5, 1.7, "mecha"),
        Box(-0.75, -0.75, -8.3, 1.5, 1.5, 1.4, "chispa"),
    ])
    # ---- 2, el cargador: el panuelo de la cabeza, las correas del arnes, el cuevano con el mineral y la almadena
    r.pieza("panuelo_cabeza", "cabeza", (0, 0, 0), boxes=[Box(-3.75, -8.6, -3.75, 7.5, 2.2, 7.5, "panuelo_cabeza")])
    r.pieza("correas", "torso", (0, 0, 0), boxes=[
        Box(-3.4, -12.2, -2.75, 1.3, 12.0, 0.4, "correa"),
        Box(2.1, -12.2, -2.75, 1.3, 12.0, 0.4, "correa"),
        Box(-4.3, -4.2, -2.75, 8.6, 1.3, 5.5, "correa"),
    ])
    r.pieza("cuevano", "torso", (0, -11.6, 2.5), boxes=[
        Box(-4.3, 0.0, 0.0, 8.6, 11.0, 5.2, "cuevano"),
        Box(-3.9, -2.6, 0.4, 7.8, 2.8, 4.4, "mineral"),
    ])
    r.pieza("almadena", "mano_d", (0, 0, 0), boxes=[
        Box(-0.8, -0.8, -23.0, 1.6, 1.6, 26.0, "mango_almadena"),
        Box(-2.5, -4.6, -27.6, 5.0, 9.2, 5.0, "cabeza_almadena"),
        Box(-2.7, -4.8, -25.6, 5.4, 9.6, 1.0, "aro_almadena"),
    ])
    r.empaquetar()
    return r


PICADOR_P = ["gorro", "candil", "rodillera_d", "rodillera_i", "pico"]
BARRENERO_P = ["gorra", "panuelo", "bandolera", "morral", "cartucho"]
CARGADOR_P = ["panuelo_cabeza", "correas", "cuevano", "almadena"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: BARRENERO_P + CARGADOR_P,
    1: PICADOR_P + CARGADOR_P,
    2: PICADOR_P + BARRENERO_P,
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#1d1916", "#2a2420", "#3a322b", "#4a4037", "#5b5045", "#6d6154"]
POLVO = ["#141416", "#1d1d20", "#28282b", "#343437"]
CAMISA = ["#2f2b24", "#3d382f", "#4f4a3f", "#635c4f", "#787061"]
CUERO = ["#160f0a", "#22170f", "#2e2015", "#3b2a1c", "#4a3624"]
LANA = ["#1f1d22", "#2a272e", "#36323b", "#433e49"]
PANO = ["#3a1512", "#4e1d17", "#62271f", "#77322a"]
HIERRO = ["#0c0c0f", "#151519", "#1f1f24", "#2a2a31", "#37373f", "#47474f"]
MADERA = ["#1e150e", "#2a1d13", "#382718", "#46331f", "#55402a"]
LATON = ["#3b2a0e", "#5a4318", "#7d5e22", "#a07c2f", "#c9a043"]
ROJO = ["#3a0c0a", "#5a140f", "#7a1d15", "#9a281c"]
CESTO = ["#2a1d0e", "#3b2a15", "#52391c", "#6a4a24", "#80592c"]
MINERAL = ["#0f0f12", "#1a1a1e", "#26262b", "#33333a"]
VETA = ["#3a0806", "#6a120a", "#a01e10", "#d63a1c", "#ff7a3c"]
LLAMA = ["#5a2c06", "#a0560e", "#e09a2a", "#ffd36a", "#fff2c0"]
OJO = ["#5d6266", "#80878b", "#a4abae", "#c8cdcf"]

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _azar01(*v):
    """Un numero de 0 a 1 que solo depende de v (sin el hash de Python, que cambia de una ejecucion a otra)."""
    h = 2166136261
    for n in v:
        h = ((h ^ (int(n) & 0xffffffff)) * 16777619) & 0xffffffff
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    h ^= h >> 15
    return h / 4294967296.0


def materiales(variante):
    piel_t = ruido(PIEL[2:6], 8111, grano=0.5, base=0.45)
    camisa_t = tela(CAMISA, 8112, pliegue=3, grano=0.22, luz=0.14)
    cuero_t = ruido(CUERO[1:5], 8113, grano=0.5)
    lana_t = tela(LANA, 8114, pliegue=2, grano=0.3, luz=0.1)
    pano_t = tela(PANO, 8115, pliegue=2, grano=0.25, luz=0.14)
    hierro_t = metal(HIERRO[1:6], 8116)
    madera_t = madera(MADERA, 8117, "z")
    cesto_t = madera(CESTO, 8118, "y", grano=0.45)
    mineral_t = ruido(MINERAL, 8119, grano=0.7, base=0.4)

    def polvo(c, cara, x, y, w, h, cuanto=0.28, sal=0):
        """El polvo de hierro: manchas grises oscuras (mas abajo, mas) y algun grumo claro."""
        kc = CARAS[cara]
        s0 = w * 31 + h * 17 + sal * 101
        g_ = 0.6 * _azar01(s0, kc, x // 2, y // 2) + 0.4 * _azar01(s0 + 7, kc, x, y)
        k = cuanto * (0.7 + 0.6 * y / max(1, h))
        if g_ < k * 0.45:
            return POLVO[0]
        if g_ < k:
            return mezcla(c, POLVO[1], 0.55)
        if g_ > 0.985:
            return claro(c, 0.18)
        return c

    def piel(cara, x, y, w, h, sal=1):
        c = polvo(piel_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, sal)
        if (x * 5 + y * 7 + CARAS[cara]) % 19 == 0:
            return oscuro(c, 0.35)                     # una grieta seca
        return c

    def cabeza(cara, x, y, w, h):
        c = piel(cara, x, y, w, h, 2)
        if cara == "frente":
            # los ojos blancos de polvo (ciegos), hundidos; la nariz, la boca abierta y la barba sucia
            C = {"K": "#0b0908", "k": PIEL[0], "s": PIEL[1], "o": OJO[2], "O": OJO[3], "b": POLVO[2], "B": POLVO[1], "m": "#120c0a",
                 "p": PIEL[3]}
            v = dibujo([
                "ppppppp",
                "pspppsp",
                "skOpkOs",
                "pspspsp",
                "pbsssbp",
                "bbmmmbb",
                "bBmKmBb",
                "BbbBbbB",
            ], C)(cara, x, y, w, h)
            return v if v is not None else c
        if cara == "arriba":
            return polvo(PIEL[2], cara, x, y, w, h, 0.5, 3)
        if y >= h - 2:
            return POLVO[2] if (x + y) % 2 else POLVO[1]  # la barba por los lados
        return c

    def torso(cara, x, y, w, h):
        if variante == 2:
            # el torso al aire, enorme, con las costillas marcadas y polvo
            c = piel(cara, x, y, w, h, 4)
            if cara == "frente" and y in (4, 6, 8) and 1 <= x <= w - 2 and x != w // 2:
                return oscuro(c, 0.25)
            return c
        # la camisa sucia y rota (asoma la piel por los rotos), el chaleco del picador encima
        if variante == 0 and cara in ("frente", "espalda", "der", "izq"):
            borde = cara == "frente" and (x == w // 2 or x == w // 2 - 1)
            if not borde and not (cara == "frente" and y < 3 and w // 2 - 2 <= x <= w // 2 + 1):
                c = polvo(cuero_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 5)
                if cara == "frente" and x in (w // 2 - 2, w // 2 + 1) and y in (4, 7, 10):
                    return LATON[1]                    # los botones
                return c
        c = polvo(camisa_t(cara, x, y, w, h), cara, x, y, w, h, 0.32, 6)
        if (x * 3 + y * 5 + CARAS[cara] * 7) % 23 == 0 and y > 2:
            return piel(cara, x, y, w, h, 7)           # un roto
        return c

    def brazo(cara, x, y, w, h):
        if variante == 2:
            return piel(cara, x, y, w, h, 8)
        return polvo(camisa_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 9)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 1:
            return PIEL[1]                             # los dedos
        if variante == 1 and y < 2:
            return CAMISA[1]                           # la manga remangada
        return piel(cara, x, y, w, h, 10) if variante != 0 or y > 2 else polvo(camisa_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 9)

    def muslo(cara, x, y, w, h):
        return polvo(lana_t(cara, x, y, w, h), cara, x, y, w, h, 0.35, 11)

    def pierna(cara, x, y, w, h):
        if y >= h - 3:
            return polvo(CUERO[1] if (x + y) % 3 else CUERO[2], cara, x, y, w, h, 0.4, 12)   # las botas
        return muslo(cara, x, y + 6, w, h)

    # ---- el picador
    def gorro(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = polvo(cuero_t(cara, x, y, w, h), cara, x, y, w, h, 0.25, 13)
        if cara != "arriba" and y == h - 1:
            return oscuro(c, 0.3)                      # la costura del borde
        return c

    def candil(cara, x, y, w, h):
        if cara == "frente" and 0 < y < h - 1:
            return LLAMA[2] if (x + y) % 2 else LLAMA[1]  # el cristal (por dentro, la llama)
        return LATON[2 + (x + y) % 2] if cara != "abajo" else LATON[0]

    def rodillera(cara, x, y, w, h):
        return polvo(cuero_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 14)

    def mango(cara, x, y, w, h):
        c = madera_t(cara, x, y, w, h)
        if y >= h - 5:
            return CUERO[2] if y % 2 else CUERO[1]     # el puno con tiras de cuero
        return polvo(c, cara, x, y, w, h, 0.2, 15)

    def pico_hierro(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if cara in ("der", "izq") and (y == 0 or y == h - 1):
            return claro(c, 0.15)
        return c

    # ---- el barrenero
    def gorra(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return polvo(lana_t(cara, x, y, w, h), cara, x, y, w, h, 0.25, 16)

    def panuelo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = polvo(pano_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 17)
        if cara == "espalda" and x in (w // 2 - 1, w // 2) and y >= 1:
            return PANO[0]                             # el nudo
        return c

    def bandolera(cara, x, y, w, h):
        return polvo(cuero_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 18)

    def cartucho(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return ROJO[0]
        c = ROJO[1 + (y // 2) % 2]
        if y in (1, h - 2):
            return CAMISA[3]                           # el papel atado
        return c

    def morral(cara, x, y, w, h):
        c = polvo(tela(["#3a3324", "#4a4230", "#5a503b"], 8120, pliegue=2)(cara, x, y, w, h), cara, x, y, w, h, 0.3, 19)
        if cara == "frente" and y == 1:
            return CUERO[2]                            # la solapa
        return c

    # ---- el cargador
    def panuelo_cabeza(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return polvo(tela(["#2e2a20", "#3b3628", "#4a4433"], 8121, pliegue=2)(cara, x, y, w, h), cara, x, y, w, h, 0.3, 20)

    def correa(cara, x, y, w, h):
        c = polvo(cuero_t(cara, x, y, w, h), cara, x, y, w, h, 0.25, 21)
        if (x + y) % 7 == 0:
            return HIERRO[3]                           # un remache
        return c

    def cuevano(cara, x, y, w, h):
        if cara == "arriba":
            return None
        c = cesto_t(cara, x, y, w, h)
        if (x + y) % 2 == 0:
            c = oscuro(c, 0.18)                        # el trenzado
        if y in (0, h - 1):
            return CESTO[1]
        return polvo(c, cara, x, y, w, h, 0.25, 22)

    def mineral(cara, x, y, w, h):
        c = mineral_t(cara, x, y, w, h)
        if (x * 7 + y * 3 + CARAS[cara]) % 11 == 0:
            return VETA[2]                             # las vetas rojas (lucen)
        return c

    def mango_almadena(cara, x, y, w, h):
        c = madera_t(cara, x, y, w, h)
        if y >= h - 6:
            return CUERO[2] if y % 2 else CUERO[1]
        return polvo(c, cara, x, y, w, h, 0.2, 23)

    def cabeza_almadena(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if cara in ("der", "izq") and (x in (0, w - 1) or y in (0, h - 1)):
            return claro(c, 0.12)
        if (x * 3 + y * 5) % 13 == 0:
            return HIERRO[0]                           # mellas
        return c

    mats = {
        "cabeza": sombra(cabeza, abajo=0.15),
        "torso": sombra(torso),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12), "antebrazo_i": sombra(antebrazo, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.15), "pierna_i": sombra(pierna, abajo=0.15),
        "gorro": sombra(gorro, abajo=0.1),
        "ala": lambda cara, x, y, w, h: CUERO[1] if cara != "arriba" else CUERO[2],
        "candil": candil,
        "llama_candil": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else LLAMA[2],
        "rodillera": sombra(rodillera, abajo=0.15),
        "mango": mango,
        "pico_hierro": sombra(pico_hierro, abajo=0.1),
        "punta": lambda cara, x, y, w, h: HIERRO[4] if (x + y) % 2 else HIERRO[3],
        "gorra": sombra(gorra, abajo=0.1),
        "panuelo": sombra(panuelo, abajo=0.1),
        "bandolera": sombra(bandolera, abajo=0.1),
        "cartucho_pecho": cartucho,
        "morral": sombra(morral, abajo=0.15),
        "cinto": lambda cara, x, y, w, h: CUERO[2] if (x + y) % 5 else LATON[2],
        "cartucho": cartucho,
        "mecha": lambda cara, x, y, w, h: CAMISA[2] if y > 0 else LLAMA[3],
        "chispa": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else LLAMA[2],
        "panuelo_cabeza": sombra(panuelo_cabeza, abajo=0.1),
        "correa": correa,
        "cuevano": sombra(cuevano, abajo=0.12),
        "mineral": mineral,
        "mango_almadena": mango_almadena,
        "cabeza_almadena": sombra(cabeza_almadena, abajo=0.1),
        "aro_almadena": lambda cara, x, y, w, h: HIERRO[4] if (x + y) % 3 else HIERRO[2],
    }
    return mats


def brillo(variante):
    """Lo que luce: el candil del picador, la mecha y la chispa del barrenero, las vetas rojas del cuevano del cargador."""
    b = {}
    if variante == 0:
        b["candil"] = lambda cara, x, y, w, h: (LLAMA[3] if (x + y) % 2 else LLAMA[2]) if cara == "frente" and 0 < y < h - 1 else None
        b["llama_candil"] = lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else None
    if variante == 1:
        b["mecha"] = lambda cara, x, y, w, h: LLAMA[4] if y == 0 else None
        b["chispa"] = lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else None
    if variante == 2:
        b["mineral"] = lambda cara, x, y, w, h: VETA[3 + (x + y) % 2] if (x * 7 + y * 3 + CARAS[cara]) % 11 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14: llega a
# unos 11). _arma = hacia donde apunta lo que lleva en la derecha (el pico, el cartucho y la almadena, a -Z de la mano).
# _agarra: la izquierda en el mango, tantos pixeles por detras (+) o por delante (-).

# de pie: encorvados, la cabeza adelantada y un poco ladeada (escuchan)
BASE = dict(raiz=raiz(-0.4), torso=(14, 0, 2), cabeza=(-4, 8, 6), muslo_d=(-4, 0, 3), pierna_d=(8, 0, 0), muslo_i=(2, 0, -3),
            pierna_i=(6, 0, 0), brazo_i=(-4, 0, -6), antebrazo_i=(-14, 0, 0))
PORTE = {
    # el pico a dos manos cruzado por delante, la cabeza abajo a su izquierda
    0: dict(_mano_d=("h", 1.5, -9.0, 5.0), _arma=(0.6, -0.55, 0.58), _agarra=-8),
    # el cartucho alzado a la altura del pecho (la mecha arriba); la izquierda en el morral
    1: dict(brazo_d=(-18, 0, 12), antebrazo_d=(-78, 0, 0), brazo_i=(-6, 0, -16), antebrazo_i=(-26, 0, 0)),
    # la almadena al hombro derecho (la cabeza a la espalda); la izquierda suelta
    2: dict(_mano_d=("h", -0.6, -3.0, 4.6), _arma=(0.1, 0.55, -0.83), brazo_i=(2, 0, -8), antebrazo_i=(-12, 0, 0)),
}
GUARDIA = dict(raiz=raiz(-1.4), torso=(22, 8, 0), cabeza=(-14, -6, 4), muslo_d=(-14, 0, 6), pierna_d=(26, 0, 0),
               muslo_i=(-2, 0, -6), pierna_i=(20, 0, 0))
LISTO = {
    0: con(GUARDIA, _mano_d=("h", 2.0, -6.0, 6.5), _arma=(0.55, 0.62, 0.55), _agarra=-8),
    1: con(GUARDIA, brazo_d=(-34, 0, 24), antebrazo_d=(-62, 0, 0), brazo_i=(-30, 0, -20), antebrazo_i=(-40, 0, 0)),
    2: con(GUARDIA, torso=(18, 22, 0), cabeza=(-8, -20, 0), _mano_d=("h", 2.0, -7.0, 4.6), _arma=(0.55, 0.72, 0.42), _agarra=-9),
}
# picando la roca (REZA del picador): de cara a la pared, el pico arriba y abajo
P_ALZA = con(BASE, raiz=raiz(-0.2, 0.6), torso=(0, 4, 0), cabeza=(-14, 0, 0), muslo_i=(-16, 0, -4), pierna_i=(14, 0, 0),
             _mano_d=("h", 2.5, 7.0, 1.5), _arma=(0.05, 0.7, -0.7), _agarra=-6)
P_DA = con(BASE, raiz=raiz(-1.6, -0.8), torso=(26, 0, 0), cabeza=(-10, 0, 0), muslo_i=(-26, 0, -4), pierna_i=(22, 0, 0),
           _mano_d=("h", 2.5, -3.0, 8.6), _arma=(0.05, 0.72, 0.69), _agarra=-6)
# agazapado (REZA del barrenero): en cuclillas junto a sus cartuchos
AGAZAPADO = dict(raiz=raiz(-7.0, 0.6), torso=(34, 0, 0), cabeza=(10, 0, 0),
                 muslo_d=(-100, 0, 8), pierna_d=(120, 0, 0), muslo_i=(-96, 0, -8), pierna_i=(118, 0, 0),
                 brazo_d=(-46, 0, 8), antebrazo_d=(-60, 0, 0), brazo_i=(-40, 0, -6), antebrazo_i=(-30, 0, 0))
# sentado en el suelo (REZA del cargador): las piernas por delante, la almadena sobre las rodillas, la cabeza gacha
SENTADO = dict(raiz=raiz(-10.6, 1.0), torso=(-6, 0, 0), cabeza=(34, 0, 4), muslo_d=(-86, 0, 10), pierna_d=(20, 0, 0),
               muslo_i=(-80, 0, -12), pierna_i=(36, 0, 0), _mano_d=("h", 1.0, -9.0, 7.0), _arma=(1, -0.05, 0.1),
               brazo_i=(-56, 0, -10), antebrazo_i=(-20, 0, 0))
# enterrado en los escombros (TUMBADO): de bruces, un brazo por delante
ENTERRADO = dict(raiz=raiz(2.4, -13.0, rot=(90, 0, 0)), torso=(0, 0, 0), cabeza=(-30, 10, 0),
                 muslo_d=(10, 0, 6), pierna_d=(30, 0, 0), muslo_i=(4, 0, -6), pierna_i=(16, 0, 0),
                 brazo_d=(-150, 0, 14), antebrazo_d=(-20, 0, 0), brazo_i=(-10, 0, -14), antebrazo_i=(-30, 0, 0))
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")
BRAZOS = BRAZO_D + ("brazo_i", "antebrazo_i", "mano_i")


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

    # ---- quieto: la cabeza ladeada, escuchando; de vez en cuando la gira de golpe a un lado
    bucle("quieto", 3.6, [
        (0.0, BASE),
        (1.3, con(BASE, torso=(11, 2, 2), cabeza=(-6, 12, 8), raiz=raiz(-0.2))),
        (2.2, con(BASE, torso=(15, -4, 1), cabeza=(-2, -18, -4))),
        (2.5, con(BASE, torso=(15, -3, 2), cabeza=(-4, -14, -2))),
        (3.6, BASE),
    ])
    # ---- andar: arrastrando los pies, los hombros caidos
    pasos = []
    for k, (md, mi, pd, pi_, ty) in enumerate(((-22, 18, 10, 16, 6), (-6, 4, 6, 28, 0), (18, -22, 16, 10, -6), (4, -6, 28, 6, 0),
                                               (-22, 18, 10, 16, 6))):
        pasos.append((1.3 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, 0), "cabeza": (0, -ty * 0.6, 0), "raiz": raiz(0.6 if k % 2 else 0.0),
                                    "brazo_i": (-mi * 0.3, 0, 0)}))
    bucle("andar", 1.3, pasos)
    for v in range(3):
        q = fija(con(BASE, PORTE[v]))
        bucle(f"porte_{VARIANTES[v]}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
        l0 = fija(LISTO[v])
        l1 = fija(con(LISTO[v], raiz=raiz(-2.0), torso=(LISTO[v]["torso"][0] + 4, LISTO[v]["torso"][1] - 3, 0)))
        bucle(f"listo_{VARIANTES[v]}", 1.6, [(0.0, l0), (0.8, l1), (1.6, l0)])
    # ---- picando (el picador, REZA): el golpe da en la pared en el segundo 1.15 (ahi suena y saltan piedrecitas)
    bucle("picando", 1.6, [
        (0.00, P_DA),
        (0.35, con(P_DA, torso=(26, 0, 0))),
        (0.70, P_ALZA),
        (0.95, con(P_ALZA, torso=(-4, 4, 0))),
        (1.15, P_DA),
        (1.60, P_DA),
    ])
    ag = fija(AGAZAPADO)
    bucle("agazapado", 5.0, [(0.0, ag), (2.5, con(ag, cabeza=(16, 10, 0))), (5.0, ag)])
    se = fija(SENTADO)
    bucle("sentado", 6.0, [(0.0, se), (3.0, fija(con(SENTADO, cabeza=(40, -6, 2), torso=(-4, 0, 0)))), (6.0, se)])
    bucle("enterrado", 6.0, [(0.0, ENTERRADO), (6.0, ENTERRADO)])

    # ================================================================ acciones
    L0, L1, L2 = LISTO[0], LISTO[1], LISTO[2]

    # ---- 1 picotazo (el picador): el pico a dos manos por encima de la cabeza, lo sostiene y lo clava delante
    PT_ALTO = con(L0, raiz=raiz(0.0, 1.2), torso=(-14, 6, 0), cabeza=(-24, -4, 0), muslo_i=(-12, 0, -4), pierna_i=(10, 0, 0),
                  _mano_d=("h", 3.0, 7.5, 0.5), _arma=(0.05, 0.55, -0.83), _agarra=-6)
    PT_BAJO = con(L0, raiz=raiz(-3.6, -2.4), torso=(46, -4, 0), cabeza=(-30, 2, 0), muslo_d=(16, 0, 4), pierna_d=(30, 0, 0),
                  muslo_i=(-50, 0, -4), pierna_i=(52, 0, 0), _mano_d=("h", 3.4, -3.0, 8.0), _arma=(0.05, -0.75, 0.66), _agarra=-6)
    accion("picotazo", 1.7, [
        (0.00, L0),
        (0.55, PT_ALTO),
        (0.85, con(PT_ALTO, raiz=raiz(0.2, 1.4), torso=(-18, 6, 0))),
        (0.93, con(L0, raiz=raiz(-1.2, 0.0), torso=(14, 2, 0), cabeza=(-26, 0, 0), _mano_d=("h", 3.2, 3.0, 8.5), _arma=(0.05, 0.4, 0.92),
                   _agarra=-6)),
        (1.00, PT_BAJO),
        (1.30, con(PT_BAJO, torso=(44, -4, 0))),
        (1.70, L0),
    ], golpes=(1.00,))
    # ---- 2 barrido (el picador): el pico de derecha a izquierda a la altura de la cintura
    BR_D = con(L0, raiz=raiz(-1.2, 0.8), torso=(12, 52, 0), cabeza=(-4, -46, 0), _mano_d=("h", 0.6, -4.0, 5.0), _arma=(-1, 0.15, -0.1),
               _agarra=-7)
    BR_F = con(L0, raiz=raiz(-2.0, -0.6), torso=(18, 10, 0), cabeza=(-8, -10, 0), _mano_d=("h", 3.6, -4.6, 7.0), _arma=(0.2, 0.05, 1),
               _agarra=-7)
    BR_I = con(L0, raiz=raiz(-2.4, -1.6), torso=(22, -38, 0), cabeza=(-10, 32, 0), muslo_i=(-30, 0, -6), pierna_i=(28, 0, 0),
               _mano_d=("h", 6.0, -4.6, 4.6), _arma=(1, 0.05, 0.3), _agarra=-7)
    accion("barrido", 1.3, [
        (0.00, L0),
        (0.42, BR_D),
        (0.58, BR_F),
        (0.68, BR_I),
        (0.95, con(BR_I, torso=(22, -42, 0))),
        (1.30, L0),
    ], golpes=(0.60,))
    # ---- 3 lanzar (el barrenero): echa atras el cartucho por encima del hombro y lo lanza en arco
    LZ_ATRAS = con(L1, raiz=raiz(-0.8, 1.6), torso=(-8, 34, 0), cabeza=(-14, -26, 0), brazo_i=(-60, 0, -30), antebrazo_i=(-20, 0, 0),
                   brazo_d=(-150, 0, 30), antebrazo_d=(-50, 0, 0))
    LZ_SUELTA = con(L1, raiz=raiz(-1.8, -1.8), torso=(26, -20, 0), cabeza=(-18, 14, 0), brazo_i=(-20, 0, -40), muslo_i=(-36, 0, -6),
                    pierna_i=(36, 0, 0), brazo_d=(-110, 0, 4), antebrazo_d=(-6, 0, 0))
    accion("lanzar", 1.4, [
        (0.00, L1),
        (0.50, LZ_ATRAS),
        (0.70, con(LZ_ATRAS, torso=(-10, 38, 0))),
        (0.80, LZ_SUELTA),
        (1.05, con(LZ_SUELTA, brazo_d=(-60, 0, 10), antebrazo_d=(-10, 0, 0))),
        (1.40, L1),
    ], golpes=(0.81,))
    # ---- 4 mechazo (el barrenero): de cerca, un golpe de lado con el cartucho encendido
    MC_ATRAS = con(L1, raiz=raiz(-1.0, 1.0), torso=(12, 44, 0), cabeza=(-8, -34, 0), brazo_d=(-50, 0, 80), antebrazo_d=(-30, 0, 0))
    MC_FIN = con(L1, raiz=raiz(-1.8, -1.4), torso=(20, -40, 0), cabeza=(-12, 30, 0), brazo_d=(-90, -20, -30), antebrazo_d=(-20, 0, 0))
    accion("mechazo", 1.0, [
        (0.00, L1),
        (0.36, MC_ATRAS),
        (0.50, con(L1, raiz=raiz(-1.6, -0.6), torso=(18, 0, 0), brazo_d=(-95, 0, 20), antebrazo_d=(-10, 0, 0))),
        (0.60, MC_FIN),
        (1.00, L1),
    ], golpes=(0.52,))
    # ---- 5 almadenazo (el cargador): alza la almadena a dos manos muy por encima, la sostiene y la estrella (la onda)
    AL_ALTO = con(L2, raiz=raiz(0.0, 1.4), torso=(-20, 4, 0), cabeza=(-22, -4, 0), muslo_d=(8, 0, 4), pierna_d=(8, 0, 0),
                  muslo_i=(-14, 0, -4), pierna_i=(10, 0, 0), _mano_d=("h", 3.6, 7.6, -0.5), _arma=(0.05, 0.4, -0.92), _agarra=-6)
    AL_BAJO = con(L2, raiz=raiz(-4.2, -2.6), torso=(52, -4, 0), cabeza=(-30, 2, 0), muslo_d=(16, 0, 4), pierna_d=(34, 0, 0),
                  muslo_i=(-56, 0, -4), pierna_i=(58, 0, 0), _mano_d=("h", 4.0, -3.6, 7.4), _arma=(0.05, -0.66, 0.75), _agarra=-6)
    accion("almadenazo", 2.2, [
        (0.00, L2),
        (0.70, AL_ALTO),
        (1.15, con(AL_ALTO, raiz=raiz(0.3, 1.7), torso=(-24, 4, 0))),
        (1.23, con(L2, raiz=raiz(-1.6, 0.0), torso=(16, 2, 0), cabeza=(-26, 0, 0), _mano_d=("h", 3.8, 3.0, 8.0), _arma=(0.05, 0.35, 0.94),
                   _agarra=-6)),
        (1.30, AL_BAJO),
        (1.70, con(AL_BAJO, torso=(50, -4, 0))),
        (2.20, L2),
    ], golpes=(1.30,))
    # ---- 6 barrido (el cargador): la almadena de derecha a izquierda, muy abierta, a una mano al final
    A1 = con(sin(L2, "_agarra"), brazo_i=(-30, 0, -40), antebrazo_i=(-20, 0, 0))
    BA_D = con(L2, raiz=raiz(-1.4, 0.8), torso=(12, 60, 0), cabeza=(-2, -52, 0), _mano_d=("h", 0.6, -4.6, 4.6), _arma=(-1, 0.1, -0.2),
               _agarra=-7)
    BA_F = con(A1, raiz=raiz(-2.0, -0.6), torso=(18, 12, 0), cabeza=(-6, -12, 0), _mano_d=("h", 4.0, -5.6, 6.0), _arma=(0.2, 0.0, 1))
    BA_I = con(A1, raiz=raiz(-2.4, -1.8), torso=(22, -40, 0), cabeza=(-8, 32, 0), muslo_i=(-34, 0, -6), pierna_i=(30, 0, 0),
               _mano_d=("h", 6.0, -5.0, 4.4), _arma=(1, 0.05, 0.3), brazo_i=(-20, 0, -60))
    accion("barrido_almadena", 1.8, [
        (0.00, L2),
        (0.60, BA_D),
        (0.85, con(BA_D, torso=(12, 66, 0), cabeza=(-2, -56, 0))),
        (0.96, BA_F),
        (1.06, BA_I),
        (1.36, con(BA_I, torso=(22, -44, 0))),
        (1.80, L2),
    ], golpes=(0.96,))
    # ---- 7 embestida (el cargador): baja el hombro y se lanza (el cuevano por delante de nada: el hombro)
    EM_CARGA = con(L2, raiz=raiz(-3.0, 1.6), torso=(40, 30, 0), cabeza=(-30, -20, 0), muslo_d=(-40, 0, 6), pierna_d=(50, 0, 0),
                   muslo_i=(10, 0, -6), pierna_i=(30, 0, 0))
    EM_CHOCA = con(L2, raiz=raiz(-2.4, -3.0), torso=(44, 34, 0), cabeza=(-34, -24, 0), muslo_d=(24, 0, 6), pierna_d=(20, 0, 0),
                   muslo_i=(-56, 0, -6), pierna_i=(44, 0, 0))
    accion("embestida", 1.4, [
        (0.00, L2),
        (0.35, EM_CARGA),
        (0.55, EM_CHOCA),
        (0.80, con(EM_CHOCA, torso=(40, 30, 0))),
        (1.40, L2),
    ], golpes=(0.55,))
    # ---- 8 aturdido
    AT = dict(raiz=raiz(-1.4, 2.0), torso=(-10, 0, 8), cabeza=(-22, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(18, 0, 0), brazo_d=(16, 0, 26), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir: se dobla, cae de rodillas y se derrumba de bruces (el servidor lo deshace en polvo y grava)
    RODILLAS = dict(raiz=raiz(-6.0, 0.8), torso=(26, 0, 0), cabeza=(30, 0, 0), muslo_d=(-10, 0, 5), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -5),
                    pierna_i=(96, 0, 0), brazo_d=(-10, 0, 14), antebrazo_d=(-10, 0, 0), brazo_i=(-10, 0, -14), antebrazo_i=(-10, 0, 0))
    BRUCES = dict(raiz=raiz(2.2, -12.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-10, 20, 0), muslo_d=(4, 0, 8), pierna_d=(6, 0, 0),
                  muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0), brazo_d=(-150, 0, 20), antebrazo_d=(-10, 0, 0), brazo_i=(-30, 0, -40),
                  antebrazo_i=(-70, 0, 0))
    accion("morir", 2.0, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0))),
        (0.45, RODILLAS),
        (1.00, con(RODILLAS, torso=(40, 0, 10), cabeza=(40, 0, 10))),
        (1.45, BRUCES),
        (2.00, BRUCES),
    ])
    # ---- 10 levantarse (el barrenero): estaba agazapado; se pone en pie de un tiron
    accion("levantarse", 1.1, [
        (0.00, AGAZAPADO),
        (0.35, con(AGAZAPADO, raiz=raiz(-5.0, 0.4), torso=(20, 0, 0), cabeza=(-20, 0, 0))),
        (0.70, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0))),
        (1.10, GUARDIA),
    ])
    # ---- 11 alzarse: enterrado en los escombros, se incorpora sacudiendose la grava
    accion("alzarse", 1.6, [
        (0.00, ENTERRADO),
        (0.40, con(ENTERRADO, raiz=raiz(2.0, -11.0, rot=(60, 0, 0)), brazo_d=(-90, 0, 30), brazo_i=(-90, 0, -30))),
        (0.85, con(AGAZAPADO, torso=(10, 0, 0), cabeza=(-20, 0, 0), brazo_d=(-20, 0, 50), brazo_i=(-20, 0, -50))),
        (1.25, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0))),
        (1.60, GUARDIA),
    ])
    # ---- 12 volverse (el picador): deja de picar y se vuelve hacia lo que ha oido (el giro lo pone el servidor)
    accion("volverse", 0.9, [
        (0.00, P_DA),
        (0.30, con(P_DA, raiz=raiz(-1.2, 0.0), torso=(16, -10, 0), cabeza=(-20, -30, 0))),
        (0.60, con(L0, torso=(18, 4, 0), cabeza=(-24, 8, 6))),
        (0.90, L0),
    ])
    # ---- 13 incorporarse (el cargador): estaba sentado; se levanta apoyandose en la almadena
    accion("incorporarse", 1.6, [
        (0.00, SENTADO),
        (0.45, con(SENTADO, raiz=raiz(-9.0, 0.0), torso=(30, 0, 0), cabeza=(10, 0, 0), _mano_d=("h", 1.5, -8.0, 7.0),
                   _arma=(0.6, -0.1, 0.8))),
        (0.95, con(sin(AGAZAPADO, "brazo_d", "antebrazo_d"), _mano_d=("h", 1.5, -8.0, 6.0), _arma=(0.3, -0.15, 0.94))),
        (1.30, con(sin(L2, "_agarra"), raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0), brazo_i=(-30, 0, -20),
                   antebrazo_i=(-30, 0, 0))),
        (1.60, L2),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "barrenero" in nombre or nombre in ("lanzar", "mechazo", "levantarse", "agazapado"):
        return 1
    if "cargador" in nombre or nombre in ("almadenazo", "barrido_almadena", "embestida", "incorporarse", "sentado"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_picador"]]
    return []
