package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Jinete;
import com.darkcraft.enemigos.JineteAnims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Lo que se nota del Jinete Nocturno en el cliente (parte 7x), sin paquetes: cada uno lo calcula con lo que ve.
 * <ul>
 * <li><b>El galope</b>: mientras corre suena su galope, pegado a el (le sigue: {@link EntityBoundSoundInstance}), y se corta
 * cuando para.</li>
 * <li><b>El fuego de los cascos</b>: llamitas de almas donde pisa (mas al galope) y un rastro de almas en la carga.</li>
 * <li><b>Los golpes</b>: el pisoton, la carga que pasa cerca y el caballo que cae hacen temblar la camara.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class JineteCliente {
    private JineteCliente() {}

    /** El galope que suena de cada jinete (por su id) y cuando empezo. */
    private static final Map<Integer, EntityBoundSoundInstance> GALOPES = new HashMap<>();
    private static final Map<Integer, Integer> DESDE = new HashMap<>();
    private static final Map<Integer, Integer> GOLPE_VISTO = new HashMap<>();

    /** Si suena el galope de alguno (pruebas). */
    public static boolean galopeSonando() {
        Minecraft mc = Minecraft.getInstance();
        for (EntityBoundSoundInstance s : GALOPES.values()) if (mc.getSoundManager().isActive(s)) return true;
        return false;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            GALOPES.clear();
            DESDE.clear();
            GOLPE_VISTO.clear();
            return;
        }
        if (mc.isPaused()) return;
        java.util.Set<Integer> vistos = new java.util.HashSet<>();
        for (Entity en : mc.level.entitiesForRendering()) {
            if (!(en instanceof Jinete j) || en.distanceToSqr(mc.player) > 96 * 96) continue;
            vistos.add(j.getId());
            galope(mc, j);
            fuego(mc, j);
            golpes(j);
        }
        // los que ya no estan: su galope se calla
        GALOPES.entrySet().removeIf(en -> {
            if (vistos.contains(en.getKey())) return false;
            mc.getSoundManager().stop(en.getValue());
            DESDE.remove(en.getKey());
            return true;
        });
        GOLPE_VISTO.keySet().retainAll(vistos);
    }

    /** El galope: suena (y se vuelve a lanzar antes de acabar) mientras corre; si para o se apaga, se corta. */
    private static void galope(Minecraft mc, Jinete j) {
        int id = j.getId();
        boolean corre = j.velocidad > 0.2f && j.isAlive() && !j.muriendo() && JineteRenderer.alfa(j, 0f) > 0.3f;
        EntityBoundSoundInstance s = GALOPES.get(id);
        if (!corre) {
            if (s != null) {
                mc.getSoundManager().stop(s);
                GALOPES.remove(id);
                DESDE.remove(id);
            }
            return;
        }
        int desde = DESDE.getOrDefault(id, -1000);
        if (s == null || j.tickCount - desde >= 72 || !mc.getSoundManager().isActive(s)) {
            float vol = 1.2f + Mth.clamp(j.velocidad, 0f, 1f) * 1.0f;
            EntityBoundSoundInstance n = new EntityBoundSoundInstance(DarkCraftMod.SFX_JINETE_GALOPE.get(), SoundSource.HOSTILE, vol,
                    0.92f + Mth.clamp(j.velocidad, 0f, 1f) * 0.16f, j, j.getRandom().nextLong());
            mc.getSoundManager().play(n);
            GALOPES.put(id, n);
            DESDE.put(id, j.tickCount);
        }
    }

    /** Las llamitas de almas de los cascos: donde pisa, mas al galope; quieto, alguna suelta; en la carga, el rastro de almas. */
    private static void fuego(Minecraft mc, Jinete j) {
        if (!j.isAlive() || j.muriendo() || JineteRenderer.alfa(j, 0f) < 0.3f) return;
        float v = j.velocidad;
        int cada = v > 0.4f ? 1 : v > 0.15f ? 2 : 9;
        if (j.tickCount % cada != 0) return;
        float giro = j.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(giro), fz = Mth.cos(giro);
        double lx = fz, lz = -fx;
        double esc = JineteRenderer.ESCALA;
        // los cuatro cascos: delante (1,05) y detras (-0,85), a cada lado (0,26)
        for (int k = 0; k < 4; k++) {
            if (v < 0.15f && j.getRandom().nextFloat() > 0.3f) continue;
            double d = (k < 2 ? 1.05 : -0.85) * esc, l = (k % 2 == 0 ? 0.26 : -0.26) * esc;
            double x = j.getX() + fx * d + lx * l, z = j.getZ() + fz * d + lz * l;
            mc.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, j.getY() + 0.06, z, (j.getRandom().nextDouble() - 0.5) * 0.02, 0.02,
                    (j.getRandom().nextDouble() - 0.5) * 0.02);
        }
        if (j.accionVista() == JineteAnims.CARGA && v > 0.5f)
            mc.level.addParticle(ParticleTypes.SOUL, j.getX() - fx * 1.2, j.getY() + 1.6, j.getZ() - fz * 1.2, -fx * 0.05, 0.02, -fz * 0.05);
    }

    /** Lo que hace temblar la camara: el pisoton al caer, la carga que pasa cerca y el caballo que cae al morir. */
    private static void golpes(Jinete j) {
        int a = j.accionVista();
        int t = j.accionDesde < 0 ? -1 : j.tickCount - j.accionDesde;
        int visto = GOLPE_VISTO.getOrDefault(j.getId(), -1);
        Vec3 p = j.position();
        if (a == JineteAnims.PISOTEA && t == JineteAnims.GOLPES[a][0] && visto != j.accionDesde) {
            GOLPE_VISTO.put(j.getId(), j.accionDesde);
            GiganteCliente.sacudir(p, 1.0f, 8, 14);
        } else if (a == JineteAnims.CARGA && t >= Jinete.CARGA_DESDE && t <= Jinete.CARGA_HASTA && t % 4 == 0) {
            GiganteCliente.sacudir(p, 0.35f, 4, 7);
        } else if (j.muriendo() && j.deathTime == 52) {
            GiganteCliente.sacudir(p, 0.8f, 10, 16);
        }
    }
}
