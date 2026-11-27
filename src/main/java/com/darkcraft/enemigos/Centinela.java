package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * El centinela de la casa de la Sombra (parte 7ab): una armadura de los caballeros de la ciudadela, vacia (la Sombra se quedo con
 * lo de dentro): por las juntas le sale humo negro y tras la rendija del yelmo arden dos brasas. "Nunca aprendieron a morir."
 * <ul>
 * <li>Espera de pie e inmovil como una armadura mas ({@code REZA}: no ve por la espalda ni de lejos; se despierta a 6 bloques, 3
 * si se pasa agachado) o tirada, hecha un monton de hierro ({@code TUMBADO}: se levanta juntando las piezas si alguien se
 * acerca).</li>
 * <li>Variante 0, el <b>espadon</b>: el <b>tajo</b> de lado a lado a la altura del pecho y el <b>golpe</b> de arriba abajo
 * (lo alza casi un segundo; al dar en el suelo, la <b>onda</b> alrededor: saltando o rodando se libra). Variante 1, la
 * <b>alabarda</b>: la <b>estocada</b> (llega lejos), el <b>barrido</b> de vuelta entera y el <b>hachazo</b>.</li>
 * <li>Los dos, el <b>humo</b>: se deshace en humo, cruza hasta su presa (entre 4,5 y 10 bloques) y sale de el con una estocada.</li>
 * <li>Al morir se desploma: las piezas caen, se le va el humo y queda un monton de hierro que se deshace.</li>
 * </ul>
 */
public class Centinela extends Enemigo {
    public static final int ESPADON = 0, ALABARDA = 1;
    /** El humo: de cuanto a cuanto lejos lo usa, lo que espera entre uno y otro y lo que cruza (ticks del cruce). */
    public static final double HUMO_MIN = 4.5, HUMO_MAX = 10.0;
    public static final int ESPERA_HUMO = 20 * 6, HUMO_DESDE = 8, HUMO_HASTA = 15;
    /** La onda del golpe del espadon: a cuanto delante cae y su radio. */
    public static final double ONDA_DELANTE = 2.6, R_ONDA = 2.6;
    static final DustParticleOptions HUMO = new DustParticleOptions(new Vector3f(0.07f, 0.07f, 0.08f), 1.8f);

    private int ultimoHumo = -100000;
    private Vec3 humoDestino;

    /** Para la prueba: despertados, levantados, humos (y los que llegan), ondas que pillan, barridos, desplomados. */
    public static int DESPIERTAN, LEVANTAN, HUMOS, HUMOS_LLEGAN, ONDAS, BARRIDOS, DESPLOMAN;

    public Centinela(EntityType<? extends Centinela> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 72.0).add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 22.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.65)
                .add(Attributes.ARMOR, 12.0).add(Attributes.ARMOR_TOUGHNESS, 2.0);
    }

    @Override
    public String tipo() {
        return variante() == ALABARDA ? "centinela_alabarda" : "centinela_espadon";
    }

    @Override
    protected int[] ticks() {
        return CentinelaAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return CentinelaAnims.GOLPES;
    }

    private boolean alabarda() {
        return variante() == ALABARDA;
    }

    @Override
    protected int accionAturdido() {
        return alabarda() ? CentinelaAnims.ATURDIDO_ALABARDA : CentinelaAnims.ATURDIDO_ESPADON;
    }

    @Override
    protected int accionMorir() {
        return alabarda() ? CentinelaAnims.MORIR_ALABARDA : CentinelaAnims.MORIR_ESPADON;
    }

    @Override
    protected int accionDespertar(int postura) {
        if (postura == TUMBADO) {
            LEVANTAN++;
            return alabarda() ? CentinelaAnims.LEVANTARSE_ALABARDA : CentinelaAnims.LEVANTARSE_ESPADON;
        }
        DESPIERTAN++;
        return alabarda() ? CentinelaAnims.DESPERTAR_ALABARDA : CentinelaAnims.DESPERTAR_ESPADON;
    }

    @Override
    protected double distanciaDeseada() {
        return alabarda() ? 3.2 : 2.4;
    }

    @Override
    protected double prisa() {
        return 0.95;
    }

    @Override
    protected int pausaTras(int accion) {
        return 14 + random.nextInt(18);
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (d > HUMO_MIN && d < HUMO_MAX && tickCount - ultimoHumo > ESPERA_HUMO && random.nextFloat() < 0.6f)
            return alabarda() ? CentinelaAnims.HUMO_ALABARDA : CentinelaAnims.HUMO_ESPADON;
        if (alabarda()) {
            // a quien se le pone al lado o detras, el barrido; de lejos la estocada; si no, el hachazo o la estocada
            Vec3 m = Vec3.directionFromRotation(0, getYRot());
            double dx = presa.getX() - getX(), dz = presa.getZ() - getZ();
            boolean detras = d > 0.5 && (dx * m.x + dz * m.z) / d < 0.2;
            if (d < 3.2 && (detras || random.nextFloat() < 0.3f)) return CentinelaAnims.BARRIDO;
            if (d < 5.0 && d > 3.0) return CentinelaAnims.ESTOCADA;
            if (d < 3.6) return random.nextFloat() < 0.5f ? CentinelaAnims.HACHAZO : CentinelaAnims.ESTOCADA;
            return 0;
        }
        if (d < 3.4) return random.nextFloat() < 0.6f ? CentinelaAnims.TAJO : CentinelaAnims.GOLPE;
        return d < 4.0 && random.nextFloat() < 0.3f ? CentinelaAnims.GOLPE : 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case CentinelaAnims.TAJO -> new Golpe(3.9f, 170f, 1.25f, 0.9f);
            case CentinelaAnims.GOLPE -> new Golpe(3.3f, 60f, 1.6f, 0.6f);
            case CentinelaAnims.ESTOCADA -> new Golpe(5.3f, 32f, 1.3f, 0.6f);
            case CentinelaAnims.BARRIDO -> new Golpe(4.0f, 360f, 0.95f, 1.1f);
            case CentinelaAnims.HACHAZO -> new Golpe(3.7f, 70f, 1.55f, 0.5f);
            case CentinelaAnims.HUMO_ESPADON, CentinelaAnims.HUMO_ALABARDA -> new Golpe(3.4f, 80f, 1.15f, 0.9f);
            default -> null;
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case CentinelaAnims.TAJO -> {
                if (t == 12) empujar(0.3);
                if (t == 16) playSound(DarkCraftMod.SFX_CENTINELA_TAJO.get(), 1.1f, 0.9f + random.nextFloat() * 0.15f);
            }
            case CentinelaAnims.GOLPE -> {
                if (t == 17) playSound(DarkCraftMod.SFX_CENTINELA_TAJO.get(), 1.0f, 0.75f);
                if (t == 20) onda();
            }
            case CentinelaAnims.ESTOCADA -> {
                if (t == 17) empujar(0.5);
                if (t == 17) playSound(DarkCraftMod.SFX_CENTINELA_ALABARDA.get(), 0.9f, 1.2f);
            }
            case CentinelaAnims.BARRIDO -> {
                if (t == 15) {
                    BARRIDOS++;
                    playSound(DarkCraftMod.SFX_CENTINELA_ALABARDA.get(), 1.2f, 0.9f + random.nextFloat() * 0.1f);
                }
            }
            case CentinelaAnims.HACHAZO -> {
                if (t == 19) playSound(DarkCraftMod.SFX_CENTINELA_TAJO.get(), 0.9f, 0.8f);
                if (t == 22) {
                    playSound(DarkCraftMod.SFX_CENTINELA_GOLPE.get(), 1.0f, 1.05f);
                    polvo(Vec3.directionFromRotation(0, getYRot()).scale(3.0));
                }
            }
            case CentinelaAnims.HUMO_ESPADON, CentinelaAnims.HUMO_ALABARDA -> humo(t);
            default -> {
            }
        }
    }

    /**
     * El humo: en el tick {@link #HUMO_DESDE} se deshace (el modelo no se ve: lo esconde el dibujante), hasta {@link #HUMO_HASTA}
     * cruza en linea hacia su presa (para un paso antes) dejando humo, y sale con la estocada (el golpe de la accion).
     */
    private void humo(int t) {
        LivingEntity p = getTarget();
        if (t == HUMO_DESDE) {
            ultimoHumo = tickCount;
            HUMOS++;
            playSound(DarkCraftMod.SFX_CENTINELA_HUMO.get(), 1.3f, 0.9f + random.nextFloat() * 0.15f);
            nube(26);
            humoDestino = null;
            if (p != null) {
                Vec3 a = p.position().subtract(position());
                double l = Math.sqrt(a.x * a.x + a.z * a.z);
                if (l > 0.1) {
                    double llega = Math.max(0, l - 2.0);
                    humoDestino = position().add(a.x / l * llega, 0, a.z / l * llega);
                }
            }
        }
        if (t > HUMO_DESDE && t <= HUMO_HASTA && humoDestino != null) {
            Vec3 falta = humoDestino.subtract(position());
            double l = Math.sqrt(falta.x * falta.x + falta.z * falta.z);
            int quedan = HUMO_HASTA - t + 1;
            double v = Math.min(1.3, l / quedan);
            if (l > 0.05) setDeltaMovement(falta.x / l * v, Math.max(getDeltaMovement().y, -0.2), falta.z / l * v);
            else setDeltaMovement(0, getDeltaMovement().y, 0);
            hasImpulse = true;
            if (p != null) girarHacia(p, 40f);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(HUMO, getX(), getY() + 1.2, getZ(), 6, 0.35, 0.7, 0.35, 0.01);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 3, 0.3, 0.5, 0.3, 0.01);
            }
        }
        if (t == HUMO_HASTA + 1) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            nube(14);
            if (p != null && distanceTo(p) < 3.6) HUMOS_LLEGAN++;
        }
    }

    /** Una bocanada de humo negro alrededor (al deshacerse y al volver). */
    private void nube(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(HUMO, getX(), getY() + 1.3, getZ(), n, 0.45, 0.9, 0.45, 0.02);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2, getZ(), n / 3, 0.4, 0.7, 0.4, 0.02);
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.4, getZ(), 3, 0.3, 0.5, 0.3, 0.01);
    }

    /** El golpe del espadon da en el suelo: la onda alrededor del golpe (a quien este de pie, 0,6 del dano). */
    private void onda() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * ONDA_DELANTE, z = getZ() + d.z * ONDA_DELANTE;
        polvo(d.scale(ONDA_DELANTE));
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, 8, 0.8, 0.1, 0.8, 0.02);
        level().playSound(null, x, getY(), z, DarkCraftMod.SFX_CENTINELA_GOLPE.get(), SoundSource.HOSTILE, 1.4f, 0.9f + random.nextFloat() * 0.1f);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        for (Player p : level().getEntitiesOfClass(Player.class, new AABB(x - 3, getY() - 1, z - 3, x + 3, getY() + 2, z + 3))) {
            if (!objetivoValido(p) || !p.onGround() || p.distanceToSqr(x, p.getY(), z) > R_ONDA * R_ONDA) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                ONDAS++;
                p.knockback(0.45, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.3, 0));
            }
        }
    }

    /** Las esquirlas del suelo donde da el arma. */
    private void polvo(Vec3 off) {
        if (!(level() instanceof ServerLevel sl)) return;
        double x = getX() + off.x, z = getZ() + off.z;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, 26, 0.6, 0.05, 0.6, 0.2);
    }

    // ------------------------------------------------------------------ cada tick: el humo que se le escapa

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide || !isAlive()) return;
        // despierto, el humo le sale por las juntas (el cuello, las sisas); dormido, apenas un hilo
        boolean despierto = postura() == DE_PIE;
        int cada = despierto ? 3 : 14;
        if (postura() == TUMBADO || tickCount % cada != 0) return;
        double y = getY() + 1.95;
        Vec3 m = Vec3.directionFromRotation(0, yBodyRot);
        level().addParticle(ParticleTypes.SMOKE, getX() - m.x * 0.05, y, getZ() - m.z * 0.05, (random.nextFloat() - 0.5f) * 0.01, 0.02, (random.nextFloat() - 0.5f) * 0.01);
        if (despierto && random.nextInt(3) == 0) {
            double lado = random.nextBoolean() ? 0.45 : -0.45;
            level().addParticle(ParticleTypes.LARGE_SMOKE, getX() + m.z * lado, getY() + 1.55, getZ() - m.x * lado, 0, 0.015, 0);
        }
    }

    // ------------------------------------------------------------------ sonidos, morir

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_CENTINELA_ALERTA.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CENTINELA_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CENTINELA_MUERTE.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState st) {
        playSound(DarkCraftMod.SFX_CENTINELA_PASO.get(), 0.45f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        DESPLOMAN++;
        nube(30);
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "centinela");
        }
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        // el humo se le escapa mientras se desploma; al final, el monton se deshace
        if (level() instanceof ServerLevel sl) {
            if (deathTime % 3 == 0) sl.sendParticles(HUMO, getX(), getY() + 0.6, getZ(), 3, 0.5, 0.3, 0.5, 0.02);
            if (deathTime == ticks()[accionMorir()] + 12) sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.4, getZ(), 20, 0.6, 0.3, 0.6, 0.03);
        }
    }

    /** Para las pruebas: lo despierta ya. */
    public void despertar() {
        if (postura() != DE_PIE) {
            int d = accionDespertar(postura());
            postura(DE_PIE);
            empezar(d);
        }
    }

    /** Para las pruebas: hace esa accion ya (mirando a su presa). */
    public void probar(int accion) {
        if (getTarget() != null) girarHacia(getTarget(), 360f);
        empezar(accion);
    }
}
