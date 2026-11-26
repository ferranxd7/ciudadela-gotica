"""
Los eruditos de la Academia Espectral (parte 7o): los maestros y aprendices que no dejaron la leccion la noche del cielo
rojo; el sello del Rector los ato a sus libros. Medio transparentes y algo violetas (lo pone el dibujante del juego), sin
piernas: la toga acaba en jirones y flotan un palmo sobre el suelo. Llevan todos la esclavina de la academia. Tres:

  0 aprendiz: toga gris azulada, cartera al costado, el calamo (una pluma con plumin de acero largo como un punal) en la
    derecha y el tintero en la izquierda. El plumazo (dos punaladas rapidas) y el tintero (lo lanza: la tinta ciega).
  1 maestro: toga morada con estola de runas que lucen, birrete con borla, barba larga, la vara con un cristal en la
    derecha y un libro abierto en la izquierda. El orbe (lo carga y lo suelta con la vara), las runas (dibuja un circulo
    en el suelo bajo su presa) y el parpadeo (se apaga y aparece mas lejos si se le acercan).
  2 custodio: el guardian de la biblioteca: encorvado y mas grande (el juego lo dibuja a 1,25), capucha, cinto con llaves
    y un tomo enorme colgado de una cadena en la derecha. El tomazo (lo alza con las dos manos y lo estrella: onda en el
    suelo) y el cadenazo (lo voltea con la cadena: llega lejos).
"""
from anim import Anim, con, raiz
from modelo import Box, Rig, humanoide
from pinta import (claro, delante, dibujo, liso, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "erudito"
VARIANTES = ("aprendiz", "maestro", "custodio")
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 4), cabeza=(8, 8, 8), brazo=(3, 13, 3), pierna=(4, 12, 4), hombro=0.5)
    # la toga (en dos trozos, para que ondee): de la cintura hasta un palmo del suelo, rota por abajo
    r.pieza("toga", "cadera", (0, -0.5, 0), boxes=[Box(-4.6, 0, -2.6, 9.2, 7, 5.2, "toga")])
    r.pieza("toga_baja", "toga", (0, 6.5, 0), boxes=[Box(-5.6, 0, -3.3, 11.2, 7, 6.6, "toga_baja")])
    # las mangas anchas que cuelgan de los antebrazos y la esclavina de la academia sobre los hombros
    for n in ("d", "i"):
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 2.5, 0), boxes=[Box(-2.4, 0, -2.4, 4.8, 4.4, 4.8, f"manga_{n}")])
    r.pieza("esclavina", "torso", (0, -12, 0), boxes=[Box(-5.2, -0.4, -2.9, 10.4, 4.2, 5.8, "esclavina")])
    # ---- 0, el aprendiz: la cartera, el calamo y el tintero
    r.pieza("cartera", "torso", (4.2, -3.6, 0), boxes=[Box(0, 0, -1.6, 1.8, 3.6, 3.2, "cartera")])
    r.pieza("calamo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.45, -0.45, -1.4, 0.9, 0.9, 3.2, "calamo_mango"),
        Box(-0.3, -0.3, -7.6, 0.6, 0.6, 6.2, "plumin"),
        Box(-0.15, -1.8, 1.6, 0.3, 2.4, 4.8, "pluma_vela"),
    ])
    r.pieza("tintero", "mano_i", (0, 0, 0), boxes=[
        Box(-1.2, 0.4, -1.2, 2.4, 2.6, 2.4, "tintero"),
        Box(-0.6, -0.3, -0.6, 1.2, 0.8, 1.2, "tintero_tapon"),
    ])
    # ---- 1, el maestro: el birrete con su borla, la barba, la estola, la vara y el libro abierto
    r.pieza("birrete", "cabeza", (0, -8, 0), boxes=[
        Box(-4.4, -1.4, -4.4, 8.8, 1.6, 8.8, "birrete"),
        Box(-5.4, -2.2, -5.4, 10.8, 0.8, 10.8, "birrete_tabla"),
        Box(4.8, -2.0, -0.4, 0.6, 3.8, 0.6, "borla"),
    ])
    r.pieza("barba", "cabeza", (0, 0, -4), boxes=[Box(-3.0, -2.6, -1.1, 6, 7.4, 1.2, "barba")])
    r.pieza("estola", "torso", (0, -12, -2.2), boxes=[Box(-3.3, 0, -0.5, 1.5, 17, 0.5, "estola"),
                                                       Box(1.8, 0, -0.5, 1.5, 17, 0.5, "estola")])
    r.pieza("vara", "mano_d", (0, 0, 0), boxes=[
        Box(-0.35, -0.35, -9.2, 0.7, 0.7, 10.8, "vara"),
        Box(-0.85, -0.85, -10.8, 1.7, 1.7, 1.7, "cristal"),
    ])
    r.pieza("libro", "mano_i", (0, 0, 0), boxes=[
        Box(-3.2, -1.0, -2.2, 3.1, 0.5, 4.4, "libro_hoja"),
        Box(0.1, -1.0, -2.2, 3.1, 0.5, 4.4, "libro_hoja"),
        Box(-3.4, -0.5, -2.4, 6.8, 0.4, 4.8, "libro_tapa"),
    ])
    # ---- 2, el custodio: la capucha, el cinto con las llaves y el tomo colgado de su cadena (los dos a lo largo de -Z,
    # como un arma: asi la mano lo apunta a donde toca)
    r.pieza("capucha", "cabeza", (0, -8, 0), boxes=[
        Box(-4.8, -0.8, -5.0, 9.6, 1.0, 9.8, "capucha_arriba"),
        Box(-4.8, -0.8, -5.0, 0.8, 8.8, 9.8, "capucha_lado"),
        Box(4.0, -0.8, -5.0, 0.8, 8.8, 9.8, "capucha_lado"),
        Box(-4.8, -0.8, 4.0, 9.6, 9.6, 0.8, "capucha_nuca"),
    ])
    r.pieza("llaves", "torso", (0, -1.6, 0), boxes=[Box(-4.3, -0.6, -2.3, 8.6, 1.0, 4.6, "cinto"),
                                                     Box(-3.2, 0.3, -2.7, 2.0, 2.6, 0.4, "llaves")])
    r.pieza("cadena", "mano_d", (0, 0, 0), boxes=[Box(-0.4, -0.4, -9.0, 0.8, 0.8, 9.6, "cadena")])
    r.pieza("tomo", "cadena", (0, 0, -9.0), boxes=[
        Box(-4.0, -2.0, -10.0, 8.0, 4.0, 10.0, "tomo"),
        Box(3.6, -1.2, -7.0, 0.8, 2.4, 1.6, "broche"),
    ])
    r.empaquetar()
    return r


PIERNAS = ["muslo_d", "muslo_i", "pierna_d", "pierna_i"]
DE_APRENDIZ = ["cartera", "calamo", "tintero"]
DE_MAESTRO = ["birrete", "barba", "estola", "vara", "libro"]
DE_CUSTODIO = ["capucha", "llaves", "cadena", "tomo"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: PIERNAS + DE_MAESTRO + DE_CUSTODIO,
    1: PIERNAS + DE_APRENDIZ + DE_CUSTODIO,
    2: PIERNAS + DE_APRENDIZ + DE_MAESTRO,
}

# ---------------------------------------------------------------- las texturas
# El juego los dibuja medio transparentes y algo violetas: aqui van con su color, de gris lila a casi blanco.

ALMA = ["#1b1a2e", "#2a2840", "#3d3a5a", "#57537c", "#7c78a3", "#a7a3c9", "#d6d3ee"]
TELA_A = ["#161a24", "#212736", "#2d3548", "#3c475e", "#4f5c76"]     # la toga del aprendiz (gris azulada)
TELA_B = ["#1a1126", "#261837", "#34214b", "#452c62", "#5a3a7e"]     # la del maestro (morada)
TELA_C = ["#17120e", "#221a14", "#2f241b", "#3e3024", "#52402f"]     # el cuero del custodio
ORO = ["#5c5a3f", "#8a8758", "#b8b47a", "#dcd79c"]
TINTA = ["#07080f", "#10132a", "#1c2350"]
PAPEL = ["#8a8270", "#b3aa92", "#d6ceb4"]
HIERRO = ["#26262c", "#3b3b44", "#575763", "#7c7c8a"]
HUECO = "#06060d"
OJO = "#d3c2ff"
RUNA = "#b98cff"


def materiales(variante):
    alma = ruido(ALMA[3:7], 811, grano=0.4, base=0.55)
    toga_a = tela(TELA_A, 821, pliegue=3, grano=0.12)
    toga_b = tela(TELA_B, 822, pliegue=4, grano=0.12, luz=0.3)
    cuero = tela(TELA_C, 823, pliegue=2, grano=0.2, luz=0.18)
    ropa = (toga_a, toga_b, cuero)[variante]
    oro = ruido(ORO, 831, grano=0.3)
    papel = ruido(PAPEL, 832, grano=0.3)
    hierro = ruido(HIERRO, 833, grano=0.3)

    def cabeza(cara, x, y, w, h):
        c = alma(cara, x, y, w, h)
        if cara == "frente":
            C = {"K": HUECO, "s": ALMA[2], "o": ALMA[3], "b": "#0b0a16", "c": ALMA[5], "g": ALMA[4]}
            filas = [
                "occccco.",
                "oscccsco",
                "osKccKso",
                "oKKccKKo",
                "oscgccso",
                "oscbbcso",
                "ocsbbsco",
                "oocsscoo",
            ]
            v = dibujo(filas, C)(cara, x, y, w, h)
            v = v if v is not None else c
            if variante == 2:
                return mezcla(v, HUECO, 0.72 - 0.05 * max(0, y - 4))        # bajo la capucha, casi negra
            if variante == 1 and y >= 5:
                return mezcla(v, ALMA[5], 0.5)                               # el arranque de la barba
            return v
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if d <= 2 and y in (4, 5, 6):
                return oscuro(c, 0.35)                                       # la mejilla hundida
        if cara == "arriba" or (cara in ("der", "izq", "espalda") and y <= 2):
            # el pelo: el aprendiz, mas oscuro y revuelto; el maestro, blanco y ralo
            pelo = ALMA[1] if variante == 0 else ALMA[5]
            return oscuro(mezcla(c, pelo, 0.65), 0.08 * ((x + y) % 3))
        return c

    def torso(cara, x, y, w, h):
        c = ropa(cara, x, y, w, h)
        if cara == "frente":
            if variante == 0:
                # la correa de la cartera, de un hombro a la cadera, y los botones
                if abs((x + y * 0.62) - 7.2) < 0.7:
                    return oscuro(TELA_C[3], 0.1)
                if x == 3 and y % 3 == 1:
                    return ORO[1]
            elif variante == 1:
                if y >= 10:
                    return oscuro(c, 0.25)
            else:
                # el peto de cuero con costuras
                if x in (0, 7) or y in (0, 11):
                    return oscuro(c, 0.2)
                if (x + y) % 5 == 0:
                    return claro(c, 0.08)
        if cara == "abajo":
            return None
        return c

    def brazo(cara, x, y, w, h):
        return ropa(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return alma(cara, x, y, w, h)                                    # la muneca, al aire
        return ropa(cara, x, y, w, h)

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None                                                      # hueca: por dentro sale la mano
        if y == h - 1 and (x * 5 + 1) % 3 == 0:
            return None
        c = ropa(cara, x, y, w, h)
        if y == h - 2:
            return ORO[1] if variante == 1 else claro(c, 0.12)               # el ribete
        return c

    def toga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = ropa(cara, x, y, w, h)
        return oscuro(c, 0.05 * y / max(1, h - 1))

    def toga_baja(cara, x, y, w, h):
        # se deshace en jirones: cada columna acaba a una altura, cada vez mas oscura y con huecos
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 3) % 4) - (1 if cara in ("der", "izq") else 0)
        if y > largo:
            return None
        if y >= h - 3 and (x * 3 + y) % 4 == 0:
            return None
        c = ropa(cara, x, y, w, h)
        if variante == 1 and y == 0:
            return ORO[1]
        return oscuro(c, 0.08 + 0.1 * y / max(1, h - 1))

    def esclavina(cara, x, y, w, h):
        if cara == "abajo":
            return None
        # la de la academia: negra con el ribete de oro abajo (la del custodio, de cuero)
        c = ruido(["#100f18", "#1a1826", "#25223a"], 841, grano=0.3)(cara, x, y, w, h)
        if variante == 2:
            c = oscuro(cuero(cara, x, y, w, h), 0.15)
        if cara != "arriba" and y == h - 1:
            return ORO[1] if (x % 3) else ORO[2]
        return c

    def cartera(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara in ("der", "izq", "frente") and y == 1:
            return oscuro(c, 0.3)                                            # la solapa
        return c

    def calamo_mango(cara, x, y, w, h):
        return oscuro(TELA_C[3], 0.1 * ((x + y) % 2))

    def plumin(cara, x, y, w, h):
        return claro(HIERRO[3], 0.15) if cara == "arriba" else HIERRO[2]

    def pluma_vela(cara, x, y, w, h):
        # la pluma, blanca con la punta negra de tinta
        if cara in ("arriba", "abajo"):
            return "#cfc8bb"
        return TINTA[1] if x <= 1 else ("#d8d2c6" if (x + y) % 3 else "#b8b0a2")

    def tintero(cara, x, y, w, h):
        if cara == "arriba":
            return TINTA[0]
        return mezcla(TINTA[2], "#6a76b8", 0.25) if (x + y) % 4 == 0 else TINTA[1]

    def birrete(cara, x, y, w, h):
        return ruido(["#120c1a", "#1b1226", "#251933"], 851, grano=0.3)(cara, x, y, w, h)

    def borla(cara, x, y, w, h):
        return ORO[2] if y < h - 1 else ORO[3]

    def barba(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        largo = h - 1 - ((x * 5 + 2) % 3)
        if y > largo:
            return None
        return mezcla(ALMA[6], ALMA[4], 0.15 * ((x + y) % 3))

    def estola(cara, x, y, w, h):
        # el pano morado con las runas de oro (las runas lucen: en el brillo)
        if y % 4 == 2:
            return ORO[2]
        return TELA_B[3] if (x + y) % 2 else TELA_B[2]

    def vara(cara, x, y, w, h):
        return ruido(["#2a1d12", "#3a2818", "#4d3520"], 861, grano=0.3)(cara, x, y, w, h)

    def cristal(cara, x, y, w, h):
        return "#cdb8ff" if (x + y) % 2 == 0 else "#9d86e6"

    def libro_hoja(cara, x, y, w, h):
        if cara == "arriba":
            # las lineas de la escritura
            return TINTA[2] if y % 2 == 1 and 0 < x < w - 1 else PAPEL[2]
        return PAPEL[1]

    def libro_tapa(cara, x, y, w, h):
        return ruido(["#2b1210", "#3d1915", "#52221c"], 862, grano=0.3)(cara, x, y, w, h)

    def capucha(cara, x, y, w, h):
        c = oscuro(cuero(cara, x, y, w, h), 0.1)
        if cara == "abajo":
            return oscuro(c, 0.5)
        if cara == "frente" and (x == 0 or x == w - 1 or y == 0):
            return claro(c, 0.15)
        return c

    def cinto(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        return ORO[1] if cara == "frente" and x in (3, 4) else oscuro(c, 0.2)

    def llaves(cara, x, y, w, h):
        return hierro(cara, x, y, w, h) if (x + y) % 2 == 0 else oscuro(HIERRO[2], 0.2)

    def cadena(cara, x, y, w, h):
        # eslabones: claro y oscuro a lo largo
        return HIERRO[3] if (y + x) % 3 == 0 else HIERRO[1]

    def tomo(cara, x, y, w, h):
        tapa = ruido(["#1d1410", "#2a1c14", "#3a271b"], 871, grano=0.35)(cara, x, y, w, h)
        if cara in ("der", "izq"):
            # el canto de las hojas (el lomo esta a la izquierda)
            return tapa if cara == "izq" else (PAPEL[0] if y % 2 else PAPEL[1])
        if cara in ("frente", "espalda"):
            return PAPEL[0] if 0 < x < w - 1 and 0 < y < h - 1 else tapa
        # las tapas: cantoneras de hierro en las esquinas
        if (x <= 1 or x >= w - 2) and (y <= 1 or y >= h - 2):
            return HIERRO[2]
        return tapa

    def broche(cara, x, y, w, h):
        return HIERRO[3]

    return {
        "cabeza": sombra(cabeza, abajo=0.12), "torso": sombra(torso, abajo=0.2),
        "brazo_d": brazo, "brazo_i": brazo, "antebrazo_d": antebrazo, "antebrazo_i": antebrazo,
        "muslo_d": liso(ALMA[2]), "muslo_i": liso(ALMA[2]), "pierna_d": liso(ALMA[2]), "pierna_i": liso(ALMA[2]),
        "manga_d": manga, "manga_i": manga, "toga": toga, "toga_baja": toga_baja, "esclavina": esclavina,
        "cartera": cartera, "calamo_mango": calamo_mango, "plumin": plumin, "pluma_vela": pluma_vela,
        "tintero": tintero, "tintero_tapon": liso("#3a2a1c"),
        "birrete": birrete, "birrete_tabla": birrete, "borla": borla, "barba": barba, "estola": estola,
        "vara": vara, "cristal": cristal, "libro_hoja": libro_hoja, "libro_tapa": libro_tapa,
        "capucha_arriba": capucha, "capucha_lado": capucha, "capucha_nuca": capucha,
        "cinto": cinto, "llaves": llaves, "cadena": cadena, "tomo": tomo, "broche": broche,
    }


def brillo(variante):
    """Lo que luce: la luz lila de las cuencas, las runas de la estola, el cristal de la vara y el broche del tomo."""
    ojos = dibujo([
        "........",
        "........",
        "..R..R..",
        "........",
    ], {"R": OJO})
    b = {"cabeza": por_cara(liso(None), frente=ojos) if variante != 2 else por_cara(liso(None), frente=dibujo([
        "........",
        "........",
        "..R..R..",
    ], {"R": "#9f8cff"}))}
    if variante == 1:
        b["estola"] = lambda cara, x, y, w, h: RUNA if y % 4 == 2 and cara == "frente" else None
        b["cristal"] = lambda cara, x, y, w, h: "#e6dcff"
        b["libro_hoja"] = lambda cara, x, y, w, h: "#8f7fd6" if cara == "arriba" and (x + y) % 5 == 0 else None
    if variante == 2:
        # el sello del Rector en la tapa del tomo: un circulo de runas que luce
        def runa_tomo(cara, x, y, w, h):
            if cara not in ("arriba", "abajo"):
                return None
            d = ((x + 0.5 - w / 2) ** 2 + (y + 0.5 - h / 2) ** 2) ** 0.5
            return "#8e6cff" if abs(d - 2.6) < 0.55 or (x in (w // 2 - 1, w // 2) and abs(y + 0.5 - h / 2) < 1.4) else None
        b["tomo"] = runa_tomo
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide ~10)

ARRIBA = 3.2   # lo que flota sobre el suelo (pixeles)
BASE = dict(raiz=raiz(ARRIBA), torso=(4, 0, 0), cabeza=(6, 0, 0), toga=(4, 0, 0), toga_baja=(6, 0, 0),
            brazo_d=(-6, 0, 4), antebrazo_d=(-10, 0, 0), brazo_i=(-6, 0, -4), antebrazo_i=(-10, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# como lleva cada uno lo suyo cuando no pasa nada
PORTE_C = dict(_mano_d=("h", 0.0, -8.4, 2.8), _arma=(0.05, -0.8, 0.6), _mano_i=("h", 1.0, -8.6, 2.0), _arma_i=(0.0, -0.2, 1.0))
PORTE_V = dict(_mano_d=("h", 0.4, -8.2, 3.0), _arma=(0.0, -0.55, 0.83), _mano_i=("h", 2.6, -5.0, 6.0), _arma_i=(0.0, 0.0, 1.0))
PORTE_T = dict(_mano_d=("h", -0.6, -9.2, 2.2), _arma=(0.0, -1.0, 0.02), brazo_i=(10, 0, -8), antebrazo_i=(-30, 0, 0))
# en guardia: el aprendiz con el calamo por delante; el maestro con la vara apuntando; el custodio encorvado con el tomo
LISTO_C = dict(raiz=raiz(ARRIBA + 0.4), torso=(12, -20, 0), cabeza=(-6, 18, 0), toga=(6, 0, 0), toga_baja=(10, 0, 0),
               _mano_d=("h", 1.0, -2.4, 8.4), _arma=(0.2, 0.0, 1), _mano_i=("h", 1.0, -6.0, 4.0), _arma_i=(0.0, -0.2, 1.0))
LISTO_V = dict(raiz=raiz(ARRIBA + 0.6), torso=(2, -10, 0), cabeza=(-2, 10, 0), toga=(4, 0, 0), toga_baja=(6, 0, 0),
               _mano_d=("h", 1.4, -1.0, 8.6), _arma=(0.1, 0.25, 1), _mano_i=("h", 2.8, -4.4, 6.2), _arma_i=(0.0, 0.0, 1.0))
LISTO_T = dict(raiz=raiz(ARRIBA - 0.4), torso=(22, 0, 0), cabeza=(-14, 0, 0), toga=(10, 0, 0), toga_baja=(14, 0, 0),
               _mano_d=("h", -1.0, -8.0, 4.4), _arma=(0.0, -1.0, 0.25), brazo_i=(20, 0, -14), antebrazo_i=(-40, 0, 0))


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

    # ---- flotar (quieto): sube y baja despacio, la toga ondea, la cabeza se ladea (como recitando)
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.0, con(BASE, raiz=raiz(ARRIBA + 0.8), torso=(3, 4, 2), cabeza=(4, -12, 6), toga=(-2, 0, 3), toga_baja=(10, 0, -4))),
        (2.0, con(BASE, raiz=raiz(ARRIBA + 1.3), torso=(5, 0, 0), cabeza=(10, 0, 0), toga=(6, 0, 0), toga_baja=(2, 0, 0))),
        (3.0, con(BASE, raiz=raiz(ARRIBA + 0.6), torso=(3, -4, -2), cabeza=(4, 12, -6), toga=(-2, 0, -3), toga_baja=(10, 0, 4))),
        (4.0, BASE),
    ])
    # ---- andar: se desliza inclinado, la toga y las mangas por detras
    DESLIZA = con(BASE, raiz=raiz(ARRIBA + 1.2), torso=(14, 0, 0), cabeza=(-8, 0, 0), toga=(22, 0, 0), toga_baja=(18, 0, 0))
    bucle("andar", 1.6, [
        (0.0, DESLIZA),
        (0.4, con(DESLIZA, raiz=raiz(ARRIBA + 1.6), toga=(26, 0, 4), toga_baja=(24, 0, -6))),
        (0.8, con(DESLIZA, raiz=raiz(ARRIBA + 1.0), toga=(20, 0, 0), toga_baja=(14, 0, 0))),
        (1.2, con(DESLIZA, raiz=raiz(ARRIBA + 1.6), toga=(26, 0, -4), toga_baja=(24, 0, 6))),
        (1.6, DESLIZA),
    ])
    # ---- como lleva cada uno las manos (se suma; solo los brazos) y en guardia
    for nombre, p in (("porte_calamo", PORTE_C), ("porte_vara", PORTE_V), ("porte_tomo", PORTE_T)):
        q = con(BASE, p)
        bucle(nombre, 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    for nombre, p in (("listo_calamo", LISTO_C), ("listo_vara", LISTO_V), ("listo_tomo", LISTO_T)):
        bucle(nombre, 2.0, [
            (0.0, p),
            (1.0, con(p, raiz=raiz(p["raiz"]["pos"][1] + 0.9), torso=(p["torso"][0] + 3, p["torso"][1], 0))),
            (2.0, p),
        ])
    # ---- leer (la postura de rezar del juego): flota quieto con la cabeza en el libro, las manos juntas por delante
    LEE = dict(raiz=raiz(ARRIBA + 0.4), torso=(14, 0, 0), cabeza=(30, 0, 0), toga=(-6, 0, 0), toga_baja=(-4, 0, 0),
               _mano_d=("h", 2.2, -6.2, 5.6), _arma=(0.0, -0.9, 0.4), _mano_i=("h", -2.2, -6.0, 5.8), _arma_i=(0.0, -0.1, 1.0))
    bucle("leer", 4.0, [
        (0.0, LEE),
        (2.0, con(LEE, raiz=raiz(ARRIBA + 1.0), cabeza=(34, 6, 0))),
        (4.0, LEE),
    ])
    # ---- tumbado: caido entre los libros, la cara contra el suelo (asi espera en las salas)
    CAIDO = dict(raiz=raiz(-6.0, -2.0), torso=(72, 0, 0), cabeza=(40, 0, 10), toga=(-60, 0, 0), toga_baja=(-50, 0, 0),
                 _mano_d=("h", 3.0, -1.0, 6.0), _arma=(-0.3, -0.6, 0.7), _mano_i=("h", -3.0, -0.5, 6.0))
    bucle("tumbado", 4.0, [(0.0, CAIDO), (2.0, con(CAIDO, torso=(74, 0, 0))), (4.0, CAIDO)])

    # ================================================================ acciones
    C, V, T = con(BASE, LISTO_C), con(BASE, LISTO_V), con(BASE, LISTO_T)
    # ---- el aprendiz: el plumazo (recoge el calamo y dos punaladas seguidas, avanzando)
    RECOGE = con(C, raiz=raiz(ARRIBA + 1.0, 1.6), torso=(6, -34, 0), cabeza=(-2, 30, 0), _mano_d=("h", -0.5, -1.4, 2.4), _arma=(0.1, 0.1, 1))
    PUNAL = con(C, raiz=raiz(ARRIBA - 0.2, -3.6), torso=(18, -10, 0), cabeza=(-10, 8, 0), toga=(18, 0, 0), toga_baja=(22, 0, 0),
                _mano_d=("h", 1.2, -1.0, 9.6), _arma=(0.05, -0.05, 1))
    accion("plumazo", 1.1, [
        (0.00, C),
        (0.24, RECOGE),
        (0.35, PUNAL),
        (0.48, con(RECOGE, raiz=raiz(ARRIBA + 0.6, -1.0))),
        (0.60, con(PUNAL, raiz=raiz(ARRIBA - 0.3, -5.0), _mano_d=("h", 2.4, -0.4, 9.4), _arma=(0.25, 0.0, 1))),
        (1.10, C),
    ], golpes=(0.35, 0.60))
    # ---- el tintero: la izquierda atras y arriba y lo lanza por encima del hombro
    ATRAS = con(C, torso=(-6, 26, 0), cabeza=(-4, -20, 0), _mano_i=("h", 1.2, 6.0, -3.0), _arma_i=(0.0, 1.0, -0.3))
    SUELTA = con(C, torso=(16, -16, 0), cabeza=(-8, 12, 0), raiz=raiz(ARRIBA, -1.2), _mano_i=("h", 0.6, 1.0, 9.2), _arma_i=(0.0, 0.1, 1.0))
    accion("tintero", 1.3, [
        (0.00, C),
        (0.40, ATRAS),
        (0.55, con(ATRAS, torso=(-8, 30, 0))),
        (0.65, SUELTA),
        (0.90, con(SUELTA, torso=(12, -10, 0))),
        (1.30, C),
    ], golpes=(0.65,))
    # ---- el maestro: el orbe (alza la vara, el libro abierto delante, y la empuja hacia su presa)
    CARGA = con(V, raiz=raiz(ARRIBA + 1.6), torso=(-8, 14, 0), cabeza=(-12, -10, 0), _mano_d=("h", 0.4, 6.0, 3.6), _arma=(0.1, 0.9, 0.2),
                _mano_i=("h", 2.4, -2.0, 7.0))
    EMPUJA = con(V, raiz=raiz(ARRIBA + 0.6, -2.0), torso=(10, -14, 0), cabeza=(-6, 12, 0), toga=(12, 0, 0), toga_baja=(16, 0, 0),
                 _mano_d=("h", 0.8, -0.6, 9.6), _arma=(0.0, 0.05, 1))
    accion("orbe", 1.5, [
        (0.00, V),
        (0.45, CARGA),
        (0.65, con(CARGA, raiz=raiz(ARRIBA + 2.0), torso=(-10, 16, 0))),
        (0.75, EMPUJA),
        (1.00, con(EMPUJA, raiz=raiz(ARRIBA + 0.4, -1.6))),
        (1.50, V),
    ], golpes=(0.75,))
    # ---- las runas: apunta la vara al suelo delante y dibuja un circulo (la punta da la vuelta)
    APUNTA = con(V, torso=(24, 0, 0), cabeza=(14, 0, 0), raiz=raiz(ARRIBA + 1.0), _mano_d=("h", 0.6, -4.6, 8.4), _arma=(0.0, -0.8, 0.6))
    accion("runas", 1.6, [
        (0.00, V),
        (0.35, APUNTA),
        (0.50, con(APUNTA, _mano_d=("h", 3.0, -5.0, 7.6), _arma=(0.3, -0.8, 0.5))),
        (0.65, con(APUNTA, _mano_d=("h", 0.6, -5.4, 6.8), _arma=(0.0, -0.9, 0.45))),
        (0.80, con(APUNTA, _mano_d=("h", -1.8, -5.0, 7.6), _arma=(-0.3, -0.8, 0.5))),
        (0.95, con(APUNTA, torso=(28, 0, 0), _mano_d=("h", 0.6, -6.0, 8.0), _arma=(0.0, -0.95, 0.3))),
        (1.60, V),
    ], golpes=(0.80,))
    # ---- el parpadeo: se encoge y se apaga (el juego lo lleva a otro sitio a los 0,5 s) y se abre al aparecer
    ENCOGE = con(BASE, raiz=raiz(ARRIBA - 1.0), torso=(30, 0, 0), cabeza=(30, 0, 0), toga=(-10, 0, 0),
                 _mano_d=("h", 3.0, -2.0, 4.0), _arma=(0.0, -0.6, 0.8), _mano_i=("h", -3.0, -2.0, 4.0))
    ABRE = con(BASE, raiz=raiz(ARRIBA + 2.0), torso=(-10, 0, 0), cabeza=(-12, 0, 0),
               _mano_d=("h", -6.0, -1.0, 4.0), _arma=(0.0, -0.4, 0.9), _mano_i=("h", 6.0, -1.0, 4.0))
    accion("parpadeo", 1.2, [
        (0.00, BASE),
        (0.30, ENCOGE),
        (0.50, con(ENCOGE, raiz=raiz(ARRIBA - 1.4), torso=(36, 0, 0))),
        (0.65, ABRE),
        (1.20, BASE),
    ])
    # ---- el custodio: el tomazo (coge la cadena con las dos manos, alza el tomo por encima y lo estrella delante)
    ALZA = con(T, raiz=raiz(ARRIBA + 1.4, 1.0), torso=(-14, 0, 0), cabeza=(-16, 0, 0),
               _mano_d=("h", 4.2, 5.8, 1.8), _arma=(0.0, 0.75, -0.66), _agarra=2.6)
    ESTRELLA = con(T, raiz=raiz(ARRIBA - 1.2, -2.8), torso=(36, 0, 0), cabeza=(-10, 0, 0), toga=(18, 0, 0), toga_baja=(24, 0, 0),
                   _mano_d=("h", 4.4, -4.2, 7.4), _arma=(0.0, -0.72, 0.69), _agarra=2.6)
    accion("tomazo", 1.6, [
        (0.00, T),
        (0.30, con(T, _mano_d=("h", 4.2, -3.0, 5.0), _arma=(0.0, -0.6, 0.8), _agarra=2.6)),
        (0.62, ALZA),
        (0.75, con(ALZA, raiz=raiz(ARRIBA + 1.8, 1.4), torso=(-18, 0, 0))),
        (0.85, ESTRELLA),
        (1.15, con(ESTRELLA, torso=(38, 0, 0))),
        (1.60, T),
    ], golpes=(0.85,))
    # ---- el cadenazo: voltea el tomo con la cadena de su derecha a su izquierda (llega lejos)
    TIRA = con(T, torso=(16, 40, 0), cabeza=(-8, -30, 0), _mano_d=("h", -7.4, -2.4, 2.6), _arma=(-0.95, 0.05, -0.3))
    BARRE = con(T, torso=(16, -44, 0), cabeza=(-8, 34, 0), raiz=raiz(ARRIBA, -1.2),
                _mano_d=("h", 5.6, -1.6, 7.2), _arma=(0.85, 0.1, 0.5))
    accion("cadenazo", 1.5, [
        (0.00, T),
        (0.40, TIRA),
        (0.55, con(TIRA, torso=(18, 48, 0))),
        (0.70, con(T, torso=(18, -4, 0), _mano_d=("h", 0.6, -2.2, 8.6), _arma=(0.0, 0.05, 1))),
        (0.85, BARRE),
        (1.10, con(BARRE, torso=(14, -50, 0))),
        (1.50, T),
    ], golpes=(0.70,))
    # ---- aturdido: se le va el cuerpo atras y tiembla
    accion("aturdido", 1.2, [
        (0.00, BASE),
        (0.15, con(BASE, raiz=raiz(ARRIBA + 1.5, 2.0), torso=(-18, 10, 0), cabeza=(-20, -14, 0), brazo_d=(30, 0, 30), brazo_i=(30, 0, -30))),
        (0.55, con(BASE, raiz=raiz(ARRIBA + 1.0, 1.5), torso=(-12, -8, 0), cabeza=(-10, 16, 0), brazo_d=(20, 0, 20), brazo_i=(20, 0, -20))),
        (0.90, con(BASE, raiz=raiz(ARRIBA + 0.6, 0.6), torso=(-4, 4, 0), cabeza=(0, -6, 0))),
        (1.20, BASE),
    ])
    # ---- morir: se dobla, suelta lo que lleva y sube deshaciendose (el juego lo va apagando)
    accion("morir", 2.2, [
        (0.00, BASE),
        (0.40, con(BASE, raiz=raiz(ARRIBA + 1.0), torso=(-16, 0, 0), cabeza=(-30, 0, 0),
                   _mano_d=("h", -5.0, 4.0, 3.0), _arma=(0.0, 0.8, 0.6), _mano_i=("h", 5.0, 4.0, 3.0))),
        (1.20, con(BASE, raiz=raiz(ARRIBA + 6.0), torso=(-24, 0, 0), cabeza=(-40, 0, 0), toga=(-20, 0, 0), toga_baja=(-20, 0, 0),
                   _mano_d=("h", -3.0, 8.6, 1.0), _arma=(0.0, 1.0, 0.1), _mano_i=("h", 3.0, 8.6, 1.0))),
        (2.20, con(BASE, raiz=raiz(ARRIBA + 12.0), torso=(-30, 0, 0), cabeza=(-44, 0, 0), toga=(-30, 0, 0), toga_baja=(-30, 0, 0),
                   _mano_d=("h", -2.0, 9.0, 0.5), _arma=(0.0, 1.0, 0.1), _mano_i=("h", 2.0, 9.0, 0.5))),
    ])
    # ---- levantarse (de entre los libros) y alzarse (de leer)
    accion("levantarse", 1.5, [
        (0.00, CAIDO),
        (0.50, con(CAIDO, raiz=raiz(-3.0, -1.0), torso=(50, 0, 0), cabeza=(10, 0, 0))),
        (1.00, con(BASE, raiz=raiz(ARRIBA + 1.5), torso=(-6, 0, 0), cabeza=(-14, 0, 0),
                   _mano_d=("h", -6.0, 0.0, 4.0), _arma=(0.0, -0.4, 0.9), _mano_i=("h", 6.0, 0.0, 4.0))),
        (1.50, BASE),
    ])
    accion("alzarse", 1.0, [
        (0.00, LEE),
        (0.45, con(LEE, raiz=raiz(ARRIBA + 1.6), torso=(4, 0, 0), cabeza=(-6, 0, 0))),
        (1.00, BASE),
    ])
    return B, A


def variante_de(nombre):
    if nombre in ("plumazo", "tintero") or nombre.endswith("calamo"):
        return 0
    if nombre in ("orbe", "runas", "parpadeo") or nombre.endswith("vara"):
        return 1
    if nombre in ("tomazo", "cadenazo") or nombre.endswith("tomo"):
        return 2
    return 1


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{('calamo', 'vara', 'tomo')[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre in ("andar", "quieto"):
        return [anims["porte_vara"]]
    return []


TABLA_EXTRA = ()
