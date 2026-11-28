"""
Los Ensamblados del Osario de San Ulfo (parte 7as): cuando la Peste Gris lleno el Camposanto de los Pobres, Fray Osmund, el
sacristan, desenterro a los muertos y ordeno sus huesos en la cripta (cuarenta mil). La noche del cielo rojo los huesos se
levantaron, pero no cada uno con los suyos: una calavera de uno, los brazos de otro, las piernas de un tercero (los huesos de
cada pieza, de un tono distinto). En las cuencas, una lucecita azul de alma. Al caer se deshacen en un monton y, si nadie lo
pisotea, se vuelven a armar (una vez). Tres:

  0 portacirios: con los jirones del habito de los hermanos (la capucha, el escapulario, el cordon) y el candelabro de huesos
    con sus tres velas encendidas (lucen) y una calavera en lo alto. Candelazo de arriba abajo y barrido (queman). Espera de
    rodillas rezando, el candelabro clavado en el suelo (REZA).
  1 sepulturero: la capucha y la capa rota de los enterradores y la pala. Palada y la tierra de tumba que echa a los ojos
    (ciega un momento). Espera cavando (REZA) o hecho un monton de huesos (TUMBADO, todos).
  2 coloso: muchos cuerpos en uno (el dibujante lo agranda): el costillar doble, calaveras metidas en el pecho, el hombro y la
    espalda, los huesos de brazos y piernas en haces, y la maza de fémures atada con una cadena. Mazazo (la onda), barrido y
    pisoton. Espera de pie, guardando.
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "ensamblado"
VARIANTES = ("portacirios", "sepulturero", "coloso")
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # huesos: brazos y piernas finos; el torso es la caja de las costillas (con huecos en la textura)
    humanoide(r, torso=(8, 12, 4), brazo=(2, 12, 2), pierna=(2, 12, 2), hombro=0.0)
    r.pieza("columna", "torso", (0, 0, 0), boxes=[Box(-1, -12, 0.2, 2, 12, 1.4, "columna")])
    r.pieza("pelvis", "cadera", (0, 0, 0), boxes=[Box(-3.5, -1.5, -1.5, 7, 3, 3, "pelvis")])
    r.pieza("mandibula", "cabeza", (0, -1.0, -1.0), boxes=[Box(-3, 0, -3, 6, 2, 4, "mandibula")])
    # ---- 0 y 1: la capucha (del habito o del enterrador)
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 8, 8, "capucha", grow=g(0.55)),
        Box(-2.5, -5.0, 4.5, 5, 6, 1.5, "capucha_pico"),
    ])
    # ---- 0, el portacirios: el escapulario, el cordon, los faldones del habito y el candelabro
    r.pieza("escapulario", "torso", (0, 0, 0), boxes=[
        Box(-2.0, -11.6, -2.7, 4.0, 14.0, 0.6, "escapulario"),
        Box(-2.0, -11.6, 2.1, 4.0, 12.0, 0.6, "escapulario"),
    ])
    r.pieza("cordon", "torso", (0, 0, 0), boxes=[
        Box(-4.3, -1.6, -2.4, 8.6, 1.2, 4.8, "cordon"),
        Box(1.6, -0.6, -2.7, 0.8, 6.0, 0.6, "cordon"),
    ])
    for n, s in (("d", -1), ("i", 1)):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.3, -0.4, -2.5, 4.6, 9.0, 5.0, "faldon")])
    r.pieza("candelabro", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -21.0, 1.2, 1.2, 26.0, "candelabro_vara"),
        Box(-4.6, -0.6, -20.6, 9.2, 1.2, 1.2, "candelabro_brazo"),
        Box(-4.6, -0.6, -21.8, 1.2, 1.2, 1.4, "candelabro_brazo"),
        Box(3.4, -0.6, -21.8, 1.2, 1.2, 1.4, "candelabro_brazo"),
        Box(-4.5, -0.5, -24.2, 1.0, 1.0, 2.6, "vela"),
        Box(3.5, -0.5, -24.2, 1.0, 1.0, 2.6, "vela"),
        Box(-0.5, -0.5, -26.6, 1.0, 1.0, 2.6, "vela"),
        Box(-4.3, -0.3, -25.2, 0.6, 0.6, 1.0, "llama"),
        Box(3.7, -0.3, -25.2, 0.6, 0.6, 1.0, "llama"),
        Box(-0.3, -0.3, -27.6, 0.6, 0.6, 1.0, "llama"),
        Box(-1.2, -1.2, -24.0, 2.4, 2.4, 2.4, "calavera_cirio"),
    ])
    # ---- 1, el sepulturero: la capa rota y la pala
    r.pieza("capa", "torso", (0, -12, 2.6), rot=(6, 0, 0), boxes=[Box(-4.5, 0, 0, 9, 17, 1, "capa")])
    r.pieza("pala", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -17.0, 1.2, 1.2, 19.0, "pala_mango"),
        Box(-1.8, -0.6, 1.4, 3.6, 1.2, 1.2, "pala_mango"),
        Box(-2.4, -0.4, -22.6, 4.8, 0.8, 5.8, "pala_hoja"),
    ])
    # ---- 2, el coloso: el costillar doble, las calaveras, los huesos en haces y la maza de fémures
    r.pieza("costillar", "torso", (0, 0, 0), boxes=[Box(-5.6, -12.6, -3.0, 11.2, 10.0, 6.0, "costillar")])
    r.pieza("craneos", "torso", (0, 0, 0), boxes=[
        Box(-1.7, -9.4, -3.9, 3.4, 3.4, 2.2, "craneo"),
        Box(-1.6, -11.4, 2.8, 3.2, 3.2, 2.2, "craneo"),
    ])
    r.pieza("craneo_hombro", "brazo_i", (0, 0, 0), boxes=[Box(-1.9, -4.6, -1.9, 3.8, 3.6, 3.8, "craneo")])
    for n in ("d", "i"):
        r.pieza(f"haz_brazo_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-1.7, -2.0, -1.7, 3.4, 6.2, 3.4, "haz")])
        r.pieza(f"haz_antebrazo_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-1.6, 0.0, -1.6, 3.2, 6.0, 3.2, "haz")])
        r.pieza(f"haz_muslo_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-1.7, 0.0, -1.7, 3.4, 6.0, 3.4, "haz")])
        r.pieza(f"haz_pierna_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-1.6, 0.0, -1.6, 3.2, 6.0, 3.2, "haz")])
    r.pieza("maza", "mano_d", (0, 0, 0), boxes=[
        Box(-0.8, -0.8, -13.0, 1.6, 1.6, 15.0, "maza_mango"),
        Box(-2.7, -2.7, -21.4, 5.4, 5.4, 8.6, "maza_cabeza"),
        Box(-2.9, -2.9, -17.2, 5.8, 5.8, 1.0, "maza_cadena"),
        Box(-2.9, -2.9, -20.4, 5.8, 5.8, 1.0, "maza_cadena"),
        Box(-1.7, -1.7, -24.0, 3.4, 3.4, 2.6, "craneo"),
    ])
    r.empaquetar()
    return r


PORTACIRIOS_P = ["escapulario", "cordon", "faldon_d", "faldon_i", "candelabro"]
SEPULTURERO_P = ["capa", "pala"]
COLOSO_P = ["costillar", "craneos", "craneo_hombro", "maza"] + [f"haz_{p}_{n}" for p in ("brazo", "antebrazo", "muslo", "pierna")
                                                                for n in ("d", "i")]
#: lo que se esconde en cada variante
OCULTAS = {
    0: SEPULTURERO_P + COLOSO_P,
    1: PORTACIRIOS_P + COLOSO_P,
    2: PORTACIRIOS_P + SEPULTURERO_P + ["capucha"],
}

# ---------------------------------------------------------------- las texturas

# tres huesos de tres muertos: el amarillento, el gris y el pardo de los mas viejos
HUESOS = (["#4a4334", "#6d6450", "#8f8569", "#ada27f", "#c4b994", "#d8cfae"],
          ["#3e3f3c", "#5d5f5a", "#7c7f78", "#9a9d95", "#b5b8af", "#cdd0c6"],
          ["#3d2f22", "#5a4733", "#7a6247", "#967c5c", "#ae9472", "#c6ad8a"])
HUECO = "#0e0c0a"
ALMA, ALMA_B = "#62c8ff", "#c8f0ff"
HABITO = ["#1d1712", "#2a2119", "#372b20", "#463628", "#554331"]
ENTERRADOR = ["#141516", "#1d1f21", "#272a2d", "#323639", "#3e4347"]
CORDON = ["#4a3c26", "#6a5634", "#8a7246"]
MADERA = ["#1e150e", "#2a1d13", "#382718", "#46331f", "#55402a"]
HIERRO = ["#15161a", "#22242a", "#30333a", "#40444c", "#535861"]
CERA = ["#8c8166", "#b3a782", "#d6cba4", "#ece3c2"]
LLAMA = ["#5a2c06", "#a0560e", "#e09a2a", "#ffd36a", "#fff2c0"]
TIERRA = ["#1d140c", "#2a1d12", "#3a2a1b"]

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _hueso(variante, pieza):
    """El hueso de esa pieza: de uno de los tres muertos (cada pieza del suyo; el coloso, de todos). La calavera, clara."""
    if pieza == "cabeza":
        k = (0, 1, 0)[variante]
    elif pieza == "mandibula":
        k = (2, 0, 1)[variante]
    else:
        k = (sum(ord(c) for c in pieza) * 7 + variante * 3) % 3
    return ruido(HUESOS[k][2:6], 9100 + k * 17 + variante, grano=0.45, base=0.55), HUESOS[k]


def _calavera(tonos):
    C = {"S": tonos[4], "s": tonos[3], "o": tonos[2], "O": tonos[1], "K": HUECO, "R": HUECO, "N": tonos[0], "c": tonos[5]}
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
    piezas = {}

    def hueso_de(pieza):
        if pieza not in piezas:
            piezas[pieza] = _hueso(variante, pieza)
        return piezas[pieza]

    habito_t = tela(HABITO, 9121, pliegue=3, grano=0.16, luz=0.12)
    enterrador_t = tela(ENTERRADOR, 9122, pliegue=3, grano=0.14, luz=0.1)
    madera_t = madera(MADERA, 9123, "z")
    hierro_t = metal(HIERRO, 9124)

    def cabeza(cara, x, y, w, h):
        t, tonos = hueso_de("cabeza")
        if cara == "frente":
            c = _calavera(tonos)(cara, x, y, w, h)
            return c if c is not None else tonos[1]
        c = t(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if y in (3, 4) and d in (1, 2):
                return HUECO                                  # la sien hundida
            if y >= 6 and d <= 3:
                return None                                   # sin carrillos: se ve la mandibula
        if cara == "abajo":
            return HUECO
        if (x * 5 + y * 3 + CARAS[cara]) % 17 == 0:
            return oscuro(c, 0.35)                            # una grieta
        return c

    def mandibula(cara, x, y, w, h):
        t, tonos = hueso_de("mandibula")
        if cara == "arriba":
            return None
        if cara == "frente":
            if y == 0:
                return tonos[5] if x % 2 == 0 else HUECO      # los dientes (le faltan algunos)
            return t(cara, x, y, w, h)
        return oscuro(t(cara, x, y, w, h), 0.1)

    def torso(cara, x, y, w, h):
        # las costillas: tiras de hueso con huecos; el esternon en el medio
        t, tonos = hueso_de("torso")
        if cara in ("arriba", "abajo"):
            return t(cara, x, y, w, h) if cara == "arriba" else None
        if y >= 9:
            return None                                       # la cintura: solo la columna
        if cara == "frente" and x in (3, 4):
            return claro(t(cara, x, y, w, h), 0.05)           # el esternon
        if y % 2 == 1:
            return None
        if cara in ("der", "izq") and y >= 6:
            return None
        if (x + y * 3) % 11 == 0:
            return None                                       # una costilla rota
        return t(cara, x, y, w, h)

    def columna(cara, x, y, w, h):
        t, tonos = hueso_de("columna")
        return tonos[3] if y % 2 == 0 else tonos[2]

    def pelvis(cara, x, y, w, h):
        t, tonos = hueso_de("pelvis")
        if cara in ("frente", "espalda") and 2 <= x <= 4 and y >= 1:
            return None
        return t(cara, x, y, w, h)

    def largo(pieza):
        def f(cara, x, y, w, h):
            # un hueso largo: mas claro en los extremos (las articulaciones), alguna muesca
            t, tonos = hueso_de(pieza)
            c = t(cara, x, y, w, h)
            if y == 0 or y == h - 1:
                return claro(c, 0.08)
            if (x + y * 5 + CARAS[cara]) % 13 == 0:
                return oscuro(c, 0.3)
            return c
        return f

    def mano(pieza):
        base = largo(pieza)

        def f(cara, x, y, w, h):
            if y >= h - 1:
                return hueso_de(pieza)[1][1]                  # los dedos
            return base(cara, x, y, w, h)
        return f

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente" and 1 <= y and 1 <= x <= 6:
            return None                                       # la calavera al aire
        tx = habito_t if variante == 0 else enterrador_t
        c = tx(cara, x, y, w, h)
        if cara == "frente":
            return oscuro(c, 0.2)
        if cara in ("der", "izq") and delante(cara, x, w) == 0:
            return oscuro(c, 0.25)
        if y == h - 1 and (x * 7 + 3) % 5 < 2:
            return None                                       # rota por abajo
        return c

    def capucha_pico(cara, x, y, w, h):
        return (habito_t if variante == 0 else enterrador_t)(cara, x, y, w, h)

    def escapulario(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if y > h - 2 - (x * 3) % 3:
            return None                                       # deshilachado
        c = habito_t(cara, x, y, w, h)
        if (x * 5 + y * 3) % 19 == 0:
            return None
        return c

    def cordon(cara, x, y, w, h):
        return CORDON[(x + y) % 3] if (x + 2 * y) % 4 else CORDON[0]

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # jirones por abajo y algun agujero; asoma el hueso de la pierna
        corte = h - 1 - ((x * 5 + CARAS[cara] * 3) % 4)
        if y > corte or (x * 3 + y * 7 + CARAS[cara]) % 21 == 0:
            return None
        return oscuro(habito_t(cara, x, y, w, h), 0.05 * (y / 3))

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo_ = h - 1 - ((x * 5 + 2) % 4)
        if y > largo_ or (x * 3 + y * 7) % 23 == 0:
            return None
        c = enterrador_t(cara, x, y, w, h)
        if y > h - 6 and (x + y) % 3 == 0:
            return TIERRA[(x + y) % 3]                        # el barro de las tumbas
        return oscuro(c, 0.04 * (y / 2))

    def vara(cara, x, y, w, h):
        t, tonos = hueso_de("candelabro")
        return tonos[3] if (y // 3) % 2 else tonos[2]         # huesos atados uno tras otro

    def brazo_cand(cara, x, y, w, h):
        t, tonos = hueso_de("candelabro_b")
        return t(cara, x, y, w, h)

    def vela(cara, x, y, w, h):
        if cara == "arriba":
            return CERA[3]
        c = CERA[1 + (x + y) % 2]
        if y >= h - 2 and (x + y) % 3 == 0:
            return CERA[3]                                    # la cera que chorrea
        return c

    def calavera_chica(cara, x, y, w, h):
        t, tonos = hueso_de("craneo")
        if cara == "frente" and y == 1 and x in (0, 2):
            return HUECO
        if cara == "frente" and y == 2 and x == 1:
            return HUECO
        return t(cara, x, y, w, h)

    def pala_mango(cara, x, y, w, h):
        c = madera_t(cara, x, y, w, h)
        if (x + y) % 11 == 0:
            return oscuro(c, 0.3)
        return c

    def pala_hoja(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if y >= h - 2 or (x * 3 + y) % 5 == 0:
            return mezcla(c, TIERRA[1], 0.6)                  # la tierra pegada
        return c

    def costillar(cara, x, y, w, h):
        t, tonos = hueso_de("costillar")
        if cara in ("arriba", "abajo"):
            return t(cara, x, y, w, h) if cara == "arriba" else None
        if y % 2 == 1 or (x * 7 + y) % 9 == 0:
            return None
        if cara == "frente" and x in (w // 2 - 1, w // 2) and y < 3:
            return None                                       # el hueco donde va la calavera
        return t(cara, x, y, w, h)

    def craneo(cara, x, y, w, h):
        t, tonos = hueso_de("craneo")
        if cara == "frente":
            if y in (1,) and x in (0, w - 1 - 0) or y == 1 and x in (w // 2 - 1 - 0, w - 2) and w >= 3:
                return HUECO
            if y == h - 1 and x % 2:
                return HUECO
        return t(cara, x, y, w, h)

    def haz(cara, x, y, w, h):
        # un haz de huesos de muchos: cada columna, un hueso de otro muerto (a lo largo), atados arriba y abajo
        tonos = HUESOS[(x + CARAS[cara] * 2) % 3]
        c = tonos[3] if x % 2 == 0 else tonos[2]
        if y in (1, h - 2):
            return CORDON[1] if x % 2 else CORDON[0]          # las ataduras
        if y in (0, h - 1):
            return claro(c, 0.1)                              # las cabezas de los huesos
        if (x * 7 + y * 3 + CARAS[cara]) % 17 == 0:
            return oscuro(c, 0.3)
        return c

    def maza_mango(cara, x, y, w, h):
        t, tonos = hueso_de("maza_mango")
        c = t(cara, x, y, w, h)
        if y >= h - 3:
            return CORDON[1] if y % 2 else CORDON[0]          # el puno atado
        return c

    def maza_cabeza(cara, x, y, w, h):
        # los fémures en haz, con los extremos gruesos arriba y abajo
        k = (x // 2 + CARAS[cara]) % 3
        c = HUESOS[k][2 + (x + y // 3) % 3]
        if x % 2 == 1 and cara not in ("arriba", "abajo"):
            return oscuro(c, 0.3)
        return c

    def maza_cadena(cara, x, y, w, h):
        return HIERRO[2 + (x + y) % 2] if (x + y) % 3 else HIERRO[0]

    mats = {
        "cabeza": sombra(cabeza, abajo=0.12), "mandibula": mandibula,
        "torso": torso, "columna": columna, "pelvis": sombra(pelvis, abajo=0.15),
        "brazo_d": largo("brazo_d"), "brazo_i": largo("brazo_i"), "antebrazo_d": mano("antebrazo_d"), "antebrazo_i": mano("antebrazo_i"),
        "muslo_d": largo("muslo_d"), "muslo_i": largo("muslo_i"), "pierna_d": sombra(largo("pierna_d"), abajo=0.1),
        "pierna_i": sombra(largo("pierna_i"), abajo=0.1),
        "capucha": sombra(capucha, abajo=0.16), "capucha_pico": sombra(capucha_pico, abajo=0.2),
        "escapulario": escapulario, "cordon": cordon, "faldon": sombra(faldon, abajo=0.1),
        "candelabro_vara": vara, "candelabro_brazo": brazo_cand, "vela": vela,
        "llama": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else LLAMA[2],
        "calavera_cirio": calavera_chica,
        "capa": capa, "pala_mango": pala_mango, "pala_hoja": sombra(pala_hoja, abajo=0.1),
        "costillar": costillar, "craneo": sombra(craneo, abajo=0.12), "haz": sombra(haz, abajo=0.1),
        "maza_mango": maza_mango, "maza_cabeza": sombra(maza_cabeza, abajo=0.12), "maza_cadena": maza_cadena,
    }
    return mats


def brillo(variante):
    """Lo que luce: la lucecita de alma en las cuencas y, en el portacirios, las llamas de sus velas."""
    C = {"R": ALMA, "r": ALMA_B}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "........",
        "..r..r..",
    ], C)
    b = {"cabeza": por_cara(liso(None), frente=ojos)}
    if variante == 0:
        b["llama"] = lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else None
    if variante == 2:
        # las calaveras del coloso tambien miran
        b["craneo"] = lambda cara, x, y, w, h: ALMA if cara == "frente" and y == 1 and x in (0, w - 1) else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos 9).
# _arma = hacia donde apunta lo que lleva en la derecha (a -Z de la mano). _agarra: la izquierda en el mango, tantos pixeles
# por detras (+) o por delante (-).

BASE = dict(raiz=raiz(-0.3), torso=(8, 0, 2), cabeza=(10, -6, 4), mandibula=(10, 0, 0),
            muslo_d=(-2, 0, 3), pierna_d=(4, 0, 0), muslo_i=(2, 0, -3), pierna_i=(6, 0, 0),
            brazo_i=(-2, 0, -6), antebrazo_i=(-10, 0, 0))
PORTE = {
    # el candelabro derecho, la vara en la mano y las velas por encima de la cabeza
    0: dict(_mano_d=("h", -0.6, -6.4, 3.4), _arma=(0.0, 1.0, 0.12), brazo_i=(-30, 0, -10), antebrazo_i=(-60, 0, 0)),
    # la pala al hombro, la hoja detras
    1: dict(_mano_d=("h", -0.4, -2.6, 4.4), _arma=(0.1, 0.55, -0.83), brazo_i=(-4, 0, -8), antebrazo_i=(-10, 0, 0)),
    # la maza arrastrando, la cabeza en el suelo por delante
    2: dict(_mano_d=("h", -0.6, -8.2, 2.0), _arma=(0.05, -0.7, 0.71), brazo_i=(-6, 0, -14), antebrazo_i=(-16, 0, 0)),
}
GUARDIA = dict(raiz=raiz(-1.0), torso=(14, 8, 0), cabeza=(-6, -8, 4), mandibula=(14, 0, 0),
               muslo_d=(-12, 0, 6), pierna_d=(22, 0, 0), muslo_i=(-2, 0, -6), pierna_i=(16, 0, 0))
LISTO = {
    0: con(GUARDIA, _mano_d=("h", 3.0, -6.0, 4.0), _arma=(0.55, 0.75, 0.38), _agarra=-5),
    1: con(GUARDIA, _mano_d=("h", 2.0, -5.6, 6.0), _arma=(0.45, -0.25, 0.86), _agarra=7),
    2: con(GUARDIA, torso=(14, 18, 0), cabeza=(-6, -16, 0), _mano_d=("h", -0.8, -2.6, 5.2), _arma=(0.1, 0.72, -0.68),
           brazo_i=(-30, 0, -24), antebrazo_i=(-40, 0, 0)),
}
# rezando (REZA del portacirios): de rodillas, el candelabro clavado delante y las manos en la vara
REZA = dict(raiz=raiz(-6.4, 0.6), torso=(16, 0, 0), cabeza=(28, 0, 0), mandibula=(18, 0, 0),
            muslo_d=(-6, 0, 4), pierna_d=(98, 0, 0), muslo_i=(-8, 0, -4), pierna_i=(98, 0, 0),
            _mano_d=("h", 1.6, -2.0, 7.4), _arma=(0.05, 1.0, 0.1), _agarra=-3)
# cavando (REZA del sepulturero): clava la pala y echa la tierra a un lado
C_CLAVA = con(BASE, raiz=raiz(-1.4, -0.4), torso=(34, 0, 0), cabeza=(-6, 0, 0), muslo_d=(-26, 0, 4), pierna_d=(30, 0, 0),
              muslo_i=(6, 0, -4), pierna_i=(12, 0, 0), _mano_d=("h", 2.0, -6.4, 6.4), _arma=(0.1, -0.9, 0.42), _agarra=8)
C_SACA = con(BASE, raiz=raiz(-0.8, 0.0), torso=(14, 30, 0), cabeza=(-6, -24, 0), muslo_d=(-10, 0, 4), pierna_d=(14, 0, 0),
             _mano_d=("h", 2.6, -3.6, 6.0), _arma=(-0.6, -0.2, 0.77), _agarra=7)
# hecho un monton de huesos (TUMBADO, y al deshacerse): de bruces, encogido, la calavera de lado
MONTON = dict(raiz=raiz(1.6, 12.8, rot=(88, 0, 22)), torso=(12, 0, 0), cabeza=(-24, 44, 22), mandibula=(34, 0, 0),
              muslo_d=(-64, 0, 24), pierna_d=(110, 0, 0), muslo_i=(-36, 0, -14), pierna_i=(84, 0, 0),
              brazo_d=(-60, 20, 50), antebrazo_d=(-80, 0, 0), brazo_i=(-30, 0, -70), antebrazo_i=(-60, 0, 0))
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

    # ---- quieto: los huesos le bailan sueltos, la mandibula se le mueve; de vez en cuando se le tuerce la cabeza
    bucle("quieto", 4.4, [
        (0.0, BASE),
        (1.2, con(BASE, raiz=raiz(-0.5), torso=(10, 4, 0), cabeza=(14, -16, 8), mandibula=(16, 0, 0))),
        (2.3, con(BASE, torso=(6, 0, 2), cabeza=(8, 0, 0), mandibula=(6, 0, 0))),
        (2.6, con(BASE, torso=(7, -2, 2), cabeza=(6, 20, -10), mandibula=(14, 0, 0))),
        (3.4, con(BASE, raiz=raiz(-0.5), torso=(9, -3, 0), cabeza=(12, 12, -4), mandibula=(10, 0, 0))),
        (4.4, BASE),
    ])
    # ---- andar: a trompicones, como si cada pierna fuera de uno
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-28, 22, 8, 18, 6, 0.0), (-4, 2, 6, 36, 0, 0.7), (22, -26, 20, 6, -6, 0.0), (2, -4, 36, 6, 0, 0.6), (-28, 22, 8, 18, 6, 0.0))):
        pasos.append((1.1 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (8, ty, -ty * 0.5), "cabeza": (8, -ty * 1.2, ty * 0.6), "raiz": raiz(up),
                                    "mandibula": (8 + abs(ty), 0, 0)}))
    bucle("andar", 1.1, pasos)
    for v in range(3):
        q = fija(con(BASE, PORTE[v]))
        bucle(f"porte_{VARIANTES[v]}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
        l0 = fija(LISTO[v])
        l1 = fija(con(LISTO[v], raiz=raiz(-1.6), torso=(LISTO[v]["torso"][0] + 4, LISTO[v]["torso"][1] - 3, 0)))
        bucle(f"listo_{VARIANTES[v]}", 1.8, [(0.0, l0), (0.9, l1), (1.8, l0)])
    # ---- rezando (el portacirios): de rodillas ante el candelabro, la calavera se balancea
    rz = fija(REZA)
    bucle("rezando", 5.0, [(0.0, rz), (2.5, fija(con(REZA, torso=(20, 0, 0), cabeza=(34, 0, 0), mandibula=(24, 0, 0)))), (5.0, rz)])
    # ---- cavando (el sepulturero): clava la pala (en el segundo 0.55 da en la tierra) y la saca echandola a su derecha
    bucle("cavando", 2.0, [
        (0.00, C_SACA),
        (0.35, con(C_CLAVA, torso=(26, 0, 0), _mano_d=("h", 2.0, -4.4, 6.4), _arma=(0.1, -0.85, 0.5))),
        (0.55, C_CLAVA),
        (0.95, con(C_CLAVA, torso=(30, 6, 0))),
        (1.40, C_SACA),
        (2.00, C_SACA),
    ])
    # ---- el monton de huesos (TUMBADO, y deshecho): solo la mandibula, de vez en cuando
    mo = fija(MONTON)
    bucle("monton", 6.0, [(0.0, mo), (4.6, mo), (5.0, fija(con(MONTON, mandibula=(44, 0, 0), cabeza=(-28, 50, 26)))), (6.0, mo)])

    # ================================================================ acciones
    L0, L1, L2 = LISTO[0], LISTO[1], LISTO[2]

    # ---- 1 candelazo (el portacirios): el candelabro a dos manos por encima de la cabeza y lo estrella delante (quema)
    CZ_ALTO = con(L0, raiz=raiz(0.0, 1.0), torso=(-12, 6, 0), cabeza=(-20, -4, 0), muslo_i=(-12, 0, -4), pierna_i=(10, 0, 0),
                  _mano_d=("h", 2.6, 6.0, 1.0), _arma=(0.05, 0.6, -0.8), _agarra=5)
    CZ_BAJO = con(L0, raiz=raiz(-3.0, -2.0), torso=(40, -4, 0), cabeza=(-26, 2, 0), muslo_d=(14, 0, 4), pierna_d=(26, 0, 0),
                  muslo_i=(-44, 0, -4), pierna_i=(46, 0, 0), _mano_d=("h", 3.0, -2.4, 7.4), _arma=(0.05, -0.62, 0.78), _agarra=5)
    accion("candelazo", 1.6, [
        (0.00, L0),
        (0.50, CZ_ALTO),
        (0.80, con(CZ_ALTO, raiz=raiz(0.2, 1.2), torso=(-16, 6, 0))),
        (0.88, con(L0, raiz=raiz(-1.0, 0.0), torso=(12, 2, 0), cabeza=(-22, 0, 0), _mano_d=("h", 2.8, 2.6, 7.6), _arma=(0.05, 0.45, 0.89),
                   _agarra=5)),
        (0.95, CZ_BAJO),
        (1.25, con(CZ_BAJO, torso=(38, -4, 0))),
        (1.60, L0),
    ], golpes=(0.95,))
    # ---- 2 barrido_cirio (el portacirios): el candelabro de derecha a izquierda, a la altura del pecho (las velas en arco)
    BC_D = con(L0, raiz=raiz(-1.0, 0.6), torso=(10, 50, 0), cabeza=(-4, -44, 0), _mano_d=("h", 0.4, -3.0, 5.0), _arma=(-1, 0.2, -0.1),
               _agarra=5)
    BC_F = con(L0, raiz=raiz(-1.8, -0.6), torso=(14, 8, 0), cabeza=(-8, -8, 0), _mano_d=("h", 3.0, -3.6, 6.8), _arma=(0.2, 0.15, 1),
               _agarra=5)
    BC_I = con(L0, raiz=raiz(-2.0, -1.4), torso=(18, -40, 0), cabeza=(-10, 34, 0), muslo_i=(-28, 0, -6), pierna_i=(26, 0, 0),
               _mano_d=("h", 5.4, -3.6, 4.6), _arma=(1, 0.15, 0.3), _agarra=3)
    accion("barrido_cirio", 1.4, [
        (0.00, L0),
        (0.45, BC_D),
        (0.62, BC_F),
        (0.72, BC_I),
        (1.00, con(BC_I, torso=(18, -44, 0))),
        (1.40, L0),
    ], golpes=(0.64,))
    # ---- 3 palada (el sepulturero): alza la pala por encima del hombro derecho y la baja de canto, en diagonal
    PL_ALTO = con(L1, raiz=raiz(-0.2, 1.0), torso=(-8, 30, 0), cabeza=(-14, -24, 0), _mano_d=("h", 0.4, 5.6, 2.0),
                  _arma=(-0.4, 0.6, -0.7), _agarra=4)
    PL_BAJO = con(L1, raiz=raiz(-2.6, -1.8), torso=(34, -24, 0), cabeza=(-22, 18, 0), muslo_i=(-40, 0, -4), pierna_i=(40, 0, 0),
                  _mano_d=("h", 3.6, -3.2, 6.6), _arma=(0.45, -0.55, 0.7), _agarra=6)
    accion("palada", 1.4, [
        (0.00, L1),
        (0.48, PL_ALTO),
        (0.70, con(PL_ALTO, torso=(-10, 34, 0))),
        (0.80, PL_BAJO),
        (1.05, con(PL_BAJO, torso=(32, -22, 0))),
        (1.40, L1),
    ], golpes=(0.80,))
    # ---- 4 tierra (el sepulturero): hunde la pala en el suelo y le echa la tierra a la cara (ciega un momento)
    TI_HUNDE = con(L1, raiz=raiz(-2.4, -0.6), torso=(40, 6, 0), cabeza=(-14, 0, 0), muslo_d=(-30, 0, 4), pierna_d=(34, 0, 0),
                   _mano_d=("h", 2.0, -6.8, 6.0), _arma=(0.1, -0.85, 0.5), _agarra=8)
    TI_ECHA = con(L1, raiz=raiz(-0.6, 0.6), torso=(-6, -10, 0), cabeza=(-16, 6, 0), muslo_i=(-26, 0, -4), pierna_i=(24, 0, 0),
                  _mano_d=("h", 2.4, 2.4, 7.4), _arma=(0.1, 0.75, 0.66), _agarra=7)
    accion("tierra", 1.5, [
        (0.00, L1),
        (0.40, TI_HUNDE),
        (0.60, con(TI_HUNDE, torso=(44, 6, 0))),
        (0.78, TI_ECHA),
        (1.05, con(TI_ECHA, torso=(-4, -8, 0))),
        (1.50, L1),
    ], golpes=(0.80,))
    # ---- 5 mazazo (el coloso): la maza a dos manos muy por encima y la estrella (la onda)
    MZ_ALTO = con(L2, raiz=raiz(0.0, 1.2), torso=(-18, 4, 0), cabeza=(-20, -4, 0), muslo_d=(8, 0, 4), pierna_d=(8, 0, 0),
                  muslo_i=(-14, 0, -4), pierna_i=(10, 0, 0), _mano_d=("h", 2.8, 6.4, -0.2), _arma=(0.05, 0.4, -0.92), _agarra=4)
    MZ_BAJO = con(L2, raiz=raiz(-3.6, -2.2), torso=(48, -4, 0), cabeza=(-28, 2, 0), muslo_d=(16, 0, 4), pierna_d=(32, 0, 0),
                  muslo_i=(-52, 0, -4), pierna_i=(54, 0, 0), _mano_d=("h", 3.2, -3.0, 6.8), _arma=(0.05, -0.66, 0.75), _agarra=4)
    accion("mazazo", 2.0, [
        (0.00, L2),
        (0.65, MZ_ALTO),
        (1.05, con(MZ_ALTO, raiz=raiz(0.3, 1.5), torso=(-22, 4, 0))),
        (1.13, con(L2, raiz=raiz(-1.4, 0.0), torso=(14, 2, 0), cabeza=(-24, 0, 0), _mano_d=("h", 3.0, 2.6, 7.4), _arma=(0.05, 0.35, 0.94),
                   _agarra=4)),
        (1.20, MZ_BAJO),
        (1.60, con(MZ_BAJO, torso=(46, -4, 0))),
        (2.00, L2),
    ], golpes=(1.20,))
    # ---- 6 barrido_maza (el coloso): de derecha a izquierda, muy abierto
    BM_D = con(L2, raiz=raiz(-1.2, 0.8), torso=(12, 58, 0), cabeza=(-2, -50, 0), _mano_d=("h", 0.4, -3.6, 4.4), _arma=(-1, 0.1, -0.2),
               _agarra=5)
    BM_F = con(L2, raiz=raiz(-1.8, -0.6), torso=(16, 12, 0), cabeza=(-6, -12, 0), _mano_d=("h", 3.2, -4.4, 6.0), _arma=(0.2, 0.0, 1),
               _agarra=5)
    BM_I = con(sin(L2, "_agarra"), raiz=raiz(-2.2, -1.6), torso=(20, -40, 0), cabeza=(-8, 32, 0), muslo_i=(-32, 0, -6), pierna_i=(28, 0, 0),
               _mano_d=("h", 5.4, -4.2, 4.2), _arma=(1, 0.05, 0.3), brazo_i=(-20, 0, -60), antebrazo_i=(-30, 0, 0))
    accion("barrido_maza", 1.7, [
        (0.00, L2),
        (0.55, BM_D),
        (0.80, con(BM_D, torso=(12, 64, 0), cabeza=(-2, -54, 0))),
        (0.92, BM_F),
        (1.02, BM_I),
        (1.30, con(BM_I, torso=(20, -44, 0))),
        (1.70, L2),
    ], golpes=(0.92,))
    # ---- 7 pisoton (el coloso): alza la pierna derecha y pisa con todo su peso (tumba a los de alrededor)
    PS_ALZA = con(L2, raiz=raiz(0.6, 0.6), torso=(-10, 6, 6), cabeza=(-12, -4, 0), muslo_d=(-86, 0, 8), pierna_d=(70, 0, 0),
                  muslo_i=(4, 0, -6), pierna_i=(6, 0, 0), brazo_i=(-40, 0, -50), antebrazo_i=(-40, 0, 0))
    PS_PISA = con(L2, raiz=raiz(-2.4, -0.6), torso=(26, 0, -4), cabeza=(-18, 0, 0), muslo_d=(-26, 0, 6), pierna_d=(20, 0, 0),
                  muslo_i=(14, 0, -6), pierna_i=(30, 0, 0), brazo_i=(-10, 0, -40), antebrazo_i=(-20, 0, 0))
    accion("pisoton", 1.5, [
        (0.00, L2),
        (0.50, PS_ALZA),
        (0.72, con(PS_ALZA, muslo_d=(-92, 0, 8), raiz=raiz(0.9, 0.6))),
        (0.82, PS_PISA),
        (1.10, con(PS_PISA, torso=(24, 0, -4))),
        (1.50, L2),
    ], golpes=(0.82,))
    # ---- 8 aturdido: se le descoyuntan los huesos
    AT = dict(raiz=raiz(-1.2, 2.0), torso=(-20, 12, 10), cabeza=(-24, -16, 20), mandibula=(28, 0, 0),
              muslo_d=(-14, 0, 10), pierna_d=(30, 0, 0), muslo_i=(18, 0, -10), pierna_i=(20, 0, 0),
              brazo_d=(16, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -30), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-6, 8, -12), cabeza=(-18, -10, -18), raiz=raiz(-2.0, 2.6))),
        (1.05, con(AT, torso=(-10, -8, 10), cabeza=(-22, 10, 14), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir (polvo): se derrumba en un monton y se deshace (el servidor echa el polvo de hueso; se apaga)
    RODILLAS = dict(raiz=raiz(-6.0, 0.8), torso=(26, 0, 0), cabeza=(30, 0, 0), mandibula=(30, 0, 0), muslo_d=(-10, 0, 5),
                    pierna_d=(96, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(96, 0, 0), brazo_d=(-10, 0, 14), antebrazo_d=(-10, 0, 0),
                    brazo_i=(-10, 0, -14), antebrazo_i=(-10, 0, 0))
    accion("morir", 1.6, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), mandibula=(30, 0, 0), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0))),
        (0.40, RODILLAS),
        (0.90, MONTON),
        (1.60, con(MONTON, raiz=raiz(0.6, 12.8, rot=(90, 0, 26)), mandibula=(50, 0, 0))),
    ])
    # ---- 10 deshacerse: se le sueltan los huesos y cae hecho un monton (si nadie lo pisotea, se vuelve a armar)
    accion("deshacerse", 1.1, [
        (0.00, con(BASE, torso=(-12, 0, 10), cabeza=(-34, 20, 10), mandibula=(36, 0, 0), brazo_d=(20, 0, 40), brazo_i=(20, 0, -40))),
        (0.30, con(RODILLAS, raiz=raiz(-4.0, 0.4), torso=(10, 20, 14), cabeza=(30, -30, 20))),
        (0.70, con(MONTON, raiz=raiz(3.0, 10.0, rot=(70, 0, 20)))),
        (1.10, MONTON),
    ])
    # ---- 11 rehacerse: el monton tiembla, los huesos se buscan y se pone en pie (tambien al despertar tumbado)
    accion("rehacerse", 2.0, [
        (0.00, MONTON),
        (0.40, con(MONTON, cabeza=(-10, 30, 0), mandibula=(50, 0, 0), brazo_d=(-90, 0, 60))),
        (0.80, con(MONTON, raiz=raiz(2.4, 10.0, rot=(60, 0, 10)), brazo_d=(-130, 0, 30), brazo_i=(-120, 0, -30))),
        (1.25, con(RODILLAS, torso=(36, 0, 0), cabeza=(-20, 0, 0), mandibula=(20, 0, 0), brazo_d=(-30, 0, 40), brazo_i=(-30, 0, -40))),
        (1.65, con(GUARDIA, raiz=raiz(-2.6), muslo_d=(-46, 0, 6), pierna_d=(64, 0, 0))),
        (2.00, GUARDIA),
    ])
    # ---- 12 levantarse: estaba de rodillas rezando o cavando; se vuelve y se pone en pie (el giro lo pone el servidor)
    accion("levantarse", 1.1, [
        (0.00, REZA),
        (0.35, con(REZA, raiz=raiz(-5.0, 0.4), torso=(6, 0, 0), cabeza=(-16, -20, 0))),
        (0.75, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0))),
        (1.10, GUARDIA),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "sepulturero" in nombre or nombre in ("palada", "tierra", "cavando"):
        return 1
    if "coloso" in nombre or nombre in ("mazazo", "barrido_maza", "pisoton"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_portacirios"]]
    return []
