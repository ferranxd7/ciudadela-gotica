package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Los helados de las Montanas Nevadas (parte 7k): los monjes y peregrinos del Monasterio de la Helada que se quedaron helados
 * rezando cuando el cielo se puso rojo; la Sombra los desperto dentro del hielo. Piel azul gris con escarcha, la barba de
 * hielo, los ojos de un azul blanco. Esperan <b>de rodillas</b>, helados en su rezo ({@code REZA}: al acercarse alguien
 * rompen el hielo y se levantan) o <b>enterrados</b> en la nieve ({@code TUMBADO}: se alzan de golpe). Tres:
 * <ul>
 * <li>el <b>monje</b>: voltea su incensario de hierro (vaho que congela) y echa un <b>aliento</b> helado delante: congela y
 * frena a quien se queda en el;</li>
 * <li>el <b>penitente</b>: el torso al aire con cadenas y un martillo de hielo a dos manos; el <b>martillazo</b> hace brotar
 * una fila de puas de hielo delante; y un barrido;</li>
 * <li>el <b>peregrino</b>: un baston con un cristal de hielo; de punta de cerca, y de lejos lanza tres <b>esquirlas</b>
 * ({@link Esquirla}).</li>
 * </ul>
 * Lo suyo hace dano de frio (el de la nieve en polvo: lo frena el fruto y el talisman de la escarcha) y deja al que pillan
 * helado un rato ({@code setTicksFrozen}). El frio no les hace nada; el fuego, mas. Al morir se quedan de rodillas y se
 * rompen en esquirlas.
 */
public class Helado extends Enemigo {
    public static final int MONJE = 0, PENITENTE = 1, PEREGRINO = 2;
    /** Lo que esperan entre un aliento o unas esquirlas y los siguientes (ticks). */
    public static final int ESPERA_ALIENTO = 20 * 6, ESPERA_ESQUIRLAS = 20 * 4;
    /** Lo que hiela cada golpe (ticks de frio: a 140 se esta helado del todo; bajan 2 por tick). */
    public static final int FRIO_GOLPE = 80, FRIO_MARTILLO = 120, FRIO_ESQUIRLA = 70, FRIO_ALIENTO_TICK = 14, FRIO_MAX = 260;
    /** El aliento: hasta donde llega y su abertura (grados). */
    public static final double ALCANCE_ALIENTO = 5.2, ARCO_ALIENTO = 50;
    private static final UUID MOD_PENITENTE = UUID.fromString("5d2b8e71-3c4a-4f19-9e06-b7a1c2d3e4f5");
    static final DustParticleOptions ESCARCHA = new DustParticleOptions(new Vector3f(0.86f, 0.95f, 1f), 1.2f),
            VAHO = new DustParticleOptions(new Vector3f(0.62f, 0.86f, 0.98f), 1.6f);
    static final BlockParticleOption TROZOS = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
            NIEVE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState());

    private record Pua(double x, double y, double z, int cuando) {}

    private final List<Pua> puas = new ArrayList<>();
    private final Set<Integer> helados = new HashSet<>();
    private int ultimoAliento = -100000, ultimasEsquirlas = -100000;

    /** Para la prueba: alientos, puas, esquirlas lanzadas, hielos rotos al alzarse, los que han helado y los que se han roto al morir. */
    public static int ALIENTOS, PUAS, ESQUIRLAS, ROTOS, HELADOS, ESTALLIDOS;

    public Helado(EntityType<? extends Helado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.ATTACK_DAMAGE, 5.5)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.35)
                .add(Attributes.ARMOR, 3.0);
    }

    /** El penitente aguanta mas (el hielo le cubre el torso). */
    @Override
    public void variante(int v) {
        super.variante(v);
        if (level().isClientSide) return;
        var vida = getAttribute(Attributes.MAX_HEALTH);
        if (vida == null) return;
        vida.removeModifier(MOD_PENITENTE);
        if (v == PENITENTE) vida.addPermanentModifier(new AttributeModifier(MOD_PENITENTE, "darkcraft_penitente", 0.45, AttributeModifier.Operation.MULTIPLY_BASE));
        setHealth(getMaxHealth());
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case PENITENTE -> "helado_penitente";
            case PEREGRINO -> "helado_peregrino";
            default -> "helado_monje";
        };
    }

    @Override
    protected int[] ticks() {
        return HeladoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return HeladoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return HeladoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return HeladoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? HeladoAnims.ALZARSE : HeladoAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case PENITENTE -> {
                if (d < 3.0) yield random.nextFloat() < 0.55f ? HeladoAnims.MARTILLAZO : HeladoAnims.BARRIDO;
                // las puas llegan mas lejos que el martillo
                yield d < 5.0 && random.nextFloat() < 0.35f ? HeladoAnims.MARTILLAZO : 0;
            }
            case PEREGRINO -> {
                if (d > 4.0 && d < 16 && tickCount - ultimasEsquirlas > ESPERA_ESQUIRLAS) yield HeladoAnims.ESQUIRLAS;
                if (d < 3.2) yield HeladoAnims.BASTONAZO;
                yield 0;
            }
            default -> {
                if (d > 2.0 && d < 5.0 && tickCount - ultimoAliento > ESPERA_ALIENTO && random.nextFloat() < 0.5f) yield HeladoAnims.ALIENTO;
                if (d < 2.9) yield HeladoAnims.INCENSARIO;
                yield d < 3.6 && random.nextFloat() < 0.3f ? HeladoAnims.INCENSARIO : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case HeladoAnims.INCENSARIO -> new Golpe(3.1f, 110f, 1.0f, 0.45f);
            case HeladoAnims.MARTILLAZO -> new Golpe(3.0f, 70f, 1.6f, 0.8f);
            case HeladoAnims.BARRIDO -> new Golpe(3.2f, 160f, 1.1f, 0.6f);
            case HeladoAnims.BASTONAZO -> new Golpe(3.4f, 40f, 1.0f, 0.5f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case HeladoAnims.INCENSARIO -> {
                if (t == 9) playSound(DarkCraftMod.SFX_HELADO_INCENSARIO.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
                if (t >= 10 && t <= 18) vahoIncensario();
            }
            case HeladoAnims.ALIENTO -> {
                if (t == 15) {
                    ultimoAliento = tickCount;
                    ALIENTOS++;
                    helados.clear();
                    playSound(DarkCraftMod.SFX_HELADO_ALIENTO.get(), 1.2f, 0.9f + random.nextFloat() * 0.15f);
                }
                if (t >= 17 && t <= 34) aliento(t);
            }
            case HeladoAnims.MARTILLAZO -> {
                if (t == 9) empujar(0.2);
                if (t == 22) playSound(DarkCraftMod.SFX_HELADO_MARTILLO.get(), 1.3f, 0.9f + random.nextFloat() * 0.15f);
                if (t == 23) puas();
            }
            case HeladoAnims.BARRIDO -> {
                if (t == 12) empujar(0.3);
            }
            case HeladoAnims.BASTONAZO -> {
                if (t == 9) empujar(0.55);
            }
            case HeladoAnims.ESQUIRLAS -> {
                if (t == 11) playSound(DarkCraftMod.SFX_HELADO_ESQUIRLAS.get(), 1.1f, 0.95f + random.nextFloat() * 0.15f);
                if (t >= 11 && t <= 18 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    // el cristal del baston, alzado: se carga de escarcha
                    sl.sendParticles(ESCARCHA, getX() + d.x * 0.3 - d.z * 0.35, getY() + getBbHeight() * 1.18, getZ() + d.z * 0.3 + d.x * 0.35,
                            3, 0.12, 0.12, 0.12, 0.0);
                }
                if (t == 19) lanzarEsquirlas();
            }
            case HeladoAnims.LEVANTARSE, HeladoAnims.ALZARSE -> {
                if (t == 2) romperHielo(accion == HeladoAnims.ALZARSE);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ el frio

    /** El dano de frio de un helado (el de la nieve en polvo: lo frenan el fruto y el talisman de la escarcha). */
    public static DamageSource frio(Level l, Entity directo, Entity causa) {
        return new DamageSource(l.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FREEZE), directo, causa);
    }

    /** Deja a alguien helado un rato (la escarcha en la pantalla, frena y, del todo helado, duele): el fruto de la escarcha lo quita. */
    public static void helar(LivingEntity le, int ticks) {
        if (le instanceof Helado || !le.canFreeze()) return;
        le.setTicksFrozen(Math.min(FRIO_MAX, Math.max(le.getTicksFrozen(), 0) + ticks));
        HELADOS++;
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case HeladoAnims.INCENSARIO, HeladoAnims.BARRIDO -> helar(le, FRIO_GOLPE);
            case HeladoAnims.MARTILLAZO -> {
                helar(le, FRIO_MARTILLO);
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
            }
            case HeladoAnims.BASTONAZO -> {
                helar(le, FRIO_GOLPE);
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
            }
            default -> {
            }
        }
    }

    /** El vaho del incensario mientras lo voltea. */
    private void vahoIncensario() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        sl.sendParticles(VAHO, getX() + d.x * 1.4 - d.z * 0.5, getY() + 1.2, getZ() + d.z * 1.4 + d.x * 0.5, 3, 0.35, 0.25, 0.35, 0.01);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX() + d.x * 1.4, getY() + 1.3, getZ() + d.z * 1.4, 2, 0.4, 0.3, 0.4, 0.02);
    }

    /**
     * El aliento del monje, un tick: el soplo blanco que sale de la boca y, a quien este en el (de pie o no: no se salta),
     * le va helando; la primera vez que le pilla, el dano de frio y lentitud.
     */
    private void aliento(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double boca = getY() + getBbHeight() * 0.8;
        for (int k = 0; k < 5; k++) {
            double s = 0.6 + k * 0.95 + random.nextDouble() * 0.4;
            double ab = s * 0.28;
            sl.sendParticles(ParticleTypes.SNOWFLAKE, getX() + d.x * s, boca - s * 0.12, getZ() + d.z * s, 2, ab, ab * 0.5, ab, 0.01);
            if (k % 2 == 0) sl.sendParticles(VAHO, getX() + d.x * s, boca - s * 0.12, getZ() + d.z * s, 1, ab, ab * 0.4, ab, 0.0);
        }
        double cosMin = Math.cos(Math.toRadians(ARCO_ALIENTO / 2));
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(ALCANCE_ALIENTO + 1, 2.0, ALCANCE_ALIENTO + 1),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > ALCANCE_ALIENTO + le.getBbWidth() * 0.5) continue;
            if (l > 0.8 && (dx * d.x + dz * d.z) / l < cosMin) continue;
            if (!hasLineOfSight(le)) continue;
            helar(le, FRIO_ALIENTO_TICK);
            if (helados.add(le.getId()) && le.hurt(frio(level(), this, this), base * 0.6f))
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1), this);
        }
    }

    /** El martillo da en el suelo: una fila de puas de hielo que brotan delante, una tras otra (el dano un instante despues). */
    private void puas() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        sl.sendParticles(TROZOS, getX() + d.x * 1.8, getY() + 0.2, getZ() + d.z * 1.8, 24, 0.5, 0.15, 0.5, 0.15);
        sl.sendParticles(NIEVE, getX() + d.x * 1.8, getY() + 0.2, getZ() + d.z * 1.8, 16, 0.6, 0.1, 0.6, 0.1);
        float yaw = (float) Math.toRadians(-getYRot());
        for (int k = 0; k < 4; k++) {
            double s = 1.8 + k * 1.25;
            double x = getX() + d.x * s, z = getZ() + d.z * s;
            double y = suelo(sl, x, z);
            if (Double.isNaN(y)) break;
            BlockState st = k % 2 == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState();
            EfectosRaiz.estaca(sl, st, x, y, z, yaw + (random.nextFloat() - 0.5f) * 0.8f, 0.3f * (random.nextFloat() - 0.5f),
                    1.5f + random.nextFloat() * 0.6f + k * 0.15f, 0.42f + random.nextFloat() * 0.12f, k * 2, 20);
            puas.add(new Pua(x, y, z, tickCount + k * 2 + 2));
            PUAS++;
        }
    }

    /** Lo alto del suelo bajo (x, z) cerca de su altura, o NaN si no hay (un hueco, un muro). */
    private double suelo(ServerLevel sl, double x, double z) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z), by = (int) Math.floor(getY());
        for (int y = by + 1; y >= by - 3; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            BlockState s = sl.getBlockState(p);
            if (!s.getCollisionShape(sl, p).isEmpty() && sl.getBlockState(p.above()).getCollisionShape(sl, p.above()).isEmpty()) return y + 1;
        }
        return Double.NaN;
    }

    /** Lanza tres esquirlas a su presa: la de en medio a donde esta, las otras dos un poco a los lados. */
    private void lanzarEsquirlas() {
        if (level().isClientSide) return;
        ultimasEsquirlas = tickCount;
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.9, oz = getZ() + d.z * 0.9, oy = getY() + getBbHeight() * 0.78;
        double tx = t == null ? ox + d.x * 10 : t.getX(), tz = t == null ? oz + d.z * 10 : t.getZ();
        double ty = t == null ? oy : t.getY() + t.getBbHeight() * 0.5;
        Vec3 v = new Vec3(tx - ox, ty - oy, tz - oz);
        for (int k = -1; k <= 1; k++) {
            Vec3 w = v.yRot((float) Math.toRadians(k * 9)).normalize().scale(Esquirla.RAPIDEZ);
            Esquirla e = new Esquirla(level(), this);
            e.setPos(ox, oy, oz);
            // un poco hacia arriba por lo que cae
            double h = Math.sqrt(v.x * v.x + v.z * v.z);
            e.setDeltaMovement(w.add(0, Esquirla.GRAVEDAD * 0.5 * (h / Esquirla.RAPIDEZ), 0));
            level().addFreshEntity(e);
            ESQUIRLAS++;
        }
    }

    /** Rompe el hielo (estaba de rodillas helado o enterrado en la nieve). */
    private void romperHielo(boolean nieve) {
        if (!(level() instanceof ServerLevel sl)) return;
        ROTOS++;
        sl.sendParticles(TROZOS, getX(), getY() + 1.0, getZ(), 34, 0.45, 0.6, 0.45, 0.15);
        sl.sendParticles(nieve ? NIEVE : ESCARCHA, getX(), getY() + 0.5, getZ(), 26, 0.6, 0.4, 0.6, 0.08);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.2, getZ(), 18, 0.5, 0.6, 0.5, 0.05);
        playSound(DarkCraftMod.SFX_HELADO_ROMPER.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ el frio no les hace nada; el fuego, mas

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.is(DamageTypeTags.IS_FREEZING)) return false;
        if (s.is(DamageTypeTags.IS_FIRE)) cuanto *= 1.6f;
        return super.hurt(s, cuanto);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case HeladoAnims.BASTONAZO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            case HeladoAnims.BARRIDO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_HELADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case PENITENTE -> 20 + random.nextInt(18);
            case PEREGRINO -> 14 + random.nextInt(14);
            default -> 16 + random.nextInt(16);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == PEREGRINO ? 6.0 : variante() == PENITENTE ? 2.2 : 2.0;
    }

    @Override
    protected double prisa() {
        return variante() == PENITENTE ? 0.9 : 1.0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive()) return;
            // el vaho del aliento al respirar, y la escarcha que se les cae; el incensario echa su vaho
            if (postura() == DE_PIE && tickCount % 23 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                level().addParticle(ParticleTypes.SNOWFLAKE, getX() + d.x * 0.35, getY() + getBbHeight() * 0.86, getZ() + d.z * 0.35, d.x * 0.02, 0.01, d.z * 0.02);
            }
            if (tickCount % 9 == 0)
                level().addParticle(ESCARCHA, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 0.4 + random.nextDouble() * 1.4,
                        getZ() + (random.nextDouble() - 0.5) * 0.6, 0, -0.01, 0);
            if (variante() == MONJE && postura() == DE_PIE && accion() == 0 && tickCount % 6 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                double x = getX() + d.x * 0.3 - d.z * 0.48, z = getZ() + d.z * 0.3 + d.x * 0.48;
                level().addParticle(VAHO, x, getY() + 0.55, z, 0, 0.02, 0);
            }
            return;
        }
        // las puas de hielo: el dano cuando acaban de brotar
        if (!puas.isEmpty() && level() instanceof ServerLevel sl) {
            float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
            for (int i = puas.size() - 1; i >= 0; i--) {
                Pua p = puas.get(i);
                if (tickCount < p.cuando()) continue;
                puas.remove(i);
                sl.sendParticles(TROZOS, p.x(), p.y() + 0.5, p.z(), 8, 0.25, 0.4, 0.25, 0.08);
                for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(p.x() - 1, p.y() - 0.5, p.z() - 1, p.x() + 1, p.y() + 2.2, p.z() + 1),
                        e -> e != this && e.isAlive() && objetivoValido(e))) {
                    if (le.distanceToSqr(p.x(), le.getY(), p.z()) > 0.95 * 0.95) continue;
                    if (le.hurt(damageSources().mobAttack(this), base * 1.1f)) {
                        helar(le, FRIO_GOLPE);
                        le.setDeltaMovement(le.getDeltaMovement().add(0, 0.45, 0));
                        le.hurtMarked = true;
                    }
                }
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_HELADO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_HELADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_HELADO_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "helado");
        }
    }

    /** Al acabar de morir (de rodillas, vencido) no se va en humo: se rompe en esquirlas de hielo. */
    @Override
    protected void tickDeath() {
        int m = HeladoAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == HeladoAnims.TICKS[m] - 4) estallar();
        if (deathTime >= HeladoAnims.TICKS[m]) remove(RemovalReason.KILLED);
    }

    private void estallar() {
        if (!(level() instanceof ServerLevel sl)) return;
        ESTALLIDOS++;
        sl.sendParticles(TROZOS, getX(), getY() + 0.7, getZ(), 50, 0.45, 0.5, 0.45, 0.22);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), getX(), getY() + 0.6, getZ(), 24, 0.4, 0.4, 0.4, 0.2);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 0.9, getZ(), 24, 0.5, 0.5, 0.5, 0.06);
        sl.sendParticles(ESCARCHA, getX(), getY() + 0.8, getZ(), 20, 0.5, 0.5, 0.5, 0.02);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_HELADO_MUERTE.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == PENITENTE && random.nextFloat() < 0.04f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }
}
