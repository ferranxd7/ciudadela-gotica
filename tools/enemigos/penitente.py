"""
Los penitentes de la Tierra sin Fe (parte 7s): los peregrinos que iban al crater a pedir perdon. La noche del cielo rojo la
Sombra les contesto, y desde entonces caminan en procesion por la tierra agrietada, de rodillas en sus campamentos o
postrados en cruz sobre el Puente de la Grieta, con la tunica y el capirote de su cofradia y el antifaz que les tapa la cara
(por los dos agujeros de los ojos asoma una luz roja). Descalzos, la piel gris y sin sangre: la sangre se la dieron a la
grieta. Tres:

  0 flagelante: el capirote y la tunica rojo sangre, la tunica caida hasta la cintura (la espalda en carne viva) y la
    disciplina (el mango y sus cuerdas con nudos de hierro). El azote (deja sangrando: si se acumula, la hemorragia), los
    azotes (tres seguidos) y la penitencia (se azota a si mismo: pierde vida y se enfervoriza).
  1 portador: mas grande (el juego lo dibuja a 1,25), todo de negro, con el escudo rojo en el antifaz y la cruz de madera
    que arrastra (la lleva por el pie, con el travesano detras). El cruzazo (la alza con las dos manos y la estrella), el
    barrido y la embestida (la lleva de frente como un ariete).
  2 turiferario: morado con el cingulo y los ribetes de oro, el incensario de laton colgando de su cadena (sale humo
    rojo). El incensario (lo voltea alrededor), el humo (sacude una nube de incienso de sangre) y el cantico (alza las
    manos y canta: los penitentes de alrededor se curan y aguantan mas).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "penitente"
VARIANTES = ("disciplina", "cruz", "incensario")

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, brazo=(4, 12, 4))
    # la capucha que tapa la cabeza, el capirote encima (un cono de cinco cajas, un poco echado atras), el antifaz que cae
    # por delante hasta el pecho (con los dos agujeros de los ojos) y la esclavina por detras
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[Box(-4.0, -8.0, -4.0, 8, 8, 8, "capucha", grow=g(0.45))])
    r.pieza("capirote", "cabeza", (0, -8.4, 0.3), rot=(-7, 0, 0), boxes=[
        Box(-3.5, -4.2, -3.5, 7.0, 4.4, 7.0, "capirote"),
        Box(-2.6, -8.2, -2.6, 5.2, 4.2, 5.2, "capirote", uv="capirote_2"),
        Box(-1.8, -12.0, -1.8, 3.6, 4.0, 3.6, "capirote", uv="capirote_3"),
        Box(-1.1, -15.4, -1.1, 2.2, 3.6, 2.2, "capirote", uv="capirote_4"),
        Box(-0.5, -18.0, -0.5, 1.0, 2.8, 1.0, "capirote", uv="capirote_5"),
    ])
    r.pieza("antifaz", "cabeza", (0, -8.2, -4.55), rot=(4, 0, 0), boxes=[Box(-4.3, 0, -0.5, 8.6, 13.4, 0.55, "antifaz")])
    r.pieza("esclavina", "cabeza", (0, -6.5, 4.5), rot=(-6, 0, 0), boxes=[Box(-4.4, 0, 0, 8.8, 11.0, 0.55, "esclavina")])
    # la tunica de la cintura abajo hasta los tobillos: el panel de atras (en la cadera) y los de cada pierna (van con ella)
    r.pieza("faldon_tras", "cadera", (0, 0, 0), boxes=[Box(-4.4, -0.5, 1.6, 8.8, 12.0, 0.9, "faldon_tras")])
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[
            Box(-2.8 if s < 0 else 1.9, -0.5, -2.5, 0.9, 11.6, 4.6, f"faldon_lado_{n}"),
            Box(-2.4 if s < 0 else -2.0, -0.5, -2.95, 4.4, 11.6, 0.9, f"faldon_frente_{n}"),
        ])
    # el cingulo (la cuerda de la cintura) con su cabo de nudos colgando por delante
    r.pieza("cingulo", "torso", (0, 0, 0), boxes=[
        Box(-4.6, -1.4, -2.6, 9.2, 1.4, 5.2, "cingulo"),
        Box(1.4, -0.2, -2.9, 0.9, 9.5, 0.9, "cingulo", uv="cabo"),
    ])
    # ---- 0, el flagelante: la tunica caida hasta la cintura (los pliegues) y la disciplina con sus cuatro cuerdas
    r.pieza("tunica_caida", "torso", (0, 0, 0), boxes=[Box(-4.7, -3.4, -2.7, 9.4, 2.4, 5.4, "tunica_caida")])
    r.pieza("disciplina", "mano_d", (0, 0, 0), boxes=[Box(-0.6, -0.6, -4.8, 1.2, 1.2, 6.4, "mango")])
    for k, (rx, ry) in enumerate(((10, 14), (-6, 9), (14, -8), (-10, -14))):
        r.pieza(f"cuerda_{k}", "disciplina", (0, 0, -4.8), rot=(rx, ry, 0), boxes=[
            Box(-0.3, -0.3, -7.6, 0.6, 0.6, 7.6, "cuerda_disc"),
            Box(-0.6, -0.6, -8.6, 1.2, 1.2, 1.2, "nudo"),
        ])
    # ---- 1, el portador: la cruz de madera (el pie hacia delante de la mano; el travesano, al final) y el escudo del pecho
    r.pieza("cruz", "mano_d", (0, 0, 0), boxes=[
        Box(-1.3, -1.3, -27.0, 2.6, 2.6, 33.0, "madero"),
        Box(-8.0, -1.2, -22.4, 16.0, 2.4, 2.6, "travesano"),
    ])
    # ---- 2, el turiferario: la cadena y el incensario de laton (con su tapa, su cupula y la anilla)
    r.pieza("cadena", "mano_d", (0, 0, 0), boxes=[Box(-0.3, -0.3, -8.5, 0.6, 0.6, 9.0, "cadena")])
    r.pieza("incensario", "cadena", (0, 0, -8.5), boxes=[
        Box(-1.9, -1.9, -4.2, 3.8, 3.8, 4.2, "incensario"),
        Box(-1.4, -1.4, -5.4, 2.8, 2.8, 1.2, "incensario", uv="tapa"),
        Box(-0.7, -0.7, -6.4, 1.4, 1.4, 1.0, "incensario", uv="cupula"),
        Box(-2.3, -2.3, -1.0, 4.6, 4.6, 1.0, "incensario", uv="aro"),
    ])
    r.empaquetar()
    return r


DE_FLAGELANTE = ["tunica_caida", "disciplina", "cuerda_0", "cuerda_1", "cuerda_2", "cuerda_3"]
DE_PORTADOR = ["cruz"]
DE_TURIFERARIO = ["cadena", "incensario"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: DE_PORTADOR + DE_TURIFERARIO,
    1: DE_FLAGELANTE + DE_TURIFERARIO,
    2: DE_FLAGELANTE + DE_PORTADOR,
}

# ---------------------------------------------------------------- las texturas
# La piel gris y sin sangre (el flagelante, la espalda en carne viva); la tela de cada cofradia (rojo sangre, negro, morado
# con oro), manchada de sangre seca por abajo; los pies descalzos y sucios; la cuerda de esparto; la madera vieja de la cruz;
# el laton ennegrecido del incensario.

PIEL = ["#47403c", "#5a524c", "#6d655e", "#827970", "#968c82", "#a99e93"]
SANGRE = ["#2a0607", "#45090c", "#620e12", "#801519", "#9e2026"]
ROJO = ["#260708", "#3a0c0d", "#521211", "#6a1916", "#82221c"]
NEGRO = ["#0c0b0d", "#141215", "#1d1a1e", "#272328", "#322d33"]
MORADO = ["#170b1d", "#24112d", "#33183f", "#432052", "#532965"]
ORO = ["#4e3a12", "#6b511b", "#8a6a25", "#ab8731", "#cba542"]
ESPARTO = ["#3e3320", "#54462c", "#6b5a39", "#827047"]
MADERA = ["#1f160f", "#2c2016", "#3b2b1e", "#4c3827", "#5e4631"]
HIERRO = ["#211d1b", "#332d2a", "#48403b", "#5f554e"]
LATON = ["#33240c", "#4c3713", "#684d1c", "#876627", "#a68034"]
CUENCA = "#060404"
OJO = "#ff3b26"


def materiales(variante):
    piel = ruido(PIEL[1:5], 1101, grano=0.45, base=0.55)
    tonos = (ROJO, NEGRO, MORADO)[variante]
    paño = tela(tonos, 1111 + variante, pliegue=3, grano=0.14)

    def manchada(c, cara, x, y, h, semilla, alto=3):
        """La sangre seca del bajo: manchas que suben desde el borde."""
        k = (x * 7 + semilla * 13 + {"frente": 0, "espalda": 3, "der": 1, "izq": 2}.get(cara, 0) * 5) % 5
        if y >= h - 1 - (k % alto):
            return mezcla(c, SANGRE[1 + (x + y) % 3], 0.55)
        return c

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return paño(cara, x, y, w, h)

    def capirote(cara, x, y, w, h):
        if cara == "abajo":
            return oscuro(paño(cara, x, y, w, h), 0.3)
        c = paño(cara, x, y, w, h)
        if variante == 2 and cara != "arriba" and y == h - 1:
            return ORO[2 + (x % 2)]                           # el ribete de oro del turiferario
        return c

    def antifaz(cara, x, y, w, h):
        c = paño(cara, x, y, w, h)
        if cara != "frente":
            return oscuro(c, 0.2) if cara == "espalda" else c
        # los dos agujeros de los ojos (filas 3 y 4 de las 13)
        if 3 <= y <= 4 and x in (1, 2, w - 3, w - 2):
            return CUENCA
        if y == 5 and x in (1, 2, w - 3, w - 2):
            return oscuro(c, 0.45)                           # la sombra bajo el agujero
        cx = x - (w - 1) / 2.0
        if variante == 1 and 7 <= y <= 11:
            # el escudo rojo de la cofradia de los portadores: una cruz sobre un corazon
            if (abs(cx) <= 0.6 and 7 <= y <= 11) or (y == 8 and abs(cx) <= 2.1):
                return SANGRE[4] if (x + y) % 3 else SANGRE[3]
        if variante == 2 and 7 <= y <= 10:
            # el sol de oro del turiferario
            d = abs(cx) + abs(y - 8.5)
            if d <= 1.6:
                return ORO[4]
            if d <= 2.6 and (x + y) % 2 == 0:
                return ORO[2]
        if y == h - 1:
            return oscuro(c, 0.25) if variante != 2 else ORO[1]
        return c

    def esclavina(cara, x, y, w, h):
        c = paño(cara, x, y, w, h)
        if y == h - 1 and x % 3 == 1:
            return None
        return c if cara != "frente" else oscuro(c, 0.15)

    def torso(cara, x, y, w, h):
        if variante == 0:
            # el flagelante, desnudo de cintura arriba: la espalda en carne viva
            c = piel(cara, x, y, w, h)
            if cara == "espalda":
                if (x + 2 * y) % 5 == 0 or (2 * x - y) % 7 == 0:
                    return SANGRE[2 + (x + y) % 3]
                if (x + y) % 4 == 0:
                    return mezcla(c, SANGRE[3], 0.5)
            if cara == "frente" and y in (4, 5) and x in (2, 5):
                return oscuro(c, 0.2)                         # las costillas
            if cara == "frente" and y in (6, 7) and x in (1, 6):
                return oscuro(c, 0.18)
            return c
        c = paño(cara, x, y, w, h)
        if cara == "frente" and variante == 2 and x in (3, 4):
            return ORO[1 + y % 2]                             # la franja de oro del pecho
        return c

    def tunica_caida(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return oscuro(paño(cara, x, y, w, h), 0.2)
        c = paño(cara, x, y, w, h)
        return oscuro(c, 0.12) if y == 0 else c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = paño(cara, x, y, w, h)
        if variante == 2 and y == h - 1:
            return ORO[2]
        return manchada(oscuro(c, 0.08 * y / max(1, h - 1)), cara, x, y, h, 3)

    def brazo(cara, x, y, w, h):
        if variante == 0:
            c = piel(cara, x, y, w, h)
            if cara == "espalda" and (x + y) % 4 == 0:
                return mezcla(c, SANGRE[3], 0.45)
            return c
        return paño(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            c = piel(cara, x, y, w, h)
            return oscuro(c, 0.15) if y == h - 1 else c         # la mano gris
        if variante == 0:
            return piel(cara, x, y, w, h)
        c = paño(cara, x, y, w, h)
        if variante == 2 and y == h - 3:
            return ORO[2]                                     # el puno de la manga
        return c

    def muslo(cara, x, y, w, h):
        return oscuro(paño(cara, x, y, w, h), 0.2)

    def pierna(cara, x, y, w, h):
        # los pies descalzos (lo de abajo) y sucios de tierra y sangre
        if cara == "abajo":
            return "#2a2220"
        if y >= h - 3:
            c = piel(cara, x, y, w, h)
            return mezcla(c, "#3b2a22", 0.45 if y == h - 1 else 0.25)
        return oscuro(paño(cara, x, y, w, h), 0.25)

    def cingulo(cara, x, y, w, h):
        if variante == 2:
            return ORO[1 + (x + y) % 3]
        return ESPARTO[(x + 2 * y) % 4]

    def mango(cara, x, y, w, h):
        return madera(MADERA, 1131, "z")(cara, x, y, w, h)

    def cuerda_disc(cara, x, y, w, h):
        c = ESPARTO[1 + (x + y) % 2]
        return mezcla(c, SANGRE[2], 0.5) if y % 3 == 0 else c

    def nudo(cara, x, y, w, h):
        return metal(HIERRO, 1141)(cara, x, y, w, h)

    def madero(cara, x, y, w, h):
        c = madera(MADERA, 1151, "z", grano=0.35)(cara, x, y, w, h)
        if cara in ("arriba", "der", "izq") and (x * 5 + y * 3) % 23 == 0:
            return SANGRE[2]                                  # la sangre de quien la carga
        return c

    def travesano(cara, x, y, w, h):
        c = madera(MADERA, 1152, "x", grano=0.35)(cara, x, y, w, h)
        if cara == "frente" and x in (w // 2 - 2, w // 2 + 1) and y == h // 2:
            return HIERRO[3]                                  # los clavos
        return c

    def cadena(cara, x, y, w, h):
        return HIERRO[2] if (x + y) % 2 else HIERRO[3]

    def incensario(cara, x, y, w, h):
        c = metal(LATON, 1161)(cara, x, y, w, h)
        if cara in ("frente", "espalda", "der", "izq") and y % 2 == 1 and x % 2 == 0:
            return CUENCA                                     # los agujeros por donde sale el humo
        return c

    return {
        "cabeza": sombra(lambda cara, x, y, w, h: paño(cara, x, y, w, h), abajo=0.16),
        "capucha": sombra(capucha, abajo=0.1), "capirote": sombra(capirote, abajo=0.05), "antifaz": antifaz, "esclavina": esclavina,
        "torso": sombra(torso), "tunica_caida": tunica_caida,
        "faldon_tras": faldon, "faldon_lado_d": faldon, "faldon_lado_i": faldon, "faldon_frente_d": faldon, "faldon_frente_i": faldon,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.16), "antebrazo_i": sombra(antebrazo, abajo=0.16),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
        "cingulo": cingulo, "mango": mango, "cuerda_disc": cuerda_disc, "nudo": nudo,
        "madero": sombra(madero, abajo=0.12), "travesano": sombra(travesano, abajo=0.12),
        "cadena": cadena, "incensario": incensario,
    }


def brillo(variante):
    """Lo que luce: la luz roja por los agujeros del antifaz (y las brasas del incensario)."""

    def antifaz(cara, x, y, w, h):
        if cara == "frente" and 3 <= y <= 4 and x in (1, 2, w - 3, w - 2):
            return OJO if (y == 3) == (x in (2, w - 3)) else "#b01a10"
        return None

    b = {"antifaz": antifaz}
    if variante == 2:
        b["incensario"] = lambda cara, x, y, w, h: "#ff5a1e" if cara in ("frente", "espalda", "der", "izq") and y % 2 == 1 and x % 2 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 12) y
# _arma = hacia donde apunta lo que lleva (las cuerdas de la disciplina, el travesano de la cruz y el incensario estan en -Z
# de la mano).

# de pie: la cabeza gacha, los hombros caidos; la izquierda sobre el pecho
BASE = dict(raiz=raiz(-0.3), torso=(8, 0, 0), cabeza=(16, 0, 0), muslo_d=(-3, 0, 2), pierna_d=(5, 0, 0), muslo_i=(2, 0, -2),
            pierna_i=(4, 0, 0), _mano_i=("h", -2.6, -6.6, 3.6), _arma_i=(0.2, 0.3, 1))
PORTE = {
    "disciplina": dict(_mano_d=("h", -0.6, -9.0, 1.8), _arma=(0.05, -1, 0.15)),        # colgando junto a la pierna
    "cruz": dict(_mano_d=("h", -0.8, -8.6, 2.4), _arma=(0.1, -0.42, -1)),              # la cruz arrastrando por detras
    "incensario": dict(_mano_d=("h", 0.4, -7.6, 3.8), _arma=(0.0, -1, 0.08)),          # el incensario colgando por delante
}
GUARDIA = dict(raiz=raiz(-1.1), torso=(16, 8, 0), cabeza=(-2, -8, 0), muslo_d=(8, 0, 5), pierna_d=(14, 0, 0), muslo_i=(-20, 0, -5),
               pierna_i=(22, 0, 0))
LISTO = {
    # la disciplina por delante, las cuerdas colgando (lista para voltearla); la izquierda por delante
    "disciplina": con(GUARDIA, _mano_d=("h", 1.5, -4.5, 6.0), _arma=(0.1, -1, 0.25), brazo_i=(-34, 0, -14), antebrazo_i=(-36, 0, 0)),
    # la cruz cruzada por delante con las dos manos (la izquierda mas alla en el madero), el travesano arriba a su izquierda
    "cruz": con(GUARDIA, torso=(16, 26, 0), cabeza=(-2, -26, 0), _mano_d=("h", 3.5, -6.5, 2.0), _arma=(0.35, 0.35, 0.87), _agarra=-6),
    # el incensario por delante, balanceandose; la izquierda alzada
    "incensario": con(GUARDIA, _mano_d=("h", 1.2, -4.4, 6.0), _arma=(0.15, -0.9, 0.35), brazo_i=(-60, 0, -20), antebrazo_i=(-30, 0, 0)),
}
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")
# de rodillas (la postura de rezar del juego)
RODILLAS = dict(raiz=raiz(-9.2, 0.8), torso=(14, 0, 0), cabeza=(24, 0, 0), faldon_tras=(-10, 0, 0),
                muslo_d=(-8, 0, 5), pierna_d=(98, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(98, 0, 0))
# postrado en cruz: boca abajo, los brazos abiertos (el juego lo baja un poco)
POSTRADO = dict(raiz=raiz(2.2, 14.0, rot=(90, 0, 0)), torso=(0, 0, 0), cabeza=(-6, 0, 0), capirote=(26, 0, 0),
                muslo_d=(0, 0, 3), pierna_d=(4, 0, 0), muslo_i=(0, 0, -3), pierna_i=(4, 0, 0),
                brazo_d=(-6, 0, 82), antebrazo_d=(-6, 0, 0), brazo_i=(-6, 0, -82), antebrazo_i=(-6, 0, 0))


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

    # ---- quieto: respira despacio, la cabeza gacha, el capirote que cabecea
    bucle("quieto", 4.4, [
        (0.0, BASE),
        (1.6, con(BASE, torso=(10, 2, 1), cabeza=(20, 3, -2), raiz=raiz(-0.5))),
        (3.0, con(BASE, torso=(7, -2, -1), cabeza=(13, -3, 2), raiz=raiz(-0.2))),
        (4.4, BASE),
    ])
    # ---- andar: el paso lento de la procesion, balanceandose de un lado a otro
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-20, 14, 6, 16, 6, 0.0), (-4, 4, 4, 28, 0, 0.5), (14, -20, 16, 6, -6, 0.0), (4, -4, 28, 4, 0, 0.5), (-20, 14, 6, 16, 6, 0.0))):
        pasos.append((1.6 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (8, ty, -ty * 0.5), "cabeza": (14, -ty * 0.6, ty * 0.4), "raiz": raiz(up),
                                    "faldon_tras": (abs(md) * 0.25, 0, 0)}))
    bucle("andar", 1.6, pasos)
    # ---- el brazo de lo que lleva (se suma al quieto) y en guardia
    for arma, p in PORTE.items():
        q = fija(con(BASE, p))
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    for arma, p in LISTO.items():
        l0 = fija(p)
        l1 = fija(con(p, raiz=raiz(-1.7), torso=(p["torso"][0] + 3, p["torso"][1] - 3, 0)))
        bucle(f"listo_{arma}", 1.6, [(0.0, l0), (0.8, l1), (1.6, l0)])
    # ---- de rodillas, cada uno lo suyo: el flagelante se azota la espalda por encima del hombro; el portador reza con las dos
    # manos en la cruz plantada delante; el turiferario mece el incensario
    R0 = con(RODILLAS, _mano_i=("h", -2.6, -6.0, 4.0), _arma_i=(0.2, 0.4, 1))
    F_ALZA = con(R0, torso=(10, 6, 0), cabeza=(18, 6, 0), _mano_d=("h", 0.6, 1.6, 5.6), _arma=(0.1, 0.9, 0.5))
    F_DA = con(R0, torso=(22, -6, 0), cabeza=(30, -4, 0), _mano_d=("h", -1.0, 4.6, -0.2), _arma=(0.15, -0.35, -1))
    bucle("rezando_disciplina", 2.0, [(0.0, F_ALZA), (0.7, F_ALZA), (1.0, F_DA), (1.3, con(F_DA, torso=(24, -6, 0))), (2.0, F_ALZA)])
    PC = con(RODILLAS, torso=(10, 0, 0), cabeza=(20, 0, 0), _mano_d=("h", 2.4, -1.4, 6.6), _arma=(0.02, 1, 0.08), _agarra=-3.5)
    bucle("rezando_cruz", 3.6, [(0.0, PC), (1.8, con(PC, torso=(14, 0, 0), cabeza=(28, 0, 0))), (3.6, PC)])
    T_A = con(R0, _mano_d=("h", 0.6, -6.6, 4.6), _arma=(0.35, -0.9, 0.3))
    T_B = con(R0, _mano_d=("h", 0.0, -6.6, 4.8), _arma=(-0.35, -0.9, 0.3))
    bucle("rezando_incensario", 2.4, [(0.0, T_A), (1.2, T_B), (2.4, T_A)])
    # ---- postrado en cruz
    bucle("postrado", 6.0, [(0.0, POSTRADO), (3.0, con(POSTRADO, cabeza=(-2, 0, 0))), (6.0, POSTRADO)])

    # ================================================================ acciones
    D, C, I = LISTO["disciplina"], LISTO["cruz"], LISTO["incensario"]

    # ---- 1 azote (el flagelante): echa la disciplina atras por encima del hombro y la descarga en diagonal
    AZ_ATRAS = con(D, raiz=raiz(-0.8, 1.0), torso=(4, 34, 0), cabeza=(-8, -28, 0), _mano_d=("h", -4.5, 3.5, -2.0), _arma=(-0.4, 0.8, -0.4))
    AZ_ARRIBA = con(D, raiz=raiz(-0.4, 0.4), torso=(-6, 10, 0), cabeza=(-14, -6, 0), _mano_d=("h", 0.5, 8.5, 2.0), _arma=(0.3, 1, 0.4))
    AZ_DA = con(D, raiz=raiz(-2.6, -2.4), torso=(30, -30, 0), cabeza=(-18, 24, 0), muslo_i=(-36, 0, -4), pierna_i=(34, 0, 0),
                _mano_d=("h", 4.0, -2.5, 7.5), _arma=(0.35, -0.6, 1))
    accion("azote", 1.1, [
        (0.00, D),
        (0.28, AZ_ATRAS),
        (0.44, AZ_ARRIBA),
        (0.56, AZ_DA),
        (0.82, con(AZ_DA, torso=(28, -34, 0))),
        (1.10, D),
    ], golpes=(0.56,))
    # ---- 2 azotes (el flagelante): tres seguidos, de un lado, del otro y de arriba abajo
    # (de vuelta: la disciplina a su izquierda y el reves hacia su derecha)
    AZ2_I = con(D, raiz=raiz(-1.6, -0.6), torso=(18, -30, 0), cabeza=(-10, 26, 0), _mano_d=("h", 5.0, 0.0, 5.0), _arma=(0.9, 0.3, 0.2))
    AZ2_D = con(D, raiz=raiz(-1.8, -1.4), torso=(22, 28, 0), cabeza=(-10, -24, 0), _mano_d=("h", -2.5, -1.0, 6.5), _arma=(-0.8, -0.2, 0.5))
    AZ2_BAJO = con(AZ_DA, torso=(34, -10, 0), cabeza=(-18, 6, 0), _mano_d=("h", 2.0, -4.0, 7.2), _arma=(0.15, -0.8, 0.7))
    accion("azotes", 1.8, [
        (0.00, D),
        (0.22, AZ_ATRAS),
        (0.32, AZ_ARRIBA),
        (0.40, AZ_DA),
        (0.62, AZ2_I),
        (0.80, AZ2_D),
        (0.98, con(AZ2_D, torso=(20, 32, 0))),
        (1.12, AZ_ARRIBA),
        (1.30, AZ2_BAJO),
        (1.52, con(AZ2_BAJO, torso=(30, -12, 0))),
        (1.80, D),
    ], golpes=(0.40, 0.80, 1.30))
    # ---- 3 penitencia (el flagelante): se planta, se azota la espalda tres veces y se yergue enfervorizado
    PE_ALZA = con(GUARDIA, raiz=raiz(-0.6), torso=(4, 4, 0), cabeza=(10, 4, 0), _mano_d=("h", 0.6, 3.0, 6.0), _arma=(0.1, 1, 0.3),
                  brazo_i=(-10, 0, -30), antebrazo_i=(-20, 0, 0))
    PE_DA = con(PE_ALZA, raiz=raiz(-1.2), torso=(24, -4, 0), cabeza=(34, -4, 0), _mano_d=("h", -0.5, 5.0, 0.5), _arma=(0.1, -0.4, -1))
    PE_ARRIBA = con(GUARDIA, raiz=raiz(0.0), torso=(-14, 0, 0), cabeza=(-26, 0, 0), _mano_d=("h", 0.4, 8.0, 1.8), _arma=(0.05, 1, 0.1),
                    brazo_i=(-160, 0, -16), antebrazo_i=(-10, 0, 0))
    accion("penitencia", 2.4, [
        (0.00, D),
        (0.30, PE_ALZA),
        (0.50, PE_DA),
        (0.75, PE_ALZA),
        (0.95, PE_DA),
        (1.20, PE_ALZA),
        (1.40, PE_DA),
        (1.80, PE_ARRIBA),
        (2.10, PE_ARRIBA),
        (2.40, D),
    ], golpes=(0.50, 0.95, 1.40, 1.80))
    # ---- 4 cruzazo (el portador): alza la cruz con las dos manos por encima de la cabeza y la estrella delante
    # (las dos manos juntas en el pie del madero: la izquierda un poco por detras de la derecha)
    CZ_ALTO = con(C, raiz=raiz(-0.2, 1.6), torso=(-16, 6, 0), cabeza=(-22, -4, 0), muslo_d=(10, 0, 4), pierna_d=(8, 0, 0),
                  muslo_i=(-14, 0, -4), pierna_i=(10, 0, 0), _mano_d=("h", 5.0, 5.5, 2.0), _arma=(0.05, 0.75, -0.65), _agarra=3)
    CZ_BAJO = con(C, raiz=raiz(-3.6, -2.6), torso=(46, -6, 0), cabeza=(-30, 4, 0), muslo_d=(16, 0, 4), pierna_d=(30, 0, 0),
                  muslo_i=(-52, 0, -4), pierna_i=(54, 0, 0), _mano_d=("h", 4.5, -3.0, 6.0), _arma=(0.05, -0.5, 1), _agarra=3)
    accion("cruzazo", 1.9, [
        (0.00, C),
        (0.55, CZ_ALTO),
        (0.82, con(CZ_ALTO, raiz=raiz(0.0, 1.8), torso=(-20, 6, 0))),
        (0.96, CZ_BAJO),
        (1.30, con(CZ_BAJO, torso=(44, -6, 0))),
        (1.90, C),
    ], golpes=(0.96,))
    # ---- 5 barrido (el portador): la cruz de lado a lado, a la altura de la cintura
    # (con una mano: la izquierda suelta, abierta para no caerse)
    C1 = con(sin(C, "_agarra"), brazo_i=(-30, 0, -40), antebrazo_i=(-20, 0, 0))
    BA_D = con(C1, raiz=raiz(-1.4, 1.0), torso=(12, 54, 0), cabeza=(-2, -48, 0), _mano_d=("h", 1.6, -5.0, 2.2), _arma=(-1, 0.12, -0.2))
    BA_I = con(C1, raiz=raiz(-2.2, -2.0), torso=(20, -32, 0), cabeza=(-8, 28, 0), muslo_i=(-34, 0, -6), pierna_i=(30, 0, 0),
               _mano_d=("h", 7.0, -6.0, 0.4), _arma=(1, -0.05, 0.3), brazo_i=(-20, 0, -60))
    accion("barrido", 1.7, [
        (0.00, C),
        (0.52, BA_D),
        (0.76, con(BA_D, torso=(12, 60, 0), cabeza=(-2, -52, 0))),
        (0.86, con(C1, raiz=raiz(-2.0, -1.0), torso=(18, 18, 0), cabeza=(-6, -18, 0), _mano_d=("h", 3.0, -6.5, 2.6), _arma=(0.05, -0.02, 1))),
        (0.96, BA_I),
        (1.26, con(BA_I, torso=(18, -36, 0))),
        (1.70, C),
    ], golpes=(0.86,))
    # ---- 6 embestida (el portador): la cruz por delante como un ariete, y corre
    EM = con(C, raiz=raiz(-2.0, -1.2), torso=(30, 4, 0), cabeza=(-18, -4, 0), _mano_d=("h", 5.0, -4.0, 5.5), _arma=(0.04, 0.0, 1), _agarra=4)
    EM_A = con(EM, muslo_d=(-40, 0, 4), pierna_d=(30, 0, 0), muslo_i=(26, 0, -4), pierna_i=(10, 0, 0))
    EM_B = con(EM, muslo_d=(26, 0, 4), pierna_d=(10, 0, 0), muslo_i=(-40, 0, -4), pierna_i=(30, 0, 0))
    accion("embestida", 1.6, [
        (0.00, C),
        (0.30, con(EM, raiz=raiz(-2.6, 1.6), torso=(20, 4, 0))),
        (0.45, EM_A),
        (0.60, EM_B),
        (0.75, EM_A),
        (0.90, EM_B),
        (1.05, con(EM_A, torso=(34, 4, 0))),
        (1.30, con(EM, raiz=raiz(-1.6, 0.4), torso=(14, 6, 0))),
        (1.60, C),
    ], golpes=(0.50, 0.80, 1.05))
    # ---- 7 incensario (el turiferario): lo voltea alrededor de si, de derecha a izquierda, y vuelve
    IN_D = con(I, raiz=raiz(-1.4, 0.4), torso=(8, 40, 0), cabeza=(-4, -34, 0), _mano_d=("h", -2.4, -3.0, 5.0), _arma=(-1, -0.25, -0.1))
    IN_F = con(I, raiz=raiz(-1.8, -0.4), torso=(12, 0, 0), cabeza=(-6, 0, 0), _mano_d=("h", 1.0, -2.0, 7.4), _arma=(0.05, -0.2, 1))
    IN_I = con(I, raiz=raiz(-2.0, -0.8), torso=(14, -40, 0), cabeza=(-8, 34, 0), _mano_d=("h", 4.5, -2.6, 5.5), _arma=(0.9, -0.3, 0.3))
    accion("incensario", 1.5, [
        (0.00, I),
        (0.36, IN_D),
        (0.56, con(IN_D, torso=(8, 46, 0))),
        (0.66, IN_F),
        (0.76, IN_I),
        (1.00, con(IN_I, torso=(14, -44, 0))),
        (1.24, con(I, _mano_d=("h", 1.5, -3.5, 7.0), _arma=(0.3, -0.7, 0.6))),
        (1.50, I),
    ], golpes=(0.66,))
    # ---- 8 humo (el turiferario): alza el incensario y lo sacude tres veces hacia delante: sale la nube
    HU_ALTO = con(I, raiz=raiz(-0.4), torso=(-6, 6, 0), cabeza=(-12, -4, 0), _mano_d=("h", 0.6, 6.2, 4.0), _arma=(0.1, -0.6, 0.8),
                  brazo_i=(-90, 0, -24), antebrazo_i=(-20, 0, 0))
    HU_SACUDE = con(HU_ALTO, torso=(4, 4, 0), _mano_d=("h", 0.6, 4.8, 6.0), _arma=(0.1, -0.2, 1))
    accion("humo", 1.6, [
        (0.00, I),
        (0.40, HU_ALTO),
        (0.55, HU_SACUDE),
        (0.68, HU_ALTO),
        (0.80, HU_SACUDE),
        (0.92, HU_ALTO),
        (1.04, HU_SACUDE),
        (1.30, con(HU_ALTO, torso=(0, 0, 0))),
        (1.60, I),
    ], golpes=(1.04,))
    # ---- 9 cantico (el turiferario): alza las dos manos al cielo y canta (el incensario en alto, en la derecha)
    CA = con(sin(I, "_mano_d", "_arma"), raiz=raiz(-0.3), torso=(-8, 0, 0), cabeza=(-14, 0, 0),
             _mano_d=("h", -1.0, 8.4, 1.6), _arma=(0.0, 1, 0.1), _mano_i=("h", 1.0, 8.4, 1.6), _arma_i=(0, 1, 0))
    accion("cantico", 2.2, [
        (0.00, I),
        (0.50, CA),
        (1.00, con(CA, torso=(-11, 0, 0), cabeza=(-20, 0, 0))),
        (1.50, CA),
        (1.80, con(CA, torso=(-6, 0, 0))),
        (1.98, con(I, _mano_d=("h", 1.0, 1.0, 7.0), _arma=(0.1, 0.3, 1))),
        (2.20, I),
    ], golpes=(1.00,))
    # ---- 10 aturdido: se tambalea hacia atras, los brazos caidos
    AT = dict(raiz=raiz(-1.6, 2.0), torso=(-14, 0, 8), cabeza=(-20, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(26, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(20, 0, 0), brazo_d=(18, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(20, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(6, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-0.8, 0.0))),
        (0.18, AT),
        (0.60, con(AT, torso=(-8, 6, -10), cabeza=(-14, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.00, con(AT, torso=(-12, -6, 8), cabeza=(-18, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(8, 0, 0), cabeza=(8, 0, 0), raiz=raiz(-1.0, 0.6))),
    ])
    # ---- 11 morir: cae de rodillas, se dobla y se derrumba de bruces (el juego lo deshace en sangre)
    CAE = dict(raiz=raiz(-9.6, 0.6), muslo_d=(-10, 0, 6), pierna_d=(102, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0), faldon_tras=(-12, 0, 0),
               torso=(8, 0, 4), cabeza=(-24, 0, 8), brazo_d=(6, 0, 24), antebrazo_d=(-10, 0, 0), mano_d=(70, 0, 0),
               brazo_i=(8, 0, -24), antebrazo_i=(-12, 0, 0))
    BRUCES = dict(raiz=raiz(2.2, 14.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-4, 20, 0), capirote=(20, 0, 0),
                  muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                  brazo_d=(-150, 0, 20), antebrazo_d=(-10, 0, 0), brazo_i=(-30, 0, -40), antebrazo_i=(-70, 0, 0))
    accion("morir", 1.7, [
        (0.00, con(sin(BASE, "_mano_i", "_arma_i"), torso=(-10, 0, 6), cabeza=(-20, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-20, 0, 0),
                   mano_d=(60, 0, 0), brazo_i=(10, 0, -30))),
        (0.45, CAE),
        (0.80, con(CAE, raiz=raiz(-9.8, 0.2), torso=(24, 0, 2), cabeza=(14, 0, 6), brazo_d=(-10, 0, 20), brazo_i=(-8, 0, -20))),
        (1.20, BRUCES),
        (1.70, BRUCES),
    ])
    # ---- 12 levantarse (de rodillas) y 13 alzarse (de postrado en cruz)
    DE_PIE = con(GUARDIA, brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0), brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    MEDIO = dict(raiz=raiz(-5.0, 0.4), muslo_d=(-60, 0, 6), pierna_d=(84, 0, 0), muslo_i=(0, 0, -6), pierna_i=(70, 0, 0),
                 torso=(30, 10, 0), cabeza=(-10, 10, 0), brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0),
                 brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    RZ = dict(raiz=raiz(-9.2, 0.8), torso=(14, 0, 0), cabeza=(24, 0, 0), faldon_tras=(-10, 0, 0),
              muslo_d=(-8, 0, 5), pierna_d=(98, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(98, 0, 0),
              brazo_d=(-40, 0, -14), antebrazo_d=(-60, 0, 0), mano_d=(40, 0, 0), brazo_i=(-40, 0, 14), antebrazo_i=(-60, 0, 0))
    accion("levantarse", 1.3, [
        (0.00, RZ),
        (0.35, con(RZ, torso=(6, 30, 0), cabeza=(-6, 36, 0), brazo_d=(-10, 0, 10), antebrazo_d=(-40, 0, 0), brazo_i=(-10, 0, -10),
                   antebrazo_i=(-30, 0, 0))),
        (0.80, MEDIO),
        (1.30, DE_PIE),
    ])
    accion("alzarse", 1.6, [
        (0.00, sin(POSTRADO, "capirote")),
        (0.40, con(sin(POSTRADO, "capirote"), raiz=raiz(1.0, 10.0, rot=(60, 0, 0)), cabeza=(-30, 0, 0), brazo_d=(-60, 0, 20),
                   antebrazo_d=(-40, 0, 0), brazo_i=(-60, 0, -20), antebrazo_i=(-40, 0, 0))),
        (0.85, dict(raiz=raiz(-6.0, 2.0, rot=(20, 0, 0)), torso=(30, 0, 0), cabeza=(-24, 0, 0),
                    muslo_d=(-80, 0, 6), pierna_d=(100, 0, 0), muslo_i=(-40, 0, -6), pierna_i=(100, 0, 0),
                    brazo_d=(-30, 0, 10), antebrazo_d=(-30, 0, 0), mano_d=(40, 0, 0), brazo_i=(-30, 0, -10), antebrazo_i=(-30, 0, 0))),
        (1.20, con(MEDIO, raiz=raiz(-4.6, 0.0), torso=(26, 0, 0), cabeza=(-8, 0, 0))),
        (1.60, DE_PIE),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if nombre in ("azote", "azotes", "penitencia") or nombre.endswith("disciplina"):
        return 0
    if nombre in ("cruzazo", "barrido", "embestida") or nombre.endswith("cruz"):
        return 1
    if nombre in ("incensario", "humo", "cantico") or nombre.endswith("incensario"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_disciplina"]]
    if nombre == "quieto":
        return [anims["porte_disciplina"]]
    return []


TABLA_EXTRA = ()
