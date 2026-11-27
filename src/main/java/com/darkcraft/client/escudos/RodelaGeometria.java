package com.darkcraft.client.escudos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/escudos/escudos.py: no tocar a mano. Piezas y cajas del modelo. */
public final class RodelaGeometria {
    private RodelaGeometria() {}

    public static final int TEX_W = 64, TEX_H = 32;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_escudo = root.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(36, 15).addBox(-2F, -6F, -2F, 4F, 1F, 1F).texOffs(0, 15).addBox(-4F, -5F, -2F, 8F, 1F, 1F).texOffs(0, 12).addBox(-5F, -4F, -2F, 10F, 2F, 1F).texOffs(28, 0).addBox(-6F, -2F, -2F, 12F, 4F, 1F).texOffs(22, 12).addBox(-5F, 2F, -2F, 10F, 2F, 1F).texOffs(18, 15).addBox(-4F, 4F, -2F, 8F, 1F, 1F).texOffs(46, 15).addBox(-2F, 5F, -2F, 4F, 1F, 1F).texOffs(16, 0).addBox(-2F, -2F, -4F, 4F, 4F, 2F).texOffs(44, 12).addBox(-1F, -1F, -5F, 2F, 2F, 1F).texOffs(0, 0).addBox(-1F, -3F, -1F, 2F, 6F, 6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }
}
