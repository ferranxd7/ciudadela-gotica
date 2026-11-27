"""
Aldric, el Primer Marcado (parte 7v): el jefe del Crater de Sangre. El primero al que la Sombra saco de la muerte y puso su
marca para que rompiera sus cadenas (el libro 30, "El diario del primero"): golpeo la cadena hasta que vinieron los demas,
subio por ella hasta ver la ciudadela de cerca ("alli arriba no hay puerta: hay una boca que espera"), bajo, y juro no dejar
subir a nadie que lleve la marca. Espera de rodillas en lo alto del ancla del Cerco, donde empieza la cadena. El juego lo
dibuja a 1,42: un viejo enorme y consumido.

La piel gris y agrietada de los marcados, con las grietas rojas que le laten; el pelo y la barba largos y cenicientos, sucios;
la cara chupada, las cuencas hundidas con la luz roja. Lo que queda de su armadura de caballero: la coraza negra abollada, con
el agujero quemado en el pecho por donde asoma la marca (grande, la del principio: el ojo en el aro roto con tres espinas, que
luce), la hombrera izquierda de placas con su pua y el guantelete izquierdo; el collar de hierro de un grillete roto con un
eslabon colgando; la capa roja hecha jirones y quemada; la malla del faldon y las grebas. El brazo derecho, desnudo, con la
cadena enrollada del codo a la muneca; de la mano le cuelga un trozo de la cadena de la Sombra (cuatro tramos de eslabones) que
acaba en el eslabon roto, abierto como un garfio: su arma.

Sus golpes: el cadenazo (la cadena de lado a lado), el latigazo (la alza por detras de la cabeza y la restalla delante), la
estrella (la alza al cielo y la estrella delante: la onda), el torbellino (la voltea alrededor de el, dos vueltas), el salto (se
agacha, salta y cae sobre su presa con la cadena), encadenar (a media vida: la alza y golpea el suelo con el puno; bajo los pies
de cada uno brotan cadenas que sujetan), el rugido del principio (los marcados del cerco se arrodillan ante el) y la marca (a
media vida: se le enciende y su sombra le repite los golpes).

La cadena se anima con su propia cuenta (`_cadena` en las poses, resuelta despues de los brazos: `posproceso`): colgando con
su peso (y tendida en el suelo lo que no cabe), en linea hacia donde se diga, una lista de direcciones (una por tramo) o los
angulos de cada tramo respecto al anterior.
"""
import math

import numpy as np

import vista
from anim import Anim, con, raiz, resolver
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "aldric"
VARIANTES = ("aldric",)
OCULTAS = {0: []}
#: un viejo enorme: lo que le da su dibujante en el juego
ESCALA = 1.42
CENTRO_Y = 1.6

#: los cuatro tramos de la cadena (de la mano al eslabon roto) y lo que mide cada uno; con el eslabon roto, lo que se mueve
CADENA = ("c1", "c2", "c3", "c4")
L_SEG = 6.0
SEGMENTOS = CADENA + ("eslabon",)
LARGOS = (L_SEG,) * len(CADENA) + (10.6,)
#: a que altura (pixeles) se queda la cadena tendida en el suelo
SUELO_CADENA = 0.9

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # grande y huesudo: el torso ancho, los brazos largos, las piernas largas (la mano llega a unos 11 de su hombro)
    humanoide(r, torso=(9, 13, 5), cabeza=(8, 8, 8), brazo=(4, 14, 4), pierna=(5, 13, 5))
    # ---- el pelo (una cascara sobre la cabeza, como el sombrero de las pieles) y la melena que le cae por la espalda
    r.pieza("pelo", "cabeza", (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, "pelo", grow=g(0.45))])
    r.pieza("melena", "cabeza", (0, -2.2, 3.9), rot=(10, 0, 0), boxes=[Box(-4.2, 0, 0, 8.4, 8.6, 1.0, "melena")])
    # ---- la barba, larga hasta el pecho
    r.pieza("barba", "cabeza", (0, -1.4, -4.1), rot=(-7, 0, 0), boxes=[Box(-3.3, 0, -1.0, 6.6, 7.6, 1.1, "barba")])
    # ---- el collar del grillete roto con su eslabon colgando por delante
    r.pieza("collar", "torso", (0, 0, 0), boxes=[
        Box(-4.8, -14.3, -2.9, 9.6, 2.0, 5.8, "collar"),
        Box(-0.6, -12.5, -3.5, 1.2, 3.0, 0.7, "eslabon_collar"),
    ])
    # ---- lo que queda de la armadura: la coraza (con el agujero de la marca), la hombrera izquierda con su pua, el
    # guantelete izquierdo, la malla del faldon y las grebas
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-4.5, -13, -2.5, 9, 13, 5, "coraza", grow=g(0.5))])
    r.pieza("hombrera", "brazo_i", (0, 0, 0), boxes=[
        Box(-2.6, -3.7, -3.1, 6.0, 4.2, 6.2, "hombrera"),
        Box(-2.8, -0.7, -3.3, 6.4, 2.2, 6.6, "hombrera_b"),
        Box(0.4, -6.5, -0.6, 1.2, 2.8, 1.2, "pua"),
    ])
    r.pieza("brazal", "antebrazo_i", (0, 0, 0), boxes=[Box(-2, 0.4, -2, 4, 6.6, 4, "brazal", grow=g(0.35))])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4.5, -0.6, -2.5, 9, 7.2, 5, "faldon", grow=g(0.42))])
    for n in ("d", "i"):
        r.pieza(f"greba_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-2.5, 0.2, -2.5, 5, 6.3, 5, "greba", grow=g(0.32), uv="greba")])
    # ---- la capa roja a jirones, de los hombros a las pantorrillas
    r.pieza("capa", "torso", (0, -12.4, 2.9), rot=(6, 0, 0), boxes=[Box(-4.8, 0, 0, 9.6, 23.0, 0.7, "capa")])
    # ---- el brazo derecho desnudo con la cadena enrollada (tres vueltas en el antebrazo y una en el brazo)
    r.pieza("vueltas", "antebrazo_d", (0, 0, 0), boxes=[
        Box(-2, 1.2, -2, 4, 1.0, 4, "vuelta", grow=g(0.28)),
        Box(-2, 4.0, -2, 4, 1.0, 4, "vuelta", grow=g(0.28), uv="vuelta"),
    ])
    r.pieza("vuelta_alta", "brazo_d", (0, 0, 0), boxes=[Box(-2, 2.6, -2, 4, 1.0, 4, "vuelta", grow=g(0.28), uv="vuelta")])
    # ---- la cadena: cuatro tramos (un eslabon tumbado y otro de pie cada uno) y el eslabon roto, abierto como un garfio
    madre = "mano_d"
    for k, n in enumerate(CADENA):
        r.pieza(n, madre, (0, 0, 0 if k == 0 else -L_SEG), boxes=[
            Box(-1.1, -0.45, -3.3, 2.2, 0.9, 3.6, "esl_plano", uv="esl_plano"),
            Box(-0.45, -1.1, -6.3, 0.9, 2.2, 3.6, "esl_pie", uv="esl_pie"),
        ])
        madre = n
    r.pieza("eslabon", "c4", (0, 0, -L_SEG), boxes=[
        Box(-3.0, -0.8, -10.6, 1.6, 1.6, 10.6, "roto_lado"),
        Box(1.4, -0.8, -10.6, 1.6, 1.6, 6.2, "roto_lado_b"),
        Box(-1.4, -0.8, -10.6, 2.8, 1.6, 1.6, "roto_punta"),
        Box(-1.4, -0.8, -1.6, 1.6, 1.6, 1.6, "roto_cuello"),
    ])
    r.empaquetar()
    r.posproceso = _posproceso(r)
    r.posproceso_clave = "_cadena"
    r.posproceso_piezas = SEGMENTOS
    r.posproceso_mezcla = lambda a, b, d: b if a is None else (a if b is None else ("mezcla", a, b, d))
    return r


# ---------------------------------------------------------------- la cadena: de direcciones a angulos

def _norm(v):
    n = math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) or 1.0
    return (v[0] / n, v[1] / n, v[2] / n)


def _a_modelo(v):
    """Una direccion de persona (x a su izquierda, y arriba, z delante) en ejes del modelo (y abajo, el frente a -z)."""
    x, y, z = _norm(v)
    return np.array([x, -y, -z])


def _de_modelo(m):
    return _norm((float(m[0]), float(-m[1]), float(-m[2])))


def _mezcla_dir(a, b, d):
    return _norm(tuple(a[k] + (b[k] - a[k]) * d for k in range(3)))


def _local(R, d_modelo, antes):
    """Los angulos (x, y) de un tramo para que apunte a d (en ejes del modelo) colgando de una pieza con giro R. La
    rotacion de las piezas es Z-Y-X: el -Z del tramo queda en (-sin y cos x, sin x, -cos y cos x) de su madre."""
    v = R.T @ d_modelo
    a = math.asin(max(-1.0, min(1.0, float(v[1]))))
    if abs(math.cos(a)) < 1e-4:
        b = math.radians(antes[1]) if antes is not None else 0.0
    else:
        b = math.atan2(-float(v[0]), -float(v[2]))
    ad, bd = math.degrees(a), math.degrees(b)
    if antes is not None:
        bd += 360.0 * round((antes[1] - bd) / 360.0)
    return ad, bd


def _rot(a_deg, b_deg):
    return vista.ry(math.radians(b_deg))[:3, :3] @ vista.rx(math.radians(a_deg))[:3, :3]


def _dirs(spec, mats):
    """Las direcciones (de persona) de los cuatro tramos y del eslabon roto para la pose de esas matrices."""
    n = len(SEGMENTOS)
    tipo = spec[0]
    if tipo == "mezcla":
        _, a, b, d = spec
        da, db = _dirs(a, mats), _dirs(b, mats)
        return [_mezcla_dir(da[k], db[k], d) for k in range(n)]
    if tipo == "lista":
        v = [_norm(x) for x in spec[1]]
        return (v + [v[-1]] * n)[:n]
    if tipo == "dir":
        return [_norm(spec[1])] * n
    if tipo == "local":
        R = mats["mano_d"][:3, :3]
        out = []
        for ang in list(spec[1]) + [(0, 0, 0)]:
            R = R @ _rot(ang[0], ang[1])
            out.append(_de_modelo(R @ np.array([0.0, 0.0, -1.0])))
        return out[:n]
    if tipo == "colgar":
        # cuelga con su peso desde la mano; lo que llega al suelo se tiende hacia donde se diga
        sx, _, sz = spec[1] if len(spec) > 1 else (0.0, 0.0, 1.0)
        h = math.hypot(sx, sz) or 1.0
        sx, sz = sx / h, sz / h
        y = 24.0 - float(mats["mano_d"][1, 3]) * 16.0
        out = []
        for largo in LARGOS:
            if y - largo > SUELO_CADENA:
                out.append((0.0, -1.0, 0.0))
                y -= largo
            else:
                baja = max(0.0, y - SUELO_CADENA)
                lado = math.sqrt(max(0.0, largo * largo - baja * baja))
                out.append(_norm((sx * lado, -baja, sz * lado)) if lado > 1e-3 else (0.0, -1.0, 0.0))
                y -= baja
        return out
    raise SystemExit(f"cadena: no se que es {tipo}")


def _sin_suelo(dirs, y):
    """Que ningun tramo se meta en el suelo: el que bajaria de mas se tiende (por donde iba) a ras de suelo."""
    out = []
    for d, largo in zip(dirs, LARGOS):
        if y + d[1] * largo < SUELO_CADENA:
            baja = max(0.0, y - SUELO_CADENA)
            lado = math.sqrt(max(0.0, largo * largo - baja * baja))
            h = math.hypot(d[0], d[2])
            hx, hz = (d[0] / h, d[2] / h) if h > 1e-3 else (0.0, 1.0)
            d = _norm((hx * lado, -baja, hz * lado))
        out.append(d)
        y += d[1] * largo
    return out


def _posproceso(r):
    def post(q, spec, prev):
        s = spec.get("_cadena")
        if s is None:
            return q
        q = dict(q)
        if s[0] == "local":
            for k, n in enumerate(CADENA):
                q[n] = tuple(float(x) for x in s[1][k])
            q["eslabon"] = (0.0, 0.0, 0.0)
            return q
        base = {k: v for k, v in q.items() if k not in SEGMENTOS}
        mats = vista.matrices(r, base)
        dirs = _sin_suelo(_dirs(s, mats), 24.0 - float(mats["mano_d"][1, 3]) * 16.0)
        R = mats["mano_d"][:3, :3]
        for k, n in enumerate(SEGMENTOS):
            pv = (prev or {}).get(n)
            antes = pv if isinstance(pv, tuple) else (pv.get("rot") if isinstance(pv, dict) else None)
            a, b = _local(R, _a_modelo(dirs[k]), antes)
            q[n] = (round(a, 2), round(b, 2), 0.0)
            R = R @ _rot(a, b)
        return q
    return post


# ---------------------------------------------------------------- las texturas

PIEL = ["#2a292f", "#38373e", "#47464e", "#58575f", "#6a6971", "#7d7c84"]
GRIETA = ["#3a0706", "#5c0d0b", "#82150f", "#a82214", "#d0331c"]
CANA = ["#4f4b46", "#68635c", "#837d75", "#9d978e", "#b7b1a7", "#cdc8bf"]
HIERRO = ["#141417", "#1d1d22", "#29292f", "#36363e", "#46464f", "#5a5a64"]
OXIDO = ["#3a1a0e", "#552612", "#6e3416"]
ROJO = ["#220608", "#33090c", "#470d11", "#5e1217", "#74181d"]
MALLA = ["#1b1b1f", "#2b2b31", "#3d3d45", "#555560"]
TELA = ["#121014", "#1a171c", "#231f26", "#2d2830"]
CUENCA = "#050404"
OJO, OJO_C = "#ff4a24", "#ffb27a"
MARCA, MARCA_C = "#6e0c0a", "#a5170f"
BORDE = ["#ff6a1a", "#c2300c", "#6e1408"]

#: la marca grande del pecho (la del principio): E espina, A aro, O ojo, P pupila; 9 x 9
MARCA_9 = [
    "E...E...E",
    ".E..E..E.",
    "..E.E.E..",
    ".AAAAAAA.",
    "A..OOO..A",
    "A.OOPOO.A",
    "A..OOO..A",
    ".A.....A.",
    "..AAA.AA.",
]


def _marca(x0, y0, x, y):
    i, j = y - y0, x - x0
    if 0 <= i < 9 and 0 <= j < 9:
        ch = MARCA_9[i][j]
        return None if ch == "." else ch
    return None


def _agujero(x, y):
    """El agujero quemado de la coraza (centrado en la marca): 0 dentro, 1 el borde al rojo, 2 el borde negro, None fuera."""
    cx, cy = 4.0, 6.0
    d = ((x - cx) / 4.6) ** 2 + ((y - cy) / 5.2) ** 2
    rugo = ((x * 7 + y * 3) % 5) * 0.06
    if d <= 0.78 + rugo:
        return 0
    if d <= 1.02 + rugo:
        return 1
    if d <= 1.25 + rugo:
        return 2
    return None


def materiales(variante):
    piel = ruido(PIEL[1:5], 3101, grano=0.42, base=0.55)

    def agrietada(c, cara, x, y, semilla):
        k = (x * 5 + y * 3 + semilla * 7 + {"frente": 0, "espalda": 2, "der": 1, "izq": 3, "arriba": 4, "abajo": 5}.get(cara, 0)) % 29
        if k == 0:
            return mezcla(c, GRIETA[2], 0.75)
        if k == 14 and y % 2 == 0:
            return mezcla(c, GRIETA[1], 0.5)
        return c

    def pielg(cara, x, y, w, h, semilla=1):
        return agrietada(piel(cara, x, y, w, h), cara, x, y, semilla)

    hierro = metal(HIERRO, 3111, grano=0.22)
    rojo = tela(ROJO, 3121, pliegue=3, grano=0.18, luz=0.2)
    negra = tela(TELA, 3131, pliegue=3, grano=0.16)

    def oxidado(c, x, y, sem):
        k = (x * 13 + y * 7 + sem) % 23
        if k == 0:
            return OXIDO[1]
        if k == 11 and y % 3 == 0:
            return mezcla(c, OXIDO[2], 0.5)
        return c

    def cabeza(cara, x, y, w, h):
        c = pielg(cara, x, y, w, h, 3)
        if cara == "frente":
            c = claro(c, 0.12)
            # la frente arrugada, las cuencas hundidas con la luz roja, los pomulos, la nariz y la boca torcida
            if y == 1 and x in (1, 2, 5, 6):
                return oscuro(c, 0.2)
            if y == 3 and x in (1, 2, 5, 6):
                return oscuro(c, 0.45)
            if y == 4 and x in (1, 2, 5, 6):
                return CUENCA
            if y == 4 and x in (3, 4):
                return claro(c, 0.1)
            if y == 5 and x in (1, 6):
                return oscuro(c, 0.3)
            if y == 5 and x in (3, 4):
                return oscuro(c, 0.15)
            if y == 6 and 2 <= x <= 5:
                return oscuro(c, 0.4)
            if y == 2 and x == 5:
                return GRIETA[2]                               # la cicatriz que le baja por la ceja
            if y == 3 and x == 6 or y == 5 and x == 5:
                return GRIETA[1]
        if cara == "arriba":
            return CANA[2]
        return c

    def mecha(x, y, sem=0):
        """El pelo cano en mechones: el tono va por columnas (cada mechon) y cambia poco a lo largo."""
        k = (x * 37 + sem * 11) % 7
        t = (1, 2, 3, 2, 4, 3, 1)[k]
        if (y * 3 + x * 5 + sem) % 11 == 0:
            t -= 1
        return CANA[max(0, min(5, t))]

    def pelo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = mecha(x, y, 1) if cara != "arriba" else mecha(x, 0, 2)
        if cara == "frente":
            # el flequillo ralo y dos mechones por los lados de la cara
            if y <= 1 and (x * 5 + 1) % 4:
                return c
            if x in (0, w - 1) and y <= 6:
                return c
            return None
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if d <= 1 and y >= 3:
                return None                                    # la cara por los lados (la sien)
            return c
        return c

    def melena(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        c = oscuro(mecha(x, y, 3), 0.08 * y / max(1, h - 1))
        # el borde de abajo, a mechones
        k = (x * 7 + 3) % 4
        if y >= h - 1 - k:
            return None
        return c

    def barba(cara, x, y, w, h):
        if cara == "arriba":
            return None
        c = mecha(x, y, 5)
        if cara == "frente":
            if y == 0 and 2 <= x <= 4:
                return None                                    # la boca
            # a mechones abajo, mas larga por el medio
            largo = (h - 4, h - 2, h - 1, h, h - 1, h - 2, h - 3)[x % 7]
            if y >= largo:
                return None
            if (x * 3 + y) % 9 == 0:
                return mezcla(c, GRIETA[0], 0.35)              # sangre seca
        return c

    def torso(cara, x, y, w, h):
        c = pielg(cara, x, y, w, h, 5)
        if cara == "frente":
            m = _marca(0, 2, x, y)
            if m:
                return MARCA_C if m in ("O", "P") else MARCA
        return c

    def coraza(cara, x, y, w, h):
        c = oxidado(hierro(cara, x, y, w, h), x, y, 3141)
        if cara == "frente":
            # el agujero quemado en el pecho: por dentro, la piel con la marca; el borde al rojo y luego negro
            a = _agujero(x, y)
            if a == 0:
                m = _marca(0, 2, x, y)
                if m:
                    return MARCA_C if m in ("O", "P") else MARCA
                return agrietada(oscuro(piel(cara, x, y, w, h), 0.15), cara, x, y, 6)
            if a == 1:
                return BORDE[1 + (x + y) % 2]
            if a == 2:
                return oscuro(c, 0.5)
            if y in (0, 1) and x in (0, w - 1):
                return claro(c, 0.12)
            if (x * 11 + y * 5) % 17 == 0:
                return oscuro(c, 0.35)                         # las abolladuras
        if cara == "espalda" and y in (3, 8) and 1 <= x <= w - 2:
            return oscuro(c, 0.25)                             # las juntas de las placas
        return c

    def collar(cara, x, y, w, h):
        c = oxidado(metal(HIERRO, 3151)(cara, x, y, w, h), x, y, 3152)
        if cara == "frente" and x in (2, w - 3):
            return OXIDO[1]                                    # los remaches
        return c

    def eslabon_collar(cara, x, y, w, h):
        return HIERRO[3 + (y % 2)] if cara != "frente" or y != 1 else CUENCA

    def hombrera(cara, x, y, w, h):
        c = oxidado(hierro(cara, x, y, w, h), x, y, 3161)
        if cara == "arriba" and (x + y) % 3 == 0:
            return claro(c, 0.12)
        if y == h - 1:
            return oscuro(c, 0.3)
        return c

    def pua(cara, x, y, w, h):
        return HIERRO[4] if y == 0 else HIERRO[2 + (y % 2)]

    def brazal(cara, x, y, w, h):
        c = oxidado(hierro(cara, x, y, w, h), x, y, 3171)
        if y == h - 1:
            return HIERRO[1]
        if y in (2, 4) and cara != "abajo":
            return oscuro(c, 0.25)                             # las lamas
        return c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # la malla: anillas (puntos claros y oscuros alternos), rota abajo
        c = MALLA[1 + ((x + (y % 2)) % 2) * 2] if (x + y) % 2 == 0 else MALLA[1]
        if y == h - 1 and (x * 5 + 2) % 3 == 0:
            return None
        if y == 0:
            return ROJO[2]                                     # el borde del tabardo que le queda
        return c

    def greba(cara, x, y, w, h):
        if cara == "abajo":
            return HIERRO[0]
        c = oxidado(hierro(cara, x, y, w, h), x, y, 3181)
        if cara == "frente" and x in (2,) and y <= 3:
            return claro(c, 0.15)                              # la arista
        return c

    def capa(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = rojo(cara, x, y, w, h)
        if cara == "espalda" and 3 <= x <= 6 and 4 <= y <= 9:
            # lo que queda del blason (una torre rota, en oro gastado)
            torre = ["..o..", ".ooo.", ".o.o.", ".ooo.", ".ooo.", "ooooo"]
            ty, tx = y - 4, x - 3
            if 0 <= ty < 6 and 0 <= tx < 5 and torre[ty][tx] == "o":
                return mezcla(c, "#7a5a22", 0.55)
        # quemada y a jirones por abajo (los de los lados mas cortos)
        k = (x * 7 + 5) % 6
        if y >= h - 1 - k - (2 if x in (0, w - 1) else 0):
            return None
        if y >= h - 3 - k:
            return mezcla(c, "#0d0605", 0.6)                   # el borde chamuscado
        if (x * 13 + y * 7) % 41 == 0:
            return None                                        # algun agujero
        return c

    def brazo_d(cara, x, y, w, h):
        return pielg(cara, x, y, w, h, 7)

    def antebrazo_d(cara, x, y, w, h):
        c = pielg(cara, x, y, w, h, 9)
        if y >= h - 2:
            return oscuro(c, 0.1)                              # el puno
        return c

    def brazo_i(cara, x, y, w, h):
        return oscuro(negra(cara, x, y, w, h), 0.1)

    def antebrazo_i(cara, x, y, w, h):
        return oxidado(hierro(cara, x, y, w, h), x, y, 3191)

    def muslo(cara, x, y, w, h):
        return oscuro(negra(cara, x, y, w, h), 0.15)

    def pierna(cara, x, y, w, h):
        if cara == "abajo":
            return "#0e0b0a"
        if y >= h - 2:
            return HIERRO[1 + (x % 2)]                         # los escarpes
        return oscuro(negra(cara, x, y, w, h), 0.2)

    def vuelta(cara, x, y, w, h):
        # la cadena enrollada: eslabones de dos en dos pixeles (claro y oscuro), con algo de oxido
        if cara in ("arriba", "abajo"):
            return HIERRO[2]
        k = (x + (2 if cara in ("der", "izq") else 0)) % 4
        c = (HIERRO[5], HIERRO[3], HIERRO[1], HIERRO[3])[k]
        return OXIDO[1] if (x * 7 + y) % 9 == 0 else c

    def esl_plano(cara, x, y, w, h):
        if cara in ("arriba", "abajo") and 0 < x < w - 1 and 0 < y < h - 1:
            return None                                        # el hueco del eslabon
        c = HIERRO[3 + (x + y) % 2]
        return OXIDO[0] if (x * 3 + y) % 7 == 0 else c

    def esl_pie(cara, x, y, w, h):
        if cara in ("der", "izq") and 0 < x < w - 1 and 0 < y < h - 1:
            return None
        c = HIERRO[2 + (x + y) % 2]
        return OXIDO[0] if (x + y * 5) % 7 == 0 else c

    def roto(cara, x, y, w, h):
        # el hierro de la Sombra: negro, con la grieta roja que le late (luce un poco)
        c = metal(HIERRO[:5], 3201, grano=0.25)(cara, x, y, w, h)
        if (x * 3 + y * 5) % 11 == 0:
            return GRIETA[1]
        return c

    return {
        "cabeza": sombra(cabeza, abajo=0.16), "pelo": pelo, "melena": melena, "barba": barba,
        "torso": sombra(torso), "coraza": sombra(coraza), "collar": collar, "eslabon_collar": eslabon_collar,
        "hombrera": sombra(hombrera), "hombrera_b": sombra(hombrera), "pua": pua, "brazal": sombra(brazal, abajo=0.12),
        "faldon": faldon, "greba": sombra(greba, abajo=0.14), "capa": capa,
        "brazo_d": sombra(brazo_d, abajo=0.1), "antebrazo_d": sombra(antebrazo_d, abajo=0.16),
        "brazo_i": sombra(brazo_i, abajo=0.1), "antebrazo_i": sombra(antebrazo_i, abajo=0.16),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
        "vuelta": vuelta, "esl_plano": esl_plano, "esl_pie": esl_pie,
        "roto_lado": roto, "roto_lado_b": roto, "roto_punta": roto, "roto_cuello": roto,
    }


def brillo(variante):
    """Lo que luce: los ojos, la marca del pecho (por el agujero de la coraza), el borde al rojo del agujero y las grietas
    del eslabon roto."""

    def marca(cara, x, y, w, h):
        if cara != "frente":
            return None
        m = _marca(0, 2, x, y)
        if m == "P":
            return OJO_C
        if m == "O":
            return OJO
        if m in ("A", "E"):
            return "#a0180c"
        return None

    def coraza(cara, x, y, w, h):
        if cara != "frente":
            return None
        a = _agujero(x, y)
        if a == 0:
            return marca(cara, x, y, w, h)
        if a == 1:
            return BORDE[0] if (x + y) % 2 == 0 else BORDE[1]
        return None

    def ojos(cara, x, y, w, h):
        if cara == "frente" and y == 4 and x in (1, 2, 5, 6):
            return OJO if x in (2, 5) else "#b31c0e"
        return None

    def roto(cara, x, y, w, h):
        return "#9a1a0c" if (x * 3 + y * 5) % 11 == 0 else None

    return {"cabeza": ojos, "torso": marca, "coraza": coraza, "roto_lado": roto, "roto_lado_b": roto, "roto_punta": roto,
            "roto_cuello": roto}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos 11);
# _arma = hacia donde apunta el puno (el -Z de la mano: por donde sale la cadena); _cadena: ver `_dirs`.

ARRIBA = (0.0, 1.0, 0.0)
ABAJO = (0.0, -1.0, 0.0)
# de pie: cargado de hombros, la cabeza adelantada; la cadena le cuelga de la derecha y se tiende en el suelo por delante
BASE = dict(raiz=raiz(-0.3), torso=(9, 0, 0), cabeza=(-9, 2, 0), capa=(-7, 0, 0), melena=(-8, 0, 0), barba=(8, 0, 0),
            muslo_d=(-2, 0, 3), pierna_d=(3, 0, 0), muslo_i=(2, 0, -3), pierna_i=(3, 0, 0),
            _mano_d=("h", -1.4, -10.4, 1.8), _arma=(-0.08, -1.0, 0.18),
            _mano_i=("h", 1.2, -10.2, 1.2), _arma_i=(0.0, -1.0, 0.12),
            _cadena=("colgar", (-0.35, 0.0, 1.0)))
# en guardia: abierto de piernas y agachado, el puno de la cadena bajo y adelantado, la izquierda delante; la cadena
# tendida en el suelo hacia su derecha y atras
LISTO = dict(raiz=raiz(-1.6), torso=(16, -14, 0), cabeza=(-10, 12, 0), capa=(-10, 0, 0), melena=(-12, 0, 0), barba=(10, 0, 0),
             muslo_d=(-14, 0, 7), pierna_d=(16, 0, 0), muslo_i=(12, 0, -7), pierna_i=(14, 0, 0),
             _mano_d=("h", -1.6, -7.2, 5.6), _arma=(-0.25, -0.75, 0.6),
             _mano_i=("h", 1.4, -4.2, 7.0), _arma_i=(0.1, 0.3, 1.0),
             _cadena=("colgar", (-0.7, 0.0, -0.4)))
# de rodillas en lo alto del ancla: las manos en los muslos, la cabeza baja, la cadena en el suelo delante
RODILLAS = dict(raiz=raiz(-6.6, 0.8), torso=(18, 0, 0), cabeza=(30, 0, 0), capa=(16, 0, 0), melena=(20, 0, 0), barba=(-12, 0, 0),
                muslo_d=(-4, 0, 5), pierna_d=(92, 0, 0), muslo_i=(-4, 0, -5), pierna_i=(92, 0, 0))
REZANDO = con(RODILLAS, _mano_d=("h", -0.6, -7.4, 6.2), _arma=(0.0, -1.0, 0.25),
              _mano_i=("h", 0.6, -7.6, 5.8), _arma_i=(0.0, -1.0, 0.2), _cadena=("colgar", (-0.2, 0.0, 1.0)))


def _cola(desde, hasta, n=4):
    """n direcciones de `desde` a `hasta` (la cadena que se curva al voltearla)."""
    return [_mezcla_dir(_norm(desde), _norm(hasta), k / max(1, n - 1)) for k in range(n)]


def animaciones(r=None):
    """(bucles, acciones): los bucles por nombre y las acciones en el orden de sus numeros."""
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses, **kw):
        a = Anim(nombre, largo, bucle=True).poses(poses, r, **kw)
        B.append(a)
        return a

    def accion(nombre, largo, poses, golpes=(), interp="c"):
        a = Anim(nombre, largo, golpes=golpes, interp=interp).poses(poses, r)
        A.append(a)
        return a

    def fija(pose):
        q = resolver(r, pose)
        if "_cadena" in pose:
            q = r.posproceso(q, pose, None)
        return q

    # ---- quieto: respira hondo, los hombros suben y bajan
    bucle("quieto", 4.6, [
        (0.0, BASE),
        (2.3, con(BASE, torso=(12, 0, 0), cabeza=(-6, 4, 0), raiz=raiz(-0.7), _mano_d=("h", -1.4, -10.6, 2.0))),
        (4.6, BASE),
    ])
    # ---- lo mismo andando: la cadena se arrastra por detras
    ARR = con(BASE, _cadena=("colgar", (0.15, 0.0, -1.0)), _mano_d=("h", -1.8, -10.2, 0.6), _arma=(-0.1, -1.0, -0.15))
    bucle("arrastra", 2.0, [(0.0, ARR), (1.0, con(ARR, torso=(11, 0, 0))), (2.0, ARR)])
    # ---- andar: el paso pesado de un viejo grande (se suma a lo de arriba)
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate(((-22, 16, 6, 18, 5, 0.0), (-4, 2, 4, 30, 0, 0.6), (16, -22, 18, 6, -5, 0.0),
                                                   (2, -4, 30, 4, 0, 0.6), (-22, 16, 6, 18, 5, 0.0))):
        pasos.append((1.8 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (0, ty, -ty * 0.4), "cabeza": (0, -ty * 0.6, ty * 0.3), "raiz": raiz(up),
                                    "capa": (abs(md) * 0.3, 0, 0), "melena": (abs(md) * 0.2, 0, 0)}))
    bucle("andar", 1.8, pasos)
    # ---- en guardia: se mece despacio
    l1 = con(LISTO, raiz=raiz(-2.2), torso=(19, -16, 0), cabeza=(-12, 14, 2), _mano_d=("h", -1.8, -7.6, 5.0))
    bucle("listo", 2.4, [(0.0, LISTO), (1.2, l1), (2.4, LISTO)])
    # ---- de rodillas en lo alto del ancla: casi quieto (la cabeza baja un poco mas y vuelve)
    bucle("rezando", 6.0, [(0.0, REZANDO), (3.0, con(REZANDO, cabeza=(36, 0, 0), torso=(20, 0, 0))), (6.0, REZANDO)])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 cadenazo: se gira con la cadena atras, a su derecha, y la pasa de lado a lado por delante (a la altura del pecho)
    accion("cadenazo", 1.6, [
        (0.00, L),
        (0.45, con(L, raiz=raiz(-2.2), torso=(10, 44, 0), cabeza=(-6, -22, 0), muslo_d=(6, 0, 7), muslo_i=(-14, 0, -7),
                   _mano_d=("h", -8.0, 0.6, -4.2), _arma=(-1.0, 0.1, -0.35), _cadena=("colgar", (-0.3, 0.0, -1.0)))),
        (0.72, con(L, raiz=raiz(-2.0), torso=(8, 14, 0), cabeza=(-6, -6, 0),
                   _mano_d=("h", -9.0, 1.6, 3.8), _arma=(-0.8, 0.1, 0.6),
                   _cadena=("lista", [(-0.9, 0.05, 0.4), (-0.95, 0.05, 0.05), (-0.92, 0.0, -0.3), (-0.8, -0.05, -0.6)]))),
        (0.90, con(L, raiz=raiz(-2.4), torso=(12, -30, 0), cabeza=(-6, 18, 0), muslo_d=(-20, 0, 7), muslo_i=(8, 0, -7),
                   _mano_d=("h", 2.2, 1.2, 9.8), _arma=(0.35, 0.05, 1.0),
                   _cadena=("lista", [(0.25, 0.0, 1.0), (0.05, 0.0, 1.0), (-0.18, 0.0, 1.0), (-0.4, -0.05, 0.92)]))),
        (1.12, con(L, raiz=raiz(-2.2), torso=(14, -52, 0), cabeza=(-6, 26, 0), muslo_d=(-24, 0, 7), muslo_i=(10, 0, -7),
                   _mano_d=("h", 9.0, 0.2, 5.8), _arma=(1.0, 0.0, 0.35),
                   _cadena=("lista", [(0.95, -0.05, 0.3), (0.85, -0.15, 0.55), (0.55, -0.25, 0.8), (0.2, -0.35, 0.95)]))),
        (1.60, L),
    ], golpes=(0.9,))
    # ---- 2 latigazo: la alza por detras de la cabeza (le cuelga por la espalda) y la restalla delante, recta
    accion("latigazo", 1.7, [
        (0.00, L),
        (0.50, con(L, raiz=raiz(-0.6), torso=(-12, 16, 0), cabeza=(-14, -6, 0), muslo_d=(8, 0, 6), muslo_i=(-10, 0, -6),
                   _mano_d=("h", -1.0, 9.6, -3.4), _arma=(0.0, 0.4, -1.0), _mano_i=("h", 3.0, -1.0, 8.0),
                   _cadena=("colgar", (0.0, 0.0, -1.0)))),
        (0.80, con(L, raiz=raiz(-0.8), torso=(2, 4, 0), cabeza=(-8, 0, 0),
                   _mano_d=("h", -1.0, 10.0, 3.0), _arma=(0.0, 0.9, 0.45),
                   _cadena=("lista", [(0.0, 0.6, -0.8), (0.0, 0.2, -1.0), (0.0, -0.2, -1.0), (0.0, -0.5, -0.85)]))),
        (1.00, con(L, raiz=raiz(-2.2, 1.2), torso=(24, -6, 0), cabeza=(0, 4, 0), muslo_d=(-26, 0, 6), pierna_d=(24, 0, 0),
                   muslo_i=(10, 0, -6), _mano_d=("h", -0.6, 1.0, 10.4), _arma=(0.0, 0.08, 1.0), _mano_i=("h", 4.0, -6.0, 3.0),
                   _cadena=("dir", (0.0, 0.02, 1.0)))),
        (1.22, con(L, raiz=raiz(-2.0, 1.0), torso=(22, -4, 0), cabeza=(2, 2, 0), muslo_d=(-24, 0, 6), pierna_d=(22, 0, 0),
                   _mano_d=("h", -0.6, -4.0, 9.6), _arma=(0.0, -0.45, 0.9),
                   _cadena=("lista", [(0.0, -0.3, 1.0), (0.0, -0.6, 0.8), (0.0, -0.85, 0.5), (0.0, -1.0, 0.15)]))),
        (1.70, L),
    ], golpes=(1.0,))
    # ---- 3 la estrella: alza la cadena al cielo, la sostiene y la estrella delante (el eslabon da en el suelo: la onda)
    alto = con(L, raiz=raiz(0.2, 0.6), torso=(-16, 6, 0), cabeza=(-20, -4, 0), muslo_d=(6, 0, 6), muslo_i=(-8, 0, -6),
               _mano_d=("h", -0.4, 11.0, 0.6), _arma=ARRIBA, _mano_i=("h", 6.0, 3.0, 4.0), _cadena=("colgar", (0.0, 0.0, -1.0)))
    accion("estrella", 2.0, [
        (0.00, L),
        (0.60, alto),
        (0.92, con(alto, torso=(-18, 6, 0), _cadena=("lista", [(0.0, 0.95, -0.3), (0.0, 0.7, -0.7), (0.0, 0.3, -0.95), (0.0, -0.1, -1.0)]))),
        (1.20, con(L, raiz=raiz(-2.8, 1.6), torso=(34, 0, 0), cabeza=(12, 0, 0), muslo_d=(-32, 0, 6), pierna_d=(42, 0, 0),
                   muslo_i=(20, 0, -6), pierna_i=(20, 0, 0), _mano_d=("h", -0.4, -5.0, 9.6), _arma=(0.0, -0.5, 0.85),
                   _mano_i=("h", 4.0, -8.0, 4.0),
                   _cadena=("lista", [(0.0, -0.45, 0.9), (0.0, -0.3, 0.95), (0.0, -0.2, 1.0), (0.0, -0.05, 1.0)]))),
        (1.55, con(L, raiz=raiz(-2.6, 1.4), torso=(32, 0, 0), cabeza=(14, 0, 0), muslo_d=(-30, 0, 6), pierna_d=(40, 0, 0),
                   muslo_i=(20, 0, -6), pierna_i=(20, 0, 0), _mano_d=("h", -0.4, -6.0, 9.0), _arma=(0.0, -0.6, 0.8),
                   _mano_i=("h", 4.0, -8.0, 4.0), _cadena=("colgar", (0.0, 0.0, 1.0)))),
        (2.00, L),
    ], golpes=(1.2,))
    # ---- 4 torbellino: el brazo en cruz y la cadena tendida; dos vueltas enteras (lineal: sin pasarse)
    lq = fija(L)
    giro = dict(lq, raiz={"pos": (0.0, -2.4, 0.0), "rot": (0, 0, 0)}, torso=(10, 0, 0), cabeza=(-6, 0, 0),
                muslo_d=(-10, 0, 8), pierna_d=(18, 0, 0), muslo_i=(6, 0, -8), pierna_i=(14, 0, 0),
                brazo_d=(0, 0, 84), antebrazo_d=(0, 0, 0), mano_d=(0, 0, 0), brazo_i=(-30, 0, -40), antebrazo_i=(-20, 0, 0),
                capa=(-30, 0, 0), melena=(-20, 0, 0))
    vuelta = ("local", [(90, 0, 0), (0, -9, 0), (0, -12, 0), (0, -14, 0)])
    poses = [(0.00, L), (0.25, con(giro, _cadena=("local", [(70, 0, 0), (-20, -10, 0), (-20, -10, 0), (-15, -10, 0)])))]
    for k in range(9):
        t = 0.35 + k * 0.15
        p = dict(giro, raiz={"pos": (0.0, -2.4, 0.0), "rot": (0, -90 * k, 0)})
        poses.append((t, con(p, _cadena=vuelta)))
    final = dict(lq, raiz={"pos": lq["raiz"]["pos"] if isinstance(lq.get("raiz"), dict) else (0, -1.6, 0), "rot": (0, -720, 0)})
    poses.append((1.65, con(dict(giro, raiz={"pos": (0.0, -2.0, 0.0), "rot": (0, -720, 0)}), _cadena=("local", [(60, 0, 0), (-10, 0, 0), (-10, 0, 0), (-10, 0, 0)]))))
    poses.append((2.00, con(final, _cadena=L["_cadena"])))
    accion("torbellino", 2.0, poses, golpes=(0.7, 1.3), interp="l")
    # ---- 5 salto: se agacha, salta con la cadena alzada y cae sobre su presa estrellandola
    accion("salto", 2.2, [
        (0.00, L),
        (0.50, con(L, raiz=raiz(-4.2, 0.4), torso=(30, 0, 0), cabeza=(-12, 0, 0), muslo_d=(-52, 0, 6), pierna_d=(84, 0, 0),
                   muslo_i=(-52, 0, -6), pierna_i=(84, 0, 0), _mano_d=("h", -2.0, -6.0, -5.6), _arma=(0.0, -0.5, -1.0),
                   _mano_i=("h", 2.0, -6.0, -4.0), _cadena=("colgar", (0.0, 0.0, -1.0)))),
        (0.66, con(L, raiz=raiz(1.2), torso=(-12, 0, 0), cabeza=(-16, 0, 0), muslo_d=(-6, 0, 4), pierna_d=(6, 0, 0),
                   muslo_i=(8, 0, -4), pierna_i=(8, 0, 0), _mano_d=("h", -1.0, 10.6, 1.4), _arma=(0.0, 1.0, 0.25),
                   _mano_i=("h", 6.0, 2.0, 3.0),
                   _cadena=("lista", [(0.0, -0.9, -0.4), (0.0, -0.8, -0.6), (0.0, -0.6, -0.8), (0.0, -0.4, -0.9)]))),
        (1.10, con(L, raiz=raiz(1.0), torso=(-6, 0, 0), cabeza=(-10, 0, 0), muslo_d=(-44, 0, 6), pierna_d=(62, 0, 0),
                   muslo_i=(-30, 0, -6), pierna_i=(56, 0, 0), _mano_d=("h", -1.0, 9.4, -3.6), _arma=(0.0, 0.5, -1.0),
                   _mano_i=("h", 6.0, 2.0, 3.0),
                   _cadena=("lista", [(0.0, -0.3, -1.0), (0.0, -0.6, -0.8), (0.0, -0.85, -0.5), (0.0, -1.0, -0.1)]))),
        (1.40, con(L, raiz=raiz(-3.8, 1.4), torso=(36, 0, 0), cabeza=(12, 0, 0), muslo_d=(-42, 0, 6), pierna_d=(72, 0, 0),
                   muslo_i=(-10, 0, -6), pierna_i=(60, 0, 0), _mano_d=("h", -0.5, -6.0, 9.2), _arma=(0.0, -0.7, 0.7),
                   _mano_i=("h", 3.0, -8.0, 4.0),
                   _cadena=("lista", [(0.0, -0.6, 0.8), (0.0, -0.3, 0.95), (0.0, -0.1, 1.0), (0.0, 0.0, 1.0)]))),
        (1.80, con(L, raiz=raiz(-3.4, 1.2), torso=(32, 0, 0), cabeza=(10, 0, 0), muslo_d=(-38, 0, 6), pierna_d=(64, 0, 0),
                   muslo_i=(-10, 0, -6), pierna_i=(54, 0, 0), _mano_d=("h", -0.5, -7.0, 8.6), _arma=(0.0, -0.8, 0.6),
                   _mano_i=("h", 3.0, -8.0, 4.0), _cadena=("colgar", (0.0, 0.0, 1.0)))),
        (2.20, L),
    ], golpes=(1.4,))
    # ---- 6 encadenar: alza la cadena al cielo y golpea el suelo con el puno, con una rodilla en tierra
    accion("encadenar", 1.8, [
        (0.00, L),
        (0.55, con(L, raiz=raiz(0.0, 0.4), torso=(-14, 0, 0), cabeza=(-32, 0, 0), muslo_d=(4, 0, 6), muslo_i=(-4, 0, -6),
                   _mano_d=("h", -0.5, 11.0, 0.6), _arma=ARRIBA, _mano_i=("h", 7.0, 1.0, 4.0), _cadena=("colgar", (0.0, 0.0, -1.0)))),
        (1.00, con(L, raiz=raiz(-5.0, 0.8), torso=(36, 0, 0), cabeza=(20, 0, 0), muslo_i=(-72, 0, -4), pierna_i=(72, 0, 0),
                   muslo_d=(8, 0, 6), pierna_d=(96, 0, 0), _mano_d=("h", -0.5, -10.0, 5.6), _arma=(0.0, -1.0, 0.25),
                   _mano_i=("h", 2.0, -6.0, 6.0), _cadena=("colgar", (0.0, 0.0, 1.0)))),
        (1.30, con(L, raiz=raiz(-4.8, 0.8), torso=(34, 0, 0), cabeza=(16, 0, 0), muslo_i=(-70, 0, -4), pierna_i=(70, 0, 0),
                   muslo_d=(8, 0, 6), pierna_d=(94, 0, 0), _mano_d=("h", -0.5, -9.6, 5.8), _arma=(0.0, -1.0, 0.25),
                   _mano_i=("h", 2.0, -6.0, 6.0), _cadena=("colgar", (0.0, 0.0, 1.0)))),
        (1.80, L),
    ], golpes=(1.0,))
    # ---- 7 el rugido (al empezar la pelea): el pecho fuera, la cabeza atras, el puno de la cadena al cielo
    accion("rugido", 2.4, [
        (0.00, BASE),
        (0.50, con(BASE, torso=(-12, 0, 0), cabeza=(-26, 0, 0), _mano_d=("h", -4.0, 9.0, 3.0), _arma=(-0.3, 0.9, 0.2),
                   _mano_i=("h", 7.0, 2.0, 4.0), _arma_i=(1.0, 0.2, 0.3))),
        (1.00, con(BASE, torso=(-16, 0, 0), cabeza=(-34, 0, 0), raiz=raiz(-0.6), _mano_d=("h", -4.2, 9.4, 2.6), _arma=(-0.3, 0.9, 0.2),
                   _mano_i=("h", 7.6, 2.4, 3.6), _arma_i=(1.0, 0.2, 0.3))),
        (1.80, con(BASE, torso=(-10, 0, 0), cabeza=(-24, 0, 0), _mano_d=("h", -4.0, 8.0, 3.4), _arma=(-0.3, 0.9, 0.2),
                   _mano_i=("h", 7.0, 1.0, 4.0), _arma_i=(1.0, 0.2, 0.3))),
        (2.40, L),
    ], golpes=(1.0,))
    # ---- 8 la marca (a media vida): se lleva la izquierda al pecho, se dobla y ruge con los brazos abiertos
    accion("marca", 2.5, [
        (0.00, L),
        (0.40, con(L, raiz=raiz(-1.8), torso=(30, 0, 4), cabeza=(30, 0, 0), _mano_i=("h", -5.6, -5.0, 4.2), _arma_i=(-0.6, 0.2, 0.8),
                   _mano_d=("h", -1.4, -9.6, 2.6), _arma=ABAJO, _cadena=("colgar", (-0.4, 0.0, 0.9)))),
        (0.90, con(L, raiz=raiz(-2.0), torso=(34, 0, 6), cabeza=(36, 0, 0), _mano_i=("h", -5.8, -5.4, 4.0), _arma_i=(-0.6, 0.2, 0.8),
                   _mano_d=("h", -1.4, -9.8, 2.4), _arma=ABAJO, _cadena=("colgar", (-0.4, 0.0, 0.9)))),
        (1.20, con(L, raiz=raiz(-0.4), torso=(-20, 0, 0), cabeza=(-32, 0, 0), _mano_d=("h", -9.0, 4.0, 3.0), _arma=(-1.0, 0.3, 0.2),
                   _mano_i=("h", 9.0, 4.0, 3.0), _arma_i=(1.0, 0.3, 0.2), _cadena=("colgar", (-0.6, 0.0, 0.5)))),
        (1.80, con(L, raiz=raiz(-0.6), torso=(-16, 0, 0), cabeza=(-26, 0, 0), _mano_d=("h", -8.6, 3.0, 3.4), _arma=(-1.0, 0.2, 0.3),
                   _mano_i=("h", 8.6, 3.0, 3.4), _arma_i=(1.0, 0.2, 0.3), _cadena=("colgar", (-0.6, 0.0, 0.5)))),
        (2.50, L),
    ], golpes=(1.2,))
    # ---- 9 aturdido: da un paso atras, la cabeza le cuelga
    accion("aturdido", 1.5, [
        (0.00, L),
        (0.25, con(L, raiz=raiz(-1.0, -1.6), torso=(-18, 10, 0), cabeza=(-22, -10, 6), muslo_d=(10, 0, 7), muslo_i=(-20, 0, -7))),
        (0.80, con(L, raiz=raiz(-2.4, -1.0), torso=(28, 6, 0), cabeza=(30, -6, 0), _mano_d=("h", -1.6, -10.0, 2.0), _arma=ABAJO,
                   _mano_i=("h", 1.6, -9.8, 2.0), _cadena=("colgar", (-0.3, 0.0, 1.0)))),
        (1.50, L),
    ])
    # ---- 10 morir: el golpe; cae de rodillas (la cadena se le escurre al suelo); mira al cielo, a la ciudadela (su ultima
    # frase) y agacha la cabeza
    VENCIDO = con(RODILLAS, torso=(32, 0, 0), cabeza=(42, 0, 0), _mano_d=("h", -0.6, -10.4, 5.0), _arma=(0.0, -1.0, 0.4),
                  _mano_i=("h", 0.8, -8.0, 5.8), _arma_i=(0.0, -1.0, 0.2), _cadena=("colgar", (-0.5, 0.0, 0.8)))
    accion("morir", 6.0, [
        (0.00, con(L, torso=(-12, 0, 6), cabeza=(-20, 0, 10), _mano_i=("h", -5.0, -4.0, 4.4), _arma_i=(-0.6, 0.2, 0.8))),
        (0.40, con(L, raiz=raiz(-2.0), torso=(-16, 0, 8), cabeza=(-26, 0, 12), _mano_i=("h", -5.4, -4.4, 4.2), _arma_i=(-0.6, 0.2, 0.8),
                   _cadena=("colgar", (-0.5, 0.0, 0.8)))),
        (1.30, con(RODILLAS, torso=(8, 0, 0), cabeza=(-8, 0, 0), _mano_d=("h", -1.0, -9.8, 3.0), _arma=ABAJO,
                   _mano_i=("h", -5.0, -5.0, 4.4), _arma_i=(-0.6, 0.2, 0.8), _cadena=("colgar", (-0.5, 0.0, 0.8)))),
        (2.40, con(RODILLAS, torso=(-10, 0, 0), cabeza=(-42, 0, 0), _mano_d=("h", -1.2, -10.4, 2.0), _arma=ABAJO,
                   _mano_i=("h", 1.0, -10.2, 1.6), _arma_i=ABAJO, _cadena=("colgar", (-0.5, 0.0, 0.8)))),
        (3.80, con(RODILLAS, torso=(-8, 0, 0), cabeza=(-38, 0, 0), _mano_d=("h", -1.2, -10.4, 2.0), _arma=ABAJO,
                   _mano_i=("h", 1.0, -10.2, 1.6), _arma_i=ABAJO, _cadena=("colgar", (-0.5, 0.0, 0.8)))),
        (5.00, VENCIDO),
        (6.00, VENCIDO),
    ])
    # ---- 11 levantarse: estaba de rodillas; pone un pie, se apoya en el muslo y se alza
    accion("levantarse", 1.6, [
        (0.00, REZANDO),
        (0.55, con(REZANDO, torso=(10, 0, 0), cabeza=(-4, 0, 0), muslo_d=(-66, 0, 5), pierna_d=(76, 0, 0), raiz=raiz(-4.8, 0.6))),
        (1.05, con(BASE, raiz=raiz(-1.0), torso=(16, 0, 0), cabeza=(-6, 0, 0))),
        (1.60, L),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    if nombre == "andar":
        return [anims["arrastra"]]
    return []


TABLA_EXTRA = ()
