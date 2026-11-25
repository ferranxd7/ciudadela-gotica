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

/**
 * El capitan de elite de la guardia de Valdrenn (parte 4c): uno al mando de cada grupo grande (la puerta, el patio de
 * armas, el patio de la fuente, la torre del arbol). Mas alto que los suyos, armadura ennegrecida con filos de oro, el
 * gran yelmo con penacho rojo, capa larga y un mandoble a dos manos.
 * <ul>
 * <li><b>Al verte arenga a los suyos</b> (accion {@code grito}, una vez por pelea): los enemigos de alrededor que aun no
 * te habian visto van a por ti y todos pegan un {@value #ARENGA_PCT} % mas durante {@value #ARENGA_S} s
 * ({@link Enemigo#animo});</li>
 * <li>el <b>tajo</b> de arriba abajo se ve venir (lo alza casi un segundo) y al dar en el suelo hace una <b>onda</b>: a
 * quien este de pie cerca le alcanza aunque esquive la hoja (saltando o rodando se libra);</li>
 * <li>el <b>barrido</b> es ancho (se esquiva rodando hacia atras), la <b>estocada</b> llega lejos y la <b>embestida</b>
 * cruza cuatro o cinco bloques de golpe y tumba;</li>
 * <li><b>si cae, los suyos se desaniman</b>: pegan menos y van mas despacio un rato.</li>
 * </ul>
 */
public class Elite extends Enemigo {
    static final int ARENGA_PCT = 30, ARENGA_S = 12;
    /** Hasta donde llega su voz: a quien arenga y a quien desanima al caer. */
    static final double VOZ = 18.0;

    private boolean arengado;
    private int embestidaEspera;

    public Elite(EntityType<? extends Elite> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 120.0).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0).add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.FOLLOW_RANGE, 22.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85);
    }

    @Override
    public String tipo() {
        return "capitan_elite";
    }

    @Override
    protected int[] ticks() {
        return EliteAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return EliteAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return EliteAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return EliteAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? EliteAnims.ALZARSE : EliteAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        // lo primero, arengar a los suyos
        if (!arengado && d < 16) {
            arengado = true;
            return EliteAnims.GRITO;
        }
        if (Math.abs(presa.getY() - getY()) > 2.6) return 0;
        if (d > 4.6 && d < 9.5 && embestidaEspera <= 0 && random.nextFloat() < 0.45f) {
            embestidaEspera = 100 + random.nextInt(80);
            return EliteAnims.EMBESTIDA;
        }
        if (d > 4.2) return 0;
        if (d > 3.3) return random.nextFloat() < 0.7f ? EliteAnims.ESTOCADA : 0;
        float r = random.nextFloat();
        return r < 0.38f ? EliteAnims.TAJO : r < 0.76f ? EliteAnims.BARRIDO : EliteAnims.ESTOCADA;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case EliteAnims.TAJO -> new Golpe(3.5f, 60f, 1.7f, 0.9f);
            case EliteAnims.BARRIDO -> new Golpe(3.8f, 200f, 1.1f, 1.2f);
            case EliteAnims.ESTOCADA -> new Golpe(4.5f, 30f, 1.45f, 0.6f);
            case EliteAnims.EMBESTIDA -> new Golpe(2.4f, 110f, 1.0f, 2.0f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = EliteAnims.GOLPES[accion];
        switch (accion) {
            case EliteAnims.TAJO -> {
                if (t == g[0] - 1) empujar(0.5);
                if (t == g[0] + 1) onda();
            }
            case EliteAnims.BARRIDO -> {
                if (t == g[0] - 1) empujar(0.35);
            }
            case EliteAnims.ESTOCADA -> {
                if (t == g[0] - 1) empujar(1.0);
            }
            case EliteAnims.EMBESTIDA -> {
                // agacha el hombro (se ve venir) y carga cuatro o cinco bloques; pega en el choque
                // (el golpe de las tomas esta al principio: 0,1-0,2 s)
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_ELITE_EMBESTIDA.get(), 1.2f, 0.9f + random.nextFloat() * 0.15f);
                if (t >= g[0] - 6 && t <= g[0]) empujar(0.62);
            }
            case EliteAnims.GRITO -> {
                if (t == 9) arengar();
            }
            default -> {
            }
        }
    }

    /** El mandoble da en el suelo: polvo, piedrecitas y una onda que alcanza a quien este de pie cerca. */
    private void onda() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * 2.9, z = getZ() + d.z * 2.9;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, 34, 0.6, 0.05, 0.6, 0.2);
        sl.sendParticles(ParticleTypes.EXPLOSION, x, getY() + 0.3, z, 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, 10, 0.8, 0.1, 0.8, 0.02);
        level().playSound(null, x, getY(), z, DarkCraftMod.SFX_ELITE_IMPACTO.get(), SoundSource.HOSTILE, 1.4f, 0.9f + random.nextFloat() * 0.15f);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.65f;
        for (Player p : level().getEntitiesOfClass(Player.class, new AABB(x - 3, getY() - 1, z - 3, x + 3, getY() + 2, z + 3))) {
            if (!objetivoValido(p) || !p.onGround() || p.distanceToSqr(x, p.getY(), z) > 2.7 * 2.7) continue;
            // (quien ya se llevo la hoja tiene aun los ticks de invulnerable: la onda no le hace mas)
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                p.knockback(0.5, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.32, 0));
            }
        }
    }

    /** El grito: los de alrededor van a por su presa y pegan mas un rato. */
    private void arengar() {
        if (!(level() instanceof ServerLevel sl)) return;
        playSound(DarkCraftMod.SFX_ELITE_GRITO.get(), 2.2f, 0.92f + random.nextFloat() * 0.1f);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 2.9, getZ(), 18, 0.25, 0.35, 0.25, 0.04);
        for (int k = 0; k < 24; k++) {
            double a = k * Math.PI * 2 / 24;
            sl.sendParticles(new DustParticleOptions(new Vector3f(0.75f, 0.08f, 0.08f), 1.6f), getX() + Math.cos(a) * 1.6, getY() + 0.2, getZ() + Math.sin(a) * 1.6,
                    1, Math.cos(a) * 0.4, 0.02, Math.sin(a) * 0.4, 0.3);
        }
        LivingEntity presa = getTarget();
        animo(ARENGA_PCT / 100.0, ARENGA_S * 20);
        for (Enemigo e : level().getEntitiesOfClass(Enemigo.class, getBoundingBox().inflate(VOZ, 6, VOZ), o -> o != this && o.isAlive() && !o.muriendo())) {
            e.animo(ARENGA_PCT / 100.0, ARENGA_S * 20);
            if (presa != null && e.getTarget() == null) e.acudir(presa);
        }
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (!(level() instanceof ServerLevel sl)) return;
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null) com.darkcraft.story.Logros.dar(quien, "capitan_elite");
        // los suyos se desaniman
        for (Enemigo e : level().getEntitiesOfClass(Enemigo.class, getBoundingBox().inflate(VOZ + 2, 6, VOZ + 2), o -> o != this && o.isAlive() && !o.muriendo())) {
            e.animo(-0.25, 15 * 20);
            e.pausa = Math.max(e.pausa, 24);
            sl.sendParticles(ParticleTypes.SMOKE, e.getX(), e.getY() + e.getBbHeight() + 0.3, e.getZ(), 8, 0.2, 0.1, 0.2, 0.01);
        }
    }

    // ------------------------------------------------------------------ cada tick, sonidos y botin

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (embestidaEspera > 0) embestidaEspera--;
        // sin presa y en su puesto, vuelve a poder arengar (la proxima pelea)
        if (getTarget() == null && accion() == 0 && tickCount % 40 == 0) arengado = false;
    }

    @Override
    protected double prisa() {
        return 1.15;
    }

    @Override
    protected int pausaTras(int accion) {
        return accion == EliteAnims.GRITO ? 6 : 16 + random.nextInt(18);
    }

    @Override
    protected double distanciaDeseada() {
        return 2.7;
    }

    @Override
    protected double alturaMaxima() {
        return 2.8;
    }

    @Override
    protected double correa() {
        return 40.0;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case EliteAnims.TAJO, EliteAnims.BARRIDO -> DarkCraftMod.SFX_ALABARDA_TAJO.get();
            case EliteAnims.ESTOCADA -> DarkCraftMod.SFX_ESTOCADA.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_ELITE_ALERTA.get();
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(DarkCraftMod.SFX_ARMADURA_PASO.get(), 0.45f, 0.7f + random.nextFloat() * 0.15f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_CABALLERO_GRUNIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ELITE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ELITE_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Override
    protected float getSoundVolume() {
        return 1.3f;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (!jugador) return;
        if (random.nextFloat() < 0.6f + 0.1f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (random.nextFloat() < 0.18f + 0.04f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }
}
