"""
Ser Lucan, el Jinete Nocturno (parte 7x; respuesta 132 del plan: "jinete nocturno, solo de noche, en caminos"): el heraldo
del Rey de Valdrenn que salio una noche por el camino de nubes con la ultima carta del Rey para la Fortaleza de Hueso y no
llego. Desde entonces cabalga cada noche, de la Torre de Ascenso hacia la fortaleza, y al amanecer se deshace sin haber
llegado.

Un solo modelo con el caballo y el jinete (el jinete va sentado en la silla: sus piezas cuelgan del lomo del caballo, asi le
sigue en el galope, al encabritarse y al caer):
- el caballo de guerra negro (el del corcel de la parte 7b, mas grande y pesado): barda de hierro negro con ribetes de oro
  (la testera con su pua, la crinera, el peto), la gualdrapa roja del Rey hecha jirones con la corona bordada en los
  costados, la crin, la cola, los ojos y el fuego de los cascos de fuego de almas azul;
- el jinete: armadura de placas negra, el yelmo cerrado con una corona por cimera y un penacho de fuego de almas, el tabardo
  rojo del heraldo con la corona de oro, la capa hecha jirones, el tubo de cuero de la carta a la espalda (con su lacre),
  el cuerno de guerra al cinto (y en la mano izquierda, solo cuando lo toca) y la guja en la derecha.

Las piezas del caballo llevan delante "c_" (las del jinete son las de una persona: brazo / antebrazo / mano, y las de un
caballo se llaman igual). Las patas del caballo, sin cinematica inversa (giros, como el corcel); los brazos del jinete, con
la de siempre (_mano_d, _arma, _mano_i, _agarra).
"""
import numpy as np

from anim import Anim, con, raiz
from modelo import Box, Rig, g
from pinta import (claro, delante, liso, metal, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "jinete"
VARIANTES = ("lucan",)
ESCALA = 1.15
CENTRO_Y = 1.75
#: para las hojas de revision (generar.py): un poco mas lejos que a uno de tamano de persona (va montado)
ZOOM_VARIANTES = 62
ZOOM_TIRA = 40
LADO_TIRA = 300

#: el cuerno en la mano: solo se ve mientras lo toca (lo decide el modelo del juego)
OCULTAS = {0: ["cuerno_mano"]}

#: el puno: el centro de lo que agarra, en el sistema de la mano (y hacia abajo)
AGARRE = 0.8


# ---------------------------------------------------------------- el modelo
# (pixeles; Y hacia abajo; el suelo en y = 24; el hocico mira a -Z; la derecha es -X)

def rig():
    r = Rig(NOMBRE, 128)
    r.pieza("raiz", "", (0, 24, 0))
    # ================================================================ el caballo
    # la cadera (atras, a 21 del suelo): de ella salen el lomo hacia delante, las patas de atras y la cola
    r.pieza("c_cadera", "raiz", (0, -21, 10), boxes=[
        Box(-5, -3, -2.5, 10, 6.5, 8.5, "grupa"),
        Box(-5.6, -3.4, -2.2, 11.2, 8.2, 8.6, "grupera", grow=g(0.12)),
    ])
    # el lomo: el cuerpo, la gualdrapa (los costados, hechos jirones), la silla y el peto
    r.pieza("c_torso", "c_cadera", (0, -0.5, -2.5), boxes=[
        Box(-5.5, -3, -20, 11, 11.5, 20, "cuerpo"),
        Box(-6.1, -3.3, -19.4, 12.2, 12.6, 19.2, "gualdrapa", grow=g(0.1)),
        Box(-4.9, -4.8, -14.5, 9.8, 2, 8, "silla"),
        Box(-1.2, -7.0, -15.0, 2.4, 2.4, 1.8, "perilla"),
        Box(-3.4, -6.3, -7.6, 6.8, 1.8, 1.4, "arzon"),
        Box(-5.9, -1.8, -21.4, 11.8, 9.5, 2.0, "peto"),
    ])
    # el cuello (sube hacia delante) con la crinera de placas, la crin de fuego encima, y la cabeza con su testera
    r.pieza("c_cuello", "c_torso", (0, -1.2, -19.2), rot=(-50, 0, 0), boxes=[
        Box(-2.7, -2.6, -12, 5.4, 5.8, 13, "cuello"),
        Box(-3.0, -3.0, -11.4, 6.0, 2.6, 11.6, "crinera", grow=g(0.08)),
        Box(-0.8, -7.0, -12.5, 1.6, 4.6, 13.5, "crin"),
    ])
    r.pieza("c_cabeza", "c_cuello", (0, 0.2, -11.5), rot=(92, 0, 0), boxes=[
        Box(-2.9, -3.2, -6, 5.8, 6, 6.5, "craneo"),
        Box(-2.2, -2.5, -13.2, 4.4, 4.4, 7.4, "hocico"),
        Box(-2.5, -3.7, -12.8, 5.0, 1.6, 10.4, "testera"),
        Box(-0.55, -6.0, -7.0, 1.1, 2.6, 1.1, "pua"),
        Box(-2.4, -5.4, -1.8, 1.2, 2.6, 1.1, "oreja"),
        Box(1.2, -5.4, -1.8, 1.2, 2.6, 1.1, "oreja"),
        Box(-2.3, -1.0, -10.2, 4.6, 1.2, 1.2, "brida", grow=g(0.05)),
        Box(-0.7, -5.8, -3.2, 1.4, 3.4, 3.6, "copete"),
    ])
    r.pieza("c_mandibula", "c_cabeza", (0, 1.7, -5.2), rot=(4, 0, 0), boxes=[Box(-1.7, 0, -7.6, 3.4, 1.4, 8.0, "mandibula")])
    # las patas de delante: del hombro a la rodilla, la cana, la cerneja y el casco
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"c_brazo_{n}", "c_torso", (s * 3.8, 6.0, -17), boxes=[Box(-1.8, -2.5, -1.9, 3.6, 9.5, 3.8, "c_brazo")])
        r.pieza(f"c_antebrazo_{n}", f"c_brazo_{n}", (0, 7.0, 0), boxes=[Box(-1.2, 0, -1.2, 2.4, 6.0, 2.4, "c_antebrazo")])
        r.pieza(f"c_mano_{n}", f"c_antebrazo_{n}", (0, 6.0, 0), boxes=[
            Box(-1.5, -0.4, -1.5, 3.0, 1.8, 3.0, "cerneja"),
            Box(-1.8, 1.4, -2.0, 3.6, 1.4, 3.8, "casco"),
        ])
    # las de atras: el muslo hacia delante, la canilla hacia atras y el corvejon con el casco
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"c_muslo_{n}", "c_cadera", (s * 3.9, 2.0, 2.6), rot=(-20, 0, 0), boxes=[Box(-2.0, -2.5, -2.2, 4.0, 9.5, 4.4, "c_muslo")])
        r.pieza(f"c_pierna_{n}", f"c_muslo_{n}", (0, 7.0, 0), rot=(45, 0, 0), boxes=[Box(-1.2, 0, -1.2, 2.4, 7.5, 2.4, "c_pierna")])
        r.pieza(f"c_pata_{n}", f"c_pierna_{n}", (0, 7.5, 0), rot=(-25, 0, 0), boxes=[
            Box(-1.0, 0, -1.0, 2.0, 2.4, 2.0, "c_tarso"),
            Box(-1.5, 2.0, -1.5, 3.0, 1.8, 3.0, "cerneja", uv="cerneja"),
            Box(-1.8, 3.6, -2.0, 3.6, 1.4, 3.8, "casco", uv="casco"),
        ])
    # la cola: el maslo corto y el fuego que cuelga
    r.pieza("c_cola", "c_cadera", (0, -2.5, 5.8), rot=(-35, 0, 0), boxes=[
        Box(-0.9, -0.9, 0, 1.8, 1.8, 4, "maslo"),
        Box(-1.7, -1.7, 2.5, 3.4, 3.4, 6.5, "cola_f"),
    ])
    r.pieza("c_cola2", "c_cola", (0, 0, 8.5), rot=(-25, 0, 0), boxes=[Box(-1.4, -1.4, 0, 2.8, 2.8, 7.5, "cola_f2")])

    # ================================================================ el jinete (sentado en la silla)
    # la cadera del jinete en lo alto de la silla; de ella, el torso y los muslos (abiertos por encima del lomo)
    r.pieza("cadera", "c_torso", (0, -4.7, -10.6))
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"muslo_{n}", "cadera", (s * 2.4, -0.4, 0), rot=(-30, 0, s * -58), boxes=[Box(-2.3, 0, -2.3, 4.6, 6.5, 4.6, f"muslo_{n}")])
        r.pieza(f"pierna_{n}", f"muslo_{n}", (0, 6.5, 0), rot=(55, 0, s * 45), boxes=[
            Box(-2.3, 0, -2.3, 4.6, 6.5, 4.6, f"pierna_{n}"),
            Box(-2.5, 4.6, -3.4, 5.0, 2.0, 5.6, "escarpe"),
        ])
    r.pieza("torso", "cadera", (0, 0, 0), boxes=[
        Box(-4.5, -12, -2.5, 9, 12, 5, "torso"),
        Box(-4.8, -11.4, -2.8, 9.6, 13.4, 5.6, "tabardo", grow=g(0.12)),
        Box(-5.0, -2.2, -3.0, 10.0, 1.6, 6.0, "cinto", grow=g(0.2)),
    ])
    r.pieza("gola", "torso", (0, 0, 0), boxes=[Box(-3.6, -13.6, -2.8, 7.2, 2.2, 5.6, "gola")])
    r.pieza("cabeza", "torso", (0, -12.4, 0), boxes=[
        Box(-4, -8.6, -4, 8, 8.6, 8, "yelmo"),
        Box(-4.4, -9.8, -4.4, 8.8, 1.6, 8.8, "corona", grow=g(0.1)),
    ])
    r.pieza("penacho", "cabeza", (0, -9.6, 1.0), rot=(30, 0, 0), boxes=[Box(-0.9, -3.0, 0, 1.8, 3.0, 9.0, "penacho")])
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"brazo_{n}", "torso", (s * 6.6, -10, 0), boxes=[Box(-2.2, -2, -2.2, 4.4, 6.5, 4.4, f"brazo_{n}")])
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-3.0, -3.4, -3.0, 6.0, 4.2, 6.0, f"hombrera_{n}")])
        r.pieza(f"antebrazo_{n}", f"brazo_{n}", (0, 4.5, 0), boxes=[
            Box(-2.2, 0, -2.2, 4.4, 6.0, 4.4, f"antebrazo_{n}"),
            Box(-2.6, 2.6, -2.6, 5.2, 2.2, 5.2, "guantelete"),
        ])
        r.pieza(f"mano_{n}", f"antebrazo_{n}", (0, 5.0, 0))
    # la capa (de los hombros a la grupa; en el galope vuela hacia atras)
    r.pieza("capa", "torso", (0, -11.6, 2.7), rot=(8, 0, 0), boxes=[Box(-5.2, 0, 0, 10.4, 9, 1.0, "capa")])
    r.pieza("capa2", "capa", (0, 9, 0.2), rot=(6, 0, 0), boxes=[Box(-5.2, 0, -0.2, 10.4, 8, 1.0, "capa2")])
    # el tubo de la carta, cruzado a la espalda sobre la capa, con sus tapas y el lacre
    r.pieza("tubo", "torso", (0, -6.5, 4.6), rot=(0, 0, 38), boxes=[
        Box(-1.3, -6.5, -1.3, 2.6, 13, 2.6, "tubo"),
        Box(-1.6, -7.4, -1.6, 3.2, 1.6, 3.2, "tapa"),
        Box(-1.6, 5.8, -1.6, 3.2, 1.6, 3.2, "tapa", uv="tapa"),
        Box(-0.8, -1.0, 1.2, 1.6, 1.6, 0.8, "lacre"),
    ])
    # el cuerno de guerra: al cinto (a su izquierda) y en la mano izquierda (solo mientras lo toca)
    r.pieza("cuerno_cinto", "torso", (5.2, -1.6, 0.6), rot=(0, 0, -20), boxes=[
        Box(-0.9, 0, -0.9, 1.8, 3.6, 1.8, "cuerno_a"),
        Box(-1.3, 3.2, -1.6, 2.6, 2.6, 3.2, "cuerno_b"),
        Box(-1.6, 5.2, -2.6, 3.2, 1.6, 3.2, "cuerno_c"),
    ])
    r.pieza("cuerno_mano", "mano_i", (0, 0, 0), rot=(90, 0, 0), boxes=[
        Box(-0.9, -4.0, -AGARRE - 0.9, 1.8, 5.0, 1.8, "cuerno_a", uv="cuerno_a"),
        Box(-1.3, -6.6, -AGARRE - 1.3, 2.6, 3.2, 2.6, "cuerno_b", uv="cuerno_b"),
        Box(-1.6, -8.4, -AGARRE - 0.4, 3.2, 2.2, 3.2, "cuerno_c", uv="cuerno_c"),
    ])
    # la guja (tumbada: a lo largo de su -Y va hacia delante; su +Z es "arriba" de la mano): el asta de fresno negro con
    # su regaton, la anilla de oro, la hoja ancha de un solo filo, su punta y el espolon de detras
    r.pieza("guja", "mano_d", (0, 0, 0), rot=(90, 0, 0), boxes=[
        Box(-1, -20, -AGARRE - 1, 2, 28, 2, "asta"),
        Box(-1.3, 7.5, -AGARRE - 1.3, 2.6, 2.2, 2.6, "regaton"),
        Box(-1.4, -21.6, -AGARRE - 1.4, 2.8, 2, 2.8, "anilla"),
        Box(-0.6, -32.5, -AGARRE - 1.2, 1.2, 12, 5.0, "hoja"),
        Box(-0.6, -35.5, -AGARRE + 0.8, 1.2, 3.4, 2.6, "punta"),
        Box(-0.6, -25.5, -AGARRE - 3.4, 1.2, 2.2, 2.4, "espolon"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas

PELO = ["#08090c", "#0f1116", "#171a21", "#20252e", "#2c3340"]
HIERRO = ["#0d0f13", "#171a20", "#22262e", "#323844", "#485062"]
ORO = ["#4a3410", "#7a5a22", "#a8823a", "#d2ae5e"]
ROJO = ["#220709", "#3a0d10", "#561418", "#701d20", "#8a2a28"]
CUERO = ["#1a120d", "#2a1d14", "#3c2a1c", "#503a26"]
CAPA = ["#0c0e14", "#151923", "#1e2432", "#2a3244"]
MARFIL = ["#8c8270", "#b4aa92", "#d6ceb6", "#efe9d6"]
ASTA = ["#120e0c", "#1c1612", "#28201a", "#34291f"]
HOJA = ["#262a32", "#3e4450", "#5c6474", "#808a9c", "#aab4c6"]
ALMA = ["#2e6c8a", "#58a6c8", "#8ed0ec", "#d0f2ff"]
HUECO = "#040508"


def _jirones(semilla, alto=3, cada=2):
    """Lo de abajo de una tela hecha jirones: devuelve si el pixel (x, y) de una cara de alto h esta roto."""
    azar = np.random.default_rng(semilla).random(128)

    def roto(x, y, h, cara_k=0):
        largo = int(azar[(x * 3 + cara_k * 17) % 128] * (alto + 1))
        if (x + cara_k) % cada == 0:
            largo += 1
        return y >= h - largo

    return roto


def materiales(variante):
    pelo = ruido(PELO[1:5], 801, grano=0.5, base=0.45)
    pelo_b = ruido(PELO[0:4], 802, grano=0.45, base=0.5)
    hierro = metal(HIERRO, 811, grano=0.22)
    hierro_b = metal(HIERRO[1:5], 812, grano=0.2)
    rojo = tela(ROJO, 821, pliegue=3, grano=0.18, luz=0.25)
    capa_t = tela(CAPA, 831, pliegue=4, grano=0.16, luz=0.3)
    cuero = ruido(CUERO, 841, grano=0.45, base=0.5)
    marfil = ruido(MARFIL, 851, grano=0.35, base=0.55)
    roto_g = _jirones(861, alto=3, cada=2)
    roto_c = _jirones(862, alto=4, cada=3)
    kc = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

    def ribete(c, cara, x, y, w, h):
        """El ribete de oro de las placas de la barda (arriba y abajo de los costados)."""
        if cara in ("der", "izq", "frente", "espalda") and (y == 0 or y == h - 1):
            return ORO[1] if (x + y) % 2 == 0 else ORO[2]
        return c

    # ---- el caballo
    def cuerpo(cara, x, y, w, h):
        c = pelo(cara, x, y, w, h)
        if cara == "abajo":
            return oscuro(c, 0.3)
        return c

    def corona(cara, x, y, w, h, cx, cy):
        """La corona del Rey bordada en oro (5 de ancho, 4 de alto) con la esquina en (cx, cy)."""
        dx, dy = x - cx, y - cy
        dibujo = ["a.a.a", "aaaaa", "abbba", "aaaaa"]
        if 0 <= dx < 5 and 0 <= dy < 4:
            ch = dibujo[dy][dx]
            if ch == "a":
                return ORO[3] if dy == 0 else ORO[2]
            if ch == "b":
                return ROJO[4]
        return None

    def gualdrapa(cara, x, y, w, h):
        # los costados: la tela roja del Rey con su franja de oro abajo, la corona en el centro y los jirones
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente":
            return None                                     # por delante la tapa el peto
        if roto_g(x, y, h, kc[cara]):
            return None
        if y <= 1:
            return None                                     # por arriba asoma el lomo
        if y == h - 4 or y == 2:
            return ORO[1] if x % 2 == 0 else ORO[2]
        if cara in ("der", "izq"):
            k = corona(cara, x, y, w, h, w // 2 - 2, 4)
            if k:
                return k
        c = rojo(cara, x, y, w, h)
        if (x * 5 + y * 3) % 11 == 0:
            return oscuro(c, 0.3)                           # desgastes
        return c

    def grupera(cara, x, y, w, h):
        if cara in ("arriba", "abajo", "frente"):
            return None
        if roto_g(x, y, h, kc[cara] + 7):
            return None
        if y == 0:
            return ORO[2] if x % 2 == 0 else ORO[1]
        return rojo(cara, x, y, w, h)

    def silla(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)):
            return ORO[1]
        if cara in ("der", "izq") and y == 0 and x % 2 == 0:
            return ORO[2]
        return c

    def peto(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "frente":
            if x == w // 2 or x == w // 2 - 1:
                return claro(c, 0.12)                       # la arista del peto
            if y == h - 1 or y == 0:
                return ORO[2]
            if (x in (1, w - 2)) and y % 2 == 1:
                return ORO[1]                               # los remaches
        return c

    def crinera(cara, x, y, w, h):
        c = hierro_b(cara, x, y, w, h)
        if cara in ("arriba", "der", "izq") and (y if cara != "arriba" else y) % 3 == 0:
            return ORO[1] if cara == "arriba" else oscuro(c, 0.3)   # las juntas de las placas
        if cara == "abajo":
            return None
        return c

    def cuello(cara, x, y, w, h):
        return pelo(cara, x, y, w, h)

    def craneo(cara, x, y, w, h):
        c = pelo(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if d <= 2 and y in (1, 2):
                return HUECO                                # el ojo (le brilla)
        if cara == "abajo":
            return oscuro(c, 0.35)
        return c

    def hocico(cara, x, y, w, h):
        c = pelo_b(cara, x, y, w, h)
        if cara == "frente" and y == 2 and x in (0, w - 1):
            return HUECO                                    # los ollares
        if cara == "abajo":
            return HUECO
        return c

    def testera(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if cara == "arriba":
            if x == w // 2 or x == w // 2 - 1:
                return ORO[2] if y % 3 == 0 else claro(c, 0.1)
            if y in (2, 3) and x in (0, w - 1):
                return HUECO                                # los agujeros de los ojos
        return ribete(c, cara, x, y, w, h)

    def pua(cara, x, y, w, h):
        return ORO[3] if y == 0 else ORO[2]

    def oreja(cara, x, y, w, h):
        return oscuro(pelo(cara, x, y, w, h), 0.1 if y > 0 else 0.0)

    def brida(cara, x, y, w, h):
        if cara in ("der", "izq") and x == w // 2:
            return ORO[2]
        return CUERO[1] if (x + y) % 2 == 0 else CUERO[2]

    def mandibula(cara, x, y, w, h):
        return oscuro(pelo(cara, x, y, w, h), 0.12)

    def pata(cara, x, y, w, h):
        c = pelo(cara, x, y, w, h)
        if y == h - 1:
            return claro(c, 0.06)
        return c

    def cerneja(cara, x, y, w, h):
        # el pelo largo de encima del casco (negro con canas azuladas: alli empieza el fuego)
        if cara == "abajo":
            return None
        c = pelo_b(cara, x, y, w, h)
        if y == h - 1 and x % 2 == 0:
            return CAPA[3]
        return c

    def casco(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "abajo":
            return HIERRO[0]
        return c

    def maslo(cara, x, y, w, h):
        return pelo(cara, x, y, w, h)

    # ---- el jinete
    def placas(cara, x, y, w, h, cada=3):
        c = hierro(cara, x, y, w, h)
        if cara in ("der", "izq", "frente", "espalda") and y % cada == cada - 1:
            return oscuro(c, 0.32)                          # las juntas de las placas
        return c

    def muslo(cara, x, y, w, h):
        return placas(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        c = placas(cara, x, y, w, h, 4)
        if cara == "frente" and y in (0, 1) and 1 <= x <= w - 2:
            return claro(c, 0.14)                           # la rodillera
        return c

    def escarpe(cara, x, y, w, h):
        c = hierro_b(cara, x, y, w, h)
        if cara == "espalda" and y == h // 2 and x == w // 2:
            return ORO[2]                                   # la espuela
        return c

    def torso(cara, x, y, w, h):
        return placas(cara, x, y, w, h, 4)

    def tabardo(cara, x, y, w, h):
        # el tabardo del heraldo: rojo, con la corona de oro en el pecho y en la espalda; abierto por los lados
        if cara in ("arriba", "abajo", "der", "izq"):
            return None
        if y >= h - 3 and (x * 7 + y) % 3 == 0:
            return None                                     # el bajo, deshilachado
        if y == 0:
            return ORO[1]
        k = corona(cara, x, y, w, h, w // 2 - 2, 3)
        if k:
            return k
        if x in (0, w - 1):
            return ORO[1] if y % 2 == 0 else ORO[0]         # la orla
        return rojo(cara, x, y, w, h)

    def cinto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente" and w // 2 - 1 <= x <= w // 2:
            return ORO[3]                                   # la hebilla
        return CUERO[1] if (x + y) % 3 else CUERO[2]

    def gola(cara, x, y, w, h):
        c = hierro_b(cara, x, y, w, h)
        if cara == "abajo":
            return None
        return c

    def yelmo(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "frente":
            if y == 3 and 1 <= x <= w - 2:
                return HUECO                                # la visera (le brillan los ojos)
            if y == 4 and x in (2, 3, 4, 5):
                return oscuro(c, 0.3)
            if y >= 5 and (x + y) % 2 == 0 and 2 <= x <= w - 3:
                return oscuro(c, 0.25)                      # los agujeros para respirar
            if x in (w // 2 - 1, w // 2) and y <= 6:
                return claro(c, 0.12)                       # la arista
        if cara == "abajo":
            return oscuro(c, 0.4)
        return c

    def corona_m(cara, x, y, w, h):
        # la corona de cimera: oro con puntas (las de arriba de cada cara, una si y otra no)
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= h - 2 else ORO[2]
        if y == 0 and x % 2 == 1:
            return None
        return ORO[3] if y == 0 else (ORO[2] if (x % 3) else ROJO[4])

    def brazo(cara, x, y, w, h):
        return placas(cara, x, y, w, h)

    def hombrera(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if cara in ("der", "izq", "frente", "espalda") and y == h - 1:
            return ORO[1]
        if cara == "arriba" and (x + y) % 4 == 0:
            return claro(c, 0.1)
        return c

    def guantelete(cara, x, y, w, h):
        c = hierro_b(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            return None
        if y == 0:
            return ORO[1]
        return c

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return capa_t(cara, x, y, w, h)

    def capa2(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if roto_c(x, y, h, kc[cara]):
            return None
        return capa_t(cara, x, y + 9, w, h + 9)

    def tubo(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara in ("der", "izq", "frente", "espalda") and y % 4 == 0:
            return CUERO[0]                                 # las costuras
        return c

    def tapa(cara, x, y, w, h):
        return ORO[2] if (x + y) % 3 else ORO[1]

    def lacre(cara, x, y, w, h):
        return ROJO[3] if (x + y) % 2 == 0 else ROJO[2]

    def cuerno_a(cara, x, y, w, h):
        c = marfil(cara, x, y, w, h)
        if y == 0:
            return "#b8bcc6"                                # la boquilla de plata
        return c

    def cuerno_b(cara, x, y, w, h):
        c = marfil(cara, x, y, w, h)
        if y == 0 or y == h - 1:
            return "#9aa0ac"                                # los aros de plata
        return c

    def cuerno_c(cara, x, y, w, h):
        if cara == "abajo":
            return HUECO                                    # la boca del cuerno
        return claro(marfil(cara, x, y, w, h), 0.05)

    def asta(cara, x, y, w, h):
        c = ruido(ASTA, 871, grano=0.35, base=0.5)(cara, x, y, w, h)
        if cara in ("der", "izq", "frente", "espalda") and 12 <= y <= 18 and y % 2 == 0:
            return CUERO[2]                                 # el puno de cuero
        return c

    def regaton(cara, x, y, w, h):
        return hierro(cara, x, y, w, h)

    def anilla(cara, x, y, w, h):
        return ORO[2] if (x + y) % 2 else ORO[3]

    def hoja(cara, x, y, w, h):
        c = metal(HOJA, 881, grano=0.16)(cara, x, y, w, h)
        if cara in ("der", "izq"):
            # el filo (el canto de arriba de la hoja) mas claro y el lomo oscuro; un canal por el medio
            d = delante(cara, x, w)
            if d >= w - 1:
                return HOJA[4]
            if d == 0:
                return HOJA[1]
            if d == w // 2 - 1 and 2 <= y <= h - 3:
                return oscuro(c, 0.25)
        return c

    def punta(cara, x, y, w, h):
        return metal(HOJA[1:], 882, grano=0.16)(cara, x, y, w, h)

    def espolon(cara, x, y, w, h):
        return hierro(cara, x, y, w, h)

    def penacho(cara, x, y, w, h):
        return None                                         # solo brilla (fuego de almas)

    return {
        # el caballo
        "grupa": cuerpo, "grupera": grupera, "cuerpo": cuerpo, "gualdrapa": gualdrapa, "silla": silla,
        "perilla": liso(ORO[2]), "arzon": silla, "peto": peto, "cuello": sombra(cuello, abajo=0.1), "crinera": crinera,
        "crin": liso(None), "craneo": sombra(craneo, abajo=0.12), "hocico": sombra(hocico, abajo=0.12), "testera": testera,
        "pua": pua, "oreja": oreja, "brida": brida, "copete": liso(None), "mandibula": mandibula,
        "c_brazo": pata, "c_antebrazo": pata, "cerneja": cerneja, "casco": casco, "c_muslo": pata, "c_pierna": pata,
        "c_tarso": sombra(pata, abajo=0.1), "maslo": maslo, "cola_f": liso(None), "cola_f2": liso(None),
        # el jinete
        "muslo_d": muslo, "muslo_i": muslo, "pierna_d": pierna, "pierna_i": pierna, "escarpe": escarpe,
        "torso": torso, "tabardo": tabardo, "cinto": cinto, "gola": gola, "yelmo": sombra(yelmo, abajo=0.1),
        "corona": corona_m, "penacho": penacho, "brazo_d": brazo, "brazo_i": brazo, "hombrera_d": hombrera,
        "hombrera_i": hombrera, "antebrazo_d": brazo, "antebrazo_i": brazo, "guantelete": guantelete,
        "capa": capa, "capa2": capa2, "tubo": tubo, "tapa": tapa, "lacre": lacre,
        "cuerno_a": cuerno_a, "cuerno_b": cuerno_b, "cuerno_c": cuerno_c,
        "asta": asta, "regaton": regaton, "anilla": anilla, "hoja": hoja, "punta": punta, "espolon": espolon,
    }


def brillo(variante):
    """Lo que brilla: los ojos del caballo, la crin, el copete y la cola de fuego de almas, el fuego de los cascos, los ojos
    del jinete tras la visera, su penacho y un hilo de luz por el filo de la guja."""

    def llama(semilla, densa=0.62):
        azar = np.random.default_rng(semilla).random((64, 64))
        kc = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

        def f(cara, x, y, w, h):
            t = azar[(y + 7 * kc[cara]) % 64, (x + 13 * kc[cara]) % 64]
            if (x * 7 + y * 3 + semilla) % 5 == 0 or t > densa + 0.25 * (y / max(1, h - 1)):
                return None
            k = min(3, max(0, int(3.4 - 3.2 * y / max(1, h - 1) + (t - 0.5) * 1.6)))
            return ALMA[k]

        return f

    ojo_lado = lambda cara, x, y, w, h: ALMA[3] if (delante(cara, x, w) == 1 and y == 1) else (ALMA[1] if (delante(cara, x, w) in (0, 2) and y in (1, 2)) else None)  # noqa: E731

    def fuego_casco(cara, x, y, w, h):
        if cara == "abajo":
            return ALMA[2] if (x + y) % 2 == 0 else ALMA[1]
        if cara in ("der", "izq", "frente", "espalda") and y == h - 1:
            return ALMA[3] if x % 2 == 0 else ALMA[2]
        return None

    def visera(cara, x, y, w, h):
        if cara == "frente" and y == 3 and 1 <= x <= w - 2:
            return ALMA[3] if x in (2, 5) else ALMA[1]
        return None

    def filo(cara, x, y, w, h):
        if cara in ("der", "izq") and delante(cara, x, w) >= w - 1:
            return ALMA[1]
        return None

    return {"craneo": por_cara(liso(None), der=ojo_lado, izq=ojo_lado), "crin": llama(5), "copete": llama(9),
            "cola_f": llama(13), "cola_f2": llama(17, densa=0.5), "casco": fuego_casco, "yelmo": visera,
            "penacho": llama(23, densa=0.7), "hoja": filo}


# ---------------------------------------------------------------- las animaciones
# Giros en grados sobre la pose de reposo. Patas del caballo: x NEGATIVO = hacia delante; las de atras doblan al reves.
# Brazos del jinete: con objetivo ("h", x, y, z): la mano respecto al hombro de ese lado (x a SU izquierda, y arriba, z
# delante); _arma = hacia donde apunta la guja (la hoja); el cuerno va en la izquierda.

CABALLO0 = dict(raiz=raiz(0.0), c_torso=(0, 0, 0), c_cuello=(0, 0, 0), c_cabeza=(0, 0, 0), c_mandibula=(0, 0, 0),
                c_cola=(0, 0, 0), c_cola2=(0, 0, 0))

#: el jinete quieto: derecho en la silla, la guja de pie a su derecha (el regaton junto al estribo), las riendas en la izquierda
GUJA_EN_PIE = dict(_mano_d=("h", -1.5, -8.5, 3.5), _arma=(0.04, 1.0, 0.12))
RIENDAS = dict(_mano_i=("h", -3.0, -6.0, 6.0))
JINETE0 = con(dict(torso=(0, 0, 0), cabeza=(0, 0, 0), capa=(0, 0, 0), capa2=(0, 0, 0), penacho=(0, 0, 0)), GUJA_EN_PIE, RIENDAS)
REPOSO = con(CABALLO0, JINETE0)


def _patas(fd, fi, td, ti, lomo=0.0, sube=0.0, cuello=0.0, cabeza=0.0, cola=0.0):
    """Una pose del paso del caballo: fd/fi = las de delante (der/izq), td/ti = las de atras; + = la pata hacia atras."""
    def delante_(a):
        return (a, 0, 0), (max(0.0, -a) * 1.1 + 4, 0, 0), (-max(0.0, -a) * 0.5, 0, 0)

    def atras_(a):
        return (a, 0, 0), (max(0.0, -a) * 0.9 + 3, 0, 0), (-max(0.0, -a) * 0.6, 0, 0)

    bd, ad, md = delante_(fd)
    bi, ai, mi = delante_(fi)
    sd, pd, ped = atras_(td)
    si, pi_, pei = atras_(ti)
    return {"c_brazo_d": bd, "c_antebrazo_d": ad, "c_mano_d": md, "c_brazo_i": bi, "c_antebrazo_i": ai, "c_mano_i": mi,
            "c_muslo_d": sd, "c_pierna_d": pd, "c_pata_d": ped, "c_muslo_i": si, "c_pierna_i": pi_, "c_pata_i": pei,
            "c_torso": (lomo, 0, 0), "c_cuello": (cuello, 0, 0), "c_cabeza": (cabeza, 0, 0), "raiz": raiz(sube),
            "c_cola": (cola, 0, 0), "c_cola2": (cola * 0.6, 0, 0)}


def variante_de(nombre):
    return 0


def reposo(v, por_nombre):
    import anim
    return anim.sumar(por_nombre["quieto"].muestra(0.0), por_nombre["jinete_quieto"].muestra(0.0))


def base_de(nombre, por_nombre):
    """Lo que en el juego va debajo de cada animacion (el caballo y el jinete van en bucles aparte, que se suman; las
    acciones que solo mueven al jinete van encima del bucle del caballo)."""
    if nombre in ("quieto", "andar"):
        return [por_nombre["jinete_quieto"]]
    if nombre == "galope":
        return [por_nombre["jinete_galope"]]
    if nombre == "jinete_quieto":
        return [por_nombre["quieto"]]
    if nombre == "jinete_galope":
        return [por_nombre["galope"]]
    if nombre in ("tajo", "barrido"):
        return [por_nombre["quieto"]]
    if nombre == "carga":
        return [por_nombre["galope"]]
    return []


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

    # ================================================================ bucles del caballo (solo sus piezas)
    # ---- quieto: respira, sacude la cabeza, mueve la cola y a ratos escarba con una mano
    bucle("quieto", 5.0, [
        (0.0, CABALLO0),
        (1.0, con(CABALLO0, c_torso=(-1, 0, 0), c_cuello=(-4, 6, 0), c_cabeza=(2, 6, 4), c_mandibula=(6, 0, 0), c_cola=(-6, 10, 0), c_cola2=(-4, 12, 0))),
        (1.5, con(CABALLO0, c_torso=(-1, 0, 0), c_cuello=(-6, -8, 0), c_cabeza=(-4, -10, -6), c_mandibula=(2, 0, 0), c_cola=(-6, -8, 0), c_cola2=(-4, -10, 0))),
        (2.2, con(CABALLO0, c_cuello=(2, -4, 0), c_cabeza=(4, -2, 0), c_cola=(-2, -4, 0))),
        (3.0, con(CABALLO0, c_cuello=(4, 0, 0), c_cabeza=(6, 0, 0), c_brazo_d=(-30, 0, 0), c_antebrazo_d=(50, 0, 0), c_mano_d=(-10, 0, 0), c_cola=(-4, 6, 0))),
        (3.4, con(CABALLO0, c_cuello=(4, 0, 0), c_cabeza=(6, 0, 0), c_brazo_d=(-8, 0, 0), c_antebrazo_d=(14, 0, 0), c_cola=(-4, 8, 0))),
        (3.8, con(CABALLO0, c_cuello=(4, 0, 0), c_cabeza=(6, 0, 0), c_brazo_d=(-28, 0, 0), c_antebrazo_d=(48, 0, 0), c_mano_d=(-10, 0, 0))),
        (4.3, con(CABALLO0, c_cuello=(2, 2, 0), c_cabeza=(2, 2, 0))),
        (5.0, CABALLO0),
    ])
    # ---- andar (al paso de cuatro tiempos; la cabeza cabecea)
    bucle("andar", 1.4, [
        (0.00, _patas(-24, 16, 14, -20, lomo=0.5, sube=0.0, cuello=4, cabeza=-2, cola=-4)),
        (0.35, _patas(-6, 22, -20, 8, lomo=-0.5, sube=0.4, cuello=-2, cabeza=2, cola=-2)),
        (0.70, _patas(16, -24, -20, 14, lomo=0.5, sube=0.0, cuello=4, cabeza=-2, cola=-4)),
        (1.05, _patas(22, -6, 8, -20, lomo=-0.5, sube=0.4, cuello=-2, cabeza=2, cola=-2)),
        (1.40, _patas(-24, 16, 14, -20, lomo=0.5, sube=0.0, cuello=4, cabeza=-2, cola=-4)),
    ])
    # ---- galope (las de atras empujan juntas, las de delante se estiran; el lomo se encoge y se estira)
    bucle("galope", 0.7, [
        (0.000, con(_patas(-58, -46, 40, 50, lomo=-6, sube=1.6, cuello=8, cabeza=-6, cola=14), c_cadera=(-6, 0, 0))),
        (0.175, con(_patas(-12, -4, -14, -4, lomo=4, sube=3.0, cuello=-4, cabeza=4, cola=24), c_cadera=(4, 0, 0))),
        (0.350, con(_patas(44, 52, -56, -48, lomo=8, sube=0.4, cuello=-10, cabeza=6, cola=30), c_cadera=(10, 0, 0))),
        (0.525, con(_patas(12, 20, 8, 16, lomo=-2, sube=1.0, cuello=2, cabeza=-2, cola=18), c_cadera=(-2, 0, 0))),
        (0.700, con(_patas(-58, -46, 40, 50, lomo=-6, sube=1.6, cuello=8, cabeza=-6, cola=14), c_cadera=(-6, 0, 0))),
    ])
    # ================================================================ bucles del jinete (solo sus piezas)
    # ---- quieto: derecho en la silla, la guja de pie, mira el camino; la capa y el penacho se mecen
    bucle("jinete_quieto", 5.0, [
        (0.00, JINETE0),
        (1.25, con(JINETE0, torso=(-2, 4, 0), cabeza=(2, 14, 0), capa=(4, 0, 2), capa2=(4, 0, 0), penacho=(-6, 4, 0))),
        (2.50, con(JINETE0, torso=(1, -2, 0), cabeza=(-2, -10, 0), capa=(-2, 0, -2), capa2=(6, 0, 0), penacho=(4, -4, 0))),
        (3.75, con(JINETE0, torso=(0, 0, 0), cabeza=(4, 2, 0), capa=(2, 0, 0), capa2=(2, 0, 0), penacho=(-2, 0, 0))),
        (5.00, JINETE0),
    ])
    # ---- al galope: echado hacia delante, la guja inclinada hacia delante, la capa y el penacho al viento
    JG = dict(torso=(16, 0, 0), cabeza=(-14, 0, 0), _mano_d=("h", -2.0, -4.5, 7.6), _arma=(0.05, 0.55, 0.85),
              _mano_i=("h", -3.0, -5.0, 7.2), capa=(46, 0, 0), capa2=(18, 0, 0), penacho=(-24, 0, 0))
    bucle("jinete_galope", 0.7, [
        (0.000, con(JG, capa=(42, 0, 0))),
        (0.175, con(JG, torso=(12, 0, 0), capa=(50, 0, 4), capa2=(24, 0, 0))),
        (0.350, con(JG, torso=(10, 0, 0), capa=(54, 0, 0), penacho=(-30, 0, 0))),
        (0.525, con(JG, capa=(46, 0, -4), capa2=(14, 0, 0))),
        (0.700, con(JG, capa=(42, 0, 0))),
    ])

    # ================================================================ acciones
    # ---- el tajo: alza la guja por encima del hombro derecho y la descarga en diagonal hacia delante y a su derecha
    ALZA = dict(torso=(-6, 32, 0), cabeza=(0, -26, 0), _mano_d=("h", -2.5, 8.0, -3.5), _arma=(-0.25, 0.75, -0.6),
                _mano_i=("h", -2.0, -5.0, 7.5), capa=(10, 0, 0))
    DA = dict(torso=(18, -28, 0), cabeza=(8, 22, 0), _mano_d=("h", -5.0, -4.0, 6.5), _arma=(-0.45, -0.55, 0.7),
              _mano_i=("h", 0.0, -5.0, 7.5), capa=(30, 0, 0))
    accion("tajo", 1.2, [
        (0.00, JINETE0),
        (0.40, ALZA),
        (0.55, con(ALZA, torso=(-8, 36, 0), _mano_d=("h", -2.0, 8.2, -4.0))),
        (0.66, DA),
        (0.85, con(DA, torso=(16, -30, 0), _mano_d=("h", -5.5, -5.0, 5.5))),
        (1.20, JINETE0),
    ], golpes=(0.65,))
    # ---- el barrido: la guja a dos manos da la vuelta entera alrededor (el caballo gira en el sitio: lo hace el servidor)
    B0 = dict(torso=(4, 50, 0), cabeza=(0, -30, 0), _mano_d=("h", -5.0, -2.0, -7.3), _arma=(-0.7, 0.15, -0.7),
              _mano_i=("h", -8.0, -4.0, 2.0), capa=(16, 0, 0))
    B1 = dict(torso=(8, -10, 0), cabeza=(0, 6, 0), _mano_d=("h", -8.0, -2.5, 4.0), _arma=(-0.95, 0.05, 0.3),
              _mano_i=("h", -5.0, -3.5, 6.5), capa=(30, 0, 0))
    B2 = dict(torso=(6, -55, 0), cabeza=(0, 30, 0), _mano_d=("h", 1.5, -3.0, 8.6), _arma=(0.6, 0.0, 0.8),
              _mano_i=("h", 5.5, -4.5, 5.5), capa=(30, 0, -8))
    accion("barrido", 1.7, [
        (0.00, JINETE0),
        (0.55, B0),
        (0.80, con(B0, torso=(4, 56, 0))),
        (0.92, B1),
        (1.05, B2),
        (1.30, con(B2, torso=(4, -46, 0))),
        (1.70, JINETE0),
    ], golpes=(0.9,))
    # ---- la carga: baja la guja como una lanza (la hoja por delante del hocico) y se echa sobre el cuello del caballo;
    # el caballo galopa debajo (el bucle de galope, en el juego); al final la vuelve a alzar
    CARGA = dict(torso=(26, 4, 0), cabeza=(-24, -4, 0), _mano_d=("h", -1.0, -5.5, 5.0), _arma=(0.06, -0.06, 1.0),
                 _mano_i=("h", -3.0, -5.0, 7.0), capa=(46, 0, 0), capa2=(22, 0, 0), penacho=(-30, 0, 0))
    accion("carga", 3.0, [
        (0.00, JINETE0),
        (0.35, con(JINETE0, torso=(-6, 10, 0), _mano_d=("h", -2.0, 4.0, 2.0), _arma=(0.1, 0.9, 0.4), capa=(10, 0, 0))),
        (0.70, CARGA),
        (2.55, con(CARGA, torso=(28, 2, 0))),
        (3.00, JINETE0),
    ])
    # ---- el pisoton: el caballo se alza sobre las patas de atras manoteando y deja caer las de delante con todo su peso
    # (el jinete se echa sobre el cuello y alza la guja)
    ALZADO = dict(raiz=raiz(2.5, 9.0, rot=(-46, 0, 0)), c_cadera=(0, 0, 0), c_torso=(0, 0, 0), c_cuello=(-22, 0, 0), c_cabeza=(-12, 0, 0),
                  c_mandibula=(26, 0, 0), c_brazo_d=(-74, 0, 0), c_antebrazo_d=(104, 0, 0), c_mano_d=(-20, 0, 0),
                  c_brazo_i=(-42, 0, 0), c_antebrazo_i=(84, 0, 0), c_mano_i=(-10, 0, 0),
                  c_muslo_d=(32, 0, 0), c_pierna_d=(10, 0, 0), c_pata_d=(10, 0, 0), c_muslo_i=(28, 0, 0), c_pierna_i=(14, 0, 0), c_pata_i=(8, 0, 0),
                  c_cola=(42, 0, 0), c_cola2=(30, 0, 0),
                  torso=(38, 0, 0), cabeza=(-30, 0, 0), _mano_d=("h", -3.0, 8.4, 2.5), _arma=(-0.1, 0.95, 0.3),
                  _mano_i=("h", -3.0, -5.0, 7.0), capa=(20, 0, 0), capa2=(10, 0, 0))
    CAE = con(CABALLO0, raiz=raiz(-1.6, 0.0, rot=(8, 0, 0)), c_cuello=(16, 0, 0), c_cabeza=(10, 0, 0), c_mandibula=(10, 0, 0),
              c_brazo_d=(-20, 0, 0), c_antebrazo_d=(10, 0, 0), c_brazo_i=(-16, 0, 0), c_antebrazo_i=(8, 0, 0),
              c_muslo_d=(-8, 0, 0), c_muslo_i=(-6, 0, 0), c_cola=(-10, 0, 0),
              torso=(22, 0, 0), cabeza=(6, 0, 0), _mano_d=("h", -4.0, -5.0, 6.5), _arma=(-0.2, -0.5, 0.85),
              _mano_i=("h", -3.0, -5.0, 7.0), capa=(28, 0, 0))
    accion("pisotea", 2.0, [
        (0.00, REPOSO),
        (0.30, con(REPOSO, raiz=raiz(0.2, 1.0, rot=(-8, 0, 0)), c_cuello=(-10, 0, 0), c_cabeza=(-6, 0, 0), torso=(8, 0, 0))),
        (0.65, ALZADO),
        (0.85, con(ALZADO, c_brazo_d=(-44, 0, 0), c_antebrazo_d=(84, 0, 0), c_brazo_i=(-76, 0, 0), c_antebrazo_i=(106, 0, 0), c_mandibula=(32, 0, 0))),
        (1.05, con(ALZADO, raiz=raiz(1.8, 7.0, rot=(-38, 0, 0)), c_brazo_d=(-70, 0, 0), c_antebrazo_d=(100, 0, 0), c_brazo_i=(-46, 0, 0), c_antebrazo_i=(86, 0, 0))),
        (1.20, CAE),
        (1.45, con(CAE, raiz=raiz(-0.8, 0.0, rot=(4, 0, 0)))),
        (2.00, REPOSO),
    ], golpes=(1.2,))
    # ---- el cuerno: se lleva el cuerno a la boca del yelmo (la izquierda), se echa atras y lo toca; la guja en alto
    SOPLA = dict(torso=(-14, -6, 0), cabeza=(-22, 10, 0), _mano_i=("h", -5.0, 5.0, 5.5), _arma_i=(0.41, 0.51, 0.76),
                 _mano_d=("h", -3.0, 4.0, 3.0), _arma=(0.0, 1.0, 0.1), capa=(-4, 0, 0))
    accion("cuerno", 2.4, [
        (0.00, REPOSO),
        (0.45, con(REPOSO, SOPLA, raiz=raiz(0.4, 1.0, rot=(-10, 0, 0)), c_cuello=(-12, 0, 0), c_cabeza=(-8, 0, 0))),
        (0.95, con(REPOSO, SOPLA, raiz=raiz(0.8, 2.0, rot=(-16, 0, 0)), c_cuello=(-18, 0, 0), c_cabeza=(-14, 0, 0), c_mandibula=(20, 0, 0),
                   c_brazo_d=(-50, 0, 0), c_antebrazo_d=(80, 0, 0), torso=(-18, -6, 0), cabeza=(-28, 10, 0))),
        (1.75, con(REPOSO, SOPLA, raiz=raiz(0.6, 1.5, rot=(-12, 0, 0)), c_cuello=(-14, 0, 0), c_cabeza=(-10, 0, 0), c_mandibula=(16, 0, 0),
                   c_brazo_d=(-30, 0, 0), c_antebrazo_d=(60, 0, 0), torso=(-16, -6, 0), cabeza=(-26, 10, 0))),
        (2.40, REPOSO),
    ], golpes=(1.0,))
    # ---- aturdido: el caballo tropieza y cae sobre las rodillas de delante; el jinete se va hacia delante, la guja abajo
    RODILLAS = con(CABALLO0, raiz=raiz(-4.5, -1.0, rot=(16, 0, 0)), c_cuello=(26, 0, 0), c_cabeza=(14, 6, 0), c_mandibula=(12, 0, 0),
                   c_brazo_d=(-6, 0, 0), c_antebrazo_d=(128, 0, 0), c_mano_d=(-40, 0, 0), c_brazo_i=(-10, 0, 0), c_antebrazo_i=(124, 0, 0), c_mano_i=(-40, 0, 0),
                   c_muslo_d=(-14, 0, 0), c_pierna_d=(8, 0, 0), c_muslo_i=(-12, 0, 0), c_pierna_i=(6, 0, 0), c_cola=(-20, 10, 0),
                   torso=(34, 8, 0), cabeza=(14, 10, 0), _mano_d=("h", -4.0, -6.0, 5.5), _arma=(-0.1, -0.85, 0.5),
                   _mano_i=("h", -2.0, -6.0, 6.5), capa=(18, 0, 6))
    accion("aturdido", 3.0, [
        (0.00, REPOSO),
        (0.35, con(RODILLAS, raiz=raiz(-2.0, -0.5, rot=(8, 0, 0)), c_antebrazo_d=(60, 0, 0), c_antebrazo_i=(50, 0, 0), torso=(20, 0, 0))),
        (0.70, RODILLAS),
        (2.10, con(RODILLAS, c_cuello=(30, -10, 0), c_cabeza=(16, -8, 0), torso=(30, -6, 0), cabeza=(10, -14, 0))),
        (2.60, con(REPOSO, raiz=raiz(-1.0, 0.0, rot=(4, 0, 0)), c_cuello=(8, 0, 0), torso=(10, 0, 0))),
        (3.00, REPOSO),
    ])
    # ---- aparecer: se alza de la niebla encabritandose un poco (el dibujante lo va haciendo visible)
    accion("aparece", 1.5, [
        (0.00, con(REPOSO, raiz=raiz(-1.0, 0.0, rot=(6, 0, 0)), c_cuello=(20, 0, 0), c_cabeza=(10, 0, 0), torso=(16, 0, 0), cabeza=(20, 0, 0))),
        (0.50, con(REPOSO, raiz=raiz(1.2, 3.0, rot=(-22, 0, 0)), c_cuello=(-16, 0, 0), c_cabeza=(-10, 0, 0), c_mandibula=(24, 0, 0),
                   c_brazo_d=(-60, 0, 0), c_antebrazo_d=(90, 0, 0), c_brazo_i=(-40, 0, 0), c_antebrazo_i=(76, 0, 0), torso=(20, 0, 0),
                   cabeza=(-10, 0, 0), _mano_d=("h", -3.0, 8.0, 3.0), _arma=(0.0, 1.0, 0.25))),
        (0.95, con(REPOSO, raiz=raiz(-0.4, 0.0, rot=(3, 0, 0)), c_cuello=(6, 0, 0), torso=(6, 0, 0))),
        (1.50, REPOSO),
    ])
    # ---- desaparecer (al amanecer): baja la cabeza, el caballo tambien, y se deshace (el dibujante lo va apagando)
    INCLINA = con(REPOSO, c_cuello=(24, 0, 0), c_cabeza=(16, 0, 0), torso=(14, 0, 0), cabeza=(30, 0, 0),
                  _mano_d=("h", -1.5, -8.3, 3.5), _arma=(0.06, 0.95, 0.3))
    accion("desaparece", 2.0, [
        (0.00, REPOSO),
        (0.70, INCLINA),
        (2.00, con(INCLINA, raiz=raiz(-2.0), c_cuello=(30, 0, 0), cabeza=(34, 0, 0))),
    ])
    # ---- morir: el caballo se alza gritando, cae sobre las rodillas de delante y se tumba de lado; el jinete se dobla
    # sobre el cuello, suelta la guja y cae con el
    TUMBA = con(CABALLO0, raiz=raiz(-12.5, 0.0, rot=(0, 0, 78)), c_cuello=(30, 10, 0), c_cabeza=(-20, 10, 0), c_mandibula=(16, 0, 0),
                c_brazo_d=(-70, 0, 6), c_antebrazo_d=(40, 0, 0), c_brazo_i=(-60, 0, -6), c_antebrazo_i=(30, 0, 0),
                c_muslo_d=(-40, 0, 6), c_pierna_d=(30, 0, 0), c_muslo_i=(-30, 0, -6), c_pierna_i=(20, 0, 0), c_cola=(-30, 20, 0), c_cola2=(-10, 10, 0),
                torso=(30, 0, -20), cabeza=(40, 0, -10), _mano_d=("h", -7.0, -5.0, 3.0), _arma=(-0.9, -0.2, -0.3),
                _mano_i=("h", 3.0, -7.5, 4.5), capa=(20, 0, 10), capa2=(10, 0, 0))
    accion("morir", 4.5, [
        (0.00, REPOSO),
        (0.50, con(ALZADO, raiz=raiz(1.8, 7.0, rot=(-36, 0, 0)), torso=(30, 0, 0), _mano_d=("h", -7.0, 2.0, 5.5), _arma=(-0.5, 0.6, 0.6))),
        (1.10, con(RODILLAS, raiz=raiz(-5.0, -1.0, rot=(18, 0, 0)), torso=(44, 10, 0), cabeza=(30, 0, 0))),
        (1.80, con(RODILLAS, raiz=raiz(-7.0, -1.0, rot=(18, 0, 18)), c_muslo_d=(-40, 0, 0), c_pierna_d=(90, 0, 0), c_muslo_i=(-36, 0, 0),
                   c_pierna_i=(86, 0, 0), torso=(46, 10, -10), cabeza=(36, 0, 0))),
        (2.80, TUMBA),
        (4.50, con(TUMBA, raiz=raiz(-13.0, 0.0, rot=(0, 0, 80)))),
    ])
    return B, A


# lo que ademas de las acciones necesita el servidor
TABLA_EXTRA = ()
