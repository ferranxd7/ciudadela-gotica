package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.RocaGigante;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

/** La roca que tiran los gigantes: un pedrusco de piedra (con un trozo de musgo pegado) que da vueltas por el aire. */
public class RocaGiganteRenderer extends EntityRenderer<RocaGigante> {
    private final BlockRenderDispatcher bloques;

    public RocaGiganteRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        bloques = ctx.getBlockRenderDispatcher();
        shadowRadius = 0.9f;
    }

    @Override
    public void render(RocaGigante e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float t = e.tickCount + pt;
        ps.pushPose();
        ps.translate(0, 0.7, 0);
        ps.mulPose(Axis.XP.rotationDegrees(t * 14f));
        ps.mulPose(Axis.ZP.rotationDegrees(t * 6f));
        ps.pushPose();
        ps.scale(1.5f, 1.3f, 1.4f);
        ps.translate(-0.5, -0.5, -0.5);
        bloques.renderSingleBlock(Blocks.COBBLED_DEEPSLATE.defaultBlockState(), ps, buf, luz, OverlayTexture.NO_OVERLAY);
        ps.popPose();
        ps.pushPose();
        ps.translate(0.25, 0.35, -0.2);
        ps.scale(0.9f, 0.8f, 0.9f);
        ps.translate(-0.5, -0.5, -0.5);
        bloques.renderSingleBlock(Blocks.MOSSY_COBBLESTONE.defaultBlockState(), ps, buf, luz, OverlayTexture.NO_OVERLAY);
        ps.popPose();
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(RocaGigante e) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
