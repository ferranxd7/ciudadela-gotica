package com.darkcraft.client.espejo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.espejo.Espejo;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * El Espejo de la Sombra en la pantalla: un espejo REDONDO en pixel art (aro de piedra oscura con filo de oro, gemas
 * rojas y la cresta con el ojo de la Sombra; tools/ui/espejo.py) arriba a la derecha, con el mapa oscurecido como un
 * reflejo que gira con el jugador, humo de la Sombra alrededor, el ojo de la cresta que late, una cuenta roja en el aro
 * hacia el norte, los lugares, cofres, jefes, amigos, marcas y la espada de luz, y debajo el nombre del sitio (sin
 * coordenadas: el usuario pidio quitarlas). Bajo techo ensena la planta del piso en el que esta el jugador.
 */
public final class EspejoHud {
    private EspejoHud() {}

    static ResourceLocation rl(String p) {
        return new ResourceLocation(DarkCraftMod.MODID, p);
    }

    /**
     * Mezcla normal encendida. Hace falta antes de cada blit con transparencia: {@code g.fill}, {@code drawString} y
     * {@code PlayerFaceRenderer} la dejan apagada al acabar, y sin ella lo transparente sale opaco.
     */
    public static void mezcla() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** Mezcla aditiva (brillos). */
    public static void suma() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }

    /** Un cuadro con toda la textura y esquinas en coma flotante (dos cuadros que comparten borde no dejan rendija). */
    static void cuadro(GuiGraphics g, ResourceLocation tex, float x0, float y0, float x1, float y1) {
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f m = g.pose().last().pose();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bb.vertex(m, x0, y0, 0).uv(0, 0).endVertex();
        bb.vertex(m, x0, y1, 0).uv(0, 1).endVertex();
        bb.vertex(m, x1, y1, 0).uv(1, 1).endVertex();
        bb.vertex(m, x1, y0, 0).uv(1, 0).endVertex();
        BufferUploader.drawWithShader(bb.end());
    }

    static final ResourceLocation HUMO = rl("textures/gui/humo.png"), ICONOS = rl("textures/gui/espejo_iconos.png"),
            BRILLO = rl("textures/gui/brillo.png");
    /** Iconos del atlas (16 por fila). */
    public static final int I_JUGADOR = 0, I_COFRE = 5, I_COFRE_LEIDO = 6, I_MARCAS = 16, I_SELLO = 28, I_SELLO_GRIS = 29,
            I_CANDADO = 30, I_PERGAMINO = 31, I_MAPA = 32, I_TROFEO = 33, I_BESTIA = 34, I_PUNTO = 35, I_FLECHA = 36,
            I_LIBRO = 14, I_PLUMA = 15, I_EQUIS = 21, I_PORTAL = 13, I_CASA = 11,
    /** Los de Morvain (parte 7d): un santuario, la ciudadela encadenada y una estela del mapa. */
    I_SANTUARIO = 37, I_CIUDADELA = 38, I_ESTELA = 39,
    /** La campana de una mision (parte 7ag): adonde ir, y la de una mision acabada (gris). */
    I_MISION = 40, I_MISION_HECHA = 41;

    // ------------------------------------------------------------------ textura del mapa del espejo

    /** Lado de la textura del mapa (un texel por bloque, centrada donde estaba el jugador al pintarla). */
    static final int N = 320;
    private static DynamicTexture tex;
    private static ResourceLocation texId;
    private static int texX = Integer.MIN_VALUE, texZ, texTick = -1000;
    private static boolean texDentro;
    private static int tickHud;

    static void soltar() {
        if (tex != null) {
            Minecraft.getInstance().getTextureManager().release(texId);
            tex = null;
            texId = null;
        }
        texX = Integer.MIN_VALUE;
        Planta.reiniciar();
    }

    /** Color del espejo: el del mapa mas oscuro y apagado, con un poco de rojo en las sombras y grano. */
    static int estilo(int argb, int x, int z) {
        if ((argb >>> 24) == 0) return niebla(x, z);
        if (argb == Exploracion.VACIO) return 0xff050306;
        int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        float l = 0.3f * r + 0.59f * g + 0.11f * b;
        float rr = l + (r - l) * 0.62f, gg = l + (g - l) * 0.62f, bb = l + (b - l) * 0.62f;
        float grano = ((ruido(x, z) & 15) - 7.5f) * 0.7f;
        rr = rr * 0.70f + 12 + grano;
        gg = gg * 0.58f + 2 + grano;
        bb = bb * 0.62f + 6 + grano;
        return 0xff000000 | (cl(rr) << 16) | (cl(gg) << 8) | cl(bb);
    }

    static int niebla(int x, int z) {
        int n = ruido(x >> 2, z >> 2) & 7;
        return 0xff000000 | ((9 + n) << 16) | (4 << 8) | (7 + (n >> 1));
    }

    static int ruido(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    static int cl(float v) {
        return v < 0 ? 0 : v > 255 ? 255 : (int) v;
    }

    private static int abgr(int argb) {
        return (argb & 0xff00ff00) | ((argb >> 16) & 0xff) | ((argb & 0xff) << 16);
    }

    private static void pintarTextura(Minecraft mc, int px, int pz) {
        if (tex == null) {
            tex = new DynamicTexture(N, N, false);
            texId = mc.getTextureManager().register("darkcraft_espejo", tex);
        }
        NativeImage img = tex.getPixels();
        if (img == null) return;
        Exploracion ex = EspejoCliente.exploracion();
        boolean dentro = EspejoCliente.dentro;
        int x0 = px - N / 2, z0 = pz - N / 2;
        for (int j = 0; j < N; j++) {
            int wz = z0 + j;
            for (int i = 0; i < N; i++) {
                int wx = x0 + i;
                int c;
                if (i == 0 || j == 0 || i == N - 1 || j == N - 1) c = niebla(wx, wz);
                else {
                    int base = ex == null ? 0 : ex.get(wx, wz);
                    if (dentro) {
                        int p = Planta.color(wx, wz);
                        if (p != 0) c = p;
                        else {
                            int e = estilo(base, wx, wz);
                            c = 0xff000000 | Exploracion.escala(e & 0xffffff, 0.32f);
                        }
                    } else c = estilo(base, wx, wz);
                }
                img.setPixelRGBA(i, j, abgr(c));
            }
        }
        tex.upload();
        RenderSystem.bindTexture(tex.getId());
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        texX = px;
        texZ = pz;
        texDentro = dentro;
        texTick = tickHud;
    }

    // ------------------------------------------------------------------ la planta del piso (bajo techo)

    /** Lo que hay a la altura de los pies del jugador alrededor: muros claros, suelo oscuro (se repinta por filas). */
    static final class Planta {
        static final int RP = 48, P = RP * 2 + 1;
        private static final int[] col = new int[P * P];
        private static int cx = Integer.MIN_VALUE, cz, cy, fila;

        static void reiniciar() {
            cx = Integer.MIN_VALUE;
        }

        static int color(int x, int z) {
            if (cx == Integer.MIN_VALUE) return 0;
            int i = x - (cx - RP), j = z - (cz - RP);
            if (i < 0 || j < 0 || i >= P || j >= P) return 0;
            int dx = i - RP, dz = j - RP;
            if (dx * dx + dz * dz > RP * RP) return 0;
            return col[j * P + i];
        }

        /** Repinta unas filas (lo llama EspejoCliente cada tick mientras esta bajo techo). */
        static void tick(ClientLevel lvl, BlockPos jugador) {
            int jx = jugador.getX(), jy = jugador.getY(), jz = jugador.getZ();
            if (cx == Integer.MIN_VALUE || Math.abs(jx - cx) > 8 || Math.abs(jz - cz) > 8 || Math.abs(jy - cy) >= 2) {
                boolean nueva = cx == Integer.MIN_VALUE || Math.abs(jy - cy) >= 2;
                cx = jx;
                cz = jz;
                cy = jy;
                fila = 0;
                if (nueva) java.util.Arrays.fill(col, 0);
            }
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int n = 0; n < 10; n++) {
                int j = fila;
                fila = (fila + 1) % P;
                int z = cz - RP + j;
                for (int i = 0; i < P; i++) {
                    int dx = i - RP, dz = j - RP;
                    if (dx * dx + dz * dz > RP * RP) continue;
                    col[j * P + i] = columna(lvl, cx - RP + i, z, cy, m);
                }
            }
        }

        private static boolean solido(ClientLevel lvl, int x, int y, int z, BlockPos.MutableBlockPos m) {
            BlockState st = lvl.getBlockState(m.set(x, y, z));
            return !st.isAir() && st.blocksMotion();
        }

        /** Se puede estar de pie: ni los pies ni la cabeza dan con algo solido. */
        private static boolean hueco(ClientLevel lvl, int x, int y, int z, BlockPos.MutableBlockPos m) {
            return !solido(lvl, x, y, z, m) && !solido(lvl, x, y + 1, z, m);
        }

        private static int columna(ClientLevel lvl, int x, int z, int y, BlockPos.MutableBlockPos m) {
            if (!lvl.hasChunk(x >> 4, z >> 4)) return 0;
            // muro: algo solido a la altura de los pies o de la cabeza
            for (int dy = 1; dy >= 0; dy--) {
                BlockState st = lvl.getBlockState(m.set(x, y + dy, z));
                if (!st.isAir() && st.blocksMotion()) {
                    // solo la cara que da a un hueco es muro; lo de dentro es roca maciza (rayada, como en un plano)
                    if (!hueco(lvl, x + 1, y, z, m) && !hueco(lvl, x - 1, y, z, m) && !hueco(lvl, x, y, z + 1, m) && !hueco(lvl, x, y, z - 1, m))
                        return Math.floorMod(x + z, 4) == 0 ? 0xff1c1418 : 0xff0b0809;
                    m.set(x, y + dy, z);
                    MapColor mc = st.getMapColor(lvl, m);
                    int base = mc == MapColor.NONE ? 0x808080 : mc.col;
                    int r = (((base >> 16) & 255) + 3 * 206) / 4, g = (((base >> 8) & 255) + 3 * 198) / 4, b = ((base & 255) + 3 * 204) / 4;
                    return 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }
            // suelo: lo primero que haya por debajo de los pies (apagado, para que los muros se lean bien)
            for (int d = 0; d < 9; d++) {
                BlockState st = lvl.getBlockState(m.set(x, y - 1 - d, z));
                if (st.isAir()) continue;
                MapColor mc = st.getMapColor(lvl, m);
                if (mc == MapColor.NONE) continue;
                int e = estilo(0xff000000 | mc.col, x, z);
                int r = (((e >> 16) & 255) * 2 + 0x46) / 3, g = (((e >> 8) & 255) * 2 + 0x30) / 3, b = ((e & 255) * 2 + 0x36) / 3;
                return 0xff000000 | Exploracion.escala((r << 16) | (g << 8) | b, Math.max(0.4f, 0.92f - d * 0.07f));
            }
            return 0xff060406;
        }
    }

    // ------------------------------------------------------------------ dibujo

    /** Radio del cristal (en px de la interfaz) de cada tamano: hay una textura dibujada a mano para cada uno. */
    static final int[] RADIOS = {28, 36, 46};
    /** Grosor del marco por fuera del cristal y lo que sube la cresta por encima (como en tools/ui/espejo.py). */
    static final int BORDE = 8, CRESTA = 7;
    private static final ResourceLocation[] REDONDO = {rl("textures/gui/espejo_r28.png"), rl("textures/gui/espejo_r36.png"),
            rl("textures/gui/espejo_r46.png")};

    /** Que tamano se dibuja: el elegido, o uno menor si la pantalla es pequena. */
    private static int tamano(int sh) {
        int i = Mth.clamp(EspejoCliente.tamano, 0, RADIOS.length - 1);
        while (i > 0 && 2 * (RADIOS[i] + BORDE) + 1 + CRESTA > sh * 0.45f) i--;
        return i;
    }

    /** Lo que ocupa el espejo a lo ancho en la esquina de arriba a la derecha (0 si no se ve): para que la brujula le deje sitio. */
    public static int anchoEnPantalla() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !EspejoCliente.activo() || !EspejoCliente.visible) return 0;
        return 2 * (RADIOS[tamano(mc.getWindow().getGuiScaledHeight())] + BORDE) + 1 + 6;
    }

    public static void render(ForgeGui gui, GuiGraphics g, float pt, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !EspejoCliente.activo()) return;
        if (EspejoCliente.aviso >= 0) aviso(mc, g, pt, sw, sh);
        if (!EspejoCliente.visible || mc.options.hideGui || mc.options.renderDebug || ClientCine.active() || com.darkcraft.client.ui.Hud.tapado(mc))
            return;
        tickHud = mc.player.tickCount;
        int idx = tamano(sh);
        int R = RADIOS[idx], lado = 2 * (R + BORDE) + 1;
        int x0 = sw - lado - 3, y0 = 2 + Math.round(efectos(mc));
        float a = Mth.clamp((EspejoCliente.aparecer + pt) / 50f, 0, 1);
        if (a <= 0.01f) return;
        // el centro del cristal cae en el centro de un pixel de la textura
        float cxs = x0 + R + BORDE + 0.5f, cys = y0 + CRESTA + R + BORDE + 0.5f;
        float t = (mc.player.tickCount + pt) / 20f;
        float s = R / 128f;

        double px = Mth.lerp(pt, mc.player.xo, mc.player.getX()), pz = Mth.lerp(pt, mc.player.zo, mc.player.getZ());
        int bx = Mth.floor(px), bz = Mth.floor(pz);
        if (texX == Integer.MIN_VALUE || Math.abs(bx - texX) > 24 || Math.abs(bz - texZ) > 24 || tickHud - texTick >= 10
                || tickHud < texTick || texDentro != EspejoCliente.dentro)
            pintarTextura(mc, bx, bz);
        if (tex == null) return;

        float yaw = Mth.rotLerp(pt, mc.player.yRotO, mc.player.getYRot()) * Mth.DEG_TO_RAD;
        float fx = -Mth.sin(yaw), fz = Mth.cos(yaw), rx = -Mth.cos(yaw), rz = -Mth.sin(yaw);
        float bloques = EspejoCliente.ZOOM[EspejoCliente.zoom];
        float esc = R / bloques;

        g.flush();
        mezcla();
        humo(g, cxs, cys, R, s, t, a, false);
        mezcla();

        // el mapa: un abanico de triangulos con la forma del cristal
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texId);
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = g.pose().last().pose();
        int alfa = (int) (255 * a);
        double u0 = px - (texX - N / 2.0), v0 = pz - (texZ - N / 2.0);
        bb.vertex(mat, cxs, cys, 0).uv((float) (u0 / N), (float) (v0 / N)).color(255, 255, 255, alfa).endVertex();
        int seg = 72;
        float rm = R + 1.0f;
        for (int k = 0; k <= seg; k++) {
            double ang = -k * Math.PI * 2 / seg;
            float ex = (float) Math.cos(ang) * rm, ey = (float) Math.sin(ang) * rm;
            double dx = (ex * rx - ey * fx) / esc, dz = (ex * rz - ey * fz) / esc;
            bb.vertex(mat, cxs + ex, cys + ey, 0).uv((float) ((u0 + dx) / N), (float) ((v0 + dz) / N)).color(255, 255, 255, alfa).endVertex();
        }
        BufferUploader.drawWithShader(bb.end());

        marcadores(mc, g, pt, cxs, cys, R, esc, px, pz, fx, fz, rx, rz, t, a);
        mezcla();

        // el jugador: la flecha en el centro (el mapa gira, la flecha siempre mira arriba)
        RenderSystem.setShaderColor(1, 1, 1, a);
        icono(g, I_JUGADOR, cxs, cys, R >= 46 ? 11f : R >= 36 ? 9f : 8f);

        // el cristal (penumbra y reflejo) y el marco, pixel a pixel
        mezcla();
        g.blit(REDONDO[idx], x0, y0, lado, 0, lado, lado + CRESTA, lado * 2, lado + CRESTA);
        mezcla();
        g.blit(REDONDO[idx], x0, y0, 0, 0, lado, lado + CRESTA, lado * 2, lado + CRESTA);
        // el ojo de la cresta late
        float latido = 0.55f + 0.45f * (float) Math.pow(Math.max(0, Math.sin(t * 2.3)), 3);
        if (EspejoCliente.aparecer < 120) latido = Math.max(latido, 1.2f - (EspejoCliente.aparecer + pt) / 120f);
        suma();
        RenderSystem.setShaderColor(1f, 0.15f, 0.12f, Math.min(1f, latido * 0.8f) * a);
        g.blit(BRILLO, x0 + R + BORDE - 9, y0 - 3, 19, 19, 0, 0, 64, 64, 64, 64);
        mezcla();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        humo(g, cxs, cys, R, s, t, a, true);
        norte(g, cxs, cys, R, yaw, a);
        texto(mc, g, cxs, y0 + CRESTA + lado, a, sw);
        mezcla();
    }

    /** Cuanto bajar el espejo para no tapar los iconos de los efectos (arriba a la derecha). */
    private static float efectos(Minecraft mc) {
        boolean bueno = false, malo = false;
        for (MobEffectInstance e : mc.player.getActiveEffects()) {
            if (!e.showIcon()) continue;
            if (e.getEffect().isBeneficial()) bueno = true;
            else malo = true;
        }
        return malo ? 52 : bueno ? 26 : 0;
    }

    /** Un icono del atlas centrado en (x, y) con lado l (en px de la gui). */
    public static void icono(GuiGraphics g, int i, float x, float y, float l) {
        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(x - l / 2, y - l / 2, 0);
        g.pose().scale(l / 16f, l / 16f, 1);
        g.blit(ICONOS, 0, 0, (i % 16) * 16, (i / 16) * 16, 16, 16, 256, 64);
        g.pose().popPose();
    }

    /** Humo de la Sombra: bocanadas negras y rojas que salen del marco y se deshacen. */
    private static void humo(GuiGraphics g, float cx, float cy, int R, float s, float t, float a, boolean delante) {
        mezcla();
        int n = delante ? 6 : 16;
        float fuera = R + BORDE - 2;
        for (int i = 0; i < n; i++) {
            float fase = (i * 0.618f + (delante ? 0.31f : 0f)) % 1f;
            float vida = ((t / (delante ? 5.5f : 4.2f)) + fase) % 1f;
            double ang = (i * (Math.PI * 2 / n)) + Math.sin(t * 0.21 + i) * 0.25 + vida * 0.35;
            float rr = ((delante ? 2f : 6f) + vida * (delante ? 8f : 16f)) * s;
            float x = cx + (float) Math.cos(ang) * (fuera + rr), y = cy + (float) Math.sin(ang) * (fuera + rr);
            float lado = (delante ? 22f : 34f + 24f * vida) * s;
            float al = (float) Math.sin(Math.PI * vida) * (delante ? 0.2f : 0.4f) * a;
            if (EspejoCliente.aparecer < 80) al = Math.min(1f, al * (2.2f - EspejoCliente.aparecer / 60f));
            boolean rojo = i % 3 == 0;
            RenderSystem.setShaderColor(rojo ? 0.34f : 0.03f, rojo ? 0.02f : 0.015f, rojo ? 0.04f : 0.025f, al);
            g.blit(HUMO, Math.round(x - lado / 2), Math.round(y - lado / 2), Math.round(lado), Math.round(lado), 0, 0, 64, 64, 64, 64);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    /** El norte: una cuenta roja que recorre el aro del marco (el mapa gira); la letra va en la brujula de arriba. */
    private static void norte(GuiGraphics g, float cx, float cy, int R, float yaw, float a) {
        float dx = Mth.sin(yaw), dy = Mth.cos(yaw);
        float r = R + 4.5f;
        int x = Math.round(cx + dx * r - 0.5f), y = Math.round(cy + dy * r - 0.5f);
        int al = (int) (a * 255) << 24;
        if ((al >>> 24) < 8) return;
        g.fill(x - 1, y - 2, x + 2, y + 3, al | 0x050408);
        g.fill(x - 2, y - 1, x + 3, y + 2, al | 0x050408);
        g.fill(x - 1, y - 1, x + 2, y + 2, al | 0xDE3A3A);
        g.fill(x - 1, y - 1, x, y, al | 0xFFD8C8);
        g.flush();
    }

    /** Debajo del espejo: solo el nombre del sitio (el usuario pidio quitar las coordenadas: quedaban mal). */
    private static void texto(Minecraft mc, GuiGraphics g, float cx, int yBottom, float a, int sw) {
        Component nombre;
        String l = EspejoCliente.lugar;
        if (l != null && !l.isEmpty()) nombre = Component.translatable(l);
        else {
            var b = mc.level.getBiome(mc.player.blockPosition()).unwrapKey();
            String k = b.isPresent() ? "biome." + b.get().location().getNamespace() + "." + b.get().location().getPath() : null;
            // biomas de otros mods sin nombre: "tierras desconocidas" (y no la clave en crudo)
            nombre = k != null && net.minecraft.client.resources.language.I18n.exists(k) ? Component.translatable(k)
                    : Component.translatable("darkcraft.espejo.desconocido");
        }
        int alfa = (int) (a * 255);
        if (alfa < 8) return;
        int w1 = mc.font.width(nombre);
        int w = w1 + 10;
        // centrado debajo del espejo, pero sin salirse por la derecha
        int x = Math.min(Math.round(cx - w / 2f), sw - 2 - w);
        int y = yBottom + 1;
        g.fill(x, y, x + w, y + 12, ((int) (0x9a * a) << 24) | 0x0a0406);
        g.fill(x, y + 12, x + w, y + 13, ((int) (0xc0 * a) << 24) | 0x96702A);
        g.fill(x, y - 1, x + w, y, ((int) (0x80 * a) << 24) | 0x4A3312);
        g.drawString(mc.font, nombre, x + 5, y + 2, (alfa << 24) | 0xEADBC4, true);
    }

    // ------------------------------------------------------------------ marcadores

    private static void marcadores(Minecraft mc, GuiGraphics g, float pt, float cx, float cy, int R, float esc, double px, double pz,
                                   float fx, float fz, float rx, float rz, float t, float a) {
        float l = R >= 46 ? 10f : R >= 36 ? 9f : 8f;
        Net.EspejoEstado e = EspejoCliente.estado;
        int siguiente = com.darkcraft.story.Story.next(e.leidos());
        if (mc.level.dimension() == Level.OVERWORLD) {
            for (Net.LugarE lu : e.lugares()) {
                float[] q = pantalla(lu.x() + 0.5, lu.z() + 0.5, px, pz, fx, fz, rx, rz, esc);
                boolean objetivo = lu.tipo() == Espejo.COFRE && lu.cap() == siguiente && lu.estado() != Espejo.HECHO;
                boolean dentroE = dentro(q[0], q[1], R * 0.9f);
                if (!dentroE && !objetivo) continue;
                float[] d = dentroE ? q : borde(q[0], q[1], R * 0.86f);
                float x = cx + d[0], y = cy + d[1];
                int ic = lu.icono();
                if (lu.tipo() == Espejo.COFRE && lu.estado() == Espejo.HECHO) ic = I_COFRE_LEIDO;
                if (objetivo) {
                    float lat = 0.6f + 0.4f * (float) Math.sin(t * 4);
                    suma();
                    RenderSystem.setShaderColor(1f, 0.2f, 0.1f, 0.75f * lat * a);
                    float r = l * 1.4f;
                    g.blit(BRILLO, Math.round(x - r), Math.round(y - r), Math.round(r * 2), Math.round(r * 2), 0, 0, 64, 64, 64, 64);
                    mezcla();
                }
                float al = (dentroE ? 1f : 0.75f) * a;
                RenderSystem.setShaderColor(1, 1, 1, al);
                icono(g, ic, x, y, dentroE ? l : l * 0.8f);
                if (lu.tipo() == Espejo.JEFE && lu.estado() == Espejo.HECHO) {
                    RenderSystem.setShaderColor(0.9f, 0.12f, 0.1f, al);
                    icono(g, I_EQUIS, x, y, l * 0.8f);
                }
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
        }
        Marcas ms = EspejoCliente.marcas();
        if (ms != null) {
            for (Marcas.Marca m : ms.lista) {
                float[] q = pantalla(m.x + 0.5, m.z + 0.5, px, pz, fx, fz, rx, rz, esc);
                boolean dentroE = dentro(q[0], q[1], R * 0.9f);
                float[] d = dentroE ? q : borde(q[0], q[1], R * 0.86f);
                int c = m.color;
                RenderSystem.setShaderColor(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, (dentroE ? 1f : 0.7f) * a);
                icono(g, I_MARCAS + Mth.clamp(m.icono, 0, Marcas.ICONOS - 1), cx + d[0], cy + d[1], dentroE ? l : l * 0.75f);
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        // la espada de luz del jugador (donde murio): un destello dorado
        if (com.darkcraft.client.ui.ClientRol.hayEspada
                && mc.level.dimension().location().toString().equals(com.darkcraft.client.ui.ClientRol.espadaDim)) {
            float[] q = pantalla(com.darkcraft.client.ui.ClientRol.ex, com.darkcraft.client.ui.ClientRol.ez, px, pz, fx, fz, rx, rz, esc);
            boolean dentroE = dentro(q[0], q[1], R * 0.9f);
            float[] d = dentroE ? q : borde(q[0], q[1], R * 0.86f);
            float x = cx + d[0], y = cy + d[1];
            float lat = 0.6f + 0.4f * (float) Math.sin(t * 3);
            suma();
            RenderSystem.setShaderColor(1f, 0.85f, 0.4f, 0.8f * lat * a);
            float r = l * 1.2f;
            g.blit(BRILLO, Math.round(x - r), Math.round(y - r), Math.round(r * 2), Math.round(r * 2), 0, 0, 64, 64, 64, 64);
            mezcla();
            RenderSystem.setShaderColor(1, 1, 1, (dentroE ? 1f : 0.75f) * a);
            com.darkcraft.client.ui.HudTex.T me = com.darkcraft.client.ui.HudTex.MARCA_ESPADA;
            g.blit(com.darkcraft.client.ui.HudTex.TEX, Math.round(x) - me.w() / 2, Math.round(y) - me.h() / 2, me.u(), me.v(), me.w(), me.h(),
                    com.darkcraft.client.ui.HudTex.TW, com.darkcraft.client.ui.HudTex.TH);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        // amigos: su cara
        for (Net.Amigo am : EspejoCliente.amigos()) {
            double ax = am.x() + 0.5, az = am.z() + 0.5;
            var ent = mc.level.getPlayerByUUID(am.id());
            if (ent != null) {
                ax = Mth.lerp(pt, ent.xo, ent.getX());
                az = Mth.lerp(pt, ent.zo, ent.getZ());
            }
            float[] q = pantalla(ax, az, px, pz, fx, fz, rx, rz, esc);
            boolean dentroE = dentro(q[0], q[1], R * 0.88f);
            float[] d = dentroE ? q : borde(q[0], q[1], R * 0.84f);
            int lado = Math.max(6, Math.round(l * (dentroE ? 1f : 0.8f)));
            float x = cx + d[0], y = cy + d[1];
            g.fill(Math.round(x - lado / 2f) - 1, Math.round(y - lado / 2f) - 1, Math.round(x - lado / 2f) + lado + 1,
                    Math.round(y - lado / 2f) + lado + 1, ((int) (a * 230) << 24) | 0x8A1016);
            mezcla();
            RenderSystem.setShaderColor(1, 1, 1, a);
            PlayerFaceRenderer.draw(g, piel(mc, am), Math.round(x - lado / 2f), Math.round(y - lado / 2f), lado);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            mezcla(); // la cara (el sombrero de la piel) apaga la mezcla al acabar
        }
    }

    static ResourceLocation piel(Minecraft mc, Net.Amigo am) {
        PlayerInfo pi = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(am.id());
        return pi != null ? pi.getSkinLocation() : DefaultPlayerSkin.getDefaultSkin(am.id());
    }

    /** Donde cae en el espejo (relativo al centro, en px de la gui) un punto del mundo. */
    static float[] pantalla(double wx, double wz, double px, double pz, float fx, float fz, float rx, float rz, float esc) {
        double dx = wx - px, dz = wz - pz;
        return new float[]{(float) ((dx * rx + dz * rz) * esc), (float) (-(dx * fx + dz * fz) * esc)};
    }

    static boolean dentro(float x, float y, float r) {
        return x * x + y * y <= r * r;
    }

    /** El punto del circulo de radio r en la direccion de (x, y). */
    static float[] borde(float x, float y, float r) {
        float f = r / (float) Math.sqrt(x * x + y * y + 1e-9f);
        return new float[]{x * f, y * f};
    }

    // ------------------------------------------------------------------ aviso de que despierta

    private static void aviso(Minecraft mc, GuiGraphics g, float pt, int sw, int sh) {
        float t = EspejoCliente.aviso + pt;
        float a = t < 25 ? t / 25f : t > 160 ? Math.max(0, (200 - t) / 40f) : 1f;
        if (a <= 0.03f) return;
        Component titulo = Component.translatable("darkcraft.espejo.despierta");
        Component sub = Component.translatable("darkcraft.espejo.despierta.teclas", EspejoCliente.TECLA_LIBRO.getTranslatedKeyMessage(),
                EspejoCliente.TECLA_ESPEJO.getTranslatedKeyMessage());
        int y = Math.round(sh * 0.26f);
        int alfa = Math.round(a * 255);
        com.darkcraft.client.ui.Pix.franja(g, 0, sw, y - 9, y + 44, 0x050408, 0.6f * a);
        int w = com.darkcraft.client.ui.Letra.anchoG(mc.font, titulo);
        com.darkcraft.client.ui.Letra.grande(g, mc.font, titulo, sw / 2 - w / 2, y, (alfa << 24) | 0xC8242C, 0xFF050408);
        int w2 = mc.font.width(sub);
        g.drawString(mc.font, sub, sw / 2 - w2 / 2, y + 28, (Math.round(a * 230) << 24) | 0xD8CCB8, true);
        mezcla();
    }
}
