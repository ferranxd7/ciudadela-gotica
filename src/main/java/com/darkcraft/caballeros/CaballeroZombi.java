package com.darkcraft.caballeros;

import com.darkcraft.DarkCraftMod;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * Los caballeros zombi del Reino de Valdrenn: muertos con la armadura oxidada de la guardia del Rey. Mas duros que un
 * zombi (40 de vida, armadura, 2-3 corazones por golpe), no se queman al sol ni se ahogan, gruñen dentro del casco y
 * hacen ruido de armadura al andar. Tres tipos: espada y escudo (se cubre), alabarda (llega mas lejos) y ballesta.
 */
public abstract class CaballeroZombi extends Zombie {
    /** Para el cliente: cubriendose con el escudo o cargando la ballesta. */
    static final EntityDataAccessor<Boolean> CARGANDO = SynchedEntityData.defineId(CaballeroZombi.class, EntityDataSerializers.BOOLEAN);

    protected CaballeroZombi(EntityType<? extends Zombie> t, Level l) {
        super(t, l);
        xpReward = 12;
    }

    public static AttributeSupplier.Builder atributos() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ARMOR, 8.0).add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.MOVEMENT_SPEED, 0.235).add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    /** Clave del bestiario y de la textura. */
    public abstract String tipo();

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CARGANDO, false);
    }

    public boolean cargando() {
        return entityData.get(CARGANDO);
    }

    @Override
    protected void addBehaviourGoals() {
        goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, CaballeroZombi.class).setAlertOthers(CaballeroZombi.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** A veces dejan una piedra de forja (para Dorvan). */
    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.07f + 0.02f * saqueo) spawnAtLocation(com.darkcraft.DarkCraftMod.PIEDRA_FORJA_1.get());
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean b) {
    }

    @Override
    public boolean canBreakDoors() {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor lvl, DifficultyInstance dif, MobSpawnType tipo, SpawnGroupData datos,
                                        net.minecraft.nbt.CompoundTag tag) {
        SpawnGroupData d = super.finalizeSpawn(lvl, dif, tipo, new ZombieGroupData(false, false), tag);
        var refuerzos = getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (refuerzos != null) {
            refuerzos.removeModifiers();
            refuerzos.setBaseValue(0);
        }
        setCanPickUpLoot(false);
        equipar(lvl.getRandom());
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0.03f);
        return d;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource r, DifficultyInstance dif) {
        equipar(r);
    }

    /** Lo que lleva en las manos cada tipo. */
    protected abstract void equipar(RandomSource r);

    // ------------------------------------------------------------------ sonidos

    @Override
    protected SoundEvent getAmbientSound() {
        return DarkCraftMod.SFX_CABALLERO_GRUNIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CABALLERO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CABALLERO_MUERTE.get();
    }

    @Override
    protected SoundEvent getStepSound() {
        return DarkCraftMod.SFX_ARMADURA_PASO.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState st) {
        if (tickCount % 2 == 0) playSound(getStepSound(), 0.32f, 0.85f + random.nextFloat() * 0.3f);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    // ------------------------------------------------------------------ los tres tipos

    /** Espada y escudo: se cubre cuando le atacan de frente. */
    public static class Espada extends CaballeroZombi {
        private int cubierto, espera;

        public Espada(EntityType<? extends Zombie> t, Level l) {
            super(t, l);
        }

        @Override
        public String tipo() {
            return "caballero_espada";
        }

        @Override
        protected void equipar(RandomSource r) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            setItemSlot(EquipmentSlot.OFFHAND, Caballeros.escudo());
        }

        @Override
        public void aiStep() {
            super.aiStep();
            if (level().isClientSide()) return;
            if (cubierto > 0) {
                if (--cubierto == 0) {
                    stopUsingItem();
                    espera = 30 + random.nextInt(40);
                }
            } else if (espera > 0) espera--;
            LivingEntity t = getTarget();
            if (cubierto == 0 && espera == 0 && t != null && t.distanceToSqr(this) < 9 && t.swinging && random.nextFloat() < 0.5f
                    && getOffhandItem().is(Items.SHIELD)) {
                startUsingItem(InteractionHand.OFF_HAND);
                cubierto = 22 + random.nextInt(18);
            }
            entityData.set(CARGANDO, isUsingItem());
        }

        @Override
        public boolean doHurtTarget(Entity e) {
            if (isUsingItem()) return false;
            return super.doHurtTarget(e);
        }
    }

    /** Alabarda: golpea desde mas lejos y mas fuerte, pero mas despacio. */
    public static class Alabarda extends CaballeroZombi {
        public Alabarda(EntityType<? extends Zombie> t, Level l) {
            super(t, l);
        }

        @Override
        public String tipo() {
            return "caballero_alabarda";
        }

        @Override
        protected void equipar(RandomSource r) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(DarkCraftMod.ALABARDA.get()));
        }

        @Override
        public boolean isWithinMeleeAttackRange(LivingEntity t) {
            double alcance = getBbWidth() * 2.0 + 1.6;
            return distanceToSqr(t) <= alcance * alcance + t.getBbWidth();
        }

        @Override
        public boolean doHurtTarget(Entity e) {
            boolean ok = super.doHurtTarget(e);
            playSound(DarkCraftMod.SFX_ALABARDA_TAJO.get(), 0.9f, 0.9f + random.nextFloat() * 0.2f);
            return ok;
        }
    }

    /** Ballesta: dispara desde lejos (como los saqueadores). */
    public static class Ballesta extends CaballeroZombi implements CrossbowAttackMob {
        public Ballesta(EntityType<? extends Zombie> t, Level l) {
            super(t, l);
        }

        @Override
        public String tipo() {
            return "caballero_ballesta";
        }

        @Override
        protected void addBehaviourGoals() {
            goalSelector.addGoal(2, new RangedCrossbowAttackGoal<>(this, 1.0, 9.0f));
            goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 1.0));
            goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
            targetSelector.addGoal(1, new HurtByTargetGoal(this, CaballeroZombi.class).setAlertOthers(CaballeroZombi.class));
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }

        @Override
        protected void equipar(RandomSource r) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        }

        @Override
        public void setChargingCrossbow(boolean b) {
            entityData.set(CARGANDO, b);
        }

        @Override
        public void shootCrossbowProjectile(LivingEntity t, ItemStack ballesta, Projectile p, float ang) {
            shootCrossbowProjectile(this, t, p, ang, 1.6f);
        }

        @Override
        public void onCrossbowAttackPerformed() {
            noActionTime = 0;
        }

        @Override
        public void performRangedAttack(LivingEntity t, float f) {
            performCrossbowAttack(this, 1.6f);
        }

        @Override
        public boolean canFireProjectileWeapon(ProjectileWeaponItem w) {
            return w == Items.CROSSBOW;
        }
    }

    /** Hace sonar el golpe en el escudo (lo llama Caballeros al parar un golpe). */
    void sonarEscudo() {
        if (level() instanceof ServerLevel sl)
            sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_ESCUDO_GOLPE.get(), SoundSource.HOSTILE, 1.0f, 0.9f + random.nextFloat() * 0.2f);
    }
}
