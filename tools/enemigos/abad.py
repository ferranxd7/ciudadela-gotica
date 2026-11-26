"""
Hrodric, el Abad de la Cumbre (parte 7l): el jefe de las Montanas Nevadas. El abad del Monasterio de la Helada que no rezo
con los suyos: subio a la cumbre a pedirle al frio que los perdonara, y el frio le contesto (libro perdido 21, del hermano
Wendel: "le he visto bajar de noche, mas alto que la torre, con una mitra de hielo"). Un viejo enorme y helado: la piel
gris azulada con escarcha, la barba larga de carambanos, los ojos azul blanco; la mitra de hielo con su banda de oro
congelada y sus infulas; el alba blanca hasta los pies, la estola bordada y la cruz al pecho; la capa pluvial carmesi con
la orla de oro, el broche y el capillo a la espalda; cristales de hielo que le salen de la espalda atravesando la capa y
carambanos colgando de las mangas. En la derecha el baculo: la vara de plata con su nudo de oro y la voluta de hielo con
un cristal que luce.

Una sola variante. Acciones: baculazo (alza el baculo con las dos manos y lo estrella delante: brotan puas de hielo en
fila), barrido (a media altura), pisoton (alza el pie y pisa: un anillo de puas a su alrededor), aliento (el soplo de la
cumbre, que barre delante), carambanos (alza el baculo al cielo: caen carambanos donde estan los suyos), grito (al empezar
y a media vida: la ventisca y los hermanos que se alzan de la nieve), aturdido, morir (cae de rodillas, se apoya en el
baculo y se queda asi, vencido), levantarse (esperaba de rodillas, rezando con el baculo delante).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, dibujo, liso, mezcla, metal, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "abad"
VARIANTES = ("abad",)
ESCALA = 2.0
CENTRO_Y = 2.4

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # grande y ancho de hombros, algo encorvado (el brazo mide 16: la mano llega a unos 13 de su hombro)
    humanoide(r, torso=(11, 14, 6), cabeza=(8, 9, 8), brazo=(4, 16, 4), pierna=(5, 14, 5), hombro=0.6)
    # ---- la mitra de hielo: la banda de oro, las dos hojas en punta (la de delante y la de atras), el relleno, los cristales de
    # la punta y las infulas que caen por la nuca
    r.pieza("mitra", "cabeza", (0, -9, 0), boxes=[
        Box(-4.5, -1.6, -4.5, 9.0, 1.6, 9.0, "mitra_banda"),
        Box(-4.0, -10.0, -3.4, 8.0, 8.4, 1.4, "mitra_hoja"),
        Box(-4.0, -10.0, 2.0, 8.0, 8.4, 1.4, "mitra_hoja"),
        Box(-3.4, -7.6, -2.0, 6.8, 6.0, 4.0, "mitra_medio"),
        Box(-0.7, -13.0, -0.7, 1.4, 3.6, 1.4, "mitra_cristal"),
        Box(-3.2, -10.6, -0.6, 1.2, 2.4, 1.2, "mitra_cristal"),
        Box(2.0, -10.6, -0.6, 1.2, 2.4, 1.2, "mitra_cristal"),
    ])
    r.pieza("infulas", "cabeza", (0, -8.6, 4.4), rot=(8, 0, 0), boxes=[
        Box(-3.2, 0, 0, 1.6, 10.0, 0.5, "infula"),
        Box(1.6, 0, 0, 1.6, 10.0, 0.5, "infula"),
    ])
    # ---- la barba de hielo (por delante del pecho) y los carambanos que le cuelgan
    r.pieza("barba", "cabeza", (0, -2.6, -4.2), rot=(-6, 0, 0), boxes=[
        Box(-3.4, 0, -0.9, 6.8, 7.6, 1.4, "barba"),
        Box(-2.6, 7.6, -0.7, 1.0, 2.6, 1.0, "carambano"),
        Box(-0.5, 7.6, -0.7, 1.0, 3.6, 1.0, "carambano"),
        Box(1.6, 7.6, -0.7, 1.0, 2.2, 1.0, "carambano"),
    ])
    # ---- la capa pluvial: sobre los hombros, la espalda hasta el suelo (con el capillo) y el broche al pecho
    r.pieza("capa_hombros", "torso", (0, -14, 0), boxes=[Box(-6.9, -0.8, -3.9, 13.8, 5.4, 7.8, "capa_hombros", grow=g(0.3))])
    r.pieza("capa", "torso", (0, -13.6, 3.4), rot=(5, 0, 0), boxes=[
        Box(-6.7, 0, 0, 13.4, 27.0, 0.9, "capa"),
        Box(-3.6, 2.0, 0.9, 7.2, 7.4, 0.5, "capillo"),
    ])
    r.pieza("broche", "torso", (0, -12.6, -3.4), boxes=[Box(-1.6, 0, -0.6, 3.2, 2.4, 0.7, "broche")])
    # ---- la estola (las dos bandas bordadas por delante, hasta por debajo de la cintura) y la cruz al pecho
    r.pieza("estola", "torso", (0, -13.2, -3.25), boxes=[
        Box(-3.3, 0, -0.4, 1.8, 16.0, 0.4, "estola"),
        Box(1.5, 0, -0.4, 1.8, 16.0, 0.4, "estola"),
    ])
    r.pieza("cruz", "torso", (0, -9.4, -3.7), boxes=[
        Box(-0.45, 0, -0.4, 0.9, 3.8, 0.4, "cruz"),
        Box(-1.4, 0.8, -0.4, 2.8, 0.9, 0.4, "cruz"),
    ])
    # ---- los cristales de hielo que le salen de la espalda (atraviesan la capa)
    for nombre, piv, rot, caja in (("cristal_a", (-4.0, -12.5, 2.6), (-28, 0, -24), (-1.0, -7.5, -1.0, 2.0, 7.5, 2.0)),
                                   ("cristal_b", (4.4, -11.8, 2.6), (-34, 0, 26), (-0.8, -5.8, -0.8, 1.6, 5.8, 1.6)),
                                   ("cristal_c", (0.6, -8.6, 3.0), (-44, 0, 6), (-1.1, -8.6, -1.1, 2.2, 8.6, 2.2))):
        r.pieza(nombre, "torso", piv, rot=rot, boxes=[Box(*caja, "cristal")])
    # ---- el alba hasta los pies (cada pierna su faldon, mas ancho que ella) y las mangas anchas con sus carambanos
    for n in ("d", "i"):
        r.pieza(f"alba_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-3.2, -0.6, -3.2, 6.4, 7.8, 6.4, f"alba_{n}")])
        r.pieza(f"alba_bajo_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-3.1, 0, -3.1, 6.2, 7.6, 6.2, f"alba_bajo_{n}")])
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 1.2, 0), boxes=[
            Box(-2.8, 0, -2.8, 5.6, 6.2, 5.6, f"manga_{n}"),
            Box(-1.9, 6.2, -1.2, 0.9, 2.4, 0.9, "carambano"),
            Box(0.9, 6.2, 0.6, 0.9, 1.8, 0.9, "carambano"),
        ])
    # ---- el baculo (apunta a -Z con el brazo caido): la vara de plata hasta el suelo, el regaton, el nudo de oro y la voluta
    # de hielo (sube, se curva hacia -Y y baja), con el cristal dentro de la curva y dos carambanos colgando
    r.pieza("baculo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.85, -0.85, -30.0, 1.7, 1.7, 45.0, "vara"),
        Box(-1.05, -1.05, 15.0, 2.1, 2.1, 1.6, "regaton"),
        Box(-1.35, -1.35, -32.0, 2.7, 2.7, 2.2, "nudo"),
        Box(-1.0, -1.0, -39.0, 2.0, 2.0, 7.2, "voluta"),
        Box(-1.0, -6.6, -41.0, 2.0, 6.6, 2.0, "voluta"),
        Box(-1.0, -7.6, -39.4, 2.0, 2.0, 4.6, "voluta"),
        Box(-1.0, -5.8, -35.2, 2.0, 1.8, 1.4, "voluta"),
        Box(-1.3, -5.2, -38.8, 2.6, 2.6, 2.6, "cristal_baculo"),
        Box(-0.45, -8.4, -37.0, 0.9, 1.0, 0.9, "carambano"),
        Box(-0.45, 1.0, -34.6, 0.9, 2.2, 0.9, "carambano"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {0: []}

#: lo que va ademas en AbadAnims (la escala, para el dibujante y el servidor)
TABLA_EXTRA = (
    "",
    f"    public static final float ESCALA = {ESCALA}f;",
)

# ---------------------------------------------------------------- las texturas

PIEL = ["#2b3640", "#3e4e5a", "#556b7a", "#71899a", "#95adbc", "#bed1dc"]
ESCARCHA = ["#b7c9d3", "#d3e1e8", "#eef6fa", "#ffffff"]
HIELO = ["#2f5f7a", "#4a86a3", "#6fadc8", "#9bd2e6", "#cdeef8", "#f0fbff"]
ALBA = ["#4f565d", "#666e76", "#7f8890", "#9aa3ab", "#b7bfc6", "#d4dbe0"]
CAPA = ["#25070a", "#38090e", "#500f15", "#69161c", "#842026", "#a02c33"]
ORO = ["#4a3a12", "#73591c", "#a07d28", "#cba43a", "#ead26a"]
PLATA = ["#2c3238", "#434b53", "#5d666f", "#7b858e", "#9ea8b0", "#c8d0d6"]
OJO = ["#3fa9d8", "#8fe0ff", "#e6fbff"]
HUECO = "#081016"

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
    piel = ruido(PIEL[1:5], 1111, grano=0.45, base=0.55)
    alba_t = tela(ALBA, 1112, pliegue=4, grano=0.14, luz=0.2)
    capa_t = tela(CAPA, 1113, pliegue=5, grano=0.18, luz=0.2)
    hielo_t = ruido(HIELO[2:6], 1114, grano=0.4, base=0.5)

    def helado(c, cara, x, y, w, h, cuanto=0.18, sal=0):
        """Escarcha a motas sueltas y a grumos (mas arriba y en la cara de arriba): las claras sobre el color, alguna blanca."""
        kc = CARAS[cara]
        s0 = w * 31 + h * 17 + sal * 101
        gg = 0.55 * _azar01(s0, kc, x // 2, y // 2) + 0.45 * _azar01(s0 + 7, kc, x, y)
        alto = 1.0 - y / max(1, h - 1)
        p = 0.55 if cara == "arriba" else cuanto * (0.45 + alto)
        if gg < p * 0.5:
            return ESCARCHA[1 + int(_azar01(s0 + 3, kc, x, y) * 2.99)]
        if gg < p:
            return mezcla(c, ESCARCHA[0], 0.45)
        return c

    def hielo(cara, x, y, w, h, sal=0):
        """Hielo: azul con vetas claras y algun brillo blanco en las aristas."""
        c = hielo_t(cara, x, y, w, h)
        if x in (0, w - 1) and (y + sal) % 3 == 0:
            return HIELO[5]
        if _azar01(w * 13 + sal, CARAS[cara], x, y) < 0.12:
            return HIELO[1]
        return c

    def cabeza(cara, x, y, w, h):
        c = helado(piel(cara, x, y, w, h), cara, x, y, w, h, 0.12, 1)
        if cara == "frente":
            # la cara: las cejas blancas de escarcha, los ojos hundidos (el brillo los enciende), la nariz larga y el bigote
            C = {"K": HUECO, "s": PIEL[1], "o": PIEL[2], "c": PIEL[3], "W": ESCARCHA[2], "w": ESCARCHA[1], "n": PIEL[4],
                 "b": HIELO[4], "B": HIELO[3]}
            v = dibujo([
                "oooooooo",
                "oooooooo",
                "wWWccWWw",
                "oKKcnKKo",
                "osccncso",
                "oscnncso",
                "obBbbBbo",
                "bbBbbBbb",
                "bbbbbbbb",
            ], C)(cara, x, y, w, h)
            return v if v is not None else c
        return c

    def torso(cara, x, y, w, h):
        # por delante: el alba en medio (la estola va encima) y la capa a los lados con la orla de oro; detras, la capa
        if cara == "frente":
            if x <= 1 or x >= w - 2:
                return helado(capa_t(cara, x, y, w, h), cara, x, y, w, h, 0.12, 2)
            if x in (2, w - 3):
                return ORO[2 + (y % 3 == 0)] if y % 2 else ORO[1]
            return helado(alba_t(cara, x, y, w, h), cara, x, y, w, h, 0.10, 3)
        if cara in ("der", "izq", "espalda", "arriba"):
            return helado(capa_t(cara, x, y, w, h), cara, x, y, w, h, 0.14, 4)
        return CAPA[1]

    def brazo(cara, x, y, w, h):
        return helado(capa_t(cara, x, y, w, h), cara, x, y, w, h, 0.16, 5)

    def antebrazo(cara, x, y, w, h):
        # la mano helada al final (lo demas lo tapa la manga)
        if y >= h - 3:
            return helado(piel(cara, x, y, w, h), cara, x, y, w, h, 0.25, 6)
        return alba_t(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return alba_t(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        return oscuro(piel(cara, x, y, w, h), 0.2)

    def alba(cara, x, y, w, h):
        return helado(alba_t(cara, x, y, w, h), cara, x, y, w, h, 0.14, 7)

    def alba_bajo(cara, x, y, w, h):
        if cara == "abajo":
            return ALBA[0]
        c = alba_t(cara, x, y + 8, w, h)
        # el bajo: una franja bordada y la escarcha que sube del suelo
        if y in (h - 3, h - 2):
            c = ORO[1] if (x + y) % 3 else CAPA[3]
        if y >= h - 2 or _azar01(w * 7, CARAS[cara], x, y) < 0.18 * (y / max(1, h - 1)):
            return ESCARCHA[(x + y) % 3]
        return c

    def manga(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = helado(alba_t(cara, x, y, w, h), cara, x, y, w, h, 0.2, 8)
        if y == h - 1:
            return ORO[2] if x % 2 else ORO[1]
        return c

    def capa_hombros(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = helado(capa_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 9)
        if y == h - 1 and cara != "arriba":
            return ORO[3] if x % 2 else ORO[2]                 # la orla
        return c

    def capa(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return ORO[2]
        c = helado(capa_t(cara, x, y, w, h), cara, x, y, w, h, 0.16, 10)
        # la orla de oro por los bordes y el bajo, con cruces bordadas
        if x in (0, w - 1) or y >= h - 2:
            return ORO[3] if (x + y) % 2 else ORO[2]
        if x in (1, w - 2) or y == h - 3:
            return ORO[1]
        if cara == "espalda" and (x - w // 2) % 6 == 0 and y % 8 == 5:
            return ORO[3]
        # la escarcha sube desde el bajo
        if y > h * 0.7 and _azar01(w, x, y, 11) < (y / h - 0.7) * 1.6:
            return ESCARCHA[(x * 3 + y) % 3]
        return c

    def capillo(cara, x, y, w, h):
        if cara != "espalda":
            return CAPA[2]
        # el escudo del capillo: oro con una cruz carmesi y el borde
        if x in (0, w - 1) or y in (0, h - 1):
            return ORO[1]
        if x == w // 2 or y == 3:
            return CAPA[3]
        return ORO[2 + (x + y) % 2]

    def broche(cara, x, y, w, h):
        if cara == "frente" and (x, y) == (w // 2, h // 2):
            return HIELO[4]
        return ORO[3] if (x + y) % 2 else ORO[2]

    def estola(cara, x, y, w, h):
        if cara != "frente":
            return CAPA[2]
        if y >= h - 2:
            return ORO[3] if x % 2 == 0 else None              # los flecos
        if y % 7 == 3:
            return ORO[4]                                      # las cruces
        if y % 7 in (2, 4) and x == w // 2:
            return ORO[3]
        return helado(CAPA[4] if x % 2 else CAPA[3], cara, x, y, w, h, 0.1, 12)

    def cruz(cara, x, y, w, h):
        return HIELO[4] if (x + y) % 3 == 0 else ORO[3]

    def mitra_banda(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return HIELO[2]
        # el oro de la banda, helado (visto a traves del hielo) con piedras azules
        if x % 4 == 2 and y == 0:
            return HIELO[4]
        return mezcla(ORO[2 + (x % 2)], HIELO[3], 0.35)

    def mitra_hoja(cara, x, y, w, h):
        # la hoja en punta: transparente fuera del arco apuntado (la punta arriba, en medio)
        if cara in ("frente", "espalda"):
            m = abs(x - (w - 1) / 2.0)
            if y < m * 1.9 - 2.4:
                return None
            if y >= h - 1:
                return mezcla(ORO[2], HIELO[3], 0.4)
            if abs(x - (w - 1) / 2.0) < 0.6 and y > 2:
                return mezcla(ORO[3], HIELO[3], 0.3)          # la franja de oro de en medio, bajo el hielo
            return hielo(cara, x, y, w, h, 1)
        if cara in ("der", "izq"):
            # los cantos: solo la parte de abajo (arriba es la punta)
            return hielo(cara, x, y, w, h, 2) if y >= 5 else None
        return HIELO[3]

    def mitra_medio(cara, x, y, w, h):
        return hielo(cara, x, y, w, h, 3)

    def mitra_cristal(cara, x, y, w, h):
        return HIELO[5] if (x + y) % 2 == 0 else HIELO[4]

    def infula(cara, x, y, w, h):
        if y >= h - 1:
            return ORO[3] if x % 2 == 0 else None
        if cara != "espalda":
            return CAPA[2]
        if y % 4 == 1:
            return ORO[3]
        return helado(CAPA[3], cara, x, y, w, h, 0.25, 13)

    def barba(cara, x, y, w, h):
        # la barba de hielo: blanca y azul, a mechones (columnas), mas clara arriba
        if y >= h - 1 and x % 2 == 0:
            return None
        k = (x * 5 + y // 3) % 4
        base = [ESCARCHA[1], HIELO[4], ESCARCHA[0], HIELO[3]][k]
        return base if y < h * 0.6 else mezcla(base, HIELO[2], 0.35)

    def carambano(cara, x, y, w, h):
        return HIELO[5] if y == 0 else (HIELO[4] if y < h - 1 else HIELO[3])

    def cristal(cara, x, y, w, h):
        if x in (0, w - 1):
            return HIELO[3]
        return HIELO[4] if (x + y) % 3 else HIELO[5]

    def vara(cara, x, y, w, h):
        c = metal(PLATA[1:5], 1121)(cara, x, y, w, h)
        # escarcha a trozos por la vara
        if _azar01(1121, CARAS[cara], x, y // 2) < 0.16:
            return ESCARCHA[(y // 2) % 3]
        return c

    def voluta(cara, x, y, w, h):
        return hielo(cara, x, y, w, h, 4)

    mats = {
        "cabeza": cabeza,
        "torso": sombra(torso),
        "brazo_d": brazo, "brazo_i": brazo,
        "antebrazo_d": antebrazo, "antebrazo_i": antebrazo,
        "muslo_d": muslo, "muslo_i": muslo,
        "pierna_d": pierna, "pierna_i": pierna,
        "alba_d": sombra(alba, abajo=0.1), "alba_i": sombra(alba, abajo=0.1),
        "alba_bajo_d": sombra(alba_bajo, abajo=0.05), "alba_bajo_i": sombra(alba_bajo, abajo=0.05),
        "manga_d": sombra(manga, abajo=0.1), "manga_i": sombra(manga, abajo=0.1),
        "capa_hombros": sombra(capa_hombros, abajo=0.1),
        "capa": capa,
        "capillo": capillo,
        "broche": broche,
        "estola": estola,
        "cruz": cruz,
        "mitra_banda": mitra_banda,
        "mitra_hoja": mitra_hoja,
        "mitra_medio": mitra_medio,
        "mitra_cristal": mitra_cristal,
        "infula": infula,
        "barba": barba,
        "carambano": carambano,
        "cristal": cristal,
        "vara": vara,
        "regaton": metal(PLATA[0:4], 1122),
        "nudo": metal(ORO[1:5], 1123),
        "voluta": voluta,
        "cristal_baculo": lambda cara, x, y, w, h: HIELO[5] if (x + y) % 2 else HIELO[4],
    }
    return mats


def brillo(variante):
    """Lo que luce: los ojos, el cristal del baculo y los de la mitra, y algo de los de la espalda."""
    C = {"a": OJO[0], "b": OJO[1], "c": OJO[2]}
    ojos = dibujo([
        "........",
        "........",
        "........",
        ".cb..bc.",
        ".a....a.",
    ], C)
    return {
        "cabeza": por_cara(liso(None), frente=ojos),
        "cristal_baculo": lambda cara, x, y, w, h: OJO[2] if (x + y) % 2 else OJO[1],
        "mitra_cristal": lambda cara, x, y, w, h: OJO[1] if (x + y) % 2 == 0 else None,
        "cristal": lambda cara, x, y, w, h: OJO[0] if (x * 3 + y) % 5 == 0 else None,
    }


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 16: llega
# a unos 13). El porte lleva el brazo derecho entero (el quieto no lo toca): se suma al quieto.

# de pie: enorme y algo encorvado, la cabeza baja; la izquierda caida por delante
BASE = dict(raiz=raiz(-0.2), torso=(10, 0, 0), cabeza=(8, 0, 0), muslo_d=(-3, 0, 3), pierna_d=(4, 0, 0), muslo_i=(2, 0, -3),
            pierna_i=(3, 0, 0), brazo_i=(-10, 0, -8), antebrazo_i=(-22, 0, 0), capa=(-3, 0, 0), infulas=(-6, 0, 0))
# el baculo de pie en la derecha, apoyado en el suelo junto al pie (la voluta arriba)
PORTE = dict(_mano_d=("h", -2.0, -9.5, 5.0), _arma=(0.0, 1.0, 0.06))
GUARDIA = dict(raiz=raiz(-1.4), torso=(18, 10, 0), cabeza=(-10, -8, 0), capa=(-8, 0, 0), infulas=(-12, 0, 0),
               muslo_d=(-16, 0, 6), pierna_d=(26, 0, 0), muslo_i=(-4, 0, -6), pierna_i=(20, 0, 0))
# en guardia: el baculo cruzado delante con las dos manos, la voluta arriba y adelante
LISTO = con(GUARDIA, _mano_d=("h", 3.0, -6.0, 7.5), _arma=(0.25, 0.75, 0.6), _agarra=-7)
# de rodillas rezando: el baculo de pie delante, las dos manos en el, la cabeza baja (asi espera en la cumbre)
RODILLAS = dict(raiz=raiz(-7.0, 0.9), torso=(16, 0, 0), cabeza=(28, 0, 0), capa=(14, 0, 0), infulas=(10, 0, 0),
                muslo_d=(-8, 0, 5), pierna_d=(96, 0, 0), muslo_i=(-6, 0, -5), pierna_i=(96, 0, 0))
REZANDO = con(RODILLAS, _mano_d=("h", 4.0, -4.0, 8.5), _arma=(0.0, 1.0, 0.02), _agarra=-3.5)
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

    def fija(pose):
        """La pose con sus objetivos ya resueltos: un bucle casi quieto no necesita un fotograma por tick."""
        return resolver(r, pose)

    # ---- quieto: respira hondo y despacio (sale vaho: lo pone el cliente)
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (2.5, con(BASE, torso=(7, 0, 0), cabeza=(5, 3, 0), raiz=raiz(0.1), brazo_i=(-12, 0, -9), capa=(-4, 0, 0))),
        (5.0, BASE),
    ])
    # ---- andar: pasos lentos y pesados; la capa se mece
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate(((-24, 18, 8, 20, 5, 0.0), (-6, 4, 6, 36, 0, 0.8), (18, -24, 20, 8, -5, 0.0),
                                                   (4, -6, 36, 6, 0, 0.8), (-24, 18, 8, 20, 5, 0.0))):
        pasos.append((2.0 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (10, ty, -ty * 0.3), "cabeza": (7, -ty * 0.6, 0), "raiz": raiz(up),
                                    "capa": (-2 + abs(md) * 0.2, 0, 0), "brazo_i": (-10 - mi * 0.3, 0, -8), "antebrazo_i": (-22, 0, 0)}))
    bucle("andar", 2.0, pasos)
    q = fija(con(BASE, PORTE))
    bucle("porte_abad", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    l0 = fija(LISTO)
    bucle("listo_abad", 2.4, [(0.0, l0), (1.2, fija(con(LISTO, raiz=raiz(-1.8), torso=(22, 7, 0), cabeza=(-12, -6, 2)))), (2.4, l0)])
    # ---- de rodillas, rezando: casi quieto (la cabeza baja un poco mas y vuelve)
    rz = fija(REZANDO)
    bucle("rezando", 6.0, [(0.0, rz), (3.0, con(rz, cabeza=(32, 0, 0), torso=(18, 0, 0))), (6.0, rz)])

    # ================================================================ acciones
    G = LISTO

    # ---- 1 baculazo: alza el baculo con las dos manos por encima de la cabeza y lo estrella delante (la voluta contra el suelo)
    BA_ALTO = con(G, raiz=raiz(0.2, 1.6), torso=(-14, 4, 0), cabeza=(-22, -4, 0), capa=(18, 0, 0), infulas=(14, 0, 0),
                  muslo_d=(10, 0, 6), pierna_d=(10, 0, 0), muslo_i=(-14, 0, -6), pierna_i=(12, 0, 0),
                  _mano_d=("h", 3.5, 11.5, 1.0), _arma=(0.05, 0.35, -0.94), _agarra=-6)
    BA_ABAJO = con(G, raiz=raiz(-4.2, -3.0), torso=(46, 0, 0), cabeza=(-26, 0, 0), capa=(-24, 0, 0), infulas=(-30, 0, 0),
                   muslo_d=(14, 0, 6), pierna_d=(30, 0, 0), muslo_i=(-52, 0, -6), pierna_i=(56, 0, 0),
                   _mano_d=("h", 3.0, -5.0, 11.5), _arma=(0.0, -0.55, 0.84), _agarra=-6)
    accion("baculazo", 2.5, [
        (0.00, G),
        (0.80, BA_ALTO),
        (1.10, con(BA_ALTO, raiz=raiz(0.4, 1.9), torso=(-18, 4, 0), cabeza=(-26, -4, 0))),
        (1.18, con(G, raiz=raiz(-1.8, -1.0), torso=(16, 0, 0), cabeza=(-12, 0, 0), _mano_d=("h", 3.0, 5.0, 10.0), _arma=(0.0, 0.2, 1),
                   _agarra=-6)),
        (1.26, BA_ABAJO),
        (1.85, con(BA_ABAJO, torso=(44, 0, 0))),
        (2.50, G),
    ], golpes=(1.25,))
    # ---- 2 barrido: echa el baculo atras a su derecha y barre a media altura de derecha a izquierda (con las dos manos)
    # (con la derecha sola: la izquierda suelta el baculo y abre el brazo para no perder el equilibrio)
    B_ATRAS = con(sin(G, "_agarra"), raiz=raiz(-1.0, 1.4), torso=(8, 60, 0), cabeza=(-6, -48, 0), muslo_d=(14, 0, 6), pierna_d=(20, 0, 0),
                  _mano_d=("h", -6.0, -1.5, -4.0), _arma=(-1, 0.15, -0.35), brazo_i=(-56, 0, -24), antebrazo_i=(-30, 0, 0))
    B_DELANTE = con(sin(G, "_agarra"), raiz=raiz(-2.0, -1.6), torso=(18, -6, 0), cabeza=(-14, 4, 0), muslo_i=(-30, 0, -6),
                    pierna_i=(30, 0, 0), _mano_d=("h", 2.5, -1.0, 12.0), _arma=(0.1, 0.0, 1), brazo_i=(-30, 0, -44), antebrazo_i=(-20, 0, 0))
    B_FIN = con(sin(G, "_agarra"), raiz=raiz(-1.8, -2.2), torso=(16, -60, 0), cabeza=(-10, 48, 0), muslo_i=(-34, 0, -6),
                pierna_i=(34, 0, 0), _mano_d=("h", 9.5, -1.0, 6.5), _arma=(1, 0.0, 0.25), brazo_i=(-14, 0, -58), antebrazo_i=(-14, 0, 0))
    accion("barrido", 2.1, [
        (0.00, G),
        (0.66, B_ATRAS),
        (0.92, con(B_ATRAS, torso=(8, 66, 0), cabeza=(-6, -54, 0))),
        (1.01, B_DELANTE),
        (1.10, B_FIN),
        (1.50, con(B_FIN, torso=(14, -64, 0))),
        (2.10, G),
    ], golpes=(1.03,))
    # ---- 3 pisoton: alza la rodilla derecha (el baculo en alto, a un lado) y pisa con todo su peso
    P_ALZA = con(G, raiz=raiz(0.6, 0.4), torso=(-6, 6, 0), cabeza=(-14, -4, 0), capa=(10, 0, 0),
                 muslo_d=(-78, 0, 8), pierna_d=(70, 0, 0), muslo_i=(-4, 0, -6), pierna_i=(8, 0, 0),
                 _mano_d=("h", -4.0, 6.0, 5.0), _arma=(-0.2, 1.0, 0.2), _agarra=-6)
    P_PISA = con(G, raiz=raiz(-3.2, 0.6), torso=(30, 0, 0), cabeza=(-20, 0, 0), capa=(-20, 0, 0), infulas=(-20, 0, 0),
                 muslo_d=(-30, 0, 8), pierna_d=(36, 0, 0), muslo_i=(20, 0, -6), pierna_i=(36, 0, 0),
                 _mano_d=("h", -1.0, -4.0, 9.0), _arma=(0.1, 0.8, 0.5), _agarra=-6)
    accion("pisoton", 1.9, [
        (0.00, G),
        (0.55, P_ALZA),
        (0.82, con(P_ALZA, raiz=raiz(0.9, 0.4), muslo_d=(-84, 0, 8))),
        (0.92, P_PISA),
        (1.25, con(P_PISA, torso=(26, 0, 0))),
        (1.90, G),
    ], golpes=(0.92,))
    # ---- 4 aliento: echa la cabeza atras, toma aire y sopla delante, barriendo de su derecha a su izquierda
    AL_TOMA = con(G, raiz=raiz(0.2, 1.2), torso=(-16, 0, 0), cabeza=(-34, 0, 0), capa=(18, 0, 0), infulas=(20, 0, 0),
                  _mano_d=("h", -2.5, -7.0, 5.0), _arma=(0.0, 1.0, 0.1),
                  brazo_i=(-20, 0, -40), antebrazo_i=(-30, 0, 0))
    AL_SOPLA = con(G, raiz=raiz(-1.8, -1.2), torso=(30, 26, 0), cabeza=(-18, 10, 0), capa=(-14, 0, 0), infulas=(-18, 0, 0),
                   _mano_d=("h", -2.5, -7.0, 5.0), _arma=(0.0, 1.0, 0.1),
                   brazo_i=(-50, 0, -30), antebrazo_i=(-20, 0, 0))
    AL_FIN = con(AL_SOPLA, torso=(30, -26, 0), cabeza=(-18, -10, 0))
    for p in (AL_TOMA, AL_SOPLA, AL_FIN):
        p.pop("_agarra", None)
    accion("aliento", 3.0, [
        (0.00, G),
        (0.55, AL_TOMA),
        (0.80, AL_SOPLA),
        (1.60, con(AL_SOPLA, torso=(30, 0, 0), cabeza=(-18, 0, 0))),
        (2.40, AL_FIN),
        (3.00, G),
    ], golpes=(0.80,))
    # ---- 5 carambanos: alza el baculo al cielo con la derecha y la izquierda abierta arriba; los carambanos se forman y caen
    CA_ALZA = con(BASE, raiz=raiz(0.4), torso=(-12, 0, 0), cabeza=(-36, 0, 0), capa=(16, 0, 0), infulas=(20, 0, 0),
                  _mano_d=("h", -1.0, 12.5, 2.0), _arma=(0.0, 1.0, 0.15),
                  brazo_i=(-160, 0, -24), antebrazo_i=(-10, 0, 0))
    accion("carambanos", 2.6, [
        (0.00, G),
        (0.40, con(BASE, torso=(20, 0, 0), _mano_d=("h", -2.0, -4.0, 7.0), _arma=(0.0, 1.0, 0.2), brazo_i=(-40, 0, -16), antebrazo_i=(-50, 0, 0))),
        (0.90, CA_ALZA),
        (1.20, con(CA_ALZA, raiz=raiz(0.6), torso=(-16, 0, 0))),
        (1.70, con(CA_ALZA, torso=(-14, 0, 3))),
        (2.00, CA_ALZA),
        (2.60, G),
    ], golpes=(1.20,))
    # ---- 6 grito: abre los brazos (el baculo en alto a su derecha), la cabeza atras: la cumbre entera le contesta
    GR = con(BASE, raiz=raiz(-0.4, 1.0), torso=(-18, 0, 0), cabeza=(-40, 0, 0), capa=(24, 0, 0), infulas=(26, 0, 0),
             _mano_d=("h", -6.0, 10.0, 3.0), _arma=(-0.3, 1, 0.1),
             brazo_i=(-30, 0, -80), antebrazo_i=(-14, 0, 0))
    accion("grito", 2.6, [
        (0.00, G),
        (0.45, con(GR, torso=(26, 0, 0), cabeza=(10, 0, 0), raiz=raiz(-2.4))),
        (0.90, GR),
        (1.40, con(GR, torso=(-22, 0, 4), cabeza=(-44, 0, 6))),
        (1.95, con(GR, torso=(-16, 0, -4), cabeza=(-38, 0, -6))),
        (2.60, G),
    ])
    # ---- 7 aturdido
    AT = con(G, raiz=raiz(-1.6, 2.2), torso=(-10, 0, 8), cabeza=(-22, 0, 14), capa=(14, 0, 0), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0),
             muslo_i=(12, 0, -8), pierna_i=(18, 0, 0), _mano_d=("h", -3.0, -9.0, 3.0), _arma=(-0.2, -0.3, 1), brazo_i=(16, 0, -30),
             antebrazo_i=(-16, 0, 0))
    AT.pop("_agarra", None)
    accion("aturdido", 1.6, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.22, AT),
        (0.70, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.8))),
        (1.12, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.4))),
        (1.60, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 8 morir: suelta el aire, cae de rodillas con el baculo delante y se queda apoyado en el, la cabeza baja: vencido
    VENCIDO = con(RODILLAS, raiz=raiz(-7.2, 0.6), torso=(30, 0, 4), cabeza=(42, 0, 8), capa=(24, 0, 0),
                  _mano_d=("h", 4.0, -6.5, 9.0), _arma=(0.0, 1.0, 0.25), _agarra=-3.0)
    accion("morir", 4.0, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), capa=(12, 0, 0), _mano_d=("h", -2.0, -8.0, 6.0), _arma=(0.0, 1.0, 0.1))),
        (0.80, con(REZANDO, raiz=raiz(-6.0, 0.9), torso=(4, 0, 0), cabeza=(-14, 0, 0))),
        (1.80, con(REZANDO, torso=(10, 0, 0), cabeza=(-6, 0, 0))),
        (2.80, VENCIDO),
        (4.00, VENCIDO),
    ])
    # ---- 9 levantarse: estaba de rodillas rezando; se apoya en el baculo, se pone en pie y lo cruza delante
    accion("levantarse", 2.2, [
        (0.00, REZANDO),
        (0.70, con(REZANDO, torso=(6, 0, 0), cabeza=(-6, 0, 0), muslo_d=(-70, 0, 5), pierna_d=(80, 0, 0), raiz=raiz(-5.2, 0.6))),
        (1.40, con(BASE, PORTE, raiz=raiz(-1.0), torso=(16, 0, 0), cabeza=(-6, 0, 0))),
        (2.20, G),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_abad"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_abad"]]
    if nombre == "quieto":
        return [anims["porte_abad"]]
    return []
