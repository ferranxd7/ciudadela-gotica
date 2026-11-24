package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rey.ReyProyectil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * Los combos de las espadas del Rey y su energia:
 * - Tajo Real (Shift + clic izquierdo): tres tajos de fuego y un giro que suelta una ola de fuego.
 * - Juicio del Rey (Ctrl + clic izquierdo): una lluvia de ocho tajos de fuego y una explosion de lava.
 * Los golpes normales llenan la energia. Con las dos espadas (una en cada mano) los combos pegan mas.
 * Cubrirse (clic derecho) para casi todo lo que llega de frente.
 */
public final class Combos {
    private Combos() {}

    public static final int TAJO_REAL = 1, JUICIO = 2;
    /** Los del Mandoble del Juramento (el cliente pide 1 o 2 y el servidor mira que arma lleva). */
    public static final int RAICES_M = 3, ESPINAS_M = 4;
    public static final int MAX = 100, COSTE_1 = 35, COSTE_2 = 80;
    private static final String KEY = "darkcraft_energia";

    private static final class Estado {
        int tipo, t;
        boolean doble;
        final Set<Integer> tocados = new HashSet<>();
        Vec3 dir;
    }

    private static final Map<UUID, Estado> ACTIVOS = new HashMap<>();

    // ------------------------------------------------------------------ energia

    public static int energia(Player p) {
        return p.getPersistentData().getInt(KEY);
    }

    private static void setEnergia(Player p, int v) {
        v = Mth.clamp(v, 0, MAX);
        p.getPersistentData().putInt(KEY, v);
        if (p instanceof ServerPlayer sp) Net.to(sp, new Net.Energia(v));
    }

    public static void sumar(Player p, int n) {
        if (ACTIVOS.containsKey(p.getUUID())) return;
        setEnergia(p, energia(p) + n);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Net.to(sp, new Net.Energia(energia(sp)));
    }

    // ------------------------------------------------------------------ empezar un combo

    /** El cliente pide un combo (Shift o Ctrl + clic). */
    public static void pedir(ServerPlayer sp, int tipo) {
        if (ACTIVOS.containsKey(sp.getUUID()) || !sp.isAlive() || sp.isSpectator()) return;
        ItemStack main = sp.getMainHandItem();
        if (MandobleJuramento.es(main)) {
            if (!MandobleJuramento.dosManos(sp)) {
                sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("darkcraft.mandoble.dos_manos"), true);
                return;
            }
            tipo = tipo == JUICIO ? ESPINAS_M : RAICES_M;
        } else if (!EspadaRey.es(main)) return;
        int coste = tipo == JUICIO || tipo == ESPINAS_M ? COSTE_2 : COSTE_1;
        if (energia(sp) < coste && !sp.isCreative()) return;
        if (!sp.isCreative()) setEnergia(sp, energia(sp) - coste);
        Estado e = new Estado();
        e.tipo = tipo;
        e.doble = EspadaRey.es(sp.getOffhandItem());
        e.dir = mirada(sp);
        ACTIVOS.put(sp.getUUID(), e);
        formas(sp, tipo == JUICIO ? 2 : 1);
    }

    private static Vec3 mirada(Player p) {
        Vec3 l = p.getLookAngle();
        Vec3 h = new Vec3(l.x, 0, l.z);
        return h.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : h.normalize();
    }

    private static void formas(ServerPlayer sp, int f) {
        if (EspadaRey.es(sp.getMainHandItem())) EspadaRey.setForma(sp.getMainHandItem(), f);
        if (EspadaRey.es(sp.getOffhandItem())) EspadaRey.setForma(sp.getOffhandItem(), f);
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ACTIVOS.isEmpty()) return;
        Iterator<Map.Entry<UUID, Estado>> it = ACTIVOS.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            ServerPlayer sp = ev.getServer().getPlayerList().getPlayer(en.getKey());
            Estado e = en.getValue();
            if (sp == null || !sp.isAlive()) {
                it.remove();
                continue;
            }
            boolean fin = switch (e.tipo) {
                case TAJO_REAL -> tajoReal(sp, e);
                case RAICES_M -> raicesMandoble(sp, e);
                case ESPINAS_M -> espinasMandoble(sp, e);
                default -> juicio(sp, e);
            };
            e.t++;
            if (fin) {
                formas(sp, 0);
                it.remove();
            }
        }
    }

    private static float mul(Estado e) {
        return e.doble ? 1.25f : 1f;
    }

    private static boolean tajoReal(ServerPlayer sp, Estado e) {
        ServerLevel sl = sp.serverLevel();
        int t = e.t;
        Vec3 f = mirada(sp);
        if (t == 0) {
            sp.setDeltaMovement(sp.getDeltaMovement().add(f.scale(0.45)).add(0, 0.05, 0));
            sp.hurtMarked = true;
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.3f);
        }
        if (t == 2 || t == 8 || t == 14) {
            e.tocados.clear();
            mano(sp, e, t / 6);
            arco(sl, sp, f, 4.3, 130, 12 * mul(e), 3, e);
        }
        if (t == 20) {
            e.tocados.clear();
            sp.swing(InteractionHand.MAIN_HAND, true);
            if (e.doble) sp.swing(InteractionHand.OFF_HAND, true);
            for (LivingEntity le : objetivos(sl, sp, 4.6)) dano(sp, le, 16 * mul(e), 4, 1.0);
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI * 2 / 24;
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, sp.getX() + Math.cos(a) * 2.5, sp.getY() + 1, sp.getZ() + Math.sin(a) * 2.5, 1, 0, 0, 0, 0);
            }
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.7f);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
            e.tocados.clear();
        }
        if (t > 20 && t <= 30) {
            // ola de fuego que sale en circulo
            double r = 1.0 + (t - 20) * 0.65;
            int n = (int) (r * 8);
            for (int i = 0; i < n; i++) {
                double a = i * Math.PI * 2 / n;
                sl.sendParticles(ParticleTypes.FLAME, sp.getX() + Math.cos(a) * r, sp.getY() + 0.2, sp.getZ() + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
            }
            for (LivingEntity le : objetivos(sl, sp, r + 0.6)) {
                if (le.distanceTo(sp) < r - 0.8 || !e.tocados.add(le.getId())) continue;
                dano(sp, le, 8 * mul(e), 5, 0.6);
            }
        }
        return t >= 30;
    }

    private static boolean juicio(ServerPlayer sp, Estado e) {
        ServerLevel sl = sp.serverLevel();
        int t = e.t;
        if (t == 0) {
            sp.setDeltaMovement(sp.getDeltaMovement().add(0, 0.55, 0));
            sp.hurtMarked = true;
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.7f, 1.3f);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.6f);
        }
        float[] angs = {-30, -15, 0, 15, 30, -22, 22, 0};
        if (t >= 6 && t <= 34 && (t - 6) % 4 == 0) {
            int i = (t - 6) / 4;
            mano(sp, e, i);
            Vec3 f = mirada(sp);
            double yaw = Math.atan2(f.z, f.x) + Math.toRadians(angs[i]);
            ReyProyectil pr = new ReyProyectil(DarkCraftMod.REY_PROYECTIL.get(), sl);
            pr.setOwner(sp);
            pr.setPos(sp.getX() + Math.cos(yaw) * 0.8, sp.getEyeY() - 0.4, sp.getZ() + Math.sin(yaw) * 0.8);
            Vec3 v = new Vec3(Math.cos(yaw), -0.02, Math.sin(yaw)).scale(1.1);
            pr.preparar(ReyProyectil.TAJO, v, 10 * mul(e), 0);
            sl.addFreshEntity(pr);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.1f);
        }
        if (t == 44) {
            // explosion de lava delante
            Vec3 f = mirada(sp);
            Vec3 c = sp.position().add(f.scale(4.5));
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.LAVA, c.x, c.y + 0.5, c.z, 50, 2, 0.6, 2, 0.1);
            sl.sendParticles(ParticleTypes.FLAME, c.x, c.y + 0.5, c.z, 60, 2.5, 0.6, 2.5, 0.08);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 1, c.z, 30, 2, 1, 2, 0.03);
            sl.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.6f, 0.7f);
            sl.playSound(null, c.x, c.y, c.z, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.PLAYERS, 1.5f, 0.6f);
            AABB box = new AABB(c.x - 5, c.y - 2, c.z - 5, c.x + 5, c.y + 4, c.z + 5);
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, box, x -> valido(sp, x))) {
                if (le.distanceToSqr(c) > 25) continue;
                dano(sp, le, 24 * mul(e), 6, 1.4);
            }
        }
        return t >= 52;
    }

    // ------------------------------------------------------------------ el Mandoble del Juramento

    /** Raices del Juramento: clava el mandoble y una linea de raices sale del suelo hacia delante. */
    private static boolean raicesMandoble(ServerPlayer sp, Estado e) {
        ServerLevel sl = sp.serverLevel();
        int t = e.t;
        if (t == 0) {
            sp.swing(InteractionHand.MAIN_HAND, true);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_GAVRAN_RAICES.get(), SoundSource.PLAYERS, 1.2f, 1.1f);
            sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.ROOTED_DIRT.defaultBlockState()),
                    sp.getX() + e.dir.x * 1.5, sp.getY() + 0.1, sp.getZ() + e.dir.z * 1.5, 20, 0.4, 0.1, 0.4, 0.15);
        }
        if (t >= 2 && t <= 22 && t % 2 == 0) {
            int k = (t - 2) / 2;
            Vec3 c = sp.position().add(e.dir.scale(2.4 + k * 1.5));
            double y = suelo(sl, c, sp.getY());
            net.minecraft.util.RandomSource r = sl.random;
            for (int i = 0; i < 3; i++) {
                double a = r.nextDouble() * Math.PI * 2, d = i == 0 ? 0 : 0.3 + r.nextDouble() * 0.6;
                EfectosRaiz.estaca(sl, c.x + Math.cos(a) * d, y, c.z + Math.sin(a) * d, EfectosRaiz.ang(r), 0.15f + r.nextFloat() * 0.35f,
                        i == 0 ? 2.4f : 1.5f, i == 0 ? 0.4f : 0.28f, 0, 16);
            }
            sl.sendParticles(com.darkcraft.gavran.Raices.VERDE, c.x, y + 0.8, c.z, 6, 0.4, 0.5, 0.4, 0);
            sl.sendParticles(ParticleTypes.GLOW, c.x, y + 0.6, c.z, 3, 0.4, 0.4, 0.4, 0.02);
            if (k % 3 == 0) sl.playSound(null, c.x, y, c.z, DarkCraftMod.SFX_GAVRAN_RAICES.get(), SoundSource.PLAYERS, 0.6f, 1.2f);
            AABB box = new AABB(c.x - 1.8, y - 1, c.z - 1.8, c.x + 1.8, y + 3, c.z + 1.8);
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, box, x -> valido(sp, x))) {
                if (!e.tocados.add(le.getId())) continue;
                dano(sp, le, 14, 0, 0.1);
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.45, 0));
                le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 50, 3));
            }
        }
        return t >= 26;
    }

    /** Juicio de Espinas: salta, clava el mandoble al caer y salen espinas y raices alrededor. */
    private static boolean espinasMandoble(ServerPlayer sp, Estado e) {
        ServerLevel sl = sp.serverLevel();
        int t = e.t;
        if (t == 0) {
            sp.setDeltaMovement(sp.getDeltaMovement().add(0, 0.62, 0));
            sp.hurtMarked = true;
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_GAVRAN_SALTO.get(), SoundSource.PLAYERS, 0.9f, 1.25f);
        }
        if (t == 9) {
            sp.setDeltaMovement(sp.getDeltaMovement().x * 0.3, -1.3, sp.getDeltaMovement().z * 0.3);
            sp.hurtMarked = true;
            sp.fallDistance = 0;
        }
        if (t >= 10 && e.dir != null && (sp.onGround() || t >= 22)) {
            sp.fallDistance = 0;
            sp.swing(InteractionHand.MAIN_HAND, true);
            double y = sp.getY();
            sl.playSound(null, sp.getX(), y, sp.getZ(), DarkCraftMod.SFX_ESPINAS.get(), SoundSource.PLAYERS, 1.6f, 1.05f);
            sl.playSound(null, sp.getX(), y, sp.getZ(), DarkCraftMod.SFX_GAVRAN_CLAVAR.get(), SoundSource.PLAYERS, 1.3f, 1.15f);
            net.minecraft.util.RandomSource r = sl.random;
            for (int i = 0; i < 26; i++) {
                double a = i * Math.PI * 2 / 26 + r.nextDouble() * 0.1;
                EfectosRaiz.espina(sl, sp.getX(), y + 0.5 + r.nextDouble() * 0.6, sp.getZ(), EfectosRaiz.yaw(Math.cos(a), Math.sin(a)),
                        5.5f + r.nextFloat() * 2f, 14);
            }
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI * 2 / 10 + r.nextDouble() * 0.3, d = 2.2 + r.nextDouble() * 2.5;
                double x = sp.getX() + Math.cos(a) * d, z = sp.getZ() + Math.sin(a) * d;
                EfectosRaiz.estaca(sl, x, suelo(sl, new Vec3(x, y, z), y), z, EfectosRaiz.ang(r), 0.2f + r.nextFloat() * 0.4f,
                        1.6f + r.nextFloat(), 0.32f, i % 3, 18);
            }
            sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.ROOTED_DIRT.defaultBlockState()),
                    sp.getX(), y + 0.1, sp.getZ(), 50, 2.5, 0.1, 2.5, 0.2);
            sl.sendParticles(ParticleTypes.GLOW, sp.getX(), y + 0.8, sp.getZ(), 30, 3, 0.6, 3, 0.05);
            for (LivingEntity le : objetivos(sl, sp, 7.0)) dano(sp, le, 22, 0, 1.3);
            e.dir = null;
            return false;
        }
        return t >= 34 || (e.dir == null && t >= 24);
    }

    /** Altura del suelo bajo c (busca hacia abajo desde un poco por encima de y0). */
    private static double suelo(ServerLevel sl, Vec3 c, double y0) {
        net.minecraft.core.BlockPos.MutableBlockPos m = new net.minecraft.core.BlockPos.MutableBlockPos();
        int x = Mth.floor(c.x), z = Mth.floor(c.z);
        for (int y = Mth.floor(y0) + 2; y >= Mth.floor(y0) - 4; y--) {
            m.set(x, y, z);
            if (sl.getBlockState(m).isFaceSturdy(sl, m, net.minecraft.core.Direction.UP) && sl.getBlockState(m.above()).getCollisionShape(sl, m.above()).isEmpty())
                return y + 1;
        }
        return Math.floor(y0);
    }

    private static void mano(ServerPlayer sp, Estado e, int i) {
        sp.swing(e.doble && i % 2 == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, true);
    }

    private static void arco(ServerLevel sl, ServerPlayer sp, Vec3 f, double reach, double arco, float dmg, int fuego, Estado e) {
        for (LivingEntity le : objetivos(sl, sp, reach)) {
            Vec3 d = le.position().subtract(sp.position());
            double dist = Math.sqrt(d.x * d.x + d.z * d.z);
            double ang = Math.toDegrees(Math.acos(Mth.clamp((d.x * f.x + d.z * f.z) / Math.max(1e-4, dist), -1, 1)));
            if (dist > 1.2 && ang > arco / 2) continue;
            if (!e.tocados.add(le.getId())) continue;
            dano(sp, le, dmg, fuego, 0.5);
        }
        double yaw = Math.atan2(f.z, f.x);
        for (int i = 0; i < 7; i++) {
            double a = yaw + Math.toRadians((i - 3) * arco / 6);
            double x = sp.getX() + Math.cos(a) * reach * 0.7, z = sp.getZ() + Math.sin(a) * reach * 0.7;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, sp.getY() + 1.1, z, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLAME, x, sp.getY() + 1.1, z, 3, 0.2, 0.2, 0.2, 0.02);
        }
        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.9f);
    }

    private static List<LivingEntity> objetivos(ServerLevel sl, ServerPlayer sp, double r) {
        return sl.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().inflate(r, 2, r), x -> valido(sp, x) && x.distanceTo(sp) <= r + x.getBbWidth() / 2);
    }

    /** A quien pueden dar los combos: nunca a jugadores, aldeanos, mascotas propias ni soportes de armadura. */
    public static boolean valido(Player owner, Entity x) {
        if (!(x instanceof LivingEntity le) || !le.isAlive() || x == owner) return false;
        if (x instanceof Player || x instanceof ArmorStand || x instanceof AbstractVillager) return false;
        return !(x instanceof TamableAnimal ta && ta.isOwnedBy(owner));
    }

    private static void dano(ServerPlayer sp, LivingEntity le, float dmg, int fuego, double kb) {
        le.invulnerableTime = 0;
        if (le.hurt(sp.damageSources().playerAttack(sp), dmg)) {
            if (fuego > 0 && !le.fireImmune()) le.setSecondsOnFire(fuego);
            le.knockback(kb, sp.getX() - le.getX(), sp.getZ() - le.getZ());
        }
    }

    // ------------------------------------------------------------------ el mandoble a una mano

    private static final Map<UUID, Integer> AVISO_MANOS = new HashMap<>();

    /** Con algo en la otra mano, el Mandoble del Juramento pega mucho menos (se empuna a dos manos). */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void onGolpeMandoble(LivingHurtEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp) || e.getSource().getDirectEntity() != sp) return;
        if (!MandobleJuramento.es(sp.getMainHandItem()) || sp.getOffhandItem().isEmpty()) return;
        e.setAmount(e.getAmount() * 0.55f);
        int now = sp.tickCount;
        if (now - AVISO_MANOS.getOrDefault(sp.getUUID(), -1000) > 200) {
            AVISO_MANOS.put(sp.getUUID(), now);
            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("darkcraft.mandoble.dos_manos"), true);
        }
    }

    // ------------------------------------------------------------------ cubrirse con las espadas

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getEntity() instanceof Player p) || !p.isUsingItem() || !EspadaRey.es(p.getUseItem())) return;
        DamageSource src = e.getSource();
        if (src.is(DamageTypeTags.BYPASSES_SHIELD) || src.is(DamageTypeTags.BYPASSES_ARMOR)) return;
        Vec3 from = src.getSourcePosition();
        if (from == null) return;
        Vec3 look = p.getViewVector(1f);
        Vec3 to = from.subtract(p.position()).normalize();
        if (look.x * to.x + look.z * to.z < 0.25) return;
        float golpe = e.getAmount();
        e.setAmount(golpe * 0.3f);
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.4f, 1.8f);
        if (p.level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.CRIT, p.getX() + look.x, p.getEyeY() - 0.3, p.getZ() + look.z, 10, 0.2, 0.2, 0.2, 0.3);
        if (src.getEntity() instanceof LivingEntity att && att.distanceTo(p) < 4)
            att.knockback(0.5, p.getX() - att.getX(), p.getZ() - att.getZ());
        p.getUseItem().hurtAndBreak(1, p, x -> x.broadcastBreakEvent(p.getUsedItemHand()));
        // parar el golpe cuesta estamina (y sin ella se rompe la guardia)
        if (p instanceof ServerPlayer sp) com.darkcraft.rol.Rol.guardia(sp, golpe * 0.7f);
    }
}
