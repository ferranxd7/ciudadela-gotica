#!/usr/bin/env python3
"""Genera todo lo del modelo del rey: texturas (3 fases, base y brillo), el codigo Java del modelo y de
las animaciones, y vistas previas en PNG.

Uso: python3 tools/rey/generar.py [carpeta del repositorio] [--vistas carpeta]
"""
import math
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import partes  # noqa: E402
from partes import PARTS, SCALE, TEX_W, TEX_H  # noqa: E402
import pintar  # noqa: E402
import vista  # noqa: E402

REPO = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 and not sys.argv[1].startswith("--") else \
    os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
TEX_DIR = os.path.join(REPO, "src/main/resources/assets/ciudadela/textures/entity/rey")
JAVA_DIR = os.path.join(REPO, "src/main/java/com/ciudadela/client/rey")


def fmt(v):
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "F"


def java_model(tex_h):
    lines = []
    for p in PARTS:
        if not p.parent:
            continue
    out = []
    out.append("package com.ciudadela.client.rey;\n")
    out.append("import net.minecraft.client.model.geom.PartPose;")
    out.append("import net.minecraft.client.model.geom.builders.*;\n")
    out.append("/** GENERADO por tools/rey/generar.py: no tocar a mano. Piezas y cajas del modelo del rey. */")
    out.append("public final class ReyGeometria {")
    out.append("    private ReyGeometria() {}\n")
    out.append(f"    public static final int TEX_W = {TEX_W}, TEX_H = {tex_h};\n")
    out.append("    public static LayerDefinition crear() {")
    out.append("        MeshDefinition mesh = new MeshDefinition();")
    out.append("        PartDefinition root = mesh.getRoot();")
    var = {"": "root"}
    for p in PARTS:
        parent = var[p.parent]
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
        out.append(f"        PartDefinition {v} = {parent}.addOrReplaceChild(\"{p.name}\", {cb}, {pose});")
    out.append(f"        return LayerDefinition.create(mesh, TEX_W, TEX_H);")
    out.append("    }")
    out.append("}")
    return "\n".join(out) + "\n"


def main():
    used = partes.pack()
    tex_h = 1 << int(math.ceil(math.log2(max(used, 16))))
    os.makedirs(TEX_DIR, exist_ok=True)
    os.makedirs(JAVA_DIR, exist_ok=True)
    texs = {}
    for ph in (1, 2, 3):
        base, glow = pintar.build(ph, used)
        base = base[: tex_h * SCALE]
        glow = glow[: tex_h * SCALE]
        texs[ph] = (base, glow)
        Image.fromarray((base * 255).astype(np.uint8), "RGBA").save(os.path.join(TEX_DIR, f"rey_{ph}.png"))
        g = glow.copy()
        # en la capa de brillo lo que no brilla va negro (se suma)
        g[..., :3] *= g[..., 3:4]
        g[..., 3] = np.where(g[..., 3] > 0, 1, 0)
        Image.fromarray((g * 255).astype(np.uint8), "RGBA").save(os.path.join(TEX_DIR, f"brillo_{ph}.png"))
    with open(os.path.join(JAVA_DIR, "ReyGeometria.java"), "w") as f:
        f.write(java_model(tex_h))
    import animaciones
    common = os.path.join(REPO, "src/main/java/com/ciudadela/rey")
    os.makedirs(common, exist_ok=True)
    animaciones.write_java(os.path.join(JAVA_DIR, "ReyAnimaciones.java"), os.path.join(common, "ReyAnims.java"))
    # vistas previas
    vdir = None
    if "--vistas" in sys.argv:
        vdir = sys.argv[sys.argv.index("--vistas") + 1]
        os.makedirs(vdir, exist_ok=True)
        tiles = []
        for ph in (1, 2, 3):
            base, glow = texs[ph]
            for yaw in (25, 145, 250):
                tiles.append(vista.render(base, glow if ph > 1 else None, yaw=yaw, pitch=-8))
        rows = [np.concatenate(tiles[i * 3:(i + 1) * 3], axis=1) for i in range(3)]
        Image.fromarray(np.concatenate(rows, axis=0)).save(os.path.join(vdir, "rey_fases.png"))
        Image.fromarray((texs[1][0] * 255).astype(np.uint8)).resize((TEX_W * 4, tex_h * 4), Image.NEAREST).save(
            os.path.join(vdir, "textura_1.png"))
    print(f"textura {TEX_W}x{tex_h} (usado {used}), {sum(len(p.boxes) for p in PARTS)} cajas, {len(PARTS)} piezas")


if __name__ == "__main__":
    main()
