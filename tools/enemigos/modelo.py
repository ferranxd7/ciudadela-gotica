"""
El esqueleto comun de los enemigos con forma de persona (parte 3c): piezas con pivote y cajas, como los modelos de
Minecraft (pixeles: 16 = 1 bloque; Y hacia ABAJO; el suelo en y = 24; el frente mira a -Z y el lado derecho del
personaje es -X). Como los de los jefes (tools/rey, tools/gavran), pero con parametros, para hacer muchos enemigos
con el mismo esqueleto y las mismas animaciones:

    raiz
      cadera
        muslo_d -> pierna_d          muslo_i -> pierna_i
        torso
          cabeza
          brazo_d -> antebrazo_d -> mano_d        brazo_i -> antebrazo_i -> mano_i

Los codos y las rodillas son lo que distingue a estos enemigos de los de Minecraft: sus golpes se ven venir.
A la mano se le cuelgan las armas (piezas hijas de mano_d / mano_i).
"""
import math
from dataclasses import dataclass, field


@dataclass
class Box:
    x: float
    y: float
    z: float
    w: float
    h: float
    d: float
    mat: str
    grow: tuple = (0.0, 0.0, 0.0)
    uv: str = ""          # zona compartida con otras cajas del mismo tamano
    u: int = 0            # se rellenan al empaquetar
    v: int = 0


@dataclass
class Part:
    name: str
    parent: str
    pivot: tuple
    rot: tuple = (0.0, 0.0, 0.0)
    boxes: list = field(default_factory=list)


def g(v):
    return (v, v, v)


class Rig:
    """Un modelo: sus piezas en orden (la madre antes que las hijas) y su textura."""

    def __init__(self, nombre, tex_w=64, escala=1):
        self.nombre = nombre
        self.tex_w = tex_w
        self.tex_h = 0
        self.escala = escala
        self.partes = []
        self.por_nombre = {}

    def pieza(self, name, parent, pivot, rot=(0.0, 0.0, 0.0), boxes=()):
        if name in self.por_nombre:
            raise SystemExit(f"pieza repetida: {name}")
        if parent and parent not in self.por_nombre:
            raise SystemExit(f"{name}: no existe la pieza madre {parent}")
        p = Part(name, parent, tuple(pivot), tuple(rot), list(boxes))
        self.partes.append(p)
        self.por_nombre[name] = p
        return p

    def caja(self, pieza, *a, **k):
        b = Box(*a, **k)
        self.por_nombre[pieza].boxes.append(b)
        return b

    def cajas(self):
        for p in self.partes:
            for b in p.boxes:
                yield p, b

    def empaquetar(self):
        """Coloca la zona de cada caja en la textura (por estanterias, las altas primero). Devuelve el alto usado."""
        compartidas = {}
        items = []
        for _, b in self.cajas():
            if b.uv and b.uv in compartidas:
                continue
            items.append(b)
            if b.uv:
                compartidas[b.uv] = b
        items.sort(key=lambda b: (-(math.ceil(b.d) + math.ceil(b.h)), -(math.ceil(b.d) + math.ceil(b.w))))
        x = y = fila = 0
        for b in items:
            bw = 2 * (math.ceil(b.d) + math.ceil(b.w))
            bh = math.ceil(b.d) + math.ceil(b.h)
            if bw > self.tex_w:
                raise SystemExit(f"la caja {b.mat} no cabe a lo ancho: {bw} > {self.tex_w}")
            if x + bw > self.tex_w:
                x = 0
                y += fila
                fila = 0
            b.u, b.v = x, y
            x += bw
            fila = max(fila, bh)
        usado = y + fila
        for _, b in self.cajas():
            if b.uv:
                b.u, b.v = compartidas[b.uv].u, compartidas[b.uv].v
        self.tex_h = 1 << int(math.ceil(math.log2(max(usado, 16))))
        return usado

    def camino(self, nombre):
        c = []
        while nombre:
            c.append(nombre)
            nombre = self.por_nombre[nombre].parent
        return list(reversed(c))


# ---------------------------------------------------------------- las caras de una caja en la textura

CARAS = ("arriba", "abajo", "der", "frente", "izq", "espalda")


def caras_de(b):
    """{cara: (x, y, ancho, alto)} en unidades de UV. der / izq = el costado derecho / izquierdo del personaje."""
    w, h, d = math.ceil(b.w), math.ceil(b.h), math.ceil(b.d)
    u, v = b.u, b.v
    return {
        "arriba": (u + d, v, w, d),
        "abajo": (u + d + w, v, w, d),
        "der": (u, v + d, d, h),
        "frente": (u + d, v + d, w, h),
        "izq": (u + d + w, v + d, d, h),
        "espalda": (u + 2 * d + w, v + d, w, h),
    }


# ---------------------------------------------------------------- el esqueleto de persona

def humanoide(rig, torso=(8, 12, 4), cabeza=(8, 8, 8), brazo=(4, 12, 4), pierna=(4, 12, 4), hombro=1.0,
              sufijo_mat=("d", "i"), cuello=0.0):
    """
    Anade a `rig` las piezas de una persona con las medidas dadas (ancho, alto, fondo). Por defecto, las de un jugador.
    Materiales: cabeza, torso, brazo_d, antebrazo_d, brazo_i, antebrazo_i, muslo_d, pierna_d, muslo_i, pierna_i.
    Devuelve las medidas utiles (alturas de las articulaciones) para colocar lo demas.
    """
    tw, th, td = torso
    cw, chh, cd = cabeza
    bw, bh, bd = brazo
    pw, ph, pd = pierna
    alto_pierna = ph
    rig.pieza("raiz", "", (0, 24, 0))
    rig.pieza("cadera", "raiz", (0, -alto_pierna, 0))
    for s, n in ((-1, "d"), (1, "i")):
        x = s * pw / 2.0 if tw >= 2 * pw else s * (tw / 2.0 - pw / 2.0)
        rig.pieza(f"muslo_{n}", "cadera", (x, 0, 0), boxes=[Box(-pw / 2.0, 0, -pd / 2.0, pw, ph / 2.0, pd, f"muslo_{n}")])
        rig.pieza(f"pierna_{n}", f"muslo_{n}", (0, ph / 2.0, 0), boxes=[Box(-pw / 2.0, 0, -pd / 2.0, pw, ph / 2.0, pd, f"pierna_{n}")])
    rig.pieza("torso", "cadera", (0, 0, 0), boxes=[Box(-tw / 2.0, -th, -td / 2.0, tw, th, td, "torso")])
    rig.pieza("cabeza", "torso", (0, -th - cuello, 0), boxes=[Box(-cw / 2.0, -chh, -cd / 2.0, cw, chh, cd, "cabeza")])
    for s, n in ((-1, "d"), (1, "i")):
        # el hombro: el brazo cuelga pegado al costado del torso
        px = s * (tw / 2.0 + bw / 2.0 - (1.0 - hombro))
        rig.pieza(f"brazo_{n}", "torso", (px, -th + 2, 0), boxes=[Box(-bw / 2.0, -2, -bd / 2.0, bw, bh / 2.0, bd, f"brazo_{n}")])
        rig.pieza(f"antebrazo_{n}", f"brazo_{n}", (0, bh / 2.0 - 2, 0), boxes=[Box(-bw / 2.0, 0, -bd / 2.0, bw, bh / 2.0, bd, f"antebrazo_{n}")])
        rig.pieza(f"mano_{n}", f"antebrazo_{n}", (0, bh / 2.0 - 1, 0))
    return {"cadera": alto_pierna, "hombro": alto_pierna + th - 2, "cuello": alto_pierna + th + cuello,
            "coronilla": alto_pierna + th + cuello + chh, "brazo": bh, "pierna": ph}


# ---------------------------------------------------------------- Java

def fmt(v):
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "F"


def java_geometria(rig, paquete, clase, herramienta, ocultas=None):
    """El codigo Java de las piezas y cajas. `ocultas`: {NOMBRE: [pieza, ...]} listas de piezas (se exportan sus nombres)."""
    out = [f"package {paquete};\n",
           "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;\n",
           f"/** GENERADO por {herramienta}: no tocar a mano. Piezas y cajas del modelo. */",
           f"public final class {clase} {{",
           f"    private {clase}() {{}}\n",
           f"    public static final int TEX_W = {rig.tex_w}, TEX_H = {rig.tex_h};\n",
           "    public static LayerDefinition crear() {",
           "        MeshDefinition mesh = new MeshDefinition();",
           "        PartDefinition root = mesh.getRoot();"]
    var = {"": "root"}
    for p in rig.partes:
        v = "p_" + p.name
        var[p.name] = v
        cb = "CubeListBuilder.create()"
        for b in p.boxes:
            gx, gy, gz = b.grow
            cb += f".texOffs({b.u}, {b.v})"
            if gx == gy == gz == 0:
                cb += f".addBox({fmt(b.x)}, {fmt(b.y)}, {fmt(b.z)}, {fmt(b.w)}, {fmt(b.h)}, {fmt(b.d)})"
            else:
                cb += (f".addBox({fmt(b.x)}, {fmt(b.y)}, {fmt(b.z)}, {fmt(b.w)}, {fmt(b.h)}, {fmt(b.d)}, "
                       f"new CubeDeformation({fmt(gx)}, {fmt(gy)}, {fmt(gz)}))")
        rx, ry, rz = (math.radians(a) for a in p.rot)
        px, py, pz = p.pivot
        if rx == ry == rz == 0:
            pose = f"PartPose.offset({fmt(px)}, {fmt(py)}, {fmt(pz)})"
        else:
            pose = f"PartPose.offsetAndRotation({fmt(px)}, {fmt(py)}, {fmt(pz)}, {fmt(rx)}, {fmt(ry)}, {fmt(rz)})"
        out.append(f"        PartDefinition {v} = {var[p.parent]}.addOrReplaceChild(\"{p.name}\", {cb}, {pose});")
    out.append("        return LayerDefinition.create(mesh, TEX_W, TEX_H);")
    out.append("    }")
    for nombre, piezas in (ocultas or {}).items():
        out.append("")
        out.append(f"    public static final String[] {nombre} = {{" + ", ".join(f'"{x}"' for x in piezas) + "};")
    out.append("}")
    return "\n".join(out) + "\n"
