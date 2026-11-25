package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lo comun de los dos guardianes de Valdrenn (parte 4d; respuesta 15 del plan: "un mini-jefe guardian antes de CADA jefe,
 * con barra de vida y logro"): Ser Brandt ante el salon del trono y Vareth en la torre del arbol.
 * <ul>
 * <li>esperan de rodillas en su puesto; la pelea empieza cuando ven a alguien (se levantan y rugen);</li>
 * <li>su <b>barra de vida</b> (la dibuja el cliente con su arte: {@code client/enemigos/GuardianCliente}) y su
 * <b>musica</b> mientras pelean; con amigos, mas vida;</li>
 * <li>a <b>media vida</b>, una vez por pelea, rugen y cambian (sus golpes nuevos, mas rapidos);</li>
 * <li>si todos caen o se van, vuelven a su puesto con la vida entera; al caer, a cada uno de los que pelearon su cartel de
 * JEFE ABATIDO con sus ecos, su logro (hazana) y lo que dejan (lo reparte {@code valdrenn/Guardianes}).</li>
 * </ul>
 * Los guardianes estan en el grupo de los jefes de {@link Puestos}: caen una vez y no vuelven.
 */
public abstract class Guardian extends Enemigo {
    private static final EntityDataAccessor<Boolean> EN_PELEA = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.BOOLEAN);

    protected final ServerBossEvent barra;
    protected boolean segunda;
    private boolean rugio;
    private int sinPresa;
    /** Los que le han plantado cara en esta pelea (para repartir lo que deja). */
    private final Set<UUID> rivales = new HashSet<>();

    protected Guardian(EntityType<? extends Guardian> t, Level l, String nombre, BossEvent.BossBarColor color) {
        super(t, l);
        xpReward = 0;
        barra = new ServerBossEvent(Component.translatable("entity.darkcraft." + nombre), color, BossEvent.BossBarOverlay.PROGRESS);
        barra.setVisible(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EN_PELEA, false);
    }

    /** Si esta peleando (lo sabe tambien el cliente: la musica). */
    public boolean enPelea() {
        return entityData.get(EN_PELEA);
    }

    public Set<UUID> rivales() {
        return rivales;
    }

    /** Si ya ha pasado de media vida en esta pelea (pruebas). */
    public boolean segunda() {
        return segunda;
    }

    /** El tema de musica de su pelea (los de {@code ClientMusic.boss}). */
    public abstract int musica();

    /** La accion con la que ruge (al empezar y a media vida). */
    protected abstract int accionRugido();

    /** Los ecos que deja a cada uno de los que le vencen. */
    public abstract int ecos();

    /** El logro de vencerle. */
    public abstract String logro();

    /** Lo que hace al pasar de media vida (ademas del rugido). */
    protected void alSegundaMitad() {
    }

    @Override
    protected double correa() {
        return 40.0;
    }

    /** De rodillas en su puesto repara en quien entra en su sala (a 16 bloques, si lo ve), no solo en quien se le acerca. */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (postura() != DE_PIE) return distanceToSqr(p) < 16 * 16 && hasLineOfSight(p);
        return super.percibe(p);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t == null || level().isClientSide || !isAlive()) return;
        if (t instanceof ServerPlayer p) rivales.add(p.getUUID());
        if (!enPelea()) empezarPelea();
    }

    private void empezarPelea() {
        entityData.set(EN_PELEA, true);
        segunda = false;
        sinPresa = 0;
        int cerca = 0;
        if (level() instanceof ServerLevel sl) for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 48 * 48) cerca++;
        paraGrupo(cerca);
        DarkCraftMod.LOGGER.info("[DarkCraft] {}: empieza la pelea ({} jugadores, {} de vida)", tipo(), cerca, Math.round(getMaxHealth()));
    }

    /** Todos han caido o se han ido: vuelve a lo suyo con la vida entera. */
    private void acabarPelea() {
        entityData.set(EN_PELEA, false);
        segunda = rugio = false;
        rivales.clear();
        setHealth(getMaxHealth());
        barra.removeAllPlayers();
        DarkCraftMod.LOGGER.info("[DarkCraft] {} vuelve a su puesto", tipo());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel sl)) return;
        barra.setProgress(Math.max(0f, getHealth() / getMaxHealth()));
        if (!isAlive()) return;
        if (enPelea()) {
            // en cuanto esta en pie, el rugido (una vez por pelea)
            if (!rugio && postura() == DE_PIE && accion() == 0) {
                rugio = true;
                empezar(accionRugido());
                pausa = Math.max(pausa, 10);
            }
            if (getTarget() == null) {
                if (++sinPresa > 110) acabarPelea();
            } else sinPresa = 0;
            // a media vida (una vez por pelea): ruge y cambia
            if (!segunda && getHealth() < getMaxHealth() * 0.5f && accion() == 0 && !aturdido()) {
                segunda = true;
                empezar(accionRugido());
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() * 0.6, getZ(), 30, 0.6, 0.8, 0.6, 0.03);
                alSegundaMitad();
            }
        }
        if (tickCount % 10 == 0) {
            for (ServerPlayer p : sl.players()) {
                boolean cerca = enPelea() && p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 48 * 48;
                if (cerca) barra.addPlayer(p);
                else barra.removePlayer(p);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // ni las caidas ni los golpes de los demas enemigos
        if (s.is(net.minecraft.tags.DamageTypeTags.IS_FALL) || s.getEntity() instanceof Enemigo) return false;
        boolean ok = super.hurt(s, cuanto);
        if (ok && s.getEntity() instanceof ServerPlayer p) rivales.add(p.getUUID());
        return ok;
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        entityData.set(EN_PELEA, false);
        if (s.getEntity() instanceof ServerPlayer p) rivales.add(p.getUUID());
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.4, getZ(), 40, 0.6, 1.0, 0.6, 0.04);
    }

    @Override
    public void remove(RemovalReason r) {
        barra.removeAllPlayers();
        super.remove(r);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        barra.removePlayer(p);
    }

    @Override
    protected float getSoundVolume() {
        return 1.6f;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    protected int accionDespertar(int postura) {
        return accionLevantarse();
    }

    /** Su animacion de ponerse en pie (estaba de rodillas en su puesto). */
    protected abstract int accionLevantarse();
}
