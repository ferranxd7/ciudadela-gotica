"""
El ahorcado del Bosque Muerto (parte 7g): los que colgaron en la Arboleda de los Ahorcados y la Sombra no dejo morir.
Flaco y largo, el cuello estirado y torcido, la piel gris verdosa con moratones, la camisa y los calzones rotos, descalzo,
la soga cortada al cuello y los ojos de un verde palido. Esperan colgados de las ramas (la soga de verdad: una correa del
juego atada a la rama) y se descuelgan sobre quien pasa por debajo. Dos:
  0  las garras  (la cabeza al aire; unas unas largas y negras: zarpazo, doble zarpazo y salto)
  1  la soga     (un saco de arpillera por la cabeza; la soga larga en la mano: latigazo y lazo, que engancha y tira)
"""
from anim import Anim, con, raiz, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide
from pinta import (brillo_de, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,  # noqa: F401
                   sombra, tela)

NOMBRE = "ahorcado"
VARIANTES = ("garras", "soga")
ESCALA = 1.1

# ---------------------------------------------------------------- el modelo


#: el nudo de la soga en la postura de colgado (px, absolutos, con el torso derecho): colgado, el cuerpo gira alrededor de
#: el (es donde la soga sube a la rama) y el servidor coloca al ahorcado para que caiga justo bajo la soga de bloques
NUDO = (4.4, -5.25, 0.0)
#: lo que sube la soga de arriba desde el nudo (px): por encima, la soga de bloques de la rama
SOGA_ARRIBA = 12.0


def rig():
    r = Rig(NOMBRE, 128)
    # el colgadero: el nudo (colgado, el bucle lo gira; en lo demas no se mueve). La raiz cuelga de el.
    r.pieza("colgadero", "", NUDO)
    # largo y flaco: los brazos casi hasta las rodillas, el cuello estirado
    humanoide(r, torso=(7, 13, 4), cabeza=(7, 8, 7), brazo=(3, 18, 3), pierna=(3, 14, 3), cuello=1.5)
    raiz = r.por_nombre["raiz"]
    raiz.parent = "colgadero"
    raiz.pivot = (-NUDO[0], 24 - NUDO[1], -NUDO[2])
    # el faldon de la camisa, por fuera de los calzones
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2.5, 8, 5, 5, "faldon", grow=g(0.3))])
    # la soga al cuello (en el torso: la cabeza se ladea sin moverla): la vuelta y el nudo, a su izquierda
    r.pieza("soga_cuello", "torso", (0, -13.75, 0), boxes=[
        Box(-3.5, -0.75, -3.5, 7, 1.5, 7, "soga", grow=g(0.25)),
        Box(3.4, -1.5, -1.0, 2, 2, 2, "nudo"),
    ])
    # el cabo cortado que cuelga por la espalda (si ya no cuelga)
    r.pieza("cabo", "torso", (0, -13, 2.2), boxes=[Box(-0.5, 0, 0, 1, 7, 1, "soga")])
    # la soga de arriba (colgado): del nudo a la soga de bloques de la rama
    r.pieza("soga_arriba", "colgadero", (0, 0, 0), boxes=[Box(-0.75, -SOGA_ARRIBA, -0.75, 1.5, SOGA_ARRIBA, 1.5, "soga")])
    # el saco de arpillera (1), con la soga que lo ata al cuello
    r.pieza("saco", "cabeza", (0, 0, 0), boxes=[
        Box(-3.5, -8.5, -3.5, 7, 9, 7, "saco", grow=g(0.55)),
        Box(-1.5, -10.0, -1.5, 3, 2, 3, "saco_pico"),
    ])
    # las unas: cuelgan de las manos (con el brazo caido, hacia abajo)
    for lado in ("d", "i"):
        r.pieza(f"unas_{lado}", f"mano_{lado}", (0, 0, 0), boxes=[
            Box(-1.5, -0.5, -0.5, 3, 4, 0, "unas"),
            Box(-1.5, -0.5, 0.5, 3, 3, 0, "unas"),
        ])
    # la soga larga (1): en la mano derecha, con el lazo en la punta (apunta a -Z como un arma)
    r.pieza("soga_mano", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -12, 1, 1, 12, "cuerda"),
        Box(-0.5, -0.5, -20, 1, 1, 8, "cuerda"),
    ])
    r.pieza("lazo", "soga_mano", (0, 0, -20), boxes=[
        Box(-2.5, -0.5, -5, 5, 1, 1, "cuerda"),
        Box(-2.5, -0.5, -5, 1, 1, 5, "cuerda"),
        Box(1.5, -0.5, -5, 1, 1, 5, "cuerda"),
        Box(-1, -1, -0.5, 2, 2, 1.5, "nudo"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {
    0: ["saco", "soga_mano", "lazo", "soga_arriba"],
    1: ["unas_d", "soga_arriba"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#2c332c", "#3d463c", "#515a4e", "#666f61", "#7d8576", "#959c8c"]
MORADO = ["#2a1d2c", "#3d2a40", "#523856", "#684a6b"]
CAMISA = ["#4d4a40", "#636056", "#7a776b", "#918d80", "#a9a596"]
CALZON = ["#241e18", "#322a21", "#40362b", "#4f4335", "#5e5040"]
SOGA = ["#3e3020", "#56442c", "#6e5838", "#876e46", "#a08656"]
SACO = ["#4a3c26", "#5e4d31", "#73603e", "#88744c", "#9d885b"]
UNA = ["#0e0c0c", "#1c1919", "#2b2726"]
OJO = ["#3a5a20", "#6c9a32", "#a8d84c", "#e0ff8c"]


def _cara(variante):
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": "#0b0d0a", "k": "#1d221b", "M": "#100a0c", "L": "#3a2030",
         "T": "#8c8a76", "V": MORADO[2]}
    filas = [
        "OossssO",
        "oSssSso",
        "oVSsSVo",
        "OKKoKKO",
        "OkKokKO",
        "OosssoO",
        "OLMMMLO",
        "OoMTMoO",
    ]
    return dibujo(filas, C)


def materiales(variante):
    piel = ruido(PIEL[1:5], 31, grano=0.45, base=0.5)
    camisa = tela(CAMISA, 61 + variante, pliegue=3)
    calzon = tela(CALZON, 63, pliegue=2)
    soga = madera(SOGA, 65, "y")
    saco_t = tela(SACO, 67, pliegue=4)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara(variante)(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq") and y >= h - 2:
            return MORADO[1 + (x % 2)]               # el cuello amoratado bajo la soga
        if cara == "arriba" and (x + y * 3) % 7 == 0:
            return PIEL[0]                            # el pelo ralo
        return c

    def torso(cara, x, y, w, h):
        c = camisa(cara, x, y, w, h)
        if cara == "frente":
            # la camisa abierta y rota: asoma el pecho
            if y <= 4 and abs(x - 3) <= 1:
                return piel(cara, x, y, w, h) if y > 0 else MORADO[1]
            if (x * 5 + y * 3) % 11 == 0 and y > 5:
                return None if y < h - 1 else c        # agujeros
        if y <= 1 and cara != "abajo":
            return MORADO[(x + y) % 2]                 # el cuello
        return oscuro(c, 0.04 * (y % 3))

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        corte = (4, 3, 5, 2, 4, 5, 3, 4, 2)[(x * 2 + {"frente": 0, "espalda": 3, "der": 1, "izq": 5}.get(cara, 0)) % 9]
        if y > corte:
            return None
        return oscuro(camisa(cara, x, y + 6, w, h + 6), 0.08)

    def brazo(cara, x, y, w, h):
        if y <= 4:
            c = camisa(cara, x, y, w, h)
            if y == 4 and (x + len(cara)) % 2 == 0:
                return None                            # la manga deshilachada
            return c
        return piel(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if (x * 3 + y) % 9 == 0:
            return MORADO[1]
        if y >= h - 2:
            return PIEL[1]                             # los nudillos huesudos
        return c

    def muslo(cara, x, y, w, h):
        return calzon(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        if y <= 2:
            c = calzon(cara, x, y, w, h)
            return None if y == 2 and (x + len(cara)) % 2 else c
        if y >= h - 1:
            return "#1c1a16" if cara == "abajo" else PIEL[0]   # los pies sucios
        return piel(cara, x, y, w, h)

    def saco(cara, x, y, w, h):
        c = saco_t(cara, x, y, w, h)
        if cara == "frente":
            # los dos agujeros de los ojos, rasgados
            if y in (4, 5) and x in (1, 2, 4, 5):
                return "#0a0806"
            if y == 7 and 2 <= x <= 4:
                return oscuro(c, 0.3)
        if cara == "abajo":
            return None
        if y >= h - 1 and cara != "arriba":
            return SOGA[2]                             # la soga que lo cierra al cuello
        if (x * 7 + y * 3) % 13 == 0:
            return oscuro(c, 0.25)                     # manchas
        return c

    C = {"N": UNA[0], "n": UNA[1], "g": UNA[2]}
    unas = dibujo([
        "gng",
        "NnN",
        "N.N",
        "N.N",
        "n.n",
    ], C)

    mats = {
        "cabeza": sombra(cabeza, abajo=0.15),
        "torso": sombra(torso),
        "faldon": faldon,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.2), "antebrazo_i": sombra(antebrazo, abajo=0.2),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
        "soga": soga,
        "nudo": madera(SOGA[1:], 66, "x"),
        "cuerda": madera(SOGA, 68, "z"),
        "saco": sombra(saco, abajo=0.12),
        "saco_pico": sombra(saco_t, abajo=0.2),
        "unas": por_cara(liso(None), frente=unas, espalda=espejado(unas)),
    }
    return mats


def brillo(variante):
    """Lo que brilla: los ojos verdes (por los agujeros del saco, en el de la soga)."""
    C = {"G": OJO[2], "g": OJO[1], "Y": OJO[3]}
    if variante == 1:
        ojos = dibujo([
            "........",
            "........",
            "........",
            "........",
            ".gY..Yg.",
            ".gg..gg.",
        ], C)
        return {"saco": por_cara(liso(None), frente=ojos)}
    ojos = dibujo([
        ".......",
        ".......",
        ".......",
        ".gY.Yg.",
        "..g.g..",
    ], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 18)

# de pie: encorvado, la cabeza caida a un lado (el cuello roto), los brazos colgando
BASE = dict(raiz=raiz(-0.4), torso=(16, 0, 0), cabeza=(14, 0, 26), muslo_d=(-4, 0, 3), pierna_d=(8, 0, 0), muslo_i=(2, 0, -3),
            pierna_i=(6, 0, 0), brazo_d=(-6, 0, 8), antebrazo_d=(-10, 0, 0), brazo_i=(-4, 0, -8), antebrazo_i=(-12, 0, 0),
            faldon=(6, 0, 0))
# colgado: las piernas muertas, las puntas de los pies abajo, los brazos sueltos, la cabeza vencida
COLGADO = dict(raiz=raiz(0.0), torso=(0, 0, 0), cabeza=(22, 0, -32), muslo_d=(2, 0, 2), pierna_d=(4, 0, 0), muslo_i=(-2, 0, -2),
               pierna_i=(6, 0, 0), brazo_d=(2, 0, 6), antebrazo_d=(-4, 0, 0), brazo_i=(4, 0, -6), antebrazo_i=(-6, 0, 0),
               faldon=(0, 0, 0))
# en guardia: agachado, las garras delante
LISTO = dict(raiz=raiz(-1.8), torso=(30, 0, 0), cabeza=(-14, 0, 14), muslo_d=(-30, 0, 6), pierna_d=(46, 0, 0), muslo_i=(-14, 0, -6),
             pierna_i=(36, 0, 0), brazo_d=(-62, -10, 22), antebrazo_d=(-40, 0, 0), brazo_i=(-56, 10, -22), antebrazo_i=(-44, 0, 0),
             faldon=(20, 0, 0))
# la de la soga: el brazo derecho con la soga enrollada a un lado
SOGA_LISTA = dict(brazo_d=(-30, 0, 30), antebrazo_d=(-50, 0, 0), mano_d=(0, 0, 0))


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

    # ---- quieto: se balancea despacio, como si aun colgara
    bucle("quieto", 3.6, [
        (0.0, BASE),
        (1.2, con(BASE, torso=(18, 4, 4), cabeza=(18, 6, 22), brazo_d=(-2, 0, 10), brazo_i=(-8, 0, -6))),
        (2.4, con(BASE, torso=(14, -4, -2), cabeza=(10, -4, 30), brazo_d=(-10, 0, 6), brazo_i=(0, 0, -10))),
        (3.6, BASE),
    ])
    # ---- andar: a trompicones, los brazos sueltos
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-24, 18, 10, 24, 8, 0.0), (-6, 2, 8, 44, 0, 0.8), (18, -24, 24, 10, -8, 0.0), (2, -6, 44, 8, 0, 0.8), (-24, 18, 10, 24, 8, 0.0))):
        pasos.append((1.4 * k / 4, {"muslo_d": (md, 0, 3), "muslo_i": (mi, 0, -3), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (18, ty, -ty * 0.6), "cabeza": (12, -ty, 26), "raiz": raiz(-0.4 + up),
                                    "brazo_d": (-md * 0.5, 0, 10), "brazo_i": (-mi * 0.5, 0, -10), "faldon": (-md * 0.2, 0, 0)}))
    bucle("andar", 1.4, pasos)
    # ---- colgado: gira despacio en su soga (alrededor del nudo, que es donde sube la soga)
    bucle("colgado", 6.0, [
        (0.0, con(COLGADO, colgadero=(0, -14, 0))),
        (1.5, con(COLGADO, colgadero=(2, 0, 2), brazo_d=(6, 0, 8), pierna_i=(10, 0, 0))),
        (3.0, con(COLGADO, colgadero=(0, 14, 0), cabeza=(24, 0, -28))),
        (4.5, con(COLGADO, colgadero=(-2, 0, -2), brazo_i=(8, 0, -8), pierna_d=(10, 0, 0))),
        (6.0, con(COLGADO, colgadero=(0, -14, 0))),
    ])
    # la soga al costado (1), colgando de la mano, con lo demas del bucle que toque
    PORTE = dict(brazo_d=(-6, 0, 12), antebrazo_d=(-30, 0, 0), mano_d=(70, 0, 0))
    bucle("porte_soga", 3.0, [(0.0, PORTE), (1.5, con(PORTE, brazo_d=(-10, 0, 14))), (3.0, PORTE)],
          solo=("brazo_d", "antebrazo_d", "mano_d"))
    bucle("listo_ahorcado", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-2.2), torso=(34, 4, 0), cabeza=(-18, 6, 10))),
        (2.0, LISTO),
    ])
    bucle("listo_soga", 2.0, [
        (0.0, con(LISTO, SOGA_LISTA)),
        (1.0, con(LISTO, SOGA_LISTA, raiz=raiz(-2.2), torso=(34, 4, 0), cabeza=(-18, 6, 10))),
        (2.0, con(LISTO, SOGA_LISTA)),
    ], solo=("brazo_d", "antebrazo_d", "mano_d"))
    # ---- de rodillas (rezando a su arbol) y tumbado (un muerto mas al pie del arbol)
    REZA = dict(raiz=raiz(-9.0, 1.0), muslo_d=(-10, 0, 6), pierna_d=(100, 0, 0), muslo_i=(-10, 0, -6), pierna_i=(100, 0, 0),
                torso=(34, 0, 0), cabeza=(30, 0, 20), brazo_d=(-10, 0, 10), antebrazo_d=(-30, 0, 0), brazo_i=(-10, 0, -10),
                antebrazo_i=(-30, 0, 0), faldon=(-20, 0, 0))
    bucle("rezar", 4.0, [(0.0, REZA), (2.0, con(REZA, torso=(38, 0, 0), cabeza=(34, 0, 16))), (4.0, REZA)])
    TUMBADO = dict(raiz=raiz(-14.8, 0.0, rot=(-88, 0, 0)), torso=(0, 0, 0), cabeza=(-10, 30, 0), muslo_d=(0, 0, 6), pierna_d=(10, 0, 0),
                   muslo_i=(0, 0, -6), pierna_i=(4, 0, 0), brazo_d=(-170, 0, 20), antebrazo_d=(0, 0, 0), brazo_i=(-10, 0, -30),
                   antebrazo_i=(-20, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (4.0, TUMBADO)])

    # ---- 1 zarpazo: de arriba a la derecha hacia abajo a la izquierda
    Z_ALZA = con(LISTO, raiz=raiz(-1.4), torso=(18, 24, 0), brazo_d=(-150, 0, 40), antebrazo_d=(-20, 0, 0))
    Z_BAJA = con(LISTO, raiz=raiz(-2.6), torso=(40, -30, 0), cabeza=(-20, 10, 10), brazo_d=(-40, 30, -10), antebrazo_d=(-10, 0, 0))
    accion("zarpazo", 1.1, [(0.00, LISTO), (0.40, Z_ALZA), (0.56, Z_BAJA), (0.80, con(Z_BAJA, torso=(36, -26, 0))), (1.10, LISTO)],
           golpes=(0.55,))
    # ---- 2 doble: derecha y luego izquierda
    D_ALZA_I = con(Z_BAJA, brazo_i=(-150, 0, -40), antebrazo_i=(-20, 0, 0), torso=(26, -30, 0))
    D_BAJA_I = con(LISTO, raiz=raiz(-2.6), torso=(40, 30, 0), cabeza=(-20, -10, 10), brazo_i=(-40, -30, 10), antebrazo_i=(-10, 0, 0),
                   brazo_d=(-40, 30, -10), antebrazo_d=(-10, 0, 0))
    accion("doble", 1.5, [(0.00, LISTO), (0.34, Z_ALZA), (0.50, Z_BAJA), (0.78, D_ALZA_I), (0.95, D_BAJA_I),
                          (1.20, con(D_BAJA_I, torso=(36, 26, 0))), (1.50, LISTO)], golpes=(0.49, 0.94))
    # ---- 3 salto: se agacha, salta con las dos garras por delante y cae (lo mueve el servidor)
    S_AGACHA = con(LISTO, raiz=raiz(-4.4), torso=(44, 0, 0), cabeza=(-30, 0, 0), muslo_d=(-70, 0, 8), pierna_d=(100, 0, 0),
                   muslo_i=(-64, 0, -8), pierna_i=(96, 0, 0), brazo_d=(-20, 0, 30), brazo_i=(-20, 0, -30))
    S_VUELA = con(LISTO, raiz=raiz(1.0), torso=(20, 0, 0), cabeza=(-24, 0, 0), muslo_d=(-10, 0, 8), pierna_d=(30, 0, 0), muslo_i=(20, 0, -8),
                  pierna_i=(40, 0, 0), brazo_d=(-170, -20, 20), antebrazo_d=(-10, 0, 0), brazo_i=(-170, 20, -20), antebrazo_i=(-10, 0, 0))
    S_CAE = con(LISTO, raiz=raiz(-4.0), torso=(54, 0, 0), cabeza=(-20, 0, 10), muslo_d=(-60, 0, 8), pierna_d=(90, 0, 0), muslo_i=(-40, 0, -8),
                pierna_i=(80, 0, 0), brazo_d=(-60, 0, 10), antebrazo_d=(-10, 0, 0), brazo_i=(-60, 0, -10), antebrazo_i=(-10, 0, 0))
    accion("salto", 1.6, [(0.00, LISTO), (0.45, S_AGACHA), (0.62, S_VUELA), (1.00, S_VUELA), (1.08, S_CAE), (1.30, S_CAE), (1.60, LISTO)],
           golpes=(1.05,))
    # ---- 4 latigazo: voltea la soga por encima de la cabeza y la descarga delante (llega lejos)
    L_ALTO = con(LISTO, SOGA_LISTA, raiz=raiz(-0.6), torso=(0, 20, 0), cabeza=(-20, 0, 10),
                 brazo_d=(-170, 0, 30), antebrazo_d=(-10, 0, 0), mano_d=(-40, 0, 0))
    L_GIRA = con(L_ALTO, brazo_d=(-170, 0, -20), mano_d=(40, 0, 0))
    L_DELANTE = con(LISTO, SOGA_LISTA, raiz=raiz(-2.4), torso=(40, -20, 0), cabeza=(-20, 10, 10),
                    brazo_d=(-80, 0, 0), antebrazo_d=(-6, 0, 0), mano_d=(0, 0, 0))
    accion("latigazo", 1.6, [(0.00, con(LISTO, SOGA_LISTA)), (0.35, L_ALTO), (0.55, L_GIRA), (0.75, L_ALTO), (0.86, L_DELANTE),
                             (1.20, con(L_DELANTE, torso=(36, -16, 0))), (1.60, con(LISTO, SOGA_LISTA))], golpes=(0.85,))
    # ---- 5 lazo: lanza la soga (el brazo entero delante) y tira de ella (lo que engancha, se lo trae)
    LZ_ATRAS = con(LISTO, SOGA_LISTA, raiz=raiz(-1.0), torso=(10, 34, 0), cabeza=(-14, -20, 0), brazo_d=(-110, 0, 50), antebrazo_d=(-50, 0, 0))
    LZ_LANZA = con(LISTO, SOGA_LISTA, raiz=raiz(-2.4), torso=(36, -14, 0), cabeza=(-24, 8, 0), brazo_d=(-96, 0, -4), antebrazo_d=(0, 0, 0),
                   mano_d=(0, 0, 0))
    LZ_TIRA = con(LISTO, SOGA_LISTA, raiz=raiz(-1.2, -1.0), torso=(-4, 20, 0), cabeza=(-10, -10, 0), brazo_d=(-40, 0, 40),
                  antebrazo_d=(-90, 0, 0), brazo_i=(-70, 0, -10), antebrazo_i=(-60, 0, 0))
    accion("lazo", 2.0, [(0.00, con(LISTO, SOGA_LISTA)), (0.55, LZ_ATRAS), (0.86, LZ_LANZA), (1.10, LZ_LANZA), (1.40, LZ_TIRA),
                         (1.70, con(LZ_TIRA, torso=(-8, 24, 0))), (2.00, con(LISTO, SOGA_LISTA))], golpes=(0.90,))
    # ---- 6 caer: se descuelga de la rama, cae y aterriza agachado (lo que pilla debajo, se lo lleva)
    C_SUELTA = con(COLGADO, brazo_d=(-30, 0, 30), brazo_i=(-30, 0, -30), cabeza=(-10, 0, 10), muslo_d=(-20, 0, 6), muslo_i=(-20, 0, -6),
                   pierna_d=(30, 0, 0), pierna_i=(30, 0, 0))
    C_ATERRIZA = con(LISTO, raiz=raiz(-5.0), torso=(50, 0, 0), cabeza=(-30, 0, 10), muslo_d=(-80, 0, 10), pierna_d=(110, 0, 0),
                     muslo_i=(-74, 0, -10), pierna_i=(106, 0, 0), brazo_d=(-30, 0, 40), antebrazo_d=(-30, 0, 0), brazo_i=(-30, 0, -40),
                     antebrazo_i=(-30, 0, 0))
    # (el servidor lo suelta en el tick 2 y cae en unos 5: toca el suelo hacia el tick 7 y pega en el 8)
    accion("caer", 1.3, [(0.00, COLGADO), (0.10, C_SUELTA), (0.34, C_SUELTA), (0.42, C_ATERRIZA), (0.80, C_ATERRIZA), (1.30, LISTO)],
           golpes=(0.42,))
    # ---- 7 aturdido
    AT = dict(raiz=raiz(-1.2, 2.0), torso=(-8, 0, 10), cabeza=(-20, 0, 30), muslo_d=(-8, 0, 8), pierna_d=(20, 0, 0), muslo_i=(10, 0, -8),
              pierna_i=(16, 0, 0), brazo_d=(16, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -30), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [(0.00, con(AT, torso=(10, 0, 0), raiz=raiz(-0.8, 0.0))), (0.20, AT),
                             (0.70, con(AT, torso=(-4, 8, -10), cabeza=(-12, -8, 20), raiz=raiz(-1.8, 2.6))),
                             (1.10, con(AT, torso=(-10, -6, 10), cabeza=(-18, 8, 34))), (1.50, con(AT, torso=(14, 0, 0), raiz=raiz(-0.8, 0.4)))])
    # ---- 8 morir: se le doblan las rodillas y cae de bruces, la cabeza colgando como en la soga
    RODILLAS = dict(raiz=raiz(-9.4, 0.6), muslo_d=(-8, 0, 6), pierna_d=(98, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0),
                    torso=(10, 0, 4), cabeza=(40, 0, 30), brazo_d=(10, 0, 20), antebrazo_d=(-6, 0, 0), brazo_i=(8, 0, -20),
                    antebrazo_i=(-6, 0, 0), faldon=(-10, 0, 0))
    BRUCES = dict(raiz=raiz(-13.6, 9.0, rot=(84, 0, 0)), torso=(6, 0, 0), cabeza=(10, 30, 0), muslo_d=(-6, 0, 6), pierna_d=(16, 0, 0),
                  muslo_i=(-4, 0, -6), pierna_i=(10, 0, 0), brazo_d=(-170, 0, 10), antebrazo_d=(0, 0, 0), brazo_i=(-20, 0, -40),
                  antebrazo_i=(-10, 0, 0), faldon=(0, 0, 0))
    accion("morir", 2.4, [(0.00, con(BASE, cabeza=(40, 0, 34))), (0.60, RODILLAS), (1.20, con(RODILLAS, torso=(24, 0, 4))),
                          (1.70, BRUCES), (2.40, BRUCES)])
    # ---- 9 levantarse (estaba de rodillas) y 10 alzarse (estaba tumbado)
    accion("levantarse", 1.6, [(0.00, REZA), (0.60, con(REZA, torso=(20, 10, 0), cabeza=(10, 10, 20))),
                               (1.00, dict(raiz=raiz(-4.0, 0.4), muslo_d=(-60, 0, 6), pierna_d=(80, 0, 0), muslo_i=(0, 0, -6),
                                           pierna_i=(60, 0, 0), torso=(36, 0, 0), cabeza=(0, 0, 24))), (1.60, LISTO)])
    accion("alzarse", 2.0, [(0.00, TUMBADO), (0.60, dict(raiz=raiz(-12.0, 2.0, rot=(-50, 0, 0)), torso=(30, 0, 0), cabeza=(-20, 0, 20),
                                                          brazo_d=(-60, 0, 20), brazo_i=(-60, 0, -20), muslo_d=(-40, 0, 6),
                                                          pierna_d=(60, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(50, 0, 0))),
                            (1.20, dict(raiz=raiz(-6.0, 0.6), torso=(50, 0, 0), cabeza=(0, 0, 26), muslo_d=(-70, 0, 6), pierna_d=(100, 0, 0),
                                        muslo_i=(-20, 0, -6), pierna_i=(80, 0, 0), brazo_d=(-30, 0, 20), brazo_i=(-30, 0, -20))),
                            (2.00, LISTO)])
    return B, A


# lo que necesita el servidor para colgarlo bajo su soga (en AhorcadoAnims)
TABLA_EXTRA = (
    "",
    "    /** El nudo de la soga en la postura de colgado (px del modelo, sin escalar) y lo que sube la soga del modelo. */",
    f"    public static final float NUDO_X = {NUDO[0]}f, NUDO_Y = {NUDO[1]}f, NUDO_Z = {NUDO[2]}f, SOGA_ARRIBA = {SOGA_ARRIBA}f;",
    f"    public static final float ESCALA = {ESCALA}f;",
)


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 1 if nombre in ("latigazo", "lazo", "listo_soga", "porte_soga") else 0


def reposo(v, anims):
    import anim
    if v == 1:
        return anim.sumar(anims["quieto"].muestra(0), anims["porte_soga"].muestra(0))
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    if nombre == "listo_soga":
        return [anims["listo_ahorcado"]]
    if nombre == "porte_soga":
        return [anims["quieto"]]
    return []
