"""Animaciones de Ser Gavran (fotogramas clave) que se convierten en codigo Java de Minecraft
(AnimationDefinition), igual que las del rey (tools/rey/animaciones.py).

Las poses se escriben con objetivos y la cinematica inversa (ik.py) saca los angulos de los brazos y del mandoble:
  _mano_d: (izquierda, arriba, delante) del puno derecho, en pixeles desde el suelo bajo el centro del cuerpo
  _hoja:   hacia donde apunta la hoja (izquierda, arriba, delante)
  _filo:   hacia donde mira el filo (para que corte de filo y no de plano)
  _dos_manos: la izquierda agarra la empunadura      _mano_i: donde va el puno izquierdo si no agarra
El resto de claves son angulos de piezas (grados, se suman a su pose) o {"pos": (x, y arriba, z), "rot": ...}.

Ejes (modelo: Y abajo, frente -Z, derecha del caballero -X):
  muslo x negativo = pierna hacia delante; torso y positivo = gira hacia su derecha; cabeza x positivo = mira abajo
  capa x positivo = la capa se levanta hacia atras.

Los golpes de cada ataque (en ticks desde que empieza) se exportan a GavranAnims.java para que el servidor pegue
justo cuando la espada llega.
"""
import json
import math
import os
import pickle

import ik

ANIMS = {}
GOLPES = {}          # nombre -> [tick, ...]
CACHE_F = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".ik_cache.pkl")
try:
    with open(CACHE_F, "rb") as fh:
        _CACHE = pickle.load(fh)
except Exception:  # noqa: BLE001
    _CACHE = {}
_PEOR = []


def _clave(spec, ref):
    return repr(sorted((k, repr(v)) for k, v in spec.items())) + "|" + repr(sorted((k, repr(v)) for k, v in (ref or {}).items()
                                                                                   if k.startswith(("brazo", "antebrazo"))))


def resolver(spec, ref=None):
    k = _clave(spec, ref)
    if k not in _CACHE:
        _CACHE[k] = ik.resolver(spec, ref)
    q = dict(_CACHE[k])
    _PEOR.append((q.get("_err", 0), spec))
    q.pop("_err", None)
    return q


def guardar_cache():
    with open(CACHE_F, "wb") as fh:
        pickle.dump(_CACHE, fh)


def _rot_de(v):
    if isinstance(v, dict):
        return v.get("rot", (0, 0, 0))
    return v


def acompanar(q):
    """Piezas que siguen a otras: hombreras (parte del giro del brazo), escarcelas y tabardo (los muslos)."""
    for n in ("d", "i"):
        b = _rot_de(q.get(f"brazo_{n}", (0, 0, 0)))
        if f"hombrera_{n}" not in q:
            q[f"hombrera_{n}"] = tuple(max(-55, min(55, b[i] * k)) for i, k in enumerate((0.38, 0.3, 0.42)))
        m = _rot_de(q.get(f"muslo_{n}", (0, 0, 0)))
        if f"escarcela_{n}" not in q:
            q[f"escarcela_{n}"] = (min(0.0, m[0] * 0.7), 0, 0)
        if f"escarcela_l{n}" not in q:
            q[f"escarcela_l{n}"] = (min(0.0, m[0] * 0.25), 0, (m[2] * 0.6))
    md, mi = _rot_de(q.get("muslo_d", (0, 0, 0))), _rot_de(q.get("muslo_i", (0, 0, 0)))
    if "tabardo" not in q:
        q["tabardo"] = (min(0.0, min(md[0], mi[0]) * 0.75), 0, 0)
    if "tabardo_t" not in q:
        q["tabardo_t"] = (max(0.0, max(md[0], mi[0]) * 0.6), 0, 0)
    return q


class Anim:
    def __init__(self, name, length, loop=False, golpes=()):
        self.name = name
        self.length = length
        self.loop = loop
        self.ch = []  # (pieza, "rot"/"pos", [(t, x, y, z, interp)])
        ANIMS[name] = self
        if golpes:
            GOLPES[name] = [int(round(t * 20)) for t in golpes]

    def poses(self, timeline, i="c", resolver_ik=True):
        """timeline: [(t, pose)] -> un canal por pieza. Las poses con objetivos (_mano_d...) se resuelven en orden."""
        res = []
        prev = None
        self.dos_manos = [bool(p.get("_dos_manos")) for _, p in timeline]
        for t, pose in timeline:
            q = resolver(pose, prev) if resolver_ik and any(k.startswith("_") for k in pose) else dict(pose)
            q = acompanar(q)
            res.append((t, q))
            prev = q
        parts = {}
        for t, pose in res:
            for p, v in pose.items():
                parts.setdefault(p, set())
                if isinstance(v, dict):
                    parts[p].update(v.keys())
                else:
                    parts[p].add("rot")
        for p, kinds in parts.items():
            for kind in sorted(kinds):
                kfs = []
                for t, pose in res:
                    v = pose.get(p)
                    if isinstance(v, dict):
                        v = v.get(kind)
                    elif kind != "rot":
                        v = None
                    if v is None:
                        v = (0, 0, 0)
                    kfs.append((t, float(v[0]), float(v[1]), float(v[2]), i))
                self.ch.append((p, kind, kfs))
        self.resueltas = res
        return self


def con(base, **over):
    d = dict(base)
    d.update(over)
    return d


def P(*poses, **kw):
    """Pose = GUARDIA_BAJA (reposo) con cambios."""
    d = dict(READY)
    for p in poses:
        d.update(p)
    d.update(kw)
    return d


def raiz(y=0.0, z=0.0, x=0.0, rot=None):
    d = {"pos": (x, y, z)}
    if rot:
        d["rot"] = rot
    return d


# ============================================================================ poses base

# piernas: reposo (pie izquierdo delante), rodilla en tierra, de rodillas, agachado
PIERNAS = dict(muslo_d=(6, 0, 4), pierna_d=(8, 0, 0), muslo_i=(-10, 0, -4), pierna_i=(10, 0, 0))
RODILLA = dict(raiz=raiz(-15.5), muslo_i=(-80, 0, -6), pierna_i=(80, 0, 0), muslo_d=(-2, 0, 6), pierna_d=(88, 0, 0),
               tabardo=(-30, 0, 0), escarcela_i=(-50, 0, 0), escarcela_d=(-10, 0, 0))
RODILLAS = dict(raiz=raiz(-15.5), muslo_i=(0, 0, -5), pierna_i=(88, 0, 0), muslo_d=(0, 0, 5), pierna_d=(88, 0, 0))
AGACHADO = dict(raiz=raiz(-11), muslo_d=(-62, 0, 14), pierna_d=(78, 0, 0), muslo_i=(-72, 0, -14), pierna_i=(84, 0, 0),
                torso=(28, 0, 0), cabeza=(-20, 0, 0), tabardo=(-45, 0, 0))

# reposo: guardia baja con las dos manos, la punta hacia delante y abajo
READY = dict(PIERNAS, raiz=raiz(-0.8), torso=(5, -6, 0), cabeza=(4, 6, 0), capa=(2, 0, 0),
             _mano_d=(-1, 48, 21), _hoja=(0.2, -0.6, 1), _filo=(0, -1, 0.2), _dos_manos=True)

# vigilia en la rama: de rodilla, la espada clavada delante, las manos en la empunadura y la cabeza gacha
VIGILIA = dict(RODILLA, torso=(8, 0, 0), cabeza=(22, 0, 0), capa=(-6, 0, 0), capa2=(10, 0, 0),
               _mano_d=(0, 57, 13), _hoja=(0, -1, 0.02), _filo=(-1, 0, 0), _dos_manos=True)

# guardia alta (cubrirse): espada vertical delante, filo hacia el enemigo, piernas abiertas
GUARDIA = dict(raiz=raiz(-2.5), muslo_d=(10, 0, 10), pierna_d=(14, 0, 0), muslo_i=(-22, 0, -10), pierna_i=(22, 0, 0),
               torso=(6, 12, 0), cabeza=(-4, -10, 0),
               _mano_d=(-4, 56, 16), _hoja=(0.15, 1, 0.12), _filo=(-0.3, 0, 1), _dos_manos=True)

# ============================================================================ bucles

Anim("posado", 4.0, loop=True).poses([
    (0.0, VIGILIA),
    (2.0, con(VIGILIA, torso=(10, 0, 0), cabeza=(24, 0, 0), raiz=raiz(-15.8))),
    (4.0, VIGILIA),
])

Anim("quieto", 3.2, loop=True).poses([
    (0.0, READY),
    (1.6, con(READY, torso=(7, -6, 0), cabeza=(6, 6, 0), raiz=raiz(-1.3), capa=(4, 0, 0), capa2=(3, 0, 0))),
    (3.2, READY),
])

_wk = 1.4
_pasos = []
for k, (md, mi, pd, pi_, ry, up) in enumerate((
        (-24, 20, 6, 20, 5, 0.0), (0, 0, 4, 34, 0, 1.0), (20, -24, 20, 6, -5, 0.0), (0, 0, 34, 4, 0, 1.0), (-24, 20, 6, 20, 5, 0.0))):
    _pasos.append((_wk * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                  "torso": (0, ry, 0), "raiz": {"pos": (0, up, 0)}, "capa": (up * 3, 0, 0), "capa2": (up * 2, 0, 0),
                                  "cabeza": (0, -ry * 0.6, 0)}))
Anim("andar", _wk, loop=True).poses(_pasos, resolver_ik=False)

Anim("hablar", 2.4, loop=True).poses([
    (0.0, {"cabeza": (0, 0, 0), "torso": (0, 0, 0)}),
    (0.4, {"cabeza": (-5, 5, 0), "torso": (-1, 1, 0)}),
    (0.8, {"cabeza": (-2, -4, 2), "torso": (0, -1, 0)}),
    (1.3, {"cabeza": (-6, 2, 0), "torso": (-1, 0, 0)}),
    (1.8, {"cabeza": (-1, -2, -1), "torso": (0, 1, 0)}),
    (2.4, {"cabeza": (0, 0, 0), "torso": (0, 0, 0)}),
], resolver_ik=False)

Anim("guardia", 1.6, loop=True).poses([
    (0.0, GUARDIA),
    (0.8, con(GUARDIA, raiz=raiz(-3.0), torso=(7, 12, 0))),
    (1.6, GUARDIA),
])

# embestida con el hombro: inclinado, el hombro izquierdo delante, la espada baja detras
_carga_a = dict(raiz=raiz(-4, rot=(0, -28, 0)), torso=(30, 0, 0), cabeza=(-26, 22, 0), capa=(40, 0, 0), capa2=(25, 0, 0),
                muslo_d=(30, 0, 6), pierna_d=(40, 0, 0), muslo_i=(-55, 0, -6), pierna_i=(30, 0, 0),
                _mano_d=(-16, 36, -6), _hoja=(-0.3, -0.5, -1), _filo=(0, -1, 0), _mano_i=(8, 58, 14))
_carga_b = con(_carga_a, muslo_d=(-55, 0, 6), pierna_d=(30, 0, 0), muslo_i=(30, 0, -6), pierna_i=(40, 0, 0), raiz=raiz(-2.5, rot=(0, -28, 0)))
Anim("carga", 0.7, loop=True).poses([(0.0, _carga_a), (0.35, _carga_b), (0.7, _carga_a)])

# ============================================================================ acciones

# --- bajar de la rama: se levanta, arranca la espada, salta y cae de rodilla con un golpe al suelo
_de_pie_rama = dict(PIERNAS, torso=(6, 0, 0), cabeza=(-10, 0, 0), raiz=raiz(0),
                    _mano_d=(0, 57, 13), _hoja=(0, -1, 0.02), _filo=(-1, 0, 0), _dos_manos=True)
_arranca = dict(PIERNAS, torso=(-6, 10, 0), cabeza=(-14, -8, 0), raiz=raiz(0),
                _mano_d=(-12, 76, 8), _hoja=(-0.15, 1, 0.25), _filo=(0, 0, 1), _dos_manos=True)
_impulso = dict(AGACHADO, raiz=raiz(-9), torso=(30, 0, 0), cabeza=(-24, 0, 0), capa=(10, 0, 0),
                _mano_d=(-14, 54, -4), _hoja=(-0.2, 0.6, -1), _filo=(0, 1, 0), _dos_manos=True)
_aire = dict(raiz=raiz(2), muslo_d=(-40, 0, 8), pierna_d=(70, 0, 0), muslo_i=(-20, 0, -8), pierna_i=(40, 0, 0),
             torso=(-8, 0, 0), cabeza=(-12, 0, 0), capa=(58, 0, 0), capa2=(24, 0, 0), tabardo=(-35, 0, 0),
             _mano_d=(0, 94, 2), _hoja=(0, 0.4, -1), _filo=(0, 1, 0), _dos_manos=True)
_aterriza = dict(RODILLA, raiz=raiz(-16.5), torso=(32, 0, 0), cabeza=(-22, 0, 0), capa=(28, 0, 0), capa2=(16, 0, 0),
                 _mano_d=(0, 30, 24), _hoja=(0, -0.85, 1), _filo=(0, -1, 0), _dos_manos=True)
# el servidor lo mueve por el aire entre el salto (1,45 s) y el aterrizaje (2,75 s)
Anim("descender", 4.0, golpes=(1.45, 2.75)).poses([
    (0.0, VIGILIA),
    (0.55, _de_pie_rama),
    (0.9, _arranca),
    (1.2, _impulso),
    (1.45, con(_aire, raiz=raiz(4), muslo_d=(-10, 0, 8), pierna_d=(10, 0, 0), muslo_i=(-5, 0, -8), pierna_i=(8, 0, 0))),
    (1.95, _aire),
    (2.5, con(_aire, capa=(75, 0, 0), capa2=(30, 0, 0))),
    (2.7, con(_aire, torso=(10, 0, 0), capa=(70, 0, 0), _mano_d=(0, 60, 22), _hoja=(0, -0.3, 1))),
    (2.77, _aterriza),
    (3.3, _aterriza),
    (4.0, READY),
])

# --- saludo de caballero: la espada vertical ante la cara, y abajo a un lado
_saludo = dict(PIERNAS, raiz=raiz(0), torso=(0, 0, 0), cabeza=(6, 0, 0),
               _mano_d=(0, 63, 13), _hoja=(0, 1, 0.06), _filo=(1, 0, 0), _dos_manos=True)
_saludo_abajo = dict(PIERNAS, raiz=raiz(0), torso=(2, 14, 0), cabeza=(-4, -10, 0),
                     _mano_d=(-12, 44, 16), _hoja=(-0.4, -1, 0.5), _filo=(-1, 0, 0), _mano_i=(16, 38, 4))
Anim("saludo", 3.0).poses([
    (0.0, READY),
    (0.55, _saludo),
    (1.5, con(_saludo, cabeza=(14, 0, 0))),
    (1.95, _saludo_abajo),
    (2.4, _saludo_abajo),
    (3.0, READY),
])

# --- combo de tres: diagonal, reves horizontal y mazazo de arriba abajo
_c_arriba_d = dict(PIERNAS, raiz=raiz(0), torso=(-4, 26, 0), cabeza=(0, -20, 0), capa=(-4, 0, 0),
                   _mano_d=(-12, 76, 6), _hoja=(-0.25, 1, -0.6), _filo=(0.6, 0, 1), _dos_manos=True)
_c_diag = dict(raiz=raiz(-3, z=-1.5), muslo_d=(14, 0, 8), pierna_d=(16, 0, 0), muslo_i=(-30, 0, -8), pierna_i=(26, 0, 0),
               torso=(14, -28, 0), cabeza=(-8, 22, 0), capa=(14, 0, 0),
               _mano_d=(10, 40, 22), _hoja=(0.75, -0.6, 0.55), _filo=(0.6, -0.6, 0.2), _dos_manos=True)
_c_izq = dict(_c_diag, torso=(10, -30, 0), _mano_d=(12, 50, 15), _hoja=(1, -0.1, -0.35), _filo=(0, 0, 1))
_c_reves = dict(raiz=raiz(-3, z=-1.5), muslo_d=(16, 0, 8), pierna_d=(16, 0, 0), muslo_i=(-26, 0, -8), pierna_i=(24, 0, 0),
                torso=(10, 30, 0), cabeza=(-6, -24, 0), capa=(18, 0, -4),
                _mano_d=(-14, 52, 18), _hoja=(-1, 0.05, 0.55), _filo=(-0.6, 0, 1), _dos_manos=True)
_c_alto = dict(PIERNAS, raiz=raiz(1), torso=(-14, 0, 0), cabeza=(-14, 0, 0), capa=(-10, 0, 0), capa2=(-6, 0, 0),
               _mano_d=(-2, 92, 4), _hoja=(0, 0.35, -1), _filo=(0, 1, 0), _dos_manos=True)
_c_mazazo = dict(raiz=raiz(-7, z=-3), muslo_d=(26, 0, 8), pierna_d=(30, 0, 0), muslo_i=(-46, 0, -8), pierna_i=(44, 0, 0),
                 torso=(34, 0, 0), cabeza=(-30, 0, 0), capa=(30, 0, 0), capa2=(18, 0, 0),
                 _mano_d=(0, 32, 27), _hoja=(0, -0.55, 1), _filo=(0, -1, 0), _dos_manos=True)
Anim("combo3", 2.9, golpes=(0.55, 1.05, 1.6)).poses([
    (0.0, READY),
    (0.36, _c_arriba_d),
    (0.55, _c_diag),
    (0.78, _c_izq),
    (1.05, _c_reves),
    (1.36, _c_alto),
    (1.6, _c_mazazo),
    (2.15, _c_mazazo),
    (2.9, READY),
])

# --- combo de cinco: diagonal, reves, tajo hacia arriba, giro completo y mazazo
_c_bajo_d = dict(_c_reves, torso=(16, 24, 0), _mano_d=(-12, 36, 16), _hoja=(-0.6, -0.7, 0.4), _filo=(0, -1, 0))
_c_sube = dict(PIERNAS, raiz=raiz(0, z=-1), torso=(-8, -24, 0), cabeza=(-14, 16, 0), capa=(6, 0, 0),
               _mano_d=(8, 76, 14), _hoja=(0.6, 0.8, 0.4), _filo=(0.4, 1, 0), _dos_manos=True)
_giro_a = dict(raiz=raiz(-2, rot=(0, 0, 0)), torso=(8, 30, 0), muslo_d=(10, 0, 10), muslo_i=(-14, 0, -10),
               _mano_d=(-16, 52, 10), _hoja=(-1, 0, -0.2), _filo=(0, 0, 1), _dos_manos=True)
_giro_b = dict(_giro_a, raiz=raiz(-2, rot=(0, -180, 0)), torso=(8, 0, 0), _mano_d=(-6, 52, 22), _hoja=(-0.4, 0, 1), _filo=(-1, 0, 0))
_giro_c = dict(_giro_a, raiz=raiz(-2.5, rot=(0, -360, 0)), torso=(10, -26, 0), _mano_d=(10, 50, 20), _hoja=(1, -0.05, 0.4), _filo=(0, 0, -1))
Anim("combo5", 4.0, golpes=(0.55, 1.05, 1.5, 2.25, 3.05)).poses([
    (0.0, READY),
    (0.36, _c_arriba_d),
    (0.55, _c_diag),
    (0.78, _c_izq),
    (1.05, _c_reves),
    (1.28, _c_bajo_d),
    (1.5, _c_sube),
    (1.8, _giro_a),
    (2.05, _giro_b),
    (2.3, _giro_c),
    (2.55, con(_giro_c, raiz=raiz(-2, rot=(0, -360, 0)), torso=(6, -10, 0))),
    (2.8, con(_c_alto, raiz=raiz(1, rot=(0, -360, 0)))),
    (3.05, con(_c_mazazo, raiz=raiz(-7, z=-3, rot=(0, -360, 0)))),
    (3.45, con(_c_mazazo, raiz=raiz(-7, z=-3, rot=(0, -360, 0)))),
    (4.0, con(READY, raiz=raiz(-0.8, rot=(0, -360, 0)))),
])

# --- contraataque: aparta el golpe y clava la espada hacia delante
_aparta = dict(GUARDIA, torso=(4, 26, 0), _mano_d=(-12, 60, 14), _hoja=(-0.5, 1, 0.3), _filo=(-1, 0, 0.3))
_atras = dict(raiz=raiz(-2), muslo_d=(14, 0, 8), pierna_d=(18, 0, 0), muslo_i=(-22, 0, -8), pierna_i=(20, 0, 0),
              torso=(2, 24, 0), cabeza=(0, -20, 0),
              _mano_d=(-11, 54, 9), _hoja=(0.15, 0.02, 1), _filo=(0, -1, 0), _dos_manos=True)
_estocada = dict(raiz=raiz(-5, z=-5), muslo_d=(30, 0, 8), pierna_d=(24, 0, 0), muslo_i=(-50, 0, -8), pierna_i=(46, 0, 0),
                 torso=(22, -8, 0), cabeza=(-20, 6, 0), capa=(26, 0, 0), capa2=(14, 0, 0),
                 _mano_d=(-1, 50, 31), _hoja=(0.02, -0.08, 1), _filo=(0, -1, 0), _dos_manos=True)
Anim("contraataque", 1.3, golpes=(0.5,)).poses([
    (0.0, GUARDIA),
    (0.12, _aparta),
    (0.32, _atras),
    (0.5, _estocada),
    (0.85, _estocada),
    (1.3, READY),
])

# --- raices: levanta el puno izquierdo y da un manotazo al suelo (las raices salen en linea)
_puno_arriba = dict(PIERNAS, raiz=raiz(1), torso=(-12, -14, 0), cabeza=(-16, 10, 0),
                    _mano_d=(-15, 40, 2), _hoja=(-0.25, -1, -0.5), _filo=(0, 0, 1), _mano_i=(14, 88, 8))
_manotazo = dict(RODILLA, raiz=raiz(-16), torso=(50, -12, 0), cabeza=(-28, 10, 0), capa=(26, 0, 0), capa2=(16, 0, 0),
                 _mano_d=(-17, 28, 6), _hoja=(-0.25, -1, -0.4), _filo=(0, 0, 1), _mano_i=(10, 12, 26))
Anim("raices", 2.3, golpes=(0.72,)).poses([
    (0.0, READY),
    (0.42, _puno_arriba),
    (0.72, _manotazo),
    (1.4, con(_manotazo, cabeza=(-30, 6, 0))),
    (1.85, con(READY, raiz=raiz(-4))),
    (2.3, READY),
])

# --- clavar la espada en el suelo: onda que se expande (hay que saltarla)
_clava_arriba = dict(PIERNAS, raiz=raiz(1), torso=(-10, 0, 0), cabeza=(-6, 0, 0), capa=(-8, 0, 0),
                     _mano_d=(0, 85, 13), _hoja=(0, -1, 0.18), _filo=(-1, 0, 0), _dos_manos=True)
_clavada = dict(raiz=raiz(-9), muslo_d=(-30, 0, 16), pierna_d=(60, 0, 0), muslo_i=(-36, 0, -16), pierna_i=(60, 0, 0),
                torso=(26, 0, 0), cabeza=(-14, 0, 0), capa=(22, 0, 0), capa2=(12, 0, 0),
                _mano_d=(0, 40, 17), _hoja=(0, -1, 0.12), _filo=(-1, 0, 0), _dos_manos=True)
Anim("clavar", 3.1, golpes=(0.85,)).poses([
    (0.0, READY),
    (0.6, _clava_arriba),
    (0.85, _clavada),
    (1.9, con(_clavada, torso=(28, 0, 0))),
    (2.4, con(_clava_arriba, raiz=raiz(-2), _mano_d=(0, 62, 14), torso=(4, 0, 0))),
    (3.1, READY),
])

# --- invocar a los escuderos: la espada al cielo con una mano y luego apuntando al suelo
_al_cielo = dict(PIERNAS, raiz=raiz(1), torso=(-12, 0, 0), cabeza=(-24, 0, 0), capa=(-6, 0, 0),
                 _mano_d=(-8, 96, 6), _hoja=(0, 1, 0.05), _filo=(1, 0, 0), _mano_i=(26, 64, 10))
_al_suelo = dict(raiz=raiz(-3), muslo_d=(10, 0, 12), pierna_d=(12, 0, 0), muslo_i=(-24, 0, -8), pierna_i=(20, 0, 0),
                 torso=(14, 18, 0), cabeza=(6, -12, 0), capa=(10, 0, 0),
                 _mano_d=(-22, 46, 16), _hoja=(-0.55, -1, 0.5), _filo=(-1, 0, 0), _mano_i=(20, 50, 12))
Anim("invocar", 2.7, golpes=(1.3,)).poses([
    (0.0, READY),
    (0.6, _al_cielo),
    (1.05, con(_al_cielo, cabeza=(-28, 0, 0))),
    (1.3, _al_suelo),
    (2.1, _al_suelo),
    (2.7, READY),
])

# --- parada rapida
_para = dict(PIERNAS, raiz=raiz(-2.5, z=1.5), torso=(-6, 0, 0), cabeza=(-6, 0, 0),
             _mano_d=(-3, 66, 16), _hoja=(-1, 0.15, 0.1), _filo=(0, 1, 0), _dos_manos=True)
Anim("bloqueo", 0.6).poses([
    (0.0, READY),
    (0.12, _para),
    (0.32, _para),
    (0.6, READY),
])

# --- aturdido: le rompen la guardia, se tambalea y cae de rodilla con la espada clavada al lado
_tambalea = dict(PIERNAS, raiz=raiz(-1, z=3), torso=(-22, 10, 0), cabeza=(-26, 0, 0), capa=(-20, 0, 0),
                 _mano_d=(-16, 72, -4), _hoja=(-0.3, 0.7, -1), _filo=(0, 1, 0), _mano_i=(26, 60, -4))
_de_rodilla = dict(RODILLA, torso=(24, 0, 0), cabeza=(30, 0, 0), capa=(4, 0, 0), capa2=(8, 0, 0),
                   _mano_d=(-15, 40, 10), _hoja=(-0.1, -1, 0.1), _filo=(0, 0, 1), _mano_i=(9, 24, 20))
Anim("aturdido", 4.2).poses([
    (0.0, READY),
    (0.25, _tambalea),
    (0.7, _de_rodilla),
    (1.6, con(_de_rodilla, torso=(28, 0, 0), cabeza=(34, 4, 0))),
    (2.5, con(_de_rodilla, torso=(23, 0, 0), cabeza=(28, -4, 0))),
    (3.3, con(_de_rodilla, torso=(27, 0, 0), cabeza=(32, 0, 0))),
    (3.75, con(READY, raiz=raiz(-6))),
    (4.2, READY),
])

# --- subir a la rama de un salto (fin de la fase 1 en la parte B1)
_salto_arriba = dict(raiz=raiz(3), muslo_d=(-30, 0, 6), pierna_d=(50, 0, 0), muslo_i=(-50, 0, -6), pierna_i=(70, 0, 0),
                     torso=(-10, 0, 0), cabeza=(-26, 0, 0), capa=(30, 0, 0), capa2=(20, 0, 0),
                     _mano_d=(-14, 58, 4), _hoja=(-0.3, 0.8, 0.1), _filo=(0, 0, 1), _mano_i=(18, 70, 6))
Anim("subir", 1.9, golpes=(0.55,)).poses([
    (0.0, READY),
    (0.4, con(_impulso, raiz=raiz(-10))),
    (0.6, con(_salto_arriba, raiz=raiz(1), muslo_d=(0, 0, 6), pierna_d=(6, 0, 0), muslo_i=(-4, 0, -6), pierna_i=(6, 0, 0))),
    (1.0, _salto_arriba),
    (1.9, _salto_arriba),
])

# --- muerte con honor (parte B3): de rodillas, clava la espada y agacha la cabeza
_muere_a = dict(PIERNAS, raiz=raiz(-1, z=2), torso=(-16, 0, 0), cabeza=(-20, 0, 0), capa=(-10, 0, 0),
                _mano_d=(-10, 50, 14), _hoja=(-0.2, -0.4, 1), _filo=(0, -1, 0), _dos_manos=True)
_de_rodillas = dict(RODILLAS, torso=(10, 0, 0), cabeza=(14, 0, 0), capa=(-4, 0, 0), capa2=(12, 0, 0),
                    _mano_d=(0, 52, 16), _hoja=(0, -1, 0.04), _filo=(-1, 0, 0), _dos_manos=True)
Anim("muerte", 6.0).poses([
    (0.0, READY),
    (0.5, _muere_a),
    (1.4, con(_de_rodillas, torso=(20, 0, 0), _mano_d=(0, 44, 22), _hoja=(0, -0.7, 1))),
    (2.3, _de_rodillas),
    (3.4, con(_de_rodillas, cabeza=(38, 0, 0), torso=(14, 0, 0))),
    (6.0, con(_de_rodillas, cabeza=(40, 0, 0), torso=(15, 0, 0))),
])

# ============================================================================ fase 2 (parte B2)

# --- alzarse del capullo de raices (cambio de fase): se levanta arrancando la espada y ruge con los brazos abiertos
_tenso = con(VIGILIA, torso=(16, 0, 0), cabeza=(32, 0, 0), raiz=raiz(-16))
_se_alza = dict(PIERNAS, raiz=raiz(-3), torso=(-4, 8, 0), cabeza=(-12, -6, 0), capa=(14, 0, 0),
                _mano_d=(-12, 74, 8), _hoja=(-0.15, 1, 0.25), _filo=(0, 0, 1), _dos_manos=True)
_rugido = dict(raiz=raiz(-2.5), muslo_d=(12, 0, 14), pierna_d=(14, 0, 0), muslo_i=(-20, 0, -14), pierna_i=(20, 0, 0),
               torso=(-18, 0, 0), cabeza=(-34, 0, 0), capa=(42, 0, 0), capa2=(26, 0, 0),
               _mano_d=(-24, 88, 4), _hoja=(-0.4, 1, 0.12), _filo=(0, 0, 1), _mano_i=(32, 64, 2))
Anim("alzarse", 4.0, golpes=(1.7,)).poses([
    (0.0, VIGILIA),
    (0.6, _tenso),
    (1.25, _se_alza),
    (1.7, _rugido),
    (2.8, con(_rugido, cabeza=(-30, 4, 0), torso=(-16, 0, 0), capa=(30, 0, 0))),
    (3.4, con(READY, raiz=raiz(-2))),
    (4.0, READY),
])

# --- el Juicio de las espinas: la espada en alto a dos manos (carga), y la clava con todo (las espinas salen)
_juicio_alto = dict(raiz=raiz(-2.5), muslo_d=(10, 0, 10), pierna_d=(14, 0, 0), muslo_i=(-22, 0, -10), pierna_i=(22, 0, 0),
                    torso=(-14, 0, 0), cabeza=(-30, 0, 0), capa=(-6, 0, 0),
                    _mano_d=(0, 90, 8), _hoja=(0, 1, 0.08), _filo=(-1, 0, 0), _dos_manos=True)
Anim("juicio", 5.6, golpes=(4.1,)).poses([
    (0.0, READY),
    (0.6, _juicio_alto),
    (1.6, con(_juicio_alto, torso=(-16, 0, 0), cabeza=(-33, 0, 0), raiz=raiz(-2.8))),
    (2.6, con(_juicio_alto, torso=(-13, 0, 0), cabeza=(-29, 0, 0))),
    (3.6, con(_juicio_alto, torso=(-20, 0, 0), cabeza=(-34, 0, 0), raiz=raiz(-1.5), _mano_d=(0, 93, 4))),
    (4.1, _clavada),
    (4.9, con(_clavada, torso=(28, 0, 0))),
    (5.6, READY),
])

# --- saltar hacia abajo (del balcon al suelo, o salto con golpe hacia alguien atrapado): cae clavando la espada
Anim("caer", 2.4, golpes=(0.5, 1.45)).poses([
    (0.0, READY),
    (0.3, con(_impulso, raiz=raiz(-10))),
    (0.5, con(_aire, raiz=raiz(4), muslo_d=(-10, 0, 8), pierna_d=(10, 0, 0), muslo_i=(-5, 0, -8), pierna_i=(8, 0, 0))),
    (0.95, _aire),
    (1.3, con(_aire, torso=(10, 0, 0), capa=(70, 0, 0), _mano_d=(0, 60, 22), _hoja=(0, -0.3, 1))),
    (1.45, _aterriza),
    (1.9, _aterriza),
    (2.4, READY),
])

# --- subir de un salto al balcon (y quedarse de pie arriba)
_aterriza_pie = dict(AGACHADO, raiz=raiz(-8), torso=(22, 0, 0), cabeza=(-16, 0, 0), capa=(24, 0, 0), capa2=(14, 0, 0),
                     _mano_d=(-6, 40, 18), _hoja=(-0.1, -0.5, 1), _filo=(0, -1, 0), _dos_manos=True)
Anim("subir_balcon", 2.2, golpes=(0.55, 1.4)).poses([
    (0.0, READY),
    (0.4, con(_impulso, raiz=raiz(-10))),
    (0.6, con(_salto_arriba, raiz=raiz(1), muslo_d=(0, 0, 6), pierna_d=(6, 0, 0), muslo_i=(-4, 0, -6), pierna_i=(6, 0, 0))),
    (1.0, _salto_arriba),
    (1.35, con(_salto_arriba, muslo_d=(-40, 0, 6), pierna_d=(40, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(30, 0, 0))),
    (1.45, _aterriza_pie),
    (1.8, _aterriza_pie),
    (2.2, READY),
])

# --- levantarse despues de estar de rodilla con la espada clavada (la marea de raices)
Anim("levantarse", 1.1).poses([
    (0.0, VIGILIA),
    (0.55, _de_pie_rama),
    (1.1, READY),
])

# ============================================================================ mano izquierda pegada a la empunadura

DT = 0.05   # un tick


def _catmull(t, p0, p1, p2, p3):
    return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t)


def muestra(anim, time):
    """Desplazamientos de cada pieza en el instante time, como KeyframeAnimations.animate (Catmull-Rom o lineal)."""
    f = time % anim.length if anim.loop else min(time, anim.length)
    out = {}
    for part, kind, kfs in anim.ch:
        n = len(kfs)
        idx = 0
        while idx < n and not (f <= kfs[idx][0]):
            idx += 1
        i = max(0, idx - 1)
        j = min(n - 1, i + 1)
        k0, k1 = kfs[i], kfs[j]
        fr = 0.0 if j == i else min(1.0, max(0.0, (f - k0[0]) / (k1[0] - k0[0])))
        vals = []
        for c in (1, 2, 3):
            if k1[4] == "l":
                vals.append(k0[c] + (k1[c] - k0[c]) * fr)
            else:
                vals.append(_catmull(fr, kfs[max(0, i - 1)][c], k0[c], k1[c], kfs[min(n - 1, j + 1)][c]))
        out.setdefault(part, {})[kind] = tuple(vals)
    return out


def _pose_de(o):
    p = {}
    for part, d in o.items():
        p[part] = d if (part == "raiz" or "pos" in d) else d.get("rot", (0, 0, 0))
    return p


_DENSO = {}


def densificar(anim):
    """En los tramos a dos manos la mano izquierda se recalcula en cada tick (sigue a la empunadura mientras la
    espada se mueve entre dos poses); el canal del brazo izquierdo queda con un fotograma por tick (lineal)."""
    ts = [t for t, _ in anim.resueltas]
    dm = anim.dos_manos
    if not any(dm):
        return
    n = int(round(anim.length / DT))
    tramos = [(ts[i], ts[i + 1]) for i in range(len(ts) - 1) if dm[i] and dm[i + 1]]
    nuevos = {"brazo_i": [], "antebrazo_i": [], "hombrera_i": []}
    prev = None
    for k in range(n + 1):
        t = min(anim.length, k * DT)
        o = muestra(anim, t)
        en_tramo = any(a - 1e-6 <= t <= b + 1e-6 for a, b in tramos)
        if en_tramo:
            q = _pose_de(o)
            g = ik.punto(q, "espada", ik.AGARRE_I)
            ref = None
            if prev is not None:
                ref = [prev[0][0], prev[0][1], prev[0][2], prev[1]]
            clave = (anim.name, k, tuple(round(float(v), 2) for v in g), tuple(round(float(v), 1) for v in (ref or [])))
            if clave not in _DENSO:
                q2, _e = ik.brazo(q, "i", g, ref=ref, peso_ref=0.004, codo_afuera=1)
                _DENSO[clave] = (tuple(float(v) for v in q2["brazo_i"]), float(q2["antebrazo_i"][0]))
            bi, ai = _DENSO[clave]
        else:
            bi = o.get("brazo_i", {}).get("rot", (0, 0, 0))
            ai = o.get("antebrazo_i", {}).get("rot", (0, 0, 0))[0]
        prev = (bi, ai)
        hi = tuple(max(-55, min(55, bi[i] * kk)) for i, kk in enumerate((0.38, 0.3, 0.42)))
        nuevos["brazo_i"].append((t, bi[0], bi[1], bi[2], "l"))
        nuevos["antebrazo_i"].append((t, ai, 0.0, 0.0, "l"))
        nuevos["hombrera_i"].append((t, hi[0], hi[1], hi[2], "l"))
    anim.ch = [c for c in anim.ch if not (c[0] in nuevos and c[1] == "rot")]
    for p, kfs in nuevos.items():
        anim.ch.append((p, "rot", kfs))


def densificar_todo():
    try:
        with open(CACHE_F + ".denso", "rb") as fh:
            _DENSO.update(pickle.load(fh))
    except Exception:  # noqa: BLE001
        pass
    for a in ANIMS.values():
        densificar(a)
    with open(CACHE_F + ".denso", "wb") as fh:
        pickle.dump(_DENSO, fh)


# ============================================================================ codigo Java

ACTIONS = ["descender", "saludo", "combo3", "combo5", "contraataque", "raices", "clavar", "invocar", "bloqueo",
           "aturdido", "subir", "muerte", "alzarse", "juicio", "caer", "subir_balcon", "levantarse"]
LOOPS = ["posado", "quieto", "andar", "hablar", "guardia", "carga"]


def _n(v, dec=2):
    s = f"{v:.{dec}f}".rstrip("0").rstrip(".")
    return "0" if s in ("-0", "", "-") else s


def _datos(a):
    """Animacion en una cadena: largo;bucle;canal;canal... con canal = pieza:R|P:C|L:t x y z/t x y z..."""
    canales = []
    for part, kind, kfs in a.ch:
        if all(abs(k[1]) < 1e-3 and abs(k[2]) < 1e-3 and abs(k[3]) < 1e-3 for k in kfs):
            continue
        interp = "L" if kfs[-1][4] == "l" else "C"
        ks = "/".join(f"{_n(k[0], 3)} {_n(k[1])} {_n(k[2])} {_n(k[3])}" for k in kfs)
        canales.append(f"{part}:{'R' if kind == 'rot' else 'P'}:{interp}:{ks}")
    return f"{_n(a.length, 3)};{1 if a.loop else 0};" + ";".join(canales)


def write_java(path_client, path_common):
    out = ["package com.darkcraft.client.gavran;\n",
           "import net.minecraft.client.animation.AnimationChannel;",
           "import net.minecraft.client.animation.AnimationDefinition;",
           "import net.minecraft.client.animation.Keyframe;",
           "import net.minecraft.client.animation.KeyframeAnimations;\n",
           "/**",
           " * GENERADO por tools/gavran/generar.py (animaciones.py): no tocar a mano. Los fotogramas van en cadenas que se",
           " * leen al cargar la clase (son muchos: la mano izquierda lleva uno por tick para no soltar la empunadura).",
           " */",
           "public final class GavranAnimaciones {",
           "    private GavranAnimaciones() {}\n"]
    for name in LOOPS + ACTIONS:
        out.append(f"    public static final AnimationDefinition {name.upper()} = leer(\"{_datos(ANIMS[name])}\");\n")
    out.append("    /** Acciones por numero (el 0 no se usa). */")
    out.append("    public static final AnimationDefinition[] ACCIONES = {null, " + ", ".join(n.upper() for n in ACTIONS) + "};\n")
    out += [
        "    private static AnimationDefinition leer(String s) {",
        "        String[] p = s.split(\";\");",
        "        AnimationDefinition.Builder b = AnimationDefinition.Builder.withLength(Float.parseFloat(p[0]));",
        "        if (p[1].equals(\"1\")) b.looping();",
        "        for (int i = 2; i < p.length; i++) {",
        "            String[] c = p[i].split(\":\");",
        "            boolean rot = c[1].equals(\"R\");",
        "            AnimationChannel.Interpolation in = c[2].equals(\"L\") ? AnimationChannel.Interpolations.LINEAR : AnimationChannel.Interpolations.CATMULLROM;",
        "            String[] ks = c[3].split(\"/\");",
        "            Keyframe[] kf = new Keyframe[ks.length];",
        "            for (int k = 0; k < ks.length; k++) {",
        "                String[] v = ks[k].split(\" \");",
        "                float t = Float.parseFloat(v[0]), x = Float.parseFloat(v[1]), y = Float.parseFloat(v[2]), z = Float.parseFloat(v[3]);",
        "                kf[k] = new Keyframe(t, rot ? KeyframeAnimations.degreeVec(x, y, z) : KeyframeAnimations.posVec(x, y, z), in);",
        "            }",
        "            b.addAnimation(c[0], new AnimationChannel(rot ? AnimationChannel.Targets.ROTATION : AnimationChannel.Targets.POSITION, kf));",
        "        }",
        "        return b.build();",
        "    }",
        "}"]
    with open(path_client, "w") as f:
        f.write("\n".join(out) + "\n")
    c = ["package com.darkcraft.gavran;\n",
         "/** GENERADO por tools/gavran/generar.py: numeros, duraciones y golpes (en ticks) de las animaciones de Gavran. */",
         "public final class GavranAnims {",
         "    private GavranAnims() {}\n"]
    for i, n in enumerate(ACTIONS, start=1):
        c.append(f"    public static final int {n.upper()} = {i};")
    c.append("")
    c.append("    /** Duracion de cada accion en ticks (por numero). */")
    c.append("    public static final int[] TICKS = {0, " + ", ".join(str(int(round(ANIMS[n].length * 20))) for n in ACTIONS) + "};")
    c.append("")
    c.append("    /** Ticks (desde que empieza la animacion) en los que la espada llega: ahi pega el servidor. */")
    for n in ACTIONS:
        if n in GOLPES:
            c.append(f"    public static final int[] GOLPES_{n.upper()} = {{{', '.join(str(t) for t in GOLPES[n])}}};")
    c.append("}")
    with open(path_common, "w") as f:
        f.write("\n".join(c) + "\n")


def peores(n=8):
    """Las poses en las que la cinematica inversa no llega (error en pixeles)."""
    return sorted(_PEOR, key=lambda e: -e[0])[:n]
