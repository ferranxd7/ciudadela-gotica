package com.darkcraft.client.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.client.ui.ClientEquipo;
import com.darkcraft.client.ui.ClientRol;
import com.darkcraft.client.ui.MenuTex;
import com.darkcraft.client.ui.Panel;
import com.darkcraft.client.ui.Pix;
import com.darkcraft.client.ui.Teclas;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector4f;

/**
 * Fijar el objetivo (como en Elden Ring): la tecla (Q, o pulsar la rueda del raton) fija al enemigo que se tiene
 * delante y la camara (y el jugador, que se mueve y pega mirandole) le sigue; al mover el raton de golpe hacia un lado
 * se salta al siguiente por ese lado. Se suelta con la misma tecla, o solo si muere, se aleja o se pierde de vista. La
 * marca (un rombo de oro con el centro rojo que late) se dibuja en el HUD donde cae el pecho del enemigo en la pantalla:
 * asi se ve igual con shaders.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Fijar {
    private Fijar() {}

    /** Hasta donde se busca a quien fijar, y desde donde se suelta solo. */
    private static final double ALCANCE = 24, SUELTA = 32;
    /** Lo que hay que mover el raton de golpe (grados) para saltar al siguiente. */
    private static final float SALTO = 16f;

    private static LivingEntity objetivo;
    private static int sinVer, desde;
    /** La rotacion que se puso el ultimo fotograma (lo que cambie hasta el siguiente lo ha movido el raton). */
    private static float giroPuesto = Float.NaN, cabeceoPuesto;
    private static long ultimo;
    /** Lo que se ha movido el raton de lado (para saltar al siguiente) y cuando se dejo de mover. */
    private static float acumulado;
    /** Donde cae la marca en la pantalla (0..1) y si se ve. */
    private static float sx, sy;
    private static boolean enPantalla;

    public static LivingEntity objetivo() {
        return objetivo;
    }

    /** La tecla: fija al que se tiene delante, o suelta al fijado. */
    public static void alternar() {
        Minecraft mc = Minecraft.getInstance();
        if (objetivo != null) {
            soltar();
            return;
        }
        LivingEntity e = buscar(mc, null, 0f);
        if (e != null) fijar(mc, e);
    }

    private static void fijar(Minecraft mc, LivingEntity e) {
        objetivo = e;
        sinVer = 0;
        desde = ClientRol.ticks;
        acumulado = 0f;
        giroPuesto = Float.NaN;
        if (mc.player != null)
            mc.getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_FIJAR.get(), 0.95f + (float) Math.random() * 0.1f, 0.7f));
    }

    public static void soltar() {
        objetivo = null;
        giroPuesto = Float.NaN;
        enPantalla = false;
    }

    /** A quien se puede fijar: criaturas vivas que se ven; los enemigos antes que los animales (y nunca jugadores ni aldeanos). */
    private static boolean valido(LocalPlayer p, LivingEntity e) {
        return e != p && e.isAlive() && !e.isInvisible() && !e.isSpectator() && !(e instanceof ArmorStand) && !(e instanceof Player)
                && !(e instanceof AbstractVillager) && (e instanceof Enemy || e instanceof Mob);
    }

    /**
     * El mejor a quien fijar: el que queda mas cerca del centro de la mirada (y algo, de cerca), en un cono delante y a la
     * vista. Con {@code lado} distinto de 0, el mas cercano por ese lado del que hay fijado (para saltar al siguiente).
     */
    private static LivingEntity buscar(Minecraft mc, LivingEntity ahora, float lado) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return null;
        Vec3 ojo = p.getEyePosition();
        Vec3 mira = p.getViewVector(1f);
        float giroAhora = ahora != null ? giroHacia(ojo, centro(ahora, 1f)) : 0f;
        LivingEntity mejor = null;
        double nota = Double.MAX_VALUE;
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, new AABB(ojo, ojo).inflate(ALCANCE), x -> valido(p, x))) {
            if (e == ahora) continue;
            Vec3 c = centro(e, 1f);
            Vec3 d = c.subtract(ojo);
            double dist = d.length();
            if (dist > ALCANCE || dist < 0.5) continue;
            if (!p.hasLineOfSight(e)) continue;
            double nt;
            if (lado != 0f && ahora != null) {
                // por el lado que se ha movido el raton: el que queda mas cerca del fijado en ese sentido
                float dif = Mth.wrapDegrees(giroHacia(ojo, c) - giroAhora);
                if (Math.signum(dif) != Math.signum(lado) || Math.abs(dif) < 2f || Math.abs(dif) > 120f) continue;
                nt = Math.abs(dif) + dist * 0.5;
            } else {
                double ang = Math.toDegrees(Math.acos(Mth.clamp(d.normalize().dot(mira), -1, 1)));
                if (ang > 65) continue;
                nt = ang + dist * 1.2;
            }
            if (!(e instanceof Enemy)) nt += 35;
            if (nt < nota) {
                nota = nt;
                mejor = e;
            }
        }
        return mejor;
    }

    private static Vec3 centro(LivingEntity e, float pt) {
        return e.getPosition(pt).add(0, e.getBbHeight() * 0.62, 0);
    }

    private static float giroHacia(Vec3 desde, Vec3 hacia) {
        Vec3 d = hacia.subtract(desde);
        return (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90f;
    }

    // ------------------------------------------------------------------ cada tick: la tecla y si hay que soltar

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        boolean pulsada = false;
        while (Teclas.FIJAR.consumeClick()) pulsada = true;
        if (p == null || mc.level == null) {
            soltar();
            return;
        }
        if (pulsada && mc.screen == null && ClientEquipo.activo() && p.isAlive() && !ClientCine.active()) alternar();
        if (objetivo == null) return;
        // se suelta solo: muerto, lejos, sin verlo un rato, en una cinematica, o si el jugador muere o deja la supervivencia
        if (!objetivo.isAlive() || objetivo.isRemoved() || objetivo.level() != p.level() || !p.isAlive() || ClientCine.active()
                || !ClientEquipo.activo() || p.distanceTo(objetivo) > SUELTA) {
            soltar();
            return;
        }
        if (p.hasLineOfSight(objetivo)) sinVer = 0;
        else if (++sinVer > 40) {
            soltar();
            return;
        }
        // el raton, de golpe hacia un lado: al siguiente por ese lado
        if (Math.abs(acumulado) > SALTO) {
            LivingEntity otro = buscar(mc, objetivo, Math.signum(acumulado));
            acumulado = 0f;
            if (otro != null) fijar(mc, otro);
        }
        acumulado *= 0.6f;
    }

    // ------------------------------------------------------------------ cada fotograma: la camara mira al fijado

    @SubscribeEvent
    public static void onFrame(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        long ahora = Util.getNanos();
        float dt = Mth.clamp((ahora - ultimo) / 1.0e9f, 0f, 0.1f);
        ultimo = ahora;
        if (objetivo == null || p == null || mc.isPaused()) {
            giroPuesto = Float.NaN;
            return;
        }
        // lo que ha movido el raton desde el ultimo fotograma (el juego ya lo sumo a la rotacion)
        if (!Float.isNaN(giroPuesto)) acumulado += Mth.wrapDegrees(p.getYRot() - giroPuesto);
        float pt = e.renderTickTime;
        Vec3 ojo = p.getEyePosition(pt);
        Vec3 d = centro(objetivo, pt).subtract(ojo);
        float giro = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90f;
        float cabeceo = (float) -(Mth.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * Mth.RAD_TO_DEG);
        // en tercera persona la camara va detras y arriba: se mira un poco mas abajo para que se vea al enemigo entero
        if (!mc.options.getCameraType().isFirstPerson()) cabeceo += 8f;
        cabeceo = Mth.clamp(cabeceo, -55f, 60f);
        float base = Float.isNaN(giroPuesto) ? p.getYRot() : giroPuesto, baseC = Float.isNaN(giroPuesto) ? p.getXRot() : cabeceoPuesto;
        float k = 1f - (float) Math.exp(-dt * 13f);
        float yaw = base + Mth.wrapDegrees(giro - base) * k;
        float pitch = baseC + (cabeceo - baseC) * k;
        p.setYRot(yaw);
        p.setXRot(pitch);
        p.yHeadRot = yaw;
        giroPuesto = yaw;
        cabeceoPuesto = pitch;
    }

    /** Donde cae el pecho del fijado en la pantalla (con las mismas matrices con que se dibuja el mundo). */
    @SubscribeEvent
    public static void onEscena(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        enPantalla = false;
        if (objetivo == null) return;
        Vec3 c = centro(objetivo, e.getPartialTick()).subtract(e.getCamera().getPosition());
        Vector4f v = new Vector4f((float) c.x, (float) c.y, (float) c.z, 1f);
        e.getPoseStack().last().pose().transform(v);
        e.getProjectionMatrix().transform(v);
        if (v.w() <= 0.05f) return;
        sx = (v.x() / v.w() + 1f) * 0.5f;
        sy = (1f - v.y() / v.w()) * 0.5f;
        enPantalla = sx > -0.05f && sx < 1.05f && sy > -0.05f && sy < 1.05f;
    }

    /** La marca, en el HUD (la llama {@code Hud}). */
    public static void dibujar(GuiGraphics g, int w, int h, float pt) {
        if (objetivo == null || !enPantalla) return;
        float edad = ClientRol.ticks - desde + pt;
        int x = Math.round(sx * w), y = Math.round(sy * h);
        // al fijar, la marca llega de grande a su sitio; luego late despacio
        float entra = Mth.clamp(edad / 5f, 0f, 1f);
        float late = 0.5f + 0.5f * Mth.sin(edad * 0.25f);
        Pix.mezcla();
        if (entra < 1f) {
            int r = Math.round((1f - entra) * 10f);
            Pix.color(1f, 1f, 1f, entra);
            Panel.t(g, MenuTex.FIJAR, x - 4 - r, y - 4);
            Panel.t(g, MenuTex.FIJAR, x - 4 + r, y - 4);
        }
        Pix.color(1f, 1f, 1f, 0.92f);
        Panel.t(g, MenuTex.FIJAR, x - 4, y - 4);
        Pix.color(1f, 1f, 1f, 0.75f * late * entra);
        Panel.t(g, MenuTex.FIJAR_LUZ, x - 4, y - 4);
        Pix.blanco();
    }

    /** Para la prueba de la CI. */
    public static String resumen() {
        if (objetivo == null) return "-";
        return objetivo.getType().toShortString() + "#" + objetivo.getId() + (enPantalla ? String.format(java.util.Locale.ROOT, " en %.2f,%.2f", sx, sy) : " fuera");
    }
}
