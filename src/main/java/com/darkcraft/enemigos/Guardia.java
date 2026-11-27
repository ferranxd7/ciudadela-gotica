package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * La guardia de Valdrenn (parte 4a): los caballeros muertos del Rey, que siguen en sus puestos dentro de su armadura
 * oxidada. Tres companias, cada una con su color en el tabardo:
 * <ul>
 * <li><b>espada y escudo</b> (rojo): tajo de arriba abajo, una tanda de tres que acaba en estocada y el escudazo que
 * empuja. <b>Se cubre</b>: de frente y sin estar atacando, el escudo para casi todo (y lo que llega en flecha, todo); a
 * golpes se le acaba rompiendo la guardia (el desequilibrio del combate) o se le coge por un lado;</li>
 * <li><b>alabarda</b> (morado): llega lejos; la pica a fondo, el hachazo que se alza (lento, pega mucho) y el barrido de
 * lado a lado;</li>
 * <li><b>ballesta</b> (verde): se queda lejos, apunta un instante y dispara un virote (se esquiva de lado o rodando);
 * luego tarda en tensar otra vez. A quien se le echa encima le da un culatazo.</li>
 * </ul>
 * Aguantan como los caballeros de antes (la dificultad de Valdrenn es que hay muchos).
 */
public class Guardia extends Enemigo {
    public static final int ESPADA = 0, ALABARDA = 1, BALLESTA = 2;
    /** Lo que deja pasar el escudo de lo que le llega de frente (y de las flechas, nada). */
    static final float ESCUDO = 0.2f;

    private int recarga;

    public Guardia(EntityType<? extends Guardia> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ATTACK_DAMAGE, 5.5).add(Attributes.ARMOR, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case ALABARDA -> "caballero_alabarda";
            case BALLESTA -> "caballero_ballesta";
            default -> "caballero_espada";
        };
    }

    @Override
    protected int[] ticks() {
        return GuardiaAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return GuardiaAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return GuardiaAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return GuardiaAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? GuardiaAnims.ALZARSE : GuardiaAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        boolean aSuAltura = Math.abs(presa.getY() - getY()) < 2.4;
        return switch (variante()) {
            case ALABARDA -> {
                if (d > 4.0 || !aSuAltura) yield 0;
                if (d > 2.9) yield GuardiaAnims.ALABARDA_PICA;
                float r = random.nextFloat();
                yield r < 0.35f ? GuardiaAnims.ALABARDA_HACHAZO : r < 0.7f ? GuardiaAnims.ALABARDA_BARRIDO : GuardiaAnims.ALABARDA_PICA;
            }
            case BALLESTA -> {
                if (d < 2.3 && aSuAltura) yield GuardiaAnims.BALLESTA_CULATAZO;
                yield d < 26 && recarga <= 0 ? GuardiaAnims.BALLESTA_DISPARO : 0;
            }
            default -> {
                if (d > 2.7 || !aSuAltura) yield 0;
                float r = random.nextFloat();
                if (d < 2.0 && r < 0.22f) yield GuardiaAnims.ESPADA_ESCUDAZO;
                yield r < 0.6f ? GuardiaAnims.ESPADA_TAJO : GuardiaAnims.ESPADA_COMBO;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case GuardiaAnims.ESPADA_TAJO -> new Golpe(2.9f, 80f, 1.3f, 0.5f);
            case GuardiaAnims.ESPADA_COMBO -> n < 2 ? new Golpe(2.8f, 130f, 0.85f, 0.25f) : new Golpe(3.3f, 40f, 1.25f, 0.6f);
            case GuardiaAnims.ESPADA_ESCUDAZO -> new Golpe(2.4f, 80f, 0.55f, 1.6f);
            case GuardiaAnims.ALABARDA_PICA -> new Golpe(4.2f, 30f, 1.3f, 0.5f);
            case GuardiaAnims.ALABARDA_HACHAZO -> new Golpe(3.8f, 50f, 1.8f, 0.8f);
            case GuardiaAnims.ALABARDA_BARRIDO -> new Golpe(3.9f, 170f, 1.0f, 0.7f);
            case GuardiaAnims.BALLESTA_CULATAZO -> new Golpe(2.4f, 80f, 0.8f, 1.0f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = GuardiaAnims.GOLPES[accion];
        switch (accion) {
            case GuardiaAnims.ESPADA_TAJO -> {
                if (t == g[0] - 1) empujar(0.55);
            }
            case GuardiaAnims.ESPADA_COMBO -> {
                if (t == g[0] - 1 || t == g[1] - 1) empujar(0.4);
                else if (t == g[2] - 1) empujar(0.85);
            }
            case GuardiaAnims.ESPADA_ESCUDAZO -> {
                if (t == g[0] - 1) empujar(0.9);
            }
            case GuardiaAnims.ALABARDA_PICA -> {
                if (t == g[0] - 1) empujar(0.8);
            }
            case GuardiaAnims.ALABARDA_HACHAZO -> {
                if (t == g[0] - 1) empujar(0.45);
                if (t == g[0]) polvo();
            }
            case GuardiaAnims.ALABARDA_BARRIDO -> {
                if (t == g[0] - 1) empujar(0.3);
            }
            case GuardiaAnims.BALLESTA_DISPARO -> {
                if (t == g[0]) disparar();
            }
            case GuardiaAnims.BALLESTA_CULATAZO -> {
                if (t == g[0] - 1) empujar(0.5);
            }
            default -> {
            }
        }
    }

    /** El hachazo da en el suelo: polvo y piedrecitas. */
    private void polvo() {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * 2.6, z = getZ() + d.z * 2.6;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (suelo.isAir()) return;
        sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, suelo),
                x, getY() + 0.1, z, 18, 0.35, 0.05, 0.35, 0.15);
    }

    /** Suelta el virote hacia donde apuntaba (el cuerpo ya no gira desde tres ticks antes: se esquiva de lado). */
    private void disparar() {
        LivingEntity t = getTarget();
        Arrow a = new Flecha(level(), this);
        a.setPos(getX(), getEyeY() - 0.25, getZ());
        a.setBaseDamage(2.2);
        a.pickup = AbstractArrow.Pickup.DISALLOWED;
        a.setShotFromCrossbow(true);
        a.setPierceLevel((byte) 0);
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double h = t == null ? 10 : Math.max(1, Math.sqrt(Math.pow(t.getX() - getX(), 2) + Math.pow(t.getZ() - getZ(), 2)));
        Vec3 v = tiro(h, t == null ? 0 : t.getY(0.55) - a.getY(), d, 3.0);
        a.shoot(v.x, v.y, v.z, 3.0f, 1.2f);
        level().addFreshEntity(a);
        playSound(SoundEvents.CROSSBOW_SHOOT, 1.2f, 0.75f + random.nextFloat() * 0.1f);
        recarga = 34 + random.nextInt(30);
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case GuardiaAnims.ALABARDA_PICA -> DarkCraftMod.SFX_ESTOCADA.get();
            case GuardiaAnims.ALABARDA_HACHAZO, GuardiaAnims.ALABARDA_BARRIDO -> DarkCraftMod.SFX_ALABARDA_TAJO.get();
            case GuardiaAnims.ESPADA_ESCUDAZO, GuardiaAnims.BALLESTA_CULATAZO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            case GuardiaAnims.BALLESTA_DISPARO -> null;
            default -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_GUARDIA_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case ALABARDA -> 20 + random.nextInt(18);
            case BALLESTA -> 10 + random.nextInt(12);
            default -> 14 + random.nextInt(18);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return switch (variante()) {
            case ALABARDA -> 2.9;
            case BALLESTA -> 9.0;
            default -> 1.8;
        };
    }

    /** El de la ballesta dispara desde las murallas a quien pasa por debajo (y desde abajo a quien esta arriba). */
    @Override
    protected double alturaMaxima() {
        return variante() == BALLESTA ? 14.0 : 2.6;
    }

    // ------------------------------------------------------------------ el escudo

    /** Si el escudo le tapa ese golpe: de frente (70 grados a cada lado), sin estar atacando, ni aturdido, ni cayendo. */
    private boolean cubre(DamageSource s) {
        if (variante() != ESPADA || postura() != DE_PIE || muriendo() || aturdido()) return false;
        int a = accion();
        if (a != 0 && a != GuardiaAnims.ESPADA_BLOQUEA) return false;
        if (s.is(DamageTypeTags.BYPASSES_ARMOR) || s.is(DamageTypeTags.IS_FIRE) || s.is(DamageTypeTags.IS_EXPLOSION)) return false;
        Vec3 de = s.getSourcePosition();
        if (de == null) return false;
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = de.x - getX(), dz = de.z - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l > 0.34;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.getEntity() instanceof Enemigo && s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) return false;
        if (!level().isClientSide && cubre(s)) {
            boolean flecha = s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
            // un martillo le rompe la guardia (parte 7aj): el golpe entra entero y se queda aturdido
            if (!flecha && com.darkcraft.armas.Martillo.rompe(s)) {
                level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_GUARDIA_ROTA.get(), SoundSource.HOSTILE, 1.0f, 0.9f + random.nextFloat() * 0.15f);
                boolean ok = super.hurt(s, cuanto);
                if (ok && isAlive()) aturdir();
                return ok;
            }
            level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_ESCUDO_GOLPE.get(), SoundSource.HOSTILE, 1.0f, 0.85f + random.nextFloat() * 0.2f);
            if (accion() == 0) empezar(GuardiaAnims.ESPADA_BLOQUEA);
            if (flecha) return false;
            cuanto *= ESCUDO;
        }
        return super.hurt(s, cuanto);
    }

    // ------------------------------------------------------------------ cada tick, sonidos y botin

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && recarga > 0) recarga--;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(DarkCraftMod.SFX_ARMADURA_PASO.get(), 0.32f, 0.85f + random.nextFloat() * 0.25f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_CABALLERO_GRUNIDO.get();
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
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.07f + 0.02f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
