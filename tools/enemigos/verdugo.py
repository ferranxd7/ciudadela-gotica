"""
El Verdugo de la Arboleda (parte 7h): Maese Odran, el que llevaba la cuenta de los ahorcados (el libro perdido 19) y se
quedo con ellos. El jefe del Bosque Muerto: un hombreton muerto y cosido, la capucha negra de verdugo con los ojos
encendidos, el pecho al aire con las costuras, el mandil de cuero manchado, el cinto con la soga enrollada y dos lazos
colgando, los brazales y las botas; el hacha de verdugo a dos manos (asta larga, hoja ancha en media luna con el filo
oxidado y la punta de atras) y en la izquierda el lazo, que lanza.

Una sola variante. Acciones: hachazo (de arriba abajo, se clava), barrido (a media altura: no se salta), doble (dos tajos
en cruz avanzando), lazo (lo lanza y tira), ejecucion (el hachazo rapido tras enganchar a alguien), pisoton (a quien se le
pone detras), grito (al empezar y a media vida: despierta a los colgados), salto (baja del patibulo de un salto),
aturdido, morir, levantarse (estaba de pie apoyado en el hacha).
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (brillo_de, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,  # noqa: F401
                   sombra, tela)

NOMBRE = "verdugo"
VARIANTES = ("verdugo",)
ESCALA = 1.5
CENTRO_Y = 1.9

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # un hombreton: el torso ancho y hondo, los brazos gruesos
    humanoide(r, torso=(10, 14, 6), cabeza=(8, 8, 8), brazo=(5, 15, 5), pierna=(5, 13, 5))
    # la capucha de verdugo (con su pico) y la esclavina sobre los hombros
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8.5, -4, 8, 9, 8, "capucha", grow=g(0.5)),
        Box(-2, -11.0, -1.5, 4, 3, 4, "capucha_pico"),
    ])
    r.pieza("esclavina", "torso", (0, -14, 0), boxes=[Box(-6, -0.5, -3.5, 12, 4, 7, "esclavina", grow=g(0.35))])
    # el mandil de cuero (del pecho a las rodillas, por delante) y el cinto con la soga enrollada y dos lazos
    r.pieza("mandil", "cadera", (0, 0, 0), boxes=[Box(-4.5, -6, -3.6, 9, 13, 0, "mandil")])
    r.pieza("cinto", "cadera", (0, 0, 0), boxes=[
        Box(-5.5, -1.8, -3.5, 11, 2, 7, "cinto", grow=g(0.25)),
        Box(4.8, -1.0, -2.0, 2, 4, 4, "rollo"),
        Box(-6.4, -0.5, -1.0, 1, 5, 2, "lazo_cinto"),
    ])
    # los brazales y las botas
    for lado in ("d", "i"):
        r.pieza(f"brazal_{lado}", f"antebrazo_{lado}", (0, 0, 0), boxes=[Box(-2.5, 1, -2.5, 5, 4, 5, "brazal", grow=g(0.35))])
        r.pieza(f"bota_{lado}", f"pierna_{lado}", (0, 0, 0), boxes=[Box(-2.5, 2.5, -2.5, 5, 4, 5, "bota", grow=g(0.3))])
    # ---- el hacha de verdugo (apunta a -Z con el brazo caido): el asta, la hoja en media luna y el contrapeso
    r.pieza("hacha", "mano_d", (0, 0, 0), boxes=[
        Box(-0.9, -0.9, -36, 1.8, 1.8, 22, "asta"),
        Box(-0.9, -0.9, -14, 1.8, 1.8, 20, "asta"),
        Box(-1.3, -1.3, 5.5, 2.6, 2.6, 2.5, "pomo"),
        Box(-1.2, -1.2, -37.5, 2.4, 2.4, 3, "cabeza_hacha"),
        # la hoja: un plano de canto (en la cara de la izquierda la media luna pintada, hacia abajo; la punta hacia arriba)
        Box(0, -5, -39, 0, 17, 13, "hoja_hacha"),
    ])
    # ---- el lazo de la izquierda (lo lleva colgando; en el lazo se lanza)
    r.pieza("lazo_mano", "mano_i", (0, 0, 0), boxes=[
        Box(-1.5, -1.5, -1.5, 3, 3, 3, "rollo"),
        Box(-0.5, 0, -0.5, 1, 7, 1, "cuerda"),
    ])
    r.pieza("lazo_i", "lazo_mano", (0, 7, 0), boxes=[
        Box(-2.5, 0, -0.5, 1, 5, 1, "cuerda"),
        Box(1.5, 0, -0.5, 1, 5, 1, "cuerda"),
        Box(-2.5, 4, -0.5, 5, 1, 1, "cuerda"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {0: []}

#: lo que va ademas en VerdugoAnims (la escala, para el dibujante y el servidor)
TABLA_EXTRA = (
    "",
    f"    public static final float ESCALA = {ESCALA}f;",
)

# ---------------------------------------------------------------- las texturas

PIEL = ["#262b2b", "#394140", "#4f5957", "#67726f", "#808b87", "#9aa49f"]
COSTURA = "#1a1612"
HILO = "#6e5d44"
CUERO = ["#0b0a0a", "#141212", "#1e1b1a", "#2a2624", "#36312e", "#433d39"]
MANDIL = ["#1c130c", "#291c12", "#382718", "#47331f", "#574028"]
SANGRE = ["#2a0807", "#3e0d0b", "#561410", "#6e1c15"]
CALZON = ["#18171a", "#222125", "#2d2b30", "#38363c", "#44414a"]
SOGA = ["#3e3020", "#56442c", "#6e5838", "#876e46", "#a08656"]
PALO = ["#1e140d", "#2b1d12", "#3a2819", "#4b3421", "#5d412b"]
HIERRO = ["#1b1b1f", "#2e2e34", "#45454d", "#5f5f69", "#7d7d87", "#a0a0aa"]
OXIDO = ["#3b2216", "#5a321c", "#7a4422"]
OJO = ["#5a1806", "#a03a0c", "#e0701a", "#ffd070"]


def _costuras():
    """Las costuras del pecho (de un hombro al vientre, cruzandose) y la cicatriz del costado: {(x, y)} en la cara de delante."""
    puntos = set()
    for y in range(0, 14):
        puntos.add((2 + y // 3, y))
    for y in range(4, 12):
        puntos.add((8 - (y - 4) // 2, y))
    return puntos


def materiales(variante):
    piel = ruido(PIEL[1:5], 23, grano=0.45, base=0.5)
    costuras = _costuras()
    cuero = ruido(CUERO[1:5], 41, grano=0.5, base=0.5)
    mandil_t = ruido(MANDIL[1:5], 43, grano=0.55, base=0.5)
    calzon = tela(CALZON, 45, pliegue=2)

    def manchado(c, cara, x, y, cuanto):
        k = (x * 5 + y * 11 + len(cara) * 3) % 13
        if k < cuanto:
            return mezcla(c, SANGRE[1 + (x + y) % 3], 0.6)
        return c

    def cabeza(cara, x, y, w, h):
        # (bajo la capucha solo asoma el cuello)
        return piel(cara, x, y, w, h)

    def capucha(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara == "frente":
            # los dos agujeros de los ojos (lo de dentro, negro: el brillo los enciende) y la costura del centro
            if y in (3, 4) and x in (1, 2, 5, 6):
                return "#050404"
            if x in (3, 4) and y <= 2:
                return CUERO[0]
            if y >= h - 2:
                return CUERO[3] if (x + y) % 2 else CUERO[2]   # el borde de abajo, gastado
        if cara == "abajo":
            return None
        if (x * 7 + y * 5) % 17 == 0:
            return CUERO[4]                                    # el cuero cuarteado
        return c

    def capucha_pico(cara, x, y, w, h):
        return oscuro(cuero(cara, x, y, w, h), 0.1)

    def esclavina(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = cuero(cara, x, y, w, h)
        if cara == "frente" and 3 <= x <= w - 4 and y >= 1:
            return None                                        # abierta por delante: el pecho al aire
        if y == h - 1 and (x * 3) % 4 == 0:
            return None                                        # los picos del borde
        return c

    def torso(cara, x, y, w, h):
        if cara in ("frente", "espalda", "der", "izq") and y >= h - 3:
            return calzon(cara, x, y, w, h)                    # la cintura del calzon
        c = piel(cara, x, y, w, h)
        if cara == "frente":
            if (x, y) in costuras:
                return COSTURA
            # los puntos del hilo, a los lados de la costura
            if (x - 1, y) in costuras and y % 2 == 0 or (x + 1, y) in costuras and y % 2 == 1:
                return HILO
            if 6 <= x <= 8 and y in (2, 3):
                return PIEL[0]                                 # el hueco de la clavicula
        if cara == "espalda" and (x * 3 + y) % 9 == 0:
            return PIEL[1]                                     # los latigazos viejos
        return c

    def mandil(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return None
        if y >= h - 1 and x % 3 == 1:
            return None
        c = mandil_t(cara, x, y, w, h)
        if y <= 1:
            return MANDIL[0] if x in (1, w - 2) else c         # las correas de arriba
        return manchado(c, cara, x, y, 4 if y > 6 else 2)

    def cinto(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara == "frente" and abs(x - w // 2) <= 1:
            return HIERRO[4] if y == 0 or x == w // 2 else HIERRO[3]   # la hebilla
        return c

    def rollo(cara, x, y, w, h):
        return SOGA[(x + y * 2 + len(cara)) % 4 + 1] if (x + y) % 3 else SOGA[1]

    def cuerda(cara, x, y, w, h):
        return SOGA[2 + (y % 2)]

    def brazo(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if cara == "frente" and (x + y * 2) % 11 == 0:
            return PIEL[1]
        return c

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return PIEL[2] if cara != "abajo" else PIEL[1]
        return piel(cara, x, y, w, h)

    def brazal(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if y in (0, h - 1):
            return CUERO[4]
        if cara in ("frente", "espalda") and x == w // 2 and y == h // 2:
            return HIERRO[4]                                   # el remache
        return c

    def muslo(cara, x, y, w, h):
        return calzon(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        return calzon(cara, x, y + 4, w, h)

    def bota(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if y == 0:
            return CUERO[4]
        if cara == "abajo":
            return CUERO[0]
        return oscuro(c, 0.05 * y)

    C = {"H": HIERRO[5], "h": HIERRO[4], "d": HIERRO[3], "o": HIERRO[2], "x": OXIDO[1], "X": OXIDO[0], "R": SANGRE[2], "r": SANGRE[1],
         "P": HIERRO[1]}
    # la hoja de canto (13 de ancho = a lo largo del asta, el frente de la hoja a la izquierda; 17 de alto: arriba la punta de
    # atras, en medio el ojo del asta, abajo la media luna con el filo oxidado y ensangrentado)
    hoja = dibujo([
        "....oo.......",
        "...odho......",
        "..odhho......",
        "..odhho......",
        "...ohho......",
        "PPPPPPPPPPPPP",
        "PPPPPPPPPPPPP",
        "oddhhddo.....",
        "oxdddhhdo....",
        "oXxdddhhdo...",
        "hXxxddddhho..",
        "HhxXddddddho.",
        "HhhxxddddddhH",
        "RHhhxXdddddHH",
        "rRHhhxxxdhHHR",
        ".rRRHHhhhHHRr",
        "...rrRRRRRr..",
    ], C)

    mats = {
        "cabeza": cabeza,
        "capucha": sombra(capucha, abajo=0.12),
        "capucha_pico": capucha_pico,
        "esclavina": sombra(esclavina),
        "torso": sombra(torso),
        "mandil": mandil,
        "cinto": cinto,
        "rollo": rollo,
        "lazo_cinto": cuerda,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.15), "antebrazo_i": sombra(antebrazo, abajo=0.15),
        "brazal": sombra(brazal),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.15), "pierna_i": sombra(pierna, abajo=0.15),
        "bota": sombra(bota, abajo=0.2),
        "asta": madera(PALO, 81, "z"),
        "pomo": metal(HIERRO[1:4], 82),
        "cabeza_hacha": metal(HIERRO[1:5], 83),
        "hoja_hacha": por_cara(liso(None), izq=hoja, der=espejado(hoja)),
        "cuerda": cuerda,
    }
    return mats


def brillo(variante):
    """Lo que brilla: los ojos por los agujeros de la capucha."""
    C = {"R": OJO[2], "r": OJO[1], "Y": OJO[3]}
    ojos = dibujo([
        "........",
        "........",
        "........",
        ".rY..Yr.",
        ".rr..rr.",
    ], C)
    return {"capucha": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 15)

# de pie: los hombros caidos, la cabeza algo baja, las piernas abiertas
BASE = dict(raiz=raiz(-0.3), torso=(8, 0, 0), cabeza=(6, 0, 0), muslo_d=(-4, 0, 6), pierna_d=(6, 0, 0), muslo_i=(2, 0, -6), pierna_i=(4, 0, 0),
            brazo_i=(-6, 0, -10), antebrazo_i=(-14, 0, 0), mandil=(4, 0, 0))
# el hacha al hombro derecho, la hoja por detras
PORTE = dict(_mano_d=("h", -1.0, 1.0, 5.0), _arma=(0.05, 0.6, -0.8))
GUARDIA = dict(raiz=raiz(-1.4), torso=(14, 18, 0), cabeza=(-6, -16, 0), mandil=(10, 0, 0),
               muslo_d=(10, 0, 8), pierna_d=(18, 0, 0), muslo_i=(-22, 0, -8), pierna_i=(26, 0, 0))
# en guardia: el hacha en diagonal delante, la hoja arriba a su izquierda, las dos manos en el asta
LISTO = con(GUARDIA, _mano_d=("h", 3.0, -9.0, 4.0), _arma=(0.55, 0.65, 0.5), _agarra=-4)
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

    # ---- quieto: respira hondo, el pecho sube y baja
    bucle("quieto", 4.6, [
        (0.0, BASE),
        (2.3, con(BASE, torso=(4, 0, 0), cabeza=(2, 4, 0), raiz=raiz(-0.1), brazo_i=(-4, 0, -12))),
        (4.6, BASE),
    ])
    # ---- andar: pasos largos y pesados
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-28, 22, 8, 24, 7, 0.0), (-6, 4, 6, 44, 0, 1.0), (22, -28, 24, 8, -7, 0.0), (4, -6, 44, 6, 0, 1.0), (-28, 22, 8, 24, 7, 0.0))):
        pasos.append((1.6 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, -ty * 0.4), "cabeza": (4, -ty * 0.6, 0), "raiz": raiz(up),
                                    "mandil": (-md * 0.4, 0, 0), "brazo_i": (-mi * 0.5, 0, -10)}))
    bucle("andar", 1.6, pasos)
    q = con(BASE, PORTE)
    bucle("porte_verdugo", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    bucle("listo_verdugo", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-1.9), torso=(LISTO["torso"][0] + 4, LISTO["torso"][1] - 3, 0), cabeza=(-9, -14, 2))),
        (2.0, LISTO),
    ])
    # ---- esperando en el patibulo: de pie, apoyado en el hacha (la hoja en el suelo delante, las dos manos en el pomo)
    APOYA = con(BASE, torso=(16, 0, 0), cabeza=(18, 0, 0), raiz=raiz(-0.5),
                _mano_d=("h", 3.5, -3.0, 9.5), _arma=(0.05, -1, 0.15), _agarra=2.5)
    bucle("apoyado", 4.0, [
        (0.0, APOYA),
        (2.0, con(APOYA, torso=(19, 2, 0), cabeza=(24, 4, 0), raiz=raiz(-0.7))),
        (4.0, APOYA),
    ])

    # ================================================================ acciones
    G = LISTO

    # ---- 1 hachazo: alza el hacha por encima de la cabeza, la sostiene y la clava delante
    H_ALTO = con(sin(G, "_agarra"), raiz=raiz(0.0, 1.8), torso=(-14, 0, 0), cabeza=(-22, 0, 0), muslo_d=(12, 0, 6), pierna_d=(10, 0, 0),
                 muslo_i=(-14, 0, -6), pierna_i=(10, 0, 0),
                 _mano_d=("h", 3.0, 13.0, -2.0), _arma=(0.0, 0.55, -0.85), _agarra=3)
    H_ABAJO = con(sin(G, "_agarra"), raiz=raiz(-4.2, -3.6), torso=(52, 0, 0), cabeza=(-30, 0, 0), muslo_d=(20, 0, 6), pierna_d=(36, 0, 0),
                  muslo_i=(-58, 0, -6), pierna_i=(60, 0, 0), mandil=(-20, 0, 0),
                  _mano_d=("h", 3.0, -6.0, 11.0), _arma=(0.0, -0.8, 0.6), _agarra=3)
    accion("hachazo", 2.3, [
        (0.00, G),
        (0.70, H_ALTO),
        (0.98, con(H_ALTO, raiz=raiz(0.2, 2.0), torso=(-18, 0, 0), cabeza=(-24, 0, 0))),
        (1.06, con(G, raiz=raiz(-1.6, -1.0), torso=(18, 0, 0), cabeza=(-14, 0, 0), _mano_d=("h", 3.0, 6.0, 11.0), _arma=(0.0, 0.3, 1),
                   _agarra=3)),
        (1.14, H_ABAJO),
        (1.70, con(H_ABAJO, torso=(50, 0, 0))),
        (2.30, G),
    ], golpes=(1.13,))
    # ---- 2 barrido: lo echa atras a su derecha y barre a media altura de derecha a izquierda
    B_ATRAS = con(sin(G, "_agarra"), raiz=raiz(-1.0, 1.4), torso=(8, 62, 0), cabeza=(-6, -52, 0), muslo_d=(14, 0, 6), pierna_d=(20, 0, 0),
                  brazo_i=(-60, 0, -30), antebrazo_i=(-30, 0, 0), _mano_d=("h", -6.0, -2.0, -4.0), _arma=(-1, 0.1, -0.3))
    B_DELANTE = con(sin(G, "_agarra"), raiz=raiz(-2.0, -1.6), torso=(18, -6, 0), cabeza=(-14, 4, 0), muslo_i=(-30, 0, -6), pierna_i=(30, 0, 0),
                    brazo_i=(-40, 0, -50), antebrazo_i=(-20, 0, 0), _mano_d=("h", 2.0, -1.0, 12.5), _arma=(0.1, 0.0, 1))
    B_FIN = con(sin(G, "_agarra"), raiz=raiz(-1.8, -2.2), torso=(16, -62, 0), cabeza=(-10, 50, 0), muslo_i=(-34, 0, -6), pierna_i=(34, 0, 0),
                brazo_i=(-20, 0, -60), antebrazo_i=(-20, 0, 0), _mano_d=("h", 9.0, -1.0, 7.0), _arma=(1, 0.0, 0.2))
    accion("barrido", 1.9, [
        (0.00, G),
        (0.62, B_ATRAS),
        (0.88, con(B_ATRAS, torso=(8, 68, 0), cabeza=(-6, -56, 0))),
        (0.97, B_DELANTE),
        (1.06, B_FIN),
        (1.40, con(B_FIN, torso=(14, -66, 0))),
        (1.90, G),
    ], golpes=(0.99,))
    # ---- 3 doble: dos tajos en cruz avanzando (de arriba a su derecha y luego de arriba a su izquierda)
    D1_ALTO = con(sin(G, "_agarra"), raiz=raiz(-0.4, 1.0), torso=(-4, 30, 0), cabeza=(-18, -24, 0),
                  brazo_i=(-50, 0, -30), antebrazo_i=(-30, 0, 0), _mano_d=("h", -3.0, 11.0, -1.0), _arma=(-0.4, 0.6, -0.6))
    D1_BAJO = con(sin(G, "_agarra"), raiz=raiz(-3.0, -2.0), torso=(40, -24, 0), cabeza=(-24, 18, 0), muslo_i=(-40, 0, -6), pierna_i=(44, 0, 0),
                  brazo_i=(-30, 0, -40), antebrazo_i=(-30, 0, 0), _mano_d=("h", 7.0, -6.0, 9.0), _arma=(0.6, -0.7, 0.4))
    D2_ALTO = con(sin(G, "_agarra"), raiz=raiz(-0.6, 1.2), torso=(-4, -30, 0), cabeza=(-18, 24, 0), muslo_d=(-36, 0, 6), pierna_d=(40, 0, 0),
                  _mano_d=("h", 9.0, 11.0, 0.0), _arma=(0.5, 0.6, -0.6), _agarra=3)
    D2_BAJO = con(sin(G, "_agarra"), raiz=raiz(-3.2, -2.4), torso=(42, 26, 0), cabeza=(-24, -20, 0), muslo_d=(-44, 0, 6), pierna_d=(48, 0, 0),
                  _mano_d=("h", 3.0, -6.0, 10.0), _arma=(-0.6, -0.7, 0.4), _agarra=3)
    accion("doble", 2.4, [
        (0.00, G),
        (0.55, D1_ALTO),
        (0.80, D1_BAJO),
        (1.15, con(D1_BAJO, torso=(36, -20, 0))),
        (1.40, D2_ALTO),
        (1.62, D2_BAJO),
        (1.95, con(D2_BAJO, torso=(38, 22, 0))),
        (2.40, G),
    ], golpes=(0.79, 1.61))
    # ---- 4 lazo: echa atras la izquierda con el lazo, lo lanza (el brazo entero delante) y tira con las dos manos
    LZ_ATRAS = con(G, raiz=raiz(-1.0), torso=(10, -36, 0), cabeza=(-14, 26, 0), brazo_i=(-120, 0, -40), antebrazo_i=(-50, 0, 0))
    LZ_LANZA = con(G, raiz=raiz(-2.2, -1.4), torso=(30, 18, 0), cabeza=(-22, -10, 0), brazo_i=(-96, 0, 4), antebrazo_i=(0, 0, 0),
                   muslo_i=(-34, 0, -6), pierna_i=(36, 0, 0))
    LZ_TIRA = con(G, raiz=raiz(-1.2, 1.2), torso=(-6, -20, 0), cabeza=(-10, 10, 0), brazo_i=(-40, 0, -40), antebrazo_i=(-90, 0, 0),
                  muslo_d=(16, 0, 6), pierna_d=(24, 0, 0))
    accion("lazo", 2.1, [
        (0.00, G),
        (0.60, LZ_ATRAS),
        (0.88, LZ_LANZA),
        (1.15, LZ_LANZA),
        (1.45, LZ_TIRA),
        (1.75, con(LZ_TIRA, torso=(-10, -24, 0))),
        (2.10, G),
    ], golpes=(0.90,))
    # ---- 5 ejecucion: tras enganchar a alguien, el hachazo rapido (casi sin alzarlo)
    accion("ejecucion", 1.6, [
        (0.00, G),
        (0.40, con(H_ALTO, raiz=raiz(-0.2, 1.4), torso=(-10, 0, 0))),
        (0.56, con(G, raiz=raiz(-1.6, -1.0), torso=(18, 0, 0), _mano_d=("h", 3.0, 6.0, 11.0), _arma=(0.0, 0.3, 1), _agarra=3)),
        (0.64, H_ABAJO),
        (1.05, con(H_ABAJO, torso=(48, 0, 0))),
        (1.60, G),
    ], golpes=(0.63,))
    # ---- 6 pisoton: se gira de golpe a quien tiene detras y le pisa
    P_ALZA = con(sin(G, "_agarra"), raiz=raiz(-0.4, 0.0, rot=(0, 70, 0)), torso=(6, 30, 0), muslo_d=(-76, 0, 10), pierna_d=(84, 0, 0),
                 brazo_i=(-30, 0, -40), antebrazo_i=(-30, 0, 0))
    P_PISA = con(sin(G, "_agarra"), raiz=raiz(-2.4, -0.6, rot=(0, 165, 0)), torso=(30, 20, 0), muslo_d=(10, 0, 8), pierna_d=(16, 0, 0),
                 muslo_i=(-32, 0, -6), pierna_i=(42, 0, 0), brazo_i=(-20, 0, -50), antebrazo_i=(-20, 0, 0))
    accion("pisoton", 1.3, [
        (0.00, G),
        (0.38, P_ALZA),
        (0.58, P_PISA),
        (0.92, con(P_PISA, raiz=raiz(-2.2, -0.6, rot=(0, 175, 0)))),
        (1.30, G),
    ], golpes=(0.59,))
    # ---- 7 grito: alza el hacha con la derecha, abre la izquierda y ruge (despierta a los colgados)
    GR = con(BASE, raiz=raiz(-0.4, 1.0), torso=(-16, 0, 0), cabeza=(-40, 0, 0), brazo_i=(-30, 0, -74), antebrazo_i=(-20, 0, 0),
             _mano_d=("h", -2.0, 14.0, 1.0), _arma=(-0.2, 1, 0.2))
    accion("grito", 2.4, [
        (0.00, G),
        (0.40, con(GR, torso=(28, 0, 0), cabeza=(10, 0, 0), raiz=raiz(-2.2))),
        (0.85, GR),
        (1.30, con(GR, torso=(-20, 0, 4), cabeza=(-44, 0, 6))),
        (1.80, con(GR, torso=(-14, 0, -4), cabeza=(-38, 0, -6))),
        (2.40, G),
    ])
    # ---- 8 salto: se agacha, salta (baja del patibulo; lo mueve el servidor) y cae con el hacha por delante
    S_AGACHA = con(G, raiz=raiz(-4.4), torso=(40, 0, 0), cabeza=(-24, 0, 0), muslo_d=(-70, 0, 8), pierna_d=(100, 0, 0),
                   muslo_i=(-64, 0, -8), pierna_i=(96, 0, 0))
    S_VUELA = con(sin(G, "_agarra"), raiz=raiz(1.0), torso=(-6, 0, 0), cabeza=(-20, 0, 0), muslo_d=(-20, 0, 8), pierna_d=(40, 0, 0),
                  muslo_i=(10, 0, -8), pierna_i=(30, 0, 0), _mano_d=("h", 3.0, 12.0, -1.0), _arma=(0.0, 0.6, -0.8), _agarra=3)
    accion("salto", 1.7, [
        (0.00, G),
        (0.40, S_AGACHA),
        (0.58, S_VUELA),
        (0.98, S_VUELA),
        (1.08, H_ABAJO),
        (1.40, con(H_ABAJO, torso=(48, 0, 0))),
        (1.70, G),
    ], golpes=(1.07,))
    # ---- 9 aturdido
    AT = dict(raiz=raiz(-1.6, 2.2), torso=(-10, 0, 8), cabeza=(-22, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(18, 0, 0), brazo_d=(16, 0, 26), antebrazo_d=(-20, 0, 0), mano_d=(60, 0, 0), brazo_i=(18, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.8))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.4))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 10 morir: cae de rodillas apoyandose en el hacha y se desploma de lado
    RODILLAS = dict(raiz=raiz(-9.0, 0.8), muslo_d=(-10, 0, 6), pierna_d=(100, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(98, 0, 0),
                    torso=(10, 0, 4), cabeza=(30, 0, 8), mandil=(-20, 0, 0),
                    _mano_d=("h", 3.0, -5.0, 9.0), _arma=(0.05, -1, 0.15), brazo_i=(8, 0, -30), antebrazo_i=(-12, 0, 0))
    CAIDA = dict(raiz=raiz(-8.0, 6.0, rot=(60, 0, 40)), torso=(10, 0, 0), cabeza=(-10, 20, 0), muslo_d=(-40, 0, 6), pierna_d=(80, 0, 0),
                 muslo_i=(-36, 0, -6), pierna_i=(80, 0, 0), mandil=(-20, 0, 0),
                 _mano_d=("h", 6.0, -3.0, 9.0), _arma=(0.8, -0.2, 0.5), brazo_i=(-120, 0, -30), antebrazo_i=(-10, 0, 0))
    accion("morir", 3.0, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), brazo_i=(10, 0, -30), _mano_d=("h", 4.0, -9.0, 5.0), _arma=(0.2, -1, 0.2))),
        (0.70, RODILLAS),
        (1.60, con(RODILLAS, raiz=raiz(-9.2, 0.4), torso=(18, 0, 2), cabeza=(36, 0, 6))),
        (2.30, CAIDA),
        (3.00, CAIDA),
    ])
    # ---- 11 levantarse: estaba apoyado en el hacha; la alza y se pone en guardia
    accion("levantarse", 1.4, [
        (0.00, APOYA),
        (0.55, con(APOYA, torso=(4, 10, 0), cabeza=(-6, 8, 0), _mano_d=("h", 3.0, -1.0, 8.0), _arma=(0.2, 0.3, 0.9), _agarra=-5)),
        (1.40, G),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_verdugo"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_verdugo"]]
    if nombre == "quieto":
        return [anims["porte_verdugo"]]
    return []
