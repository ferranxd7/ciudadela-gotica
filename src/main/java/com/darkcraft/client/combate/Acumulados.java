package com.darkcraft.client.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ui.Pix;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * El sangrado y la congelacion que se ven (parte 7aj, {@link Net.Acumula}): encima de cada uno que tenga algo sumado, una barrita
 * roja (sangre) o azul palido (frio) que se llena con lo que manda el servidor y baja sola como alli (a los 3 s sin sumar, 2 por
 * tick); al reventar se llena de golpe y se apaga en un destello. La del propio jugador va bajo sus barras, con su gota o su copo.
 * Donde cae cada uno en la pantalla se mira con las matrices con que se dibuja el mundo (como la marca de {@link Fijar}: asi
 * se ve igual con shaders).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Acumulados {
    private Acumulados() {}

    /** Hasta donde se ven las barritas de los demas (bloques). */
    private static final double LEJOS = 22;
    /** Lo que dura el destello de reventar (ticks). */
    private static final int DESTELLO = 16;

    private static final class Dato {
        final float[] nivel = new float[2];
        final long[] desde = new long[2];
        final long[] estallo = {Long.MIN_VALUE / 2, Long.MIN_VALUE / 2};
        float sx, sy;
        boolean enPantalla, aLaVista = true;
    }

    private static final Map<Integer, Dato> DATOS = new HashMap<>();

    private static long ahora() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0L : mc.level.getGameTime();
    }

    public static void recibir(Net.Acumula m) {
        int c = m.clase() == Net.Acumula.FRIO ? 1 : 0;
        Dato d = DATOS.computeIfAbsent(m.entidad(), k -> new Dato());
        long t = ahora();
        d.nivel[c] = m.nivel();
        d.desde[c] = t;
        if (m.estalla()) d.estallo[c] = t;
    }

    /** Lo que tiene ahora (0..100) de una clase (0 sangre, 1 frio). */
    private static float nivel(Dato d, int c, float t) {
        float quieto = t - d.desde[c];
        return Math.max(0f, d.nivel[c] - (quieto > 60 ? (quieto - 60) * 2f : 0f));
    }

    /** Lo que queda del destello de reventar (1 al reventar, 0 pasado). */
    private static float destello(Dato d, int c, float t) {
        float pasado = t - d.estallo[c];
        return pasado < 0 || pasado > DESTELLO ? 0f : 1f - pasado / DESTELLO;
    }

    private static boolean vivo(Dato d, float t) {
        return nivel(d, 0, t) > 0.5f || nivel(d, 1, t) > 0.5f || destello(d, 0, t) > 0f || destello(d, 1, t) > 0f;
    }

    /** Para la prueba de la CI: lo que tiene esa entidad ({sangre, frio}). */
    public static float[] de(int entidad) {
        Dato d = DATOS.get(entidad);
        long t = ahora();
        return d == null ? new float[]{0f, 0f} : new float[]{nivel(d, 0, t), nivel(d, 1, t)};
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            DATOS.clear();
            return;
        }
        long t = ahora();
        for (Iterator<Map.Entry<Integer, Dato>> it = DATOS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Dato> en = it.next();
            Entity ent = mc.level.getEntity(en.getKey());
            if (ent == null || !ent.isAlive() && destello(en.getValue(), 0, t) <= 0f && destello(en.getValue(), 1, t) <= 0f || !vivo(en.getValue(), t)) {
                it.remove();
                continue;
            }
            // tras una pared no se ve (se mira una vez por tick, no en cada fotograma)
            if (ent != mc.player) en.getValue().aLaVista = mc.player.hasLineOfSight(ent);
        }
    }

    /** Donde cae en la pantalla lo de encima de la cabeza de cada uno. */
    @SubscribeEvent
    public static void onEscena(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || DATOS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Vec3 cam = e.getCamera().getPosition();
        float pt = e.getPartialTick();
        for (Map.Entry<Integer, Dato> en : DATOS.entrySet()) {
            Dato d = en.getValue();
            d.enPantalla = false;
            Entity ent = mc.level.getEntity(en.getKey());
            if (ent == null || ent == mc.player || !d.aLaVista) continue;
            Vec3 p = ent.getPosition(pt).add(0, ent.getBbHeight() + 0.45, 0);
            if (p.distanceToSqr(cam) > LEJOS * LEJOS) continue;
            Vec3 c = p.subtract(cam);
            Vector4f v = new Vector4f((float) c.x, (float) c.y, (float) c.z, 1f);
            e.getPoseStack().last().pose().transform(v);
            e.getProjectionMatrix().transform(v);
            if (v.w() <= 0.05f) continue;
            d.sx = (v.x() / v.w() + 1f) * 0.5f;
            d.sy = (1f - v.y() / v.w()) * 0.5f;
            d.enPantalla = d.sx > -0.05f && d.sx < 1.05f && d.sy > -0.05f && d.sy < 1.05f;
        }
    }

    /** Las barritas de los demas, en el HUD (las llama {@code Hud}, debajo de todo lo demas). */
    public static void dibujar(GuiGraphics g, int w, int h, float pt) {
        if (DATOS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        float t = ahora() + pt;
        for (Map.Entry<Integer, Dato> en : DATOS.entrySet()) {
            Dato d = en.getValue();
            if (!d.enPantalla) continue;
            int x = Math.round(d.sx * w), y = Math.round(d.sy * h);
            int fila = 0;
            for (int c = 0; c < 2; c++) {
                float f = nivel(d, c, t) / 100f, luz = destello(d, c, t);
                if (f <= 0.005f && luz <= 0f) continue;
                barrita(g, x - 13, y - 6 * fila, 26, luz > 0f ? 1f : f, c, luz);
                fila++;
            }
        }
        Pix.blanco();
    }

    /**
     * La del propio jugador: bajo las barras (a partir de {@code y}), una por clase con su gota o su copo delante; devuelve
     * donde acaba (para lo que vaya debajo).
     */
    public static int dibujarPropio(GuiGraphics g, int x, int y, float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return y;
        Dato d = DATOS.get(mc.player.getId());
        if (d == null) return y;
        float t = ahora() + pt;
        for (int c = 0; c < 2; c++) {
            float f = nivel(d, c, t) / 100f, luz = destello(d, c, t);
            if (f <= 0.005f && luz <= 0f) continue;
            icono(g, x, y - 1, c);
            barrita(g, x + 10, y + 1, 56, luz > 0f ? 1f : f, c, luz);
            y += 9;
        }
        Pix.blanco();
        return y;
    }

    /** Una barrita: marco oscuro con las esquinas de oro, el relleno (con su brillo arriba) y el destello al reventar. */
    private static void barrita(GuiGraphics g, int x, int y, int ancho, float f, int clase, float luz) {
        g.fill(x - 1, y - 1, x + ancho + 1, y + 4, 0xE0120A0C);
        g.fill(x, y, x + ancho, y + 3, 0xB0382830);
        int oro = 0xFFC9A44C;
        g.fill(x - 1, y - 1, x, y, oro);
        g.fill(x + ancho, y - 1, x + ancho + 1, y, oro);
        g.fill(x - 1, y + 3, x, y + 4, oro);
        g.fill(x + ancho, y + 3, x + ancho + 1, y + 4, oro);
        int lleno = Mth.clamp(Math.round(ancho * f), 0, ancho);
        if (lleno > 0) {
            g.fill(x, y, x + lleno, y + 3, clase == 0 ? 0xFF9E1018 : 0xFF6FB6DA);
            g.fill(x, y, x + lleno, y + 1, clase == 0 ? 0xFFE04448 : 0xFFDDF4FF);
        }
        if (luz > 0f) g.fill(x - 1, y - 1, x + ancho + 1, y + 4, (Math.round(200 * luz) << 24) | (clase == 0 ? 0xFF5A4A : 0xF0FBFF));
    }

    /** La gota de sangre o el copo de escarcha (8 x 8, a pixeles). */
    private static void icono(GuiGraphics g, int x, int y, int clase) {
        if (clase == 0) {
            int o = 0xFF3A060A, r = 0xFFB3141C, b = 0xFFE86064;
            g.fill(x + 3, y, x + 5, y + 2, o);
            g.fill(x + 2, y + 2, x + 6, y + 4, o);
            g.fill(x + 1, y + 4, x + 7, y + 7, o);
            g.fill(x + 2, y + 7, x + 6, y + 8, o);
            g.fill(x + 3, y + 1, x + 5, y + 3, r);
            g.fill(x + 2, y + 3, x + 6, y + 7, r);
            g.fill(x + 2, y + 4, x + 3, y + 6, b);
        } else {
            int o = 0xFF1C2A36, c = 0xFFBFE8FA, b = 0xFFFFFFFF;
            g.fill(x + 3, y, x + 5, y + 8, o);
            g.fill(x, y + 3, x + 8, y + 5, o);
            g.fill(x + 1, y + 1, x + 3, y + 3, o);
            g.fill(x + 5, y + 5, x + 7, y + 7, o);
            g.fill(x + 5, y + 1, x + 7, y + 3, o);
            g.fill(x + 1, y + 5, x + 3, y + 7, o);
            g.fill(x + 4, y + 1, x + 4 + 1, y + 7, c);
            g.fill(x + 1, y + 4, x + 7, y + 4 + 1, c);
            g.fill(x + 2, y + 2, x + 3, y + 3, c);
            g.fill(x + 5, y + 5, x + 6, y + 6, c);
            g.fill(x + 5, y + 2, x + 6, y + 3, c);
            g.fill(x + 2, y + 5, x + 3, y + 6, c);
            g.fill(x + 4, y + 4, x + 5, y + 5, b);
        }
    }

    /** Para la prueba de la CI: cuantas entidades tienen algo y lo del jugador. */
    public static String resumen() {
        Minecraft mc = Minecraft.getInstance();
        long t = ahora();
        int vivos = 0;
        for (Dato d : DATOS.values()) if (vivo(d, t)) vivos++;
        float[] yo = mc.player == null ? new float[2] : de(mc.player.getId());
        return vivos + " con algo; el jugador sangre " + Math.round(yo[0]) + " frio " + Math.round(yo[1]);
    }

    /** Si se ve a esa entidad con algo sumado (para la prueba). */
    public static boolean tiene(LivingEntity le) {
        Dato d = DATOS.get(le.getId());
        return d != null && vivo(d, ahora());
    }
}
