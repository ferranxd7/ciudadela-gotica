package com.darkcraft.client.espejo;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * La escena de escribir una marca: el personaje del jugador (con su piel) en una mesa, coge la pluma y escribe el
 * nombre de la marca en un pergamino; al acabar sella el pergamino con el simbolo de la marca. Todo en 3D dentro de
 * la pagina izquierda del libro (vista desde delante y un poco desde arriba).
 */
final class Escena {
    private Escena() {}

    private static PlayerModel<AbstractClientPlayer> modelo, modeloFino;

    private static PlayerModel<AbstractClientPlayer> modelo(boolean fino) {
        Minecraft mc = Minecraft.getInstance();
        if (fino) {
            if (modeloFino == null) modeloFino = new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
            return modeloFino;
        }
        if (modelo == null) modelo = new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        return modelo;
    }

    private static float suave(float x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    /**
     * @param p progreso de la escritura (0..1) o -1 mientras se rellena el dialogo
     */
    static void render(GuiGraphics g, LibroScreen s, int x, int y, int w, int h, float p, String texto, int icono, int color) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float t = (net.minecraft.Util.getMillis() % 100000L) / 1000f;
        // fondo: la pared oscura de una celda de escriba, con la luz de una vela detras
        g.fill(x, y, x + w, y + h, 0xFF120A0C);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.9f, 0.45f, 0.15f, 0.35f + 0.05f * (float) Math.sin(t * 7));
        int r = Math.round(w * 0.55f);
        g.blit(EspejoHud.BRILLO, x + w / 2 - r, y + Math.round(h * 0.32f) - r, r * 2, r * 2, 0, 0, 64, 64, 64, 64);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        g.enableScissor(x, y, x + w, y + h);

        boolean fino = "slim".equals(mc.player.getModelName());
        PlayerModel<AbstractClientPlayer> m = modelo(fino);
        ResourceLocation piel = mc.player.getSkinTextureLocation();
        if (piel == null) piel = DefaultPlayerSkin.getDefaultSkin(mc.player.getUUID());

        // poses: reposo -> coger la pluma (0..0.12) -> escribir (0.14..0.78) -> sellar (0.8..1)
        float coger = p < 0 ? 0 : suave(p / 0.12f);
        float escribir = p < 0 ? 0 : Mth.clamp((p - 0.14f) / 0.64f, 0, 1);
        float sellar = p < 0 ? 0 : suave((p - 0.8f) / 0.12f);
        boolean enMano = p >= 0.06f;
        resetear(m);
        m.head.xRot = 0.35f + 0.15f * coger - 0.1f * sellar;
        m.head.yRot = -0.12f * coger + 0.05f * (float) Math.sin(t * 1.3);
        m.body.xRot = 0.1f * coger;
        float garabato = (escribir > 0 && escribir < 1) ? 1 : 0;
        m.rightArm.xRot = Mth.lerp(coger, -0.15f, -1.02f) + 0.05f * garabato * (float) Math.sin(t * 31) + 0.35f * sellar;
        m.rightArm.yRot = Mth.lerp(coger, 0f, -0.48f + 0.36f * escribir) + 0.05f * garabato * (float) Math.sin(t * 13);
        m.rightArm.zRot = 0.06f;
        m.leftArm.xRot = Mth.lerp(coger, -0.1f, -0.92f);
        m.leftArm.yRot = Mth.lerp(coger, 0f, 0.16f);
        m.leftArm.zRot = -0.06f;
        m.hat.copyFrom(m.head);
        m.jacket.copyFrom(m.body);
        m.rightSleeve.copyFrom(m.rightArm);
        m.leftSleeve.copyFrom(m.leftArm);
        m.rightLeg.visible = m.leftLeg.visible = m.rightPants.visible = m.leftPants.visible = false;

        g.flush();
        PoseStack ps = g.pose();
        ps.pushPose();
        float sc = h * 0.68f;
        ps.translate(x + w / 2f, y + h * 0.36f, 200);
        ps.scale(sc, sc, -sc);
        ps.mulPose(Axis.XP.rotationDegrees(32));
        ps.mulPose(Axis.YP.rotationDegrees(-18));
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        int luz = LightTexture.FULL_BRIGHT;
        // la mesa: un tablero de roble oscuro
        TextureAtlasSprite madera = mc.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.DARK_OAK_PLANKS.defaultBlockState());
        float mesaY = 0.6f;
        quad(ps, buf.getBuffer(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS)), -0.62f, -0.92f, 0.62f, -0.2f, mesaY,
                madera.getU0(), madera.getV0(), madera.getU1(), madera.getV1(), 0.55f, 0.45f, 0.4f, luz);
        // el pergamino
        quad(ps, buf.getBuffer(RenderType.entityCutoutNoCull(LibroScreen.PERGAMINO)), -0.4f, -0.86f, 0.4f, -0.3f, mesaY - 0.004f,
                0.04f, 0.04f, 0.96f, 0.96f, 1f, 1f, 1f, luz);
        buf.endBatch();
        // el personaje
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent(piel));
        m.renderToBuffer(ps, vc, luz, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        buf.endBatch();
        // la pluma: en la mesa hasta que la coge, luego en la mano derecha
        ItemStack pluma = new ItemStack(Items.FEATHER);
        if (enMano) {
            ps.pushPose();
            m.translateToHand(HumanoidArm.RIGHT, ps);
            ps.mulPose(Axis.XP.rotationDegrees(-90));
            ps.mulPose(Axis.YP.rotationDegrees(180));
            ps.translate(1 / 16f, 0.125f, -0.625f);
            mc.getItemRenderer().renderStatic(pluma, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, luz, OverlayTexture.NO_OVERLAY, ps, buf, mc.level, 0);
            ps.popPose();
        } else {
            ps.pushPose();
            ps.translate(-0.48f, mesaY - 0.02f, -0.5f);
            ps.mulPose(Axis.XP.rotationDegrees(90));
            ps.mulPose(Axis.ZP.rotationDegrees(35));
            ps.scale(0.45f, 0.45f, 0.45f);
            mc.getItemRenderer().renderStatic(pluma, ItemDisplayContext.GROUND, luz, OverlayTexture.NO_OVERLAY, ps, buf, mc.level, 0);
            ps.popPose();
        }
        buf.endBatch();
        // el texto en el pergamino: tinta que va apareciendo (antes de escribir, la guia a lapiz)
        Font font = mc.font;
        String mostrar = p < 0 ? texto : texto.substring(0, Math.round(texto.length() * escribir));
        int tw = Math.max(1, font.width(texto));
        float ts = Math.min(0.0105f, 0.62f / tw);
        ps.pushPose();
        ps.translate(-tw * ts / 2f, mesaY - 0.006f, -0.42f - 9 * ts);
        ps.mulPose(Axis.XP.rotationDegrees(-90));
        ps.scale(ts, ts, ts);
        int tinta = p < 0 ? 0x557A6A50 : 0xFF000000 | Exploracion.escala(color, 0.45f);
        Matrix4f mt = ps.last().pose();
        font.drawInBatch(mostrar, 0, 0, tinta, false, mt, buf, Font.DisplayMode.NORMAL, 0, luz);
        ps.popPose();
        // el sello con el simbolo de la marca
        if (sellar > 0) {
            float e = 0.08f + 0.06f * (1 - sellar) * (1 - sellar) * 3;
            ps.pushPose();
            ps.translate(0.24f, mesaY - 0.008f, -0.74f);
            int c = color;
            int ic = EspejoHud.I_MARCAS + Mth.clamp(icono, 0, Marcas.ICONOS - 1);
            float u0 = (ic % 16) * 16 / 256f, v0 = (ic / 16) * 16 / 64f;
            quad(ps, buf.getBuffer(RenderType.entityCutoutNoCull(EspejoHud.ICONOS)), -e, -e, e, e, 0, u0, v0, u0 + 16 / 256f, v0 + 16 / 64f,
                    ((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, luz);
            ps.popPose();
        }
        buf.endBatch();
        ps.popPose();
        Lighting.setupFor3DItems();
        g.disableScissor();
        // marco fino alrededor de la escena
        g.fill(x, y, x + w, y + 1, 0xFF5A3A20);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF5A3A20);
        g.fill(x, y, x + 1, y + h, 0xFF5A3A20);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF5A3A20);
    }

    private static void resetear(PlayerModel<AbstractClientPlayer> m) {
        m.head.resetPose();
        m.body.resetPose();
        m.rightArm.resetPose();
        m.leftArm.resetPose();
        m.rightLeg.resetPose();
        m.leftLeg.resetPose();
        m.young = false;
        m.crouching = false;
        m.attackTime = 0;
        m.riding = false;
        m.setAllVisible(true);
    }

    /** Un rectangulo horizontal (en el plano y) de x0..x1 y z0..z1, mirando hacia arriba. */
    private static void quad(PoseStack ps, VertexConsumer vc, float x0, float z0, float x1, float z1, float y, float u0, float v0, float u1, float v1,
                             float r, float g, float b, int luz) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        vc.vertex(m, x0, y, z0).color(r, g, b, 1f).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz).normal(n, 0, -1, 0).endVertex();
        vc.vertex(m, x0, y, z1).color(r, g, b, 1f).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz).normal(n, 0, -1, 0).endVertex();
        vc.vertex(m, x1, y, z1).color(r, g, b, 1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz).normal(n, 0, -1, 0).endVertex();
        vc.vertex(m, x1, y, z0).color(r, g, b, 1f).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz).normal(n, 0, -1, 0).endVertex();
    }
}
