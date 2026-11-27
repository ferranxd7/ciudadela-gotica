package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Dragon;
import com.darkcraft.enemigos.DragonAnims;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lo que se nota de Vorthak en el cliente (parte 7z), sin paquetes: cada uno lo calcula con lo que ve.
 * <ul>
 * <li><b>El fuego</b>: el aliento (de su boca, en cono, barriendo de su derecha a su izquierda, y por el suelo donde da), el del
 * vuelo rasante (de la boca al suelo de delante) y el de escupir; antes del aliento, el humo y las brasas de la boca.</li>
 * <li><b>Los sonidos que duran</b>: el aleteo mientras vuela batiendo las alas (pegado a el) y, dormido, su respiracion.</li>
 * <li><b>Dormido</b>, el humo de los ollares; <b>despierto</b>, alguna brasa que se le escapa de las grietas.</li>
 * <li><b>Los golpes</b>: sus pasos, el pisoton, aterrizar, el picado, despegar, el rugido, caer aturdido y desplomarse al morir hacen
 * temblar la camara.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class DragonCliente {
    private DragonCliente() {}

    private static final Map<Integer, EntityBoundSoundInstance> ALETEOS = new HashMap<>(), RESPIRA = new HashMap<>();
    private static final Map<Integer, Integer> DESDE_ALETEO = new HashMap<>(), DESDE_RESPIRA = new HashMap<>();
    private static final Map<Integer, Integer> GOLPE_VISTO = new HashMap<>();
    private static final Map<Integer, Float> PASO = new HashMap<>();

    /** Si suena el aleteo de alguno (pruebas). */
    public static boolean aleteoSonando() {
        Minecraft mc = Minecraft.getInstance();
        for (EntityBoundSoundInstance s : ALETEOS.values()) if (mc.getSoundManager().isActive(s)) return true;
        return false;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ALETEOS.clear();
            RESPIRA.clear();
            DESDE_ALETEO.clear();
            DESDE_RESPIRA.clear();
            GOLPE_VISTO.clear();
            PASO.clear();
            return;
        }
        if (mc.isPaused()) return;
        Set<Integer> vistos = new HashSet<>();
        for (Entity en : mc.level.entitiesForRendering()) {
            if (!(en instanceof Dragon d) || en.distanceToSqr(mc.player) > 128 * 128) continue;
            vistos.add(d.getId());
            sonidos(mc, d);
            fuego(mc.level, d);
            brasas(mc.level, d);
            golpes(d);
        }
        // los que ya no estan: sus sonidos se callan
        callar(mc, ALETEOS, DESDE_ALETEO, vistos);
        callar(mc, RESPIRA, DESDE_RESPIRA, vistos);
        GOLPE_VISTO.keySet().retainAll(vistos);
        PASO.keySet().retainAll(vistos);
    }

    private static void callar(Minecraft mc, Map<Integer, EntityBoundSoundInstance> m, Map<Integer, Integer> desde, Set<Integer> vistos) {
        m.entrySet().removeIf(en -> {
            if (vistos.contains(en.getKey())) return false;
            mc.getSoundManager().stop(en.getValue());
            desde.remove(en.getKey());
            return true;
        });
    }

    /** El aleteo (volando y batiendo las alas) y la respiracion (dormido): suenan pegados a el y se vuelven a lanzar al acabar. */
    private static void sonidos(Minecraft mc, Dragon d) {
        boolean vivo = d.isAlive() && !d.muriendo();
        int a = d.accionVista();
        boolean aletea = vivo && d.vuela() && a != DragonAnims.PICADO && DragonModelo.planea(d, 0f) < 0.6f;
        bucle(mc, d, ALETEOS, DESDE_ALETEO, aletea, DarkCraftMod.SFX_DRAGON_ALETEO.get(), 2.6f, 76);
        boolean duerme = vivo && d.postura() != Enemigo.DE_PIE && a == 0;
        bucle(mc, d, RESPIRA, DESDE_RESPIRA, duerme, DarkCraftMod.SFX_DRAGON_RESPIRA.get(), 1.4f, 96);
    }

    private static void bucle(Minecraft mc, Dragon d, Map<Integer, EntityBoundSoundInstance> m, Map<Integer, Integer> desde, boolean suena,
                              SoundEvent s, float vol, int dura) {
        int id = d.getId();
        EntityBoundSoundInstance i = m.get(id);
        if (!suena) {
            if (i != null) {
                mc.getSoundManager().stop(i);
                m.remove(id);
                desde.remove(id);
            }
            return;
        }
        int t0 = desde.getOrDefault(id, -1000);
        if (i == null || d.tickCount - t0 >= dura || !mc.getSoundManager().isActive(i)) {
            EntityBoundSoundInstance n = new EntityBoundSoundInstance(s, SoundSource.HOSTILE, vol, 0.95f + d.getRandom().nextFloat() * 0.1f, d,
                    d.getRandom().nextLong());
            mc.getSoundManager().play(n);
            m.put(id, n);
            desde.put(id, d.tickCount);
        }
    }

    /** El fuego: el del aliento, el del rasante y el de escupir. */
    private static void fuego(ClientLevel l, Dragon d) {
        if (!d.isAlive() || d.muriendo()) return;
        int a = d.accionVista();
        if (a == 0 || d.accionDesde < 0) return;
        int t = d.tickCount - d.accionDesde;
        RandomSource r = d.getRandom();
        if (a == DragonAnims.ALIENTO) {
            if (t >= 4 && t < DragonAnims.ALIENTO_DESDE) {
                // coge aire: humo y brasas en la boca
                Vec3 b = Dragon.bocaAliento(d, Dragon.giroAliento(DragonAnims.ALIENTO_DESDE));
                l.addParticle(ParticleTypes.SMOKE, b.x, b.y, b.z, 0, 0.03, 0);
                if (t % 2 == 0) l.addParticle(ParticleTypes.SMALL_FLAME, b.x, b.y, b.z, (r.nextDouble() - 0.5) * 0.05, 0.02, (r.nextDouble() - 0.5) * 0.05);
            } else if (t >= DragonAnims.ALIENTO_DESDE && t <= DragonAnims.ALIENTO_HASTA) {
                float giro = Dragon.giroAliento(t);
                Vec3 b = Dragon.bocaAliento(d, giro);
                float yaw = (d.getYRot() + giro) * Mth.DEG_TO_RAD;
                chorro(l, r, b, -Mth.sin(yaw), Mth.cos(yaw), -0.45, 10, (float) Dragon.ALIENTO_LARGO);
                // donde da en el suelo: llamas que suben
                for (int k = 0; k < 3; k++) {
                    double s = 8 + r.nextDouble() * 9;
                    l.addParticle(ParticleTypes.FLAME, b.x - Mth.sin(yaw) * s + (r.nextDouble() - 0.5) * 2, d.getY() + 0.2,
                            b.z + Mth.cos(yaw) * s + (r.nextDouble() - 0.5) * 2, 0, 0.06 + r.nextDouble() * 0.06, 0);
                }
                if (t % 2 == 0) {
                    double s = 10 + r.nextDouble() * 6;
                    l.addParticle(ParticleTypes.LARGE_SMOKE, b.x - Mth.sin(yaw) * s, d.getY() + 1.0, b.z + Mth.cos(yaw) * s, 0, 0.05, 0);
                }
            }
        } else if (a == DragonAnims.RASANTE && t >= DragonAnims.RASANTE_DESDE - 2 && t <= DragonAnims.RASANTE_HASTA) {
            Vec3 b = Dragon.bocaVuelo(d);
            Vec3 g = Dragon.sueloRasante(d);
            if (g == null) return;
            Vec3 v = g.subtract(b);
            double largo = v.length();
            if (largo < 0.5) return;
            Vec3 u = v.scale(1 / largo);
            double h = Math.sqrt(u.x * u.x + u.z * u.z);
            chorro(l, r, b, u.x / Math.max(1e-3, h), u.z / Math.max(1e-3, h), u.y / Math.max(1e-3, h), 12, (float) largo);
            for (int k = 0; k < 5; k++)
                l.addParticle(ParticleTypes.FLAME, g.x + (r.nextDouble() - 0.5) * 3, g.y + 0.2, g.z + (r.nextDouble() - 0.5) * 3, 0,
                        0.05 + r.nextDouble() * 0.08, 0);
            l.addParticle(ParticleTypes.LARGE_SMOKE, g.x, g.y + 1, g.z, 0, 0.08, 0);
        } else if (a == DragonAnims.ESCUPE && t >= DragonAnims.GOLPES[a][0] - 4 && t <= DragonAnims.GOLPES[a][0] + 1) {
            Vec3 b = Dragon.bocaVuelo(d);
            for (int k = 0; k < 6; k++)
                l.addParticle(ParticleTypes.FLAME, b.x, b.y, b.z, (r.nextDouble() - 0.5) * 0.3, (r.nextDouble() - 0.5) * 0.3, (r.nextDouble() - 0.5) * 0.3);
        }
    }

    /**
     * Un chorro de fuego desde b hacia (hx, hz) en horizontal (unitario) con {@code dy} de bajada por bloque: {@code n} llamas por
     * tick, deprisa y abriendose, que llegan a unos {@code largo} bloques; humo y alguna gota de lava.
     */
    private static void chorro(ClientLevel l, RandomSource r, Vec3 b, double hx, double hz, double dy, int n, float largo) {
        double vel = largo / 14.0;
        for (int k = 0; k < n; k++) {
            double ab = 0.16;
            double sx = hx + (r.nextDouble() - 0.5) * ab * 2, sz = hz + (r.nextDouble() - 0.5) * ab * 2, sy = dy + (r.nextDouble() - 0.5) * ab;
            double s = vel * (0.7 + r.nextDouble() * 0.6);
            l.addParticle(ParticleTypes.FLAME, b.x, b.y, b.z, sx * s, sy * s, sz * s);
        }
        if (r.nextInt(3) == 0) l.addParticle(ParticleTypes.LARGE_SMOKE, b.x, b.y, b.z, hx * vel * 0.5, dy * vel * 0.5 + 0.03, hz * vel * 0.5);
        if (r.nextInt(5) == 0) l.addParticle(ParticleTypes.LAVA, b.x, b.y, b.z, 0, 0, 0);
    }

    /** Dormido, el humo de los ollares cada vez que echa el aire; despierto, alguna brasa de las grietas. */
    private static void brasas(ClientLevel l, Dragon d) {
        RandomSource r = d.getRandom();
        if (d.isAlive() && d.postura() != Enemigo.DE_PIE && d.accionVista() == 0) {
            if (d.tickCount % 120 > 70) return;
            if (d.tickCount % 6 != 0) return;
            // la cabeza, dormida a un lado (de la pose del modelo: tres bloques delante, cinco a su izquierda, a ras de suelo)
            float yaw = d.getYRot() * Mth.DEG_TO_RAD;
            double fx = -Mth.sin(yaw), fz = Mth.cos(yaw);
            double x = d.getX() + fx * 3.3 + fz * 5.3, z = d.getZ() + fz * 3.3 - fx * 5.3;
            l.addParticle(ParticleTypes.SMOKE, x, d.getY() + 0.6, z, fx * 0.04, 0.03, fz * 0.04);
            if (r.nextInt(4) == 0) l.addParticle(ParticleTypes.SMALL_FLAME, x, d.getY() + 0.5, z, 0, 0.01, 0);
            return;
        }
        if (!d.isAlive() && d.deathTime > 160) return;
        if (r.nextInt(d.isAlive() ? 3 : 2) != 0) return;
        double x = d.getX() + (r.nextDouble() - 0.5) * 5, y = d.getY() + 1.5 + r.nextDouble() * 2.5, z = d.getZ() + (r.nextDouble() - 0.5) * 5;
        l.addParticle(r.nextBoolean() ? ParticleTypes.SMALL_FLAME : ParticleTypes.ASH, x, y, z, 0, 0.02 + r.nextDouble() * 0.03, 0);
    }

    /** Lo que hace temblar la camara (cada golpe una vez: por el tick en que empezo su accion). */
    private static void golpes(Dragon d) {
        int a = d.accionVista();
        int t = d.accionDesde < 0 ? -1 : d.tickCount - d.accionDesde;
        int visto = GOLPE_VISTO.getOrDefault(d.getId(), -1);
        Vec3 p = d.position();
        if (d.muriendo()) {
            if (d.deathTime == 48) GiganteCliente.sacudir(p, 1.1f, 14, 30);
            return;
        }
        int golpe = switch (a) {
            case DragonAnims.PISOTON -> DragonAnims.GOLPES[a][0];
            case DragonAnims.ATERRIZA -> DragonAnims.GOLPES[a][0];
            case DragonAnims.DESPEGA -> DragonAnims.DESPEGA_SALTO;
            case DragonAnims.RUGIDO -> 17;
            case DragonAnims.ASCUAS -> DragonAnims.ASCUAS_RUGIDO;
            case DragonAnims.ATURDIDO -> 12;
            case DragonAnims.COLETAZO -> DragonAnims.GOLPES[a][0];
            default -> -1;
        };
        if (golpe >= 0 && t == golpe && visto != d.accionDesde) {
            GOLPE_VISTO.put(d.getId(), d.accionDesde);
            switch (a) {
                case DragonAnims.PISOTON -> GiganteCliente.sacudir(p, 1.4f, 10, 26);
                case DragonAnims.ATERRIZA -> GiganteCliente.sacudir(p, 1.6f, 12, 32);
                case DragonAnims.DESPEGA -> GiganteCliente.sacudir(p, 0.6f, 8, 20);
                case DragonAnims.RUGIDO, DragonAnims.ASCUAS -> GiganteCliente.sacudir(p, 0.5f, 24, 40);
                case DragonAnims.ATURDIDO -> GiganteCliente.sacudir(p, 1.0f, 10, 24);
                default -> GiganteCliente.sacudir(p, 0.5f, 6, 16);
            }
        }
        // sus pasos en tierra: un temblor pequeno cada paso
        if (!d.vuela() && a == 0 && d.velocidadC > 0.05f) {
            float antes = PASO.getOrDefault(d.getId(), d.fase);
            if (Math.floor(d.fase / 2.6f) != Math.floor(antes / 2.6f)) GiganteCliente.sacudir(p, 0.3f, 4, 18);
            PASO.put(d.getId(), d.fase);
        }
    }
}
