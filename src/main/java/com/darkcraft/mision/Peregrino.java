package com.darkcraft.mision;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Un peregrino muerto (parte 7ag): cuando Oswin vuelve a tener su campana y la toca, los que no podian pasar echan a andar hacia el
 * norte por el camino de los peregrinos, rezando, medio transparentes, y se desvanecen ({@link Oswin#procesion}). No se guarda, no
 * se le puede pegar ni empujar, no choca con nada: anda por encima del suelo ({@link #DURA} ticks) y se va.
 */
public class Peregrino extends Mob {
    /** Lo que dura (ticks): aparece en 30 y se va apagando los ultimos 50. */
    public static final int DURA = 250;
    /** Lo que anda por tick (bloques). */
    public static final double PASO = 0.055;

    private static final EntityDataAccessor<Float> RUMBO = SynchedEntityData.defineId(Peregrino.class, EntityDataSerializers.FLOAT);

    public Peregrino(EntityType<? extends Peregrino> t, Level l) {
        super(t, l);
        noPhysics = true;
        setNoGravity(true);
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, 0.1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RUMBO, 180f);
    }

    /** Hacia donde anda (grados de los de Minecraft: 180 el norte). */
    public void rumbo(float r) {
        entityData.set(RUMBO, r);
        setYRot(r);
        setYBodyRot(r);
        setYHeadRot(r);
    }

    public float rumbo() {
        return entityData.get(RUMBO);
    }

    /** Lo que se le ve (0 a 1), segun lo que lleva andado. */
    public float alfa(float pt) {
        float t = tickCount + pt;
        float entra = Mth.clamp(t / 30f, 0f, 1f), sale = Mth.clamp((DURA - t) / 50f, 0f, 1f);
        return 0.55f * Math.min(entra, sale);
    }

    @Override
    public void tick() {
        super.tick();
        float r = rumbo();
        setYRot(r);
        yBodyRot = r;
        yHeadRot = r;
        if (level().isClientSide) return;
        if (tickCount > DURA) {
            discard();
            return;
        }
        double a = Math.toRadians(r);
        double x = getX() - Math.sin(a) * PASO, z = getZ() + Math.cos(a) * PASO;
        // por encima del suelo de cada sitio (sube y baja con el terreno, despacio)
        int suelo = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
        double y = getY() + Mth.clamp(suelo - getY(), -0.12, 0.12);
        setPos(x, y, z);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean hurt(DamageSource s, float d) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity e) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int looting, boolean jugador) {
    }
}
