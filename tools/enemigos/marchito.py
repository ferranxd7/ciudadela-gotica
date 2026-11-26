"""
El marchito de Morvain (parte 7e): lo que la Sombra dejo de la gente de las Tierras Marchitas. Alto y consumido, la piel
gris de ceniza agrietada con brasas rojas en las grietas, las cuencas con un rescoldo y harapos claros. Tres:
  0  la guadana  (capucha; siega ancha y tajo de arriba abajo, con las dos manos)
  1  el farol    (la cabeza pelada; un cuchillo curvo y el farol de almas colgando de la otra mano: lo estrella
                  y prende fuego de almas)
  2  la campana  (el peregrino: capucha y baculo con una campana; la toca y acuden todos los de alrededor)
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (brillo_de, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,  # noqa: F401
                   sombra, tela)

NOMBRE = "marchito"
VARIANTES = ("guadana", "farol", "campana")
ESCALA = 1.08

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # alto y flaco: piernas y brazos largos, el torso estrecho
    humanoide(r, torso=(7, 13, 4), cabeza=(7, 8, 7), brazo=(3, 14, 3), pierna=(3, 13, 3))
    # el harapo: de la cintura a media pierna, roto por abajo
    r.pieza("harapo", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2.5, 8, 9, 5, "harapo", grow=g(0.35))])
    # la capucha (0 y 2): por encima de la cabeza, con el pico caido por detras
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-3.5, -8, -3.5, 7, 8, 7, "capucha", grow=g(0.6)),
        Box(-2, -6, 3.5, 4, 6, 2, "capucha_pico"),
    ])
    # el cuello de trapo del del farol (1): una vuelta de harapo por los hombros
    r.pieza("cuello", "torso", (0, -13, 0), boxes=[Box(-4.5, 0, -2.5, 9, 3, 5, "cuello", grow=g(0.3))])
    # ---- lo de la mano derecha (el arma apunta a -Z con el brazo caido)
    r.pieza("guadana", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -24, 1, 1, 16, "asta"),
        Box(-0.5, -0.5, -8, 1, 1, 14, "asta"),
        Box(-1.5, -0.5, -12, 2, 1, 1, "puno"),
        # la hoja: un plano de canto que cuelga del extremo (la media luna pintada)
        Box(0, 0, -27, 0, 10, 14, "hoja_guadana"),
    ])
    r.pieza("cuchillo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -3, 1, 1, 5, "mango_cuchillo"),
        Box(0, -2, -10, 0, 3, 7, "hoja_cuchillo"),
    ])
    r.pieza("baculo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -22, 1, 1, 16, "asta"),
        Box(-0.5, -0.5, -6, 1, 1, 14, "asta"),
        Box(-3, -0.5, -22, 6, 1, 1, "travesano"),
    ])
    # la campana cuelga del travesano (hacia la mano: con el baculo de pie, queda colgando)
    r.pieza("campana", "baculo", (2.5, 0, -21), boxes=[
        Box(-0.5, -0.5, 0, 1, 1, 1, "cuerda"),
        Box(-1.5, -1.5, 1, 3, 3, 3, "campana"),
        Box(-2, -2, 3.6, 4, 4, 0.8, "campana_boca"),
    ])
    # ---- el farol: cuelga de la mano izquierda (la cadena a -Z; con el brazo cerrado apunta abajo)
    r.pieza("farol", "mano_i", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -3, 1, 1, 3, "cadena"),
        Box(-2, -2, -3.8, 4, 4, 0.8, "farol_tapa"),
        Box(-1.5, -1.5, -7.8, 3, 3, 4, "farol"),
        Box(-2, -2, -8.6, 4, 4, 0.8, "farol_tapa"),
    ])
    r.pieza("llama_farol", "farol", (0, 0, -5.8), boxes=[Box(-1, -1, -1.5, 2, 2, 3, "llama_farol")])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {
    0: ["cuello", "cuchillo", "baculo", "campana", "farol", "llama_farol"],
    1: ["capucha", "guadana", "baculo", "campana"],
    2: ["cuello", "guadana", "cuchillo", "farol", "llama_farol"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#2e2b29", "#45413d", "#5d5852", "#767069", "#8f8880", "#aaa298"]
GRIETA = "#1f1612"
BRASA = ["#5a1408", "#8a2410", "#c0401a", "#f07a2a", "#ffc070"]
CUENCA = "#0a0707"
HARAPO = {
    0: ["#2b2722", "#3b352e", "#4c453b", "#5f5649", "#716757"],
    1: ["#3c3a36", "#504d47", "#66625a", "#7c776d", "#938d81"],
    2: ["#4d4636", "#625a46", "#797057", "#90866a", "#a89d7e"],
}
CAPUCHA = {0: ["#1d1a17", "#29251f", "#36302a", "#443c34", "#524940"], 2: ["#3a2f24", "#4a3d2e", "#5b4b39", "#6d5b45", "#806b52"]}
FAJA = ["#3a0f0f", "#521616", "#6c1e1d", "#872827"]
PALO = ["#241a12", "#33261a", "#443323", "#57412d", "#6b5038"]
HIERRO = ["#1f1f23", "#33333a", "#4a4a53", "#66666f", "#86868f", "#a8a8b2"]
BRONCE = ["#3b2a12", "#58401c", "#7a5a28", "#9c7636", "#c09a50", "#e0c27a"]
ALMA = ["#123a4a", "#1e6278", "#3aa0c0", "#7ad6ec", "#c8f4ff"]


def _grietas(semilla):
    """Las grietas de la piel: lineas finas que se ramifican (True donde hay grieta)."""
    import random
    r = random.Random(semilla)
    puntos = set()
    for _ in range(6):
        x, y = r.randrange(0, 16), r.randrange(0, 16)
        for _ in range(r.randrange(4, 9)):
            puntos.add((x, y))
            x += r.choice((-1, 0, 1))
            y += 1 if r.random() < 0.7 else 0
    return puntos


def _cara(variante):
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": CUENCA, "k": "#1a1311", "M": "#120c0a", "G": GRIETA,
         "T": "#8a8070"}
    filas = [
        "OossssO",
        "oSsGSso",
        "oSSsSSo",
        "OKKoKKO",
        "OKkokKO",
        "OosssoO",
        "OoMMMoO",
        "OOoToOO",
    ]
    return dibujo(filas, C)


def materiales(variante):
    harapo_t = HARAPO[variante]
    piel_base = ruido(PIEL[1:5], 13, grano=0.5, base=0.5)
    grietas = _grietas(77)

    def piel(cara, x, y, w, h):
        if ((x * 3 + y * 5 + len(cara)) % 16, (y * 2 + x) % 16) in grietas:
            return GRIETA
        return piel_base(cara, x, y, w, h)

    harapo = tela(harapo_t, 23 + variante, pliegue=3)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara(variante)(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 3 <= y <= 4 and d in (2, 3):
                return PIEL[1]
        if cara == "arriba" and (x * 2 + y) % 5 == 0:
            return GRIETA
        return c

    def torso(cara, x, y, w, h):
        c = harapo(cara, x, y, w, h)
        if cara == "frente":
            # el pecho hundido asoma por el harapo abierto: las costillas
            if y <= 5 and abs(x - 3) <= 1.5 - y * 0.2:
                return PIEL[3] if y % 2 == 0 else PIEL[1]
            if variante == 2 and y in (9, 10):
                return FAJA[1 + (x % 2)]           # la faja roja del peregrino
            if y == 10 and variante != 2:
                return oscuro(harapo_t[1], 0.15)
        if cara == "espalda" and variante == 2 and y in (9, 10):
            return FAJA[1]
        return c

    def harapo_f(cara, x, y, w, h):
        corte = (6, 8, 7, 5, 8, 6, 7, 8, 5, 7, 6)[(x * 3 + {"frente": 0, "espalda": 4, "der": 2, "izq": 7}.get(cara, 1)) % 11]
        if cara in ("arriba", "abajo") or y > corte:
            return None
        c = harapo(cara, x, y + 4, w, h + 4)
        return oscuro(c, 0.06 + 0.04 * y)

    def brazo(cara, x, y, w, h):
        if y <= 3:
            return harapo(cara, x, y, w, h)           # la manga rota
        return piel(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 1:
            return PIEL[1] if cara != "abajo" else PIEL[2]   # los dedos largos
        return piel(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return oscuro(harapo(cara, x, y, w, h), 0.2)

    def pierna(cara, x, y, w, h):
        if y >= h - 1:
            return PIEL[1]
        return piel(cara, x, y, w, h)

    def capucha(cara, x, y, w, h):
        t = tela(CAPUCHA.get(variante, CAPUCHA[0]), 41 + variante, pliegue=4)(cara, x, y, w, h)
        if cara == "frente":
            if 1 <= x <= w - 2 and 2 <= y <= h - 1:
                return None
            if y == 1 and 2 <= x <= w - 3:
                return None
        if cara == "abajo":
            return None
        if cara in ("der", "izq") and y >= h - 2 and delante(cara, x, w) <= 1:
            return None
        return t

    def cuello(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = tela(HARAPO[1], 47, pliegue=2)(cara, x, y, w, h)
        if cara == "frente" and 3 <= x <= 5 and y <= 1:
            return None
        if y == h - 1 and (x * 7) % 3 == 0:
            return None                                 # los flecos
        return c

    C = {"H": HIERRO[4], "h": HIERRO[3], "d": HIERRO[2], "o": HIERRO[1], "x": "#3b2418", "r": "#5a2a18"}
    hoja_guadana = dibujo([
        # de costado (el frente a la izquierda): la hoja sale del extremo del asta y cae hacia atras en media luna;
        # el filo (lo claro) por dentro de la curva
        "HHh...........",
        "dHHh..........",
        "odHHh.........",
        ".odHHHh.......",
        "..odhHHHh.....",
        "...oddhHHHh...",
        ".....oddhHHHh.",
        ".......ooddhHH",
        "..........oddH",
        "............oo",
    ], C)
    hoja_cuchillo = dibujo([
        "..hHHHH",
        ".hHdddH",
        "hHd....",
    ], C)

    mats = {
        "cabeza": sombra(cabeza, abajo=0.18),
        "torso": sombra(torso),
        "harapo": harapo_f,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.18), "antebrazo_i": sombra(antebrazo, abajo=0.18),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.25), "pierna_i": sombra(pierna, abajo=0.25),
        "capucha": capucha, "capucha_pico": sombra(tela(CAPUCHA.get(variante, CAPUCHA[0]), 42, pliegue=3), abajo=0.3),
        "cuello": cuello,
        "asta": madera(PALO, 71, "z"),
        "puno": madera(PALO, 72, "x"),
        "travesano": madera(PALO, 73, "x"),
        "hoja_guadana": por_cara(liso(None), izq=hoja_guadana, der=espejado(hoja_guadana)),
        "mango_cuchillo": madera(["#2a1810", "#3a2216", "#4a2e1e"], 74, "z"),
        "hoja_cuchillo": por_cara(liso(None), izq=hoja_cuchillo, der=espejado(hoja_cuchillo)),
        "cuerda": liso("#5a4a32"),
        "campana": metal(BRONCE[1:5], 75),
        "campana_boca": por_cara(metal(BRONCE[0:4], 76), abajo=liso(BRONCE[0]), arriba=liso(BRONCE[0])),
        "cadena": metal(HIERRO[1:4], 77),
        "farol_tapa": metal(HIERRO[1:4], 78),
        "farol": por_cara(lambda cara, x, y, w, h: HIERRO[1] if x in (0, w - 1) else ALMA[2], arriba=liso(HIERRO[1]), abajo=liso(HIERRO[1])),
        "llama_farol": ruido(ALMA[2:5], 79, grano=0.8, base=0.6),
    }
    return mats


def brillo(variante):
    """Lo que brilla: el rescoldo de las cuencas, las brasas de algunas grietas y la llama de almas del farol."""
    C = {"R": BRASA[3], "r": BRASA[2]}
    ojos = dibujo([
        ".......",
        ".......",
        ".......",
        ".rR.Rr.",
        "..r.r..",
    ], C)
    grietas = _grietas(77)

    def brasas(cara, x, y, w, h):
        k = ((x * 3 + y * 5 + len(cara)) % 16, (y * 2 + x) % 16)
        if k in grietas and (x + y * 3) % 4 == 0:
            return BRASA[2] if (x + y) % 3 else BRASA[3]
        return None

    b = {"cabeza": por_cara(liso(None), frente=ojos), "brazo_d": brasas, "brazo_i": brasas, "antebrazo_d": brasas,
         "antebrazo_i": brasas, "pierna_d": brasas, "pierna_i": brasas}
    if variante == 1:
        b["llama_farol"] = ruido(ALMA[3:5] + ["#ffffff"], 79, grano=0.8, base=0.6)
        b["farol"] = por_cara(lambda cara, x, y, w, h: None if x in (0, w - 1) else ALMA[3], arriba=liso(None), abajo=liso(None))
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14)

# de pie, muy encorvado: la cabeza adelantada y colgando, las rodillas algo dobladas
BASE = dict(raiz=raiz(-0.8), torso=(20, 0, 2), cabeza=(-6, 0, -4),
            muslo_d=(-8, 0, 3), pierna_d=(14, 0, 0), muslo_i=(0, 0, -3), pierna_i=(10, 0, 0),
            brazo_i=(-10, 0, -6), antebrazo_i=(-10, 0, 0), harapo=(10, 0, 0))
PORTE = {
    # la guadana arrastrando la hoja por detras
    "guadana": dict(_mano_d=("h", -1.0, -12.4, 1.0), _arma=(-0.1, -0.38, -1)),
    # el cuchillo colgando; el farol colgando de la otra mano, un poco adelantado
    "farol": dict(_mano_d=("h", -0.5, -12.6, 1.5), _arma=(0, -1, 0.3),
                  _mano_i=("h", 0.0, -9.0, 7.0), _arma_i=(0, -1, 0.05)),
    # el baculo de pie, como un peregrino
    "campana": dict(_mano_d=("h", 0.5, -8.0, 5.5), _arma=(0.04, 1, 0.06)),
}
GUARDIA = dict(raiz=raiz(-1.8), torso=(26, 10, 0), cabeza=(-14, -10, 0), harapo=(14, 0, 0),
               muslo_d=(6, 0, 6), pierna_d=(22, 0, 0), muslo_i=(-26, 0, -6), pierna_i=(30, 0, 0))
LISTO = {
    "guadana": con(GUARDIA, _mano_d=("h", 0.5, -10.0, 1.5), _arma=(0.2, 0.22, 1), _agarra=-10),
    "farol": con(GUARDIA, _mano_d=("h", 0.0, -5.0, 8.5), _arma=(0.1, 1, 0.5),
                 _mano_i=("h", 1.0, -2.0, 10.0), _arma_i=(0, -1, 0.05)),
    "campana": con(GUARDIA, _mano_d=("h", 1.5, -4.0, 10.0), _arma=(0, 1, 0.5), brazo_i=(-24, 0, -18), antebrazo_i=(-40, 0, 0)),
}
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")


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

    # ---- quieto: se mece despacio, la cabeza cae de lado y vuelve
    bucle("quieto", 4.6, [
        (0.0, BASE),
        (1.5, con(BASE, torso=(23, 4, 4), cabeza=(-2, 6, -10), raiz=raiz(-1.1), brazo_i=(-6, 0, -8))),
        (3.1, con(BASE, torso=(18, -4, 0), cabeza=(-10, -6, 2), raiz=raiz(-0.6), brazo_i=(-12, 0, -5))),
        (4.6, BASE),
    ])
    # ---- andar: a trancos largos, arrastrando un pie
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-30, 22, 10, 26, 7, 0.0), (-6, 6, 8, 46, 0, 1.0), (22, -30, 26, 10, -7, 0.0), (6, -6, 46, 8, 0, 1.0), (-30, 22, 10, 26, 7, 0.0))):
        pasos.append((1.3 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, -ty * 0.5), "cabeza": (0, -ty * 0.6, ty * 0.6), "raiz": raiz(up),
                                    "harapo": (-md * 0.3, 0, 0)}))
    bucle("andar", 1.3, pasos)
    # ---- lo que lleva cada uno (solo los brazos)
    for arma, p in PORTE.items():
        q = con(BASE, p)
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS if arma == "farol" else BRAZO_D)
    for arma, p in LISTO.items():
        bucle(f"listo_{arma}", 1.8, [
            (0.0, p),
            (0.9, con(p, raiz=raiz(-2.4), torso=(p["torso"][0] + 4, p["torso"][1] - 4, 0), cabeza=(-10, -8, 3))),
            (1.8, p),
        ])
    # ---- de rodillas ante una tumba
    REZA = dict(raiz=raiz(-10.4, 1.0), muslo_d=(-8, 0, 4), pierna_d=(100, 0, 0), muslo_i=(-8, 0, -4), pierna_i=(100, 0, 0),
                torso=(34, 0, 0), cabeza=(30, 0, 0), harapo=(-10, 0, 0),
                brazo_d=(-50, 0, -16), antebrazo_d=(-74, 0, 0), mano_d=(60, 0, 0), brazo_i=(-50, 0, 16), antebrazo_i=(-74, 0, 0))
    bucle("rezar", 4.0, [
        (0.0, REZA),
        (2.0, con(REZA, torso=(46, 0, 0), cabeza=(38, 0, 0), raiz=raiz(-10.8, 0.4))),
        (4.0, REZA),
    ])
    # ---- tirado boca abajo entre los muertos
    TUMBADO = dict(raiz=raiz(2.0, 15.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 36, 0), harapo=(0, 0, 0),
                   muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                   _mano_d=("h", -3.0, -0.6, 10.0), _arma=(-0.25, 0.0, 1), brazo_i=(-22, 0, -30), antebrazo_i=(-70, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (2.0, con(TUMBADO, cabeza=(-8, 40, 0))), (4.0, TUMBADO)])

    # ================================================================ acciones
    G, F, C = LISTO["guadana"], LISTO["farol"], LISTO["campana"]

    # ---- la guadana: siega ancha de derecha a izquierda, a la altura de las rodillas
    G_ATRAS = con(sin(G, "_agarra"), raiz=raiz(-1.6, 1.4), torso=(16, 58, 0), cabeza=(-8, -50, 0), muslo_d=(18, 0, 6), pierna_d=(24, 0, 0),
                  brazo_i=(-70, 0, 30), antebrazo_i=(-40, 0, 0),
                  _mano_d=("h", -3.0, -9.0, -3.0), _arma=(-1, -0.15, -0.2))
    G_DELANTE = con(G, raiz=raiz(-2.6, -1.6), torso=(30, -4, 0), cabeza=(-18, 2, 0), muslo_i=(-38, 0, -6), pierna_i=(36, 0, 0),
                    _mano_d=("h", 1.5, -8.0, 9.0), _arma=(0.1, -0.3, 1), _agarra=-4)
    G_FIN = con(sin(G, "_agarra"), raiz=raiz(-2.4, -2.4), torso=(26, -56, 0), cabeza=(-14, 46, 0), muslo_i=(-40, 0, -6), pierna_i=(40, 0, 0),
                brazo_i=(-30, 0, -30), antebrazo_i=(-30, 0, 0),
                _mano_d=("h", 6.0, -8.5, 6.0), _arma=(1, -0.25, 0.2))
    accion("guadana_siega", 1.9, [
        (0.00, G),
        (0.60, G_ATRAS),
        (0.86, con(G_ATRAS, torso=(16, 64, 0), cabeza=(-8, -54, 0))),
        (0.96, G_DELANTE),
        (1.06, G_FIN),
        (1.40, con(G_FIN, torso=(24, -60, 0))),
        (1.90, G),
    ], golpes=(0.98,))
    # ---- la guadana: la alza por encima de la cabeza y la clava delante
    G_ALTO = con(G, raiz=raiz(-0.4, 1.8), torso=(-10, 12, 0), cabeza=(-24, -10, 0), muslo_d=(12, 0, 4), pierna_d=(10, 0, 0),
                 muslo_i=(-16, 0, -4), pierna_i=(12, 0, 0), _mano_d=("h", 1.0, 11.0, -2.0), _arma=(0.05, 0.6, -0.8), _agarra=-4)
    G_ABAJO = con(G, raiz=raiz(-3.8, -3.0), torso=(52, -6, 0), cabeza=(-34, 4, 0), muslo_d=(18, 0, 4), pierna_d=(34, 0, 0),
                  muslo_i=(-56, 0, -4), pierna_i=(58, 0, 0), _mano_d=("h", 2.0, -6.0, 10.0), _arma=(0.05, -0.75, 0.66), _agarra=-5)
    accion("guadana_tajo", 1.7, [
        (0.00, G),
        (0.55, G_ALTO),
        (0.80, con(G_ALTO, raiz=raiz(-0.2, 2.0), torso=(-14, 12, 0), cabeza=(-26, -10, 0))),
        (0.88, con(G, raiz=raiz(-1.8, -1.0), torso=(18, 0, 0), cabeza=(-16, 0, 0), _mano_d=("h", 1.5, 4.0, 10.0),
                   _arma=(0.05, 0.25, 1), _agarra=-4)),
        (0.94, G_ABAJO),
        (1.25, con(G_ABAJO, torso=(50, -6, 0))),
        (1.70, G),
    ], golpes=(0.92,))
    # ---- el cuchillo: dos tajos rapidos, de ida y de vuelta (el farol sigue colgando)
    F_FUERA = con(F, raiz=raiz(-1.4, 0.6), torso=(18, 42, 0), cabeza=(-10, -36, 0), _mano_d=("h", -7.0, -1.0, 4.5), _arma=(-1, 0.3, 0.1))
    F_DENTRO = con(F, raiz=raiz(-2.4, -2.0), torso=(28, -46, 0), cabeza=(-16, 38, 0), muslo_i=(-34, 0, -4), pierna_i=(32, 0, 0),
                   _mano_d=("h", 8.0, -2.0, 5.5), _arma=(1, 0.1, 0.2))
    F_MEDIO = con(F, raiz=raiz(-2.0, -0.8), torso=(24, 0, 0), cabeza=(-14, 0, 0), _mano_d=("h", 1.5, -1.0, 11.0), _arma=(0, 0.2, 1))
    accion("farol_tajos", 1.5, [
        (0.00, F),
        (0.26, F_FUERA),
        (0.36, F_MEDIO),
        (0.44, F_DENTRO),
        (0.66, con(F_MEDIO, raiz=raiz(-2.4, -2.6))),
        (0.74, con(F_FUERA, raiz=raiz(-2.6, -3.2), torso=(26, 40, 0))),
        (1.00, con(F_FUERA, raiz=raiz(-2.4, -3.0), torso=(24, 44, 0))),
        (1.50, F),
    ], golpes=(0.36, 0.66))
    # ---- el farol: lo voltea por encima de la cabeza y lo estrella delante (estalla en fuego de almas)
    F_ALTO = con(F, raiz=raiz(-0.6, 1.4), torso=(-6, -20, 0), cabeza=(-24, 14, 0),
                 _mano_i=("h", -1.0, 12.0, -1.0), _arma_i=(0, 0.6, -0.8))
    F_ESTRELLA = con(F, raiz=raiz(-3.6, -2.6), torso=(50, 16, 0), cabeza=(-30, -10, 0), muslo_i=(-50, 0, -4), pierna_i=(52, 0, 0),
                     _mano_i=("h", -2.0, -7.0, 10.0), _arma_i=(0, -0.6, 1))
    accion("farol_golpe", 1.9, [
        (0.00, F),
        (0.50, F_ALTO),
        (0.80, con(F_ALTO, raiz=raiz(-0.4, 1.6), torso=(-10, -22, 0))),
        (0.92, con(F, raiz=raiz(-2.0, -0.6), torso=(20, 0, 0), _mano_i=("h", -1.0, 4.0, 10.0), _arma_i=(0, 0.2, 1))),
        (1.00, F_ESTRELLA),
        (1.40, con(F_ESTRELLA, torso=(48, 16, 0))),
        (1.90, F),
    ], golpes=(0.98,))
    # ---- el baculo: de arriba abajo, con las dos manos
    C_ALTO = con(C, raiz=raiz(-0.4, 1.4), torso=(-8, 10, 0), cabeza=(-22, -8, 0), brazo_i=(-160, 0, -12), antebrazo_i=(-40, 0, 0),
                 _mano_d=("h", 1.5, 11.0, -1.0), _arma=(0, 0.7, -0.7))
    C_ABAJO = con(C, raiz=raiz(-3.4, -2.6), torso=(46, -8, 0), cabeza=(-30, 6, 0), muslo_i=(-50, 0, -4), pierna_i=(52, 0, 0),
                  brazo_i=(-50, 0, -20), antebrazo_i=(-30, 0, 0), _mano_d=("h", 3.0, -6.0, 9.5), _arma=(0, -0.5, 1))
    accion("campana_golpe", 1.6, [
        (0.00, C),
        (0.48, C_ALTO),
        (0.70, con(C_ALTO, raiz=raiz(-0.2, 1.6), torso=(-12, 10, 0))),
        (0.76, con(C, raiz=raiz(-1.6, -1.0), torso=(16, 0, 0), _mano_d=("h", 2.0, 4.0, 10.5), _arma=(0, 0.6, 1))),
        (0.82, C_ABAJO),
        (1.10, con(C_ABAJO, torso=(44, -8, 0))),
        (1.60, C),
    ], golpes=(0.80,))
    # ---- la campana: alza el baculo y lo sacude (cada sacudida, una campanada: acuden los de alrededor)
    C_TOCA = con(C, raiz=raiz(-0.6, 0.6), torso=(-4, 0, 0), cabeza=(-30, 0, 0), brazo_i=(-120, 0, -20), antebrazo_i=(-30, 0, 0),
                 _mano_d=("h", 0.5, 12.5, 2.0), _arma=(0, 1, 0.05))

    def sacude(k):
        return con(C_TOCA, torso=(-4, 8 * k, 4 * k), _mano_d=("h", 0.5 + 1.6 * k, 12.0, 2.0), _arma=(0.35 * k, 1, 0.05))
    accion("campana_tocar", 2.2, [
        (0.00, C),
        (0.40, C_TOCA),
        (0.55, sacude(1)),
        (0.70, sacude(-1)),
        (0.95, sacude(1)),
        (1.10, sacude(-1)),
        (1.35, sacude(1)),
        (1.50, sacude(-1)),
        (1.70, C_TOCA),
        (2.20, C),
    ], golpes=(0.55, 0.95, 1.35))
    # ---- aturdido
    AT = dict(raiz=raiz(-1.8, 2.2), torso=(-10, 0, 8), cabeza=(-24, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(28, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(22, 0, 0), brazo_d=(18, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(20, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.18, AT),
        (0.60, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.2, 2.8))),
        (1.00, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-2.0, 2.4))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- morir: se queda de rodillas, la cabeza atras, y se derrumba de lado deshaciendose en ceniza
    RODILLAS = dict(raiz=raiz(-10.4, 0.6), muslo_d=(-10, 0, 6), pierna_d=(102, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0),
                    torso=(-6, 0, 4), cabeza=(-40, 0, 8), brazo_d=(6, 0, 30), antebrazo_d=(-10, 0, 0), mano_d=(70, 0, 0),
                    brazo_i=(8, 0, -30), antebrazo_i=(-12, 0, 0))
    CAIDO = con(TUMBADO, cabeza=(-4, 22, 0), brazo_i=(-30, 0, -40), _mano_d=("h", -6.0, -0.6, 5.0), _arma=(-0.8, 0.0, 1))
    accion("morir", 1.9, [
        (0.00, con(BASE, torso=(-6, 0, 6), cabeza=(-30, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0),
                   brazo_i=(10, 0, -30))),
        (0.50, RODILLAS),
        (0.95, con(RODILLAS, raiz=raiz(-10.6, 0.2), torso=(-12, 0, 2), cabeza=(-50, 0, 6))),
        (1.35, CAIDO),
        (1.90, CAIDO),
    ])
    DE_PIE = con(GUARDIA, brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0), brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    MEDIO = dict(raiz=raiz(-5.4, 0.4), muslo_d=(-60, 0, 6), pierna_d=(84, 0, 0), muslo_i=(0, 0, -6), pierna_i=(70, 0, 0),
                 torso=(36, 10, 0), cabeza=(-18, 10, 0), brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0),
                 brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    accion("levantarse", 1.4, [
        (0.00, REZA),
        (0.40, con(REZA, torso=(16, 30, 0), cabeza=(-10, 40, 0), brazo_d=(-10, 0, 10), antebrazo_d=(-40, 0, 0), mano_d=(40, 0, 0),
                   brazo_i=(-10, 0, -10), antebrazo_i=(-30, 0, 0))),
        (0.85, MEDIO),
        (1.40, DE_PIE),
    ])
    accion("alzarse", 2.0, [
        (0.00, TUMBADO),
        (0.45, con(TUMBADO, cabeza=(-30, 10, 0), brazo_i=(-70, 0, -30), antebrazo_i=(-90, 0, 0), _mano_d=("h", -4.0, -0.8, 5.0), _arma=(-0.4, 0.0, 1))),
        (0.95, dict(raiz=raiz(-6.0, 6.0, rot=(50, 0, 0)), torso=(-6, 0, 0), cabeza=(-40, 0, 0),
                    muslo_d=(-40, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(96, 0, 0),
                    brazo_d=(-96, 0, 14), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(-92, 0, -14), antebrazo_i=(-20, 0, 0))),
        (1.45, con(MEDIO, raiz=raiz(-6.0, 0.0), torso=(40, 0, 0), cabeza=(-12, 0, 0))),
        (2.00, DE_PIE),
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
        return [anims["quieto"], anims["porte_guadana"]]
    if nombre == "quieto":
        return [anims["porte_guadana"]]
    return []
