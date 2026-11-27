"""
El centinela de la casa de la Sombra (parte 7ab): las armaduras de los caballeros de la ciudadela. La Sombra se quedo con lo
que habia dentro y dejo la cascara: por las juntas sale humo negro y tras la rendija del yelmo arden dos brasas rojas ("nunca
aprendieron a morir", dice la Sombra). Hierro negro con los cantos de acero frio, la sobrevesta morada hecha jirones, la capa
rota, un yelmo cerrado de cresta alta y, en el peto, la marca de la Sombra (luce). Mas grande que un hombre (x1,3).

Dos variantes:
  0 espadon: un montante de hoja ancha y mellada, a dos manos. Tajo de lado a lado a la altura del pecho y golpe de arriba
    abajo que agrieta el suelo (la onda alrededor del golpe).
  1 alabarda: la pica a fondo (llega lejos), el barrido de vuelta entera y el hachazo de arriba abajo.
Los dos: el humo (se deshace en humo, cruza hasta su presa y sale de el con una estocada) y, al morir, se desploma: las piezas
caen y queda un monton de hierro (asi esperan algunos, tirados: se levantan juntandose al acercarse alguien).
"""
import guardia as G
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, mezcla, metal, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "centinela"
VARIANTES = ("espadon", "alabarda")
ESCALA = 1.3

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r)
    # la coraza (peto y espaldar), la gola del cuello y las escarcelas de laminas
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, "coraza", grow=g(0.6))])
    r.pieza("gola", "torso", (0, -12, 0), boxes=[Box(-3.4, -1.4, -2.4, 6.8, 2.0, 4.8, "gola")])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2, 8, 7, 4, "faldon", grow=g(0.5))])
    # la sobrevesta: dos jirones (delante y detras) que cuelgan del cinto
    r.pieza("sobrevesta", "cadera", (0, -0.5, 0), boxes=[
        Box(-2.6, 0, -3.0, 5.2, 11.5, 0.6, "jiron"),
        Box(-2.6, 0, 2.4, 5.2, 10.5, 0.6, "jiron"),
    ])
    for n in ("d", "i"):
        # las hombreras: tres laminas y una pua
        s = -1 if n == "d" else 1
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[
            Box(-3.2, -3.6, -3.0, 6.4, 3.8, 6.0, f"hombrera_{n}"),
            Box(-3.4, -1.2, -3.2, 6.8, 1.8, 6.4, f"lamina_{n}"),
            Box(-0.6 + s * 0.8, -6.2, -0.6, 1.2, 2.8, 1.2, "pua"),
        ])
        # los guanteletes (por encima del antebrazo)
        r.pieza(f"guante_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-2.3, 3.0, -2.3, 4.6, 3.4, 4.6, f"guante_{n}")])
    # el yelmo: cerrado, con la rendija de la vista (en la textura), la cresta y un penacho negro y roto
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8.5, -4, 8, 8.5, 8, "yelmo", grow=g(0.7)),
        Box(-0.6, -11.2, -4.6, 1.2, 2.4, 9.2, "cresta"),
    ])
    # la capa rota, de los hombros a las corvas
    r.pieza("capa", "torso", (0, -11.6, 2.7), rot=(6, 0, 0), boxes=[Box(-5, 0, 0, 10, 19, 1, "capa")])
    # ---- 0, el espadon (a dos manos: la derecha junto a la guarda, la izquierda en el pomo)
    r.pieza("espadon", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -2.5, 1.2, 1.2, 7, "puno"),
        Box(-1.3, -1.3, 4.5, 2.6, 2.6, 1.6, "pomo"),
        Box(-5.0, -0.9, -3.8, 10, 1.8, 1.5, "guarda"),
        Box(-0.7, -1.8, -22.5, 1.4, 3.6, 19, "hoja"),
        Box(-0.7, -1.0, -25.0, 1.4, 2.0, 2.5, "hoja_punta"),
    ])
    # ---- 1, la alabarda (de asta larga: la cuchilla, el gancho y la pica arriba; el regaton abajo)
    r.pieza("alabarda", "mano_d", (0, 0, 0), boxes=[
        Box(-0.55, -0.55, -27, 1.1, 1.1, 42, "asta"),
        Box(-0.55, -7.0, -26, 1.1, 6.5, 7, "cuchilla"),
        Box(-0.55, 0.5, -24.5, 1.1, 3.0, 2.5, "gancho"),
        Box(-0.55, -0.55, -34, 1.1, 1.1, 7, "pica"),
        Box(-0.8, -0.8, 15, 1.6, 1.6, 1.5, "regaton"),
    ])
    r.empaquetar()
    return r


#: lo que esconde cada variante (el arma de la otra)
OCULTAS = {0: ["alabarda"], 1: ["espadon"]}

# ---------------------------------------------------------------- las texturas

NEGRO = ["#08090b", "#101216", "#181b20", "#22262c", "#2e333a", "#3d434b"]
ACERO = ["#4a525b", "#5f6871", "#7a848e", "#98a2ab"]
MORADO = ["#140a18", "#1f1024", "#2b1632", "#3a1e43", "#4a2755"]
HUECO = "#030304"
HUMO = "#1a1a1f"
MARCA = "#e0281e"
BRASA = "#ff4a2a"


def materiales(variante):
    placa = metal(NEGRO[1:6], 801)
    placa_b = metal(NEGRO[0:5], 802)
    acero = metal(ACERO, 803)
    sobre = tela(MORADO, 811, pliegue=3, grano=0.16, luz=0.2)
    capa_t = tela(MORADO[:4], 812, pliegue=4, grano=0.18, luz=0.15)

    def mancha(c, cara, x, y, semilla):
        # alguna mancha de herrumbre negra y un arañazo claro
        k = (x * 7 + y * 13 + semilla + {"frente": 0, "espalda": 5, "der": 9, "izq": 3, "arriba": 2, "abajo": 6}.get(cara, 1)) % 41
        if k == 0:
            return mezcla(c, ACERO[1], 0.35)
        if k in (5, 17):
            return oscuro(c, 0.3)
        return c

    def hueco(cara, x, y, w, h):
        # lo de dentro de la armadura: nada (negro, con algo de humo)
        return HUMO if (x + y * 3) % 7 == 0 else HUECO

    def coraza(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = mancha(placa(cara, x, y, w, h), cara, x, y, 3)
        if cara == "arriba":
            return c
        if cara == "frente":
            # la arista del peto y la marca de la Sombra (un ojo en un aro roto, en oscuro: lo rojo es del brillo)
            if x in (3, 4) and y <= 8:
                return claro(c, 0.12)
            if y in (9, 10):
                return acero(cara, x, y, w, h) if y == 9 else NEGRO[0]
        if y == h - 1:
            return acero(cara, x, y, w, h)
        if cara in ("der", "izq") and y in (2, 3) and delante(cara, x, w) <= 1:
            return HUECO                                           # la sisa: dentro, nada
        return c

    def gola(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return HUECO if cara == "arriba" and 1 <= x <= w - 2 and 1 <= y <= h - 2 else None
        return acero(cara, x, y, w, h) if y == 0 else placa_b(cara, x, y, w, h)

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if y == h - 1:
            return acero(cara, x, y, w, h)
        c = placa_b(cara, x, y, w, h)
        return oscuro(c, 0.35) if y % 2 == 1 else c

    def jiron(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 5 + 3) % 4)
        if y > largo or (x * 3 + y * 5) % 19 == 0:
            return None
        c = sobre(cara, x, y, w, h)
        return oscuro(c, 0.05 + 0.04 * y)

    def brazo(cara, x, y, w, h):
        return mancha(placa_b(cara, x, y, w, h), cara, x, y, 7)

    def antebrazo(cara, x, y, w, h):
        if y == 0:
            return acero(cara, x, y, w, h)
        return placa(cara, x, y, w, h)

    def guante(cara, x, y, w, h):
        if cara == "arriba":
            return HUECO                                           # la boca del guantelete: hueco
        c = placa_b(cara, x, y, w, h)
        if cara == "abajo":
            return NEGRO[0]
        return claro(c, 0.1) if (x + y) % 3 == 0 else c

    def pierna(cara, x, y, w, h):
        if y == 0:
            return acero(cara, x, y, w, h) if cara == "frente" else placa(cara, x, y, w, h)   # la rodillera
        if y >= h - 1:
            return NEGRO[1] if cara != "abajo" else NEGRO[0]                               # el escarpe
        return mancha(placa_b(cara, x, y, w, h), cara, x, y, 11)

    def muslo(cara, x, y, w, h):
        return mancha(placa(cara, x, y, w, h), cara, x, y, 13)

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return acero(cara, x, y, w, h)
        return mancha(c, cara, x, y, 17)

    def lamina(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return acero(cara, x, y, w, h) if y == h - 1 else placa_b(cara, x, y, w, h)

    def yelmo(cara, x, y, w, h):
        if cara == "abajo":
            return HUECO
        c = placa(cara, x, y, w, h)
        if cara == "frente":
            if y in (3, 4) and 1 <= x <= 6:
                return HUECO                                        # la rendija de la vista: dentro, nada
            if x in (3, 4) and y >= 5:
                return claro(c, 0.15)                               # la arista de la cara
            if y >= 6 and (x + y) % 2 == 0 and x not in (3, 4):
                return NEGRO[0]                                     # los respiraderos
        if cara in ("der", "izq") and y in (3, 4) and delante(cara, x, w) <= 1:
            return HUECO
        if y == h - 1:
            return acero(cara, x, y, w, h)
        return mancha(c, cara, x, y, 19)

    def cresta(cara, x, y, w, h):
        return acero(cara, x, y, w, h) if cara == "arriba" else placa(cara, x, y, w, h)

    def penacho(cara, x, y, w, h):
        if cara in ("abajo",):
            return None
        if y >= h - 2 and (x + y) % 2 == 0:
            return None
        return MORADO[(x * 2 + y) % 3]

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 1) % 5)
        if y > largo or (x * 5 + y * 7) % 23 == 0:
            return None
        c = capa_t(cara, x, y, w, h)
        return oscuro(c, 0.04 * y / 3) if cara == "espalda" else oscuro(c, 0.3)

    hierro = metal(G.HIERRO[1:6], 821)

    def hoja(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara in ("der", "izq"):
            if y in (0, h - 1):
                # el filo, con mellas
                return None if (x * 7 + y) % 9 == 0 else claro(c, 0.22)
            if y == h // 2:
                return oscuro(c, 0.3)                               # la acanaladura
        return oscuro(c, 0.15)

    def asta(cara, x, y, w, h):
        return ruido(G.PALO, 831, grano=0.35)(cara, x, y, w, h) if (x + y) % 9 else NEGRO[2]

    def cuchilla(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara in ("der", "izq") and (x == 0 or y == h - 1):
            return claro(c, 0.2)
        return oscuro(c, 0.12)

    return {
        "cabeza": hueco, "torso": hueco,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12), "antebrazo_i": sombra(antebrazo, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.12), "muslo_i": sombra(muslo, abajo=0.12),
        "pierna_d": sombra(pierna, abajo=0.16), "pierna_i": sombra(pierna, abajo=0.16),
        "coraza": sombra(coraza, abajo=0.14), "gola": gola, "faldon": sombra(faldon, abajo=0.1), "jiron": jiron,
        "hombrera_d": sombra(hombrera, abajo=0.1), "hombrera_i": sombra(hombrera, abajo=0.1),
        "lamina_d": lamina, "lamina_i": lamina, "pua": liso(ACERO[1]),
        "guante_d": guante, "guante_i": guante,
        "yelmo": sombra(yelmo, abajo=0.12), "cresta": cresta, "capa": capa,
        "puno": ruido(G.CUERO, 841, grano=0.4), "pomo": liso(ACERO[0]), "guarda": metal(NEGRO[2:6], 842),
        "hoja": hoja, "hoja_punta": hoja,
        "asta": asta, "cuchilla": cuchilla, "gancho": liso(NEGRO[3]), "pica": cuchilla, "regaton": liso(NEGRO[2]),
    }


def brillo(variante):
    """Lo que brilla: las dos brasas tras la rendija del yelmo y la marca de la Sombra en el peto."""
    ojos = dibujo([
        "........",
        "........",
        "........",
        ".R....R.",
        "........",
    ], {"R": BRASA})
    # la marca: un ojo en un aro roto con tres espinas (8 x 10 del peto)
    marca = dibujo([
        "........",
        "..MMMM..",
        ".M....M.",
        "M..mm..M",
        "M.mMMm.M",
        "M..mm..M",
        ".M....M.",
        "..M..M..",
        "...MM...",
        "........",
    ], {"M": MARCA, "m": "#7a120e"})
    return {"yelmo": por_cara(liso(None), frente=ojos), "coraza": por_cara(liso(None), frente=marca)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno derecho respecto a su hombro; _agarra = la izquierda en el arma, a tantos pixeles de la
# derecha (positivo: detras, hacia el pomo; negativo: delante, por el asta)

BASE = dict(raiz=raiz(-0.2), torso=(1, 0, 0), cabeza=(2, 0, 0),
            muslo_d=(0, 0, 5), pierna_d=(2, 0, 0), muslo_i=(0, 0, -5), pierna_i=(2, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
PORTE = {
    # el espadon de punta en el suelo delante de el, las dos manos en el pomo
    "espadon": dict(_mano_d=("h", 5.2, -0.6, 5.2), _arma=(0.0, -1.0, 0.04), _agarra=3.0),
    # la alabarda derecha, el regaton en el suelo, la izquierda mas arriba en el asta
    "alabarda": dict(_mano_d=("h", -0.8, -4.2, 3.4), _arma=(0.0, 1.0, 0.03), brazo_i=(4, 0, -6), antebrazo_i=(-12, 0, 0)),
}
GUARDIA = dict(raiz=raiz(-1.2), torso=(8, 22, 0), cabeza=(-4, -20, 0),
               muslo_d=(12, 0, 6), pierna_d=(16, 0, 0), muslo_i=(-22, 0, -6), pierna_i=(24, 0, 0))
LISTO = {
    # a dos manos, la punta a la cara del otro
    "espadon": con(GUARDIA, _mano_d=("h", 3.0, -3.4, 6.4), _arma=(-0.1, 0.45, 1), _agarra=3.4),
    # la alabarda baja, la pica hacia el pecho del otro
    "alabarda": con(GUARDIA, torso=(10, 34, 0), cabeza=(-6, -30, 0), _mano_d=("h", 2.4, -6.2, 1.6), _arma=(0.0, 0.2, 1), _agarra=-8),
}
#: dormido (de pie e inmovil como una armadura vacia: la cabeza caida sobre el pecho)
DORMIDO = dict(raiz=raiz(-0.4), torso=(6, 0, 0), cabeza=(34, 0, 4))
#: el monton de hierro (tirado boca arriba, desarmado: el yelmo de lado, las piernas torcidas, los brazos abiertos)
MONTON = dict(raiz=raiz(2.6, 10.0, rot=(-84, 0, 14)), torso=(0, 0, 0), cabeza=(26, 64, 40),
              muslo_d=(-24, 0, 30), pierna_d=(70, 0, 0), muslo_i=(-74, 0, -18), pierna_i=(88, 0, 0),
              _mano_d=("h", -3.4, -8.0, 1.6), _arma=(-0.15, -1.0, 0.1), brazo_i=(24, 0, -78), antebrazo_i=(-30, 0, 0))


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

    # ---- quieto: como una estatua; la cabeza gira despacio de vez en cuando
    bucle("quieto", 7.0, [
        (0.0, BASE),
        (2.5, con(BASE, cabeza=(2, -18, 0))),
        (4.5, con(BASE, cabeza=(4, 16, 0))),
        (7.0, BASE),
    ])
    # ---- andar: pasos pesados, la capa y los jirones detras
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-26, 20, 6, 18, 4, 0.0), (-2, 2, 4, 38, 0, 0.7), (20, -26, 18, 6, -4, 0.0), (2, -2, 38, 4, 0, 0.7), (-26, 20, 6, 18, 4, 0.0))):
        pasos.append((1.6 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (3, ty, -ty * 0.3), "cabeza": (0, -ty * 0.6, ty * 0.3), "raiz": raiz(up),
                                    "capa": (14 + 6 * up, ty * 0.5, 0), "sobrevesta": (-6 * (md / 26.0), 0, 0)}))
    bucle("andar", 1.6, pasos)
    # ---- como lleva cada uno su arma (se suma; solo los brazos)
    for nombre, p in PORTE.items():
        q = con(BASE, p)
        bucle(f"porte_{nombre}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    # ---- en guardia
    for nombre, p in LISTO.items():
        bucle(f"listo_{nombre}", 2.0, [
            (0.0, p),
            (1.0, con(p, raiz=raiz(-1.6), torso=(p["torso"][0] + 2, p["torso"][1], 0))),
            (2.0, p),
        ])
    # ---- dormido (velar): de pie e inmovil, la cabeza caida; respira humo (casi no se mueve)
    for nombre, p in PORTE.items():
        q = con(BASE, DORMIDO, p)
        bucle(f"dormido_{nombre}", 6.0, [(0.0, q), (3.0, con(q, cabeza=(36, 0, 4))), (6.0, q)])
    # ---- tirado: el monton de hierro
    TIRADO = dict(MONTON)
    bucle("tumbado", 4.0, [(0.0, TIRADO), (4.0, TIRADO)])

    # ================================================================ acciones
    E, H = con(BASE, LISTO["espadon"]), con(BASE, LISTO["alabarda"])
    # ---- espadon: el tajo de lado a lado, a la altura del pecho (se esquiva agachandose... o rodando hacia atras)
    ATRAS = con(E, raiz=raiz(-1.6, 1.0), torso=(8, 64, 0), cabeza=(-6, -54, 0),
                _mano_d=("h", -3.6, -0.6, 0.6), _arma=(-0.9, 0.2, -0.4), _agarra=3.4)
    CRUZA = con(E, raiz=raiz(-2.0, -1.6), torso=(12, -36, 0), cabeza=(-8, 30, 0), muslo_i=(-36, 0, -6), pierna_i=(32, 0, 0),
                _mano_d=("h", 7.4, -1.6, 2.6), _arma=(0.94, 0.05, -0.3), _agarra=3.4)
    accion("tajo", 1.8, [
        (0.00, E),
        (0.50, ATRAS),
        (0.78, con(ATRAS, torso=(8, 68, 0))),
        (0.88, con(E, raiz=raiz(-1.8, -0.6), torso=(10, 8, 0), _mano_d=("h", 0.0, -1.4, 9.2), _arma=(0.1, 0.1, 1), _agarra=3.4)),
        (0.96, CRUZA),
        (1.30, con(CRUZA, torso=(12, -40, 0))),
        (1.80, E),
    ], golpes=(0.88,))
    # ---- espadon: el golpe de arriba abajo (lo alza muy arriba, casi un segundo; el suelo se agrieta alrededor)
    ALZA = con(E, raiz=raiz(-0.4, 1.4), torso=(-12, 8, 0), cabeza=(-8, -6, 0),
               _mano_d=("h", 4.4, 8.2, -0.8), _arma=(0.0, 0.5, -0.86), _agarra=3.4)
    CLAVA = con(E, raiz=raiz(-3.4, -3.2), torso=(34, 4, 0), cabeza=(-14, -2, 0),
                muslo_i=(-50, 0, -6), pierna_i=(48, 0, 0), muslo_d=(26, 0, 6), pierna_d=(10, 0, 0),
                _mano_d=("h", 2.4, -6.6, 6.4), _arma=(0.0, -0.78, 0.62), _agarra=3.4)
    accion("golpe", 2.0, [
        (0.00, E),
        (0.60, ALZA),
        (0.92, con(ALZA, raiz=raiz(-0.2, 1.8), torso=(-16, 8, 0), _mano_d=("h", 4.2, 8.6, -1.6))),
        (0.96, con(E, raiz=raiz(-1.0, 0.4), torso=(-4, 6, 0), _mano_d=("h", 4.6, 7.4, 3.2), _arma=(0.0, 0.95, 0.3), _agarra=3.4)),
        (0.99, con(E, raiz=raiz(-2.0, -1.4), torso=(16, 5, 0), _mano_d=("h", 4.0, 1.8, 8.2), _arma=(0.0, 0.2, 1), _agarra=3.4)),
        (1.02, CLAVA),
        (1.50, con(CLAVA, torso=(32, 4, 0))),
        (2.00, E),
    ], golpes=(1.02,))
    # ---- alabarda: la pica a fondo (se echa atras y llega lejos; se esquiva de lado)
    ATRAS_H = con(H, raiz=raiz(-1.4, 2.0), torso=(6, 52, 0), cabeza=(-4, -44, 0),
                  muslo_d=(18, 0, 6), pierna_d=(20, 0, 0), muslo_i=(-28, 0, -4), pierna_i=(14, 0, 0),
                  _mano_d=("h", 1.5, -5.4, -2.8), _arma=(0.0, 0.06, 1), _agarra=-8)
    FONDO_H = con(sin(H, "_agarra"), raiz=raiz(-2.6, -5.2), torso=(24, -20, 0), cabeza=(-18, 18, 0),
                  muslo_i=(-54, 0, -5), pierna_i=(48, 0, 0), muslo_d=(28, 0, 5), pierna_d=(8, 0, 0),
                  _mano_d=("h", 3.4, -2.4, 8.0), _arma=(0.0, 0.02, 1), brazo_i=(-64, 0, -10), antebrazo_i=(-16, 0, 0))
    accion("estocada", 1.7, [
        (0.00, H),
        (0.55, ATRAS_H),
        (0.86, con(ATRAS_H, raiz=raiz(-1.4, 2.4), torso=(4, 56, 0))),
        (0.95, FONDO_H),
        (1.25, con(FONDO_H, torso=(22, -18, 0))),
        (1.70, H),
    ], golpes=(0.95,))
    # ---- alabarda: el barrido de vuelta entera (gira sobre si mismo con el arma tendida: no vale echarse atras)
    TENDIDA = con(H, raiz=raiz(-1.8, 0.0), torso=(10, 0, 0), cabeza=(-6, 0, 0),
                  _mano_d=("h", 6.0, -3.0, 4.4), _arma=(0.96, 0.0, 0.25), _agarra=-6)
    vuelta = []
    for k in range(9):
        a = 360.0 * k / 8
        vuelta.append((0.62 + 0.52 * k / 8, con(TENDIDA, raiz=raiz(-1.8 - 0.4 * (k % 2), 0.0, rot=(0, -a, 0)))))
    accion("barrido", 2.0, [
        (0.00, H),
        (0.50, con(TENDIDA, raiz=raiz(-1.4, 0.4, rot=(0, 30, 0)), torso=(6, 30, 0))),
        *vuelta,
        (1.60, con(TENDIDA, raiz=raiz(-1.6, 0.0, rot=(0, -370, 0)))),
        (2.00, con(H, raiz=raiz(-1.2, 0.0, rot=(0, -360, 0)))),
    ], golpes=(0.88, 1.01))
    # ---- alabarda: el hachazo (la alza con las dos manos y la descarga; parte la guardia)
    ALTO_H = con(H, raiz=raiz(-0.2, 1.4), torso=(-14, 14, 0), cabeza=(-10, -12, 0),
                 muslo_d=(6, 0, 5), pierna_d=(8, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(10, 0, 0),
                 _mano_d=("h", 2.4, 6.6, 0.6), _arma=(0.0, 0.85, -0.5), _agarra=-7)
    BAJO_H = con(H, raiz=raiz(-2.8, -3.0), torso=(36, 10, 0), cabeza=(-26, -8, 0), muslo_i=(-46, 0, -5), pierna_i=(44, 0, 0),
                 _mano_d=("h", 2.6, -6.0, 4.6), _arma=(0.0, -0.55, 1), _agarra=-7)
    accion("hachazo", 2.0, [
        (0.00, H),
        (0.65, ALTO_H),
        (1.00, con(ALTO_H, raiz=raiz(0.0, 1.7), torso=(-18, 14, 0))),
        (1.10, BAJO_H),
        (1.50, con(BAJO_H, torso=(34, 10, 0))),
        (2.00, H),
    ], golpes=(1.10,))
    # ---- el humo (los dos): se encoge (se deshace en humo: lo hace el juego), cruza y sale con una estocada
    for nombre, L in (("humo_espadon", E), ("humo_alabarda", H)):
        ENCOGE = con(L, raiz=raiz(-4.0, 1.0), torso=(40, 10, 0), cabeza=(-20, -6, 0),
                     muslo_d=(-40, 0, 6), pierna_d=(70, 0, 0), muslo_i=(-60, 0, -6), pierna_i=(80, 0, 0))
        SALE = con(sin(L, "_agarra"), raiz=raiz(-2.4, -6.0), torso=(26, -10, 0), cabeza=(-18, 10, 0),
                   muslo_i=(-56, 0, -5), pierna_i=(48, 0, 0), muslo_d=(30, 0, 5), pierna_d=(8, 0, 0),
                   _mano_d=("h", 2.0, -1.4, 9.2), _arma=(0.0, 0.0, 1), brazo_i=(-60, 0, -14), antebrazo_i=(-20, 0, 0))
        accion(nombre, 1.5, [
            (0.00, L),
            (0.35, ENCOGE),
            (0.55, con(ENCOGE, raiz=raiz(-4.4, 0.0))),
            (0.80, con(ENCOGE, raiz=raiz(-3.0, -2.0), torso=(30, 0, 0))),
            (0.88, SALE),
            (1.10, con(SALE, torso=(24, -12, 0))),
            (1.50, L),
        ], golpes=(0.88,))
    # ---- despertar (de dormido): levanta la cabeza (se le encienden las brasas), se sacude y se pone en guardia
    for nombre, L in (("despertar_espadon", E), ("despertar_alabarda", H)):
        D = con(BASE, DORMIDO, PORTE[nombre.split("_")[1]])
        accion(nombre, 1.6, [
            (0.00, D),
            (0.35, con(D, cabeza=(-10, 0, 0), torso=(-4, 0, 0))),
            (0.55, con(D, cabeza=(-6, 18, 6), torso=(-2, -8, 4))),
            (0.75, con(D, cabeza=(-6, -16, -6), torso=(-2, 8, -4))),
            (1.10, con(L, raiz=raiz(-1.0, 0.6))),
            (1.60, L),
        ])
    # ---- levantarse (del monton): las piezas se juntan y se alza
    for nombre, L in (("levantarse_espadon", E), ("levantarse_alabarda", H)):
        accion(nombre, 2.2, [
            (0.00, MONTON),
            (0.40, con(MONTON, raiz=raiz(1.2, 8.0, rot=(-70, 0, 10)), cabeza=(10, 30, 20))),
            (0.90, con(BASE, raiz=raiz(-7.0, 1.0), torso=(40, 0, 0), cabeza=(30, 0, 0),
                       muslo_d=(-80, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(100, 0, 0),
                       brazo_d=(-30, 0, 20), antebrazo_d=(-40, 0, 0), brazo_i=(-30, 0, -20), antebrazo_i=(-40, 0, 0))),
            (1.50, con(L, raiz=raiz(-2.4, 0.4), torso=(16, 0, 0))),
            (2.20, L),
        ])
    # ---- aturdido
    for nombre, L in (("aturdido_espadon", E), ("aturdido_alabarda", H)):
        accion(nombre, 1.6, [
            (0.00, L),
            (0.18, con(L, raiz=raiz(-2.2, 2.0), torso=(-16, 26, 0), cabeza=(-24, -10, 0), muslo_i=(-10, 0, -6))),
            (0.80, con(L, raiz=raiz(-2.6, 1.6), torso=(-10, 18, 0), cabeza=(-8, 12, 0))),
            (1.20, con(L, raiz=raiz(-1.8, 0.8), torso=(0, 20, 0), cabeza=(-4, -10, 0))),
            (1.60, L),
        ])
    # ---- morir: se le doblan las rodillas, se le va el yelmo hacia un lado y se desploma: queda el monton de hierro
    for nombre, L in (("morir_espadon", E), ("morir_alabarda", H)):
        accion(nombre, 2.4, [
            (0.00, L),
            (0.35, con(L, raiz=raiz(-3.0, 0.6), torso=(-12, 10, 0), cabeza=(-30, 20, 0))),
            (0.80, con(BASE, raiz=raiz(-7.4, 0.8), torso=(30, 6, 8), cabeza=(50, 30, 20),
                       muslo_d=(-84, 0, 8), pierna_d=(98, 0, 0), muslo_i=(-60, 0, -8), pierna_i=(96, 0, 0),
                       brazo_d=(-10, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(-10, 0, -30), antebrazo_i=(-20, 0, 0))),
            (1.40, con(MONTON, raiz=raiz(0.8, 6.0, rot=(-60, 0, 20)))),
            (1.80, MONTON),
            (2.40, MONTON),
        ])
    return B, A


def variante_de(nombre):
    if nombre in ("tajo", "golpe") or nombre.endswith("espadon"):
        return 0
    if nombre in ("estocada", "barrido", "hachazo") or nombre.endswith("alabarda"):
        return 1
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre in ("andar", "quieto"):
        return [anims["porte_espadon"]]
    return []


TABLA_EXTRA = ()
