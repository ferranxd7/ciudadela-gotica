"""
Los salobres de la Costa de los Huesos (parte 7q): la gente del Puerto Varado. La noche del cielo rojo el mar se retiro de
golpe ("como si alguien se lo bebiera") y dejo en seco el puerto, los barcos y las ballenas; bajaron a la arena a recoger lo
que el mar dejaba y el mar no volvio. La sal se los fue comiendo: la piel blanca y agrietada, costras y cristales de sal en
los hombros y la cabeza, los ojos hundidos con una luz fria de agua. Siguen faenando: remiendan redes, rastrillan sal y
vigilan un mar que no esta. Con el chubasquero de hule y botas altas. Tres:

  0 redero: gorro de lana, la red al hombro y un plomo de red colgando de su cuerda en la derecha. La plomada (lo voltea y lo
    descarga) y la red (la lanza: a quien pilla lo deja enredado, casi sin poder moverse).
  1 bichero: el sueste de hule y el bichero (la pertiga larga con su gancho de hierro). El garfio (lo clava y tira: te arrastra
    hacia el) y el bichazo (un barrido ancho con la pertiga).
  2 salinero: mas grande (el juego lo dibuja a 1,15), los brazos al aire con costras de sal, sombrero de paja, delantal de lona,
    el saco de sal a la espalda y el rastro de las salinas. El rastrillazo (lo alza con las dos manos y lo estrella: deja una
    costra de sal que frena) y la sal (un punado a la cara: escuece y deja sin aliento).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "salobre"
VARIANTES = ("plomo", "bichero", "rastro")

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, brazo=(4, 12, 4))
    # el chubasquero: los faldones por debajo de la cintura, abiertos por delante
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4.3, -0.5, -2.3, 8.6, 6, 4.6, "faldon", grow=g(0.3))])
    # las botas altas de goma (sobre las piernas)
    for n in ("d", "i"):
        r.pieza(f"bota_{n}", f"pierna_{n}", (0, 1.4, 0), boxes=[Box(-2.3, 0, -2.3, 4.6, 4.9, 4.6, f"bota_{n}")])
    # los cristales de sal que les crecen en los hombros y en la cabeza
    r.pieza("sal_hombros", "torso", (0, -12, 0), boxes=[
        Box(-6.2, -2.0, -1.4, 2.4, 2.6, 2.4, "cristal"),
        Box(-4.6, -2.8, 0.4, 1.4, 2.0, 1.4, "cristal"),
        Box(3.8, -1.6, -1.0, 2.2, 2.2, 2.0, "cristal"),
        Box(5.0, -2.4, 0.8, 1.2, 1.6, 1.2, "cristal"),
    ])
    r.pieza("sal_cabeza", "cabeza", (0, -8, 0), boxes=[
        Box(1.6, -1.6, -1.8, 1.6, 1.8, 1.6, "cristal"),
        Box(-3.0, -1.0, 1.2, 1.2, 1.2, 1.2, "cristal"),
    ])
    # ---- 0, el redero: el gorro de lana, la red al hombro y el plomo colgando de su cuerda
    r.pieza("gorro", "cabeza", (0, -8, 0), boxes=[
        Box(-4.0, -1.6, -4.0, 8, 2.8, 8, "gorro", grow=g(0.35)),
        Box(-3.4, -2.6, -3.4, 6.8, 1.0, 6.8, "gorro_alto"),
    ])
    r.pieza("red_hombro", "torso", (0, -11.5, 2.0), rot=(0, 0, 12), boxes=[
        Box(-5.2, 0, 0, 10.4, 8.5, 2.6, "red"),
        Box(-5.4, 7.6, -0.2, 10.8, 1.2, 3.0, "corchos"),
    ])
    r.pieza("cuerda", "mano_d", (0, 0, 0), boxes=[Box(-0.3, -0.3, -6.0, 0.6, 0.6, 6.4, "cuerda")])
    r.pieza("plomo", "cuerda", (0, 0, -6.0), boxes=[Box(-1.3, -1.3, -2.6, 2.6, 2.6, 2.8, "plomo")])
    # ---- 1, el bichero: el sueste (el sombrero de agua, largo por detras) y el bichero
    r.pieza("sueste", "cabeza", (0, -7.6, 0), rot=(-6, 0, 0), boxes=[
        Box(-5.2, -0.8, -5.0, 10.4, 0.9, 11.2, "sueste_ala"),
        Box(-4.2, -3.4, -4.2, 8.4, 2.8, 8.4, "sueste_copa"),
    ])
    r.pieza("bichero", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -22, 1, 1, 27, "pertiga"),
        Box(-0.65, -0.65, -25.5, 1.3, 1.3, 3.6, "garfio"),
        Box(-0.5, -3.6, -24.4, 1, 3.4, 1, "garfio", uv="gancho"),
        Box(-0.5, -4.0, -23.2, 1, 1, 1.6, "garfio", uv="punta"),
    ])
    # ---- 2, el salinero: el sombrero de paja, el delantal, el saco a la espalda y el rastro de la sal
    r.pieza("sombrero", "cabeza", (0, -7.6, 0), rot=(-4, 0, 3), boxes=[
        Box(-7.5, -1, -7.5, 15, 1, 15, "ala"),
        Box(-4.2, -3.8, -4.2, 8.4, 3.0, 8.4, "copa", grow=g(0.2)),
    ])
    r.pieza("delantal", "torso", (0, -9.0, -2.3), boxes=[Box(-3.9, 0, -0.5, 7.8, 15.5, 0.5, "delantal")])
    r.pieza("saco", "torso", (0, -11.5, 2.1), boxes=[Box(-3.6, 0, 0, 7.2, 8.5, 3.8, "saco", grow=g(0.2))])
    r.pieza("rastro", "mano_d", (0, 0, 0), boxes=[
        Box(-0.55, -0.55, -24, 1.1, 1.1, 29, "pertiga_rastro"),
        Box(-5.5, -1.0, -26.4, 11, 2.0, 2.4, "tabla"),
        Box(-5.0, 1.0, -26.0, 1, 2.4, 1.6, "tabla", uv="diente_1"),
        Box(-2.5, 1.0, -26.0, 1, 2.4, 1.6, "tabla", uv="diente_2"),
        Box(0.0, 1.0, -26.0, 1, 2.4, 1.6, "tabla", uv="diente_3"),
        Box(2.5, 1.0, -26.0, 1, 2.4, 1.6, "tabla", uv="diente_4"),
        Box(4.5, 1.0, -26.0, 1, 2.4, 1.6, "tabla", uv="diente_5"),
    ])
    r.empaquetar()
    return r


DE_REDERO = ["gorro", "red_hombro", "cuerda", "plomo"]
DE_BICHERO = ["sueste", "bichero"]
DE_SALINERO = ["sombrero", "delantal", "saco", "rastro"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: DE_BICHERO + DE_SALINERO,
    1: DE_REDERO + DE_SALINERO + ["sal_cabeza"],
    2: DE_REDERO + DE_BICHERO + ["sal_cabeza"],
}

# ---------------------------------------------------------------- las texturas
# La piel comida por la sal: gris blanquecina con grietas; costras blancas; el hule del chubasquero, de un mostaza
# desvaido con manchas de sal; las botas, de goma negra.

PIEL = ["#4e4b46", "#67635c", "#827d74", "#9d978c", "#b7b1a5", "#d0cbbf", "#e6e2d8"]
SAL = ["#c9cfcd", "#dce2df", "#eef2ef", "#ffffff"]
GRIETA = "#3a3632"
CUENCA = "#0d1214"
HULE = ["#3e3418", "#54461f", "#6b5a28", "#837034", "#9b8642"]
HULE_SUESTE = ["#4a3c12", "#655218", "#816a20", "#9c832b", "#b49a3a"]
BOTA = ["#141312", "#1d1b19", "#282522", "#35312c"]
LANA = ["#3a1512", "#521d18", "#6b2620", "#843128"]
RED = ["#3d3424", "#54482f", "#6c5d3c"]
CORCHO = ["#6d5434", "#8a6c44", "#a58556"]
PLOMO = ["#2a2c30", "#3c3f45", "#53575e", "#6d7179"]
PALO = ["#2c2318", "#3d3122", "#51412d", "#665439", "#7c6847"]
PALO_BLANCO = ["#6f6a5e", "#8a8475", "#a39d8c", "#bdb6a3"]     # el rastro, curtido de sal
HIERRO = ["#2b2220", "#45302a", "#5f3d31", "#7a4c3a", "#93604a"]  # oxidado
PAJA = ["#6d5e3c", "#857450", "#9d8a62", "#b4a176", "#cbb98c"]
LONA = ["#6e685c", "#8a8476", "#a39d8e", "#bcb6a6", "#d2cdbe"]
SACO = ["#4c4030", "#62533e", "#78674d", "#8e7a5d"]
OJO = "#9fe6ff"


def _cara(variante):
    C = {"S": PIEL[5], "s": PIEL[4], "o": PIEL[3], "O": PIEL[2], "K": CUENCA, "k": "#1d2629", "M": "#211c1a", "g": GRIETA,
         "W": SAL[2], "w": SAL[1]}
    filas = [
        "OosWwsoO",
        "osSSgSso",
        "oSSssSSo",
        "OKKosKKO",
        "oKkosgKo",
        "OosoosoO",
        "OoMMMMoO",
        "wOoggoOw",
    ]
    if variante == 2:
        # el salinero, con la barba blanca de sal
        filas[6] = "OWMMMMWO"
        filas[7] = "wWWwwWWw"
    return dibujo(filas, C)


def materiales(variante):
    piel = ruido(PIEL[2:6], 911, grano=0.45, base=0.55)
    sal = ruido(SAL, 912, grano=0.5, base=0.5)
    hule = tela(HULE, 921, pliegue=3, grano=0.16)
    bota = ruido(BOTA, 923, grano=0.3)

    def costra(c, cara, x, y, semilla, cuanto=0.18):
        """Manchas de sal: unas celdas blancas al azar (fijas)."""
        h = (x * 73856093 ^ y * 19349663 ^ semilla * 83492791 ^ hash(cara) % 9973) & 0xFFFF
        if h % 1000 < cuanto * 1000:
            return mezcla(c, SAL[1 + h % 3], 0.75)
        return c

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara(variante)(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 3 <= y <= 4 and d in (3, 4):
                return PIEL[1] if d == 3 else PIEL[2]       # la oreja
            if (x + 2 * y) % 7 == 0:
                return GRIETA
        if cara == "arriba":
            return costra(c, cara, x, y, 1, 0.35)
        return costra(c, cara, x, y, 2, 0.15)

    def torso(cara, x, y, w, h):
        c = hule(cara, x, y, w, h)
        if variante == 2:
            # el salinero, con la camisa de lona abierta: el pecho de sal
            c = tela(LONA, 931, pliegue=3)(cara, x, y, w, h)
            if cara == "frente" and y <= 4 and abs(x - 3.5) <= 2.0 - y * 0.3:
                return costra(piel(cara, x, y, w, h), cara, x, y, 3, 0.4)
        if cara == "frente":
            # el chubasquero abierto por delante, con sus botones de hueso
            if variante != 2 and x in (3, 4):
                return oscuro(c, 0.35) if y > 1 else PIEL[3]
            if variante != 2 and x == 2 and y % 3 == 1:
                return "#cfc5ad"
        if cara == "abajo":
            return None
        return costra(c, cara, x, y, 4, 0.12)

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente" and abs(x - (w - 1) / 2.0) < 1.2:
            return None                                       # abierto por delante
        corte = h - 1 - ((x * 5 + {"frente": 0, "espalda": 2, "der": 1, "izq": 3}.get(cara, 0)) % 3)
        if y > corte:
            return None
        c = (tela(LONA, 932, pliegue=3) if variante == 2 else hule)(cara, x, y, w, h)
        return costra(oscuro(c, 0.06 * y / max(1, h - 1)), cara, x, y, 5, 0.2)

    def brazo(cara, x, y, w, h):
        if variante == 2:
            return costra(piel(cara, x, y, w, h), cara, x, y, 6, 0.35)   # los brazos al aire
        return costra(hule(cara, x, y, w, h), cara, x, y, 6, 0.1)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 1:
            return PIEL[2] if cara != "abajo" else PIEL[1]   # los dedos
        if variante == 2 or y >= h - 3:
            return costra(piel(cara, x, y, w, h), cara, x, y, 7, 0.3)
        return hule(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return oscuro(tela(SACO, 941, pliegue=4)(cara, x, y, w, h), 0.15)

    def pierna(cara, x, y, w, h):
        return bota(cara, x, y, w, h)

    def bota_f(cara, x, y, w, h):
        if cara == "abajo":
            return BOTA[0]
        c = bota(cara, x, y, w, h)
        if y == 0:
            return BOTA[3]                                    # la vuelta de arriba
        return costra(c, cara, x, y, 8, 0.12 if y > h - 3 else 0.04)

    def cristal(cara, x, y, w, h):
        return SAL[3] if cara == "arriba" else (SAL[2] if (x + y) % 2 == 0 else SAL[0])

    def gorro(cara, x, y, w, h):
        c = ruido(LANA, 951, grano=0.5)(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if cara != "arriba" and y >= h - 1:
            return oscuro(c, 0.25)                            # la vuelta
        return costra(c, cara, x, y, 9, 0.08)

    def red(cara, x, y, w, h):
        # la malla: cuerdas cada dos celdas, lo demas, hueco
        if cara in ("arriba", "abajo"):
            return RED[1]
        if x % 2 == 0 or y % 2 == 0:
            return RED[(x + y) % 3]
        return None

    def corchos(cara, x, y, w, h):
        return CORCHO[(x // 2) % 3] if x % 3 != 2 else RED[0]

    def cuerda(cara, x, y, w, h):
        return RED[1] if (x + y) % 2 else RED[2]

    def plomo(cara, x, y, w, h):
        return metal(PLOMO, 961)(cara, x, y, w, h)

    def sueste_ala(cara, x, y, w, h):
        c = ruido(HULE_SUESTE, 971, grano=0.3)(cara, x, y, w, h)
        if cara == "abajo":
            return oscuro(c, 0.35)
        return costra(c, cara, x, y, 10, 0.1)

    def sueste_copa(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return costra(ruido(HULE_SUESTE, 972, grano=0.3)(cara, x, y, w, h), cara, x, y, 11, 0.1)

    def pertiga(cara, x, y, w, h):
        return madera(PALO, 981, "z")(cara, x, y, w, h)

    def garfio(cara, x, y, w, h):
        return metal(HIERRO, 982)(cara, x, y, w, h)

    def ala(cara, x, y, w, h):
        v = madera(PAJA, 991, "x", grano=0.5)(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            cx, cy = x - (w - 1) / 2.0, y - (h - 1) / 2.0
            r2 = cx * cx + cy * cy
            if r2 > 7.5 ** 2:
                return None
            if r2 > 6.4 ** 2 and (x * 7 + y * 3) % 5 == 0:
                return None
            if cara == "abajo":
                return oscuro(v, 0.3)
            return costra(oscuro(v, 0.1) if int(r2 ** 0.5) % 2 == 0 else v, cara, x, y, 12, 0.2)
        return oscuro(v, 0.2)

    def copa(cara, x, y, w, h):
        return costra(madera(PAJA, 992, "x", grano=0.5)(cara, x, y, w, h), cara, x, y, 13, 0.25)

    def delantal(cara, x, y, w, h):
        c = tela(LONA, 993, pliegue=4)(cara, x, y, w, h)
        if y == h - 1 and x % 2 == 0:
            return None                                       # el borde deshilachado
        return costra(c, cara, x, y, 14, 0.3)

    def saco(cara, x, y, w, h):
        c = tela(SACO, 994, pliegue=3, grano=0.3)(cara, x, y, w, h)
        if cara == "arriba":
            return SAL[1] if (x + y) % 3 else SAL[2]           # la sal que asoma
        if y <= 1 and cara != "abajo":
            return oscuro(c, 0.25)                             # la boca atada
        return c

    def pertiga_rastro(cara, x, y, w, h):
        return costra(madera(PALO_BLANCO, 995, "z")(cara, x, y, w, h), cara, x, y, 15, 0.15)

    def tabla(cara, x, y, w, h):
        return costra(madera(PALO_BLANCO, 996, "x")(cara, x, y, w, h), cara, x, y, 16, 0.3)

    return {
        "cabeza": sombra(cabeza, abajo=0.16), "torso": sombra(torso), "faldon": faldon,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.16), "antebrazo_i": sombra(antebrazo, abajo=0.16),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
        "bota_d": sombra(bota_f, abajo=0.25), "bota_i": sombra(bota_f, abajo=0.25),
        "cristal": cristal, "gorro": gorro, "gorro_alto": gorro, "red": red, "corchos": corchos, "cuerda": cuerda, "plomo": plomo,
        "sueste_ala": sueste_ala, "sueste_copa": sueste_copa, "pertiga": pertiga, "garfio": garfio,
        "ala": ala, "copa": copa, "delantal": delantal, "saco": saco, "pertiga_rastro": pertiga_rastro, "tabla": tabla,
    }


def brillo(variante):
    """Lo que luce: la luz fria de agua de las cuencas y algun cristal de sal."""
    ojos = dibujo([
        "........",
        "........",
        "........",
        ".R....R.",
        "..r..r..",
    ], {"R": OJO, "r": "#5fb0d0"})
    b = {"cabeza": por_cara(liso(None), frente=ojos)}
    b["cristal"] = lambda cara, x, y, w, h: "#d8f4ff" if cara == "arriba" and (x + y) % 3 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 12) y
# _arma = hacia donde apunta lo que lleva (el plomo cuelga hacia -Z de la mano; el gancho y el rastro estan en -Z).

# de pie: cargado de hombros, con el peso de la sal; el brazo derecho lo pone el porte de cada uno
BASE = dict(raiz=raiz(-0.3), torso=(10, 0, 2), cabeza=(8, 0, -4),
            muslo_d=(-5, 0, 3), pierna_d=(8, 0, 0), muslo_i=(2, 0, -3), pierna_i=(6, 0, 0),
            brazo_i=(-4, 0, -6), antebrazo_i=(-14, 0, 0))
PORTE = {
    "plomo": dict(_mano_d=("h", -0.6, -9.6, 1.6), _arma=(0.0, -1, 0.12)),           # el plomo colgando junto a la pierna
    "bichero": dict(_mano_d=("h", 0.4, -6.4, 3.6), _arma=(0.04, 1, 0.12)),          # de pie, como un baston, el gancho arriba
    "rastro": dict(_mano_d=("h", -1.4, -9.0, 0.8), _arma=(-0.1, -0.42, -1)),        # arrastrandolo por detras
}
GUARDIA = dict(raiz=raiz(-1.2), torso=(18, 8, 0), cabeza=(-6, -8, 0),
               muslo_d=(8, 0, 5), pierna_d=(16, 0, 0), muslo_i=(-22, 0, -5), pierna_i=(24, 0, 0))
LISTO = {
    # el plomo por delante, colgando, listo para voltearlo; la izquierda abierta
    "plomo": con(GUARDIA, _mano_d=("h", 1.5, -4.5, 6.0), _arma=(0.1, -1, 0.25), brazo_i=(-30, 0, -16), antebrazo_i=(-46, 0, 0)),
    # el bichero con las dos manos, el gancho por delante
    "bichero": con(GUARDIA, torso=(18, 26, 0), cabeza=(-6, -26, 0), _mano_d=("h", 2.5, -6.0, 1.0), _arma=(0.2, 0.08, 1), _agarra=-7),
    # el rastro con las dos manos, alto
    "rastro": con(GUARDIA, torso=(18, 26, 0), cabeza=(-6, -26, 0), _mano_d=("h", 2.5, -5.5, 1.5), _arma=(0.15, 0.22, 1), _agarra=-7),
}
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")
# de rodillas faenando (la postura de rezar del juego): el redero remendando, el bichero con la pertiga cruzada sobre las
# rodillas, el salinero amontonando sal con las manos
RODILLAS = dict(raiz=raiz(-9.2, 0.8), torso=(28, 0, 0), cabeza=(34, 0, 0), faldon=(-8, 0, 0),
                muslo_d=(-8, 0, 5), pierna_d=(98, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(98, 0, 0))
FAENANDO = {
    0: con(RODILLAS, _mano_d=("h", 3.0, -7.0, 5.5), _arma=(0.1, -1, 0.3), _mano_i=("h", -3.0, -7.0, 5.5), _arma_i=(0, -0.5, 1)),
    1: con(RODILLAS, _mano_d=("h", 2.5, -7.4, 4.0), _arma=(0.95, -0.2, 0.1), _agarra=-10),
    2: con(RODILLAS, _mano_d=("h", 2.0, -8.0, 5.0), _arma=(-0.3, -0.3, -1), _mano_i=("h", -2.0, -8.0, 5.5), _arma_i=(0, -0.5, 1)),
}
# enterrado en la costra de sal: boca arriba, rigido (el juego lo hunde un poco: solo asoma)
ENTERRADO = dict(raiz=raiz(2.4, -14.0, rot=(-90, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 0, 0),
                 muslo_d=(0, 0, 4), pierna_d=(4, 0, 0), muslo_i=(0, 0, -4), pierna_i=(4, 0, 0),
                 brazo_d=(0, 0, 12), antebrazo_d=(-8, 0, 0), brazo_i=(0, 0, -12), antebrazo_i=(-8, 0, 0))


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

    # ---- quieto: respira hondo (los hombros suben y bajan con la sal crujiendo)
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.4, con(BASE, torso=(13, 3, 2), cabeza=(14, 4, -6), raiz=raiz(-0.6), brazo_i=(-2, 0, -7))),
        (2.6, con(BASE, torso=(9, -3, 3), cabeza=(6, -6, -2), raiz=raiz(-0.2), brazo_i=(-6, 0, -5))),
        (4.0, BASE),
    ])
    # ---- andar: pesado, arrastrando las botas por la arena
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-24, 18, 8, 20, 5, 0.0), (-4, 4, 6, 36, 0, 0.7), (18, -24, 20, 8, -5, 0.0), (4, -4, 36, 6, 0, 0.7), (-24, 18, 8, 20, 5, 0.0))):
        pasos.append((1.3 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, -ty * 0.4), "cabeza": (0, -ty * 0.5, ty * 0.4), "raiz": raiz(up),
                                    "brazo_i": (md * 0.5, 0, 0), "faldon": (abs(md) * 0.3, 0, 0)}))
    bucle("andar", 1.3, pasos)
    # ---- el brazo de lo que lleva (se suma al quieto) y en guardia
    for arma, p in PORTE.items():
        q = fija(con(BASE, p))
        bucle(f"porte_{arma}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    for arma, p in LISTO.items():
        l0 = fija(p)
        l1 = fija(con(p, raiz=raiz(-1.8), torso=(p["torso"][0] + 3, p["torso"][1] - 3, 0)))
        bucle(f"listo_{arma}", 1.6, [(0.0, l0), (0.8, l1), (1.6, l0)])
    # ---- faenando de rodillas (cada uno lo suyo): las manos van y vienen
    for v in range(3):
        f0 = fija(FAENANDO[v])
        f1 = fija(con(FAENANDO[v], torso=(32, 0, 0), cabeza=(30, 6, 0)))
        bucle(f"faenando_{VARIANTES[v]}", 3.2, [(0.0, f0), (1.6, f1), (3.2, f0)])
    # ---- enterrado en la sal
    bucle("enterrado", 6.0, [(0.0, ENTERRADO), (6.0, ENTERRADO)])

    # ================================================================ acciones
    P, BI, S = LISTO["plomo"], LISTO["bichero"], LISTO["rastro"]

    # ---- 1 plomada (el redero): voltea el plomo por encima de la cabeza y lo descarga en diagonal
    P_ATRAS = con(P, raiz=raiz(-0.8, 1.0), torso=(4, 34, 0), cabeza=(-8, -28, 0), _mano_d=("h", -4.5, 3.5, -2.0), _arma=(-0.4, 0.8, -0.4))
    P_ARRIBA = con(P, raiz=raiz(-0.4, 0.4), torso=(-6, 10, 0), cabeza=(-14, -6, 0), _mano_d=("h", 0.5, 9.0, 1.5), _arma=(0.3, 1, 0.4))
    P_DA = con(P, raiz=raiz(-2.6, -2.4), torso=(30, -30, 0), cabeza=(-18, 24, 0), muslo_i=(-36, 0, -4), pierna_i=(34, 0, 0),
               _mano_d=("h", 4.0, -2.5, 7.5), _arma=(0.35, -0.6, 1))
    accion("plomada", 1.3, [
        (0.00, P),
        (0.30, P_ATRAS),
        (0.46, P_ARRIBA),
        (0.60, P_DA),
        (0.90, con(P_DA, torso=(28, -34, 0))),
        (1.30, P),
    ], golpes=(0.60,))
    # ---- 2 red (el redero): coge la red del hombro con las dos manos, la echa atras y la lanza por delante, abierta
    R_COGE = con(P, torso=(-2, 22, 0), cabeza=(-6, -16, 0), _mano_d=("h", 2.5, 4.0, -1.0), _arma=(0, -1, 0.2),
                 _mano_i=("h", -2.5, 4.0, -1.5), _arma_i=(0, 0, 1))
    R_ATRAS = con(P, raiz=raiz(-1.0, 1.6), torso=(-12, 30, 0), cabeza=(-10, -24, 0), _mano_d=("h", -2.0, 6.0, -3.5), _arma=(0, -1, 0),
                  _mano_i=("h", 2.0, 6.0, -3.5), _arma_i=(0, 0, 1))
    R_LANZA = con(P, raiz=raiz(-1.8, -2.2), torso=(22, -12, 0), cabeza=(-12, 10, 0), muslo_i=(-34, 0, -4), pierna_i=(30, 0, 0),
                  _mano_d=("h", 0.5, 3.5, 8.5), _arma=(0, 0.4, 1), _mano_i=("h", 0.5, 3.5, 8.5), _arma_i=(0, 0.4, 1))
    accion("red", 1.6, [
        (0.00, P),
        (0.35, R_COGE),
        (0.62, R_ATRAS),
        (0.78, con(R_ATRAS, torso=(-14, 32, 0))),
        (0.88, R_LANZA),
        (1.15, con(R_LANZA, torso=(18, -10, 0))),
        (1.60, P),
    ], golpes=(0.88,))
    # ---- 3 garfio (el bichero): se echa atras, clava el gancho a fondo y tira con todo el cuerpo
    G_ATRAS = con(BI, raiz=raiz(-1.4, 2.2), torso=(8, 38, 0), cabeza=(-4, -34, 0), muslo_d=(18, 0, 6), pierna_d=(20, 0, 0),
                  muslo_i=(-26, 0, -4), pierna_i=(14, 0, 0), _mano_d=("h", 2.0, -5.5, -3.5), _arma=(0.06, 0.05, 1), _agarra=-7)
    G_FONDO = con(sin(BI, "_agarra"), raiz=raiz(-2.4, -4.2), torso=(26, -30, 0), cabeza=(-20, 26, 0), muslo_d=(28, 0, 4), pierna_d=(10, 0, 0),
                  muslo_i=(-46, 0, -4), pierna_i=(38, 0, 0), brazo_i=(36, 0, -24), antebrazo_i=(-34, 0, 0),
                  _mano_d=("h", 3.5, -3.0, 7.8), _arma=(0, 0.02, 1))
    G_TIRA = con(BI, raiz=raiz(-1.0, 3.2), torso=(-8, 30, 0), cabeza=(-12, -22, 0), muslo_d=(32, 0, 6), pierna_d=(30, 0, 0),
                 muslo_i=(-4, 0, -4), pierna_i=(8, 0, 0), _mano_d=("h", 2.0, -5.0, -2.0), _arma=(0.1, 0.12, 1), _agarra=-6)
    accion("garfio", 1.6, [
        (0.00, BI),
        (0.42, G_ATRAS),
        (0.62, con(G_ATRAS, _mano_d=("h", 2.0, -5.5, -4.2))),
        (0.74, G_FONDO),
        (0.92, G_FONDO),
        (1.12, G_TIRA),
        (1.60, BI),
    ], golpes=(0.74,))
    # ---- 4 bichazo (el bichero): barre de lado a lado con la pertiga, a la altura de la cintura
    LADO_D = con(BI, raiz=raiz(-1.4, 1.0), torso=(12, 52, 0), cabeza=(-2, -46, 0),
                 _mano_d=("h", 1.5, -5.0, 2.0), _arma=(-1, 0.1, -0.15), _agarra=3)
    LADO_I = con(BI, raiz=raiz(-2.2, -2.0), torso=(20, -30, 0), cabeza=(-8, 26, 0), muslo_i=(-34, 0, -6), pierna_i=(30, 0, 0),
                 _mano_d=("h", 7.0, -6.0, 0.0), _arma=(1, -0.05, 0.3), _agarra=-3)
    accion("bichazo", 1.7, [
        (0.00, BI),
        (0.52, LADO_D),
        (0.76, con(LADO_D, torso=(12, 58, 0), cabeza=(-2, -50, 0))),
        (0.86, con(BI, raiz=raiz(-2.0, -1.0), torso=(18, 18, 0), cabeza=(-6, -18, 0),
                   _mano_d=("h", 3.0, -6.5, 2.5), _arma=(0.05, -0.02, 1), _agarra=-1)),
        (0.96, LADO_I),
        (1.26, con(LADO_I, torso=(18, -34, 0))),
        (1.70, BI),
    ], golpes=(0.86,))
    # ---- 5 rastrillazo (el salinero): alza el rastro con las dos manos por encima de la cabeza y lo estrella delante
    S_ALTO = con(S, raiz=raiz(-0.2, 1.6), torso=(-16, 8, 0), cabeza=(-22, -6, 0), muslo_d=(10, 0, 4), pierna_d=(8, 0, 0),
                 muslo_i=(-14, 0, -4), pierna_i=(10, 0, 0), _mano_d=("h", 3.5, 8.0, 0.5), _arma=(0.05, 0.8, -0.6), _agarra=3)
    S_BAJO = con(S, raiz=raiz(-3.6, -2.6), torso=(46, -6, 0), cabeza=(-30, 4, 0), muslo_d=(16, 0, 4), pierna_d=(30, 0, 0),
                 muslo_i=(-52, 0, -4), pierna_i=(54, 0, 0), _mano_d=("h", 3.5, -4.0, 6.5), _arma=(0.05, -0.55, 1), _agarra=3)
    accion("rastrillazo", 1.8, [
        (0.00, S),
        (0.52, S_ALTO),
        (0.76, con(S_ALTO, raiz=raiz(0.0, 1.8), torso=(-20, 8, 0))),
        (0.88, S_BAJO),
        (1.22, con(S_BAJO, torso=(44, -6, 0))),
        (1.80, S),
    ], golpes=(0.88,))
    # ---- 6 sal (el salinero): mete la izquierda en el saco y le tira un punado de sal a la cara
    S1 = con(sin(S, "_agarra"), _mano_d=("h", 0.5, -6.8, 3.0), _arma=(0.1, 0.9, 0.3))
    SAL_COGE = con(S1, torso=(-6, 30, 0), cabeza=(-6, -20, 0), _mano_i=("h", 1.5, 2.0, -4.0), _arma_i=(0, -1, 0))
    SAL_TIRA = con(S1, raiz=raiz(-1.4, -1.6), torso=(20, -34, 0), cabeza=(-12, 28, 0), muslo_i=(-30, 0, -4), pierna_i=(26, 0, 0),
                   _mano_i=("h", -2.0, 2.5, 9.0), _arma_i=(0, 0.2, 1))
    accion("sal", 1.3, [
        (0.00, S),
        (0.24, S1),
        (0.42, SAL_COGE),
        (0.54, con(SAL_COGE, torso=(-8, 36, 0))),
        (0.66, SAL_TIRA),
        (0.92, con(SAL_TIRA, torso=(18, -40, 0))),
        (1.30, S),
    ], golpes=(0.66,))
    # ---- 7 aturdido: se tambalea hacia atras, los brazos caidos
    AT = dict(raiz=raiz(-1.6, 2.0), torso=(-14, 0, 8), cabeza=(-24, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(26, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(20, 0, 0), brazo_d=(18, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(20, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(6, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-0.8, 0.0))),
        (0.18, AT),
        (0.60, con(AT, torso=(-8, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.00, con(AT, torso=(-12, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(8, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.0, 0.6))),
    ])
    # ---- 8 morir: cae de rodillas y se derrumba de bruces (el juego lo deshace en sal)
    CAE_RODILLAS = dict(raiz=raiz(-9.6, 0.6), muslo_d=(-10, 0, 6), pierna_d=(102, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(100, 0, 0),
                        torso=(8, 0, 4), cabeza=(-30, 0, 8), brazo_d=(6, 0, 24), antebrazo_d=(-10, 0, 0), mano_d=(70, 0, 0),
                        brazo_i=(8, 0, -24), antebrazo_i=(-12, 0, 0))
    BRUCES = dict(raiz=raiz(2.2, 14.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-4, 20, 0),
                  muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                  brazo_d=(-150, 0, 20), antebrazo_d=(-10, 0, 0), brazo_i=(-30, 0, -40), antebrazo_i=(-70, 0, 0))
    accion("morir", 1.7, [
        (0.00, con(BASE, torso=(-10, 0, 6), cabeza=(-26, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0),
                   brazo_i=(10, 0, -30))),
        (0.45, CAE_RODILLAS),
        (0.80, con(CAE_RODILLAS, raiz=raiz(-9.8, 0.2), torso=(24, 0, 2), cabeza=(10, 0, 6), brazo_d=(-10, 0, 20), brazo_i=(-8, 0, -20))),
        (1.20, BRUCES),
        (1.70, BRUCES),
    ])
    # ---- 9 levantarse (de faenar de rodillas) y 10 alzarse (de la costra de sal)
    DE_PIE = con(GUARDIA, brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0), brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    MEDIO = dict(raiz=raiz(-5.0, 0.4), muslo_d=(-60, 0, 6), pierna_d=(84, 0, 0), muslo_i=(0, 0, -6), pierna_i=(70, 0, 0),
                 torso=(30, 10, 0), cabeza=(-14, 10, 0), brazo_d=(0, 0, 10), antebrazo_d=(-50, 0, 0), mano_d=(40, 0, 0),
                 brazo_i=(-20, 0, -12), antebrazo_i=(-30, 0, 0))
    RZ = dict(raiz=raiz(-9.2, 0.8), torso=(28, 0, 0), cabeza=(34, 0, 0), faldon=(-8, 0, 0),
              muslo_d=(-8, 0, 5), pierna_d=(98, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(98, 0, 0),
              brazo_d=(-46, 0, -16), antebrazo_d=(-74, 0, 0), mano_d=(60, 0, 0), brazo_i=(-46, 0, 16), antebrazo_i=(-74, 0, 0))
    accion("levantarse", 1.3, [
        (0.00, RZ),
        (0.35, con(RZ, torso=(10, 30, 0), cabeza=(-10, 40, 0), brazo_d=(-10, 0, 10), antebrazo_d=(-40, 0, 0), mano_d=(40, 0, 0),
                   brazo_i=(-10, 0, -10), antebrazo_i=(-30, 0, 0))),
        (0.80, MEDIO),
        (1.30, DE_PIE),
    ])
    accion("alzarse", 1.6, [
        (0.00, ENTERRADO),
        (0.40, con(ENTERRADO, raiz=raiz(3.0, -10.0, rot=(-50, 0, 0)), cabeza=(-20, 0, 0), brazo_d=(-30, 0, 30), brazo_i=(-30, 0, -30))),
        (0.85, dict(raiz=raiz(-5.5, 1.0, rot=(-10, 0, 0)), torso=(20, 0, 0), cabeza=(-20, 0, 0),
                    muslo_d=(-70, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(96, 0, 0),
                    brazo_d=(-60, 0, 14), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(-60, 0, -14), antebrazo_i=(-20, 0, 0))),
        (1.20, con(MEDIO, raiz=raiz(-4.6, 0.0), torso=(28, 0, 0), cabeza=(-10, 0, 0))),
        (1.60, DE_PIE),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if nombre in ("plomada", "red") or nombre.endswith("plomo"):
        return 0
    if nombre in ("garfio", "bichazo") or nombre.endswith("bichero"):
        return 1
    if nombre in ("rastrillazo", "sal") or nombre.endswith("rastro"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_plomo"]]
    if nombre == "quieto":
        return [anims["porte_plomo"]]
    return []


TABLA_EXTRA = ()
