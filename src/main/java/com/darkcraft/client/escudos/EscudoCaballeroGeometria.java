package com.darkcraft.client.escudos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/escudos/escudos.py: no tocar a mano. Piezas y cajas del modelo. */
public final class EscudoCaballeroGeometria {
    private EscudoCaballeroGeometria() {}

    public static final int TEX_W = 64, TEX_H = 32;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_escudo = root.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(0, 0).addBox(-6F, -11F, -2F, 12F, 14F, 1F).texOffs(42, 0).addBox(-5F, 3F, -2F, 10F, 3F, 1F).texOffs(0, 15).addBox(-4F, 6F, -2F, 8F, 2F, 1F).texOffs(18, 15).addBox(-3F, 8F, -2F, 6F, 2F, 1F).texOffs(32, 15).addBox(-2F, 10F, -2F, 4F, 1F, 1F).texOffs(42, 15).addBox(-1F, 11F, -2F, 2F, 1F, 1F).texOffs(26, 0).addBox(-1F, -3F, -1F, 2F, 6F, 6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }
}
