package com.darkcraft.personajes;

import com.darkcraft.dialogo.Conversaciones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Lo comun a los personajes con los que se habla (Maelis, Dorvan, Kerrick...): estan quietos en su sitio, no reciben dano
 * (salvo lo que salta la invulnerabilidad, como /kill), no se empujan ni se los lleva el agua, no se van, miran a quien
 * tengan cerca (solo giran la cabeza: el cuerpo se queda como se puso) y hablan con quien los usa (clic derecho o la tecla
 * del aviso, {@link Conversaciones}). Cada uno dice con que dialogo habla ({@link #dialogo()}).
 */
public abstract class PersonajeBase extends Mob {
    protected PersonajeBase(EntityType<? extends PersonajeBase> t, Level l) {
        super(t, l);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder atributos() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.FOLLOW_RANGE, 10);
    }

    /** El id de su dialogo ({@code dialogo.Dialogos}: "guia", "herrero", "mercader"). */
    public abstract String dialogo();

    /** Hasta donde mira a los jugadores (bloques). */
    protected float vista() {
        return 9f;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, vista(), 1f));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    /** Quieto: el cuerpo no gira hacia donde mira (solo la cabeza). */
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

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
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
    protected void doPush(Entity e) {
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

    /**
     * Pone un personaje en {@code pos} (centrado en el bloque), mirando hacia {@code mira}, con su etiqueta (para
     * encontrarlo y para que {@code world.SinMonstruos} no lo quite).
     */
    protected static <T extends PersonajeBase> T poner(EntityType<T> tipo, ServerLevel lvl, BlockPos pos, BlockPos mira, String etiqueta,
                                                       java.util.function.Consumer<T> antes) {
        T e = tipo.create(lvl);
        if (e == null) return null;
        float giro = mira == null ? 0f : (float) (Math.atan2(mira.getZ() - pos.getZ(), mira.getX() - pos.getX()) * 180 / Math.PI) - 90f;
        e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, giro, 0f);
        e.setYHeadRot(giro);
        e.setYBodyRot(giro);
        e.addTag(etiqueta);
        if (antes != null) antes.accept(e);
        lvl.addFreshEntity(e);
        return e;
    }
}
