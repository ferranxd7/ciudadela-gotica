package com.darkcraft.combate;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Ficha;
import com.darkcraft.rol.Frascos;
import com.darkcraft.rol.Rol;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * El combate propio, parte del servidor (el que pega, gasta la estamina y da los instantes invulnerables al rodar). El
 * cliente lleva el ritmo (cuando empieza cada golpe, la cadena, la carga) y lo pide; aqui se comprueba que se pueda,
 * se cobra la estamina y, en los ticks de dano del movimiento, se busca a quien alcanza el arma en su arco o en su
 * linea. Los demas jugadores ven la animacion porque se les manda ({@link Net.GolpeVisto}).
 * <ul>
 * <li>Cada golpe desequilibra: si a un enemigo normal se le acumula bastante, se queda aturdido un momento.</li>
 * <li>Rodar da unos ticks invulnerables (menos cuanto mas cargado se va); con sobrecarga no se rueda y se anda despacio.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Combate {
    private Combate() {}

    private static final class Estado {
        int accion = -1, paso, t;
        Armas.Tipo tipo;
        Armas.Mov mov;
        boolean izquierda, aterrizo;
        float carga;
        int cargaDesde = -1;
        final Set<Integer> tocados = new HashSet<>();
        /** Un golpe pedido antes de tiempo (por el retraso de la red): empieza en cuanto se pueda. */
        int[] pendiente;
        int invulnerable;
        int clasePeso = Peso.LIGERO;
    }

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();
    /** Lo que se le ha desequilibrado a cada enemigo: {acumulado, tick del ultimo golpe}. */
    private static final Map<LivingEntity, float[]> EQUILIBRIO = new WeakHashMap<>();
    private static final UUID MOD_SOBRECARGA = UUID.fromString("5e0c1a2b-7d14-4c0a-9a51-0d4c6f1e2a10");

    private static Estado de(Player p) {
        return ESTADOS.computeIfAbsent(p.getUUID(), k -> new Estado());
    }

    /** Si el jugador esta en sus instantes invulnerables de la voltereta. */
    public static boolean invulnerable(Player p) {
        Estado e = ESTADOS.get(p.getUUID());
        return e != null && e.invulnerable > 0;
    }

    public static boolean atacando(Player p) {
        Estado e = ESTADOS.get(p.getUUID());
        return e != null && e.accion >= 0;
    }

    // ------------------------------------------------------------------ lo que pide el cliente

    /** Empezar un golpe (o la carga de uno fuerte): accion, paso de la cadena, si va con la mano izquierda y lo cargado. */
    public static void pedir(ServerPlayer sp, int accion, int paso, boolean izquierda, int carga) {
        if (!sp.isAlive() || sp.isSpectator() || sp.isCreative() || Frascos.bebiendo(sp) || sp.isUsingItem()) {
            no(sp);
            return;
        }
        Armas.Tipo tipo = Armas.de(sp.getMainHandItem());
        if (tipo == null || accion < Armas.LIGERO || accion > Armas.CARGA) {
            no(sp);
            return;
        }
        Estado e = de(sp);
        // aun no ha acabado de pegar el anterior (la red adelanta un poco al cliente): se guarda para despues
        if (e.accion >= 0 && e.accion != Armas.CARGA && e.t < e.mov.golpe1() + 1 && accion != Armas.PESADO) {
            e.pendiente = new int[]{accion, paso, izquierda ? 1 : 0, carga};
            return;
        }
        empezar(sp, e, tipo, accion, paso, izquierda, carga);
    }

    private static void empezar(ServerPlayer sp, Estado e, Armas.Tipo tipo, int accion, int paso, boolean izquierda, int carga) {
        Armas.Mov mov = tipo.mov(accion, paso);
        if (accion == Armas.PESADO) {
            // lo cargado: lo que dice el cliente, sin pasarse de lo que de verdad ha pasado desde que empezo a cargar
            int medido = e.accion == Armas.CARGA && e.cargaDesde >= 0 ? sp.server.getTickCount() - e.cargaDesde + 3 : 0;
            e.carga = Mth.clamp(Math.min(carga, medido), 0, Armas.CARGA_MAX) / (float) Armas.CARGA_MAX;
        } else e.carga = 0f;
        // la carga no gasta (se paga al soltar el golpe)
        if (accion != Armas.CARGA && !Rol.gastarCombate(sp, mov.estamina())) {
            fin(e);
            no(sp);
            return;
        }
        e.accion = accion;
        e.tipo = tipo;
        e.mov = mov;
        e.paso = paso;
        e.t = 0;
        e.izquierda = izquierda && (Armas.dobles(sp) || tipo == Armas.Tipo.PUNOS);
        e.aterrizo = false;
        e.tocados.clear();
        e.pendiente = null;
        e.invulnerable = 0;
        if (accion == Armas.CARGA) {
            e.cargaDesde = sp.server.getTickCount();
            sp.level().playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), DarkCraftMod.SFX_CARGA_GOLPE.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
        } else if (accion == Armas.PESADO || accion == Armas.SALTO)
            sp.level().playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), DarkCraftMod.SFX_ESFUERZO.get(), SoundSource.PLAYERS, 0.7f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        visto(sp, accion, paso, e.izquierda, tipo, 0);
    }

    /** Rodar hacia (dx, dz) o dar un paso atras: cuesta estamina y da los instantes invulnerables. */
    public static void rodar(ServerPlayer sp, float dx, float dz, boolean atras) {
        if (!sp.isAlive() || sp.isSpectator() || Frascos.bebiendo(sp)) return;
        Estado e = de(sp);
        if (e.clasePeso == Peso.SOBRECARGADO && !sp.isCreative()) {
            no(sp);
            return;
        }
        Armas.Mov mov = atras ? Armas.PASO_ATRAS_MOV : Armas.RODAR_MOV;
        float coste = mov.estamina() + (e.clasePeso == Peso.PESADO && !atras ? 4f : 0f);
        if (!Rol.gastarCombate(sp, coste)) {
            no(sp);
            return;
        }
        fin(e);
        e.invulnerable = atras ? Armas.PASO_ATRAS_INVULNERABLE : Armas.invulnerableRodar(e.clasePeso);
        int ang = Math.round((float) Math.toDegrees(Math.atan2(dz, dx)));
        sp.level().playSound(sp, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_RODAR.get(), SoundSource.PLAYERS, 0.8f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        visto(sp, atras ? Armas.PASO_ATRAS : Armas.RODAR, e.clasePeso, false, Armas.Tipo.PUNOS, ang);
    }

    private static void no(ServerPlayer sp) {
        Net.to(sp, new Net.GolpeNo());
    }

    private static void fin(Estado e) {
        e.accion = -1;
        e.mov = null;
        e.cargaDesde = -1;
        e.pendiente = null;
    }

    /** Para los hechizos: los demas ven el gesto de lanzar (el paso es el hechizo). */
    public static void vistoHechizo(ServerPlayer sp, com.darkcraft.hechizos.Hechizo h) {
        visto(sp, Armas.HECHIZO, h.ordinal(), false, Armas.Tipo.PUNOS, 0);
    }

    /** Los que ven al jugador ven su golpe (el propio jugador ya lo anima el solo). */
    private static void visto(ServerPlayer sp, int accion, int paso, boolean izquierda, Armas.Tipo tipo, int extra) {
        Net.CH.send(PacketDistributor.TRACKING_ENTITY.with(() -> sp), new Net.GolpeVisto(sp.getId(), accion, paso, izquierda, tipo.ordinal(), extra));
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ESTADOS.isEmpty()) return;
        for (Iterator<Map.Entry<UUID, Estado>> it = ESTADOS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Estado> en = it.next();
            ServerPlayer sp = ev.getServer().getPlayerList().getPlayer(en.getKey());
            if (sp == null) {
                it.remove();
                continue;
            }
            Estado e = en.getValue();
            if (e.invulnerable > 0) e.invulnerable--;
            if (sp.tickCount % 10 == 6) peso(sp, e);
            if (e.accion < 0) continue;
            if (!sp.isAlive() || Armas.de(sp.getMainHandItem()) != e.tipo) {
                fin(e);
                continue;
            }
            e.t++;
            Armas.Mov m = e.mov;
            switch (e.accion) {
                case Armas.CARGA -> {
                    if (e.t > Armas.CARGA_MAX + 60) fin(e);
                }
                case Armas.SALTO -> {
                    if (e.t == 1) sp.fallDistance = 0;
                    if (!e.aterrizo && e.t >= 2 && (sp.onGround() || sp.isInWater() || e.t >= 30)) {
                        e.aterrizo = true;
                        e.t = 0;
                        caer(sp, e);
                    } else if (e.aterrizo && e.t >= 6) fin(e);
                }
                default -> {
                    if (e.t == Math.max(1, m.golpe0() - 2)) silbido(sp, e);
                    if (e.t >= m.golpe0() && e.t <= m.golpe1()) golpear(sp, e, false);
                    if (e.pendiente != null && e.t >= m.golpe1() + 1) {
                        int[] p = e.pendiente;
                        empezar(sp, e, e.tipo, p[0], p[1], p[2] == 1, p[3]);
                    } else if (e.t >= m.dura()) fin(e);
                }
            }
        }
    }

    /** El silbido del arma (para los demas: el que pega ya lo oye en su cliente, a su tiempo). */
    private static void silbido(ServerPlayer sp, Estado e) {
        boolean fuerte = e.accion == Armas.PESADO || e.tipo.dosManos;
        SoundEvent s = e.mov.estocada() ? DarkCraftMod.SFX_ESTOCADA.get() : fuerte ? DarkCraftMod.SFX_GOLPE_PESADO.get() : DarkCraftMod.SFX_GOLPE_LIGERO.get();
        sp.level().playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), s, SoundSource.PLAYERS, 0.8f, 0.92f + sp.getRandom().nextFloat() * 0.16f);
    }

    /** El golpe del salto al caer: alrededor y, sobre todo, delante. */
    private static void caer(ServerPlayer sp, Estado e) {
        ServerLevel sl = sp.serverLevel();
        BlockPos pie = sp.blockPosition().below();
        BlockState suelo = sl.getBlockState(pie);
        if (!suelo.isAir())
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), sp.getX(), sp.getY() + 0.1, sp.getZ(), 26, 1.0, 0.05, 1.0, 0.15);
        sl.sendParticles(ParticleTypes.CLOUD, sp.getX(), sp.getY() + 0.2, sp.getZ(), 10, 0.8, 0.05, 0.8, 0.04);
        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_GOLPE_SALTO.get(), SoundSource.PLAYERS, 1.0f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        golpear(sp, e, true);
    }

    // ------------------------------------------------------------------ a quien alcanza

    private static void golpear(ServerPlayer sp, Estado e, boolean salto) {
        ServerLevel sl = sp.serverLevel();
        Armas.Mov m = e.mov;
        double alcance = e.tipo.alcance * m.alcance() * (m.estocada() ? 1.1 : 1.0);
        double arco = salto ? 100 : m.arco() > 0 ? m.arco() : m.estocada() ? 16 : e.tipo.arco;
        Vec3 pecho = sp.position().add(0, sp.getBbHeight() * 0.62, 0);
        Vec3 mira = sp.getLookAngle();
        Vec3 h = new Vec3(mira.x, 0, mira.z);
        h = h.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : h.normalize();
        AABB caja = sp.getBoundingBox().inflate(alcance + 1.5, 2.0, alcance + 1.5);
        boolean alguno = false, fuerte = false;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, caja, x -> Combos.valido(sp, x))) {
            if (e.tocados.contains(le.getId())) continue;
            Vec3 c = le.position().add(0, le.getBbHeight() * 0.5, 0);
            double dx = c.x - pecho.x, dz = c.z - pecho.z;
            double dist = Math.sqrt(dx * dx + dz * dz) - le.getBbWidth() * 0.5;
            if (dist > alcance) continue;
            // en altura: que la caja del enemigo pase por donde va el arma
            if (le.getBoundingBox().maxY < sp.getY() - 0.8 || le.getBoundingBox().minY > sp.getY() + sp.getBbHeight() + 1.2) continue;
            if (m.estocada() && !salto) {
                // una estocada va en linea: lo lejos que queda el enemigo de la linea de la mirada
                Vec3 a = c.subtract(pecho);
                double largo = a.dot(mira);
                if (largo < -0.3) continue;
                double lado = a.subtract(mira.scale(largo)).length() - le.getBbWidth() * 0.5;
                if (lado > 0.9 && dist > 1.0) continue;
            } else if (dist > (salto ? 1.6 : 0.9)) {
                double ang = Math.toDegrees(Math.acos(Mth.clamp((dx * h.x + dz * h.z) / Math.max(1e-4, Math.sqrt(dx * dx + dz * dz)), -1, 1)));
                if (ang > arco) continue;
            }
            if (!sp.hasLineOfSight(le)) continue;
            e.tocados.add(le.getId());
            if (pegar(sp, e, le)) {
                alguno = true;
                fuerte |= e.accion == Armas.PESADO || salto || e.tipo.dosManos;
            }
        }
        if (alguno) Net.to(sp, new Net.Impacto(fuerte));
    }

    /** El dano de un golpe a un enemigo (con todo lo que hace el juego en un golpe normal: encantamientos, desgaste...). */
    private static boolean pegar(ServerPlayer sp, Estado e, LivingEntity le) {
        Armas.Mov m = e.mov;
        InteractionHand mano = e.izquierda && e.tipo != Armas.Tipo.PUNOS ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack arma = sp.getItemInHand(mano);
        // el ataque del arma con lo que le suman los atributos (sus letras); lo demas que lleve el jugador (fuerza de una
        // pocion...) se suma igual
        ItemStack principal = sp.getMainHandItem();
        float medido = (float) sp.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float otros = medido - Escalado.base(principal);
        float base = Escalado.total(arma, Escalado.base(arma), Ficha.atributos(sp)) + otros;
        float encanto = EnchantmentHelper.getDamageBonus(arma, le.getMobType());
        float mult = m.mult() * (1f + Armas.CARGA_EXTRA * e.carga);
        if (e.accion == Armas.SALTO && com.darkcraft.rol.Talismanes.lleva(sp, com.darkcraft.rol.Talisman.Clase.GARRA)) mult *= 1.15f;
        if (e.accion == Armas.PESADO && com.darkcraft.rol.Talismanes.lleva(sp, com.darkcraft.rol.Talisman.Clase.FURIA)) mult *= 1.12f;
        if (sp.hasEffect(com.darkcraft.rol.Efectos.BENDICION.get())) mult *= 1.15f;
        float dano = (base + encanto) * mult;
        DamageSource fuente = sp.damageSources().playerAttack(sp);
        float antes = le.getHealth();
        le.invulnerableTime = 0;
        if (!le.hurt(fuente, dano)) return false;
        // retroceso hacia donde mira el que pega
        float kb = m.retroceso() + EnchantmentHelper.getKnockbackBonus(sp) * 0.5f;
        if (kb > 0) le.knockback(kb, Mth.sin(sp.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(sp.getYRot() * Mth.DEG_TO_RAD));
        int fuego = EnchantmentHelper.getFireAspect(sp);
        if (fuego > 0 && !le.fireImmune()) le.setSecondsOnFire(fuego * 4);
        sp.setLastHurtMob(le);
        EnchantmentHelper.doPostHurtEffects(le, sp);
        EnchantmentHelper.doPostDamageEffects(sp, le);
        if (!arma.isEmpty()) {
            ItemStack copia = arma.copy();
            arma.hurtEnemy(le, sp);
            if (arma.isEmpty()) {
                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(sp, copia, mano);
                sp.setItemInHand(mano, ItemStack.EMPTY);
            }
        }
        float hecho = antes - le.getHealth();
        if (hecho > 0) sp.awardStat(Stats.DAMAGE_DEALT, Math.round(hecho * 10f));
        // chispas, y el sonido segun de que este hecho el enemigo
        ServerLevel sl = sp.serverLevel();
        Vec3 c = le.position().add(0, le.getBbHeight() * 0.6, 0);
        sl.sendParticles(ParticleTypes.CRIT, c.x, c.y, c.z, 8, 0.25, 0.3, 0.25, 0.25);
        if (e.accion == Armas.PESADO || e.tipo.dosManos) sl.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        boolean metal = le.getArmorValue() >= 8 || le instanceof com.darkcraft.caballeros.CaballeroZombi || le instanceof com.darkcraft.rey.Rey
                || le instanceof com.darkcraft.gavran.Gavran;
        sl.playSound(null, c.x, c.y, c.z, metal ? DarkCraftMod.SFX_IMPACTO_ARMADURA.get() : DarkCraftMod.SFX_IMPACTO_CARNE.get(), SoundSource.PLAYERS,
                0.9f, 0.9f + sp.getRandom().nextFloat() * 0.2f);
        desequilibrar(sp, le, m.aturde() * (1f + e.carga));
        return true;
    }

    /** Lo que desequilibra cada golpe: a un enemigo normal, si se le acumula bastante, se queda aturdido (los jefes llevan lo suyo). */
    private static void desequilibrar(ServerPlayer sp, LivingEntity le, float cuanto) {
        if (le instanceof com.darkcraft.rey.Rey || le instanceof com.darkcraft.gavran.Gavran || !le.isAlive()) return;
        int ahora = sp.server.getTickCount();
        float[] eq = EQUILIBRIO.computeIfAbsent(le, k -> new float[]{0f, ahora});
        // se recupera solo: dos puntos por tick desde los dos segundos sin golpes
        float pasado = ahora - eq[1];
        if (pasado > 40) eq[0] = Math.max(0f, eq[0] - (pasado - 40) * 2f);
        eq[0] += cuanto;
        eq[1] = ahora;
        float aguanta = Math.max(18f, le.getMaxHealth() * 0.5f);
        if (eq[0] < aguanta) return;
        eq[0] = 0f;
        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 9, false, false, false));
        le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 4, false, false, false));
        if (le instanceof Mob mob) mob.getNavigation().stop();
        ServerLevel sl = sp.serverLevel();
        sl.sendParticles(ParticleTypes.CRIT, le.getX(), le.getY() + le.getBbHeight() + 0.2, le.getZ(), 14, 0.3, 0.1, 0.3, 0.05);
        sl.playSound(null, le.getX(), le.getY(), le.getZ(), DarkCraftMod.SFX_ATURDIDO.get(), SoundSource.HOSTILE, 1.0f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
    }

    // ------------------------------------------------------------------ el peso del equipo

    private static void peso(ServerPlayer sp, Estado e) {
        float max = Peso.maximo(sp);
        e.clasePeso = Peso.clase(Peso.de(sp.getInventory()), max);
        var inst = sp.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) return;
        boolean lento = e.clasePeso == Peso.SOBRECARGADO && !sp.isCreative();
        boolean tiene = inst.getModifier(MOD_SOBRECARGA) != null;
        if (lento && !tiene) inst.addTransientModifier(new AttributeModifier(MOD_SOBRECARGA, "darkcraft_sobrecarga", -0.35, AttributeModifier.Operation.MULTIPLY_TOTAL));
        else if (!lento && tiene) inst.removeModifier(MOD_SOBRECARGA);
    }

    // ------------------------------------------------------------------ rodando no te dan

    /** En los instantes invulnerables de la voltereta no llega nada de lo que pega o se dispara (caer, ahogarse o el veneno, si). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAtaque(LivingAttackEvent ev) {
        if (!(ev.getEntity() instanceof ServerPlayer sp) || !invulnerable(sp)) return;
        DamageSource s = ev.getSource();
        if (s.getEntity() != null || s.getDirectEntity() != null || s.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) ev.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) de(sp);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        ESTADOS.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        ESTADOS.clear();
        EQUILIBRIO.clear();
    }

    /** Para la prueba de la CI. */
    public static String resumen(ServerPlayer sp) {
        Estado e = de(sp);
        return "accion=" + e.accion + " paso=" + e.paso + " t=" + e.t + (e.tipo != null ? " " + e.tipo : "") + " invulnerable=" + e.invulnerable
                + " peso=" + String.format(java.util.Locale.ROOT, "%.1f/%.1f", Peso.de(sp.getInventory()), Peso.maximo(sp))
                + " clase=" + e.clasePeso;
    }
}
