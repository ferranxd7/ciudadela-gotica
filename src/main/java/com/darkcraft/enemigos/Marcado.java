package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Los marcados del Crater de Sangre (parte 7u): los de Morvain a los que la Sombra saco de la muerte antes que al jugador y
 * les puso su marca para que rompieran sus cadenas. No pudieron. Siguen al pie de las anclas: de rodillas ante la cadena
 * ({@code REZA}), caidos de bruces como un muerto mas ({@code TUMBADO}: se alzan de golpe) o de guardia, con la marca
 * encendida en el pecho. Tres:
 * <ul>
 * <li>el <b>rompedor</b> (el del mazo de forja; de rodillas golpea el suelo al pie del ancla): el <b>mazazo</b> (lo alza
 * por encima de la cabeza y lo estrella: la onda tumba a quien este de pie cerca) y el <b>barrido</b>;</li>
 * <li>el <b>caballero</b> (espada y escudo con el ojo): el <b>tajo</b>, la <b>estocada</b> a fondo (se tira desde lejos),
 * el <b>escudazo</b> (se lleva la estamina) y <b>se cubre</b> de frente (el escudo para casi todo; las flechas, todo);</li>
 * <li>el <b>cazador</b> (arco y flecha): el <b>disparo</b>, la <b>flecha negra</b> (tensa mucho rato; a quien da le nubla la
 * vista y le debilita), la <b>punzada</b> con la flecha de la mano y el <b>desliz</b> (si se le acercan se deshace en
 * sombra y aparece unos bloques mas alla).</li>
 * </ul>
 * <b>La marca arde</b>: a media vida, cada golpe que dan (y cada disparo) lo repite su <b>reflejo</b> un instante despues
 * desde donde lo dio: una sombra de el, medio transparente, que hace lo mismo y se deshace (no se le puede pegar). Al
 * morir se llevan la mano a la marca, caen de bruces y se deshacen en sombra.
 */
public class Marcado extends Enemigo {
    public static final int ROMPEDOR = 0, CABALLERO = 1, CAZADOR = 2;
    /** Lo que deja pasar el escudo del caballero (las flechas, nada). */
    public static final float ESCUDO = 0.25f;
    /** Lo que tarda el reflejo en repetir el golpe (ticks desde que empieza el de verdad). */
    public static final int RETRASO = 12;
    /** La onda del mazazo: delante (bloques) y su radio. */
    public static final double ONDA_DELANTE = 2.9, R_ONDA = 2.7;
    /** El desliz: hasta donde (bloques) y lo que espera entre dos. */
    public static final double DESLIZ_MIN = 4.5, DESLIZ_MAX = 7.5;
    public static final int ESPERA_DESLIZ = 20 * 5, ESPERA_ESTOCADA = 20 * 5, ESPERA_NEGRA = 20 * 9;

    private static final EntityDataAccessor<Boolean> REFLEJO = SynchedEntityData.defineId(Marcado.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ARDE = SynchedEntityData.defineId(Marcado.class, EntityDataSerializers.BOOLEAN);

    public static final DustParticleOptions SOMBRA = new DustParticleOptions(new Vector3f(0.05f, 0.04f, 0.06f), 1.6f),
            BRASA = new DustParticleOptions(new Vector3f(0.95f, 0.18f, 0.08f), 0.9f),
            MARCA = new DustParticleOptions(new Vector3f(0.85f, 0.08f, 0.04f), 1.2f);

    private int ultimoDesliz = -100000, ultimaEstocada = -100000, ultimaNegra = -100000, recarga;
    /** El reflejo que esta por salir: la accion, desde donde y hacia donde, y en que tick. */
    private int reflejoAccion, reflejoCuando;
    private double reflejoX, reflejoY, reflejoZ;
    private float reflejoGiro;
    /** El de verdad (en un reflejo): a quien pega su reflejo. */
    private LivingEntity presaDelReflejo;
    /** (en el cliente) por donde iba el bucle de rodillas del rompedor, para el golpe contra el suelo. */
    private long faseAntes = -1;

    /**
     * Para la prueba: los mazazos y sus ondas que dan, los barridos, tajos, estocadas y escudazos, lo que para el escudo,
     * los disparos y flechas negras (y las que nublan), las punzadas, los deslices, los reflejos que salen y los golpes de
     * reflejo que dan, los que se alzan de bruces y los que se deshacen al morir.
     */
    public static int MAZAZOS, ONDAS, BARRIDOS, TAJOS, ESTOCADAS, ESCUDAZOS, PARADOS, DISPAROS, NEGRAS, NUBLADOS, PUNZADAS, DESLICES,
            REFLEJOS, REFLEJOS_DAN, ALZADOS, DESHECHOS;

    public Marcado(EntityType<? extends Marcado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 48.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 28.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(REFLEJO, false);
        entityData.define(ARDE, false);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case CABALLERO -> "marcado_caballero";
            case CAZADOR -> "marcado_cazador";
            default -> "marcado_rompedor";
        };
    }

    /** Cada uno, su vida, su golpe y su paso: el rompedor, grande y duro; el cazador, ligero. */
    @Override
    public void variante(int v) {
        super.variante(v);
        set(Attributes.MAX_HEALTH, v == ROMPEDOR ? 58 : v == CABALLERO ? 48 : 34);
        set(Attributes.ATTACK_DAMAGE, v == ROMPEDOR ? 7.5 : v == CABALLERO ? 6.0 : 5.0);
        set(Attributes.ARMOR, v == ROMPEDOR ? 5.0 : v == CABALLERO ? 8.0 : 2.0);
        set(Attributes.MOVEMENT_SPEED, v == ROMPEDOR ? 0.2 : v == CABALLERO ? 0.23 : 0.26);
        set(Attributes.KNOCKBACK_RESISTANCE, v == ROMPEDOR ? 0.85 : v == CABALLERO ? 0.55 : 0.2);
        set(Attributes.FOLLOW_RANGE, v == CAZADOR ? 40 : 28);
        setHealth(getMaxHealth());
    }

    private void set(net.minecraft.world.entity.ai.attributes.Attribute a, double valor) {
        var i = getAttribute(a);
        if (i != null) i.setBaseValue(valor);
    }

    /** Si es el reflejo de otro (una sombra que repite su golpe y se deshace). */
    public boolean reflejo() {
        return entityData.get(REFLEJO);
    }

    /** Si la marca le arde (media vida: sus golpes los repite su reflejo). */
    public boolean arde() {
        return entityData.get(ARDE);
    }

    @Override
    protected int[] ticks() {
        return MarcadoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return MarcadoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return MarcadoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return MarcadoAnims.MORIR;
    }

    /** Caido de bruces: se alza; de rodillas: se levanta. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? MarcadoAnims.ALZARSE : MarcadoAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (reflejo()) return 0;
        double dy = Math.abs(presa.getY() - getY());
        return switch (variante()) {
            case CABALLERO -> {
                if (dy > 3.0) yield 0;
                if (d >= 3.8 && d <= 7.5 && tickCount - ultimaEstocada > ESPERA_ESTOCADA && hasLineOfSight(presa) && random.nextFloat() < 0.45f)
                    yield MarcadoAnims.ESTOCADA;
                if (d < 2.9) yield random.nextFloat() < 0.25f ? MarcadoAnims.ESCUDAZO : MarcadoAnims.TAJO;
                yield d < 3.4 && random.nextFloat() < 0.3f ? MarcadoAnims.TAJO : 0;
            }
            case CAZADOR -> {
                if (d < 3.4 && dy < 3.0) {
                    if (tickCount - ultimoDesliz > ESPERA_DESLIZ && random.nextFloat() < 0.55f) yield MarcadoAnims.DESLIZ;
                    yield MarcadoAnims.PUNZADA;
                }
                if (d >= 4.5 && d <= 30 && dy < 16 && hasLineOfSight(presa)) {
                    if (tickCount - ultimaNegra > ESPERA_NEGRA && random.nextFloat() < 0.35f) yield MarcadoAnims.FLECHA_NEGRA;
                    if (recarga <= 0) yield MarcadoAnims.DISPARO;
                }
                yield 0;
            }
            default -> {
                if (dy > 3.0) yield 0;
                if (d < 3.4) yield random.nextFloat() < 0.5f ? MarcadoAnims.MAZAZO : MarcadoAnims.BARRIDO;
                yield d < 4.2 && random.nextFloat() < 0.25f ? MarcadoAnims.MAZAZO : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case MarcadoAnims.MAZAZO -> new Golpe(3.7f, 60f, 1.4f, 0.9f);
            case MarcadoAnims.BARRIDO -> new Golpe(4.1f, 170f, 1.0f, 1.1f);
            case MarcadoAnims.TAJO -> new Golpe(3.3f, 90f, 1.0f, 0.4f);
            case MarcadoAnims.ESTOCADA -> new Golpe(3.8f, 40f, 1.3f, 0.6f);
            case MarcadoAnims.ESCUDAZO -> new Golpe(2.7f, 80f, 0.6f, 1.3f);
            case MarcadoAnims.PUNZADA -> new Golpe(2.9f, 60f, 0.85f, 0.2f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = MarcadoAnims.GOLPES[accion];
        // la marca arde: el reflejo repetira este golpe desde aqui
        if (t == 1 && !reflejo() && arde() && repite(accion)) {
            reflejoAccion = accion;
            reflejoCuando = tickCount + RETRASO;
            reflejoX = getX();
            reflejoY = getY();
            reflejoZ = getZ();
            reflejoGiro = getYRot();
        }
        switch (accion) {
            case MarcadoAnims.MAZAZO -> {
                if (t == 6) playSound(DarkCraftMod.SFX_MARCADO_ESFUERZO.get(), 1.1f, 0.85f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.3);
                if (t == g[0]) {
                    if (!reflejo()) MAZAZOS++;
                    onda();
                }
            }
            case MarcadoAnims.BARRIDO -> {
                if (t == g[0] - 2) empujar(0.3);
                if (t == g[0] && !reflejo()) BARRIDOS++;
            }
            case MarcadoAnims.TAJO -> {
                if (t == g[0] && !reflejo()) TAJOS++;
            }
            case MarcadoAnims.ESTOCADA -> {
                // se tira a fondo: dos o tres bloques en un instante
                if (t == 4 && !reflejo()) ultimaEstocada = tickCount;
                if (t >= g[0] - 3 && t <= g[0]) empujar(0.62);
                if (t == g[0] && !reflejo()) ESTOCADAS++;
            }
            case MarcadoAnims.ESCUDAZO -> {
                if (t == g[0] - 1) empujar(0.4);
                if (t == g[0]) {
                    if (!reflejo()) ESCUDAZOS++;
                    playSound(DarkCraftMod.SFX_ESCUDO_GOLPE.get(), 1.2f, 0.7f + random.nextFloat() * 0.1f);
                }
            }
            case MarcadoAnims.DISPARO -> {
                if (t == 5) playSound(DarkCraftMod.SFX_ARCO_TENSAR.get(), 1.0f, 0.85f + random.nextFloat() * 0.15f);
                if (t == g[0]) disparar(false);
            }
            case MarcadoAnims.FLECHA_NEGRA -> {
                if (t == 4) {
                    ultimaNegra = tickCount;
                    playSound(DarkCraftMod.SFX_ARCO_TENSAR.get(), 1.0f, 0.7f);
                }
                // mientras tensa, la sombra se le enrosca en la flecha
                if (t > 8 && t < g[0] && t % 2 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(SOMBRA, getX() + d.x * 0.6, getEyeY() - 0.1, getZ() + d.z * 0.6, 2, 0.15, 0.1, 0.15, 0.0);
                }
                if (t == g[0]) disparar(true);
            }
            case MarcadoAnims.PUNZADA -> {
                if (t == g[0] - 2) empujar(0.35);
                if (t == g[0] && !reflejo()) PUNZADAS++;
            }
            case MarcadoAnims.DESLIZ -> {
                if (t == 2) ultimoDesliz = tickCount;
                if (t == g[0]) desliz();
            }
            case MarcadoAnims.LEVANTARSE, MarcadoAnims.ALZARSE -> {
                if (t == 2) sacudirse(accion == MarcadoAnims.ALZARSE);
            }
            default -> {
            }
        }
    }

    /** Lo que repite el reflejo: los golpes y los disparos (no la flecha negra, ni el desliz, ni la punzada). */
    private static boolean repite(int accion) {
        return switch (accion) {
            case MarcadoAnims.MAZAZO, MarcadoAnims.BARRIDO, MarcadoAnims.TAJO, MarcadoAnims.ESTOCADA, MarcadoAnims.ESCUDAZO, MarcadoAnims.DISPARO -> true;
            default -> false;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (reflejo()) REFLEJOS_DAN++;
        switch (accion) {
            case MarcadoAnims.ESCUDAZO -> {
                // el escudazo rompe la guardia: se lleva la estamina y deja un momento sin pies
                if (le instanceof ServerPlayer sp) com.darkcraft.rol.Rol.quitarEstamina(sp, 32f);
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2), this);
            }
            case MarcadoAnims.MAZAZO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1), this);
            default -> {
            }
        }
    }

    /** A quien puede pegar: lo de siempre; un reflejo, solo a los jugadores y a la presa del que lo echo. */
    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (reflejo()) {
            if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
            return e == presaDelReflejo || e == getTarget();
        }
        return super.objetivoValido(e);
    }

    // ------------------------------------------------------------------ la onda, los disparos, el desliz

    /** El mazo da en el suelo: polvo, piedras y una onda que alcanza a quien este de pie cerca. */
    private void onda() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * ONDA_DELANTE, z = getZ() + d.z * ONDA_DELANTE;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, 34, 0.7, 0.05, 0.7, 0.25);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, 8, 0.7, 0.1, 0.7, 0.02);
        sl.sendParticles(reflejo() ? SOMBRA : BRASA, x, getY() + 0.3, z, 16, 0.8, 0.1, 0.8, 0.05);
        level().playSound(null, x, getY(), z, DarkCraftMod.SFX_MARCADO_MAZAZO.get(), SoundSource.HOSTILE, reflejo() ? 1.0f : 1.6f,
                (reflejo() ? 0.7f : 0.9f) + random.nextFloat() * 0.12f);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(x - 3, getY() - 1, z - 3, x + 3, getY() + 2, z + 3),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (!le.onGround() || le.distanceToSqr(x, le.getY(), z) > R_ONDA * R_ONDA) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                ONDAS++;
                if (reflejo()) REFLEJOS_DAN++;
                le.knockback(0.45, x - le.getX(), z - le.getZ());
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.32, 0));
            }
        }
    }

    /** Suelta la flecha hacia donde apuntaba (el cuerpo ya no gira desde tres ticks antes: se esquiva de lado). */
    private void disparar(boolean negra) {
        LivingEntity t = getTarget() != null ? getTarget() : presaDelReflejo;
        AbstractArrow a = negra ? new FlechaNegra(level(), this) : new Flecha(level(), this);
        a.setPos(getX(), getEyeY() - 0.2, getZ());
        a.setBaseDamage(negra ? 3.0 : 2.4);
        a.pickup = AbstractArrow.Pickup.DISALLOWED;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double h = t == null ? 12 : Math.max(1, Math.sqrt(Math.pow(t.getX() - getX(), 2) + Math.pow(t.getZ() - getZ(), 2)));
        double v = negra ? 3.2 : 2.8;
        Vec3 dir = tiro(h, t == null ? 0 : t.getY(0.55) - a.getY(), d, v);
        a.shoot(dir.x, dir.y, dir.z, (float) v, negra ? 0.6f : 1.4f);
        level().addFreshEntity(a);
        playSound(DarkCraftMod.SFX_ARCO_SUELTA.get(), 1.1f, (negra ? 0.7f : 0.95f) + random.nextFloat() * 0.1f);
        if (negra) {
            playSound(DarkCraftMod.SFX_MARCADO_FLECHA.get(), 1.4f, 1.0f);
            if (!reflejo()) NEGRAS++;
        } else if (!reflejo()) DISPAROS++;
        recarga = 26 + random.nextInt(26);
    }

    /** Se deshace en sombra y aparece unos bloques mas alla (de lado o hacia atras de su presa), donde haya suelo. */
    private void desliz() {
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity p = getTarget();
        double ox = getX(), oy = getY(), oz = getZ();
        double base = p == null ? Math.toRadians(getYRot() + 90) : Math.atan2(getZ() - p.getZ(), getX() - p.getX());
        for (int intento = 0; intento < 10; intento++) {
            double a = base + (random.nextDouble() - 0.5) * Math.PI * 1.2;
            double r = DESLIZ_MIN + random.nextDouble() * (DESLIZ_MAX - DESLIZ_MIN);
            double x = ox + Math.cos(a) * r, z = oz + Math.sin(a) * r;
            double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(oy) + 2, Mth.floor(z));
            if (Math.abs(y - oy) > 2.6) continue;
            Vec3 antes = position();
            moveTo(Mth.floor(x) + 0.5, y, Mth.floor(z) + 0.5, getYRot(), getXRot());
            if (!sl.noCollision(this)) {
                moveTo(antes.x, antes.y, antes.z, getYRot(), getXRot());
                continue;
            }
            DESLICES++;
            humo(sl, ox, oy, oz);
            humo(sl, getX(), getY(), getZ());
            sl.playSound(null, ox, oy, oz, DarkCraftMod.SFX_MARCADO_DESLIZ.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
            if (p != null) {
                float giro = (float) (Mth.atan2(p.getZ() - getZ(), p.getX() - getX()) * Mth.RAD_TO_DEG) - 90f;
                setYRot(giro);
                yBodyRot = yHeadRot = giro;
            }
            recarga = 0;
            return;
        }
    }

    private void humo(ServerLevel sl, double x, double y, double z) {
        sl.sendParticles(SOMBRA, x, y + 1.0, z, 30, 0.35, 0.7, 0.35, 0.02);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.8, z, 8, 0.3, 0.5, 0.3, 0.01);
        sl.sendParticles(BRASA, x, y + 1.2, z, 6, 0.3, 0.4, 0.3, 0.02);
    }

    /** Se levanta de rodillas (el polvo) o se alza de bruces (la ceniza y la sombra que se le despega). */
    private void sacudirse(boolean deBruces) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (deBruces) {
            ALZADOS++;
            sl.sendParticles(SOMBRA, getX(), getY() + 0.3, getZ(), 24, 0.6, 0.15, 0.6, 0.02);
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 10, 0.6, 0.2, 0.6, 0.02);
            playSound(DarkCraftMod.SFX_MARCADO_ALERTA.get(), 1.1f, 0.85f + random.nextFloat() * 0.2f);
        } else sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.4, getZ(), 8, 0.4, 0.2, 0.4, 0.01);
    }

    // ------------------------------------------------------------------ el escudo

    /** Si el escudo le tapa ese golpe: el caballero, de frente (70 grados a cada lado), sin estar atacando ni aturdido. */
    private boolean cubre(DamageSource s) {
        if (variante() != CABALLERO || postura() != DE_PIE || muriendo() || aturdido() || reflejo()) return false;
        int a = accion();
        if (a != 0 && a != MarcadoAnims.BLOQUEO) return false;
        if (s.is(DamageTypeTags.BYPASSES_ARMOR) || s.is(DamageTypeTags.IS_FIRE) || s.is(DamageTypeTags.IS_EXPLOSION)) return false;
        Vec3 de = s.getSourcePosition();
        if (de == null) return false;
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = de.x - getX(), dz = de.z - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l > 0.34;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // al reflejo no se le puede pegar: es una sombra
        if (reflejo()) return false;
        if (!level().isClientSide && cubre(s)) {
            boolean flecha = s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
            level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_ESCUDO_GOLPE.get(), SoundSource.HOSTILE, 1.0f, 0.75f + random.nextFloat() * 0.2f);
            if (accion() == 0) empezar(MarcadoAnims.BLOQUEO);
            PARADOS++;
            if (flecha) return false;
            cuanto *= ESCUDO;
        }
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide && !arde() && isAlive() && getHealth() <= getMaxHealth() * 0.5f) arderMarca();
        return ok;
    }

    /** A media vida le arde la marca: un fogonazo rojo y desde ahora sus golpes los repite su reflejo. */
    private void arderMarca() {
        entityData.set(ARDE, true);
        playSound(DarkCraftMod.SFX_MARCADO_ALERTA.get(), 1.4f, 0.7f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(MARCA, getX(), getY() + 1.3, getZ(), 30, 0.3, 0.3, 0.3, 0.12);
            sl.sendParticles(SOMBRA, getX(), getY() + 1.0, getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        }
    }

    // ------------------------------------------------------------------ el reflejo

    /** Sale el reflejo: una sombra suya en el sitio donde empezo el golpe, que lo repite (y se deshace al acabar). */
    private void echarReflejo() {
        if (!(level() instanceof ServerLevel sl) || reflejoAccion == 0) return;
        int accion = reflejoAccion;
        reflejoAccion = 0;
        Marcado r = DarkCraftMod.MARCADO.get().create(sl);
        if (r == null) return;
        r.variante(variante());
        var dano = r.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dano != null) dano.setBaseValue(getAttributeValue(Attributes.ATTACK_DAMAGE));
        r.moveTo(reflejoX, reflejoY, reflejoZ, reflejoGiro, 0f);
        r.yBodyRot = r.yHeadRot = reflejoGiro;
        r.entityData.set(REFLEJO, true);
        r.presaDelReflejo = getTarget();
        r.setNoGravity(true);
        r.setSilent(false);
        sl.addFreshEntity(r);
        r.setTarget(getTarget());
        r.empezar(accion);
        REFLEJOS++;
        sl.sendParticles(SOMBRA, reflejoX, reflejoY + 1.0, reflejoZ, 16, 0.3, 0.6, 0.3, 0.02);
        sl.playSound(null, reflejoX, reflejoY, reflejoZ, DarkCraftMod.SFX_MARCADO_REFLEJO.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    public boolean isPickable() {
        return !reflejo() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return !reflejo() && super.isPushable();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !reflejo() && super.canBeCollidedWith();
    }

    /** El reflejo no se gira hacia nadie: repite el golpe hacia donde se dio. */
    @Override
    protected float girarHacia(LivingEntity t, float max) {
        return reflejo() ? getYRot() : super.girarHacia(t, max);
    }

    @Override
    public boolean shouldBeSaved() {
        return !reflejo() && super.shouldBeSaved();
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        if (reflejo()) {
            // la sombra: hace su golpe sin moverse de donde salio y se deshace al acabar
            setDeltaMovement(Vec3.ZERO);
            super.aiStep();
            if (level().isClientSide) {
                if (tickCount % 2 == 0)
                    level().addParticle(SOMBRA, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + random.nextDouble() * 1.9,
                            getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
                return;
            }
            if (tickCount > 3 && accion() == 0 || tickCount > 80) {
                if (level() instanceof ServerLevel sl) sl.sendParticles(SOMBRA, getX(), getY() + 1.0, getZ(), 14, 0.3, 0.6, 0.3, 0.02);
                discard();
            }
            return;
        }
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive()) return;
            // la marca que arde: brasas que le salen del pecho
            if (arde() && tickCount % 3 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                level().addParticle(BRASA, getX() + d.x * 0.32, getY() + 1.35, getZ() + d.z * 0.32, d.x * 0.02, 0.03, d.z * 0.02);
            }
            golpeDeRodillas();
            return;
        }
        if (recarga > 0) recarga--;
        if (reflejoAccion != 0 && tickCount >= reflejoCuando) echarReflejo();
    }

    /** (cliente) El rompedor de rodillas golpea el suelo al ritmo de su bucle: el golpe, las chispas y el polvo. */
    private void golpeDeRodillas() {
        if (variante() != ROMPEDOR || postura() != REZA || accion() != 0) {
            faseAntes = -1;
            return;
        }
        long fase = reloj.getAccumulatedTime() % 2400L;
        if (faseAntes >= 0 && faseAntes < 1600L && fase >= 1600L) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            double x = getX() + d.x * 1.3, z = getZ() + d.z * 1.3;
            for (int k = 0; k < 6; k++)
                level().addParticle(ParticleTypes.SMALL_FLAME, x, getY() + 0.15, z, (random.nextDouble() - 0.5) * 0.15, 0.08 + random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.15);
            level().addParticle(ParticleTypes.SMOKE, x, getY() + 0.2, z, 0, 0.02, 0);
            level().playLocalSound(getX(), getY(), getZ(), DarkCraftMod.SFX_MARCADO_CADENA.get(), SoundSource.HOSTILE, 0.9f,
                    0.85f + random.nextFloat() * 0.2f, false);
        }
        faseAntes = fase;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case MarcadoAnims.MAZAZO, MarcadoAnims.BARRIDO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            case MarcadoAnims.ESTOCADA -> DarkCraftMod.SFX_ESTOCADA.get();
            case MarcadoAnims.TAJO, MarcadoAnims.PUNZADA -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_MARCADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case ROMPEDOR -> 20 + random.nextInt(18);
            case CAZADOR -> 8 + random.nextInt(10);
            default -> 12 + random.nextInt(16);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == ROMPEDOR ? 2.5 : variante() == CAZADOR ? 11.0 : 2.0;
    }

    /** El cazador tira desde lo alto de las torres a quien pasa por debajo (y desde abajo a quien esta arriba). */
    @Override
    protected double alturaMaxima() {
        return variante() == CAZADOR ? 16.0 : 2.6;
    }

    @Override
    protected double prisa() {
        return variante() == ROMPEDOR ? 0.9 : 1.0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return reflejo() ? null : DarkCraftMod.SFX_MARCADO_SUSURRO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_MARCADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_MARCADO_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "marcado");
        }
    }

    /** Al acabar de morir (de bruces) se le apaga la marca y se deshace en sombra. */
    @Override
    protected void tickDeath() {
        int m = MarcadoAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == MarcadoAnims.TICKS[m] - 2) deshacerse();
        if (deathTime >= MarcadoAnims.TICKS[m] + 2) remove(RemovalReason.KILLED);
    }

    private void deshacerse() {
        if (!(level() instanceof ServerLevel sl)) return;
        DESHECHOS++;
        sl.sendParticles(SOMBRA, getX(), getY() + 0.4, getZ(), 40, 0.7, 0.25, 0.7, 0.03);
        sl.sendParticles(MARCA, getX(), getY() + 0.5, getZ(), 14, 0.4, 0.2, 0.4, 0.08);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.4, getZ(), 10, 0.6, 0.2, 0.6, 0.02);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_MARCADO_MUERTE.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected boolean shouldDropLoot() {
        return !reflejo() && super.shouldDropLoot();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (!jugador) return;
        if (random.nextFloat() < 0.08f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (random.nextFloat() < (variante() == ROMPEDOR ? 0.07f : 0.04f)) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }

    /** Para las pruebas: echar ya el reflejo de un golpe (aunque no le arda la marca). */
    public void reflejoDe(int accion) {
        reflejoAccion = accion;
        reflejoCuando = tickCount;
        reflejoX = getX();
        reflejoY = getY();
        reflejoZ = getZ();
        reflejoGiro = getYRot();
        echarReflejo();
    }

    /** Para las pruebas: que le arda ya la marca. */
    public void encenderMarca() {
        if (!level().isClientSide && !arde()) arderMarca();
    }
}
