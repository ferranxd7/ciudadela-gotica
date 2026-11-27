"""
La Sombra sin Nombre (parte 7ac): lo que espera en el corazon de la aguja de la ciudadela, encadenado. Altisima (x2,4: unos seis
bloques), flota: no tiene piernas, la tunica negra y hecha jirones acaba en humo. Una capucha honda (dentro no hay cara: oscuridad y
dos ojos rojos), y encima, flotando, la corona rota de Corvin (el oro viejo agrietado, con su gema). Un manto de placas de hueso negro
con puas en los hombros, el pecho abierto como una jaula de costillas y dentro su corazon, rojo, que late. Brazos largos y flacos de
sombra hasta las rodillas, con grilletes de hierro en las munecas de los que cuelgan los eslabones de las cadenas que lo sujetaban, y
garras enormes. La capa le cae por la espalda hasta el suelo.

Dos variantes (la misma Sombra):
  0 la Sombra: dos ojos.
  1 a media vida (se rompe la ultima cadena: "¡Basta!"): se le abren mas ojos en la oscuridad de la capucha (seis), se agrieta en rojo
    (el pecho, los brazos) y el bajo de la tunica arde.
"""
from anim import Anim, con, raiz, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide  # noqa: F401
from pinta import (claro, delante, dibujo, liso, mezcla, metal, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "sombra"
VARIANTES = ("sombra", "furia")
ESCALA = 2.4
CENTRO_Y = 3.4
ZOOM_VARIANTES = 34
ZOOM_TIRA = 30
LADO_TIRA = 320

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(10, 14, 5), cabeza=(8, 9, 8), brazo=(3, 20, 3), pierna=(4, 12, 4), hombro=0.5)
    # ---- la tunica (en dos trozos, para que ondee), hecha jirones abajo: acaba en humo
    r.pieza("tunica", "cadera", (0, -0.5, 0), boxes=[Box(-5.8, 0, -3.4, 11.6, 7.0, 6.8, "tunica")])
    r.pieza("tunica_bajo", "tunica", (0, 6.5, 0), boxes=[Box(-7.6, 0, -4.4, 15.2, 7.5, 8.8, "tunica_bajo")])
    # ---- la capa, de los hombros al suelo, por la espalda
    r.pieza("capa", "torso", (0, -13.5, 2.6), rot=(8, 0, 0), boxes=[Box(-6.6, 0, 0, 13.2, 28, 0.8, "capa")])
    # ---- el manto de placas de hueso negro, con dos puas en cada hombro
    r.pieza("manto", "torso", (0, -13.0, 0), boxes=[
        Box(-8.0, -1.6, -3.6, 16, 4.2, 7.2, "manto"),
        Box(-8.6, -5.4, -0.6, 1.2, 3.8, 1.2, "pua"),
        Box(-6.8, -4.4, 1.0, 1.0, 2.8, 1.0, "pua"),
        Box(7.4, -5.4, -0.6, 1.2, 3.8, 1.2, "pua"),
        Box(5.8, -4.4, 1.0, 1.0, 2.8, 1.0, "pua"),
    ])
    # ---- el pecho abierto (las costillas, por delante) y el corazon dentro
    r.pieza("costillas", "torso", (0, 0, 0), boxes=[Box(-4.4, -12.6, -3.1, 8.8, 9.2, 0.6, "costillas")])
    r.pieza("corazon", "torso", (0, -8.4, -1.4), boxes=[Box(-1.6, -1.6, -1.6, 3.2, 3.2, 3.2, "corazon")])
    # ---- la capucha (abierta por delante: dentro, la oscuridad y los ojos) y la corona rota de Corvin, flotando encima
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-5.0, -10.4, -4.6, 10, 10.8, 9.6, "capucha"),
        Box(-3.0, -12.2, -0.4, 6, 2.6, 5.6, "capucha_punta"),
    ])
    r.pieza("corona", "cabeza", (0, -14.4, -0.4), rot=(0, 0, -9), boxes=[
        Box(-4.6, -1.4, -4.6, 9.2, 1.8, 9.2, "corona"),
        Box(-0.7, -5.0, -4.9, 1.4, 3.8, 1.0, "punta_corona"),
        Box(-3.9, -3.8, -4.8, 1.0, 2.6, 1.0, "punta_corona"),
        Box(2.9, -3.2, -4.8, 1.0, 2.0, 1.0, "punta_corona"),
        Box(-4.8, -3.6, -0.5, 1.0, 2.4, 1.0, "punta_corona"),
        Box(3.8, -2.4, -0.5, 1.0, 1.2, 1.0, "punta_corona"),
        Box(-0.8, -1.0, -5.0, 1.6, 1.4, 0.6, "gema_corona"),
    ])
    # ---- los grilletes de las munecas, los eslabones rotos que cuelgan y las garras
    for n in ("d", "i"):
        r.pieza(f"grillete_{n}", f"antebrazo_{n}", (0, 6.6, 0), boxes=[Box(-2.2, 0, -2.2, 4.4, 2.4, 4.4, "grillete")])
        r.pieza(f"eslabones_{n}", f"antebrazo_{n}", (0, 9.0, -1.8), rot=(0, 0, 0), boxes=[
            Box(-0.6, 0, -1.3, 1.2, 2.6, 2.6, "eslabon", uv="eslabon_a"),
            Box(-1.3, 2.2, -0.6, 2.6, 2.6, 1.2, "eslabon", uv="eslabon_b"),
            Box(-0.6, 4.4, -1.3, 1.2, 2.6, 2.6, "eslabon", uv="eslabon_a"),
        ])
        r.pieza(f"garra_{n}", f"mano_{n}", (0, 0, 0), boxes=[Box(-2.4, -0.8, -2.4, 4.8, 9.6, 4.8, f"garras_{n}")])
    r.empaquetar()
    return r


PIERNAS = ["muslo_d", "muslo_i", "pierna_d", "pierna_i"]
#: lo que se esconde en cada variante (no anda: flota)
OCULTAS = {0: PIERNAS, 1: PIERNAS}

# ---------------------------------------------------------------- las texturas

NEGRO = "#030204"
SOMBRA = ["#050307", "#0a0610", "#120a1a", "#1b0f26", "#251433", "#311a42"]
HUESO = ["#1c1814", "#2b2520", "#3d352c", "#524638", "#6a5b48"]
HIERRO = ["#0f0d0d", "#1c1918", "#2c2725", "#3e3632", "#55463e"]
ORO = ["#3a2508", "#5a3a0e", "#7d5518", "#a37526", "#c99a3c"]
ROJO = "#e3231c"
SANGRE = ["#2a0306", "#4a060b", "#700b12", "#9a1018"]


def materiales(variante):
    sombra_ = tela(SOMBRA, 1101, pliegue=4, grano=0.16, luz=0.24)
    piel = ruido(SOMBRA[1:5], 1102, grano=0.4, base=0.45)
    hueso = ruido(HUESO, 1103, grano=0.3)
    hierro = metal(HIERRO, 1104)
    oro = metal(ORO, 1105)

    def jirones(f, semilla, cada=4, minimo=0):
        # el borde de abajo deshilachado: cada columna acaba a una altura
        def g_(cara, x, y, w, h):
            if cara in ("arriba", "abajo"):
                return None
            largo = h - 1 - ((x * 7 + semilla) % cada)
            if y > max(minimo, largo):
                return None
            return f(cara, x, y, w, h)
        return g_

    def cabeza(cara, x, y, w, h):
        # dentro de la capucha no hay cara: oscuridad (los ojos van en el brillo; aqui, un rojo apagado)
        if cara == "frente":
            ojos = {(2, 4), (5, 4)} if variante == 0 else {(2, 4), (5, 4), (1, 2), (6, 2), (1, 6), (6, 6)}
            if (x, y) in ojos:
                return "#5a0608"
        return mezcla(NEGRO, SOMBRA[2], 0.15 * ((x * 3 + y) % 3))

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = sombra_(cara, x, y, w, h)
        if cara == "frente":
            # abierta: solo el borde (el canto de la tela, algo mas claro)
            if 1 < x < w - 2 and y > 1:
                return None
            return claro(c, 0.06)
        return oscuro(c, 0.05 * (y / max(1, h - 1)))

    def capucha_punta(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return sombra_(cara, x, y, w, h)

    def corona(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= w - 2 else ORO[3]
        c = oro(cara, x, y, w, h)
        # la grieta (la corona rota de Corvin): una raya oscura en zigzag
        if cara == "frente" and (x == 6 or (x == 7 and y == 1)):
            return NEGRO
        if cara == "der" and x == 3:
            return NEGRO
        return ORO[4] if y == 0 else oscuro(c, 0.25)

    def punta_corona(cara, x, y, w, h):
        return oscuro(oro(cara, x, y, w, h), 0.15) if y > 0 else ORO[4]

    def torso(cara, x, y, w, h):
        if cara == "frente":
            # la cavidad del pecho: roja oscura en el hueco, negra alrededor
            if 2 <= x <= w - 3 and 2 <= y <= 10:
                return SANGRE[0] if (x + y) % 3 else SANGRE[1]
            return NEGRO
        return piel(cara, x, y, w, h)

    def costillas(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara != "frente" and cara != "espalda":
            return hueso(cara, x, y, w, h)
        # el esternon y cinco costillas a cada lado, con huecos entre ellas
        if abs(x - (w - 1) / 2) < 0.6 and y < h - 2:
            return claro(hueso(cara, x, y, w, h), 0.1)
        if y % 2 == 1 and abs(x - (w - 1) / 2) > 0.6:
            return hueso(cara, x, y, w, h)
        if x in (0, w - 1):
            return oscuro(hueso(cara, x, y, w, h), 0.2)
        return None

    def corazon(cara, x, y, w, h):
        return SANGRE[2] if (x + y) % 2 else SANGRE[3]

    def manto(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = hueso(cara, x, y, w, h)
        c = oscuro(c, 0.35)
        if cara in ("frente", "espalda", "der", "izq") and y == h - 1 and x % 3 == 0:
            return None                                                # el borde de las placas, dentado
        if cara == "arriba" and x % 4 == 0:
            return oscuro(c, 0.3)                                       # las juntas de las placas
        if cara == "frente" and abs(x - (w - 1) / 2) < 1.2 and 1 <= y <= 3:
            return SANGRE[1]                                            # la marca (luce en el brillo)
        return c

    def pua(cara, x, y, w, h):
        return claro(hueso(cara, x, y, w, h), 0.05) if y > 0 else HUESO[4]

    def brazo(cara, x, y, w, h):
        return piel(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if variante == 1 and (x * 5 + y * 3) % 11 == 0:
            return SANGRE[2]                                            # las grietas (lucen en el brillo)
        return c

    def grillete(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            return None if 1 <= x <= w - 2 and 1 <= y <= h - 2 else c
        if y in (0, h - 1):
            return claro(c, 0.08)
        return oscuro(c, 0.1) if x % 3 == 1 else c

    def eslabon(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        # el agujero del eslabon (por los lados anchos)
        if 0 < x < w - 1 and 0 < y < h - 1 and w > 1 and h > 1:
            return None
        return c

    def garras(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if y <= 2:
            return piel(cara, x, y, w, h)                               # el dorso de la mano
        if cara in ("frente", "der", "izq", "espalda"):
            # dedos largos y separados que acaban en unas
            if x % 2 == 0:
                return mezcla(NEGRO, SOMBRA[4], 0.3) if y < h - 3 else "#16101c"
            return None
        return None

    def tunica(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = sombra_(cara, x, y, w, h)
        if cara == "frente" and abs(x - (w - 1) / 2) < 0.6:
            return oscuro(c, 0.4)                                       # la abertura de delante
        return oscuro(c, 0.04 * y)

    def tunica_bajo(cara, x, y, w, h):
        c = sombra_(cara, x, y, w, h)
        if variante == 1 and y >= h - 3 and (x + y) % 3 == 0:
            return SANGRE[3]                                            # el bajo que arde (en el brillo)
        return oscuro(c, 0.08 + 0.08 * y / max(1, h - 1))

    def capa(cara, x, y, w, h):
        c = sombra_(cara, x, y, w, h)
        # agujeros y desgarrones
        if (x * 13 + y * 7) % 37 == 0 and y > 4:
            return None
        return oscuro(c, 0.02 * (y / 2))

    def pierna(cara, x, y, w, h):
        return NEGRO

    return {
        "cabeza": cabeza, "torso": sombra(torso, abajo=0.15),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.15), "antebrazo_i": sombra(antebrazo, abajo=0.15),
        "muslo_d": pierna, "muslo_i": pierna, "pierna_d": pierna, "pierna_i": pierna,
        "tunica": tunica, "tunica_bajo": jirones(tunica_bajo, 3, cada=5, minimo=2), "capa": jirones(capa, 5, cada=6, minimo=8),
        "manto": sombra(manto, abajo=0.12), "pua": pua, "costillas": costillas, "corazon": corazon,
        "capucha": capucha, "capucha_punta": capucha_punta, "corona": corona, "punta_corona": punta_corona,
        "gema_corona": liso("#7a0b0b"), "grillete": sombra(grillete, abajo=0.15), "eslabon": eslabon,
        "garras_d": garras, "garras_i": garras,
    }


def brillo(variante):
    """Lo que brilla: los ojos (dos; con la furia, seis), el corazon, la marca del manto, la gema de la corona; con la furia, las
    grietas de los brazos y el bajo de la tunica que arde."""
    ojos = {(2, 4), (5, 4)} if variante == 0 else {(2, 4), (5, 4), (1, 2), (6, 2), (1, 6), (6, 6)}
    b = {
        "cabeza": lambda cara, x, y, w, h: ROJO if cara == "frente" and (x, y) in ojos else None,
        "corazon": lambda cara, x, y, w, h: "#ff3a2a" if (x + y) % 2 else "#c4141a",
        "gema_corona": liso(ROJO),
        "manto": lambda cara, x, y, w, h: "#b8141a" if cara == "frente" and abs(x - (w - 1) / 2) < 1.2 and 1 <= y <= 3 else None,
    }
    if variante == 1:
        b["antebrazo_d"] = b["antebrazo_i"] = lambda cara, x, y, w, h: "#d2201a" if (x * 5 + y * 3) % 11 == 0 else None
        b["tunica_bajo"] = lambda cara, x, y, w, h: (
            "#ff5a1a" if cara not in ("arriba", "abajo") and y >= h - 3 and (x + y) % 3 == 0 and y <= h - 1 - ((x * 7 + 3) % 5) else None)
        b["torso"] = lambda cara, x, y, w, h: "#c4141a" if cara == "frente" and 2 <= x <= w - 3 and 2 <= y <= 10 and (x + y) % 4 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va la mano respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide ~19)

ARRIBA = 2.0     # lo que flota sobre el suelo (pixeles, a escala 1)
COLGADA = 9.0    # lo que sube encadenada (en el aire, entre los pilares)
BASE = dict(raiz=raiz(ARRIBA), torso=(-4, 0, 0), cabeza=(4, 0, 0), tunica=(4, 0, 0), tunica_bajo=(6, 0, 0), capa=(6, 0, 0),
            corona=(0, 0, 0))
#: los brazos colgando a los lados, las garras cerca de las rodillas
CUELGA = dict(_mano_d=("h", -3.0, -18.0, 2.0), _mano_i=("h", 3.0, -18.0, 2.0))
#: en guardia: inclinada hacia delante, los brazos abiertos con las garras por delante
LISTO = dict(raiz=raiz(ARRIBA + 0.8, -0.8), torso=(16, 0, 0), cabeza=(-12, 0, 0), tunica=(10, 0, 0), tunica_bajo=(12, 0, 0),
             capa=(30, 0, 0), _mano_d=("h", -7.0, -9.0, 9.0), _mano_i=("h", 7.0, -9.0, 9.0))
#: encadenada: en el aire, los brazos abiertos en cruz tirados por las cadenas y la cabeza caida
ATADA = dict(raiz=raiz(ARRIBA + COLGADA), torso=(6, 0, 0), cabeza=(34, 0, 0), tunica=(4, 0, 0), tunica_bajo=(8, 0, 0), capa=(4, 0, 0),
             _mano_d=("h", -17.0, 5.0, 2.0), _mano_i=("h", 17.0, 5.0, 2.0))


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

    Q = con(BASE, CUELGA)
    # ---- quieta: flota, sube y baja despacio, la capa y la tunica ondean
    bucle("quieto", 5.0, [
        (0.0, Q),
        (1.25, con(Q, raiz=raiz(ARRIBA + 0.8), torso=(-3, 3, 1), cabeza=(6, -8, 4), tunica_bajo=(10, 0, -3), capa=(10, 0, 2))),
        (2.5, con(Q, raiz=raiz(ARRIBA + 1.4), torso=(-4, 0, 0), cabeza=(8, 0, 0), tunica_bajo=(4, 0, 0), capa=(4, 0, 0))),
        (3.75, con(Q, raiz=raiz(ARRIBA + 0.6), torso=(-3, -3, -1), cabeza=(6, 8, -4), tunica_bajo=(10, 0, 3), capa=(10, 0, -2))),
        (5.0, Q),
    ])
    # ---- andar: se desliza inclinada, la capa y la tunica por detras
    DESLIZA = con(Q, raiz=raiz(ARRIBA + 1.0, -0.6), torso=(10, 0, 0), cabeza=(-8, 0, 0), tunica=(20, 0, 0), tunica_bajo=(18, 0, 0),
                  capa=(40, 0, 0), _mano_d=("h", -4.0, -17.0, -2.0), _mano_i=("h", 4.0, -17.0, -2.0))
    bucle("andar", 2.0, [
        (0.0, DESLIZA),
        (0.5, con(DESLIZA, raiz=raiz(ARRIBA + 1.6, -0.6), tunica_bajo=(24, 0, 4), capa=(46, 0, 3))),
        (1.0, con(DESLIZA, raiz=raiz(ARRIBA + 0.8, -0.6), tunica_bajo=(14, 0, 0), capa=(36, 0, 0))),
        (1.5, con(DESLIZA, raiz=raiz(ARRIBA + 1.6, -0.6), tunica_bajo=(24, 0, -4), capa=(46, 0, -3))),
        (2.0, DESLIZA),
    ])
    # ---- en guardia
    bucle("listo", 2.6, [
        (0.0, LISTO),
        (1.3, con(LISTO, raiz=raiz(ARRIBA + 1.8, -0.8), torso=(18, 0, 0), _mano_d=("h", -7.6, -8.2, 9.4), _mano_i=("h", 7.6, -8.2, 9.4))),
        (2.6, LISTO),
    ])
    # ---- encadenada: cuelga en el aire, se mece un poco, la cabeza caida (asi espera)
    bucle("atada", 6.0, [
        (0.0, ATADA),
        (1.5, con(ATADA, raiz=raiz(ARRIBA + COLGADA + 0.6), torso=(6, 3, 2), cabeza=(32, -6, 3), tunica_bajo=(12, 0, -3), capa=(8, 0, 2))),
        (3.0, con(ATADA, raiz=raiz(ARRIBA + COLGADA + 0.9), torso=(4, 0, 0), cabeza=(30, 0, 0))),
        (4.5, con(ATADA, raiz=raiz(ARRIBA + COLGADA + 0.5), torso=(6, -3, -2), cabeza=(32, 6, -3), tunica_bajo=(12, 0, 3), capa=(8, 0, -2))),
        (6.0, ATADA),
    ])
    # ---- arrodillada en el aire (cuando cae: lo que queda mientras le sale el corazon)
    RODILLAS = con(BASE, raiz=raiz(ARRIBA - 6.0, 1.0), torso=(28, 0, 0), cabeza=(30, 0, 0), tunica=(-70, 0, 0), tunica_bajo=(64, 0, 0),
                   capa=(40, 0, 0), _mano_d=("h", -6.0, -19.0, 4.0), _mano_i=("h", 6.0, -19.0, 4.0))
    bucle("rodillas", 4.0, [
        (0.0, RODILLAS),
        (2.0, con(RODILLAS, torso=(30, 0, 0), cabeza=(34, 0, 0))),
        (4.0, RODILLAS),
    ])

    # ================================================================ acciones
    L = con(BASE, LISTO)
    # ---- despertar (la primera vez: mientras dice su frase larga): alza la cabeza, tira de las cadenas (tres tirones, cada vez
    # mas fuertes) y al acabar las rompe (a los 16,6 s) y baja al suelo
    TIRA = con(ATADA, cabeza=(-6, 0, 0), torso=(-4, 0, 0), _mano_d=("h", -15.0, 3.0, 5.0), _mano_i=("h", 15.0, 3.0, 5.0))
    TIRA2 = con(ATADA, raiz=raiz(ARRIBA + COLGADA - 0.6), cabeza=(-12, 0, 0), torso=(10, 0, 0),
                _mano_d=("h", -12.0, 1.0, 8.0), _mano_i=("h", 12.0, 1.0, 8.0))
    ROMPE = con(L, raiz=raiz(ARRIBA + COLGADA * 0.6, -1.0), torso=(-14, 0, 0), cabeza=(-26, 0, 0), capa=(-10, 0, 0),
                _mano_d=("h", -16.0, 10.0, -3.0), _mano_i=("h", 16.0, 10.0, -3.0))
    accion("despierta", 17.6, [
        (0.0, ATADA),
        (1.6, con(ATADA, cabeza=(10, 0, 0))),
        (3.2, con(ATADA, cabeza=(-4, 0, 0), torso=(2, 0, 0))),
        (6.0, con(ATADA, cabeza=(-8, -14, 0), torso=(2, 0, 0))),
        (7.4, TIRA),
        (8.2, con(ATADA, cabeza=(-6, 6, 0))),
        (10.6, con(ATADA, cabeza=(-10, 10, 0), torso=(4, 0, 0))),
        (11.6, TIRA2),
        (12.6, con(ATADA, cabeza=(-8, 0, 0))),
        (14.6, con(ATADA, cabeza=(-14, 0, 0), torso=(6, 0, 0))),
        (15.8, TIRA2),
        (16.6, ROMPE),
        (17.1, con(L, raiz=raiz(ARRIBA + 3.0, -0.8))),
        (17.6, L),
    ], golpes=(16.6,))
    # ---- despertar corto (si vuelve a pelear enseguida: "¿Otra vez aqui?")
    accion("despierta_corta", 3.6, [
        (0.0, ATADA),
        (0.8, con(ATADA, cabeza=(-8, 0, 0))),
        (1.8, TIRA2),
        (2.6, ROMPE),
        (3.1, con(L, raiz=raiz(ARRIBA + 3.0, -0.8))),
        (3.6, L),
    ], golpes=(2.6,))
    # ---- el rugido: echa los brazos atras y ruge con la cabeza hacia delante (al empezar y a media vida)
    RUGE = con(BASE, raiz=raiz(ARRIBA + 2.4, -1.0), torso=(18, 0, 0), cabeza=(-34, 0, 0), tunica=(-14, 0, 0), tunica_bajo=(-12, 0, 0),
               capa=(60, 0, 0), _mano_d=("h", -10.0, -4.0, -8.0), _mano_i=("h", 10.0, -4.0, -8.0))
    accion("rugido", 2.2, [
        (0.00, L),
        (0.45, con(RUGE, torso=(30, 0, 0), cabeza=(10, 0, 0))),
        (0.65, RUGE),
        (1.60, con(RUGE, raiz=raiz(ARRIBA + 2.8, -1.0), cabeza=(-38, 0, 0))),
        (2.20, L),
    ], golpes=(0.65,))
    # ---- los zarpazos: la derecha de arriba abajo cruzando y luego la izquierda
    DER = con(L, raiz=raiz(ARRIBA + 1.4, 0.8), torso=(4, 34, 0), cabeza=(-8, -24, 0),
              _mano_d=("h", -9.0, 6.0, 2.0), _mano_i=("h", 4.0, -10.0, 8.0))
    DER_B = con(L, raiz=raiz(ARRIBA + 0.2, -3.0), torso=(24, -20, 0), cabeza=(-14, 16, 0),
                _mano_d=("h", 8.0, -10.0, 9.0), _mano_i=("h", 5.0, -10.0, 8.0))
    IZQ = con(L, raiz=raiz(ARRIBA + 1.0, -3.0), torso=(10, -34, 0), cabeza=(-8, 24, 0),
              _mano_d=("h", 6.0, -10.0, 8.0), _mano_i=("h", 9.0, 6.0, 2.0))
    IZQ_B = con(L, raiz=raiz(ARRIBA + 0.2, -5.0), torso=(26, 20, 0), cabeza=(-14, -16, 0),
                _mano_d=("h", 5.0, -10.0, 8.0), _mano_i=("h", -8.0, -10.0, 9.0))
    accion("zarpazos", 1.8, [
        (0.00, L),
        (0.40, DER),
        (0.55, DER_B),
        (0.85, IZQ),
        (1.00, IZQ_B),
        (1.40, con(IZQ_B, torso=(22, 16, 0))),
        (1.80, L),
    ], golpes=(0.55, 1.00))
    # ---- el latigazo de la cadena: echa el brazo derecho atras y lo lanza (el eslabon vuela hasta su presa)
    ATRAS = con(L, raiz=raiz(ARRIBA + 1.0, 0.6), torso=(0, 40, 0), cabeza=(-10, -30, 0),
                _mano_d=("h", -12.0, 4.0, -8.0), _mano_i=("h", 6.0, -10.0, 6.0))
    LANZA = con(L, raiz=raiz(ARRIBA + 0.6, -2.4), torso=(18, -24, 0), cabeza=(-12, 18, 0),
                _mano_d=("h", 2.0, -2.0, 18.0), _mano_i=("h", 6.0, -12.0, 2.0))
    accion("cadena", 1.6, [
        (0.00, L),
        (0.50, ATRAS),
        (0.75, LANZA),
        (1.15, con(LANZA, _mano_d=("h", -2.0, -6.0, 14.0))),
        (1.60, L),
    ], golpes=(0.75,))
    # ---- las manos: alza los brazos y golpea el suelo con las dos garras (salen manos del suelo bajo cada uno)
    ALZA = con(L, raiz=raiz(ARRIBA + 3.0, 0.4), torso=(-16, 0, 0), cabeza=(-20, 0, 0),
               _mano_d=("h", -6.0, 16.0, 2.0), _mano_i=("h", 6.0, 16.0, 2.0))
    SUELO = con(L, raiz=raiz(ARRIBA - 1.0, -3.0), torso=(40, 0, 0), cabeza=(-24, 0, 0), capa=(60, 0, 0),
                _mano_d=("h", -5.0, -14.0, 11.0), _mano_i=("h", 5.0, -14.0, 11.0))
    accion("manos", 1.8, [
        (0.00, L),
        (0.35, ALZA),
        (0.50, SUELO),
        (1.20, con(SUELO, torso=(36, 0, 0))),
        (1.80, L),
    ], golpes=(0.50,))
    # ---- las lanzas: alza la garra derecha (se forman las lanzas de sombra) y la abre hacia su presa
    FORMA = con(L, raiz=raiz(ARRIBA + 2.0, 0.6), torso=(-8, 20, 0), cabeza=(-16, -16, 0),
                _mano_d=("h", -4.0, 14.0, 0.0), _mano_i=("h", 6.0, -10.0, 6.0))
    ARROJA = con(L, raiz=raiz(ARRIBA + 1.0, -2.0), torso=(16, -16, 0), cabeza=(-10, 12, 0),
                 _mano_d=("h", 0.0, 2.0, 17.0), _mano_i=("h", 6.0, -10.0, 6.0))
    accion("lanzas", 1.6, [
        (0.00, L),
        (0.45, FORMA),
        (0.75, ARROJA),
        (1.20, con(ARROJA, _mano_d=("h", -2.0, -4.0, 15.0))),
        (1.60, L),
    ], golpes=(0.75,))
    # ---- el giro: da una vuelta entera con los brazos abiertos (la capa en abanico)
    ABRE = con(L, raiz=raiz(ARRIBA + 1.8, 0.0), torso=(0, 0, 0), cabeza=(-6, 0, 0), tunica=(-24, 0, 0), tunica_bajo=(-20, 0, 0),
               capa=(76, 0, 0), _mano_d=("h", -18.0, -2.0, 2.0), _mano_i=("h", 18.0, -2.0, 2.0))
    vuelta = []
    for k in range(9):
        a = 360.0 * k / 8
        vuelta.append((0.45 + 0.40 * k / 8, con(ABRE, raiz=raiz(ARRIBA + 1.8 + 0.6 * (k % 2), 0.0, rot=(0, -a, 0)),
                                                  tunica=(-30, 0, 0), tunica_bajo=(-34, 0, 0))))
    accion("giro", 1.4, [
        (0.00, L),
        (0.35, con(ABRE, raiz=raiz(ARRIBA + 1.2, 0.0, rot=(0, 30, 0)))),
        *vuelta,
        (1.05, con(ABRE, raiz=raiz(ARRIBA + 1.2, 0.0, rot=(0, -380, 0)))),
        (1.40, con(L, raiz=raiz(ARRIBA + 0.8, -0.8, rot=(0, -360, 0)))),
    ], golpes=(0.65,))
    # ---- la niebla: se encoge y se deshace (el juego la lleva detras de su presa) y se abre al aparecer
    ENCOGE = con(BASE, raiz=raiz(ARRIBA - 2.0), torso=(34, 0, 0), cabeza=(30, 0, 0), tunica=(-10, 0, 0),
                 _mano_d=("h", 4.0, -8.0, 6.0), _mano_i=("h", -4.0, -8.0, 6.0))
    accion("niebla", 1.5, [
        (0.00, L),
        (0.30, ENCOGE),
        (0.65, con(ENCOGE, raiz=raiz(ARRIBA - 3.0), torso=(40, 0, 0))),
        (0.80, con(L, raiz=raiz(ARRIBA + 3.0), torso=(-12, 0, 0), cabeza=(-16, 0, 0), _mano_d=("h", -14.0, 2.0, 6.0),
                   _mano_i=("h", 14.0, 2.0, 6.0))),
        (1.50, L),
    ])
    # ---- la mirada: sube, echa la cabeza atras (se carga; los ojos arden) y la baja hacia su presa: el rayo barre la sala
    CARGA = con(BASE, raiz=raiz(ARRIBA + 7.0), torso=(-20, 0, 0), cabeza=(-40, 0, 0), capa=(20, 0, 0),
                _mano_d=("h", -12.0, -6.0, -4.0), _mano_i=("h", 12.0, -6.0, -4.0))
    MIRA = con(BASE, raiz=raiz(ARRIBA + 7.0, -1.0), torso=(14, 0, 0), cabeza=(-4, 0, 0), capa=(30, 0, 0),
               _mano_d=("h", -14.0, -2.0, 4.0), _mano_i=("h", 14.0, -2.0, 4.0))
    accion("mirada", 3.4, [
        (0.00, L),
        (0.70, CARGA),
        (1.40, con(CARGA, raiz=raiz(ARRIBA + 7.6), cabeza=(-46, 0, 0))),
        (1.55, MIRA),
        (2.30, con(MIRA, torso=(14, -10, 0))),
        (3.05, con(MIRA, torso=(14, 10, 0))),
        (3.40, L),
    ], golpes=(1.55,))
    # ---- el grito: abre los brazos de par en par y grita (empuja a todos y lo oscurece todo)
    GRITA = con(BASE, raiz=raiz(ARRIBA + 3.0, -1.0), torso=(-12, 0, 0), cabeza=(-30, 0, 0), tunica=(-16, 0, 0), tunica_bajo=(-14, 0, 0),
                capa=(70, 0, 0), _mano_d=("h", -16.0, 8.0, 2.0), _mano_i=("h", 16.0, 8.0, 2.0))
    accion("grito", 1.8, [
        (0.00, L),
        (0.50, con(GRITA, torso=(24, 0, 0), cabeza=(14, 0, 0), _mano_d=("h", -4.0, -6.0, 6.0), _mano_i=("h", 4.0, -6.0, 6.0))),
        (0.80, GRITA),
        (1.40, con(GRITA, raiz=raiz(ARRIBA + 3.4, -1.0), cabeza=(-34, 0, 0))),
        (1.80, L),
    ], golpes=(0.80,))
    # ---- los que le fallaron: alza las garras al techo (se levantan del suelo los marcados de sombra)
    LLAMA = con(BASE, raiz=raiz(ARRIBA + 4.0), torso=(-14, 0, 0), cabeza=(-26, 0, 0), capa=(14, 0, 0),
                _mano_d=("h", -8.0, 17.0, 3.0), _mano_i=("h", 8.0, 17.0, 3.0))
    accion("caidos", 2.2, [
        (0.00, L),
        (0.60, LLAMA),
        (1.20, con(LLAMA, raiz=raiz(ARRIBA + 4.6), _mano_d=("h", -10.0, 16.0, 6.0), _mano_i=("h", 10.0, 16.0, 6.0))),
        (1.60, con(LLAMA, torso=(10, 0, 0), _mano_d=("h", -9.0, -10.0, 10.0), _mano_i=("h", 9.0, -10.0, 10.0))),
        (2.20, L),
    ], golpes=(1.20,))
    # ---- la furia (a media vida): se encoge, se agarra el pecho y se abre de golpe (se le abren los ojos y se agrieta: la
    # variante 1 desde los 1,6 s)
    accion("furia", 3.0, [
        (0.00, L),
        (0.50, con(BASE, raiz=raiz(ARRIBA + 1.0), torso=(30, 0, 0), cabeza=(30, 0, 0), _mano_d=("h", 4.0, -6.0, 6.0), _mano_i=("h", -4.0, -6.0, 6.0))),
        (1.20, con(BASE, raiz=raiz(ARRIBA + 0.6), torso=(36, 12, 6), cabeza=(34, -10, 8), _mano_d=("h", 4.4, -5.0, 6.0), _mano_i=("h", -3.6, -6.0, 6.4))),
        (1.60, con(GRITA, raiz=raiz(ARRIBA + 4.0, -1.0))),
        (2.40, con(GRITA, raiz=raiz(ARRIBA + 4.4, -1.0), cabeza=(-36, 0, 0))),
        (3.00, L),
    ], golpes=(1.60,))
    # ---- aturdida
    accion("aturdido", 1.4, [
        (0.00, L),
        (0.15, con(L, raiz=raiz(ARRIBA + 1.6, 2.0), torso=(-16, 10, 0), cabeza=(-20, -14, 0))),
        (0.60, con(L, raiz=raiz(ARRIBA + 1.2, 1.5), torso=(-10, -8, 0), cabeza=(-8, 16, 0))),
        (1.00, con(L, raiz=raiz(ARRIBA + 0.8, 0.6), torso=(6, 4, 0), cabeza=(2, -6, 0))),
        (1.40, L),
    ])
    # ---- caer: se dobla, baja hasta quedar de rodillas en el aire, deja caer los brazos y se le abre el pecho (le sale el corazon)
    accion("cae", 5.0, [
        (0.00, L),
        (0.50, con(L, raiz=raiz(ARRIBA + 2.0, 1.0), torso=(-20, 0, 0), cabeza=(-30, 0, 0), _mano_d=("h", -10.0, 4.0, 2.0),
                   _mano_i=("h", 10.0, 4.0, 2.0))),
        (1.60, con(RODILLAS, torso=(10, 0, 0), cabeza=(0, 0, 0), _mano_d=("h", 2.0, -4.0, 8.0), _mano_i=("h", -2.0, -4.0, 8.0))),
        (2.60, con(RODILLAS, torso=(-10, 0, 0), cabeza=(-20, 0, 0), _mano_d=("h", -6.0, -10.0, 8.0), _mano_i=("h", 6.0, -10.0, 8.0))),
        (3.60, RODILLAS),
        (5.00, RODILLAS),
    ])
    return B, A


def variante_de(nombre):
    return 1 if nombre in ("grito", "caidos", "mirada", "furia", "cae") else 0


def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    return []


TABLA_EXTRA = ()
