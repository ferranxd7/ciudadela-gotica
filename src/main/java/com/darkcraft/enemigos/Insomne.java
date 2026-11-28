package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Fuerte;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Los Insomnes del Fuerte de la Marca (parte 7aw): la guarnicion que el castellano dejo de guardia la noche en que llegaron los
 * refugiados y a la que nadie vino a relevar. No duermen: tienen los ojos abiertos de par en par y la cabeza se les cae y la levantan
 * de golpe. Tres, con el tabardo ocre de la Marca y la torre negra:
 * <ul>
 * <li>el <b>centinela</b> (lanza con banderola y el cuerno a la cadera): <b>estocada</b> que llega lejos y <b>barrido</b> a la altura
 * de las rodillas. Si ve a alguien y no lo han tocado ya, se lleva el <b>cuerno</b> a la boca y <b>toca a rebato</b>: si nadie le pega
 * antes de que acabe ({@link #CUERNO_CORTA}), suena la campana del fuerte y toda la guarnicion acude ({@link Fuerte#rebato});</li>
 * <li>el <b>ballestero</b> (ballesta de torno, el paves a la espalda): de lejos dispara un {@link Virote} pesado que <b>el escudo no
 * para</b>, y luego tarda en volver a tensar (<b>recarga</b>: la ballesta de punta al suelo, el pie en el estribo; ahi es cuando hay que
 * llegar a el). De cerca, <b>culatazo</b>. Desde lo alto de las murallas no baja: se gira y tira. Espera sentado contra el muro
 * ({@code TUMBADO});</li>
 * <li>el <b>escudero</b> (bacinete cerrado, escudo de torre y maza de pestanas; algo mas grande, {@link #GRANDE}): <b>de frente no le
 * entra casi nada</b> (el escudo, {@link #ESCUDO}; las flechas, nada) mientras no ataque; por un lado, por la espalda o mientras
 * golpea, todo; un martillo le rompe la guardia. <b>Mazazo</b>, <b>escudazo</b> (empuja y frena; tras parar dos golpes contesta con
 * uno) y <b>carga</b> con el escudo por delante desde lejos. Espera apoyado en el escudo ({@code REZA}).</li>
 * </ul>
 */
public class Insomne extends Enemigo {
    public static final int CENTINELA = 0, BALLESTERO = 1, ESCUDERO = 2;
    /** Lo que se agranda el escudero (el dibujante y la caja). */
    public static final float GRANDE = 1.12f;
    /** Lo que deja pasar el escudo de torre de lo que le llega de frente (las flechas, nada). */
    public static final float ESCUDO = 0.1f;
    /** El cuerno: cada centinela toca como mucho uno por minuto; si le pegan antes de este tick de la accion, no llega a sonar entero. */
    public static final int ESPERA_CUERNO = 20 * 60, CUERNO_CORTA = 30;
    /** La carga del escudero (cada cuanto) y lo que pega el virote (antes de la armadura). */
    public static final int ESPERA_CARGA = 20 * 6;
    public static final float VIROTE = 9f;
    private static final UUID MOD_VARIANTE = UUID.fromString("6a1f3c9e-2b84-4d7a-9e15-8c3f7b2d4e61");
    private static final EntityDataAccessor<Boolean> CARGADA = SynchedEntityData.defineId(Insomne.class, EntityDataSerializers.BOOLEAN);

    private int ultimoCuerno = -100000, ultimaCarga = -100000, bloqueos;
    /** Acaba de reparar en alguien (el centinela toca el cuerno nada mas verle, si puede). */
    private boolean recienVisto;

    /** Para la prueba: cuernos (empezados, cortados, que han sonado enteros), virotes, recargas, golpes parados, guardias rotas, cargas. */
    public static int CUERNOS, CUERNOS_CORTADOS, CUERNOS_ENTEROS, VIROTES, RECARGAS, BLOQUEOS, ROTAS, CARGAS, MAZAZOS, ALZADOS;

    public Insomne(EntityType<? extends Insomne> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 28.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CARGADA, true);
    }

    /** Si el ballestero tiene el virote puesto en la ballesta (el modelo lo ensena). */
    public boolean cargada() {
        return entityData.get(CARGADA);
    }

    void cargar(boolean si) {
        entityData.set(CARGADA, si);
    }

    /** El ballestero ve lejos y aguanta menos; el escudero aguanta mucho, pesa y no se le empuja. */
    @Override
    public void variante(int v) {
        super.variante(v);
        refreshDimensions();
        if (level().isClientSide) return;
        var vida = getAttribute(Attributes.MAX_HEALTH);
        var armadura = getAttribute(Attributes.ARMOR);
        var empuje = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var paso = getAttribute(Attributes.MOVEMENT_SPEED);
        var golpe = getAttribute(Attributes.ATTACK_DAMAGE);
        var vista = getAttribute(Attributes.FOLLOW_RANGE);
        if (vida == null || armadura == null || empuje == null || paso == null || golpe == null || vista == null) return;
        for (var a : new net.minecraft.world.entity.ai.attributes.AttributeInstance[]{vida, armadura, empuje, paso, golpe, vista})
            a.removeModifier(MOD_VARIANTE);
        switch (v) {
            case ESCUDERO -> {
                vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_escudero", 0.6, AttributeModifier.Operation.MULTIPLY_BASE));
                armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_escudero", 6.0, AttributeModifier.Operation.ADDITION));
                empuje.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_escudero", 0.5, AttributeModifier.Operation.ADDITION));
                paso.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_escudero", -0.12, AttributeModifier.Operation.MULTIPLY_BASE));
                golpe.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_escudero", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            case BALLESTERO -> {
                vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_ballestero", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
                armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_ballestero", -2.0, AttributeModifier.Operation.ADDITION));
                vista.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_ballestero", 16.0, AttributeModifier.Operation.ADDITION));
            }
            default -> {
            }
        }
        setHealth(getMaxHealth());
    }

    @Override
    public EntityDimensions getDimensions(Pose p) {
        EntityDimensions d = super.getDimensions(p);
        return variante() == ESCUDERO ? d.scale(GRANDE) : d;
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case BALLESTERO -> "insomne_ballestero";
            case ESCUDERO -> "insomne_escudero";
            default -> "insomne_centinela";
        };
    }

    @Override
    protected int[] ticks() {
        return InsomneAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return InsomneAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return InsomneAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return InsomneAnims.MORIR;
    }

    /** Sentado contra el muro se levanta; de guardia o apoyado en el escudo, se vuelve. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? InsomneAnims.LEVANTARSE : InsomneAnims.VOLVERSE;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    public void setTarget(LivingEntity t) {
        if (t != null && getTarget() == null) recienVisto = true;
        super.setTarget(t);
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        boolean aSuAltura = Math.abs(presa.getY() - getY()) < 2.4;
        boolean nuevo = recienVisto;
        recienVisto = false;
        return switch (variante()) {
            case BALLESTERO -> {
                if (d < 2.5 && aSuAltura && random.nextFloat() < 0.7f) yield InsomneAnims.CULATAZO;
                if (!cargada()) yield InsomneAnims.RECARGA;
                yield d < 36 ? InsomneAnims.DISPARO : 0;
            }
            case ESCUDERO -> {
                if (!aSuAltura) yield 0;
                if (bloqueos >= 2 && d < 2.9) {
                    bloqueos = 0;
                    yield InsomneAnims.ESCUDAZO;
                }
                if (d > 4.5 && d < 10 && tickCount - ultimaCarga > ESPERA_CARGA && random.nextFloat() < 0.4f) yield InsomneAnims.CARGA;
                if (d < 3.2) yield random.nextFloat() < 0.65f ? InsomneAnims.MAZAZO : InsomneAnims.ESCUDAZO;
                yield 0;
            }
            default -> {
                // nada mas ver a alguien (si no estan ya tocando a rebato), el cuerno
                if (nuevo && presa instanceof Player && d > 3.5 && tickCount - ultimoCuerno > ESPERA_CUERNO && !Fuerte.rebatoActivo(level())
                        && random.nextFloat() < 0.8f) yield InsomneAnims.CUERNO;
                if (!aSuAltura || d > 3.9) yield 0;
                if (d < 3.0 && random.nextFloat() < 0.4f) yield InsomneAnims.BARRIDO;
                yield InsomneAnims.ESTOCADA;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case InsomneAnims.ESTOCADA -> new Golpe(3.9f, 36f, 1.2f, 0.6f);
            case InsomneAnims.BARRIDO -> new Golpe(3.4f, 150f, 0.85f, 0.9f);
            case InsomneAnims.CULATAZO -> new Golpe(2.5f, 80f, 0.7f, 1.2f);
            case InsomneAnims.MAZAZO -> new Golpe(3.1f, 70f, 1.45f, 0.8f);
            case InsomneAnims.ESCUDAZO -> new Golpe(2.7f, 100f, 0.55f, 1.8f);
            case InsomneAnims.CARGA -> new Golpe(2.5f, 110f, 1.0f, 2.2f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int g = golpes()[accion].length > 0 ? golpes()[accion][0] : -1;
        switch (accion) {
            case InsomneAnims.ESTOCADA -> {
                if (t == g - 2) empujar(0.5);
            }
            case InsomneAnims.BARRIDO -> {
                if (t == g - 2) empujar(0.2);
            }
            case InsomneAnims.CUERNO -> {
                if (t == 2) {
                    ultimoCuerno = tickCount;
                    CUERNOS++;
                }
                // se lo lleva a la boca y toca (lo oye todo el fuerte)
                if (t == 9 && !level().isClientSide)
                    level().playSound(null, getX(), getEyeY(), getZ(), DarkCraftMod.SFX_CUERNO_REBATO.get(), SoundSource.HOSTILE, 5.0f, 0.95f + random.nextFloat() * 0.1f);
                if (t > 9 && t < CUERNO_CORTA && t % 3 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(-30, getYRot());
                    sl.sendParticles(ParticleTypes.CLOUD, getX() + d.x * 0.8, getEyeY() + 0.3, getZ() + d.z * 0.8, 1, 0.05, 0.05, 0.05, 0.02);
                }
                // ha sonado entero: tocan a rebato
                if (t == CUERNO_CORTA && level() instanceof ServerLevel sl) {
                    CUERNOS_ENTEROS++;
                    Fuerte.rebato(sl, getTarget(), this);
                }
            }
            case InsomneAnims.DISPARO -> {
                if (t == g) disparar();
            }
            case InsomneAnims.RECARGA -> {
                if (t == 3) playSound(DarkCraftMod.SFX_BALLESTA_RECARGA.get(), 1.0f, 0.95f + random.nextFloat() * 0.1f);
                if (t == g) {
                    cargar(true);
                    RECARGAS++;
                }
            }
            case InsomneAnims.CULATAZO -> {
                if (t == g - 2) empujar(0.3);
            }
            case InsomneAnims.MAZAZO -> {
                if (t == g - 3) empujar(0.35);
                if (t == g) impacto(2.4);
            }
            case InsomneAnims.ESCUDAZO -> {
                if (t == g - 3) empujar(0.7);
                if (t == g) playSound(DarkCraftMod.SFX_ESCUDO_TORRE.get(), 1.0f, 0.8f + random.nextFloat() * 0.1f);
            }
            case InsomneAnims.CARGA -> {
                if (t == 2) {
                    ultimaCarga = tickCount;
                    CARGAS++;
                    playSound(DarkCraftMod.SFX_INSOMNE_ALERTA.get(), 1.2f, 0.75f);
                }
                if (t >= 6 && t <= g + 1) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    setDeltaMovement(d.x * 0.55, getDeltaMovement().y, d.z * 0.55);
                    hasImpulse = true;
                    if (t % 3 == 0 && level() instanceof ServerLevel sl) {
                        BlockState suelo = level().getBlockState(blockPosition().below());
                        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), getX(), getY() + 0.1, getZ(), 6, 0.3, 0.05, 0.3, 0.1);
                    }
                }
                if (t == g) playSound(DarkCraftMod.SFX_ESCUDO_TORRE.get(), 1.3f, 0.7f);
            }
            case InsomneAnims.LEVANTARSE, InsomneAnims.VOLVERSE -> {
                if (t == 2) ALZADOS++;
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case InsomneAnims.ESCUDAZO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            case InsomneAnims.CARGA -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
            case InsomneAnims.MAZAZO -> MAZAZOS++;
            default -> {
            }
        }
    }

    /** Suelta el virote hacia donde apunta (el cuerpo ya no gira desde tres ticks antes: se esquiva de lado). */
    private void disparar() {
        if (level().isClientSide) return;
        LivingEntity t = getTarget();
        Virote a = new Virote(level(), this, VIROTE);
        a.setPos(getX(), getEyeY() - 0.25, getZ());
        a.setShotFromCrossbow(true);
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double h = t == null ? 12 : Math.max(1, Math.sqrt(Math.pow(t.getX() - getX(), 2) + Math.pow(t.getZ() - getZ(), 2)));
        Vec3 v = tiro(h, t == null ? 0 : t.getY(0.55) - a.getY(), d, 3.2);
        a.shoot(v.x, v.y, v.z, 3.2f, 1.0f);
        level().addFreshEntity(a);
        VIROTES++;
        cargar(false);
        playSound(DarkCraftMod.SFX_VIROTE_DISPARA.get(), 1.4f, 0.9f + random.nextFloat() * 0.15f);
    }

    /** El mazazo da en el suelo: el suelo que salta. */
    private void impacto(double lejos) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, 16, 0.4, 0.05, 0.4, 0.15);
        playSound(DarkCraftMod.SFX_MAZAZO.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ el cuerno cortado y el escudo de torre

    /** Le pegan mientras se lleva el cuerno a la boca: no llega a tocar (y el cuerno calla para los de alrededor). */
    private void cortarCuerno() {
        CUERNOS_CORTADOS++;
        cortarAccion();
        pausa = Math.max(pausa, 12);
        if (level() instanceof ServerLevel sl) {
            var id = DarkCraftMod.SFX_CUERNO_REBATO.getId();
            for (ServerPlayer p : sl.players())
                if (p.distanceToSqr(this) < 96 * 96) p.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(id, SoundSource.HOSTILE));
        }
    }

    /** Si el escudo de torre le tapa ese golpe: de frente (70 grados a cada lado), sin estar atacando, ni aturdido, ni cayendo. */
    private boolean cubre(DamageSource s) {
        if (variante() != ESCUDERO || postura() != DE_PIE || muriendo() || aturdido()) return false;
        int a = accion();
        if (a != 0 && a != InsomneAnims.BLOQUEA) return false;
        if (s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) || s.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                || s.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return false;
        Vec3 de = s.getSourcePosition();
        if (de == null) return false;
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = de.x - getX(), dz = de.z - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l > 0.34;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.getEntity() instanceof Enemigo && s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) return false;
        if (!level().isClientSide && accion() == InsomneAnims.CUERNO && accionT < CUERNO_CORTA && s.getEntity() instanceof Player) cortarCuerno();
        if (!level().isClientSide && cubre(s)) {
            boolean flecha = s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
            // un martillo le rompe la guardia: el golpe entra entero y se queda aturdido
            if (!flecha && com.darkcraft.armas.Martillo.rompe(s)) {
                ROTAS++;
                level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_GUARDIA_ROTA.get(), SoundSource.HOSTILE, 1.0f, 0.85f + random.nextFloat() * 0.15f);
                boolean ok = super.hurt(s, cuanto);
                if (ok && isAlive()) aturdir();
                return ok;
            }
            BLOQUEOS++;
            bloqueos++;
            level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_ESCUDO_TORRE.get(), SoundSource.HOSTILE, 1.0f, 0.95f + random.nextFloat() * 0.15f);
            if (level() instanceof ServerLevel sl) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                sl.sendParticles(ParticleTypes.CRIT, getX() + d.x * 0.7, getY() + 1.2, getZ() + d.z * 0.7, 8, 0.2, 0.3, 0.2, 0.2);
            }
            if (accion() == 0) empezar(InsomneAnims.BLOQUEA);
            // el que le pega se le pone delante: lo ve
            if (getTarget() == null && s.getEntity() instanceof LivingEntity quien && objetivoValido(quien)) setTarget(quien);
            if (flecha) return false;
            cuanto *= ESCUDO;
        }
        return super.hurt(s, cuanto);
    }

    // ------------------------------------------------------------------ morir: por fin duerme

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "insomne");
        }
    }

    /** Al acabar de caer cierra los ojos: un suspiro y algo de polvo. */
    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (level() instanceof ServerLevel sl && deathTime == InsomneAnims.TICKS[InsomneAnims.MORIR] - 4)
            sl.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.4, getZ(), 8, 0.4, 0.1, 0.4, 0.01);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.07f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == ESCUDERO && random.nextFloat() < 0.1f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
        if (jugador && variante() == BALLESTERO && random.nextFloat() < 0.3f)
            spawnAtLocation(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 2 + random.nextInt(4)));
    }

    // ------------------------------------------------------------------ ritmo y sonidos

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case InsomneAnims.ESTOCADA -> DarkCraftMod.SFX_ESTOCADA.get();
            case InsomneAnims.BARRIDO, InsomneAnims.CULATAZO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            case InsomneAnims.MAZAZO, InsomneAnims.ESCUDAZO, InsomneAnims.CARGA -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_INSOMNE_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case BALLESTERO -> accion == InsomneAnims.DISPARO ? 4 + random.nextInt(6) : 10 + random.nextInt(12);
            case ESCUDERO -> 18 + random.nextInt(16);
            default -> 14 + random.nextInt(16);
        };
    }

    /** El ballestero se queda lejos (en lo alto de las murallas y las torres, quieto: no baja); el centinela, a lo que da la lanza. */
    @Override
    protected double distanciaDeseada() {
        return switch (variante()) {
            case BALLESTERO -> hogar() != null && Fuerte.enLoAlto(hogar()) ? 99.0 : 9.0;
            case ESCUDERO -> 2.0;
            default -> 2.9;
        };
    }

    @Override
    protected double alturaMaxima() {
        return variante() == BALLESTERO ? 18.0 : super.alturaMaxima();
    }

    /** Lo lejos que persigue: tocando a rebato acuden de todo el fuerte (y el ballestero ve de punta a punta). */
    @Override
    protected double correa() {
        if (Fuerte.rebatoActivo(level())) return 80;
        return variante() == BALLESTERO ? 44 : 32;
    }

    @Override
    protected double prisa() {
        return variante() == ESCUDERO ? 0.95 : 1.05;
    }

    @Override
    public float getVoicePitch() {
        float base = variante() == ESCUDERO ? 0.82f : variante() == BALLESTERO ? 1.05f : 0.95f;
        return base + (random.nextFloat() - 0.5f) * 0.1f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DarkCraftMod.SFX_INSOMNE_MURMULLO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_INSOMNE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_INSOMNE_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        if (variante() == ESCUDERO) playSound(DarkCraftMod.SFX_ARMADURA_PASO.get(), 0.32f, 0.8f + random.nextFloat() * 0.2f);
        else playSound(SoundEvents.ZOMBIE_STEP, 0.15f, 1.0f);
    }

    // ------------------------------------------------------------------ para las pruebas

    /** Para las pruebas: que pueda volver a hacerlo todo ya (el cuerno, la carga) y con el virote puesto. */
    public void sinEsperas() {
        ultimoCuerno = ultimaCarga = -100000;
        bloqueos = 0;
        if (!level().isClientSide) cargar(true);
    }

    /** Para las pruebas: que el siguiente que vea le parezca nuevo (el centinela, el cuerno). */
    public void recienVisto() {
        recienVisto = true;
    }

    public String estado() {
        return String.format(java.util.Locale.ROOT, "%s vida %.0f/%.0f, postura %d, accion %d%s", tipo(), getHealth(), getMaxHealth(), postura(), accion(),
                variante() == BALLESTERO ? (cargada() ? ", cargada" : ", sin cargar") : "");
    }
}
