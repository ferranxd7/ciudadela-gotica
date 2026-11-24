package com.darkcraft.hechizos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Efectos;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Frascos;
import com.darkcraft.rol.Memoria;
import com.darkcraft.rol.Rol;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Lanzar hechizos, en el servidor: el cliente lo pide ({@link Net.Lanzar}) al pulsar atacar con el baston o el sello en
 * la mano; aqui se comprueba (memorizado, catalizador de su escuela, el minimo del atributo, mana y algo de estamina),
 * se cobra y, en el tick en que sale, se hace lo suyo: proyectiles, el tajo fantasma, la llamarada, curar o bendecir.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Lanzar {
    private Lanzar() {}

    /** Estamina que cuesta lanzar (como en Elden Ring: poca). */
    private static final float ESTAMINA = 6f;
    private static final DustParticleOptions SOMBRA = new DustParticleOptions(new Vector3f(0.42f, 0.5f, 1f), 1.2f),
            ORO = new DustParticleOptions(new Vector3f(1f, 0.86f, 0.45f), 1.1f);

    private record Lanzando(Hechizo h, int inicio, float dano) {}

    private static final Map<UUID, Lanzando> LANZANDO = new HashMap<>();

    public static boolean lanzando(ServerPlayer sp) {
        return LANZANDO.containsKey(sp.getUUID());
    }

    /** El cliente pide lanzar un hechizo. */
    public static void pedir(ServerPlayer sp, String id) {
        Hechizo h = Hechizo.de(id);
        if (h == null || !sp.isAlive() || sp.isSpectator() || Frascos.bebiendo(sp) || lanzando(sp)) {
            no(sp, null);
            return;
        }
        if (Catalizador.de(sp.getMainHandItem()) != h.escuela) {
            no(sp, "darkcraft.hechizo.sin_catalizador");
            return;
        }
        if (!Memoria.memorizados(sp).contains(h)) {
            no(sp, "darkcraft.hechizo.no_memorizado");
            return;
        }
        int at = Ficha.atributos(sp)[h.escuela.atributo.ordinal()];
        if (at < h.requisito) {
            no(sp, "darkcraft.hechizo.no_requisito");
            return;
        }
        boolean gasta = Rol.gasta(sp);
        if (gasta && Combos.energia(sp) < h.mana) {
            no(sp, "darkcraft.hechizo.sin_mana");
            return;
        }
        if (!Rol.gastarCombate(sp, ESTAMINA)) {
            no(sp, null);
            return;
        }
        if (gasta) Combos.dar(sp, -h.mana);
        LANZANDO.put(sp.getUUID(), new Lanzando(h, sp.server.getTickCount(), h.danoCon(at)));
        com.darkcraft.combate.Combate.vistoHechizo(sp, h);
        sonido(sp, DarkCraftMod.SFX_HECHIZO_LANZAR.get(), 0.7f);
    }

    private static void no(ServerPlayer sp, String aviso) {
        Net.to(sp, new Net.GolpeNo());
        if (aviso != null) Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, aviso, 0));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || LANZANDO.isEmpty()) return;
        int ahora = ev.getServer().getTickCount();
        for (Iterator<Map.Entry<UUID, Lanzando>> it = LANZANDO.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Lanzando> en = it.next();
            ServerPlayer sp = ev.getServer().getPlayerList().getPlayer(en.getKey());
            Lanzando l = en.getValue();
            int t = ahora - l.inicio;
            if (sp == null || !sp.isAlive()) {
                it.remove();
                continue;
            }
            if (t == l.h.sale) efecto(sp, l.h, l.dano);
            if (t >= l.h.dura) it.remove();
        }
    }

    // ------------------------------------------------------------------ lo que hace cada uno

    private static void efecto(ServerPlayer sp, Hechizo h, float dano) {
        ServerLevel sl = sp.serverLevel();
        Vec3 ojo = sp.getEyePosition(), mira = sp.getLookAngle();
        Vec3 sale = ojo.add(mira.scale(0.8)).add(0, -0.25, 0);
        switch (h) {
            case GUIJARRO -> disparar(sp, h, dano, sale, mira, 1.7f, 0f);
            case LANZA -> disparar(sp, h, dano, sale, mira, 2.1f, 0f);
            case ESQUIRLAS -> {
                for (int i = -2; i <= 2; i++) disparar(sp, h, dano, sale, mira, 1.1f, i * 9f);
            }
            case RAYO -> {
                disparar(sp, h, dano, sale, mira, 2.5f, 0f);
                sonido(sp, DarkCraftMod.SFX_HECHIZO_RAYO.get(), 0.8f);
            }
            case ESPADA -> arco(sp, dano, 3.8, 75, false);
            case LLAMA -> {
                arco(sp, dano, 4.2, 50, true);
                sonido(sp, DarkCraftMod.SFX_HECHIZO_LLAMA.get(), 1.0f);
            }
            case CURAR -> {
                for (ServerPlayer o : sl.getEntitiesOfClass(ServerPlayer.class, sp.getBoundingBox().inflate(6.0), x -> x.isAlive() && !x.isSpectator())) {
                    o.heal(6f + o.getMaxHealth() * 0.3f);
                    sl.sendParticles(ParticleTypes.HEART, o.getX(), o.getY() + 1.4, o.getZ(), 5, 0.4, 0.4, 0.4, 0.02);
                    sl.sendParticles(ORO, o.getX(), o.getY() + 1.0, o.getZ(), 20, 0.4, 0.7, 0.4, 0.02);
                }
                sonido(sp, DarkCraftMod.SFX_HECHIZO_CURAR.get(), 1.0f);
            }
            case BENDICION -> {
                sp.addEffect(new MobEffectInstance(Efectos.BENDICION.get(), 600, 0, false, false, true));
                sl.sendParticles(ORO, sp.getX(), sp.getY() + 1.0, sp.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2, sp.getZ(), 10, 0.3, 0.6, 0.3, 0.04);
                sonido(sp, DarkCraftMod.SFX_HECHIZO_BENDICION.get(), 1.0f);
            }
        }
    }

    private static void disparar(ServerPlayer sp, Hechizo h, float dano, Vec3 sale, Vec3 mira, float rapidez, float giro) {
        ProyectilHechizo p = new ProyectilHechizo(sp.level(), sp, h, dano);
        p.setPos(sale.x, sale.y, sale.z);
        Vec3 v = mira.yRot(giro * Mth.DEG_TO_RAD).normalize().scale(rapidez);
        p.setDeltaMovement(v);
        sp.level().addFreshEntity(p);
    }

    /** El tajo fantasma o la llamarada: a todo lo que haya delante, en un arco y a la vista. */
    private static void arco(ServerPlayer sp, float dano, double alcance, double grados, boolean fuego) {
        ServerLevel sl = sp.serverLevel();
        Vec3 pecho = sp.position().add(0, sp.getBbHeight() * 0.62, 0);
        Vec3 m = sp.getLookAngle();
        Vec3 h = new Vec3(m.x, 0, m.z).normalize();
        DamageSource fuente = fuego
                ? new DamageSource(sl.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.IN_FIRE), sp, sp)
                : sp.damageSources().indirectMagic(sp, sp);
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().inflate(alcance + 1, 2, alcance + 1),
                x -> Combos.valido(sp, x))) {
            Vec3 d = le.position().add(0, le.getBbHeight() * 0.5, 0).subtract(pecho);
            double dist = Math.sqrt(d.x * d.x + d.z * d.z) - le.getBbWidth() * 0.5;
            if (dist > alcance) continue;
            double ang = Math.toDegrees(Math.acos(Mth.clamp((d.x * h.x + d.z * h.z) / Math.max(1e-4, Math.sqrt(d.x * d.x + d.z * d.z)), -1, 1)));
            if (ang > grados && dist > 0.8) continue;
            if (!sp.hasLineOfSight(le)) continue;
            le.invulnerableTime = 0;
            if (le.hurt(fuente, dano)) {
                if (fuego && !le.fireImmune()) le.setSecondsOnFire(4);
                le.knockback(0.35, -h.x, -h.z);
            }
        }
        // lo que se ve: el arco de chispas o de fuego delante
        for (int i = 0; i <= 14; i++) {
            double a = Math.toRadians(-grados + 2 * grados * i / 14.0);
            Vec3 dir = h.yRot((float) a);
            for (double r = 1.2; r <= alcance; r += fuego ? 0.7 : 1.3) {
                Vec3 p = pecho.add(dir.scale(r));
                if (fuego) sl.sendParticles(ParticleTypes.FLAME, p.x, p.y - 0.2, p.z, 1, 0.1, 0.15, 0.1, 0.02);
                else sl.sendParticles(SOMBRA, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
        if (!fuego) {
            Vec3 c = pecho.add(h.scale(2));
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 2, 0.6, 0.1, 0.6, 0);
            sonido(sp, DarkCraftMod.SFX_GOLPE_PESADO.get(), 0.8f);
        }
    }

    private static void sonido(ServerPlayer sp, SoundEvent s, float vol) {
        sp.level().playSound(null, sp.getX(), sp.getY() + 1, sp.getZ(), s, SoundSource.PLAYERS, vol, 0.95f + sp.getRandom().nextFloat() * 0.1f);
    }

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        LANZANDO.remove(e.getEntity().getUUID());
    }

    /** Para la prueba de la CI. */
    public static String resumen(ServerPlayer sp) {
        Lanzando l = LANZANDO.get(sp.getUUID());
        return "memoria=" + Memoria.memorizados(sp) + " (" + Memoria.usados(Memoria.memorizados(sp)) + "/" + Memoria.huecos(sp) + ")"
                + (l == null ? "" : " lanzando=" + l.h + " t=" + (sp.server.getTickCount() - l.inicio)) + " mana=" + Combos.energia(sp);
    }
}
