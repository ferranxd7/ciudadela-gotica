package com.darkcraft.client.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ui.ClientRol;
import com.darkcraft.combate.Armas;
import com.darkcraft.combate.Armas.Anim;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Como se ve el combate: las poses de cada golpe (brazos y giro del cuerpo, en tercera persona, para el jugador y para
 * los demas, que lo ven todos y tambien con shaders: es el modelo del jugador de siempre), la voltereta (el cuerpo
 * entero da la vuelta) y, en primera persona, por donde pasa el arma ({@link #fp}).
 * <p>
 * Cada golpe tiene dos poses: la de prepararlo (al empezar a hacer dano) y la de acabarlo (al dejar de hacerlo). Del
 * cuerpo normal a la primera se va suave, de la primera a la segunda de golpe (es el tajo), y luego se vuelve.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Poses {
    private Poses() {}

    private static final float N = Float.NaN;

    /** Una pose en grados: brazo derecho (x, y, z), brazo izquierdo (x, y, z; NaN = el que ya tenga) y giro del cuerpo. */
    record P(float rx, float ry, float rz, float lx, float ly, float lz, float giro) {
        P espejo() {
            return new P(lx, Float.isNaN(ly) ? N : -ly, Float.isNaN(lz) ? N : -lz, rx, -ry, -rz, -giro);
        }

        /** A dos manos: si el brazo izquierdo esta libre, se va a agarrar el arma junto al derecho. */
        P dosManos() {
            if (!Float.isNaN(lx)) return this;
            return new P(rx, ry, rz, rx + 4f, ry + 38f, 0f, giro);
        }

        static P mezcla(P a, P b, float k) {
            return new P(l(a.rx, b.rx, k), l(a.ry, b.ry, k), l(a.rz, b.rz, k), l(a.lx, b.lx, k), l(a.ly, b.ly, k), l(a.lz, b.lz, k), l(a.giro, b.giro, k));
        }

        private static float l(float a, float b, float k) {
            if (Float.isNaN(a) || Float.isNaN(b)) return Float.isNaN(b) ? a : b;
            return a + (b - a) * k;
        }
    }

    /** Las dos poses de cada animacion: preparar y acabar. */
    private static final Map<Anim, P[]> TP = new HashMap<>();
    /** En primera persona, por donde va el arma: {tx, ty, tz, rx, ry, rz} al preparar y al acabar. */
    private static final Map<Anim, float[][]> FP = new HashMap<>();

    private static void tp(Anim a, P prepara, P golpe) {
        TP.put(a, new P[]{prepara, golpe});
    }

    private static void fp(Anim a, float[] prepara, float[] golpe) {
        FP.put(a, new float[][]{prepara, golpe});
    }

    static {
        tp(Anim.TAJO_DI, new P(-110, 55, 10, -20, N, N, 28), new P(-80, -75, 0, -15, N, N, -32));
        tp(Anim.TAJO_ID, new P(-95, -75, 0, -15, N, N, -28), new P(-85, 65, 10, -20, N, N, 30));
        tp(Anim.VERTICAL, new P(-175, 15, 0, -30, N, N, 12), new P(-45, -5, 0, -25, N, N, -8));
        tp(Anim.ESTOCADA, new P(-55, 25, 0, -40, N, N, 22), new P(-92, -5, 0, -10, N, N, -22));
        tp(Anim.MAZAZO, new P(-170, -12, 0, -170, 12, 0, 0), new P(-40, -22, 0, -40, 22, 0, 0));
        tp(Anim.BARRIDO_DI, new P(-80, 50, 0, -80, 85, 0, 35), new P(-80, -70, 0, -80, -35, 0, -40));
        tp(Anim.BARRIDO_ID, new P(-80, -55, 0, -80, -20, 0, -35), new P(-80, 55, 0, -80, 90, 0, 40));
        tp(Anim.SALTO, new P(-178, 0, 0, -45, N, N, 0), new P(-30, 0, 0, -20, N, N, 0));
        tp(Anim.CARGA, new P(-165, 35, 15, -40, N, N, 25), new P(-165, 35, 15, -40, N, N, 25));
        // los punetazos son el golpe del propio juego (en primera y en tercera persona)
        tp(Anim.DAGA_D, new P(-75, 45, 0, -25, N, N, 15), new P(-85, -45, 0, -20, N, N, -18));
        tp(Anim.DAGA_I, new P(-80, -50, 0, -20, N, N, -15), new P(-80, 40, 0, -25, N, N, 15));
        tp(Anim.RODAR, new P(-50, 0, 25, -50, 0, -25, 0), new P(-50, 0, 25, -50, 0, -25, 0));
        tp(Anim.PASO_ATRAS, new P(-35, 0, 10, -35, 0, -10, 0), new P(-35, 0, 10, -35, 0, -10, 0));

        // primera persona: {x, y, z} sobre el sitio de siempre del arma (derecha, arriba, hacia la camara) y giros en
        // grados: X+ echa la hoja hacia atras (arriba), Y+ la lleva hacia la izquierda, Z+ la inclina a la izquierda
        fp(Anim.TAJO_DI, new float[]{0.12f, 0.18f, 0.05f, 15, -20, -45}, new float[]{-0.55f, -0.12f, -0.10f, -10, 35, 55});
        fp(Anim.TAJO_ID, new float[]{-0.45f, 0.12f, 0.00f, 10, 20, 45}, new float[]{0.20f, -0.15f, -0.08f, -10, -30, -60});
        fp(Anim.VERTICAL, new float[]{-0.12f, 0.35f, 0.15f, 55, 0, 8}, new float[]{-0.18f, -0.28f, -0.12f, -75, 0, 5});
        fp(Anim.ESTOCADA, new float[]{-0.05f, -0.02f, 0.22f, -70, 8, 0}, new float[]{-0.22f, 0.04f, -0.45f, -85, 5, 0});
        fp(Anim.MAZAZO, new float[]{-0.28f, 0.42f, 0.18f, 65, 0, 0}, new float[]{-0.30f, -0.32f, -0.18f, -80, 0, 0});
        fp(Anim.BARRIDO_DI, new float[]{0.15f, 0.08f, 0.08f, 5, -25, -70}, new float[]{-0.70f, -0.08f, -0.08f, -5, 45, 75});
        fp(Anim.BARRIDO_ID, new float[]{-0.62f, 0.05f, 0.04f, 5, 35, 70}, new float[]{0.18f, -0.10f, -0.08f, -5, -40, -75});
        fp(Anim.SALTO, new float[]{-0.15f, 0.45f, 0.22f, 75, 0, 0}, new float[]{-0.18f, -0.36f, -0.18f, -85, 0, 0});
        fp(Anim.CARGA, new float[]{0.08f, 0.32f, 0.22f, 50, -15, -30}, new float[]{0.08f, 0.32f, 0.22f, 50, -15, -30});
        fp(Anim.DAGA_D, new float[]{0.08f, 0.08f, 0.04f, 0, -20, -30}, new float[]{-0.42f, -0.06f, -0.10f, -10, 30, 40});
        fp(Anim.DAGA_I, new float[]{-0.38f, 0.05f, 0.02f, 0, 25, 35}, new float[]{0.14f, -0.06f, -0.10f, -10, -25, -35});
    }

    // ------------------------------------------------------------------ lo que se esta animando

    /** Una animacion en marcha: de que accion y arma, cuando empezo y, en el salto, cuando toco el suelo. */
    public static final class Anim_ {
        public int accion, paso, extra;
        public Armas.Tipo tipo;
        public Armas.Mov mov;
        public boolean izquierda;
        public int inicio;
        public int aterrizo = -1;
    }

    /** Las de los demas jugadores (por id de entidad). */
    private static final Map<Integer, Anim_> REMOTAS = new HashMap<>();

    public static void visto(Net.GolpeVisto m) {
        Anim_ a = new Anim_();
        a.accion = m.accion();
        a.paso = m.paso();
        a.izquierda = m.izquierda();
        a.extra = m.extra();
        Armas.Tipo[] tipos = Armas.Tipo.values();
        a.tipo = tipos[Mth.clamp(m.tipo(), 0, tipos.length - 1)];
        a.mov = a.accion == Armas.RODAR ? Armas.rodar(a.paso) : a.accion == Armas.PASO_ATRAS ? Armas.PASO_ATRAS_MOV : a.tipo.mov(a.accion, a.paso);
        a.inicio = ClientRol.ticks;
        REMOTAS.put(m.entity(), a);
    }

    static void tick(Minecraft mc) {
        if (REMOTAS.isEmpty() || mc.level == null) return;
        REMOTAS.entrySet().removeIf(en -> {
            Anim_ a = en.getValue();
            Entity e = mc.level.getEntity(en.getKey());
            if (e == null || !e.isAlive()) return true;
            int t = ClientRol.ticks - a.inicio;
            if (a.accion == Armas.SALTO) {
                if (a.aterrizo < 0 && t >= 2 && (e.onGround() || t >= 30)) a.aterrizo = ClientRol.ticks;
                return a.aterrizo >= 0 && ClientRol.ticks - a.aterrizo > 8;
            }
            if (a.accion == Armas.CARGA) return t > Armas.CARGA_MAX + 60;
            return t > a.mov.dura() + 1;
        });
    }

    static void olvidar() {
        REMOTAS.clear();
    }

    private static Anim_ de(Entity e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && e == mc.player) return ClientCombate.anim();
        return REMOTAS.get(e.getId());
    }

    // ------------------------------------------------------------------ la pose en un momento

    /** Suavizado: entra y sale despacio. */
    private static float suave(float k) {
        k = Mth.clamp(k, 0f, 1f);
        return k * k * (3f - 2f * k);
    }

    /** Rapido al principio (el tajo). */
    private static float latigo(float k) {
        k = Mth.clamp(k, 0f, 1f);
        return 1f - (1f - k) * (1f - k) * (1f - k);
    }

    /**
     * Donde va cada cosa en el instante t (ticks desde que empezo, con la fraccion): devuelve {pose, peso} con el peso de
     * la pose sobre la del cuerpo normal (0 nada, 1 entera).
     */
    private static Object[] momento(Anim_ a, float t, int fases) {
        Armas.Mov m = a.mov;
        P[] dos = TP.get(m.anim());
        if (dos == null) return null;
        P prep = dos[0], golpe = dos[1];
        if (a.tipo != null && a.tipo.dosManos && a.accion != Armas.RODAR && a.accion != Armas.PASO_ATRAS) {
            prep = prep.dosManos();
            golpe = golpe.dosManos();
        }
        if (a.izquierda && a.tipo != Armas.Tipo.PUNOS) {
            prep = prep.espejo();
            golpe = golpe.espejo();
        }
        switch (a.accion) {
            case Armas.CARGA -> {
                // se sube el arma y se queda temblando de la fuerza
                float w = suave(t / 5f);
                float tiembla = t > 5 ? Mth.sin(t * 2.7f) * Math.min(1f, (t - 5) / Armas.CARGA_MAX) * 2.5f : 0f;
                P c = new P(prep.rx + tiembla, prep.ry, prep.rz + tiembla * 0.5f, prep.lx, prep.ly, prep.lz, prep.giro);
                return new Object[]{c, w};
            }
            case Armas.RODAR, Armas.PASO_ATRAS -> {
                float w = Math.min(suave(t / 2f), suave((m.dura() - t) / 3f));
                return new Object[]{prep, w};
            }
            case Armas.SALTO -> {
                if (a.aterrizo < 0) return new Object[]{prep, suave(t / 4f)};
                float d = ClientRol.ticks - a.aterrizo + (t - (int) t);
                if (d < 2f) return new Object[]{P.mezcla(prep, golpe, latigo(d / 2f)), 1f};
                return new Object[]{golpe, 1f - suave((d - 3f) / 5f)};
            }
            default -> {
                float g0 = Math.max(1, m.golpe0()), g1 = m.golpe1() + 1, fin = m.dura();
                // el golpe fuerte sale de la carga, con el arma ya arriba
                if (t < g0) return new Object[]{prep, a.accion == Armas.PESADO ? 1f : suave(t / g0)};
                if (t < g1) return new Object[]{P.mezcla(prep, golpe, latigo((t - g0) / Math.max(1f, g1 - g0))), 1f};
                float resto = Math.max(1f, fin - g1);
                float k = (t - g1) / resto;
                return new Object[]{golpe, k < 0.3f ? 1f : 1f - suave((k - 0.3f) / 0.7f)};
            }
        }
    }

    // ------------------------------------------------------------------ tercera persona: los brazos y el cuerpo

    private static final float GRAD = Mth.DEG_TO_RAD;

    private static final HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("DARKCRAFT_COMBATE", true, (model, e, arm) -> {
        Anim_ a = de(e);
        if (a == null) return;
        float t = ClientRol.ticks - a.inicio + Minecraft.getInstance().getFrameTime();
        Object[] mo = momento(a, t, 0);
        if (mo == null) return;
        P p = (P) mo[0];
        float w = (Float) mo[1];
        // el giro del cuerpo, y los hombros con el (como hace el juego en su golpe)
        float giro = p.giro * GRAD * w;
        model.body.yRot = giro;
        model.rightArm.z = Mth.sin(giro) * 5f;
        model.rightArm.x = -Mth.cos(giro) * 5f;
        model.leftArm.z = -Mth.sin(giro) * 5f;
        model.leftArm.x = Mth.cos(giro) * 5f;
        ModelPart brazo = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        float x = arm == HumanoidArm.RIGHT ? p.rx : p.lx, y = arm == HumanoidArm.RIGHT ? p.ry : p.ly, z = arm == HumanoidArm.RIGHT ? p.rz : p.lz;
        if (!Float.isNaN(x)) brazo.xRot = Mth.lerp(w, brazo.xRot, x * GRAD);
        brazo.yRot = giro + (Float.isNaN(y) ? 0f : y * GRAD * w);
        if (!Float.isNaN(z)) brazo.zRot = Mth.lerp(w, brazo.zRot, z * GRAD);
    });

    /** Los que estan rodando ahora mismo (se les ha girado el cuerpo entero: hay que deshacerlo al acabar de dibujarlos). */
    private static final Set<Integer> GIRADOS = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void antes(RenderPlayerEvent.Pre e) {
        Anim_ a = de(e.getEntity());
        if (a == null || !TP.containsKey(a.mov.anim())) return;
        PlayerModel<AbstractClientPlayer> m = e.getRenderer().getModel();
        m.rightArmPose = POSE;
        m.leftArmPose = POSE;
    }

    /** La voltereta: el cuerpo entero da la vuelta (lo ultimo antes de dibujar; se deshace al acabar). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void girar(RenderPlayerEvent.Pre e) {
        AbstractClientPlayer p = (AbstractClientPlayer) e.getEntity();
        Anim_ a = de(p);
        if (a == null || (a.accion != Armas.RODAR && a.accion != Armas.PASO_ATRAS)) return;
        float t = ClientRol.ticks - a.inicio + e.getPartialTick();
        float k = Mth.clamp(t / a.mov.dura(), 0f, 1f);
        // rodar: una vuelta entera hacia donde va; paso atras: se echa un poco hacia atras y vuelve
        float ang = a.accion == Armas.RODAR ? 360f * suave(k) : -22f * Mth.sin(k * Mth.PI);
        float rumbo = a.extra * GRAD;
        float dx = Mth.cos(rumbo), dz = Mth.sin(rumbo);
        PoseStack ps = e.getPoseStack();
        ps.pushPose();
        float medio = p.getBbHeight() * 0.5f;
        ps.translate(0, medio, 0);
        ps.mulPose(new Quaternionf().rotateAxis(ang * GRAD, dz, 0f, -dx));
        ps.translate(0, -medio, 0);
        GIRADOS.add(p.getId());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void despues(RenderPlayerEvent.Post e) {
        if (GIRADOS.remove(e.getEntity().getId())) e.getPoseStack().popPose();
    }

    // ------------------------------------------------------------------ primera persona

    /**
     * Por donde va el arma en primera persona en este momento: {tx, ty, tz, rx, ry, rz} sobre su sitio de siempre en la
     * mano, o null si esa animacion se deja como la del juego (los punos y la voltereta).
     */
    static float[] fp(Anim_ a, float t) {
        float[][] dos = FP.get(a.mov.anim());
        if (dos == null) return null;
        Object[] mo = momento(a, t, 0);
        if (mo == null) return null;
        float w = (Float) mo[1];
        float[] prep = dos[0], golpe = dos[1];
        float[] r = new float[6];
        float k;
        switch (a.accion) {
            case Armas.CARGA -> k = 0f;
            case Armas.SALTO -> k = a.aterrizo < 0 ? 0f : latigo((ClientRol.ticks - a.aterrizo + (t - (int) t)) / 2f);
            default -> {
                float g0 = Math.max(1, a.mov.golpe0()), g1 = a.mov.golpe1() + 1;
                k = t < g0 ? 0f : t < g1 ? latigo((t - g0) / Math.max(1f, g1 - g0)) : 1f;
            }
        }
        for (int i = 0; i < 6; i++) r[i] = (prep[i] + (golpe[i] - prep[i]) * k) * w;
        if (a.accion == Armas.CARGA && t > 5) {
            float tiembla = Mth.sin(t * 2.7f) * Math.min(1f, (t - 5) / Armas.CARGA_MAX);
            r[0] += tiembla * 0.01f;
            r[1] += tiembla * 0.008f;
        }
        return r;
    }

    /** Lo que avanza el puno en primera persona (0..1, como el golpe del juego), o -1 si no toca. */
    static float puno(Anim_ a, float t) {
        if (a.tipo != Armas.Tipo.PUNOS || (a.accion != Armas.LIGERO && a.accion != Armas.PESADO && a.accion != Armas.CORRIENDO)) return -1f;
        float g0 = Math.max(1, a.mov.golpe0()) - 1f, fin = a.mov.golpe1() + 4f;
        return t < g0 ? 0f : Mth.clamp((t - g0) / (fin - g0), 0f, 1f);
    }

    /** Para la prueba de la CI: si el jugador (o la entidad) tiene una pose de combate y cual. */
    public static String resumen(LivingEntity e) {
        Anim_ a = de(e);
        return a == null ? "-" : a.mov.anim() + " t=" + (ClientRol.ticks - a.inicio) + (a.izquierda ? " izq" : "");
    }
}
