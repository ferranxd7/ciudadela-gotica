"""
El guardia de Valdrenn (parte 4a): los caballeros zombi de la guardia del Rey, hechos de nuevo como enemigos propios.
Muertos dentro de su armadura oxidada: la piel verdosa, los ojos rojos en la rendija, la cota de malla, la coraza y el
tabardo con la calavera negra. Tres, cada uno con el color de su compania:
  0  espada y escudo (tabardo rojo, yelmo cerrado con cresta; se cubre con el escudo)
  1  alabarda        (tabardo morado, capacete de ala ancha; llega lejos)
  2  ballesta        (tabardo verde, cofia y capucha; dispara y recarga)
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "guardia"
VARIANTES = ("espada", "alabarda", "ballesta")
ESCALA = 1.08

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r)
    # la coraza, por encima de la cota; el faldon del tabardo y las escarcelas, por debajo del cinto
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, "coraza", grow=g(0.5))])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2, 8, 6, 4, "faldon", grow=g(0.42))])
    for n in ("d", "i"):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-2.7, -3.1, -2.6, 5.4, 3.4, 5.2, f"hombrera_{n}")])
    # ---- lo de la cabeza
    # 0: el yelmo cerrado, con su rendija y su cresta
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 8, 8, "yelmo", grow=g(0.6)),
        Box(-0.5, -11.0, -3.5, 1, 2.4, 8, "cresta"),
    ])
    # 1 y 2: la cofia de malla (la cara al aire)
    r.pieza("cofia", "cabeza", (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, "cofia", grow=g(0.3))])
    # 1: el capacete: la copa y el ala
    r.pieza("capacete", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8.5, -4, 8, 4, 8, "capacete", grow=g(0.62)),
        Box(-6, -5.0, -6, 12, 1, 12, "ala"),
    ])
    # 2: la capucha, sobre la cofia
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 8, 8, "capucha", grow=g(0.66)),
        Box(-2, -6.5, 4.4, 4, 5, 1.6, "capucha_pico"),
    ])
    # ---- lo que llevan en la mano derecha (apunta hacia delante, -Z, con el brazo caido)
    r.pieza("espada", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2.5, 1, 1, 5, "puno"),
        Box(-1, -1, 2.5, 2, 2, 1.5, "pomo"),
        Box(-3, -0.5, -3.5, 6, 1, 1, "guarda"),
        Box(-0.5, -1, -20.5, 1, 2, 17, "hoja"),
        Box(-0.5, -0.5, -22.5, 1, 1, 2, "hoja_punta"),
    ])
    r.pieza("alabarda", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -24, 1, 1, 38, "asta"),
        Box(-0.5, -6.0, -23, 1, 5.5, 6, "hacha"),
        Box(-0.5, 0.5, -21.5, 1, 2.5, 2.5, "gancho"),
        Box(-0.5, -0.5, -30, 1, 1, 6, "pica"),
    ])
    r.pieza("ballesta", "mano_d", (0, 0, 0), boxes=[
        Box(-1, -1, -9, 2, 2, 13, "curena"),
        Box(-6.5, -0.6, -9.5, 13, 1.2, 1.5, "arco"),
        Box(-1, -0.5, -11, 2, 1, 1.5, "estribo"),
        Box(-0.4, -1.7, -8.5, 0.8, 0.7, 7, "virote"),
    ])
    # el escudo, en el antebrazo izquierdo (por fuera)
    r.pieza("escudo", "antebrazo_i", (0, 0, 0), boxes=[
        Box(2.3, -3.5, -4.5, 1, 12, 9, "escudo"),
        Box(3.3, 1.0, -1.0, 0.6, 2, 2, "umbo"),
    ])
    # la aljaba de los virotes, a la cadera derecha
    r.pieza("aljaba", "cadera", (0, 0, 0), rot=(12, 0, 0), boxes=[
        Box(-5.9, -2.0, 0.0, 1.6, 6.5, 2.4, "aljaba"),
        Box(-5.7, -3.4, 0.3, 1.2, 1.4, 1.8, "plumas"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {
    0: ["cofia", "capacete", "capucha", "alabarda", "ballesta", "aljaba"],
    1: ["yelmo", "capucha", "espada", "escudo", "ballesta", "aljaba"],
    2: ["yelmo", "capacete", "espada", "escudo", "alabarda"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#27331f", "#35462a", "#465c38", "#587347", "#6b8957", "#7f9e69"]
OJO, OJO_B = "#ff3b24", "#ff9a70"
CUENCA, BOCA, DIENTE = "#0c0a09", "#140d0b", "#b3a88f"
HIERRO = ["#1b1d20", "#2c3034", "#42484d", "#5a6268", "#767f86", "#959ea4"]
OXIDO = ["#4d2a15", "#6e3c1c", "#8c5126"]
MALLA = ["#202327", "#30353a", "#454b52", "#5c646c"]
CUERO = ["#17110c", "#241a12", "#33251a", "#443223"]
PALO = ["#261c13", "#36281a", "#483622", "#5a442c"]
HUESO = "#b9b29c"
ORO = ["#4a3c14", "#6b5820", "#8f7a34", "#b09848"]
TABARDO = {
    0: ["#350a0d", "#4c1016", "#66161e", "#801d27", "#9a2632"],
    1: ["#1f0e28", "#2e153c", "#401e52", "#532868", "#66337e"],
    2: ["#0f1f0d", "#172e13", "#213f1b", "#2c5124", "#38632e"],
}
NEGRO = "#0a0a0c"

#: la calavera del tabardo (6 x 6)
CALAVERA = [
    ".KKKK.",
    "KKKKKK",
    "K.KK.K",
    "KKKKKK",
    ".K..K.",
    ".KKKK.",
]


def _oxidado(tonos, semilla, cuanto=37):
    """Hierro con alguna mancha de oxido (una cada `cuanto` pixeles, mas o menos), fundida con el hierro."""
    base = metal(tonos, semilla)

    def f(cara, x, y, w, h):
        c = base(cara, x, y, w, h)
        k = (x * 7 + y * 11 + semilla * 3 + {"frente": 0, "espalda": 5, "der": 9, "izq": 3, "arriba": 2, "abajo": 6}.get(cara, 1)) % cuanto
        if k == 0:
            return mezcla(c, OXIDO[1], 0.6)
        if k in (1, 7):
            return mezcla(c, OXIDO[0], 0.45)
        return c
    return f


def _malla(semilla):
    """Cota de malla: anillas a tresbolillo, con poco contraste (de lejos, un gris con grano)."""
    def f(cara, x, y, w, h):
        c = mezcla(MALLA[1], MALLA[2], 0.75 if (x + y) % 2 == 0 else 0.25)
        if (x * 5 + y * 3 + semilla) % 13 == 0:
            c = mezcla(c, MALLA[3], 0.5)
        return c
    return f


def _cara():
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": CUENCA, "k": "#1a1512", "R": OJO, "r": OJO_B, "M": BOCA,
         "T": DIENTE, "n": PIEL[0]}
    return dibujo([
        "OossssoO",
        "osSSSSso",
        "oSSssSSo",
        "OKKosKKO",
        "oRkonkRo",
        "OosnnsoO",
        "OoMTTMoO",
        "OOoMMoOO",
    ], C)


def materiales(variante):
    tab = TABARDO[variante]
    piel = ruido(PIEL[1:5], 311, grano=0.5, base=0.55)
    placa = _oxidado(HIERRO[1:6], 321)
    placa_b = _oxidado(HIERRO[0:5], 322, cuanto=23)
    malla = _malla(331)
    tela_t = tela(tab, 341 + variante, pliegue=3, grano=0.14)
    cuero = ruido(CUERO, 351, grano=0.4)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara()(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 3 <= y <= 4 and d in (3, 4):
                return PIEL[1]
            if y >= 6 and d <= 2:
                return PIEL[0]
        return c

    def torso(cara, x, y, w, h):
        # debajo de la coraza: la cota de malla y el cinto
        if y == 10:
            return CUERO[1] if not (cara == "frente" and x in (3, 4)) else ORO[2]
        return malla(cara, x, y, w, h)

    def coraza(cara, x, y, w, h):
        # el peto hasta la cintura; por debajo, nada (se ve la malla y el cinto); encima, la tira del tabardo
        if cara in ("arriba", "abajo"):
            return placa(cara, x, y, w, h) if cara == "arriba" else None
        if y >= 9:
            return None
        if cara in ("frente", "espalda"):
            if 1 <= x <= 6:
                c = tela_t(cara, x, y, w, h)
                # la calavera negra en el pecho (solo delante)
                if cara == "frente" and 1 <= y <= 6 and CALAVERA[y - 1][x - 1] == "K":
                    return NEGRO
                if x in (1, 6):
                    return oscuro(c, 0.18)                    # el canto del tabardo
                return c
        c = placa(cara, x, y, w, h)
        if y == 8:
            return oscuro(c, 0.25)                            # el canto de abajo del peto
        return c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # delante y detras, la tira del tabardo, rota por abajo; a los lados, las escarcelas de placa
        if cara in ("frente", "espalda"):
            if 1 <= x <= 6:
                largo = (4, 5, 6, 4, 6, 5)[x - 1] if cara == "frente" else (5, 6, 5, 6, 4, 5)[x - 1]
                if y > largo:
                    return None
                c = tela_t(cara, x, y + 9, w, h + 9)
                return oscuro(c, 0.06 * y + (0.15 if x in (1, 6) else 0))
            if y <= 3:
                return placa_b(cara, x, y, w, h)
            return None
        if y <= 4:
            c = placa_b(cara, x, y, w, h)
            return oscuro(c, 0.2) if y == 4 else c
        return None

    def brazo(cara, x, y, w, h):
        return malla(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        # el brazal de placa y el guantelete
        if y >= h - 2:
            return oscuro(placa_b(cara, x, y, w, h), 0.1) if cara != "abajo" else HIERRO[1]
        if y <= 0:
            return malla(cara, x, y, w, h)
        c = placa(cara, x, y, w, h)
        return oscuro(c, 0.2) if y in (1, h - 3) else c

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara != "arriba" and y >= h - 1:
            return oscuro(c, 0.3)
        if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)):
            return oscuro(c, 0.15)
        return c

    def muslo(cara, x, y, w, h):
        return malla(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        # la rodillera, la greba y la bota
        if y == 0:
            return claro(placa(cara, x, y, w, h), 0.08)
        if y >= h - 1:
            return CUERO[0] if cara != "abajo" else CUERO[1]
        if cara in ("frente", "der", "izq"):
            return placa_b(cara, x, y, w, h)
        return cuero(cara, x, y, w, h)

    def yelmo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara == "frente":
            if y == 4 and 1 <= x <= 6:
                return None                                   # la rendija: se le ven los ojos
            if y in (3, 5) and 1 <= x <= 6:
                return oscuro(c, 0.3)
            if x in (3, 4) and y >= 5:
                return claro(c, 0.1)                          # la arista del medio
            if y >= 6 and x in (1, 6):
                return HIERRO[0]                              # los respiraderos
        if cara in ("der", "izq") and y == 4 and delante(cara, x, w) <= 1:
            return None
        if y == h - 1:
            return oscuro(c, 0.3)
        return c

    def cresta(cara, x, y, w, h):
        # una cresta de crin del color de la compania, raida
        if cara in ("der", "izq"):
            if y == 0 and (x * 3) % 5 == 0:
                return None
            return tab[2 + ((x + y) % 2)]
        if cara == "arriba":
            return tab[3]
        return tab[1]

    def cofia(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente" and 2 <= y <= 6 and 1 <= x <= 6:
            return None                                       # la cara al aire
        if cara == "frente" and y == 7 and 2 <= x <= 5:
            return None
        return malla(cara, x, y, w, h)

    def capacete(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara == "arriba" and x in (3, 4):
            return claro(c, 0.1)
        return c

    def ala(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            cx, cy = x - (w - 1) / 2.0, y - (h - 1) / 2.0
            if cx * cx + cy * cy > 38:
                return None
            c = placa(cara, x, y, w, h)
            return oscuro(c, 0.35) if cara == "abajo" else (oscuro(c, 0.12) if cx * cx + cy * cy > 26 else c)
        return HIERRO[2] if x % 3 else HIERRO[3]

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente" and 1 <= y <= 7 and 1 <= x <= 6:
            return None
        c = tela_t(cara, x, y, w, h)
        if cara == "frente":
            return oscuro(c, 0.15)
        if cara in ("der", "izq") and delante(cara, x, w) == 0:
            return oscuro(c, 0.2)
        return c

    def escudo(cara, x, y, w, h):
        # una tarja: recta por arriba, en punta por abajo; el campo del color de la compania con la calavera
        if cara in ("arriba", "abajo", "frente", "espalda"):
            return HIERRO[2]
        cx = abs(x - (w - 1) / 2.0)
        if y >= 7 and cx > (h - y) * 0.95 - 0.2:
            return None
        if x in (0, w - 1) or y == 0 or (y >= 7 and cx > (h - y) * 0.95 - 1.3):
            return HIERRO[3] if (x + y) % 4 else OXIDO[1]     # el cerco de hierro
        if cara == "der":
            return madera(PALO, 361, "y")(cara, x, y, w, h)   # el reves, de tabla
        c = tela_t(cara, x, y, w, h)
        if 2 <= y <= 7 and 1 <= x <= 7:
            fila = CALAVERA[y - 2]
            k = x - 1 - 1
            if 0 <= k < 6 and fila[k] == "K":
                return NEGRO
        if (x * 5 + y * 7) % 19 == 0:
            return oscuro(c, 0.3)                             # tajos viejos
        return c

    hoja = _oxidado(HIERRO[2:6], 371, cuanto=29)

    def hacha(cara, x, y, w, h):
        if cara not in ("der", "izq"):
            return HIERRO[3]
        # la hoja del hacha: media luna hacia delante (z menor); el filo, claro
        d = delante(cara, x, w)
        borde = 1.2 + 0.25 * (y - (h - 1) / 2.0) ** 2
        if d < borde - 1.2 and y not in (h - 1,):
            return None if d < borde - 2.0 else HIERRO[5]
        c = hoja(cara, x, y, w, h)
        return claro(c, 0.15) if d < borde - 0.2 else c

    return {
        "cabeza": sombra(cabeza, abajo=0.14),
        "torso": sombra(torso), "coraza": sombra(coraza, abajo=0.16), "faldon": faldon,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.14), "antebrazo_i": sombra(antebrazo, abajo=0.14),
        "hombrera_d": sombra(hombrera, abajo=0.12), "hombrera_i": sombra(hombrera, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.18), "pierna_i": sombra(pierna, abajo=0.18),
        "yelmo": sombra(yelmo, abajo=0.12), "cresta": cresta, "cofia": sombra(cofia, abajo=0.12),
        "capacete": sombra(capacete, abajo=0.1), "ala": ala, "capucha": sombra(capucha, abajo=0.14),
        "capucha_pico": sombra(tela_t, abajo=0.2),
        "puno": ruido(CUERO, 381, grano=0.4), "pomo": metal(HIERRO[1:5], 382), "guarda": metal(HIERRO[1:5], 383),
        "hoja": hoja, "hoja_punta": hoja,
        "asta": madera(PALO, 384, "z"), "hacha": hacha, "gancho": metal(HIERRO[2:6], 385), "pica": metal(HIERRO[2:6], 386),
        "curena": madera(PALO, 387, "z"), "arco": metal(HIERRO[1:5], 388), "estribo": metal(HIERRO[1:4], 389),
        "virote": por_cara(liso(PALO[3]), frente=liso(HIERRO[5])),
        "escudo": escudo, "umbo": metal(HIERRO[2:6], 390),
        "aljaba": ruido(CUERO, 391, grano=0.4), "plumas": liso(HUESO),
    }


def brillo(variante):
    """Lo que brilla: los ojos."""
    C = {"R": OJO, "r": OJO_B}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "........",
        ".R....R.",
    ], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 9)
# y _arma = hacia donde apunta lo que lleva.

ABAJO = (0.0, -1.0, 0.0)
# de guardia: firme, los pies algo abiertos; apenas se mueve
BASE = dict(raiz=raiz(-0.2), torso=(2, 0, 0), cabeza=(4, 0, 0),
            muslo_d=(0, 0, 4), pierna_d=(2, 0, 0), muslo_i=(0, 0, -4), pierna_i=(2, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# como lleva cada uno lo suyo cuando no pasa nada (los dos brazos)
PORTE = {
    # la espada baja, la punta hacia el suelo; el escudo colgando al costado
    "espada": dict(_mano_d=("h", -1.2, -8.3, 1.2), _arma=(-0.05, -0.8, 0.6), brazo_i=(-6, 0, -8), antebrazo_i=(-22, 0, 0)),
    # la alabarda derecha, con el regaton en el suelo, como un centinela
    "alabarda": dict(_mano_d=("h", -0.8, -4.6, 3.2), _arma=(0.0, 1.0, 0.03), brazo_i=(4, 0, -5), antebrazo_i=(-10, 0, 0)),
    # la ballesta cruzada delante del pecho, apuntando al suelo
    "ballesta": dict(_mano_d=("h", 2.0, -6.0, 4.6), _arma=(0.62, -0.45, 0.64), _agarra=-6),
}
# en guardia: un pie delante, las rodillas flojas
GUARDIA = dict(raiz=raiz(-1.0), torso=(8, 0, 0), cabeza=(-4, 0, 0),
               muslo_d=(10, 0, 5), pierna_d=(14, 0, 0), muslo_i=(-20, 0, -5), pierna_i=(22, 0, 0))
LISTO = {
    # de perfil tras el escudo (el hombro izquierdo delante), la espada atras, a la altura de la cadera
    "espada": con(GUARDIA, torso=(8, 24, 0), cabeza=(-4, -22, 0),
                  _mano_d=("h", -1.0, -5.6, 2.6), _arma=(0.1, 0.35, 1),
                  _mano_i=("h", -3.6, -2.6, 6.2), _arma_i=(-0.2, 1, 0.2)),
    # la alabarda con las dos manos (la derecha atras, junto a la cadera), la punta hacia la cara del otro
    "alabarda": con(GUARDIA, torso=(10, 34, 0), cabeza=(-6, -30, 0),
                    _mano_d=("h", 2.4, -6.4, 1.6), _arma=(0.0, 0.2, 1), _agarra=-7),
    # la ballesta a media altura, lista para echarsela a la cara
    "ballesta": con(GUARDIA, torso=(8, 14, 0), cabeza=(-4, -12, 0),
                    _mano_d=("h", 2.6, -4.4, 4.6), _arma=(0.1, -0.12, 1), _agarra=-6),
}


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

    # ---- quieto: respira dentro del peto; de vez en cuando gira la cabeza
    bucle("quieto", 6.0, [
        (0.0, BASE),
        (1.5, con(BASE, raiz=raiz(-0.4), torso=(3, 2, 0), cabeza=(5, -10, 0))),
        (3.0, con(BASE, raiz=raiz(-0.1), torso=(1, 0, 0), cabeza=(2, 0, 0))),
        (4.5, con(BASE, raiz=raiz(-0.4), torso=(3, -2, 0), cabeza=(5, 12, 0))),
        (6.0, BASE),
    ])
    # ---- andar: pasos pesados, de armadura
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-26, 20, 6, 16, 4, 0.0), (-2, 2, 4, 36, 0, 0.7), (20, -26, 16, 6, -4, 0.0), (2, -2, 36, 4, 0, 0.7), (-26, 20, 6, 16, 4, 0.0))):
        pasos.append((1.25 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                     "torso": (3, ty, -ty * 0.3), "cabeza": (0, -ty * 0.7, ty * 0.3), "raiz": raiz(up)}))
    bucle("andar", 1.25, pasos)
    # ---- como lleva cada uno lo suyo (se suma a lo demas; solo mueve los brazos)
    for arma, p in PORTE.items():
        q = con(BASE, p)
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    # ---- en guardia, con cada arma
    for arma, p in LISTO.items():
        bucle(f"listo_{arma}", 2.0, [
            (0.0, p),
            (1.0, con(p, raiz=raiz(-1.5), torso=(p["torso"][0] + 2, p["torso"][1] - 2, 0))),
            (2.0, p),
        ])
    # ---- velando: una rodilla en tierra, la cabeza baja (ante una tumba, un altar, el trono vacio)
    VELA = dict(raiz=raiz(-6.4, 0.6), torso=(16, 0, 0), cabeza=(26, 0, 0),
                muslo_d=(-4, 0, 4), pierna_d=(98, 0, 0), muslo_i=(-86, 0, -6), pierna_i=(84, 0, 0),
                _mano_d=("h", -0.5, -6.6, 4.4), _arma=(0.0, -0.9, 0.42), brazo_i=(-40, 0, -6), antebrazo_i=(-36, 0, 0))
    bucle("velar", 4.0, [
        (0.0, VELA),
        (2.0, con(VELA, raiz=raiz(-6.6, 0.4), torso=(19, 0, 0), cabeza=(30, 0, 0))),
        (4.0, VELA),
    ])
    # ---- tirado boca abajo entre los muertos (parece uno mas)
    TUMBADO = dict(raiz=raiz(2.2, 14.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 34, 0),
                   muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                   _mano_d=("h", -3.0, -0.6, 7.5), _arma=(-0.25, 0.0, 1), brazo_i=(-22, 0, -30), antebrazo_i=(-70, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (2.0, con(TUMBADO, cabeza=(-8, 38, 0))), (4.0, TUMBADO)])

    # ================================================================ acciones
    E, H, X = LISTO["espada"], LISTO["alabarda"], LISTO["ballesta"]
    ESC = dict(_mano_i=E["_mano_i"], _arma_i=E["_arma_i"])

    # ---- la espada: un tajo de arriba abajo, cruzado (la levanta sobre el hombro; se ve venir)
    ARRIBA = con(E, raiz=raiz(-0.6, 1.2), torso=(-6, 40, 0), cabeza=(0, -34, 0),
                 _mano_d=("h", -2.0, 6.6, -1.0), _arma=(-0.2, 0.8, -0.55), _mano_i=("h", -3.0, -2.0, 6.8))
    CORTE = con(E, raiz=raiz(-1.6, -1.6), torso=(14, -8, 0), cabeza=(-8, 6, 0),
                _mano_d=("h", 1.0, -1.0, 8.2), _arma=(0.25, 0.1, 1), _mano_i=("h", -1.0, -3.0, 5.0))
    ABAJO_E = con(E, raiz=raiz(-2.4, -2.8), torso=(26, -34, 0), cabeza=(-16, 28, 0),
                  muslo_i=(-40, 0, -5), pierna_i=(36, 0, 0),
                  _mano_d=("h", 5.0, -6.0, 4.5), _arma=(0.6, -0.6, 0.5), _mano_i=("h", 2.0, -4.0, 2.0))
    accion("espada_tajo", 1.5, [
        (0.00, E),
        (0.50, ARRIBA),
        (0.72, con(ARRIBA, raiz=raiz(-0.4, 1.5), torso=(-9, 44, 0), _mano_d=("h", -2.0, 6.9, -1.6))),
        (0.80, CORTE),
        (0.90, ABAJO_E),
        (1.15, con(ABAJO_E, torso=(24, -36, 0))),
        (1.50, E),
    ], golpes=(0.80,))
    # ---- la tanda: de fuera adentro, de vuelta, y una estocada a fondo
    FUERA = con(E, raiz=raiz(-1.0, 0.6), torso=(6, 42, 0), cabeza=(-4, -36, 0),
                _mano_d=("h", -6.2, -0.5, 1.5), _arma=(-1, 0.2, 0.1), _mano_i=("h", -3.0, -2.4, 6.6))
    PASA = con(E, raiz=raiz(-1.4, -1.0), torso=(10, 4, 0), cabeza=(-6, -2, 0),
               _mano_d=("h", 0.5, -0.8, 8.4), _arma=(0.0, 0.05, 1), _mano_i=("h", -0.5, -3.2, 4.6))
    DENTRO = con(E, raiz=raiz(-1.8, -1.6), torso=(12, -36, 0), cabeza=(-8, 30, 0),
                 muslo_i=(-34, 0, -5), pierna_i=(30, 0, 0),
                 _mano_d=("h", 6.0, -1.5, 4.6), _arma=(1, 0.05, 0.3), _mano_i=("h", 3.0, -3.5, 1.0))
    ATRAS_E = con(E, raiz=raiz(-1.6, -1.2), torso=(6, 34, 0), cabeza=(-4, -30, 0),
                  muslo_i=(-30, 0, -5), pierna_i=(28, 0, 0),
                  _mano_d=("h", -1.0, -4.8, -4.0), _arma=(0.05, 0.05, 1), _mano_i=("h", -3.0, -2.4, 6.8))
    FONDO_E = con(E, raiz=raiz(-2.6, -5.0), torso=(22, -38, 0), cabeza=(-16, 32, 0),
                  muslo_i=(-52, 0, -5), pierna_i=(46, 0, 0), muslo_d=(26, 0, 5), pierna_d=(8, 0, 0),
                  _mano_d=("h", 2.6, -1.6, 8.6), _arma=(0.0, 0.0, 1), _mano_i=("h", 3.5, -4.0, -1.0))
    accion("espada_combo", 2.5, [
        (0.00, E),
        (0.45, FUERA),
        (0.62, con(FUERA, torso=(6, 46, 0), cabeza=(-4, -40, 0))),
        (0.70, PASA),
        (0.80, DENTRO),
        (1.05, con(DENTRO, torso=(12, -40, 0), cabeza=(-8, 34, 0))),
        (1.25, con(PASA, raiz=raiz(-1.8, -2.6), muslo_i=(-38, 0, -5), pierna_i=(34, 0, 0))),
        (1.35, con(FUERA, raiz=raiz(-1.8, -3.0), muslo_i=(-38, 0, -5), pierna_i=(34, 0, 0))),
        (1.58, con(ATRAS_E, raiz=raiz(-1.6, -2.6))),
        (1.82, con(ATRAS_E, raiz=raiz(-1.6, -2.2), torso=(4, 38, 0), _mano_d=("h", -1.0, -4.8, -4.8))),
        (1.90, FONDO_E),
        (2.15, con(FONDO_E, torso=(20, -36, 0))),
        (2.50, E),
    ], golpes=(0.70, 1.25, 1.90))
    # ---- el escudazo: recoge el escudo y lo estampa con un paso (empuja y deja sin guardia)
    RECOGE = con(E, raiz=raiz(-1.4, 1.4), torso=(4, -8, 0), cabeza=(0, 6, 0),
                 _mano_i=("h", 1.0, -3.0, 1.5), _mano_d=("h", -1.5, -6.0, 1.0), _arma=(0.1, -0.2, 1))
    EMPUJA = con(E, raiz=raiz(-1.8, -3.6), torso=(16, 46, 0), cabeza=(-8, -40, 0),
                 muslo_i=(-46, 0, -5), pierna_i=(40, 0, 0),
                 _mano_i=("h", -2.0, -1.0, 8.6), _mano_d=("h", -2.0, -6.0, -2.0), _arma=(0.0, -0.3, 1))
    accion("espada_escudazo", 1.3, [
        (0.00, E),
        (0.40, RECOGE),
        (0.54, con(RECOGE, raiz=raiz(-1.5, 1.7))),
        (0.60, EMPUJA),
        (0.85, con(EMPUJA, raiz=raiz(-1.8, -3.4))),
        (1.30, E),
    ], golpes=(0.60,))
    # ---- le han dado en el escudo: lo aguanta
    accion("espada_bloquea", 0.5, [
        (0.00, E),
        (0.08, con(E, raiz=raiz(-1.6, 1.2), torso=(2, 30, 0), _mano_i=("h", -3.2, -1.6, 7.4))),
        (0.25, con(E, raiz=raiz(-1.2, 0.6), torso=(6, 26, 0))),
        (0.50, E),
    ])

    # ---- la alabarda: la pica (se echa atras y a fondo; llega lejos, se esquiva de lado)
    ATRAS_H = con(H, raiz=raiz(-1.4, 2.0), torso=(6, 52, 0), cabeza=(-4, -44, 0),
                  muslo_d=(18, 0, 6), pierna_d=(20, 0, 0), muslo_i=(-28, 0, -4), pierna_i=(14, 0, 0),
                  _mano_d=("h", 1.5, -5.6, -2.6), _arma=(0.0, 0.08, 1), _agarra=-7)
    FONDO_H = con(sin(H, "_agarra"), raiz=raiz(-2.6, -5.0), torso=(24, -20, 0), cabeza=(-18, 18, 0),
                  muslo_i=(-52, 0, -5), pierna_i=(46, 0, 0), muslo_d=(28, 0, 5), pierna_d=(8, 0, 0),
                  _mano_d=("h", 3.4, -2.6, 7.8), _arma=(0.0, 0.02, 1), brazo_i=(-64, 0, -10), antebrazo_i=(-16, 0, 0))
    accion("alabarda_pica", 1.7, [
        (0.00, H),
        (0.55, ATRAS_H),
        (0.86, con(ATRAS_H, raiz=raiz(-1.4, 2.4), torso=(4, 56, 0))),
        (0.95, FONDO_H),
        (1.25, con(FONDO_H, torso=(22, -18, 0))),
        (1.70, H),
    ], golpes=(0.95,))
    # ---- el hachazo: la alza con las dos manos y la descarga (pesado: parte la guardia)
    ALTO_H = con(H, raiz=raiz(-0.2, 1.4), torso=(-14, 14, 0), cabeza=(-10, -12, 0),
                 muslo_d=(6, 0, 5), pierna_d=(8, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(10, 0, 0),
                 _mano_d=("h", 2.4, 6.6, 0.6), _arma=(0.0, 0.85, -0.5), _agarra=-6)
    BAJO_H = con(H, raiz=raiz(-2.8, -3.0), torso=(36, 10, 0), cabeza=(-26, -8, 0),
                 muslo_i=(-46, 0, -5), pierna_i=(44, 0, 0),
                 _mano_d=("h", 2.6, -6.0, 4.6), _arma=(0.0, -0.55, 1), _agarra=-6)
    accion("alabarda_hachazo", 2.0, [
        (0.00, H),
        (0.65, ALTO_H),
        (1.00, con(ALTO_H, raiz=raiz(0.0, 1.7), torso=(-18, 14, 0))),
        (1.10, BAJO_H),
        (1.50, con(BAJO_H, torso=(34, 10, 0))),
        (2.00, H),
    ], golpes=(1.10,))
    # ---- el barrido: de lado a lado, a media altura (no vale echarse atras: hay que agacharse o salir)
    FUERA_H = con(H, raiz=raiz(-1.0, 0.8), torso=(8, 60, 0), cabeza=(-4, -52, 0),
                  _mano_d=("h", 0.0, -4.6, -2.0), _arma=(-1, 0.1, 0.3), _agarra=-6)
    MEDIO_H = con(H, raiz=raiz(-1.6, -1.4), torso=(12, 12, 0), cabeza=(-6, -10, 0),
                  _mano_d=("h", 2.6, -4.6, 3.0), _arma=(0.0, 0.05, 1), _agarra=-6)
    DENTRO_H = con(H, raiz=raiz(-2.0, -2.4), torso=(14, -40, 0), cabeza=(-8, 34, 0),
                   muslo_i=(-38, 0, -5), pierna_i=(34, 0, 0),
                   _mano_d=("h", 5.4, -4.6, 3.6), _arma=(1, 0.0, 0.4), _agarra=-5)
    accion("alabarda_barrido", 1.9, [
        (0.00, H),
        (0.55, FUERA_H),
        (0.86, con(FUERA_H, torso=(8, 64, 0), cabeza=(-4, -56, 0))),
        (0.95, MEDIO_H),
        (1.05, DENTRO_H),
        (1.40, con(DENTRO_H, torso=(14, -44, 0), cabeza=(-8, 38, 0))),
        (1.90, H),
    ], golpes=(0.95,))

    # ---- la ballesta: se la echa a la cara, apunta un instante, dispara; luego la baja, la tensa y pone otro virote
    APUNTA = con(X, raiz=raiz(-0.8), torso=(4, 24, 0), cabeza=(2, -22, 0),
                 _mano_d=("h", 3.6, -0.8, 4.2), _arma=(0.0, 0.0, 1), _agarra=-6)
    BAJA_X = con(X, raiz=raiz(-2.4), torso=(30, 6, 0), cabeza=(24, 0, 0),
                 _mano_d=("h", 2.4, -7.4, 3.2), _arma=(0.1, -1, 0.3), _agarra=-5)
    accion("ballesta_disparo", 2.6, [
        (0.00, X),
        (0.45, APUNTA),
        (0.84, con(APUNTA, raiz=raiz(-0.9))),
        (0.90, con(APUNTA, raiz=raiz(-0.8, 0.9), torso=(0, 26, 0), _mano_d=("h", 3.6, -0.4, 3.4), _arma=(0.0, 0.12, 1))),
        (1.08, APUNTA),
        (1.45, BAJA_X),
        (1.80, con(BAJA_X, raiz=raiz(-1.4), torso=(14, 6, 0), cabeza=(14, 0, 0), _mano_d=("h", 2.4, -5.6, 2.8))),
        (2.10, con(BAJA_X, raiz=raiz(-2.0), torso=(24, 6, 0), cabeza=(20, 0, 0))),
        (2.60, X),
    ], golpes=(0.90,))
    # ---- de cerca: un culatazo de lado
    ARMA_X = con(sin(X, "_agarra"), raiz=raiz(-1.0, 0.8), torso=(4, 44, 0), cabeza=(-2, -38, 0),
                 _mano_d=("h", -3.0, -1.0, 1.0), _arma=(-0.9, 0.35, -0.2), brazo_i=(-30, 0, -14), antebrazo_i=(-50, 0, 0))
    PEGA_X = con(sin(X, "_agarra"), raiz=raiz(-1.8, -2.2), torso=(12, -32, 0), cabeza=(-8, 28, 0),
                 muslo_i=(-36, 0, -5), pierna_i=(32, 0, 0),
                 _mano_d=("h", 4.5, -1.5, 6.5), _arma=(-0.7, 0.2, -0.65), brazo_i=(10, 0, -24), antebrazo_i=(-40, 0, 0))
    accion("ballesta_culatazo", 1.3, [
        (0.00, X),
        (0.38, ARMA_X),
        (0.54, con(ARMA_X, torso=(4, 48, 0))),
        (0.60, PEGA_X),
        (0.85, con(PEGA_X, torso=(12, -34, 0))),
        (1.30, X),
    ], golpes=(0.60,))

    # ---- aturdido: le han parado el golpe o le han roto la guardia; se tambalea con los brazos abiertos
    AT = dict(raiz=raiz(-1.4, 2.4), torso=(-18, 10, 8), cabeza=(-16, -6, 12),
              muslo_d=(-12, 0, 8), pierna_d=(26, 0, 0), muslo_i=(16, 0, -8), pierna_i=(18, 0, 0),
              _mano_d=("h", -5.0, -6.6, -1.0), _arma=(-0.7, -0.6, 0.4), brazo_i=(10, 0, -34), antebrazo_i=(-30, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, raiz=raiz(-0.8, 0.6), torso=(4, 0, 0), cabeza=(0, 0, 4))),
        (0.22, AT),
        (0.70, con(AT, raiz=raiz(-1.8, 3.0), torso=(-14, -8, -8), cabeza=(-10, 8, -10))),
        (1.10, con(AT, raiz=raiz(-1.6, 2.6), torso=(-16, 6, 6), cabeza=(-14, -6, 10))),
        (1.50, con(GUARDIA, _mano_d=("h", -1.0, -5.6, 2.6), _arma=(0.1, 0.35, 1), brazo_i=(-20, 0, -10), antebrazo_i=(-40, 0, 0))),
    ])
    # ---- morir: cae de rodillas y se vence de bruces, con todo su hierro
    RODILLAS = dict(raiz=raiz(-9.6, 0.0), torso=(10, 0, 0), cabeza=(26, 0, 0),
                    muslo_d=(-12, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -6), pierna_i=(92, 0, 0),
                    _mano_d=("h", -3.6, -7.0, 4.0), _arma=(-0.3, -0.75, 0.6), brazo_i=(-10, 0, -20), antebrazo_i=(-20, 0, 0))
    CAIDO = dict(raiz=raiz(2.4, 14.6, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-12, 26, 0),
                 muslo_d=(4, 0, 10), pierna_d=(8, 0, 0), muslo_i=(-4, 0, -14), pierna_i=(24, 0, 0),
                 _mano_d=("h", -4.5, -1.2, 7.0), _arma=(-0.45, 0.0, 1), brazo_i=(-30, 0, -34), antebrazo_i=(-60, 0, 0))
    accion("morir", 2.2, [
        (0.00, con(GUARDIA, raiz=raiz(-1.6, 1.4), torso=(-10, 0, 6), cabeza=(-14, 0, 8),
                   _mano_d=("h", -3.0, -6.0, 1.0), _arma=(-0.3, -0.5, 0.8), brazo_i=(0, 0, -24), antebrazo_i=(-30, 0, 0))),
        (0.55, RODILLAS),
        (1.05, con(RODILLAS, torso=(16, 0, 0), cabeza=(32, 0, 0))),
        (1.65, CAIDO),
        (2.20, CAIDO),
    ])
    # ---- deja de velar: se pone en pie apoyandose en el arma
    DE_PIE = con(GUARDIA, _mano_d=("h", -1.0, -5.6, 2.6), _arma=(0.1, 0.35, 1), brazo_i=(-20, 0, -10), antebrazo_i=(-40, 0, 0))
    accion("levantarse", 1.3, [
        (0.00, VELA),
        (0.45, con(VELA, raiz=raiz(-5.4, 0.2), torso=(26, 0, 0), cabeza=(-10, 0, 0))),
        (0.90, con(DE_PIE, raiz=raiz(-2.6, 0.0), torso=(22, 0, 0), cabeza=(-12, 0, 0), muslo_i=(-44, 0, -5), pierna_i=(50, 0, 0))),
        (1.30, DE_PIE),
    ])
    # ---- el "muerto" se levanta
    accion("alzarse", 1.9, [
        (0.00, TUMBADO),
        (0.40, con(TUMBADO, cabeza=(-30, 10, 0), brazo_i=(-70, 0, -30), antebrazo_i=(-90, 0, 0), _mano_d=("h", -4.0, -0.8, 4.0), _arma=(-0.4, 0.0, 1))),
        (0.90, dict(raiz=raiz(-5.5, 6.0, rot=(50, 0, 0)), torso=(-6, 0, 0), cabeza=(-40, 0, 0),
                    muslo_d=(-40, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(96, 0, 0),
                    _mano_d=("h", -3.0, -4.0, 6.0), _arma=(-0.3, -0.4, 0.85), brazo_i=(-92, 0, -14), antebrazo_i=(-20, 0, 0))),
        (1.35, con(DE_PIE, raiz=raiz(-5.6, 0.0), torso=(34, 0, 0), cabeza=(-10, 0, 0), muslo_d=(-30, 0, 5), pierna_d=(70, 0, 0),
                   muslo_i=(-50, 0, -5), pierna_i=(70, 0, 0))),
        (1.90, DE_PIE),
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
    """Las animaciones que el juego pone debajo de esta (para verla como se vera)."""
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_espada"]]
    if nombre == "quieto":
        return [anims["porte_espada"]]
    return []


#: de que variante es cada accion (para el servidor: `GuardiaAnims`)
TABLA_EXTRA = ()
