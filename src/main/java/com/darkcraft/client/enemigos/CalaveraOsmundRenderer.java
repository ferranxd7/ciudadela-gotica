package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.CalaveraOsmund;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SkullBlock;

/**
 * Una calavera de la corona de Fray Osmund (parte 7at): el craneo de esqueleto del juego (su modelo y su piel), algo mas grande, a
 * plena luz (en la oscuridad del Relicario se ve venir), mirando hacia donde va y meciendose un poco mientras orbita.
 */
public class CalaveraOsmundRenderer extends EntityRenderer<CalaveraOsmund> {
    private static final ResourceLocation PIEL = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private final SkullModelBase modelo;

    public CalaveraOsmundRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        modelo = SkullBlockRenderer.createSkullRenderers(ctx.getModelSet()).get(SkullBlock.Types.SKELETON);
        shadowRadius = 0.2f;
    }

    @Override
    public void render(CalaveraOsmund e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        ps.pushPose();
        float edad = e.tickCount + pt;
        float giro = e.giro(pt);
        float cabeceo = e.vuela() ? Mth.lerp(pt, e.xRotO, e.getXRot()) : 8f * Mth.sin(edad * 0.25f);
        ps.translate(0, 0.28, 0);
        ps.mulPose(Axis.YP.rotationDegrees(-giro));
        ps.mulPose(Axis.XP.rotationDegrees(cabeceo));
        if (!e.vuela()) ps.mulPose(Axis.ZP.rotationDegrees(10f * Mth.sin(edad * 0.17f)));
        float s = 1.25f;
        ps.scale(-s, -s, s);
        // el craneo del juego va de 0 a -8 pixeles en su eje Y: centrado en el sitio de la calavera
        ps.translate(0, 0.25, 0);
        modelo.setupAnim(0f, 180f, 0f);
        modelo.renderToBuffer(ps, buf.getBuffer(RenderType.entityCutoutNoCullZOffset(PIEL)), 0xF000F0, OverlayTexture.NO_OVERLAY, 0.92f, 0.95f, 1f, 1f);
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(CalaveraOsmund e) {
        return PIEL;
    }
}
