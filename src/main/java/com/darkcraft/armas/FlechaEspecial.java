package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Congelacion;
import com.darkcraft.enemigos.Sangrado;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

/**
 * Las flechas de fuego, de sangre y de escarcha (parte 7aj, respuesta 109 del plan): una flecha del juego (la disparan el arco y la
 * ballesta de siempre, `FlechaItem`) que lleva su clase sincronizada (el cliente la dibuja con su textura y deja su rastro):
 * <ul>
 * <li><b>fuego</b>: vuela ardiendo y prende a quien da (6 s);</li>
 * <li><b>sangre</b>: suma {@link #SUMA} de sangrado ({@link Sangrado}: a la tercera, hemorragia);</li>
 * <li><b>escarcha</b>: suma {@link #SUMA} de congelacion ({@link Congelacion}: a la tercera, se congela).</li>
 * </ul>
 * Se recogen del suelo como las de siempre (las que se clavan en alguien se pierden).
 */
public class FlechaEspecial extends AbstractArrow {
    public enum Clase {
        FUEGO("fuego"), SANGRE("sangre"), ESCARCHA("escarcha");

        public final String id;

        Clase(String id) {
            this.id = id;
        }

        public static Clase de(int i) {
            Clase[] v = values();
            return v[Math.floorMod(i, v.length)];
        }
    }

    /** Lo que suma cada flecha de sangre o de escarcha (de los 100 que hacen falta). */
    public static final float SUMA = 34f;
    /** Lo que arde quien recibe una flecha de fuego (segundos). */
    public static final int ARDE = 6;
    private static final EntityDataAccessor<Byte> CLASE = SynchedEntityData.defineId(FlechaEspecial.class, EntityDataSerializers.BYTE);

    public FlechaEspecial(EntityType<? extends FlechaEspecial> t, Level l) {
        super(t, l);
    }

    public FlechaEspecial(Level l, LivingEntity tirador, Clase c) {
        super(DarkCraftMod.FLECHA_ESPECIAL.get(), tirador, l);
        setClase(c);
        // la de fuego va ardiendo (la llama del juego alrededor de la flecha; al darle a alguien le prende)
        if (c == Clase.FUEGO) setSecondsOnFire(100);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CLASE, (byte) 0);
    }

    public Clase clase() {
        return Clase.de(entityData.get(CLASE));
    }

    public void setClase(Clase c) {
        entityData.set(CLASE, (byte) c.ordinal());
    }

    @Override
    public ItemStack getPickupItem() {
        RegistryObject<net.minecraft.world.item.Item> it = DarkCraftMod.FLECHAS.get(clase());
        return it == null ? ItemStack.EMPTY : new ItemStack(it.get());
    }

    /** Lo de cada clase al dar a alguien (solo en el servidor: el juego lo llama si el golpe ha entrado). */
    @Override
    protected void doPostHurtEffects(LivingEntity le) {
        super.doPostHurtEffects(le);
        Entity de = getOwner();
        LivingEntity causa = de instanceof LivingEntity l ? l : null;
        double y = le.getY() + le.getBbHeight() * 0.6;
        switch (clase()) {
            case FUEGO -> {
                if (!le.fireImmune()) le.setSecondsOnFire(ARDE);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, le.getX(), y, le.getZ(), 14, 0.25, 0.35, 0.25, 0.04);
                    sl.sendParticles(ParticleTypes.LAVA, le.getX(), y, le.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
                    sl.playSound(null, le.getX(), y, le.getZ(), DarkCraftMod.SFX_FLECHA_FUEGO.get(), SoundSource.PLAYERS, 0.9f,
                            0.95f + random.nextFloat() * 0.1f);
                }
            }
            case SANGRE -> Sangrado.sumar(le, SUMA, causa);
            case ESCARCHA -> Congelacion.sumar(le, SUMA, causa);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) return;
        // el rastro de cada clase (en el aire, siempre; clavada, de vez en cuando)
        if (inGround && tickCount % 6 != 0) return;
        double x = getX(), y = getY(), z = getZ();
        switch (clase()) {
            case FUEGO -> {
                level().addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0, 0.01, 0);
                if (!inGround && tickCount % 2 == 0) level().addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01, 0);
            }
            case SANGRE -> level().addParticle(Sangrado.GOTA, x, y, z, 0, 0, 0);
            case ESCARCHA -> {
                level().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, -0.01, 0);
                if (!inGround && tickCount % 2 == 0) level().addParticle(Congelacion.ESCARCHA, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putByte("Clase", (byte) clase().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        setClase(Clase.de(t.getByte("Clase")));
    }
}
