package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
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

/**
 * Ser Brandt, el Muro del Rey (parte 4d): el guardian del salon del trono. Espera de rodillas en el gran salon, ante
 * las puertas del trono (selladas mientras viva: {@code valdrenn/Guardianes}). Tres bloques de hierro negro con un pavés
 * y un martillo de guerra:
 * <ul>
 * <li><b>el pavés</b>: de frente, sin estar atacando, para casi todo (las flechas, todo); en el <b>muro</b> se cubre del
 * todo y, si le han pegado, contesta con un escudazo. Hay que buscarle los lados o la espalda, o romperle la guardia;</li>
 * <li><b>martillazo</b> (lo alza casi un segundo; al dar en el suelo, una onda: se salta o se rueda), <b>golpe</b> de lado
 * (rapido), <b>escudazo</b> (empuja y frena) y <b>embestida</b> con el pavés delante (cruza media sala);</li>
 * <li>con media vida, el <b>terremoto</b>: salta y cae con todo (la onda llega a seis bloques).</li>
 * </ul>
 */
public class Brandt extends Guardian {
    public static final int ECOS = 3500;
    /** Lo que deja pasar el pavés de lo que le llega de frente (en el muro, nada). */
    static final float PAVES = 0.15f;

    private int embestidaEspera = 60, terremotoEspera, muroEspera = 80, golpesRecibidos, ultimoGolpe;
    private boolean pegadoEnMuro, contestar;

    public Brandt(EntityType<? extends Brandt> t, Level l) {
        super(t, l, "brandt", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 420.0).add(Attributes.ATTACK_DAMAGE, 9.0).add(Attributes.ARMOR, 14.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public String tipo() {
        return "brandt";
    }

    @Override
    public int musica() {
        return 31;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "brandt";
    }

    @Override
    protected int[] ticks() {
        return BrandtAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return BrandtAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return BrandtAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return BrandtAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return BrandtAnims.RUGIDO;
    }

    @Override
    protected int accionLevantarse() {
        return BrandtAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 3.0) return 0;
        // le han pegado mucho seguido: se cubre
        if (golpesRecibidos >= 3 && muroEspera <= 0 && d < 5) {
            golpesRecibidos = 0;
            muroEspera = 160 + random.nextInt(80);
            return BrandtAnims.MURO;
        }
        if (segunda && terremotoEspera <= 0 && d < 7.5 && random.nextFloat() < 0.4f) {
            terremotoEspera = 200 + random.nextInt(100);
            return BrandtAnims.TERREMOTO;
        }
        if (d > 6.0 && d < 13 && embestidaEspera <= 0 && random.nextFloat() < 0.5f) {
            embestidaEspera = 140 + random.nextInt(80);
            return BrandtAnims.EMBESTIDA;
        }
        if (d > 4.4) return 0;
        if (d < 2.6 && random.nextFloat() < 0.25f) return BrandtAnims.ESCUDAZO;
        return random.nextFloat() < 0.45f ? BrandtAnims.MARTILLAZO : BrandtAnims.GOLPE;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case BrandtAnims.MARTILLAZO -> new Golpe(4.2f, 60f, 1.9f, 1.0f);
            case BrandtAnims.GOLPE -> new Golpe(4.3f, 160f, 1.2f, 1.1f);
            case BrandtAnims.ESCUDAZO -> new Golpe(3.0f, 90f, 0.7f, 2.2f);
            case BrandtAnims.EMBESTIDA -> new Golpe(2.9f, 110f, 1.1f, 2.4f);
            case BrandtAnims.TERREMOTO -> new Golpe(3.4f, 360f, 1.5f, 1.2f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == BrandtAnims.ESCUDAZO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = BrandtAnims.GOLPES[accion];
        switch (accion) {
            case BrandtAnims.MARTILLAZO -> {
                if (t == g[0] - 1) empujar(0.5);
                if (t == g[0] + 1) onda(3.1, 3.2, 0.7f, 0.45);
            }
            case BrandtAnims.GOLPE -> {
                if (t == g[0] - 1) empujar(0.35);
            }
            case BrandtAnims.ESCUDAZO -> {
                if (t == g[0] - 1) empujar(0.9);
                if (t == g[0]) playSound(DarkCraftMod.SFX_BRANDT_ESCUDO.get(), 1.4f, 0.8f);
            }
            case BrandtAnims.EMBESTIDA -> {
                if (t == g[0] - 12) playSound(DarkCraftMod.SFX_ELITE_EMBESTIDA.get(), 1.5f, 0.75f);
                if (t >= g[0] - 11 && t <= g[0]) empujar(0.62);
            }
            case BrandtAnims.MURO -> {
                // al acabar, si le han pegado mientras se cubria, contesta con un escudazo
                if (t == 1) pegadoEnMuro = false;
                if (t == BrandtAnims.TICKS[BrandtAnims.MURO] - 1 && pegadoEnMuro) {
                    pegadoEnMuro = false;
                    contestar = true;
                }
            }
            case BrandtAnims.TERREMOTO -> {
                if (t == 17) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    setDeltaMovement(d.x * 0.35, 0.62, d.z * 0.35);
                    hasImpulse = true;
                }
                if (t == g[0]) onda(0.6, 6.0, 1.1f, 0.7);
            }
            case BrandtAnims.RUGIDO -> {
                if (t == 9) {
                    playSound(DarkCraftMod.SFX_BRANDT_RUGIDO.get(), 2.4f, 1.0f);
                    if (level() instanceof ServerLevel sl)
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 3.2, getZ(), 16, 0.4, 0.3, 0.4, 0.03);
                }
            }
            default -> {
            }
        }
    }

    /**
     * El golpe en el suelo: polvo, piedras y una onda (a {@code delante} bloques, de radio {@code r}) que alcanza a quien
     * este de pie (saltando o rodando se libra).
     */
    private void onda(double delante, double r, float mult, double arriba) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * delante, z = getZ() + d.z * delante;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        int n = (int) Math.round(r * 14);
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, n, r * 0.4, 0.05, r * 0.4, 0.25);
        sl.sendParticles(ParticleTypes.EXPLOSION, x, getY() + 0.3, z, r > 4 ? 3 : 1, r * 0.3, 0, r * 0.3, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, n / 2, r * 0.4, 0.1, r * 0.4, 0.02);
        level().playSound(null, x, getY(), z, DarkCraftMod.SFX_BRANDT_MARTILLAZO.get(), SoundSource.HOSTILE, r > 4 ? 2.4f : 1.8f, r > 4 ? 0.8f : 0.95f);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * mult;
        for (Player p : level().getEntitiesOfClass(Player.class, new AABB(x - r - 1, getY() - 1, z - r - 1, x + r + 1, getY() + 2.5, z + r + 1))) {
            if (!objetivoValido(p) || !p.onGround() || p.distanceToSqr(x, p.getY(), z) > r * r) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                p.knockback(0.6, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, arriba, 0));
                p.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ el pavés

    /** Si el pavés le tapa ese golpe: de frente (75 grados a cada lado), quieto o en el muro, ni aturdido ni cayendo. */
    private boolean cubre(DamageSource s) {
        if (postura() != DE_PIE || muriendo() || aturdido()) return false;
        int a = accion();
        if (a != 0 && a != BrandtAnims.MURO && a != BrandtAnims.EMBESTIDA) return false;
        if (s.is(DamageTypeTags.BYPASSES_ARMOR) || s.is(DamageTypeTags.IS_FIRE) || s.is(DamageTypeTags.IS_EXPLOSION)) return false;
        Vec3 de = s.getSourcePosition();
        if (de == null) return false;
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = de.x - getX(), dz = de.z - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l > 0.26;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.getEntity() instanceof Enemigo && s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) return false;
        if (!level().isClientSide && cubre(s)) {
            boolean flecha = s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
            level().playSound(null, getX(), getY() + 1.5, getZ(), DarkCraftMod.SFX_BRANDT_ESCUDO.get(), SoundSource.HOSTILE, 1.1f, 0.9f + random.nextFloat() * 0.2f);
            if (accion() == BrandtAnims.MURO) {
                pegadoEnMuro = true;
                return false;
            }
            if (flecha) return false;
            cuanto *= PAVES;
        }
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide) {
            golpesRecibidos = tickCount - ultimoGolpe < 60 ? golpesRecibidos + 1 : 1;
            ultimoGolpe = tickCount;
        }
        return ok;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (embestidaEspera > 0) embestidaEspera--;
        if (terremotoEspera > 0) terremotoEspera--;
        if (muroEspera > 0) muroEspera--;
        // tras el muro, si le pegaron mientras se cubria: escudazo
        if (contestar && accion() == 0) {
            contestar = false;
            if (getTarget() != null) {
                girarHacia(getTarget(), 60f);
                empezar(BrandtAnims.ESCUDAZO);
            }
        }
    }

    @Override
    protected void alSegundaMitad() {
        terremotoEspera = 30;
    }

    @Override
    protected double prisa() {
        return 1.2;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case BrandtAnims.RUGIDO -> 4;
            case BrandtAnims.MURO -> 2;
            case BrandtAnims.TERREMOTO -> 26 + random.nextInt(14);
            default -> (segunda ? 10 : 16) + random.nextInt(14);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return 2.9;
    }

    @Override
    protected double alturaMaxima() {
        return 3.0;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case BrandtAnims.MARTILLAZO, BrandtAnims.GOLPE -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(DarkCraftMod.SFX_ARMADURA_PASO.get(), 0.7f, 0.55f + random.nextFloat() * 0.1f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_BRANDT_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_BRANDT_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new net.minecraft.world.item.ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new net.minecraft.world.item.ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.SISMO).get()));
    }
}
