package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Los eruditos de la Academia Espectral (parte 7o): los que no dejaron la leccion la noche del cielo rojo. Flotan como los
 * espectros (ni caen con dano ni hacen ruido al moverse) y se dibujan medio transparentes. Esperan leyendo (la postura de
 * rezar) o caidos entre los libros.
 * <ul>
 * <li>{@link #APRENDIZ}: el **plumazo** (dos punaladas con el calamo) y el **tintero**: lo lanza en arco ({@link Tinta}) y
 * la tinta ciega y frena.</li>
 * <li>{@link #MAESTRO}: se queda lejos. El **orbe** (una bola de luz que sigue despacio a su presa, {@link Orbe}), las
 * **runas** (un circulo que se enciende bajo los pies de su presa y revienta poco despues: hay que salir) y el
 * **parpadeo** (si se le acercan, se apaga y aparece mas lejos).</li>
 * <li>{@link #CUSTODIO}: el guardian de la biblioteca, mas grande y duro. El **tomazo** (estrella el tomo: a quien este de
 * pie cerca le llega la onda) y el **cadenazo** (voltea el tomo con la cadena: llega lejos y frena).</li>
 * </ul>
 */
public class Erudito extends Enemigo {
    public static final int APRENDIZ = 0, MAESTRO = 1, CUSTODIO = 2;
    /** Lo que tardan en reventar las runas desde que se encienden, y su radio. */
    public static final int RUNAS_TICKS = 22;
    public static final double R_RUNAS = 1.8, R_ONDA = 2.8;
    /** Para las pruebas: lo que han hecho todos. */
    public static int TINTEROS, ORBES, RUNAS, RUNAS_DAN, PARPADEOS, TOMAZOS, ONDAS, CADENAZOS;

    static final DustParticleOptions VIOLETA = new DustParticleOptions(new Vector3f(0.62f, 0.45f, 1.0f), 1.0f);
    static final DustParticleOptions TINTA = new DustParticleOptions(new Vector3f(0.03f, 0.03f, 0.08f), 1.4f);

    private int tinteroEspera = 30, runasEspera = 50, parpadeoEspera = 40, tomazoEspera = 0;
    /** Los circulos de runas encendidos: x, y, z, tick en que revientan. */
    private final List<double[]> runas = new ArrayList<>();

    public Erudito(EntityType<? extends Erudito> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 28.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case MAESTRO -> "erudito_maestro";
            case CUSTODIO -> "erudito_custodio";
            default -> "erudito_aprendiz";
        };
    }

    /** Cada uno, su vida y su golpe: el aprendiz es fragil; el custodio, duro. */
    @Override
    public void variante(int v) {
        super.variante(v);
        set(Attributes.MAX_HEALTH, v == CUSTODIO ? 70 : v == MAESTRO ? 30 : 26);
        set(Attributes.ATTACK_DAMAGE, v == CUSTODIO ? 7.0 : v == MAESTRO ? 5.0 : 4.5);
        set(Attributes.ARMOR, v == CUSTODIO ? 4.0 : 0.0);
        set(Attributes.KNOCKBACK_RESISTANCE, v == CUSTODIO ? 0.85 : 0.45);
        setHealth(getMaxHealth());
    }

    private void set(net.minecraft.world.entity.ai.attributes.Attribute a, double valor) {
        var i = getAttribute(a);
        if (i != null) i.setBaseValue(valor);
    }

    @Override
    protected int[] ticks() {
        return EruditoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return EruditoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return EruditoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return EruditoAnims.MORIR;
    }

    /** Caido entre los libros: se levanta; leyendo: se alza. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? EruditoAnims.LEVANTARSE : EruditoAnims.ALZARSE;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 3.0) return 0;
        switch (variante()) {
            case MAESTRO -> {
                if (d < 3.2 && parpadeoEspera <= 0) return EruditoAnims.PARPADEO;
                if (runasEspera <= 0 && d < 14 && random.nextFloat() < 0.45f) {
                    runasEspera = 90 + random.nextInt(60);
                    return EruditoAnims.RUNAS;
                }
                if (d < 18 && hasLineOfSight(presa)) return EruditoAnims.ORBE;
                return 0;
            }
            case CUSTODIO -> {
                if (d <= 3.2 && tomazoEspera <= 0 && random.nextFloat() < 0.45f) {
                    tomazoEspera = 50 + random.nextInt(40);
                    return EruditoAnims.TOMAZO;
                }
                if (d <= 4.6) return EruditoAnims.CADENAZO;
                return 0;
            }
            default -> {
                if (d >= 4.0 && d <= 12.0 && tinteroEspera <= 0 && hasLineOfSight(presa) && random.nextFloat() < 0.5f) {
                    tinteroEspera = 100 + random.nextInt(60);
                    return EruditoAnims.TINTERO;
                }
                if (d <= 2.6) return EruditoAnims.PLUMAZO;
                return 0;
            }
        }
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case EruditoAnims.PLUMAZO -> new Golpe(2.6f, 60f, 0.9f, 0.3f);
            case EruditoAnims.TOMAZO -> new Golpe(3.2f, 70f, 1.6f, 0.9f);
            case EruditoAnims.CADENAZO -> new Golpe(4.8f, 200f, 1.15f, 1.1f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        super.alGolpear(le, accion);
        if (accion == EruditoAnims.CADENAZO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = EruditoAnims.GOLPES[accion];
        switch (accion) {
            case EruditoAnims.PLUMAZO -> {
                if (t == 4) playSound(DarkCraftMod.SFX_ERUDITO_PLUMA.get(), 1.2f, 0.95f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1 || t == g[1] - 1) empujar(0.35);
            }
            case EruditoAnims.TINTERO -> {
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 0.8f, 1.2f);
                if (t == g[0]) lanzarTinta();
            }
            case EruditoAnims.ORBE -> {
                if (t == 3) playSound(DarkCraftMod.SFX_ERUDITO_ORBE.get(), 1.4f, 0.95f + random.nextFloat() * 0.1f);
                if (t > 4 && t < g[0] && level() instanceof ServerLevel sl && t % 2 == 0) {
                    Vec3 p = punta();
                    sl.sendParticles(VIOLETA, p.x, p.y, p.z, 3, 0.12, 0.12, 0.12, 0.0);
                    sl.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, 4, 0.3, 0.3, 0.3, 0.4);
                }
                if (t == g[0]) lanzarOrbe();
            }
            case EruditoAnims.RUNAS -> {
                if (t == g[0]) encenderRunas();
            }
            case EruditoAnims.PARPADEO -> {
                if (t == 2) playSound(DarkCraftMod.SFX_ERUDITO_PARPADEO.get(), 1.0f, 1.0f);
                if (t == 10) apartarse();
            }
            case EruditoAnims.TOMAZO -> {
                if (t == g[0] - 8) playSound(DarkCraftMod.SFX_ERUDITO_CADENA.get(), 0.9f, 0.8f);
                if (t == g[0] - 1) empujar(0.25);
                if (t == g[0]) {
                    TOMAZOS++;
                    playSound(DarkCraftMod.SFX_ERUDITO_TOMAZO.get(), 1.8f, 0.9f + random.nextFloat() * 0.1f);
                    onda();
                }
            }
            case EruditoAnims.CADENAZO -> {
                if (t == g[0]) CADENAZOS++;
            }
            default -> {
            }
        }
    }

    /** Donde esta la punta de la vara (o la mano): delante y un poco arriba. */
    private Vec3 punta() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + d.x * 0.8, getY() + 1.5, getZ() + d.z * 0.8);
    }

    /** El tintero, en arco hacia su presa (cae donde estaba al soltarlo). */
    private void lanzarTinta() {
        LivingEntity p = getTarget();
        if (p == null || !(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.5 - d.z * 0.3, oy = getY() + 1.7, oz = getZ() + d.z * 0.5 + d.x * 0.3;
        Tinta t = new Tinta(sl, this);
        t.setPos(ox, oy, oz);
        t.setDeltaMovement(Cantaro.tiro(ox, oy, oz, p.getX(), p.getY() + 0.2, p.getZ()));
        sl.addFreshEntity(t);
        TINTEROS++;
    }

    /** El orbe: sale de la vara hacia su presa y la sigue despacio. */
    private void lanzarOrbe() {
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 o = punta();
        Vec3 dir = p != null ? p.getEyePosition().subtract(o).normalize() : Vec3.directionFromRotation(getXRot(), getYRot());
        Orbe b = new Orbe(sl, this, p, (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.1f);
        b.setPos(o.x, o.y, o.z);
        b.setDeltaMovement(dir.scale(Orbe.VELOCIDAD));
        sl.addFreshEntity(b);
        sl.sendParticles(VIOLETA, o.x, o.y, o.z, 10, 0.2, 0.2, 0.2, 0.0);
        ORBES++;
    }

    /** El circulo de runas bajo los pies de su presa (donde pisa): revienta a los {@link #RUNAS_TICKS}. */
    private void encenderRunas() {
        LivingEntity p = getTarget();
        if (p == null || !(level() instanceof ServerLevel sl)) return;
        double y = Puestos.suelo(sl, p.getBlockX(), p.getBlockY() + 1, p.getBlockZ());
        if (Math.abs(y - p.getY()) > 3) y = p.getY();
        runas.add(new double[]{p.getX(), y, p.getZ(), tickCount + RUNAS_TICKS});
        sl.playSound(null, p.getX(), y, p.getZ(), DarkCraftMod.SFX_ERUDITO_RUNAS.get(), SoundSource.HOSTILE, 1.4f, 1.0f);
        RUNAS++;
    }

    /** Cada tick: los circulos se dibujan (un anillo de chispas lilas y letras) y al llegar su hora revientan. */
    private void tickRunas(ServerLevel sl) {
        Iterator<double[]> it = runas.iterator();
        while (it.hasNext()) {
            double[] r = it.next();
            int queda = (int) r[3] - tickCount;
            if (queda > 0) {
                if (queda % 2 == 0)
                    for (int k = 0; k < 12; k++) {
                        double a = k * Math.PI * 2 / 12 + tickCount * 0.12;
                        sl.sendParticles(VIOLETA, r[0] + Math.cos(a) * R_RUNAS, r[1] + 0.1, r[2] + Math.sin(a) * R_RUNAS, 1, 0.02, 0.0, 0.02, 0.0);
                    }
                if (queda % 4 == 0) sl.sendParticles(ParticleTypes.ENCHANT, r[0], r[1] + 0.6, r[2], 10, R_RUNAS * 0.6, 0.2, R_RUNAS * 0.6, 0.6);
                continue;
            }
            it.remove();
            sl.sendParticles(ParticleTypes.END_ROD, r[0], r[1] + 0.3, r[2], 30, R_RUNAS * 0.5, 0.4, R_RUNAS * 0.5, 0.08);
            sl.sendParticles(ParticleTypes.WITCH, r[0], r[1] + 0.8, r[2], 20, R_RUNAS * 0.5, 0.6, R_RUNAS * 0.5, 0.05);
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
            for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(r[0] - R_RUNAS, r[1] - 0.5, r[2] - R_RUNAS, r[0] + R_RUNAS, r[1] + 2.5, r[2] + R_RUNAS),
                    q -> objetivoValido(q) && q.isAlive())) {
                if (Math.hypot(p.getX() - r[0], p.getZ() - r[2]) > R_RUNAS + 0.3) continue;
                if (p.hurt(damageSources().indirectMagic(this, this), dano)) {
                    p.setDeltaMovement(p.getDeltaMovement().add(0, 0.45, 0));
                    p.hurtMarked = true;
                    RUNAS_DAN++;
                }
            }
        }
    }

    /** El parpadeo: a 6-9 bloques, lejos de su presa (de espaldas a ella), donde quepa y haya suelo. */
    private void apartarse() {
        parpadeoEspera = 70 + random.nextInt(50);
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 desde = p != null ? position().subtract(p.position()) : Vec3.directionFromRotation(0, getYRot()).scale(-1);
        double base = Math.atan2(desde.z, desde.x);
        for (int k = 0; k < 12; k++) {
            double ang = base + (random.nextDouble() - 0.5) * Math.PI * 0.9;
            double r = 6.0 + random.nextDouble() * 3.0;
            double x = getX() + Math.cos(ang) * r, z = getZ() + Math.sin(ang) * r;
            BlockPos b = BlockPos.containing(x, getY() + 0.5, z);
            double y = Puestos.suelo(sl, b.getX(), b.getY() + 1, b.getZ());
            if (Math.abs(y - getY()) > 2.5) continue;
            Vec3 antes = position();
            setPos(x, y, z);
            if (!sl.noCollision(this) || sl.noCollision(this, getBoundingBox().move(0, -1.2, 0))) {
                setPos(antes.x, antes.y, antes.z);
                continue;
            }
            sl.sendParticles(VIOLETA, antes.x, antes.y + 1.0, antes.z, 16, 0.3, 0.6, 0.3, 0.0);
            teleportTo(x, y, z);
            if (p != null) girarHacia(p, 180f);
            sl.sendParticles(VIOLETA, x, y + 1.0, z, 16, 0.3, 0.6, 0.3, 0.0);
            playSound(DarkCraftMod.SFX_ERUDITO_PARPADEO.get(), 0.9f, 1.25f);
            PARPADEOS++;
            return;
        }
    }

    /** La onda del tomazo: a quien este de pie cerca de donde da el tomo. */
    private void onda() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * 2.4, z = getZ() + d.z * 2.4;
        BlockState s = sl.getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!s.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), x, getY() + 0.2, z, 40, 1.0, 0.1, 1.0, 0.2);
        sl.sendParticles(ParticleTypes.END_ROD, x, getY() + 0.3, z, 16, 0.8, 0.1, 0.8, 0.06);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(x - R_ONDA, getY() - 1, z - R_ONDA, x + R_ONDA, getY() + 2, z + R_ONDA),
                q -> q != this && q.isAlive() && objetivoValido(q))) {
            if (!le.onGround() || Math.hypot(le.getX() - x, le.getZ() - z) > R_ONDA) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.38, 0));
                le.hurtMarked = true;
                ONDAS++;
            }
        }
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (muriendo()) return;
            // las letras que se le escapan al maestro de su libro, y un rastro frio de vez en cuando
            if (variante() == MAESTRO && random.nextInt(5) == 0)
                level().addParticle(ParticleTypes.ENCHANT, getRandomX(0.6), getY() + 1.0 + random.nextDouble() * 0.8, getRandomZ(0.6), 0, 0.2, 0);
            if (random.nextInt(16) == 0)
                level().addParticle(ParticleTypes.SOUL, getRandomX(0.5), getY() + 0.2 + random.nextDouble() * 1.4, getRandomZ(0.5), 0, 0.01, 0);
            return;
        }
        if (tinteroEspera > 0) tinteroEspera--;
        if (runasEspera > 0) runasEspera--;
        if (parpadeoEspera > 0) parpadeoEspera--;
        if (tomazoEspera > 0) tomazoEspera--;
        if (!runas.isEmpty() && level() instanceof ServerLevel sl) tickRunas(sl);
    }

    /** Las runas que quedan por reventar (pruebas). */
    public int runasPendientes() {
        return runas.size();
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        boolean ok = super.hurt(s, cuanto);
        // el maestro, tras un golpe, a veces se aparta
        if (ok && !level().isClientSide && isAlive() && variante() == MAESTRO && accion() == 0 && parpadeoEspera <= 0 && getTarget() != null
                && random.nextFloat() < 0.35f) empezar(EruditoAnims.PARPADEO);
        return ok;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        runas.clear();
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "erudito");
        }
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.2, getZ(), 40, 0.5, 0.8, 0.5, 0.8);
            sl.sendParticles(VIOLETA, getX(), getY() + 1.0, getZ(), 14, 0.4, 0.7, 0.4, 0.0);
        }
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == EruditoAnims.CADENAZO ? DarkCraftMod.SFX_ERUDITO_CADENA.get() : null;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_ERUDITO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case EruditoAnims.ORBE -> 18 + random.nextInt(14);
            case EruditoAnims.RUNAS -> 20 + random.nextInt(12);
            case EruditoAnims.PARPADEO -> 6;
            case EruditoAnims.TINTERO -> 14 + random.nextInt(10);
            case EruditoAnims.TOMAZO -> 16 + random.nextInt(12);
            default -> 8 + random.nextInt(14);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return switch (variante()) {
            case MAESTRO -> 7.5;
            case CUSTODIO -> 2.6;
            default -> 2.0;
        };
    }

    @Override
    protected double prisa() {
        return variante() == CUSTODIO ? 0.85 : 1.0;
    }

    /** Flotan: ni caen con fuerza ni se hacen dano al caer, ni hacen ruido al moverse. */
    @Override
    public boolean causeFallDamage(float distancia, float multiplicador, DamageSource s) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_ERUDITO_SUSURRO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ERUDITO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ERUDITO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (!jugador) return;
        if (random.nextFloat() < (variante() == CUSTODIO ? 0.12f : 0.06f) + 0.02f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (variante() == CUSTODIO && random.nextFloat() < 0.05f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }
}
