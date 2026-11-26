package com.darkcraft.client.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * El cielo rojo en el cliente (parte 5e; lo manda {@code fortaleza/CieloRojo}): desde el rayo, en ocho segundos, la niebla
 * y el cielo se tiñen de rojo (un velo rojo sobre el cielo, justo despues de dibujarlo) y la luz del cielo enrojece; las
 * nubes de Minecraft (arriba, a 640) y las del mar de nubes (los bloques de nube, con su tinte) arden rojas; de noche sale
 * la luna de sangre (encima de la luna de siempre, mas grande); y de cada portal abierto sube una columna de luz roja
 * (como la de un faro, se ve de lejos).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class CieloRojoCliente {
    private CieloRojoCliente() {}

    private static final ResourceLocation LUNA = new ResourceLocation(DarkCraftMod.MODID, "textures/environment/luna_sangre.png");
    /** Lo que tarda en enrojecer todo (ticks). */
    private static final float FUNDIDO = 160f;

    private static boolean activo;
    private static long desde;
    private static int[] columnas = new int[0];
    /** El tinte de las nubes del mar de nubes que se ha dibujado (para rehacer los chunks si cambia). */
    private static boolean nubesTenidas;

    /** Del servidor: si esta el cielo rojo, desde cuando y sus columnas. */
    public static void recibir(boolean act, long t, int[] cols) {
        activo = act;
        desde = t;
        columnas = cols == null ? new int[0] : cols;
        Minecraft mc = Minecraft.getInstance();
        if (nubesTenidas != activo && mc.level != null && mc.player != null) {
            nubesTenidas = activo;
            // (las nubes del mar de nubes cambian de tinte: se vuelven a montar las secciones de alrededor, con las de
            // antes a la vista hasta que esten las nuevas; cae con el destello del rayo)
            int rd = mc.options.getEffectiveRenderDistance() + 1;
            int px = mc.player.blockPosition().getX() >> 4, pz = mc.player.blockPosition().getZ() >> 4;
            int y0 = mc.level.getMinSection(), y1 = mc.level.getMaxSection();
            for (int x = px - rd; x <= px + rd; x++)
                for (int z = pz - rd; z <= pz + rd; z++)
                    for (int y = y0; y < y1; y++) mc.levelRenderer.setSectionDirty(x, y, z);
        }
        nubesTenidas = activo;
    }

    public static boolean activo() {
        return activo;
    }

    public static int columnas() {
        return columnas.length / 3;
    }

    /** Lo rojo que esta ya todo (0-1), con el fundido desde el rayo. */
    public static float k(float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (!activo || mc.level == null || mc.level.dimension() != Level.OVERWORLD) return 0f;
        float t = (float) ((mc.level.getGameTime() + pt - desde) / FUNDIDO);
        if (t >= 1f) return 1f;
        if (t <= 0f) return desde > mc.level.getGameTime() + 200 ? 1f : 0f;
        return t * t * (3 - 2 * t);
    }

    // ------------------------------------------------------------------ las nubes y la luz (ClientNubes.Efectos los pide)

    /** El color de las nubes de Minecraft (multiplica): blancas, o rojas desde el rayo. */
    public static void tenirNubes(float pt) {
        float k = k(pt);
        if (k > 0) RenderSystem.setShaderColor(1f, 1f - 0.62f * k, 1f - 0.68f * k, 1f);
    }

    /** La luz del cielo tira a rojo (la de las antorchas no). */
    public static void luz(float pt, int cielo, Vector3f c) {
        float k = k(pt);
        if (k <= 0) return;
        float s = cielo / 15f * k;
        c.set(c.x() * (1f + 0.06f * s), c.y() * (1f - 0.24f * s), c.z() * (1f - 0.3f * s));
    }

    // ------------------------------------------------------------------ el mar de nubes (tinte de los bloques de nube)

    @Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Colores {
        private Colores() {}

        @SubscribeEvent
        public static void bloques(RegisterColorHandlersEvent.Block e) {
            e.register((st, lvl, pos, i) -> nubesTenidas ? 0xF06A5C : 0xFFFFFF, DarkCraftMod.NUBE.get());
        }
    }

    // ------------------------------------------------------------------ la niebla

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW)
    public static void onNiebla(ViewportEvent.ComputeFogColor e) {
        float k = k((float) e.getPartialTick());
        if (k <= 0) return;
        float r = e.getRed(), g = e.getGreen(), b = e.getBlue();
        float br = Math.max(0.06f, (r + g + b) / 3f);
        float tr = Math.min(1f, br * 1.55f + 0.04f), tg = br * 0.34f, tb = br * 0.3f;
        float m = 0.6f * k;
        e.setRed(Mth.lerp(m, r, tr));
        e.setGreen(Mth.lerp(m, g, tg));
        e.setBlue(Mth.lerp(m, b, tb));
    }

    // ------------------------------------------------------------------ el cielo, la luna de sangre y las columnas

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) cielo(e);
        else if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) columnas(e);
    }

    /** El velo rojo sobre el cielo (una caja alrededor de la camara, sin profundidad: lo de despues la tapa) y la luna de sangre. */
    private static void cielo(RenderLevelStageEvent e) {
        Minecraft mc = Minecraft.getInstance();
        float pt = e.getPartialTick(), k = k(pt);
        if (k <= 0 || mc.level == null) return;
        PoseStack ps = e.getPoseStack();
        Matrix4f m = ps.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float s = 60f, a = 0.32f * k;
        // (arriba, mas rojo; abajo, en el horizonte, un poco menos)
        float[][] caras = {
                {-s, s, -s, s, s, -s, s, s, s, -s, s, s},     // techo
                {-s, -s, -s, -s, s, -s, s, s, -s, s, -s, -s},  // norte
                {s, -s, s, s, s, s, -s, s, s, -s, -s, s},      // sur
                {-s, -s, s, -s, s, s, -s, s, -s, -s, -s, -s},  // oeste
                {s, -s, -s, s, s, -s, s, s, s, s, -s, s},      // este
                {-s, -s, s, -s, -s, -s, s, -s, -s, s, -s, s}}; // suelo
        for (float[] c : caras)
            for (int v = 0; v < 4; v++) {
                float y = c[v * 3 + 1];
                float al = y > 0 ? a : a * 0.55f;
                bb.vertex(m, c[v * 3], y, c[v * 3 + 2]).color(0.62f, 0.05f, 0.03f, al).endVertex();
            }
        BufferUploader.drawWithShader(bb.end());
        // la luna de sangre: donde esta la de siempre (el mismo giro del cielo), mas grande, tapandola; solo de noche
        float noche = Math.min(1f, mc.level.getStarBrightness(pt) * 2.2f) * (1f - mc.level.getRainLevel(pt) * 0.6f);
        if (noche > 0.02f) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(-90f));
            ps.mulPose(Axis.XP.rotationDegrees(mc.level.getTimeOfDay(pt) * 360f));
            Matrix4f mm = ps.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, LUNA);
            RenderSystem.setShaderColor(1f, 1f, 1f, noche * k);
            float l = 27f;
            bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bb.vertex(mm, -l, -99f, l).uv(0f, 1f).endVertex();
            bb.vertex(mm, l, -99f, l).uv(1f, 1f).endVertex();
            bb.vertex(mm, l, -99f, -l).uv(1f, 0f).endVertex();
            bb.vertex(mm, -l, -99f, -l).uv(0f, 0f).endVertex();
            BufferUploader.drawWithShader(bb.end());
            ps.popPose();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /** Las columnas de luz roja de los portales abiertos (el haz de un faro, rojo y ancho, de mil bloques). */
    private static void columnas(RenderLevelStageEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (!activo || columnas.length < 3 || mc.level == null || mc.level.dimension() != Level.OVERWORLD) return;
        Vec3 cam = e.getCamera().getPosition();
        PoseStack ps = e.getPoseStack();
        MultiBufferSource.BufferSource bs = mc.renderBuffers().bufferSource();
        long t = mc.level.getGameTime();
        float pt = e.getPartialTick();
        for (int i = 0; i + 2 < columnas.length; i += 3) {
            double dx = columnas[i] + 0.5 - cam.x, dz = columnas[i + 2] + 0.5 - cam.z;
            if (dx * dx + dz * dz > 1200.0 * 1200.0) continue;
            ps.pushPose();
            ps.translate(dx - 0.5, columnas[i + 1] - cam.y, dz - 0.5);
            BeaconRenderer.renderBeaconBeam(ps, bs, BeaconRenderer.BEAM_LOCATION, pt, 0.6f, t, 0, 1024, new float[]{1f, 0.1f, 0.06f}, 1.3f, 2.4f);
            ps.popPose();
        }
        bs.endBatch();
    }
}
