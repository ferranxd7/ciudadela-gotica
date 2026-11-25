package com.darkcraft.personajes;

import com.darkcraft.dialogo.Conversaciones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Maelis, la guia de los santuarios: sentada en el suelo junto a los santuarios importantes, con un farol de almas en el
 * regazo. No se mueve, no se le puede hacer dano ni empujar, mira a quien tenga cerca y habla con quien la usa (clic
 * derecho o la tecla del aviso, {@link Conversaciones}). Guarda el santuario al que acompana (para subir de nivel con ella).
 */
public class Guia extends Mob {
    private BlockPos santuario;

    public Guia(EntityType<? extends Guia> t, Level l) {
        super(t, l);
        setPersistenceRequired();
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SOUL_LANTERN));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    public static AttributeSupplier.Builder atributos() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.FOLLOW_RANGE, 10);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 9f, 1f));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    /** Sentada: el cuerpo no gira hacia donde mira (solo la cabeza). */
    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                yBodyRot = yBodyRotO = getYRot();
            }
        };
    }

    @Override
    public int getMaxHeadYRot() {
        return 65;
    }

    public BlockPos santuario() {
        return santuario;
    }

    public void santuario(BlockPos p) {
        santuario = p == null ? null : p.immutable();
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
        // de vez en cuando, una mota azul del farol
        if (level().isClientSide && random.nextInt(9) == 0) {
            double a = Math.toRadians(yBodyRot);
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() - Math.sin(a) * 0.45 + (random.nextDouble() - 0.5) * 0.1,
                    getY() + 0.4, getZ() + Math.cos(a) * 0.45 + (random.nextDouble() - 0.5) * 0.1, 0, 0.012, 0);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player p, InteractionHand mano) {
        if (mano != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (p instanceof ServerPlayer sp) Conversaciones.hablar(sp, this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    // ------------------------------------------------------------------ ni dano, ni empujones, ni irse

    @Override
    public boolean isInvulnerableTo(DamageSource s) {
        return !s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity e) {
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
    public boolean canBeLeashed(Player p) {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int looting, boolean jugador) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        if (santuario != null) t.put("santuario", NbtUtils.writeBlockPos(santuario));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        if (t.contains("santuario")) santuario = NbtUtils.readBlockPos(t.getCompound("santuario"));
    }

    /** Pone a la guia sentada en {@code pos}, mirando hacia {@code mira} (el santuario). */
    public static Guia poner(ServerLevel lvl, BlockPos pos, BlockPos mira, BlockPos santuario) {
        Guia g = com.darkcraft.DarkCraftMod.GUIA.get().create(lvl);
        if (g == null) return null;
        float giro = (float) (Math.atan2(mira.getZ() - pos.getZ(), mira.getX() - pos.getX()) * 180 / Math.PI) - 90f;
        g.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, giro, 0f);
        g.setYHeadRot(giro);
        g.setYBodyRot(giro);
        g.santuario(santuario);
        g.addTag("darkcraft_guia");
        lvl.addFreshEntity(g);
        return g;
    }
}
