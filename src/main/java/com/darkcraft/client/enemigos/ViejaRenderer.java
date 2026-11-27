package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Vieja;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * El dibujante de la Vieja: el cachalote fantasma a su tamano (unos diecisiete bloques), medio transparente, frio y luciendo
 * (con toda la luz: es lo que queda de ella), girado hacia donde va y con la cabeza alta al salir o baja al hundirse; aparece y
 * se apaga ({@link Vieja#alfa}). Su ojo y sus cicatrices lucen mas (la capa de brillo).
 */
public class ViejaRenderer extends EntityRenderer<Vieja> {
    private static final ResourceLocation TEX = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/vieja_0.png"),
            BRILLO = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/vieja_0_brillo.png");
    /** Lo grande que se dibuja el modelo (de 92 pixeles de largo). */
    public static final float ESCALA = 3.0f;
    /** Lo transparente que es del todo (cuando mas se ve). */
    private static final float ALFA = 0.62f;
    private final ViejaModelo modelo;

    public ViejaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        modelo = new ViejaModelo(ctx.bakeLayer(ViejaModelo.CAPA));
        shadowRadius = 0f;
    }

    @Override
    public void render(Vieja v, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = v.alfa(pt) * ALFA;
        if (a <= 0.01f) return;
        ps.pushPose();
        // el modelo tiene el centro de su cuerpo 12 px por encima de los pies: lo bajamos para que la entidad este en medio
        ps.mulPose(Axis.YP.rotationDegrees(180f - Mth.rotLerp(pt, v.yRotO, v.getYRot())));
        ps.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(pt, v.xRotO, v.getXRot())));
        ps.scale(-ESCALA, -ESCALA, ESCALA);
        ps.translate(0, -1.5f + 12f / 16f, 0);
        modelo.setupAnim(v, 0, 0, v.tickCount + pt, 0, 0);
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(TEX));
        modelo.renderToBuffer(ps, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, 0.82f, 0.92f, 1.0f, a);
        VertexConsumer vb = buf.getBuffer(RenderType.eyes(BRILLO));
        modelo.renderToBuffer(ps, vb, 0xF000F0, OverlayTexture.NO_OVERLAY, a / ALFA, a / ALFA, a / ALFA, 1f);
        ps.popPose();
        super.render(v, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Vieja v) {
        return TEX;
    }
}
