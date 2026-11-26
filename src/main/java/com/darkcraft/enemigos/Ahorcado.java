package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.SogaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * El ahorcado del Bosque Muerto (parte 7g): los que colgaron en la Arboleda de los Ahorcados y la Sombra no dejo morir.
 * Largo, flaco y rapido; aguanta poco. <b>Esperan colgados</b> de la soga de una rama ({@link Enemigo#COLGADO}: el cuerpo
 * cuelga justo bajo la soga de bloques, {@link SogaBlock}, y gira despacio alrededor del nudo) y <b>se descuelgan</b> sobre
 * quien pasa cerca ({@value #CAIDA} bloques) o les pega: la soga se parte, caen hacia su presa y el golpe de la caida
 * alcanza a todo lo de alrededor. Dos:
 * <ul>
 * <li>las <b>garras</b>: zarpazo, doble zarpazo y el salto (se lanza a quien esta a 3,6-7,5 bloques);</li>
 * <li>la <b>soga</b> (el del saco en la cabeza): latigazo que llega lejos y el <b>lazo</b>: lo lanza a quien esta a
 * {@value #LAZO_MIN}-{@value #LAZO_MAX} bloques y, si le engancha (rodar o cubrirse le libran), tira de el hasta sus pies.</li>
 * </ul>
 * Tambien esperan de rodillas al pie de su arbol ({@code REZA}) o tirados entre las raices ({@code TUMBADO}).
 */
public class Ahorcado extends Enemigo {
    public static final int GARRAS = 0, SOGA = 1;
    /** Lo cerca que tiene que pasar alguien (en horizontal) para que se descuelgue. */
    public static final double CAIDA = 4.2;
    public static final double LAZO_MIN = 4.0, LAZO_MAX = 10.0;
    /** Lo que sube el nudo sobre los pies y la soga del modelo sobre el nudo (bloques), y lo que se aparta el nudo del centro. */
    public static final double NUDO_ALTO = AhorcadoAnims.ESCALA * (24 - AhorcadoAnims.NUDO_Y) / 16.0,
            SOGA_ALTO = AhorcadoAnims.ESCALA * AhorcadoAnims.SOGA_ARRIBA / 16.0,
            NUDO_LADO = AhorcadoAnims.ESCALA * AhorcadoAnims.NUDO_X / 16.0;

    /** A quien lleva el lazo (el id; -1 = a nadie): el cliente dibuja la soga hasta el. */
    private static final EntityDataAccessor<Integer> LAZO = SynchedEntityData.defineId(Ahorcado.class, EntityDataSerializers.INT);

    // ---- en el servidor
    /** Donde cuelga (los pies) y el bloque de abajo de su soga (null = no cuelga de nada). */
    private double colX, colY, colZ;
    private BlockPos soga;
    private double lanzaX, lanzaZ;
    private int saltoEspera, lazoEspera = 40;
    private LivingEntity enganchado;

    public Ahorcado(EntityType<? extends Ahorcado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 28.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LAZO, -1);
    }

    @Override
    public String tipo() {
        return variante() == SOGA ? "ahorcado_soga" : "ahorcado_garras";
    }

    @Override
    protected int[] ticks() {
        return AhorcadoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return AhorcadoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return AhorcadoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return AhorcadoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return switch (postura) {
            case COLGADO -> AhorcadoAnims.CAER;
            case TUMBADO -> AhorcadoAnims.ALZARSE;
            default -> AhorcadoAnims.LEVANTARSE;
        };
    }

    // ------------------------------------------------------------------ colgado

    /** Si es una soga de la arboleda (en el mundo de mentira de las vistas no hay bloques del mod). */
    private static boolean esSoga(BlockState s) {
        return s.getBlock() instanceof SogaBlock;
    }

    @Override
    public void asignar(String sitio, int puesto, BlockPos hogar, float giro, int postura, int ronda) {
        super.asignar(sitio, puesto, hogar, giro, postura, ronda);
        if (postura == COLGADO && !colgar()) postura(DE_PIE);
    }

    /**
     * Lo cuelga de la soga que tiene encima (la primera a menos de nueve bloques de sus pies): el nudo justo debajo de la
     * soga de bloques, que llega hasta la de su modelo. Si la soga se habia quedado deshilachada de otra vez, se rehace.
     */
    private boolean colgar() {
        BlockPos h = hogar();
        if (h == null) return false;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int fondo = Integer.MIN_VALUE;
        for (int dy = 1; dy <= 9; dy++) {
            m.set(h.getX(), h.getY() + dy, h.getZ());
            if (esSoga(level().getBlockState(m))) {
                fondo = h.getY() + dy;
                break;
            }
        }
        if (fondo == Integer.MIN_VALUE) return false;
        soga = new BlockPos(h.getX(), fondo, h.getZ());
        BlockState s = level().getBlockState(soga);
        if (s.getValue(SogaBlock.PARTE) != SogaBlock.Parte.CUERDA) level().setBlock(soga, s.setValue(SogaBlock.PARTE, SogaBlock.Parte.CUERDA), 2);
        // el nudo esta a su izquierda: el cuerpo queda un poco al otro lado de la soga
        double a = Math.toRadians(getYRot());
        colX = h.getX() + 0.5 - NUDO_LADO * Math.cos(a);
        colZ = h.getZ() + 0.5 - NUDO_LADO * Math.sin(a);
        colY = fondo - NUDO_ALTO - SOGA_ALTO;
        setNoGravity(true);
        setPos(colX, colY, colZ);
        setDeltaMovement(Vec3.ZERO);
        return true;
    }

    /** Si cuelga todavia (y no esta cayendo). */
    public boolean cuelga() {
        return postura() == COLGADO && accion() == 0;
    }

    @Override
    protected boolean percibe(LivingEntity p) {
        if (postura() != COLGADO) return super.percibe(p);
        // colgado: se descuelga sobre quien pasa cerca por debajo (agachado, hay que pasar mas cerca)
        double dx = p.getX() - getX(), dz = p.getZ() - getZ(), dy = p.getY() - getY();
        double r = p.isCrouching() ? CAIDA * 0.6 : CAIDA;
        return dx * dx + dz * dz < r * r && dy < 0.8 && dy > -6;
    }

    @Override
    public boolean isPushable() {
        return postura() != COLGADO && super.isPushable();
    }

    @Override
    public void knockback(double fuerza, double x, double z) {
        if (postura() != COLGADO) super.knockback(fuerza, x, z);
    }

    // ------------------------------------------------------------------ pelear

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (variante() == SOGA) {
            if (d >= LAZO_MIN && d <= LAZO_MAX && lazoEspera <= 0 && random.nextFloat() < 0.55f) return AhorcadoAnims.LAZO;
            return d < 3.6 ? AhorcadoAnims.LATIGAZO : 0;
        }
        if (d >= 3.6 && d <= 7.5 && saltoEspera <= 0 && random.nextFloat() < 0.4f) return AhorcadoAnims.SALTO;
        return d < 2.5 ? (random.nextFloat() < 0.45f ? AhorcadoAnims.DOBLE : AhorcadoAnims.ZARPAZO) : 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case AhorcadoAnims.ZARPAZO -> new Golpe(2.4f, 90f, 1.0f, 0.35f);
            case AhorcadoAnims.DOBLE -> new Golpe(2.4f, 100f, 0.8f, 0.25f);
            case AhorcadoAnims.SALTO -> new Golpe(2.2f, 120f, 1.4f, 0.6f);
            case AhorcadoAnims.LATIGAZO -> new Golpe(3.8f, 40f, 1.1f, 0.6f);
            // la caida: a todo lo de alrededor de donde cae
            case AhorcadoAnims.CAER -> new Golpe(2.6f, 360f, 1.25f, 0.8f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case AhorcadoAnims.CAER -> caer(t);
            case AhorcadoAnims.ZARPAZO -> {
                if (t == 9) empujar(0.45);
            }
            case AhorcadoAnims.DOBLE -> {
                if (t == 8) empujar(0.35);
                if (t == 17) empujar(0.3);
            }
            case AhorcadoAnims.SALTO -> saltar(t);
            case AhorcadoAnims.LATIGAZO -> {
                if (t == 15) empujar(0.3);
            }
            case AhorcadoAnims.LAZO -> lazo(t);
            default -> {
            }
        }
    }

    /** Se descuelga: la soga se parte (tick 1), cae hacia su presa (2) y el golpe de la caida (8). */
    private void caer(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 1) {
            playSound(SoundEvents.LEASH_KNOT_BREAK, 1.2f, 0.7f + random.nextFloat() * 0.15f);
            if (soga != null) {
                BlockState s = sl.getBlockState(soga);
                if (esSoga(s)) {
                    sl.setBlock(soga, s.setValue(SogaBlock.PARTE, SogaBlock.Parte.CABO), 2);
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), soga.getX() + 0.5, soga.getY() + 0.1, soga.getZ() + 0.5, 8, 0.1, 0.1, 0.1, 0.05);
                }
            }
        } else if (t == 2) {
            setNoGravity(false);
            LivingEntity p = getTarget();
            lanzaX = lanzaZ = 0;
            if (p != null) {
                double dx = p.getX() - getX(), dz = p.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
                girarHacia(p, 180f);
                // cae unos 5 ticks: que acabe a un bloque largo de su presa
                double v = Math.min(0.5, Math.max(0, d - 1.2) / 5.0);
                if (d > 0.01) {
                    lanzaX = dx / d * v;
                    lanzaZ = dz / d * v;
                }
            }
            setDeltaMovement(lanzaX, 0, lanzaZ);
            hasImpulse = true;
        } else if (t > 2 && t < AhorcadoAnims.GOLPES[AhorcadoAnims.CAER][0] && !onGround()) {
            setDeltaMovement(lanzaX, getDeltaMovement().y, lanzaZ);
        } else if (t == AhorcadoAnims.GOLPES[AhorcadoAnims.CAER][0]) {
            playSound(DarkCraftMod.SFX_AHORCADO_CAE.get(), 1.3f, 0.9f + random.nextFloat() * 0.2f);
            BlockState bajo = sl.getBlockState(blockPosition().below());
            if (!bajo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, bajo), getX(), getY() + 0.1, getZ(), 24, 0.7, 0.05, 0.7, 0.1);
            CAIDAS++;
        }
    }

    /** Se agacha, salta hacia donde esta su presa (tick 12) y vuela hasta caer encima. */
    private void saltar(int t) {
        if (t == 12) {
            LivingEntity p = getTarget();
            Vec3 d = Vec3.directionFromRotation(0, getYRot());
            double lejos = p == null ? 4.0 : Math.min(6.5, Math.max(2.5, Math.sqrt(distanceToSqr(p)) - 1.2));
            double v = 0.16 + lejos * 0.12;
            setDeltaMovement(d.x * v, 0.42, d.z * v);
            hasImpulse = true;
            saltoEspera = 70 + random.nextInt(60);
        } else if (t > 12 && t < AhorcadoAnims.GOLPES[AhorcadoAnims.SALTO][0] && !onGround()) {
            // el paso de cada tick de la accion frena al que ataca: en el aire no
            Vec3 d = Vec3.directionFromRotation(0, getYRot());
            double h = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z) / 0.6;
            setDeltaMovement(d.x * h, getDeltaMovement().y, d.z * h);
        }
    }

    /** El lazo: lo lanza (tick 14), engancha o no (18) y, si engancha, tira de quien lleva hasta tenerlo a los pies. */
    private void lazo(int t) {
        LivingEntity p = getTarget();
        if (t == 14) {
            lazoEspera = 90 + random.nextInt(70);
            playSound(DarkCraftMod.SFX_AHORCADO_LAZO.get(), 1.2f, 0.9f + random.nextFloat() * 0.2f);
            entityData.set(LAZO, p == null ? -1 : p.getId());
        } else if (t == AhorcadoAnims.GOLPES[AhorcadoAnims.LAZO][0]) {
            enganchado = null;
            if (p != null && p.isAlive() && objetivoValido(p) && hasLineOfSight(p)) {
                double dx = p.getX() - getX(), dz = p.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
                Vec3 mira = Vec3.directionFromRotation(0, getYRot());
                boolean delante = d < 1.0 || (dx * mira.x + dz * mira.z) / d > Math.cos(Math.toRadians(24));
                // (rodar o cubrirse libran: el golpe no entra)
                if (d <= LAZO_MAX + 1.0 && delante && Math.abs(p.getY() - getY()) < 3.5
                        && p.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f)) {
                    enganchado = p;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, true));
                    LAZOS++;
                }
            }
            if (enganchado == null) entityData.set(LAZO, -1);
        } else if (t > AhorcadoAnims.GOLPES[AhorcadoAnims.LAZO][0] && enganchado != null) {
            LivingEntity e = enganchado;
            double dx = getX() - e.getX(), dz = getZ() - e.getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (!e.isAlive() || d < 1.7 || t > 31) {
                soltarLazo();
                return;
            }
            // tira: hacia sus pies, deprisa al principio
            double v = Math.min(0.55, 0.22 + d * 0.05);
            e.setDeltaMovement(dx / d * v, Math.max(e.getDeltaMovement().y, 0.06), dz / d * v);
            e.hurtMarked = true;
            e.hasImpulse = true;
        }
    }

    private void soltarLazo() {
        enganchado = null;
        if (!level().isClientSide) entityData.set(LAZO, -1);
    }

    /** En el cliente: a quien lleva el lazo (o null). */
    public Entity lazo() {
        int id = entityData.get(LAZO);
        return id < 0 ? null : level().getEntity(id);
    }

    /** Para las pruebas: caidas desde la soga y lazos que han enganchado desde que arranco el juego. */
    public static int CAIDAS, LAZOS;

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == AhorcadoAnims.CAER) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case AhorcadoAnims.LATIGAZO -> DarkCraftMod.SFX_AHORCADO_LAZO.get();
            case AhorcadoAnims.LAZO, AhorcadoAnims.CAER -> null;
            default -> DarkCraftMod.SFX_AHORCADO_ZARPAZO.get();
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_AHORCADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        if (accion == AhorcadoAnims.CAER) return 6;
        return variante() == SOGA ? 14 + random.nextInt(16) : 8 + random.nextInt(14);
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == SOGA ? 3.0 : 1.6;
    }

    @Override
    protected double prisa() {
        return variante() == SOGA ? 1.0 : 1.2;
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // una hebra de polvo de la soga de vez en cuando, mientras cuelga
            if (postura() == COLGADO && accion() == 0 && tickCount % 23 == 0 && random.nextInt(3) == 0)
                level().addParticle(new DustParticleOptions(new Vector3f(0.42f, 0.34f, 0.22f), 0.7f),
                        getX(), getY() + NUDO_ALTO + SOGA_ALTO * random.nextDouble(), getZ(), 0, -0.02, 0);
            return;
        }
        if (saltoEspera > 0) saltoEspera--;
        if (lazoEspera > 0) lazoEspera--;
        // colgado: quieto bajo su soga (nada le empuja)
        if (postura() == COLGADO && accion() == 0 && soga != null) {
            setPos(colX, colY, colZ);
            setDeltaMovement(Vec3.ZERO);
        }
        // si deja de tirar (le han aturdido, ha cambiado de accion), suelta el lazo
        if (enganchado != null && accion() != AhorcadoAnims.LAZO) soltarLazo();
        else if (enganchado == null && accion() != AhorcadoAnims.LAZO && entityData.get(LAZO) >= 0) entityData.set(LAZO, -1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (postura()) {
            case COLGADO -> DarkCraftMod.SFX_AHORCADO_SOGA.get();
            case TUMBADO -> null;
            default -> DarkCraftMod.SFX_AHORCADO_GEMIDO.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_AHORCADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_AHORCADO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public void die(DamageSource s) {
        soltarLazo();
        setNoGravity(false);
        super.die(s);
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 16, 0.3, 0.7, 0.3, 0.01);
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "ahorcado");
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }

    /** Para las pruebas y los comandos: como esta. */
    public String estado() {
        return (variante() == SOGA ? "soga" : "garras") + (postura() == COLGADO ? (accion() == 0 ? " colgado" : " cayendo") : "")
                + " en " + String.format(java.util.Locale.ROOT, "%.1f,%.1f,%.1f", getX(), getY(), getZ())
                + (soga != null ? " soga " + soga.toShortString() : "");
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    /** El giro del cuerpo en el cliente: colgado, siempre el del puesto (si no, el nudo se saldria de debajo de la soga). */
    @Override
    public void tick() {
        super.tick();
        if (postura() == COLGADO && accion() == 0) {
            yBodyRot = yBodyRotO = getYRot();
        }
    }

    /** Para la prueba: la soga de la que cuelga (o null). */
    public BlockPos soga() {
        return soga;
    }
}
