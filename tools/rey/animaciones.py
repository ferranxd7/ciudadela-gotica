"""Animaciones del rey (fotogramas clave), en un formato corto que se convierte en codigo Java de
Minecraft (AnimationDefinition) y que tambien se puede dibujar con vista.py.

Rotaciones en grados (se SUMAN a la pose inicial de la pieza). Posiciones en pixeles con Y hacia
ARRIBA (como KeyframeAnimations.posVec). Interpolacion: "c" = Catmull-Rom (suave), "l" = lineal.

Recordatorio de ejes (modelo: Y abajo, frente -Z, derecha del rey -X):
  x negativo en brazos/piernas = hacia delante; z positivo = brazo derecho hacia fuera (izquierdo con -)
  y positivo en el torso = gira hacia su derecha; x positivo en la cabeza = mira hacia abajo
"""
import math

ANIMS = {}


class Anim:
    def __init__(self, name, length, loop=False):
        self.name = name
        self.length = length
        self.loop = loop
        self.ch = []  # (pieza, "rot"/"pos", [(t, x, y, z, interp)])
        ANIMS[name] = self

    def rot(self, part, *kfs, i="c"):
        self.ch.append((part, "rot", [(k[0], k[1], k[2], k[3], k[4] if len(k) > 4 else i) for k in kfs]))
        return self

    def pos(self, part, *kfs, i="c"):
        self.ch.append((part, "pos", [(k[0], k[1], k[2], k[3], k[4] if len(k) > 4 else i) for k in kfs]))
        return self

    def poses(self, timeline, i="c"):
        """timeline: [(t, {pieza: (rx, ry, rz) o {"rot":..., "pos":...}})] -> un canal por pieza."""
        parts = {}
        for t, pose in timeline:
            for p in pose:
                parts.setdefault(p, set())
                v = pose[p]
                if isinstance(v, dict):
                    parts[p].update(v.keys())
                else:
                    parts[p].add("rot")
        for p, kinds in parts.items():
            for kind in sorted(kinds):
                kfs = []
                for t, pose in timeline:
                    v = pose.get(p)
                    if isinstance(v, dict):
                        v = v.get(kind)
                    elif kind != "rot":
                        v = None
                    if v is None:
                        v = (0, 0, 0)
                    kfs.append((t, v[0], v[1], v[2], i))
                self.ch.append((p, kind, kfs))
        return self


def catmull(t, p0, p1, p2, p3):
    return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t)


def sample(anim, time, scale=1.0):
    """Desplazamientos de cada pieza en el instante time (segundos), como KeyframeAnimations.animate."""
    f = time % anim.length if anim.loop else time
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
                vals.append((k0[c] + (k1[c] - k0[c]) * fr) * scale)
            else:
                a = kfs[max(0, i - 1)][c]
                d = kfs[min(n - 1, j + 1)][c]
                vals.append(catmull(fr, a, k0[c], k1[c], d) * scale)
        o = out.setdefault(part, {})
        prev = o.get(kind, (0, 0, 0))
        o[kind] = tuple(prev[q] + vals[q] for q in range(3))
    return out


def combine(*offs):
    out = {}
    for o in offs:
        for p, d in o.items():
            q = out.setdefault(p, {})
            for k, v in d.items():
                pv = q.get(k, (0, 0, 0))
                q[k] = tuple(pv[i] + v[i] for i in range(3))
    return out


# ============================================================================ poses base

SIT = {
    "raiz": {"pos": (0, -10, 0.5)},
    "muslo_d": (-88, 6, 0), "muslo_i": (-88, -6, 0),
    "pierna_d": (88, 0, 0), "pierna_i": (88, 0, 0),
    "falda_d": (-78, 0, 0), "falda_i": (-78, 0, 0),
    "falda_ld": (-40, 0, 6), "falda_li": (-40, 0, -6),
    "falda_t": (8, 0, 0),
    "torso": (4, 0, 0),
    "cabeza": (6, 0, 0),
    "brazo_d": (-18, 0, 14), "antebrazo_d": (-52, 0, 0), "espada_d": (108, 0, 0),
    "brazo_i": (-18, 0, -14), "antebrazo_i": (-52, 0, 0), "espada_i": (108, 0, 0),
}

# guardia baja de pie: espadas delante y abajo, un poco abiertas
READY = {
    "brazo_d": (-14, 0, 10), "antebrazo_d": (-30, 0, 0), "espada_d": (34, 8, 0),
    "brazo_i": (-14, 0, -10), "antebrazo_i": (-30, 0, 0), "espada_i": (34, -8, 0),
    "torso": (3, 0, 0),
}


def blend(a, b, k):
    """Mezcla dos poses (k=0 a, k=1 b)."""
    out = {}
    for p in set(a) | set(b):
        va, vb = a.get(p, (0, 0, 0)), b.get(p, (0, 0, 0))
        if isinstance(va, dict) or isinstance(vb, dict):
            da = va if isinstance(va, dict) else {"rot": va}
            db = vb if isinstance(vb, dict) else {"rot": vb}
            out[p] = {kk: tuple(da.get(kk, (0, 0, 0))[i] * (1 - k) + db.get(kk, (0, 0, 0))[i] * k for i in range(3))
                      for kk in set(da) | set(db)}
        else:
            out[p] = tuple(va[i] * (1 - k) + vb[i] * k for i in range(3))
    return out


def with_(base, **over):
    d = dict(base)
    d.update(over)
    return d


# ============================================================================ animaciones

# --- sentado en el trono (bucle, respira despacio)
Anim("sentado", 4.0, loop=True).poses([
    (0.0, SIT),
    (2.0, with_(SIT, torso=(6, 0, 0), cabeza=(8, 0, 0))),
    (4.0, SIT),
])

# --- hablando (se suma a sentado o a quieto): mandibula (barba), cabeza y un gesto con la espada
Anim("hablar", 2.4, loop=True).poses([
    (0.0, {"barba": (0, 0, 0), "cabeza": (0, 0, 0)}),
    (0.3, {"barba": (9, 0, 0), "cabeza": (-4, 6, 0)}),
    (0.55, {"barba": (2, 0, 0), "cabeza": (-2, 4, 0)}),
    (0.8, {"barba": (10, 0, 0), "cabeza": (-6, -4, 0)}),
    (1.1, {"barba": (1, 0, 0), "cabeza": (-3, -6, 0)}),
    (1.4, {"barba": (8, 0, 0), "cabeza": (-5, 0, 0)}),
    (1.8, {"barba": (2, 0, 0), "cabeza": (-2, 3, 0)}),
    (2.1, {"barba": (7, 0, 0), "cabeza": (-4, 2, 0)}),
    (2.4, {"barba": (0, 0, 0), "cabeza": (0, 0, 0)}),
])

# --- de pie, quieto (bucle): respiracion y balanceo de las espadas
Anim("quieto", 3.0, loop=True).poses([
    (0.0, READY),
    (1.5, with_(READY, torso=(5, 0, 0), cabeza=(2, 0, 0), raiz={"pos": (0, -0.3, 0)},
                brazo_d=(-17, 0, 10), brazo_i=(-17, 0, -10))),
    (3.0, READY),
])

# --- andar (bucle; la velocidad la pone el modelo)
_wk = 1.25
Anim("andar", _wk, loop=True).poses([
    (0.0, {"muslo_d": (-28, 0, 0), "muslo_i": (24, 0, 0), "pierna_d": (8, 0, 0), "pierna_i": (22, 0, 0),
           "falda_d": (-16, 0, 0), "falda_i": (10, 0, 0), "falda_t": (6, 0, 0),
           "brazo_d": (10, 0, 0), "brazo_i": (-12, 0, 0), "torso": (0, 5, 0), "raiz": {"pos": (0, 0, 0)}}),
    (_wk * 0.25, {"muslo_d": (0, 0, 0), "muslo_i": (0, 0, 0), "pierna_d": (4, 0, 0), "pierna_i": (34, 0, 0),
                  "falda_d": (-2, 0, 0), "falda_i": (-2, 0, 0), "falda_t": (10, 0, 0),
                  "brazo_d": (0, 0, 0), "brazo_i": (0, 0, 0), "torso": (0, 0, 0), "raiz": {"pos": (0, 0.8, 0)}}),
    (_wk * 0.5, {"muslo_d": (24, 0, 0), "muslo_i": (-28, 0, 0), "pierna_d": (22, 0, 0), "pierna_i": (8, 0, 0),
                 "falda_d": (10, 0, 0), "falda_i": (-16, 0, 0), "falda_t": (6, 0, 0),
                 "brazo_d": (-12, 0, 0), "brazo_i": (10, 0, 0), "torso": (0, -5, 0), "raiz": {"pos": (0, 0, 0)}}),
    (_wk * 0.75, {"muslo_d": (0, 0, 0), "muslo_i": (0, 0, 0), "pierna_d": (34, 0, 0), "pierna_i": (4, 0, 0),
                  "falda_d": (-2, 0, 0), "falda_i": (-2, 0, 0), "falda_t": (10, 0, 0),
                  "brazo_d": (0, 0, 0), "brazo_i": (0, 0, 0), "torso": (0, 0, 0), "raiz": {"pos": (0, 0.8, 0)}}),
    (_wk, {"muslo_d": (-28, 0, 0), "muslo_i": (24, 0, 0), "pierna_d": (8, 0, 0), "pierna_i": (22, 0, 0),
           "falda_d": (-16, 0, 0), "falda_i": (10, 0, 0), "falda_t": (6, 0, 0),
           "brazo_d": (10, 0, 0), "brazo_i": (-12, 0, 0), "torso": (0, 5, 0), "raiz": {"pos": (0, 0, 0)}}),
])

# --- levantarse del trono (desde la pose sentado hasta la guardia)
_lean = with_(SIT, raiz={"pos": (0, -6, -2)}, torso=(-28, 0, 0), cabeza=(-14, 0, 0),
              muslo_d=(-60, 6, 0), muslo_i=(-60, -6, 0), pierna_d=(70, 0, 0), pierna_i=(70, 0, 0),
              falda_d=(-50, 0, 0), falda_i=(-50, 0, 0))
_up = {"raiz": {"pos": (0, 0.5, 0)}, "torso": (-6, 0, 0), "cabeza": (-10, 0, 0),
       "brazo_d": (-160, 0, 22), "antebrazo_d": (-10, 0, 0), "espada_d": (25, 0, 10),
       "brazo_i": (-160, 0, -22), "antebrazo_i": (-10, 0, 0), "espada_i": (25, 0, -10)}
Anim("levantarse", 3.0).poses([
    (0.0, SIT),
    (0.7, _lean),
    (1.4, blend(_lean, READY, 0.8)),
    (2.0, _up),
    (2.5, _up),
    (3.0, READY),
])


# ============================================================================ apuntar las espadas

_DIRS = {
    "delante": (0, 0, -1), "arriba": (0, -1, 0), "abajo": (0, 1, 0), "atras": (0, 0, 1),
    "derecha": (-1, 0, 0), "izquierda": (1, 0, 0),
}


def _dir(v):
    import numpy as np
    if isinstance(v, str):
        acc = np.zeros(3)
        for w in v.split("+"):
            acc += np.array(_DIRS[w.strip()], dtype=float)
        v = acc
    v = np.array(v, dtype=float)
    return v / np.linalg.norm(v)


_aim_cache = {}


def aimed(pose, **targets):
    """Devuelve la pose con la rotacion de espada_d / espada_i elegida para que la hoja apunte a la
    direccion pedida (en el espacio del rey: "delante", "arriba+izquierda", ...)."""
    import numpy as np
    import vista
    pose = dict(pose)
    for part, target in targets.items():
        tgt = _dir(target)
        key = (part, repr(sorted((k, repr(v)) for k, v in pose.items())), tuple(np.round(tgt, 3)))
        if key in _aim_cache:
            pose[part] = _aim_cache[key]
            continue
        best, bestv = None, 9
        offs_base = {k: ({"rot": v} if not isinstance(v, dict) else v) for k, v in pose.items()}
        for ox in range(-180, 181, 6):
            for oz in range(-90, 91, 6):
                o = dict(offs_base)
                o[part] = {"rot": (ox, 0, oz)}
                m = vista.part_matrices(o)[part]
                d = m[:3, :3] @ np.array([0, 0, -1.0])
                err = 1 - float(d @ tgt)
                if err < bestv - 1e-9:
                    bestv, best = err, (ox, 0, oz)
        _aim_cache[key] = best
        pose[part] = best
    return pose


def place(pose, part, target_px, world_rot_deg=(0, 0, 0)):
    """Desplazamiento (pos y rot de animacion) para que la pieza quede en target_px (pixeles, espacio del
    modelo: Y abajo, suelo en y=24) con esa rotacion en el mundo (grados, orden Z-Y-X)."""
    import math
    import numpy as np
    import vista
    from partes import BY_NAME
    offs = {k: ({"rot": v} if not isinstance(v, dict) else v) for k, v in pose.items() if k != part}
    mats = vista.part_matrices(offs)
    pp = BY_NAME[part]
    mp = mats[pp.parent]
    tgt = np.array([target_px[0] / 16, target_px[1] / 16, target_px[2] / 16, 1.0])
    local = np.linalg.inv(mp) @ tgt
    off = local[:3] * 16 - np.array(pp.pivot, dtype=float)
    d = math.pi / 180
    rw = (vista.rz(world_rot_deg[2] * d) @ vista.ry(world_rot_deg[1] * d) @ vista.rx(world_rot_deg[0] * d))[:3, :3]
    rl = np.linalg.inv(mp[:3, :3]) @ rw
    b = -math.asin(max(-1, min(1, rl[2, 0])))
    a = math.atan2(rl[2, 1], rl[2, 2])
    c = math.atan2(rl[1, 0], rl[0, 0])
    base = np.array(pp.rot, dtype=float)
    rot = (a / d - base[0], b / d - base[1], c / d - base[2])
    return {"pos": (off[0], -off[1], off[2]), "rot": rot}


# ============================================================================ combate

def P(*poses, **kw):
    """Pose = READY con cambios (las piezas no nombradas se quedan como en READY). Las poses que se
    pasan se aplican en orden (la ultima manda)."""
    d = dict(READY)
    for p in poses:
        d.update(p)
    d.update(kw)
    return d


# brazos: tajo horizontal con la derecha (de su derecha a su izquierda)
_r_back = dict(torso=(0, 40, 0), brazo_d=(-95, 0, 75), antebrazo_d=(-20, 0, 0), espada_d=(-30, 0, 40))
_r_cut = dict(torso=(6, -38, 0), brazo_d=(-85, 0, -20), antebrazo_d=(-10, 0, 0), espada_d=(-20, 0, -20),
              raiz={"pos": (0, -0.5, -1.5)})
# diagonal con la izquierda (de arriba a abajo)
_l_up = dict(torso=(-6, -25, 0), brazo_i=(-165, 0, -25), antebrazo_i=(-25, 0, 0), espada_i=(10, 0, 0))
_l_cut = dict(torso=(14, 20, 0), brazo_i=(-25, 0, 18), antebrazo_i=(-15, 0, 0), espada_i=(40, 0, 0),
              raiz={"pos": (0, -1, -2)})
# las dos desde arriba
_both_up = dict(torso=(-12, 0, 0), cabeza=(-10, 0, 0), brazo_d=(-170, 0, 12), antebrazo_d=(-15, 0, 0), espada_d=(15, 0, 0),
                brazo_i=(-170, 0, -12), antebrazo_i=(-15, 0, 0), espada_i=(15, 0, 0), raiz={"pos": (0, 0.5, 0)})
_both_cut = dict(torso=(30, 0, 0), cabeza=(-15, 0, 0), brazo_d=(-45, 0, 8), antebrazo_d=(-5, 0, 0), espada_d=(30, 0, 0),
                 brazo_i=(-45, 0, -8), antebrazo_i=(-5, 0, 0), espada_i=(30, 0, 0), raiz={"pos": (0, -3, -2)},
                 muslo_d=(-30, 0, 0), pierna_d=(40, 0, 0), muslo_i=(10, 0, 0), pierna_i=(20, 0, 0))

Anim("combo3", 2.2).poses([
    (0.0, P()),
    (0.32, P(**_r_back)),
    (0.5, P(**_r_cut)),
    (0.68, P(_r_cut, {k: v for k, v in _l_up.items() if k != "torso"})),
    (0.85, P(**_l_up)),
    (1.02, P(**_l_cut)),
    (1.3, P(**_both_up)),
    (1.55, P(**_both_cut)),
    (1.85, P(**_both_cut)),
    (2.2, P()),
])

# salto con golpe al suelo
_crouch = dict(raiz={"pos": (0, -4, 0)}, torso=(22, 0, 0), muslo_d=(-45, 0, 0), muslo_i=(-45, 0, 0),
               pierna_d=(70, 0, 0), pierna_i=(70, 0, 0), falda_d=(-30, 0, 0), falda_i=(-30, 0, 0),
               brazo_d=(35, 0, 20), antebrazo_d=(-20, 0, 0), espada_d=(20, 0, 0),
               brazo_i=(35, 0, -20), antebrazo_i=(-20, 0, 0), espada_i=(20, 0, 0))
_air = dict(raiz={"pos": (0, 2, 0)}, torso=(-10, 0, 0), cabeza=(-10, 0, 0), muslo_d=(-60, 0, 0), muslo_i=(-20, 0, 0),
            pierna_d=(80, 0, 0), pierna_i=(50, 0, 0), falda_d=(-40, 0, 0), falda_i=(-20, 0, 0), falda_t=(30, 0, 0),
            brazo_d=(-175, 0, 15), antebrazo_d=(-10, 0, 0), espada_d=(30, 0, 0),
            brazo_i=(-175, 0, -15), antebrazo_i=(-10, 0, 0), espada_i=(30, 0, 0))
_slam = dict(raiz={"pos": (0, -6, -1)}, torso=(40, 0, 0), cabeza=(-25, 0, 0), muslo_d=(-70, 0, 0), muslo_i=(20, 0, 0),
             pierna_d=(70, 0, 0), pierna_i=(80, 0, 0), falda_d=(-50, 0, 0), falda_i=(5, 0, 0),
             brazo_d=(-40, 0, 10), antebrazo_d=(0, 0, 0), espada_d=(65, 0, 0),
             brazo_i=(-40, 0, -10), antebrazo_i=(0, 0, 0), espada_i=(65, 0, 0))
Anim("salto", 2.0).poses([
    (0.0, P()),
    (0.4, P(**_crouch)),
    (0.6, P(**_air)),
    (1.05, P(**_air)),
    (1.2, P(**_slam)),
    (1.6, P(**_slam)),
    (2.0, P()),
])

# giro con las dos espadas abiertas (dos vueltas)
_spin = aimed(dict(brazo_d=(-10, 0, 85), antebrazo_d=(-5, 0, 0), brazo_i=(-10, 0, -85), antebrazo_i=(-5, 0, 0),
                   torso=(0, 0, 0), muslo_d=(-10, 0, 8), muslo_i=(10, 0, -8)),
              espada_d="derecha+delante", espada_i="izquierda+delante")
Anim("giro", 1.8).poses([
    (0.0, P(raiz={"rot": (0, 0, 0)})),
    (0.3, P(**_spin, raiz={"rot": (0, -30, 0), "pos": (0, -1, 0)})),
    (0.8, P(**_spin, raiz={"rot": (0, 330, 0), "pos": (0, -1, 0)})),
    (1.3, P(**_spin, raiz={"rot": (0, 690, 0), "pos": (0, -1, 0)})),
    (1.8, P(raiz={"rot": (0, 720, 0)})),
], i="l")

# guardia: espadas cruzadas delante de la cara (bucle)
_guard = aimed(dict(torso=(6, 0, 0), cabeza=(8, 0, 0),
                    brazo_d=(-60, -25, -5), antebrazo_d=(-50, 0, 0),
                    brazo_i=(-60, 25, 5), antebrazo_i=(-50, 0, 0),
                    muslo_d=(-20, 0, 0), pierna_d=(25, 0, 0), muslo_i=(15, 0, 0), pierna_i=(10, 0, 0),
                    raiz={"pos": (0, -1, 0)}),
               espada_d="arriba+izquierda+delante", espada_i="arriba+derecha+delante")
Anim("guardia", 1.6, loop=True).poses([
    (0.0, P(**_guard)),
    (0.8, P(**with_(_guard, raiz={"pos": (0, -1.6, 0)}, torso=(9, 0, 0)))),
    (1.6, P(**_guard)),
])

# contraataque: estocada doble desde la guardia
_thrust = aimed(dict(torso=(18, 0, 0), raiz={"pos": (0, -2, -4)},
                     brazo_d=(-88, 6, 4), antebrazo_d=(0, 0, 0),
                     brazo_i=(-88, -6, -4), antebrazo_i=(0, 0, 0),
                     muslo_d=(-45, 0, 0), pierna_d=(30, 0, 0), muslo_i=(25, 0, 0), pierna_i=(10, 0, 0)),
                espada_d="delante", espada_i="delante")
Anim("contraataque", 1.0).poses([
    (0.0, P(**_guard)),
    (0.15, P(**with_(_guard, torso=(-4, 0, 0)))),
    (0.32, P(**_thrust)),
    (0.6, P(**_thrust)),
    (1.0, P()),
])

# tajo a distancia: gran diagonal que lanza una onda
Anim("tajo_lejos", 1.4).poses([
    (0.0, P()),
    (0.45, P(torso=(-8, 45, 0), brazo_d=(-175, 0, 40), antebrazo_d=(-20, 0, 0), espada_d=(20, 0, 0),
             brazo_i=(-30, 0, -30), raiz={"pos": (0, 0, 1)})),
    (0.62, P(torso=(20, -40, 0), brazo_d=(-30, 0, -25), antebrazo_d=(-5, 0, 0), espada_d=(40, 0, -10),
             brazo_i=(10, 0, -40), raiz={"pos": (0, -2, -2)}, muslo_d=(-40, 0, 0), pierna_d=(35, 0, 0))),
    (0.95, P(torso=(20, -40, 0), brazo_d=(-30, 0, -25), antebrazo_d=(-5, 0, 0), espada_d=(40, 0, -10),
             brazo_i=(10, 0, -40), raiz={"pos": (0, -2, -2)}, muslo_d=(-40, 0, 0), pierna_d=(35, 0, 0))),
    (1.4, P()),
])

# grito (cambio de fase): brazos abiertos hacia arriba, cabeza atras
_roar = dict(torso=(-18, 0, 0), cabeza=(-32, 0, 0), barba=(14, 0, 0),
             brazo_d=(-120, 0, 75), antebrazo_d=(-20, 0, 0), espada_d=(0, 0, 30),
             brazo_i=(-120, 0, -75), antebrazo_i=(-20, 0, 0), espada_i=(0, 0, -30),
             muslo_d=(-10, 0, 10), muslo_i=(-10, 0, -10), raiz={"pos": (0, -1, 0)})
Anim("grito", 3.2).poses([
    (0.0, P()),
    (0.4, P(torso=(25, 0, 0), cabeza=(20, 0, 0), raiz={"pos": (0, -3, 0)}, brazo_d=(10, 0, 15), brazo_i=(10, 0, -15))),
    (0.9, P(**_roar)),
    (1.2, P(**with_(_roar, cabeza=(-36, 4, 0)))),
    (1.6, P(**with_(_roar, cabeza=(-34, -4, 0)))),
    (2.4, P(**_roar)),
    (3.2, P()),
])

# ============================================================================ fase 2 (fuego)

_l_back = dict(torso=(0, -40, 0), brazo_i=(-95, 0, -75), antebrazo_i=(-20, 0, 0), espada_i=(-30, 0, -40))
_l_hcut = dict(torso=(6, 38, 0), brazo_i=(-85, 0, 20), antebrazo_i=(-10, 0, 0), espada_i=(-20, 0, 20),
               raiz={"pos": (0, -0.5, -1.5)})
_r_up = dict(torso=(-6, 25, 0), brazo_d=(-165, 0, 25), antebrazo_d=(-25, 0, 0), espada_d=(10, 0, 0))
_r_dcut = dict(torso=(14, -20, 0), brazo_d=(-25, 0, -18), antebrazo_d=(-15, 0, 0), espada_d=(40, 0, 0),
               raiz={"pos": (0, -1, -2)})

Anim("combo5", 2.5).poses([
    (0.0, P()),
    (0.22, P(**_r_back)),
    (0.36, P(**_r_cut)),
    (0.52, P(_r_cut, _l_back)),
    (0.66, P(**_l_hcut)),
    (0.84, P(**_r_up)),
    (0.98, P(**_r_dcut)),
    (1.14, P(_r_dcut, {k: v for k, v in _l_up.items() if k != "torso"})),
    (1.3, P(**_l_cut)),
    (1.55, P(**_both_up)),
    (1.78, P(**_both_cut)),
    (2.1, P(**_both_cut)),
    (2.5, P()),
])

# embestida: inclinado hacia delante, espadas atras (bucle mientras corre)
_charge_a = aimed(dict(torso=(32, 0, 0), cabeza=(-28, 0, 0), raiz={"pos": (0, -1.5, 0)},
                       brazo_d=(40, 0, 25), antebrazo_d=(-10, 0, 0), brazo_i=(40, 0, -25), antebrazo_i=(-10, 0, 0),
                       muslo_d=(-50, 0, 0), pierna_d=(40, 0, 0), muslo_i=(35, 0, 0), pierna_i=(50, 0, 0),
                       falda_d=(-30, 0, 0), falda_i=(20, 0, 0), falda_t=(35, 0, 0)),
                  espada_d="atras+derecha+abajo", espada_i="atras+izquierda+abajo")
_charge_b = with_(_charge_a, muslo_d=(35, 0, 0), pierna_d=(50, 0, 0), muslo_i=(-50, 0, 0), pierna_i=(40, 0, 0),
                  falda_d=(20, 0, 0), falda_i=(-30, 0, 0), raiz={"pos": (0, -0.5, 0)})
Anim("embestida", 0.6, loop=True).poses([
    (0.0, _charge_a), (0.3, _charge_b), (0.6, _charge_a),
])

# pilares: clava las dos espadas en el suelo
_raise2 = aimed(dict(torso=(-14, 0, 0), cabeza=(-12, 0, 0), brazo_d=(-170, 0, 18), antebrazo_d=(-10, 0, 0),
                     brazo_i=(-170, 0, -18), antebrazo_i=(-10, 0, 0), raiz={"pos": (0, 0.5, 0)}),
                espada_d="abajo+delante", espada_i="abajo+delante")
_raise2 = aimed(_raise2, espada_d="arriba", espada_i="arriba")
_stab2 = aimed(dict(torso=(38, 0, 0), cabeza=(-20, 0, 0), raiz={"pos": (0, -5, -1)},
                    brazo_d=(-50, 0, 6), antebrazo_d=(-10, 0, 0), brazo_i=(-50, 0, -6), antebrazo_i=(-10, 0, 0),
                    muslo_d=(-55, 0, 0), pierna_d=(70, 0, 0), muslo_i=(15, 0, 0), pierna_i=(60, 0, 0),
                    falda_d=(-40, 0, 0)),
               espada_d="abajo", espada_i="abajo")
Anim("pilares", 1.7).poses([
    (0.0, P()),
    (0.5, P(**_raise2)),
    (0.78, P(**_stab2)),
    (1.3, P(**_stab2)),
    (1.7, P()),
])

# bolas de fuego: estocada con una y con otra
_th_r = aimed(P(torso=(10, -25, 0), brazo_d=(-88, 0, -5), antebrazo_d=(0, 0, 0), raiz={"pos": (0, -1, -2)},
                muslo_d=(-30, 0, 0), pierna_d=(25, 0, 0)), espada_d="delante")
_th_l = aimed(P(torso=(10, 25, 0), brazo_i=(-88, 0, 5), antebrazo_i=(0, 0, 0), raiz={"pos": (0, -1, -2)},
                muslo_i=(-30, 0, 0), pierna_i=(25, 0, 0)), espada_i="delante")
Anim("bolas", 1.3).poses([
    (0.0, P()),
    (0.18, P(torso=(-4, 20, 0), brazo_d=(30, 0, 20))),
    (0.36, _th_r),
    (0.56, P(torso=(-4, -20, 0), brazo_i=(30, 0, -20))),
    (0.76, _th_l),
    (1.3, P()),
])

# abanico de fuego: barrido horizontal con las dos espadas
_fan_a = aimed(dict(torso=(0, 55, 0), cabeza=(0, -30, 0), brazo_d=(-80, 0, 70), antebrazo_d=(-10, 0, 0),
                    brazo_i=(-90, 40, 0), antebrazo_i=(-30, 0, 0), raiz={"pos": (0, 0, 1)}),
               espada_d="derecha+atras", espada_i="derecha")
_fan_b = aimed(dict(torso=(10, -55, 0), cabeza=(0, 30, 0), brazo_d=(-90, -40, 0), antebrazo_d=(-30, 0, 0),
                    brazo_i=(-80, 0, -70), antebrazo_i=(-10, 0, 0), raiz={"pos": (0, -2, -2)},
                    muslo_d=(-35, 0, 0), pierna_d=(30, 0, 0)),
               espada_d="izquierda", espada_i="izquierda+atras")
Anim("abanico", 1.5).poses([
    (0.0, P()),
    (0.5, _fan_a),
    (0.66, _fan_b),
    (1.05, _fan_b),
    (1.5, P()),
])

# ============================================================================ fase 3 (lava)

_hits = [
    _r_back, _r_cut, _l_back, _l_hcut, _r_up, _r_dcut, _l_up, _l_cut,
]
_c8 = [(0.0, P())]
_t = 0.12
_seq = [(_r_back, _r_cut), (_l_back, _l_hcut), (_r_up, _r_dcut), (_l_up, _l_cut),
        (_r_back, _r_cut), (_l_back, _l_hcut), (_r_up, _r_dcut), (_both_up, _both_cut)]
for k, (a, b) in enumerate(_seq):
    _c8.append((_t, P(**a)))
    _c8.append((_t + 0.11, P(**b)))
    _t += 0.27
_c8.append((_t + 0.25, P(**_both_cut)))
_c8.append((_t + 0.6, P()))
Anim("combo8", round(_t + 0.6, 2)).poses(_c8)

Anim("geiser", 1.6).poses([
    (0.0, P()),
    (0.45, P(**with_(_raise2, raiz={"pos": (0, 1.5, 0)}))),
    (0.7, P(**_stab2)),
    (1.2, P(**_stab2)),
    (1.6, P()),
])

# golpe final: salta muy alto, carga con las espadas arriba y cae
_hover = aimed(dict(raiz={"pos": (0, 6, 0)}, torso=(-10, 0, 0), cabeza=(-18, 0, 0), muslo_d=(-40, 0, 0), muslo_i=(-25, 0, 0),
                    pierna_d=(60, 0, 0), pierna_i=(45, 0, 0), falda_d=(-25, 0, 0), falda_i=(-20, 0, 0), falda_t=(20, 0, 0),
                    brazo_d=(-175, 0, 30), antebrazo_d=(-5, 0, 0), brazo_i=(-175, 0, -30), antebrazo_i=(-5, 0, 0)),
               espada_d="arriba+derecha", espada_i="arriba+izquierda")
Anim("golpe_final", 4.6).poses([
    (0.0, P()),
    (0.45, P(**_crouch)),
    (0.8, P(**_air)),
    (1.4, _hover),
    (2.4, with_(_hover, raiz={"pos": (0, 7, 0)})),
    (3.3, with_(_hover, raiz={"pos": (0, 6, 0)})),
    (3.6, P(**_both_up)),
    (3.75, P(**_slam)),
    (4.15, P(**_slam)),
    (4.6, P()),
])

# ============================================================================ otras

# aturdido: una rodilla en el suelo, cabeza gacha
_knee = aimed(dict(raiz={"pos": (0, -8, 0)}, muslo_d=(-85, 0, 0), pierna_d=(85, 0, 0), muslo_i=(-5, 0, 0), pierna_i=(95, 0, 0),
                   falda_d=(-60, 0, 0), falda_i=(-10, 0, 0), falda_t=(20, 0, 0),
                   torso=(28, 0, 0), cabeza=(25, 0, 0), brazo_d=(-20, 0, 15), antebrazo_d=(-20, 0, 0),
                   brazo_i=(5, 0, -10), antebrazo_i=(0, 0, 0)),
              espada_d="abajo+delante", espada_i="abajo")
Anim("aturdido", 3.2).poses([
    (0.0, P()),
    (0.35, P(**with_(_knee, raiz={"pos": (0, -4, 0)}, torso=(10, 0, 0)))),
    (0.6, _knee),
    (1.6, with_(_knee, cabeza=(30, 8, 0), torso=(32, 0, 0))),
    (2.6, _knee),
    (3.2, P()),
])

# bloqueo rapido (cuando para un golpe)
Anim("bloqueo", 0.55).poses([
    (0.0, P()),
    (0.1, P(**_guard)),
    (0.25, P(**with_(_guard, raiz={"pos": (0, -1.5, 1.5)}, torso=(0, 0, 0)))),
    (0.55, P()),
])

# muerte: de rodillas, la corona se cae, se queda inclinado
_kneel2 = aimed(dict(raiz={"pos": (0, -9, 0)}, muslo_d=(-8, 0, 4), pierna_d=(92, 0, 0), muslo_i=(-8, 0, -4), pierna_i=(92, 0, 0),
                     falda_d=(-10, 0, 0), falda_i=(-10, 0, 0), falda_t=(30, 0, 0), falda_ld=(-10, 0, 0), falda_li=(-10, 0, 0),
                     torso=(18, 0, 0), cabeza=(15, 0, 0),
                     brazo_d=(-5, 0, 12), antebrazo_d=(-15, 0, 0), brazo_i=(-5, 0, -12), antebrazo_i=(-15, 0, 0)),
                espada_d="abajo+derecha", espada_i="abajo+izquierda")
_bow = with_(_kneel2, torso=(34, 0, 0), cabeza=(40, 0, 0), brazo_d=(10, 0, 8), brazo_i=(10, 0, -8))
Anim("muerte", 6.0).poses([
    (0.0, P()),
    (0.45, P(torso=(-18, 0, 0), cabeza=(-20, 0, 0), raiz={"pos": (0, 0, 2)}, brazo_d=(10, 0, 30), brazo_i=(10, 0, -30))),
    (1.3, _kneel2),
    (2.4, with_(_kneel2, cabeza=(22, 0, 0))),
    (2.9, with_(_bow, corona={"pos": (0, 0, 0), "rot": (0, 0, 0)})),
    (3.25, with_(_bow, corona=place(_bow, "corona", (-1, 12, -16), (30, 20, 15)))),
    (3.55, with_(_bow, corona=place(_bow, "corona", (-2, 22.6, -19), (75, 30, 10)))),
    (3.7, with_(_bow, corona=place(_bow, "corona", (-2, 21.5, -19.5), (68, 30, 10)))),
    (3.85, with_(_bow, corona=place(_bow, "corona", (-2, 22.8, -20), (78, 30, 10)))),
    (6.0, with_(_bow, corona=place(_bow, "corona", (-2, 22.8, -20), (78, 30, 10)))),
])


# ============================================================================ codigo Java

# acciones (se sincronizan con un numero); el orden fija el numero (1, 2, ...)
# arrancarse la chaqueta (fase 3, al aterrizar en los volcanes): cruza los brazos al pecho, agarra la chaqueta y
# la arranca abriendo los brazos de golpe (a los 0,75 s la chaqueta sale volando), y se queda rugiendo
_agarra = dict(torso=(22, 0, 0), cabeza=(18, 0, 0), barba=(-6, 0, 0),
               brazo_d=(-95, 0, -55), antebrazo_d=(-75, 0, 0), espada_d=(-60, 0, 0),
               brazo_i=(-95, 0, 55), antebrazo_i=(-75, 0, 0), espada_i=(-60, 0, 0),
               muslo_d=(-25, 0, 6), pierna_d=(30, 0, 0), muslo_i=(-25, 0, -6), pierna_i=(30, 0, 0),
               raiz={"pos": (0, -2.5, 0)})
_arranca = dict(torso=(-24, 0, 0), cabeza=(-30, 0, 0), barba=(18, 0, 0),
                brazo_d=(-40, 0, 95), antebrazo_d=(-10, 0, 0), espada_d=(10, 0, 35),
                brazo_i=(-40, 0, -95), antebrazo_i=(-10, 0, 0), espada_i=(10, 0, -35),
                muslo_d=(-12, 0, 14), muslo_i=(-12, 0, -14), raiz={"pos": (0, -0.5, 0)})
Anim("arrancar", 2.6).poses([
    (0.0, P()),
    (0.3, P(**with_(_agarra, torso=(12, 0, 0), raiz={"pos": (0, -1, 0)}))),
    (0.6, P(**_agarra)),
    (0.75, P(**_arranca)),
    (1.3, P(**with_(_arranca, cabeza=(-36, 5, 0)))),
    (1.8, P(**with_(_arranca, cabeza=(-34, -5, 0)))),
    (2.6, P()),
])

ACTIONS = ["levantarse", "combo3", "salto", "giro", "contraataque", "tajo_lejos", "grito", "combo5", "pilares",
           "bolas", "abanico", "combo8", "geiser", "golpe_final", "aturdido", "bloqueo", "muerte", "arrancar"]
# estados que se repiten mientras duran
LOOPS = ["sentado", "quieto", "andar", "hablar", "guardia", "embestida"]


def _f(v):
    s = f"{v:.3f}".rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "F"


def _java_anim(a):
    lines = [f"AnimationDefinition.Builder.withLength({_f(a.length)})" + (".looping()" if a.loop else "")]
    for part, kind, kfs in a.ch:
        # canales que no se mueven nada no hacen falta
        if all(abs(k[1]) < 1e-4 and abs(k[2]) < 1e-4 and abs(k[3]) < 1e-4 for k in kfs):
            continue
        target = "ROTATION" if kind == "rot" else ("POSITION" if kind == "pos" else "SCALE")
        vec = "degreeVec" if kind == "rot" else "posVec"
        ks = ", ".join(
            f"new Keyframe({_f(k[0])}, KeyframeAnimations.{vec}({_f(k[1])}, {_f(k[2])}, {_f(k[3])}), "
            f"AnimationChannel.Interpolations.{'LINEAR' if k[4] == 'l' else 'CATMULLROM'})" for k in kfs)
        lines.append(f"            .addAnimation(\"{part}\", new AnimationChannel(AnimationChannel.Targets.{target}, {ks}))")
    lines.append("            .build()")
    return "\n".join(lines)


def write_java(path_client, path_common=None):
    out = ["package com.ciudadela.client.rey;\n",
           "import net.minecraft.client.animation.AnimationChannel;",
           "import net.minecraft.client.animation.AnimationDefinition;",
           "import net.minecraft.client.animation.Keyframe;",
           "import net.minecraft.client.animation.KeyframeAnimations;\n",
           "/** GENERADO por tools/rey/generar.py (animaciones.py): no tocar a mano. */",
           "public final class ReyAnimaciones {",
           "    private ReyAnimaciones() {}\n"]
    for name in LOOPS + ACTIONS:
        out.append(f"    public static final AnimationDefinition {name.upper()} = {_java_anim(ANIMS[name])};\n")
    out.append("    /** Acciones por numero (el 0 no se usa). */")
    out.append("    public static final AnimationDefinition[] ACCIONES = {null, " + ", ".join(n.upper() for n in ACTIONS) + "};")
    out.append("}")
    with open(path_client, "w") as f:
        f.write("\n".join(out) + "\n")
    if path_common:
        c = ["package com.ciudadela.rey;\n",
             "/** GENERADO por tools/rey/generar.py: numeros y duraciones (en ticks) de las animaciones del rey. */",
             "public final class ReyAnims {",
             "    private ReyAnims() {}\n"]
        for i, n in enumerate(ACTIONS, start=1):
            c.append(f"    public static final int {n.upper()} = {i};")
        c.append("")
        c.append("    /** Duracion de cada accion en ticks (por numero). */")
        c.append("    public static final int[] TICKS = {0, " + ", ".join(str(int(round(ANIMS[n].length * 20))) for n in ACTIONS) + "};")
        c.append("}")
        with open(path_common, "w") as f:
            f.write("\n".join(c) + "\n")
