package com.darkcraft.client.escudos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/escudos/escudos.py: no tocar a mano. Piezas y cajas del modelo. */
public final class PavesGeometria {
    private PavesGeometria() {}

    public static final int TEX_W = 64, TEX_H = 32;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_escudo = root.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(0, 25).addBox(-6F, -13F, -2F, 12F, 1F, 1F).texOffs(0, 0).addBox(-7F, -12F, -2F, 14F, 24F, 1F).texOffs(26, 25).addBox(-6F, 12F, -2F, 12F, 1F, 1F).texOffs(30, 0).addBox(-1F, -12F, -3F, 2F, 24F, 1F).texOffs(52, 0).addBox(-1F, 13F, -2F, 2F, 2F, 1F).texOffs(36, 0).addBox(-1F, -3F, -1F, 2F, 6F, 6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }
}
