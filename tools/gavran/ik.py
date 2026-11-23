"""Cinematica inversa para posar a Gavran: se dice donde va la mano derecha, hacia donde apunta la hoja del
mandoble (y hacia donde mira el filo) y si la mano izquierda agarra la empunadura, y aqui se calculan los
angulos de los brazos y de la espada (los mismos que usa Minecraft: rotacion Z-Y-X sumada a la de la pieza).

Coordenadas "intuitivas" para los objetivos (en pixeles, desde el pie de la raiz): (izquierda, arriba, delante).
Internamente el modelo usa (x, y, z) con y hacia abajo y delante = -z: (x, -arriba, -delante) respecto a la raiz.
"""
import math

import numpy as np
from scipy.optimize import minimize

import vista
from partes import BY_NAME

D = math.pi / 180

# centro del puno (en la pieza del antebrazo) y punto de la empunadura donde agarra la izquierda (en la espada)
PUNO = np.array([0.0, 16.5, 0.0])
AGARRE_I = np.array([0.0, 0.0, 9.0])


def a_modelo(p):
    """(izquierda, arriba, delante) -> coordenadas locales de la raiz (x, y abajo, z detras)."""
    return np.array([p[0], -p[1], -p[2]], dtype=float)


def direccion(v):
    """(izquierda, arriba, delante) -> vector unitario en el espacio de la raiz."""
    w = np.array([v[0], -v[1], -v[2]], dtype=float)
    return w / np.linalg.norm(w)


def _rot(a, b, c):
    return (vista.rz(c * D) @ vista.ry(b * D) @ vista.rx(a * D))


def cadena(pose, nombre):
    """Matriz 4x4 (en bloques) de la pieza en el espacio de la raiz (sin la raiz), con la pose dada."""
    chain = []
    p = BY_NAME[nombre]
    while p.parent:
        chain.append(p)
        p = BY_NAME[p.parent]
    m = np.eye(4)
    # la raiz: solo su desplazamiento y giro de animacion (su pivote no cuenta: espacio de la raiz)
    r = pose.get("raiz")
    if isinstance(r, dict):
        if "pos" in r:
            m = vista.tr(r["pos"][0] / 16, -r["pos"][1] / 16, r["pos"][2] / 16)
        if "rot" in r:
            m = m @ _rot(*r["rot"])
    elif r is not None:
        m = _rot(*r)
    for q in reversed(chain):
        o = pose.get(q.name, (0, 0, 0))
        rot = o if not isinstance(o, dict) else o.get("rot", (0, 0, 0))
        pos = (0, 0, 0) if not isinstance(o, dict) else o.get("pos", (0, 0, 0))
        px, py, pz = q.pivot
        px += pos[0]
        py -= pos[1]
        pz += pos[2]
        a, b, c = (q.rot[i] + rot[i] for i in range(3))
        m = m @ vista.tr(px / 16, py / 16, pz / 16) @ _rot(a, b, c)
    return m


def punto(pose, nombre, local_px):
    m = cadena(pose, nombre)
    v = m @ np.array([local_px[0] / 16, local_px[1] / 16, local_px[2] / 16, 1.0])
    return v[:3] * 16


def _set(pose, part, rot):
    o = pose.get(part)
    if isinstance(o, dict):
        o = dict(o)
        o["rot"] = tuple(rot)
        pose[part] = o
    else:
        pose[part] = tuple(rot)


def _get(pose, part):
    o = pose.get(part, (0, 0, 0))
    return tuple(o.get("rot", (0, 0, 0))) if isinstance(o, dict) else tuple(o)


# cuerpo (en el espacio del torso): cajas que los brazos no deben atravesar (un poco infladas)
_CUERPO = [(-14, -32, -9, 14, -12, 9), (-11, -14, -7.5, 11, 0, 7.5), (-12.5, -3, -8.5, 12.5, 4, 8.5)]
# puntos del brazo (en su pieza) que se comprueban
_PUNTOS = [("brazo", (0, 8, 0)), ("brazo", (0, 15, 0)), ("antebrazo", (0, 5, 0)), ("antebrazo", (0, 10, 0))]


def _penetra(q, lado):
    """Cuanto se meten los brazos en el cuerpo (suma de profundidades al cuadrado)."""
    mt = np.linalg.inv(cadena(q, "torso"))
    tot = 0.0
    for pieza, lp in _PUNTOS:
        w = punto(q, f"{pieza}_{lado}", lp)
        t = mt @ np.array([w[0] / 16, w[1] / 16, w[2] / 16, 1.0])
        x, y, z = t[:3] * 16
        for (x0, y0, z0, x1, y1, z1) in _CUERPO:
            if x0 < x < x1 and y0 < y < y1 and z0 < z < z1:
                tot += min(x - x0, x1 - x, y - y0, y1 - y, z - z0, z1 - z) ** 2
    return tot


def brazo(pose, lado, objetivo_px, ref=None, peso_ref=0.002, codo_afuera=0.0):
    """Angulos del brazo (brazo: rx, ry, rz; antebrazo: rx) para que el centro del puno llegue a objetivo_px
    (espacio de la raiz, coordenadas del modelo). Devuelve la pose cambiada."""
    b, a = f"brazo_{lado}", f"antebrazo_{lado}"
    s = -1 if lado == "d" else 1
    x0 = list(_get(pose, b)) + [_get(pose, a)[0]]
    ref = ref if ref is not None else x0
    obj = np.asarray(objetivo_px, dtype=float)

    def f(x):
        q = dict(pose)
        _set(q, b, (x[0], x[1], x[2]))
        _set(q, a, (x[3], 0, 0))
        p = punto(q, a, PUNO)
        e = np.sum((p - obj) ** 2)
        r = sum((x[i] - ref[i]) ** 2 for i in range(4)) * peso_ref
        # el codo hacia fuera (un poco) queda mas natural
        r += codo_afuera * (x[2] * s - 10) ** 2 * 0.001 if codo_afuera else 0
        return e + r + 4.0 * _penetra(q, lado)

    bounds = [(-220, 90), (-100, 100), (-130, 130), (-155, 2)]
    best = None
    for x_ini in (x0, ref, [-60, 0, 10 * -s, -40], [-120, 0, 30 * -s, -20], [-30, 30 * s, -20 * s, -80],
                  [-70, -40 * s, -10 * s, -70], [-50, 40 * s, 10 * s, -90]):
        r = minimize(f, x_ini, method="L-BFGS-B", bounds=bounds)
        if best is None or r.fun < best.fun:
            best = r
    x = best.x
    q = dict(pose)
    _set(q, b, (x[0], x[1], x[2]))
    _set(q, a, (x[3], 0, 0))
    return q, math.sqrt(max(0.0, np.sum((punto(q, a, PUNO) - obj) ** 2)))


def orientar_espada(pose, hoja, filo):
    """Rotacion de la pieza 'espada' para que la hoja (su -Z) apunte a 'hoja' y el filo (su +Y) hacia 'filo'
    (direcciones en el espacio de la raiz). Devuelve la pose cambiada."""
    m_padre = cadena(pose, "antebrazo_d")[:3, :3]
    zf = -np.asarray(hoja, dtype=float)
    zf /= np.linalg.norm(zf)
    yf = np.asarray(filo, dtype=float)
    yf = yf - zf * (yf @ zf)
    n = np.linalg.norm(yf)
    if n < 1e-6:
        yf = np.array([0.0, 1.0, 0.0]) - zf * zf[1]
        n = np.linalg.norm(yf)
    yf /= n
    xf = np.cross(yf, zf)
    rw = np.stack([xf, yf, zf], axis=1)      # columnas: ejes locales de la espada en el mundo
    rl = np.linalg.inv(m_padre) @ rw
    b = -math.asin(max(-1.0, min(1.0, rl[2, 0])))
    a = math.atan2(rl[2, 1], rl[2, 2])
    c = math.atan2(rl[1, 0], rl[0, 0])
    base = BY_NAME["espada"].rot
    q = dict(pose)
    _set(q, "espada", (a / D - base[0], b / D - base[1], c / D - base[2]))
    return q


def resolver(pose, ref=None):
    """Pose con claves especiales -> pose de piezas:
      _mano_d: (izq, arriba, delante) del puno derecho      _hoja: direccion de la hoja   _filo: hacia donde el filo
      _dos_manos: la izquierda agarra la empunadura         _mano_i: (izq, arriba, delante) del puno izquierdo
    ref: pose resuelta anterior (para que los angulos cambien poco de una a otra)."""
    q = {k: v for k, v in pose.items() if not k.startswith("_")}
    ref = ref or {}
    errs = []
    # los objetivos giran con la raiz (si la raiz gira, por ejemplo en el giro del combo de cinco)
    rr = q.get("raiz")
    rr = rr.get("rot", (0, 0, 0)) if isinstance(rr, dict) else (rr or (0, 0, 0))
    R = _rot(*rr)[:3, :3]

    def obj(p):
        return R @ a_modelo(p)

    def dir_(v):
        return R @ direccion(v)

    if "_mano_d" in pose:
        r = list(_get(ref, "brazo_d")) + [_get(ref, "antebrazo_d")[0]] if ref else None
        q, e = brazo(q, "d", obj(pose["_mano_d"]), ref=r, codo_afuera=1)
        errs.append(e)
    if "_hoja" in pose:
        q = orientar_espada(q, dir_(pose["_hoja"]), dir_(pose.get("_filo", (0, 1, 0))))
    if pose.get("_dos_manos"):
        g = punto(q, "espada", AGARRE_I)
        r = list(_get(ref, "brazo_i")) + [_get(ref, "antebrazo_i")[0]] if ref else None
        q, e = brazo(q, "i", g, ref=r, codo_afuera=1)
        errs.append(e)
    elif "_mano_i" in pose:
        r = list(_get(ref, "brazo_i")) + [_get(ref, "antebrazo_i")[0]] if ref else None
        q, e = brazo(q, "i", obj(pose["_mano_i"]), ref=r, codo_afuera=1)
        errs.append(e)
    q["_err"] = max(errs) if errs else 0.0
    return q
