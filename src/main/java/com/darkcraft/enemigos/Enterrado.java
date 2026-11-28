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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Los Enterrados de las Minas de Hierro Negro (parte 7aq): el turno de noche, que estaba abajo cuando el cielo se puso rojo y la
 * montana tembló. Siguen picando. Secos y grises del polvo de hierro, con los ojos blancos de polvo: <b>estan ciegos, oyen</b>
 * ({@link #percibe}: quien corre se oye de lejos, quien anda a media distancia, quien va agachado casi nada; no necesitan verte).
 * Tres:
 * <ul>
 * <li>el <b>picador</b>: el pico a dos manos y el candil en la frente. <b>Picotazo</b> de arriba abajo (si lo paras con el
 * escudo, el pico lo engancha: te rompe la guardia y entra entero) y <b>barrido</b>. Espera picando la roca ({@code REZA}: se le
 * oye antes de verle);</li>
 * <li>el <b>barrenero</b>: la bandolera de cartuchos; de 4,5 a 14 bloques <b>lanza</b> un cartucho ({@link Cartucho}: cae,
 * chisporrotea la mecha y revienta —tambien a los suyos—); de cerca golpea con el (<b>mechazo</b>, quema). Al morir, a veces se
 * le enciende el morral: aparta. Espera agazapado ({@code REZA});</li>
 * <li>el <b>cargador</b>: enorme, el cuevano a la espalda y la almadena. <b>Almadenazo</b> (la onda alcanza a quien este de pie
 * cerca: salta o rueda), <b>barrido</b> muy abierto y <b>embestida</b>. Espera sentado ({@code REZA}).</li>
 * </ul>
 * Cualquiera puede esperar enterrado en los escombros ({@code TUMBADO}). La costra de polvo y roca: <b>las flechas les entran a
 * medias</b> y los martillos, un 30 % mas. Al morir se deshacen en polvo y grava.
 */
public class Enterrado extends Enemigo {
    public static final int PICADOR = 0, BARRENERO = 1, CARGADOR = 2;
    /** Lo que se agranda el cargador (el dibujante y la caja). */
    public static final float GRANDE = 1.3f;
    /** Lo que esperan entre un cartucho y el siguiente, entre dos embestidas (ticks). */
    public static final int ESPERA_CARTUCHO = 20 * 3 + 10, ESPERA_EMBESTIDA = 20 * 5;
    /** Lo que les entra lo que llega volando (las flechas) y lo de mas de los martillos. */
    public static final float FLECHAS = 0.5f, MARTILLOS = 1.3f;
    /** Lo lejos que oyen a quien corre, a quien anda, a quien esta quieto y a quien va agachado (bloques). */
    public static final double OYE_CORRE = 16, OYE_ANDA = 8, OYE_QUIETO = 3.2, OYE_AGACHADO = 2.4;
    /** La onda del almadenazo: radio. */
    public static final double R_ONDA = 3.0;
    private static final UUID MOD_VARIANTE = UUID.fromString("2f6a1c3e-9b7d-4e21-8c5a-0d4f6e8a9b1c");
    static final DustParticleOptions POLVO = new DustParticleOptions(new Vector3f(0.22f, 0.22f, 0.24f), 1.4f),
            POLVO_CLARO = new DustParticleOptions(new Vector3f(0.45f, 0.43f, 0.4f), 1.8f);
    static final BlockParticleOption GRAVA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()),
            PIEDRA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLED_DEEPSLATE.defaultBlockState());

    private int ultimoCartucho = -100000, ultimaEmbestida = -100000;

    /** Para la prueba: guardias rotas por el pico, cartuchos lanzados, ondas que pillan, embestidas, los que se alzan, morrales. */
    public static int GUARDIAS, CARTUCHOS, ONDAS, EMBESTIDAS, ALZADOS, MORRALES, OYEN;

    public Enterrado(EntityType<? extends Enterrado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.ARMOR, 4.0);
    }

    /** El barrenero aguanta menos; el cargador, mucho mas (y pesa: casi no se le empuja y anda despacio). */
    @Override
    public void variante(int v) {
        super.variante(v);
        refreshDimensions();
        if (level().isClientSide) return;
        var vida = getAttribute(Attributes.MAX_HEALTH);
        var armadura = getAttribute(Attributes.ARMOR);
        var empuje = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var paso = getAttribute(Attributes.MOVEMENT_SPEED);
        if (vida == null || armadura == null || empuje == null || paso == null) return;
        vida.removeModifier(MOD_VARIANTE);
        armadura.removeModifier(MOD_VARIANTE);
        empuje.removeModifier(MOD_VARIANTE);
        paso.removeModifier(MOD_VARIANTE);
        if (v == BARRENERO) {
            vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_barrenero", -0.2, AttributeModifier.Operation.MULTIPLY_BASE));
            armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_barrenero", -2.0, AttributeModifier.Operation.ADDITION));
        } else if (v == CARGADOR) {
            vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cargador", 1.3, AttributeModifier.Operation.MULTIPLY_BASE));
            armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cargador", 4.0, AttributeModifier.Operation.ADDITION));
            empuje.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cargador", 0.55, AttributeModifier.Operation.ADDITION));
            paso.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cargador", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        setHealth(getMaxHealth());
    }

    /** El cargador es mas grande (su caja tambien). */
    @Override
    public EntityDimensions getDimensions(Pose p) {
        EntityDimensions d = super.getDimensions(p);
        return variante() == CARGADOR ? d.scale(GRANDE) : d;
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case BARRENERO -> "enterrado_barrenero";
            case CARGADOR -> "enterrado_cargador";
            default -> "enterrado_picador";
        };
    }

    @Override
    protected int[] ticks() {
        return EnterradoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return EnterradoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return EnterradoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return EnterradoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        if (postura == TUMBADO) return EnterradoAnims.ALZARSE;
        return switch (variante()) {
            case PICADOR -> EnterradoAnims.VOLVERSE;
            case CARGADOR -> EnterradoAnims.INCORPORARSE;
            default -> EnterradoAnims.LEVANTARSE;
        };
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case BARRENERO -> {
                if (d > 4.5 && d < 14 && tickCount - ultimoCartucho > ESPERA_CARTUCHO) yield EnterradoAnims.LANZAR;
                yield d < 2.8 ? EnterradoAnims.MECHAZO : 0;
            }
            case CARGADOR -> {
                if (d > 4.0 && d < 8.5 && tickCount - ultimaEmbestida > ESPERA_EMBESTIDA && random.nextFloat() < 0.4f) yield EnterradoAnims.EMBESTIDA;
                if (d < 3.4) yield random.nextFloat() < 0.45f ? EnterradoAnims.ALMADENAZO : EnterradoAnims.BARRIDO_ALMADENA;
                yield d < 3.9 && random.nextFloat() < 0.3f ? EnterradoAnims.ALMADENAZO : 0;
            }
            default -> {
                if (d < 3.1) yield random.nextFloat() < 0.5f ? EnterradoAnims.PICOTAZO : EnterradoAnims.BARRIDO;
                yield d < 3.6 && random.nextFloat() < 0.3f ? EnterradoAnims.PICOTAZO : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case EnterradoAnims.PICOTAZO -> new Golpe(3.4f, 70f, 1.5f, 0.5f);
            case EnterradoAnims.BARRIDO -> new Golpe(3.1f, 130f, 1.0f, 0.7f);
            case EnterradoAnims.MECHAZO -> new Golpe(2.7f, 100f, 0.9f, 0.4f);
            case EnterradoAnims.ALMADENAZO -> new Golpe(3.8f, 60f, 1.7f, 1.2f);
            case EnterradoAnims.BARRIDO_ALMADENA -> new Golpe(3.7f, 150f, 1.15f, 1.5f);
            case EnterradoAnims.EMBESTIDA -> new Golpe(2.5f, 90f, 1.2f, 1.9f);
            default -> null;
        };
    }

    /**
     * El pico engancha el escudo: a quien para el picotazo con un escudo se le baja (5 s sin poder cubrirse, como con el hacha del
     * juego) y el golpe entra entero.
     */
    @Override
    protected boolean pilla(int accion, int n, LivingEntity le) {
        if (accion == EnterradoAnims.PICOTAZO && le instanceof Player p && p.isBlocking()) {
            ItemStack escudo = p.getUseItem();
            if (!escudo.isEmpty()) p.getCooldowns().addCooldown(escudo.getItem(), 100);
            p.stopUsingItem();
            p.level().broadcastEntityEvent(p, (byte) 30);
            level().playSound(null, p.getX(), p.getY() + 1, p.getZ(), DarkCraftMod.SFX_GUARDIA_ROTA.get(), SoundSource.HOSTILE, 1.0f, 0.95f);
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1.1, p.getZ(), 16, 0.3, 0.3, 0.3, 0.3);
            GUARDIAS++;
        }
        return true;
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int g = golpes()[accion].length > 0 ? golpes()[accion][0] : -1;
        switch (accion) {
            case EnterradoAnims.PICOTAZO -> {
                if (t == g - 6) empujar(0.2);
                if (t == g) impacto(2.4, 0.9f);
            }
            case EnterradoAnims.BARRIDO -> {
                if (t == g - 3) empujar(0.25);
            }
            case EnterradoAnims.LANZAR -> {
                if (t == 4) playSound(DarkCraftMod.SFX_ENTERRADO_MECHA.get(), 0.7f, 1.2f);
                if (t == g) lanzarCartucho();
            }
            case EnterradoAnims.MECHAZO -> {
                if (t == 3) empujar(0.3);
            }
            case EnterradoAnims.ALMADENAZO -> {
                if (t == 8) playSound(DarkCraftMod.SFX_ESFUERZO.get(), 1.0f, 0.7f);
                if (t == g) {
                    impacto(2.9, 1.3f);
                    onda();
                }
            }
            case EnterradoAnims.BARRIDO_ALMADENA -> {
                if (t == g - 4) empujar(0.3);
            }
            case EnterradoAnims.EMBESTIDA -> {
                if (t == 4) {
                    ultimaEmbestida = tickCount;
                    EMBESTIDAS++;
                    playSound(DarkCraftMod.SFX_ENTERRADO_ALERTA.get(), 1.1f, 0.7f);
                }
                if (t >= 5 && t <= g + 1) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    setDeltaMovement(d.x * 0.62, getDeltaMovement().y, d.z * 0.62);
                    hasImpulse = true;
                    if (level() instanceof ServerLevel sl && t % 2 == 0) sl.sendParticles(POLVO_CLARO, getX(), getY() + 0.2, getZ(), 4, 0.4, 0.1, 0.4, 0.01);
                }
            }
            case EnterradoAnims.VOLVERSE, EnterradoAnims.LEVANTARSE, EnterradoAnims.INCORPORARSE, EnterradoAnims.ALZARSE -> {
                if (t == 2) despertar(accion == EnterradoAnims.ALZARSE);
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case EnterradoAnims.MECHAZO -> {
                if (!le.fireImmune()) le.setSecondsOnFire(Math.max(2, le.getRemainingFireTicks() / 20));
            }
            case EnterradoAnims.EMBESTIDA -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            case EnterradoAnims.ALMADENAZO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
            default -> {
            }
        }
    }

    /** Donde da el golpe contra el suelo: grava, chispas, polvo, el ruido; y lo que cae del techo (y el temblor de la mina). */
    private void impacto(double lejos, float fuerte) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, (int) (20 * fuerte), 0.4, 0.05, 0.4, 0.15);
        sl.sendParticles(ParticleTypes.CRIT, x, getY() + 0.2, z, 6, 0.2, 0.1, 0.2, 0.2);
        sl.sendParticles(POLVO_CLARO, x, getY() + 0.4, z, (int) (8 * fuerte), 0.5, 0.2, 0.5, 0.02);
        playSound(fuerte > 1f ? DarkCraftMod.SFX_ENTERRADO_ALMADENA.get() : DarkCraftMod.SFX_ENTERRADO_PICA.get(), fuerte > 1f ? 1.5f : 1.0f,
                0.85f + random.nextFloat() * 0.2f);
        if (fuerte > 1f) {
            // el techo suelta polvo y piedrecitas
            for (int k = 0; k < 6; k++)
                sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), getX() + (random.nextDouble() - 0.5) * 6,
                        getY() + 3.5 + random.nextDouble(), getZ() + (random.nextDouble() - 0.5) * 6, 2, 0.2, 0.1, 0.2, 0.0);
            com.darkcraft.morvain.Mina.temblor(sl, x, getY(), z);
        }
    }

    /** La onda del almadenazo: a quien este de pie cerca del golpe (los jugadores y su presa; saltando o rodando se libra). */
    private void onda() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * 2.9, z = getZ() + d.z * 2.9;
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        for (LivingEntity p : level().getEntitiesOfClass(LivingEntity.class, new AABB(x - R_ONDA, getY() - 1, z - R_ONDA, x + R_ONDA, getY() + 2, z + R_ONDA),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (!p.onGround() || p.distanceToSqr(x, p.getY(), z) > R_ONDA * R_ONDA) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                ONDAS++;
                p.knockback(0.5, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.34, 0));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            }
        }
    }

    /** Para las pruebas: lo que hace el pico con quien lo para con el escudo (le rompe la guardia; devuelve si el golpe entra). */
    public boolean engancha(Player p) {
        return pilla(EnterradoAnims.PICOTAZO, 0, p);
    }

    /** Lanza el cartucho en arco a donde esta su presa. */
    private void lanzarCartucho() {
        if (level().isClientSide) return;
        ultimoCartucho = tickCount;
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.6, oz = getZ() + d.z * 0.6, oy = getY() + getBbHeight() + 0.1;
        double tx = t == null ? ox + d.x * 8 : t.getX(), tz = t == null ? oz + d.z * 8 : t.getZ(), ty = t == null ? getY() : t.getY();
        Cartucho c = new Cartucho(level(), this);
        c.setPos(ox, oy, oz);
        c.setDeltaMovement(Cantaro.tiro(ox, oy, oz, tx, ty + 0.1, tz));
        level().addFreshEntity(c);
        CARTUCHOS++;
        playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 0.8f, 0.85f);
    }

    /** Se levanta (dejo de picar, estaba agazapado o sentado) o sale de los escombros: polvo y grava. */
    private void despertar(boolean escombros) {
        if (!(level() instanceof ServerLevel sl)) return;
        ALZADOS++;
        sl.sendParticles(escombros ? GRAVA : PIEDRA, getX(), getY() + 0.5, getZ(), escombros ? 30 : 10, 0.5, 0.4, 0.5, 0.1);
        sl.sendParticles(POLVO_CLARO, getX(), getY() + 0.8, getZ(), escombros ? 24 : 10, 0.6, 0.5, 0.6, 0.02);
        playSound(escombros ? DarkCraftMod.SFX_ENTERRADO_ALZARSE.get() : DarkCraftMod.SFX_ENTERRADO_ALERTA.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ ciegos: oyen

    /**
     * No ven: oyen. Quien corre se oye a {@link #OYE_CORRE} bloques, quien anda a {@link #OYE_ANDA}, quien esta quieto a
     * {@link #OYE_QUIETO} y quien va agachado a {@link #OYE_AGACHADO} (no importa por donde llegue: no tienen espalda). Los que
     * esperan (picando, agazapados, sentados o enterrados) oyen a la mitad; con roca de por medio, solo a quien corre y de mas
     * cerca. El Farol de Corwen (parte 7ao) no les dice nada: estan ciegos.
     */
    @Override
    protected boolean percibe(LivingEntity p) {
        double d2 = distanceToSqr(p);
        if (d2 < 1.8 * 1.8) return true;
        double v = p.getDeltaMovement().horizontalDistance();
        double r = p.isCrouching() ? OYE_AGACHADO : p.isSprinting() || v > 0.2 ? OYE_CORRE : v > 0.03 || !p.onGround() ? OYE_ANDA : OYE_QUIETO;
        if (p instanceof Player pl && pl.swinging) r = Math.max(r, OYE_ANDA * 1.2);
        if (postura() != DE_PIE) r *= 0.5;
        if (!hasLineOfSight(p)) r = p.isSprinting() ? Math.min(r, OYE_ANDA) : Math.min(r, 2.5);
        boolean oye = d2 < r * r;
        if (oye) OYEN++;
        return oye;
    }

    // ------------------------------------------------------------------ la costra: flechas a medias, martillos de mas

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile && !(s.getDirectEntity() instanceof Cartucho)) cuanto *= FLECHAS;
        if (s.getEntity() instanceof Player p && s.getDirectEntity() == p && p.getMainHandItem().getItem() instanceof com.darkcraft.armas.Martillo) cuanto *= MARTILLOS;
        return super.hurt(s, cuanto);
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case EnterradoAnims.BARRIDO, EnterradoAnims.MECHAZO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            case EnterradoAnims.PICOTAZO, EnterradoAnims.ALMADENAZO, EnterradoAnims.BARRIDO_ALMADENA -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_ENTERRADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case CARGADOR -> 22 + random.nextInt(16);
            case BARRENERO -> 14 + random.nextInt(14);
            default -> 14 + random.nextInt(14);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == BARRENERO ? 7.0 : variante() == CARGADOR ? 2.6 : 2.0;
    }

    @Override
    protected double prisa() {
        return variante() == CARGADOR ? 0.95 : variante() == BARRENERO ? 1.05 : 1.0;
    }

    /** El picador lleva su candil (luce), el barrenero la mecha que chisporrotea; todos sueltan polvo; el picador que pica, piedrecitas. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && Math.abs(getBbHeight() - getDimensions(getPose()).height) > 0.01) refreshDimensions();
        if (!level().isClientSide || !isAlive()) return;
        if (tickCount % 9 == 0 && postura() != TUMBADO)
            level().addParticle(POLVO, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 0.3 + random.nextDouble() * 1.4, getZ() + (random.nextDouble() - 0.5) * 0.6, 0, -0.01, 0);
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        if (variante() == BARRENERO && postura() == DE_PIE && accion() == 0 && tickCount % 3 == 0) {
            double x = getX() + d.x * 0.45 - d.z * 0.38, z = getZ() + d.z * 0.45 + d.x * 0.38;
            level().addParticle(ParticleTypes.SMALL_FLAME, x, getY() + 1.45, z, 0, 0.01, 0);
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.SMOKE, x, getY() + 1.55, z, 0, 0.02, 0);
        }
        // el picador picando: en el segundo 1,15 de cada 1,6 da en la roca (el sonido y las piedrecitas, aqui: van a la vez que el dibujo)
        if (variante() == PICADOR && postura() == REZA && accion() == 0 && tickCount % 32 == 23) {
            double x = getX() + d.x * 1.3, z = getZ() + d.z * 1.3;
            level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), x, getY() + 1.6, z, 0, 0.05, 0);
            level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE_IRON_ORE.defaultBlockState()), x, getY() + 1.5, z, 0, 0.05, 0);
            level().addParticle(ParticleTypes.CRIT, x, getY() + 1.6, z, -d.x * 0.1, 0.05, -d.z * 0.1);
            level().playLocalSound(getX(), getY() + 1.5, getZ(), DarkCraftMod.SFX_ENTERRADO_PICA.get(), SoundSource.HOSTILE, 0.9f, 0.9f + random.nextFloat() * 0.2f, false);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO || (postura() == REZA && variante() == PICADOR) ? null : DarkCraftMod.SFX_ENTERRADO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ENTERRADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ENTERRADO_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "enterrado");
        }
        // al barrenero, a veces se le prende el morral: chisporrotea y revienta al poco (aparta)
        if (variante() == BARRENERO && level() instanceof ServerLevel sl && random.nextFloat() < 0.5f) {
            Cartucho c = new Cartucho(level(), this);
            c.morral(getX(), getY() + 0.3, getZ());
            sl.addFreshEntity(c);
            MORRALES++;
        }
    }

    /** Al acabar de morir (de bruces) se deshace en polvo y grava. */
    @Override
    protected void tickDeath() {
        int m = EnterradoAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == EnterradoAnims.TICKS[m] - 4) deshacerse();
        if (deathTime >= EnterradoAnims.TICKS[m]) remove(RemovalReason.KILLED);
    }

    private void deshacerse() {
        if (!(level() instanceof ServerLevel sl)) return;
        float k = variante() == CARGADOR ? 1.6f : 1f;
        sl.sendParticles(GRAVA, getX(), getY() + 0.3, getZ(), (int) (40 * k), 0.5 * k, 0.25, 0.5 * k, 0.15);
        sl.sendParticles(PIEDRA, getX(), getY() + 0.3, getZ(), (int) (16 * k), 0.4 * k, 0.2, 0.4 * k, 0.12);
        sl.sendParticles(POLVO_CLARO, getX(), getY() + 0.5, getZ(), (int) (30 * k), 0.6 * k, 0.4, 0.6 * k, 0.02);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_ENTERRADO_MUERTE.get(), SoundSource.HOSTILE, 1.1f, (variante() == CARGADOR ? 0.75f : 0.9f)
                + random.nextFloat() * 0.2f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == CARGADOR && random.nextFloat() < 0.08f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }
}
