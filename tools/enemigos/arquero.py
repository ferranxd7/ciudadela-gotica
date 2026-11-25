"""
El arquero esqueleto de Valdrenn (parte 4a): los arqueros de la guardia del Rey, que se quedaron en las murallas y en
las torres hasta que no les quedo carne. Huesos amarillentos, la capucha y la capa raidas, la aljaba a la espalda y el
arco largo en la izquierda. Dispara desde arriba (se esquiva de lado o rodando) y a quien se le acerca le da una
patada. Una sola variante.
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, madera, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "arquero"
VARIANTES = ("arco",)
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # huesos: brazos y piernas finos; el torso es la caja de las costillas (con huecos en la textura)
    humanoide(r, torso=(8, 12, 4), brazo=(2, 12, 2), pierna=(2, 12, 2), hombro=0.0)
    # la columna, que se ve entre las costillas, y la pelvis
    r.pieza("columna", "torso", (0, 0, 0), boxes=[Box(-1, -12, 0.2, 2, 12, 1.4, "columna")])
    r.pieza("pelvis", "cadera", (0, 0, 0), boxes=[Box(-3.5, -1.5, -1.5, 7, 3, 3, "pelvis")])
    # la mandibula (cuelga un poco) y la capucha con la capa
    r.pieza("mandibula", "cabeza", (0, -1.0, -1.0), boxes=[Box(-3, 0, -3, 6, 2, 4, "mandibula")])
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 8, 8, "capucha", grow=g(0.55)),
        Box(-2.5, -5.0, 4.5, 5, 6, 1.5, "capucha_pico"),
    ])
    r.pieza("capa", "torso", (0, -12, 2.6), rot=(6, 0, 0), boxes=[Box(-4.5, 0, 0, 9, 17, 1, "capa")])
    # la aljaba a la espalda, cruzada, con las plumas asomando por encima del hombro derecho
    r.pieza("aljaba", "torso", (1.5, -6, 3.2), rot=(0, 0, -24), boxes=[
        Box(-1.5, -6, 0, 3, 11, 2.4, "aljaba"),
        Box(-1.2, -8.6, 0.3, 2.4, 2.6, 1.8, "plumas"),
    ])
    # el arco largo en la izquierda: el puno en la mano y las palas hacia arriba y hacia abajo (a lo largo de -Z / +Z
    # de la mano: como la hoja de una espada), algo curvadas hacia delante
    r.pieza("arco", "mano_i", (0, 0, 0), boxes=[
        Box(-0.5, -0.6, -1.5, 1, 1.2, 3, "arco_puno"),
        Box(-0.5, -0.2, -9.5, 1, 1, 8, "arco_pala"),
        Box(-0.5, 0.5, -14.5, 1, 1, 5.2, "arco_pala"),
        Box(-0.5, -0.2, 1.5, 1, 1, 8, "arco_pala"),
        Box(-0.5, 0.5, 9.3, 1, 1, 5.2, "arco_pala"),
        Box(-0.1, 1.45, -14.2, 0.2, 0.2, 28.4, "cuerda"),
    ])
    # la flecha que lleva en la derecha (la que pone en el arco)
    r.pieza("flecha", "mano_d", (0, 0, 0), boxes=[
        Box(-0.25, -0.25, -10.5, 0.5, 0.5, 13, "flecha_asta"),
        Box(-0.5, -0.5, -12.0, 1, 1, 1.6, "flecha_punta"),
        Box(-0.05, -0.9, 1.0, 0.1, 1.8, 2.2, "flecha_pluma"),
    ])
    r.empaquetar()
    return r


OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

HUESO = ["#4a4334", "#6d6450", "#8f8569", "#ada27f", "#c4b994", "#d8cfae"]
HUECO = "#120f0c"
OJO, OJO_B = "#e8402a", "#ffb08a"
TELA = ["#14161a", "#1c1f24", "#262a30", "#30353c", "#3b4149"]
CUERO = ["#17110c", "#241a12", "#33251a", "#443223"]
PALO = ["#22180f", "#33251a", "#463424", "#5a442e"]
PLUMA = ["#5e1b17", "#7d2620"]
HIERRO = ["#2c3034", "#4f565c", "#7b848a"]


def _hueso(semilla):
    return ruido(HUESO[2:6], semilla, grano=0.45, base=0.55)


def _calavera():
    C = {"S": HUESO[4], "s": HUESO[3], "o": HUESO[2], "O": HUESO[1], "K": HUECO, "R": OJO, "N": HUESO[0], "c": HUESO[5]}
    return dibujo([
        "OoscsoO.",
        "osScScso",
        "oSSssSSo",
        "oKKssKKo",
        "oKRsoRKo",
        "OosNNsoO",
        "OsoNNosO",
        "OOoooOOO",
    ], C)


def materiales(variante):
    hueso = _hueso(511)
    hueso_b = _hueso(512)
    tela_t = tela(TELA, 521, pliegue=3, grano=0.12)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            c = _calavera()(cara, x, y, w, h)
            return c if c is not None else HUESO[1]
        c = hueso(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if y in (3, 4) and d in (1, 2):
                return HUECO                                  # la sien hundida
            if y >= 6 and d <= 3:
                return None                                   # sin carrillos: se ve la mandibula
        if cara == "abajo":
            return HUECO
        return c

    def mandibula(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "frente":
            if y == 0:
                return HUESO[5] if x % 2 == 0 else HUECO      # los dientes
            return hueso(cara, x, y, w, h)
        return oscuro(hueso(cara, x, y, w, h), 0.1)

    def torso(cara, x, y, w, h):
        # las costillas: tiras de hueso con huecos; el esternon en el medio
        if cara in ("arriba", "abajo"):
            return hueso(cara, x, y, w, h) if cara == "arriba" else None
        if y >= 9:
            return None                                       # la cintura: solo la columna
        if cara == "frente" and x in (3, 4):
            return claro(hueso(cara, x, y, w, h), 0.05)        # el esternon
        if y % 2 == 1:
            return None
        if cara in ("der", "izq") and y >= 6:
            return None
        return hueso(cara, x, y, w, h)

    def columna(cara, x, y, w, h):
        return HUESO[3] if y % 2 == 0 else HUESO[2]

    def pelvis(cara, x, y, w, h):
        if cara in ("frente", "espalda") and 2 <= x <= 4 and y >= 1:
            return None
        return hueso_b(cara, x, y, w, h)

    def hueso_largo(cara, x, y, w, h):
        # un hueso largo: mas claro en los extremos (las articulaciones)
        c = hueso(cara, x, y, w, h)
        if y == 0 or y == h - 1:
            return claro(c, 0.08)
        return c

    def antebrazo(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if y >= h - 1:
            return HUESO[1]                                   # los dedos
        return c

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente" and 1 <= y and 1 <= x <= 6:
            return None                                       # la cara al aire
        c = tela_t(cara, x, y, w, h)
        if cara == "frente":
            return oscuro(c, 0.2)
        if cara in ("der", "izq") and delante(cara, x, w) == 0:
            return oscuro(c, 0.25)
        # rota por abajo
        if y == h - 1 and (x * 7 + 3) % 5 < 2:
            return None
        return c

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # raida: jirones por abajo y algun agujero
        largo = h - 1 - ((x * 5 + 2) % 4)
        if y > largo:
            return None
        if (x * 3 + y * 7) % 23 == 0:
            return None
        c = tela_t(cara, x, y, w, h)
        return oscuro(c, 0.04 * (y / 2))

    def aljaba(cara, x, y, w, h):
        c = ruido(CUERO, 531, grano=0.4)(cara, x, y, w, h)
        if y in (1, h - 2):
            return HIERRO[1]                                  # los aros
        return c

    def plumas(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return PLUMA[(x + y) % 2]

    def arco_pala(cara, x, y, w, h):
        return madera(PALO, 541, "z")(cara, x, y, w, h)

    return {
        "cabeza": sombra(cabeza, abajo=0.12), "mandibula": mandibula,
        "torso": torso, "columna": columna, "pelvis": sombra(pelvis, abajo=0.15),
        "brazo_d": hueso_largo, "brazo_i": hueso_largo, "antebrazo_d": antebrazo, "antebrazo_i": antebrazo,
        "muslo_d": hueso_largo, "muslo_i": hueso_largo, "pierna_d": sombra(hueso_largo, abajo=0.1), "pierna_i": sombra(hueso_largo, abajo=0.1),
        "capucha": sombra(capucha, abajo=0.16), "capucha_pico": sombra(tela_t, abajo=0.2), "capa": capa,
        "aljaba": sombra(aljaba, abajo=0.12), "plumas": plumas,
        "arco_puno": ruido(CUERO, 542, grano=0.4), "arco_pala": arco_pala, "cuerda": liso("#b8ad90"),
        "flecha_asta": liso(PALO[3]), "flecha_punta": liso(HIERRO[2]), "flecha_pluma": liso(PLUMA[1]),
    }


def brillo(variante):
    """Lo que brilla: las brasas de los ojos."""
    C = {"R": OJO}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "........",
        "..R..R..",
    ], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 9)

BASE = dict(raiz=raiz(-0.3), torso=(6, 0, 0), cabeza=(10, 0, 0), mandibula=(8, 0, 0),
            muslo_d=(0, 0, 3), pierna_d=(4, 0, 0), muslo_i=(0, 0, -3), pierna_i=(4, 0, 0))
# el arco colgando de la izquierda, la flecha en la derecha, apuntando al suelo
PORTE = dict(_mano_i=("h", 0.8, -8.4, 1.6), _arma_i=(0.12, -0.55, 0.82),
             _mano_d=("h", -0.8, -8.4, 1.0), _arma=(0.1, -0.9, 0.4))
GUARDIA = dict(raiz=raiz(-0.8), torso=(10, -10, 0), cabeza=(-2, 10, 0), mandibula=(10, 0, 0),
               muslo_d=(8, 0, 5), pierna_d=(12, 0, 0), muslo_i=(-14, 0, -5), pierna_i=(16, 0, 0))
# el arco a media altura, la flecha puesta
LISTO = con(GUARDIA, _mano_i=("h", 1.6, -5.2, 5.4), _arma_i=(0.0, 0.9, 0.45),
            _mano_d=("h", 3.4, -4.2, 4.0), _arma=(0.1, 0.25, 1.0))
# tensando: de perfil (el hombro izquierdo hacia el enemigo), el arco a la altura de los ojos, la cuerda a la barbilla
TENSA = con(GUARDIA, raiz=raiz(-1.0), torso=(2, -62, 0), cabeza=(0, 58, 0), mandibula=(6, 0, 0),
            muslo_d=(6, 0, 6), pierna_d=(8, 0, 0), muslo_i=(-10, 0, -6), pierna_i=(10, 0, 0),
            _mano_i=("h", 0.0, 0.2, 8.8), _arma_i=(0.0, 1.0, 0.0),
            _mano_d=("h", 7.4, 0.8, 1.6), _arma=(-0.98, 0.0, 0.2))


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

    BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")

    # ---- quieto: se balancea un poco, la mandibula le cuelga y se le mueve
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (1.3, con(BASE, raiz=raiz(-0.5), torso=(8, 3, 0), cabeza=(12, -12, 4), mandibula=(14, 0, 0))),
        (2.5, con(BASE, raiz=raiz(-0.2), torso=(5, 0, 0), cabeza=(8, 0, 0), mandibula=(6, 0, 0))),
        (3.8, con(BASE, raiz=raiz(-0.5), torso=(8, -3, 0), cabeza=(12, 14, -4), mandibula=(12, 0, 0))),
        (5.0, BASE),
    ])
    # ---- andar: a saltitos, los huesos sueltos
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-30, 24, 6, 18, 6, 0.0), (-2, 2, 4, 40, 0, 0.8), (24, -30, 18, 6, -6, 0.0), (2, -2, 40, 4, 0, 0.8), (-30, 24, 6, 18, 6, 0.0))):
        pasos.append((1.0 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, -ty * 0.4), "cabeza": (6, -ty, ty * 0.5), "raiz": raiz(up)}))
    bucle("andar", 1.0, pasos)
    q = con(BASE, PORTE)
    bucle("porte_arco", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    bucle("listo_arco", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-1.1), torso=(12, -12, 0))),
        (2.0, LISTO),
    ])
    # ---- tirado: un monton de huesos (de cara al suelo, encogido)
    TUMBADO = dict(raiz=raiz(1.8, 13.5, rot=(86, 0, 18)), torso=(10, 0, 0), cabeza=(-20, 40, 18), mandibula=(30, 0, 0),
                   muslo_d=(-60, 0, 20), pierna_d=(100, 0, 0), muslo_i=(-40, 0, -10), pierna_i=(80, 0, 0),
                   _mano_i=("h", 2.0, -2.0, 7.0), _arma_i=(0.6, 0.0, 0.8), _mano_d=("h", -4.0, -1.0, 6.0), _arma=(-0.4, 0.0, 1.0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (4.0, TUMBADO)])
    # ---- velando: de rodillas, la cabeza baja, el arco en el suelo a su lado
    VELA = dict(raiz=raiz(-6.6, 0.6), torso=(18, 0, 0), cabeza=(30, 0, 0), mandibula=(16, 0, 0),
                muslo_d=(-4, 0, 4), pierna_d=(98, 0, 0), muslo_i=(-86, 0, -6), pierna_i=(84, 0, 0),
                _mano_i=("h", 1.0, -7.6, 4.0), _arma_i=(0.0, 1.0, 0.1), _mano_d=("h", -0.6, -8.0, 2.4), _arma=(0.1, -0.9, 0.4))
    bucle("velar", 4.0, [(0.0, VELA), (2.0, con(VELA, torso=(21, 0, 0), cabeza=(34, 0, 0))), (4.0, VELA)])

    # ================================================================ acciones
    # ---- el disparo: alza el arco, tensa (se ve venir), aguanta, suelta; luego saca otra flecha de la aljaba
    SUELTA = con(TENSA, _mano_d=("h", 8.2, 1.6, -0.6), _arma=(-0.9, 0.3, -0.3), torso=(0, -66, 0), cabeza=(-2, 60, 0))
    SACA = con(GUARDIA, torso=(8, -20, 0), cabeza=(0, 18, 0),
               _mano_i=("h", 1.0, -5.6, 5.0), _arma_i=(0.0, 0.9, 0.4),
               _mano_d=("h", 0.6, 6.2, -2.4), _arma=(0.0, 1.0, -0.2))
    accion("disparo", 2.2, [
        (0.00, LISTO),
        (0.30, con(TENSA, _mano_d=("h", 3.0, 0.6, 5.0), _arma=(0.0, 0.0, 1.0))),
        (0.80, TENSA),
        (1.05, con(TENSA, raiz=raiz(-1.1), torso=(2, -64, 0))),
        (1.10, SUELTA),
        (1.40, con(SUELTA, torso=(4, -50, 0), cabeza=(0, 46, 0))),
        (1.75, SACA),
        (2.20, LISTO),
    ], golpes=(1.10,))
    # ---- la patada: a quien se le echa encima
    PREPARA = con(GUARDIA, raiz=raiz(-0.4, 1.0), torso=(-8, -6, 0), cabeza=(6, 6, 0), muslo_d=(-70, 0, 4), pierna_d=(70, 0, 0),
                  muslo_i=(4, 0, -4), pierna_i=(10, 0, 0), _mano_i=("h", 3.0, -4.0, 2.0), _arma_i=(0.2, 1.0, 0.2),
                  _mano_d=("h", -3.0, -5.0, -1.0), _arma=(0.0, -0.8, 0.6))
    PATADA = con(PREPARA, raiz=raiz(-1.0, -1.0), torso=(-16, -4, 0), muslo_d=(-92, 0, 4), pierna_d=(0, 0, 0))
    accion("patada", 1.0, [
        (0.00, LISTO),
        (0.30, PREPARA),
        (0.42, PATADA),
        (0.60, con(PATADA, muslo_d=(-80, 0, 4), pierna_d=(20, 0, 0))),
        (1.00, LISTO),
    ], golpes=(0.42,))
    # ---- un golpe con el arco (de lado), si se le pega mucho
    ARRIBA_A = con(GUARDIA, torso=(4, -40, 0), cabeza=(0, 36, 0), _mano_i=("h", 5.0, 3.0, 2.0), _arma_i=(0.6, 0.6, 0.5),
                   _mano_d=("h", -1.0, -4.0, 4.0), _arma=(0.2, 0.2, 1.0))
    ABAJO_A = con(GUARDIA, raiz=raiz(-1.6, -1.6), torso=(18, 30, 0), cabeza=(-10, -26, 0), muslo_d=(-30, 0, 5), pierna_d=(26, 0, 0),
                  _mano_i=("h", -2.0, -3.0, 7.0), _arma_i=(-0.8, -0.2, 0.6), _mano_d=("h", -3.0, -5.0, 1.0), _arma=(0.0, -0.8, 0.6))
    accion("arcazo", 1.2, [
        (0.00, LISTO),
        (0.36, ARRIBA_A),
        (0.48, ABAJO_A),
        (0.75, con(ABAJO_A, torso=(16, 28, 0))),
        (1.20, LISTO),
    ], golpes=(0.48,))
    # ---- aturdido: se le descoyuntan los huesos
    AT = dict(raiz=raiz(-1.2, 2.0), torso=(-20, 12, 10), cabeza=(-24, -16, 20), mandibula=(28, 0, 0),
              muslo_d=(-14, 0, 10), pierna_d=(30, 0, 0), muslo_i=(18, 0, -10), pierna_i=(20, 0, 0),
              _mano_i=("h", 5.0, -6.0, -1.0), _arma_i=(0.5, -0.6, 0.6), _mano_d=("h", -5.0, -6.4, -1.0), _arma=(-0.5, -0.8, 0.3))
    accion("aturdido", 1.4, [
        (0.00, con(AT, raiz=raiz(-0.6, 0.6), torso=(4, 0, 0), cabeza=(0, 0, 6))),
        (0.20, AT),
        (0.70, con(AT, torso=(-14, -10, -8), cabeza=(-18, 14, -16))),
        (1.05, con(AT, torso=(-18, 8, 8), cabeza=(-22, -10, 14))),
        (1.40, LISTO),
    ])
    # ---- morir: se le doblan las rodillas y se viene abajo, hecho un monton de huesos
    RODILLAS = dict(raiz=raiz(-9.0, 0.0), torso=(14, 0, 8), cabeza=(30, 10, 20), mandibula=(30, 0, 0),
                    muslo_d=(-14, 0, 8), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -8), pierna_i=(92, 0, 0),
                    _mano_i=("h", 3.0, -8.0, 2.0), _arma_i=(0.4, -0.6, 0.6), _mano_d=("h", -3.0, -8.0, 2.0), _arma=(-0.2, -0.9, 0.3))
    accion("morir", 2.0, [
        (0.00, con(GUARDIA, raiz=raiz(-1.4, 1.2), torso=(-12, 0, 6), cabeza=(-16, 0, 10), mandibula=(24, 0, 0),
                   _mano_i=("h", 3.0, -6.0, 1.0), _arma_i=(0.3, -0.5, 0.7), _mano_d=("h", -3.0, -6.0, 1.0), _arma=(-0.3, -0.6, 0.6))),
        (0.45, RODILLAS),
        (0.95, con(RODILLAS, torso=(22, 0, 12), cabeza=(40, 10, 24))),
        (1.50, TUMBADO),
        (2.00, TUMBADO),
    ])
    # ---- deja de velar
    DE_PIE = LISTO
    accion("levantarse", 1.2, [
        (0.00, VELA),
        (0.45, con(VELA, raiz=raiz(-5.4, 0.2), torso=(26, 0, 0), cabeza=(-10, 0, 0))),
        (0.85, con(DE_PIE, raiz=raiz(-2.6, 0.0), torso=(22, -6, 0), muslo_i=(-44, 0, -5), pierna_i=(50, 0, 0))),
        (1.20, DE_PIE),
    ])
    # ---- el monton de huesos se levanta
    accion("alzarse", 2.0, [
        (0.00, TUMBADO),
        (0.45, con(TUMBADO, cabeza=(-40, 10, 0), mandibula=(36, 0, 0))),
        (0.95, dict(raiz=raiz(-5.8, 5.0, rot=(46, 0, 6)), torso=(-6, 0, 0), cabeza=(-40, 0, 10), mandibula=(24, 0, 0),
                    muslo_d=(-40, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(96, 0, 0),
                    _mano_i=("h", 2.0, -5.0, 6.0), _arma_i=(0.3, 0.0, 0.9), _mano_d=("h", -3.0, -6.0, 5.0), _arma=(-0.3, -0.4, 0.8))),
        (1.45, con(DE_PIE, raiz=raiz(-5.6, 0.0), torso=(34, -6, 0), cabeza=(-10, 0, 0), muslo_d=(-30, 0, 5), pierna_d=(70, 0, 0),
                   muslo_i=(-50, 0, -5), pierna_i=(70, 0, 0))),
        (2.00, DE_PIE),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_arco"].muestra(0))


def base_de(nombre, anims):
    if nombre == "porte_arco":
        return [anims["quieto"]]
    if nombre in ("andar", "quieto"):
        return [anims["porte_arco"]] if nombre == "quieto" else [anims["quieto"], anims["porte_arco"]]
    return []
