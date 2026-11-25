"""
Cinematica inversa para los brazos del esqueleto de tools/enemigos: las poses de los golpes se escriben diciendo DONDE
va la mano y HACIA DONDE apunta el arma, y de aqui salen los angulos del hombro, el codo y la muneca.

Claves de una pose (en "coordenadas de persona", pixeles desde el suelo bajo el centro del cuerpo:
x = hacia SU IZQUIERDA, y = hacia ARRIBA, z = hacia DELANTE):
    _mano_d: (x, y, z)        donde va el puno derecho            _mano_i: lo mismo para el izquierdo
    _arma:   (x, y, z)        hacia donde apunta lo que lleva la mano derecha (no hace falta que mida 1)
    _arma_i: (x, y, z)        idem para la izquierda
    _agarra: d                la mano izquierda agarra el asta del arma d pixeles por detras de la derecha
    _codo_d / _codo_i: (x, y, z)   hacia donde tira el codo (por defecto, hacia fuera y abajo)
Lo demas de la pose (raiz, torso, piernas...) va en angulos, como siempre, y se tiene en cuenta.
"""
import math

import numpy as np
from scipy.optimize import least_squares

import vista

LADOS = {"d": -1.0, "i": 1.0}


def _a_modelo(p):
    return np.array([p[0] / 16.0, (24.0 - p[1]) / 16.0, -p[2] / 16.0])


def _dir_modelo(d):
    v = np.array([d[0], -d[1], -d[2]], dtype=float)
    return v / np.linalg.norm(v)


def _pose_con(pose, lado, q):
    p = dict(pose)
    p[f"brazo_{lado}"] = (q[0], q[1], q[2])
    p[f"antebrazo_{lado}"] = (q[3], 0.0, 0.0)
    p[f"mano_{lado}"] = (q[4], q[5], 0.0)
    return p


def _limpia(pose):
    return {k: v for k, v in pose.items() if not k.startswith("_")}


def _resolver_brazo(rig, pose, lado, mano, arma, codo, ref):
    """Los angulos de brazo, antebrazo y mano de un lado para que el puno llegue a `mano` y el arma apunte a `arma`."""
    s = LADOS[lado]
    base = _limpia(pose)
    if len(mano) == 4:
        # ("h", x, y, z): respecto al hombro de ese lado, tal como queda con el resto de la pose
        h = vista.matrices(rig, base)[f"brazo_{lado}"][:3, 3]
        mano = (h[0] * 16.0 + mano[1], 24.0 - h[1] * 16.0 + mano[2], -h[2] * 16.0 + mano[3])
    objetivo = _a_modelo(mano)
    direccion = _dir_modelo(arma) if arma is not None else None
    tira = _dir_modelo(codo) if codo is not None else _dir_modelo((s * 0.8, -0.6, -0.3))
    if ref is None:
        ref = (-30.0, 0.0, -s * 10.0, -40.0, 20.0, 0.0)

    def res(q):
        m = vista.matrices(rig, _pose_con(base, lado, q))
        mm = m[f"mano_{lado}"]
        r = list((mm[:3, 3] - objetivo) * 16.0)
        if direccion is not None:
            d = mm[:3, :3] @ np.array([0.0, 0.0, -1.0])
            r += list((d - direccion) * 9.0)
        # el codo, hacia donde tira (flojo: solo desempata)
        c = m[f"antebrazo_{lado}"][:3, 3]
        h = m[f"brazo_{lado}"][:3, 3]
        medio = (h + mm[:3, 3]) / 2.0
        v = c - medio
        n = np.linalg.norm(v)
        if n > 1e-6:
            r += list((v / n - tira) * 0.6)
        # que no se aleje sin motivo de la pose anterior, y la muneca, lo menos torcida que pueda
        r += [(q[i] - ref[i]) * 0.006 for i in range(6)]
        r.append(q[5] * 0.012)
        return r

    lo, hi = [-200, -110, -110, -150, -200, -120], [80, 110, 110, 0, 200, 120]
    mejor = None
    for semilla in (ref, (-60.0, 0.0, -s * 20.0, -60.0, 30.0, 0.0), (-120.0, 0.0, -s * 30.0, -30.0, 60.0, 0.0), (20.0, 0.0, -s * 30.0, -90.0, 40.0, 0.0)):
        x0 = np.clip(np.array(semilla, dtype=float), np.array(lo) + 0.01, np.array(hi) - 0.01)
        sol = least_squares(res, x0, bounds=(lo, hi), xtol=1e-6, ftol=1e-6)
        if mejor is None or sol.cost < mejor.cost:
            mejor = sol
        # si partiendo de la pose anterior ya llega (la mano y hacia donde apunta), no se buscan otras: asi va seguido
        if _error(rig, base, lado, mejor.x, objetivo, direccion) < 0.35:
            break
    q = mejor.x
    m = vista.matrices(rig, _pose_con(base, lado, q))
    err = float(np.linalg.norm((m[f"mano_{lado}"][:3, 3] - objetivo) * 16.0))
    return tuple(float(round(x, 2)) for x in q), err, m


def _error(rig, base, lado, q, objetivo, direccion):
    m = vista.matrices(rig, _pose_con(base, lado, q))
    mm = m[f"mano_{lado}"]
    e = float(np.linalg.norm((mm[:3, 3] - objetivo) * 16.0))
    if direccion is not None:
        e += float(np.linalg.norm(mm[:3, :3] @ np.array([0.0, 0.0, -1.0]) - direccion)) * 4.0
    return e


PEORES = []


def resolver(rig, pose, ref=None):
    """La pose con los brazos resueltos (sin las claves que empiezan por _). ref: la pose resuelta anterior."""
    out = _limpia(pose)

    def de(lado):
        if ref is None or f"brazo_{lado}" not in ref:
            return None
        b, a, m = ref[f"brazo_{lado}"], ref.get(f"antebrazo_{lado}", (0, 0, 0)), ref.get(f"mano_{lado}", (0, 0, 0))
        return (b[0], b[1], b[2], a[0], m[0], m[1])

    mats = None
    if "_mano_d" in pose:
        q, err, mats = _resolver_brazo(rig, out, "d", pose["_mano_d"], pose.get("_arma"), pose.get("_codo_d"), de("d"))
        out = _pose_con(out, "d", q)
        PEORES.append((err, "d", pose["_mano_d"], pose.get("_arma")))
    if "_agarra" in pose:
        if mats is None:
            mats = vista.matrices(rig, out)
        mm = mats["mano_d"]
        d = mm[:3, :3] @ np.array([0.0, 0.0, 1.0])            # hacia la cola del arma
        p = mm[:3, 3] + d * (pose["_agarra"] / 16.0)
        persona = (p[0] * 16.0, 24.0 - p[1] * 16.0, -p[2] * 16.0)
        q, err, _ = _resolver_brazo(rig, out, "i", persona, None, pose.get("_codo_i"), de("i"))
        out = _pose_con(out, "i", q)
        PEORES.append((err, "i", persona))
    elif "_mano_i" in pose:
        q, err, _ = _resolver_brazo(rig, out, "i", pose["_mano_i"], pose.get("_arma_i"), pose.get("_codo_i"), de("i"))
        out = _pose_con(out, "i", q)
        PEORES.append((err, "i", pose["_mano_i"]))
    return out


def peores(n=5):
    return sorted(PEORES, key=lambda e: -e[0])[:n]
