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
        /** La habilidad del arma que se esta haciendo (accion HABILIDAD) y si la parada esta en su instante. */
        Habilidad habilidad;
        boolean parada;
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
        // el muro falso de la Fortaleza de Hueso se deshace a golpes (en aventura no llega el clic de romper bloques)
        com.darkcraft.fortaleza.Trampas.golpe(sp);
        // y los muros ilusorios de Morvain (parte 7y)
        com.darkcraft.morvain.MurosIlusorios.golpe(sp);
        // y las calaveras de la corona de Fray Osmund (parte 7at): se rompen de un golpe
        com.darkcraft.enemigos.CalaveraOsmund.golpe(sp);
        if (accion == Armas.HABILIDAD) {
            habilidad(sp, paso);
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
        e.habilidad = null;
        e.parada = false;
    }

    // ------------------------------------------------------------------ la habilidad del arma (su ceniza de guerra)

    /** El cliente usa la habilidad del arma de la mano derecha ({@code ord}: la que el cree que tiene). */
    private static void habilidad(ServerPlayer sp, int ord) {
        ItemStack arma = sp.getMainHandItem();
        Armas.Tipo tipo = Armas.de(arma);
        Habilidad h = Habilidad.de(arma);
        if (tipo == null || h == null || h.ordinal() != ord) {
            no(sp);
            return;
        }
        Estado e = de(sp);
        if (e.accion >= 0 && e.accion != Armas.CARGA && e.mov != null && e.t < e.mov.libre()) {
            no(sp);
            return;
        }
        boolean gasta = Rol.gasta(sp);
        if (gasta && h.mana > 0 && Combos.energia(sp) < h.mana) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.hechizo.sin_mana", 0));
            no(sp);
            return;
        }
        float vida = sp.getMaxHealth() * Habilidad.VIDA_SANGRE;
        if (gasta && h == Habilidad.SANGRE && sp.getHealth() <= vida + 1f) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.habilidad.sin_vida", 0));
            no(sp);
            return;
        }
        if (!Rol.gastarCombate(sp, h.mov.estamina())) {
            no(sp);
            return;
        }
        if (gasta) {
            if (h.mana > 0) Combos.dar(sp, -h.mana);
            if (h == Habilidad.SANGRE) sp.setHealth(Math.max(1f, sp.getHealth() - vida));
        }
        e.accion = Armas.HABILIDAD;
        e.habilidad = h;
        e.tipo = tipo;
        e.mov = h.mov;
        e.paso = ord;
        e.t = 0;
        e.izquierda = false;
        e.aterrizo = false;
        e.carga = 0f;
        e.cargaDesde = -1;
        e.tocados.clear();
        e.pendiente = null;
        e.parada = false;
        e.invulnerable = h == Habilidad.PASO ? Habilidad.PASO_INVULNERABLE : 0;
        if (h == Habilidad.PASO || h == Habilidad.PARADA)
            sp.level().playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), sonido(h), SoundSource.PLAYERS, 0.8f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        else sp.level().playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), DarkCraftMod.SFX_ESFUERZO.get(), SoundSource.PLAYERS, 0.7f, 0.95f);
        visto(sp, Armas.HABILIDAD, ord, false, tipo, 0);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} usa la habilidad {} ({})", sp.getGameProfile().getName(), h.id, tipo);
    }

    /** El sonido de cada habilidad. */
    public static SoundEvent sonido(Habilidad h) {
        return switch (h) {
            case TORBELLINO -> DarkCraftMod.SFX_HABILIDAD_TORBELLINO.get();
            case ESTOCADA -> DarkCraftMod.SFX_HABILIDAD_ESTOCADA.get();
            case GRITO -> DarkCraftMod.SFX_HABILIDAD_GRITO.get();
            case SISMO -> DarkCraftMod.SFX_HABILIDAD_SISMO.get();
            case PASO -> DarkCraftMod.SFX_HABILIDAD_PASO.get();
            case LLAMAS -> DarkCraftMod.SFX_HABILIDAD_LLAMAS.get();
            case ESCARCHA -> DarkCraftMod.SFX_HABILIDAD_ESCARCHA.get();
            case RELAMPAGO -> DarkCraftMod.SFX_HABILIDAD_RELAMPAGO.get();
            case SANGRE -> DarkCraftMod.SFX_HABILIDAD_SANGRE.get();
            case PARADA -> DarkCraftMod.SFX_HABILIDAD_PARADA.get();
        };
    }

    /** Cada tick de una habilidad. */
    private static void tickHabilidad(ServerPlayer sp, Estado e) {
        Habilidad h = e.habilidad;
        Armas.Mov m = e.mov;
        if (h == null || m == null) {
            fin(e);
            return;
        }
        switch (h) {
            case SISMO -> {
                if (e.t == 1) sp.fallDistance = 0;
                if (!e.aterrizo && e.t >= 6 && (sp.onGround() || sp.isInWater() || e.t >= 34)) {
                    e.aterrizo = true;
                    e.t = 0;
                    sismo(sp, e);
                } else if (e.aterrizo && e.t >= 8) fin(e);
            }
            case GRITO -> {
                if (e.t == m.golpe0()) grito(sp);
                if (e.t >= m.dura()) fin(e);
            }
            case PASO -> {
                if (e.t >= m.dura()) fin(e);
            }
            case PARADA -> {
                e.parada = e.t >= m.golpe0() && e.t <= m.golpe1();
                if (e.t >= m.dura()) fin(e);
            }
            default -> {
                if (e.t == Math.max(1, m.golpe0() - 2))
                    sp.level().playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), sonido(h), SoundSource.PLAYERS, 0.9f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
                if (e.t == m.golpe0()) estela(sp, h);
                if (e.t >= m.golpe0() && e.t <= m.golpe1()) golpear(sp, e, false);
                if (e.t >= m.dura()) fin(e);
            }
        }
    }

    /** El grito de guerra: un rato pegando mas fuerte. */
    private static void grito(ServerPlayer sp) {
        sp.addEffect(new MobEffectInstance(com.darkcraft.rol.Efectos.FURIA.get(), Habilidad.FURIA_DURA, 0, false, false, true));
        ServerLevel sl = sp.serverLevel();
        sl.playSound(sp, sp.getX(), sp.getY() + 1, sp.getZ(), sonido(Habilidad.GRITO), SoundSource.PLAYERS, 1.0f, 1.0f);
        sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, sp.getX(), sp.getY() + 2.1, sp.getZ(), 3, 0.3, 0.1, 0.3, 0.0);
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2 / 24;
            sl.sendParticles(ParticleTypes.FLAME, sp.getX() + Math.cos(a) * 1.2, sp.getY() + 0.2, sp.getZ() + Math.sin(a) * 1.2, 1,
                    Math.cos(a) * 0.1, 0.05, Math.sin(a) * 0.1, 0.02);
        }
    }

    /** El sismo: la onda alrededor de donde cae (sobre todo delante). */
    private static void sismo(ServerPlayer sp, Estado e) {
        ServerLevel sl = sp.serverLevel();
        Vec3 mira = sp.getLookAngle();
        Vec3 h = new Vec3(mira.x, 0, mira.z);
        h = h.lengthSqr() < 1e-4 ? Vec3.ZERO : h.normalize();
        Vec3 c = sp.position().add(h.scale(1.0));
        BlockState suelo = sl.getBlockState(BlockPos.containing(c.x, sp.getY() - 0.5, c.z));
        if (!suelo.isAir()) {
            for (int i = 0; i < 32; i++) {
                double a = i * Math.PI * 2 / 32;
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), c.x + Math.cos(a) * 2.2, sp.getY() + 0.1, c.z + Math.sin(a) * 2.2,
                        3, 0.2, 0.05, 0.2, 0.15);
            }
        }
        sl.sendParticles(ParticleTypes.EXPLOSION, c.x, sp.getY() + 0.3, c.z, 2, 0.4, 0.05, 0.4, 0.0);
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, c.x, sp.getY() + 0.2, c.z, 10, 1.2, 0.05, 1.2, 0.01);
        sl.playSound(null, c.x, sp.getY(), c.z, sonido(Habilidad.SISMO), SoundSource.PLAYERS, 1.0f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        boolean alguno = false;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(3.4, 2.0, 3.4), x -> Combos.valido(sp, x))) {
            if (e.tocados.contains(le.getId()) || le.distanceToSqr(c.x, le.getY(), c.z) > 3.4 * 3.4) continue;
            e.tocados.add(le.getId());
            if (pegar(sp, e, le)) {
                alguno = true;
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.45, 0));
                le.hurtMarked = true;
            }
        }
        if (alguno) Net.to(sp, new Net.Impacto(true));
    }

    /** Lo que se ve del golpe de cada habilidad (para todos): llamas, escarcha, el rayo en linea, sangre... */
    private static void estela(ServerPlayer sp, Habilidad h) {
        ServerLevel sl = sp.serverLevel();
        Vec3 mira = sp.getLookAngle();
        Vec3 f = new Vec3(mira.x, 0, mira.z);
        f = f.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : f.normalize();
        Vec3 o = sp.position().add(0, sp.getBbHeight() * 0.6, 0);
        double base = Math.atan2(f.z, f.x);
        switch (h) {
            case LLAMAS, ESCARCHA, SANGRE, TORBELLINO -> {
                double arco = h == Habilidad.TORBELLINO ? Math.PI : Math.toRadians(h.mov.arco());
                int n = h == Habilidad.TORBELLINO ? 28 : 16;
                for (int i = 0; i <= n; i++) {
                    double a = base - arco + 2 * arco * i / n;
                    double r = 1.9 + sp.getRandom().nextDouble() * 0.5;
                    double x = o.x + Math.cos(a) * r, z = o.z + Math.sin(a) * r;
                    switch (h) {
                        case LLAMAS -> {
                            sl.sendParticles(ParticleTypes.FLAME, x, o.y, z, 2, 0.08, 0.1, 0.08, 0.02);
                            if (i % 3 == 0) sl.sendParticles(ParticleTypes.LAVA, x, o.y, z, 1, 0, 0, 0, 0);
                        }
                        case ESCARCHA -> {
                            sl.sendParticles(ParticleTypes.SNOWFLAKE, x, o.y, z, 3, 0.1, 0.12, 0.1, 0.02);
                            if (i % 2 == 0) sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, x, o.y, z, 1, 0.05, 0.05, 0.05, 0.05);
                        }
                        case SANGRE -> sl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.62f, 0.02f, 0.05f), 1.6f),
                                x, o.y, z, 3, 0.1, 0.1, 0.1, 0.0);
                        default -> {
                            if (i % 4 == 0) sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, o.y, z, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
            case RELAMPAGO, ESTOCADA -> {
                double largo = (h == Habilidad.RELAMPAGO ? 5.0 : 3.6);
                for (double d = 0.8; d <= largo; d += 0.35) {
                    double x = o.x + mira.x * d, y = o.y + mira.y * d, z = o.z + mira.z * d;
                    if (h == Habilidad.RELAMPAGO) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 3, 0.12, 0.12, 0.12, 0.05);
                        if (d > largo - 0.4) sl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
                    } else sl.sendParticles(ParticleTypes.CRIT, x, y, z, 2, 0.05, 0.05, 0.05, 0.1);
                }
            }
            default -> {
            }
        }
    }

    /** Lo que hace de mas el golpe de una habilidad al que alcanza. */
    private static void efectoGolpe(ServerPlayer sp, Habilidad h, LivingEntity le, float dano) {
        ServerLevel sl = sp.serverLevel();
        Vec3 c = le.position().add(0, le.getBbHeight() * 0.6, 0);
        switch (h) {
            case LLAMAS -> {
                if (!le.fireImmune()) le.setSecondsOnFire(5);
                sl.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 12, 0.25, 0.35, 0.25, 0.03);
            }
            case ESCARCHA -> {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 2, false, true, true));
                le.setTicksFrozen(Math.max(le.getTicksFrozen(), 180));
                sl.sendParticles(ParticleTypes.SNOWFLAKE, c.x, c.y, c.z, 16, 0.3, 0.4, 0.3, 0.03);
                // (parte 7aj) y suma congelacion, como una flecha de escarcha
                com.darkcraft.enemigos.Congelacion.sumar(le, ESCARCHA_SUMA, sp);
            }
            case RELAMPAGO -> {
                // un rayo de mas (dano de rayo: lo frenan las resistencias al rayo)
                le.invulnerableTime = 0;
                le.hurt(new DamageSource(sl.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT), sp, sp), dano * 0.35f);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 20, 0.3, 0.5, 0.3, 0.15);
            }
            case SANGRE -> {
                sl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.7f, 0.0f, 0.04f), 1.8f),
                        c.x, c.y, c.z, 24, 0.3, 0.4, 0.3, 0.0);
                // (parte 7aj) y suma sangrado, como una flecha de sangre
                com.darkcraft.enemigos.Sangrado.sumar(le, SANGRE_SUMA, sp);
            }
            default -> {
            }
        }
    }

    /** Lo que suman de sangrado el tajo sangriento y de congelacion el tajo helado (parte 7aj; de los 100 que hacen falta). */
    public static final float SANGRE_SUMA = 40f, ESCARCHA_SUMA = 30f;

    /** Si el jugador esta en el instante de la parada. */
    public static boolean parando(Player p) {
        Estado e = ESTADOS.get(p.getUUID());
        return e != null && e.parada;
    }

    /** La parada ha desviado un golpe: el que pego se queda aturdido (los jefes no) y se oye el choque. */
    private static void parar(ServerPlayer sp, Estado e, DamageSource s) {
        e.parada = false;
        ServerLevel sl = sp.serverLevel();
        Vec3 c = sp.position().add(sp.getLookAngle().scale(0.8)).add(0, sp.getBbHeight() * 0.65, 0);
        sl.playSound(null, c.x, c.y, c.z, sonido(Habilidad.PARADA), SoundSource.PLAYERS, 1.0f, 0.95f + sp.getRandom().nextFloat() * 0.1f);
        sl.sendParticles(ParticleTypes.CRIT, c.x, c.y, c.z, 14, 0.15, 0.15, 0.15, 0.4);
        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, c.x, c.y, c.z, 10, 0.2, 0.2, 0.2, 0.3);
        if (s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile pr) pr.discard();
        if (s.getEntity() instanceof LivingEntity le && le != sp) {
            desequilibrar(sp, le, 9999f);
            if (!(le instanceof com.darkcraft.rey.Rey) && !(le instanceof com.darkcraft.gavran.Gavran) && !(le instanceof com.darkcraft.enemigos.Capitan))
                le.knockback(0.5, sp.getX() - le.getX(), sp.getZ() - le.getZ());
        }
        Net.to(sp, new Net.Impacto(true));
        com.darkcraft.story.Logros.dar(sp, "parada");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} para un golpe de {}", sp.getGameProfile().getName(),
                s.getEntity() == null ? s.getMsgId() : s.getEntity().getType().toShortString());
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
                case Armas.HABILIDAD -> tickHabilidad(sp, e);
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
        if (sp.hasEffect(com.darkcraft.rol.Efectos.FURIA.get())) mult *= 1f + Habilidad.FURIA_EXTRA;
        float dano = (base + encanto) * mult;
        boolean metal = le.getArmorValue() >= 8 || le instanceof com.darkcraft.caballeros.CaballeroZombi || le instanceof com.darkcraft.rey.Rey
                || le instanceof com.darkcraft.gavran.Gavran;
        // los martillos (parte 7aj): pegan mas a los que llevan armadura y a quien se cubre con un escudo le rompen la guardia
        boolean martillo = arma.getItem() instanceof com.darkcraft.armas.Martillo;
        if (martillo) {
            if (metal) dano *= com.darkcraft.armas.Martillo.CONTRA_ARMADURA;
            if (le.isBlocking()) romperGuardia(sp, le);
        }
        DamageSource fuente = sp.damageSources().playerAttack(sp);
        float antes = le.getHealth();
        le.invulnerableTime = 0;
        if (!le.hurt(fuente, dano)) return false;
        if (e.accion == Armas.HABILIDAD && e.habilidad != null) efectoGolpe(sp, e.habilidad, le, dano);
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
        sl.playSound(null, c.x, c.y, c.z, martillo ? DarkCraftMod.SFX_MARTILLO_GOLPE.get() : metal ? DarkCraftMod.SFX_IMPACTO_ARMADURA.get()
                : DarkCraftMod.SFX_IMPACTO_CARNE.get(), SoundSource.PLAYERS, martillo ? 1.0f : 0.9f, 0.9f + sp.getRandom().nextFloat() * 0.2f);
        if (martillo && (e.accion == Armas.PESADO || e.tipo == Armas.Tipo.GRAN_MARTILLO))
            sl.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y - 0.2, c.z, 1, 0, 0, 0, 0);
        desequilibrar(sp, le, m.aturde() * (1f + e.carga));
        return true;
    }

    /** Para las pruebas: las guardias que han roto los martillos. */
    public static int GUARDIAS_ROTAS;

    /**
     * Un martillo da en un escudo levantado (parte 7aj): baja el escudo (un jugador no puede volver a cubrirse en 5 s, como con el
     * hacha del juego; un enemigo, un rato) y el golpe entra entero; suena la guardia rota y saltan chispas.
     */
    private static void romperGuardia(ServerPlayer sp, LivingEntity le) {
        GUARDIAS_ROTAS++;
        if (le instanceof Player p) {
            // como el hacha del juego, pero con el escudo que lleve (los del mod tambien)
            ItemStack escudo = p.getUseItem();
            if (!escudo.isEmpty()) p.getCooldowns().addCooldown(escudo.getItem(), 100);
            p.stopUsingItem();
            p.level().broadcastEntityEvent(p, (byte) 30);
        } else {
            ItemStack escudo = le.getUseItem();
            le.stopUsingItem();
            if (le instanceof com.darkcraft.caballeros.CaballeroZombi cz) cz.guardiaRota(60);
            if (!escudo.isEmpty()) le.level().broadcastEntityEvent(le, (byte) 30);
        }
        ServerLevel sl = sp.serverLevel();
        Vec3 c = le.position().add(0, le.getBbHeight() * 0.6, 0);
        sl.sendParticles(ParticleTypes.CRIT, c.x, c.y, c.z, 18, 0.3, 0.3, 0.3, 0.35);
        sl.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_GUARDIA_ROTA.get(), SoundSource.PLAYERS, 1.0f, 0.9f + sp.getRandom().nextFloat() * 0.15f);
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
        // (al Capitan cuesta mas desequilibrarle a golpes: lo suyo es pararle)
        float aguanta = le instanceof com.darkcraft.enemigos.Capitan ? 90f : Math.max(18f, le.getMaxHealth() * 0.5f);
        if (eq[0] < aguanta) return;
        eq[0] = 0f;
        if (le instanceof com.darkcraft.enemigos.Enemigo en) {
            // los enemigos del mod tienen su propio aturdimiento (corta lo que estuvieran haciendo)
            en.aturdir();
        } else {
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 9, false, false, false));
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 4, false, false, false));
        }
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
        if (!(ev.getEntity() instanceof ServerPlayer sp)) return;
        DamageSource s = ev.getSource();
        // la parada: un golpe que llega de delante en su instante se desvia
        Estado pe = ESTADOS.get(sp.getUUID());
        if (pe != null && pe.parada && !s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            net.minecraft.world.entity.Entity de = s.getDirectEntity() != null ? s.getDirectEntity() : s.getEntity();
            if (de != null) {
                Vec3 a = de.position().subtract(sp.position());
                Vec3 mira = sp.getLookAngle();
                double l = Math.sqrt(a.x * a.x + a.z * a.z), lm = Math.sqrt(mira.x * mira.x + mira.z * mira.z);
                if (l < 1e-3 || lm < 1e-3 || (a.x * mira.x + a.z * mira.z) / (l * lm) > 0.2) {
                    ev.setCanceled(true);
                    parar(sp, pe, s);
                    return;
                }
            }
        }
        if (!invulnerable(sp)) return;
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
                + (e.habilidad != null ? " habilidad=" + e.habilidad.id : "") + (e.parada ? " PARANDO" : "") + " peso=" + String.format(java.util.Locale.ROOT, "%.1f/%.1f", Peso.de(sp.getInventory()), Peso.maximo(sp))
                + " clase=" + e.clasePeso;
    }
}
