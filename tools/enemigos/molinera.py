"""
La Molinera de Velmor (parte 7f): el jefe de las Llanuras Palidas. La molinera del libro del campanero ("sigue en su
campo, segando lo que ya no crece"), crecida por la Sombra hasta el doble de alto: una vieja enorme y encorvada, de
ceniza agrietada con brasas, la saya larga y el delantal blanqueados de harina, el chal rojo oscuro, el panuelo negro
y una guadana del doble de larga que la de los marchitos.

Una sola variante. Acciones: siega (ancha, a ras de suelo), tajo (de arriba abajo, con onda), giro (dos vueltas
avanzando), estocada (embiste con la punta), pisoton (a quien se le pone detras), grito (al empezar y a media vida:
llama a los marchitos), aturdido, morir, levantarse (estaba de rodillas ante la piedra del molino).
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (brillo_de, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,  # noqa: F401
                   sombra, tela)

NOMBRE = "molinera"
VARIANTES = ("molinera",)
ESCALA = 1.9

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # grande y huesuda: brazos largos, el torso ancho de hombros
    humanoide(r, torso=(9, 14, 5), cabeza=(8, 8, 8), brazo=(4, 16, 4), pierna=(4, 13, 4))
    # la saya: de la cintura hasta los tobillos (las piernas casi no se ven), rota por abajo
    r.pieza("saya", "cadera", (0, 0, 0), boxes=[Box(-5.5, -0.5, -3.5, 11, 13, 7, "saya", grow=g(0.5))])
    # el delantal, por delante de la saya
    r.pieza("delantal", "cadera", (0, 0, 0), boxes=[Box(-3.5, -0.2, -4.2, 7, 11, 0, "delantal")])
    # el chal sobre los hombros, con su pico por la espalda
    r.pieza("chal", "torso", (0, -14, 0), boxes=[
        Box(-5.5, -0.5, -3.0, 11, 5, 6, "chal", grow=g(0.4)),
        Box(-3.5, 4.0, 2.6, 7, 6, 1, "chal_pico"),
    ])
    # el panuelo negro atado a la nuca
    r.pieza("panuelo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 6, 8, "panuelo", grow=g(0.55)),
        Box(-1.5, -4.5, 4.2, 3, 2, 2, "nudo"),
    ])
    # ---- la guadana grande (apunta a -Z con el brazo caido)
    r.pieza("guadana", "mano_d", (0, 0, 0), boxes=[
        Box(-0.75, -0.75, -32, 1.5, 1.5, 20, "asta"),
        Box(-0.75, -0.75, -12, 1.5, 1.5, 22, "asta"),
        Box(-2.0, -0.75, -16, 3, 1.5, 1.5, "puno"),
        Box(-1.0, -1.0, -33.5, 2, 2, 2, "virola"),
        # la hoja: un plano de canto que cuelga del extremo (la media luna pintada)
        Box(0, 0, -37, 0, 14, 20, "hoja_guadana"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

PIEL = ["#2e2b29", "#45413d", "#5d5852", "#767069", "#8f8880", "#aaa298"]
GRIETA = "#1f1612"
BRASA = ["#5a1408", "#8a2410", "#c0401a", "#f07a2a", "#ffc070"]
CUENCA = "#0a0707"
SAYA = ["#231c16", "#30261d", "#3e3125", "#4c3c2d", "#5b4836"]
HARINA = ["#b8b2a2", "#cbc5b4", "#dcd7c8", "#ebe7da"]
CHAL = ["#2c0c0c", "#3e1312", "#511a18", "#66221f", "#7a2a26"]
PANUELO = ["#121010", "#1c1918", "#272321", "#332e2b", "#3f3935"]
PALO = ["#241a12", "#33261a", "#443323", "#57412d", "#6b5038"]
HIERRO = ["#1f1f23", "#33333a", "#4a4a53", "#66666f", "#86868f", "#a8a8b2"]


def _grietas(semilla):
    """Las grietas de la piel: lineas finas que se ramifican (True donde hay grieta)."""
    import random
    r = random.Random(semilla)
    puntos = set()
    for _ in range(7):
        x, y = r.randrange(0, 16), r.randrange(0, 16)
        for _ in range(r.randrange(4, 10)):
            puntos.add((x, y))
            x += r.choice((-1, 0, 1))
            y += 1 if r.random() < 0.7 else 0
    return puntos


def _cara():
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": CUENCA, "k": "#1a1311", "M": "#120c0a", "G": GRIETA,
         "T": "#8a8070", "D": "#6a6258"}
    filas = [
        "OossssoO",
        "oSsGSsSo",
        "oSSsSSso",
        "OKKosKKO",
        "OKkoskKO",
        "OossSsoO",
        "OoMMMMoO",
        "OODoToDO",
    ]
    return dibujo(filas, C)


def materiales(variante):
    piel_base = ruido(PIEL[1:5], 17, grano=0.5, base=0.5)
    grietas = _grietas(91)

    def piel(cara, x, y, w, h):
        if ((x * 3 + y * 5 + len(cara)) % 16, (y * 2 + x) % 16) in grietas:
            return GRIETA
        return piel_base(cara, x, y, w, h)

    saya_t = tela(SAYA, 61, pliegue=3)
    harina = ruido(HARINA, 62, grano=0.7, base=0.4)

    def enharinado(c, cara, x, y, w, h, cuanto):
        # manchas de harina por encima (mas en lo de abajo y por delante)
        k = (x * 7 + y * 13 + len(cara) * 5) % 11
        if k < cuanto:
            return mezcla(c, harina(cara, x, y, w, h), 0.55)
        return c

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara()(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara == "arriba" and (x * 2 + y) % 5 == 0:
            return GRIETA
        return c

    def torso(cara, x, y, w, h):
        c = saya_t(cara, x, y, w, h)
        if cara == "frente":
            # el corpino abierto: el pecho hundido y gris
            if y <= 5 and abs(x - 4) <= 1.5 - y * 0.2:
                return PIEL[3] if y % 2 == 0 else PIEL[1]
            if y in (11, 12):
                return CHAL[1 + (x % 2)]          # el cordon de la cintura
        return enharinado(c, cara, x, y, w, h, 1)

    def saya(cara, x, y, w, h):
        corte = (11, 12, 10, 12, 11, 9, 12, 11, 10, 12, 11)[(x * 3 + {"frente": 0, "espalda": 4, "der": 2, "izq": 7}.get(cara, 1)) % 11]
        if cara in ("arriba", "abajo") or y > corte:
            return None
        c = saya_t(cara, x, y + 3, w, h + 3)
        c = oscuro(c, 0.04 + 0.025 * y)
        return enharinado(c, cara, x, y, w, h, 1 if y < 8 else 2)

    def delantal(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return None
        if y >= h - 1 and (x * 5) % 3 == 0:
            return None
        c = harina(cara, x, y, w, h)
        if (x + y * 3) % 7 == 0:
            return oscuro(c, 0.25)                # los rotos y lo sucio
        return c

    def brazo(cara, x, y, w, h):
        if y <= 5:
            return enharinado(saya_t(cara, x, y, w, h), cara, x, y, w, h, 3)   # la manga
        return piel(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return PIEL[1] if cara != "abajo" else PIEL[2]   # los dedos largos y nudosos
        return piel(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return oscuro(saya_t(cara, x, y, w, h), 0.25)

    def pierna(cara, x, y, w, h):
        if y >= h - 2:
            return "#1c1512"                       # los zuecos
        return piel(cara, x, y, w, h)

    def chal(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = tela(CHAL, 63, pliegue=2)(cara, x, y, w, h)
        if cara == "frente" and 3 <= x <= w - 4 and y >= 1:
            return None                            # abierto por delante
        if y == h - 1 and (x * 7) % 3 == 0:
            return None                            # los flecos
        return c

    def chal_pico(cara, x, y, w, h):
        if cara not in ("espalda", "frente"):
            return tela(CHAL, 64, pliegue=2)(cara, x, y, w, h)
        # un triangulo hacia abajo
        if abs(x - (w - 1) / 2) > (h - y) * 0.6:
            return None
        return tela(CHAL, 64, pliegue=2)(cara, x, y, w, h)

    def panuelo(cara, x, y, w, h):
        t = tela(PANUELO, 65, pliegue=3)(cara, x, y, w, h)
        if cara == "frente":
            if 1 <= x <= w - 2 and 2 <= y:
                return None                        # la cara
        if cara == "abajo":
            return None
        if cara in ("der", "izq") and y >= h - 2 and delante(cara, x, w) <= 1:
            return None
        return t

    C = {"H": HIERRO[4], "h": HIERRO[3], "d": HIERRO[2], "o": HIERRO[1], "x": "#3b2418", "R": "#7a2a1a"}
    hoja_guadana = dibujo([
        # de costado (el frente a la izquierda): la hoja larga en media luna, con muescas y oxido
        "HHh.................",
        "dHHh................",
        "odHHHh..............",
        ".odHHHh.............",
        "..odhHHHh...........",
        "...oddhHHHh.........",
        "....oRddhHHHh.......",
        "......oddhRHHHh.....",
        "........ooddhHHHh...",
        "..........oodd.HHHh.",
        ".............ooddhHH",
        "...............oddH.",
        ".................oo.",
        "....................",
    ], C)

    mats = {
        "cabeza": sombra(cabeza, abajo=0.18),
        "torso": sombra(torso),
        "saya": saya,
        "delantal": delantal,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.18), "antebrazo_i": sombra(antebrazo, abajo=0.18),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.25), "pierna_i": sombra(pierna, abajo=0.25),
        "chal": chal, "chal_pico": chal_pico,
        "panuelo": panuelo, "nudo": tela(PANUELO, 66, pliegue=2),
        "asta": madera(PALO, 71, "z"),
        "puno": madera(PALO, 72, "x"),
        "virola": metal(HIERRO[1:4], 73),
        "hoja_guadana": por_cara(liso(None), izq=hoja_guadana, der=espejado(hoja_guadana)),
    }
    return mats


def brillo(variante):
    """Lo que brilla: los rescoldos de las cuencas y las brasas de las grietas."""
    C = {"R": BRASA[3], "r": BRASA[2], "Y": BRASA[4]}
    ojos = dibujo([
        "........",
        "........",
        "........",
        ".rRY.RYr",
        "..rr.rr.",
    ], C)
    grietas = _grietas(91)

    def brasas(cara, x, y, w, h):
        k = ((x * 3 + y * 5 + len(cara)) % 16, (y * 2 + x) % 16)
        if k in grietas and (x + y * 3) % 3 == 0:
            return BRASA[2] if (x + y) % 3 else BRASA[3]
        return None

    return {"cabeza": por_cara(liso(None), frente=ojos), "brazo_d": brasas, "brazo_i": brasas, "antebrazo_d": brasas,
            "antebrazo_i": brasas, "pierna_d": brasas, "pierna_i": brasas}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 16)

# de pie, encorvada y pesada: la cabeza adelantada, las rodillas algo dobladas
BASE = dict(raiz=raiz(-0.6), torso=(18, 0, 2), cabeza=(-6, 0, -3),
            muslo_d=(-6, 0, 3), pierna_d=(10, 0, 0), muslo_i=(0, 0, -3), pierna_i=(8, 0, 0),
            brazo_i=(-8, 0, -6), antebrazo_i=(-12, 0, 0), saya=(8, 0, 0), delantal=(8, 0, 0))
# la guadana al hombro, la hoja colgando por detras
PORTE = dict(_mano_d=("h", 0.0, -4.0, 6.0), _arma=(-0.05, 0.55, -0.83))
GUARDIA = dict(raiz=raiz(-1.6), torso=(24, 10, 0), cabeza=(-12, -10, 0), saya=(12, 0, 0), delantal=(12, 0, 0),
               muslo_d=(6, 0, 6), pierna_d=(20, 0, 0), muslo_i=(-24, 0, -6), pierna_i=(28, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", 2.5, -11.0, 3.0), _arma=(0.35, 0.25, 1), _agarra=-6)
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

    # ---- quieta: respira hondo, la cabeza ladeada
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (1.7, con(BASE, torso=(21, 3, 4), cabeza=(-2, 8, -10), raiz=raiz(-0.9), brazo_i=(-5, 0, -8))),
        (3.4, con(BASE, torso=(16, -3, 0), cabeza=(-9, -6, 2), raiz=raiz(-0.4), brazo_i=(-10, 0, -5))),
        (5.0, BASE),
    ])
    # ---- andar: pasos pesados, la saya se mece
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-26, 20, 8, 22, 6, 0.0), (-4, 4, 6, 40, 0, 1.2), (20, -26, 22, 8, -6, 0.0), (4, -4, 40, 6, 0, 1.2), (-26, 20, 8, 22, 6, 0.0))):
        pasos.append((1.5 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, -ty * 0.5), "cabeza": (0, -ty * 0.6, ty * 0.6), "raiz": raiz(up),
                                    "saya": (-md * 0.15, 0, -ty * 0.4), "delantal": (-md * 0.3, 0, 0)}))
    bucle("andar", 1.5, pasos)
    q = con(BASE, PORTE)
    bucle("porte_molinera", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    bucle("listo_molinera", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-2.2), torso=(LISTO["torso"][0] + 4, LISTO["torso"][1] - 4, 0), cabeza=(-9, -8, 3))),
        (2.0, LISTO),
    ])
    # ---- de rodillas ante la piedra del molino, las manos sobre ella (la guadana tumbada al lado)
    REZA = dict(raiz=raiz(-9.6, 1.2), muslo_d=(-10, 0, 6), pierna_d=(100, 0, 0), muslo_i=(-10, 0, -6), pierna_i=(100, 0, 0),
                torso=(38, 0, 0), cabeza=(24, 0, 0), saya=(-14, 0, 0), delantal=(-30, 0, 0),
                _mano_d=("h", -1.0, -9.0, 9.0), _arma=(-1, -0.1, 0.1), brazo_i=(-60, 0, 14), antebrazo_i=(-40, 0, 0))
    bucle("rezar", 4.4, [
        (0.0, REZA),
        (2.2, con(REZA, torso=(46, 0, 0), cabeza=(32, 0, 0), raiz=raiz(-10.0, 0.6), brazo_i=(-70, 0, 14))),
        (4.4, REZA),
    ])

    # ================================================================ acciones
    G = LISTO

    # ---- 1 siega: ancha, de derecha a izquierda, a ras de suelo
    S_ATRAS = con(sin(G, "_agarra"), raiz=raiz(-1.4, 1.6), torso=(14, 60, 0), cabeza=(-8, -50, 0), muslo_d=(16, 0, 6), pierna_d=(22, 0, 0),
                  brazo_i=(-70, 0, 30), antebrazo_i=(-40, 0, 0),
                  _mano_d=("h", -3.5, -10.0, -3.5), _arma=(-1, -0.15, -0.2))
    S_DELANTE = con(sin(G, "_agarra"), raiz=raiz(-2.6, -1.8), torso=(30, -4, 0), cabeza=(-18, 2, 0), muslo_i=(-36, 0, -6), pierna_i=(34, 0, 0),
                    brazo_i=(-50, 0, -30), antebrazo_i=(-20, 0, 0), _mano_d=("h", 1.5, -9.0, 10.5), _arma=(0.1, -0.35, 1))
    S_FIN = con(sin(G, "_agarra"), raiz=raiz(-2.4, -2.6), torso=(26, -58, 0), cabeza=(-14, 46, 0), muslo_i=(-38, 0, -6), pierna_i=(40, 0, 0),
                brazo_i=(-30, 0, -30), antebrazo_i=(-30, 0, 0),
                _mano_d=("h", 7.0, -9.5, 7.0), _arma=(1, -0.3, 0.2))
    accion("siega", 2.0, [
        (0.00, G),
        (0.66, S_ATRAS),
        (0.92, con(S_ATRAS, torso=(14, 66, 0), cabeza=(-8, -54, 0))),
        (1.02, S_DELANTE),
        (1.12, S_FIN),
        (1.50, con(S_FIN, torso=(24, -62, 0))),
        (2.00, G),
    ], golpes=(1.04,))
    # ---- 2 tajo: la alza por encima de la cabeza y la clava delante (la onda corre por el suelo)
    T_ALTO = con(sin(G, "_agarra"), raiz=raiz(-0.2, 2.0), torso=(-12, 12, 0), cabeza=(-26, -10, 0), muslo_d=(12, 0, 4), pierna_d=(10, 0, 0),
                 muslo_i=(-16, 0, -4), pierna_i=(12, 0, 0), brazo_i=(-172, 0, -24), antebrazo_i=(-14, 0, 0),
                 _mano_d=("h", 1.0, 12.5, -2.5), _arma=(0.05, 0.6, -0.8))
    T_ABAJO = con(sin(G, "_agarra"), raiz=raiz(-4.0, -3.4), torso=(54, -6, 0), cabeza=(-34, 4, 0), muslo_d=(18, 0, 4), pierna_d=(34, 0, 0),
                  muslo_i=(-56, 0, -4), pierna_i=(58, 0, 0), brazo_i=(-62, 0, -26), antebrazo_i=(-26, 0, 0),
                  _mano_d=("h", 2.0, -7.0, 11.5), _arma=(0.05, -0.75, 0.66))
    accion("tajo", 2.1, [
        (0.00, G),
        (0.65, T_ALTO),
        (0.95, con(T_ALTO, raiz=raiz(0.0, 2.2), torso=(-16, 12, 0), cabeza=(-28, -10, 0))),
        (1.03, con(sin(G, "_agarra"), raiz=raiz(-1.8, -1.0), torso=(18, 0, 0), cabeza=(-16, 0, 0), brazo_i=(-120, 0, -24),
                   antebrazo_i=(-16, 0, 0), _mano_d=("h", 1.5, 5.0, 11.5), _arma=(0.05, 0.25, 1))),
        (1.10, T_ABAJO),
        (1.55, con(T_ABAJO, torso=(52, -6, 0))),
        (2.10, G),
    ], golpes=(1.08,))
    # ---- 3 giro: dos vueltas enteras con la guadana tendida (avanza mientras gira; lo mueve el servidor)
    GIRO = con(sin(G, "_agarra"), raiz=raiz(-1.8), torso=(20, 0, 0), cabeza=(-10, 0, 0), brazo_i=(-80, 0, -40), antebrazo_i=(-20, 0, 0),
               _mano_d=("h", 4.0, -2.0, 11.0), _arma=(1, -0.1, 0.6))
    vueltas = [(0.00, G), (0.55, con(S_ATRAS, torso=(16, 50, 0)))]
    for k in range(9):
        t = 0.75 + k * 0.18
        vueltas.append((t, con(GIRO, raiz=raiz(-1.8, rot=(0, -90 * k - 90, 0)))))
    vueltas += [(2.40, con(GIRO, raiz=raiz(-2.0, rot=(0, -720, 0)))), (2.80, G)]
    accion("giro", 2.8, vueltas, golpes=(1.11, 1.83))
    # ---- 4 estocada: echa atras la guadana y embiste con la punta (cruza media era)
    E_ATRAS = con(sin(G, "_agarra"), raiz=raiz(-1.4, 2.4), torso=(6, 30, 0), cabeza=(-6, -26, 0), muslo_d=(20, 0, 4), pierna_d=(28, 0, 0),
                  brazo_i=(-90, 0, -10), antebrazo_i=(-30, 0, 0), _mano_d=("h", -2.0, -2.0, -6.0), _arma=(0, 0.1, 1))
    E_FONDO = con(sin(G, "_agarra"), raiz=raiz(-3.4, -4.4), torso=(44, -10, 0), cabeza=(-30, 6, 0), muslo_d=(30, 0, 4), pierna_d=(10, 0, 0),
                  muslo_i=(-60, 0, -4), pierna_i=(64, 0, 0), brazo_i=(-110, 0, -20), antebrazo_i=(-20, 0, 0),
                  _mano_d=("h", 1.0, -1.0, 15.0), _arma=(0, -0.05, 1))
    accion("estocada", 1.8, [
        (0.00, G),
        (0.55, E_ATRAS),
        (0.80, con(E_ATRAS, raiz=raiz(-1.2, 2.8))),
        (0.88, E_FONDO),
        (1.25, con(E_FONDO, torso=(42, -10, 0))),
        (1.80, G),
    ], golpes=(0.90,))
    # ---- 5 pisoton: a quien se le pone detras, se gira de golpe y patea con el zueco
    P_ALZA = con(G, raiz=raiz(-0.6, 0.0, rot=(0, 60, 0)), torso=(10, 30, 0), muslo_d=(-70, 0, 10), pierna_d=(80, 0, 0))
    P_PISA = con(G, raiz=raiz(-2.4, -0.6, rot=(0, 160, 0)), torso=(30, 20, 0), muslo_d=(10, 0, 8), pierna_d=(16, 0, 0),
                 muslo_i=(-30, 0, -6), pierna_i=(40, 0, 0))
    accion("pisoton", 1.3, [
        (0.00, G),
        (0.36, P_ALZA),
        (0.56, P_PISA),
        (0.90, con(P_PISA, raiz=raiz(-2.2, -0.6, rot=(0, 170, 0)))),
        (1.30, G),
    ], golpes=(0.57,))
    # ---- 6 grito: echa la cabeza atras, abre los brazos y chilla (llama a los marchitos)
    GR = con(BASE, raiz=raiz(-0.4, 1.2), torso=(-18, 0, 0), cabeza=(-46, 0, 0), brazo_i=(-30, 0, -70), antebrazo_i=(-20, 0, 0),
             _mano_d=("h", 9.0, 2.0, 6.0), _arma=(0.2, 1, 0.1))
    accion("grito", 2.2, [
        (0.00, G),
        (0.40, con(GR, torso=(30, 0, 0), cabeza=(10, 0, 0), raiz=raiz(-2.4))),
        (0.80, GR),
        (1.20, con(GR, torso=(-22, 0, 4), cabeza=(-50, 0, 6))),
        (1.60, con(GR, torso=(-16, 0, -4), cabeza=(-44, 0, -6))),
        (2.20, G),
    ])
    # ---- 7 aturdido
    AT = dict(raiz=raiz(-1.8, 2.4), torso=(-10, 0, 8), cabeza=(-24, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(26, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(20, 0, 0), brazo_d=(18, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(20, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.2, 3.0))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-2.0, 2.6))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 8 morir: cae de rodillas, se apoya en la guadana y se desploma hacia delante deshaciendose en ceniza
    RODILLAS = dict(raiz=raiz(-9.6, 0.8), muslo_d=(-10, 0, 6), pierna_d=(102, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0),
                    torso=(-4, 0, 4), cabeza=(-36, 0, 8), saya=(-14, 0, 0), delantal=(-30, 0, 0),
                    _mano_d=("h", 2.0, -6.0, 8.0), _arma=(0.1, 1, 0.2), brazo_i=(8, 0, -30), antebrazo_i=(-12, 0, 0))
    CAIDA = dict(raiz=raiz(-6.0, 9.0, rot=(70, 0, 0)), torso=(10, 0, 0), cabeza=(-20, 20, 0), muslo_d=(-40, 0, 6), pierna_d=(80, 0, 0),
                 muslo_i=(-36, 0, -6), pierna_i=(80, 0, 0), saya=(-20, 0, 0), delantal=(-20, 0, 0),
                 _mano_d=("h", 4.0, -2.0, 10.0), _arma=(0.6, -0.2, 0.8), brazo_i=(-120, 0, -30), antebrazo_i=(-10, 0, 0))
    accion("morir", 3.0, [
        (0.00, con(BASE, torso=(-6, 0, 6), cabeza=(-30, 0, 10), brazo_i=(10, 0, -30), _mano_d=("h", 4.0, -10.0, 4.0), _arma=(0.2, -1, 0.2))),
        (0.70, RODILLAS),
        (1.60, con(RODILLAS, raiz=raiz(-9.8, 0.4), torso=(6, 0, 2), cabeza=(-20, 0, 6))),
        (2.30, CAIDA),
        (3.00, CAIDA),
    ])
    # ---- 9 levantarse: estaba de rodillas ante la piedra; se pone en pie con la guadana
    MEDIO = dict(raiz=raiz(-5.0, 0.4), muslo_d=(-56, 0, 6), pierna_d=(80, 0, 0), muslo_i=(0, 0, -6), pierna_i=(66, 0, 0),
                 torso=(34, 10, 0), cabeza=(-16, 10, 0), saya=(-6, 0, 0), delantal=(-10, 0, 0),
                 _mano_d=("h", 2.0, -8.0, 8.0), _arma=(0.2, 0.4, 0.9), brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    accion("levantarse", 1.8, [
        (0.00, REZA),
        (0.50, con(REZA, torso=(14, 30, 0), cabeza=(-10, 36, 0), _mano_d=("h", 1.0, -8.0, 8.0), _arma=(0.2, 0.2, 1))),
        (1.10, MEDIO),
        (1.80, G),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_molinera"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_molinera"]]
    if nombre == "quieto":
        return [anims["porte_molinera"]]
    return []
