"""
Animaciones por fotogramas clave para los modelos de tools/enemigos, que se convierten en las AnimationDefinition de
Minecraft. Una pose es {pieza: (rx, ry, rz)} en grados (se suman a la pose de reposo de la pieza), o
{pieza: {"rot": (...), "pos": (x, y hacia ARRIBA, z)}} en pixeles.

Ejes (modelo: Y abajo, el frente mira a -Z, la derecha del personaje es -X):
  brazo / muslo x NEGATIVO = hacia DELANTE (levantar el brazo al frente, dar el paso); x positivo = hacia atras
  antebrazo x NEGATIVO = doblar el codo (la mano sube hacia delante); pierna x POSITIVO = doblar la rodilla
  torso x positivo = inclinarse hacia delante; torso / cabeza y positivo = girar hacia su DERECHA (el hombro derecho
  se va atras: asi se arma un golpe con la derecha; y negativo = el hombro derecho va delante: asi se descarga)
  cabeza x positivo = mirar abajo; brazo_d z positivo = separar el brazo derecho del cuerpo (brazo_i: z negativo)

`muestra(t)` da la pose en el segundo t igual que la calcularia el juego (misma interpolacion), para dibujarla con
vista.py sin abrir Minecraft. Los golpes de cada ataque (ticks desde que empieza) se exportan para el servidor.
"""


import os
import pickle

_CACHE_F = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".ik_cache.pkl")
try:
    with open(_CACHE_F, "rb") as _fh:
        _CACHE = pickle.load(_fh)
except Exception:  # noqa: BLE001
    _CACHE = {}
_NUEVAS = [0]


def resolver(rig, pose, ref=None):
    """ik.resolver con memoria (la cinematica inversa tarda): la clave es la pose, el modelo y los brazos de la anterior."""
    import ik
    brazos = sorted((k, repr(v)) for k, v in (ref or {}).items() if k.startswith(("brazo", "antebrazo", "mano")))
    k = rig.nombre + "|" + repr(sorted((a, repr(b)) for a, b in pose.items())) + "|" + repr(brazos)
    if k not in _CACHE:
        _CACHE[k] = ik.resolver(rig, pose, ref)
        _NUEVAS[0] += 1
    return dict(_CACHE[k])


def guardar_cache():
    if _NUEVAS[0]:
        with open(_CACHE_F, "wb") as fh:
            pickle.dump(_CACHE, fh)


def catmull(t, p0, p1, p2, p3):
    return 0.5 * (2 * p1 + (p2 - p0) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (3 * p1 - p0 - 3 * p2 + p3) * t * t * t)


def _partes(v):
    """(rot, pos) de lo que lleve una pieza en una pose."""
    if isinstance(v, dict):
        return v.get("rot"), v.get("pos")
    return v, None


def con(base, *mas, **over):
    """Una pose a partir de otra, con cambios."""
    d = dict(base)
    for m in mas:
        d.update(m)
    d.update(over)
    return d


def sin(pose, *claves):
    """La pose sin esas claves (para soltar un objetivo heredado, como _agarra)."""
    return {k: v for k, v in pose.items() if k not in claves}


def raiz(y=0.0, z=0.0, x=0.0, rot=None):
    d = {"pos": (x, y, z)}
    if rot:
        d["rot"] = rot
    return d


def espejo(pose):
    """La misma pose con la derecha y la izquierda cambiadas."""
    out = {}
    for k, v in pose.items():
        if k.endswith("_d"):
            k2 = k[:-2] + "_i"
        elif k.endswith("_i"):
            k2 = k[:-2] + "_d"
        else:
            k2 = k
        rot, pos = _partes(v)
        r2 = None if rot is None else (rot[0], -rot[1], -rot[2])
        p2 = None if pos is None else (-pos[0], pos[1], pos[2])
        out[k2] = r2 if pos is None else ({"pos": p2} if rot is None else {"rot": r2, "pos": p2})
    return out


BRAZOS = {"d": ("brazo_d", "antebrazo_d", "mano_d"), "i": ("brazo_i", "antebrazo_i", "mano_i")}


def _v3(v):
    return (float(v[0]), float(v[1]), float(v[2]))


def _mezcla_objetivo(a, b, d):
    """Entre dos objetivos de mano (los dos respecto al hombro, o los dos absolutos)."""
    if len(a) != len(b):
        return b if d > 0.5 else a
    if len(a) == 4:
        return ("h",) + tuple(a[k] + (b[k] - a[k]) * d for k in (1, 2, 3))
    return tuple(a[k] + (b[k] - a[k]) * d for k in range(3))


def _mezcla_dir(a, b, d):
    import math
    na = math.sqrt(sum(x * x for x in a))
    nb = math.sqrt(sum(x * x for x in b))
    v = [a[k] / na + (b[k] / nb - a[k] / na) * d for k in range(3)]
    n = math.sqrt(sum(x * x for x in v)) or 1.0
    return tuple(x / n for x in v)


class Anim:
    def __init__(self, nombre, largo, bucle=False, golpes=(), interp="c"):
        self.nombre = nombre
        self.largo = float(largo)
        self.bucle = bucle
        self.interp = interp          # "c" = Catmull-Rom (suave), "l" = lineal
        self.golpes = [int(round(t * 20)) for t in golpes]
        self.ch = {}                  # (pieza, "rot" | "pos") -> [(t, x, y, z)]
        self.modo = {}                # (pieza, tipo) -> "c" | "l" (si no esta, el de la animacion)

    def _canales(self, lista, modo=None, solo=None):
        claves = set()
        for _, pose in lista:
            for p, v in pose.items():
                if solo is not None and p not in solo:
                    continue
                rot, pos = _partes(v)
                if rot is not None:
                    claves.add((p, "rot"))
                if pos is not None:
                    claves.add((p, "pos"))
        for p, tipo in sorted(claves):
            kfs = []
            for t, pose in lista:
                rot, pos = _partes(pose.get(p, (0, 0, 0)))
                v = rot if tipo == "rot" else pos
                if v is None:
                    v = (0, 0, 0)
                kfs.append((float(t), float(v[0]), float(v[1]), float(v[2])))
            self.ch[(p, tipo)] = kfs
            if modo:
                self.modo[(p, tipo)] = modo

    def poses(self, lista, rig=None, solo=None):
        """
        lista: [(segundo, pose)]. Lo que una pose no nombre se queda en reposo (0). Las poses con objetivos (_mano_d,
        _arma...: ver ik.py) se resuelven en orden, cada una partiendo de la anterior; para eso hace falta el `rig`.
        Entre dos poses con objetivo para un brazo, ese brazo lleva un fotograma por tick (se resuelve el objetivo
        intermedio): asi el arma no hace cosas raras por el camino.
        solo: si se da, solo se guardan los canales de esas piezas (para una animacion que se suma a otra).
        """
        con_ik = rig is not None and any(k.startswith("_") for _, p in lista for k in p)
        if not con_ik:
            self.resueltas = lista
            self._canales(lista, solo=solo)
            return self
        specs = lista
        res, prev = [], None
        for t, pose in specs:
            q = resolver(rig, pose, prev) if any(k.startswith("_") for k in pose) else dict(pose)
            res.append((t, q))
            prev = q
        self.resueltas = res
        # los brazos que van con objetivo en dos poses seguidas: un fotograma por tick
        densos = set()
        for lado, clave in (("d", ("_mano_d",)), ("i", ("_agarra", "_mano_i"))):
            for k in range(len(specs) - 1):
                if any(c in specs[k][1] for c in clave) and any(c in specs[k + 1][1] for c in clave):
                    densos.add(lado)
        piezas_densas = set()
        for lado in densos:
            piezas_densas.update(BRAZOS[lado])
        self._canales(res, solo=None if solo is None else [p for p in solo if p not in piezas_densas])
        if solo is not None:
            piezas_densas = {p for p in piezas_densas if p in solo}
        if not piezas_densas:
            return self
        ralo = Anim(self.nombre + "_ralo", self.largo, self.bucle)
        ralo._canales(res)
        n = int(round(self.largo * 20))
        ticks = []
        prev = None
        for kt in range(n + 1):
            tt = min(self.largo, kt / 20.0)
            base = {p: (v if "pos" in v else v["rot"]) for p, v in ralo.muestra(tt).items()}
            # los fotogramas clave que rodean a tt
            i = 0
            while i + 1 < len(specs) - 1 and specs[i + 1][0] <= tt + 1e-9:
                i += 1
            (t0, s0), (t1, s1) = specs[i], specs[min(len(specs) - 1, i + 1)]
            d = 0.0 if t1 <= t0 else max(0.0, min(1.0, (tt - t0) / (t1 - t0)))
            spec = dict(base)
            if "_mano_d" in s0 and "_mano_d" in s1:
                spec["_mano_d"] = _mezcla_objetivo(s0["_mano_d"], s1["_mano_d"], d)
                if "_arma" in s0 and "_arma" in s1:
                    spec["_arma"] = _mezcla_dir(s0["_arma"], s1["_arma"], d)
                elif "_arma" in (s1 if d > 0.5 else s0):
                    spec["_arma"] = (s1 if d > 0.5 else s0)["_arma"]
            if "_agarra" in s0 and "_agarra" in s1:
                spec["_agarra"] = s0["_agarra"] + (s1["_agarra"] - s0["_agarra"]) * d
            elif "_mano_i" in s0 and "_mano_i" in s1:
                spec["_mano_i"] = _mezcla_objetivo(s0["_mano_i"], s1["_mano_i"], d)
                if "_arma_i" in s0 and "_arma_i" in s1:
                    spec["_arma_i"] = _mezcla_dir(s0["_arma_i"], s1["_arma_i"], d)
                elif "_arma_i" in (s1 if d > 0.5 else s0):
                    spec["_arma_i"] = (s1 if d > 0.5 else s0)["_arma_i"]
            for c in ("_codo_d", "_codo_i"):
                if c in s0 or c in s1:
                    spec[c] = (s1 if d > 0.5 and c in s1 else s0 if c in s0 else s1)[c]
            q = resolver(rig, spec, prev) if any(k.startswith("_") for k in spec) else spec
            ticks.append((tt, q))
            prev = q
        self._canales(ticks, modo="l", solo=sorted(piezas_densas))
        return self

    def muestra(self, t, escala=1.0):
        """La pose (para vista.py) en el segundo t, con la interpolacion del juego."""
        if self.bucle and self.largo > 0:
            t = t % self.largo
        t = max(0.0, min(self.largo, t))
        out = {}
        for (p, tipo), kfs in self.ch.items():
            i = 0
            while i + 1 < len(kfs) and kfs[i + 1][0] < t:
                i += 1
            # como el juego: el ultimo fotograma con tiempo < t (o el primero) y el siguiente
            j = min(len(kfs) - 1, i + 1)
            k0, k1 = kfs[i], kfs[j]
            d = 0.0 if j == i or k1[0] == k0[0] else max(0.0, min(1.0, (t - k0[0]) / (k1[0] - k0[0])))
            if self.modo.get((p, tipo), self.interp) == "l":
                v = tuple(k0[c] + (k1[c] - k0[c]) * d for c in (1, 2, 3))
            else:
                a, b = kfs[max(0, i - 1)], kfs[min(len(kfs) - 1, j + 1)]
                v = tuple(catmull(d, a[c], k0[c], k1[c], b[c]) for c in (1, 2, 3))
            v = tuple(x * escala for x in v)
            out.setdefault(p, {})[tipo] = v
        return out

    def datos(self):
        """La animacion en una cadena (la lee client/enemigos/Animaciones.leer)."""
        def n(v, dec=2):
            s = f"{v:.{dec}f}".rstrip("0").rstrip(".")
            return "0" if s in ("-0", "") else s
        trozos = [n(self.largo), "1" if self.bucle else "0"]
        for (p, tipo), kfs in sorted(self.ch.items()):
            if all(abs(k[1]) < 1e-6 and abs(k[2]) < 1e-6 and abs(k[3]) < 1e-6 for k in kfs):
                continue
            lineal = self.modo.get((p, tipo), self.interp) == "l"
            ks = "/".join(f"{n(t)} {n(x, 1)} {n(y, 1)} {n(z, 1)}" for t, x, y, z in kfs)
            trozos.append(f"{p}:{'R' if tipo == 'rot' else 'P'}:{'L' if lineal else 'C'}:{ks}")
        return ";".join(trozos)


def sumar(*poses):
    """Suma de poses (lo que hace el juego al poner una animacion encima de otra)."""
    out = {}
    for pose in poses:
        for p, v in pose.items():
            o = out.setdefault(p, {})
            if not isinstance(v, dict):
                v = {"rot": v}
            for k, x in v.items():
                a = o.get(k, (0, 0, 0))
                o[k] = (a[0] + x[0], a[1] + x[1], a[2] + x[2])
    return out


def java_animaciones(paquete, clase, herramienta, bucles, acciones):
    """
    El codigo Java (cliente) de las animaciones: una constante por bucle y la lista ACCIONES (por numero; el 0 no se usa).
    bucles: [Anim]; acciones: [Anim] en el orden de sus numeros.
    """
    out = [f"package {paquete};\n",
           "import net.minecraft.client.animation.AnimationDefinition;\n",
           f"/** GENERADO por {herramienta}: no tocar a mano. Las animaciones (fotogramas clave en cadenas). */",
           f"public final class {clase} {{",
           f"    private {clase}() {{}}\n"]
    for a in bucles + acciones:
        out.append(f"    public static final AnimationDefinition {a.nombre.upper()} = Animaciones.leer(\"{a.datos()}\");\n")
    out.append("    /** Las acciones por numero (el 0 no se usa). */")
    out.append("    public static final AnimationDefinition[] ACCIONES = {null, " + ", ".join(a.nombre.upper() for a in acciones) + "};")
    out.append("}")
    return "\n".join(out) + "\n"


def java_tabla(paquete, clase, herramienta, acciones, extra=()):
    """El codigo Java (servidor y cliente) con los numeros, las duraciones y los golpes de cada accion."""
    c = [f"package {paquete};\n",
         f"/** GENERADO por {herramienta}: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */",
         f"public final class {clase} {{",
         f"    private {clase}() {{}}\n"]
    for i, a in enumerate(acciones, start=1):
        c.append(f"    public static final int {a.nombre.upper()} = {i};")
    c.append("")
    c.append("    /** Lo que dura cada accion, en ticks (por numero). */")
    c.append("    public static final int[] TICKS = {0, " + ", ".join(str(int(round(a.largo * 20))) for a in acciones) + "};")
    c.append("")
    c.append("    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */")
    c.append("    public static final int[][] GOLPES = {{}, " + ", ".join("{" + ", ".join(str(t) for t in a.golpes) + "}" for a in acciones) + "};")
    c.append("")
    c.append("    public static final String[] NOMBRES = {\"\", " + ", ".join(f'"{a.nombre}"' for a in acciones) + "};")
    for linea in extra:
        c.append(linea)
    c.append("}")
    return "\n".join(c) + "\n"
