package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Gigante;
import com.darkcraft.enemigos.GiganteAnims;
import com.darkcraft.enemigos.RocaGigante;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lo que se nota de un gigante en el cliente (parte 7w), sin paquetes: cada uno lo calcula con lo que ve.
 * <ul>
 * <li><b>Los pasos</b>: cada vez que planta un pie (dos por ciclo de su animacion de andar, que va al ritmo de lo que avanza:
 * cada {@link #PASO} puntos del paso del juego) suena el paso, sale el polvo del suelo bajo el pie y la camara tiembla un poco
 * (menos cuanto mas lejos).</li>
 * <li><b>Los golpes contra el suelo</b> (el mazazo, el pisoton, el rugido, la rodilla al caer aturdido, las rodillas y el cuerpo
 * al morir) y <b>la roca</b> al reventar: la camara tiembla fuerte si se esta cerca.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class GiganteCliente {
    private GiganteCliente() {}

    /** Los puntos del paso del juego entre pie y pie: medio ciclo de la animacion (3,2 s) a su escala. */
    public static final float PASO = 3200f / 50f / GiganteModelo.ESCALA_PASO / 2f;

    private static float temblor, temblorMax;
    private static int temblorT, temblorDura;
    private static long tick;

    static {
        RocaGigante.AL_CAER = p -> sacudir(p, 1.5f, 10, 26);
    }

    /** Lo que tiembla la camara ahora (pruebas). */
    public static float temblor() {
        return temblorT > 0 ? temblor * temblorT / (float) Math.max(1, temblorDura) : 0f;
    }

    /** Un golpe en p: tiembla con {@code fuerza} a quien este cerca, menos hasta {@code alcance} bloques. */
    public static void sacudir(Vec3 p, float fuerza, int ticks, double alcance) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double d = mc.player.position().distanceTo(p);
        float f = fuerza * (float) Mth.clamp(1.0 - d / alcance, 0.0, 1.0);
        if (f <= 0.02f) return;
        if (temblorT <= 0 || f >= temblor() || ticks > temblorT) {
            temblor = Math.max(f, temblor());
            temblorT = temblorDura = ticks;
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        tick++;
        if (temblorT > 0) temblorT--;
        for (Entity en : mc.level.entitiesForRendering()) {
            if (!(en instanceof Gigante g) || en.distanceToSqr(mc.player) > 96 * 96) continue;
            pasos(mc, g);
            golpes(g);
        }
    }

    /** El pie que planta (si ha llegado al siguiente punto del ciclo): el paso, el polvo y el temblor. */
    private static void pasos(Minecraft mc, Gigante g) {
        float pos = g.walkAnimation.position();
        int n = (int) Math.floor(pos / PASO);
        if (g.pasoVisto == Integer.MIN_VALUE) {
            g.pasoVisto = n;
            return;
        }
        if (n == g.pasoVisto) return;
        g.pasoVisto = n;
        if (g.walkAnimation.speed() < 0.08f || g.accionVista() != 0 || !g.isAlive()) return;
        Vec3 f = Vec3.directionFromRotation(0, g.yBodyRot);
        double lado = (n & 1) == 0 ? 0.7 : -0.7;
        // su derecha es (-f.z, f.x)
        double x = g.getX() + f.x * 1.2 - f.z * lado, z = g.getZ() + f.z * 1.2 + f.x * lado;
        mc.level.playLocalSound(x, g.getY(), z, DarkCraftMod.SFX_GIGANTE_PASO.get(), SoundSource.HOSTILE, 2.6f,
                0.85f + g.getRandom().nextFloat() * 0.15f, false);
        BlockState b = mc.level.getBlockState(BlockPos.containing(x, g.getY() - 0.5, z));
        if (!b.isAir())
            for (int k = 0; k < 10; k++)
                mc.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, b), x + (g.getRandom().nextDouble() - 0.5) * 2.0, g.getY() + 0.1,
                        z + (g.getRandom().nextDouble() - 0.5) * 2.0, 0, 0.1, 0);
        sacudir(new Vec3(x, g.getY(), z), 0.45f, 6, 22);
    }

    /** Los golpes contra el suelo de cada accion (y al morir): el temblor, en el tick en que llegan. */
    private static void golpes(Gigante g) {
        int a = g.accionVista();
        if (a == 0 || g.accionDesde < 0) {
            g.golpeVisto = -1;
            return;
        }
        int t = g.tickCount - g.accionDesde;
        int antes = g.golpeVisto;
        g.golpeVisto = t;
        if (antes < 0 || antes >= t) return;
        Vec3 p = g.position();
        switch (a) {
            case GiganteAnims.MAZAZO -> {
                if (cruza(antes, t, GiganteAnims.GOLPES[a][0])) sacudir(p, 2.4f, 14, 34);
            }
            case GiganteAnims.PISOTON -> {
                if (cruza(antes, t, GiganteAnims.GOLPES[a][0])) sacudir(p, 2.0f, 12, 28);
            }
            case GiganteAnims.RUGIDO -> {
                if (cruza(antes, t, GiganteAnims.GOLPES[a][0])) sacudir(p, 1.2f, 30, 30);
            }
            case GiganteAnims.ATURDIDO -> {
                if (cruza(antes, t, 18)) sacudir(p, 1.4f, 10, 26);
            }
            case GiganteAnims.MORIR -> {
                if (cruza(antes, t, 30)) sacudir(p, 1.4f, 10, 28);
                if (cruza(antes, t, 146)) sacudir(p, 2.8f, 18, 40);
            }
            default -> {
            }
        }
    }

    private static boolean cruza(int antes, int ahora, int k) {
        return antes < k && ahora >= k;
    }

    @SubscribeEvent
    public static void camara(ViewportEvent.ComputeCameraAngles e) {
        if (temblorT <= 0) return;
        float pt = (float) e.getPartialTick();
        float k = Mth.clamp((temblorT - pt) / Math.max(1f, temblorDura), 0f, 1f);
        float s = temblor * k;
        float t = tick + pt;
        e.setRoll(e.getRoll() + Mth.sin(t * 8.3f) * s * 0.8f);
        e.setPitch(e.getPitch() + Mth.sin(t * 11.7f + 1.3f) * s);
        e.setYaw(e.getYaw() + Mth.sin(t * 6.1f + 2.1f) * s * 0.4f);
    }
}
