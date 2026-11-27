package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

/**
 * Las gargolas de la ciudadela (parte 7aa): guardianes de piedra de la Sombra posados en los pedestales de los rellanos de
 * la cadena del sur y en la isla (de rodillas, {@code REZA}: una estatua, sin el brillo de los ojos), o volando en ronda.
 * Al acercarse alguien (o al pegarle) se les resquebraja la piedra, gritan y alzan el vuelo.
 * <ul>
 * <li>En el aire, <b>sin fisica</b> (como el dragon y el jinete): dan vueltas alrededor de su presa por encima y a un lado
 * (el camino es estrecho: se le ve venir desde el vacio), y cada poco atacan: el <b>picado</b> (se para, grita y se lanza en
 * linea recta contra el pecho de su presa: a quien pille se lo lleva por delante —un buen empujon, de lado: fuera de la
 * cadena si no se agacha o rueda—), las <b>garras</b> (se cierne delante de su presa y le lanza las patas) o <b>se posa</b>
 * en el camino junto a ella.</li>
 * <li>En tierra (con fisica): el <b>zarpazo</b> (dos garrazos) y el <b>coletazo</b> (da la vuelta entera); a las dos o tres,
 * o si su presa se aleja, vuelve a alzar el vuelo (<b>despega</b>).</li>
 * <li>Al morir se le parte la piedra: si volaba, cae.</li>
 * </ul>
 */
public class Gargola extends Enemigo {
    private static final EntityDataAccessor<Boolean> VUELA = SynchedEntityData.defineId(Gargola.class, EntityDataSerializers.BOOLEAN);

    /** Las vueltas alrededor de su presa: radio, altura sobre ella y lo que gira por tick (radianes). */
    public static final double R_ORBITA = 6.5, ALTO_ORBITA = 4.5, GIRO_ORBITA = 0.035;
    /** Lo que vuela (bloques por tick): de paso y en el picado. */
    public static final double V_VUELO = 0.42, V_PICADO = 1.05;
    /** Lo que ve posada y volando (agachado, la mitad). */
    public static final double R_VE = 14, R_VE_VUELO = 24;
    /** El picado: el golpe (multiplica su ataque) y el empujon. */
    public static final float DANO_PICADO = 1.1f, EMPUJE_PICADO = 1.5f;
    /** Lo que espera entre un ataque y otro en el aire (ticks). */
    static final int ESPERA_AIRE = 70;
    static final DustParticleOptions POLVO = new DustParticleOptions(new Vector3f(0.42f, 0.41f, 0.43f), 1.4f),
            BRASA = new DustParticleOptions(new Vector3f(0.92f, 0.16f, 0.08f), 0.8f);

    /** Para la prueba: las que despiertan, los picados y lo que pillan, los garrazos, las que se posan y despegan, los zarpazos y coletazos, las que caen. */
    public static int DESPIERTAS, PICADOS, PICADOS_DAN, GARRAS_N, POSADAS, DESPEGUES, ZARPAZOS, COLETAZOS, CAEN;

    private static final int ORBITA = 0, A_GARRAS = 1, A_POSARSE = 2, A_CASA = 3;
    private int modo = ORBITA, modoT, espera = ESPERA_AIRE / 2, ataquesTierra, sinSuelo, sinPresaT;
    private double ang, sentido = 1;
    private Vec3 vel = Vec3.ZERO, picadoDir = null;
    private double posaX, posaY, posaZ;
    private final Set<Integer> pillados = new HashSet<>();
    /** Como espera en su puesto (posada o en ronda) y cuanto ronda. */
    private int posturaPuesto = DE_PIE, rondaPuesto = 0;
    /** Para la prueba: quieta en el aire (no ataca ni se mueve sola). */
    public boolean quieta;

    public Gargola(EntityType<? extends Gargola> t, Level l) {
        super(t, l);
        xpReward = 0;
        noPhysics = true;
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 56.0).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.ARMOR, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VUELA, false);
    }

    /** Si va volando (sin fisica). */
    public boolean vuela() {
        return entityData.get(VUELA);
    }

    private void vuela(boolean v) {
        entityData.set(VUELA, v);
        noPhysics = v || postura() == REZA;
        setNoGravity(v || postura() == REZA);
        if (v) {
            setOnGround(false);
            getNavigation().stop();
        } else vel = Vec3.ZERO;
    }

    @Override
    public void asignar(String sitio, int puesto, BlockPos hogar, float giro, int postura, int ronda) {
        super.asignar(sitio, puesto, hogar, giro, postura, ronda);
        posturaPuesto = postura;
        rondaPuesto = ronda;
        // las de ronda salen ya volando; las de los pedestales, estatuas (sin fisica: no se caen del pedestal)
        if (postura == DE_PIE) vuela(true);
        else {
            noPhysics = true;
            setNoGravity(true);
        }
        ang = random.nextDouble() * Math.PI * 2;
        sentido = random.nextBoolean() ? 1 : -1;
    }

    @Override
    public String tipo() {
        return "gargola";
    }

    @Override
    protected int[] ticks() {
        return GargolaAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return GargolaAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return GargolaAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return GargolaAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == REZA ? GargolaAnims.DESPIERTA : 0;
    }

    @Override
    protected int pausaTras(int a) {
        return switch (a) {
            case GargolaAnims.ZARPAZO -> 12;
            case GargolaAnims.COLETAZO -> 16;
            case GargolaAnims.ATERRIZA -> 6;
            default -> 8;
        };
    }

    @Override
    protected double distanciaDeseada() {
        return vuela() ? 1000 : 1.8;
    }

    /** El desnivel lo mira ella ({@link #elegir}: si su presa sube o baja mucho, vuela). */
    @Override
    protected double alturaMaxima() {
        return 1000;
    }

    @Override
    protected double correa() {
        return 44;
    }

    @Override
    protected double prisa() {
        return 1.15;
    }

    /**
     * Posada (de piedra) ve a {@link #R_VE} bloques; volando, a {@link #R_VE_VUELO} (se la ve venir desde lejos); agachado, la
     * mitad. Siempre con linea de vista y por todos lados (desde lo alto se ve todo).
     */
    @Override
    protected boolean percibe(LivingEntity p) {
        double r = postura() == REZA ? R_VE : R_VE_VUELO;
        if (p.isCrouching()) r *= 0.5;
        return distanceToSqr(p) < r * r && hasLineOfSight(p);
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case GargolaAnims.ZARPAZO -> new Golpe(2.8f, 110f, 0.9f, 0.5f);
            case GargolaAnims.COLETAZO -> new Golpe(3.2f, 360f, 0.8f, 1.1f);
            case GargolaAnims.GARRAS -> new Golpe(3.4f, 120f, 0.85f, 0.8f);
            default -> null;
        };
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (vuela() || quieta) return 0;
        // en tierra: a las dos o tres, o si se le va, vuelve a volar
        if (ataquesTierra >= 2 + (getId() & 1) || d > 8.5 || Math.abs(presa.getY() - getY()) > 2.5) return GargolaAnims.DESPEGA;
        Vec3 mira = Vec3.directionFromRotation(0, getYRot());
        double dx = presa.getX() - getX(), dz = presa.getZ() - getZ();
        boolean detras = d > 0.5 && (dx * mira.x + dz * mira.z) / d < -0.2;
        if (d < 3.2 && (detras || random.nextFloat() < 0.3)) {
            ataquesTierra++;
            return GargolaAnims.COLETAZO;
        }
        if (d < 2.9) {
            ataquesTierra++;
            return GargolaAnims.ZARPAZO;
        }
        return 0;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_GARGOLA_GRITO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_GARGOLA_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == GargolaAnims.ZARPAZO || accion == GargolaAnims.GARRAS ? DarkCraftMod.SFX_GARGOLA_GARRAS.get()
                : accion == GargolaAnims.COLETAZO ? DarkCraftMod.SFX_GARGOLA_COLA.get() : null;
    }

    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    /** La que vuela grita al ver a alguien (la de piedra ya grita al despertar). */
    @Override
    public void setTarget(LivingEntity t) {
        LivingEntity antes = getTarget();
        super.setTarget(t);
        if (!level().isClientSide && t != null && antes == null && isAlive() && vuela() && accion() == 0) empezar(GargolaAnims.GRITO);
    }

    @Override
    public boolean isPushable() {
        return !vuela() && postura() == DE_PIE;
    }

    @Override
    protected void pushEntities() {
        if (!vuela()) super.pushEntities();
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    /** En el aire (y de estatua) no anda: se mueve ella sola ({@link #volar}). */
    @Override
    protected boolean anda() {
        return !vuela() && !noPhysics;
    }

    /** En el aire (y de estatua en su pedestal) no la mueve la fisica del juego: va por su cuenta (en el servidor). */
    @Override
    public void travel(Vec3 v) {
        if (!level().isClientSide && isAlive() && (vuela() || noPhysics)) return;
        super.travel(v);
    }

    /** En el aire, un golpe la aparta un poco (lo suma a lo que vuela); de estatua, nada. */
    @Override
    public void knockback(double fuerza, double x, double z) {
        if (level().isClientSide || !isAlive()) {
            super.knockback(fuerza, x, z);
            return;
        }
        if (vuela()) {
            double l = Math.sqrt(x * x + z * z);
            if (l > 1e-4) vel = vel.add(-x / l * fuerza * 0.5, 0.08, -z / l * fuerza * 0.5);
            return;
        }
        if (noPhysics) return;
        super.knockback(fuerza, x, z);
    }

    /** De rodillas en su pedestal es una estatua: sin fisica (no se cae ni se la empuja); de pie en tierra, con ella. */
    @Override
    public void postura(int p) {
        super.postura(p);
        if (level() == null || level().isClientSide) return;
        if (p == REZA) {
            noPhysics = true;
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        }
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        if (!level().isClientSide && isAlive()) {
            if (vuela()) {
                volar();
                setDeltaMovement(Vec3.ZERO);
            } else if (postura() == DE_PIE && !noPhysics && accion() != GargolaAnims.DESPEGA) {
                // en tierra: si se queda sin suelo (la han echado del camino), alza el vuelo
                if (!onGround() && getDeltaMovement().y < -0.3) {
                    if (++sinSuelo > 4) despegarYa();
                } else sinSuelo = 0;
                // sin presa y lejos de su puesto, vuelve volando (a su pedestal no se sube andando)
                BlockPos h = hogar();
                if (getTarget() == null && accion() == 0 && h != null && h.distToCenterSqr(getX(), getY(), getZ()) > 2.5 * 2.5) {
                    if (++sinPresaT > 30) {
                        sinPresaT = 0;
                        empezar(GargolaAnims.DESPEGA);
                    }
                } else sinPresaT = 0;
            }
        }
        super.aiStep();
        if (!level().isClientSide && isAlive() && vuela()) {
            // en el aire mira a su presa (o a donde va)
            LivingEntity t = getTarget();
            if (accion() != GargolaAnims.PICADO || picadoDir == null) {
                if (t != null) {
                    float quiere = (float) (Mth.atan2(t.getZ() - getZ(), t.getX() - getX()) * Mth.RAD_TO_DEG) - 90f;
                    girar(quiere, 14f);
                } else if (vel.horizontalDistanceSqr() > 1e-3) girar((float) (Mth.atan2(vel.z, vel.x) * Mth.RAD_TO_DEG) - 90f, 8f);
            } else girar((float) (Mth.atan2(picadoDir.z, picadoDir.x) * Mth.RAD_TO_DEG) - 90f, 30f);
            if (tickCount % 50 == 0) playSound(DarkCraftMod.SFX_GARGOLA_ALETEO.get(), 0.9f, 0.9f + random.nextFloat() * 0.2f);
        }
    }

    private void girar(float quiere, float max) {
        float y = Mth.approachDegrees(getYRot(), quiere, max);
        setYRot(y);
        yBodyRot = yHeadRot = y;
    }

    // ------------------------------------------------------------------ en el aire

    private void volar() {
        LivingEntity t = getTarget();
        int a = accion();
        modoT++;
        if (a == GargolaAnims.PICADO) {
            picado(t);
            return;
        }
        if (a == GargolaAnims.ATURDIDO) {
            // aturdida en el aire: se queda sin fuerzas y baja un poco
            moverA(position().add(0, -1.0, 0), 0.07);
            return;
        }
        if (a == GargolaAnims.GARRAS || a == GargolaAnims.GRITO) {
            frenar();
            return;
        }
        if (a == GargolaAnims.DESPEGA) {
            moverA(position().add(0, 2.5, 0), 0.3);
            return;
        }
        if (quieta) {
            frenar();
            return;
        }
        if (t == null || !t.isAlive()) {
            sinPresa();
            return;
        }
        if (espera > 0) espera--;
        switch (modo) {
            case A_GARRAS -> {
                // delante de su presa, a un paso y a la altura de su cabeza
                Vec3 desde = new Vec3(getX() - t.getX(), 0, getZ() - t.getZ());
                desde = desde.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : desde.normalize();
                Vec3 p = t.position().add(desde.scale(2.0)).add(0, 1.0, 0);
                moverA(p, V_VUELO);
                if (position().distanceTo(p) < 0.9 || modoT > 50) {
                    empezar(GargolaAnims.GARRAS);
                    GARRAS_N++;
                    modo = ORBITA;
                    espera = ESPERA_AIRE + random.nextInt(40);
                }
            }
            case A_POSARSE -> {
                Vec3 p = new Vec3(posaX, posaY, posaZ);
                double d = position().distanceTo(p);
                // primero por encima y luego abajo
                Vec3 ir = Math.hypot(getX() - posaX, getZ() - posaZ) > 1.2 ? p.add(0, 2.5, 0) : p;
                moverA(ir, V_VUELO * 0.8);
                if (d < 0.7) posarse(p);
                else if (modoT > 80) {
                    modo = ORBITA;
                    espera = ESPERA_AIRE / 2;
                }
            }
            default -> {
                // las vueltas: por encima y a un lado de su presa, cambiando de sentido de vez en cuando
                ang += GIRO_ORBITA * sentido;
                if (random.nextInt(260) == 0) sentido = -sentido;
                Vec3 p = t.position().add(Math.cos(ang) * R_ORBITA, ALTO_ORBITA + Math.sin(tickCount * 0.05) * 0.8, Math.sin(ang) * R_ORBITA);
                moverA(p, V_VUELO);
                if (espera <= 0 && hasLineOfSight(t)) atacarDesdeElAire(t);
            }
        }
    }

    private void atacarDesdeElAire(LivingEntity t) {
        modoT = 0;
        float q = random.nextFloat();
        if (q < 0.55) {
            empezar(GargolaAnims.PICADO);
            picadoDir = null;
            pillados.clear();
            PICADOS++;
            espera = ESPERA_AIRE + random.nextInt(50);
        } else if (q < 0.8 && sitioParaPosarse(t)) {
            modo = A_POSARSE;
        } else {
            modo = A_GARRAS;
        }
    }

    /** El picado: se para y se prepara; en su tick se lanza en linea recta contra el pecho de su presa y pasa de largo. */
    private void picado(LivingEntity t) {
        int k = accionT;
        if (k < GargolaAnims.PICADO_DESDE) {
            frenar();
            if (k == GargolaAnims.PICADO_DESDE - 4) playSound(DarkCraftMod.SFX_GARGOLA_GRITO.get(), 1.2f, 1.05f);
            return;
        }
        if (picadoDir == null) {
            // (adonde va: lo que ha andado en el ultimo tick; la velocidad de un jugador en el servidor no dice nada)
            Vec3 anda = t != null ? new Vec3(t.getX() - t.xo, 0, t.getZ() - t.zo) : Vec3.ZERO;
            Vec3 blanco = t != null ? t.position().add(anda.scale(5)).add(0, t.getBbHeight() * 0.6, 0) : position().add(Vec3.directionFromRotation(10, getYRot()).scale(8));
            Vec3 d = blanco.subtract(position().add(0, getBbHeight() * 0.5, 0));
            picadoDir = d.lengthSqr() < 1e-4 ? Vec3.directionFromRotation(0, getYRot()) : d.normalize();
            playSound(DarkCraftMod.SFX_GARGOLA_PICADO.get(), 1.4f, 1f);
        }
        if (k > GargolaAnims.PICADO_HASTA) {
            frenar();
            return;
        }
        // no baja por debajo de su presa: a su altura sigue de largo
        if (t != null && getY() < t.getY() + 0.3 && picadoDir.y < 0) picadoDir = new Vec3(picadoDir.x, 0, picadoDir.z).normalize();
        Vec3 paso = picadoDir.scale(V_PICADO);
        AABB caja = getBoundingBox().move(paso);
        if (!level().noCollision(this, caja.deflate(0.15))) {
            // da con algo (los tablones, la cadena): se acaba el picado y sube
            vel = new Vec3(0, 0.25, 0);
            picadoDir = new Vec3(0, 0, 0);
            return;
        }
        if (picadoDir.lengthSqr() < 1e-4) {
            frenar();
            return;
        }
        setPos(getX() + paso.x, getY() + paso.y, getZ() + paso.z);
        vel = paso;
        // a quien pille por el camino
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.6), e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (!pillados.add(le.getId())) continue;
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * DANO_PICADO;
            if (!le.hurt(damageSources().mobAttack(this), dano)) continue;
            // agachado aguanta en el sitio (un empujon pequeno y sin levantarlo: el juego no le deja caerse del borde)
            if (le.isCrouching()) le.knockback(EMPUJE_PICADO * 0.3f, -picadoDir.x, -picadoDir.z);
            else {
                le.knockback(EMPUJE_PICADO, -picadoDir.x, -picadoDir.z);
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.32, 0));
            }
            le.hurtMarked = true;
            PICADOS_DAN++;
            setLastHurtMob(le);
            if (level() instanceof ServerLevel sl)
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), le.getX(), le.getY() + 1, le.getZ(), 10, 0.3, 0.4, 0.3, 0.1);
        }
        if (level() instanceof ServerLevel sl && k % 2 == 0)
            sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.9, getZ(), 1, 0.15, 0.15, 0.15, 0.0);
    }

    /** Sin presa: las de ronda dan vueltas a su puesto; las de los pedestales vuelven a el y se posan. */
    private void sinPresa() {
        BlockPos h = hogar();
        if (h == null) {
            frenar();
            return;
        }
        Vec3 casa = new Vec3(h.getX() + 0.5, h.getY(), h.getZ() + 0.5);
        if (posturaPuesto == REZA) {
            double dh = Math.hypot(getX() - casa.x, getZ() - casa.z);
            moverA(dh > 1.2 ? casa.add(0, 2.5, 0) : casa, V_VUELO * 0.7);
            if (position().distanceTo(casa) < 0.6) posarse(casa);
            return;
        }
        ang += GIRO_ORBITA * 0.6 * sentido;
        double r = Math.max(3, rondaPuesto);
        moverA(casa.add(Math.cos(ang) * r, 1.5 + Math.sin(tickCount * 0.03) * 1.2, Math.sin(ang) * r), V_VUELO * 0.5);
    }

    /** Busca donde posarse junto a su presa (en el camino: suelo firme y sitio encima, a 2,5-3,5 bloques). */
    private boolean sitioParaPosarse(LivingEntity t) {
        for (int k = 0; k < 8; k++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = 2.5 + random.nextDouble();
            double x = t.getX() + Math.cos(a) * r, z = t.getZ() + Math.sin(a) * r;
            Double y = suelo(x, t.getY(), z);
            if (y == null) continue;
            AABB caja = getType().getDimensions().makeBoundingBox(x, y + 0.01, z);
            if (!level().noCollision(this, caja)) continue;
            posaX = x;
            posaY = y;
            posaZ = z;
            return true;
        }
        return false;
    }

    /** Lo alto del suelo firme bajo (x, z) cerca de y (dos arriba, tres abajo), o null. */
    private Double suelo(double x, double y, double z) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int bx = Mth.floor(x), bz = Mth.floor(z);
        for (int by = Mth.floor(y) + 2; by >= Mth.floor(y) - 3; by--) {
            m.set(bx, by, bz);
            VoxelShape f = level().getBlockState(m).getCollisionShape(level(), m);
            if (f.isEmpty()) continue;
            double arriba = by + f.max(net.minecraft.core.Direction.Axis.Y);
            if (arriba > y + 2.01) continue;
            return arriba;
        }
        return null;
    }

    private void posarse(Vec3 p) {
        vuela(false);
        setPos(p.x, p.y, p.z);
        empezar(GargolaAnims.ATERRIZA);
        ataquesTierra = 0;
        modo = ORBITA;
        POSADAS++;
        playSound(DarkCraftMod.SFX_GARGOLA_ATERRIZA.get(), 1.1f, 0.95f);
    }

    private void despegarYa() {
        vuela(true);
        vel = new Vec3(0, 0.45, 0);
        modo = ORBITA;
        espera = ESPERA_AIRE / 2;
        ataquesTierra = 0;
        sinSuelo = 0;
        DESPEGUES++;
    }

    /** Hacia un punto, a lo sumo v por tick, suavizado; sin meterse en los bloques (si da con algo, sube). */
    private void moverA(Vec3 p, double v) {
        Vec3 d = p.subtract(position());
        double l = d.length();
        Vec3 quiere = l < 1e-4 ? Vec3.ZERO : d.scale(Math.min(v, l * 0.25) / l);
        vel = vel.scale(0.82).add(quiere.scale(0.18));
        if (vel.length() > v) vel = vel.normalize().scale(v);
        AABB caja = getBoundingBox().move(vel);
        if (!level().noCollision(this, caja.deflate(0.1))) {
            Vec3 arriba = new Vec3(vel.x * 0.3, Math.max(0.15, Math.abs(vel.y)), vel.z * 0.3);
            if (level().noCollision(this, getBoundingBox().move(arriba).deflate(0.1))) vel = arriba;
            else vel = Vec3.ZERO;
        }
        setPos(getX() + vel.x, getY() + vel.y, getZ() + vel.z);
    }

    private void frenar() {
        vel = vel.scale(0.7);
        if (vel.lengthSqr() < 1e-5) return;
        if (level().noCollision(this, getBoundingBox().move(vel).deflate(0.1))) setPos(getX() + vel.x, getY() + vel.y, getZ() + vel.z);
        else vel = Vec3.ZERO;
    }

    // ------------------------------------------------------------------ lo de cada accion

    @Override
    protected void alAvanzar(int accion, int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (accion == GargolaAnims.DESPIERTA) {
            if (t == 1) {
                DESPIERTAS++;
                playSound(DarkCraftMod.SFX_GARGOLA_DESPIERTA.get(), 1.3f, 1f);
            }
            if (t < 20 && t % 3 == 0)
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), getX(), getY() + 1.0, getZ(), 6, 0.4, 0.6, 0.4, 0.05);
            if (t == 26) playSound(DarkCraftMod.SFX_GARGOLA_GRITO.get(), 1.3f, 0.95f);
            if (t == GargolaAnims.TICKS[accion] - 1) {
                // al acabar de despertar, al aire
                vuela(true);
                vel = new Vec3(0, 0.35, 0);
                espera = 20;
                DESPEGUES++;
            }
        } else if (accion == GargolaAnims.DESPEGA) {
            if (t == 1) DESPEGUES++;
            if (t == GargolaAnims.DESPEGA_SALTO) {
                vuela(true);
                vel = new Vec3(0, 0.45, 0);
                modo = ORBITA;
                espera = ESPERA_AIRE / 2;
                ataquesTierra = 0;
                sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.2, getZ(), 8, 0.5, 0.1, 0.5, 0.05);
            }
        } else if (accion == GargolaAnims.ZARPAZO && t == 1) ZARPAZOS++;
        else if (accion == GargolaAnims.COLETAZO && t == 1) COLETAZOS++;
        else if (accion == GargolaAnims.ATERRIZA && t == GargolaAnims.GOLPES[accion][0])
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_PLANKS.defaultBlockState()), getX(), getY() + 0.1, getZ(), 10, 0.5, 0.1, 0.5, 0.1);
        // en el aire, mientras hace algo, sigue mirando a su presa
        if (vuela() && getTarget() != null && accion != GargolaAnims.PICADO) {
            float quiere = (float) (Mth.atan2(getTarget().getZ() - getZ(), getTarget().getX() - getX()) * Mth.RAD_TO_DEG) - 90f;
            girar(quiere, 12f);
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide && level() instanceof ServerLevel sl)
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), getX(), getY() + getBbHeight() * 0.6, getZ(), 6, 0.3, 0.4, 0.3, 0.05);
        return ok;
    }

    // ------------------------------------------------------------------ morir

    @Override
    public void die(DamageSource s) {
        boolean volaba = vuela();
        super.die(s);
        if (!level().isClientSide) {
            CAEN++;
            entityData.set(VUELA, false);
            noPhysics = false;
            setNoGravity(false);
            playSound(DarkCraftMod.SFX_GARGOLA_MUERTE.get(), 1.3f, 1f);
            if (volaba) setDeltaMovement(vel.x * 0.5, -0.2, vel.z * 0.5);
            net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                    : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
            if (quien != null && !esEfimero()) {
                com.darkcraft.espejo.Espejo.bestia(quien, tipo());
                com.darkcraft.story.Logros.dar(quien, "gargola");
            }
        }
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (level() instanceof ServerLevel sl && deathTime % 4 == 0)
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), getX(), getY() + 0.8, getZ(), 5, 0.4, 0.5, 0.4, 0.05);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putBoolean("vuela", vuela());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        vuela(t.getBoolean("vuela"));
    }

    // ------------------------------------------------------------------ para la prueba

    /** Para la prueba: que alce el vuelo ya (o que se pose donde esta). */
    public void probarVuelo(boolean v) {
        if (v) despegarYa();
        else {
            vuela(false);
            noPhysics = false;
            setNoGravity(false);
        }
    }

    /** Para la prueba: un picado ya contra su presa. */
    public void probarPicado() {
        empezar(GargolaAnims.PICADO);
        picadoDir = null;
        pillados.clear();
        PICADOS++;
    }

    /** Para la prueba: que vaya a lanzarle las garras a su presa (se pone delante y las lanza). */
    public void probarGarras() {
        if (!vuela()) despegarYa();
        modo = A_GARRAS;
        modoT = 0;
    }

    /** Para la prueba: que vaya a posarse junto a su presa (si encuentra sitio). */
    public boolean probarPosarse() {
        LivingEntity t = getTarget();
        if (t == null || !sitioParaPosarse(t)) return false;
        modo = A_POSARSE;
        modoT = 0;
        return true;
    }

    /** Lo que hace ahora (para la prueba y el comando). */
    public String estado() {
        return (vuela() ? "volando" : postura() == REZA ? "posada" : "en tierra") + ", accion " + GargolaAnims.NOMBRES[Math.min(accion(), GargolaAnims.NOMBRES.length - 1)]
                + ", modo " + modo + ", vida " + Math.round(getHealth()) + "/" + Math.round(getMaxHealth());
    }

}
