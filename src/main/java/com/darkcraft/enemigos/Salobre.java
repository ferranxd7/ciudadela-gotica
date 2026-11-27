package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Los salobres de la Costa de los Huesos (parte 7q): la gente del Puerto Varado. La noche del cielo rojo el mar se retiro de
 * golpe y dejo en seco el puerto, los barcos y las ballenas; bajaron a la arena a recoger lo que el mar dejaba y el mar no
 * volvio. La sal se los fue comiendo (la piel blanca y agrietada, costras y cristales de sal, los ojos con una luz fria de
 * agua) y siguen faenando: remiendan redes de rodillas ({@code REZA}: se levantan al acercarse alguien) o yacen medio
 * enterrados en la costra de sal ({@code TUMBADO}: se alzan de golpe). Andan, no flotan. Tres:
 * <ul>
 * <li>el <b>redero</b>: voltea el <b>plomo</b> de la red en su cuerda y lo descarga; y de lejos lanza la <b>red</b> ({@link Red}):
 * a quien pilla lo deja enredado, casi sin poder moverse;</li>
 * <li>el <b>bichero</b>: clava el <b>garfio</b> de su pertiga (llega lejos) y tira: te arrastra hacia el; y el <b>bichazo</b>, un
 * barrido ancho con la pertiga;</li>
 * <li>el <b>salinero</b> (mas grande y duro): el <b>rastrillazo</b>, que deja en el suelo una costra de sal que frena; y un
 * punado de <b>sal</b> a la cara, que escuece y deja sin aliento (se lleva la estamina).</li>
 * </ul>
 * Al morir caen de rodillas y se deshacen en sal.
 */
public class Salobre extends Enemigo {
    public static final int REDERO = 0, BICHERO = 1, SALINERO = 2;
    /** Lo que esperan entre una red y la siguiente, y entre dos punados de sal (ticks). */
    public static final int ESPERA_RED = 20 * 7, ESPERA_SAL = 20 * 5;
    /** El garfio: hasta donde llega y con que fuerza arrastra. */
    public static final float ALCANCE_GARFIO = 4.8f;
    public static final double TIRON = 0.85;
    /** El punado de sal: hasta donde llega, su abertura (grados) y la estamina que quita. */
    public static final double ALCANCE_SAL = 4.6, ARCO_SAL = 64;
    public static final float ESTAMINA_SAL = 38f;
    /** La costra de sal del rastrillazo: su radio y lo que dura (ticks). */
    public static final double R_COSTRA = 2.3;
    public static final int COSTRA_TICKS = 80;
    public static final DustParticleOptions SAL = new DustParticleOptions(new Vector3f(0.96f, 0.97f, 0.95f), 1.0f),
            SAL_GRUESA = new DustParticleOptions(new Vector3f(0.88f, 0.92f, 0.92f), 1.7f),
            AGUA = new DustParticleOptions(new Vector3f(0.62f, 0.9f, 1f), 0.8f);
    public static final BlockParticleOption COSTRA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CALCITE.defaultBlockState()),
            ARENA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
            POLVO = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WHITE_CONCRETE_POWDER.defaultBlockState());

    private record Costra(double x, double y, double z, int hasta) {}

    private final List<Costra> costras = new ArrayList<>();
    private final Set<Integer> frenados = new HashSet<>();
    private int ultimaRed = -100000, ultimaSal = -100000;

    /**
     * Para la prueba: redes lanzadas, enredados, garfios que muerden, arrastrados (y lo que se acercaron), costras de sal, los que
     * han frenado, punados de sal, los que se han quedado sin aliento, los que se alzan de la sal y los que se deshacen al morir.
     */
    public static int REDES, ENREDADOS, GARFIOS, COSTRAS, FRENADOS, SALES, SALADOS, ALZADOS, DESHECHOS;
    public static double ARRASTRE;

    public Salobre(EntityType<? extends Salobre> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 4.5).add(Attributes.ARMOR, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case BICHERO -> "salobre_bichero";
            case SALINERO -> "salobre_salinero";
            default -> "salobre_redero";
        };
    }

    /** Cada uno, su vida y su golpe: el salinero, grande y duro. */
    @Override
    public void variante(int v) {
        super.variante(v);
        set(Attributes.MAX_HEALTH, v == SALINERO ? 60 : v == BICHERO ? 36 : 30);
        set(Attributes.ATTACK_DAMAGE, v == SALINERO ? 7.5 : v == BICHERO ? 5.5 : 4.5);
        set(Attributes.ARMOR, v == SALINERO ? 3.0 : 1.0);
        set(Attributes.KNOCKBACK_RESISTANCE, v == SALINERO ? 0.8 : 0.4);
        setHealth(getMaxHealth());
    }

    private void set(net.minecraft.world.entity.ai.attributes.Attribute a, double valor) {
        var i = getAttribute(a);
        if (i != null) i.setBaseValue(valor);
    }

    @Override
    protected int[] ticks() {
        return SalobreAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return SalobreAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return SalobreAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return SalobreAnims.MORIR;
    }

    /** Enterrado en la sal: se alza; faenando de rodillas: se levanta. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? SalobreAnims.ALZARSE : SalobreAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 3.0) return 0;
        return switch (variante()) {
            case BICHERO -> {
                if (d < 2.7 && random.nextFloat() < 0.5f) yield SalobreAnims.BICHAZO;
                if (d < ALCANCE_GARFIO - 0.3) yield SalobreAnims.GARFIO;
                yield 0;
            }
            case SALINERO -> {
                if (d > 1.6 && d < ALCANCE_SAL - 0.4 && tickCount - ultimaSal > ESPERA_SAL && random.nextFloat() < 0.45f) yield SalobreAnims.SAL;
                if (d < 3.1) yield SalobreAnims.RASTRILLAZO;
                yield d < 4.2 && random.nextFloat() < 0.25f ? SalobreAnims.RASTRILLAZO : 0;
            }
            default -> {
                if (d > 3.6 && d < 11 && tickCount - ultimaRed > ESPERA_RED && hasLineOfSight(presa)) yield SalobreAnims.RED;
                if (d < 2.9) yield SalobreAnims.PLOMADA;
                yield d < 3.5 && random.nextFloat() < 0.3f ? SalobreAnims.PLOMADA : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case SalobreAnims.PLOMADA -> new Golpe(3.0f, 80f, 1.0f, 0.65f);
            case SalobreAnims.GARFIO -> new Golpe(ALCANCE_GARFIO, 26f, 0.9f, 0f);
            case SalobreAnims.BICHAZO -> new Golpe(3.6f, 170f, 0.85f, 0.7f);
            case SalobreAnims.RASTRILLAZO -> new Golpe(3.0f, 70f, 1.5f, 0.6f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = SalobreAnims.GOLPES[accion];
        int golpe = g.length > 0 ? g[0] : -1;
        switch (accion) {
            case SalobreAnims.PLOMADA -> {
                if (t == 4) playSound(DarkCraftMod.SFX_SALOBRE_PLOMO.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
                if (t == golpe - 2) empujar(0.25);
            }
            case SalobreAnims.RED -> {
                if (t == 12) playSound(DarkCraftMod.SFX_SALOBRE_RED.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
                if (t == golpe) lanzarRed();
            }
            case SalobreAnims.GARFIO -> {
                if (t == golpe - 2) {
                    empujar(0.35);
                    playSound(DarkCraftMod.SFX_SALOBRE_GARFIO.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
                }
            }
            case SalobreAnims.BICHAZO -> {
                if (t == golpe - 3) empujar(0.3);
            }
            case SalobreAnims.RASTRILLAZO -> {
                if (t == 9) empujar(0.2);
                if (t == golpe) {
                    playSound(DarkCraftMod.SFX_SALOBRE_RASTRILLO.get(), 1.3f, 0.9f + random.nextFloat() * 0.15f);
                    costra();
                }
            }
            case SalobreAnims.SAL -> {
                if (t >= 6 && t <= golpe - 2 && level() instanceof ServerLevel sl) {
                    // la mano en el saco: la sal que se le escurre entre los dedos
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(SAL, getX() - d.x * 0.2 + d.z * 0.4, getY() + getBbHeight() * 0.8, getZ() - d.z * 0.2 - d.x * 0.4, 2, 0.1, 0.1, 0.1, 0.0);
                }
                if (t == golpe) {
                    playSound(DarkCraftMod.SFX_SALOBRE_SAL.get(), 1.2f, 0.95f + random.nextFloat() * 0.15f);
                    sal();
                }
            }
            case SalobreAnims.LEVANTARSE, SalobreAnims.ALZARSE -> {
                if (t == 2) sacudirse(accion == SalobreAnims.ALZARSE);
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case SalobreAnims.GARFIO -> arrastrar(le);
            case SalobreAnims.RASTRILLAZO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ el garfio, la red, la costra y la sal

    /** El garfio muerde: tira de su presa hacia el (por el suelo, un poco hacia arriba). */
    private void arrastrar(LivingEntity le) {
        GARFIOS++;
        double dx = getX() - le.getX(), dz = getZ() - le.getZ(), l = Math.sqrt(dx * dx + dz * dz);
        if (l < 1.6) return;
        double f = TIRON * Math.min(1.0, (l - 1.2) / 3.0) * (1.0 - le.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.6);
        le.setDeltaMovement(dx / l * f, 0.28, dz / l * f);
        le.hurtMarked = true;
        ARRASTRE = Math.max(ARRASTRE, f);
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.CRIT, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 8, 0.2, 0.2, 0.2, 0.1);
    }

    /** La red, en arco hacia su presa (cae donde estaba al soltarla). */
    private void lanzarRed() {
        LivingEntity p = getTarget();
        if (p == null || !(level() instanceof ServerLevel sl)) return;
        ultimaRed = tickCount;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.7, oy = getY() + 1.8, oz = getZ() + d.z * 0.7;
        Red r = new Red(sl, this);
        r.setPos(ox, oy, oz);
        r.setDeltaMovement(Cantaro.tiro(ox, oy, oz, p.getX(), p.getY() + 0.6, p.getZ()));
        sl.addFreshEntity(r);
        REDES++;
    }

    /** El rastro da en el suelo: alli queda una costra de sal que frena a quien la pise. */
    private void costra() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * 2.0, z = getZ() + d.z * 2.0, y = getY();
        sl.sendParticles(COSTRA, x, y + 0.2, z, 30, 0.6, 0.15, 0.6, 0.15);
        sl.sendParticles(POLVO, x, y + 0.3, z, 18, 0.5, 0.2, 0.5, 0.1);
        sl.sendParticles(SAL_GRUESA, x, y + 0.4, z, 14, 0.7, 0.2, 0.7, 0.02);
        costras.add(new Costra(x, y, z, tickCount + COSTRA_TICKS));
        frenados.clear();
        COSTRAS++;
    }

    /** El punado de sal a la cara: a quien pille delante le escuece (dano), le quita el aliento (estamina) y le frena. */
    private void sal() {
        if (!(level() instanceof ServerLevel sl)) return;
        ultimaSal = tickCount;
        SALES++;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double boca = getY() + getBbHeight() * 0.75;
        for (int k = 0; k < 6; k++) {
            double s = 0.6 + k * 0.75;
            double ab = 0.15 + s * 0.22;
            sl.sendParticles(SAL, getX() + d.x * s, boca - s * 0.08, getZ() + d.z * s, 6, ab, ab * 0.5, ab, 0.02);
            if (k % 2 == 1) sl.sendParticles(POLVO, getX() + d.x * s, boca - s * 0.08, getZ() + d.z * s, 3, ab, ab * 0.4, ab, 0.05);
        }
        double cosMin = Math.cos(Math.toRadians(ARCO_SAL / 2));
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(ALCANCE_SAL + 1, 2.0, ALCANCE_SAL + 1),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > ALCANCE_SAL + le.getBbWidth() * 0.5) continue;
            if (l > 0.8 && (dx * d.x + dz * d.z) / l < cosMin) continue;
            if (!hasLineOfSight(le)) continue;
            if (!le.hurt(damageSources().mobAttack(this), base * 0.5f)) continue;
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), this);
            if (le instanceof ServerPlayer sp) com.darkcraft.rol.Rol.quitarEstamina(sp, ESTAMINA_SAL);
            SALADOS++;
        }
    }

    /** Se sacude la sal (al levantarse de faenar) o rompe la costra y sale de ella (al alzarse). */
    private void sacudirse(boolean deLaSal) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (deLaSal) {
            ALZADOS++;
            sl.sendParticles(COSTRA, getX(), getY() + 0.3, getZ(), 40, 0.6, 0.3, 0.6, 0.15);
            sl.sendParticles(ARENA, getX(), getY() + 0.2, getZ(), 26, 0.7, 0.2, 0.7, 0.12);
            playSound(DarkCraftMod.SFX_SALOBRE_ALZARSE.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
        } else {
            sl.sendParticles(SAL, getX(), getY() + 1.0, getZ(), 16, 0.4, 0.5, 0.4, 0.02);
            sl.sendParticles(ARENA, getX(), getY() + 0.3, getZ(), 10, 0.4, 0.2, 0.4, 0.08);
        }
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive()) return;
            // la sal que se les cae y, de pie, la luz de agua que les gotea de los ojos
            if (tickCount % 11 == 0)
                level().addParticle(SAL, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 0.5 + random.nextDouble() * 1.3,
                        getZ() + (random.nextDouble() - 0.5) * 0.6, 0, -0.02, 0);
            if (postura() == DE_PIE && tickCount % 37 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                level().addParticle(AGUA, getX() + d.x * 0.3, getY() + getBbHeight() * 0.84, getZ() + d.z * 0.3, 0, -0.03, 0);
            }
            return;
        }
        // las costras de sal: brillan en el suelo y frenan a quien las pisa
        if (!costras.isEmpty() && level() instanceof ServerLevel sl) {
            for (int i = costras.size() - 1; i >= 0; i--) {
                Costra c = costras.get(i);
                if (tickCount >= c.hasta()) {
                    costras.remove(i);
                    continue;
                }
                if (tickCount % 4 == 0) sl.sendParticles(SAL, c.x(), c.y() + 0.12, c.z(), 4, R_COSTRA * 0.5, 0.02, R_COSTRA * 0.5, 0.0);
                if (tickCount % 10 == 0) sl.sendParticles(POLVO, c.x(), c.y() + 0.1, c.z(), 2, R_COSTRA * 0.4, 0.02, R_COSTRA * 0.4, 0.0);
                if (tickCount % 5 != 0) continue;
                for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class,
                        new AABB(c.x() - R_COSTRA, c.y() - 1.0, c.z() - R_COSTRA, c.x() + R_COSTRA, c.y() + 1.5, c.z() + R_COSTRA),
                        e -> e != this && e.isAlive() && e.onGround() && objetivoValido(e))) {
                    if (le.distanceToSqr(c.x(), le.getY(), c.z()) > R_COSTRA * R_COSTRA) continue;
                    le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 2), this);
                    if (frenados.add(le.getId())) FRENADOS++;
                }
            }
        }
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case SalobreAnims.BICHAZO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_SALOBRE_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case SALINERO -> 20 + random.nextInt(18);
            case BICHERO -> 14 + random.nextInt(14);
            default -> 14 + random.nextInt(16);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == BICHERO ? 3.4 : variante() == SALINERO ? 2.3 : 2.0;
    }

    @Override
    protected double prisa() {
        return variante() == SALINERO ? 0.85 : 1.0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_SALOBRE_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_SALOBRE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_SALOBRE_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "salobre");
        }
    }

    /** Al acabar de morir (de bruces) no se va en humo: se deshace en sal. */
    @Override
    protected void tickDeath() {
        int m = SalobreAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == SalobreAnims.TICKS[m] - 2) deshacerse();
        if (deathTime >= SalobreAnims.TICKS[m] + 2) remove(RemovalReason.KILLED);
    }

    private void deshacerse() {
        if (!(level() instanceof ServerLevel sl)) return;
        DESHECHOS++;
        sl.sendParticles(COSTRA, getX(), getY() + 0.3, getZ(), 46, 0.7, 0.25, 0.7, 0.18);
        sl.sendParticles(POLVO, getX(), getY() + 0.3, getZ(), 30, 0.7, 0.2, 0.7, 0.12);
        sl.sendParticles(SAL_GRUESA, getX(), getY() + 0.4, getZ(), 24, 0.8, 0.3, 0.8, 0.02);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_SALOBRE_MUERTE.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == SALINERO && random.nextFloat() < 0.05f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }
}
